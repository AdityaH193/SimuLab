package com.simulab.engine.dto;

public class TimeSeriesPointDto {
    private double timestampSec;
    private double throughputRps;
    private double avgLatencyMs;
    private double p95LatencyMs;
    private int activeInFlight;
    private int errorCount;
    private int queueDepth;

    public TimeSeriesPointDto() {}

    public TimeSeriesPointDto(double timestampSec, double throughputRps, double avgLatencyMs, double p95LatencyMs, int activeInFlight, int errorCount, int queueDepth) {
        this.timestampSec = timestampSec;
        this.throughputRps = throughputRps;
        this.avgLatencyMs = avgLatencyMs;
        this.p95LatencyMs = p95LatencyMs;
        this.activeInFlight = activeInFlight;
        this.errorCount = errorCount;
        this.queueDepth = queueDepth;
    }

    public double getTimestampSec() { return timestampSec; }
    public void setTimestampSec(double timestampSec) { this.timestampSec = timestampSec; }

    public double getThroughputRps() { return throughputRps; }
    public void setThroughputRps(double throughputRps) { this.throughputRps = throughputRps; }

    public double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }

    public double getP95LatencyMs() { return p95LatencyMs; }
    public void setP95LatencyMs(double p95LatencyMs) { this.p95LatencyMs = p95LatencyMs; }

    public int getActiveInFlight() { return activeInFlight; }
    public void setActiveInFlight(int activeInFlight) { this.activeInFlight = activeInFlight; }

    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }

    public int getQueueDepth() { return queueDepth; }
    public void setQueueDepth(int queueDepth) { this.queueDepth = queueDepth; }
}
