import { useState } from 'react';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { extractApiErrorMessage } from '../../shared/utils/apiError';
import { RecipientInput } from './components/RecipientInput';
import { SimulationResultsList } from './components/SimulationResultsList';
import { TemplatePreview } from './components/TemplatePreview';
import { TemplateSelector } from './components/TemplateSelector';
import { usePhishingResults, usePhishingTemplates, useSendPhishingSimulation } from './hooks/usePhishingSimulator';

export function PhishingSimulatorPage() {
  const { data: templates, isLoading: isLoadingTemplates } = usePhishingTemplates();
  const { data: results, isLoading: isLoadingResults } = usePhishingResults();
  const sendMutation = useSendPhishingSimulation();

  const [recipients, setRecipients] = useState<string[]>([]);
  const [templateId, setTemplateId] = useState<string | null>(null);
  const [consentConfirmed, setConsentConfirmed] = useState(false);

  const selectedTemplate = templates?.find((t) => t.id === templateId) ?? null;
  const canSubmit = recipients.length > 0 && templateId !== null && consentConfirmed && !sendMutation.isPending;

  const handleSubmit = () => {
    if (!templateId) return;
    sendMutation.mutate(
      { recipients, templateId, consentConfirmed },
      {
        onSuccess: () => {
          setRecipients([]);
          setConsentConfirmed(false);
        },
      },
    );
  };

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Simulador de phishing
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Envia un email de prueba educativo a alguien de tu confianza (con su consentimiento) para practicar
          juntos como reconocer un intento de phishing real.
        </p>
      </div>

      {isLoadingTemplates ? (
        <LoadingSpinner label="Cargando plantillas…" />
      ) : templates && templates.length > 0 ? (
        <div className="flex flex-col gap-4 border border-cyber-border bg-cyber-panel p-5">
          <RecipientInput recipients={recipients} onChange={setRecipients} />

          <TemplateSelector templates={templates} selectedTemplateId={templateId} onSelect={setTemplateId} />

          {selectedTemplate && <TemplatePreview template={selectedTemplate} />}

          <label className="flex cursor-pointer items-start gap-3 border border-cyber-yellow/40 bg-cyber-yellow/5 p-3 text-sm text-cyber-text">
            <input
              type="checkbox"
              checked={consentConfirmed}
              onChange={(e) => setConsentConfirmed(e.target.checked)}
              className="mt-0.5 h-4 w-4 accent-cyber-yellow"
            />
            <span>
              Confirmo que tengo el consentimiento de cada destinatario para enviarle esta simulacion educativa, y
              que no la voy a usar contra nadie sin su permiso.
            </span>
          </label>

          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="inline-flex items-center gap-2 self-start border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
          >
            {sendMutation.isPending && (
              <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
            )}
            {sendMutation.isPending ? 'Enviando…' : 'Enviar simulacion'}
          </button>

          {sendMutation.isError && (
            <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
              {extractApiErrorMessage(sendMutation.error, 'No se ha podido enviar la simulacion. Intentalo de nuevo.')}
            </p>
          )}
          {sendMutation.isSuccess && (
            <p className="border border-risk-green/40 bg-risk-green/10 px-4 py-3 text-sm text-risk-green">
              Simulacion enviada correctamente.
            </p>
          )}
        </div>
      ) : null}

      <div>
        <h3 className="mb-3 font-mono text-lg font-semibold uppercase tracking-wide text-cyber-text">
          Simulaciones enviadas
        </h3>
        {isLoadingResults ? <LoadingSpinner label="Cargando resultados…" /> : <SimulationResultsList results={results ?? []} />}
      </div>
    </div>
  );
}
