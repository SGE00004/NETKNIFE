import type { ActiveConnection } from '../types';
import { remoteLabel } from '../utils';

interface ConnectionCardProps {
  connection: ActiveConnection;
  onSelect: (connection: ActiveConnection) => void;
}

export function ConnectionCard({ connection, onSelect }: ConnectionCardProps) {
  const borderStyle = connection.encrypted ? 'border-cyber-border' : 'border-risk-red/50 shadow-glow-red';

  return (
    <button
      onClick={() => onSelect(connection)}
      className={`flex w-full flex-col gap-1 border bg-cyber-panel p-4 text-left sm:flex-row sm:items-center sm:justify-between ${borderStyle}`}
    >
      <div>
        <div className="flex flex-wrap items-center gap-2">
          <p className="font-mono font-semibold text-cyber-text">
            Tu ordenador esta hablando con {remoteLabel(connection)}
            {connection.country ? ` (${connection.country})` : ''}
          </p>
          {connection.isNew && (
            <span className="border border-risk-yellow/60 bg-risk-yellow/10 px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-yellow">
              Nueva
            </span>
          )}
        </div>
        <p className="text-sm text-cyber-textDim">
          {connection.processName ?? 'Proceso desconocido'} · {connection.protocol}:{connection.remotePort ?? '?'}
        </p>
      </div>
      <span
        className={`whitespace-nowrap border px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide ${
          connection.encrypted
            ? 'border-risk-green/40 bg-risk-green/10 text-risk-green'
            : 'border-risk-red/40 bg-risk-red/10 text-risk-red'
        }`}
      >
        {connection.encrypted ? 'Cifrado' : 'Sin cifrar'}
      </span>
    </button>
  );
}
