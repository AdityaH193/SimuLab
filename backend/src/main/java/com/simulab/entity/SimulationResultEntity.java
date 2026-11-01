package com.simulab.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "simulation_results")
public class SimulationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String runId;

    private double throughputRps;
    private double p95LatencyMs;
    private double errorRatePercent;

    private String bottleneckNodeName;
    private String bottleneckSeverity;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resultJson;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SimulationResultEntity() {}

    public SimulationResultEntity(String runId, double throughputRps, double p95LatencyMs, double errorRatePercent, String bottleneckNodeName, String bottleneckSeverity, String resultJson) {
        this.runId = runId;
        this.throughputRps = throughputRps;
        this.p95LatencyMs = p95LatencyMs;
        this.errorRatePercent = errorRatePercent;
        this.bottleneckNodeName = bottleneckNodeName;
        this.bottleneckSeverity = bottleneckSeverity;
        this.resultJson = resultJson;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public double getThroughputRps() { return throughputRps; }
    public void setThroughputRps(double throughputRps) { this.throughputRps = throughputRps; }

    public double getP95LatencyMs() { return p95LatencyMs; }
    public void setP95LatencyMs(double p95LatencyMs) { this.p95LatencyMs = p95LatencyMs; }

    public double getErrorRatePercent() { return errorRatePercent; }
    public void setErrorRatePercent(double errorRatePercent) { this.errorRatePercent = errorRatePercent; }

    public String getBottleneckNodeName() { return bottleneckNodeName; }
    public void setBottleneckNodeName(String bottleneckNodeName) { this.bottleneckNodeName = bottleneckNodeName; }

    public String getBottleneckSeverity() { return bottleneckSeverity; }
    public void setBottleneckSeverity(String bottleneckSeverity) { this.bottleneckSeverity = bottleneckSeverity; }

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
