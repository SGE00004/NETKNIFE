import type { CheckStatus } from '../types/checkStatus';

interface CheckStatusStyle {
  label: string;
  icon: string;
  text: string;
  border: string;
  bg: string;
  glow: string;
}

const STYLES: Record<CheckStatus, CheckStatusStyle> = {
  OK: {
    label: 'Bien',
    icon: '✓',
    text: 'text-risk-green',
    border: 'border-risk-green/40',
    bg: 'bg-risk-green/10',
    glow: 'shadow-glow-green',
  },
  ATENCION: {
    label: 'Atencion',
    icon: '!',
    text: 'text-risk-yellow',
    border: 'border-risk-yellow/40',
    bg: 'bg-risk-yellow/10',
    glow: 'shadow-glow-yellow-sm',
  },
  PELIGRO: {
    label: 'Peligro',
    icon: '✕',
    text: 'text-risk-red',
    border: 'border-risk-red/40',
    bg: 'bg-risk-red/10',
    glow: 'shadow-glow-red',
  },
  NO_VERIFICABLE: {
    label: 'No verificable',
    icon: '?',
    text: 'text-risk-neutral',
    border: 'border-risk-neutral/40',
    bg: 'bg-risk-neutral/10',
    glow: 'shadow-glow-neutral',
  },
};

export function checkStatusStyle(status: CheckStatus): CheckStatusStyle {
  return STYLES[status];
}

/** El peor estado de una lista, usado para calcular semaforos generales en el cliente. */
export function worstCheckStatus(statuses: CheckStatus[]): CheckStatus {
  const severity: CheckStatus[] = ['PELIGRO', 'ATENCION', 'NO_VERIFICABLE', 'OK'];
  for (const candidate of severity) {
    if (statuses.includes(candidate)) {
      return candidate;
    }
  }
  return 'OK';
}
