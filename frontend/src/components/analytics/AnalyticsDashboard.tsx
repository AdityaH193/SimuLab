import React, { useState } from 'react';
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Gauge,
  Lightbulb,
  ShieldAlert,
  Sliders,
  TrendingUp,
  ListFilter
} from 'lucide-react';
import {
  AreaChart,
  Area,
  LineChart,
  Line,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
  Legend
} from 'recharts';
import { useSimulationStore } from '../../store/useSimulationStore';

export const AnalyticsDashboard: React.FC = () => {
  const { result, setActiveTab, runSimulation, isRunning } = useSimulationStore();
  const [logFilter, setLogFilter] = useState<'ALL' | 'ERRORS'>('ALL');

  if (!result) {
    return (
      <div className="flex-1 flex flex-col items-center justify-center p-8 text-center bg-[#0B0F17] select-none">
        <Gauge className="w-12 h-12 text-slate-600 mb-3 animate-pulse" />
        <h3 className="text-sm font-semibold text-slate-300">No Simulation Data Available</h3>
        <p className="text-xs text-slate-500 max-w-sm mt-1 mb-4">
          Execute a discrete-event simulation run to analyze latency percentiles, component saturation, and automated recommendations.
        </p>
        <button
          onClick={runSimulation}
          disabled={isRunning}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-xs font-semibold shadow-md transition-colors"
        >
          Run Simulation Now
        </button>
      </div>
    );
  }

  const { bottleneckReport, nodeMetrics, timeSeries, eventLogs } = result;

  const filteredLogs = eventLogs.filter((log) => {
    if (logFilter === 'ERRORS') {
      return log.type.includes('FAIL') || log.type.includes('TIMEOUT') || log.description.includes('fail');
    }
    return true;
  });

  return (
    <div className="flex-1 overflow-y-auto p-6 bg-[#0B0F17] text-slate-200 space-y-6">
      {/* Top Bar with Run Meta & Switch to Canvas */}
      <div className="flex items-center justify-between border-b border-[#1F2937] pb-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-lg font-bold text-slate-100">Simulation Performance Report</h1>
            <span className="text-xs font-mono px-2 py-0.5 rounded bg-blue-950 text-blue-400 border border-blue-800">
              Run: {result.runId.slice(0, 8)}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-0.5">
            Discrete-event execution over {result.durationSec}s simulated time. Total requests generated:{' '}
            {result.totalRequestsGenerated}.
          </p>
        </div>
        <button
          onClick={() => setActiveTab('canvas')}
          className="px-3 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium transition-colors"
        >
          ← Return to Architecture Canvas
        </button>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Throughput */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] shadow-sm">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>Sustained Throughput</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100">
            {result.throughputRps.toFixed(1)} <span className="text-xs text-slate-500 font-sans">req/s</span>
          </div>
          <div className="text-[11px] text-emerald-400 mt-1 flex items-center gap-1">
            ✓ {result.totalRequestsCompleted} requests completed
          </div>
        </div>

        {/* P95 & P99 Latency */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] shadow-sm">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>Tail Latency (P95 / P99)</span>
            <Clock className="w-4 h-4 text-blue-400" />
          </div>
          <div className="text-2xl font-bold font-mono text-slate-100">
            {result.p95LatencyMs.toFixed(1)} <span className="text-xs text-slate-500 font-sans">ms</span>
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            P50: {result.p50LatencyMs.toFixed(1)}ms • P99: {result.p99LatencyMs.toFixed(1)}ms
          </div>
        </div>

        {/* Error Rate */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] shadow-sm">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>Error / Drop Rate</span>
            <ShieldAlert
              className={`w-4 h-4 ${result.errorRatePercent > 0 ? 'text-red-400' : 'text-emerald-400'}`}
            />
          </div>
          <div
            className={`text-2xl font-bold font-mono ${
              result.errorRatePercent > 0 ? 'text-red-400' : 'text-emerald-400'
            }`}
          >
            {result.errorRatePercent.toFixed(1)}%
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            {result.totalRequestsFailed} dropped • {result.totalRequestsTimedOut} timed out
          </div>
        </div>

        {/* Bottleneck Status */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937] shadow-sm">
          <div className="flex items-center justify-between text-xs text-slate-400 mb-1">
            <span>System Health</span>
            {bottleneckReport.severity === 'CRITICAL' ? (
              <AlertTriangle className="w-4 h-4 text-red-400" />
            ) : bottleneckReport.severity === 'WARNING' ? (
              <AlertTriangle className="w-4 h-4 text-amber-400" />
            ) : (
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            )}
          </div>
          <div className="text-base font-bold text-slate-100 truncate">
            {bottleneckReport.hasBottleneck
              ? bottleneckReport.bottleneckNodeName
              : 'Optimal (No Bottlenecks)'}
          </div>
          <div
            className={`text-[11px] mt-1 font-semibold ${
              bottleneckReport.severity === 'CRITICAL'
                ? 'text-red-400'
                : bottleneckReport.severity === 'WARNING'
                ? 'text-amber-400'
                : 'text-emerald-400'
            }`}
          >
            {bottleneckReport.severity}
          </div>
        </div>
      </div>

      {/* Automated Architectural Diagnosis & Recommendations */}
      <div
        className={`p-5 rounded-xl border ${
          bottleneckReport.severity === 'CRITICAL'
            ? 'bg-red-950/20 border-red-900/60'
            : bottleneckReport.severity === 'WARNING'
            ? 'bg-amber-950/20 border-amber-900/60'
            : 'bg-emerald-950/20 border-emerald-900/60'
        }`}
      >
        <div className="flex items-start gap-3">
          <div
            className={`p-2 rounded-lg ${
              bottleneckReport.severity === 'CRITICAL'
                ? 'bg-red-500/20 text-red-400'
                : bottleneckReport.severity === 'WARNING'
                ? 'bg-amber-500/20 text-amber-400'
                : 'bg-emerald-500/20 text-emerald-400'
            }`}
          >
            <Lightbulb className="w-5 h-5" />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2">
              <h2 className="text-sm font-bold text-slate-100">
                Automated Bottleneck Diagnosis & Architectural Advisory
              </h2>
              <span
                className={`text-[10px] uppercase font-bold px-2 py-0.5 rounded ${
                  bottleneckReport.severity === 'CRITICAL'
                    ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                    : bottleneckReport.severity === 'WARNING'
                    ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                    : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                }`}
              >
                {bottleneckReport.severity}
              </span>
            </div>

            <p className="text-xs text-slate-300 mt-1.5 leading-relaxed">
              {bottleneckReport.finding}
            </p>

            <div className="mt-3 p-3 rounded-lg bg-[#0B0F17]/80 border border-[#1F2937] space-y-1.5">
              <div className="text-xs font-semibold text-blue-400 flex items-center gap-1.5">
                <span>Recommended Architectural Action:</span>
              </div>
              <p className="text-xs text-slate-300 leading-relaxed">
                {bottleneckReport.recommendation}
              </p>
              {bottleneckReport.estimatedImpact && (
                <p className="text-[11px] text-emerald-400 font-medium">
                  Expected Outcome: {bottleneckReport.estimatedImpact}
                </p>
              )}
            </div>

            {bottleneckReport.observations?.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {bottleneckReport.observations.map((obs, idx) => (
                  <span
                    key={idx}
                    className="text-[11px] px-2.5 py-1 rounded bg-[#161F30] border border-[#263145] text-slate-300"
                  >
                    • {obs}
                  </span>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recharts Analytics Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart 1: Throughput & Errors Over Time */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937]">
          <h3 className="text-xs font-semibold text-slate-200 mb-3 flex items-center gap-2">
            <Activity className="w-4 h-4 text-blue-400" />
            Throughput (RPS) & Error Count Over Time
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={timeSeries}>
                <defs>
                  <linearGradient id="throughputGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" />
                <XAxis dataKey="timestampSec" stroke="#6B7280" unit="s" fontSize={11} />
                <YAxis stroke="#6B7280" fontSize={11} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#111827',
                    borderColor: '#374151',
                    fontSize: 12,
                  }}
                />
                <Legend />
                <Area
                  type="monotone"
                  dataKey="throughputRps"
                  name="Throughput (RPS)"
                  stroke="#3B82F6"
                  fillOpacity={1}
                  fill="url(#throughputGrad)"
                />
                <Line
                  type="monotone"
                  dataKey="errorCount"
                  name="Errors"
                  stroke="#EF4444"
                  strokeWidth={2}
                  dot={{ r: 3 }}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Chart 2: Latency Percentiles Over Time */}
        <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937]">
          <h3 className="text-xs font-semibold text-slate-200 mb-3 flex items-center gap-2">
            <Clock className="w-4 h-4 text-purple-400" />
            Latency Trajectory (Avg vs P95 Latency ms)
          </h3>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={timeSeries}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1F2937" />
                <XAxis dataKey="timestampSec" stroke="#6B7280" unit="s" fontSize={11} />
                <YAxis stroke="#6B7280" fontSize={11} unit="ms" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#111827',
                    borderColor: '#374151',
                    fontSize: 12,
                  }}
                />
                <Legend />
                <Line
                  type="monotone"
                  dataKey="p95LatencyMs"
                  name="P95 Latency (ms)"
                  stroke="#A855F7"
                  strokeWidth={2}
                  dot={false}
                />
                <Line
                  type="monotone"
                  dataKey="avgLatencyMs"
                  name="Avg Latency (ms)"
                  stroke="#06B6D4"
                  strokeWidth={2}
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Component Utilization & Contention Breakdown Table */}
      <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937]">
        <h3 className="text-xs font-semibold text-slate-200 mb-3 flex items-center gap-2">
          <Sliders className="w-4 h-4 text-indigo-400" />
          Component Utilization & Capacity Breakdown
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-300">
            <thead className="bg-[#161F30] text-slate-400 font-mono text-[11px] uppercase border-b border-[#1F2937]">
              <tr>
                <th className="py-2.5 px-3">Node</th>
                <th className="py-2.5 px-3">Type</th>
                <th className="py-2.5 px-3">Utilization</th>
                <th className="py-2.5 px-3">Avg Proc Time</th>
                <th className="py-2.5 px-3">Avg Queue Wait</th>
                <th className="py-2.5 px-3">Peak Concurrency</th>
                <th className="py-2.5 px-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#1F2937]">
              {nodeMetrics.map((nm) => (
                <tr key={nm.nodeId} className="hover:bg-[#161F30]/60 transition-colors">
                  <td className="py-2.5 px-3 font-medium text-slate-100 flex items-center gap-2">
                    {nm.nodeName}
                    {nm.bottleneck && (
                      <span className="px-1.5 py-0.2 rounded text-[10px] font-bold bg-red-500/20 text-red-400 border border-red-500/30">
                        BOTTLENECK
                      </span>
                    )}
                  </td>
                  <td className="py-2.5 px-3 font-mono text-[11px] text-slate-400">
                    {nm.componentType}
                  </td>
                  <td className="py-2.5 px-3">
                    <div className="flex items-center gap-2">
                      <div className="w-24 bg-slate-800 rounded-full h-2 overflow-hidden">
                        <div
                          className={`h-full rounded-full ${
                            nm.utilizationPercent > 80
                              ? 'bg-red-500'
                              : nm.utilizationPercent > 50
                              ? 'bg-amber-500'
                              : 'bg-emerald-500'
                          }`}
                          style={{ width: `${Math.min(100, nm.utilizationPercent)}%` }}
                        />
                      </div>
                      <span className="font-mono text-[11px]">{nm.utilizationPercent.toFixed(1)}%</span>
                    </div>
                  </td>
                  <td className="py-2.5 px-3 font-mono">{nm.avgProcessingTimeMs.toFixed(1)}ms</td>
                  <td className="py-2.5 px-3 font-mono">
                    <span
                      className={
                        nm.avgQueueWaitTimeMs > 30 ? 'text-red-400 font-semibold' : 'text-slate-300'
                      }
                    >
                      {nm.avgQueueWaitTimeMs.toFixed(1)}ms
                    </span>
                  </td>
                  <td className="py-2.5 px-3 font-mono">
                    {nm.peakConcurrency} / {nm.maxCapacity}
                  </td>
                  <td className="py-2.5 px-3">
                    {nm.errorRatePercent > 0 ? (
                      <span className="text-red-400 font-semibold">
                        {nm.errorRatePercent.toFixed(1)}% Drop
                      </span>
                    ) : (
                      <span className="text-emerald-400">Healthy</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Discrete Simulation Event Stream Table */}
      <div className="p-4 rounded-xl bg-[#111827] border border-[#1F2937]">
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-xs font-semibold text-slate-200 flex items-center gap-2">
            <ListFilter className="w-4 h-4 text-emerald-400" />
            Discrete-Event Trace Stream ({filteredLogs.length} events logged)
          </h3>
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setLogFilter('ALL')}
              className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors ${
                logFilter === 'ALL'
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-800 text-slate-400 hover:text-slate-200'
              }`}
            >
              All Events
            </button>
            <button
              onClick={() => setLogFilter('ERRORS')}
              className={`px-2.5 py-1 rounded text-[11px] font-medium transition-colors ${
                logFilter === 'ERRORS'
                  ? 'bg-red-600 text-white'
                  : 'bg-slate-800 text-slate-400 hover:text-slate-200'
              }`}
            >
              Failures Only
            </button>
          </div>
        </div>

        <div className="max-h-64 overflow-y-auto font-mono text-[11px] space-y-1 bg-[#0B0F17] p-2.5 rounded-lg border border-[#1F2937]">
          {filteredLogs.map((log) => (
            <div
              key={log.eventId}
              className={`flex items-center gap-3 p-1 rounded ${
                log.type.includes('FAIL') || log.type.includes('TIMEOUT')
                  ? 'bg-red-950/40 text-red-300 border-l-2 border-red-500'
                  : 'text-slate-300 hover:bg-[#161F30]'
              }`}
            >
              <span className="text-slate-500 w-16">{log.timestampMs.toFixed(1)}ms</span>
              <span className="text-blue-400 font-semibold w-36 truncate">{log.type}</span>
              <span className="text-amber-400 w-24 truncate">{log.requestId || 'system'}</span>
              <span className="text-slate-400 w-28 truncate">{log.nodeId}</span>
              <span className="text-slate-200 flex-1 truncate">{log.description}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
