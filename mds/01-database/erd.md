# SƠ ĐỒ THỰC THỂ QUAN HỆ (ERD TOÀN HỆ THỐNG)

**Hệ thống:** Personal Finance App  
**Cơ sở dữ liệu:** `personal_finance_app` (MySQL 8.0)  
**Nguồn sự thật:** `database/schema.sql:10-213`  

---

## 1. Sơ đồ Mermaid erDiagram (9 Bảng dữ liệu)

```mermaid
erDiagram
    users ||--o{ accounts : "1 User sở hữu N Ví (CASCADE)"
    users ||--o{ categories : "1 User tạo N Danh mục riêng (CASCADE)"
    users ||--o{ transactions : "1 User có N Giao dịch (CASCADE)"
    users ||--o{ budgets : "1 User đặt N Ngân sách (CASCADE)"
    users ||--o{ recurring_transactions : "1 User lập N Lịch định kỳ (CASCADE)"
    users ||--o{ ai_scan_logs : "1 User thực hiện N lần quét OCR (CASCADE)"
    users ||--o{ ai_product_logs : "1 User thực hiện N lần quét YOLO (CASCADE)"

    accounts ||--o{ transactions : "1 Ví ghi nhận N Giao dịch (RESTRICT)"
    accounts ||--o{ recurring_transactions : "1 Ví gắn với N Lịch định kỳ (RESTRICT)"

    categories ||--o{ transactions : "1 Danh mục phân loại N Giao dịch (RESTRICT)"
    categories ||--o{ budgets : "1 Danh mục áp dụng Ngân sách (SET NULL)"
    categories ||--o{ recurring_transactions : "1 Danh mục gắn với Lịch định kỳ (SET NULL)"
    categories ||--o{ ai_scan_logs : "1 Danh mục gợi ý / xác nhận OCR (SET NULL)"
    categories ||--o{ ai_product_logs : "1 Danh mục gợi ý từ YOLO (RESTRICT)"

    transactions ||--o{ transaction_images : "1 Giao dịch có N Ảnh hóa đơn (CASCADE)"
    transactions ||--o| ai_scan_logs : "1 Giao dịch tạo từ log OCR (SET NULL)"
    transactions ||--o| ai_product_logs : "1 Giao dịch tạo từ log YOLO (SET NULL)"

    users {
        int user_id PK "Khóa chính tự tăng"
        varchar firebase_uid UK "Mã UID từ Firebase Auth"
        varchar full_name "Họ và tên người dùng"
        varchar email UK "Email đăng nhập"
        varchar phone "Số điện thoại"
        text avatar_url "Đường dẫn ảnh đại diện"
        varchar password_hash "Mật khẩu mã hóa BCrypt"
        varchar auth_provider "Nhà cung cấp xác thực"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật"
    }

    accounts {
        int account_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng sở hữu"
        varchar account_name "Tên ví"
        varchar account_type "Loại ví: cash, bank, e-wallet"
        decimal balance "Số dư hiện tại"
        varchar currency "Tiền tệ: VND"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm sửa đổi"
    }

    categories {
        int category_id PK "Khóa chính tự tăng"
        int user_id FK "Chủ sở hữu (NULL nếu mặc định)"
        varchar category_name "Tên danh mục"
        varchar category_type "expense hoặc income"
        varchar icon "Tên drawable icon"
        varchar color "Mã màu Hex"
        boolean is_default "Cờ danh mục hệ thống"
        datetime created_at "Thời điểm tạo"
    }

    transactions {
        int transaction_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng tạo"
        int account_id FK "Ví thanh toán nguồn"
        int category_id FK "Danh mục thu chi"
        varchar title "Tiêu đề giao dịch"
        decimal amount "Số tiền giao dịch"
        varchar transaction_type "expense hoặc income"
        date transaction_date "Ngày phát sinh giao dịch"
        text note "Ghi chú giao dịch"
        varchar status "Trạng thái: confirmed, pending"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm sửa đổi"
    }

    budgets {
        int budget_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng thiết lập"
        int category_id FK "Danh mục áp dụng (NULL: toàn ví)"
        varchar budget_name "Tên kế hoạch ngân sách"
        decimal amount_limit "Hạn mức tối đa"
        decimal daily_amount_limit "Hạn mức chi tiêu mỗi ngày"
        decimal spent_amount "Số tiền đã chi tiêu tích lũy"
        date start_date "Ngày bắt đầu chu kỳ"
        date end_date "Ngày kết thúc chu kỳ"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật"
    }

    recurring_transactions {
        int recurring_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng thiết lập"
        int account_id FK "Ví nguồn thanh toán"
        int category_id FK "Danh mục áp dụng"
        varchar title "Tiêu đề giao dịch"
        decimal amount "Số tiền mỗi kỳ"
        varchar transaction_type "expense hoặc income"
        varchar repeat_type "daily, weekly, monthly, yearly"
        int repeat_interval "Khoảng cách lặp (mặc định 1)"
        date start_date "Ngày bắt đầu lặp"
        date end_date "Ngày kết thúc lặp"
        date next_run_date "Ngày thực thi tiếp theo"
        text note "Ghi chú"
        boolean is_active "Trạng thái kích hoạt"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật"
    }

    transaction_images {
        int image_id PK "Khóa chính tự tăng"
        int transaction_id FK "Giao dịch đính kèm"
        text image_url "Đường dẫn tệp ảnh"
        text ocr_text "Chuỗi văn bản OCR thô"
        datetime uploaded_at "Thời điểm tải ảnh"
    }

    ai_scan_logs {
        int ai_scan_log_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng quét"
        int transaction_id FK "Giao dịch tạo ra (nếu có)"
        text raw_ocr_text "Chuỗi văn bản OCR thô"
        varchar detected_merchant "Tên cửa hàng phát hiện"
        decimal detected_amount "Tổng tiền phát hiện"
        date detected_date "Ngày phát sinh phát hiện"
        int suggested_category_id FK "Danh mục AI gợi ý (RF)"
        int actual_category_id FK "Danh mục thực tế người dùng chọn"
        boolean was_corrected "Cờ đánh dấu người dùng sửa gợi ý"
        datetime confirmed_at "Thời điểm xác nhận"
        decimal confidence_score "Độ tin cậy dự đoán AI"
        datetime created_at "Thời điểm tạo log"
    }

    ai_product_logs {
        int ai_product_log_id PK "Khóa chính tự tăng"
        int user_id FK "Người dùng quét"
        int transaction_id FK "Giao dịch tạo ra (nếu có)"
        text raw_yolo_json "JSON kết quả nhận diện YOLOv8n"
        int suggested_category_id FK "Danh mục gợi ý từ nhãn vật phẩm"
        datetime created_at "Thời điểm nhận diện"
    }
```

