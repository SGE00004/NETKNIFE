import { RiskSemaphore } from '../../../shared/components/RiskSemaphore';
import type { DeviceSummary } from '../types';

interface SummaryHeaderProps {
  summary: DeviceSummary;
}

export function SummaryHeader({ summary }: SummaryHeaderProps) {
  const { totalDevices, unrecognizedDevices, newSinceLastScan, riskLevel } = summary;

  const title =
    newSinceLastScan > 0
      ? `${newSinceLastScan} dispositivo${newSinceLastScan === 1 ? '' : 's'} nuevo${newSinceLastScan === 1 ? '' : 's'} desde tu ultimo escaneo`
      : unrecognizedDevices > 0
        ? `${totalDevices} dispositivos detectados, ${unrecognizedDevices} desconocido${unrecognizedDevices === 1 ? '' : 's'}`
        : `${totalDevices} dispositivos detectados, todos reconocidos`;

  const description =
    newSinceLastScan > 0
      ? 'Han aparecido dispositivos que no estaban en tu ultimo escaneo. Revisalos y confirma si los reconoces.'
      : unrecognizedDevices > 0
        ? 'Hay al menos un dispositivo en tu red que no has marcado como reconocido. Revisa la lista y confirma si es tuyo.'
        : totalDevices === 0
          ? 'Todavia no se ha escaneado tu red. Pulsa "Escanear ahora" para ver que dispositivos estan conectados.'
          : 'Todos los dispositivos de tu red han sido reconocidos por ti. Todo en orden.';

  return <RiskSemaphore level={riskLevel} title={title} description={description} />;
}
