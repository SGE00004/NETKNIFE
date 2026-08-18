import type { PhishingTemplate } from '../types';

interface TemplateSelectorProps {
  templates: PhishingTemplate[];
  selectedTemplateId: string | null;
  onSelect: (templateId: string) => void;
}

export function TemplateSelector({ templates, selectedTemplateId, onSelect }: TemplateSelectorProps) {
  return (
    <div>
      <p className="font-mono text-sm font-medium uppercase tracking-wide text-cyber-textDim">Plantilla</p>
      <div className="mt-2 flex flex-col gap-2">
        {templates.map((template) => {
          const selected = template.id === selectedTemplateId;
          return (
            <button
              key={template.id}
              onClick={() => onSelect(template.id)}
              className={`border p-3 text-left transition ${
                selected
                  ? 'border-cyber-yellow bg-cyber-yellow/10 shadow-glow-yellow-sm'
                  : 'border-cyber-border bg-cyber-panel hover:border-cyber-yellow/50'
              }`}
            >
              <p className={`font-mono font-semibold ${selected ? 'text-cyber-yellow' : 'text-cyber-text'}`}>
                {template.name}
              </p>
              <p className="mt-0.5 text-sm text-cyber-textDim">{template.subject}</p>
            </button>
          );
        })}
      </div>
    </div>
  );
}
