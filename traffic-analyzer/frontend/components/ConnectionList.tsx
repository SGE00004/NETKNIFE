import type { ActiveConnection } from '../types';
import { ConnectionCard } from './ConnectionCard';

interface ConnectionListProps {
  connections: ActiveConnection[];
  onSelect: (connection: ActiveConnection) => void;
}

export function ConnectionList({ connections, onSelect }: ConnectionListProps) {
  if (connections.length === 0) {
    return (
      <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
        Todavia no se ha visto ninguna conexion. Si acabas de empezar a analizar, dale unos segundos.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      {connections.map((connection) => (
        <ConnectionCard
          key={`${connection.protocol}-${connection.remoteIp}-${connection.remotePort}`}
          connection={connection}
          onSelect={onSelect}
        />
      ))}
    </div>
  );
}
