import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { analyzeDomain, fetchLastDomainReport } from '../api';

const LAST_REPORT_KEY = ['digital-footprint', 'last-domain-report'];

export function useLastDomainReport() {
  return useQuery({
    queryKey: LAST_REPORT_KEY,
    queryFn: fetchLastDomainReport,
  });
}

export function useAnalyzeDomain() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: analyzeDomain,
    onSuccess: (report) => {
      queryClient.setQueryData(LAST_REPORT_KEY, report);
    },
  });
}
