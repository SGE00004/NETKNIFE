import { httpClient } from '../../shared/api/httpClient';
import type { WifiRouterAuditReport } from './types';

export async function runAudit(routerAddress: string): Promise<WifiRouterAuditReport> {
  const { data } = await httpClient.post<WifiRouterAuditReport>('/wifi-router-audit/check', {
    routerAddress: routerAddress.trim() === '' ? null : routerAddress.trim(),
  });
  return data;
}

export async function fetchLastAudit(): Promise<WifiRouterAuditReport | null> {
  const response = await httpClient.get<WifiRouterAuditReport>('/wifi-router-audit/last-report', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  return response.status === 204 ? null : response.data;
}
