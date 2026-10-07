import { CircleCheck } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLocation } from 'react-router';
import { Alert } from '@/components/ui/Alert';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { DeviceCheckFailed, DeviceChecking } from '../components/DeviceCheckState';
import { LoginPanel } from '../components/LoginPanel';
import { UnregisteredDeviceNotice } from '../components/UnregisteredDeviceNotice';
import { useCurrentDevice } from '../hooks/useCurrentDevice';
import type { DeviceRegisteredState } from '../types/auth.types';

/**
 * Trang dành cho máy tính bảng đặt tại quầy. Máy đã đăng ký: đăng nhập tại quầy (S3). Máy chưa đăng ký:
 * yêu cầu quản lý đăng nhập để đăng ký máy ngay khi mở trang, trước khi lễ tân nhập tài khoản (S4, TC-03).
 */
export function CounterPage() {
  const { t } = useTranslation();
  useDocumentTitle(t('auth.login.counterTitle'));
  const device = useCurrentDevice();
  const registered = (useLocation().state as DeviceRegisteredState | null)?.registeredDevice;

  if (device.isPending) {
    return <DeviceChecking />;
  }
  if (device.isError) {
    return (
      <DeviceCheckFailed message={device.error.message} onRetry={() => void device.refetch()} />
    );
  }
  if (!device.data.registered || !device.data.device) {
    return <UnregisteredDeviceNotice />;
  }
  return (
    <div className="flex flex-col gap-4">
      {registered ? (
        <Alert variant="success" icon={<CircleCheck aria-hidden />}>
          {t('auth.register.success', { device: registered.name, branch: registered.branchName })}
        </Alert>
      ) : null}
      <LoginPanel device={device.data.device} />
    </div>
  );
}
