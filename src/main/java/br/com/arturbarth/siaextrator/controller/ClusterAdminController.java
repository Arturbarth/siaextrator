package br.com.arturbarth.siaextrator.controller;

import br.com.arturbarth.siaextrator.dto.ClusterRequestDTO;
import br.com.arturbarth.siaextrator.dto.ClusterResponseDTO;
import br.com.arturbarth.siaextrator.service.ClusterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clusters")
@CrossOrigin(origins = "*")
public class ClusterAdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterAdminController.class);
    
    @Autowired
    private ClusterService clusterService;
    
    /**
     * Criar novo cluster
     * POST /api/v1/clusters
     */
    @PostMapping
    public ResponseEntity<ClusterResponseDTO> createCluster(@Valid @RequestBody ClusterRequestDTO clusterRequestDTO) {
        logger.info("Recebida requisição para criar cluster: {}", clusterRequestDTO.getAlias());
        ClusterResponseDTO createdCluster = clusterService.createCluster(clusterRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCluster);
    }
    
    /**
     * Listar todos os clusters
     * GET /api/v1/clusters
     */
    @GetMapping
    public ResponseEntity<List<ClusterResponseDTO>> getAllClusters() {
        logger.info("Recebida requisição para listar todos os clusters");
        List<ClusterResponseDTO> clusters = clusterService.getAllClusters();
        return ResponseEntity.ok(clusters);
    }
    
    /**
     * Buscar cluster por ID
     * GET /api/v1/clusters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClusterResponseDTO> getClusterById(@PathVariable Long id) {
        logger.info("Recebida requisição para buscar cluster por ID: {}", id);
        ClusterResponseDTO cluster = clusterService.getClusterById(id);
        return ResponseEntity.ok(cluster);
    }
    
    /**
     * Buscar cluster por alias
     * GET /api/v1/clusters/alias/{alias}
     */
    @GetMapping("/alias/{alias}")
    public ResponseEntity<ClusterResponseDTO> getClusterByAlias(@PathVariable String alias) {
        logger.info("Recebida requisição para buscar cluster por alias: {}", alias);
        ClusterResponseDTO cluster = clusterService.getClusterByAlias(alias);
        return ResponseEntity.ok(cluster);
    }
    
    /**
     * Atualizar cluster
     * PUT /api/v1/clusters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClusterResponseDTO> updateCluster(
            @PathVariable Long id,
            @Valid @RequestBody ClusterRequestDTO clusterRequestDTO) {
        logger.info("Recebida requisição para atualizar cluster ID: {}", id);
        ClusterResponseDTO updatedCluster = clusterService.updateCluster(id, clusterRequestDTO);
        return ResponseEntity.ok(updatedCluster);
    }
    
    /**
     * Deletar cluster
     * DELETE /api/v1/clusters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCluster(@PathVariable Long id) {
        logger.info("Recebida requisição para deletar cluster ID: {}", id);
        clusterService.deleteCluster(id);
        return ResponseEntity.ok(Map.of("message", "Cluster deletado com sucesso", "id", id.toString()));
    }
    
    /**
     * Descobrir bancos de dados do cluster
     * POST /api/v1/clusters/{id}/discover
     */
    @PostMapping("/{id}/discover")
    public ResponseEntity<ClusterResponseDTO> discoverDatabases(@PathVariable Long id) {
        logger.info("Recebida requisição para descobrir bancos do cluster ID: {}", id);
        ClusterResponseDTO cluster = clusterService.discoverDatabases(id);
        return ResponseEntity.ok(cluster);
    }
    
    /**
     * Testar conexão com cluster
     * GET /api/v1/clusters/{id}/test-connection
     */
    @GetMapping("/{id}/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable Long id) {
        logger.info("Recebida requisição para testar conexão do cluster ID: {}", id);
        boolean connectionSuccessful = clusterService.testConnection(id);
        
        Map<String, Object> response = Map.of(
                "clusterId", id,
                "connectionSuccessful", connectionSuccessful,
                "message", connectionSuccessful ? "Conexão bem-sucedida" : "Falha na conexão"
        );
        
        return ResponseEntity.ok(response);
    }
}