# NCL-01-CN-001 — Thiết kế màn hình đăng nhập

> **Công việc**: `NCL-01-CN-001-CV-02` · 🟩 `FE-UI` · Jira `VHPT-18`
> **Đầu vào**: [Phân tích nghiệp vụ](../../requirements/stories/NCL-01-CN-001-dang-nhap-he-thong.md) ·
> [Mã lỗi API](../../api/error-codes.md)

Thiết kế gồm bố cục, luồng thao tác và **toàn văn thông báo**. Người làm `FE-DEV` (`CV-04`) dùng đúng câu chữ dưới đây.

## 1. Nguyên tắc chung

| Hạng mục | Quy định |
|---|---|
| Kích thước kiểm tra | 360 px (điện thoại), 768–1024 px (máy tính bảng tại quầy), 1280 px (máy tính) — không tràn ngang |
| Vùng chạm | Ô nhập và nút cao **48 px** (dễ chạm trên máy tính bảng và điện thoại), chữ ô nhập 16 px để iOS không tự phóng to |
| Màu | Màu chính xanh ngọc (`primary`), lỗi đỏ (`destructive`), cảnh báo hổ phách (`warning`); có chế độ tối theo hệ điều hành |
| Phông | Inter / hệ thống, hỗ trợ đầy đủ dấu tiếng Việt |
| Khả năng tiếp cận | Mọi ô nhập có nhãn; thông báo lỗi trong vùng `role="alert"`; giữ tiêu điểm hợp lý sau khi gửi; tương phản ≥ 4.5:1 |
| Trạng thái chung | **Đang tải** (nút có vòng quay, khóa nhập), **lỗi** (khung thông báo phía trên nút), **trống** (không áp dụng ở form) |

## 2. Bản đồ màn hình

| Mã | Màn hình | Đường dẫn | Ai dùng |
|---|---|---|---|
| S1 | Đăng nhập — máy tính văn phòng | `/login` | Mọi nhân viên |
| S2 | Đăng nhập — điện thoại | `/login` (cùng màn hình, bố cục 1 cột) | Huấn luyện viên, quản lý |
| S3 | Đăng nhập — máy quầy đã đăng ký | `/counter` hoặc `/login` khi máy có mã máy quầy | Lễ tân |
| S4 | Máy quầy chưa đăng ký | `/counter` khi máy chưa đăng ký | Lễ tân → quản lý |
| S5 | Đăng ký máy quầy (2 bước) | `/counter/register` | Quản lý câu lạc bộ, quản trị viên |
| S6 | Trang chính theo vai trò | `/` | Mọi nhân viên sau khi đăng nhập |
| S7 | Cảnh báo sắp hết phiên | hộp thoại trên mọi trang sau đăng nhập | Mọi nhân viên |

```
            ┌─────────────┐  máy có mã máy quầy   ┌────────────┐
 /counter ─►│ hỏi máy chủ ├──────────────────────►│ S3 Đăng    │──┐
            │ trạng thái  │                       │ nhập quầy  │  │ đăng nhập
            └──────┬──────┘                       └────────────┘  │ thành công
                   │ chưa đăng ký                                  ▼
                   ▼                                         ┌───────────┐
            ┌─────────────┐ quản lý  ┌──────────────────┐    │ S6 Trang  │
            │ S4 Máy chưa ├─────────►│ S5 Đăng ký máy   │    │ chính theo│
            │ đăng ký     │          │ 1. Quản lý đăng  │    │ vai trò   │
            └─────────────┘          │    nhập          │    └───────────┘
                                     │ 2. Chọn CLB, tên │         ▲
                                     └────────┬─────────┘         │
                                     lưu mã máy, đăng xuất ──► S3 ┘
 /login ──► S1 / S2 ─────────────────────────────────────────────►S6
```

## 3. S1 — Đăng nhập trên máy tính (≥ 1024 px)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ ┌───────────────────────────────┐                                            │
│ │  ◆ Vận Hành Phòng Tập         │      ┌──────────────────────────────────┐  │
│ │                               │      │ Đăng nhập                        │  │
│ │  Vận hành phòng tập thể hình  │      │ Dùng tài khoản được cấp để làm   │  │
│ │  từ buổi tập thử tới hội viên │      │ việc theo vai trò của bạn.       │  │
│ │  gắn bó lâu dài               │      │                                  │  │
│ │                               │      │ Tên đăng nhập                    │  │
│ │  • Ra vào đúng quyền lợi      │      │ [ vd. letan.caugiay            ] │  │
│ │  • Lớp nhóm và huấn luyện     │      │ Mật khẩu                         │  │
│ │  • Thu tiền minh bạch         │      │ [ ••••••••••              (👁) ] │  │
│ │                               │      │ ⚠ Caps Lock đang bật             │  │
│ │  (nền xanh ngọc đậm)          │      │ ┌ khung thông báo lỗi ─────────┐ │  │
│ │                               │      │ └──────────────────────────────┘ │  │
│ │                               │      │ [          Đăng nhập           ] │  │
│ │                               │      │ Máy quầy lễ tân? Mở trang máy    │  │
│ └───────────────────────────────┘      │ quầy →                           │  │
│                                        └──────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

