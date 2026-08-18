import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchLastExposureReport, runExposureCheck } from '../api';

const LAST_REPORT_KEY = ['exposure-checker', 'last-report'];

export function useLastExposureReport() {
  return useQuery({
    queryKey: LAST_REPORT_KEY,
    queryFn: fetchLastExposureReport,
  });
}

export function useRunExposureCheck() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: runExposureCheck,
    onSuccess: (report) => {
      queryClient.setQueryData(LAST_REPORT_KEY, report);
    },
  });
}
