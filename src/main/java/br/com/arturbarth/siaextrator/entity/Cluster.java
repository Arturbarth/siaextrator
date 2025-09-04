package br.com.arturbarth.siaextrator.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clusters")
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Alias é obrigatório")
    @Column(nullable = false, unique = true)
    private String alias;
    
    @NotBlank(message = "Host é obrigatório")
    @Column(nullable = false)
    private String host;
    
    @NotNull(message = "Porta é obrigatória")
    @Column(nullable = false)
    private Integer port = 5432;
    
    @NotBlank(message = "Username é obrigatório")
    @Column(nullable = false)
    private String username;
    
    @NotBlank(message = "Password é obrigatória")
    @Column(nullable = false)
    private String password;
    
    @Column(name = "connection_active")
    private Boolean connectionActive = false;
    
    @Column(name = "last_discovery")
    private LocalDateTime lastDiscovery;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DatabaseInstance> databases = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Construtores
    public Cluster() {}
    
    public Cluster(String alias, String host, Integer port, String username, String password) {
        this.alias = alias;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
    
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
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Boolean getConnectionActive() { return connectionActive; }
    public void setConnectionActive(Boolean connectionActive) { this.connectionActive = connectionActive; }
    
    public LocalDateTime getLastDiscovery() { return lastDiscovery; }
    public void setLastDiscovery(LocalDateTime lastDiscovery) { this.lastDiscovery = lastDiscovery; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<DatabaseInstance> getDatabases() { return databases; }
    public void setDatabases(List<DatabaseInstance> databases) { this.databases = databases; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Método para adicionar database
    public void addDatabase(DatabaseInstance database) {
        databases.add(database);
        database.setCluster(this);
    }
    
    // Método para remover database
    public void removeDatabase(DatabaseInstance database) {
        databases.remove(database);
        database.setCluster(null);
    }
    
    // Método para limpar databases
    public void clearDatabases() {
        databases.clear();
    }
    
    @Override
    public String toString() {
        return "Cluster{" +
                "id=" + id +
                ", alias='" + alias + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", connectionActive=" + connectionActive +
                '}';
    }

}
