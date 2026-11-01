package com.simulab.engine.event;

import java.util.Objects;

public class SimEvent implements Comparable<SimEvent> {
    private final long eventId;
    private final double timestampMs;
    private final EventType type;
    private final String requestId;
    private final String nodeId;
    private final String targetNodeId;
    private final String details;

    public SimEvent(long eventId, double timestampMs, EventType type, String requestId, String nodeId, String targetNodeId, String details) {
        this.eventId = eventId;
        this.timestampMs = timestampMs;
        this.type = type;
        this.requestId = requestId;
        this.nodeId = nodeId;
        this.targetNodeId = targetNodeId;
        this.details = details;
    }

    public long getEventId() {
        return eventId;
    }

    public double getTimestampMs() {
        return timestampMs;
    }

    public EventType getType() {
        return type;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public int compareTo(SimEvent other) {
        int cmp = Double.compare(this.timestampMs, other.timestampMs);
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(this.eventId, other.eventId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimEvent simEvent = (SimEvent) o;
        return eventId == simEvent.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("[%8.2fms] Event#%d: %s (Req: %s, Node: %s -> %s) %s",
                timestampMs, eventId, type, requestId, nodeId, targetNodeId != null ? targetNodeId : "-", details != null ? details : "");
    }
}
