import { useState } from 'react';
import type { ToolId } from '../../../shared/types/toolId';
import { useSummary } from '../../../network-scanner/frontend/hooks/useNetworkScanner';
import type { TreeNode } from '../types';

interface TreeStepProps {
  node: TreeNode;
  onChooseOption: (nextNodeId: string) => void;
  onNavigateToTool: (toolId: ToolId) => void;
  onRestart: () => void;
}

export function TreeStep({ node, onChooseOption, onNavigateToTool, onRestart }: TreeStepProps) {
  if (node.type === 'question') {
    return <QuestionStep prompt={node.prompt} options={node.options} onChooseOption={onChooseOption} />;
  }
  if (node.type === 'dynamic-network-check') {
    return <DynamicNetworkStep node={node} onChooseOption={onChooseOption} />;
  }
  return <RecommendationStep node={node} onNavigateToTool={onNavigateToTool} onRestart={onRestart} />;
}

function QuestionStep({
  prompt,
  options,
  onChooseOption,
}: {
  prompt: string;
  options: { label: string; nextNodeId: string }[];
  onChooseOption: (nextNodeId: string) => void;
}) {
  return (
    <div className="flex flex-col gap-4">
      <p className="font-mono text-lg text-cyber-text">{prompt}</p>
      <div className="flex flex-col gap-2">
        {options.map((option) => (
          <button
            key={option.nextNodeId}
            onClick={() => onChooseOption(option.nextNodeId)}
            className="border border-cyber-border bg-cyber-panel px-4 py-3 text-left text-sm text-cyber-text transition hover:border-cyber-yellow hover:text-cyber-yellow"
          >
            {option.label}
          </button>
        ))}
      </div>
    </div>
  );
}

function DynamicNetworkStep({
  node,
  onChooseOption,
}: {
  node: Extract<TreeNode, { type: 'dynamic-network-check' }>;
  onChooseOption: (nextNodeId: string) => void;
}) {
  const { data: summary, isLoading } = useSummary();

  let prompt = node.fallbackPrompt;
  if (!isLoading && summary) {
    prompt =
      summary.newSinceLastScan > 0
        ? `Vimos que en tu ultimo escaneo aparecio${summary.newSinceLastScan === 1 ? '' : 'n'} ${summary.newSinceLastScan} dispositivo${summary.newSinceLastScan === 1 ? '' : 's'} nuevo${summary.newSinceLastScan === 1 ? '' : 's'} en el Escaner de Red. ¿Es sobre eso?`
        : 'No vemos dispositivos nuevos en tu ultimo escaneo del Escaner de Red. ¿Es sobre uno que ya estaba en la lista?';
  }

  return <QuestionStep prompt={prompt} options={node.options} onChooseOption={onChooseOption} />;
}

function RecommendationStep({
  node,
  onNavigateToTool,
  onRestart,
}: {
  node: Extract<TreeNode, { type: 'recommendation' }>;
  onNavigateToTool: (toolId: ToolId) => void;
  onRestart: () => void;
}) {
  const criticalSteps = node.steps.filter((step) => step.requiresConfirmation);
  const [confirmed, setConfirmed] = useState<boolean[]>(() => criticalSteps.map(() => false));
  const allConfirmed = confirmed.every(Boolean);

  const toneStyle =
    node.tone === 'urgente'
      ? { border: 'border-risk-red/40', text: 'text-risk-red', glow: 'shadow-glow-red' }
      : { border: 'border-risk-green/40', text: 'text-risk-green', glow: 'shadow-glow-green' };

  let criticalIndex = -1;

  return (
    <div className="flex flex-col gap-4">
      <div className={`border bg-cyber-panel p-5 ${toneStyle.border} ${toneStyle.glow}`}>
        <p className={`font-mono text-lg font-semibold uppercase tracking-wide ${toneStyle.text}`}>{node.title}</p>
        <p className="mt-2 text-sm text-cyber-textDim">{node.summary}</p>
      </div>

      <div className="flex flex-col gap-2">
        {node.steps.map((step, i) => {
          if (!step.requiresConfirmation) {
            return (
              <p key={i} className="border-l-2 border-cyber-border pl-3 text-sm text-cyber-textDim">
                {step.text}
              </p>
            );
          }
          criticalIndex += 1;
          const idx = criticalIndex;
          return (
            <label
              key={i}
              className="flex cursor-pointer items-start gap-3 border border-cyber-border bg-cyber-panel p-3 text-sm text-cyber-text"
            >
              <input
                type="checkbox"
                checked={confirmed[idx]}
                onChange={(e) =>
                  setConfirmed((prev) => prev.map((value, valueIdx) => (valueIdx === idx ? e.target.checked : value)))
                }
                className="mt-0.5 h-4 w-4 accent-cyber-yellow"
              />
              <span>{step.text}</span>
            </label>
          );
        })}
      </div>

      {criticalSteps.length > 0 && !allConfirmed && (
        <p className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
          Marca los pasos criticos a medida que los completes.
        </p>
      )}

      {node.relatedTool && (
        <button
          onClick={() => onNavigateToTool(node.relatedTool!.toolId)}
          className="border border-cyber-yellow bg-black px-4 py-3 font-mono text-sm font-semibold uppercase tracking-wide text-cyber-yellow transition hover:bg-cyber-yellow hover:text-black"
        >
          {node.relatedTool.label}
        </button>
      )}

      <button
        onClick={onRestart}
        className="self-start font-mono text-xs font-medium uppercase tracking-wide text-cyber-textDim underline underline-offset-2 hover:text-cyber-yellow"
      >
        Volver a los sintomas
      </button>
    </div>
  );
}
