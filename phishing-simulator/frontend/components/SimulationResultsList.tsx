import type { SimulationResult } from '../types';

interface SimulationResultsListProps {
  results: SimulationResult[];
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function SimulationResultsList({ results }: SimulationResultsListProps) {
  if (results.length === 0) {
    return (
      <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
        Todavia no has enviado ninguna simulacion.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      {results.map((result) => (
        <div
          key={result.id}
          className={`flex flex-col gap-1 border bg-cyber-panel p-3 sm:flex-row sm:items-center sm:justify-between ${
            result.clicked ? 'border-risk-red/40' : 'border-cyber-border'
          }`}
        >
          <div>
            <p className="font-mono text-sm text-cyber-text">{result.recipientEmail}</p>
            <p className="text-xs text-cyber-textDim">
              {result.templateName} · Enviado {formatDate(result.sentAt)}
            </p>
          </div>
          <span
            className={`self-start border px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide sm:self-auto ${
              result.clicked
                ? 'border-risk-red/60 bg-risk-red/10 text-risk-red'
                : 'border-cyber-border text-cyber-textDim'
            }`}
          >
            {result.clicked ? `Hizo clic · ${formatDate(result.clickedAt!)}` : 'Pendiente'}
          </span>
        </div>
      ))}
    </div>
  );
}
