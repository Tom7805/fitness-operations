import { Ban, Building2, CircleAlert, Lock, MonitorX, WifiOff } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router';
import { Alert } from '@/components/ui/Alert';
import { buttonVariants } from '@/components/ui/Button';
import { ROUTES } from '@/constants/routes';
import { NETWORK_ERROR, type ApiError } from '@/services/http/apiError';
import { formatCountdown } from '../utils/formatDuration';

interface LoginErrorAlertProps {
  error: ApiError;
  /** Số giây còn tạm khóa; chỉ dùng với ACCOUNT_TEMPORARILY_LOCKED. */
  lockSecondsLeft?: number;
}

/** Thông báo lỗi đăng nhập theo mục 8–9 của thiết kế; nội dung lấy từ máy chủ, tiêu đề theo mã lỗi. */
export function LoginErrorAlert({ error, lockSecondsLeft = 0 }: LoginErrorAlertProps) {
  const { t } = useTranslation();

  switch (error.code) {
    case 'ACCOUNT_TEMPORARILY_LOCKED':
      if (lockSecondsLeft <= 0) {
        return <Alert variant="info" title={t('auth.errors.lockExpired')} />;
      }
      return (
        <Alert variant="error" icon={<Lock aria-hidden />} title={t('auth.errors.lockedTitle')}>
          <p>{error.message}</p>
          <p className="mt-1 font-semibold tabular-nums" aria-live="off">
            {t('auth.errors.lockedCountdown', { time: formatCountdown(lockSecondsLeft) })}
          </p>
        </Alert>
      );
    case 'ACCOUNT_DISABLED':
      return (
        <Alert variant="error" icon={<Ban aria-hidden />} title={t('auth.errors.disabledTitle')}>
          {error.message}
        </Alert>
      );
    case 'DEVICE_NOT_REGISTERED':
      return (
        <Alert
          variant="warning"
          icon={<MonitorX aria-hidden />}
          title={t('auth.errors.deviceNotRegisteredTitle')}
        >
          <p>{error.message}</p>
          <Link
            to={ROUTES.counterRegister}
            className={buttonVariants({ variant: 'outline', size: 'sm', className: 'mt-2' })}
          >
            {t('auth.errors.deviceNotRegisteredAction')}
          </Link>
        </Alert>
      );
    case 'DEVICE_BRANCH_NOT_ASSIGNED':
      return (
        <Alert
          variant="warning"
          icon={<Building2 aria-hidden />}
          title={t('auth.errors.deviceBranchTitle')}
        >
          {error.message}
        </Alert>
      );
    case NETWORK_ERROR:
      return (
        <Alert variant="error" icon={<WifiOff aria-hidden />} title={t('auth.errors.networkTitle')}>
          {error.message}
        </Alert>
      );
    case 'INVALID_CREDENTIALS':
      return (
        <Alert
          variant="error"
          icon={<CircleAlert aria-hidden />}
          title={t('auth.errors.invalidCredentialsTitle')}
        >
          {error.message}
        </Alert>
      );
    default:
      return (
        <Alert
          variant="error"
          icon={<CircleAlert aria-hidden />}
          title={t('auth.errors.genericTitle')}
        >
          {error.message}
        </Alert>
      );
  }
}
