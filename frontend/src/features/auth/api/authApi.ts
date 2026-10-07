import { API_ENDPOINTS } from '@/constants/apiEndpoints';
import { httpClient } from '@/services/http/httpClient';
import type {
  AuthResponse,
  Branch,
  CurrentDevice,
  DeviceRegistrationPayload,
  DeviceRegistrationResult,
  LoginPayload,
  Session,
} from '../types/auth.types';

export const authApi = {
  login: (payload: LoginPayload) =>
    httpClient
      .post<AuthResponse>(API_ENDPOINTS.auth.login, payload, { skipAuth: true })
      .then((response) => response.data),

  logout: () => httpClient.post<void>(API_ENDPOINTS.auth.logout).then(() => undefined),

  me: () => httpClient.get<Session>(API_ENDPOINTS.auth.me).then((response) => response.data),

  currentDevice: () =>
    httpClient
      .get<CurrentDevice>(API_ENDPOINTS.devices.current, { skipAuth: true })
      .then((response) => response.data),

  registrableBranches: () =>
    httpClient
      .get<Branch[]>(API_ENDPOINTS.devices.registrableBranches)
      .then((response) => response.data),

  registerDevice: (payload: DeviceRegistrationPayload) =>
    httpClient
      .post<DeviceRegistrationResult>(API_ENDPOINTS.devices.register, payload)
      .then((response) => response.data),
};
