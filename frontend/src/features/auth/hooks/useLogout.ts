import { useMutation } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useAuthStore, type SessionNotice } from '@/store/authStore';
import { authApi } from '../api/authApi';

interface LogoutOptions {
  /** Thông báo hiển thị ở trang đăng nhập; mặc định "Bạn đã đăng xuất.", {@code null} để không hiển thị. */
  notice?: SessionNotice | null;
}

/** Đăng xuất: kết thúc phiên ở máy chủ rồi xóa phiên ở giao diện, kể cả khi máy chủ đã kết thúc phiên trước. */
export function useLogout() {
  const signOut = useAuthStore((state) => state.signOut);
  const { t } = useTranslation();

  return useMutation<void, never, LogoutOptions | void>({
    mutationFn: async () => {
      try {
        await authApi.logout();
      } catch {
        // Phiên đã hết ở máy chủ hoặc mất mạng: vẫn xóa phiên ở giao diện.
      }
    },
    onSettled: (_data, _error, options) => {
      const notice =
        options && options.notice !== undefined
          ? options.notice
          : { reason: 'logout' as const, message: t('auth.notice.logout') };
      signOut(notice ?? undefined);
    },
  });
}
