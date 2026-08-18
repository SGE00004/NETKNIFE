import type { CheckStatus } from '../../shared/types/checkStatus';

export interface HygieneItem {
  id: string;
  title: string;
  whyItMatters: string;
  automatic: boolean;
  status: CheckStatus;
  detail: string | null;
  howToFix: string | null;
  lastUpdated: string | null;
}

export interface HygieneChecklist {
  items: HygieneItem[];
  totalItems: number;
  itemsInGoodShape: number;
  overallStatus: CheckStatus;
}

export interface UpdateHygieneItemRequest {
  status: CheckStatus;
}
