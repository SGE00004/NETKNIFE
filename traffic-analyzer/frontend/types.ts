export interface ActiveConnection {
  remoteIp: string;
  remotePort: number | null;
  protocol: string;
  encrypted: boolean;
  processName: string | null;
  pid: number | null;
  country: string | null;
  city: string | null;
  isp: string | null;
  org: string | null;
  firstSeen: string;
  lastSeen: string;
  isNew: boolean;
}

export interface CaptureStatus {
  running: boolean;
  available: boolean;
  unavailableReasonCode: string;
  unavailableMessage: string | null;
}

export interface TrafficCaptureCapability {
  available: boolean;
  reasonCode: string;
  message: string | null;
}

export type ConnectionFilter = 'all' | 'unencrypted' | 'new';
