import { useState } from 'react';
import { checkStatusStyle } from '../../../shared/utils/checkStatus';
import type { HygieneItem } from '../types';

interface HygieneItemRowProps {
  item: HygieneItem;
  onAnswer: (itemId: string, status: 'OK' | 'PELIGRO') => void;
  isUpdating: boolean;
}

function formatLastUpdated(iso: string): string {
  return new Date(iso).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function HygieneItemRow({ item, onAnswer, isUpdating }: HygieneItemRowProps) {
  const [expanded, setExpanded] = useState(false);
  const style = checkStatusStyle(item.status);

  return (
    <div className={`border bg-cyber-panel p-4 ${style.border}`}>
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <span
            className={`flex h-6 w-6 flex-shrink-0 items-center justify-center border font-mono text-xs font-bold ${style.border} ${style.bg} ${style.text}`}
            aria-hidden
          >
            {style.icon}
          </span>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <p className="font-mono font-semibold text-cyber-text">{item.title}</p>
              <span className="border border-cyber-border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide text-cyber-textDim">
                {item.automatic ? 'Automatico' : 'Manual'}
              </span>
              <span className={`border px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide ${style.border} ${style.bg} ${style.text}`}>
                {style.label}
              </span>
            </div>
            <p className="mt-1 text-sm text-cyber-textDim">{item.whyItMatters}</p>
            {item.lastUpdated && (
              <p className="mt-1 font-mono text-xs text-cyber-textDim">
                Respondido el {formatLastUpdated(item.lastUpdated)}
              </p>
            )}
          </div>
        </div>

        {!item.automatic && (
          <div className="flex gap-2">
            <button
              onClick={() => onAnswer(item.id, 'OK')}
              disabled={isUpdating}
              className="border border-risk-green/60 bg-risk-green/10 px-3 py-1.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-green transition hover:bg-risk-green hover:text-black disabled:opacity-50"
            >
              Si
            </button>
            <button
              onClick={() => onAnswer(item.id, 'PELIGRO')}
              disabled={isUpdating}
              className="border border-risk-red/60 bg-risk-red/10 px-3 py-1.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:opacity-50"
            >
              No
            </button>
          </div>
        )}
      </div>

      {item.howToFix && (
        <div className="mt-3 pl-9">
          <button
            onClick={() => setExpanded((v) => !v)}
            className={`font-mono text-xs font-medium uppercase tracking-wide underline underline-offset-2 ${style.text}`}
          >
            {expanded ? 'Ocultar como solucionarlo' : 'Como solucionarlo'}
          </button>
          {expanded && (
            <p className="mt-2 border-l-2 border-cyber-border pl-3 text-sm text-cyber-textDim">{item.howToFix}</p>
          )}
        </div>
      )}
    </div>
  );
}
