# Fitness Operations

Hệ thống quản lý vận hành phòng tập (gym/fitness center): hội viên, gói tập, huấn luyện viên, lớp học, đặt lịch, check-in, thanh toán, thiết bị và báo cáo.

> **Trạng thái:** 🚧 Đang phát triển. Dự án đang ở giai đoạn **Walking Skeleton**: đã có cấu trúc thư mục và file, chưa có mã nguồn.

---

## Mục lục

- [Tính năng dự kiến](#tính-năng-dự-kiến)
- [Công nghệ](#công-nghệ)
- [Kiến trúc](#kiến-trúc)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Bắt đầu](#bắt-đầu)
- [Quy trình làm việc](#quy-trình-làm-việc)
- [Tài liệu](#tài-liệu)
- [Giấy phép](#giấy-phép)

---

## Tính năng dự kiến

| Module | Mô tả |
|---|---|
| `auth` | Đăng ký, đăng nhập, JWT access/refresh token, quên mật khẩu, xác thực email |
| `user` | Quản lý người dùng, vai trò (role) và quyền (permission) |
| `branch` | Chi nhánh, phòng tập, giờ hoạt động |
| `member` | Hồ sơ hội viên, thẻ hội viên, liên hệ khẩn cấp |
| `membership` | Gói tập, đăng ký, gia hạn, bảo lưu, hủy, chuyển nhượng |
| `staff` | Nhân viên, ca làm việc |
| `trainer` | Huấn luyện viên, chứng chỉ, lịch rảnh, gói PT và buổi tập PT |
| `gymclass` | Loại lớp, lịch lớp học (hỗ trợ lặp lại) |
| `booking` | Đặt chỗ lớp học, danh sách chờ |
| `checkin` | Check-in/check-out bằng QR, thẻ hoặc thủ công; lịch sử ra vào |
| `payment` | Thanh toán, hóa đơn, hoàn tiền; tích hợp VNPay, MoMo, tiền mặt |
| `promotion` | Chương trình khuyến mãi, voucher |
| `equipment` | Thiết bị, danh mục, lịch bảo trì |
| `workout` | Bài tập, giáo án, nhật ký tập luyện |
| `progress` | Chỉ số cơ thể, mục tiêu tập luyện |
| `nutrition` | Kế hoạch dinh dưỡng, bữa ăn |
| `feedback` | Phản hồi, góp ý của hội viên |
| `notification` | Thông báo qua Email, SMS, Push, In-app |
| `report` | Dashboard, báo cáo doanh thu, hội viên, điểm danh; xuất Excel/PDF/CSV |
| `file` | Upload và lưu trữ file (Local / S3) |
| `audit` | Nhật ký thao tác hệ thống |
| `setting` | Cấu hình hệ thống |

## Công nghệ

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation, Mail, Actuator) |
| Build | Maven (Maven Wrapper) |
| Cơ sở dữ liệu | PostgreSQL |
| Migration | Flyway |
| Cache | Redis |
| Xác thực | JWT |
| Mapping | MapStruct, Lombok |
| Tài liệu API | SpringDoc OpenAPI (Swagger UI) |
| Kiểm thử | JUnit 5, Mockito, Testcontainers, ArchUnit |
| Chất lượng mã | Checkstyle, SpotBugs, PMD, JaCoCo |
| Hạ tầng | Docker, Docker Compose, Kubernetes (Kustomize), Nginx |
| Giám sát | Prometheus, Grafana |
| CI/CD | GitHub Actions |

> Phiên bản cụ thể sẽ được chốt trong `pom.xml`.

## Kiến trúc

Dự án theo mô hình **Modular Monolith**: code được chia theo nghiệp vụ trong `modules/`, mỗi module có các tầng giống nhau:

```
modules/<module>/
├── controller/          # REST API
├── dto/request/         # Dữ liệu đầu vào
├── dto/response/        # Dữ liệu trả về
├── entity/              # JPA Entity
├── enums/
├── mapper/              # MapStruct mapper
├── repository/          # Spring Data JPA (+ specification/)
├── service/             # Interface nghiệp vụ (+ impl/)
├── exception/           # Exception riêng của module
└── event/               # Domain event
```

Các module giao tiếp với nhau qua service interface hoặc domain event (Spring `ApplicationEvent`), không gọi thẳng repository của module khác.

## Cấu trúc thư mục

```
fitness-operations/
├── .github/                 # CI/CD workflows, PR/Issue templates
├── config/                  # Cấu hình Checkstyle, SpotBugs, PMD
├── deploy/k8s/              # Kubernetes manifests (base + overlays)
├── docker/                  # Cấu hình Postgres, Nginx, Redis, Prometheus, Grafana
├── docs/                    # Tài liệu yêu cầu, kiến trúc, DB, API, hướng dẫn
├── http/                    # File .http để test API trong IDE
├── scripts/                 # Script build, chạy dev, backup/restore DB
└── src/
    ├── main/
    │   ├── java/com/fitnessops/
    │   │   ├── common/      # Thành phần dùng chung: DTO, exception, util, validation
    │   │   ├── config/      # Cấu hình Spring
    │   │   ├── security/    # JWT, UserDetails, xử lý lỗi xác thực
    │   │   ├── scheduler/   # Các job định kỳ
    │   │   └── modules/     # Các module nghiệp vụ
    │   └── resources/
    │       ├── db/migration/    # Flyway migration
    │       ├── db/seed/         # Dữ liệu khởi tạo
    │       ├── i18n/            # Đa ngôn ngữ (vi, en)
    │       └── templates/       # Email & báo cáo
    └── test/                # Unit test, integration test, architecture test
```

## Bắt đầu

> ⚠️ Phần này sẽ dùng được khi đã hoàn thiện `pom.xml`, `docker-compose.yml` và các file cấu hình.

### Yêu cầu

- JDK 17
- Docker và Docker Compose
- Git

### Cài đặt và chạy

```bash
# 1. Clone dự án
git clone <repository-url>
cd fitness-operations

# 2. Tạo file biến môi trường
cp .env.example .env

# 3. Khởi động hạ tầng (PostgreSQL, Redis...)
docker compose -f docker-compose.dev.yml up -d

# 4. Chạy ứng dụng với profile dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Sau khi chạy:

| Dịch vụ | URL |
|---|---|
| API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |

### Kiểm thử

```bash
./mvnw test      # Unit test
./mvnw verify    # Toàn bộ test + kiểm tra chất lượng mã
```

### Profile cấu hình

| Profile | File | Mục đích |
|---|---|---|
| `dev` | `application-dev.yml` | Phát triển trên máy cá nhân |
| `test` | `application-test.yml` | Chạy test |
| `staging` | `application-staging.yml` | Môi trường thử nghiệm |
| `prod` | `application-prod.yml` | Môi trường production |

## Quy trình làm việc

- **Nhánh:** `main` (production) · `develop` (tích hợp) · `feature/<tên>` · `fix/<tên>` · `hotfix/<tên>`
- **Commit:** theo [Conventional Commits](https://www.conventionalcommits.org/), ví dụ `feat(member): add member registration API`
- **Pull Request:** cần qua CI và review trước khi merge

Chi tiết xem [CONTRIBUTING.md](CONTRIBUTING.md) và [docs/development/](docs/development/).

## Tài liệu

| Tài liệu | Đường dẫn |
|---|---|
| Yêu cầu hệ thống | [docs/requirements/](docs/requirements/) |
| Kiến trúc & ADR | [docs/architecture/](docs/architecture/) |
| Cơ sở dữ liệu | [docs/database/](docs/database/) |
| API | [docs/api/](docs/api/) |
| Triển khai | [docs/deployment/](docs/deployment/) |
| Lịch sử thay đổi | [CHANGELOG.md](CHANGELOG.md) |

## Tác giả

- **Hoàng Mạnh Hùng**

## Giấy phép

Xem [LICENSE](LICENSE).
