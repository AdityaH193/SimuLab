package com.simulab.engine.model;

import java.util.ArrayList;
import java.util.List;

public class SimulationScenario {
    private String id;
    private String name;
    private SimulationGraph graph = new SimulationGraph();
    private double durationSec = 10.0;
    private long randomSeed = 42L;
    private List<FailureConfig> failures = new ArrayList<>();

    public SimulationScenario() {}

    public SimulationScenario(String id, String name, SimulationGraph graph, double durationSec, long randomSeed) {
        this.id = id;
        this.name = name;
        this.graph = graph;
        this.durationSec = durationSec;
        this.randomSeed = randomSeed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public SimulationGraph getGraph() { return graph; }
    public void setGraph(SimulationGraph graph) { this.graph = graph; }

    public double getDurationSec() { return durationSec; }
    public void setDurationSec(double durationSec) { this.durationSec = durationSec; }

    public long getRandomSeed() { return randomSeed; }
    public void setRandomSeed(long randomSeed) { this.randomSeed = randomSeed; }

    public List<FailureConfig> getFailures() { return failures; }
    public void setFailures(List<FailureConfig> failures) { this.failures = failures; }
}
