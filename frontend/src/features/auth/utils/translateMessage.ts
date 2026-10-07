import type { TFunction } from 'i18next';

/** Thông báo kiểm tra phía giao diện là khóa i18n; thông báo từ máy chủ đã là câu tiếng Việt hoàn chỉnh. */
export function translateMessage(t: TFunction, message: string | undefined): string | undefined {
  if (!message) {
    return undefined;
  }
  return message.startsWith('validation:') ? t(message) : message;
}
