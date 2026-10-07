import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { useTranslation } from 'react-i18next';
import { DeviceChecking } from '../components/DeviceCheckState';
import { LoginPanel } from '../components/LoginPanel';
import { useCurrentDevice } from '../hooks/useCurrentDevice';

/**
 * Đăng nhập trên máy tính và điện thoại (S1, S2). Nếu trình duyệt là máy quầy đã đăng ký thì hiển thị như
 * đăng nhập tại quầy (S3) — máy chủ cũng tự gắn phiên với máy quầy.
 */
export function LoginPage() {
  const { t } = useTranslation();
  useDocumentTitle(t('auth.login.title'));
  const device = useCurrentDevice();

  if (device.isPending) {
    return <DeviceChecking />;
  }
  // Không hỏi được trạng thái máy thì vẫn cho đăng nhập; máy chủ mới là bên quyết định máy quầy.
  return <LoginPanel device={device.data?.registered ? device.data.device : undefined} />;
}
