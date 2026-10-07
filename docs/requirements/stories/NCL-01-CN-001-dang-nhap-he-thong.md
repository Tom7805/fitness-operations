# NCL-01-CN-001 — Đăng nhập hệ thống · Phân tích nghiệp vụ

> **Công việc**: `NCL-01-CN-001-CV-01` · 🟦 `BE-BA` · Jira `VHPT-18`
> **Quy tắc ràng buộc**: `QTN-01` Phân quyền theo vai trò và câu lạc bộ · `QTN-02` Nhật ký thao tác không được sửa xóa
> **Phụ thuộc**: không có. Đây là story đầu tiên của chu kỳ số không.

Tài liệu này chốt luồng, tham số, ràng buộc và ngoại lệ của chức năng đăng nhập **trước khi code**. Người làm `BE-API`
(`CV-03`) và `FE-UI` (`CV-02`) lấy tài liệu này làm đầu vào; mọi giá trị dưới đây là giá trị chính thức của hệ thống.

---

## 1. Phạm vi

| Trong phạm vi | Ngoài phạm vi (story khác) |
|---|---|
| Đăng nhập bằng tên đăng nhập + mật khẩu cho **nhân viên** | Tạo, sửa, khóa tài khoản — `NCL-01-CN-002` |
| Ba loại thiết bị: máy quầy lễ tân, máy tính văn phòng, điện thoại | Tra cứu nhật ký chung — `NCL-01-CN-003` |
| Đăng ký máy quầy với một câu lạc bộ | Ma trận vai trò × chức năng, lọc dữ liệu theo câu lạc bộ — `NCL-01-CN-004` |
| Hết phiên sau 30 phút không thao tác | Đổi / khôi phục mật khẩu, buộc đổi mật khẩu lần đầu — `NCL-01-CN-005` |
| Tạm khóa 15 phút sau 5 lần nhập sai liên tiếp | Đăng nhập ứng dụng hội viên bằng mã — `NCL-14-CN-001` |
| Ghi nhật ký mọi lần đăng nhập thành công / thất bại | Khai báo câu lạc bộ đầy đủ (giờ mở cửa, khu tập…) — `NCL-02-CN-001` |
| Mở trang chính theo vai trò và câu lạc bộ được giao | Nghiệp vụ của từng nhóm chức năng |

Story này chỉ tạo **phần dữ liệu tối thiểu** của tài khoản, vai trò và câu lạc bộ mà đăng nhập cần (xem mục 7). Các story
sau mở rộng các bảng này bằng migration mới, không sửa migration đã phát hành.

## 2. Tác nhân

| Tác nhân | Vai trò trong story |
|---|---|
| Nhân viên phòng tập (`VT-14`) | Đăng nhập, đăng xuất |
| Lễ tân (`VT-04`) | Chỉ đăng nhập được trên **máy quầy đã đăng ký** |
| Quản lý câu lạc bộ (`VT-02`) | Đăng nhập trên máy quầy mới để **đăng ký máy** với câu lạc bộ mình được giao |
| Quản trị viên (`VT-13`) | Đăng ký máy quầy cho bất kỳ câu lạc bộ nào (cấu hình hệ thống thuộc quyền quản trị viên) |
| Huấn luyện viên cá nhân / lớp nhóm (`VT-05`, `VT-06`) | Đăng nhập trên điện thoại |

## 3. Tham số chính thức

| Tham số | Giá trị | Khóa cấu hình |
|---|---|---|
| Số lần nhập sai **liên tiếp** trước khi tạm khóa | 5 | `app.security.auth.max-failed-attempts` |
| Thời gian tạm khóa | 15 phút | `app.security.auth.lock-duration` |
| Hết phiên khi không thao tác | 30 phút | `app.security.auth.idle-timeout` |
| Thời hạn tuyệt đối của một phiên (dù vẫn thao tác) | 12 giờ — dài hơn một ca làm việc | `app.security.auth.absolute-timeout` |
| Cảnh báo trước khi hết phiên trên giao diện | 60 giây | phía giao diện |
| Thuật toán băm mật khẩu | BCrypt, cost 12 | — |

