package com.simulab.engine.model;

import java.util.ArrayList;
import java.util.List;

public class SimRequest {
    private final String id;
    private final double startTimeMs;
    private double endTimeMs;
    private RequestStatus status;
    private String failureReason;
    private final List<String> visitedNodes;
    private final List<TraceHop> trace;

    public record TraceHop(double timestampMs, String nodeId, String action) {}

    public SimRequest(String id, double startTimeMs) {
        this.id = id;
        this.startTimeMs = startTimeMs;
        this.status = RequestStatus.PENDING;
        this.visitedNodes = new ArrayList<>();
        this.trace = new ArrayList<>();
        addTrace(startTimeMs, "CLIENT", "Request created");
    }

    public void addTrace(double timestampMs, String nodeId, String action) {
        trace.add(new TraceHop(timestampMs, nodeId, action));
        if (nodeId != null && !nodeId.equals("CLIENT") && !visitedNodes.contains(nodeId)) {
            visitedNodes.add(nodeId);
        }
    }

    public void markCompleted(double timestampMs) {
        if (this.status == RequestStatus.PENDING || this.status == RequestStatus.IN_FLIGHT) {
            this.status = RequestStatus.COMPLETED;
            this.endTimeMs = timestampMs;
            addTrace(timestampMs, "CLIENT", "Request successfully completed");
        }
    }

    public void markFailed(double timestampMs, String reason) {
        if (this.status == RequestStatus.PENDING || this.status == RequestStatus.IN_FLIGHT) {
            this.status = RequestStatus.FAILED;
            this.failureReason = reason;
            this.endTimeMs = timestampMs;
            addTrace(timestampMs, "SYSTEM", "Request failed: " + reason);
        }
    }

    public void markTimeout(double timestampMs) {
        if (this.status == RequestStatus.PENDING || this.status == RequestStatus.IN_FLIGHT) {
            this.status = RequestStatus.TIMEOUT;
            this.failureReason = "Timeout exceeded";
            this.endTimeMs = timestampMs;
            addTrace(timestampMs, "CLIENT", "Client timeout triggered");
        }
    }

    public double getLatencyMs() {
        return Math.max(0.0, endTimeMs - startTimeMs);
    }

    public String getId() {
        return id;
    }

    public double getStartTimeMs() {
        return startTimeMs;
    }

    public double getEndTimeMs() {
        return endTimeMs;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public List<String> getVisitedNodes() {
        return visitedNodes;
    }

    public List<TraceHop> getTrace() {
        return trace;
    }
}
