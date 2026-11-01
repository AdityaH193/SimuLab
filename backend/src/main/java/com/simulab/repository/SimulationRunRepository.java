package com.simulab.repository;

import com.simulab.entity.SimulationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulationRunRepository extends JpaRepository<SimulationRunEntity, String> {
    List<SimulationRunEntity> findAllByOrderByCreatedAtDesc();
    List<SimulationRunEntity> findByArchitectureIdOrderByCreatedAtDesc(Long architectureId);
}
