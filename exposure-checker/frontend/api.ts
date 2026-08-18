import { httpClient } from '../../shared/api/httpClient';
import type { ExposureReport } from './types';

export async function runExposureCheck(): Promise<ExposureReport> {
  const { data } = await httpClient.post<ExposureReport>('/exposure/check');
  return data;
}

export async function fetchLastExposureReport(): Promise<ExposureReport | null> {
  const response = await httpClient.get<ExposureReport>('/exposure/last-report', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  return response.status === 204 ? null : response.data;
}
