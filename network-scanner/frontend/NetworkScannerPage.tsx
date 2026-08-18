import { useState } from 'react';
import { LoadingSpinner } from '../../shared/components/LoadingSpinner';
import { BlockConfirmDialog } from './components/BlockConfirmDialog';
import { DeviceDetail } from './components/DeviceDetail';
import { DeviceList } from './components/DeviceList';
import { ScanButton } from './components/ScanButton';
import { SummaryHeader } from './components/SummaryHeader';
import {
  useBlockDevice,
  useBlockingCapability,
  useDevices,
  useScanNetwork,
  useSummary,
  useUnblockDevice,
  useUpdateDevice,
} from './hooks/useNetworkScanner';
import type { NetworkDevice } from './types';
import { extractErrorMessage } from './utils';

export function NetworkScannerPage() {
  const { data: devices, isLoading: isLoadingDevices } = useDevices();
  const { data: summary, isLoading: isLoadingSummary } = useSummary();
  const { data: capability } = useBlockingCapability();
  const scanMutation = useScanNetwork();
  const updateMutation = useUpdateDevice();
  const blockMutation = useBlockDevice();
  const unblockMutation = useUnblockDevice();

  const [selectedDevice, setSelectedDevice] = useState<NetworkDevice | null>(null);
  const [blockConfirmTarget, setBlockConfirmTarget] = useState<NetworkDevice | null>(null);

  const handleToggleRecognized = (device: NetworkDevice) => {
    updateMutation.mutate(
      { id: device.id, request: { recognized: !device.recognized } },
      {
        onSuccess: (updated) => {
          setSelectedDevice((current) => (current?.id === updated.id ? updated : current));
        },
      },
    );
  };

  const handleSaveName = (device: NetworkDevice, name: string) => {
    updateMutation.mutate(
      { id: device.id, request: { customName: name } },
      {
        onSuccess: (updated) => {
          setSelectedDevice((current) => (current?.id === updated.id ? updated : current));
        },
      },
    );
  };

  const handleConfirmBlock = () => {
    if (!blockConfirmTarget) return;
    const deviceId = blockConfirmTarget.id;
    blockMutation.mutate(deviceId, {
      onSuccess: (updated) => {
        setSelectedDevice((current) => (current?.id === updated.id ? updated : current));
        setBlockConfirmTarget(null);
      },
      onError: () => {
        setBlockConfirmTarget(null);
      },
    });
  };

  const handleUnblock = (device: NetworkDevice) => {
    unblockMutation.mutate(device.id, {
      onSuccess: (updated) => {
        setSelectedDevice((current) => (current?.id === updated.id ? updated : current));
      },
    });
  };

  const blockingBusyDeviceId = blockMutation.isPending
    ? (blockMutation.variables ?? null)
    : unblockMutation.isPending
      ? (unblockMutation.variables ?? null)
      : null;

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2 className="font-mono text-2xl font-bold uppercase tracking-wide text-cyber-yellow text-glow-accent">
          Escaner de red domestica
        </h2>
        <p className="mt-1 text-cyber-textDim">
          Descubre que dispositivos estan conectados a tu red y detecta si hay alguno que no reconoces.
        </p>
      </div>

      {isLoadingSummary ? (
        <LoadingSpinner label="Cargando el estado de tu red…" />
      ) : summary ? (
        <SummaryHeader summary={summary} />
      ) : null}

      <ScanButton onScan={() => scanMutation.mutate()} isScanning={scanMutation.isPending} />

      {scanMutation.data && (
        <p className="text-sm text-cyber-textDim">
          Red escaneada: <span className="font-medium text-cyber-text">{scanMutation.data.networkInterfaceName ?? 'red local'}</span>
          {' '}({scanMutation.data.subnetScanned}). Si no es la red que esperabas, comprueba que tu equipo esta
          conectado al Wi-Fi correcto y vuelve a escanear.
        </p>
      )}

      {scanMutation.isError && (
        <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
          No se ha podido completar el escaneo. Comprueba tu conexion e intentalo de nuevo.
        </p>
      )}

      {capability && !capability.available && (
        <p className="border border-cyber-border bg-cyber-panel px-4 py-3 text-sm text-cyber-textDim">
          El bloqueo automatico de dispositivos no esta disponible: {capability.message}
        </p>
      )}

      {(blockMutation.isError || unblockMutation.isError) && (
        <p className="border border-risk-red/40 bg-risk-red/10 px-4 py-3 text-sm text-risk-red">
          {extractErrorMessage(
            blockMutation.error ?? unblockMutation.error,
            'No se ha podido completar la operacion sobre el dispositivo. Intentalo de nuevo.',
          )}
        </p>
      )}

      <div>
        <h3 className="mb-3 font-mono text-lg font-semibold uppercase tracking-wide text-cyber-text">
          Dispositivos detectados
        </h3>
        {isLoadingDevices ? (
          <LoadingSpinner label="Cargando dispositivos guardados…" />
        ) : (
          <DeviceList
            devices={devices ?? []}
            onSelect={setSelectedDevice}
            onToggleRecognized={handleToggleRecognized}
            updatingDeviceId={updateMutation.isPending ? (updateMutation.variables?.id ?? null) : null}
            capability={capability}
            onRequestBlock={setBlockConfirmTarget}
            onUnblock={handleUnblock}
            blockingBusyDeviceId={blockingBusyDeviceId}
          />
        )}
      </div>

      {selectedDevice && (
        <DeviceDetail
          device={selectedDevice}
          onClose={() => setSelectedDevice(null)}
          onToggleRecognized={handleToggleRecognized}
          onSaveName={handleSaveName}
          isUpdating={updateMutation.isPending}
          capability={capability}
          onRequestBlock={setBlockConfirmTarget}
          onUnblock={handleUnblock}
          isBlockingBusy={blockingBusyDeviceId === selectedDevice.id}
        />
      )}

      {blockConfirmTarget && (
        <BlockConfirmDialog
          device={blockConfirmTarget}
          onCancel={() => setBlockConfirmTarget(null)}
          onConfirm={handleConfirmBlock}
          isBlocking={blockMutation.isPending}
        />
      )}
    </div>
  );
}
