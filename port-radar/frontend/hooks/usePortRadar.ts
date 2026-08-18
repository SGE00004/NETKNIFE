import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchLastPortScan, runPortScan } from '../api';

const LAST_REPORT_KEY = ['port-radar', 'last-report'];

export function useLastPortScan() {
  return useQuery({
    queryKey: LAST_REPORT_KEY,
    queryFn: fetchLastPortScan,
  });
}

export function useRunPortScan() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: runPortScan,
    onSuccess: (report) => {
      queryClient.setQueryData(LAST_REPORT_KEY, report);
    },
  });
}
