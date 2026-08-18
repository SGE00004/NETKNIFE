import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { RiskSemaphore } from '../../shared/components/RiskSemaphore';
import type { ToolId } from '../../shared/types/toolId';
import { FindingCard } from './components/FindingCard';
import { useFindingsReport } from './hooks/useFindingsReport';
import type { Finding } from './types';

interface FindingsReportPageProps {
  onNavigateToTool: (toolId: ToolId) => void;
}

const RISK_ORDER: Record<Finding['riskLevel'], number> = { ROJO: 0, AMARILLO: 1, VERDE: 2 };

function overallTitle(highRiskCount: number, mediumRiskCount: number): string {
  if (highRiskCount > 0) return 'Riesgo alto';
  if (mediumRiskCount > 0) return 'Riesgo medio';
  return 'Riesgo bajo';
}

function overallDescription(total: number, low: number, medium: number, high: number): string {
  if (total === 0) {
    return 'Todavia no hay hallazgos. Ejecuta las herramientas del equipo rojo para generar este informe.';
  }
  return `${total} hallazgo(s) en total: ${high} de riesgo alto, ${medium} de riesgo medio y ${low} de riesgo bajo.`;
}

export function FindingsReportPage({ onNavigateToTool }: FindingsReportPageProps) {
  const { data: report, isLoading } = useFindingsReport();

  const sortedFindings = report ? [...report.findings].sort((a, b) => RISK_ORDER[a.riskLevel] - RISK_ORDER[b.riskLevel]) : [];

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Informe automatico de hallazgos
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Resumen consolidado de lo que han encontrado las herramientas del equipo rojo, con el nivel de
          riesgo de cada hallazgo.
        </p>
      </div>

      {isLoading || !report ? (
        <LoadingSpinner label="Cargando el informe…" />
      ) : (
        <div className="flex flex-col gap-6">
          <RiskSemaphore
            level={report.overallRisk}
            title={overallTitle(report.highRiskCount, report.mediumRiskCount)}
            description={overallDescription(report.totalFindings, report.lowRiskCount, report.mediumRiskCount, report.highRiskCount)}
          />

          {report.modulesWithoutData.length > 0 && (
            <div className="border border-dashed border-cyber-border p-4">
              <p className="mb-2 font-mono text-xs uppercase tracking-wide text-cyber-textDim">
                Aun no has ejecutado estas herramientas del equipo rojo:
              </p>
              <div className="flex flex-wrap gap-2">
                {report.modulesWithoutData.map((module) => (
                  <button
                    key={module.id}
                    onClick={() => onNavigateToTool(module.id)}
                    className="border border-cyber-yellow px-3 py-1.5 font-mono text-xs font-medium uppercase tracking-wide text-cyber-yellow transition hover:bg-cyber-yellow hover:text-black"
                  >
                    {module.label}
                  </button>
                ))}
              </div>
            </div>
          )}

          {sortedFindings.length > 0 ? (
            <div className="grid gap-4 sm:grid-cols-1">
              {sortedFindings.map((finding) => (
                <FindingCard key={`${finding.sourceModuleId}-${finding.id}`} finding={finding} onNavigateToTool={onNavigateToTool} />
              ))}
            </div>
          ) : (
            <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
              No hay hallazgos que mostrar todavia.
            </p>
          )}
        </div>
      )}
    </div>
  );
}
