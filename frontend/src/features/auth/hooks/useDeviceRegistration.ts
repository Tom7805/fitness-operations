import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '@/constants/queryKeys';
import type { ApiError } from '@/services/http/apiError';
import { tokenStorage } from '@/services/storage/tokenStorage';
import { authApi } from '../api/authApi';
import type {
  Branch,
  CurrentDevice,
  DeviceRegistrationPayload,
  DeviceRegistrationResult,
} from '../types/auth.types';

export function useRegistrableBranches(enabled: boolean) {
  return useQuery<Branch[], ApiError>({
    queryKey: queryKeys.devices.registrableBranches,
    queryFn: authApi.registrableBranches,
    enabled,
  });
}

/** Đăng ký máy quầy và lưu mã máy quầy vào trình duyệt của máy (mã chỉ được trả về một lần). */
export function useRegisterDevice() {
  const queryClient = useQueryClient();
  return useMutation<DeviceRegistrationResult, ApiError, DeviceRegistrationPayload>({
    mutationFn: authApi.registerDevice,
    onSuccess: (result) => {
      tokenStorage.setDeviceToken(result.deviceToken);
      queryClient.setQueryData<CurrentDevice>(queryKeys.devices.current, {
        registered: true,
        device: result.device,
      });
    },
  });
}
