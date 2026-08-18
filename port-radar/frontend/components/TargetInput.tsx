interface TargetInputProps {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function TargetInput({ value, onChange, disabled }: TargetInputProps) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor="port-radar-target" className="font-mono text-xs uppercase tracking-wide text-cyber-textDim">
        Host o direccion IP a escanear
      </label>
      <input
        id="port-radar-target"
        type="text"
        value={value}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
        placeholder="127.0.0.1"
        className="w-full max-w-xs border border-cyber-border bg-cyber-panel px-3 py-2 font-mono text-sm text-cyber-text outline-none focus:border-cyber-yellow disabled:opacity-60"
      />
      <p className="max-w-md text-xs text-cyber-textDim">
        Escanea solo equipos que sean tuyos o para los que tengas permiso explicito. Por defecto se
        rellena con tu propio equipo (127.0.0.1).
      </p>
    </div>
  );
}
