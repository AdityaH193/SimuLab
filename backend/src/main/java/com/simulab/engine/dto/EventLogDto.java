package com.simulab.engine.dto;

import com.simulab.engine.event.EventType;

public class EventLogDto {
    private long eventId;
    private double timestampMs;
    private EventType type;
    private String requestId;
    private String nodeId;
    private String targetNodeId;
    private String description;

    public EventLogDto() {}

    public EventLogDto(long eventId, double timestampMs, EventType type, String requestId, String nodeId, String targetNodeId, String description) {
        this.eventId = eventId;
        this.timestampMs = timestampMs;
        this.type = type;
        this.requestId = requestId;
        this.nodeId = nodeId;
        this.targetNodeId = targetNodeId;
        this.description = description;
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public double getTimestampMs() { return timestampMs; }
    public void setTimestampMs(double timestampMs) { this.timestampMs = timestampMs; }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
