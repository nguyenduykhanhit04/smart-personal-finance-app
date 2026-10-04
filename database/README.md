# 🗄️ Database Module - Smart Personal Finance App

Module cơ sở dữ liệu lưu trữ toàn bộ thông tin người dùng, ví/tài khoản, giao dịch tài chính, ngân sách, danh mục thu chi và nhật ký phân tích AI (OCR hóa đơn & nhận diện sản phẩm).

---

## 📌 Tổng quan công nghệ
- **Hệ quản trị CSDL:** MySQL 8.0 Community Server.
- **Bảng mã (Character Set):** `utf8mb4` (Hỗ trợ đầy đủ tiếng Việt có dấu và Emoji).
- **Collation:** `utf8mb4_unicode_ci` (So sánh chuỗi đa ngôn ngữ chuẩn xác).
- **Môi trường triển khai:** Chạy độc lập hoặc tích hợp đóng gói tự động qua **Docker Compose**.
- **Cơ chế khởi tạo tự động:** Sử dụng thư mục `/docker-entrypoint-initdb.d/` để tự động chạy schema và nạp dữ liệu danh mục mặc định khi container khởi chạy lần đầu.

---

## 📁 Cấu trúc thư mục

```text
database/
├── schema.sql        # Kịch bản DDL: Tạo CSDL, định nghĩa 9 bảng, ràng buộc khóa ngoại, chỉ mục (Index)
├── seed_data.sql     # Kịch bản DML: Nạp dữ liệu danh mục thu chi mặc định ban đầu
└── README.md         # Tài liệu chi tiết module CSDL
```

---

## 🏛️ Sơ đồ thiết kế CSDL (Schema & Entity Relationship)

Cơ sở dữ liệu gồm **9 bảng chính** được chuẩn hóa theo chuẩn 3NF:

```text
       ┌──────────────┐
       │    users     │
       └──────┬───────┘
              │ 1:N
   ┌──────────┼───────────────┬─────────────────┐
   │          │               │                 │
   ▼          ▼               ▼                 ▼
┌──────┐ ┌──────────┐ ┌───────────────┐ ┌───────────────┐
│accounts│ │categories│ │    budgets    │ │  ai_scan_logs │
└──────┬─┘ └────┬─────┘ └───────────────┘ └───────┬───────┘
       │        │                                 │
       ▼        ▼                                 ▼
┌───────────────────┐                     ┌───────────────┐
│   transactions    │                     │ai_product_logs│
└─────────┬─────────┘                     └───────────────┘
          │ 1:N
   ┌──────┴──────────────┐
   │                     │
   ▼                     ▼
┌──────────────────┐  ┌─────────────────────────┐
│transaction_images│  │ recurring_transactions  │
└──────────────────┘  └─────────────────────────┘
```

### 1. Bảng `users` (Người dùng)
Lưu thông tin tài khoản được đồng bộ từ Firebase Authentication.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `user_id` | `INT` | `PK`, `AUTO_INCREMENT` | Định danh người dùng nội bộ |
| `firebase_uid` | `VARCHAR(128)` | `UNIQUE`, `NOT NULL` | Định danh duy nhất từ Firebase Auth |
| `full_name` | `VARCHAR(100)` | `NULL` | Họ và tên hiển thị |
| `email` | `VARCHAR(150)` | `UNIQUE`, `NOT NULL` | Địa chỉ email đăng nhập |
| `phone` | `VARCHAR(20)` | `NULL` | Số điện thoại liên hệ |
| `avatar_url` | `TEXT` | `NULL` | Đường dẫn ảnh đại diện |
| `password_hash` | `VARCHAR(255)` | `NULL` | Mật khẩu hash (nếu dùng local auth) |
| `auth_provider` | `VARCHAR(30)` | `DEFAULT 'firebase'` | Nhà cung cấp (`firebase`, `google`) |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm tạo tài khoản |
| `updated_at` | `DATETIME` | `ON UPDATE CURRENT_TIMESTAMP`| Thời điểm cập nhật cuối |

