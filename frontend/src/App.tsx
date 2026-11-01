import { Header } from './components/layout/Header';
import { ComponentPalette } from './components/canvas/ComponentPalette';
import { ArchitectureCanvas } from './components/canvas/ArchitectureCanvas';
import { PropertyInspector } from './components/canvas/PropertyInspector';
import { SimulationControlBar } from './components/canvas/SimulationControlBar';
import { AnalyticsDashboard } from './components/analytics/AnalyticsDashboard';
import { ArchitectureComparisonModal } from './components/comparison/ArchitectureComparisonModal';
import { useSimulationStore } from './store/useSimulationStore';

export function App() {
  const activeTab = useSimulationStore((state) => state.activeTab);

  return (
    <div className="flex flex-col h-screen w-screen overflow-hidden bg-[#0B0F17] text-slate-100 font-sans select-none">
      {/* Top Header */}
      <Header />

      {/* Main Workspace Body */}
      <main className="flex-1 flex overflow-hidden relative">
        {activeTab === 'canvas' ? (
          <>
            {/* Left Sidebar: Component Palette */}
            <ComponentPalette />

            {/* Central Visual Canvas */}
            <ArchitectureCanvas />

            {/* Right Sidebar: Property Inspector */}
            <PropertyInspector />
          </>
        ) : (
          /* Analytics & Diagnostics Dashboard */
          <AnalyticsDashboard />
        )}
      </main>

      {/* Bottom Control Bar */}
      <SimulationControlBar />

      {/* Comparison Modal */}
      <ArchitectureComparisonModal />
    </div>
  );
}

export default App;