> **Vì sao có thời hạn tuyệt đối 12 giờ**: tiêu chí chỉ nêu hết phiên khi không thao tác; giới hạn tuyệt đối ngăn một
> phiên bị giữ mãi trên máy quầy dùng chung. 12 giờ đủ cho ca dài nhất nên không làm gián đoạn công việc.

## 4. Quy tắc nghiệp vụ đã chốt

### R1. Tên đăng nhập và mật khẩu
- Tên đăng nhập **không phân biệt hoa thường**, bỏ khoảng trắng hai đầu, 3–50 ký tự `a-z 0-9 . _ -`.
- Mật khẩu bắt buộc, tối đa 128 ký tự; chỉ lưu **bản băm BCrypt**, không bao giờ lưu hay ghi log bản rõ.
- Sai tên đăng nhập hoặc sai mật khẩu đều trả **cùng một thông báo** để không lộ tên đăng nhập nào tồn tại:
  *"Tên đăng nhập hoặc mật khẩu không đúng."* Khi tên đăng nhập không tồn tại, máy chủ vẫn chạy một phép so khớp BCrypt
  giả để thời gian phản hồi không khác biệt.

### R2. Tạm khóa sau 5 lần nhập sai liên tiếp (TC-02)
- Mỗi lần **sai mật khẩu** của một tài khoản tồn tại làm tăng bộ đếm sai liên tiếp của tài khoản đó.
- Lần sai thứ 5 đặt **thời điểm hết khóa = thời điểm đó + 15 phút**.
- Từ lần gửi thứ 6 trở đi, trong lúc còn khóa, hệ thống **từ chối mà không kiểm tra mật khẩu** (kể cả mật khẩu đúng) và
  trả thời gian chờ còn lại: *"Tài khoản tạm khóa do nhập sai mật khẩu 5 lần liên tiếp. Vui lòng thử lại sau N phút."*
  Lần từ chối này không kéo dài thời gian khóa.
- Hết 15 phút, bộ đếm về 0 và người dùng có lại đủ 5 lần thử.
- Đăng nhập **thành công** đưa bộ đếm về 0 (vì vậy là "liên tiếp").
- Các lần gửi đồng thời của cùng một tài khoản được xử lý tuần tự (khóa dòng tài khoản) để bộ đếm không bị đếm thiếu.

### R3. Tài khoản không hoạt động
- Tài khoản ở trạng thái **đã khóa** (do quản trị viên, `NCL-01-CN-002`) không đăng nhập được:
  *"Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên."* Trạng thái này chỉ được báo **sau khi mật khẩu đúng** để
  người không biết mật khẩu không dò được trạng thái tài khoản.

### R4. Loại thiết bị và máy quầy (TC-01, TC-03)
| Loại phiên | Cách nhận biết | Ràng buộc |
|---|---|---|
| `COUNTER` — máy quầy lễ tân | Yêu cầu mang **mã máy quầy hợp lệ** (`X-Device-Token`) | Phiên gắn với máy và với câu lạc bộ của máy |
| `OFFICE` — máy tính văn phòng | Không có mã máy quầy, trình duyệt máy tính | Không gắn máy |
| `MOBILE` — điện thoại | Không có mã máy quầy, trình duyệt di động | Không gắn máy |

- **Đăng ký máy quầy**: quản lý câu lạc bộ đăng nhập ngay trên máy cần đăng ký, chọn một câu lạc bộ **trong phạm vi
  được giao** và đặt tên máy (vd. *Quầy lễ tân 1*). Quản trị viên được chọn mọi câu lạc bộ. Tên máy là duy nhất trong
  một câu lạc bộ (không phân biệt hoa thường) trong số các máy đang hoạt động.
- Hệ thống sinh **mã máy quầy ngẫu nhiên 256 bit**, trả về **một lần duy nhất** để trình duyệt của máy lưu lại; máy chủ
  chỉ lưu bản băm SHA-256. Mất mã thì đăng ký lại máy. Sau khi đăng ký, phiên của quản lý trên máy đó được đăng xuất để
  máy sẵn sàng cho lễ tân.
