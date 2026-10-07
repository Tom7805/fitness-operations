import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { queryKeys } from '@/constants/queryKeys';
import type { ApiError } from '@/services/http/apiError';
import { useAuthStore } from '@/store/authStore';
import { authApi } from '../api/authApi';

/**
 * Tải lại phiên khi đã có mã truy cập nhưng chưa có thông tin phiên (vd. tải lại trang). Phiên đã hết ở máy
 * chủ thì bộ chặn HTTP đăng xuất, người dùng được đưa về trang đăng nhập kèm lý do.
 */
export function useSessionBootstrap() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const session = useAuthStore((state) => state.session);
  const setSession = useAuthStore((state) => state.setSession);

  const query = useQuery({
    queryKey: queryKeys.auth.me,
    queryFn: authApi.me,
    enabled: accessToken !== null && session === null,
    staleTime: Infinity,
    retry: false,
  });

  useEffect(() => {
    if (query.data && accessToken !== null && session === null) {
      setSession(query.data);
    }
  }, [query.data, accessToken, session, setSession]);

  return {
    accessToken,
    session,
    isLoading: accessToken !== null && session === null && !query.isError,
    error: (query.error as ApiError | null) ?? null,
    retry: () => void query.refetch(),
  };
}
