package com.simulab.engine.core;

import com.simulab.engine.analysis.BottleneckDetector;
import com.simulab.engine.analysis.MetricsCalculator;
import com.simulab.engine.dto.EventLogDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.engine.event.EventType;
import com.simulab.engine.event.SimEvent;
import com.simulab.engine.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class SimulationEngine {

    private static final int MAX_LOG_EVENTS = 600;

    public SimulationResultDto execute(SimulationScenario scenario) {
        if (scenario == null || scenario.getGraph() == null) {
            throw new IllegalArgumentException("Simulation scenario and graph must not be null");
        }

        SimulationGraph graph = scenario.getGraph();
        double durationMs = Math.max(1000.0, scenario.getDurationSec() * 1000.0);
        Random random = new Random(scenario.getRandomSeed());
        AtomicLong eventIdSeq = new AtomicLong(1);

        PriorityQueue<SimEvent> eventQueue = new PriorityQueue<>();
        Map<String, SimRequest> requestMap = new HashMap<>();
        Map<String, Double> queueEntryTimes = new HashMap<>();
        List<EventLogDto> eventLogs = new ArrayList<>();

        // Reset runtime state of nodes and edges
        for (SimNode node : graph.getNodes()) {
            node.resetRuntimeState();
        }
        for (SimEdge edge : graph.getEdges()) {
            edge.setSevered(false);
            edge.setFailureLatencySpikeMs(0.0);
        }

        // 1. Schedule Failures
        if (scenario.getFailures() != null) {
            for (FailureConfig failure : scenario.getFailures()) {
                double startMs = failure.getStartSec() * 1000.0;
                double endMs = startMs + (failure.getDurationSec() * 1000.0);

                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        startMs,
                        EventType.FAILURE_INJECT,
                        null,
                        failure.getTargetId(),
                        null,
                        failure.getType() + " started (magnitude: " + failure.getMagnitude() + ")"
                ));

                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        endMs,
                        EventType.FAILURE_RECOVER,
                        null,
                        failure.getTargetId(),
                        null,
                        failure.getType() + " recovered"
                ));
            }
        }

        // 2. Schedule Client Traffic
        List<SimNode> clientNodes = graph.getClientNodes();
        if (clientNodes.isEmpty() && !graph.getNodes().isEmpty()) {
            // If no explicit Client node, use the first node as entry point if possible
            clientNodes = Collections.singletonList(graph.getNodes().get(0));
        }

        long reqSeq = 1;
        for (SimNode client : clientNodes) {
            double rps = Math.max(1.0, client.getWorkloadRps());
            long totalReqs = client.getTotalRequests() > 0 ?
                    client.getTotalRequests() : Math.round(rps * scenario.getDurationSec());

            double currentTimeMs = 0.0;
            for (int i = 0; i < totalReqs; i++) {
                if (currentTimeMs >= durationMs) break;

                double intervalMs;
                if (client.getWorkloadDistribution() == WorkloadDistribution.POISSON) {
                    double u = Math.max(0.00001, random.nextDouble());
                    intervalMs = -Math.log(u) * (1000.0 / rps);
                } else if (client.getWorkloadDistribution() == WorkloadDistribution.BURST) {
                    // 70% chance of small interval, 30% chance of standard interval
                    boolean inBurst = (currentTimeMs % 4000.0) < 1000.0;
                    intervalMs = inBurst ? (1000.0 / (rps * 3.0)) : (1000.0 / (rps * 0.4));
                } else {
                    intervalMs = 1000.0 / rps;
                }

                currentTimeMs += Math.max(0.1, intervalMs);
                if (currentTimeMs >= durationMs) break;

                String reqId = "req-" + (reqSeq++);
                SimRequest simReq = new SimRequest(reqId, currentTimeMs);
                requestMap.put(reqId, simReq);

                // Schedule request arrival at client
                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        currentTimeMs,
                        EventType.REQUEST_ARRIVAL,
                        reqId,
                        client.getId(),
                        null,
                        "Request initiated"
                ));

                // Schedule timeout event
                double timeoutTimeMs = currentTimeMs + client.getTimeoutMs();
                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        timeoutTimeMs,
                        EventType.REQUEST_TIMEOUT,
                        reqId,
                        client.getId(),
                        null,
                        "Client timeout triggered"
                ));
            }
        }

        // 3. Event Loop Execution
        double maxSimulationCutoffMs = durationMs + 8000.0; // Grace period for in-flight requests to complete
        double lastSimTime = 0.0;

        while (!eventQueue.isEmpty()) {
            SimEvent event = eventQueue.poll();
            lastSimTime = event.getTimestampMs();

            if (lastSimTime > maxSimulationCutoffMs) {
                break;
            }

            // Log key events for visualization
            if (eventLogs.size() < MAX_LOG_EVENTS) {
                eventLogs.add(new EventLogDto(
                        event.getEventId(),
                        event.getTimestampMs(),
                        event.getType(),
                        event.getRequestId(),
                        event.getNodeId(),
                        event.getTargetNodeId(),
                        event.getDetails()
                ));
            }

            // Process event by type
            switch (event.getType()) {
                case FAILURE_INJECT -> handleFailureInject(event, graph, scenario);
                case FAILURE_RECOVER -> handleFailureRecover(event, graph, scenario);
                case REQUEST_TIMEOUT -> handleRequestTimeout(event, requestMap);
                case REQUEST_ARRIVAL -> handleRequestArrival(event, graph, requestMap, eventQueue, eventIdSeq, random);
                case DISPATCH -> handleDispatch(event, graph, requestMap, eventQueue, eventIdSeq, random);
                case PROCESS_START -> handleProcessStart(event, graph, requestMap, eventQueue, eventIdSeq, queueEntryTimes, random);
                case PROCESS_COMPLETE -> handleProcessComplete(event, graph, requestMap, eventQueue, eventIdSeq, queueEntryTimes, random);
                case QUEUE_ENQUEUE -> {
                    SimRequest req = requestMap.get(event.getRequestId());
                    SimNode node = graph.getNode(event.getNodeId());
                    if (req != null && node != null) {
                        handleMessageQueue(event, node, req, eventQueue, eventIdSeq, queueEntryTimes);
                    }
                }
                case QUEUE_DEQUEUE -> handleQueueDequeue(event, graph, requestMap, eventQueue, eventIdSeq, queueEntryTimes, random);
                default -> {}
            }
        }

        // Mark any remaining IN_FLIGHT requests at end of cutoff as TIMEOUT
        for (SimRequest req : requestMap.values()) {
            if (req.getStatus() == RequestStatus.PENDING || req.getStatus() == RequestStatus.IN_FLIGHT) {
                req.markTimeout(lastSimTime);
            }
        }

        // 4. Calculate Metrics & Bottlenecks
        SimulationResultDto result = MetricsCalculator.calculate(scenario, new ArrayList<>(requestMap.values()), lastSimTime);
        result.setRunId(UUID.randomUUID().toString());
        result.setEventLogs(eventLogs);

        // Detect bottlenecks
        result.setBottleneckReport(BottleneckDetector.analyze(result));

        return result;
    }

    private void handleFailureInject(SimEvent event, SimulationGraph graph, SimulationScenario scenario) {
        String targetId = event.getNodeId();
        SimNode node = graph.getNode(targetId);
        if (node != null) {
            FailureConfig fc = findFailureConfig(scenario, targetId);
            if (fc != null) {
                switch (fc.getType()) {
                    case NODE_CRASH -> node.setFailed(true);
                    case LATENCY_SPIKE -> node.setFailureLatencySpikeMs(fc.getMagnitude());
                    case CAPACITY_DROP -> node.setCapacityReductionFactor(Math.max(0.1, 1.0 - (fc.getMagnitude() / 100.0)));
                    default -> {}
                }
            } else {
                node.setFailed(true);
            }
            return;
        }

        SimEdge edge = graph.getEdge(targetId);
        if (edge != null) {
            FailureConfig fc = findFailureConfig(scenario, targetId);
            if (fc != null && fc.getType() == FailureConfig.FailureType.LATENCY_SPIKE) {
                edge.setFailureLatencySpikeMs(fc.getMagnitude());
            } else {
                edge.setSevered(true);
            }
        }
    }

    private void handleFailureRecover(SimEvent event, SimulationGraph graph, SimulationScenario scenario) {
        String targetId = event.getNodeId();
        SimNode node = graph.getNode(targetId);
        if (node != null) {
            node.setFailed(false);
            node.setFailureLatencySpikeMs(0.0);
            node.setCapacityReductionFactor(1.0);
            return;
        }

        SimEdge edge = graph.getEdge(targetId);
        if (edge != null) {
            edge.setSevered(false);
            edge.setFailureLatencySpikeMs(0.0);
        }
    }

    private FailureConfig findFailureConfig(SimulationScenario scenario, String targetId) {
        if (scenario.getFailures() == null) return null;
        return scenario.getFailures().stream()
                .filter(f -> targetId.equals(f.getTargetId()))
                .findFirst()
                .orElse(null);
    }

    private void handleRequestTimeout(SimEvent event, Map<String, SimRequest> requestMap) {
        SimRequest req = requestMap.get(event.getRequestId());
        if (req != null && (req.getStatus() == RequestStatus.PENDING || req.getStatus() == RequestStatus.IN_FLIGHT)) {
            req.markTimeout(event.getTimestampMs());
        }
    }

    private void handleRequestArrival(
            SimEvent event,
            SimulationGraph graph,
            Map<String, SimRequest> requestMap,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Random random) {

        SimRequest req = requestMap.get(event.getRequestId());
        if (req == null || req.getStatus() == RequestStatus.TIMEOUT) return;

        req.setStatus(RequestStatus.IN_FLIGHT);
        String currentNodeId = event.getNodeId();
        SimNode clientNode = graph.getNode(currentNodeId);

        List<SimEdge> outgoingEdges = graph.getOutgoingEdges(currentNodeId);
        if (outgoingEdges.isEmpty()) {
            req.markFailed(event.getTimestampMs(), "Client has no connected downstream services");
            return;
        }

        // Pick downstream edge
        SimEdge chosenEdge = outgoingEdges.get(random.nextInt(outgoingEdges.size()));
        double dispatchTime = event.getTimestampMs() + chosenEdge.getLatencyMs();

        eventQueue.add(new SimEvent(
                eventIdSeq.getAndIncrement(),
                dispatchTime,
                EventType.DISPATCH,
                req.getId(),
                currentNodeId,
                chosenEdge.getTargetNodeId(),
                "Transmitting over edge " + chosenEdge.getId()
        ));
    }

    private void handleDispatch(
            SimEvent event,
            SimulationGraph graph,
            Map<String, SimRequest> requestMap,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Random random) {

        SimRequest req = requestMap.get(event.getRequestId());
        if (req == null || req.getStatus() != RequestStatus.IN_FLIGHT) return;

        String sourceId = event.getNodeId();
        String targetId = event.getTargetNodeId();

        // Check edge validity
        List<SimEdge> edges = graph.getOutgoingEdges(sourceId).stream()
                .filter(e -> targetId.equals(e.getTargetNodeId()))
                .toList();
        SimEdge edge = edges.isEmpty() ? null : edges.get(0);

        if (edge != null && edge.isSevered()) {
            req.markFailed(event.getTimestampMs(), "Network partition: connection severed between " + sourceId + " and " + targetId);
            return;
        }

        if (edge != null && edge.getPacketLossPercent() > 0.0) {
            if (random.nextDouble() * 100.0 < edge.getPacketLossPercent()) {
                req.markFailed(event.getTimestampMs(), "Packet loss encountered between " + sourceId + " and " + targetId);
                return;
            }
        }

        SimNode targetNode = graph.getNode(targetId);
        if (targetNode == null) {
            req.markFailed(event.getTimestampMs(), "Target node " + targetId + " not found");
            return;
        }

        req.addTrace(event.getTimestampMs(), targetId, "Arrived at " + targetNode.getName());

        // Process request at target node
        eventQueue.add(new SimEvent(
                eventIdSeq.getAndIncrement(),
                event.getTimestampMs(),
                EventType.PROCESS_START,
                req.getId(),
                targetId,
                null,
                "Begin processing at " + targetNode.getName()
        ));
    }

    private void handleProcessStart(
            SimEvent event,
            SimulationGraph graph,
            Map<String, SimRequest> requestMap,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Map<String, Double> queueEntryTimes,
            Random random) {

        SimRequest req = requestMap.get(event.getRequestId());
        if (req == null || req.getStatus() != RequestStatus.IN_FLIGHT) return;

        SimNode node = graph.getNode(event.getNodeId());
        if (node == null) return;

        node.incrementRequestsReceived();

        // Check if node is failed
        if (node.isFailed()) {
            node.incrementRequestsFailed();
            req.markFailed(event.getTimestampMs(), "Node crashed: " + node.getName() + " is down");
            return;
        }

        // Check component type specifics
        switch (node.getType()) {
            case LOAD_BALANCER -> handleLoadBalancer(event, node, graph, req, eventQueue, eventIdSeq, random);
            case CACHE -> handleCache(event, node, graph, req, eventQueue, eventIdSeq, random);
            case MESSAGE_QUEUE -> handleMessageQueue(event, node, req, eventQueue, eventIdSeq, queueEntryTimes);
            default -> handleServerNode(event, node, graph, req, eventQueue, eventIdSeq, queueEntryTimes, random);
        }
    }

    private void handleLoadBalancer(
            SimEvent event,
            SimNode lbNode,
            SimulationGraph graph,
            SimRequest req,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Random random) {

        List<SimEdge> outgoingEdges = graph.getOutgoingEdges(lbNode.getId());
        if (outgoingEdges.isEmpty()) {
            lbNode.incrementRequestsFailed();
            req.markFailed(event.getTimestampMs(), "Load Balancer has no backend targets");
            return;
        }

        // Filter out severed connections
        List<SimEdge> healthyEdges = outgoingEdges.stream()
                .filter(e -> !e.isSevered())
                .toList();

        if (healthyEdges.isEmpty()) {
            lbNode.incrementRequestsFailed();
            req.markFailed(event.getTimestampMs(), "All backend connections severed from " + lbNode.getName());
            return;
        }

        SimEdge chosenEdge;
        if (lbNode.getLbAlgorithm() == LoadBalancingAlgorithm.ROUND_ROBIN) {
            int idx = Math.abs(lbNode.getRoundRobinIndex() % healthyEdges.size());
            lbNode.setRoundRobinIndex(idx + 1);
            chosenEdge = healthyEdges.get(idx);
        } else if (lbNode.getLbAlgorithm() == LoadBalancingAlgorithm.LEAST_CONNECTIONS) {
            chosenEdge = healthyEdges.stream()
                    .min(Comparator.comparingInt(e -> {
                        SimNode target = graph.getNode(e.getTargetNodeId());
                        return target != null ? target.getCurrentActiveConcurrency() : 0;
                    }))
                    .orElse(healthyEdges.get(0));
        } else {
            chosenEdge = healthyEdges.get(random.nextInt(healthyEdges.size()));
        }

        lbNode.incrementRequestsCompleted();
        double dispatchTime = event.getTimestampMs() + 0.5 + chosenEdge.getLatencyMs();

        eventQueue.add(new SimEvent(
                eventIdSeq.getAndIncrement(),
                dispatchTime,
                EventType.DISPATCH,
                req.getId(),
                lbNode.getId(),
                chosenEdge.getTargetNodeId(),
                "Routed via " + lbNode.getLbAlgorithm() + " to " + chosenEdge.getTargetNodeId()
        ));
    }

    private void handleCache(
            SimEvent event,
            SimNode cacheNode,
            SimulationGraph graph,
            SimRequest req,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Random random) {

        cacheNode.recordConcurrencyIncrease(event.getTimestampMs());
        boolean hit = (random.nextDouble() * 100.0) < cacheNode.getCacheHitRatio();
        double cacheLatency = cacheNode.getEffectiveLatency(cacheNode.getCacheLatencyMs());

        if (hit) {
            // Cache HIT -> Request successfully satisfied!
            cacheNode.incrementRequestsCompleted();
            cacheNode.addProcessingTime(cacheLatency);
            cacheNode.recordConcurrencyDecrease(event.getTimestampMs() + cacheLatency);

            req.addTrace(event.getTimestampMs() + cacheLatency, cacheNode.getId(), "Cache HIT (served in " + cacheLatency + "ms)");
            req.markCompleted(event.getTimestampMs() + cacheLatency);
        } else {
            // Cache MISS -> Forward to downstream nodes (e.g. Database)
            cacheNode.recordConcurrencyDecrease(event.getTimestampMs() + cacheLatency);
            req.addTrace(event.getTimestampMs() + cacheLatency, cacheNode.getId(), "Cache MISS: forwarding downstream");

            List<SimEdge> outgoing = graph.getOutgoingEdges(cacheNode.getId());
            if (outgoing.isEmpty()) {
                // If no downstream, complete request
                cacheNode.incrementRequestsCompleted();
                req.markCompleted(event.getTimestampMs() + cacheLatency);
            } else {
                SimEdge edge = outgoing.get(0);
                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        event.getTimestampMs() + cacheLatency + edge.getLatencyMs(),
                        EventType.DISPATCH,
                        req.getId(),
                        cacheNode.getId(),
                        edge.getTargetNodeId(),
                        "Cache miss forward"
                ));
            }
        }
    }

    private void handleMessageQueue(
            SimEvent event,
            SimNode mqNode,
            SimRequest req,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Map<String, Double> queueEntryTimes) {

        if (mqNode.getQueue().size() >= mqNode.getQueueCapacity()) {
            mqNode.incrementRequestsFailed();
            req.markFailed(event.getTimestampMs(), "Queue buffer overflow: capacity exceeded (" + mqNode.getQueueCapacity() + ")");
            return;
        }

        mqNode.recordQueueEnqueue(req);
        queueEntryTimes.put(req.getId() + "@" + mqNode.getId(), event.getTimestampMs());
        req.addTrace(event.getTimestampMs(), mqNode.getId(), "Enqueued in message queue (depth: " + mqNode.getQueue().size() + ")");

        // Schedule dequeue attempt
        double enqueueDelay = mqNode.getEffectiveLatency(mqNode.getEnqueueLatencyMs());
        eventQueue.add(new SimEvent(
                eventIdSeq.getAndIncrement(),
                event.getTimestampMs() + enqueueDelay,
                EventType.QUEUE_DEQUEUE,
                req.getId(),
                mqNode.getId(),
                null,
                "Ready for worker consumption"
        ));
    }

    private void handleQueueDequeue(
            SimEvent event,
            SimulationGraph graph,
            Map<String, SimRequest> requestMap,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Map<String, Double> queueEntryTimes,
            Random random) {

        SimRequest req = requestMap.get(event.getRequestId());
        if (req == null || req.getStatus() != RequestStatus.IN_FLIGHT) return;

        SimNode mqNode = graph.getNode(event.getNodeId());
        if (mqNode == null) return;

        List<SimEdge> outgoingEdges = graph.getOutgoingEdges(mqNode.getId());
        if (outgoingEdges.isEmpty()) {
            mqNode.incrementRequestsCompleted();
            req.markCompleted(event.getTimestampMs());
            return;
        }

        // Find available worker target
        SimEdge workerEdge = outgoingEdges.get(random.nextInt(outgoingEdges.size()));
        Double entryTime = queueEntryTimes.get(req.getId() + "@" + mqNode.getId());
        if (entryTime != null) {
            mqNode.recordQueueDequeue(event.getTimestampMs(), entryTime);
        }
        mqNode.incrementRequestsCompleted();

        eventQueue.add(new SimEvent(
                eventIdSeq.getAndIncrement(),
                event.getTimestampMs() + workerEdge.getLatencyMs(),
                EventType.DISPATCH,
                req.getId(),
                mqNode.getId(),
                workerEdge.getTargetNodeId(),
                "Dequeued to worker"
        ));
    }

    private void handleServerNode(
            SimEvent event,
            SimNode node,
            SimulationGraph graph,
            SimRequest req,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Map<String, Double> queueEntryTimes,
            Random random) {

        int maxCapacity = node.getMaxConcurrency();

        if (node.getCurrentActiveConcurrency() < maxCapacity) {
            // Concurrency slot available! Process immediately
            node.recordConcurrencyIncrease(event.getTimestampMs());
            double processingDuration = calculateProcessingDuration(node, random);

            eventQueue.add(new SimEvent(
                    eventIdSeq.getAndIncrement(),
                    event.getTimestampMs() + processingDuration,
                    EventType.PROCESS_COMPLETE,
                    req.getId(),
                    node.getId(),
                    null,
                    "Completed work on " + node.getName()
            ));
        } else {
            // Capacity saturated -> Check queue buffer limit
            int maxQueueBuffer = Math.max(50, maxCapacity * 4);
            if (node.getQueue().size() >= maxQueueBuffer) {
                node.incrementRequestsFailed();
                String reason = node.getType() == ComponentType.DATABASE ?
                        "Database connection pool exhausted (" + maxCapacity + " connections)" :
                        "Server overload: queue full at " + node.getName();
                req.markFailed(event.getTimestampMs(), reason);
            } else {
                node.recordQueueEnqueue(req);
                queueEntryTimes.put(req.getId() + "@" + node.getId(), event.getTimestampMs());
                req.addTrace(event.getTimestampMs(), node.getId(), "Queued awaiting available slot (queue size: " + node.getQueue().size() + ")");
            }
        }
    }

    private void handleProcessComplete(
            SimEvent event,
            SimulationGraph graph,
            Map<String, SimRequest> requestMap,
            PriorityQueue<SimEvent> eventQueue,
            AtomicLong eventIdSeq,
            Map<String, Double> queueEntryTimes,
            Random random) {

        SimRequest req = requestMap.get(event.getRequestId());
        SimNode node = graph.getNode(event.getNodeId());
        if (node == null) return;

        node.recordConcurrencyDecrease(event.getTimestampMs());
        node.incrementRequestsCompleted();

        // Check if there are queued requests waiting for a slot on this node
        if (!node.getQueue().isEmpty()) {
            SimRequest nextReq = node.getQueue().peek();
            if (nextReq != null && nextReq.getStatus() == RequestStatus.IN_FLIGHT) {
                Double entryTime = queueEntryTimes.get(nextReq.getId() + "@" + node.getId());
                node.recordQueueDequeue(event.getTimestampMs(), entryTime != null ? entryTime : event.getTimestampMs());
                node.recordConcurrencyIncrease(event.getTimestampMs());

                double nextDuration = calculateProcessingDuration(node, random);
                eventQueue.add(new SimEvent(
                        eventIdSeq.getAndIncrement(),
                        event.getTimestampMs() + nextDuration,
                        EventType.PROCESS_COMPLETE,
                        nextReq.getId(),
                        node.getId(),
                        null,
                        "Dequeued and completed work on " + node.getName()
                ));
            } else if (nextReq != null) {
                // Was cancelled or timed out
                node.getQueue().poll();
            }
        }

        if (req == null || req.getStatus() != RequestStatus.IN_FLIGHT) return;

        // Check if node has downstream edges
        List<SimEdge> outgoingEdges = graph.getOutgoingEdges(node.getId());
        if (outgoingEdges.isEmpty()) {
            // Sink / terminal node -> Request successfully completed!
            req.markCompleted(event.getTimestampMs());
        } else {
            // Dispatches to next downstream target
            SimEdge edge = outgoingEdges.get(random.nextInt(outgoingEdges.size()));
            eventQueue.add(new SimEvent(
                    eventIdSeq.getAndIncrement(),
                    event.getTimestampMs() + edge.getLatencyMs(),
                    EventType.DISPATCH,
                    req.getId(),
                    node.getId(),
                    edge.getTargetNodeId(),
                    "Forwarding to downstream " + edge.getTargetNodeId()
            ));
        }
    }

    private double calculateProcessingDuration(SimNode node, Random random) {
        double baseTime;
        switch (node.getType()) {
            case API_SERVER -> baseTime = node.getProcessingTimeMs();
            case DATABASE -> {
                // Reads are slightly faster than writes
                boolean isRead = (random.nextDouble() * 100.0) < node.getReadRatio();
                baseTime = isRead ? node.getQueryLatencyMs() * 0.7 : node.getQueryLatencyMs() * 1.4;
            }
            case WORKER -> baseTime = node.getJobProcessingTimeMs();
            case OBJECT_STORAGE -> baseTime = node.getStorageLatencyMs();
            default -> baseTime = 10.0;
        }

        // Add 10% normal jitter for realism
        double jitter = (random.nextGaussian() * 0.1) * baseTime;
        double duration = Math.max(1.0, baseTime + jitter);
        return node.getEffectiveLatency(duration);
    }
}
