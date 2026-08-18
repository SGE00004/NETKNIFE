import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchHistory, fetchStatus, killProcess } from '../api';

const STATUS_KEY = ['cryptojacking-detector', 'status'];
const HISTORY_KEY = ['cryptojacking-detector', 'history'];

export function useCryptojackingStatus() {
  return useQuery({
    queryKey: STATUS_KEY,
    queryFn: fetchStatus,
    refetchInterval: 4_000,
  });
}

export function useCryptojackingHistory() {
  return useQuery({
    queryKey: HISTORY_KEY,
    queryFn: () => fetchHistory(),
  });
}

export function useKillProcess() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: killProcess,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: STATUS_KEY });
      queryClient.invalidateQueries({ queryKey: HISTORY_KEY });
    },
  });
}
