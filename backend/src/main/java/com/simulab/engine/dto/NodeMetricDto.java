package com.simulab.engine.dto;

import com.simulab.engine.model.ComponentType;

public class NodeMetricDto {
    private String nodeId;
    private String nodeName;
    private ComponentType componentType;
    private long requestsReceived;
    private long requestsCompleted;
    private long requestsFailed;
    private double avgProcessingTimeMs;
    private double avgQueueWaitTimeMs;
    private int peakQueueDepth;
    private int peakConcurrency;
    private int maxCapacity;
    private double utilizationPercent;
    private double errorRatePercent;
    private boolean bottleneck;
    private String bottleneckReason;

    public NodeMetricDto() {}

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    public long getRequestsReceived() { return requestsReceived; }
    public void setRequestsReceived(long requestsReceived) { this.requestsReceived = requestsReceived; }

    public long getRequestsCompleted() { return requestsCompleted; }
    public void setRequestsCompleted(long requestsCompleted) { this.requestsCompleted = requestsCompleted; }

    public long getRequestsFailed() { return requestsFailed; }
    public void setRequestsFailed(long requestsFailed) { this.requestsFailed = requestsFailed; }

    public double getAvgProcessingTimeMs() { return avgProcessingTimeMs; }
    public void setAvgProcessingTimeMs(double avgProcessingTimeMs) { this.avgProcessingTimeMs = avgProcessingTimeMs; }

    public double getAvgQueueWaitTimeMs() { return avgQueueWaitTimeMs; }
    public void setAvgQueueWaitTimeMs(double avgQueueWaitTimeMs) { this.avgQueueWaitTimeMs = avgQueueWaitTimeMs; }

    public int getPeakQueueDepth() { return peakQueueDepth; }
    public void setPeakQueueDepth(int peakQueueDepth) { this.peakQueueDepth = peakQueueDepth; }

    public int getPeakConcurrency() { return peakConcurrency; }
    public void setPeakConcurrency(int peakConcurrency) { this.peakConcurrency = peakConcurrency; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public double getUtilizationPercent() { return utilizationPercent; }
    public void setUtilizationPercent(double utilizationPercent) { this.utilizationPercent = utilizationPercent; }

    public double getErrorRatePercent() { return errorRatePercent; }
    public void setErrorRatePercent(double errorRatePercent) { this.errorRatePercent = errorRatePercent; }

    public boolean isBottleneck() { return bottleneck; }
    public void setBottleneck(boolean bottleneck) { this.bottleneck = bottleneck; }

    public String getBottleneckReason() { return bottleneckReason; }
    public void setBottleneckReason(String bottleneckReason) { this.bottleneckReason = bottleneckReason; }
}
