package com.simulab.engine.model;

public class SimEdge {
    private String id;
    private String sourceNodeId;
    private String targetNodeId;
    private double latencyMs = 2.0;
    private double bandwidthMbps = 1000.0;
    private double packetLossPercent = 0.0;
    private boolean severed = false;
    private double failureLatencySpikeMs = 0.0;

    public SimEdge() {}

    public SimEdge(String id, String sourceNodeId, String targetNodeId) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
    }

    public SimEdge(String id, String sourceNodeId, String targetNodeId, double latencyMs, double bandwidthMbps, double packetLossPercent) {
        this.id = id;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.latencyMs = latencyMs;
        this.bandwidthMbps = bandwidthMbps;
        this.packetLossPercent = packetLossPercent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public double getLatencyMs() {
        return latencyMs + failureLatencySpikeMs;
    }

    public void setLatencyMs(double latencyMs) {
        this.latencyMs = latencyMs;
    }

    public double getBandwidthMbps() {
        return bandwidthMbps;
    }

    public void setBandwidthMbps(double bandwidthMbps) {
        this.bandwidthMbps = bandwidthMbps;
    }

    public double getPacketLossPercent() {
        return packetLossPercent;
    }

    public void setPacketLossPercent(double packetLossPercent) {
        this.packetLossPercent = packetLossPercent;
    }

    public boolean isSevered() {
        return severed;
    }

    public void setSevered(boolean severed) {
        this.severed = severed;
    }

    public double getFailureLatencySpikeMs() {
        return failureLatencySpikeMs;
    }

    public void setFailureLatencySpikeMs(double failureLatencySpikeMs) {
        this.failureLatencySpikeMs = failureLatencySpikeMs;
    }
}
