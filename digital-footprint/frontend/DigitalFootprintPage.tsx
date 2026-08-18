import { useState } from 'react';
import { DomainAnalysisPanel } from './components/DomainAnalysisPanel';
import { FileAnalysisPanel } from './components/FileAnalysisPanel';

type Tab = 'archivo' | 'dominio';

export function DigitalFootprintPage() {
  const [tab, setTab] = useState<Tab>('archivo');

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Detective de huella digital
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Descubre que expones publicamente sin darte cuenta: metadatos ocultos en tus archivos, o
          subdominios y datos de registro de un dominio.
        </p>
      </div>

      <div className="flex gap-1 border-b border-cyber-border">
        {(
          [
            { id: 'archivo', label: 'Analizar archivo' },
            { id: 'dominio', label: 'Analizar dominio' },
          ] as const
        ).map((option) => (
          <button
            key={option.id}
            onClick={() => setTab(option.id)}
            className={`border-b-2 px-4 py-2 font-mono text-xs font-medium uppercase tracking-wide transition ${
              tab === option.id
                ? 'border-cyber-yellow text-cyber-yellow'
                : 'border-transparent text-cyber-textDim hover:text-cyber-text'
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>

      {tab === 'archivo' ? <FileAnalysisPanel /> : <DomainAnalysisPanel />}
    </div>
  );
}
