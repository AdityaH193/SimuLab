import { memo } from 'react';
import { Handle, Position } from '@xyflow/react';
import type { NodeProps } from '@xyflow/react';
import {
  Globe,
  Server,
  Zap,
  Database,
  Layers,
  Cpu,
  HardDrive,
  Split,
  AlertTriangle,
  AlertCircle
} from 'lucide-react';
import type { ComponentType, SimNodeData } from '../../types/simulation';

const typeConfig: Record<
  ComponentType,
  {
    icon: any;
    accentColor: string;
    bgBadge: string;
    textBadge: string;
    borderGlow: string;
    label: string;
  }
> = {
  CLIENT: {
    icon: Globe,
    accentColor: '#10B981',
    bgBadge: 'bg-emerald-500/10',
    textBadge: 'text-emerald-400',
    borderGlow: 'hover:border-emerald-500/50',
    label: 'Client Workload',
  },
  LOAD_BALANCER: {
    icon: Split,
    accentColor: '#F59E0B',
    bgBadge: 'bg-amber-500/10',
    textBadge: 'text-amber-400',
    borderGlow: 'hover:border-amber-500/50',
    label: 'Load Balancer',
  },
  API_SERVER: {
    icon: Server,
    accentColor: '#3B82F6',
    bgBadge: 'bg-blue-500/10',
    textBadge: 'text-blue-400',
    borderGlow: 'hover:border-blue-500/50',
    label: 'API Server',
  },
  CACHE: {
    icon: Zap,
    accentColor: '#8B5CF6',
    bgBadge: 'bg-purple-500/10',
    textBadge: 'text-purple-400',
    borderGlow: 'hover:border-purple-500/50',
    label: 'Cache (Redis)',
  },
  DATABASE: {
    icon: Database,
    accentColor: '#F43F5E',
    bgBadge: 'bg-rose-500/10',
    textBadge: 'text-rose-400',
    borderGlow: 'hover:border-rose-500/50',
    label: 'Database',
  },
  MESSAGE_QUEUE: {
    icon: Layers,
    accentColor: '#EAB308',
    bgBadge: 'bg-yellow-500/10',
    textBadge: 'text-yellow-400',
    borderGlow: 'hover:border-yellow-500/50',
    label: 'Message Queue',
  },
  WORKER: {
    icon: Cpu,
    accentColor: '#14B8A6',
    bgBadge: 'bg-teal-500/10',
    textBadge: 'text-teal-400',
    borderGlow: 'hover:border-teal-500/50',
    label: 'Worker Pool',
  },
  OBJECT_STORAGE: {
    icon: HardDrive,
    accentColor: '#06B6D4',
    bgBadge: 'bg-cyan-500/10',
    textBadge: 'text-cyan-400',
    borderGlow: 'hover:border-cyan-500/50',
    label: 'Object Storage',
  },
};

