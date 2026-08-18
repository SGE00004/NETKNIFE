import { httpClient } from '../../shared/api/httpClient';
import type { CryptojackingAlert, CryptojackingStatus, KillProcessResult } from './types';

export async function fetchStatus(): Promise<CryptojackingStatus> {
  const { data } = await httpClient.get<CryptojackingStatus>('/cryptojacking-detector/status');
  return data;
}

export async function fetchHistory(limit = 50): Promise<CryptojackingAlert[]> {
  const { data } = await httpClient.get<CryptojackingAlert[]>('/cryptojacking-detector/history', {
    params: { limit },
  });
  return data;
}

export async function killProcess(pid: number): Promise<KillProcessResult> {
  const { data } = await httpClient.post<KillProcessResult>(`/cryptojacking-detector/processes/${pid}/kill`);
  return data;
}
