import { useState } from 'react';
import type { CheckStatus } from '../types/checkStatus';
import { checkStatusStyle } from '../utils/checkStatus';

interface CheckStatusCardProps {
  title: string;
  status: CheckStatus;
  summary: string;
  detail?: string | null;
  howToFix?: string | null;
  featured?: boolean;
}

export function CheckStatusCard({ title, status, summary, detail, howToFix, featured }: CheckStatusCardProps) {
  const [expanded, setExpanded] = useState(false);
  const style = checkStatusStyle(status);

  return (
    <div className={`border bg-cyber-panel p-5 ${style.border} ${featured ? style.glow : ''}`}>
      <div className="flex items-start gap-3">
        <span
          className={`flex h-7 w-7 flex-shrink-0 items-center justify-center border font-mono text-sm font-bold ${style.border} ${style.bg} ${style.text}`}
          aria-hidden
        >
          {style.icon}
        </span>
        <div className="flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <p className={`font-mono font-semibold uppercase tracking-wide ${featured ? 'text-lg' : 'text-base'} text-cyber-text`}>
              {title}
            </p>
            <span className={`border px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide ${style.border} ${style.bg} ${style.text}`}>
              {style.label}
            </span>
          </div>
          <p className="mt-1 text-sm text-cyber-textDim">{summary}</p>
          {detail && <p className="mt-1 font-mono text-xs text-cyber-textDim">{detail}</p>}

          {howToFix && (
            <div className="mt-3">
              <button
                onClick={() => setExpanded((v) => !v)}
                className={`font-mono text-xs font-medium uppercase tracking-wide underline underline-offset-2 ${style.text}`}
              >
                {expanded ? 'Ocultar como solucionarlo' : 'Como solucionarlo'}
              </button>
              {expanded && (
                <p className="mt-2 border-l-2 border-cyber-border pl-3 text-sm text-cyber-textDim">{howToFix}</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
