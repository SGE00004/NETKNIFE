import type { IncidentSymptom } from '../types';

interface SymptomSelectorProps {
  symptoms: IncidentSymptom[];
  onSelect: (symptom: IncidentSymptom) => void;
}

export function SymptomSelector({ symptoms, onSelect }: SymptomSelectorProps) {
  return (
    <div className="flex flex-col gap-3">
      {symptoms.map((symptom) => {
        const available = symptom.tree !== null;
        return (
          <button
            key={symptom.id}
            disabled={!available}
            onClick={() => available && onSelect(symptom)}
            className={`flex flex-col gap-1 border p-4 text-left transition ${
              available
                ? 'border-cyber-border bg-cyber-panel hover:border-cyber-yellow hover:shadow-glow-yellow-sm'
                : 'cursor-not-allowed border-cyber-border/50 bg-cyber-panel/50 opacity-60'
            }`}
          >
            <div className="flex items-center gap-2">
              <p className="font-mono font-semibold text-cyber-text">{symptom.title}</p>
              {!available && (
                <span className="border border-cyber-border px-1.5 py-0.5 font-mono text-[10px] uppercase tracking-wide text-cyber-textDim">
                  Proximamente
                </span>
              )}
            </div>
            <p className="text-sm text-cyber-textDim">{symptom.description}</p>
          </button>
        );
      })}
    </div>
  );
}
