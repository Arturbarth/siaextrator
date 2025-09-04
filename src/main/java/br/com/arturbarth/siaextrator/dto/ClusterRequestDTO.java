package br.com.arturbarth.siaextrator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class ClusterRequestDTO {
    
    @NotBlank(message = "Alias é obrigatório")
    private String alias;
    
    @NotBlank(message = "Host é obrigatório")
    private String host;
    
    @NotNull(message = "Porta é obrigatória")
    @Min(value = 1, message = "Porta deve ser maior que 0")
    @Max(value = 65535, message = "Porta deve ser menor que 65536")
    private Integer port = 5432;
    
    @NotBlank(message = "Username é obrigatório")
    private String username;
    
    @NotBlank(message = "Password é obrigatória")
    private String password;
    
    private String description;
    
    // Construtores
    public ClusterRequestDTO() {}
    
    // Getters e Setters
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
