import type { ToolId } from '../types/toolId';
import type { ThemeCategory } from '../theme/ThemeContext';

export const TOOLS: { id: ToolId; label: string; category: ThemeCategory }[] = [
  { id: 'network-scanner', label: 'Escaner de Red', category: 'yellow' },
  { id: 'exposure-checker', label: 'Exposicion', category: 'yellow' },
  { id: 'digital-footprint', label: 'Huella Digital', category: 'red' },
  { id: 'port-radar', label: 'Radar de Puertos', category: 'red' },
  { id: 'wifi-router-audit', label: 'Auditor Wi-Fi/Router', category: 'red' },
  { id: 'findings-report', label: 'Informe de Hallazgos', category: 'red' },
  { id: 'hygiene-checklist', label: 'Higiene', category: 'blue' },
  { id: 'incident-guide', label: 'Incidentes', category: 'blue' },
  { id: 'traffic-analyzer', label: 'Analizador de Trafico', category: 'blue' },
  { id: 'cryptojacking-detector', label: 'Cryptojacking', category: 'blue' },
  { id: 'phishing-simulator', label: 'Phishing', category: 'yellow' },
];
