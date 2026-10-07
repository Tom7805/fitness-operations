# Mã lỗi API

Mọi lỗi trả về cùng một cấu trúc JSON:

```json
{
  "status": 423,
  "code": "ACCOUNT_TEMPORARILY_LOCKED",
  "message": "Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau 15 phút.",
  "details": { "lockedUntil": "2026-10-08T03:15:00Z", "retryAfterSeconds": 900 },
  "fieldErrors": [],
  "path": "/api/v1/auth/login",
  "timestamp": "2026-10-08T03:00:00Z",
  "traceId": "6f1c2a3b4d5e6f70"
}
```

- `code` là mã ổn định để giao diện xử lý; `message` là câu thông báo tiếng Việt hiển thị được ngay cho người dùng.
- `details` chỉ có ở một số mã (ghi rõ bên dưới). `fieldErrors` chỉ có với `VALIDATION_ERROR`.
- `traceId` trùng với header `X-Request-Id` của phản hồi, dùng để tra log máy chủ.

## Chung

| HTTP | `code` | Ý nghĩa |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Dữ liệu gửi lên không hợp lệ; xem `fieldErrors[{field, message}]` |
| 400 | `MALFORMED_REQUEST` | Thân yêu cầu không đọc được (JSON sai, sai kiểu) |
| 401 | `UNAUTHORIZED` | Chưa đăng nhập |
| 403 | `FORBIDDEN` | Đã đăng nhập nhưng vai trò không có quyền dùng chức năng |
| 404 | `NOT_FOUND` | Không tìm thấy tài nguyên |
| 405 | `METHOD_NOT_ALLOWED` | Sai phương thức HTTP |
| 500 | `INTERNAL_ERROR` | Lỗi không mong muốn; nội dung chi tiết chỉ ghi ở log máy chủ |

## Đăng nhập và phiên — `NCL-01-CN-001`

| HTTP | `code` | Thông báo | `details` |
|---|---|---|---|
| 401 | `INVALID_CREDENTIALS` | Tên đăng nhập hoặc mật khẩu không đúng. | — |
| 423 | `ACCOUNT_TEMPORARILY_LOCKED` | Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau N phút. | `lockedUntil`, `retryAfterSeconds` (kèm header `Retry-After`) |
| 403 | `ACCOUNT_DISABLED` | Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên. | — |
| 403 | `DEVICE_NOT_REGISTERED` | Máy chưa được đăng ký với câu lạc bộ. Vui lòng nhờ quản lý câu lạc bộ đăng nhập để đăng ký máy trước khi dùng. | — |
| 403 | `DEVICE_BRANCH_NOT_ASSIGNED` | Tài khoản không được giao làm việc tại câu lạc bộ của máy quầy này. | `branchName` |
| 401 | `SESSION_EXPIRED` | Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại. | — |
| 401 | `SESSION_INVALID` | Phiên làm việc không hợp lệ hoặc đã kết thúc. Vui lòng đăng nhập lại. | — |

## Máy quầy — `NCL-01-CN-001`

| HTTP | `code` | Thông báo |
|---|---|---|
| 403 | `BRANCH_NOT_IN_SCOPE` | Bạn không được giao quản lý câu lạc bộ này. |
| 404 | `BRANCH_NOT_FOUND` | Không tìm thấy câu lạc bộ hoặc câu lạc bộ đã ngừng hoạt động. |
| 409 | `DEVICE_NAME_DUPLICATE` | Câu lạc bộ đã có máy quầy mang tên này. |
