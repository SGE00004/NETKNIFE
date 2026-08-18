import type { NetworkDevice } from '../types';

interface BlockConfirmDialogProps {
  device: NetworkDevice;
  onCancel: () => void;
  onConfirm: () => void;
  isBlocking: boolean;
}

export function BlockConfirmDialog({ device, onCancel, onConfirm, isBlocking }: BlockConfirmDialogProps) {
  return (
    <div className="fixed inset-0 z-20 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm" onClick={onCancel}>
      <div
        className="w-full max-w-md border border-risk-red/50 bg-cyber-panel p-6 shadow-glow-red"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="font-mono text-xl font-bold uppercase tracking-wide text-risk-red">
          Vas a cortar la conexion de {device.displayName}
        </h2>

        <p className="mt-3 text-sm text-cyber-textDim">
          NETKNIFE va a impedir que este dispositivo hable con tu router usando una tecnica llamada
          &quot;ARP spoofing&quot; (la misma que usan herramientas como NetCut). El dispositivo perdera
          internet y acceso a la red mientras el bloqueo este activo.
        </p>
        <p className="mt-3 text-sm text-cyber-textDim">
          Puedes revertirlo en cualquier momento pulsando &quot;Restaurar conexion&quot;. Si cierras
          NETKNIFE sin desbloquearlo, la app lo restaura automaticamente al cerrarse.
        </p>

        <div className="mt-5 flex gap-3">
          <button
            onClick={onCancel}
            autoFocus
            disabled={isBlocking}
            className="flex-1 border border-cyber-border px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim transition hover:border-cyber-yellow hover:text-cyber-yellow disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            disabled={isBlocking}
            className="flex-1 border border-risk-red bg-risk-red/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:opacity-50"
          >
            {isBlocking ? 'Cortando conexion…' : 'Si, cortar la conexion'}
          </button>
        </div>
      </div>
    </div>
  );
}