export const CustomServiceNode = memo(({ data, selected }: NodeProps<any>) => {
  const nodeData = data as SimNodeData;
  const config = typeConfig[nodeData.type] || typeConfig.API_SERVER;
  const Icon = config.icon;

  const isFailed = nodeData.isFailed;
  const isBottleneck = nodeData.isBottleneck;

  const getMetricSummary = () => {
    switch (nodeData.type) {
      case 'CLIENT':
        return `${nodeData.workloadRps ?? 20} RPS • ${nodeData.workloadDistribution || 'POISSON'}`;
      case 'LOAD_BALANCER':
        return `${nodeData.lbAlgorithm || 'ROUND_ROBIN'}`;
      case 'API_SERVER':
        return `${nodeData.instances ?? 1}x inst • ${nodeData.processingTimeMs ?? 20}ms`;
      case 'CACHE':
        return `${nodeData.cacheHitRatio ?? 80}% hit • ${nodeData.cacheLatencyMs ?? 2}ms`;
      case 'DATABASE':
        return `Pool: ${nodeData.connectionPoolSize ?? 15} • ${nodeData.queryLatencyMs ?? 35}ms`;
      case 'MESSAGE_QUEUE':
        return `Cap: ${nodeData.queueCapacity ?? 500} • ${nodeData.enqueueLatencyMs ?? 3}ms`;
      case 'WORKER':
        return `${nodeData.workerPoolSize ?? 4} workers • ${nodeData.jobProcessingTimeMs ?? 40}ms`;
      case 'OBJECT_STORAGE':
        return `${nodeData.storageLatencyMs ?? 25}ms latency`;
      default:
        return '';
    }
  };

  return (
    <div
      className={`relative min-w-[210px] rounded-xl border backdrop-blur-md transition-all duration-200 select-none shadow-xl ${
        selected
          ? 'border-blue-500 ring-2 ring-blue-500/30 bg-[#161F30]'
          : isBottleneck
          ? 'border-red-500 ring-2 ring-red-500/40 bg-[#25151A]'
          : isFailed
          ? 'border-red-800 bg-[#221316] opacity-75'
          : 'border-[#263145] bg-[#111827] hover:border-[#3B4C68]'
      }`}
    >
      {/* Target connection handles */}
      <Handle
        type="target"
        position={Position.Top}
        className="!w-2.5 !h-2.5 !bg-blue-400 !border-2 !border-[#0B0F17] hover:!scale-125 transition-transform"
      />
      <Handle
        type="target"
        position={Position.Left}
        className="!w-2.5 !h-2.5 !bg-blue-400 !border-2 !border-[#0B0F17] hover:!scale-125 transition-transform"
      />

      {/* Header bar with icon and title */}
      <div className="p-3">
        <div className="flex items-center justify-between gap-2 mb-2">
          <div className="flex items-center gap-2">
            <div
              className="p-1.5 rounded-lg flex items-center justify-center shadow-inner"
              style={{ backgroundColor: `${config.accentColor}20` }}
            >
              <Icon className="w-4 h-4" style={{ color: config.accentColor }} />
            </div>
            <div>
              <div className="text-[10px] font-mono uppercase tracking-wider text-slate-400">
                {config.label}
              </div>
              <div className="text-xs font-semibold text-slate-100 truncate max-w-[125px]">
                {nodeData.name || 'Service Node'}
              </div>
            </div>
          </div>

          {/* Status badge */}
          {isBottleneck ? (
            <span className="flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-500/20 text-red-400 border border-red-500/30 animate-pulse">
              <AlertTriangle className="w-3 h-3 text-red-400" />
              HOT
            </span>
          ) : isFailed ? (
            <span className="flex items-center gap-1 px-1.5 py-0.5 rounded text-[10px] font-bold bg-red-900/40 text-red-400 border border-red-700/50">
              <AlertCircle className="w-3 h-3" />
              DOWN
            </span>
          ) : (
            <span className="w-2 h-2 rounded-full bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.8)]" />
          )}
        </div>

        {/* Metrics Subtitle */}
        <div className="mt-1 pt-2 border-t border-[#1F2937] flex items-center justify-between text-[11px] text-slate-400 font-mono">
          <span>{getMetricSummary()}</span>
        </div>

        {/* Bottleneck Diagnostic Banner */}
        {isBottleneck && nodeData.bottleneckReason && (
          <div className="mt-2 text-[10px] text-red-300 bg-red-950/60 p-1.5 rounded border border-red-800/40 line-clamp-2">
            ⚠️ {nodeData.bottleneckReason}
          </div>
        )}
      </div>

      {/* Source connection handles */}
      <Handle
        type="source"
        position={Position.Bottom}
        className="!w-2.5 !h-2.5 !bg-emerald-400 !border-2 !border-[#0B0F17] hover:!scale-125 transition-transform"
      />
      <Handle
        type="source"
        position={Position.Right}
        className="!w-2.5 !h-2.5 !bg-emerald-400 !border-2 !border-[#0B0F17] hover:!scale-125 transition-transform"
      />
    </div>
  );
});

CustomServiceNode.displayName = 'CustomServiceNode';
