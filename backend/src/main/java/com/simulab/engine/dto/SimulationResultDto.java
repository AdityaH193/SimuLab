package com.simulab.engine.dto;

import java.util.ArrayList;
import java.util.List;

public class SimulationResultDto {
    private String runId;
    private double durationSec;
    private long totalRequestsGenerated;
    private long totalRequestsCompleted;
    private long totalRequestsFailed;
    private long totalRequestsTimedOut;
    private double throughputRps;
    private double errorRatePercent;

    // Latency Percentiles (ms)
    private double minLatencyMs;
    private double meanLatencyMs;
    private double p50LatencyMs;
    private double p90LatencyMs;
    private double p95LatencyMs;
    private double p99LatencyMs;
    private double maxLatencyMs;

    // Detailed metrics
    private List<NodeMetricDto> nodeMetrics = new ArrayList<>();
    private List<TimeSeriesPointDto> timeSeries = new ArrayList<>();
    private List<EventLogDto> eventLogs = new ArrayList<>();
    private BottleneckReportDto bottleneckReport = new BottleneckReportDto();

    public SimulationResultDto() {}

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public double getDurationSec() { return durationSec; }
    public void setDurationSec(double durationSec) { this.durationSec = durationSec; }

    public long getTotalRequestsGenerated() { return totalRequestsGenerated; }
    public void setTotalRequestsGenerated(long totalRequestsGenerated) { this.totalRequestsGenerated = totalRequestsGenerated; }

    public long getTotalRequestsCompleted() { return totalRequestsCompleted; }
    public void setTotalRequestsCompleted(long totalRequestsCompleted) { this.totalRequestsCompleted = totalRequestsCompleted; }

    public long getTotalRequestsFailed() { return totalRequestsFailed; }
    public void setTotalRequestsFailed(long totalRequestsFailed) { this.totalRequestsFailed = totalRequestsFailed; }

    public long getTotalRequestsTimedOut() { return totalRequestsTimedOut; }
    public void setTotalRequestsTimedOut(long totalRequestsTimedOut) { this.totalRequestsTimedOut = totalRequestsTimedOut; }

    public double getThroughputRps() { return throughputRps; }
    public void setThroughputRps(double throughputRps) { this.throughputRps = throughputRps; }

    public double getErrorRatePercent() { return errorRatePercent; }
    public void setErrorRatePercent(double errorRatePercent) { this.errorRatePercent = errorRatePercent; }

    public double getMinLatencyMs() { return minLatencyMs; }
    public void setMinLatencyMs(double minLatencyMs) { this.minLatencyMs = minLatencyMs; }

    public double getMeanLatencyMs() { return meanLatencyMs; }
    public void setMeanLatencyMs(double meanLatencyMs) { this.meanLatencyMs = meanLatencyMs; }

    public double getP50LatencyMs() { return p50LatencyMs; }
    public void setP50LatencyMs(double p50LatencyMs) { this.p50LatencyMs = p50LatencyMs; }

    public double getP90LatencyMs() { return p90LatencyMs; }
    public void setP90LatencyMs(double p90LatencyMs) { this.p90LatencyMs = p90LatencyMs; }

    public double getP95LatencyMs() { return p95LatencyMs; }
    public void setP95LatencyMs(double p95LatencyMs) { this.p95LatencyMs = p95LatencyMs; }

    public double getP99LatencyMs() { return p99LatencyMs; }
    public void setP99LatencyMs(double p99LatencyMs) { this.p99LatencyMs = p99LatencyMs; }

    public double getMaxLatencyMs() { return maxLatencyMs; }
    public void setMaxLatencyMs(double maxLatencyMs) { this.maxLatencyMs = maxLatencyMs; }

    public List<NodeMetricDto> getNodeMetrics() { return nodeMetrics; }
    public void setNodeMetrics(List<NodeMetricDto> nodeMetrics) { this.nodeMetrics = nodeMetrics; }

    public List<TimeSeriesPointDto> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<TimeSeriesPointDto> timeSeries) { this.timeSeries = timeSeries; }

    public List<EventLogDto> getEventLogs() { return eventLogs; }
    public void setEventLogs(List<EventLogDto> eventLogs) { this.eventLogs = eventLogs; }

    public BottleneckReportDto getBottleneckReport() { return bottleneckReport; }
    public void setBottleneckReport(BottleneckReportDto bottleneckReport) { this.bottleneckReport = bottleneckReport; }
}
