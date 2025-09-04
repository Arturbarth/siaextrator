package br.com.arturbarth.siaextrator.repository;

import br.com.arturbarth.siaextrator.entity.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {
    
    Optional<Cluster> findByAlias(String alias);
    
    List<Cluster> findByConnectionActive(Boolean connectionActive);
    
    @Query("SELECT c FROM Cluster c LEFT JOIN FETCH c.databases")
    List<Cluster> findAllWithDatabases();
    
    @Query("SELECT c FROM Cluster c LEFT JOIN FETCH c.databases WHERE c.id = :id")
    Optional<Cluster> findByIdWithDatabases(Long id);
    
    boolean existsByAlias(String alias);
    
}
