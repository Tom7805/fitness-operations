import { isAxiosError } from 'axios';
import type { ApiErrorBody, FieldError } from '@/types/api';

/** Mã lỗi phía giao diện khi không nhận được phản hồi hợp lệ từ máy chủ. */
export const NETWORK_ERROR = 'NETWORK_ERROR';

/** Lỗi API đã chuẩn hóa: luôn có `code` ổn định và `message` hiển thị được cho người dùng. */
export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: Record<string, unknown>;
  readonly fieldErrors: FieldError[];
  readonly traceId?: string;

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.name = 'ApiError';
    this.status = body.status;
    this.code = body.code;
    this.details = body.details ?? {};
    this.fieldErrors = body.fieldErrors ?? [];
    this.traceId = body.traceId;
  }

  static from(error: unknown): ApiError {
    if (error instanceof ApiError) {
      return error;
    }
    if (isAxiosError(error) && error.response && isApiErrorBody(error.response.data)) {
      return new ApiError(error.response.data);
    }
    if (isAxiosError(error) && error.response) {
      return new ApiError({
        status: error.response.status,
        code: 'UNEXPECTED_RESPONSE',
        message: 'Hệ thống gặp lỗi. Vui lòng thử lại sau.',
      });
    }
    return new ApiError({
      status: 0,
      code: NETWORK_ERROR,
      message: 'Không kết nối được máy chủ. Vui lòng kiểm tra mạng rồi thử lại.',
    });
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

function isApiErrorBody(data: unknown): data is ApiErrorBody {
  return (
    typeof data === 'object' &&
    data !== null &&
    typeof (data as ApiErrorBody).code === 'string' &&
    typeof (data as ApiErrorBody).message === 'string'
  );
}
