import { useTranslation } from 'react-i18next';
import { Button } from '@/components/ui/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/Dialog';
import { useAuthStore } from '@/store/authStore';
import { useSessionActivity } from '../hooks/useSessionActivity';

/** Thiết kế S7: cảnh báo 60 giây trước khi phiên tự kết thúc do không thao tác. */
export function SessionTimeoutDialog() {
  const { t } = useTranslation();
  const idleTimeoutSeconds = useAuthStore((state) => state.session?.idleTimeoutSeconds);
  const { secondsLeft, stayActive, logoutNow } = useSessionActivity(idleTimeoutSeconds);
  const open = secondsLeft !== null;

  return (
    <Dialog
      open={open}
      onOpenChange={(nextOpen) => {
        if (!nextOpen) {
          void stayActive();
        }
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('auth.session.warningTitle')}</DialogTitle>
          <DialogDescription>
            {t('auth.session.warningBody', { seconds: secondsLeft ?? 0 })}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="outline" onClick={logoutNow}>
            {t('auth.session.logout')}
          </Button>
          <Button onClick={() => void stayActive()} autoFocus>
            {t('auth.session.stay')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
