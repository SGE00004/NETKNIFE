import axios from 'axios';

/**
 * Extrae el mensaje de error real que devuelve el backend (ApiError.message), en
 * vez del generico "no se ha podido completar la operacion" que usa el resto del
 * escaner: para el bloqueo de dispositivos el motivo especifico (guardarraiel de
 * seguridad, falta de Npcap...) es informacion accionable que el usuario necesita ver.
 */
export function extractErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error) && typeof error.response?.data?.message === 'string') {
    return error.response.data.message;
  }
  return fallback;
}

export function formatRelativeTime(isoDate: string): string {
  const date = new Date(isoDate);
  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.round(diffMs / 60_000);

  if (diffMinutes < 1) return 'hace un momento';
  if (diffMinutes < 60) return `hace ${diffMinutes} minuto${diffMinutes === 1 ? '' : 's'}`;

  const diffHours = Math.round(diffMinutes / 60);
  if (diffHours < 24) return `hace ${diffHours} hora${diffHours === 1 ? '' : 's'}`;

  const diffDays = Math.round(diffHours / 24);
  if (diffDays < 30) return `hace ${diffDays} dia${diffDays === 1 ? '' : 's'}`;

  const diffMonths = Math.round(diffDays / 30);
  return `hace ${diffMonths} mes${diffMonths === 1 ? '' : 'es'}`;
}

export function vendorExplanation(vendor: string | null): string {
  if (!vendor || vendor === 'Fabricante desconocido') {
    return 'No hemos podido identificar el fabricante de este dispositivo a partir de su direccion de red.';
  }
  return `Este dispositivo es probablemente de ${vendor}.`;
}
