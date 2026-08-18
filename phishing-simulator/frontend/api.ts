import { httpClient } from '../../shared/api/httpClient';
import type { PhishingTemplate, SendSimulationRequest, SimulationResult } from './types';

export async function fetchPhishingTemplates(): Promise<PhishingTemplate[]> {
  const { data } = await httpClient.get<PhishingTemplate[]>('/phishing/templates');
  return data;
}

export async function sendPhishingSimulation(request: SendSimulationRequest): Promise<SimulationResult[]> {
  const { data } = await httpClient.post<SimulationResult[]>('/phishing/send', request);
  return data;
}

export async function fetchPhishingResults(): Promise<SimulationResult[]> {
  const { data } = await httpClient.get<SimulationResult[]>('/phishing/results');
  return data;
}