- Cột trái (thương hiệu) chỉ hiện từ 1024 px; nhỏ hơn thì ẩn, chỉ giữ logo phía trên form.
- Tiêu điểm tự đặt vào ô tên đăng nhập khi mở trang.
- Nhấn **Enter** ở bất kỳ ô nào là gửi form.

## 4. S2 — Đăng nhập trên điện thoại (360 px)

```
┌────────────────────────────┐
│ ◆ Vận Hành Phòng Tập       │
│                            │
│ Đăng nhập                  │
│ Dùng tài khoản được cấp... │
│                            │
│ Tên đăng nhập              │
│ [                        ] │
│ Mật khẩu                   │
│ [                    (👁) ] │
│                            │
│ [       Đăng nhập        ] │
│                            │
│ Máy quầy lễ tân? Mở trang  │
│ máy quầy →                 │
└────────────────────────────┘
```

- Một cột, lề 16 px, nút rộng hết chiều ngang.
- Ô tên đăng nhập: `autocapitalize="none"`, `autocorrect="off"`, `autocomplete="username"`;
  ô mật khẩu: `autocomplete="current-password"` để trình quản lý mật khẩu của điện thoại hoạt động.
- Giao diện tự nhận biết điện thoại và gửi `clientType = MOBILE`, máy tính gửi `OFFICE`.

## 5. S3 — Đăng nhập trên máy quầy đã đăng ký

```
┌─────────────────────────────────────────────┐
│ ┌─────────────────────────────────────────┐ │
│ │ 🖥 Máy quầy · Quầy lễ tân 1              │ │  ← dải nhận diện, nền xanh ngọc nhạt
│ │   Fitness Cầu Giấy                      │ │
│ └─────────────────────────────────────────┘ │
│ Đăng nhập tại quầy                          │
│ Phiên làm việc gắn với máy quầy này.        │
│ Tên đăng nhập  [                          ] │
│ Mật khẩu       [                     (👁) ] │
│ [               Đăng nhập                 ] │
└─────────────────────────────────────────────┘
```

- Dải nhận diện máy quầy luôn hiện để lễ tân biết đang ở đúng quầy, đúng câu lạc bộ.
- Không có liên kết "Mở trang máy quầy" (đã là máy quầy).

## 6. S4 — Máy quầy chưa đăng ký (TC-03)

Hiện **ngay khi mở** `/counter` trên máy chưa có mã máy quầy hợp lệ, trước khi nhập tài khoản.

```
┌─────────────────────────────────────────────┐
│              ⚠ (biểu tượng máy tính bảng)   │
│  Máy chưa được đăng ký với câu lạc bộ       │
│                                             │
│  Máy quầy phải được quản lý câu lạc bộ đăng │
│  ký trước khi lễ tân đăng nhập. Vui lòng    │
│  nhờ quản lý đăng nhập trên máy này để đăng │
│  ký máy với câu lạc bộ.                     │
│                                             │
│  [  Quản lý đăng nhập để đăng ký máy     ]  │  ← nút chính
│  Không phải máy quầy? Đăng nhập thường →    │
└─────────────────────────────────────────────┘
```

## 7. S5 — Đăng ký máy quầy

**Bước 1/2 — Quản lý xác nhận**: dùng lại form đăng nhập, tiêu đề *"Đăng ký máy quầy · Bước 1/2"*, mô tả
*"Quản lý câu lạc bộ đăng nhập để xác nhận đăng ký máy này."* Nếu tài khoản không phải quản lý câu lạc bộ hoặc quản
trị viên: *"Chỉ quản lý câu lạc bộ hoặc quản trị viên được đăng ký máy quầy."* rồi tự đăng xuất tài khoản đó.

**Bước 2/2 — Chọn câu lạc bộ và đặt tên máy**

```
┌─────────────────────────────────────────────┐
│ Đăng ký máy quầy · Bước 2/2                 │
│ Người xác nhận: Lê Thị Quản Lý              │
│                                             │
│ Câu lạc bộ                                  │
│ ( ) Fitness Cầu Giấy                        │  ← chỉ câu lạc bộ được phép; 1 câu lạc bộ thì chọn sẵn
│ ( ) Fitness Hai Bà Trưng                    │
│ Tên máy quầy                                │
│ [ Quầy lễ tân 1                           ] │
│ Tên giúp phân biệt các máy trong câu lạc bộ │
│                                             │
│ [            Đăng ký máy                  ] │
│ Hủy và đăng xuất                            │
└─────────────────────────────────────────────┘
```

- Thành công: lưu mã máy quầy vào trình duyệt, đăng xuất quản lý, chuyển về S3 với thông báo xanh
  *"Đã đăng ký máy quầy "Quầy lễ tân 1" cho Fitness Cầu Giấy. Lễ tân có thể đăng nhập."*
- Không có câu lạc bộ nào được phép: *"Tài khoản chưa được giao câu lạc bộ nào để đăng ký máy quầy."*

## 8. Thông báo trên form đăng nhập

