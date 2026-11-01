package com.simulab.engine.analysis;

import com.simulab.engine.dto.BottleneckReportDto;
import com.simulab.engine.dto.NodeMetricDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.engine.model.ComponentType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BottleneckDetector {

    public static BottleneckReportDto analyze(SimulationResultDto result) {
        BottleneckReportDto report = new BottleneckReportDto();
        List<String> observations = new ArrayList<>();

        if (result == null || result.getNodeMetrics() == null || result.getNodeMetrics().isEmpty()) {
            report.setHasBottleneck(false);
            report.setSeverity("HEALTHY");
            report.setFinding("No nodes configured in the architecture.");
            report.setRecommendation("Add components and configure traffic flow.");
            return report;
        }

        // Filter non-client nodes
        List<NodeMetricDto> backendNodes = result.getNodeMetrics().stream()
                .filter(n -> n.getComponentType() != ComponentType.CLIENT && n.getComponentType() != ComponentType.LOAD_BALANCER)
                .toList();

        if (backendNodes.isEmpty()) {
            report.setHasBottleneck(false);
            report.setSeverity("HEALTHY");
            report.setFinding("System contains only edge/client nodes.");
            report.setRecommendation("Add backend processing nodes (API Servers, Databases, Caches) to simulate workload execution.");
            return report;
        }

        // Rank by queue wait time & utilization
        NodeMetricDto worstWaitNode = backendNodes.stream()
                .max(Comparator.comparingDouble(NodeMetricDto::getAvgQueueWaitTimeMs))
                .orElse(null);

        NodeMetricDto highestUtilNode = backendNodes.stream()
                .max(Comparator.comparingDouble(NodeMetricDto::getUtilizationPercent))
                .orElse(null);

        NodeMetricDto highestErrorNode = backendNodes.stream()
                .max(Comparator.comparingDouble(NodeMetricDto::getErrorRatePercent))
                .orElse(null);

        // General observations
        if (result.getErrorRatePercent() > 0.0) {
            observations.add(String.format("Global error rate reached %.1f%% with %d failed/timed out requests.",
                    result.getErrorRatePercent(), result.getTotalRequestsFailed() + result.getTotalRequestsTimedOut()));
        }

        if (result.getP99LatencyMs() > 500.0) {
            observations.add(String.format("High tail latency: P99 latency reached %.1fms (P50 is %.1fms).",
                    result.getP99LatencyMs(), result.getP50LatencyMs()));
        }

        // Detect critical bottlenecks
        boolean hasCriticalBottleneck = false;
        NodeMetricDto bottleneckNode = null;
        String bottleneckType = "NONE";
        String finding = "";
        String recommendation = "";
        String impact = "";

        // Check 1: Significant errors on a specific node
        if (highestErrorNode != null && highestErrorNode.getErrorRatePercent() > 5.0) {
            hasCriticalBottleneck = true;
            bottleneckNode = highestErrorNode;
            bottleneckType = "NODE_FAILURE_OR_CAPACITY_EXCEEDED";
            finding = String.format("Node '%s' (%s) experienced an error rate of %.1f%% (%d requests dropped).",
                    highestErrorNode.getNodeName(), highestErrorNode.getComponentType(),
                    highestErrorNode.getErrorRatePercent(), highestErrorNode.getRequestsFailed());

            if (highestErrorNode.getComponentType() == ComponentType.DATABASE) {
                recommendation = "Database connection pool saturated or node crashed. Increase database connection pool size, introduce connection pooling (e.g. HikariCP / PgBouncer), or add a caching layer.";
                impact = "Adding a Cache layer in front of the database typically shields 70-90% of read traffic, preventing database thread exhaustion.";
            } else if (highestErrorNode.getComponentType() == ComponentType.API_SERVER) {
                recommendation = "Scale API Server horizontally by adding more instances or increase threads per instance.";
                impact = "Horizontal scaling to 3+ instances distributes concurrency and eliminates queue overflows.";
            } else if (highestErrorNode.getComponentType() == ComponentType.MESSAGE_QUEUE) {
                recommendation = "Message queue buffer overflowed. Increase queue capacity or scale up downstream Worker consumers.";
                impact = "Scaling worker consumers allows asynchronous drain of bursts without dropping messages.";
            } else {
                recommendation = "Investigate node resource allocation or network failure injection.";
                impact = "Removes cascading dropped requests across downstream services.";
            }
        }
        // Check 2: Severe Queue Wait Time
        else if (worstWaitNode != null && worstWaitNode.getAvgQueueWaitTimeMs() > 40.0) {
            bottleneckNode = worstWaitNode;
            hasCriticalBottleneck = worstWaitNode.getAvgQueueWaitTimeMs() > 100.0 || result.getErrorRatePercent() > 0.0;
            bottleneckType = "QUEUE_CONTENTION";
            finding = String.format("Node '%s' (%s) suffers from heavy queue contention with an average queue wait of %.1fms (vs processing time of %.1fms). Peak queue depth: %d.",
                    worstWaitNode.getNodeName(), worstWaitNode.getComponentType(),
                    worstWaitNode.getAvgQueueWaitTimeMs(), worstWaitNode.getAvgProcessingTimeMs(), worstWaitNode.getPeakQueueDepth());

            if (worstWaitNode.getComponentType() == ComponentType.DATABASE) {
                recommendation = "Database connection pool is too small for incoming concurrency. Increase connection pool size from "
                        + worstWaitNode.getMaxCapacity() + " or add an in-memory Cache layer.";
                impact = "Will reduce queue wait times near 0ms for cached queries and prevent request queue buildups.";
            } else if (worstWaitNode.getComponentType() == ComponentType.API_SERVER) {
                recommendation = "API Server thread pool is saturated. Scale API Server instances or increase threads per instance.";
                impact = "Decreases P95 latency by up to 60-80% under current traffic.";
            } else if (worstWaitNode.getComponentType() == ComponentType.WORKER) {
                recommendation = "Workers cannot process jobs as fast as arrival rate. Increase Worker pool size.";
                impact = "Enables faster consumption from upstream queue.";
            } else {
                recommendation = "Optimize component processing capacity or parallelize workloads.";
                impact = "Smoothes out request throughput and eliminates queue backlogs.";
            }
        }
        // Check 3: High Resource Utilization
        else if (highestUtilNode != null && highestUtilNode.getUtilizationPercent() > 75.0) {
            bottleneckNode = highestUtilNode;
            hasCriticalBottleneck = highestUtilNode.getUtilizationPercent() > 90.0;
            bottleneckType = "RESOURCE_SATURATION";
            finding = String.format("Node '%s' (%s) is operating near peak capacity at %.1f%% average utilization (Peak concurrency: %d / %d max).",
                    highestUtilNode.getNodeName(), highestUtilNode.getComponentType(),
                    highestUtilNode.getUtilizationPercent(), highestUtilNode.getPeakConcurrency(), highestUtilNode.getMaxCapacity());

            recommendation = String.format("Allocate more resources or horizontally scale node '%s' to avoid latency degradation as load grows.",
                    highestUtilNode.getNodeName());
            impact = "Maintains system headroom for traffic spikes.";
        }

        if (bottleneckNode != null) {
            report.setHasBottleneck(true);
            report.setBottleneckNodeId(bottleneckNode.getNodeId());
            report.setBottleneckNodeName(bottleneckNode.getNodeName());
            report.setBottleneckType(bottleneckType);
            report.setSeverity(hasCriticalBottleneck ? "CRITICAL" : "WARNING");
            report.setFinding(finding);
            report.setRecommendation(recommendation);
            report.setEstimatedImpact(impact);

            // Mark node in result
            for (NodeMetricDto nm : result.getNodeMetrics()) {
                if (nm.getNodeId().equals(bottleneckNode.getNodeId())) {
                    nm.setBottleneck(true);
                    nm.setBottleneckReason(finding);
                }
            }
        } else {
            report.setHasBottleneck(false);
            report.setSeverity("HEALTHY");
            report.setFinding("Architecture is healthy under current workload. No component exceeded safe capacity limits.");
            report.setRecommendation("Current architecture can sustain higher RPS. Try increasing client traffic to find the breaking point.");
            report.setEstimatedImpact("Zero dropped requests and low latency overhead.");
        }

        report.setObservations(observations);
        return report;
    }
}
