package br.com.arturbarth.siaextrator.repository;

import br.com.arturbarth.siaextrator.entity.DatabaseInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseInstanceRepository extends JpaRepository<DatabaseInstance, Long>  {
    
    List<DatabaseInstance> findByClusterId(Long clusterId);
    
    List<DatabaseInstance> findByDatabaseNameContainingIgnoreCase(String databaseName);
    
    void deleteByClusterId(Long clusterId);

}
