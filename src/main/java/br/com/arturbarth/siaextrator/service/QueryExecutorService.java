package br.com.arturbarth.siaextrator.service;

import br.com.arturbarth.siaextrator.dto.RdsExecutionResult;
import br.com.arturbarth.siaextrator.entity.*;
import br.com.arturbarth.siaextrator.repository.ClusterRepository;
import br.com.arturbarth.siaextrator.repository.DatabaseInstanceRepository;
import br.com.arturbarth.siaextrator.repository.QueryExecutionRepository;
import br.com.arturbarth.siaextrator.repository.QueryExecutionResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class QueryExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutorService.class);
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Autowired
    private QueryExecutionRepository queryExecutionRepository;
    
    @Autowired
    private QueryExecutionResultRepository queryExecutionResultRepository;
    
    @Autowired
    private ClusterRepository clusterRepository;
    
    @Autowired
    private DatabaseInstanceRepository databaseInstanceRepository;
    
    @Autowired
    private RdsExecutorService rdsExecutorService;
    
    @Autowired
    private CsvService csvService;
    
    
    @Async("taskExecutor")
    public CompletableFuture<Void> executeQueryAsync(QueryExecution queryExecution) {
        logger.info("Iniciando execução assíncrona para query: {}", queryExecution.getExecutionId());
        
        return CompletableFuture.runAsync(() -> {
            try {
                executeQuery(queryExecution);
            } catch (Exception e) {
                logger.error("Erro na execução da query {}: {}", queryExecution.getExecutionId(), e.getMessage(), e);
                handleExecutionFailure(queryExecution, e);
            }
        }, executorService);
    }
    
    @Transactional
    public void executeQuery(QueryExecution queryExecution) {
        logger.info("Executando query: {}", queryExecution.getExecutionId());
        
        try {
            // Marcar como iniciada
            queryExecution.markAsStarted();
            queryExecutionRepository.save(queryExecution);
            
            // Enviar notificação de início
//            if (slackNotificationService != null) {
//                slackNotificationService.notifyQueryStarted(queryExecution);
//            }
            
            // Obter clusters e bancos para execução
            List<QueryExecutionResult> executionResults = prepareExecutionResults(queryExecution);
            
            if (executionResults.isEmpty()) {
                throw new RuntimeException("Nenhum banco de dados encontrado para execução");
            }
            
            logger.info("Preparados {} resultados de execução para query {}",
                    executionResults.size(), queryExecution.getExecutionId());
            
            // Executar em cada cluster/banco
            long totalRows = 0;
            for (QueryExecutionResult result : executionResults) {
                try {
                    logger.debug("Executando no cluster {} - banco {}",
                            result.getCluster().getAlias(), result.getDatabaseName());
                    
                    long startTime = System.currentTimeMillis();
                    
                    // Executar query via RDS Executor Service
                    RdsExecutionResult rdsResult = rdsExecutorService.executeQuery(
                            result.getCluster(),
                            result.getDatabaseName(),
                            queryExecution.getSqlQuery()
                    );
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    
                    // Atualizar resultado
                    result.setStatus(QueryExecutionStatus.COMPLETED);
                    result.setRowsAffected(rdsResult.getRowCount());
                    result.setExecutionTimeMs(executionTime);
                    
                    // Salvar dados no CSV via CSV Service
                    if (rdsResult.getData() != null && !rdsResult.getData().isEmpty()) {
                        String csvPath = csvService.saveResultToCsv(
                                queryExecution.getExecutionId(),
                                result.getCluster().getAlias(),
                                result.getDatabaseName(),
                                rdsResult.getData()
                        );
                        result.setResultFilePath(csvPath);
                    }
                    
                    totalRows += rdsResult.getRowCount();
                    queryExecution.incrementCompletedClusters();
                    
                    logger.debug("Execução concluída com sucesso: {} linhas processadas em {}ms",
                            rdsResult.getRowCount(), executionTime);
                    
                } catch (Exception e) {
                    logger.error("Erro na execução no cluster {} - banco {}: {}",
                            result.getCluster().getAlias(), result.getDatabaseName(), e.getMessage());
                    
                    result.setStatus(QueryExecutionStatus.FAILED);
                    result.setErrorMessage(e.getMessage());
                    result.setExecutionTimeMs(System.currentTimeMillis() -
                            (queryExecution.getStartedAt() != null ?
                                    queryExecution.getStartedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() :
                                    System.currentTimeMillis()));
                    queryExecution.incrementFailedClusters();
                }
                
                // Salvar resultado individual
                queryExecutionResultRepository.save(result);
                
                // Atualizar execução principal
                queryExecution.setTotalRows(totalRows);
                queryExecutionRepository.save(queryExecution);
            }
            
            // Consolidar resultados finais se houver dados
            if (totalRows > 0) {
                try {
                    String consolidatedCsvPath = csvService.consolidateResults(queryExecution.getExecutionId());
                    queryExecution.setS3ResultPath(consolidatedCsvPath);
                } catch (Exception e) {
                    logger.warn("Erro ao consolidar resultados CSV para {}: {}",
                            queryExecution.getExecutionId(), e.getMessage());
                }
            }
            
            // Marcar como concluída
            queryExecution.markAsCompleted();
            
            // Enviar notificação de conclusão
//            if (slackNotificationService != null) {
//                slackNotificationService.notifyQueryCompleted(queryExecution);
//            }
            
            logger.info("Query {} executada com sucesso: {} linhas, {} clusters concluídos, {} falharam",
                    queryExecution.getExecutionId(), totalRows,
                    queryExecution.getCompletedClusters(), queryExecution.getFailedClusters());
            
        } catch (Exception e) {
            logger.error("Erro geral na execução da query {}: {}", queryExecution.getExecutionId(), e.getMessage(), e);
            queryExecution.markAsFailed(e.getMessage());
            
            // Enviar notificação de falha
//            if (slackNotificationService != null) {
//                slackNotificationService.notifyQueryFailed(queryExecution);
//            }
            
            throw e;
        } finally {
            queryExecutionRepository.save(queryExecution);
        }
    }
    
    private List<QueryExecutionResult> prepareExecutionResults(QueryExecution queryExecution) {
        logger.debug("Preparando resultados de execução para query: {}", queryExecution.getExecutionId());
        
        // Extrair cluster IDs dos metadados
        String metadata = queryExecution.getMetadata();
        List<Long> clusterIds = extractClusterIdsFromMetadata(metadata);
        
        // Extrair filtro de databases se especificado
        List<String> databaseFilter = extractDatabaseFilterFromMetadata(metadata);
        
        List<QueryExecutionResult> results = new ArrayList<>();
        
        for (Long clusterId : clusterIds) {
            Cluster cluster = clusterRepository.findById(clusterId)
                    .orElseThrow(() -> new RuntimeException("Cluster não encontrado: " + clusterId));
            
            // Verificar se cluster está ativo
            if (!Boolean.TRUE.equals(cluster.getConnectionActive())) {
                logger.warn("Cluster {} está inativo, tentando descobrir bancos novamente", cluster.getAlias());
                
                try {
                    // Tentar descobrir bancos novamente
                    List<String> discoveredDatabases = discoverDatabasesForCluster(cluster);
                    if (discoveredDatabases.isEmpty()) {
                        logger.error("Não foi possível descobrir bancos para o cluster inativo: {}", cluster.getAlias());
                        continue;
                    }
                } catch (Exception e) {
                    logger.error("Falha ao redescobrir bancos para cluster {}: {}", cluster.getAlias(), e.getMessage());
                    continue;
                }
            }
            
            // Obter bancos do cluster
            List<DatabaseInstance> databases = databaseInstanceRepository.findByClusterId(clusterId);
            
            if (databases.isEmpty()) {
                logger.warn("Nenhum banco encontrado para o cluster: {}", cluster.getAlias());
                continue;
            }
            
            // Aplicar filtro de databases se especificado
            if (databaseFilter != null && !databaseFilter.isEmpty()) {
                databases = databases.stream()
                        .filter(db -> databaseFilter.contains(db.getDatabaseName()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            // Criar resultado para cada banco
            for (DatabaseInstance database : databases) {
                // Verificar se banco está acessível
                if (!Boolean.TRUE.equals(database.getIsAccessible())) {
                    logger.debug("Pulando banco inacessível: {} no cluster {}",
                            database.getDatabaseName(), cluster.getAlias());
                    continue;
                }
                
                QueryExecutionResult result = new QueryExecutionResult(
                        queryExecution, cluster, database.getDatabaseName()
                );
                results.add(result);
                
                logger.debug("Adicionado para execução: cluster={}, database={}",
                        cluster.getAlias(), database.getDatabaseName());
            }
        }
        
        logger.debug("Preparados {} resultados de execução", results.size());
        return results;
    }
    
    private List<Long> extractClusterIdsFromMetadata(String metadata) {
        // Implementação simplificada - em produção usar biblioteca JSON (Jackson/Gson)
        try {
            if (metadata == null || metadata.trim().isEmpty()) {
                throw new RuntimeException("Metadados não encontrados na execução");
            }
            
            // Procurar por "clusterIds":[1,2,3]
            int startIndex = metadata.indexOf("\"clusterIds\":");
            if (startIndex == -1) {
                throw new RuntimeException("ClusterIds não encontrados nos metadados");
            }
            
            startIndex = metadata.indexOf("[", startIndex);
            int endIndex = metadata.indexOf("]", startIndex);
            
            if (startIndex == -1 || endIndex == -1) {
                throw new RuntimeException("Formato inválido de clusterIds nos metadados");
            }
            
            String clusterIdsStr = metadata.substring(startIndex + 1, endIndex);
            
            if (clusterIdsStr.trim().isEmpty()) {
                throw new RuntimeException("Nenhum clusterId especificado nos metadados");
            }
            
            return java.util.Arrays.stream(clusterIdsStr.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(java.util.stream.Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Erro ao extrair cluster IDs dos metadados: {}", e.getMessage());
            throw new RuntimeException("Erro ao processar metadados da execução: " + e.getMessage(), e);
        }
    }
    
    private List<String> extractDatabaseFilterFromMetadata(String metadata) {
        try {
            if (metadata == null || !metadata.contains("\"databaseFilter\"")) {
                return null; // Sem filtro, executar em todos os bancos
            }
            
            // Procurar por "databaseFilter":["db1","db2"]
            int startIndex = metadata.indexOf("\"databaseFilter\":");
            if (startIndex == -1) {
                return null;
            }
            
            startIndex = metadata.indexOf("[", startIndex);
            int endIndex = metadata.indexOf("]", startIndex);
            
            if (startIndex == -1 || endIndex == -1) {
                return null;
            }
            
            String databaseFilterStr = metadata.substring(startIndex + 1, endIndex);
            
            if (databaseFilterStr.trim().isEmpty()) {
                return null;
            }
            
            return java.util.Arrays.stream(databaseFilterStr.split(","))
                    .map(String::trim)
                    .map(s -> s.replaceAll("\"", ""))
                    .collect(java.util.stream.Collectors.toList());
            
        } catch (Exception e) {
            logger.warn("Erro ao extrair filtro de databases dos metadados: {}", e.getMessage());
            return null; // Em caso de erro, não aplicar filtro
        }
    }
    
    private List<String> discoverDatabasesForCluster(Cluster cluster) {
        try {
            // Usar o mesmo serviço que o ClusterService usa
            List<String> databases = new ArrayList<>();
            
            String jdbcUrl = String.format("jdbc:postgresql://%s:%d/postgres", cluster.getHost(), cluster.getPort());
            
            try (java.sql.Connection connection = java.sql.DriverManager.getConnection(
                    jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
                
                String query = "SELECT datname FROM pg_database WHERE datistemplate = false AND datallowconn = true";
                
                try (java.sql.PreparedStatement statement = connection.prepareStatement(query);
                     java.sql.ResultSet resultSet = statement.executeQuery()) {
                    
                    while (resultSet.next()) {
                        databases.add(resultSet.getString("datname"));
                    }
                }
            }
            
            return databases;
            
        } catch (Exception e) {
            logger.error("Erro ao descobrir bancos para cluster {}: {}", cluster.getAlias(), e.getMessage());
            throw new RuntimeException("Falha na descoberta de bancos: " + e.getMessage(), e);
        }
    }
    
    private void handleExecutionFailure(QueryExecution queryExecution, Exception e) {
        try {
            queryExecution.markAsFailed(e.getMessage());
            queryExecutionRepository.save(queryExecution);
            
            // Enviar notificação de falha
//            if (slackNotificationService != null) {
//                slackNotificationService.notifyQueryFailed(queryExecution);
//            }
            
        } catch (Exception saveException) {
            logger.error("Erro ao salvar falha da execução: {}", saveException.getMessage());
        }
    }
    
    public boolean cancelExecution(String executionId) {
        logger.info("Solicitação de cancelamento para execução: {}", executionId);
        
        try {
            QueryExecution queryExecution = queryExecutionRepository.findByExecutionId(executionId)
                    .orElseThrow(() -> new RuntimeException("Execução não encontrada: " + executionId));
            
            if (queryExecution.getStatus() == QueryExecutionStatus.COMPLETED ||
                    queryExecution.getStatus() == QueryExecutionStatus.FAILED ||
                    queryExecution.getStatus() == QueryExecutionStatus.CANCELLED) {
                logger.warn("Tentativa de cancelar execução que já finalizou: {} - Status: {}",
                        executionId, queryExecution.getStatus());
                return false;
            }
            
            queryExecution.setStatus(QueryExecutionStatus.CANCELLED);
            queryExecution.setErrorMessage("Execução cancelada pelo usuário");
            queryExecution.setCompletedAt(java.time.LocalDateTime.now());
            
            queryExecutionRepository.save(queryExecution);
            
            logger.info("Execução {} marcada como cancelada", executionId);
            return true;
            
        } catch (Exception e) {
            logger.error("Erro ao cancelar execução {}: {}", executionId, e.getMessage());
            return false;
        }
    }
    
    public List<QueryExecution> getRunningExecutions() {
        return queryExecutionRepository.findByStatus(QueryExecutionStatus.RUNNING);
    }
    
    public List<QueryExecution> getPendingExecutions() {
        return queryExecutionRepository.findByStatus(QueryExecutionStatus.PENDING);
    }
    
    public void shutdown() {
        logger.info("Encerrando QueryExecutorService...");
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                logger.warn("Timeout no encerramento do executor, forçando parada");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("QueryExecutorService encerrado");
    }
    
    // Método para ser chamado no @PreDestroy da aplicação
    @jakarta.annotation.PreDestroy
    public void onDestroy() {
        shutdown();
    }
}