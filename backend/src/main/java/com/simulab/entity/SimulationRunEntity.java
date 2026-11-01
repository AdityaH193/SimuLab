package com.simulab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_runs")
public class SimulationRunEntity {

    @Id
    private String id; // UUID

    private Long architectureId;
    private String architectureName;

    private double durationSec;
    private double workloadRps;
    private long randomSeed;

    private String status = "COMPLETED";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SimulationRunEntity() {}

    public SimulationRunEntity(String id, Long architectureId, String architectureName, double durationSec, double workloadRps, long randomSeed) {
        this.id = id;
        this.architectureId = architectureId;
        this.architectureName = architectureName;
        this.durationSec = durationSec;
        this.workloadRps = workloadRps;
        this.randomSeed = randomSeed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getArchitectureId() { return architectureId; }
    public void setArchitectureId(Long architectureId) { this.architectureId = architectureId; }

    public String getArchitectureName() { return architectureName; }
    public void setArchitectureName(String architectureName) { this.architectureName = architectureName; }

    public double getDurationSec() { return durationSec; }
    public void setDurationSec(double durationSec) { this.durationSec = durationSec; }

    public double getWorkloadRps() { return workloadRps; }
    public void setWorkloadRps(double workloadRps) { this.workloadRps = workloadRps; }

    public long getRandomSeed() { return randomSeed; }
    public void setRandomSeed(long randomSeed) { this.randomSeed = randomSeed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
