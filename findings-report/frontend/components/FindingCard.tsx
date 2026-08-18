import { RiskSemaphore } from '../../../shared/components/RiskSemaphore';
import type { ToolId } from '../../../shared/types/toolId';
import type { Finding } from '../types';

interface FindingCardProps {
  finding: Finding;
  onNavigateToTool: (toolId: ToolId) => void;
}

export function FindingCard({ finding, onNavigateToTool }: FindingCardProps) {
  return (
    <div className="flex flex-col gap-3">
      <RiskSemaphore level={finding.riskLevel} title={finding.title} description={finding.summary} />
      <div className="flex flex-wrap items-center justify-between gap-2 border-t border-cyber-border pt-3">
        <span className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
          Origen: {finding.sourceModuleLabel}
        </span>
        <button
          onClick={() => onNavigateToTool(finding.relatedTool.toolId)}
          className="font-mono text-xs font-medium uppercase tracking-wide text-cyber-yellow underline underline-offset-2"
        >
          {finding.relatedTool.label}
        </button>
      </div>
    </div>
  );
}
