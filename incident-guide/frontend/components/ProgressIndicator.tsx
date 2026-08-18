interface ProgressIndicatorProps {
  step: number;
  onBack: () => void;
}

export function ProgressIndicator({ step, onBack }: ProgressIndicatorProps) {
  return (
    <div className="flex items-center gap-3">
      <button
        onClick={onBack}
        className="font-mono text-xs font-medium uppercase tracking-wide text-cyber-textDim underline underline-offset-2 hover:text-cyber-yellow"
      >
        ← Atras
      </button>
      <span className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">Paso {step}</span>
    </div>
  );
}
