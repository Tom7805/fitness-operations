# NCL-01-CN-001 — Đăng nhập hệ thống · Báo cáo kiểm thử

> **Công việc**: `NCL-01-CN-001-CV-05` · 🟩 `FE-QA` · Jira `VHPT-18`
> **Ngày chạy**: 08/10/2026 · **Môi trường**: Windows 11, Chrome (Playwright), MySQL 8.0.46 cài trên máy (không Docker),
> backend Spring Boot 3.5 profile `dev`, frontend Vite 8 · **Dữ liệu**: `R__seed_dev_sample_data.sql`, mật khẩu chung `Fitness@2026`

## 1. Kết quả tổng hợp

| Lớp kiểm thử | Công cụ | Số ca | Đạt |
|---|---|---|---|
| API + cơ sở dữ liệu (máy chủ) | JUnit 5, MockMvc trên MySQL 8.0 thật (database `fitness_operations_test`) | 32 | **32** |
| Giao diện (component + luồng trang, API giả lập) | Vitest, Testing Library, MSW | 25 | **25** |
| Đầu-cuối trên hệ thống thật, 1280 px và 360 px | Playwright + Chrome | 10 | **10** |
| Lint, kiểm tra kiểu, build | ESLint, `tsc -b`, `vite build`, `mvnw verify` | — | Sạch |

## 2. Tiêu chí chấp nhận

| Tiêu chí | Mức | Kết quả | Bằng chứng |
|---|---|---|---|
| `TC-01` Lễ tân đăng nhập đúng trên máy quầy đã đăng ký → trang chính theo vai trò lễ tân, phiên gắn với máy quầy | Cao | ✅ Đạt | E2E `TC-03 → TC-01` (truy vấn `user_sessions`: `COUNTER`, đúng máy, đúng câu lạc bộ); `LoginPage.test.tsx`; `AuthIntegrationTest$Tc01` |
| `TC-02` Sai mật khẩu 5 lần liên tiếp → lần thứ 6 tạm khóa 15 phút, báo thời gian chờ | Cao | ✅ Đạt | E2E `TC-02` (giao diện đếm ngược + API trả `423`, `Retry-After` > 14 phút, CSDL `failed_login_attempts = 5`); `LoginForm.test.tsx`; `AuthIntegrationTest$Tc02` (kể cả hết khóa đúng giây thứ 900) |
| `TC-03` Máy tính bảng mới chưa đăng ký → yêu cầu quản lý đăng nhập để đăng ký máy | Cao | ✅ Đạt | E2E `TC-03 → TC-01`, E2E `TC-03` lễ tân bị từ chối; `CounterPage.test.tsx`, `DeviceRegistrationPage.test.tsx`; `AuthIntegrationTest$Tc03`, `CounterDeviceIntegrationTest` |
| `TC-04` Đăng nhập thành công hoặc thất bại đều ghi người thực hiện, nội dung, thời điểm | Cao | ✅ Đạt | E2E `TC-04` (đối chiếu dòng nhật ký + thử `DELETE` bị từ chối); `AuthIntegrationTest$Tc04` (thời điểm chính xác, `UPDATE`/`DELETE` bị trigger chặn) |

## 3. Dữ liệu kiểm thử đã dùng

| Tài khoản | Vai trò | Dùng cho |
|---|---|---|
| `quanly.caugiay` | Quản lý câu lạc bộ | Đăng ký máy quầy (TC-03) |
| `letan.caugiay` | Lễ tân, Fitness Cầu Giấy | Đăng nhập trên máy quầy (TC-01) |
| `letan.haibatrung` | Lễ tân, Fitness Hai Bà Trưng | Bị từ chối trên máy chưa đăng ký (TC-03) |
| `ketoan` (1280 px), `kythuat.caugiay` (360 px) | Kế toán, kỹ thuật | 5 lần sai + lần thứ 6 (TC-02) |
| `tuvan.caugiay` | Nhân viên tư vấn | 1 lần sai + 1 lần đúng (TC-04) |
| `hlv.caugiay` | Huấn luyện viên cá nhân | Đăng nhập trên máy tính / điện thoại |

