# HỒ SƠ THIẾT KẾ CƠ SỞ DỮ LIỆU CHI TIẾT
## ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TÍCH HỢP THỊ GIÁC MÁY TÍNH VÀ AI
**Sinh viên thực hiện:** Nguyễn Duy Khánh - MSV: 2255010114 - Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội  
**Hệ quản trị CSDL:** MySQL 8.0+ (InnoDB Engine, Charset: `utf8mb4`, Collation: `utf8mb4_unicode_ci`)  
**Tên Database:** `personal_finance_app`  
**Phiên bản:** v1.2 (Tháng 11/2026 - Chuẩn thiết kế Database bám sát 100% `database/schema.sql`)

---

## 1. TỔNG QUAN CƠ SỞ DỮ LIỆU

Hệ cơ sở dữ liệu `personal_finance_app` bao gồm **9 bảng dữ liệu** được thiết kế chuẩn hóa theo dạng chuẩn 3NF (Third Normal Form), phân tách rõ ràng trách nhiệm nghiệp vụ giữa các phân hệ:

1. **Nhóm Định danh & Quản lý Tài khoản:** `users`, `accounts`
2. **Nhóm Quản lý Giao dịch & Dòng tiền:** `categories`, `transactions`
3. **Nhóm Kế hoạch Tài chính & Tự động hóa:** `budgets`, `recurring_transactions`
4. **Nhóm Thị giác máy tính & AI:** `transaction_images`, `ai_scan_logs`, `ai_product_logs`

---

## 2. BẢNG ĐẶC TẢ CHI TIẾT 9 BẢNG DỮ LIỆU

### 2.1. Bảng `users` (Thông tin Người dùng)
* **Mục đích:** Lưu trữ hồ sơ định danh người dùng cá nhân, liên kết tài khoản Firebase Authentication và các thiết lập bảo mật.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `user_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh người dùng |
| 2 | `firebase_uid` | VARCHAR(128) | **UK** | NULL | - | Mã định danh người dùng từ Firebase Auth (Unique) |
| 3 | `full_name` | VARCHAR(100) | - | NULL | - | Họ và tên người dùng |
| 4 | `email` | VARCHAR(150) | **UK** | NOT NULL | - | Địa chỉ email đăng nhập (Unique) |
| 5 | `phone` | VARCHAR(20) | - | NULL | - | Số điện thoại liên hệ |
| 6 | `avatar_url` | TEXT | - | NULL | - | Đường dẫn ảnh đại diện URL |
| 7 | `password_hash` | VARCHAR(255) | - | NULL | - | Mật khẩu mã hóa BCrypt (dự phòng) |
| 8 | `auth_provider` | VARCHAR(30) | - | NULL | `'firebase'` | Phương thức xác thực: `'firebase'`, `'google'` |
| 9 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo tài khoản |
| 10 | `updated_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP ON UPDATE` | Thời điểm cập nhật hồ sơ gần nhất |

---

### 2.2. Bảng `accounts` (Ví & Tài khoản Tài chính)
* **Mục đích:** Quản lý các nguồn tiền cá nhân (Tiền mặt, Tài khoản ngân hàng, Ví điện tử) và số dư hiện tại của người dùng.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `account_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh ví |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `account_name` | VARCHAR(100) | - | NOT NULL | - | Tên hiển thị của ví (ví dụ: 'Ví tiền mặt', 'Techcombank') |
| 4 | `account_type` | VARCHAR(50) | - | NULL | - | Loại ví: `'cash'`, `'bank'`, `'e-wallet'` |
| 5 | `balance` | DECIMAL(15,2) | - | NULL | `0.00` | Số dư hiện tại của ví |
| 6 | `currency` | VARCHAR(10) | - | NULL | `'VND'` | Đơn vị tiền tệ |
| 7 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm khởi tạo ví |
| 8 | `updated_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP ON UPDATE` | Thời điểm số dư biến động gần nhất |

---

