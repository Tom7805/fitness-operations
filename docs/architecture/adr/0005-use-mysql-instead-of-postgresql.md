# ADR 0005 — Dùng MySQL 8 thay cho PostgreSQL

- **Trạng thái**: Chấp nhận — thay thế lựa chọn PostgreSQL trong README ban đầu và ADR 0004
- **Ngày**: 2026-10-08
- **Liên quan**: `NCL-01-CN-001`, `QTN-02`

## Bối cảnh

Dự án ban đầu chọn PostgreSQL chạy trong Docker. Trên máy phát triển, Docker Desktop chiếm khoảng 1–2 GB RAM chỉ để
chạy một cơ sở dữ liệu, trong khi máy đã cài sẵn **MySQL Server 8.0** (dịch vụ Windows `MySQL80`) và nhóm quen quản lý
dữ liệu bằng **MySQL Workbench**.

## Quyết định

- Dùng **MySQL 8.0** (InnoDB, `utf8mb4_0900_ai_ci`) cho mọi môi trường. Flyway quản lý lược đồ như cũ.
- Máy phát triển dùng MySQL cài sẵn, không cần Docker. Chạy `scripts/mysql-dev-setup.sql` một lần bằng root để tạo
  database `fitness_operations`, `fitness_operations_test` và tài khoản `fitness`.
- Kiểm thử tích hợp chạy trên MySQL thật, database `fitness_operations_test` bị xóa sạch và tạo lại bằng Flyway ở đầu
  mỗi lần chạy. CI trên GitHub dùng MySQL 8.0 làm service container.
- Thời điểm lưu ở cột `DATETIME(6)` theo UTC (`hibernate.jdbc.time_zone=UTC`); mã UUID lưu `CHAR(36)` để đọc trực tiếp
  được trong Workbench.

## Khác biệt so với PostgreSQL và cách xử lý

| Nhu cầu | PostgreSQL | MySQL 8 |
|---|---|---|
| Tên máy quầy duy nhất trong số máy đang hoạt động | Chỉ mục có điều kiện `WHERE status = 'ACTIVE'` | Chỉ mục hàm `(CASE WHEN status = 'ACTIVE' THEN name END)` |
| Nhật ký chỉ thêm mới (QTN-02) | Trigger chặn `UPDATE`, `DELETE`, `TRUNCATE` | Trigger chặn `UPDATE`, `DELETE`; `TRUNCATE` không có trigger nên chặn bằng quyền: tài khoản ứng dụng ở staging/prod **không** được cấp quyền `DROP` |
| So khớp không phân biệt hoa thường | `lower(...)` | Collation `_ci` của cột |
| Tạo trigger khi bật binary log | Không giới hạn | Cần `log_bin_trust_function_creators = 1` (đã có trong script cài đặt) |

## Hệ quả

- Không cần Docker để chạy ứng dụng hay chạy kiểm thử trên máy phát triển; xem và sửa dữ liệu bằng MySQL Workbench.
- Kiểm thử dùng chung một máy chủ MySQL nên không chạy song song nhiều lượt kiểm thử backend cùng lúc trên một máy.
