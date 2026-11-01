package com.simulab.engine;

import com.simulab.engine.analysis.ArchitectureComparator;
import com.simulab.engine.core.SimulationEngine;
import com.simulab.engine.dto.ComparisonResultDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.engine.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new SimulationEngine();
    }

    @Test
    @DisplayName("Should simulate basic 3-tier architecture with realistic metrics")
    void testBasicThreeTierSimulation() {
        // Arrange
        SimNode client = new SimNode("client-1", "Web Client", ComponentType.CLIENT);
        client.setWorkloadRps(20.0);
        client.setWorkloadDistribution(WorkloadDistribution.CONSTANT);

        SimNode api = new SimNode("api-1", "API Gateway", ComponentType.API_SERVER);
        api.setInstances(2);
        api.setThreadsPerInstance(10);
        api.setProcessingTimeMs(15.0);

        SimNode db = new SimNode("db-1", "Postgres DB", ComponentType.DATABASE);
        db.setConnectionPoolSize(20);
        db.setQueryLatencyMs(25.0);

        SimEdge edge1 = new SimEdge("e1", "client-1", "api-1", 5.0, 1000.0, 0.0);
        SimEdge edge2 = new SimEdge("e2", "api-1", "db-1", 2.0, 1000.0, 0.0);

        SimulationGraph graph = new SimulationGraph(
                Arrays.asList(client, api, db),
                Arrays.asList(edge1, edge2)
        );

        SimulationScenario scenario = new SimulationScenario("sc-1", "3-Tier Test", graph, 3.0, 42L);

        // Act
        SimulationResultDto result = engine.execute(scenario);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalRequestsGenerated() > 0, "Requests should have been generated");
        assertTrue(result.getTotalRequestsCompleted() > 0, "Requests should have completed");
        assertEquals(0, result.getTotalRequestsFailed(), "Error rate should be 0 in healthy system");
        assertTrue(result.getP50LatencyMs() > 0, "P50 latency should be positive");
        assertTrue(result.getThroughputRps() > 10.0, "Throughput should match workload");
        assertNotNull(result.getBottleneckReport());
        assertEquals("HEALTHY", result.getBottleneckReport().getSeverity());
    }

    @Test
    @DisplayName("Should detect bottleneck when database connection pool is saturated")
    void testDatabaseBottleneckDetection() {
        // Arrange
        SimNode client = new SimNode("client-1", "Load Generator", ComponentType.CLIENT);
        client.setWorkloadRps(100.0); // High RPS

        SimNode api = new SimNode("api-1", "Fast API", ComponentType.API_SERVER);
        api.setInstances(10);
        api.setThreadsPerInstance(20);
        api.setProcessingTimeMs(5.0);

        SimNode db = new SimNode("db-1", "Bottleneck DB", ComponentType.DATABASE);
        db.setConnectionPoolSize(2); // Heavily constrained connection pool
        db.setQueryLatencyMs(60.0);

        SimEdge edge1 = new SimEdge("e1", "client-1", "api-1");
        SimEdge edge2 = new SimEdge("e2", "api-1", "db-1");

        SimulationGraph graph = new SimulationGraph(
                Arrays.asList(client, api, db),
                Arrays.asList(edge1, edge2)
        );

        SimulationScenario scenario = new SimulationScenario("sc-bottleneck", "Bottleneck Test", graph, 3.0, 42L);

        // Act
        SimulationResultDto result = engine.execute(scenario);

        // Assert
        assertNotNull(result);
        assertTrue(result.getBottleneckReport().isHasBottleneck(), "Should flag a bottleneck");
        assertEquals("db-1", result.getBottleneckReport().getBottleneckNodeId(), "DB should be identified as bottleneck");
        assertNotNull(result.getBottleneckReport().getRecommendation());
    }

    @Test
    @DisplayName("Should properly inject and recover node crash failures")
    void testNodeCrashFailureInjection() {
        // Arrange
        SimNode client = new SimNode("client-1", "Client", ComponentType.CLIENT);
        client.setWorkloadRps(20.0);

        SimNode api = new SimNode("api-1", "API Server", ComponentType.API_SERVER);
        SimEdge edge = new SimEdge("e1", "client-1", "api-1");

        SimulationGraph graph = new SimulationGraph(
                Arrays.asList(client, api),
                Collections.singletonList(edge)
        );

        SimulationScenario scenario = new SimulationScenario("sc-fail", "Fail Test", graph, 4.0, 42L);
        // Inject failure on api-1 between second 1.0 and 2.5
        FailureConfig crash = new FailureConfig(FailureConfig.FailureType.NODE_CRASH, "api-1", 1.0, 1.5, 0.0);
        scenario.setFailures(Collections.singletonList(crash));

        // Act
        SimulationResultDto result = engine.execute(scenario);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalRequestsFailed() > 0, "Requests should have failed during crash window");
        assertTrue(result.getErrorRatePercent() > 0.0, "Error rate should reflect the downtime");
    }

    @Test
    @DisplayName("Should accurately compare two architectures side by side")
    void testArchitectureComparison() {
        // Architecture A: Monolith (API directly to DB)
        SimNode clientA = new SimNode("c-1", "Client", ComponentType.CLIENT);
        clientA.setWorkloadRps(40.0);
        SimNode apiA = new SimNode("api-1", "Monolith", ComponentType.API_SERVER);
        apiA.setProcessingTimeMs(30.0);
        SimNode dbA = new SimNode("db-1", "DB", ComponentType.DATABASE);
        dbA.setQueryLatencyMs(40.0);
        dbA.setConnectionPoolSize(5);

        SimulationGraph graphA = new SimulationGraph(
                Arrays.asList(clientA, apiA, dbA),
                Arrays.asList(new SimEdge("e1", "c-1", "api-1"), new SimEdge("e2", "api-1", "db-1"))
        );
        SimulationScenario scenarioA = new SimulationScenario("archA", "Monolith", graphA, 3.0, 123L);
        SimulationResultDto resA = engine.execute(scenarioA);

        // Architecture B: Scaled with Cache
        SimNode clientB = new SimNode("c-1", "Client", ComponentType.CLIENT);
        clientB.setWorkloadRps(40.0);
        SimNode apiB = new SimNode("api-1", "API", ComponentType.API_SERVER);
        apiB.setProcessingTimeMs(15.0);
        apiB.setInstances(3);
        SimNode cache = new SimNode("cache-1", "Redis Cache", ComponentType.CACHE);
        cache.setCacheHitRatio(85.0);
        cache.setCacheLatencyMs(2.0);
        SimNode dbB = new SimNode("db-1", "DB", ComponentType.DATABASE);
        dbB.setQueryLatencyMs(40.0);
        dbB.setConnectionPoolSize(20);

        SimulationGraph graphB = new SimulationGraph(
                Arrays.asList(clientB, apiB, cache, dbB),
                Arrays.asList(new SimEdge("e1", "c-1", "api-1"), new SimEdge("e2", "api-1", "cache-1"), new SimEdge("e3", "cache-1", "db-1"))
        );
        SimulationScenario scenarioB = new SimulationScenario("archB", "Cached & Scaled", graphB, 3.0, 123L);
        SimulationResultDto resB = engine.execute(scenarioB);

        // Compare
        ComparisonResultDto comparison = ArchitectureComparator.compare("Monolith", resA, "Cached & Scaled", resB);

        // Assert
        assertNotNull(comparison);
        assertEquals("ARCHITECTURE_B", comparison.getWinner(), "Cached and scaled architecture should win");
        assertTrue(comparison.getP95LatencyDeltaPercent() < 0, "Latency delta should be negative (faster)");
        assertFalse(comparison.getTradeoffs().isEmpty(), "Tradeoffs should be generated");
    }
}
