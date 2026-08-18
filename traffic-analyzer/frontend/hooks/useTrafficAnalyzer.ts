import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchCapability, fetchCaptureStatus, fetchConnections, startCapture, stopCapture } from '../api';

const CAPABILITY_KEY = ['traffic-analyzer', 'capability'];
const STATUS_KEY = ['traffic-analyzer', 'status'];
const CONNECTIONS_KEY = ['traffic-analyzer', 'connections'];

export function useCapability() {
  return useQuery({
    queryKey: CAPABILITY_KEY,
    queryFn: fetchCapability,
    staleTime: 5 * 60 * 1000,
  });
}

export function useCaptureStatus() {
  return useQuery({
    queryKey: STATUS_KEY,
    queryFn: fetchCaptureStatus,
    refetchInterval: 5_000,
  });
}

export function useConnections(enabled: boolean) {
  return useQuery({
    queryKey: CONNECTIONS_KEY,
    queryFn: fetchConnections,
    refetchInterval: 3_000,
    enabled,
  });
}

export function useStartCapture() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: startCapture,
    onSuccess: (status) => {
      queryClient.setQueryData(STATUS_KEY, status);
      queryClient.invalidateQueries({ queryKey: CONNECTIONS_KEY });
    },
  });
}

export function useStopCapture() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: stopCapture,
    onSuccess: (status) => {
      queryClient.setQueryData(STATUS_KEY, status);
      queryClient.setQueryData(CONNECTIONS_KEY, []);
    },
  });
}
