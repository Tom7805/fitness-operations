import { Navigate, Outlet, useLocation } from 'react-router';
import { ROUTES } from '@/constants/routes';
import { useAuthStore } from '@/store/authStore';

/** Trang đăng nhập: người đã đăng nhập được đưa thẳng tới trang định vào hoặc trang chính. */
export function GuestRoute() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const from = (useLocation().state as { from?: string } | null)?.from;
  if (accessToken) {
    return <Navigate to={from && from !== ROUTES.login ? from : ROUTES.home} replace />;
  }
  return <Outlet />;
}
