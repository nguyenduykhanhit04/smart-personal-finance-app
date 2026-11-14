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
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thực hiện giao dịch |
| `account_id` | `INT` | `FK -> accounts(account_id)`, `RESTRICT` | Tài khoản/Ví phát sinh giao dịch |
| `category_id` | `INT` | `FK -> categories(category_id)`, `RESTRICT` | Danh mục tương ứng |
| `title` | `VARCHAR(150)` | `NOT NULL` | Tên/tiêu đề giao dịch |
| `amount` | `DECIMAL(15,2)` | `NOT NULL` | Số tiền giao dịch |
| `transaction_type` | `VARCHAR(20)`| `NOT NULL` | `expense` (chi tiêu) hoặc `income` (thu nhập)|
| `transaction_date` | `DATE` | `NOT NULL` | Ngày phát sinh giao dịch |
| `note` | `TEXT` | `NULL` | Ghi chú chi tiết |
| `status` | `VARCHAR(20)` | `DEFAULT 'confirmed'` | Trạng thái giao dịch (`confirmed`, `pending`)|
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm tạo bản ghi |
| `updated_at` | `DATETIME` | `ON UPDATE CURRENT_TIMESTAMP`| Thời điểm cập nhật cuối |

### 5. Bảng `budgets` (Ngân sách chi tiêu)
Quản lý hạn mức chi tiêu theo khoảng thời gian và danh mục, phục vụ tính năng cảnh báo chi tiêu vượt ngưỡng.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `budget_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã ngân sách |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thiết lập ngân sách |
| `category_id` | `INT` | `FK -> categories(category_id)`, `SET NULL` | Danh mục áp dụng (`NULL` nếu áp dụng chung) |
| `budget_name` | `VARCHAR(100)` | `NOT NULL` | Tên ngân sách |
| `amount_limit` | `DECIMAL(15,2)` | `NOT NULL` | Hạn mức chi tiêu tối đa |
| `daily_amount_limit` | `DECIMAL(15,2)` | `NULL` | Hạn mức chi tiêu ngày khuyến nghị |
| `spent_amount` | `DECIMAL(15,2)` | `DEFAULT 0` | Số tiền đã chi tiêu thực tế |
| `start_date` | `DATE` | `NOT NULL` | Ngày bắt đầu áp dụng |
| `end_date` | `DATE` | `NOT NULL` | Ngày kết thúc áp dụng |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm tạo |
| `updated_at` | `DATETIME` | `ON UPDATE CURRENT_TIMESTAMP`| Thời điểm cập nhật cuối |

### 6. Bảng `recurring_transactions` (Giao dịch định kỳ)
Lên lịch tự động ghi nhận các khoản thu/chi theo chu kỳ (Tiền thuê nhà, cước mạng, tiền điện, lương...).
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `recurring_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã lịch định kỳ |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thiết lập |
| `account_id` | `INT` | `FK -> accounts(account_id)`, `RESTRICT` | Ví thanh toán tự động |
| `category_id` | `INT` | `FK -> categories(category_id)`, `SET NULL` | Danh mục chi tiêu tương ứng |
| `title` | `VARCHAR(150)` | `NOT NULL` | Tiêu đề giao dịch lặp |
| `amount` | `DECIMAL(15,2)` | `NOT NULL` | Số tiền định kỳ |
| `transaction_type`| `VARCHAR(20)` | `NOT NULL` | Loại giao dịch: `expense` hoặc `income` |
| `repeat_type` | `VARCHAR(20)` | `NOT NULL` | Chu kỳ lặp: `daily`, `weekly`, `monthly`, `yearly` |
| `repeat_interval`| `INT` | `DEFAULT 1` | Bước nhảy chu kỳ (VD: Mỗi 1 tháng, Mỗi 2 tuần) |
| `start_date` | `DATE` | `NOT NULL` | Ngày bắt đầu hiệu lực |
| `end_date` | `DATE` | `NULL` | Ngày kết thúc hiệu lực |
| `next_run_date` | `DATE` | `NOT NULL` | Ngày chạy tiếp theo (quét bởi Cronjob 00:00) |
| `note` | `TEXT` | `NULL` | Ghi chú |
| `is_active` | `BOOLEAN` | `DEFAULT TRUE` | Trạng thái kích hoạt |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm tạo |
| `updated_at` | `DATETIME` | `ON UPDATE CURRENT_TIMESTAMP`| Thời điểm cập nhật |