- **Vai trò bắt buộc máy quầy**: lễ tân (`requires_counter_device = true` trên vai trò). Người có vai trò này đăng nhập
  trên máy chưa đăng ký bị từ chối: *"Máy chưa được đăng ký với câu lạc bộ. Vui lòng nhờ quản lý câu lạc bộ đăng nhập
  để đăng ký máy trước khi dùng."* Trên giao diện, đường dẫn dành cho máy quầy (`/counter`) báo yêu cầu này **ngay khi
  mở màn hình đăng nhập**, trước khi nhập tài khoản.
- **Máy quầy chỉ dùng tại quầy của câu lạc bộ đó**: bất kỳ ai đăng nhập trên một máy quầy đều phải được giao câu lạc bộ
  của máy (hoặc phạm vi toàn chuỗi), nếu không: *"Tài khoản không được giao làm việc tại câu lạc bộ của máy quầy này."*
- **Phiên gắn với máy**: mọi yêu cầu của một phiên `COUNTER` phải mang đúng mã của máy đã tạo phiên. Lấy mã phiên mang
  sang máy khác thì bị từ chối như phiên không hợp lệ.
- Các kiểm tra về máy quầy và trạng thái tài khoản chỉ chạy **sau khi mật khẩu đúng**, và một lần đăng nhập bị từ chối
  vì lý do máy quầy **không** tính là nhập sai (mật khẩu đúng nên bộ đếm về 0).

### R5. Phiên làm việc
- Đăng nhập thành công tạo một **phiên** lưu ở máy chủ (thời điểm tạo, lần thao tác cuối, thiết bị, câu lạc bộ đang làm
  việc, địa chỉ IP, trình duyệt) và trả về mã truy cập dạng JWT ký HMAC-SHA256 chứa mã phiên.
- Mỗi yêu cầu hợp lệ cập nhật **lần thao tác cuối**. Quá 30 phút không có yêu cầu nào → phiên hết hạn:
  *"Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại."*
- Giao diện theo dõi thao tác thực của người dùng (chuột, bàn phím, chạm). Khi người dùng còn thao tác mà lâu chưa gọi
  máy chủ, giao diện gửi một yêu cầu giữ phiên; khi người dùng ngừng thao tác, giao diện cảnh báo trước 60 giây rồi tự
  đăng xuất. **Máy chủ luôn là bên quyết định** — giao diện chỉ phản ánh.
- Đăng xuất kết thúc phiên ngay ở máy chủ.
- **Câu lạc bộ đang làm việc** của phiên: máy quầy → câu lạc bộ của máy; người được giao đúng một câu lạc bộ → câu lạc bộ
  đó; người được giao nhiều câu lạc bộ hoặc toàn chuỗi → để trống (chọn câu lạc bộ thuộc `NCL-01-CN-004`).

### R6. Mở đúng nhóm chức năng (QTN-01)
- Kết quả đăng nhập trả về vai trò, phạm vi câu lạc bộ (danh sách câu lạc bộ hoặc toàn chuỗi), câu lạc bộ đang làm
  việc và thông tin máy quầy nếu có.
- Giao diện mở **trang chính theo vai trò**: chỉ hiện các nhóm chức năng của vai trò người dùng, kèm câu lạc bộ đang
  làm việc. Kiểm soát quyền chi tiết ở từng API thuộc `NCL-01-CN-004`; story này đặt nền: mọi API ngoài đăng nhập đều
  yêu cầu phiên hợp lệ.

### R7. Nhật ký đăng nhập (QTN-02, TC-04)
- Mỗi lần xử lý yêu cầu đăng nhập — thành công hay thất bại — ghi **một dòng nhật ký** gồm: người thực hiện (tài khoản
  nếu xác định được, và tên đăng nhập đã nhập), **nội dung** (loại sự kiện, mã lý do, mô tả tiếng Việt), **thời điểm**,
  loại thiết bị, máy quầy, câu lạc bộ, địa chỉ IP, trình duyệt.
