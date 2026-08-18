import { httpClient } from '../../shared/api/httpClient';
import type { PortScanReport } from './types';

export async function runPortScan(target: string): Promise<PortScanReport> {
  const { data } = await httpClient.post<PortScanReport>('/port-radar/scan', { target });
  return data;
}

export async function fetchLastPortScan(): Promise<PortScanReport | null> {
  const response = await httpClient.get<PortScanReport>('/port-radar/last-report', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  return response.status === 204 ? null : response.data;
}
