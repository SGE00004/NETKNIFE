interface LoadingSpinnerProps {
  label?: string;
}

export function LoadingSpinner({ label }: LoadingSpinnerProps) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-10 text-cyber-textDim">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-cyber-border border-t-cyber-yellow shadow-glow-yellow-sm" />
      {label && <p className="font-mono text-sm uppercase tracking-wide">{label}</p>}
    </div>
  );
}
