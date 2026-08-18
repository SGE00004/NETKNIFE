import { useState } from 'react';
import { CaptureControls } from './components/CaptureControls';
import { ConnectionDetail } from './components/ConnectionDetail';
import { ConnectionFilterBar } from './components/ConnectionFilterBar';
import { ConnectionList } from './components/ConnectionList';
import { useCapability, useCaptureStatus, useConnections, useStartCapture, useStopCapture } from './hooks/useTrafficAnalyzer';
import type { ActiveConnection, ConnectionFilter } from './types';

function applyFilter(connections: ActiveConnection[], filter: ConnectionFilter): ActiveConnection[] {
  switch (filter) {
    case 'unencrypted':
      return connections.filter((c) => !c.encrypted);
    case 'new':
      return connections.filter((c) => c.isNew);
    default:
      return connections;
  }
}

export function TrafficAnalyzerPage() {
  const { data: capability } = useCapability();
  const { data: status } = useCaptureStatus();
  const isRunning = status?.running ?? false;
  const { data: connections } = useConnections(isRunning);
  const startMutation = useStartCapture();
  const stopMutation = useStopCapture();

  const [filter, setFilter] = useState<ConnectionFilter>('all');
  const [selectedConnection, setSelectedConnection] = useState<ActiveConnection | null>(null);

  const filteredConnections = applyFilter(connections ?? [], filter);

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Analizador de trafico
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Mira en tiempo real con quien esta hablando tu ordenador: que programa, a donde, y si va cifrado.
        </p>
      </div>

      <CaptureControls
        status={status}
        capability={capability}
        onStart={() => startMutation.mutate()}
        onStop={() => stopMutation.mutate()}
        isStarting={startMutation.isPending}
        isStopping={stopMutation.isPending}
      />

      {isRunning && (
        <div className="flex flex-col gap-4">
          <ConnectionFilterBar value={filter} onChange={setFilter} />
          <ConnectionList connections={filteredConnections} onSelect={setSelectedConnection} />
        </div>
      )}

      {!isRunning && status?.available && (
        <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
          Pulsa "Empezar a analizar" para ver las conexiones activas de tu ordenador.
        </p>
      )}

      {selectedConnection && (
        <ConnectionDetail connection={selectedConnection} onClose={() => setSelectedConnection(null)} />
      )}
    </div>
  );
}
