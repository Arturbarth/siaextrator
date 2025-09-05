package br.com.arturbarth.siaextrator.repository;

import br.com.arturbarth.siaextrator.entity.QueryExecutionResult;
import br.com.arturbarth.siaextrator.entity.QueryExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryExecutionResultRepository extends JpaRepository<QueryExecutionResult, Long> {
    
    List<QueryExecutionResult> findByQueryExecutionId(Long queryExecutionId);
    
    List<QueryExecutionResult> findByClusterId(Long clusterId);
    
    List<QueryExecutionResult> findByStatus(QueryExecutionStatus status);
    
    List<QueryExecutionResult> findByQueryExecutionIdAndStatus(Long queryExecutionId, QueryExecutionStatus status);
}