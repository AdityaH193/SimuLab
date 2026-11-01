package com.simulab.repository;

import com.simulab.entity.ArchitectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchitectureRepository extends JpaRepository<ArchitectureEntity, Long> {
    List<ArchitectureEntity> findByIsTemplateTrue();
    List<ArchitectureEntity> findAllByOrderByUpdatedAtDesc();
}
