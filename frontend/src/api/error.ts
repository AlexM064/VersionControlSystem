import { AxiosError } from 'axios';

interface ErrorPayload {
  message?: string;
  error?: string;
  details?: string;
}

const getPayloadMessage = (payload: unknown): string | null => {
  if (!payload) return null;

  if (typeof payload === 'string') {
    return payload;
  }

  if (typeof payload === 'object') {
    const candidate = payload as ErrorPayload;
    return candidate.message || candidate.error || candidate.details || null;
  }

  return null;
};

export const getApiErrorMessage = (error: unknown, fallback = 'An unexpected error occurred'): string => {
  if (error instanceof AxiosError) {
    const responseMessage = getPayloadMessage(error.response?.data);
    if (responseMessage) {
      return responseMessage;
    }

    if (error.response?.status === 404) {
      return 'Resource not found';
    }

    if (error.response?.status === 401) {
      return 'Unauthorized. Please log in again.';
    }

    if (error.response?.status === 403) {
      return 'You do not have permission to perform this action.';
    }

    if (error.response?.status === 400) {
      return 'The request is invalid. Please verify your input.';
    }

    if (error.response?.status === 500) {
      return 'Server error. Please try again in a moment.';
    }

    return error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  return fallback;
};
