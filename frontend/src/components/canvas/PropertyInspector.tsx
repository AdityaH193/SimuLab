import React from 'react';
import {
  X,
  Trash2,
  Sliders,
  Flame,
  Clock,
  Zap,
  Server,
  Layers,
  HardDrive,
  Database
} from 'lucide-react';
import { useSimulationStore } from '../../store/useSimulationStore';
import type { LoadBalancingAlgorithm, WorkloadDistribution } from '../../types/simulation';

export const PropertyInspector: React.FC = () => {
  const selectedNodeId = useSimulationStore((state) => state.selectedNodeId);
  const nodes = useSimulationStore((state) => state.nodes);
  const updateNodeData = useSimulationStore((state) => state.updateNodeData);
  const deleteNode = useSimulationStore((state) => state.deleteNode);
  const selectNode = useSimulationStore((state) => state.selectNode);

  const selectedNode = nodes.find((n) => n.id === selectedNodeId);

  if (!selectedNode) {
    return (
      <aside className="w-80 bg-[#0E131F] border-l border-[#1F2937] p-4 text-center select-none flex flex-col justify-center items-center text-slate-500">
        <Sliders className="w-8 h-8 mb-2 opacity-40 text-slate-400" />
        <div className="text-xs font-medium text-slate-400">No Component Selected</div>
        <p className="text-[11px] text-slate-500 mt-1 max-w-[180px]">
          Click any component on the canvas to configure its concurrency, latencies, and failure injection.
        </p>
      </aside>
    );
  }

  const { data } = selectedNode;

  return (
    <aside className="w-80 bg-[#0E131F] border-l border-[#1F2937] flex flex-col h-full select-none z-10">
      {/* Header */}
      <div className="p-3.5 border-b border-[#1F2937] flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Sliders className="w-4 h-4 text-blue-400" />
          <h2 className="text-xs font-semibold text-slate-200 uppercase tracking-wider">
            Properties & Tuning
          </h2>
        </div>
        <button
          onClick={() => selectNode(null)}
          className="p-1 rounded hover:bg-slate-800 text-slate-400 hover:text-slate-200 transition-colors"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Form Content */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 text-xs text-slate-300">
        {/* Component Name */}
        <div>
          <label className="block text-[11px] font-medium text-slate-400 mb-1">
            Display Label
          </label>
          <input
            type="text"
            value={data.name || ''}
            onChange={(e) => updateNodeData(selectedNode.id, { name: e.target.value })}
            className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 focus:outline-none focus:border-blue-500 text-xs"
          />
        </div>

        {/* Client Configuration */}
        {data.type === 'CLIENT' && (
          <div className="space-y-3.5 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-emerald-400 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5" /> Traffic Rate (RPS)
              </label>
              <span className="font-mono text-emerald-400 font-semibold">{data.workloadRps ?? 20} req/s</span>
            </div>
            <input
              type="range"
              min="1"
              max="200"
              value={data.workloadRps ?? 20}
              onChange={(e) => updateNodeData(selectedNode.id, { workloadRps: Number(e.target.value) })}
              className="w-full accent-emerald-500 cursor-pointer"
            />

            <div>
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Traffic Distribution
              </label>
              <select
                value={data.workloadDistribution || 'POISSON'}
                onChange={(e) =>
                  updateNodeData(selectedNode.id, {
                    workloadDistribution: e.target.value as WorkloadDistribution,
                  })
                }
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 text-xs"
              >
                <option value="CONSTANT">Constant Interval</option>
                <option value="POISSON">Poisson (Stochastic)</option>
                <option value="BURST">Burst / Spiky Peaks</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Client Timeout (ms)
              </label>
              <input
                type="number"
                min="100"
                max="10000"
                step="100"
                value={data.timeoutMs ?? 2000}
                onChange={(e) => updateNodeData(selectedNode.id, { timeoutMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>
          </div>
        )}

        {/* Load Balancer Configuration */}
        {data.type === 'LOAD_BALANCER' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <label className="block text-[11px] font-medium text-amber-400">
              Balancing Strategy
            </label>
            <select
              value={data.lbAlgorithm || 'ROUND_ROBIN'}
              onChange={(e) =>
                updateNodeData(selectedNode.id, {
                  lbAlgorithm: e.target.value as LoadBalancingAlgorithm,
                })
              }
              className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 text-xs"
            >
              <option value="ROUND_ROBIN">Round Robin</option>
              <option value="LEAST_CONNECTIONS">Least Connections</option>
              <option value="RANDOM">Random Uniform</option>
            </select>
          </div>
        )}

        {/* API Server Configuration */}
        {data.type === 'API_SERVER' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-blue-400 flex items-center gap-1.5">
                <Server className="w-3.5 h-3.5" /> Replicas / Instances
              </label>
              <span className="font-mono font-semibold text-blue-400">{data.instances ?? 1}</span>
            </div>
            <input
              type="range"
              min="1"
              max="8"
              value={data.instances ?? 1}
              onChange={(e) => updateNodeData(selectedNode.id, { instances: Number(e.target.value) })}
              className="w-full accent-blue-500 cursor-pointer"
            />

            <div className="flex items-center justify-between pt-2">
              <label className="text-[11px] font-medium text-slate-400">Threads per Instance</label>
              <span className="font-mono text-slate-300">{data.threadsPerInstance ?? 10}</span>
            </div>
            <input
              type="range"
              min="1"
              max="50"
              value={data.threadsPerInstance ?? 10}
              onChange={(e) => updateNodeData(selectedNode.id, { threadsPerInstance: Number(e.target.value) })}
              className="w-full accent-slate-400 cursor-pointer"
            />

            <div className="pt-2">
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Processing Latency (ms)
              </label>
              <input
                type="number"
                min="1"
                max="500"
                value={data.processingTimeMs ?? 20}
                onChange={(e) => updateNodeData(selectedNode.id, { processingTimeMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>
          </div>
        )}

        {/* Cache Configuration */}
        {data.type === 'CACHE' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-purple-400 flex items-center gap-1.5">
                <Zap className="w-3.5 h-3.5" /> Cache Hit Ratio
              </label>
              <span className="font-mono font-semibold text-purple-400">{data.cacheHitRatio ?? 80}%</span>
            </div>
            <input
              type="range"
              min="0"
              max="100"
              value={data.cacheHitRatio ?? 80}
              onChange={(e) => updateNodeData(selectedNode.id, { cacheHitRatio: Number(e.target.value) })}
              className="w-full accent-purple-500 cursor-pointer"
            />

            <div className="pt-2">
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Cache Access Latency (ms)
              </label>
              <input
                type="number"
                min="0.5"
                max="20"
                step="0.5"
                value={data.cacheLatencyMs ?? 2}
                onChange={(e) => updateNodeData(selectedNode.id, { cacheLatencyMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>
          </div>
        )}

        {/* Database Configuration */}
        {data.type === 'DATABASE' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-rose-400 flex items-center gap-1.5">
                <Database className="w-3.5 h-3.5" /> Connection Pool Size
              </label>
              <span className="font-mono font-semibold text-rose-400">{data.connectionPoolSize ?? 15}</span>
            </div>
            <input
              type="range"
              min="1"
              max="100"
              value={data.connectionPoolSize ?? 15}
              onChange={(e) => updateNodeData(selectedNode.id, { connectionPoolSize: Number(e.target.value) })}
              className="w-full accent-rose-500 cursor-pointer"
            />

            <div className="pt-2">
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Query Execution Latency (ms)
              </label>
              <input
                type="number"
                min="1"
                max="500"
                value={data.queryLatencyMs ?? 35}
                onChange={(e) => updateNodeData(selectedNode.id, { queryLatencyMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>

            <div className="flex items-center justify-between pt-2">
              <label className="text-[11px] font-medium text-slate-400">Read / Write Ratio</label>
              <span className="font-mono text-slate-300">{data.readRatio ?? 80}% Read</span>
            </div>
            <input
              type="range"
              min="0"
              max="100"
              value={data.readRatio ?? 80}
              onChange={(e) => updateNodeData(selectedNode.id, { readRatio: Number(e.target.value) })}
              className="w-full accent-slate-400 cursor-pointer"
            />
          </div>
        )}

        {/* Message Queue Configuration */}
        {data.type === 'MESSAGE_QUEUE' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-yellow-400 flex items-center gap-1.5">
                <Layers className="w-3.5 h-3.5" /> Buffer Capacity
              </label>
              <span className="font-mono font-semibold text-yellow-400">{data.queueCapacity ?? 500} msg</span>
            </div>
            <input
              type="range"
              min="50"
              max="5000"
              step="50"
              value={data.queueCapacity ?? 500}
              onChange={(e) => updateNodeData(selectedNode.id, { queueCapacity: Number(e.target.value) })}
              className="w-full accent-yellow-500 cursor-pointer"
            />

            <div className="pt-2">
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Enqueue Overhead (ms)
              </label>
              <input
                type="number"
                min="0.5"
                max="50"
                value={data.enqueueLatencyMs ?? 3}
                onChange={(e) => updateNodeData(selectedNode.id, { enqueueLatencyMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>
          </div>
        )}

        {/* Worker Configuration */}
        {data.type === 'WORKER' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <div className="flex items-center justify-between">
              <label className="text-[11px] font-medium text-teal-400">Worker Pool Size</label>
              <span className="font-mono font-semibold text-teal-400">{data.workerPoolSize ?? 4}</span>
            </div>
            <input
              type="range"
              min="1"
              max="20"
              value={data.workerPoolSize ?? 4}
              onChange={(e) => updateNodeData(selectedNode.id, { workerPoolSize: Number(e.target.value) })}
              className="w-full accent-teal-500 cursor-pointer"
            />

            <div className="pt-2">
              <label className="block text-[11px] font-medium text-slate-400 mb-1">
                Job Processing Time (ms)
              </label>
              <input
                type="number"
                min="5"
                max="500"
                value={data.jobProcessingTimeMs ?? 40}
                onChange={(e) => updateNodeData(selectedNode.id, { jobProcessingTimeMs: Number(e.target.value) })}
                className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
              />
            </div>
          </div>
        )}

        {/* Object Storage Configuration */}
        {data.type === 'OBJECT_STORAGE' && (
          <div className="space-y-3 p-3 rounded-lg bg-[#131B2E] border border-[#1F2937]">
            <label className="text-[11px] font-medium text-cyan-400 flex items-center gap-1.5">
              <HardDrive className="w-3.5 h-3.5" /> Latency (ms)
            </label>
            <input
              type="number"
              min="5"
              max="500"
              value={data.storageLatencyMs ?? 25}
              onChange={(e) => updateNodeData(selectedNode.id, { storageLatencyMs: Number(e.target.value) })}
              className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2.5 py-1.5 text-slate-200 font-mono text-xs"
            />
          </div>
        )}

        {/* Dynamic Failure Injection Section */}
        <div className="p-3 rounded-lg bg-red-950/20 border border-red-900/40 space-y-3">
          <div className="flex items-center gap-1.5 text-red-400 font-semibold text-xs">
            <Flame className="w-4 h-4" /> Chaos & Failure Injection
          </div>

          <label className="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              checked={!!data.isFailed}
              onChange={(e) => updateNodeData(selectedNode.id, { isFailed: e.target.checked })}
              className="rounded accent-red-500 w-4 h-4"
            />
            <span className="text-slate-300">Force Node Crash (Unavailable)</span>
          </label>

          <div>
            <label className="block text-[11px] text-slate-400 mb-1">
              Add Latency Spike (+ms)
            </label>
            <input
              type="number"
              min="0"
              max="2000"
              step="50"
              value={data.failureLatencySpikeMs ?? 0}
              onChange={(e) =>
                updateNodeData(selectedNode.id, {
                  failureLatencySpikeMs: Number(e.target.value),
                })
              }
              className="w-full bg-[#161F30] border border-[#2D3A52] rounded px-2 py-1 text-slate-200 font-mono text-xs"
            />
          </div>
        </div>

        {/* Delete Component Button */}
        <button
          onClick={() => deleteNode(selectedNode.id)}
          className="w-full py-2 px-3 rounded bg-red-950/40 hover:bg-red-900/60 border border-red-800/40 text-red-400 hover:text-red-300 transition-colors flex items-center justify-center gap-2 text-xs font-medium"
        >
          <Trash2 className="w-3.5 h-3.5" /> Delete Component
        </button>
      </div>
    </aside>
  );
};
