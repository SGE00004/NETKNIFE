import type { CryptojackingAlert } from '../types';

interface KillProcessConfirmDialogProps {
  alert: CryptojackingAlert;
  onCancel: () => void;
  onConfirm: () => void;
  isKilling: boolean;
}

export function KillProcessConfirmDialog({ alert, onCancel, onConfirm, isKilling }: KillProcessConfirmDialogProps) {
  return (
    <div className="fixed inset-0 z-20 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm" onClick={onCancel}>
      <div
        className="w-full max-w-md border border-risk-red/50 bg-cyber-panel p-6 shadow-glow-red"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="font-mono text-xl font-bold uppercase tracking-wide text-risk-red">
          Vas a finalizar {alert.processName}
        </h2>

        <p className="mt-3 text-sm text-cyber-textDim">
          NETKNIFE va a pedirle al sistema operativo que cierre este proceso (PID {alert.pid}) de inmediato.
          Cualquier trabajo sin guardar en ese programa se perdera.
        </p>
        <p className="mt-3 text-sm text-cyber-textDim">
          Solo puedes finalizar procesos que el propio detector ha marcado como sospechosos activamente.
        </p>

        <div className="mt-5 flex gap-3">
          <button
            onClick={onCancel}
            autoFocus
            disabled={isKilling}
            className="flex-1 border border-cyber-border px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim transition hover:border-cyber-yellow hover:text-cyber-yellow disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            disabled={isKilling}
            className="flex-1 border border-risk-red bg-risk-red/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:opacity-50"
          >
            {isKilling ? 'Finalizando…' : 'Si, finalizar proceso'}
          </button>
        </div>
      </div>
    </div>
  );
}
