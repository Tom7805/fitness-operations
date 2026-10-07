# Chạy dự án trên máy phát triển

## Yêu cầu

| Công cụ | Phiên bản |
|---|---|
| JDK | 17 trở lên (mã được biên dịch với `--release 17`) |
| Node.js | 22 trở lên |
| Docker Desktop | bản mới, dùng cho PostgreSQL và Testcontainers |

## 1. Cơ sở dữ liệu

```bash
cp .env.example .env                       # tùy chọn, mặc định đã chạy được
docker compose -f docker-compose.dev.yml up -d
```

PostgreSQL 17 chạy ở `localhost:5432`, cơ sở dữ liệu `fitness_operations`, tài khoản `fitness` / `fitness`.

## 2. Backend

```bash
cd backend
./mvnw spring-boot:run          # profile mặc định: dev
```

- Flyway tạo bảng (`db/migration`) và nạp **dữ liệu mẫu** (`db/seed`, chỉ ở profile `dev`).
- API: <http://localhost:8080/api/v1> · Swagger UI: <http://localhost:8080/swagger-ui.html>
- Thử nhanh bằng `backend/http/auth.http` (IntelliJ HTTP Client hoặc REST Client của VS Code).

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

Gỡ tạm khóa do nhập sai nhiều lần (chỉ dùng trên máy phát triển):

```bash
docker compose -f docker-compose.dev.yml exec -T postgres \
  psql -U fitness -d fitness_operations -c "UPDATE users SET failed_login_attempts = 0, locked_until = NULL"
```

## 3. Frontend

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173 — /api được chuyển tới http://localhost:8080
```

## 4. Kiểm thử

```bash
cd backend && ./mvnw verify          # unit + tích hợp (Testcontainers cần Docker đang chạy)
cd frontend && npm run lint && npm run typecheck && npm test -- --run
cd frontend && npm run test:e2e      # cần backend + cơ sở dữ liệu ở bước 1–2; dùng Chrome đã cài
```
