import { CircleAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';

/** Đang hỏi máy chủ trạng thái máy quầy. */
export function DeviceChecking() {
  const { t } = useTranslation();
  return (
    <div
      className="flex items-center justify-center gap-2 py-10 text-sm text-muted-foreground"
      role="status"
    >
      <Spinner />
      {t('auth.device.checking')}
    </div>
  );
}

/** Không hỏi được trạng thái máy quầy (mất mạng, máy chủ lỗi). */
export function DeviceCheckFailed({ message, onRetry }: { message: string; onRetry: () => void }) {
  const { t } = useTranslation();
  return (
    <div className="flex flex-col gap-4">
      <Alert
        variant="error"
        icon={<CircleAlert aria-hidden />}
        title={t('auth.device.checkFailed')}
      >
        {message}
      </Alert>
      <Button variant="outline" onClick={onRetry}>
        {t('auth.device.retry')}
      </Button>
    </div>
  );
}
