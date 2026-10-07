/**
 * Bọc Web Storage an toàn: chế độ duyệt riêng tư hoặc trình duyệt chặn lưu trữ sẽ ném lỗi khi truy cập,
 * lúc đó hệ thống vẫn chạy được (chỉ không ghi nhớ được giá trị).
 */
export interface SafeStorage {
  get(key: string): string | null;
  set(key: string, value: string): void;
  remove(key: string): void;
}

export function createSafeStorage(getStorage: () => Storage): SafeStorage {
  return {
    get(key) {
      try {
        return getStorage().getItem(key);
      } catch {
        return null;
      }
    },
    set(key, value) {
      try {
        getStorage().setItem(key, value);
      } catch {
        // Bỏ qua: không lưu được thì giá trị chỉ sống trong bộ nhớ.
      }
    },
    remove(key) {
      try {
        getStorage().removeItem(key);
      } catch {
        // Bỏ qua.
      }
    },
  };
}

export const safeLocalStorage = createSafeStorage(() => window.localStorage);
export const safeSessionStorage = createSafeStorage(() => window.sessionStorage);
