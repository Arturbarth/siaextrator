package br.com.arturbarth.siaextrator.service;

import br.com.arturbarth.siaextrator.dto.RdsExecutionResult;
import br.com.arturbarth.siaextrator.entity.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RdsExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(RdsExecutorService.class);
    
    @Value("${query.execution.timeout.seconds:300}")
    private int queryTimeoutSeconds;
    
    @Value("${query.execution.max.rows:1000000}")
    private long maxRows;
    
    @Value("${database.connection.timeout:30}")
    private int connectionTimeoutSeconds;
    
    public RdsExecutionResult executeQuery(Cluster cluster, String databaseName, String sqlQuery) {
        logger.info("Executando query no cluster {} - banco {}", cluster.getAlias(), databaseName);
        
        long startTime = System.currentTimeMillis();
        String jdbcUrl = buildJdbcUrl(cluster, databaseName);
        
        try (Connection connection = createConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                
                // Configurar timeout da query
                statement.setQueryTimeout(queryTimeoutSeconds);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    
                    // Processar resultados
                    RdsExecutionResult result = processResultSet(resultSet);
                    
                    long executionTime = System.currentTimeMillis() - startTime;
                    result.setExecutionTimeMs(executionTime);
                    
                    logger.info("Query executada com sucesso: {} linhas em {}ms",
                            result.getRowCount(), executionTime);
                    
                    return result;
                }
            }
            
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Erro na execução da query no cluster {} - banco {}: {}",
                    cluster.getAlias(), databaseName, e.getMessage());
            
            throw new RuntimeException("Falha na execução da query: " + e.getMessage(), e);
        }
    }
    
    private Connection createConnection(String jdbcUrl, String username, String password) throws SQLException {
        logger.debug("Criando conexão: {}", jdbcUrl);
        
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        
        // Configurar conexão
        connection.setReadOnly(true);
        connection.setAutoCommit(false);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        
        // Configurar timeout da conexão
        if (connection.isValid(connectionTimeoutSeconds)) {
            logger.debug("Conexão estabelecida com sucesso");
        } else {
            connection.close();
            throw new SQLException("Conexão inválida após " + connectionTimeoutSeconds + " segundos");
        }
        
        return connection;
    }
    
    private RdsExecutionResult processResultSet(ResultSet resultSet) throws SQLException {
        logger.debug("Processando ResultSet");
        
        List<Map<String, Object>> data = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        
        // Obter metadados das colunas
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Extrair nomes das colunas
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }
        
        logger.debug("Colunas encontradas: {}", columnNames);
        
        // Processar linhas
        long rowCount = 0;
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            
            for (int i = 1; i <= columnCount; i++) {
                String columnName = columnNames.get(i - 1);
                Object value = getColumnValue(resultSet, i, metaData.getColumnType(i));
                row.put(columnName, value);
            }
            
            data.add(row);
            rowCount++;
            
            // Limite de segurança para evitar OutOfMemory
            if (rowCount >= maxRows) {
                logger.warn("Limite de {} linhas atingido, interrompendo processamento", maxRows);
                break;
            }
            
            // Log de progresso a cada 10k linhas
            if (rowCount % 10000 == 0) {
                logger.debug("Processadas {} linhas...", rowCount);
            }
        }
        
        logger.debug("ResultSet processado: {} linhas, {} colunas", rowCount, columnCount);
        
        return new RdsExecutionResult(data, columnNames, rowCount);
    }
    
    private Object getColumnValue(ResultSet resultSet, int columnIndex, int columnType) throws SQLException {
        Object value = resultSet.getObject(columnIndex);
        
        if (value == null) {
            return null;
        }
        
        // Tratamento específico para tipos de dados PostgreSQL
        switch (columnType) {
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                Timestamp timestamp = resultSet.getTimestamp(columnIndex);
                return timestamp != null ? timestamp.toLocalDateTime().toString() : null;
            
            case Types.DATE:
                Date date = resultSet.getDate(columnIndex);
                return date != null ? date.toLocalDate().toString() : null;
            
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                Time time = resultSet.getTime(columnIndex);
                return time != null ? time.toLocalTime().toString() : null;
            
            case Types.DECIMAL:
            case Types.NUMERIC:
                return resultSet.getBigDecimal(columnIndex);
            
            case Types.BOOLEAN:
                return resultSet.getBoolean(columnIndex);
            
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                byte[] bytes = resultSet.getBytes(columnIndex);
                return bytes != null ? java.util.Base64.getEncoder().encodeToString(bytes) : null;
            
            case Types.CLOB:
                Clob clob = resultSet.getClob(columnIndex);
                if (clob != null) {
                    try {
                        return clob.getSubString(1, (int) clob.length());
                    } finally {
                        clob.free();
                    }
                }
                return null;
            
            case Types.BLOB:
                Blob blob = resultSet.getBlob(columnIndex);
                if (blob != null) {
                    try {
                        byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                        return java.util.Base64.getEncoder().encodeToString(blobBytes);
                    } finally {
                        blob.free();
                    }
                }
                return null;
            
            case Types.ARRAY:
                Array array = resultSet.getArray(columnIndex);
                if (array != null) {
                    try {
                        Object[] arrayData = (Object[]) array.getArray();
                        return java.util.Arrays.toString(arrayData);
                    } finally {
                        array.free();
                    }
                }
                return null;
            
            default:
                // Para outros tipos, converter para string
                return value.toString();
        }
    }
    
    private String buildJdbcUrl(Cluster cluster, String databaseName) {
        return String.format("jdbc:postgresql://%s:%d/%s?connectTimeout=%d&socketTimeout=%d&loginTimeout=%d",
                cluster.getHost(),
                cluster.getPort(),
                databaseName,
                connectionTimeoutSeconds,
                queryTimeoutSeconds,
                connectionTimeoutSeconds);
    }
    
    public boolean testConnection(Cluster cluster, String databaseName) {
        String jdbcUrl = buildJdbcUrl(cluster, databaseName);
        
        logger.debug("Testando conexão: {}", jdbcUrl);
        
        try (Connection connection = DriverManager.getConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            boolean isValid = connection.isValid(connectionTimeoutSeconds);
            logger.debug("Teste de conexão {}: {}", jdbcUrl, isValid ? "SUCESSO" : "FALHA");
            return isValid;
            
        } catch (SQLException e) {
            logger.warn("Falha no teste de conexão para {} - {}: {}",
                    cluster.getAlias(), databaseName, e.getMessage());
            return false;
        }
    }
    
    public List<String> discoverDatabases(Cluster cluster) {
        logger.info("Descobrindo bancos no cluster: {}", cluster.getAlias());
        
        List<String> databases = new ArrayList<>();
        String jdbcUrl = buildJdbcUrl(cluster, "postgres"); // Conectar no banco padrão
        
        try (Connection connection = createConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            
            String query = """
                SELECT datname,
                       pg_database_size(datname) as size_bytes,
                       datcollate,
                       datctype,
                       datallowconn
                FROM pg_database
                WHERE datistemplate = false
                  AND datallowconn = true
                ORDER BY datname
                """;
            
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    String databaseName = resultSet.getString("datname");
                    long sizeBytes = resultSet.getLong("size_bytes");
                    boolean allowConn = resultSet.getBoolean("datallowconn");
                    
                    if (allowConn) {
                        databases.add(databaseName);
                        logger.debug("Banco encontrado: {} ({})", databaseName, formatBytes(sizeBytes));
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao descobrir bancos no cluster {}: {}", cluster.getAlias(), e.getMessage());
            throw new RuntimeException("Falha na descoberta de bancos: " + e.getMessage(), e);
        }
        
        logger.info("Descoberta concluída para {}: {} bancos encontrados", cluster.getAlias(), databases.size());
        return databases;
    }
    
    public DatabaseInfo getDatabaseInfo(Cluster cluster, String databaseName) {
        logger.debug("Obtendo informações do banco {} no cluster {}", databaseName, cluster.getAlias());
        
        String jdbcUrl = buildJdbcUrl(cluster, databaseName);
        
        try (Connection connection = createConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            
            String query = """
                SELECT
                    current_database() as name,
                    pg_database_size(current_database()) as size_bytes,
                    current_setting('server_encoding') as encoding,
                    current_setting('server_version') as version,
                    current_setting('TimeZone') as timezone
                """;
            
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                
                if (resultSet.next()) {
                    DatabaseInfo info = new DatabaseInfo();
                    info.setName(resultSet.getString("name"));
                    info.setSizeBytes(resultSet.getLong("size_bytes"));
                    info.setEncoding(resultSet.getString("encoding"));
                    info.setVersion(resultSet.getString("version"));
                    info.setTimezone(resultSet.getString("timezone"));
                    info.setAccessible(true);
                    
                    return info;
                }
            }
            
        } catch (SQLException e) {
            logger.warn("Erro ao obter informações do banco {} - {}: {}",
                    cluster.getAlias(), databaseName, e.getMessage());
            
            DatabaseInfo info = new DatabaseInfo();
            info.setName(databaseName);
            info.setAccessible(false);
            info.setErrorMessage(e.getMessage());
            
            return info;
        }
        
        return null;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    // Classe interna para informações de banco
    public static class DatabaseInfo {
        private String name;
        private Long sizeBytes;
        private String encoding;
        private String version;
        private String timezone;
        private Boolean accessible;
        private String errorMessage;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Long getSizeBytes() { return sizeBytes; }
        public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
        
        public String getEncoding() { return encoding; }
        public void setEncoding(String encoding) { this.encoding = encoding; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        
        public Boolean getAccessible() { return accessible; }
        public void setAccessible(Boolean accessible) { this.accessible = accessible; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}