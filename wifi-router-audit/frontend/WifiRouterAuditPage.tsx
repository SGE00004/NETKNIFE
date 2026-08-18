import { useState } from 'react';
import { CheckStatusCard } from '../../shared/components/CheckStatusCard';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { useLastAudit, useRunAudit } from './hooks/useWifiRouterAudit';

function formatCheckedAt(iso: string): string {
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function overallSummary(status: string): string {
  switch (status) {
    case 'OK':
      return 'No se ha detectado ningun problema en las comprobaciones realizadas.';
    case 'ATENCION':
      return 'Hay algo que conviene revisar, aunque no es una emergencia.';
    case 'PELIGRO':
      return 'Se ha detectado al menos un problema serio. Revisa las categorias marcadas en rojo.';
    default:
      return 'No se ha podido verificar todo de forma fiable. Revisa las categorias marcadas como no verificables.';
  }
}

export function WifiRouterAuditPage() {
  const [routerAddress, setRouterAddress] = useState('');
  const { data: report, isLoading } = useLastAudit();
  const auditMutation = useRunAudit();

  const activeReport = auditMutation.data ?? report;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Auditor Wi-Fi/router
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Revisa el cifrado de tu red WiFi, si el panel de tu router sigue con la contrasena de fabrica
          y si WPS esta activado.
        </p>
      </div>

      <div className="flex flex-col items-start gap-3">
        <div className="flex flex-col gap-1">
          <label htmlFor="router-address" className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            IP de tu router (opcional)
          </label>
          <input
            id="router-address"
            type="text"
            value={routerAddress}
            disabled={auditMutation.isPending}
            onChange={(e) => setRouterAddress(e.target.value)}
            placeholder="Se detecta automaticamente si lo dejas en blanco"
            className="w-full max-w-xs border border-cyber-border bg-cyber-panel px-3 py-2 font-mono text-sm text-cyber-text outline-none focus:border-cyber-yellow disabled:opacity-60"
          />
        </div>
        <button
          onClick={() => auditMutation.mutate(routerAddress)}
          disabled={auditMutation.isPending}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {auditMutation.isPending && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
          )}
          {auditMutation.isPending ? 'Auditando tu Wi-Fi/router…' : 'Auditar mi Wi-Fi/router'}
        </button>
        {auditMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido completar la auditoria. Intentalo de nuevo.
          </p>
        )}
      </div>

      {isLoading && !activeReport ? (
        <LoadingSpinner label="Cargando la ultima auditoria…" />
      ) : activeReport ? (
        <div className="flex flex-col gap-4">
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            {activeReport.routerAddress ? `Router: ${activeReport.routerAddress} · ` : ''}
            Ultima auditoria: {formatCheckedAt(activeReport.checkedAt)}
          </p>
          <CheckStatusCard
            title="Resultado general"
            status={activeReport.overallStatus}
            summary={overallSummary(activeReport.overallStatus)}
            featured
          />
          <div className="grid gap-4 sm:grid-cols-1">
            <CheckStatusCard
              title="Cifrado WiFi"
              status={activeReport.wifiEncryption.status}
              summary={activeReport.wifiEncryption.summary}
              detail={activeReport.wifiEncryption.detail}
              howToFix={activeReport.wifiEncryption.howToFix}
            />
            <CheckStatusCard
              title="Credenciales del panel del router"
              status={activeReport.defaultCredentials.status}
              summary={activeReport.defaultCredentials.summary}
              detail={activeReport.defaultCredentials.detail}
              howToFix={activeReport.defaultCredentials.howToFix}
            />
            <CheckStatusCard
              title="WPS"
              status={activeReport.wps.status}
              summary={activeReport.wps.summary}
              detail={activeReport.wps.detail}
              howToFix={activeReport.wps.howToFix}
            />
          </div>
        </div>
      ) : (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Todavia no se ha auditado tu Wi-Fi/router. Pulsa "Auditar mi Wi-Fi/router" para empezar.
        </p>
      )}
    </div>
  );
}
