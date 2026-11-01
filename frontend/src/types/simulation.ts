export type ComponentType =
  | 'CLIENT'
  | 'LOAD_BALANCER'
  | 'API_SERVER'
  | 'CACHE'
  | 'DATABASE'
  | 'MESSAGE_QUEUE'
  | 'WORKER'
  | 'OBJECT_STORAGE';

export type LoadBalancingAlgorithm = 'ROUND_ROBIN' | 'LEAST_CONNECTIONS' | 'RANDOM';
export type WorkloadDistribution = 'CONSTANT' | 'POISSON' | 'BURST';

export interface SimNodeData {
  [key: string]: unknown;
  id: string;
  name: string;
  type: ComponentType;

  // Client
  workloadRps?: number;
  workloadDistribution?: WorkloadDistribution;
  timeoutMs?: number;
  totalRequests?: number;

  // Load Balancer
  lbAlgorithm?: LoadBalancingAlgorithm;

  // API Server
  instances?: number;
  threadsPerInstance?: number;
  processingTimeMs?: number;

  // Cache
  cacheHitRatio?: number;
  cacheLatencyMs?: number;

  // Database
  connectionPoolSize?: number;
  queryLatencyMs?: number;
  readRatio?: number;

  // Message Queue
  queueCapacity?: number;
  enqueueLatencyMs?: number;

  // Worker
  workerPoolSize?: number;
  jobProcessingTimeMs?: number;

  // Object Storage
  storageLatencyMs?: number;
  storageBandwidthLimitMbps?: number;

  // Dynamic failure overrides
  isFailed?: boolean;
  failureLatencySpikeMs?: number;
  capacityReductionPercent?: number;

  // Live status / execution telemetry
  status?: 'idle' | 'processing' | 'congested' | 'failed';
  currentActiveConcurrency?: number;
  isBottleneck?: boolean;
  bottleneckReason?: string;
}

export interface SimEdgeData {
  id: string;
  sourceNodeId: string;
  targetNodeId: string;
  latencyMs?: number;
  bandwidthMbps?: number;
  packetLossPercent?: number;
  isSevered?: boolean;
}

export interface FailureConfig {
  type: 'NODE_CRASH' | 'LATENCY_SPIKE' | 'LINK_CUT' | 'CAPACITY_DROP';
  targetId: string;
  startSec: number;
  durationSec: number;
  magnitude: number;
}

export interface NodeMetric {
  nodeId: string;
  nodeName: string;
  componentType: ComponentType;
  requestsReceived: number;
  requestsCompleted: number;
  requestsFailed: number;
  avgProcessingTimeMs: number;
  avgQueueWaitTimeMs: number;
  peakQueueDepth: number;
  peakConcurrency: number;
  maxCapacity: number;
  utilizationPercent: number;
  errorRatePercent: number;
  bottleneck: boolean;
  bottleneckReason?: string;
}

export interface TimeSeriesPoint {
  timestampSec: number;
  throughputRps: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  activeInFlight: number;
  errorCount: number;
  queueDepth: number;
}

export interface EventLog {
  eventId: number;
  timestampMs: number;
  type: string;
  requestId: string;
  nodeId: string;
  targetNodeId?: string;
  description: string;
}

export interface BottleneckReport {
  hasBottleneck: boolean;
  bottleneckNodeId?: string;
  bottleneckNodeName?: string;
  bottleneckType?: string;
  severity: 'CRITICAL' | 'WARNING' | 'HEALTHY';
  finding: string;
  recommendation: string;
  estimatedImpact: string;
  observations: string[];
}

export interface SimulationResult {
  runId: string;
  durationSec: number;
  totalRequestsGenerated: number;
  totalRequestsCompleted: number;
  totalRequestsFailed: number;
  totalRequestsTimedOut: number;
  throughputRps: number;
  errorRatePercent: number;
  minLatencyMs: number;
  meanLatencyMs: number;
  p50LatencyMs: number;
  p90LatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  maxLatencyMs: number;
  nodeMetrics: NodeMetric[];
  timeSeries: TimeSeriesPoint[];
  eventLogs: EventLog[];
  bottleneckReport: BottleneckReport;
}

export interface ComparisonResult {
  architectureAName: string;
  architectureBName: string;
  resultA: SimulationResult;
  resultB: SimulationResult;
  throughputDeltaPercent: number;
  p95LatencyDeltaPercent: number;
  errorRateDeltaPercent: number;
  winner: 'ARCHITECTURE_A' | 'ARCHITECTURE_B' | 'EQUIVALENT';
  summary: string;
  tradeoffs: string[];
}

export interface ArchitectureTemplate {
  id?: number;
  name: string;
  description: string;
  isTemplate?: boolean;
  canvasDataJson: string;
  scenarioDataJson: string;
  createdAt?: string;
  updatedAt?: string;
}
