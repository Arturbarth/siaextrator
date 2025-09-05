package br.com.arturbarth.siaextrator.dto;

import br.com.arturbarth.siaextrator.entity.QueryExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class QueryExecutionResponseDTO {
    
    private Long id;
    private String executionId;
    private String sqlQuery;
    private String userId;
    private String userEmail;
    private QueryExecutionStatus status;
    private Integer totalClusters;
    private Integer completedClusters;
    private Integer failedClusters;
    private Long totalRows;
    private Long executionTimeMs;
    private String s3ResultPath;
    private String errorMessage;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<QueryExecutionResultDTO> results;
    
    // Constructors
    public QueryExecutionResponseDTO() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public QueryExecutionStatus getStatus() { return status; }
    public void setStatus(QueryExecutionStatus status) { this.status = status; }
    
    public Integer getTotalClusters() { return totalClusters; }
    public void setTotalClusters(Integer totalClusters) { this.totalClusters = totalClusters; }
    
    public Integer getCompletedClusters() { return completedClusters; }
    public void setCompletedClusters(Integer completedClusters) { this.completedClusters = completedClusters; }
    
    public Integer getFailedClusters() { return failedClusters; }
    public void setFailedClusters(Integer failedClusters) { this.failedClusters = failedClusters; }
    
    public Long getTotalRows() { return totalRows; }
    public void setTotalRows(Long totalRows) { this.totalRows = totalRows; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    
    public String getS3ResultPath() { return s3ResultPath; }
    public void setS3ResultPath(String s3ResultPath) { this.s3ResultPath = s3ResultPath; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public List<QueryExecutionResultDTO> getResults() { return results; }
    public void setResults(List<QueryExecutionResultDTO> results) { this.results = results; }
}