import type { RouteObject } from 'react-router';
import { guestAuthRoutes, openAuthRoutes } from '@/features/auth';
import { AuthLayout, MainLayout } from '@/layouts';
import { ForbiddenPage } from '@/pages/ForbiddenPage';
import { HomePage } from '@/pages/HomePage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { ROUTES } from '@/constants/routes';
import { GuestRoute } from './GuestRoute';
import { ProtectedRoute } from './ProtectedRoute';

export const routes: RouteObject[] = [
  {
    element: <AuthLayout />,
    children: [{ element: <GuestRoute />, children: guestAuthRoutes }, ...openAuthRoutes],
  },
  {
    element: <ProtectedRoute />,
    children: [
      { element: <MainLayout />, children: [{ path: ROUTES.home, element: <HomePage /> }] },
    ],
  },
  { path: ROUTES.forbidden, element: <ForbiddenPage /> },
  { path: '*', element: <NotFoundPage /> },
];
