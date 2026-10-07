import { safeLocalStorage, safeSessionStorage } from './localStorage';

const ACCESS_TOKEN_KEY = 'fo.accessToken';
const DEVICE_TOKEN_KEY = 'fo.counterDeviceToken';

/**
 * Mã truy cập nằm trong sessionStorage: mất khi đóng tab, phù hợp máy quầy dùng chung.
 * Mã máy quầy nằm trong localStorage: gắn lâu dài với trình duyệt của máy quầy đã đăng ký.
 */
export const tokenStorage = {
  getAccessToken: () => safeSessionStorage.get(ACCESS_TOKEN_KEY),
  setAccessToken: (token: string) => safeSessionStorage.set(ACCESS_TOKEN_KEY, token),
  clearAccessToken: () => safeSessionStorage.remove(ACCESS_TOKEN_KEY),

  getDeviceToken: () => safeLocalStorage.get(DEVICE_TOKEN_KEY),
  setDeviceToken: (token: string) => safeLocalStorage.set(DEVICE_TOKEN_KEY, token),
  clearDeviceToken: () => safeLocalStorage.remove(DEVICE_TOKEN_KEY),
};
