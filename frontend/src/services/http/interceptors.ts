import { AxiosHeaders, type AxiosInstance } from 'axios';
import { useAuthStore } from '@/store/authStore';
import { tokenStorage } from '@/services/storage/tokenStorage';
import { ApiError } from './apiError';
import { serverActivity } from './serverActivity';

const AUTHORIZATION = 'Authorization';
const DEVICE_TOKEN = 'X-Device-Token';
const SESSION_ENDED_CODES = new Set(['SESSION_EXPIRED', 'SESSION_INVALID', 'UNAUTHORIZED']);

declare module 'axios' {
  interface AxiosRequestConfig {
    /** Không gửi mã truy cập (yêu cầu đăng nhập). */
    skipAuth?: boolean;
  }
}

/**
 * - Gửi kèm mã truy cập và mã máy quầy (nếu máy là máy quầy) cho mọi yêu cầu.
 * - Chuẩn hóa mọi lỗi thành {@link ApiError}.
 * - Phiên hết hạn hoặc không hợp lệ: đăng xuất và lưu lý do để trang đăng nhập hiển thị.
 */
export function attachInterceptors(client: AxiosInstance): void {
  client.interceptors.request.use((config) => {
    const headers = AxiosHeaders.from(config.headers);
    const accessToken = useAuthStore.getState().accessToken;
    if (accessToken && !config.skipAuth) {
      headers.set(AUTHORIZATION, `Bearer ${accessToken}`);
    }
    const deviceToken = tokenStorage.getDeviceToken();
    if (deviceToken) {
      headers.set(DEVICE_TOKEN, deviceToken);
    }
    config.headers = headers;
    return config;
  });

  client.interceptors.response.use(
    (response) => {
      if (AxiosHeaders.from(response.config.headers).has(AUTHORIZATION)) {
        serverActivity.mark();
      }
      return response;
    },
    (error: unknown) => {
      const apiError = ApiError.from(error);
      const sentToken =
        typeof error === 'object' &&
        error !== null &&
        'config' in error &&
        AxiosHeaders.from((error as { config?: { headers?: AxiosHeaders } }).config?.headers).has(
          AUTHORIZATION,
        );
      if (sentToken && apiError.status === 401 && SESSION_ENDED_CODES.has(apiError.code)) {
        useAuthStore.getState().signOut({
          reason: apiError.code === 'SESSION_EXPIRED' ? 'expired' : 'invalid',
          message: apiError.message,
        });
      }
      return Promise.reject(apiError);
    },
  );
}
