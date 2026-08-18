import { useQuery } from '@tanstack/react-query';
import { fetchFindingsReport } from '../api';

export function useFindingsReport() {
  return useQuery({
    queryKey: ['findings-report'],
    queryFn: fetchFindingsReport,
  });
}
