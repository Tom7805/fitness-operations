import { create } from 'zustand';
import { tokenStorage } from '@/services/storage/tokenStorage';
import type { Session } from '@/types/auth';

/** Lý do người dùng bị đưa về trang đăng nhập, để trang đăng nhập hiển thị đúng thông báo. */
export interface SessionNotice {
  reason: 'expired' | 'invalid' | 'logout';
  message: string;
}

interface AuthState {
  accessToken: string | null;
  session: Session | null;
  notice: SessionNotice | null;
  signIn: (accessToken: string, session: Session) => void;
  setSession: (session: Session) => void;
  signOut: (notice?: SessionNotice) => void;
  clearNotice: () => void;
}

export const useAuthStore = create<AuthState>()((set) => ({
  accessToken: tokenStorage.getAccessToken(),
  session: null,
  notice: null,
  signIn: (accessToken, session) => {
    tokenStorage.setAccessToken(accessToken);
    set({ accessToken, session, notice: null });
  },
  setSession: (session) => set({ session }),
  signOut: (notice) => {
    tokenStorage.clearAccessToken();
    set({ accessToken: null, session: null, notice: notice ?? null });
  },
  clearNotice: () => set({ notice: null }),
}));
