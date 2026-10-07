import type { RoleCode } from '@/constants/roles';

/** Loại thiết bị của phiên. COUNTER do máy chủ xác định qua mã máy quầy. */
export type ClientType = 'COUNTER' | 'OFFICE' | 'MOBILE';

export interface Branch {
  id: number;
  code: string;
  name: string;
}

export interface RoleSummary {
  code: RoleCode;
  name: string;
}

export interface CounterDevice {
  id: number;
  name: string;
  branch?: Branch;
  registeredAt: string;
}

export interface SessionUser {
  id: number;
  username: string;
  fullName: string;
  jobTitle?: string;
  roles: RoleSummary[];
  allBranches: boolean;
  branches: Branch[];
  mustChangePassword: boolean;
}

/** Phiên làm việc hiện tại (GET /auth/me). */
export interface Session {
  sessionId: string;
  clientType: ClientType;
  startedAt: string;
  expiresAt: string;
  idleTimeoutSeconds: number;
  user: SessionUser;
  activeBranch?: Branch;
  device?: CounterDevice;
}
