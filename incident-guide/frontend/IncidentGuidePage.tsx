import { useState } from 'react';
import type { ToolId } from '../../shared/types/toolId';
import { ProgressIndicator } from './components/ProgressIndicator';
import { SymptomSelector } from './components/SymptomSelector';
import { TreeStep } from './components/TreeStep';
import { symptoms } from './symptoms';
import type { IncidentSymptom } from './types';

interface IncidentGuidePageProps {
  onNavigateToTool: (toolId: ToolId) => void;
}

export function IncidentGuidePage({ onNavigateToTool }: IncidentGuidePageProps) {
  const [selectedSymptom, setSelectedSymptom] = useState<IncidentSymptom | null>(null);
  const [history, setHistory] = useState<string[]>([]);

  const currentNodeId = history[history.length - 1] ?? null;
  const currentNode = selectedSymptom?.tree && currentNodeId ? selectedSymptom.tree.nodes[currentNodeId] : null;

  const handleSelectSymptom = (symptom: IncidentSymptom) => {
    if (!symptom.tree) return;
    setSelectedSymptom(symptom);
    setHistory([symptom.tree.rootNodeId]);
  };

  const handleChooseOption = (nextNodeId: string) => {
    setHistory((prev) => [...prev, nextNodeId]);
  };

  const handleRestart = () => {
    setSelectedSymptom(null);
    setHistory([]);
  };

  const handleBack = () => {
    if (history.length <= 1) {
      handleRestart();
    } else {
      setHistory((prev) => prev.slice(0, -1));
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Guia de respuesta a incidentes
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Elige lo que te esta pasando y te llevamos paso a paso hasta acciones concretas, sin tecnicismos.
        </p>
      </div>

      {!selectedSymptom || !currentNode ? (
        <SymptomSelector symptoms={symptoms} onSelect={handleSelectSymptom} />
      ) : (
        <div className="flex flex-col gap-4">
          <ProgressIndicator step={history.length} onBack={handleBack} />
          <TreeStep
            node={currentNode}
            onChooseOption={handleChooseOption}
            onNavigateToTool={onNavigateToTool}
            onRestart={handleRestart}
          />
        </div>
      )}
    </div>
  );
}