### 2.3. Bảng `categories` (Danh mục Thu Chi)
* **Mục đích:** Phân loại các khoản chi tiêu và thu nhập, bao gồm cả danh mục mặc định của hệ thống và danh mục tự tạo của người dùng.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `category_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh danh mục |
| 2 | `user_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `users(user_id)`. NULL nếu là danh mục mặc định hệ thống. **ON DELETE CASCADE** |
| 3 | `category_name` | VARCHAR(100) | - | NOT NULL | - | Tên danh mục (Ăn uống, Mua sắm, Di chuyển, Lương,...) |
| 4 | `category_type` | VARCHAR(20) | - | NOT NULL | - | Phân loại danh mục: `'expense'` (chi) hoặc `'income'` (thu) |
| 5 | `icon` | VARCHAR(100) | - | NULL | - | Tên mã biểu tượng drawable (ví dụ: `'ic_food'`, `'ic_salary'`) |
| 6 | `color` | VARCHAR(20) | - | NULL | - | Mã màu trang trí dạng Hex (ví dụ: `'#FF9800'`) |
| 7 | `is_default` | BOOLEAN | - | NULL | `FALSE` | Đánh dấu danh mục mặc định hệ thống |
| 8 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo danh mục |

---

### 2.4. Bảng `transactions` (Lịch sử Giao dịch Thu Chi)
* **Mục đích:** Bảng cốt lõi lưu trữ mọi khoản tiền vào/ra, gắn với người dùng, ví tài khoản và danh mục chi tiêu.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `transaction_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh giao dịch |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `account_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `accounts(account_id)`. **ON DELETE RESTRICT** |
| 4 | `category_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `categories(category_id)`. **ON DELETE RESTRICT** |
| 5 | `title` | VARCHAR(150) | - | NOT NULL | - | Tiêu đề giao dịch |
| 6 | `amount` | DECIMAL(15,2) | - | NOT NULL | - | Số tiền giao dịch (> 0) |
| 7 | `transaction_type` | VARCHAR(20) | - | NOT NULL | - | Loại giao dịch: `'expense'` hoặc `'income'` |
| 8 | `transaction_date` | DATE | - | NOT NULL | - | Ngày phát sinh giao dịch (YYYY-MM-DD) |
| 9 | `note` | TEXT | - | NULL | - | Ghi chú chi tiết nội dung giao dịch |
| 10 | `status` | VARCHAR(20) | - | NULL | `'confirmed'` | Trạng thái: `'confirmed'`, `'pending'` |
| 11 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo bản ghi |
| 12 | `updated_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP ON UPDATE` | Thời điểm sửa đổi gần nhất |

---

### 2.5. Bảng `budgets` (Hạn mức Ngân sách)
* **Mục đích:** Thiết lập giới hạn chi tiêu trong tháng theo từng danh mục và giám sát tiến độ chi tiêu thực tế.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `budget_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh ngân sách |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `category_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| 4 | `budget_name` | VARCHAR(100) | - | NOT NULL | - | Tên ngân sách (ví dụ: 'Ăn uống tháng 11') |
| 5 | `amount_limit` | DECIMAL(15,2) | - | NOT NULL | - | Hạn mức tổng cộng trong chu kỳ |
| 6 | `daily_amount_limit` | DECIMAL(15,2) | - | NULL | - | Hạn mức cho phép chi mỗi ngày |
| 7 | `spent_amount` | DECIMAL(15,2) | - | NULL | `0.00` | Số tiền đã chi tích lũy thực tế |
| 8 | `start_date` | DATE | - | NOT NULL | - | Ngày bắt đầu chu kỳ |
| 9 | `end_date` | DATE | - | NOT NULL | - | Ngày kết thúc chu kỳ |
| 10 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo kế hoạch ngân sách |
| 11 | `updated_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP ON UPDATE` | Thời điểm cập nhật tiến độ chi tiêu |

---

### 2.6. Bảng `recurring_transactions` (Giao dịch Định kỳ)
* **Mục đích:** Cấu hình các giao dịch tự động lặp lại theo chu kỳ thời gian (tiền nhà, tiền điện, lương hàng tháng).

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `recurring_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh giao dịch lặp |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `account_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `accounts(account_id)`. **ON DELETE RESTRICT** |
| 4 | `category_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `categories(category_id)`. **ON DELETE SET NULL** |
| 5 | `title` | VARCHAR(150) | - | NOT NULL | - | Tiêu đề giao dịch định kỳ |
| 6 | `amount` | DECIMAL(15,2) | - | NOT NULL | - | Số tiền cố định |
| 7 | `transaction_type` | VARCHAR(20) | - | NOT NULL | - | Loại: `'expense'` hoặc `'income'` |
| 8 | `repeat_type` | VARCHAR(20) | - | NOT NULL | - | Chu kỳ: `'daily'`, `'weekly'`, `'monthly'`, `'yearly'` |
| 9 | `repeat_interval` | INT | - | NULL | `1` | Khoảng lặp lại (ví dụ 1 tháng/lần) |
| 10 | `start_date` | DATE | - | NOT NULL | - | Ngày bắt đầu lặp |
| 11 | `end_date` | DATE | - | NULL | - | Ngày kết thúc lặp (NULL nếu vô hạn) |
| 12 | `next_run_date` | DATE | - | NOT NULL | - | Ngày hệ thống sẽ tự động quét thực thi tiếp theo |
| 13 | `note` | TEXT | - | NULL | - | Ghi chú giao dịch |
| 14 | `is_active` | BOOLEAN | - | NULL | `TRUE` | Trạng thái đang chạy hay tạm dừng |
| 15 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo |
| 16 | `updated_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP ON UPDATE` | Thời điểm cập nhật |