Khung thông báo nằm ngay trên nút **Đăng nhập**, có biểu tượng và tiêu đề đậm. Nội dung lấy đúng `message` máy chủ trả
về; tiêu đề do giao diện đặt theo `code`:

| `code` | Kiểu | Tiêu đề | Hành động kèm theo |
|---|---|---|---|
| `INVALID_CREDENTIALS` | Lỗi | Không đăng nhập được | Xóa ô mật khẩu, đưa tiêu điểm về ô mật khẩu |
| `ACCOUNT_TEMPORARILY_LOCKED` | Lỗi | Tài khoản tạm khóa | Xem mục 9 |
| `ACCOUNT_DISABLED` | Lỗi | Tài khoản đã bị khóa | — |
| `DEVICE_NOT_REGISTERED` | Cảnh báo | Máy chưa được đăng ký | Nút **Đăng ký máy quầy** → S5 |
| `DEVICE_BRANCH_NOT_ASSIGNED` | Cảnh báo | Sai câu lạc bộ | — |
| `VALIDATION_ERROR` | Lỗi dưới từng ô | — | — |
| Mất kết nối | Lỗi | Không kết nối được máy chủ | *"Vui lòng kiểm tra mạng rồi thử lại."* |

Kiểm tra phía giao diện trước khi gửi: *"Vui lòng nhập tên đăng nhập."*, *"Vui lòng nhập mật khẩu."*

Thông báo khi bị đưa về trang đăng nhập:

| Lý do | Kiểu | Nội dung |
|---|---|---|
| Hết phiên do không thao tác | Cảnh báo | Phiên làm việc đã hết hạn do không thao tác trong 30 phút. Vui lòng đăng nhập lại. |
| Phiên không hợp lệ | Cảnh báo | Phiên làm việc không hợp lệ hoặc đã kết thúc. Vui lòng đăng nhập lại. |
| Đăng xuất | Thông tin | Bạn đã đăng xuất. |

## 9. Thông báo tạm khóa có thời gian chờ (TC-02)

```
┌ 🔒 Tài khoản tạm khóa ───────────────────────┐
│ Tài khoản tạm khóa do nhập sai mật khẩu 5 lần│
│ liên tiếp. Vui lòng thử lại sau 15 phút.     │
│ Có thể thử lại sau: 14:52                    │  ← đếm ngược mm:ss mỗi giây, đọc từ retryAfterSeconds
└──────────────────────────────────────────────┘
[ Thử lại sau 14:52 ]                             ← nút bị khóa tới khi đếm về 0
```

- Đếm ngược theo `details.retryAfterSeconds`; về 0 thì khung thông báo tự đổi thành
  *"Đã hết thời gian tạm khóa. Bạn có thể đăng nhập lại."* và nút mở lại.
- Đổi tên đăng nhập khác thì bỏ khóa trên giao diện (khóa là của tài khoản, không phải của máy).

## 10. S6 — Trang chính theo vai trò

```
┌──────────────────────────────────────────────────────────────────────┐
│ ◆ Vận Hành Phòng Tập     🖥 Quầy lễ tân 1 · Fitness Cầu Giấy   (PT)▾ │ ← thanh trên: máy quầy / CLB, menu người dùng
├──────────────────────────────────────────────────────────────────────┤
│ Xin chào, Phạm Thu Hà                                                │
│ Lễ tân · Fitness Cầu Giấy                                            │
│                                                                      │
│ Chức năng của bạn                                                    │
│ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                   │
│ │ Ra vào CLB   │ │ Thu tiền     │ │ Đặt lớp tại  │  …                │
│ │ Sắp ra mắt   │ │ Sắp ra mắt   │ │ quầy         │                   │
│ └──────────────┘ └──────────────┘ └──────────────┘                   │
│                                                                      │
│ Phiên làm việc: máy quầy · tự hết hạn sau 30 phút không thao tác     │
└──────────────────────────────────────────────────────────────────────┘
```

- Chỉ hiện nhóm chức năng của các vai trò người dùng có (QTN-01). Chức năng chưa phát triển ghi *"Sắp ra mắt"* và
  không bấm được.
- Menu người dùng: họ tên, tên đăng nhập, vai trò, **Đăng xuất**.
- Người có nhiều câu lạc bộ: thanh trên ghi *"N câu lạc bộ"*; phạm vi toàn chuỗi ghi *"Toàn chuỗi"*.

## 11. S7 — Cảnh báo sắp hết phiên

Hiện khi người dùng không thao tác 29 phút:

```
┌ Phiên làm việc sắp hết hạn ──────────────────┐
│ Bạn chưa thao tác trong một lúc. Phiên sẽ tự │
│ kết thúc sau 60 giây để bảo vệ tài khoản.    │
│                                              │
│        [ Đăng xuất ]  [ Tiếp tục làm việc ]  │
└──────────────────────────────────────────────┘
```

- Số giây đếm ngược trực tiếp. Bấm **Tiếp tục làm việc** gửi một yêu cầu giữ phiên. Hết giờ thì đăng xuất và về trang
  đăng nhập với thông báo hết phiên.
