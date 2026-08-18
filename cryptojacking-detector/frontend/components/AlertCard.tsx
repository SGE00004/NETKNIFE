import type { CryptojackingAlert } from '../types';
import { formatRelativeTime } from '../utils';

interface AlertCardProps {
  alert: CryptojackingAlert;
  onKill: (alert: CryptojackingAlert) => void;
  isKilling: boolean;
}

function reasonExplanation(reason: CryptojackingAlert['reason']): string {
  switch (reason) {
    case 'KNOWN_MINER_NAME':
      return 'Este programa coincide con software de minado de criptomonedas conocido.';
    case 'SUSTAINED_HIGH_CPU_NO_WINDOW':
      return 'Lleva un rato usando mucha CPU sin tener ninguna ventana abierta: un patron tipico de minado oculto.';
    case 'SUSTAINED_HIGH_CPU':
      return 'Lleva un rato usando mucha CPU. Tiene una ventana abierta, asi que podria ser una app que estas usando activamente: revisalo antes de finalizarlo.';
    default:
      return 'Actividad de CPU sospechosa.';
  }
}

export function AlertCard({ alert, onKill, isKilling }: AlertCardProps) {
  return (
    <div className="flex flex-col gap-3 border border-risk-red/40 bg-cyber-panel p-5 shadow-glow-red">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <p className="font-mono text-lg font-semibold text-cyber-text">{alert.processName}</p>
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            PID {alert.pid} · {Math.round(alert.peakCpuPercent)}% de CPU · desde {formatRelativeTime(alert.detectedAt)}
          </p>
        </div>
        <button
          onClick={() => onKill(alert)}
          disabled={isKilling}
          className="border border-risk-red bg-risk-red/10 px-4 py-2 font-mono text-xs font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:cursor-not-allowed disabled:opacity-50"
        >
          Finalizar proceso
        </button>
      </div>
      <p className="text-sm text-cyber-textDim">{reasonExplanation(alert.reason)}</p>
      {alert.processPath && <p className="font-mono text-xs text-cyber-textDim">{alert.processPath}</p>}
    </div>
  );
}
