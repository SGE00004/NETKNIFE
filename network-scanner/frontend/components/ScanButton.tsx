interface ScanButtonProps {
  onScan: () => void;
  isScanning: boolean;
}

export function ScanButton({ onScan, isScanning }: ScanButtonProps) {
  return (
    <div className="flex flex-col items-start gap-2">
      <button
        onClick={onScan}
        disabled={isScanning}
        className="inline-flex items-center gap-2 border border-cyber-yellow bg-black px-5 py-3 font-mono font-semibold uppercase tracking-widest text-cyber-yellow shadow-glow-yellow transition hover:bg-cyber-yellow hover:text-black disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:bg-black disabled:hover:text-cyber-yellow"
      >
        {isScanning && (
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-current/40 border-t-current" />
        )}
        {isScanning ? 'Escaneando tu red…' : 'Escanear ahora'}
      </button>
      {isScanning && (
        <p className="font-mono text-sm text-cyber-textDim">
          Esto puede tardar hasta medio minuto. Estamos revisando todos los dispositivos conectados a tu red.
        </p>
      )}
    </div>
  );
}
