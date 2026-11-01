package com.simulab.engine.dto;

import java.util.ArrayList;
import java.util.List;

public class BottleneckReportDto {
    private boolean hasBottleneck;
    private String bottleneckNodeId;
    private String bottleneckNodeName;
    private String bottleneckType;
    private String severity; // CRITICAL, WARNING, HEALTHY
    private String finding;
    private String recommendation;
    private String estimatedImpact;
    private List<String> observations = new ArrayList<>();

    public BottleneckReportDto() {}

    public boolean isHasBottleneck() { return hasBottleneck; }
    public void setHasBottleneck(boolean hasBottleneck) { this.hasBottleneck = hasBottleneck; }

    public String getBottleneckNodeId() { return bottleneckNodeId; }
    public void setBottleneckNodeId(String bottleneckNodeId) { this.bottleneckNodeId = bottleneckNodeId; }

    public String getBottleneckNodeName() { return bottleneckNodeName; }
    public void setBottleneckNodeName(String bottleneckNodeName) { this.bottleneckNodeName = bottleneckNodeName; }

    public String getBottleneckType() { return bottleneckType; }
    public void setBottleneckType(String bottleneckType) { this.bottleneckType = bottleneckType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getFinding() { return finding; }
    public void setFinding(String finding) { this.finding = finding; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getEstimatedImpact() { return estimatedImpact; }
    public void setEstimatedImpact(String estimatedImpact) { this.estimatedImpact = estimatedImpact; }

    public List<String> getObservations() { return observations; }
    public void setObservations(List<String> observations) { this.observations = observations; }
}
