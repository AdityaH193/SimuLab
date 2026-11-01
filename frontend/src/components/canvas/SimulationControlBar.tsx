import React from 'react';
import {
  Play,
  Dice5,
  Flame,
  BarChart3,
  GitCompare,
  LayoutGrid,
  Loader2,
  Trash2
} from 'lucide-react';
import { useSimulationStore } from '../../store/useSimulationStore';

export const SimulationControlBar: React.FC = () => {
  const {
    durationSec,
    setDurationSec,
    randomSeed,
    setRandomSeed,
    isRunning,
    runSimulation,
    activeTab,
    setActiveTab,
    clearCanvas,
    setComparisonOpen,
    nodes,
    updateNodeData,
  } = useSimulationStore();

  const handleRandomizeSeed = () => {
    setRandomSeed(Math.floor(Math.random() * 100000) + 1);
  };

  const handleQuickKillDb = () => {
    const dbNode = nodes.find((n) => n.data.type === 'DATABASE');
    if (dbNode) {
      updateNodeData(dbNode.id, { isFailed: !dbNode.data.isFailed });
    }
  };

  const handleQuickLatencySpike = () => {
    const apiNode = nodes.find((n) => n.data.type === 'API_SERVER');
    if (apiNode) {
      const current = apiNode.data.failureLatencySpikeMs || 0;
      updateNodeData(apiNode.id, { failureLatencySpikeMs: current > 0 ? 0 : 350 });
    }
  };

  return (
    <div className="bg-[#0B0F17] border-t border-[#1F2937] px-4 py-2.5 flex items-center justify-between select-none z-20">
      {/* Left: View Switching Tabs */}
      <div className="flex items-center gap-1.5 bg-[#131B2E] p-1 rounded-lg border border-[#1F2937]">
        <button
          onClick={() => setActiveTab('canvas')}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
            activeTab === 'canvas'
              ? 'bg-blue-600 text-white shadow-sm'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <LayoutGrid className="w-3.5 h-3.5" /> Architecture Canvas
        </button>
        <button
          onClick={() => setActiveTab('analytics')}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium transition-all ${
            activeTab === 'analytics'
              ? 'bg-blue-600 text-white shadow-sm'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <BarChart3 className="w-3.5 h-3.5" /> Performance Analytics
        </button>
        <button
          onClick={() => {
            setComparisonOpen(true);
          }}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium text-slate-400 hover:text-slate-200 transition-all hover:bg-slate-800/60"
        >
          <GitCompare className="w-3.5 h-3.5 text-purple-400" /> Compare Designs
        </button>
      </div>

      {/* Middle: Simulation Controls */}
      <div className="flex items-center gap-4 text-xs">
        {/* Duration */}
        <div className="flex items-center gap-2 text-slate-400">
          <span className="text-[11px] font-medium">Duration:</span>
          <div className="flex items-center gap-1">
            {[5, 10, 20].map((sec) => (
              <button
                key={sec}
                onClick={() => setDurationSec(sec)}
                className={`px-2 py-0.5 rounded text-[11px] font-mono transition-colors ${
                  durationSec === sec
                    ? 'bg-slate-700 text-slate-100 font-semibold'
                    : 'text-slate-500 hover:text-slate-300'
                }`}
              >
                {sec}s
              </button>
            ))}
          </div>
        </div>

        {/* Seed */}
        <div className="flex items-center gap-1.5 text-slate-400">
          <span className="text-[11px] font-medium">Seed:</span>
          <input
            type="number"
            value={randomSeed}
            onChange={(e) => setRandomSeed(Number(e.target.value))}
            className="w-16 bg-[#161F30] border border-[#2D3A52] rounded px-1.5 py-0.5 text-slate-200 font-mono text-[11px] text-center"
          />
          <button
            onClick={handleRandomizeSeed}
            title="Randomize Seed"
            className="p-1 rounded hover:bg-slate-800 text-slate-400 hover:text-slate-200"
          >
            <Dice5 className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Quick Chaos Buttons */}
        <div className="h-4 w-[1px] bg-[#1F2937]" />
        <div className="flex items-center gap-2">
          <button
            onClick={handleQuickLatencySpike}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-amber-950/40 border border-amber-900/50 hover:bg-amber-900/60 text-amber-300 text-[11px] font-medium transition-colors"
            title="Inject 350ms latency spike onto API server"
          >
            <Flame className="w-3 h-3 text-amber-400" /> Latency Spike
          </button>
          <button
            onClick={handleQuickKillDb}
            className="flex items-center gap-1 px-2.5 py-1 rounded bg-red-950/40 border border-red-900/50 hover:bg-red-900/60 text-red-300 text-[11px] font-medium transition-colors"
            title="Simulate primary database crash"
          >
            <Flame className="w-3 h-3 text-red-400" /> Crash Database
          </button>
        </div>
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2.5">
        <button
          onClick={clearCanvas}
          className="p-1.5 rounded hover:bg-slate-800/80 text-slate-500 hover:text-slate-300 transition-colors"
          title="Clear Canvas"
        >
          <Trash2 className="w-4 h-4" />
        </button>

        {/* Primary Run Button */}
        <button
          onClick={runSimulation}
          disabled={isRunning}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-semibold text-xs transition-all shadow-[0_0_15px_rgba(59,130,246,0.3)] hover:shadow-[0_0_20px_rgba(59,130,246,0.5)] disabled:opacity-50 cursor-pointer"
        >
          {isRunning ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Simulating Engine...
            </>
          ) : (
            <>
              <Play className="w-4 h-4 fill-white" />
              Run Simulation
            </>
          )}
        </button>
      </div>
    </div>
  );
};
