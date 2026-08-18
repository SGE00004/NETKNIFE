import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { analyzeFile, fetchLastFileReport } from '../api';

const LAST_REPORT_KEY = ['digital-footprint', 'last-file-report'];

export function useLastFileReport() {
  return useQuery({
    queryKey: LAST_REPORT_KEY,
    queryFn: fetchLastFileReport,
  });
}

export function useAnalyzeFile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: analyzeFile,
    onSuccess: (report) => {
      queryClient.setQueryData(LAST_REPORT_KEY, report);
    },
  });
}
