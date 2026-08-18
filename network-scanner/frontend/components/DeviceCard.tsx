import type { BlockingCapability, NetworkDevice } from '../types';
import { formatRelativeTime } from '../utils';

interface DeviceCardProps {
  device: NetworkDevice;
  onSelect: (device: NetworkDevice) => void;
  onToggleRecognized: (device: NetworkDevice) => void;
  isUpdating: boolean;
  capability?: BlockingCapability;
  onRequestBlock: (device: NetworkDevice) => void;
  onUnblock: (device: NetworkDevice) => void;
  isBlockingBusy: boolean;
}

export function DeviceCard({
  device,
  onSelect,
  onToggleRecognized,
  isUpdating,
  capability,
  onRequestBlock,
  onUnblock,
  isBlockingBusy,
}: DeviceCardProps) {
  const borderStyle = device.blocked
    ? 'border-risk-red bg-risk-red/5'
    : device.recognized
      ? 'border-cyber-border'
      : device.newSinceLastScan
        ? 'border-risk-red/60 shadow-glow-red'
        : 'border-risk-yellow/50 shadow-glow-yellow-sm';

  return (
    <div className={`flex flex-col gap-3 border bg-cyber-panel p-4 sm:flex-row sm:items-center sm:justify-between ${borderStyle}`}>
      <button className="flex-1 text-left" onClick={() => onSelect(device)}>
        <div className="flex items-center gap-2">
          <p className="font-mono font-semibold text-cyber-text">{device.displayName}</p>
          {device.blocked && (
            <span className="border border-risk-red bg-risk-red/10 px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-red">
              Bloqueado
            </span>
          )}
          {!device.recognized && device.newSinceLastScan && (
            <span className="animate-flicker border border-risk-red/60 bg-risk-red/10 px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-red">
              Nuevo
            </span>
          )}
          {!device.recognized && !device.newSinceLastScan && (
            <span className="border border-risk-yellow/60 bg-risk-yellow/10 px-2 py-0.5 font-mono text-xs font-medium uppercase tracking-wide text-risk-yellow">
              Desconocido
            </span>
          )}
        </div>
        <p className="text-sm text-cyber-textDim">
          {device.ipAddress} · Visto por ultima vez {formatRelativeTime(device.lastSeen)}
        </p>
      </button>

      <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
        <button
          onClick={() => onToggleRecognized(device)}
          disabled={isUpdating}
          className={`whitespace-nowrap border px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide transition disabled:opacity-50 ${
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
            className="whitespace-nowrap border border-risk-green bg-risk-green/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-green transition hover:bg-risk-green hover:text-black disabled:opacity-50"
          >
            {isBlockingBusy ? 'Restaurando…' : 'Restaurar conexion'}
          </button>
        )}

        {!device.recognized && !device.blocked && (
          <button
            onClick={() => onRequestBlock(device)}
            disabled={isBlockingBusy || !capability?.available}
            title={!capability?.available ? (capability?.message ?? undefined) : undefined}
            className="whitespace-nowrap border border-risk-red/60 bg-risk-red/10 px-4 py-2 font-mono text-sm font-medium uppercase tracking-wide text-risk-red transition hover:bg-risk-red hover:text-black disabled:cursor-not-allowed disabled:opacity-40"
          >
            {isBlockingBusy ? 'Cortando conexion…' : 'Cortar conexion'}
          </button>
        )}
      </div>
    </div>
  );
}
