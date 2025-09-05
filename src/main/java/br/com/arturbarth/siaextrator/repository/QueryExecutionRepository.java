package br.com.arturbarth.siaextrator.repository;

import br.com.arturbarth.siaextrator.entity.QueryExecution;
import br.com.arturbarth.siaextrator.entity.QueryExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueryExecutionRepository extends JpaRepository<QueryExecution, Long> {
    
    Optional<QueryExecution> findByExecutionId(String executionId);
    
    List<QueryExecution> findByUserId(String userId);
    
    List<QueryExecution> findByStatus(QueryExecutionStatus status);
    
    List<QueryExecution> findByUserIdAndStatus(String userId, QueryExecutionStatus status);
    
    @Query("SELECT qe FROM QueryExecution qe WHERE qe.createdAt >= :startDate ORDER BY qe.createdAt DESC")
    List<QueryExecution> findRecentExecutions(LocalDateTime startDate);
    
    @Query("SELECT qe FROM QueryExecution qe LEFT JOIN FETCH qe.results WHERE qe.executionId = :executionId")
    Optional<QueryExecution> findByExecutionIdWithResults(String executionId);
}