import { Outlet } from 'react-router';
import { LoadingScreen } from '@/components/common/LoadingScreen';
import { SessionTimeoutDialog } from '@/features/auth';
import { useAuthStore } from '@/store/authStore';
import { Header } from './Header';

/** Khung trang sau khi đăng nhập: thanh trên, nội dung, cảnh báo sắp hết phiên. */
export function MainLayout() {
  const session = useAuthStore((state) => state.session);
  if (!session) {
    return <LoadingScreen />;
  }
  return (
    <div className="flex min-h-dvh flex-col">
      <Header session={session} />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:py-8">
        <Outlet />
      </main>
      <SessionTimeoutDialog />
    </div>
  );
}
