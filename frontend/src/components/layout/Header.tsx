import React, { useState, useEffect } from 'react';
import {
  Cpu,
  Save,
  Download,
  Upload,
  Sparkles,
  Check
} from 'lucide-react';
import { useSimulationStore } from '../../store/useSimulationStore';
import { api } from '../../services/api';
import type { ArchitectureTemplate } from '../../types/simulation';

export const Header: React.FC = () => {
  const {
    architectureTitle,
    setArchitectureTitle,
    architectureDescription,
    nodes,
    edges,
    durationSec,
    randomSeed,
    failures,
    loadTemplate,
  } = useSimulationStore();

  const [templates, setTemplates] = useState<ArchitectureTemplate[]>([]);
  const [isSaved, setIsSaved] = useState(false);
  const [isBackendConnected, setIsBackendConnected] = useState<boolean | null>(null);

  useEffect(() => {
    api.getTemplates()
      .then((data) => {
        setTemplates(data);
        setIsBackendConnected(true);
      })
      .catch((err) => {
        console.warn('Backend not yet reachable:', err);
        setIsBackendConnected(false);
      });
  }, []);

  const handleSave = async () => {
    try {
      const canvasDataJson = JSON.stringify({ nodes, edges });
      const scenarioDataJson = JSON.stringify({ durationSec, randomSeed, failures });

      await api.saveArchitecture({
        name: architectureTitle,
        description: architectureDescription,
        isTemplate: false,
        canvasDataJson,
        scenarioDataJson,
      });

      setIsSaved(true);
      setTimeout(() => setIsSaved(false), 2500);
    } catch (err: any) {
      alert(`Failed to save architecture: ${err.message}`);
    }
  };

  const handleExportJson = () => {
    const data = {
      title: architectureTitle,
      description: architectureDescription,
      nodes,
      edges,
      scenario: { durationSec, randomSeed, failures },
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${architectureTitle.toLowerCase().replace(/\s+/g, '-')}.simulab.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportJson = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const parsed = JSON.parse(event.target?.result as string);
        if (parsed.nodes && parsed.edges) {
          loadTemplate({
            name: parsed.title || 'Imported Architecture',
            description: parsed.description || '',
            canvasDataJson: JSON.stringify({ nodes: parsed.nodes, edges: parsed.edges }),
            scenarioDataJson: JSON.stringify(parsed.scenario || {}),
          });
        }
      } catch {
        alert('Invalid SimuLab JSON file format');
      }
    };
    reader.readAsText(file);
  };

  return (
    <header className="h-14 bg-[#0B0F17] border-b border-[#1F2937] px-4 flex items-center justify-between select-none z-30">
      {/* Brand & Title */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Cpu className="w-4 h-4 text-white" />
          </div>
          <span className="font-bold text-sm tracking-wide bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
            SimuLab
          </span>
        </div>

        <div className="h-4 w-[1px] bg-[#1F2937]" />

        {/* Title Input */}
        <input
          type="text"
          value={architectureTitle}
          onChange={(e) => setArchitectureTitle(e.target.value)}
          className="bg-transparent text-xs font-semibold text-slate-200 hover:bg-[#161F30] focus:bg-[#161F30] px-2 py-1 rounded border border-transparent focus:border-[#2D3A52] focus:outline-none transition-colors w-64 truncate"
          title="Click to rename architecture"
        />
      </div>

      {/* Templates & Quick Actions */}
      <div className="flex items-center gap-2 text-xs">
        {/* Backend Status indicator */}
        <div className="flex items-center gap-1.5 px-2 py-1 rounded bg-[#131B2E] border border-[#1F2937] text-[11px] text-slate-400">
          <span
            className={`w-2 h-2 rounded-full ${
              isBackendConnected ? 'bg-emerald-400 shadow-[0_0_8px_rgba(52,211,153,0.8)]' : 'bg-amber-400'
            }`}
          />
          <span>{isBackendConnected ? 'Engine Online' : 'Engine Connecting'}</span>
        </div>

        {/* Templates Dropdown */}
        <div className="relative group">
          <button className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#131B2E] border border-[#1F2937] hover:border-slate-600 text-slate-200 font-medium transition-colors">
            <Sparkles className="w-3.5 h-3.5 text-amber-400" />
            Templates
          </button>
          <div className="absolute right-0 top-full mt-1 w-64 bg-[#111827] border border-[#2D3A52] rounded-xl shadow-2xl py-1.5 opacity-0 pointer-events-none group-hover:opacity-100 group-hover:pointer-events-auto transition-all z-50">
            <div className="px-3 py-1 text-[10px] font-mono uppercase tracking-wider text-slate-500">
              Pre-Seeded Architectures
            </div>
            {templates.map((tpl) => (
              <button
                key={tpl.id || tpl.name}
                onClick={() => loadTemplate(tpl)}
                className="w-full text-left px-3 py-2 hover:bg-[#1A243D] text-slate-200 text-xs transition-colors flex flex-col"
              >
                <span className="font-semibold">{tpl.name}</span>
                <span className="text-[10px] text-slate-400 truncate">{tpl.description}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Save Architecture */}
        <button
          onClick={handleSave}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#131B2E] border border-[#1F2937] hover:border-slate-600 text-slate-200 font-medium transition-colors"
        >
          {isSaved ? (
            <>
              <Check className="w-3.5 h-3.5 text-emerald-400" />
              <span className="text-emerald-400">Saved</span>
            </>
          ) : (
            <>
              <Save className="w-3.5 h-3.5 text-blue-400" />
              Save
            </>
          )}
        </button>

        {/* Export JSON */}
        <button
          onClick={handleExportJson}
          className="p-1.5 rounded-lg bg-[#131B2E] border border-[#1F2937] hover:bg-slate-800 text-slate-300 hover:text-white transition-colors"
          title="Export Architecture JSON"
        >
          <Download className="w-3.5 h-3.5" />
        </button>

        {/* Import JSON */}
        <label
          className="p-1.5 rounded-lg bg-[#131B2E] border border-[#1F2937] hover:bg-slate-800 text-slate-300 hover:text-white transition-colors cursor-pointer"
          title="Import Architecture JSON"
        >
          <Upload className="w-3.5 h-3.5" />
          <input type="file" accept=".json" onChange={handleImportJson} className="hidden" />
        </label>
      </div>
    </header>
  );
};
