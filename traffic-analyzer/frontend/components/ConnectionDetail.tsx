import type { ActiveConnection } from '../types';
import { formatRelativeTime, remoteLabel } from '../utils';

interface ConnectionDetailProps {
  connection: ActiveConnection;
  onClose: () => void;
}

export function ConnectionDetail({ connection, onClose }: ConnectionDetailProps) {
  return (
    <div className="fixed inset-0 z-10 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm" onClick={onClose}>
      <div
        className="w-full max-w-md border border-cyber-yellow/40 bg-cyber-panel p-6 shadow-glow-yellow"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <h2 className="font-mono text-xl font-bold uppercase tracking-wide text-cyber-yellow">
            {remoteLabel(connection)}
          </h2>
          <button onClick={onClose} className="text-cyber-textDim hover:text-cyber-yellow" aria-label="Cerrar">
            ✕
          </button>
        </div>

        {!connection.encrypted && (
          <p className="mt-2 border border-risk-red/40 bg-risk-red/10 px-3 py-2 text-sm text-risk-red">
            Este trafico no va cifrado: si contiene datos sensibles, cualquiera en tu red podria verlos.
          </p>
        )}

        <dl className="mt-4 grid grid-cols-3 gap-y-1 text-sm text-cyber-textDim">
          <dt>Proceso</dt>
          <dd className="col-span-2 text-cyber-text">{connection.processName ?? 'Desconocido'}</dd>
          {connection.pid !== null && (
            <>
              <dt>PID</dt>
              <dd className="col-span-2 font-mono text-cyber-text">{connection.pid}</dd>
            </>
          )}
          <dt>Direccion</dt>
          <dd className="col-span-2 font-mono text-cyber-text">
            {connection.remoteIp}:{connection.remotePort ?? '?'}
          </dd>
          <dt>Protocolo</dt>
          <dd className="col-span-2 text-cyber-text">{connection.protocol}</dd>
          <dt>Cifrado</dt>
          <dd className="col-span-2 text-cyber-text">{connection.encrypted ? 'Si' : 'No'}</dd>
          <dt>Pais</dt>
          <dd className="col-span-2 text-cyber-text">{connection.country ?? 'Desconocido'}</dd>
          <dt>Ciudad</dt>
          <dd className="col-span-2 text-cyber-text">{connection.city ?? 'Desconocido'}</dd>
          <dt>Proveedor</dt>
          <dd className="col-span-2 text-cyber-text">{connection.isp ?? 'Desconocido'}</dd>
        </dl>

        <p className="mt-4 text-xs text-cyber-textDim">
          Vista por primera vez {formatRelativeTime(connection.firstSeen)}, activa hasta{' '}
          {formatRelativeTime(connection.lastSeen)}.
        </p>
      </div>
    </div>
  );
}
