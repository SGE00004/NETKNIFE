import { httpClient } from '../../shared/api/httpClient';
import type { DomainFootprintReport, FileMetadataReport } from './types';

export async function analyzeFile(file: File): Promise<FileMetadataReport> {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await httpClient.post<FileMetadataReport>('/digital-footprint/analyze-file', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function fetchLastFileReport(): Promise<FileMetadataReport | null> {
  const response = await httpClient.get<FileMetadataReport>('/digital-footprint/last-file-report', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  return response.status === 204 ? null : response.data;
}

export async function analyzeDomain(domain: string): Promise<DomainFootprintReport> {
  const { data } = await httpClient.post<DomainFootprintReport>('/digital-footprint/analyze-domain', { domain });
  return data;
}

export async function fetchLastDomainReport(): Promise<DomainFootprintReport | null> {
  const response = await httpClient.get<DomainFootprintReport>('/digital-footprint/last-domain-report', {
    validateStatus: (status) => status === 200 || status === 204,
  });
  return response.status === 204 ? null : response.data;
}