### 2. Bảng `accounts` (Ví & Tài khoản thanh toán)
Quản lý các nguồn tiền của người dùng (Tiền mặt, Tài khoản ngân hàng, Ví điện tử Momo, ZaloPay...).
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `account_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã tài khoản |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Chủ sở hữu tài khoản |
| `account_name` | `VARCHAR(100)` | `NOT NULL` | Tên ví (VD: Ví tiền mặt, VCB, Momo) |
| `account_type` | `VARCHAR(50)` | `NULL` | Loại tài khoản (`cash`, `bank`, `e_wallet`)|
| `balance` | `DECIMAL(15,2)` | `DEFAULT 0` | Số dư hiện tại |
| `currency` | `VARCHAR(10)` | `DEFAULT 'VND'` | Đơn vị tiền tệ |

### 3. Bảng `categories` (Danh mục thu chi)
Phân loại các khoản chi tiêu và nguồn thu nhập. Hỗ trợ danh mục dùng chung mặc định hệ thống (`user_id = NULL`) và danh mục do người dùng tự tạo.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `category_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã danh mục |
| `user_id` | `INT` | `FK -> users(user_id)`, `NULL`, `CASCADE` | Người sở hữu (`NULL` nếu là mặc định hệ thống) |
| `category_name` | `VARCHAR(100)` | `NOT NULL` | Tên danh mục (VD: Ăn uống, Mua sắm) |
| `category_type` | `VARCHAR(20)` | `NOT NULL` | Loại danh mục: `expense` (chi) hoặc `income` (thu) |
| `icon` | `VARCHAR(100)` | `NULL` | Tên icon drawable Android |
| `color` | `VARCHAR(20)` | `NULL` | Mã màu HEX (`#FF9800`, `#2196F3`...) |
| `is_default` | `BOOLEAN` | `DEFAULT FALSE` | Cờ đánh dấu danh mục mặc định |

### 4. Bảng `transactions` (Giao dịch thu/chi)
Lưu nhật ký chi tiêu và thu nhập chi tiết theo thời gian thực.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `transaction_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã giao dịch |
| `account_id` | `INT` | `FK -> accounts(account_id)`, `CASCADE` | Tài khoản phát sinh giao dịch |
| `category_id` | `INT` | `FK -> categories(category_id)`, `RESTRICT` | Danh mục tương ứng |
| `amount` | `DECIMAL(15,2)` | `NOT NULL` | Số tiền giao dịch |
| `transaction_type` | `VARCHAR(20)`| `NOT NULL` | `expense` (chi tiêu) hoặc `income` (thu nhập)|
| `transaction_date` | `DATETIME` | `NOT NULL` | Thời điểm phát sinh giao dịch |
| `note` | `TEXT` | `NULL` | Ghi chú chi tiết |
| `source` | `VARCHAR(30)` | `DEFAULT 'manual'` | Nguồn tạo (`manual`, `ocr`, `product_ai`) |

### 5. Bảng `budgets` (Ngân sách chi tiêu)
Quản lý hạn mức chi tiêu theo tháng và danh mục, phục vụ tính năng cảnh báo chi tiêu vượt ngưỡng.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `budget_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã ngân sách |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thiết lập ngân sách |
| `category_id` | `INT` | `FK -> categories(category_id)`, `RESTRICT` | Danh mục áp dụng hạn mức |
| `amount_limit` | `DECIMAL(15,2)` | `NOT NULL` | Số tiền tối đa cho phép chi |
| `month` | `INT` | `NOT NULL (1 - 12)` | Tháng áp dụng |
| `year` | `INT` | `NOT NULL` | Năm áp dụng |

### 6. Bảng `recurring_transactions` (Giao dịch định kỳ)
Lên lịch tự động ghi nhận các khoản thu/chi theo chu kỳ (Tiền thuê nhà, tiền cước mạng, tiền lương...).
| Cột | Kiểu dữ liệu | Diễn giải |
| :--- | :--- | :--- |
| `recurring_id` | `INT (PK)` | Mã lịch định kỳ |
| `frequency` | `VARCHAR(20)` | Tần suất lặp: `daily`, `weekly`, `monthly`, `yearly` |
| `interval_count` | `INT DEFAULT 1` | Bước nhảy chu kỳ (VD: Mỗi 2 tuần, Mỗi 1 tháng) |
| `start_date` / `end_date` | `DATE` | Khoảng thời gian hiệu lực |
| `is_active` | `BOOLEAN` | Trạng thái kích hoạt |

