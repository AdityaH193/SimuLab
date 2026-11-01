package com.simulab.engine.analysis;

import com.simulab.engine.dto.ComparisonResultDto;
import com.simulab.engine.dto.SimulationResultDto;

import java.util.ArrayList;
import java.util.List;

public class ArchitectureComparator {

    public static ComparisonResultDto compare(
            String archAName,
            SimulationResultDto resultA,
            String archBName,
            SimulationResultDto resultB) {

        ComparisonResultDto comparison = new ComparisonResultDto();
        comparison.setArchitectureAName(archAName != null ? archAName : "Architecture A");
        comparison.setArchitectureBName(archBName != null ? archBName : "Architecture B");
        comparison.setResultA(resultA);
        comparison.setResultB(resultB);

        if (resultA == null || resultB == null) {
            comparison.setSummary("Both architectures must have valid simulation runs to compare.");
            return comparison;
        }

        // Throughput delta
        double tA = Math.max(0.001, resultA.getThroughputRps());
        double tB = resultB.getThroughputRps();
        double throughputDelta = ((tB - tA) / tA) * 100.0;
        comparison.setThroughputDeltaPercent(round(throughputDelta));

        // P95 Latency delta
        double pA = Math.max(0.001, resultA.getP95LatencyMs());
        double pB = resultB.getP95LatencyMs();
        double latencyDelta = ((pB - pA) / pA) * 100.0;
        comparison.setP95LatencyDeltaPercent(round(latencyDelta));

        // Error rate delta
        double errorDelta = resultB.getErrorRatePercent() - resultA.getErrorRatePercent();
        comparison.setErrorRateDeltaPercent(round(errorDelta));

        // Score & winner determination
        int scoreA = 0;
        int scoreB = 0;
        List<String> tradeoffs = new ArrayList<>();

        if (resultB.getErrorRatePercent() < resultA.getErrorRatePercent()) {
            scoreB += 3;
            tradeoffs.add(String.format("Resilience: '%s' reduced error rate by %.1f%% (from %.1f%% down to %.1f%%).",
                    comparison.getArchitectureBName(), Math.abs(errorDelta), resultA.getErrorRatePercent(), resultB.getErrorRatePercent()));
        } else if (resultA.getErrorRatePercent() < resultB.getErrorRatePercent()) {
            scoreA += 3;
            tradeoffs.add(String.format("Resilience: '%s' maintained a lower error rate (%.1f%% vs %.1f%%).",
                    comparison.getArchitectureAName(), resultA.getErrorRatePercent(), resultB.getErrorRatePercent()));
        }

        if (latencyDelta < -5.0) {
            scoreB += 2;
            tradeoffs.add(String.format("Performance: '%s' improved P95 latency by %.1f%% (%.1fms vs %.1fms).",
                    comparison.getArchitectureBName(), Math.abs(latencyDelta), resultB.getP95LatencyMs(), resultA.getP95LatencyMs()));
        } else if (latencyDelta > 5.0) {
            scoreA += 2;
            tradeoffs.add(String.format("Performance: '%s' has %.1f%% lower P95 latency (%.1fms vs %.1fms).",
                    comparison.getArchitectureAName(), latencyDelta, resultA.getP95LatencyMs(), resultB.getP95LatencyMs()));
        }

        if (throughputDelta > 5.0) {
            scoreB += 2;
            tradeoffs.add(String.format("Scalability: '%s' achieved %.1f%% higher sustained throughput (%.1f RPS vs %.1f RPS).",
                    comparison.getArchitectureBName(), throughputDelta, resultB.getThroughputRps(), resultA.getThroughputRps()));
        } else if (throughputDelta < -5.0) {
            scoreA += 2;
            tradeoffs.add(String.format("Scalability: '%s' achieved higher sustained throughput (%.1f RPS vs %.1f RPS).",
                    comparison.getArchitectureAName(), resultA.getThroughputRps(), resultB.getThroughputRps()));
        }

        // Bottlenecks comparison
        boolean botA = resultA.getBottleneckReport() != null && resultA.getBottleneckReport().isHasBottleneck();
        boolean botB = resultB.getBottleneckReport() != null && resultB.getBottleneckReport().isHasBottleneck();

        if (botA && !botB) {
            scoreB += 2;
            tradeoffs.add(String.format("Stability: '%s' resolved the critical bottleneck identified in '%s' (%s).",
                    comparison.getArchitectureBName(), comparison.getArchitectureAName(),
                    resultA.getBottleneckReport().getBottleneckNodeName()));
        } else if (!botA && botB) {
            scoreA += 2;
            tradeoffs.add(String.format("Stability: '%s' operates without bottlenecks, whereas '%s' encountered saturation at '%s'.",
                    comparison.getArchitectureAName(), comparison.getArchitectureBName(),
                    resultB.getBottleneckReport().getBottleneckNodeName()));
        }

        if (scoreB > scoreA) {
            comparison.setWinner("ARCHITECTURE_B");
            comparison.setSummary(String.format("'%s' outperforms '%s' overall under this workload, providing better latency and lower drop rates.",
                    comparison.getArchitectureBName(), comparison.getArchitectureAName()));
        } else if (scoreA > scoreB) {
            comparison.setWinner("ARCHITECTURE_A");
            comparison.setSummary(String.format("'%s' demonstrates superior performance or lower overhead compared to '%s'.",
                    comparison.getArchitectureAName(), comparison.getArchitectureBName()));
        } else {
            comparison.setWinner("EQUIVALENT");
            comparison.setSummary("Both architectures perform equivalently under the configured workload parameters.");
        }

        comparison.setTradeoffs(tradeoffs);
        return comparison;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
