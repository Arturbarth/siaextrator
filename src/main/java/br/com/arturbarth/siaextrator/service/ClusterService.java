package br.com.arturbarth.siaextrator.service;

import br.com.arturbarth.siaextrator.dto.ClusterRequestDTO;
import br.com.arturbarth.siaextrator.dto.ClusterResponseDTO;
import br.com.arturbarth.siaextrator.entity.Cluster;
import br.com.arturbarth.siaextrator.entity.DatabaseInstance;
import br.com.arturbarth.siaextrator.exceptions.ClusterAlreadyExistsException;
import br.com.arturbarth.siaextrator.exceptions.ClusterNotFoundException;
import br.com.arturbarth.siaextrator.mapper.ClusterMapper;
import br.com.arturbarth.siaextrator.repository.ClusterRepository;
import br.com.arturbarth.siaextrator.repository.DatabaseInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClusterService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);
    
    @Autowired
    private ClusterRepository clusterRepository;
    
    @Autowired
    private DatabaseInstanceRepository databaseInstanceRepository;
    
    @Autowired
    private DatabaseDiscoveryService databaseDiscoveryService;
    
    @Autowired
    private ClusterMapper clusterMapper;
    
    public ClusterResponseDTO createCluster(ClusterRequestDTO clusterRequestDTO) {
        logger.info("Criando novo cluster com alias: {}", clusterRequestDTO.getAlias());
        
        // Verificar se já existe cluster com este alias
        if (clusterRepository.existsByAlias(clusterRequestDTO.getAlias())) {
            throw new ClusterAlreadyExistsException("Cluster com alias '" + clusterRequestDTO.getAlias() + "' já existe");
        }
        
        Cluster cluster = clusterMapper.toEntity(clusterRequestDTO);
        cluster = clusterRepository.save(cluster);
        
        // Descobrir bancos de dados automaticamente após salvar
        try {
            discoverDatabases(cluster.getId());
            logger.info("Cluster criado com sucesso: {}", cluster.getAlias());
        } catch (Exception e) {
            logger.warn("Falha na descoberta de bancos para o cluster {}: {}", cluster.getAlias(), e.getMessage());
        }
        
        return clusterMapper.toResponseDTO(clusterRepository.findByIdWithDatabases(cluster.getId()).orElse(cluster));
    }
    
    public List<ClusterResponseDTO> getAllClusters() {
        List<Cluster> clusters = clusterRepository.findAllWithDatabases();
        return clusters.stream()
                .map(clusterMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    public ClusterResponseDTO getClusterById(Long id) {
        Cluster cluster = clusterRepository.findByIdWithDatabases(id)
                .orElseThrow(() -> new ClusterNotFoundException("Cluster não encontrado com ID: " + id));
        return clusterMapper.toResponseDTO(cluster);
    }
    
    public ClusterResponseDTO getClusterByAlias(String alias) {
        Cluster cluster = clusterRepository.findByAlias(alias)
                .orElseThrow(() -> new ClusterNotFoundException("Cluster não encontrado com alias: " + alias));
        return clusterMapper.toResponseDTO(cluster);
    }
    
    public ClusterResponseDTO updateCluster(Long id, ClusterRequestDTO clusterRequestDTO) {
        logger.info("Atualizando cluster ID: {}", id);
        
        Cluster existingCluster = clusterRepository.findById(id)
                .orElseThrow(() -> new ClusterNotFoundException("Cluster não encontrado com ID: " + id));
        
        // Verificar se o novo alias não está em uso por outro cluster
        if (!existingCluster.getAlias().equals(clusterRequestDTO.getAlias()) &&
                clusterRepository.existsByAlias(clusterRequestDTO.getAlias())) {
            throw new ClusterAlreadyExistsException("Cluster com alias '" + clusterRequestDTO.getAlias() + "' já existe");
        }
        
        clusterMapper.updateEntityFromDTO(clusterRequestDTO, existingCluster);
        existingCluster = clusterRepository.save(existingCluster);
        
        logger.info("Cluster atualizado com sucesso: {}", existingCluster.getAlias());
        return clusterMapper.toResponseDTO(existingCluster);
    }
    
    public void deleteCluster(Long id) {
        logger.info("Deletando cluster ID: {}", id);
        
        if (!clusterRepository.existsById(id)) {
            throw new ClusterNotFoundException("Cluster não encontrado com ID: " + id);
        }
        
        clusterRepository.deleteById(id);
        logger.info("Cluster deletado com sucesso: ID {}", id);
    }
    
    public ClusterResponseDTO discoverDatabases(Long clusterId) {
        logger.info("Iniciando descoberta de bancos para cluster ID: {}", clusterId);
        
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ClusterNotFoundException("Cluster não encontrado com ID: " + clusterId));
        
        try {
            List<String> discoveredDatabases = databaseDiscoveryService.discoverDatabases(cluster);
            
            // Limpar bancos existentes
            cluster.clearDatabases();
            
            // Adicionar novos bancos descobertos
            for (String databaseName : discoveredDatabases) {
                DatabaseInstance databaseInstance = new DatabaseInstance(databaseName, cluster);
                cluster.addDatabase(databaseInstance);
            }
            
            cluster.setConnectionActive(true);
            cluster.setLastDiscovery(LocalDateTime.now());
            
            cluster = clusterRepository.save(cluster);
            logger.info("Descoberta concluída para cluster {}: {} bancos encontrados",
                    cluster.getAlias(), discoveredDatabases.size());
            
        } catch (Exception e) {
            logger.error("Erro na descoberta de bancos para cluster {}: {}", cluster.getAlias(), e.getMessage());
            cluster.setConnectionActive(false);
            clusterRepository.save(cluster);
            throw new RuntimeException("Falha na descoberta de bancos: " + e.getMessage(), e);
        }
        
        return clusterMapper.toResponseDTO(clusterRepository.findByIdWithDatabases(clusterId).orElse(cluster));
    }
    
    public boolean testConnection(Long clusterId) {
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ClusterNotFoundException("Cluster não encontrado com ID: " + clusterId));
        
        return databaseDiscoveryService.testConnection(cluster);
    }
}
