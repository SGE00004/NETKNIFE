import type { CheckStatus } from '../../shared/types/checkStatus';

export interface CategoryResult {
  status: CheckStatus;
  summary: string;
  detail: string | null;
  howToFix: string | null;
}

export interface ExposureReport {
  id: number;
  checkedAt: string;
  wifiEncryption: CategoryResult;
  openPorts: CategoryResult;
  services: CategoryResult;
  overallStatus: CheckStatus;
}
