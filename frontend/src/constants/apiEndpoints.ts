/** Đường dẫn API, tương đối với VITE_API_BASE_URL. */
export const API_ENDPOINTS = {
  auth: {
    login: '/auth/login',
    logout: '/auth/logout',
    me: '/auth/me',
  },
  devices: {
    current: '/devices/current',
    registrableBranches: '/devices/registrable-branches',
    register: '/devices',
  },
} as const;
