import { useMutation, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/constants/queryKeys';
import type { ApiError } from '@/services/http/apiError';
import { serverActivity } from '@/services/http/serverActivity';
import { useAuthStore } from '@/store/authStore';
import { authApi } from '../api/authApi';
import type { AuthResponse, LoginPayload } from '../types/auth.types';

export function useLogin() {
  const signIn = useAuthStore((state) => state.signIn);
  const queryClient = useQueryClient();

  return useMutation<AuthResponse, ApiError, LoginPayload>({
    mutationFn: authApi.login,
    onSuccess: (response) => {
      serverActivity.mark();
      signIn(response.accessToken, response.session);
      queryClient.setQueryData(queryKeys.auth.me, response.session);
    },
  });
}
