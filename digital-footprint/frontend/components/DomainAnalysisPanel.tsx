import { useState } from 'react';
import { CheckStatusCard } from '../../../shared/components/CheckStatusCard';
import { LoadingSpinner } from '../../../shared/components/LoadingSpinner';
import { useAnalyzeDomain, useLastDomainReport } from '../hooks/useDomainFootprint';
import { SubdomainTable } from './SubdomainTable';

function overallSummary(status: string): string {
  switch (status) {
    case 'OK':
      return 'No se ha encontrado ningun subdominio sensible expuesto.';
    case 'ATENCION':
      return 'Se han encontrado subdominios que podrian no deberian ser publicos. Revisalos abajo.';
    default:
      return 'Resultado del analisis de dominio.';
  }
}

export function DomainAnalysisPanel() {
  const [domain, setDomain] = useState('');
  const { data: report, isLoading } = useLastDomainReport();
  const analyzeMutation = useAnalyzeDomain();

  const activeReport = analyzeMutation.data ?? report;

  return (
    <div className="flex flex-col gap-6">
      <p className="text-cyber-textDim">
        Indica un dominio para ver que subdominios publicos tiene, a quien pertenece segun su registro
        publico y donde esta alojado.
      </p>

      <div className="flex flex-col items-start gap-3">
        <div className="flex flex-col gap-1">
          <label htmlFor="footprint-domain" className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            Dominio
          </label>
          <input
            id="footprint-domain"
            type="text"
            value={domain}
            disabled={analyzeMutation.isPending}
            onChange={(e) => setDomain(e.target.value)}
            placeholder="ejemplo.com"
            className="w-full max-w-xs border border-cyber-border bg-cyber-panel px-3 py-2 font-mono text-sm text-cyber-text outline-none focus:border-cyber-yellow disabled:opacity-60"
          />
        </div>
        <button
          onClick={() => analyzeMutation.mutate(domain)}
          disabled={analyzeMutation.isPending || domain.trim().length === 0}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {analyzeMutation.isPending && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
          )}
          {analyzeMutation.isPending ? 'Analizando dominio…' : 'Analizar dominio'}
        </button>
        {analyzeMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido analizar el dominio. Comprueba que esta bien escrito e intentalo de nuevo.
          </p>
        )}
      </div>

      {isLoading && !activeReport ? (
        <LoadingSpinner label="Cargando el ultimo analisis…" />
      ) : activeReport ? (
        <div className="flex flex-col gap-4">
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">Dominio: {activeReport.domain}</p>
          <CheckStatusCard
            title="Resultado general"
            status={activeReport.overallStatus}
            summary={overallSummary(activeReport.overallStatus)}
            featured
          />

          <div>
            <h3 className="mb-2 font-mono text-sm font-semibold uppercase tracking-wide text-cyber-text">
              Subdominios encontrados
            </h3>
            <SubdomainTable subdomains={activeReport.subdomains} />
          </div>

          <div className="grid gap-4 sm:grid-cols-2">
            <div className="border border-cyber-border bg-cyber-panel p-4">
              <h3 className="mb-2 font-mono text-xs font-semibold uppercase tracking-wide text-cyber-textDim">
                A quien pertenece (whois)
              </h3>
              {activeReport.whois ? (
                <dl className="space-y-1 text-sm text-cyber-text">
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Registrador</dt>
                    <dd>{activeReport.whois.registrar ?? 'Desconocido'}</dd>
                  </div>
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Registrado el</dt>
                    <dd>{activeReport.whois.createdDate ?? 'Desconocido'}</dd>
                  </div>
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Caduca el</dt>
                    <dd>{activeReport.whois.expiresDate ?? 'Desconocido'}</dd>
                  </div>
                </dl>
              ) : (
                <p className="text-sm text-cyber-textDim">No se ha podido consultar el registro publico de este dominio.</p>
              )}
            </div>

            <div className="border border-cyber-border bg-cyber-panel p-4">
              <h3 className="mb-2 font-mono text-xs font-semibold uppercase tracking-wide text-cyber-textDim">
                Donde esta alojado
              </h3>
              {activeReport.geo ? (
                <dl className="space-y-1 text-sm text-cyber-text">
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">IP</dt>
                    <dd>{activeReport.geo.ipAddress}</dd>
                  </div>
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Pais</dt>
                    <dd>{activeReport.geo.country ?? 'Desconocido'}</dd>
                  </div>
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Ciudad</dt>
                    <dd>{activeReport.geo.city ?? 'Desconocido'}</dd>
                  </div>
                  <div className="flex justify-between gap-2">
                    <dt className="text-cyber-textDim">Proveedor</dt>
                    <dd>{activeReport.geo.isp ?? 'Desconocido'}</dd>
                  </div>
                </dl>
              ) : (
                <p className="text-sm text-cyber-textDim">No se ha podido geolocalizar este dominio.</p>
              )}
            </div>
          </div>
        </div>
      ) : (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Todavia no se ha analizado ningun dominio. Escribe uno y pulsa "Analizar dominio".
        </p>
      )}
    </div>
  );
}
