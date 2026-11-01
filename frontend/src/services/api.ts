import type {
  ArchitectureTemplate,
  ComparisonResult,
  FailureConfig,
  SimEdgeData,
  SimNodeData,
  SimulationResult
} from '../types/simulation';

const API_BASE_URL = 'http://localhost:8080/api';

export interface ExecuteAdhocPayload {
  name: string;
  nodes: SimNodeData[];
  edges: SimEdgeData[];
  durationSec: number;
  randomSeed: number;
  failures: FailureConfig[];
}

export interface ComparePayload {
  architectureAName: string;
  architectureA: ExecuteAdhocPayload;
  architectureBName: string;
  architectureB: ExecuteAdhocPayload;
}

export const api = {
  async executeAdhoc(payload: ExecuteAdhocPayload): Promise<SimulationResult> {
    const res = await fetch(`${API_BASE_URL}/simulations/execute-adhoc`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      throw new Error(`Simulation failed: ${res.statusText}`);
    }
    return res.json();
  },

  async compare(payload: ComparePayload): Promise<ComparisonResult> {
    const res = await fetch(`${API_BASE_URL}/simulations/compare`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!res.ok) {
      throw new Error(`Comparison failed: ${res.statusText}`);
    }
    return res.json();
  },

  async getTemplates(): Promise<ArchitectureTemplate[]> {
    const res = await fetch(`${API_BASE_URL}/architectures/templates`);
    if (!res.ok) {
      throw new Error('Failed to load templates');
    }
    return res.json();
  },

  async getArchitectures(): Promise<ArchitectureTemplate[]> {
    const res = await fetch(`${API_BASE_URL}/architectures`);
    if (!res.ok) {
      throw new Error('Failed to load architectures');
    }
    return res.json();
  },

  async saveArchitecture(data: Partial<ArchitectureTemplate>): Promise<ArchitectureTemplate> {
    const res = await fetch(`${API_BASE_URL}/architectures`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      throw new Error('Failed to save architecture');
    }
    return res.json();
  },

  async updateArchitecture(id: number, data: Partial<ArchitectureTemplate>): Promise<ArchitectureTemplate> {
    const res = await fetch(`${API_BASE_URL}/architectures/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    if (!res.ok) {
      throw new Error('Failed to update architecture');
    }
    return res.json();
  },

  async deleteArchitecture(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/architectures/${id}`, {
      method: 'DELETE',
    });
    if (!res.ok) {
      throw new Error('Failed to delete architecture');
    }
  },

  async getRuns(): Promise<any[]> {
    const res = await fetch(`${API_BASE_URL}/simulations/runs`);
    if (!res.ok) {
      throw new Error('Failed to fetch runs');
    }
    return res.json();
  },
};
