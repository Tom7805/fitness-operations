import type { ReactNode } from 'react';
import { ErrorBoundary } from '@/components/common/ErrorBoundary';
import { I18nProvider } from './I18nProvider';
import { QueryProvider } from './QueryProvider';

export function AppProviders({ children }: { children: ReactNode }) {
  return (
    <ErrorBoundary>
      <I18nProvider>
        <QueryProvider>{children}</QueryProvider>
      </I18nProvider>
    </ErrorBoundary>
  );
}
