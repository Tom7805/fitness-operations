import type { Session } from '@/types/auth';

/** Dữ liệu kiểm thử bám theo dữ liệu mẫu dev (R__seed_dev_sample_data.sql). */
export const BRANCH_CAU_GIAY = { id: 1, code: 'CLB-CG', name: 'Fitness Cầu Giấy' };
export const BRANCH_HAI_BA_TRUNG = { id: 2, code: 'CLB-HBT', name: 'Fitness Hai Bà Trưng' };

export const COUNTER_DEVICE = {
  id: 7,
  name: 'Quầy lễ tân 1',
  branch: BRANCH_CAU_GIAY,
  registeredAt: '2026-10-08T01:00:00Z',
};

export const DEVICE_TOKEN = 'ma-may-quay-kiem-thu';
export const ACCESS_TOKEN = 'ma-truy-cap-kiem-thu';

export const receptionistSession: Session = {
  sessionId: '0b8f8f62-7d3c-4d8a-9a52-6d7f1c0f0001',
  clientType: 'COUNTER',
  startedAt: '2026-10-08T01:00:00Z',
  expiresAt: '2026-10-08T13:00:00Z',
  idleTimeoutSeconds: 1800,
  user: {
    id: 4,
    username: 'letan.caugiay',
    fullName: 'Phạm Thu Hà',
    jobTitle: 'Lễ tân',
    roles: [{ code: 'RECEPTIONIST', name: 'Lễ tân' }],
    allBranches: false,
    branches: [BRANCH_CAU_GIAY],
    mustChangePassword: false,
  },
  activeBranch: BRANCH_CAU_GIAY,
  device: COUNTER_DEVICE,
};

export const managerSession: Session = {
  sessionId: '0b8f8f62-7d3c-4d8a-9a52-6d7f1c0f0002',
  clientType: 'OFFICE',
  startedAt: '2026-10-08T01:00:00Z',
  expiresAt: '2026-10-08T13:00:00Z',
  idleTimeoutSeconds: 1800,
  user: {
    id: 3,
    username: 'quanly.caugiay',
    fullName: 'Lê Thị Quản Lý',
    jobTitle: 'Quản lý câu lạc bộ',
    roles: [{ code: 'CLUB_MANAGER', name: 'Quản lý câu lạc bộ' }],
    allBranches: false,
    branches: [BRANCH_CAU_GIAY],
    mustChangePassword: false,
  },
  activeBranch: BRANCH_CAU_GIAY,
};

export const errorBody = (status: number, code: string, message: string, details?: object) => ({
  status,
  code,
  message,
  details,
  path: '/api/v1/auth/login',
  timestamp: '2026-10-08T01:00:00Z',
  traceId: 'abc123def456',
});

export const LOCKED_MESSAGE =
  'Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau 15 phút.';
export const INVALID_CREDENTIALS_MESSAGE = 'Tên đăng nhập hoặc mật khẩu không đúng.';
export const DEVICE_NOT_REGISTERED_MESSAGE =
  'Máy chưa được đăng ký với câu lạc bộ. Vui lòng nhờ quản lý câu lạc bộ đăng nhập để đăng ký máy trước khi dùng.';
