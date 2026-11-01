package com.simulab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simulab.dto.CompareSimulationsRequest;
import com.simulab.dto.ExecuteAdhocSimulationRequest;
import com.simulab.engine.analysis.ArchitectureComparator;
import com.simulab.engine.core.SimulationEngine;
import com.simulab.engine.dto.ComparisonResultDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.engine.model.ComponentType;
import com.simulab.engine.model.SimNode;
import com.simulab.engine.model.SimulationGraph;
import com.simulab.engine.model.SimulationScenario;
import com.simulab.entity.SimulationResultEntity;
import com.simulab.entity.SimulationRunEntity;
import com.simulab.repository.SimulationResultRepository;
import com.simulab.repository.SimulationRunRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SimulationService {

    private final SimulationEngine simulationEngine = new SimulationEngine();
    private final SimulationRunRepository runRepository;
    private final SimulationResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    public SimulationService(SimulationRunRepository runRepository,
                             SimulationResultRepository resultRepository,
                             ObjectMapper objectMapper) {
        this.runRepository = runRepository;
        this.resultRepository = resultRepository;
        this.objectMapper = objectMapper;
    }

    public SimulationResultDto executeAdhoc(ExecuteAdhocSimulationRequest request) {
        SimulationGraph graph = new SimulationGraph(request.getNodes(), request.getEdges());
        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID().toString(),
                request.getName(),
                graph,
                request.getDurationSec(),
                request.getRandomSeed()
        );
        scenario.setFailures(request.getFailures());

        SimulationResultDto result = simulationEngine.execute(scenario);

        // Persist run and result
        try {
            double clientRps = request.getNodes().stream()
                    .filter(n -> n.getType() == ComponentType.CLIENT)
                    .mapToDouble(SimNode::getWorkloadRps)
                    .sum();

            SimulationRunEntity runEntity = new SimulationRunEntity(
                    result.getRunId(),
                    null,
                    request.getName(),
                    result.getDurationSec(),
                    clientRps,
                    request.getRandomSeed()
            );
            runRepository.save(runEntity);

            String bottleneckNode = result.getBottleneckReport() != null ?
                    result.getBottleneckReport().getBottleneckNodeName() : "None";
            String severity = result.getBottleneckReport() != null ?
                    result.getBottleneckReport().getSeverity() : "HEALTHY";

            String resultJson = objectMapper.writeValueAsString(result);
            SimulationResultEntity resultEntity = new SimulationResultEntity(
                    result.getRunId(),
                    result.getThroughputRps(),
                    result.getP95LatencyMs(),
                    result.getErrorRatePercent(),
                    bottleneckNode,
                    severity,
                    resultJson
            );
            resultRepository.save(resultEntity);
        } catch (Exception ex) {
            // Non-blocking logging if persistence fails
            System.err.println("Warning: failed to persist simulation run: " + ex.getMessage());
        }

        return result;
    }

    public ComparisonResultDto compare(CompareSimulationsRequest request) {
        ExecuteAdhocSimulationRequest reqA = request.getArchitectureA();
        ExecuteAdhocSimulationRequest reqB = request.getArchitectureB();

        SimulationResultDto resA = executeAdhoc(reqA);
        SimulationResultDto resB = executeAdhoc(reqB);

        return ArchitectureComparator.compare(
                request.getArchitectureAName(), resA,
                request.getArchitectureBName(), resB
        );
    }

    public List<SimulationRunEntity> getRuns() {
        return runRepository.findAllByOrderByCreatedAtDesc();
    }

    public SimulationResultDto getRunResult(String runId) {
        SimulationResultEntity entity = resultRepository.findByRunId(runId)
                .orElseThrow(() -> new IllegalArgumentException("Run result not found for runId: " + runId));

        try {
            return objectMapper.readValue(entity.getResultJson(), SimulationResultDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize simulation result", e);
        }
    }
}
