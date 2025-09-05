package br.com.arturbarth.siaextrator.controller;

import br.com.arturbarth.siaextrator.dto.QueryExecutionRequestDTO;
import br.com.arturbarth.siaextrator.dto.QueryExecutionResponseDTO;
import br.com.arturbarth.siaextrator.service.CsvService;
import br.com.arturbarth.siaextrator.service.QueryOrchestratorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/query-executions")
@CrossOrigin(origins = "*")
public class QueryExecutionController {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionController.class);
    
    @Autowired
    private QueryOrchestratorService queryOrchestratorService;
    
    @Autowired
    private CsvService csvService;
    
    /**
     * Submeter nova execução de query
     * POST /api/v1/query-executions
     */
    @PostMapping
    public ResponseEntity<QueryExecutionResponseDTO> submitQuery(@Valid @RequestBody QueryExecutionRequestDTO requestDTO) {
        logger.info("Recebida requisição de execução de query do usuário: {}", requestDTO.getUserId());
        
        QueryExecutionResponseDTO response = queryOrchestratorService.submitQuery(requestDTO);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    /**
     * Consultar status de execução
     * GET /api/v1/query-executions/{executionId}
     */
    @GetMapping("/{executionId}")
    public ResponseEntity<QueryExecutionResponseDTO> getExecutionStatus(@PathVariable String executionId) {
        logger.info("Consultando status da execução: {}", executionId);
        
        QueryExecutionResponseDTO response = queryOrchestratorService.getExecutionStatus(executionId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Listar execuções do usuário
     * GET /api/v1/query-executions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QueryExecutionResponseDTO>> getUserExecutions(@PathVariable String userId) {
        logger.info("Listando execuções do usuário: {}", userId);
        
        List<QueryExecutionResponseDTO> executions = queryOrchestratorService.getUserExecutions(userId);
        
        return ResponseEntity.ok(executions);
    }
    
    /**
     * Download do arquivo CSV consolidado
     * GET /api/v1/query-executions/{executionId}/download
     */
    @GetMapping("/{executionId}/download")
    public ResponseEntity<ByteArrayResource> downloadResults(@PathVariable String executionId) {
        logger.info("Download de resultados solicitado para execução: {}", executionId);
        
        try {
            QueryExecutionResponseDTO execution = queryOrchestratorService.getExecutionStatus(executionId);
            
            if (execution.getS3ResultPath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] csvData = csvService.downloadCsvFile(execution.getS3ResultPath());
            ByteArrayResource resource = new ByteArrayResource(csvData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + executionId + "_results.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvData.length)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
            
        } catch (Exception e) {
            logger.error("Erro no download de resultados para execução {}: {}", executionId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}