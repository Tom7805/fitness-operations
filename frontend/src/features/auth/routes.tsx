import type { RouteObject } from 'react-router';
import { ROUTES } from '@/constants/routes';
import { CounterPage } from './pages/CounterPage';
import { DeviceRegistrationPage } from './pages/DeviceRegistrationPage';
import { LoginPage } from './pages/LoginPage';

/** Trang chỉ dành cho người chưa đăng nhập. */
export const guestAuthRoutes: RouteObject[] = [
  { path: ROUTES.login, element: <LoginPage /> },
  { path: ROUTES.counter, element: <CounterPage /> },
];

/** Trang dùng được cả khi chưa đăng nhập lẫn khi quản lý đã đăng nhập. */
export const openAuthRoutes: RouteObject[] = [
  { path: ROUTES.counterRegister, element: <DeviceRegistrationPage /> },
];
