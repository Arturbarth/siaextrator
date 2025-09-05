package br.com.arturbarth.siaextrator.service;

import br.com.arturbarth.siaextrator.entity.Cluster;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDiscoveryService.class);
    
    public List<String> discoverDatabases(Cluster cluster) {
        List<String> databases = new ArrayList<>();
        String jdbcUrl = buildJdbcUrl(cluster);
        
        logger.info("Descobrindo bancos no cluster: {} ({})", cluster.getAlias(), jdbcUrl);
        
        try (Connection connection = DriverManager.getConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            String query = "SELECT datname FROM pg_database WHERE datistemplate = false AND datallowconn = true and datname like 'banco%'";
            
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    String databaseName = resultSet.getString("datname");
                    databases.add(databaseName);
                    logger.debug("Banco encontrado: {}", databaseName);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erro ao descobrir bancos no cluster {}: {}", cluster.getAlias(), e.getMessage());
            throw new RuntimeException("Falha na conexão com o cluster: " + e.getMessage(), e);
        }
        
        logger.info("Descoberta concluída para {}: {} bancos encontrados", cluster.getAlias(), databases.size());
        return databases;
    }
    
    public boolean testConnection(Cluster cluster) {
        String jdbcUrl = buildJdbcUrl(cluster);
        
        logger.info("Testando conexão com cluster: {} ({})", cluster.getAlias(), jdbcUrl);
        
        try (Connection connection = DriverManager.getConnection(jdbcUrl, cluster.getUsername(), cluster.getPassword())) {
            return connection.isValid(5); // 5 segundos de timeout
        } catch (SQLException e) {
            logger.warn("Falha no teste de conexão para cluster {}: {}", cluster.getAlias(), e.getMessage());
            return false;
        }
    }
    
    private String buildJdbcUrl(Cluster cluster) {
        return String.format("jdbc:postgresql://%s:%d/postgres", cluster.getHost(), cluster.getPort());
    }
}
