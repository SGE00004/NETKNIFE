import { useState } from 'react';
import type { BlockingCapability, NetworkDevice } from '../types';
import { formatRelativeTime, vendorExplanation } from '../utils';

interface DeviceDetailProps {
  device: NetworkDevice;
  onClose: () => void;
  onToggleRecognized: (device: NetworkDevice) => void;
  onSaveName: (device: NetworkDevice, name: string) => void;
  isUpdating: boolean;
  capability?: BlockingCapability;
  onRequestBlock: (device: NetworkDevice) => void;
  onUnblock: (device: NetworkDevice) => void;
  isBlockingBusy: boolean;
}

export function DeviceDetail({
  device,
  onClose,
  onToggleRecognized,
  onSaveName,
  isUpdating,
  capability,
  onRequestBlock,
  onUnblock,
  isBlockingBusy,
}: DeviceDetailProps) {
  const [name, setName] = useState(device.customName ?? '');

  return (
    <div className="fixed inset-0 z-10 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-md border border-cyber-yellow/40 bg-cyber-panel p-6 shadow-glow-yellow"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <h2 className="font-mono text-xl font-bold uppercase tracking-wide text-cyber-yellow">{device.displayName}</h2>
          <button onClick={onClose} className="text-cyber-textDim hover:text-cyber-yellow" aria-label="Cerrar">
            ✕
          </button>
        </div>

        {device.blocked && (
          <p className="mt-2 border border-risk-red/60 bg-risk-red/10 px-3 py-2 text-sm text-risk-red">
            Bloqueado desde hace {device.blockedAt ? formatRelativeTime(device.blockedAt) : 'un momento'}. Este
            dispositivo no tiene acceso a tu red mientras el bloqueo este activo.
          </p>
        )}
        {!device.recognized && device.newSinceLastScan && (
          <p className="mt-2 border border-risk-red/40 bg-risk-red/10 px-3 py-2 text-sm text-risk-red">
            Este dispositivo es nuevo desde tu ultimo escaneo. Revisalo con atencion antes de confiar en el.
          </p>
        )}
        {!device.recognized && !device.newSinceLastScan && (
          <p className="mt-2 border border-risk-yellow/40 bg-risk-yellow/10 px-3 py-2 text-sm text-risk-yellow">
            No has reconocido este dispositivo todavia. Si no sabes que es, revisalo con atencion.
          </p>
        )}

        <div className="mt-4 space-y-3 text-sm">
          <p className="text-cyber-textDim">{vendorExplanation(device.vendor)}</p>
          <p className="text-cyber-textDim">
            Fue visto por primera vez {formatRelativeTime(device.firstSeen)} y por ultima vez{' '}
            {formatRelativeTime(device.lastSeen)}.
          </p>

          <dl className="grid grid-cols-3 gap-y-1 border-t border-cyber-border pt-3 text-cyber-textDim">
            <dt>Direccion IP</dt>
            <dd className="col-span-2 font-mono text-cyber-text">{device.ipAddress}</dd>
            <dt>Direccion MAC</dt>
            <dd className="col-span-2 font-mono text-cyber-text">{device.macAddress ?? 'No disponible'}</dd>
            <dt>Nombre de red</dt>
            <dd className="col-span-2 text-cyber-text">{device.hostname ?? 'No disponible'}</dd>
          </dl>
        </div>

        <div className="mt-4">
          <label className="font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim" htmlFor="custom-name">
            Ponle un nombre que reconozcas (opcional)
          </label>
          <div className="mt-1 flex gap-2">
            <input
              id="custom-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Ej: Movil de Ana"
              className="flex-1 border border-cyber-border bg-black px-3 py-2 text-sm text-cyber-text placeholder:text-cyber-textDim focus:border-cyber-yellow focus:outline-none focus:ring-1 focus:ring-cyber-yellow"
            />
            <button
              onClick={() => onSaveName(device, name)}
              disabled={isUpdating}
              className="border border-cyber-yellow px-3 py-2 font-mono text-sm font-medium uppercase tracking-wide text-cyber-yellow transition hover:bg-cyber-yellow hover:text-black disabled:opacity-50"
            >
              Guardar
            </button>
          </div>
        </div>

        <button
          onClick={() => onToggleRecognized(device)}
          disabled={isUpdating}
          className={`mt-5 w-full border px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide transition disabled:opacity-50 ${
            device.recognized
              ? 'border-cyber-border text-cyber-textDim hover:border-cyber-yellow hover:text-cyber-yellow'
              : 'border-risk-green bg-risk-green/10 text-risk-green hover:bg-risk-green hover:text-black'
          }`}
        >
          {device.recognized ? 'Marcar como no reconocido' : 'Lo reconozco'}
        </button>

        {!device.recognized && device.blocked && (
          <button
            onClick={() => onUnblock(device)}
            disabled={isBlockingBusy}
            className="mt-3 w-full border border-risk-green bg-risk-green/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-green transition hover:bg-risk-green hover:text-black disabled:opacity-50"
          >
            {isBlockingBusy ? 'Restaurando…' : 'Restaurar conexion'}
          </button>
        )}

        {!device.recognized && !device.blocked && (
          <>
            <button
              onClick={() => onRequestBlock(device)}
              disabled={isBlockingBusy || !capability?.available}
              className="mt-3 w-full border border-risk-red/60 bg-risk-red/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:cursor-not-allowed disabled:opacity-40"
            >
              {isBlockingBusy ? 'Cortando conexion…' : 'Cortar conexion'}
            </button>
            {!capability?.available && capability?.message && (
              <p className="mt-2 text-xs text-cyber-textDim">{capability.message}</p>
            )}
          </>
        )}
      </div>
    </div>
  );
}
