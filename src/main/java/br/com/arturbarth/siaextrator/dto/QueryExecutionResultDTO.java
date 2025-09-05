package br.com.arturbarth.siaextrator.dto;

import br.com.arturbarth.siaextrator.entity.QueryExecutionStatus;

import java.time.LocalDateTime;

public class QueryExecutionResultDTO {
    
    private Long id;
    private String clusterAlias;
    private String databaseName;
    private QueryExecutionStatus status;
    private Long rowsAffected;
    private Long executionTimeMs;
    private String errorMessage;
    private String resultFilePath;
    private LocalDateTime executedAt;
    
    // Constructors
    public QueryExecutionResultDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getClusterAlias() { return clusterAlias; }
    public void setClusterAlias(String clusterAlias) { this.clusterAlias = clusterAlias; }
    
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    
    public QueryExecutionStatus getStatus() { return status; }
    public void setStatus(QueryExecutionStatus status) { this.status = status; }
    
    public Long getRowsAffected() { return rowsAffected; }
    public void setRowsAffected(Long rowsAffected) { this.rowsAffected = rowsAffected; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getResultFilePath() { return resultFilePath; }
    public void setResultFilePath(String resultFilePath) { this.resultFilePath = resultFilePath; }
    
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
