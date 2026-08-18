import type { RiskLevel } from '../types/riskLevel';

interface RiskSemaphoreProps {
  level: RiskLevel;
  title: string;
  description: string;
}

const STYLES: Record<RiskLevel, { dot: string; border: string; glow: string; text: string }> = {
  VERDE: {
    dot: 'bg-risk-green',
    border: 'border-risk-green/40',
    glow: 'shadow-glow-green',
    text: 'text-risk-green',
  },
  AMARILLO: {
    dot: 'bg-risk-yellow',
    border: 'border-risk-yellow/40',
    glow: 'shadow-glow-yellow',
    text: 'text-risk-yellow',
  },
  ROJO: {
    dot: 'bg-risk-red',
    border: 'border-risk-red/40',
    glow: 'shadow-glow-red',
    text: 'text-risk-red',
  },
};

export function RiskSemaphore({ level, title, description }: RiskSemaphoreProps) {
  const style = STYLES[level];
  return (
    <div className={`flex items-start gap-4 border bg-cyber-panel p-5 ${style.border}`}>
      <span className={`mt-1 h-3.5 w-3.5 flex-shrink-0 rounded-full ${style.dot} ${style.glow}`} aria-hidden />
      <div>
        <p className={`font-mono text-lg font-semibold uppercase tracking-wide ${style.text}`}>{title}</p>
        <p className="mt-1 text-sm text-cyber-textDim">{description}</p>
      </div>
    </div>
  );
}
