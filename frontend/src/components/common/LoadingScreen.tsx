import { useTranslation } from 'react-i18next';
import { Spinner } from '@/components/ui/Spinner';

export function LoadingScreen({ label }: { label?: string }) {
  const { t } = useTranslation();
  return (
    <div
      className="flex min-h-dvh items-center justify-center gap-2 text-sm text-muted-foreground"
      role="status"
    >
      <Spinner className="size-5" />
      {label ?? t('common.loading')}
    </div>
  );
}
