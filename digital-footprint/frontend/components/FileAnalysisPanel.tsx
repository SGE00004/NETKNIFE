import { useState } from 'react';
import { CheckStatusCard } from '../../../shared/components/CheckStatusCard';
import { LoadingSpinner } from '../../../shared/components/LoadingSpinner';
import { useAnalyzeFile, useLastFileReport } from '../hooks/useFileMetadata';

function overallSummary(status: string): string {
  switch (status) {
    case 'OK':
      return 'No se ha encontrado ningun metadato sensible en este archivo.';
    case 'ATENCION':
      return 'Este archivo contiene algun dato que podria identificarte o a tu organizacion.';
    case 'PELIGRO':
      return 'Este archivo revela informacion muy sensible, como tu ubicacion exacta.';
    default:
      return 'Resultado del analisis de metadatos.';
  }
}

export function FileAnalysisPanel() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const { data: report, isLoading } = useLastFileReport();
  const analyzeMutation = useAnalyzeFile();

  const activeReport = analyzeMutation.data ?? report;

  return (
    <div className="flex flex-col gap-6">
      <p className="text-cyber-textDim">
        Sube un PDF, Word o imagen para ver que datos ocultos expone: autor, software usado, fechas y,
        en fotos, la ubicacion GPS donde se tomaron.
      </p>

      <div className="flex flex-col items-start gap-3">
        <input
          type="file"
          accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          onChange={(e) => setSelectedFile(e.target.files?.[0] ?? null)}
          className="font-mono text-sm text-cyber-textDim file:mr-3 file:border file:border-cyber-yellow file:bg-black file:px-3 file:py-2 file:font-mono file:text-xs file:font-semibold file:uppercase file:tracking-widest file:text-cyber-yellow"
        />
        <button
          onClick={() => selectedFile && analyzeMutation.mutate(selectedFile)}
          disabled={!selectedFile || analyzeMutation.isPending}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {analyzeMutation.isPending && (
            <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
          )}
          {analyzeMutation.isPending ? 'Analizando archivo…' : 'Analizar archivo'}
        </button>
        {analyzeMutation.isError && (
          <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
            No se ha podido analizar el archivo. Comprueba que es un PDF, Word o imagen valido.
          </p>
        )}
      </div>

      {isLoading && !activeReport ? (
        <LoadingSpinner label="Cargando el ultimo analisis…" />
      ) : activeReport ? (
        <div className="flex flex-col gap-4">
          <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
            Archivo: {activeReport.originalFilename ?? 'desconocido'}
          </p>
          <CheckStatusCard
            title="Resultado general"
            status={activeReport.overallStatus}
            summary={overallSummary(activeReport.overallStatus)}
            featured
          />
          {activeReport.findings.length > 0 && (
            <div className="grid gap-4 sm:grid-cols-1">
              {activeReport.findings.map((finding, index) => (
                <CheckStatusCard
                  key={`${finding.category}-${index}`}
                  title={finding.category}
                  status={finding.status}
                  summary={finding.explanation}
                  detail={finding.value}
                />
              ))}
            </div>
          )}
        </div>
      ) : (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Todavia no se ha analizado ningun archivo. Selecciona uno y pulsa "Analizar archivo".
        </p>
      )}
    </div>
  );
}
