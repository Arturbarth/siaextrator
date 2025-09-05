package br.com.arturbarth.siaextrator.mapper;

import br.com.arturbarth.siaextrator.dto.QueryExecutionResponseDTO;
import br.com.arturbarth.siaextrator.dto.QueryExecutionResultDTO;
import br.com.arturbarth.siaextrator.entity.QueryExecution;
import br.com.arturbarth.siaextrator.entity.QueryExecutionResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QueryExecutionMapper {
    
    public QueryExecutionResponseDTO toResponseDTO(QueryExecution entity) {
        if (entity == null) {
            return null;
        }
        
        QueryExecutionResponseDTO dto = new QueryExecutionResponseDTO();
        dto.setId(entity.getId());
        dto.setExecutionId(entity.getExecutionId());
        dto.setSqlQuery(entity.getSqlQuery());
        dto.setUserId(entity.getUserId());
        dto.setUserEmail(entity.getUserEmail());
        dto.setStatus(entity.getStatus());
        dto.setTotalClusters(entity.getTotalClusters());
        dto.setCompletedClusters(entity.getCompletedClusters());
        dto.setFailedClusters(entity.getFailedClusters());
        dto.setTotalRows(entity.getTotalRows());
        dto.setExecutionTimeMs(entity.getExecutionTimeMs());
        dto.setS3ResultPath(entity.getS3ResultPath());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setMetadata(entity.getMetadata());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        
        if (entity.getResults() != null) {
            List<QueryExecutionResultDTO> resultDTOs = entity.getResults().stream()
                    .map(this::toResultDTO)
                    .collect(Collectors.toList());
            dto.setResults(resultDTOs);
        }
        
        return dto;
    }
    
    public List<QueryExecutionResponseDTO> toResponseDTOList(List<QueryExecution> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    private QueryExecutionResultDTO toResultDTO(QueryExecutionResult entity) {
        if (entity == null) {
            return null;
        }
        
        QueryExecutionResultDTO dto = new QueryExecutionResultDTO();
        dto.setId(entity.getId());
        dto.setClusterAlias(entity.getCluster() != null ? entity.getCluster().getAlias() : null);
        dto.setDatabaseName(entity.getDatabaseName());
        dto.setStatus(entity.getStatus());
        dto.setRowsAffected(entity.getRowsAffected());
        dto.setExecutionTimeMs(entity.getExecutionTimeMs());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setResultFilePath(entity.getResultFilePath());
        dto.setExecutedAt(entity.getExecutedAt());
        
        return dto;
    }
    
    public List<QueryExecutionResultDTO> toResultDTOList(List<QueryExecutionResult> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResultDTO)
                .collect(Collectors.toList());
    }
}