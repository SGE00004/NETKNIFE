import type { CaptureStatus, TrafficCaptureCapability } from '../types';

interface CaptureControlsProps {
  status: CaptureStatus | undefined;
  capability: TrafficCaptureCapability | undefined;
  onStart: () => void;
  onStop: () => void;
  isStarting: boolean;
  isStopping: boolean;
}

export function CaptureControls({ status, capability, onStart, onStop, isStarting, isStopping }: CaptureControlsProps) {
  const isRunning = status?.running ?? false;
  const isAvailable = capability?.available ?? true;

  return (
    <div className="flex flex-col items-start gap-2">
      {isRunning ? (
        <button
          onClick={onStop}
          disabled={isStopping}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-cyber-yellow px-5 py-3 font-mono font-semibold uppercase tracking-widest text-black transition hover:bg-black hover:text-cyber-yellow disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isStopping ? 'Deteniendo…' : 'Detener analisis'}
        </button>
      ) : (
        <button
          onClick={onStart}
          disabled={isStarting || !isAvailable}
          title={!isAvailable ? (capability?.message ?? undefined) : undefined}
          className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
        >
          {isStarting && <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />}
          {isStarting ? 'Empezando…' : 'Empezar a analizar'}
        </button>
      )}
      {!isAvailable && capability?.message && (
        <p className="max-w-md text-sm text-cyber-textDim">{capability.message}</p>
      )}
    </div>
  );
}
