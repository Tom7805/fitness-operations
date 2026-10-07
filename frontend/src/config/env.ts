/** Cấu hình đọc từ biến môi trường lúc build. */
export const env = {
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/api/v1',
} as const;
