package com.simulab.controller;

import com.simulab.dto.CompareSimulationsRequest;
import com.simulab.dto.ExecuteAdhocSimulationRequest;
import com.simulab.engine.dto.ComparisonResultDto;
import com.simulab.engine.dto.SimulationResultDto;
import com.simulab.entity.SimulationRunEntity;
import com.simulab.service.SimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/execute-adhoc")
    public ResponseEntity<SimulationResultDto> executeAdhoc(@RequestBody ExecuteAdhocSimulationRequest request) {
        return ResponseEntity.ok(simulationService.executeAdhoc(request));
    }

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResultDto> compare(@RequestBody CompareSimulationsRequest request) {
        return ResponseEntity.ok(simulationService.compare(request));
    }

    @GetMapping("/runs")
    public ResponseEntity<List<SimulationRunEntity>> getRuns() {
        return ResponseEntity.ok(simulationService.getRuns());
    }

    @GetMapping("/runs/{runId}/results")
    public ResponseEntity<SimulationResultDto> getRunResult(@PathVariable String runId) {
        return ResponseEntity.ok(simulationService.getRunResult(runId));
    }
}
