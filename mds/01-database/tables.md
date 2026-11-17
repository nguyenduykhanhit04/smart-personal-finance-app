# TỪ ĐIỂN DỮ LIỆU TOÀN DIỆN (DATA DICTIONARY)
## NGUỒN SỰ THẬT DUY NHẤT CHO CƠ SỞ DỮ LIỆU `personal_finance_app`

**Truy vết mã nguồn:** `database/schema.sql:1-213`  
**Hệ quản trị CSDL:** MySQL 8.0+  
**Engine:** InnoDB  
**Bảng mã:** `utf8mb4` | **Collation:** `utf8mb4_unicode_ci`  

---

## Danh sách 9 Bảng trong Cơ sở dữ liệu

1. [`users`](#1-bảng-users-thông-tin-người-dùng)
2. [`accounts`](#2-bảng-accounts-ví--tài-khoản-tài-chính)
3. [`categories`](#3-bảng-categories-danh-mục-thu-chi)
4. [`transactions`](#4-bảng-transactions-giao-dịch-thu-chi)
5. [`budgets`](#5-bảng-budgets-hạn-mức-ngân-sách)
6. [`recurring_transactions`](#6-bảng-recurring_transactions-giao-dịch-định-kỳ)
7. [`transaction_images`](#7-bảng-transaction_images-hình-ảnh-giao-dịch)
8. [`ai_scan_logs`](#8-bảng-ai_scan_logs-nhật-ký-quét-hóa-đơn-ocr)
9. [`ai_product_logs`](#9-bảng-ai_product_logs-nhật-ký-nhận-diện-vật-phẩm-yolo)

---

### 1. Bảng `users` (Thông tin Người dùng)
* **Mục đích:** Lưu trữ hồ sơ người dùng, thông tin liên kết Firebase Authentication và phương thức xác thực.
* **Truy vết schema:** `database/schema.sql:10-21`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/User.java:12-58`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `user_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `firebase_uid` | VARCHAR(128) | YES | NULL | **UK** | Mã định danh người dùng từ Firebase Auth (Unique) |
| `full_name` | VARCHAR(100) | YES | NULL | - | Họ và tên đầy đủ |
| `email` | VARCHAR(150) | NO | - | **UK** | Địa chỉ email đăng nhập (Unique, Not Null) |
| `phone` | VARCHAR(20) | YES | NULL | - | Số điện thoại liên hệ |
| `avatar_url` | TEXT | YES | NULL | - | Đường dẫn ảnh đại diện (local URL `/uploads/...` hoặc remote) |
| `password_hash` | VARCHAR(255) | YES | NULL | - | Mật khẩu mã hóa BCrypt (dự phòng) |
| `auth_provider` | VARCHAR(30) | YES | `'firebase'` | - | Nhà cung cấp: `'firebase'`, `'google'`, `'demo'` |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo tài khoản |
| `updated_at` | DATETIME | YES | `CURRENT_TIMESTAMP ON UPDATE` | - | Thời điểm sửa đổi hồ sơ |

---

### 2. Bảng `accounts` (Ví & Tài khoản Tài chính)
* **Mục đích:** Quản lý các nguồn tiền cá nhân (Tiền mặt, Ngân hàng, Ví điện tử) và số dư hiện tại của người dùng.
* **Truy vết schema:** `database/schema.sql:23-36`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/Account.java:13-53`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `account_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng định danh ví |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `account_name` | VARCHAR(100) | NO | - | - | Tên ví (ví dụ: 'Ví tiền mặt', 'Techcombank') |
| `account_type` | VARCHAR(50) | YES | NULL | - | Phân loại: `'cash'`, `'bank'`, `'e-wallet'` |
| `balance` | DECIMAL(15,2) | YES | `0.00` | - | Số dư khả dụng hiện tại (hỗ trợ số tiền lớn) |
| `currency` | VARCHAR(10) | YES | `'VND'` | - | Mã đơn vị tiền tệ |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm khởi tạo ví |
| `updated_at` | DATETIME | YES | `CURRENT_TIMESTAMP ON UPDATE` | - | Thời điểm số dư biến động gần nhất |

* **Chỉ mục (Index):**
  - `idx_accounts_user_id`: B-Tree trên cột `user_id` (`schema.sql:184`)

---

### 3. Bảng `categories` (Danh mục Thu Chi)
* **Mục đích:** Phân loại các khoản chi tiêu và nguồn thu, hỗ trợ 11 danh mục mặc định của hệ thống và danh mục cá nhân tự tạo.
* **Truy vết schema:** `database/schema.sql:38-51`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/Category.java:13-53`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `category_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | YES | NULL | **FK** | Tham chiếu `users(user_id)`. NULL nếu là danh mục mặc định. **ON DELETE CASCADE** |
| `category_name` | VARCHAR(100) | NO | - | - | Tên danh mục (Ăn uống, Di chuyển, Lương, Mua sắm,...) |
| `category_type` | VARCHAR(20) | NO | - | - | Loại: `'expense'` (chi tiêu) hoặc `'income'` (thu nhập) |
| `icon` | VARCHAR(100) | YES | NULL | - | Tên mã biểu tượng drawable (ví dụ: `'ic_food'`) |
| `color` | VARCHAR(20) | YES | NULL | - | Mã màu Hex (ví dụ: `'#FF9800'`) |
| `is_default` | BOOLEAN | YES | `FALSE` | - | Cờ đánh dấu: `TRUE` nếu là danh mục hệ thống tạo sẵn |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo danh mục |

* **Chỉ mục (Index):**
  - `idx_categories_user_id`: B-Tree trên cột `user_id` (`schema.sql:185`)

---

### 4. Bảng `transactions` (Giao dịch Thu Chi)
* **Mục đích:** Lưu trữ toàn bộ lịch sử biến động dòng tiền, liên kết ví nguồn và danh mục phân loại.
* **Truy vết schema:** `database/schema.sql:53-76`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/Transaction.java:13-68`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `transaction_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `account_id` | INT | NO | - | **FK** | Tham chiếu `accounts(account_id)`. **ON DELETE RESTRICT** |
| `category_id` | INT | YES | NULL | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE RESTRICT** |
| `title` | VARCHAR(150) | NO | - | - | Tiêu đề giao dịch |
| `amount` | DECIMAL(15,2) | NO | - | - | Số tiền giao dịch (> 0) |
| `transaction_type`| VARCHAR(20) | NO | - | - | Loại giao dịch: `'expense'` hoặc `'income'` |
| `transaction_date`| DATE | NO | - | - | Ngày phát sinh giao dịch (YYYY-MM-DD, không được trong tương lai) |
| `note` | TEXT | YES | NULL | - | Ghi chú giao dịch |
| `status` | VARCHAR(20) | YES | `'confirmed'` | - | Trạng thái: `'confirmed'`, `'pending'` |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo bản ghi |
| `updated_at` | DATETIME | YES | `CURRENT_TIMESTAMP ON UPDATE` | - | Thời điểm sửa đổi gần nhất |

* **Chỉ mục (Index):**
  - `idx_transactions_user_id`: B-Tree trên cột `user_id` (`schema.sql:186`)
  - `idx_transactions_account_id`: B-Tree trên cột `account_id` (`schema.sql:187`)
  - `idx_transactions_transaction_date`: B-Tree trên cột `transaction_date` (`schema.sql:188`)

---

### 5. Bảng `budgets` (Hạn mức Ngân sách)
* **Mục đích:** Thiết lập hạn mức chi tiêu trong chu kỳ tháng và theo dõi số tiền đã chi tiêu tích lũy.
* **Truy vết schema:** `database/schema.sql:78-98`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/Budget.java:13-63`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `budget_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `category_id` | INT | YES | NULL | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| `budget_name` | VARCHAR(100) | NO | - | - | Tên kế hoạch ngân sách |
| `amount_limit` | DECIMAL(15,2) | NO | - | - | Hạn mức tổng cộng trong chu kỳ |
| `daily_amount_limit`| DECIMAL(15,2)| YES | NULL | - | Hạn mức cho phép chi mỗi ngày |
| `spent_amount` | DECIMAL(15,2) | YES | `0.00` | - | Số tiền đã chi tích lũy thực tế |
| `start_date` | DATE | NO | - | - | Ngày bắt đầu chu kỳ |
| `end_date` | DATE | NO | - | - | Ngày kết thúc chu kỳ |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo kế hoạch |
| `updated_at` | DATETIME | YES | `CURRENT_TIMESTAMP ON UPDATE` | - | Thời điểm cập nhật tiến độ |

* **Chỉ mục (Index):**
  - `idx_budgets_user_id`: B-Tree trên cột `user_id` (`schema.sql:189`)

---

### 6. Bảng `recurring_transactions` (Giao dịch Định kỳ)
* **Mục đích:** Cấu hình các giao dịch tự động lặp lại theo chu kỳ thời gian (tiền nhà, tiền mạng, tiền lương).
* **Truy vết schema:** `database/schema.sql:144-181`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/RecurringTransaction.java:13-79`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `recurring_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `account_id` | INT | NO | - | **FK** | Tham chiếu `accounts(account_id)`. **ON DELETE RESTRICT** |
| `category_id` | INT | YES | NULL | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| `title` | VARCHAR(150) | NO | - | - | Tiêu đề giao dịch định kỳ |
| `amount` | DECIMAL(15,2) | NO | - | - | Số tiền cố định mỗi kỳ |
| `transaction_type`| VARCHAR(20) | NO | - | - | Loại: `'expense'` hoặc `'income'` |
| `repeat_type` | VARCHAR(20) | NO | - | - | Chu kỳ: `'daily'`, `'weekly'`, `'monthly'`, `'yearly'` |
| `repeat_interval`| INT | YES | `1` | - | Khoảng cách lặp (ví dụ: 1 tháng/lần) |
| `start_date` | DATE | NO | - | - | Ngày bắt đầu áp dụng |
| `end_date` | DATE | YES | NULL | - | Ngày kết thúc (NULL nếu lặp vô hạn) |
| `next_run_date` | DATE | NO | - | - | Ngày hệ thống sẽ tự động quét thực thi tiếp theo |
| `note` | TEXT | YES | NULL | - | Ghi chú giao dịch |
| `is_active` | BOOLEAN | YES | `TRUE` | - | Trạng thái: `TRUE` (đang chạy), `FALSE` (tạm dừng) |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo |
| `updated_at` | DATETIME | YES | `CURRENT_TIMESTAMP ON UPDATE` | - | Thời điểm sửa cấu hình |

---

### 7. Bảng `transaction_images` (Hình ảnh Giao dịch)
* **Mục đích:** Lưu trữ tệp ảnh hóa đơn chứng từ đính kèm với giao dịch và chuỗi văn bản OCR thô trích xuất từ ảnh.
* **Truy vết schema:** `database/schema.sql:100-110`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/TransactionImage.java:13-40`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `image_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `transaction_id` | INT | NO | - | **FK** | Tham chiếu `transactions(transaction_id)`. **ON DELETE CASCADE** |
| `image_url` | TEXT | NO | - | - | Đường dẫn tệp ảnh URL (ví dụ: `/uploads/...`) |
| `ocr_text` | TEXT | YES | NULL | - | Chuỗi văn bản thô trích xuất được từ OCR |
| `uploaded_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tải ảnh lên |

---

### 8. Bảng `ai_scan_logs` (Nhật ký Quét Hóa đơn OCR)
* **Mục đích:** Lưu vết kết quả bóc tách thông tin hóa đơn từ OCR, đo lường độ chính xác và phục vụ tái huấn luyện mô hình.
* **Truy vết schema:** `database/schema.sql:112-142`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/AiScanLog.java:13-74`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `ai_scan_log_id` | INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `transaction_id` | INT | YES | NULL | **FK** | Tham chiếu `transactions(transaction_id)`. **ON DELETE SET NULL** |
| `raw_ocr_text` | TEXT | YES | NULL | - | Chuỗi văn bản thô bóc tách từ ảnh |
| `detected_merchant`| VARCHAR(150)| YES | NULL | - | Tên đơn vị bán lẻ / cửa hàng phát hiện |
| `detected_amount`| DECIMAL(15,2)| YES | NULL | - | Số tiền tổng cộng phát hiện |
| `detected_date` | DATE | YES | NULL | - | Ngày phát sinh trên hóa đơn |
| `suggested_category_id`| INT | YES | NULL | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| `actual_category_id` | INT | YES | NULL | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| `was_corrected` | BOOLEAN | YES | `FALSE` | - | Đánh dấu người dùng có sửa gợi ý AI hay không |
| `confirmed_at` | DATETIME | YES | NULL | - | Thời điểm người dùng xác nhận |
| `confidence_score`| DECIMAL(5,4)| YES | NULL | - | Độ tin cậy dự đoán (0.0000 - 1.0000) |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm tạo bản ghi log |

---

### 9. Bảng `ai_product_logs` (Nhật ký Nhận diện Vật phẩm YOLO)
* **Mục đích:** Ghi nhận lịch sử phát hiện đồ vật chi tiêu từ mô hình YOLOv8n TFLite 18 lớp.
* **Truy vết schema:** `database/schema.sql:191-209`
* **JPA Entity:** `backend/src/main/java/com/example/financebackend/model/AiProductLog.java:13-47`

| Tên cột | Kiểu dữ liệu | Null | Mặc định | Khóa | Ràng buộc / Ý nghĩa |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `ai_product_log_id`| INT | NO | AUTO_INCREMENT | **PK** | Khóa chính tự tăng |
| `user_id` | INT | NO | - | **FK** | Tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| `transaction_id` | INT | YES | NULL | **FK** | Tham chiếu `transactions(transaction_id)`. **ON DELETE SET NULL** |
| `raw_yolo_json` | TEXT | NO | - | - | JSON chứa danh sách Bounding Boxes, class_id và confidence |
| `suggested_category_id`| INT | NO | - | **FK** | Tham chiếu `categories(category_id)`. **ON DELETE RESTRICT** |
| `created_at` | DATETIME | YES | `CURRENT_TIMESTAMP` | - | Thời điểm nhận diện |

* **Chỉ mục (Index):**
  - `idx_ai_product_logs_transaction_id`: B-Tree trên cột `transaction_id` (`schema.sql:211`)
  - `idx_ai_product_logs_user_id`: B-Tree trên cột `user_id` (`schema.sql:212`)

---

## 3. Đối chiếu số lượng bảng (Cross-Check)

- **Số bảng khai báo trong `database/schema.sql`:** 9 bảng
- **Số bảng đặc tả trong tài liệu `tables.md`:** 9 bảng
- **Kết quả đối chiếu:** Khớp 100%, không thiếu bất kỳ bảng nào, không có bảng thừa.
