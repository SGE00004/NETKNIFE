import { useState } from 'react';
import { CheckStatusCard } from '../../shared/components/CheckStatusCard';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { TargetInput } from './components/TargetInput';
import { useLastPortScan, useRunPortScan } from './hooks/usePortRadar';

function formatScannedAt(iso: string): string {
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function overallSummary(portsScanned: number, openPortsCount: number): string {
  if (openPortsCount === 0) {
    return `Ninguno de los ${portsScanned} puertos comunes revisados esta abierto en este objetivo.`;
  }
  return `${openPortsCount} de ${portsScanned} puertos comunes revisados estan abiertos. Revisa cada uno abajo.`;
}

export function PortRadarPage() {
  const [target, setTarget] = useState('127.0.0.1');
  const { data: report, isLoading } = useLastPortScan();
  const scanMutation = useRunPortScan();

  const activeReport = scanMutation.data ?? report;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Radar de puertos
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Comprueba que puertos comunes estan abiertos en un equipo y que dicen de si mismos, traducido
          a lenguaje llano.
        </p>
      </div>

      <div className="flex flex-col items-start gap-3">
        <TargetInput value={target} onChange={setTarget} disabled={scanMutation.isPending} />
        <button
          onClick={() => scanMutation.mutate(target)}
          disabled={scanMutation.isPending || target.trim().length === 0}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {scanMutation.isPending && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
          )}
          {scanMutation.isPending ? 'Escaneando puertos…' : 'Escanear puertos'}
        </button>
        {scanMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido completar el escaneo. Comprueba el host indicado e intentalo de nuevo.
          </p>
        )}
      </div>

      {isLoading && !activeReport ? (
        <LoadingSpinner label="Cargando el ultimo escaneo…" />
      ) : activeReport ? (
        <div className="flex flex-col gap-4">
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            Objetivo: {activeReport.target} · Ultimo escaneo: {formatScannedAt(activeReport.scannedAt)}
          </p>
          <CheckStatusCard
            title="Resultado general"
            status={activeReport.overallStatus}
            summary={overallSummary(activeReport.portsScanned, activeReport.openPortsCount)}
            featured
          />
          {activeReport.openPorts.length > 0 && (
            <div className="grid gap-4 sm:grid-cols-1">
              {activeReport.openPorts.map((port) => (
                <CheckStatusCard
                  key={port.port}
                  title={`Puerto ${port.port} · ${port.protocolLabel}`}
                  status={port.status}
                  summary={port.summary}
                  detail={port.banner}
                  howToFix={port.howToFix}
                />
              ))}
            </div>
          )}
        </div>
      ) : (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Todavia no se ha escaneado ningun objetivo. Indica un host y pulsa "Escanear puertos" para
          empezar.
        </p>
      )}
    </div>
  );
}
