import type { RiskLevel } from '../../shared/types/riskLevel';

export type SuspicionReason = 'KNOWN_MINER_NAME' | 'SUSTAINED_HIGH_CPU_NO_WINDOW' | 'SUSTAINED_HIGH_CPU';
export type AlertResolution = 'PROCESS_ENDED_BY_USER' | 'PROCESS_EXITED_ON_ITS_OWN' | 'CPU_DROPPED';

export interface CryptojackingAlert {
  id: number;
  pid: number;
  processName: string;
  processPath: string | null;
  reason: SuspicionReason;
  peakCpuPercent: number;
  detectedAt: string;
  resolvedAt: string | null;
  resolution: AlertResolution | null;
}

export interface CryptojackingStatus {
  overallStatus: RiskLevel;
  activeAlerts: CryptojackingAlert[];
}

export interface KillProcessResult {
  pid: number;
  success: boolean;
  message: string;
}
