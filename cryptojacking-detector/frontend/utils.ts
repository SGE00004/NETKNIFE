import axios from 'axios';

/** Extrae el mensaje de error real del backend (ApiError.message) para mostrarlo tal cual. */
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
  return `hace ${diffDays} dia${diffDays === 1 ? '' : 's'}`;
}
