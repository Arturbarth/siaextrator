package br.com.arturbarth.siaextrator.dto;

import java.time.LocalDateTime;

public class DatabaseInstanceDTO {
    
    private Long id;
    private String databaseName;
    private Long databaseSize;
    private String databaseEncoding;
    private Boolean isAccessible;
    private LocalDateTime discoveredAt;
    
    // Construtores
    public DatabaseInstanceDTO() {}
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    
    public Long getDatabaseSize() { return databaseSize; }
    public void setDatabaseSize(Long databaseSize) { this.databaseSize = databaseSize; }
    
    public String getDatabaseEncoding() { return databaseEncoding; }
    public void setDatabaseEncoding(String databaseEncoding) { this.databaseEncoding = databaseEncoding; }
    
    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }
    
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
}
