package br.com.arturbarth.siaextrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class QueryExecutionRequestDTO {
    
    @NotBlank(message = "SQL é obrigatório")
    private String sqlQuery;
    
    @NotEmpty(message = "Pelo menos um cluster deve ser selecionado")
    private List<Long> clusterIds;
    
    private List<String> databaseNames; // Opcional - se não informado, executa em todos
    
    @NotBlank(message = "ID do usuário é obrigatório")
    private String userId;
    
    private String userEmail;
    
    private String description;
    
    // Constructors
    public QueryExecutionRequestDTO() {}
    
    // Getters and Setters
    public String getSqlQuery() { return sqlQuery; }
    public void setSqlQuery(String sqlQuery) { this.sqlQuery = sqlQuery; }
    
    public List<Long> getClusterIds() { return clusterIds; }
    public void setClusterIds(List<Long> clusterIds) { this.clusterIds = clusterIds; }
    
    public List<String> getDatabaseNames() { return databaseNames; }
    public void setDatabaseNames(List<String> databaseNames) { this.databaseNames = databaseNames; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
