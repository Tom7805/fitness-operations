import { useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { SESSION_WARNING_SECONDS } from '@/constants/app';
import { serverActivity } from '@/services/http/serverActivity';
import { useAuthStore } from '@/store/authStore';
import { authApi } from '../api/authApi';
import { useLogout } from './useLogout';

const ACTIVITY_EVENTS = ['pointerdown', 'keydown', 'wheel', 'touchstart', 'mousemove'] as const;

/**
 * Người dùng còn thao tác mà máy chủ lâu chưa nhận yêu cầu nào thì gửi một yêu cầu giữ phiên, nhưng không quá
 * một lần mỗi phút. Nhờ vậy phiên ở máy chủ hết hạn đúng khoảng 30 phút sau thao tác cuối (sai số dưới một phút).
 */
export const KEEP_ALIVE_THROTTLE_MS = 60_000;

export interface SessionActivity {
  /** Số giây còn lại trước khi tự đăng xuất; {@code null} khi chưa tới lúc cảnh báo. */
  secondsLeft: number | null;
  stayActive: () => Promise<void>;
  logoutNow: () => void;
}

/**
 * Theo dõi thao tác của người dùng để giữ phiên, cảnh báo 60 giây trước khi hết phiên và tự đăng xuất khi hết
 * phiên (thiết kế S7). Mốc tính là lần cuối máy chủ ghi nhận thao tác — máy chủ mới là bên quyết định hết phiên.
 */
export function useSessionActivity(idleTimeoutSeconds: number | undefined): SessionActivity {
  const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
  const warningRef = useRef(false);
  const inFlightRef = useRef(false);
  const setSession = useAuthStore((state) => state.setSession);
  const { t } = useTranslation();
  const logout = useLogout();
  const logoutRef = useRef(logout.mutate);
  const tRef = useRef(t);

  useEffect(() => {
    logoutRef.current = logout.mutate;
    tRef.current = t;
  });

  const stayActive = useCallback(async () => {
    if (inFlightRef.current) {
      return;
    }
    inFlightRef.current = true;
    try {
      setSession(await authApi.me());
    } catch {
      // Phiên đã hết ở máy chủ: bộ chặn HTTP đã đăng xuất và lưu thông báo.
    } finally {
      inFlightRef.current = false;
    }
  }, [setSession]);

  useEffect(() => {
    if (!idleTimeoutSeconds) {
      return undefined;
    }
    const idleMs = idleTimeoutSeconds * 1000;
    let finished = false;

    const onActivity = () => {
      // Đang cảnh báo thì chỉ nút "Tiếp tục làm việc" mới giữ phiên, tránh vô tình giữ phiên trên máy dùng chung.
      if (!warningRef.current && Date.now() - serverActivity.lastAt() >= KEEP_ALIVE_THROTTLE_MS) {
        void stayActive();
      }
    };

    const tick = () => {
      if (finished) {
        return;
      }
      const remainingMs = serverActivity.lastAt() + idleMs - Date.now();
      if (remainingMs <= 0) {
        finished = true;
        warningRef.current = false;
        setSecondsLeft(null);
        logoutRef.current({
          notice: {
            reason: 'expired',
            message: tRef.current('auth.session.expired', {
              minutes: Math.round(idleTimeoutSeconds / 60),
            }),
          },
        });
        return;
      }
      const warning = remainingMs <= SESSION_WARNING_SECONDS * 1000;
      warningRef.current = warning;
      setSecondsLeft(warning ? Math.ceil(remainingMs / 1000) : null);
    };

    ACTIVITY_EVENTS.forEach((event) =>
      window.addEventListener(event, onActivity, { passive: true }),
    );
    const timer = window.setInterval(tick, 1000);
    tick();
    return () => {
      ACTIVITY_EVENTS.forEach((event) => window.removeEventListener(event, onActivity));
      window.clearInterval(timer);
    };
  }, [idleTimeoutSeconds, stayActive]);

  const logoutNow = useCallback(() => logoutRef.current(), []);

  return { secondsLeft, stayActive, logoutNow };
}
