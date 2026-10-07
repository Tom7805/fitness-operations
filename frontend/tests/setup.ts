import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterAll, afterEach, beforeAll, vi } from 'vitest';
import '@/i18n';
import { useAuthStore } from '@/store/authStore';
import { server } from './mocks/server';

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));

afterEach(() => {
  cleanup();
  server.resetHandlers();
  vi.useRealTimers();
  window.sessionStorage.clear();
  window.localStorage.clear();
  useAuthStore.setState({ accessToken: null, session: null, notice: null });
});

afterAll(() => server.close());