---

### 2.7. Bảng `transaction_images` (Hình ảnh Hóa đơn)
* **Mục đích:** Lưu trữ tệp ảnh hóa đơn và kết quả nhận diện chữ thô (Raw OCR text) từ Google ML Kit.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `image_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh ảnh |
| 2 | `transaction_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `transactions(transaction_id)`. **ON DELETE CASCADE** |
| 3 | `image_url` | TEXT | - | NOT NULL | - | Đường dẫn URL tệp hình ảnh hóa đơn |
| 4 | `ocr_text` | TEXT | - | NULL | - | Toàn bộ chuỗi văn bản trích xuất được từ ảnh |
| 5 | `uploaded_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tải ảnh lên hệ thống |

---

### 2.8. Bảng `ai_scan_logs` (Nhật ký Bóc tách Hóa đơn AI)
* **Mục đích:** Ghi nhận chi tiết kết quả xử lý hóa đơn của AI, phục vụ đánh giá độ chính xác và Active Learning.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `ai_scan_log_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh log scan |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `transaction_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `transactions(transaction_id)`. **ON DELETE SET NULL** |
| 4 | `raw_ocr_text` | TEXT | - | NULL | - | Chuỗi văn bản thô bóc tách từ OCR |
| 5 | `detected_merchant` | VARCHAR(150) | - | NULL | - | Tên cửa hàng/đơn vị bán lẻ phát hiện được |
| 6 | `detected_amount` | DECIMAL(15,2) | - | NULL | - | Số tiền tổng cộng phát hiện được |
| 7 | `detected_date` | DATE | - | NULL | - | Ngày in trên hóa đơn |
| 8 | `suggested_category_id` | INT | **FK** | NULL | - | Danh mục do Random Forest dự đoán. **ON DELETE SET NULL** |
| 9 | `actual_category_id` | INT | **FK** | NULL | - | Danh mục thực tế do người dùng xác nhận. **ON DELETE SET NULL** |
| 10 | `was_corrected` | BOOLEAN | - | NULL | `FALSE` | Cờ đánh dấu người dùng có chỉnh sửa gợi ý AI hay không |
| 11 | `confirmed_at` | DATETIME | - | NULL | - | Thời điểm người dùng nhấn xác nhận |
| 12 | `confidence_score` | DECIMAL(5,4) | - | NULL | - | Điểm số độ tin cậy của mô hình (0.0000 - 1.0000) |
| 13 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm tạo bản ghi log |

---

### 2.9. Bảng `ai_product_logs` (Nhật ký Nhận diện Vật phẩm YOLO)
* **Mục đích:** Ghi nhận lịch sử phát hiện đồ vật chi tiêu từ mô hình YOLOv8n TFLite 18 lớp.

| STT | Tên cột (Field) | Kiểu dữ liệu | Khóa | Null? | Mặc định | Mô tả ý nghĩa / Ràng buộc |
| :---: | :--- | :--- | :---: | :---: | :--- | :--- |
| 1 | `ai_product_log_id` | INT AUTO_INCREMENT | **PK** | NOT NULL | - | Khóa chính tự tăng định danh log YOLO |
| 2 | `user_id` | INT | **FK** | NOT NULL | - | Khóa ngoại tham chiếu `users(user_id)`. **ON DELETE CASCADE** |
| 3 | `transaction_id` | INT | **FK** | NULL | - | Khóa ngoại tham chiếu `transactions(transaction_id)`. **ON DELETE SET NULL** |
| 4 | `raw_yolo_json` | TEXT | - | NOT NULL | - | Chuỗi JSON chứa danh sách Bounding Boxes, class_id và confidence |
| 5 | `suggested_category_id` | INT | **FK** | NOT NULL | - | Danh mục được ánh xạ từ nhãn vật phẩm. **ON DELETE RESTRICT** |
| 6 | `created_at` | DATETIME | - | NULL | `CURRENT_TIMESTAMP` | Thời điểm nhận diện |

---

## 3. TỐI ƯU HÓA CHỈ MỤC (INDEXES)

| Tên Chỉ mục (Index Name) | Bảng áp dụng | Cột đánh chỉ mục | Mục đích tối ưu hiệu năng |
| :--- | :--- | :--- | :--- |
| `idx_accounts_user_id` | `accounts` | `user_id` | Tăng tốc truy xuất danh sách ví của từng người dùng khi khởi động |
| `idx_categories_user_id` | `categories` | `user_id` | Tối ưu lấy danh mục cá nhân kết hợp danh mục mặc định hệ thống |
| `idx_transactions_user_id` | `transactions` | `user_id` | Tăng tốc độ truy xuất toàn bộ giao dịch của người dùng |
| `idx_transactions_account_id`| `transactions` | `account_id` | Tối ưu lọc danh sách giao dịch theo từng ví cụ thể |
| `idx_transactions_date` | `transactions` | `transaction_date` | Tối ưu truy vấn báo cáo tài chính theo khoảng thời gian (ngày, tháng) |
| `idx_budgets_user_id` | `budgets` | `user_id` | Tăng tốc kiểm tra hạn mức và cảnh báo chi tiêu vượt ngưỡng |
| `idx_ai_product_logs_tx_id` | `ai_product_logs` | `transaction_id` | Tra cứu nhanh log nhận diện vật phẩm gắn với giao dịch |
| `idx_ai_product_logs_user_id`| `ai_product_logs` | `user_id` | Thống kê tần suất nhận diện đồ vật của người dùng |

---

## 4. DỮ LIỆU KHỞI TẠO HỆ THỐNG (SEED DATA - 11 DANH MỤC MẶC ĐỊNH)

| STT | Tên Danh mục | Phân loại (Type) | Icon Drawable | Mã màu Hex | Mặc định (is_default) |
| :---: | :--- | :--- | :--- | :--- | :---: |
| 1 | **Ăn uống** | `expense` (Chi tiêu) | `ic_food` | `#FF9800` (Cam) | `TRUE` |
| 2 | **Di chuyển** | `expense` (Chi tiêu) | `ic_transport` | `#2196F3` (Xanh dương) | `TRUE` |
| 3 | **Mua sắm** | `expense` (Chi tiêu) | `ic_shopping` | `#E91E63` (Hồng) | `TRUE` |
| 4 | **Hóa đơn** | `expense` (Chi tiêu) | `ic_bill` | `#9C27B0` (Tím) | `TRUE` |
| 5 | **Giải trí** | `expense` (Chi tiêu) | `ic_entertainment` | `#673AB7` (Tím đậm) | `TRUE` |
| 6 | **Sức khỏe** | `expense` (Chi tiêu) | `ic_health` | `#4CAF50` (Xanh lá) | `TRUE` |
| 7 | **Khác (Chi)** | `expense` (Chi tiêu) | `ic_other` | `#607D8B` (Xám xanh) | `TRUE` |
| 8 | **Lương** | `income` (Thu nhập) | `ic_salary` | `#4CAF50` (Xanh lá) | `TRUE` |
| 9 | **Thưởng** | `income` (Thu nhập) | `ic_bonus` | `#FFC107` (Vàng) | `TRUE` |
| 10 | **Đầu tư** | `income` (Thu nhập) | `ic_investment` | `#009688` (Xanh ngọc) | `TRUE` |
| 11 | **Khác (Thu)** | `income` (Thu nhập) | `ic_other` | `#607D8B` (Xám xanh) | `TRUE` |
