import { CheckStatusCard } from '../../shared/components/CheckStatusCard';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { useLastExposureReport, useRunExposureCheck } from './hooks/useExposureChecker';

function formatCheckedAt(iso: string): string {
  return new Date(iso).toLocaleString('es-ES', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function ExposureCheckerPage() {
  const { data: report, isLoading } = useLastExposureReport();
  const checkMutation = useRunExposureCheck();

  const activeReport = checkMutation.data ?? report;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Comprobador de exposicion
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Revisa si tu red domestica tiene puntos debiles expuestos: cifrado WiFi, puertos peligrosos y
          servicios innecesarios como UPnP.
        </p>
      </div>

      <div className="flex flex-col items-start gap-2">
        <button
          onClick={() => checkMutation.mutate()}
          disabled={checkMutation.isPending}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {checkMutation.isPending && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
          )}
          {checkMutation.isPending ? 'Comprobando tu exposicion…' : 'Comprobar mi exposicion'}
        </button>
        {checkMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido completar la comprobacion. Intentalo de nuevo.
          </p>
        )}
      </div>

      {isLoading && !activeReport ? (
        <LoadingSpinner label="Cargando el ultimo informe…" />
      ) : activeReport ? (
        <div className="flex flex-col gap-4">
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            Ultima comprobacion: {formatCheckedAt(activeReport.checkedAt)}
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
              title="Puertos peligrosos"
              status={activeReport.openPorts.status}
              summary={activeReport.openPorts.summary}
              detail={activeReport.openPorts.detail}
              howToFix={activeReport.openPorts.howToFix}
            />
            <CheckStatusCard
              title="Servicios innecesarios (UPnP)"
              status={activeReport.services.status}
              summary={activeReport.services.summary}
              detail={activeReport.services.detail}
              howToFix={activeReport.services.howToFix}
            />
          </div>
        </div>
      ) : (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Todavia no se ha comprobado tu exposicion. Pulsa "Comprobar mi exposicion" para empezar.
        </p>
      )}
    </div>
  );
}

function overallSummary(status: string): string {
  switch (status) {
    case 'OK':
      return 'No se ha detectado ningun punto debil en las comprobaciones realizadas.';
    case 'ATENCION':
      return 'Hay algo que conviene revisar, aunque no es una emergencia.';
    case 'PELIGRO':
      return 'Se ha detectado al menos un punto debil serio. Revisa las categorias marcadas en rojo.';
    default:
      return 'No se ha podido verificar todo de forma fiable en este sistema. Revisa las categorias marcadas como no verificables.';
  }
}