### 7. Bảng `transaction_images` (Hình ảnh chứng từ đính kèm)
Lưu trữ ảnh hóa đơn hoặc ảnh chụp sản phẩm phục vụ lưu trữ chứng từ chi tiêu.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `image_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã ảnh |
| `transaction_id` | `INT` | `FK -> transactions(transaction_id)`, `CASCADE`| Giao dịch đính kèm |
| `image_url` | `TEXT` | `NOT NULL` | Đường dẫn ảnh hoặc URL lưu trữ |
| `ocr_text` | `TEXT` | `NULL` | Văn bản bóc tách từ ảnh nếu có |
| `uploaded_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm tải lên |

### 8. Bảng `ai_scan_logs` (Nhật ký Quét hóa đơn OCR + Gemini)
Lưu trữ lịch sử OCR và bóc tách thực thể từ hóa đơn thanh toán.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `ai_scan_log_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã nhật ký OCR |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thực hiện quét |
| `transaction_id` | `INT` | `FK -> transactions`, `SET NULL` | Giao dịch tạo từ hóa đơn (nếu đã lưu) |
| `raw_ocr_text` | `TEXT` | `NULL` | Văn bản thô bóc tách từ Google ML Kit |
| `detected_merchant`| `VARCHAR(150)` | `NULL` | Tên cửa hàng/đơn vị do Gemini bóc tách |
| `detected_amount` | `DECIMAL(15,2)` | `NULL` | Số tiền tổng cộng bóc tách |
| `detected_date` | `DATE` | `NULL` | Ngày giao dịch trên hóa đơn |
| `suggested_category_id` | `INT` | `FK -> categories`, `SET NULL` | Danh mục đề xuất ban đầu |
| `actual_category_id` | `INT` | `FK -> categories`, `SET NULL` | Danh mục người dùng chọn thực tế |
| `was_corrected` | `BOOLEAN` | `DEFAULT FALSE` | Cờ đánh dấu người dùng có chỉnh sửa không |
| `confirmed_at` | `DATETIME` | `NULL` | Thời điểm người dùng xác nhận giao dịch |
| `confidence_score`| `DECIMAL(5,4)` | `NULL` | Độ tin cậy dự đoán (0.0000 - 1.0000) |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm quét |

### 9. Bảng `ai_product_logs` (Nhật ký Camera AI & Random Forest)
Lưu trữ lịch sử nhận diện vật thể mua sắm và phân loại danh mục On-Device / ONNX.
| Cột | Kiểu dữ liệu | Ràng buộc | Diễn giải |
| :--- | :--- | :--- | :--- |
| `ai_product_log_id` | `INT` | `PK`, `AUTO_INCREMENT` | Mã nhật ký Camera AI |
| `user_id` | `INT` | `FK -> users(user_id)`, `CASCADE` | Người thực hiện quét |
| `transaction_id` | `INT` | `FK -> transactions`, `SET NULL` | Giao dịch liên kết |
| `raw_yolo_json` | `TEXT` | `NOT NULL` | JSON danh sách vật thể YOLO phát hiện |
| `suggested_category_id` | `INT` | `FK -> categories`, `RESTRICT` | Danh mục do Random Forest ONNX gợi ý |
| `created_at` | `DATETIME` | `DEFAULT CURRENT_TIMESTAMP` | Thời điểm nhận diện |


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

---

## 👨‍🎓 Thông tin Đề tài Đồ án Tốt nghiệp
- **Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android
- **Sinh viên thực hiện:** Nguyễn Duy Khánh — Mã SV: 2255010114 — Lớp: 22CN4
- **Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng
- **Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội
