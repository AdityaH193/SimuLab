import { create } from 'zustand';
import {
  applyNodeChanges,
  applyEdgeChanges,
  addEdge
} from '@xyflow/react';
import type {
  Node,
  Edge,
  NodeChange,
  EdgeChange,
  Connection
} from '@xyflow/react';
import type {
  ComponentType,
  FailureConfig,
  SimNodeData,
  SimulationResult,
  ComparisonResult,
  ArchitectureTemplate
} from '../types/simulation';
import { api } from '../services/api';

interface SimulationState {
  // Canvas State
  architectureTitle: string;
  architectureDescription: string;
  nodes: Node<SimNodeData>[];
  edges: Edge[];
  selectedNodeId: string | null;
  selectedEdgeId: string | null;

  // Scenario Settings
  durationSec: number;
  randomSeed: number;
  failures: FailureConfig[];

  // Simulation execution
  isRunning: boolean;
  result: SimulationResult | null;
  activeTab: 'canvas' | 'analytics' | 'comparison' | 'runs';
  playbackCurrentTimeSec: number;
  isPlaying: boolean;
  playbackSpeed: number;

  // Comparison
  isComparisonOpen: boolean;
  comparisonResult: ComparisonResult | null;
  comparisonArchAName: string;
  comparisonArchBName: string;
  isComparing: boolean;

  // Actions
  setArchitectureTitle: (title: string) => void;
  setArchitectureDescription: (desc: string) => void;
  setNodes: (nodes: Node<SimNodeData>[]) => void;
  setEdges: (edges: Edge[]) => void;
  onNodesChange: (changes: NodeChange<Node<SimNodeData>>[]) => void;
  onEdgesChange: (changes: EdgeChange[]) => void;
  onConnect: (connection: Connection) => void;

  addNode: (type: ComponentType, position?: { x: number; y: number }) => void;
  updateNodeData: (id: string, data: Partial<SimNodeData>) => void;
  deleteNode: (id: string) => void;
  selectNode: (id: string | null) => void;
  selectEdge: (id: string | null) => void;

  setDurationSec: (sec: number) => void;
  setRandomSeed: (seed: number) => void;
  addFailure: (failure: FailureConfig) => void;
  removeFailure: (index: number) => void;
  clearFailures: () => void;

  setActiveTab: (tab: 'canvas' | 'analytics' | 'comparison' | 'runs') => void;
  setIsPlaying: (playing: boolean) => void;
  setPlaybackTime: (timeSec: number) => void;
  setPlaybackSpeed: (speed: number) => void;

  runSimulation: () => Promise<void>;
  runComparison: (archAName: string, nodesA: Node<SimNodeData>[], edgesA: Edge[], archBName: string, nodesB: Node<SimNodeData>[], edgesB: Edge[]) => Promise<void>;
  loadTemplate: (template: ArchitectureTemplate) => void;
  clearCanvas: () => void;
  setComparisonOpen: (open: boolean) => void;
}

const defaultMonolithNodes: Node<SimNodeData>[] = [
  {
    id: 'client-1',
    type: 'simNode',
    position: { x: 100, y: 220 },
    data: {
      id: 'client-1',
      name: 'Web & Mobile Clients',
      type: 'CLIENT',
      workloadRps: 25.0,
      workloadDistribution: 'CONSTANT',
      timeoutMs: 2000.0,
      status: 'idle',
    },
  },
  {
    id: 'api-gw',
    type: 'simNode',
    position: { x: 380, y: 220 },
    data: {
      id: 'api-gw',
      name: 'API Gateway',
      type: 'API_SERVER',
      instances: 1,
      threadsPerInstance: 15,
      processingTimeMs: 12.0,
      status: 'idle',
    },
  },
  {
    id: 'app-mono',
    type: 'simNode',
    position: { x: 650, y: 220 },
    data: {
      id: 'app-mono',
      name: 'Monolith Server',
      type: 'API_SERVER',
      instances: 1,
      threadsPerInstance: 10,
      processingTimeMs: 35.0,
      status: 'idle',
    },
  },
  {
    id: 'db-mono',
    type: 'simNode',
    position: { x: 920, y: 220 },
    data: {
      id: 'db-mono',
      name: 'PostgreSQL DB',
      type: 'DATABASE',
      connectionPoolSize: 10,
      queryLatencyMs: 40.0,
      readRatio: 80.0,
      status: 'idle',
    },
  },
];

const defaultMonolithEdges: Edge[] = [
  { id: 'e-1', source: 'client-1', target: 'api-gw', animated: true },
  { id: 'e-2', source: 'api-gw', target: 'app-mono', animated: true },
  { id: 'e-3', source: 'app-mono', target: 'db-mono', animated: true },
];

export const useSimulationStore = create<SimulationState>((set, get) => ({
  architectureTitle: 'Classic Monolith Architecture',
  architectureDescription: 'Single gateway forwarding requests to a monolithic service connected to a relational database.',
  nodes: defaultMonolithNodes,
  edges: defaultMonolithEdges,
  selectedNodeId: 'client-1',
  selectedEdgeId: null,

  durationSec: 10,
  randomSeed: 42,
  failures: [],

  isRunning: false,
  result: null,
  activeTab: 'canvas',
  playbackCurrentTimeSec: 0,
  isPlaying: false,
  playbackSpeed: 1,

  isComparisonOpen: false,
  comparisonResult: null,
  comparisonArchAName: 'Monolith Architecture',
  comparisonArchBName: 'Cached Microservices',
  isComparing: false,

  setArchitectureTitle: (title) => set({ architectureTitle: title }),
  setArchitectureDescription: (desc) => set({ architectureDescription: desc }),

  setNodes: (nodes) => set({ nodes }),
  setEdges: (edges) => set({ edges }),

  onNodesChange: (changes) =>
    set({
      nodes: applyNodeChanges(changes, get().nodes),
    }),

  onEdgesChange: (changes) =>
    set({
      edges: applyEdgeChanges(changes, get().edges),
    }),

  onConnect: (connection) =>
    set({
      edges: addEdge({ ...connection, animated: true }, get().edges),
    }),

  addNode: (type, position) => {
    const id = `${type.toLowerCase()}-${Date.now().toString().slice(-4)}`;
    const pos = position || {
      x: 300 + Math.random() * 200,
      y: 150 + Math.random() * 200,
    };

    let name = type.replace('_', ' ');
    if (type === 'CLIENT') name = 'Workload Client';
    else if (type === 'LOAD_BALANCER') name = 'HAProxy Balancer';
    else if (type === 'API_SERVER') name = 'API Service';
    else if (type === 'CACHE') name = 'Redis Cache';
    else if (type === 'DATABASE') name = 'PostgreSQL DB';
    else if (type === 'MESSAGE_QUEUE') name = 'Kafka Queue';
    else if (type === 'WORKER') name = 'Async Worker Pool';
    else if (type === 'OBJECT_STORAGE') name = 'S3 Object Storage';

    const newNode: Node<SimNodeData> = {
      id,
      type: 'simNode',
      position: pos,
      data: {
        id,
        name,
        type,
        workloadRps: 20.0,
        workloadDistribution: 'POISSON',
        timeoutMs: 2000.0,
        lbAlgorithm: 'ROUND_ROBIN',
        instances: 2,
        threadsPerInstance: 10,
        processingTimeMs: 20.0,
        cacheHitRatio: 80.0,
        cacheLatencyMs: 2.0,
        connectionPoolSize: 15,
        queryLatencyMs: 35.0,
        readRatio: 80.0,
        queueCapacity: 500,
        enqueueLatencyMs: 3.0,
        workerPoolSize: 4,
        jobProcessingTimeMs: 40.0,
        storageLatencyMs: 25.0,
        status: 'idle',
      },
    };

    set({
      nodes: [...get().nodes, newNode],
      selectedNodeId: id,
    });
  },

  updateNodeData: (id, partialData) => {
    set({
      nodes: get().nodes.map((node) => {
        if (node.id === id) {
          return {
            ...node,
            data: { ...node.data, ...partialData },
          };
        }
        return node;
      }),
    });
  },

  deleteNode: (id) => {
    set({
      nodes: get().nodes.filter((n) => n.id !== id),
      edges: get().edges.filter((e) => e.source !== id && e.target !== id),
      selectedNodeId: get().selectedNodeId === id ? null : get().selectedNodeId,
    });
  },

  selectNode: (id) => set({ selectedNodeId: id, selectedEdgeId: null }),
  selectEdge: (id) => set({ selectedEdgeId: id, selectedNodeId: null }),

  setDurationSec: (sec) => set({ durationSec: sec }),
  setRandomSeed: (seed) => set({ randomSeed: seed }),

  addFailure: (failure) => set({ failures: [...get().failures, failure] }),
  removeFailure: (index) =>
    set({
      failures: get().failures.filter((_, i) => i !== index),
    }),
  clearFailures: () => set({ failures: [] }),

  setActiveTab: (tab) => set({ activeTab: tab }),
  setIsPlaying: (playing) => set({ isPlaying: playing }),
  setPlaybackTime: (timeSec) => set({ playbackCurrentTimeSec: timeSec }),
  setPlaybackSpeed: (speed) => set({ playbackSpeed: speed }),

  runSimulation: async () => {
    const { nodes, edges, durationSec, randomSeed, failures, architectureTitle } = get();
    set({ isRunning: true });

    try {
      const payload = {
        name: architectureTitle || 'Simulation Run',
        nodes: nodes.map((n) => n.data),
        edges: edges.map((e) => ({
          id: e.id,
          sourceNodeId: e.source,
          targetNodeId: e.target,
          latencyMs: 2.0,
          bandwidthMbps: 1000.0,
          packetLossPercent: 0.0,
        })),
        durationSec,
        randomSeed,
        failures,
      };

      const result = await api.executeAdhoc(payload);

      // Apply bottleneck highlighting to nodes
      const updatedNodes = nodes.map((n) => {
        const metric = result.nodeMetrics.find((m) => m.nodeId === n.id);
        const isBottleneck = !!metric?.bottleneck;
        return {
          ...n,
          data: {
            ...n.data,
            isBottleneck,
            bottleneckReason: metric?.bottleneckReason,
            status: isBottleneck ? ('congested' as const) : ('processing' as const),
          },
        };
      });

      set({
        result,
        nodes: updatedNodes,
        isRunning: false,
        activeTab: 'analytics',
        playbackCurrentTimeSec: 0,
      });
    } catch (err: any) {
      console.error('Simulation execution failed:', err);
      set({ isRunning: false });
      alert(`Simulation failed: ${err.message || 'Error communicating with backend'}`);
    }
  },

  runComparison: async (archAName, nodesA, edgesA, archBName, nodesB, edgesB) => {
    set({ isComparing: true });
    try {
      const payloadA = {
        name: archAName,
        nodes: nodesA.map((n) => n.data),
        edges: edgesA.map((e) => ({
          id: e.id,
          sourceNodeId: e.source,
          targetNodeId: e.target,
          latencyMs: 2.0,
        })),
        durationSec: get().durationSec,
        randomSeed: get().randomSeed,
        failures: get().failures,
      };

      const payloadB = {
        name: archBName,
        nodes: nodesB.map((n) => n.data),
        edges: edgesB.map((e) => ({
          id: e.id,
          sourceNodeId: e.source,
          targetNodeId: e.target,
          latencyMs: 2.0,
        })),
        durationSec: get().durationSec,
        randomSeed: get().randomSeed,
        failures: get().failures,
      };

      const comparisonResult = await api.compare({
        architectureAName: archAName,
        architectureA: payloadA,
        architectureBName: archBName,
        architectureB: payloadB,
      });

      set({
        comparisonResult,
        comparisonArchAName: archAName,
        comparisonArchBName: archBName,
        isComparing: false,
        isComparisonOpen: true,
      });
    } catch (err: any) {
      console.error('Comparison failed:', err);
      set({ isComparing: false });
      alert(`Comparison failed: ${err.message}`);
    }
  },

  loadTemplate: (template) => {
    try {
      const canvasData = JSON.parse(template.canvasDataJson);
      const scenarioData = template.scenarioDataJson ? JSON.parse(template.scenarioDataJson) : {};

      set({
        architectureTitle: template.name,
        architectureDescription: template.description,
        nodes: canvasData.nodes || [],
        edges: canvasData.edges || [],
        durationSec: scenarioData.durationSec || 10,
        randomSeed: scenarioData.randomSeed || 42,
        failures: scenarioData.failures || [],
        result: null,
        selectedNodeId: canvasData.nodes?.[0]?.id || null,
        activeTab: 'canvas',
      });
    } catch (e) {
      console.error('Failed to parse template JSON', e);
    }
  },

  clearCanvas: () => {
    set({
      nodes: [],
      edges: [],
      selectedNodeId: null,
      selectedEdgeId: null,
      result: null,
      failures: [],
    });
  },

  setComparisonOpen: (open) => set({ isComparisonOpen: open }),
}));
