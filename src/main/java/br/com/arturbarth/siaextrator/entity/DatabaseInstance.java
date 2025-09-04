package br.com.arturbarth.siaextrator.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "database_instances",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cluster_id", "database_name"}))
public class DatabaseInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do banco é obrigatório")
    @Column(name = "database_name", nullable = false)
    private String databaseName;
    
    @Column(name = "database_size")
    private Long databaseSize;
    
    @Column(name = "database_encoding")
    private String databaseEncoding;
    
    @Column(name = "is_accessible")
    private Boolean isAccessible = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    @JsonBackReference
    private Cluster cluster;
    
    @CreationTimestamp
    @Column(name = "discovered_at", updatable = false)
    private LocalDateTime discoveredAt;
    
    // Construtores
    public DatabaseInstance() {}
    
    public DatabaseInstance(String databaseName, Cluster cluster) {
        this.databaseName = databaseName;
        this.cluster = cluster;
    }
    
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
    
    public Cluster getCluster() { return cluster; }
    public void setCluster(Cluster cluster) { this.cluster = cluster; }
    
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
    
    @Override
    public String toString() {
        return "DatabaseInstance{" +
                "id=" + id +
                ", databaseName='" + databaseName + '\'' +
                ", isAccessible=" + isAccessible +
                '}';
    }
    
}
