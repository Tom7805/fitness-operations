# Fitness Operations

Hệ thống quản lý vận hành phòng tập (gym/fitness center) gồm backend Spring Boot và frontend React, quản lý hội viên, gói tập, huấn luyện viên, lớp học, đặt lịch, check-in, thanh toán, thiết bị và báo cáo.

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

### Backend

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.x (Web, Data JPA, Security, Validation, Mail, Actuator) |
| Build | Maven (Maven Wrapper) |
| Cơ sở dữ liệu | MySQL 8 ([ADR 0005](docs/architecture/adr/0005-use-mysql-instead-of-postgresql.md)) |
| Migration | Flyway |
| Cache | Redis |
| Xác thực | JWT |
| Mapping | MapStruct, Lombok |
| Tài liệu API | SpringDoc OpenAPI (Swagger UI) |
| Kiểm thử | JUnit 5, Mockito, Spring MockMvc (tích hợp trên MySQL thật), ArchUnit |
| Chất lượng mã | Checkstyle, SpotBugs, PMD, JaCoCo |

### Frontend

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ | TypeScript |
| Framework | React + Vite |
| Routing | React Router |
| Gọi API & cache dữ liệu | Axios, TanStack Query |
| State toàn cục | Zustand |
| Form & validation | React Hook Form, Zod |
| Giao diện | Tailwind CSS, shadcn/ui |
| Biểu đồ | Recharts |
| Đa ngôn ngữ | i18next |
| Kiểm thử | Vitest, Testing Library, MSW, Playwright |
| Chất lượng mã | ESLint, Prettier |

### Hạ tầng & vận hành

| Hạng mục | Công nghệ |
|---|---|
| Container | Docker, Docker Compose |
| Triển khai | Kubernetes (Kustomize), Nginx |
| Giám sát | Prometheus, Grafana |
| CI/CD | GitHub Actions |

> Phiên bản cụ thể sẽ được chốt trong `backend/pom.xml` và `frontend/package.json`.

## Kiến trúc

Dự án tổ chức theo dạng **monorepo**: backend và frontend nằm chung một repo, dùng chung tài liệu, cấu hình hạ tầng và CI/CD.

```
Trình duyệt ──► Nginx ──┬──► frontend (React SPA)
                        └──► /api ──► backend (Spring Boot) ──► MySQL, Redis
```

### Backend: Modular Monolith

Code được chia theo nghiệp vụ trong `modules/`, mỗi module có các tầng giống nhau:

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

### Frontend: Feature-based

Mỗi tính năng nằm gọn trong `features/<feature>/`, tương ứng với module nghiệp vụ ở backend:

```
features/<feature>/
├── api/                 # Hàm gọi API
├── components/          # Component riêng của tính năng
├── hooks/               # Query/mutation hook (TanStack Query)
├── pages/               # Trang gắn với route
├── schemas/             # Zod schema cho form
├── types/               # Kiểu dữ liệu
├── __tests__/           # Unit test
├── routes.tsx           # Khai báo route của tính năng
└── index.ts             # Public API của tính năng
```

Thành phần dùng chung (UI primitives, layout, hook, HTTP client, store) nằm ngoài `features/`. Một tính năng không import trực tiếp file nội bộ của tính năng khác, chỉ dùng qua `index.ts`.

## Cấu trúc thư mục

```
fitness-operations/
├── .github/                     # CI/CD workflows (backend, frontend), PR/Issue templates
├── backend/                     # Spring Boot API
│   ├── config/                  # Checkstyle, SpotBugs, PMD
│   ├── http/                    # File .http để test API trong IDE
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fitnessops/
│   │   │   │   ├── common/      # DTO, exception, util, validation dùng chung
│   │   │   │   ├── config/      # Cấu hình Spring
│   │   │   │   ├── security/    # JWT, UserDetails, xử lý lỗi xác thực
│   │   │   │   ├── scheduler/   # Các job định kỳ
│   │   │   │   └── modules/     # Các module nghiệp vụ
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway migration
│   │   │       ├── db/seed/         # Dữ liệu khởi tạo
│   │   │       ├── i18n/            # Đa ngôn ngữ (vi, en)
│   │   │       └── templates/       # Email & báo cáo
│   │   └── test/                # Unit test, integration test, architecture test
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                    # React + Vite + TypeScript
│   ├── nginx/                   # Cấu hình Nginx phục vụ bản build
│   ├── public/
│   ├── src/
│   │   ├── app/                 # Router, providers
│   │   ├── components/          # UI dùng chung (ui, common, form, charts)
│   │   ├── features/            # Các tính năng nghiệp vụ
│   │   ├── layouts/             # MainLayout, AuthLayout
│   │   ├── pages/               # Trang lỗi (404, 403, 500)
│   │   ├── services/            # HTTP client, storage
│   │   ├── store/               # State toàn cục (Zustand)
│   │   ├── hooks/ lib/ utils/ constants/ types/ i18n/ styles/ config/
│   │   └── main.tsx
│   ├── tests/                   # Setup test, MSW mocks, E2E (Playwright)
│   ├── Dockerfile
│   └── package.json
├── deploy/k8s/                  # Kubernetes manifests (base + overlays dev/staging/prod)
├── docker/                      # Cấu hình Postgres, Nginx, Redis, Prometheus, Grafana
├── docs/                        # Tài liệu yêu cầu, kiến trúc, DB, API, hướng dẫn
├── scripts/                     # Script build, chạy dev, backup/restore DB
└── docker-compose*.yml          # Chạy toàn bộ hệ thống bằng Docker
```

## Bắt đầu

> ⚠️ Phần này sẽ dùng được khi đã hoàn thiện `pom.xml`, `package.json`, `docker-compose*.yml` và các file cấu hình.

### Yêu cầu

- JDK 17
- Node.js 22 LTS trở lên
- MySQL Server 8 (xem MySQL Workbench để quản lý dữ liệu)
- Docker và Docker Compose (chỉ cần khi chạy toàn bộ hệ thống bằng container)
- Git

### Cài đặt và chạy

```bash
# 1. Clone dự án
git clone https://github.com/Tom7805/fitness-operations.git
cd fitness-operations

# 2. Tạo file biến môi trường
cp .env.example .env
cp frontend/.env.example frontend/.env

# 3. Tạo database và tài khoản MySQL (chạy một lần bằng root, ví dụ trong MySQL Workbench)
#    scripts/mysql-dev-setup.sql

# 4. Chạy backend với profile dev
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 5. Chạy frontend (mở terminal khác)
cd frontend
npm install
npm run dev
```

Hoặc chạy toàn bộ hệ thống bằng Docker:

```bash
docker compose up -d --build
```

Sau khi chạy:

| Dịch vụ | URL |
|---|---|
| Frontend | http://localhost:5173 |
| API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health check | http://localhost:8080/actuator/health |

### Kiểm thử

```bash
# Backend
cd backend
./mvnw test          # Unit test
./mvnw verify        # Toàn bộ test + kiểm tra chất lượng mã

# Frontend
cd frontend
npm run lint         # ESLint
npm run test         # Unit test (Vitest)
npm run test:e2e     # E2E test (Playwright)
```

### Profile cấu hình backend

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