Máy quầy được tạo mới mỗi lượt chạy với tên `Quầy E2E <kích thước> <số>`. Trước và sau khi chạy, kịch bản gỡ tạm khóa
các tài khoản mẫu để có thể chạy lại ngay.

## 4. Nhật ký trong cơ sở dữ liệu sau lượt chạy E2E (TC-04)

Truy vấn trong MySQL (thời điểm lưu theo UTC, đổi sang giờ Việt Nam để đọc):

```text
+----+-----------+----------------------------+--------------------------+----------------+-------------+------------------------------------------------------------------------------------------------------------------------------------------------+
| id | thoi_diem | event_type                 | ly_do                    | username       | client_type | detail                                                                                                                                         |
+----+-----------+----------------------------+--------------------------+----------------+-------------+------------------------------------------------------------------------------------------------------------------------------------------------+
|  1 | 05:40:05  | LOGIN_REJECTED             | DEVICE_NOT_REGISTERED    | letan.caugiay  | OFFICE      | Từ chối đăng nhập: vai trò chỉ được đăng nhập trên máy quầy đã đăng ký, máy đang dùng chưa được đăng ký                                        |
|  3 | 05:40:16  | LOGIN_SUCCEEDED            |                          | quanly.caugiay | OFFICE      | Đăng nhập thành công trên máy tính văn phòng                                                                                                   |
|  4 | 05:40:16  | DEVICE_REGISTERED          |                          | quanly.caugiay | OFFICE      | Đăng ký máy quầy "Quầy E2E desktop-1280 16532" cho Fitness Cầu Giấy                                                                            |
|  5 | 05:40:16  | LOGOUT                     |                          | quanly.caugiay | OFFICE      | Đăng xuất khỏi máy tính văn phòng                                                                                                              |
|  6 | 05:40:17  | LOGIN_SUCCEEDED            |                          | letan.caugiay  | COUNTER     | Đăng nhập thành công trên máy quầy "Quầy E2E desktop-1280 16532" (Fitness Cầu Giấy)                                                            |
|  7 | 05:40:18  | LOGOUT                     |                          | letan.caugiay  | COUNTER     | Đăng xuất khỏi máy quầy                                                                                                                        |
|  9 | 05:40:21  | LOGIN_FAILED               | INVALID_PASSWORD         | ketoan         | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 1/5 liên tiếp)                                                                                           |
| 10 | 05:40:21  | LOGIN_FAILED               | INVALID_PASSWORD         | ketoan         | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 2/5 liên tiếp)                                                                                           |
| 11 | 05:40:22  | LOGIN_FAILED               | INVALID_PASSWORD         | ketoan         | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 3/5 liên tiếp)                                                                                           |
| 12 | 05:40:22  | LOGIN_FAILED               | INVALID_PASSWORD         | ketoan         | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 4/5 liên tiếp)                                                                                           |
| 13 | 05:40:23  | LOGIN_FAILED               | INVALID_PASSWORD         | ketoan         | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 5/5 liên tiếp)                                                                                           |
| 14 | 05:40:23  | ACCOUNT_TEMPORARILY_LOCKED | TOO_MANY_FAILED_ATTEMPTS | ketoan         | OFFICE      | Tạm khóa đăng nhập 15 phút đến 05:55 08/10/2026 sau 5 lần nhập sai mật khẩu liên tiếp                                                          |
| 15 | 05:40:23  | LOGIN_REJECTED             | ACCOUNT_LOCKED           | ketoan         | OFFICE      | Từ chối đăng nhập: tài khoản đang tạm khóa đến 05:55 08/10/2026                                                                                |
| 16 | 05:40:25  | LOGIN_FAILED               | INVALID_PASSWORD         | tuvan.caugiay  | OFFICE      | Đăng nhập thất bại: sai mật khẩu (lần 1/5 liên tiếp)                                                                                           |
| 17 | 05:40:25  | LOGIN_SUCCEEDED            |                          | tuvan.caugiay  | OFFICE      | Đăng nhập thành công trên máy tính văn phòng                                                                                                   |
| 19 | 05:40:30  | LOGIN_SUCCEEDED            |                          | quanly.caugiay | MOBILE      | Đăng nhập thành công trên điện thoại                                                                                                           |
+----+-----------+----------------------------+--------------------------+----------------+-------------+------------------------------------------------------------------------------------------------------------------------------------------------+
```

