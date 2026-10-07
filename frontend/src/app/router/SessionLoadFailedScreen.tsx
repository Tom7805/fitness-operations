import { CircleAlert } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Alert } from '@/components/ui/Alert';
import { Button } from '@/components/ui/Button';

/** Không tải được phiên vì lỗi mạng hoặc máy chủ (không phải do hết phiên). */
export function SessionLoadFailedScreen({
  message,
  onRetry,
}: {
  message: string;
  onRetry: () => void;
}) {
  const { t } = useTranslation();
  return (
    <div className="mx-auto flex min-h-dvh max-w-md flex-col justify-center gap-4 px-4">
      <Alert variant="error" icon={<CircleAlert aria-hidden />} title={t('errors.unexpectedTitle')}>
        {message}
      </Alert>
      <Button onClick={onRetry}>{t('auth.device.retry')}</Button>
    </div>
  );
}
