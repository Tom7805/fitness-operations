import { Navigate, Outlet, useLocation } from 'react-router';
import { LoadingScreen } from '@/components/common/LoadingScreen';
import { ROUTES } from '@/constants/routes';
import { useSessionBootstrap } from '@/features/auth';
import { SessionLoadFailedScreen } from './SessionLoadFailedScreen';

/** Chỉ cho vào khi đã đăng nhập; tải lại phiên nếu cần, phiên hết hạn thì về trang đăng nhập kèm lý do. */
export function ProtectedRoute() {
  const location = useLocation();
  const { accessToken, isLoading, error, retry } = useSessionBootstrap();

  if (!accessToken) {
    return (
      <Navigate to={ROUTES.login} replace state={{ from: location.pathname + location.search }} />
    );
  }
  if (isLoading) {
    return <LoadingScreen />;
  }
  if (error) {
    return <SessionLoadFailedScreen message={error.message} onRetry={retry} />;
  }
  return <Outlet />;
}
