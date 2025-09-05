package br.com.arturbarth.siaextrator.service;

import br.com.arturbarth.siaextrator.dto.QueryExecutionRequestDTO;
import br.com.arturbarth.siaextrator.dto.QueryExecutionResponseDTO;
import br.com.arturbarth.siaextrator.entity.Cluster;
import br.com.arturbarth.siaextrator.entity.QueryExecution;
import br.com.arturbarth.siaextrator.entity.QueryExecutionStatus;
import br.com.arturbarth.siaextrator.mapper.QueryExecutionMapper;
import br.com.arturbarth.siaextrator.repository.ClusterRepository;
import br.com.arturbarth.siaextrator.repository.DatabaseInstanceRepository;
import br.com.arturbarth.siaextrator.repository.QueryExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class QueryOrchestratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryOrchestratorService.class);
    
    @Autowired
    private QueryExecutionRepository queryExecutionRepository;
    
    @Autowired
    private ClusterRepository clusterRepository;
    
    @Autowired
    private DatabaseInstanceRepository databaseInstanceRepository;
    
    @Autowired
    private QueryExecutorService queryExecutorService;
    
    @Autowired
    private QueryExecutionMapper queryExecutionMapper;
    
    public QueryExecutionResponseDTO submitQuery(QueryExecutionRequestDTO requestDTO) {
        logger.info("Recebida nova solicitação de execução de query do usuário: {}", requestDTO.getUserId());
        
        // 1. Validar a consulta
        validateQuery(requestDTO);
        
        // 2. Criar registro de execução
        String executionId = generateExecutionId();
        QueryExecution queryExecution = createQueryExecution(requestDTO, executionId);
        
        // 3. Enriquecer com metadados
        enrichWithMetadata(queryExecution, requestDTO);
        
        // 4. Salvar no banco
        queryExecution = queryExecutionRepository.save(queryExecution);
        
        // 5. Iniciar execução assíncrona
        logger.info("Iniciando execução assíncrona para query: {}", executionId);
        queryExecutorService.executeQueryAsync(queryExecution);
        
        return queryExecutionMapper.toResponseDTO(queryExecution);
    }
    
    public QueryExecutionResponseDTO getExecutionStatus(String executionId) {
        logger.debug("Consultando status da execução: {}", executionId);
        
        QueryExecution queryExecution = queryExecutionRepository.findByExecutionIdWithResults(executionId)
                .orElseThrow(() -> new RuntimeException("Execução não encontrada: " + executionId));
        
        return queryExecutionMapper.toResponseDTO(queryExecution);
    }
    
    public List<QueryExecutionResponseDTO> getUserExecutions(String userId) {
        logger.debug("Consultando execuções do usuário: {}", userId);
        
        List<QueryExecution> executions = queryExecutionRepository.findByUserId(userId);
        return executions.stream()
                .map(queryExecutionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    private void validateQuery(QueryExecutionRequestDTO requestDTO) {
        logger.debug("Validando consulta SQL e parâmetros");
        
        // Validação básica do SQL
        String sql = requestDTO.getSqlQuery().trim().toLowerCase();
        
        // Verificar se é uma consulta SELECT
        if (!sql.startsWith("select")) {
            throw new IllegalArgumentException("Apenas consultas SELECT são permitidas");
        }
        
        // Verificar comandos perigosos
        String[] dangerousCommands = {"drop", "delete", "insert", "update", "truncate", "alter", "create"};
        for (String cmd : dangerousCommands) {
            if (sql.contains(cmd)) {
                throw new IllegalArgumentException("Comando SQL não permitido: " + cmd);
            }
        }
        
        // Validar clusters
        List<Long> clusterIds = requestDTO.getClusterIds();
        List<Cluster> clusters = clusterRepository.findAllById(clusterIds);
        
        if (clusters.size() != clusterIds.size()) {
            throw new IllegalArgumentException("Um ou mais clusters não foram encontrados");
        }
        
        // Verificar se clusters estão ativos
        List<Cluster> inactiveClusters = clusters.stream()
                .filter(cluster -> !Boolean.TRUE.equals(cluster.getConnectionActive()))
                .collect(Collectors.toList());
        
        if (!inactiveClusters.isEmpty()) {
            logger.warn("Clusters inativos detectados: {}",
                    inactiveClusters.stream().map(Cluster::getAlias).collect(Collectors.toList()));
        }
        
        logger.info("Validação concluída com sucesso para {} clusters", clusters.size());
    }
    
    private QueryExecution createQueryExecution(QueryExecutionRequestDTO requestDTO, String executionId) {
        QueryExecution queryExecution = new QueryExecution(
                requestDTO.getSqlQuery(),
                executionId,
                requestDTO.getUserId()
        );
        
        queryExecution.setUserEmail(requestDTO.getUserEmail());
        queryExecution.setTotalClusters(requestDTO.getClusterIds().size());
        queryExecution.setStatus(QueryExecutionStatus.PENDING);
        
        return queryExecution;
    }
    
    private void enrichWithMetadata(QueryExecution queryExecution, QueryExecutionRequestDTO requestDTO) {
        logger.debug("Enriquecendo execução com metadados");
        
        // Criar metadados em formato JSON
        StringBuilder metadata = new StringBuilder();
        metadata.append("{");
        metadata.append("\"clusterIds\":").append(requestDTO.getClusterIds());
        
        if (requestDTO.getDatabaseNames() != null && !requestDTO.getDatabaseNames().isEmpty()) {
            metadata.append(",\"databaseFilter\":").append(requestDTO.getDatabaseNames());
        }
        
        if (requestDTO.getDescription() != null) {
            metadata.append(",\"description\":\"").append(requestDTO.getDescription()).append("\"");
        }
        
        metadata.append(",\"sqlLength\":").append(requestDTO.getSqlQuery().length());
        metadata.append(",\"submittedAt\":\"").append(java.time.LocalDateTime.now()).append("\"");
        metadata.append("}");
        
        queryExecution.setMetadata(metadata.toString());
        
        logger.debug("Metadados adicionados à execução: {}", queryExecution.getExecutionId());
    }
    
    private String generateExecutionId() {
        return "exec_" + UUID.randomUUID().toString().replace("-", "");
    }
}
