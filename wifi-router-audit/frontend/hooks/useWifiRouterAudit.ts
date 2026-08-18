import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchLastAudit, runAudit } from '../api';

const LAST_REPORT_KEY = ['wifi-router-audit', 'last-report'];

export function useLastAudit() {
  return useQuery({
    queryKey: LAST_REPORT_KEY,
    queryFn: fetchLastAudit,
  });
}

export function useRunAudit() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: runAudit,
    onSuccess: (report) => {
      queryClient.setQueryData(LAST_REPORT_KEY, report);
    },
  });
}
