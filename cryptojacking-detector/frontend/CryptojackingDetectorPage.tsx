import { useState } from 'react';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { RiskSemaphore } from '../../shared/components/RiskSemaphore';
import { AlertCard } from './components/AlertCard';
import { AlertHistoryList } from './components/AlertHistoryList';
import { KillProcessConfirmDialog } from './components/KillProcessConfirmDialog';
import { useCryptojackingHistory, useCryptojackingStatus, useKillProcess } from './hooks/useCryptojackingDetector';
import type { CryptojackingAlert } from './types';
import { extractErrorMessage } from './utils';

export function CryptojackingDetectorPage() {
  const { data: status, isLoading } = useCryptojackingStatus();
  const { data: history } = useCryptojackingHistory();
  const killMutation = useKillProcess();
  const [pendingKill, setPendingKill] = useState<CryptojackingAlert | null>(null);

  const activeAlerts = status?.activeAlerts ?? [];
  const isAllNormal = status !== undefined && activeAlerts.length === 0;

  const handleConfirmKill = () => {
    if (!pendingKill) return;
    killMutation.mutate(pendingKill.pid, {
      onSuccess: () => setPendingKill(null),
    });
  };

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Detector de cryptojacking
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Vigila en segundo plano si algun proceso esta usando tu ordenador para minar criptomonedas sin
          que te des cuenta.
        </p>
      </div>

      {isLoading || !status ? (
        <LoadingSpinner label="Comprobando el uso de CPU…" />
      ) : (
        <RiskSemaphore
          level={status.overallStatus}
          title={isAllNormal ? 'Todo normal' : 'Actividad sospechosa detectada'}
          description={
            isAllNormal
              ? 'No se ha detectado ningun proceso con un uso de CPU sospechoso.'
              : `${activeAlerts.length} proceso(s) con actividad sospechosa. Revisa los detalles abajo.`
          }
        />
      )}

      {killMutation.isError && (
        <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
          {extractErrorMessage(killMutation.error, 'No se ha podido finalizar el proceso. Intentalo de nuevo.')}
        </p>
      )}

      {activeAlerts.length > 0 && (
        <div className="flex flex-col gap-4">
          {activeAlerts.map((alert) => (
            <AlertCard
              key={alert.id}
              alert={alert}
              onKill={setPendingKill}
              isKilling={killMutation.isPending && pendingKill?.pid === alert.pid}
            />
          ))}
        </div>
      )}

      <div>
        <h3 className="mb-2 font-mono text-sm font-semibold uppercase tracking-wide text-cyber-text">
          Historial de alertas
        </h3>
        <AlertHistoryList alerts={history ?? []} />
      </div>

      {pendingKill && (
        <KillProcessConfirmDialog
          alert={pendingKill}
          onCancel={() => setPendingKill(null)}
          onConfirm={handleConfirmKill}
          isKilling={killMutation.isPending}
        />
      )}
    </div>
  );
}
