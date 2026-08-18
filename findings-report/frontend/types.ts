import type { CheckStatus } from '../../shared/types/checkStatus';
import type { RiskLevel } from '../../shared/types/riskLevel';
import type { ToolId } from '../../shared/types/toolId';

export interface RelatedTool {
  toolId: ToolId;
  label: string;
}

export interface Finding {
  sourceModuleId: string;
  sourceModuleLabel: string;
  id: string;
  title: string;
  status: CheckStatus;
  riskLevel: RiskLevel;
  summary: string;
  detail: string | null;
  howToFix: string | null;
  detectedAt: string;
  relatedTool: RelatedTool;
}

export interface FindingsReport {
  generatedAt: string;
  totalFindings: number;
  lowRiskCount: number;
  mediumRiskCount: number;
  highRiskCount: number;
  overallRisk: RiskLevel;
  findings: Finding[];
  modulesWithoutData: { id: ToolId; label: string }[];
}