---

## 2. Ma trận Cardinality và Ràng buộc toàn vẹn tham chiếu

| Bảng cha | Bản số | Bảng con | Khóa ngoại (FK) | Ràng buộc ON DELETE | Giải thích nghiệp vụ |
| :--- | :---: | :--- | :--- | :--- | :--- |
| `users` | 1 : N | `accounts` | `accounts.user_id` | **CASCADE** | Xóa User sẽ xóa sạch toàn bộ ví tài sản của họ |
| `users` | 1 : N | `categories` | `categories.user_id` | **CASCADE** | Xóa User sẽ xóa các danh mục cá nhân của họ |
| `users` | 1 : N | `transactions` | `transactions.user_id` | **CASCADE** | Xóa User sẽ xóa sạch lịch sử giao dịch phát sinh |
| `users` | 1 : N | `budgets` | `budgets.user_id` | **CASCADE** | Xóa User sẽ xóa các kế hoạch ngân sách |
| `users` | 1 : N | `recurring_transactions` | `recurring_transactions.user_id` | **CASCADE** | Xóa User sẽ hủy các lịch chạy định kỳ |
| `users` | 1 : N | `ai_scan_logs` | `ai_scan_logs.user_id` | **CASCADE** | Xóa User sẽ xóa log quét hóa đơn cá nhân |
| `users` | 1 : N | `ai_product_logs` | `ai_product_logs.user_id` | **CASCADE** | Xóa User sẽ xóa log quét vật phẩm cá nhân |
| `accounts` | 1 : N | `transactions` | `transactions.account_id` | **RESTRICT** | Không cho phép xóa ví khi còn giao dịch liên kết |
| `accounts` | 1 : N | `recurring_transactions` | `recurring_transactions.account_id` | **RESTRICT** | Không cho phép xóa ví khi còn lịch định kỳ gắn với ví |
| `categories` | 1 : N | `transactions` | `transactions.category_id` | **RESTRICT** | Không cho phép xóa danh mục khi còn giao dịch phát sinh |
| `categories` | 1 : N | `budgets` | `budgets.category_id` | **SET NULL** | Xóa danh mục thì ngân sách chuyển thành ngân sách chung |
| `categories` | 1 : N | `recurring_transactions` | `recurring_transactions.category_id` | **SET NULL** | Xóa danh mục thì lịch định kỳ giữ nguyên số tiền, category_id = NULL |
| `categories` | 1 : N | `ai_scan_logs` | `ai_scan_logs.suggested_category_id` | **SET NULL** | Xóa danh mục thì log gợi ý OCR chuyển category_id = NULL |
| `categories` | 1 : N | `ai_product_logs` | `ai_product_logs.suggested_category_id` | **RESTRICT** | Nhãn vật phẩm YOLO bắt buộc phải thuộc một danh mục hợp lệ |
| `transactions` | 1 : 1..N | `transaction_images` | `transaction_images.transaction_id` | **CASCADE** | Xóa giao dịch sẽ tự động xóa các ảnh chứng từ đính kèm |
| `transactions` | 1 : 1 | `ai_scan_logs` | `ai_scan_logs.transaction_id` | **SET NULL** | Xóa giao dịch thì log OCR vẫn giữ lại dataset huấn luyện AI |
| `transactions` | 1 : 1 | `ai_product_logs` | `ai_product_logs.transaction_id` | **SET NULL** | Xóa giao dịch thì log YOLO vẫn giữ lại dữ liệu thống kê |
