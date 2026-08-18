import { isAxiosError } from 'axios';

/** Extrae el mensaje legible que devuelve GlobalExceptionHandler, con fallback generico. */
export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (isAxiosError(error) && typeof error.response?.data?.message === 'string') {
    return error.response.data.message;
  }
  return fallback;
}
