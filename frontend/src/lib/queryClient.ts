import { QueryClient } from '@tanstack/react-query';
import { isApiError } from '@/services/http/apiError';
import { useAuthStore } from '@/store/authStore';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      refetchOnWindowFocus: false,
      // Lỗi 4xx là kết quả nghiệp vụ, thử lại không đổi được kết quả.
      retry: (failureCount, error) =>
        !(isApiError(error) && error.status >= 400 && error.status < 500) && failureCount < 2,
    },
    mutations: { retry: false },
  },
});

// Đăng xuất (chủ động hoặc do hết phiên) thì bỏ toàn bộ dữ liệu đã đệm của người dùng trước.
useAuthStore.subscribe((state, previous) => {
  if (previous.accessToken && !state.accessToken) {
    queryClient.clear();
  }
});
