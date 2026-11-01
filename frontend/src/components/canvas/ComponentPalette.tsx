import React from 'react';
import {
  Globe,
  Server,
  Zap,
  Database,
  Layers,
  Cpu,
  HardDrive,
  Split,
  Plus
} from 'lucide-react';
import type { ComponentType } from '../../types/simulation';
import { useSimulationStore } from '../../store/useSimulationStore';

interface PaletteItem {
  type: ComponentType;
  title: string;
  description: string;
  icon: React.ComponentType<any>;
  color: string;
}

const items: PaletteItem[] = [
  {
    type: 'CLIENT',
    title: 'Client Workload',
    description: 'Generates RPS, concurrency, Poisson/burst traffic',
    icon: Globe,
    color: '#10B981',
  },
  {
    type: 'LOAD_BALANCER',
    title: 'Load Balancer',
    description: 'Round-Robin, Least-Connections routing',
    icon: Split,
    color: '#F59E0B',
  },
  {
    type: 'API_SERVER',
    title: 'API Server',
    description: 'Stateless service with thread pool & instances',
    icon: Server,
    color: '#3B82F6',
  },
  {
    type: 'CACHE',
    title: 'In-Memory Cache',
    description: 'Redis/Memcached cache with configurable hit ratio',
    icon: Zap,
    color: '#8B5CF6',
  },
  {
    type: 'DATABASE',
    title: 'Database',
    description: 'Relational DB with connection pool limits',
    icon: Database,
    color: '#F43F5E',
  },
  {
    type: 'MESSAGE_QUEUE',
    title: 'Message Queue',
    description: 'Asynchronous buffer with capacity & backpressure',
    icon: Layers,
    color: '#EAB308',
  },
  {
    type: 'WORKER',
    title: 'Worker Service',
    description: 'Background worker pool draining queued jobs',
    icon: Cpu,
    color: '#14B8A6',
  },
  {
    type: 'OBJECT_STORAGE',
    title: 'Object Storage',
    description: 'Blob storage with bandwidth & I/O latency',
    icon: HardDrive,
    color: '#06B6D4',
  },
];

export const ComponentPalette: React.FC = () => {
  const addNode = useSimulationStore((state) => state.addNode);

  const onDragStart = (event: React.DragEvent, nodeType: ComponentType) => {
    event.dataTransfer.setData('application/reactflow/type', nodeType);
    event.dataTransfer.effectAllowed = 'move';
  };

  return (
    <aside className="w-64 bg-[#0E131F] border-r border-[#1F2937] flex flex-col h-full select-none z-10">
      <div className="p-3 border-b border-[#1F2937] flex items-center justify-between">
        <div>
          <h2 className="text-xs font-semibold text-slate-200 uppercase tracking-wider">
            Component Palette
          </h2>
          <p className="text-[11px] text-slate-500">Drag or click to add node</p>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto p-2.5 space-y-2">
        {items.map((item) => {
          const Icon = item.icon;
          return (
            <div
              key={item.type}
              draggable
              onDragStart={(e) => onDragStart(e, item.type)}
              onClick={() => addNode(item.type)}
              className="group p-2.5 rounded-lg border border-[#1F2937] bg-[#131B2E]/60 hover:bg-[#1A243D] hover:border-[#374151] transition-all cursor-pointer shadow-sm hover:shadow-md flex items-center justify-between"
            >
              <div className="flex items-center gap-2.5">
                <div
                  className="w-8 h-8 rounded-lg flex items-center justify-center transition-transform group-hover:scale-110"
                  style={{ backgroundColor: `${item.color}18` }}
                >
                  <Icon className="w-4 h-4" style={{ color: item.color }} />
                </div>
                <div>
                  <div className="text-xs font-medium text-slate-200 group-hover:text-blue-400 transition-colors">
                    {item.title}
                  </div>
                  <div className="text-[10px] text-slate-400 leading-tight line-clamp-1">
                    {item.description}
                  </div>
                </div>
              </div>
              <Plus className="w-3.5 h-3.5 text-slate-500 opacity-0 group-hover:opacity-100 transition-opacity" />
            </div>
          );
        })}
      </div>
    </aside>
  );
};