Mỗi dòng còn lưu mã tài khoản, phiên, máy quầy, câu lạc bộ, địa chỉ IP và trình duyệt. Lệnh xóa bị trigger từ chối:

```text
mysql> DELETE FROM auth_audit_logs WHERE id = 1;
ERROR 1644 (45000): auth_audit_logs chỉ cho phép thêm mới, không được DELETE (QTN-02)
```

## 5. Ảnh chụp luồng chính

| Bước | 1280 px | 360 px |
|---|---|---|
| S1/S2 Đăng nhập | ![](screenshots/desktop-1280-01-dang-nhap.png) | ![](screenshots/mobile-360-01-dang-nhap.png) |
| S6 Trang chính huấn luyện viên | ![](screenshots/desktop-1280-02-trang-chinh-hlv.png) | ![](screenshots/mobile-360-02-trang-chinh-hlv.png) |
| S4 Máy quầy chưa đăng ký (TC-03) | ![](screenshots/desktop-1280-03-may-quay-chua-dang-ky.png) | ![](screenshots/mobile-360-03-may-quay-chua-dang-ky.png) |
| S5 Đăng ký máy — bước 2 | ![](screenshots/desktop-1280-04-dang-ky-may-buoc-2.png) | ![](screenshots/mobile-360-04-dang-ky-may-buoc-2.png) |
| S3 Đăng nhập tại quầy | ![](screenshots/desktop-1280-05-dang-nhap-tai-quay.png) | ![](screenshots/mobile-360-05-dang-nhap-tai-quay.png) |
| S6 Trang chính lễ tân (TC-01) | ![](screenshots/desktop-1280-06-trang-chinh-le-tan.png) | ![](screenshots/mobile-360-06-trang-chinh-le-tan.png) |
| Lễ tân trên máy chưa đăng ký (TC-03) | ![](screenshots/desktop-1280-07-le-tan-may-chua-dang-ky.png) | ![](screenshots/mobile-360-07-le-tan-may-chua-dang-ky.png) |
| Tạm khóa 15 phút (TC-02) | ![](screenshots/desktop-1280-08-tam-khoa-15-phut.png) | ![](screenshots/mobile-360-08-tam-khoa-15-phut.png) |

Mọi màn hình ở cả hai kích thước đều được kiểm tra tự động không tràn ngang.

## 6. Ngoài phạm vi / ghi nhận

- Không có lỗi tồn đọng. Các lỗi phát hiện trong quá trình kiểm thử đã sửa trước khi bàn giao: kiểu cột `token_hash`
  không khớp ánh xạ (bắt bởi kiểm tra lược đồ của Hibernate), các migration rỗng của skeleton bị Flyway ghi là đã chạy
  (khắc phục bằng `spring.flyway.target`), và biến môi trường Windows `DB_PASSWORD` của dự án khác ghi đè cấu hình
  (chuyển sang biến chuẩn `SPRING_DATASOURCE_*`).
- Chuyển từ PostgreSQL sang MySQL 8 theo [ADR 0005](../../architecture/adr/0005-use-mysql-instead-of-postgresql.md);
  toàn bộ kiểm thử được chạy lại trên MySQL. MySQL không có trigger cho `TRUNCATE` nên ở staging/prod tài khoản ứng
  dụng không được có quyền `DROP` trên bảng nhật ký.
- Kiểm thử hết phiên 30 phút dùng đồng hồ điều khiển được (máy chủ) và đồng hồ giả (giao diện), không chờ thời gian thật.
## 7. Cách chạy lại

Xem [docs/development/local-setup.md](../../development/local-setup.md) mục 4.
