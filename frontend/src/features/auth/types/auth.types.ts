import type { CounterDevice, Session } from '@/types/auth';

export type {
  Branch,
  ClientType,
  CounterDevice,
  RoleSummary,
  Session,
  SessionUser,
} from '@/types/auth';

export interface LoginPayload {
  username: string;
  password: string;
  /** Máy quầy do máy chủ tự nhận biết qua mã máy quầy; giao diện chỉ phân biệt máy tính và điện thoại. */
  clientType: 'OFFICE' | 'MOBILE';
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  session: Session;
}

export interface CurrentDevice {
  registered: boolean;
  device?: CounterDevice;
}

export interface DeviceRegistrationPayload {
  branchId: number;
  name: string;
}

export interface DeviceRegistrationResult {
  device: CounterDevice;
  /** Chỉ trả về một lần; trình duyệt của máy phải lưu lại. */
  deviceToken: string;
}

/** Thông tin chuyển từ trang đăng ký máy về trang máy quầy để báo đăng ký thành công. */
export interface DeviceRegisteredState {
  registeredDevice: { name: string; branchName: string };
}
