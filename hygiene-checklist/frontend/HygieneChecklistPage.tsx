import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { checkStatusStyle } from '../../shared/utils/checkStatus';
import { HygieneItemRow } from './components/HygieneItemRow';
import { useHygieneChecklist, useRunHygieneCheck, useUpdateHygieneItem } from './hooks/useHygieneChecklist';

export function HygieneChecklistPage() {
  const { data: checklist, isLoading } = useHygieneChecklist();
  const checkMutation = useRunHygieneCheck();
  const updateMutation = useUpdateHygieneItem();

  const activeChecklist = checkMutation.data ?? checklist;
  const overallStyle = activeChecklist ? checkStatusStyle(activeChecklist.overallStatus) : null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Checklist de higiene
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Comprobaciones activas de tu propio equipo: firewall, antivirus, cifrado de disco, y preguntas
          rapidas sobre lo que no se puede detectar automaticamente.
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
          {checkMutation.isPending ? 'Revisando tu equipo…' : 'Revisar mi equipo'}
        </button>
        {checkMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido completar la revision. Intentalo de nuevo.
          </p>
        )}
      </div>

      {isLoading && !activeChecklist ? (
        <LoadingSpinner label="Cargando el checklist…" />
      ) : activeChecklist ? (
        <div className="flex flex-col gap-4">
          {overallStyle && (
            <div className={`border bg-cyber-panel p-5 ${overallStyle.border} ${overallStyle.glow}`}>
              <p className={`font-mono text-lg font-semibold uppercase tracking-wide ${overallStyle.text}`}>
                {activeChecklist.itemsInGoodShape} de {activeChecklist.totalItems} en orden
              </p>
              <p className="mt-1 text-sm text-cyber-textDim">
                {activeChecklist.overallStatus === 'PELIGRO'
                  ? 'Hay al menos un punto serio que corregir cuanto antes.'
                  : activeChecklist.overallStatus === 'ATENCION'
                    ? 'Hay algo que conviene revisar.'
                    : activeChecklist.overallStatus === 'NO_VERIFICABLE'
                      ? 'Hay items que no se han podido comprobar o responder todavia.'
                      : 'Todo en orden en las comprobaciones realizadas.'}
              </p>
            </div>
          )}

          <div className="flex flex-col gap-3">
            {activeChecklist.items.map((item) => (
              <HygieneItemRow
                key={item.id}
                item={item}
                onAnswer={(itemId, status) => updateMutation.mutate({ itemId, request: { status } })}
                isUpdating={updateMutation.isPending && updateMutation.variables?.itemId === item.id}
              />
            ))}
          </div>
        </div>
      ) : null}
    </div>
  );
}
