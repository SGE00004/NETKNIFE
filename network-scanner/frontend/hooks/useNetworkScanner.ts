import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  blockDevice,
  fetchBlockingCapability,
  fetchDevices,
  fetchSummary,
  triggerScan,
  unblockDevice,
  updateDevice,
} from '../api';
import type { UpdateDeviceRequest } from '../types';

const DEVICES_KEY = ['network-scanner', 'devices'];
const SUMMARY_KEY = ['network-scanner', 'summary'];
const CAPABILITY_KEY = ['network-scanner', 'blocking-capability'];

export function useDevices() {
  return useQuery({
    queryKey: DEVICES_KEY,
    queryFn: fetchDevices,
  });
}

export function useSummary() {
  return useQuery({
    queryKey: SUMMARY_KEY,
    queryFn: fetchSummary,
  });
}

export function useScanNetwork() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: triggerScan,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: DEVICES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}

export function useUpdateDevice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateDeviceRequest }) => updateDevice(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: DEVICES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}

export function useBlockingCapability() {
  return useQuery({
    queryKey: CAPABILITY_KEY,
    queryFn: fetchBlockingCapability,
    staleTime: 5 * 60 * 1000,
  });
}

export function useBlockDevice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => blockDevice(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: DEVICES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}

export function useUnblockDevice() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => unblockDevice(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: DEVICES_KEY });
      queryClient.invalidateQueries({ queryKey: SUMMARY_KEY });
    },
  });
}
