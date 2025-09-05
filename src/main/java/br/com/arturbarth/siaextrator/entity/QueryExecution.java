package br.com.arturbarth.siaextrator.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "query_executions")
public class QueryExecution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "SQL é obrigatório")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String sqlQuery;
    
    @Column(name = "execution_id", unique = true, nullable = false)
    private String executionId;
    
    @NotBlank(message = "Usuário é obrigatório")
    @Column(nullable = false)
    private String userId;
    
    @Column(name = "user_email")
    private String userEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryExecutionStatus status = QueryExecutionStatus.PENDING;
    
    @Column(name = "total_clusters")
    private Integer totalClusters = 0;
    
    @Column(name = "completed_clusters")
    private Integer completedClusters = 0;
    
    @Column(name = "failed_clusters")
    private Integer failedClusters = 0;
    
    @Column(name = "total_rows")
    private Long totalRows = 0L;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "s3_result_path")
    private String s3ResultPath;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @OneToMany(mappedBy = "queryExecution", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QueryExecutionResult> results = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Constructors
    public QueryExecution() {}
    
    public QueryExecution(String sqlQuery, String executionId, String userId) {
        this.sqlQuery = sqlQuery;
        this.executionId = executionId;
        this.userId = userId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
    
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    
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
    
    public List<QueryExecutionResult> getResults() { return results; }
    public void setResults(List<QueryExecutionResult> results) { this.results = results; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    // Helper methods
    public void markAsStarted() {
        this.status = QueryExecutionStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        this.status = QueryExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = QueryExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.executionTimeMs = java.time.Duration.between(this.startedAt, this.completedAt).toMillis();
        }
    }
    
    public void incrementCompletedClusters() {
        this.completedClusters = (this.completedClusters == null) ? 1 : this.completedClusters + 1;
    }
    
    public void incrementFailedClusters() {
        this.failedClusters = (this.failedClusters == null) ? 1 : this.failedClusters + 1;
    }
}