- Nhật ký **chỉ được thêm mới**: cơ sở dữ liệu chặn `UPDATE`, `DELETE`, `TRUNCATE` trên bảng nhật ký bằng trigger.
- Ghi nhật ký nằm **trong cùng giao dịch** với thao tác: không ghi được nhật ký thì thao tác không hoàn tất.
- Không ghi mật khẩu (kể cả mật khẩu sai) vào nhật ký hay log ứng dụng.

| Sự kiện | Khi nào | Mã lý do |
|---|---|---|
| `LOGIN_SUCCEEDED` | Đăng nhập thành công | — |
| `LOGIN_FAILED` | Sai tên đăng nhập / mật khẩu | `UNKNOWN_USERNAME`, `INVALID_PASSWORD` |
| `ACCOUNT_TEMPORARILY_LOCKED` | Lần sai thứ 5 làm tài khoản bị tạm khóa | `TOO_MANY_FAILED_ATTEMPTS` |
| `LOGIN_REJECTED` | Từ chối dù mật khẩu đúng hoặc không được kiểm tra | `ACCOUNT_LOCKED`, `ACCOUNT_DISABLED`, `DEVICE_NOT_REGISTERED`, `DEVICE_BRANCH_NOT_ASSIGNED` |
| `LOGOUT` | Đăng xuất | — |
| `SESSION_EXPIRED` | Phát hiện phiên quá 30 phút không thao tác | `IDLE_TIMEOUT` |
| `DEVICE_REGISTERED` | Đăng ký máy quầy | — |

## 5. Luồng xử lý

### 5.1 Đăng nhập (máy chủ)

```
Nhận {tên đăng nhập, mật khẩu, loại thiết bị} + mã máy quầy (nếu có)
 │
 ├─ Kiểm tra định dạng ─────────────────────────────── sai → 400 VALIDATION_ERROR (không ghi nhật ký, không tính lần sai)
 ├─ Mã máy quầy có nhưng không hợp lệ / đã thu hồi → coi như không có máy quầy
 ├─ Tìm tài khoản (khóa dòng)
 │    └─ không có → so khớp BCrypt giả → ghi LOGIN_FAILED/UNKNOWN_USERNAME → 401 INVALID_CREDENTIALS
 ├─ Đang tạm khóa? → ghi LOGIN_REJECTED/ACCOUNT_LOCKED → 423 ACCOUNT_TEMPORARILY_LOCKED (+ thời gian chờ)
 ├─ Khóa đã hết hạn? → bộ đếm = 0
 ├─ So khớp mật khẩu
 │    └─ sai → bộ đếm + 1 → ghi LOGIN_FAILED/INVALID_PASSWORD
 │             └─ bộ đếm = 5 → đặt hết khóa = bây giờ + 15 phút → ghi ACCOUNT_TEMPORARILY_LOCKED
 │             → 401 INVALID_CREDENTIALS
 ├─ Mật khẩu đúng → bộ đếm = 0
 ├─ Tài khoản đã khóa? → ghi LOGIN_REJECTED/ACCOUNT_DISABLED → 403 ACCOUNT_DISABLED
 ├─ Có máy quầy và tài khoản không được giao câu lạc bộ của máy → ghi LOGIN_REJECTED → 403 DEVICE_BRANCH_NOT_ASSIGNED
 ├─ Không có máy quầy và vai trò bắt buộc máy quầy → ghi LOGIN_REJECTED → 403 DEVICE_NOT_REGISTERED
 └─ Tạo phiên → cập nhật lần đăng nhập cuối → ghi LOGIN_SUCCEEDED → 200 {mã truy cập, phiên, người dùng}
```

### 5.2 Mỗi yêu cầu sau đăng nhập

```
Mã truy cập → kiểm tra chữ ký + hạn tuyệt đối ─── sai → 401 SESSION_INVALID
Tìm phiên → đã kết thúc? ─────────────────────── → 401 SESSION_INVALID
         → quá 30 phút không thao tác? ───────── → kết thúc phiên, ghi SESSION_EXPIRED → 401 SESSION_EXPIRED
         → phiên máy quầy mà mã máy không khớp? ─ → 401 SESSION_INVALID
         → tài khoản không còn hoạt động? ──────── → 401 SESSION_INVALID
         → cập nhật lần thao tác cuối → xử lý yêu cầu
```

