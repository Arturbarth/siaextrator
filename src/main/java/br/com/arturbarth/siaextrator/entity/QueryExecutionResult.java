package br.com.arturbarth.siaextrator.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "query_execution_results")
public class QueryExecutionResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_execution_id", nullable = false)
    private QueryExecution queryExecution;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private Cluster cluster;
    
    @Column(name = "database_name", nullable = false)
    private String databaseName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueryExecutionStatus status;
    
    @Column(name = "rows_affected")
    private Long rowsAffected = 0L;
    
    @Column(name = "execution_time_ms")
    private Long executionTimeMs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "result_file_path")
    private String resultFilePath;
    
    @CreationTimestamp
    @Column(name = "executed_at", updatable = false)
    private LocalDateTime executedAt;
    
    // Constructors
    public QueryExecutionResult() {}
    
    public QueryExecutionResult(QueryExecution queryExecution, Cluster cluster, String databaseName) {
        this.queryExecution = queryExecution;
        this.cluster = cluster;
        this.databaseName = databaseName;
        this.status = QueryExecutionStatus.PENDING;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public QueryExecution getQueryExecution() { return queryExecution; }
    public void setQueryExecution(QueryExecution queryExecution) { this.queryExecution = queryExecution; }
    
    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    
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