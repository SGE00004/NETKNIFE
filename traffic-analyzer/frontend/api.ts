import { httpClient } from '../../shared/api/httpClient';
import type { ActiveConnection, CaptureStatus, TrafficCaptureCapability } from './types';

export async function fetchCapability(): Promise<TrafficCaptureCapability> {
  const { data } = await httpClient.get<TrafficCaptureCapability>('/traffic-analyzer/capability');
  return data;
}

export async function fetchCaptureStatus(): Promise<CaptureStatus> {
  const { data } = await httpClient.get<CaptureStatus>('/traffic-analyzer/capture/status');
  return data;
}

export async function startCapture(): Promise<CaptureStatus> {
  const { data } = await httpClient.post<CaptureStatus>('/traffic-analyzer/capture/start');
  return data;
}

export async function stopCapture(): Promise<CaptureStatus> {
  const { data } = await httpClient.post<CaptureStatus>('/traffic-analyzer/capture/stop');
  return data;
}

export async function fetchConnections(): Promise<ActiveConnection[]> {
  const { data } = await httpClient.get<ActiveConnection[]>('/traffic-analyzer/connections');
  return data;
}
