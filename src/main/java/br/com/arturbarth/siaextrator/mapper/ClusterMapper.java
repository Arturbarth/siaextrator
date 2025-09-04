package br.com.arturbarth.siaextrator.mapper;

import br.com.arturbarth.siaextrator.dto.ClusterRequestDTO;
import br.com.arturbarth.siaextrator.dto.ClusterResponseDTO;
import br.com.arturbarth.siaextrator.dto.DatabaseInstanceDTO;
import br.com.arturbarth.siaextrator.entity.Cluster;
import br.com.arturbarth.siaextrator.entity.DatabaseInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClusterMapper {
    
    public Cluster toEntity(ClusterRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Cluster cluster = new Cluster();
        cluster.setAlias(dto.getAlias());
        cluster.setHost(dto.getHost());
        cluster.setPort(dto.getPort());
        cluster.setUsername(dto.getUsername());
        cluster.setPassword(dto.getPassword());
        cluster.setDescription(dto.getDescription());
        
        return cluster;
    }
    
    public ClusterResponseDTO toResponseDTO(Cluster entity) {
        if (entity == null) {
            return null;
        }
        
        ClusterResponseDTO dto = new ClusterResponseDTO();
        dto.setId(entity.getId());
        dto.setAlias(entity.getAlias());
        dto.setHost(entity.getHost());
        dto.setPort(entity.getPort());
        dto.setUsername(entity.getUsername());
        dto.setConnectionActive(entity.getConnectionActive());
        dto.setLastDiscovery(entity.getLastDiscovery());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        if (entity.getDatabases() != null) {
            List<DatabaseInstanceDTO> databaseDTOs = entity.getDatabases().stream()
                    .map(this::toDatabaseInstanceDTO)
                    .collect(Collectors.toList());
            dto.setDatabases(databaseDTOs);
        }
        
        return dto;
    }
    
    public void updateEntityFromDTO(ClusterRequestDTO dto, Cluster entity) {
        if (dto != null && entity != null) {
            entity.setAlias(dto.getAlias());
            entity.setHost(dto.getHost());
            entity.setPort(dto.getPort());
            entity.setUsername(dto.getUsername());
            entity.setPassword(dto.getPassword());
            entity.setDescription(dto.getDescription());
        }
    }
    
    private DatabaseInstanceDTO toDatabaseInstanceDTO(DatabaseInstance entity) {
        if (entity == null) {
            return null;
        }
        
        DatabaseInstanceDTO dto = new DatabaseInstanceDTO();
        dto.setId(entity.getId());
        dto.setDatabaseName(entity.getDatabaseName());
        dto.setDatabaseSize(entity.getDatabaseSize());
        dto.setDatabaseEncoding(entity.getDatabaseEncoding());
        dto.setIsAccessible(entity.getIsAccessible());
        dto.setDiscoveredAt(entity.getDiscoveredAt());
        
        return dto;
    }
}