import { httpClient } from '../../shared/api/httpClient';
import type { HygieneChecklist, HygieneItem, UpdateHygieneItemRequest } from './types';

export async function fetchHygieneChecklist(): Promise<HygieneChecklist> {
  const { data } = await httpClient.get<HygieneChecklist>('/hygiene/checklist');
  return data;
}

export async function runHygieneCheck(): Promise<HygieneChecklist> {
  const { data } = await httpClient.post<HygieneChecklist>('/hygiene/check');
  return data;
}

export async function updateHygieneItem(itemId: string, request: UpdateHygieneItemRequest): Promise<HygieneItem> {
  const { data } = await httpClient.patch<HygieneItem>(`/hygiene/checklist/${itemId}`, request);
  return data;
}