### 7. Bảng `transaction_images` (Hình ảnh chứng từ đính kèm)
Lưu trữ ảnh hóa đơn hoặc ảnh chụp sản phẩm phục vụ lưu trữ chứng từ chi tiêu.
| Cột | Kiểu dữ liệu | Diễn giải |
| :--- | :--- | :--- |
| `image_id` | `INT (PK)` | Mã ảnh |
| `transaction_id` | `INT (FK)` | Giao dịch đính kèm |
| `image_url` | `VARCHAR(255)` | Đường dẫn URL hình ảnh tĩnh trên máy chủ |

### 8. Bảng `ai_scan_logs` & `ai_product_logs` (Nhật ký AI)
Lưu vết các tác vụ Computer Vision & OCR để đánh giá độ chính xác của mô hình và phục vụ huấn luyện tiếp (Active Learning).
- `ai_scan_logs`: Lưu kết quả OCR hóa đơn (tổng tiền nhận diện, ngày nhận diện, text thô trích xuất từ hóa đơn, độ tin cậy).
- `ai_product_logs`: Lưu kết quả nhận diện vật thể YOLO và xác suất phân loại danh mục từ mô hình Random Forest.

---

## 🌱 Dữ liệu mẫu ban đầu (`seed_data.sql`)

Khi hệ thống khởi chạy lần đầu, 11 danh mục mặc định được nạp sẵn:
- **Khoản chi (Expense):**
  - Ăn uống (`ic_food`, `#FF9800`)
  - Di chuyển (`ic_transport`, `#2196F3`)
  - Mua sắm (`ic_shopping`, `#E91E63`)
  - Hóa đơn (`ic_bill`, `#9C27B0`)
  - Giải trí (`ic_entertainment`, `#673AB7`)
  - Sức khỏe (`ic_health`, `#4CAF50`)
  - Khác (`ic_other`, `#607D8B`)
- **Khoản thu (Income):**
  - Lương (`ic_salary`, `#4CAF50`)
  - Thưởng (`ic_bonus`, `#FFC107`)
  - Đầu tư (`ic_investment`, `#009688`)
  - Khác (`ic_other`, `#607D8B`)

---

## 🔌 Hướng dẫn kết nối cơ sở dữ liệu

### 1. Kết nối qua Docker Compose (Mặc định của dự án)
Database chạy trong container `finance_db`, tự động ánh xạ cổng `3306`:
```yaml
Host: localhost (hoặc 127.0.0.1)
Port: 3306
Database: personal_finance_app
Username: finance_user   (hoặc root)
Password: finance_pass   (hoặc rootpassword)
```

### 2. Kết nối bằng DBeaver / MySQL Workbench
1. Chọn **New Connection** -> **MySQL**.
2. Nhập thông tin:
   - **Host:** `localhost`
   - **Port:** `3306`
   - **Database:** `personal_finance_app`
   - **Username:** `root`
   - **Password:** `rootpassword`
3. Tại tab **Driver Properties**:
   - `allowPublicKeyRetrieval`: `true`
   - `useSSL`: `false`
4. Nhấn **Test Connection** -> **Finish**.

### 3. Kết nối từ dòng lệnh (CLI qua Docker)
```bash
docker exec -it finance_db mysql -u root -prootpassword personal_finance_app
```

---

## 🛠️ Bảo trì & Sao lưu dữ liệu (Backup & Restore)

### Sao lưu CSDL ra file SQL:
```bash
docker exec finance_db mysqldump -u root -prootpassword --default-character-set=utf8mb4 personal_finance_app > backup_finance.sql
```

### Phục hồi CSDL từ file SQL:
```bash
docker exec -i finance_db mysql -u root -prootpassword --default-character-set=utf8mb4 personal_finance_app < backup_finance.sql
```
