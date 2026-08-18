import type { CheckStatus } from '../../shared/types/checkStatus';

export interface PortFinding {
  port: number;
  protocolLabel: string;
  banner: string | null;
  status: CheckStatus;
  summary: string;
  howToFix: string | null;
}

export interface PortScanReport {
  id: number;
  target: string;
  scannedAt: string;
  portsScanned: number;
  openPortsCount: number;
  overallStatus: CheckStatus;
  openPorts: PortFinding[];
}
