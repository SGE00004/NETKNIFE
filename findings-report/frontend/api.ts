import { httpClient } from '../../shared/api/httpClient';
import type { FindingsReport } from './types';

export async function fetchFindingsReport(): Promise<FindingsReport> {
  const { data } = await httpClient.get<FindingsReport>('/findings-report');
  return data;
}
