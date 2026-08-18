import type { ConnectionFilter } from '../types';

interface ConnectionFilterBarProps {
  value: ConnectionFilter;
  onChange: (filter: ConnectionFilter) => void;
}

const OPTIONS: { id: ConnectionFilter; label: string }[] = [
  { id: 'all', label: 'Todas' },
  { id: 'unencrypted', label: 'Solo sin cifrar' },
  { id: 'new', label: 'Solo nuevas' },
];

export function ConnectionFilterBar({ value, onChange }: ConnectionFilterBarProps) {
  return (
    <div className="flex flex-wrap gap-2">
      {OPTIONS.map((option) => (
        <button
          key={option.id}
          onClick={() => onChange(option.id)}
          className={`border px-3 py-1.5 font-mono text-xs font-medium uppercase tracking-wide transition ${
            value === option.id
              ? 'border-cyber-yellow bg-cyber-yellow text-black'
              : 'border-cyber-border text-cyber-textDim hover:border-cyber-yellow hover:text-cyber-yellow'
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
