/** Lỗi của một trường dữ liệu. */
export interface FieldError {
  field: string;
  message: string;
}

/** Cấu trúc lỗi thống nhất của API (docs/api/error-codes.md). */
export interface ApiErrorBody {
  status: number;
  code: string;
  message: string;
  details?: Record<string, unknown>;
  fieldErrors?: FieldError[];
  path?: string;
  timestamp?: string;
  traceId?: string;
}
