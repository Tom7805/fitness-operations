import { useTranslation } from 'react-i18next';
import { Link, useLocation, useNavigate } from 'react-router';
import { ROUTES } from '@/constants/routes';
import type { AuthResponse, CounterDevice } from '../types/auth.types';
import { DeviceBanner } from './DeviceBanner';
import { LoginForm } from './LoginForm';
import { SessionNoticeAlert } from './SessionNoticeAlert';

interface LoginPanelProps {
  /** Máy quầy đã đăng ký mà trình duyệt đang chạy trên đó; không có thì là máy tính hoặc điện thoại. */
  device?: CounterDevice;
}

/** Thiết kế S1/S2 (máy tính, điện thoại) và S3 (máy quầy đã đăng ký). */
export function LoginPanel({ device }: LoginPanelProps) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;

  const handleSuccess = (_response: AuthResponse) => {
    navigate(from && from !== ROUTES.login ? from : ROUTES.home, { replace: true });
  };

  return (
    <div className="flex flex-col gap-6">
      {device ? <DeviceBanner device={device} /> : null}
      <header className="flex flex-col gap-1.5">
        <h1 className="text-2xl font-bold tracking-tight">
          {device ? t('auth.login.counterTitle') : t('auth.login.title')}
        </h1>
        <p className="text-sm text-muted-foreground">
          {device ? t('auth.login.counterDescription') : t('auth.login.description')}
        </p>
      </header>
      <SessionNoticeAlert />
      <LoginForm
        onSuccess={handleSuccess}
        footer={
          device ? null : (
            <p className="text-center text-sm text-muted-foreground">
              {t('auth.login.counterLink')}{' '}
              <Link to={ROUTES.counter} className="font-medium text-primary hover:underline">
                {t('auth.login.counterLinkAction')}
              </Link>
            </p>
          )
        }
      />
    </div>
  );
}
