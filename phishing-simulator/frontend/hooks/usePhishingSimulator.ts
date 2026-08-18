import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchPhishingResults, fetchPhishingTemplates, sendPhishingSimulation } from '../api';

const RESULTS_KEY = ['phishing-simulator', 'results'];

export function usePhishingTemplates() {
  return useQuery({
    queryKey: ['phishing-simulator', 'templates'],
    queryFn: fetchPhishingTemplates,
  });
}

export function usePhishingResults() {
  return useQuery({
    queryKey: RESULTS_KEY,
    queryFn: fetchPhishingResults,
    refetchInterval: 15_000,
  });
}

export function useSendPhishingSimulation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: sendPhishingSimulation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: RESULTS_KEY });
    },
  });
}
