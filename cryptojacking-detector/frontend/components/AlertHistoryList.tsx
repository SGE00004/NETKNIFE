import type { AlertResolution, CryptojackingAlert } from '../types';
import { formatRelativeTime } from '../utils';

interface AlertHistoryListProps {
  alerts: CryptojackingAlert[];
}

function resolutionLabel(resolution: AlertResolution | null): string {
  switch (resolution) {
    case 'PROCESS_ENDED_BY_USER':
      return 'Finalizado manualmente';
    case 'PROCESS_EXITED_ON_ITS_OWN':
      return 'Termino por su cuenta';
    case 'CPU_DROPPED':
      return 'La CPU volvio a la normalidad';
    default:
      return 'Activa';
  }
}

export function AlertHistoryList({ alerts }: AlertHistoryListProps) {
  const resolved = alerts.filter((alert) => alert.resolvedAt);

  if (resolved.length === 0) {
    return (
      <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
        Todavia no hay alertas anteriores.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      {resolved.map((alert) => (
        <div key={alert.id} className="flex flex-wrap items-center justify-between gap-2 border border-cyber-border bg-cyber-panel px-4 py-3">
          <div>
            <p className="font-mono text-sm text-cyber-text">{alert.processName}</p>
            <p className="font-mono text-xs text-cyber-textDim">
              PID {alert.pid} · pico {Math.round(alert.peakCpuPercent)}% CPU · detectado {formatRelativeTime(alert.detectedAt)}
            </p>
          </div>
          <span className="border border-cyber-border px-2 py-0.5 font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            {resolutionLabel(alert.resolution)}
          </span>
        </div>
      ))}
    </div>
  );
}
