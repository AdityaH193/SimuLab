package com.simulab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulab.dto.ArchitectureDto;
import com.simulab.entity.ArchitectureEntity;
import com.simulab.repository.ArchitectureRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArchitectureService {

    private final ArchitectureRepository architectureRepository;
    private final ObjectMapper objectMapper;

    public ArchitectureService(ArchitectureRepository architectureRepository, ObjectMapper objectMapper) {
        this.architectureRepository = architectureRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void seedTemplatesIfEmpty() {
        if (architectureRepository.count() == 0) {
            seedMonolithTemplate();
            seedMicroservicesTemplate();
            seedAsyncQueueTemplate();
        }
    }

    private void seedMonolithTemplate() {
        String canvasJson = """
        {
          "nodes": [
            { "id": "client-1", "type": "simNode", "position": { "x": 100, "y": 200 }, "data": { "id": "client-1", "name": "Web & Mobile Clients", "type": "CLIENT", "workloadRps": 25.0, "workloadDistribution": "CONSTANT", "timeoutMs": 2000.0 } },
            { "id": "api-gw", "type": "simNode", "position": { "x": 360, "y": 200 }, "data": { "id": "api-gw", "name": "API Gateway", "type": "API_SERVER", "instances": 1, "threadsPerInstance": 15, "processingTimeMs": 10.0 } },
            { "id": "app-mono", "type": "simNode", "position": { "x": 620, "y": 200 }, "data": { "id": "app-mono", "name": "Monolith App Server", "type": "API_SERVER", "instances": 1, "threadsPerInstance": 10, "processingTimeMs": 35.0 } },
            { "id": "db-mono", "type": "simNode", "position": { "x": 880, "y": 200 }, "data": { "id": "db-mono", "name": "PostgreSQL Primary", "type": "DATABASE", "connectionPoolSize": 10, "queryLatencyMs": 45.0, "readRatio": 80.0 } }
          ],
          "edges": [
            { "id": "e-1", "source": "client-1", "target": "api-gw", "animated": true },
            { "id": "e-2", "source": "api-gw", "target": "app-mono", "animated": true },
            { "id": "e-3", "source": "app-mono", "target": "db-mono", "animated": true }
          ]
        }
        """;

        ArchitectureEntity monolith = new ArchitectureEntity(
                "Classic Monolith Architecture",
                "Single deployment unit where API Gateway forwards all traffic to a centralized monolithic application server and database.",
                true,
                canvasJson,
                "{ \"durationSec\": 10, \"randomSeed\": 42, \"failures\": [] }"
        );
        architectureRepository.save(monolith);
    }

    private void seedMicroservicesTemplate() {
        String canvasJson = """
        {
          "nodes": [
            { "id": "client-1", "type": "simNode", "position": { "x": 80, "y": 240 }, "data": { "id": "client-1", "name": "Client Traffic", "type": "CLIENT", "workloadRps": 50.0, "workloadDistribution": "POISSON", "timeoutMs": 2000.0 } },
            { "id": "lb-1", "type": "simNode", "position": { "x": 300, "y": 240 }, "data": { "id": "lb-1", "name": "HAProxy Load Balancer", "type": "LOAD_BALANCER", "lbAlgorithm": "ROUND_ROBIN" } },
            { "id": "api-1", "type": "simNode", "position": { "x": 540, "y": 140 }, "data": { "id": "api-1", "name": "User Service API", "type": "API_SERVER", "instances": 2, "threadsPerInstance": 15, "processingTimeMs": 15.0 } },
            { "id": "api-2", "type": "simNode", "position": { "x": 540, "y": 340 }, "data": { "id": "api-2", "name": "Catalog Service API", "type": "API_SERVER", "instances": 2, "threadsPerInstance": 15, "processingTimeMs": 15.0 } },
            { "id": "cache-1", "type": "simNode", "position": { "x": 780, "y": 240 }, "data": { "id": "cache-1", "name": "Redis Cluster Cache", "type": "CACHE", "cacheHitRatio": 85.0, "cacheLatencyMs": 2.0 } },
            { "id": "db-1", "type": "simNode", "position": { "x": 1020, "y": 240 }, "data": { "id": "db-1", "name": "PostgreSQL Main DB", "type": "DATABASE", "connectionPoolSize": 25, "queryLatencyMs": 35.0, "readRatio": 85.0 } }
          ],
          "edges": [
            { "id": "e-1", "source": "client-1", "target": "lb-1", "animated": true },
            { "id": "e-2", "source": "lb-1", "target": "api-1", "animated": true },
            { "id": "e-3", "source": "lb-1", "target": "api-2", "animated": true },
            { "id": "e-4", "source": "api-1", "target": "cache-1", "animated": true },
            { "id": "e-5", "source": "api-2", "target": "cache-1", "animated": true },
            { "id": "e-6", "source": "cache-1", "target": "db-1", "animated": true }
          ]
        }
        """;

        ArchitectureEntity microservices = new ArchitectureEntity(
                "Microservices with Redis Cache",
                "High-throughput horizontally-scaled architecture featuring round-robin load balancing, multi-instance microservices, and 85% hit-rate caching.",
                true,
                canvasJson,
                "{ \"durationSec\": 10, \"randomSeed\": 42, \"failures\": [] }"
        );
        architectureRepository.save(microservices);
    }

    private void seedAsyncQueueTemplate() {
        String canvasJson = """
        {
          "nodes": [
            { "id": "client-1", "type": "simNode", "position": { "x": 80, "y": 220 }, "data": { "id": "client-1", "name": "Ingestion Traffic", "type": "CLIENT", "workloadRps": 80.0, "workloadDistribution": "BURST", "timeoutMs": 3000.0 } },
            { "id": "api-ingest", "type": "simNode", "position": { "x": 320, "y": 220 }, "data": { "id": "api-ingest", "name": "Event Ingest API", "type": "API_SERVER", "instances": 2, "threadsPerInstance": 20, "processingTimeMs": 8.0 } },
            { "id": "mq-kafka", "type": "simNode", "position": { "x": 560, "y": 220 }, "data": { "id": "mq-kafka", "name": "Apache Kafka Broker", "type": "MESSAGE_QUEUE", "queueCapacity": 1000, "enqueueLatencyMs": 3.0 } },
            { "id": "worker-pool", "type": "simNode", "position": { "x": 800, "y": 220 }, "data": { "id": "worker-pool", "name": "Async Worker Pool", "type": "WORKER", "workerPoolSize": 6, "jobProcessingTimeMs": 35.0 } },
            { "id": "s3-storage", "type": "simNode", "position": { "x": 1040, "y": 140 }, "data": { "id": "s3-storage", "name": "S3 Object Store", "type": "OBJECT_STORAGE", "storageLatencyMs": 25.0, "storageBandwidthLimitMbps": 1000 } },
            { "id": "db-events", "type": "simNode", "position": { "x": 1040, "y": 300 }, "data": { "id": "db-events", "name": "TimescaleDB Analytics", "type": "DATABASE", "connectionPoolSize": 30, "queryLatencyMs": 20.0, "readRatio": 30.0 } }
          ],
          "edges": [
            { "id": "e-1", "source": "client-1", "target": "api-ingest", "animated": true },
            { "id": "e-2", "source": "api-ingest", "target": "mq-kafka", "animated": true },
            { "id": "e-3", "source": "mq-kafka", "target": "worker-pool", "animated": true },
            { "id": "e-4", "source": "worker-pool", "target": "s3-storage", "animated": true },
            { "id": "e-5", "source": "worker-pool", "target": "db-events", "animated": true }
          ]
        }
        """;

        ArchitectureEntity asyncQueue = new ArchitectureEntity(
                "Event-Driven Async Queue & Workers",
                "Decoupled asynchronous architecture buffering spikey burst workloads with Kafka, consumed by scalable parallel workers writing to object storage and analytical databases.",
                true,
                canvasJson,
                "{ \"durationSec\": 10, \"randomSeed\": 42, \"failures\": [] }"
        );
        architectureRepository.save(asyncQueue);
    }

    public List<ArchitectureDto> getAll() {
        return architectureRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ArchitectureDto> getTemplates() {
        return architectureRepository.findByIsTemplateTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ArchitectureDto getById(Long id) {
        ArchitectureEntity entity = architectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + id));
        return toDto(entity);
    }

    public ArchitectureDto create(ArchitectureDto dto) {
        ArchitectureEntity entity = new ArchitectureEntity(
                dto.getName(),
                dto.getDescription(),
                dto.isTemplate(),
                dto.getCanvasDataJson(),
                dto.getScenarioDataJson()
        );
        return toDto(architectureRepository.save(entity));
    }

    public ArchitectureDto update(Long id, ArchitectureDto dto) {
        ArchitectureEntity entity = architectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Architecture not found: " + id));

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCanvasDataJson(dto.getCanvasDataJson());
        entity.setScenarioDataJson(dto.getScenarioDataJson());
        return toDto(architectureRepository.save(entity));
    }

    public void delete(Long id) {
        architectureRepository.deleteById(id);
    }

    private ArchitectureDto toDto(ArchitectureEntity entity) {
        ArchitectureDto dto = new ArchitectureDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setTemplate(entity.isTemplate());
        dto.setCanvasDataJson(entity.getCanvasDataJson());
        dto.setScenarioDataJson(entity.getScenarioDataJson());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
