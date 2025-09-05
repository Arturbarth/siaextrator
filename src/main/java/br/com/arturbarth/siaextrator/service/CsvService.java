package br.com.arturbarth.siaextrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CsvService {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvService.class);
    
    @Value("${app.csv.storage.path:/tmp/query-results}")
    private String csvStoragePath;
    
    @Value("${app.csv.delimiter:,}")
    private String csvDelimiter;
    
    @Value("${app.csv.encoding:UTF-8}")
    private String csvEncoding;
    
    @Value("${app.csv.include.header:true}")
    private boolean includeHeader;
    
    public String saveResultToCsv(String executionId, String clusterAlias,
                                  String databaseName, List<Map<String, Object>> data) {
        
        logger.info("Salvando resultado em CSV para execução {} - cluster {} - banco {}",
                executionId, clusterAlias, databaseName);
        
        if (data == null || data.isEmpty()) {
            logger.info("Nenhum dado para salvar no CSV");
            return null;
        }
        
        // Criar estrutura de diretórios
        String directoryPath = String.format("%s/%s", csvStoragePath, executionId);
        createDirectoryIfNotExists(directoryPath);
        
        // Nome do arquivo
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s_%s.csv",
                sanitizeFileName(clusterAlias),
                sanitizeFileName(databaseName),
                timestamp);
        String filePath = String.format("%s/%s", directoryPath, fileName);
        
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), csvEncoding))) {
            
            // Escrever cabeçalho
            if (includeHeader && !data.isEmpty()) {
                Map<String, Object> firstRow = data.get(0);
                String header = String.join(csvDelimiter, firstRow.keySet());
                writer.println(header);
            }
            
            // Escrever dados
            long writtenRows = 0;
            for (Map<String, Object> row : data) {
                String csvRow = row.values().stream()
                        .map(this::formatCsvValue)
                        .collect(Collectors.joining(csvDelimiter));
                writer.println(csvRow);
                writtenRows++;
                
                // Log de progresso a cada 10k linhas
                if (writtenRows % 10000 == 0) {
                    logger.debug("Escritas {} linhas no CSV...", writtenRows);
                }
            }
            
            logger.info("CSV salvo com sucesso: {} ({} linhas)", fileName, writtenRows);
            return filePath;
            
        } catch (IOException e) {
            logger.error("Erro ao salvar CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar resultado em CSV: " + e.getMessage(), e);
        }
    }
    
    public String consolidateResults(String executionId) {
        logger.info("Consolidando resultados CSV para execução: {}", executionId);
        
        String executionDirectory = String.format("%s/%s", csvStoragePath, executionId);
        String consolidatedFileName = String.format("%s_consolidated.csv", executionId);
        String consolidatedFilePath = String.format("%s/%s", executionDirectory, consolidatedFileName);
        
        try {
            // Listar todos os arquivos CSV da execução
            Path executionPath = Paths.get(executionDirectory);
            if (!Files.exists(executionPath)) {
                logger.warn("Diretório de execução não encontrado: {}", executionDirectory);
                return null;
            }
            
            List<Path> csvFiles;
            try (Stream<Path> paths = Files.list(executionPath)) {
                csvFiles = paths
                        .filter(path -> path.toString().endsWith(".csv"))
                        .filter(path -> !path.toString().contains("consolidated"))
                        .sorted()
                        .collect(Collectors.toList());
            }
            
            if (csvFiles.isEmpty()) {
                logger.warn("Nenhum arquivo CSV encontrado para consolidar");
                return null;
            }
            
            logger.info("Consolidando {} arquivos CSV", csvFiles.size());
            
            // Consolidar arquivos
            try (PrintWriter consolidatedWriter = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(consolidatedFilePath), csvEncoding))) {
                
                boolean headerWritten = false;
                long totalLines = 0;
                
                for (Path csvFile : csvFiles) {
                    logger.debug("Processando arquivo: {}", csvFile.getFileName());
                    
                    try (BufferedReader reader = Files.newBufferedReader(csvFile)) {
                        String line;
                        boolean isFirstLine = true;
                        long fileLines = 0;
                        
                        while ((line = reader.readLine()) != null) {
                            if (isFirstLine && includeHeader) {
                                // Escrever cabeçalho apenas uma vez
                                if (!headerWritten) {
                                    // Adicionar colunas de metadados
                                    consolidatedWriter.println("cluster" + csvDelimiter +
                                            "database" + csvDelimiter +
                                            "source_file" + csvDelimiter +
                                            line);
                                    headerWritten = true;
                                }
                                isFirstLine = false;
                            } else {
                                // Extrair cluster e database do nome do arquivo
                                String fileName = csvFile.getFileName().toString();
                                String[] fileParts = extractClusterAndDatabaseFromFileName(fileName);
                                String cluster = fileParts[0];
                                String database = fileParts[1];
                                
                                // Escrever linha com metadados
                                consolidatedWriter.println(cluster + csvDelimiter +
                                        database + csvDelimiter +
                                        fileName + csvDelimiter +
                                        line);
                                totalLines++;
                                fileLines++;
                                
                                if (!isFirstLine) {
                                    isFirstLine = false;
                                }
                            }
                        }
                        
                        logger.debug("Arquivo {} processado: {} linhas",
                                csvFile.getFileName(), fileLines);
                    }
                }
                
                logger.info("Arquivo consolidado criado: {} ({} linhas totais)",
                        consolidatedFileName, totalLines);
                return consolidatedFilePath;
                
            }
            
        } catch (IOException e) {
            logger.error("Erro ao consolidar resultados CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao consolidar resultados CSV: " + e.getMessage(), e);
        }
    }
    
    public byte[] downloadCsvFile(String filePath) {
        logger.debug("Fazendo download do arquivo CSV: {}", filePath);
        
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("Arquivo não encontrado: " + filePath);
            }
            
            // Verificar tamanho do arquivo
            long fileSize = Files.size(path);
            logger.debug("Tamanho do arquivo: {} bytes", fileSize);
            
            // Limite de 100MB para download
            if (fileSize > 100 * 1024 * 1024) {
                throw new RuntimeException("Arquivo muito grande para download: " +
                        formatBytes(fileSize) + " (limite: 100MB)");
            }
            
            return Files.readAllBytes(path);
            
        } catch (IOException e) {
            logger.error("Erro ao fazer download do arquivo CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Falha no download do arquivo CSV: " + e.getMessage(), e);
        }
    }
    
    public CsvFileInfo getCsvFileInfo(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return null;
            }
            
            CsvFileInfo info = new CsvFileInfo();
            info.setFilePath(filePath);
            info.setFileName(path.getFileName().toString());
            info.setFileSize(Files.size(path));
            info.setLastModified(Files.getLastModifiedTime(path).toInstant().toEpochMilli());
            
            // Contar linhas
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                long lineCount = reader.lines().count();
                info.setLineCount(lineCount);
                
                // Subtrair cabeçalho se existir
                if (includeHeader && lineCount > 0) {
                    info.setDataLineCount(lineCount - 1);
                } else {
                    info.setDataLineCount(lineCount);
                }
            }
            
            return info;
            
        } catch (IOException e) {
            logger.error("Erro ao obter informações do arquivo CSV: {}", e.getMessage());
            return null;
        }
    }
    
    public void cleanupOldFiles(int daysToKeep) {
        logger.info("Iniciando limpeza de arquivos antigos (manter {} dias)", daysToKeep);
        
        try {
            Path storagePath = Paths.get(csvStoragePath);
            if (!Files.exists(storagePath)) {
                logger.debug("Diretório de armazenamento não existe: {}", csvStoragePath);
                return;
            }
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            long cutoffEpoch = cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            
            try (Stream<Path> paths = Files.walk(storagePath)) {
                List<Path> oldDirectories = paths
                        .filter(Files::isDirectory)
                        .filter(path -> !path.equals(storagePath))
                        .filter(directory -> {
                            try {
                                return Files.getLastModifiedTime(directory).toMillis() < cutoffEpoch;
                            } catch (IOException e) {
                                logger.warn("Erro ao verificar data do diretório {}: {}", directory, e.getMessage());
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
                
                int deletedDirectories = 0;
                long deletedBytes = 0;
                
                for (Path directory : oldDirectories) {
                    try {
                        long dirSize = calculateDirectorySize(directory);
                        deleteDirectoryRecursively(directory);
                        deletedDirectories++;
                        deletedBytes += dirSize;
                        logger.debug("Diretório removido: {} ({})", directory.getFileName(), formatBytes(dirSize));
                    } catch (IOException e) {
                        logger.warn("Erro ao remover diretório {}: {}", directory, e.getMessage());
                    }
                }
                
                logger.info("Limpeza concluída: {} diretórios removidos, {} liberados",
                        deletedDirectories, formatBytes(deletedBytes));
            }
            
        } catch (IOException e) {
            logger.error("Erro na limpeza de arquivos antigos: {}", e.getMessage(), e);
        }
    }
    
    public List<String> listExecutionFiles(String executionId) {
        String executionDirectory = String.format("%s/%s", csvStoragePath, executionId);
        Path executionPath = Paths.get(executionDirectory);
        
        if (!Files.exists(executionPath)) {
            return java.util.Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.list(executionPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .map(path -> path.toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Erro ao listar arquivos da execução {}: {}", executionId, e.getMessage());
            return java.util.Collections.emptyList();
        }
    }
    
    private void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.debug("Diretório criado: {}", directoryPath);
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório {}: {}", directoryPath, e.getMessage());
            throw new RuntimeException("Falha ao criar diretório de armazenamento: " + e.getMessage(), e);
        }
    }
    
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted((path1, path2) -> path2.compareTo(path1)) // Reverso para deletar arquivos primeiro
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warn("Erro ao deletar {}: {}", path, e.getMessage());
                        }
                    });
        }
    }
    
    private long calculateDirectorySize(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0L;
        }
    }
    
    private String formatCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        
        String stringValue = value.toString();
        
        // Escapar aspas duplas
        stringValue = stringValue.replace("\"", "\"\"");
        
        // Adicionar aspas se contém delimitador, quebra de linha ou aspas
        if (stringValue.contains(csvDelimiter) ||
                stringValue.contains("\n") ||
                stringValue.contains("\r") ||
                stringValue.contains("\"")) {
            stringValue = "\"" + stringValue + "\"";
        }
        
        return stringValue;
    }
    
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        
        // Remover caracteres não permitidos em nomes de arquivo
        return fileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
    
    private String[] extractClusterAndDatabaseFromFileName(String fileName) {
        // Formato esperado: cluster_database_timestamp.csv
        String nameWithoutExtension = fileName.replace(".csv", "");
        String[] parts = nameWithoutExtension.split("_");
        
        if (parts.length >= 2) {
            return new String[]{parts[0], parts[1]};
        } else {
            return new String[]{"unknown", "unknown"};
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    // Classe para informações de arquivo CSV
    public static class CsvFileInfo {
        private String filePath;
        private String fileName;
        private long fileSize;
        private long lastModified;
        private long lineCount;
        private long dataLineCount;
        
        // Getters and Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        
        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
        
        public long getLineCount() { return lineCount; }
        public void setLineCount(long lineCount) { this.lineCount = lineCount; }
        
        public long getDataLineCount() { return dataLineCount; }
        public void setDataLineCount(long dataLineCount) { this.dataLineCount = dataLineCount; }
        
        @Override
        public String toString() {
            return "CsvFileInfo{" +
                    "fileName='" + fileName + '\'' +
                    ", fileSize=" + fileSize +
                    ", lineCount=" + lineCount +
                    ", dataLineCount=" + dataLineCount +
                    '}';
        }
    }
}