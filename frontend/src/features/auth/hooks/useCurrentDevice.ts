import { useQuery } from '@tanstack/react-query';
import { useEffect } from 'react';
import { queryKeys } from '@/constants/queryKeys';
import type { ApiError } from '@/services/http/apiError';
import { tokenStorage } from '@/services/storage/tokenStorage';
import { authApi } from '../api/authApi';
import type { CurrentDevice } from '../types/auth.types';

const NOT_REGISTERED: CurrentDevice = { registered: false };

/**
 * Máy đang dùng có phải máy quầy đã đăng ký không. Máy chưa từng được đăng ký (không có mã máy quầy) thì trả lời
 * ngay mà không cần hỏi máy chủ; mã máy quầy đã bị thu hồi thì được xóa khỏi trình duyệt.
 */
export function useCurrentDevice() {
  const hasDeviceToken = tokenStorage.getDeviceToken() !== null;
  const query = useQuery<CurrentDevice, ApiError>({
    queryKey: queryKeys.devices.current,
    queryFn: hasDeviceToken ? authApi.currentDevice : () => Promise.resolve(NOT_REGISTERED),
    staleTime: 5 * 60 * 1000,
  });

  useEffect(() => {
    if (query.data && !query.data.registered && tokenStorage.getDeviceToken() !== null) {
      tokenStorage.clearDeviceToken();
    }
  }, [query.data]);

  return query;
}
