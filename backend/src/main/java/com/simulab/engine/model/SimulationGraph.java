package com.simulab.engine.model;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationGraph {
    private List<SimNode> nodes = new ArrayList<>();
    private List<SimEdge> edges = new ArrayList<>();

    public SimulationGraph() {}

    public SimulationGraph(List<SimNode> nodes, List<SimEdge> edges) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.edges = edges != null ? edges : new ArrayList<>();
    }

    public List<SimNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<SimNode> nodes) {
        this.nodes = nodes;
    }

    public List<SimEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<SimEdge> edges) {
        this.edges = edges;
    }

    public SimNode getNode(String id) {
        if (id == null) return null;
        for (SimNode node : nodes) {
            if (id.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    public SimEdge getEdge(String id) {
        if (id == null) return null;
        for (SimEdge edge : edges) {
            if (id.equals(edge.getId())) {
                return edge;
            }
        }
        return null;
    }

    public List<SimEdge> getOutgoingEdges(String nodeId) {
        if (nodeId == null) return Collections.emptyList();
        return edges.stream()
                .filter(e -> nodeId.equals(e.getSourceNodeId()))
                .collect(Collectors.toList());
    }

    public List<SimEdge> getIncomingEdges(String nodeId) {
        if (nodeId == null) return Collections.emptyList();
        return edges.stream()
                .filter(e -> nodeId.equals(e.getTargetNodeId()))
                .collect(Collectors.toList());
    }

    public List<SimNode> getClientNodes() {
        return nodes.stream()
                .filter(n -> n.getType() == ComponentType.CLIENT)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the graph contains illegal cycles that would cause infinite loops
     */
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (SimNode node : nodes) {
            if (detectCycleUtil(node.getId(), visited, recStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean detectCycleUtil(String nodeId, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(nodeId)) {
            return true;
        }
        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        recStack.add(nodeId);

        for (SimEdge edge : getOutgoingEdges(nodeId)) {
            if (detectCycleUtil(edge.getTargetNodeId(), visited, recStack)) {
                return true;
            }
        }

        recStack.remove(nodeId);
        return false;
    }
}
