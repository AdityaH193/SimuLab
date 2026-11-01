package com.simulab.engine.model;

public enum ComponentType {
    CLIENT("Client Workload Generator", "Generates traffic with configurable RPS, concurrency, and distribution"),
    LOAD_BALANCER("Load Balancer", "Distributes requests across multiple downstream target nodes"),
    API_SERVER("API Server", "Executes application logic with concurrency, CPU limits, and thread pools"),
    CACHE("In-Memory Cache", "Fast memory lookups with configurable hit/miss ratio and TTL"),
    DATABASE("Database", "Relational/NoSQL database with connection pooling and query latencies"),
    MESSAGE_QUEUE("Message Queue", "Asynchronous message broker with buffer limits and backpressure"),
    WORKER("Worker Service", "Background worker pool consuming tasks asynchronously"),
    OBJECT_STORAGE("Object Storage", "Blob storage with bandwidth limits and upload/download latencies");

    private final String displayName;
    private final String description;

    ComponentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
