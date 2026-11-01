package com.simulab.dto;

import com.simulab.engine.model.FailureConfig;
import com.simulab.engine.model.SimEdge;
import com.simulab.engine.model.SimNode;

import java.util.ArrayList;
import java.util.List;

public class ExecuteAdhocSimulationRequest {
    private String name = "Simulation Scenario";
    private List<SimNode> nodes = new ArrayList<>();
    private List<SimEdge> edges = new ArrayList<>();
    private double durationSec = 10.0;
    private long randomSeed = 42L;
    private List<FailureConfig> failures = new ArrayList<>();

    public ExecuteAdhocSimulationRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<SimNode> getNodes() { return nodes; }
    public void setNodes(List<SimNode> nodes) { this.nodes = nodes; }

    public List<SimEdge> getEdges() { return edges; }
    public void setEdges(List<SimEdge> edges) { this.edges = edges; }

    public double getDurationSec() { return durationSec; }
    public void setDurationSec(double durationSec) { this.durationSec = durationSec; }

    public long getRandomSeed() { return randomSeed; }
    public void setRandomSeed(long randomSeed) { this.randomSeed = randomSeed; }

    public List<FailureConfig> getFailures() { return failures; }
    public void setFailures(List<FailureConfig> failures) { this.failures = failures; }
}
