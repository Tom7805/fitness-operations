# Chạy dự án trên máy phát triển

## Yêu cầu

| Công cụ | Phiên bản |
|---|---|
| JDK | 17 trở lên (mã được biên dịch với `--release 17`) |
| Node.js | 22 trở lên |
| MySQL Server | 8.0 trở lên (trên Windows: dịch vụ `MySQL80`), quản lý bằng MySQL Workbench |

Không cần Docker.

## 1. Cơ sở dữ liệu — làm một lần

Mở **MySQL Workbench**, vào kết nối bằng tài khoản **root**, mở file `scripts/mysql-dev-setup.sql`
(*File → Open SQL Script*) rồi bấm biểu tượng tia sét ⚡ để chạy. Script tạo:

| Thành phần | Dùng cho |
|---|---|
| Database `fitness_operations` | Ứng dụng khi chạy dev |
| Database `fitness_operations_test` | Kiểm thử tự động — bị xóa sạch và tạo lại mỗi lần chạy kiểm thử |
| Tài khoản `fitness` / `fitness` | Ứng dụng kết nối bằng tài khoản này, không dùng root |

Bảng **không** tạo bằng tay: backend tự tạo bằng Flyway khi khởi động. Muốn xem dữ liệu, tạo kết nối mới trong
Workbench: host `127.0.0.1`, port `3306`, user `fitness`, password `fitness`, schema `fitness_operations`.

## 2. Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run          # profile mặc định: dev
```

- Flyway tạo bảng (`db/migration`) và nạp **dữ liệu mẫu** (`db/seed`, chỉ ở profile `dev`).
- API: <http://localhost:8080/api/v1> · Swagger UI: <http://localhost:8080/swagger-ui.html>
- Thử nhanh bằng `backend/http/auth.http` (IntelliJ HTTP Client hoặc REST Client của VS Code).
- Máy có nhiều JDK: đặt `$env:JAVA_HOME` trỏ tới JDK 17 trở lên trước khi chạy.

### Tài khoản mẫu — mật khẩu chung `Fitness@2026`

| Tên đăng nhập | Vai trò | Câu lạc bộ |
|---|---|---|
| `admin` | Quản trị viên | Toàn chuỗi |
| `chuphongtap` | Chủ phòng tập | Toàn chuỗi |
| `quanly.caugiay` | Quản lý câu lạc bộ | Fitness Cầu Giấy |
| `letan.caugiay` | Lễ tân (chỉ đăng nhập trên máy quầy) | Fitness Cầu Giấy |
| `letan.haibatrung` | Lễ tân (chỉ đăng nhập trên máy quầy) | Fitness Hai Bà Trưng |
| `tuvan.caugiay` | Nhân viên tư vấn | Fitness Cầu Giấy |
| `hlv.caugiay` | Huấn luyện viên cá nhân | Fitness Cầu Giấy |
| `hlvnhom.caugiay` | Huấn luyện viên lớp nhóm | Fitness Cầu Giấy |
| `ketoan` | Kế toán | Toàn chuỗi |
| `kythuat.caugiay` | Nhân viên kỹ thuật | Fitness Cầu Giấy |
| `chamsoc.caugiay` | Nhân viên chăm sóc hội viên | Fitness Cầu Giấy |

Lễ tân cần một **máy quầy đã đăng ký**: mở <http://localhost:5173/counter>, bấm *Quản lý đăng nhập để đăng ký máy*,
đăng nhập `quanly.caugiay`, chọn câu lạc bộ và đặt tên máy. Mã máy quầy được lưu trong trình duyệt đó.

Gỡ tạm khóa do nhập sai nhiều lần (chỉ dùng trên máy phát triển) — chạy trong Workbench:

```sql
UPDATE fitness_operations.users SET failed_login_attempts = 0, locked_until = NULL;
```

## 3. Frontend

```powershell
cd frontend
npm install
npm run dev        # http://localhost:5173 — /api được chuyển tới http://localhost:8080
```

## 4. Kiểm thử

```powershell
cd backend;  .\mvnw.cmd verify                  # unit + tích hợp trên database fitness_operations_test
cd frontend; npm run lint; npm run typecheck; npm test -- --run
cd frontend; npm run test:e2e                   # cần backend đang chạy ở bước 2; dùng Chrome đã cài
```

Kiểm thử đầu-cuối đọc trực tiếp cơ sở dữ liệu bằng `mysql.exe` của MySQL Server 8.0; đặt biến `MYSQL_CLI` nếu MySQL
cài ở thư mục khác.
