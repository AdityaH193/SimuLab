package com.simulab.dto;

public class CompareSimulationsRequest {
    private String architectureAName = "Architecture A";
    private ExecuteAdhocSimulationRequest architectureA;
    private String architectureBName = "Architecture B";
    private ExecuteAdhocSimulationRequest architectureB;

    public CompareSimulationsRequest() {}

    public String getArchitectureAName() { return architectureAName; }
    public void setArchitectureAName(String architectureAName) { this.architectureAName = architectureAName; }

    public ExecuteAdhocSimulationRequest getArchitectureA() { return architectureA; }
    public void setArchitectureA(ExecuteAdhocSimulationRequest architectureA) { this.architectureA = architectureA; }

    public String getArchitectureBName() { return architectureBName; }
    public void setArchitectureBName(String architectureBName) { this.architectureBName = architectureBName; }

    public ExecuteAdhocSimulationRequest getArchitectureB() { return architectureB; }
    public void setArchitectureB(ExecuteAdhocSimulationRequest architectureB) { this.architectureB = architectureB; }
}
