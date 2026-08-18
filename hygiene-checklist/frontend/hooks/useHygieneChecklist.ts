import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchHygieneChecklist, runHygieneCheck, updateHygieneItem } from '../api';
import type { UpdateHygieneItemRequest } from '../types';

const CHECKLIST_KEY = ['hygiene-checklist'];

export function useHygieneChecklist() {
  return useQuery({
    queryKey: CHECKLIST_KEY,
    queryFn: fetchHygieneChecklist,
  });
}

export function useRunHygieneCheck() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: runHygieneCheck,
    onSuccess: (checklist) => {
      queryClient.setQueryData(CHECKLIST_KEY, checklist);
    },
  });
}

export function useUpdateHygieneItem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ itemId, request }: { itemId: string; request: UpdateHygieneItemRequest }) =>
      updateHygieneItem(itemId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CHECKLIST_KEY });
    },
  });
}
