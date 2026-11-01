package com.simulab.repository;

import com.simulab.entity.SimulationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulationResultRepository extends JpaRepository<SimulationResultEntity, Long> {
    Optional<SimulationResultEntity> findByRunId(String runId);
}
