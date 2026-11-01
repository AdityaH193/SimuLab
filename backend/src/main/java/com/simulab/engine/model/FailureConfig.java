package com.simulab.engine.model;

public class FailureConfig {
    public enum FailureType {
        NODE_CRASH,
        LATENCY_SPIKE,
        LINK_CUT,
        CAPACITY_DROP
    }

    private FailureType type;
    private String targetId; // Node ID or Edge ID
    private double startSec;
    private double durationSec;
    private double magnitude; // e.g. latency spike ms, or capacity drop %

    public FailureConfig() {}

    public FailureConfig(FailureType type, String targetId, double startSec, double durationSec, double magnitude) {
        this.type = type;
        this.targetId = targetId;
        this.startSec = startSec;
        this.durationSec = durationSec;
        this.magnitude = magnitude;
    }

    public FailureType getType() {
        return type;
    }

    public void setType(FailureType type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public double getStartSec() {
        return startSec;
    }

    public void setStartSec(double startSec) {
        this.startSec = startSec;
    }

    public double getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(double durationSec) {
        this.durationSec = durationSec;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
}
