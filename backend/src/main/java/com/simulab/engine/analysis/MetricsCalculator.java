package com.simulab.engine.analysis;

import com.simulab.engine.dto.NodeMetricDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.engine.dto.TimeSeriesPointDto;
import com.simulab.engine.model.SimNode;
import com.simulab.engine.model.SimRequest;
import com.simulab.engine.model.RequestStatus;
import com.simulab.engine.model.SimulationScenario;

import java.util.*;
import java.util.stream.Collectors;

public class MetricsCalculator {

    public static SimulationResultDto calculate(
            SimulationScenario scenario,
            List<SimRequest> requests,
            double totalSimDurationMs) {

        SimulationResultDto result = new SimulationResultDto();
        double durationSec = Math.max(0.001, totalSimDurationMs / 1000.0);
        result.setDurationSec(durationSec);

        long totalGenerated = requests.size();
        result.setTotalRequestsGenerated(totalGenerated);

        List<SimRequest> completed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .collect(Collectors.toList());

        List<SimRequest> failed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.FAILED)
                .collect(Collectors.toList());

        List<SimRequest> timedOut = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.TIMEOUT)
                .collect(Collectors.toList());

        result.setTotalRequestsCompleted(completed.size());
        result.setTotalRequestsFailed(failed.size());
        result.setTotalRequestsTimedOut(timedOut.size());

        double totalErrors = failed.size() + timedOut.size();
        result.setErrorRatePercent(totalGenerated > 0 ? (totalErrors / totalGenerated) * 100.0 : 0.0);
        result.setThroughputRps(completed.size() / durationSec);

        // Latency percentiles
        List<Double> latencies = completed.stream()
                .map(SimRequest::getLatencyMs)
                .sorted()
                .collect(Collectors.toList());

        if (!latencies.isEmpty()) {
            result.setMinLatencyMs(round(latencies.get(0)));
            result.setMaxLatencyMs(round(latencies.get(latencies.size() - 1)));
            result.setMeanLatencyMs(round(latencies.stream().mapToDouble(Double::doubleValue).average().orElse(0.0)));
            result.setP50LatencyMs(round(getPercentile(latencies, 50.0)));
            result.setP90LatencyMs(round(getPercentile(latencies, 90.0)));
            result.setP95LatencyMs(round(getPercentile(latencies, 95.0)));
            result.setP99LatencyMs(round(getPercentile(latencies, 99.0)));
        } else {
            result.setMinLatencyMs(0.0);
            result.setMaxLatencyMs(0.0);
            result.setMeanLatencyMs(0.0);
            result.setP50LatencyMs(0.0);
            result.setP90LatencyMs(0.0);
            result.setP95LatencyMs(0.0);
            result.setP99LatencyMs(0.0);
        }

        // Calculate node-level metrics
        List<NodeMetricDto> nodeMetrics = new ArrayList<>();
        for (SimNode node : scenario.getGraph().getNodes()) {
            NodeMetricDto dto = new NodeMetricDto();
            dto.setNodeId(node.getId());
            dto.setNodeName(node.getName());
            dto.setComponentType(node.getType());
            dto.setRequestsReceived(node.getTotalRequestsReceived());
            dto.setRequestsCompleted(node.getTotalRequestsCompleted());
            dto.setRequestsFailed(node.getTotalRequestsFailed());
            dto.setPeakConcurrency(node.getPeakConcurrency());
            dto.setPeakQueueDepth(node.getPeakQueueDepth());
            dto.setMaxCapacity(node.getMaxConcurrency());

            long nodeTotal = node.getTotalRequestsReceived();
            dto.setErrorRatePercent(nodeTotal > 0 ? (node.getTotalRequestsFailed() * 100.0 / nodeTotal) : 0.0);

            long nodeCompleted = node.getTotalRequestsCompleted();
            dto.setAvgProcessingTimeMs(nodeCompleted > 0 ? round(node.getTotalProcessingTime() / nodeCompleted) : 0.0);
            dto.setAvgQueueWaitTimeMs(nodeCompleted > 0 ? round(node.getTotalQueueWaitTime() / nodeCompleted) : 0.0);

            // Utilization % = (busyTimeMs / totalSimDurationMs) * 100
            double busyTime = node.getBusyTimeMs(totalSimDurationMs);
            double util = (totalSimDurationMs > 0) ? (busyTime / (totalSimDurationMs * Math.max(1, node.getMaxConcurrency()))) * 100.0 : 0.0;
            dto.setUtilizationPercent(round(Math.min(100.0, Math.max(0.0, util))));

            nodeMetrics.add(dto);
        }
        result.setNodeMetrics(nodeMetrics);

        // Time Series generation (1-second intervals)
        int totalSeconds = (int) Math.ceil(durationSec);
        List<TimeSeriesPointDto> timeSeries = new ArrayList<>();

        for (int sec = 0; sec < totalSeconds; sec++) {
            double startSecMs = sec * 1000.0;
            double endSecMs = (sec + 1) * 1000.0;

            List<SimRequest> bucketCompleted = completed.stream()
                    .filter(r -> r.getEndTimeMs() >= startSecMs && r.getEndTimeMs() < endSecMs)
                    .collect(Collectors.toList());

            List<SimRequest> bucketFailed = requests.stream()
                    .filter(r -> (r.getStatus() == RequestStatus.FAILED || r.getStatus() == RequestStatus.TIMEOUT)
                            && r.getEndTimeMs() >= startSecMs && r.getEndTimeMs() < endSecMs)
                    .collect(Collectors.toList());

            long inFlight = requests.stream()
                    .filter(r -> r.getStartTimeMs() <= endSecMs && (r.getStatus() == RequestStatus.IN_FLIGHT || r.getEndTimeMs() > endSecMs))
                    .count();

            double secThroughput = bucketCompleted.size();
            double secAvgLatency = bucketCompleted.isEmpty() ? 0.0 :
                    bucketCompleted.stream().mapToDouble(SimRequest::getLatencyMs).average().orElse(0.0);

            List<Double> secLatencies = bucketCompleted.stream().map(SimRequest::getLatencyMs).sorted().collect(Collectors.toList());
            double secP95 = secLatencies.isEmpty() ? 0.0 : getPercentile(secLatencies, 95.0);

            int totalQueueDepth = scenario.getGraph().getNodes().stream()
                    .mapToInt(SimNode::getPeakQueueDepth)
                    .sum();

            timeSeries.add(new TimeSeriesPointDto(
                    sec + 1.0,
                    round(secThroughput),
                    round(secAvgLatency),
                    round(secP95),
                    (int) inFlight,
                    bucketFailed.size(),
                    totalQueueDepth
            ));
        }
        result.setTimeSeries(timeSeries);

        return result;
    }

    private static double getPercentile(List<Double> sortedValues, double percentile) {
        if (sortedValues == null || sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        return sortedValues.get(index);
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
