import type { CheckStatus } from '../../shared/types/checkStatus';

export interface CategoryResult {
  status: CheckStatus;
  summary: string;
  detail: string | null;
  howToFix: string | null;
}

export interface WifiRouterAuditReport {
  id: number;
  checkedAt: string;
  routerAddress: string | null;
  wifiEncryption: CategoryResult;
  defaultCredentials: CategoryResult;
  wps: CategoryResult;
  overallStatus: CheckStatus;
}
