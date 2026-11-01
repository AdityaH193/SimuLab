import React, { useState, useEffect } from 'react';
import {
  X,
  GitCompare,
  Trophy,
  Loader2,
  CheckCircle2
} from 'lucide-react';
import { useSimulationStore } from '../../store/useSimulationStore';
import { api } from '../../services/api';
import type { ArchitectureTemplate } from '../../types/simulation';

export const ArchitectureComparisonModal: React.FC = () => {
  const {
    isComparisonOpen,
    setComparisonOpen,
    nodes: currentNodes,
    edges: currentEdges,
    runComparison,
    comparisonResult,
    isComparing,
  } = useSimulationStore();

  const [templates, setTemplates] = useState<ArchitectureTemplate[]>([]);
  const [selectedTemplateB, setSelectedTemplateB] = useState<string>('Microservices with Redis Cache');

  useEffect(() => {
    if (isComparisonOpen) {
      api.getTemplates()
        .then((data) => setTemplates(data))
        .catch((err) => console.error('Failed to load templates for comparison', err));
    }
  }, [isComparisonOpen]);

  if (!isComparisonOpen) return null;

  const handleRunComparison = async () => {
    const templateB = templates.find((t) => t.name === selectedTemplateB) || templates[0];
    let nodesB = currentNodes;
    let edgesB = currentEdges;

    if (templateB) {
      try {
        const parsed = JSON.parse(templateB.canvasDataJson);
        nodesB = parsed.nodes || [];
        edgesB = parsed.edges || [];
      } catch (e) {
        console.error(e);
      }
    }

    await runComparison(
      'Current Canvas Design',
      currentNodes,
      currentEdges,
      selectedTemplateB,
      nodesB,
      edgesB
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4">
      <div className="w-full max-w-4xl bg-[#0E131F] border border-[#2D3A52] rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-[#1F2937] flex items-center justify-between bg-[#131B2E]">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-lg bg-purple-500/20 text-purple-400">
              <GitCompare className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-100">
                Side-by-Side Architecture Comparison Engine
              </h2>
              <p className="text-xs text-slate-400">
                Run identical workloads & deterministic seeds across two architectures to analyze tradeoffs.
              </p>
            </div>
          </div>
          <button
            onClick={() => setComparisonOpen(false)}
            className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-slate-200 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Comparison Setup Bar */}
        <div className="p-4 border-b border-[#1F2937] bg-[#0B0F17] flex flex-wrap items-center justify-between gap-4 text-xs">
          <div className="flex items-center gap-3">
            <span className="font-semibold text-blue-400">Arch A: Current Canvas</span>
            <span className="text-slate-500 font-bold">VS</span>
            <div className="flex items-center gap-2">
              <span className="font-semibold text-purple-400">Arch B:</span>
              <select
                value={selectedTemplateB}
                onChange={(e) => setSelectedTemplateB(e.target.value)}
                className="bg-[#161F30] border border-[#2D3A52] rounded-lg px-2.5 py-1.5 text-slate-200 font-medium text-xs focus:outline-none focus:border-purple-500"
              >
                {templates.map((t) => (
                  <option key={t.id || t.name} value={t.name}>
                    {t.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <button
            onClick={handleRunComparison}
            disabled={isComparing}
            className="px-4 py-2 rounded-lg bg-purple-600 hover:bg-purple-500 text-white font-semibold text-xs transition-colors flex items-center gap-2 shadow-md cursor-pointer disabled:opacity-50"
          >
            {isComparing ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" /> Simulating Both...
              </>
            ) : (
              <>
                <GitCompare className="w-4 h-4" /> Compare Designs
              </>
            )}
          </button>
        </div>

        {/* Comparison Results Content */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6 text-xs">
          {!comparisonResult ? (
            <div className="text-center py-12 text-slate-500">
              <GitCompare className="w-10 h-10 mx-auto mb-2 opacity-30 text-purple-400" />
              <p>Click &quot;Compare Designs&quot; to execute identical deterministic runs.</p>
            </div>
          ) : (
            <>
              {/* Winner Verdict Banner */}
              <div className="p-4 rounded-xl bg-gradient-to-r from-purple-950/40 via-indigo-950/30 to-blue-950/40 border border-purple-800/40 flex items-center gap-3.5">
                <div className="p-3 rounded-xl bg-purple-500/20 text-purple-300">
                  <Trophy className="w-6 h-6" />
                </div>
                <div>
                  <div className="text-xs font-mono font-bold uppercase tracking-wider text-purple-300">
                    Engine Tradeoff Verdict: {comparisonResult.winner.replace('_', ' ')}
                  </div>
                  <p className="text-sm font-semibold text-slate-100 mt-0.5">
                    {comparisonResult.summary}
                  </p>
                </div>
              </div>

              {/* Side-by-Side KPI Cards */}
              <div className="grid grid-cols-2 gap-6">
                {/* Architecture A Card */}
                <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] space-y-3">
                  <div className="text-xs font-bold text-blue-400 uppercase tracking-wider flex items-center justify-between">
                    <span>{comparisonResult.architectureAName}</span>
                    <span className="text-[10px] text-slate-500">Arch A</span>
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Throughput:</span>
                      <span className="font-mono font-bold text-slate-100">
                        {comparisonResult.resultA.throughputRps.toFixed(1)} RPS
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">P95 Latency:</span>
                      <span className="font-mono font-bold text-slate-100">
                        {comparisonResult.resultA.p95LatencyMs.toFixed(1)} ms
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Error Rate:</span>
                      <span className="font-mono font-bold text-slate-100">
                        {comparisonResult.resultA.errorRatePercent.toFixed(1)} %
                      </span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Bottleneck:</span>
                      <span className="font-medium text-slate-300 truncate max-w-[150px]">
                        {comparisonResult.resultA.bottleneckReport?.bottleneckNodeName || 'None'}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Architecture B Card */}
                <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] space-y-3">
                  <div className="text-xs font-bold text-purple-400 uppercase tracking-wider flex items-center justify-between">
                    <span>{comparisonResult.architectureBName}</span>
                    <span className="text-[10px] text-slate-500">Arch B</span>
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Throughput:</span>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-slate-100">
                          {comparisonResult.resultB.throughputRps.toFixed(1)} RPS
                        </span>
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                            comparisonResult.throughputDeltaPercent >= 0
                              ? 'bg-emerald-500/20 text-emerald-400'
                              : 'bg-red-500/20 text-red-400'
                          }`}
                        >
                          {comparisonResult.throughputDeltaPercent >= 0 ? '+' : ''}
                          {comparisonResult.throughputDeltaPercent.toFixed(1)}%
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">P95 Latency:</span>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-slate-100">
                          {comparisonResult.resultB.p95LatencyMs.toFixed(1)} ms
                        </span>
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                            comparisonResult.p95LatencyDeltaPercent <= 0
                              ? 'bg-emerald-500/20 text-emerald-400'
                              : 'bg-red-500/20 text-red-400'
                          }`}
                        >
                          {comparisonResult.p95LatencyDeltaPercent.toFixed(1)}%
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Error Rate:</span>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-slate-100">
                          {comparisonResult.resultB.errorRatePercent.toFixed(1)} %
                        </span>
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                            comparisonResult.errorRateDeltaPercent <= 0
                              ? 'bg-emerald-500/20 text-emerald-400'
                              : 'bg-red-500/20 text-red-400'
                          }`}
                        >
                          {comparisonResult.errorRateDeltaPercent <= 0 ? '' : '+'}
                          {comparisonResult.errorRateDeltaPercent.toFixed(1)}%
                        </span>
                      </div>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="text-slate-400">Bottleneck:</span>
                      <span className="font-medium text-slate-300 truncate max-w-[150px]">
                        {comparisonResult.resultB.bottleneckReport?.bottleneckNodeName || 'None'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Tradeoff Observations List */}
              <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] space-y-2">
                <h3 className="text-xs font-bold text-slate-200 uppercase tracking-wider mb-2">
                  Key Architectural Tradeoffs & Insights
                </h3>
                {comparisonResult.tradeoffs.map((tradeoff, idx) => (
                  <div key={idx} className="flex items-start gap-2 text-slate-300 leading-relaxed">
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 mt-0.5 shrink-0" />
                    <span>{tradeoff}</span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
