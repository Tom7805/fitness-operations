import { Clock, Info } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Alert } from '@/components/ui/Alert';
import { useAuthStore } from '@/store/authStore';

/** Lý do người dùng được đưa về trang đăng nhập: hết phiên, phiên không hợp lệ hoặc vừa đăng xuất. */
export function SessionNoticeAlert() {
  const { t } = useTranslation();
  const notice = useAuthStore((state) => state.notice);
  if (!notice) {
    return null;
  }
  if (notice.reason === 'logout') {
    return <Alert variant="info" icon={<Info aria-hidden />} title={notice.message} />;
  }
  return (
    <Alert
      variant="warning"
      icon={<Clock aria-hidden />}
      title={
        notice.reason === 'expired' ? t('auth.notice.expiredTitle') : t('auth.notice.invalidTitle')
      }
    >
      {notice.message}
    </Alert>
  );
}
