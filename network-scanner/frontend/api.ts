import { httpClient } from '../../shared/api/httpClient';
import type { BlockingCapability, DeviceSummary, NetworkDevice, ScanResult, UpdateDeviceRequest } from './types';

export async function triggerScan(): Promise<ScanResult> {
  const { data } = await httpClient.post<ScanResult>('/network/scan');
  return data;
}

export async function fetchDevices(): Promise<NetworkDevice[]> {
  const { data } = await httpClient.get<NetworkDevice[]>('/network/devices');
  return data;
}

export async function fetchSummary(): Promise<DeviceSummary> {
  const { data } = await httpClient.get<DeviceSummary>('/network/summary');
  return data;
}

export async function updateDevice(id: number, request: UpdateDeviceRequest): Promise<NetworkDevice> {
  const { data } = await httpClient.patch<NetworkDevice>(`/network/devices/${id}`, request);
  return data;
}

export async function fetchBlockingCapability(): Promise<BlockingCapability> {
  const { data } = await httpClient.get<BlockingCapability>('/network/blocking/capability');
  return data;
}

export async function blockDevice(id: number): Promise<NetworkDevice> {
  const { data } = await httpClient.post<NetworkDevice>(`/network/devices/${id}/block`);
  return data;
}

export async function unblockDevice(id: number): Promise<NetworkDevice> {
  const { data } = await httpClient.post<NetworkDevice>(`/network/devices/${id}/unblock`);
  return data;
}
