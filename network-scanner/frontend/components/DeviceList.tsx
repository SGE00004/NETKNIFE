import type { BlockingCapability, NetworkDevice } from '../types';
import { DeviceCard } from './DeviceCard';

interface DeviceListProps {
  devices: NetworkDevice[];
  onSelect: (device: NetworkDevice) => void;
  onToggleRecognized: (device: NetworkDevice) => void;
  updatingDeviceId: number | null;
  capability?: BlockingCapability;
  onRequestBlock: (device: NetworkDevice) => void;
  onUnblock: (device: NetworkDevice) => void;
  blockingBusyDeviceId: number | null;
}

export function DeviceList({
  devices,
  onSelect,
  onToggleRecognized,
  updatingDeviceId,
  capability,
  onRequestBlock,
  onUnblock,
  blockingBusyDeviceId,
}: DeviceListProps) {
  if (devices.length === 0) {
    return (
      <p className="border border-dashed border-cyber-border p-6 text-center font-mono text-sm text-cyber-textDim">
        Todavia no hay dispositivos guardados. Escanea tu red para empezar.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      {devices.map((device) => (
        <DeviceCard
          key={device.id}
          device={device}
          onSelect={onSelect}
          onToggleRecognized={onToggleRecognized}
          isUpdating={updatingDeviceId === device.id}
          capability={capability}
          onRequestBlock={onRequestBlock}
          onUnblock={onUnblock}
          isBlockingBusy={blockingBusyDeviceId === device.id}
        />
      ))}
    </div>
  );
}
