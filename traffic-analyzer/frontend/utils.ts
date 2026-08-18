import type { ActiveConnection } from './types';

/** Nombre bajo el que se muestra el destino de una conexion: el proveedor si se conoce, si no la IP. */
export function remoteLabel(connection: ActiveConnection): string {
  return connection.org || connection.isp || connection.remoteIp;
}

export function formatRelativeTime(isoDate: string): string {
  const date = new Date(isoDate);
  const diffMs = Date.now() - date.getTime();
  const diffSeconds = Math.round(diffMs / 1000);

  if (diffSeconds < 60) return 'hace un momento';
  const diffMinutes = Math.round(diffSeconds / 60);
  if (diffMinutes < 60) return `hace ${diffMinutes} minuto${diffMinutes === 1 ? '' : 's'}`;
  const diffHours = Math.round(diffMinutes / 60);
  return `hace ${diffHours} hora${diffHours === 1 ? '' : 's'}`;
}
