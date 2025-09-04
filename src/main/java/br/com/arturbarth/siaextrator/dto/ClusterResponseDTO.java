package br.com.arturbarth.siaextrator.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ClusterResponseDTO {
    
    private Long id;
    private String alias;
    private String host;
    private Integer port;
    private String username;
    private Boolean connectionActive;
    private LocalDateTime lastDiscovery;
    private String description;
    private List<DatabaseInstanceDTO> databases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Construtores
    public ClusterResponseDTO() {}
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Boolean getConnectionActive() { return connectionActive; }
    public void setConnectionActive(Boolean connectionActive) { this.connectionActive = connectionActive; }
    
    public LocalDateTime getLastDiscovery() { return lastDiscovery; }
    public void setLastDiscovery(LocalDateTime lastDiscovery) { this.lastDiscovery = lastDiscovery; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<DatabaseInstanceDTO> getDatabases() { return databases; }
    public void setDatabases(List<DatabaseInstanceDTO> databases) { this.databases = databases; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}