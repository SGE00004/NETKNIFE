import type { PhishingTemplate } from '../types';

interface TemplatePreviewProps {
  template: PhishingTemplate;
}

export function TemplatePreview({ template }: TemplatePreviewProps) {
  return (
    <div>
      <p className="font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim">Vista previa</p>
      <div className="mt-2 border border-cyber-border bg-cyber-panel">
        <div className="border-b border-cyber-border p-3 font-mono text-xs text-cyber-textDim">
          <p>
            <span className="text-cyber-text">De:</span> {template.senderLabel}
          </p>
          <p>
            <span className="text-cyber-text">Asunto:</span> [Simulacion NETKNIFE] {template.subject}
          </p>
        </div>
        {/* bodyHtml es contenido fijo definido por NETKNIFE, no aportado por el usuario: seguro de renderizar. */}
        <div className="max-h-72 overflow-y-auto bg-white p-2" dangerouslySetInnerHTML={{ __html: template.bodyHtml }} />
      </div>
    </div>
  );
}