### 5.3 Máy quầy mới (TC-03)

```
Mở /counter trên máy tính bảng mới → hỏi máy chủ trạng thái máy → chưa đăng ký
 → màn hình "Máy chưa được đăng ký với câu lạc bộ" + yêu cầu quản lý đăng nhập
 → quản lý đăng nhập → chọn câu lạc bộ + đặt tên máy → máy chủ trả mã máy quầy một lần
 → trình duyệt lưu mã → đăng xuất quản lý → màn hình đăng nhập máy quầy (hiện tên máy + câu lạc bộ)
```

## 6. Hợp đồng API

| Phương thức | Đường dẫn | Xác thực | Mục đích |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Không | Đăng nhập |
| `POST` | `/api/v1/auth/logout` | Phiên | Đăng xuất |
| `GET` | `/api/v1/auth/me` | Phiên | Thông tin phiên hiện tại (đồng thời giữ phiên) |
| `GET` | `/api/v1/devices/current` | Không (mã máy quầy trong header) | Máy này đã đăng ký chưa |
| `GET` | `/api/v1/devices/registrable-branches` | Phiên · quản lý / quản trị viên | Câu lạc bộ được phép đăng ký máy |
| `POST` | `/api/v1/devices` | Phiên · quản lý / quản trị viên | Đăng ký máy quầy |

Mã lỗi chi tiết: [docs/api/error-codes.md](../../api/error-codes.md).

## 7. Dữ liệu tối thiểu

| Bảng | Mục đích trong story này |
|---|---|
| `roles` | Danh mục vai trò nhân viên, cờ `requires_counter_device` |
| `users`, `user_roles` | Tài khoản, bản băm mật khẩu, trạng thái, bộ đếm sai, thời điểm hết khóa |
| `branches` | Câu lạc bộ (mã, tên, trạng thái) — `NCL-02-CN-001` bổ sung các cột còn lại |
| `user_branch_scopes` + `users.all_branches` | Phạm vi câu lạc bộ được giao — `NCL-01-CN-004` hoàn thiện |
| `counter_devices` | Máy quầy đã đăng ký |
| `user_sessions` | Phiên làm việc |
| `auth_audit_logs` | Nhật ký đăng nhập, chỉ thêm mới |

## 8. Điều kiện kiểm chứng

| Điều kiện trong story | Kiểm chứng được bằng |
|---|---|
| **Bắt đầu**: nhân viên có tài khoản đang hoạt động và chưa đăng nhập | Tài khoản `status = ACTIVE`, không có `Authorization` trong yêu cầu |
| **Kết quả**: vào được hệ thống với đúng quyền | Phản hồi 200 có vai trò và phạm vi câu lạc bộ khớp dữ liệu; `GET /auth/me` trả 200 bằng mã nhận được |
| **Kết quả**: phiên được ghi nhận | Có dòng `user_sessions` với thiết bị, câu lạc bộ, thời điểm; có dòng `LOGIN_SUCCEEDED` |

## 9. Ánh xạ tiêu chí chấp nhận

| Tiêu chí | Quy tắc | Kiểm thử máy chủ | Kiểm thử giao diện |
|---|---|---|---|
| `TC-01` Lễ tân đăng nhập đúng trên máy quầy đã đăng ký | R4, R5, R6 | `AuthIntegrationTest.tc01_*` | `LoginPage.test.tsx`, `auth.spec.ts` |
| `TC-02` Lần thứ sáu sau 5 lần sai → khóa 15 phút, báo thời gian chờ | R2 | `AuthIntegrationTest.tc02_*` | `LoginPage.test.tsx`, `auth.spec.ts` |
| `TC-03` Máy tính bảng mới chưa đăng ký → yêu cầu quản lý đăng ký | R4 | `AuthIntegrationTest.tc03_*`, `DeviceIntegrationTest` | `CounterPage.test.tsx`, `auth.spec.ts` |
| `TC-04` Ghi người thực hiện, nội dung, thời điểm | R7 | `AuthIntegrationTest.tc04_*` | Kiểm tra trên cơ sở dữ liệu trong báo cáo kiểm thử |
