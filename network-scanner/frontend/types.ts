import type { RiskLevel } from '../../shared/types/riskLevel';

export interface NetworkDevice {
  id: number;
  macAddress: string | null;
  ipAddress: string;
  hostname: string | null;
  vendor: string | null;
  customName: string | null;
  displayName: string;
  recognized: boolean;
  /** true solo si aun no se reconoce y se detecto por primera vez en el ultimo escaneo. */
  newSinceLastScan: boolean;
  blocked: boolean;
  blockedAt: string | null;
  firstSeen: string;
  lastSeen: string;
}

export type BlockingReasonCode =
  | 'NONE'
  | 'UNSUPPORTED_OS'
  | 'NPCAP_NOT_INSTALLED'
  | 'MISSING_ADMIN_PRIVILEGES'
  | 'INITIALIZATION_ERROR';

export interface BlockingCapability {
  available: boolean;
  reasonCode: BlockingReasonCode;
  message: string | null;
}

export interface ScanResult {
  scannedAt: string;
  subnetScanned: string;
  networkInterfaceName: string | null;
  totalDevices: number;
  unrecognizedDevices: number;
  newDevicesThisScan: number;
  riskLevel: RiskLevel;
  devices: NetworkDevice[];
}

export interface DeviceSummary {
  totalDevices: number;
  unrecognizedDevices: number;
  newSinceLastScan: number;
  riskLevel: RiskLevel;
}

export interface UpdateDeviceRequest {
  recognized?: boolean;
  customName?: string;
}
