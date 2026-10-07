import { TriangleAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/Button';

export function ErrorFallback() {
  const { t } = useTranslation();
  return (
    <div
      className="flex min-h-dvh flex-col items-center justify-center gap-4 px-4 text-center"
      role="alert"
    >
      <TriangleAlert className="size-10 text-destructive" aria-hidden />
      <h1 className="text-xl font-bold">{t('errors.unexpectedTitle')}</h1>
      <p className="max-w-sm text-sm text-muted-foreground">{t('errors.unexpectedBody')}</p>
      <Button onClick={() => window.location.reload()}>{t('errors.reload')}</Button>
    </div>
  );
}
