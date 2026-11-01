package com.simulab.engine.dto;

import java.util.ArrayList;
import java.util.List;

public class ComparisonResultDto {
    private String architectureAName;
    private String architectureBName;
    private SimulationResultDto resultA;
    private SimulationResultDto resultB;

    private double throughputDeltaPercent;
    private double p95LatencyDeltaPercent;
    private double errorRateDeltaPercent;

    private String winner; // ARCHITECTURE_A, ARCHITECTURE_B, EQUIVALENT
    private String summary;
    private List<String> tradeoffs = new ArrayList<>();

    public ComparisonResultDto() {}

    public String getArchitectureAName() { return architectureAName; }
    public void setArchitectureAName(String architectureAName) { this.architectureAName = architectureAName; }

    public String getArchitectureBName() { return architectureBName; }
    public void setArchitectureBName(String architectureBName) { this.architectureBName = architectureBName; }

    public SimulationResultDto getResultA() { return resultA; }
    public void setResultA(SimulationResultDto resultA) { this.resultA = resultA; }

    public SimulationResultDto getResultB() { return resultB; }
    public void setResultB(SimulationResultDto resultB) { this.resultB = resultB; }

    public double getThroughputDeltaPercent() { return throughputDeltaPercent; }
    public void setThroughputDeltaPercent(double throughputDeltaPercent) { this.throughputDeltaPercent = throughputDeltaPercent; }

    public double getP95LatencyDeltaPercent() { return p95LatencyDeltaPercent; }
    public void setP95LatencyDeltaPercent(double p95LatencyDeltaPercent) { this.p95LatencyDeltaPercent = p95LatencyDeltaPercent; }

    public double getErrorRateDeltaPercent() { return errorRateDeltaPercent; }
    public void setErrorRateDeltaPercent(double errorRateDeltaPercent) { this.errorRateDeltaPercent = errorRateDeltaPercent; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getTradeoffs() { return tradeoffs; }
    public void setTradeoffs(List<String> tradeoffs) { this.tradeoffs = tradeoffs; }
}
