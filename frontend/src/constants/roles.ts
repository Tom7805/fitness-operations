/** Mã vai trò nhân viên, khớp `roles.code` ở máy chủ. */
export const ROLE_CODES = [
  'CHAIN_OWNER',
  'CLUB_MANAGER',
  'SALES_CONSULTANT',
  'RECEPTIONIST',
  'PERSONAL_TRAINER',
  'GROUP_TRAINER',
  'ACCOUNTANT',
  'TECHNICIAN',
  'MEMBER_CARE',
  'ADMIN',
] as const;

export type RoleCode = (typeof ROLE_CODES)[number];

/** Vai trò được đăng ký máy quầy với câu lạc bộ. */
export const DEVICE_REGISTRAR_ROLES: readonly RoleCode[] = ['CLUB_MANAGER', 'ADMIN'];
