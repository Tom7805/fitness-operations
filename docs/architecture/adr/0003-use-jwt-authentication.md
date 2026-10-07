# ADR 0003 — Xác thực bằng JWT gắn với phiên lưu ở máy chủ

- **Trạng thái**: Chấp nhận
- **Ngày**: 2026-10-08
- **Liên quan**: `NCL-01-CN-001` Đăng nhập hệ thống, `NCL-01-CN-002` Quản lý tài khoản, `QTN-01`, `QTN-02`

## Bối cảnh

Đăng nhập phải đáp ứng đồng thời:

1. Phiên **hết hạn sau 30 phút không thao tác** (`NCL-01-CN-001`).
2. **Khóa tài khoản thì mọi phiên đang mở bị chấm dứt ngay** (`NCL-01-CN-002`).
3. Phiên trên máy quầy **gắn với đúng máy quầy** đã đăng ký; mã mang sang máy khác phải vô hiệu.
4. Thay đổi vai trò hoặc phạm vi câu lạc bộ **có hiệu lực ở lần gọi tiếp theo** (`NCL-01-CN-004`).

Một JWT thuần không trạng thái không làm được (1), (2), (4): mã đã cấp còn hiệu lực tới khi hết hạn và mang theo quyền
tại thời điểm cấp.

## Quyết định

- Đăng nhập tạo một dòng `user_sessions` ở máy chủ và trả về **JWT ký HMAC-SHA256** chỉ chứa mã phiên (`sid`),
  mã tài khoản (`sub`), `iss`, `iat`, `exp` (thời hạn tuyệt đối 12 giờ). JWT không chứa vai trò hay phạm vi.
- Mỗi yêu cầu: kiểm chữ ký và hạn của JWT, rồi đọc phiên và tài khoản từ cơ sở dữ liệu để kiểm tra phiên còn mở,
  chưa quá 30 phút không thao tác, đúng máy quầy, tài khoản còn hoạt động; sau đó cập nhật lần thao tác cuối.
  Vai trò và phạm vi luôn được đọc mới.
- Mã gửi trong header `Authorization: Bearer`, không dùng cookie nên không cần chống CSRF. Giao diện giữ mã trong
  `sessionStorage` (mất khi đóng tab — phù hợp máy quầy dùng chung).
- Máy quầy được nhận diện bằng mã máy quầy 256 bit ngẫu nhiên trong header `X-Device-Token`; máy chủ chỉ lưu SHA-256.
- Mật khẩu băm bằng BCrypt cost 12.

## Hệ quả

- **Được**: hết phiên theo thời gian không thao tác, thu hồi phiên tức thì, phiên gắn máy quầy, quyền luôn mới nhất;
  JWT ký số ngăn giả mạo mã phiên.
- **Phải trả**: mỗi yêu cầu có thêm vài truy vấn và một lệnh cập nhật lần thao tác cuối. Ở quy mô một chuỗi phòng tập
  vừa và nhỏ chi phí này không đáng kể; khi cần có thể đệm thông tin phiên trong Redis.
- Không có mã làm mới (refresh token): phiên tự gia hạn theo thao tác, tối đa 12 giờ rồi phải đăng nhập lại.
