/** Cảnh báo sắp hết phiên trước bao nhiêu giây (thiết kế S7). */
export const SESSION_WARNING_SECONDS = 60;

/**
 * Khi người dùng còn thao tác trên giao diện mà lâu chưa gọi máy chủ, gửi một yêu cầu giữ phiên sau khoảng này,
 * để máy chủ — bên quyết định hết phiên — biết người dùng vẫn đang làm việc.
 */
export const KEEP_ALIVE_INTERVAL_MS = 5 * 60 * 1000;

/** Thời gian chờ tối đa của một yêu cầu API. */
export const HTTP_TIMEOUT_MS = 15_000;
