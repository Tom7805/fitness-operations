import type { RoleCode } from '@/constants/roles';
import { useAuthStore } from '@/store/authStore';

/** Phiên hiện tại và các câu hỏi thường gặp về quyền của người dùng. */
export function useAuth() {
  const session = useAuthStore((state) => state.session);
  const accessToken = useAuthStore((state) => state.accessToken);
  const roleCodes = session?.user.roles.map((role) => role.code) ?? [];

  return {
    session,
    user: session?.user ?? null,
    isAuthenticated: accessToken !== null,
    roleCodes,
    hasAnyRole: (roles: readonly RoleCode[]) => roleCodes.some((code) => roles.includes(code)),
  };
}
