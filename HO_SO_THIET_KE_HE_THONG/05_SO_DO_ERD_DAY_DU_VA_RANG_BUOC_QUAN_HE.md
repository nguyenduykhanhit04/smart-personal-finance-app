# HỒ SƠ THIẾT KẾ SƠ ĐỒ THỰC THỂ QUAN HỆ (ERD)
## ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TÍCH HỢP THỊ GIÁC MÁY TÍNH VÀ AI
**Sinh viên thực hiện:** Nguyễn Duy Khánh - MSV: 2255010114 - Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội  
**Hệ quản trị CSDL:** MySQL 8.0+ (InnoDB Engine, utf8mb4)  
**Phiên bản:** v1.2 (Tháng 11/2026 - Chuẩn thiết kế ERD đầy đủ 9 bảng bám sát 100% `database/schema.sql`)

---

## 1. SƠ ĐỒ THỰC THỂ QUAN HỆ ĐẦY ĐỦ (FULL ERD DIAGRAM)

Dưới đây là sơ đồ ERD chi tiết thể hiện toàn bộ 9 thực thể, các trường thuộc tính, kiểu dữ liệu, khóa chính (PK), khóa ngoại (FK) và liên kết quan hệ 1:1, 1:N:

```mermaid
erDiagram
    users ||--o{ accounts : "1 người dùng sở hữu N ví (CASCADE)"
    users ||--o{ categories : "1 người dùng tạo N danh mục cá nhân (CASCADE)"
    users ||--o{ transactions : "1 người dùng có N giao dịch (CASCADE)"
    users ||--o{ budgets : "1 người dùng lập N ngân sách (CASCADE)"
    users ||--o{ recurring_transactions : "1 người dùng cấu hình N định kỳ (CASCADE)"
    users ||--o{ ai_scan_logs : "1 người dùng lưu N log scan OCR (CASCADE)"
    users ||--o{ ai_product_logs : "1 người dùng lưu N log scan YOLO (CASCADE)"

    accounts ||--o{ transactions : "1 ví ghi nhận N giao dịch (RESTRICT)"
    accounts ||--o{ recurring_transactions : "1 ví gắn với N lịch định kỳ (RESTRICT)"

    categories ||--o{ transactions : "1 danh mục phân loại N giao dịch (RESTRICT)"
    categories ||--o{ budgets : "1 danh mục áp dụng ngân sách (SET NULL)"
    categories ||--o{ recurring_transactions : "1 danh mục gắn lịch định kỳ (SET NULL)"
    categories ||--o{ ai_scan_logs : "1 danh mục gợi ý/xác nhận scan (SET NULL)"
    categories ||--o{ ai_product_logs : "1 danh mục gợi ý từ YOLO (RESTRICT)"

    transactions ||--o{ transaction_images : "1 giao dịch đính kèm N ảnh hóa đơn (CASCADE)"
    transactions ||--o| ai_scan_logs : "1 giao dịch tạo từ log OCR (SET NULL)"
    transactions ||--o| ai_product_logs : "1 giao dịch tạo từ log YOLO (SET NULL)"

    users {
        int user_id PK "Tự tăng định danh người dùng"
        varchar firebase_uid UK "Định danh từ Firebase Auth"
        varchar full_name "Họ và tên"
        varchar email UK "Email đăng nhập"
        varchar phone "Số điện thoại"
        text avatar_url "Đường dẫn ảnh đại diện"
        varchar password_hash "Mật khẩu mã hóa BCrypt"
        varchar auth_provider "Nhà cung cấp xác thực"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật"
    }

    accounts {
        int account_id PK "Tự tăng định danh ví"
        int user_id FK "Người dùng sở hữu"
        varchar account_name "Tên ví hiển thị"
        varchar account_type "Loại ví: cash, bank, e-wallet"
        decimal balance "Số dư khả dụng"
        varchar currency "Tiền tệ: VND"
        datetime created_at "Thời điểm tạo ví"
        datetime updated_at "Thời điểm cập nhật số dư"
    }

    categories {
        int category_id PK "Tự tăng định danh danh mục"
        int user_id FK "Chủ sở hữu (NULL nếu mặc định)"
        varchar category_name "Tên danh mục"
        varchar category_type "expense hoặc income"
        varchar icon "Tên drawable icon"
        varchar color "Mã màu Hex"
        boolean is_default "Cờ danh mục mặc định"
        datetime created_at "Thời điểm tạo"
    }

    transactions {
        int transaction_id PK "Tự tăng định danh giao dịch"
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
        int budget_id PK "Tự tăng định danh ngân sách"
        int user_id FK "Người dùng thiết lập"
        int category_id FK "Danh mục áp dụng (NULL: toàn bộ)"
        varchar budget_name "Tên kế hoạch ngân sách"
        decimal amount_limit "Hạn mức tối đa"
        decimal daily_amount_limit "Hạn mức chi tiêu mỗi ngày"
        decimal spent_amount "Số tiền đã chi tiêu tích lũy"
        date start_date "Ngày bắt đầu chu kỳ"
        date end_date "Ngày kết thúc chu kỳ"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật tiến độ"
    }

    recurring_transactions {
        int recurring_id PK "Tự tăng định danh giao dịch lặp"
        int user_id FK "Người dùng thiết lập"
        int account_id FK "Ví nguồn thanh toán"
        int category_id FK "Danh mục giao dịch"
        varchar title "Tiêu đề giao dịch"
        decimal amount "Số tiền mỗi kỳ"
        varchar transaction_type "expense hoặc income"
        varchar repeat_type "daily, weekly, monthly, yearly"
        int repeat_interval "Khoảng cách lặp (mặc định 1)"
        date start_date "Ngày bắt đầu lặp"
        date end_date "Ngày kết thúc lặp"
        date next_run_date "Ngày quét thực thi tiếp theo"
        text note "Ghi chú"
        boolean is_active "Trạng thái kích hoạt"
        datetime created_at "Thời điểm tạo"
        datetime updated_at "Thời điểm cập nhật"
    }

    transaction_images {
        int image_id PK "Tự tăng định danh ảnh"
        int transaction_id FK "Giao dịch đính kèm"
        text image_url "Đường dẫn tệp ảnh"
        text ocr_text "Chuỗi văn bản trích xuất OCR"
        datetime uploaded_at "Thời điểm tải ảnh"
    }

    ai_scan_logs {
        int ai_scan_log_id PK "Tự tăng định danh log scan"
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
        int ai_product_log_id PK "Tự tăng định danh log YOLO"
        int user_id FK "Người dùng quét"
        int transaction_id FK "Giao dịch tạo ra (nếu có)"
        text raw_yolo_json "JSON kết quả nhận diện YOLOv8n"
        int suggested_category_id FK "Danh mục gợi ý từ nhãn vật phẩm"
        datetime created_at "Thời điểm nhận diện"
    }
```

---

## 2. MA TRẬN PHÂN TÍCH BẢN SỐ QUAN HỆ (CARDINALITY)

| Thực thể nguồn | Bản số | Thực thể đích | Tên Khóa ngoại (FK) | Hành vi Ràng buộc (ON DELETE) | Mô tả ý nghĩa nghiệp vụ |
| :--- | :---: | :--- | :--- | :--- | :--- |
| `users` | 1 : N | `accounts` | `accounts.user_id` | **CASCADE** | Một người dùng có nhiều ví tài khoản; xóa user sẽ xóa sạch ví thuộc về họ. |
| `users` | 1 : N | `categories` | `categories.user_id` | **CASCADE** | Một người dùng có thể tạo nhiều danh mục tùy biến cá nhân. |
| `users` | 1 : N | `transactions` | `transactions.user_id` | **CASCADE** | Một người dùng sở hữu toàn bộ lịch sử giao dịch phát sinh. |
| `users` | 1 : N | `budgets` | `budgets.user_id` | **CASCADE** | Một người dùng có thể đặt nhiều hạn mức chi tiêu cho các danh mục khác nhau. |
| `users` | 1 : N | `recurring_transactions` | `recurring_transactions.user_id` | **CASCADE** | Một người dùng quản lý danh sách các hóa đơn định kỳ tự động. |
| `users` | 1 : N | `ai_scan_logs` | `ai_scan_logs.user_id` | **CASCADE** | Lưu vết lịch sử bóc tách hóa đơn OCR của từng cá nhân. |
| `users` | 1 : N | `ai_product_logs` | `ai_product_logs.user_id` | **CASCADE** | Lưu vết lịch sử nhận diện vật phẩm YOLO của từng cá nhân. |
| `accounts` | 1 : N | `transactions` | `transactions.account_id` | **RESTRICT** | Một ví có nhiều giao dịch; không cho phép xóa ví khi còn bản ghi giao dịch chưa dọn dẹp. |
| `accounts` | 1 : N | `recurring_transactions` | `recurring_transactions.account_id` | **RESTRICT** | Không cho phép xóa ví khi còn lịch giao dịch định kỳ đang hoạt động. |
| `categories` | 1 : N | `transactions` | `transactions.category_id` | **RESTRICT** | Bảo toàn tính toàn vẹn báo cáo chi tiêu, không cho phép xóa danh mục khi còn giao dịch liên kết. |
| `categories` | 1 : N | `budgets` | `budgets.category_id` | **SET NULL** | Nếu xóa danh mục, hạn mức ngân sách vẫn giữ nguyên và gán về NULL (ngân sách chung). |
| `categories` | 1 : N | `recurring_transactions` | `recurring_transactions.category_id` | **SET NULL** | Nếu xóa danh mục, giao dịch định kỳ giữ nguyên số tiền và gán danh mục về NULL. |
| `categories` | 1 : N | `ai_scan_logs` | `ai_scan_logs.suggested_category_id` | **SET NULL** | Lưu trữ kết quả AI đề xuất danh mục; nếu danh mục bị xóa đặt FK về NULL. |
| `categories` | 1 : N | `ai_product_logs` | `ai_product_logs.suggested_category_id` | **RESTRICT** | Nhãn vật phẩm YOLO phải thuộc một danh mục hợp lệ trong hệ thống. |
| `transactions` | 1 : 1..N | `transaction_images` | `transaction_images.transaction_id` | **CASCADE** | Một giao dịch có ảnh hóa đơn đính kèm; khi xóa giao dịch, ảnh đính kèm tự động xóa theo. |
| `transactions` | 1 : 1 | `ai_scan_logs` | `ai_scan_logs.transaction_id` | **SET NULL** | Khi xóa giao dịch được tạo từ scan hóa đơn, log OCR vẫn được giữ lại để học máy. |
| `transactions` | 1 : 1 | `ai_product_logs` | `ai_product_logs.transaction_id` | **SET NULL** | Khi xóa giao dịch, log nhận diện YOLO vẫn được giữ lại để thống kê mô hình. |

---

## 3. NGUYÊN TẮC TOÀN VẸN DỮ LIỆU TÀI CHÍNH VÀ HỌC MÁY

1. **Bảo vệ Tính toàn vẹn Dòng tiền (`RESTRICT`):**
   * Các bảng tham chiếu trung tâm như `accounts` và `categories` được bảo vệ bằng ràng buộc `RESTRICT`. Điều này ngăn chặn triệt để tình trạng xóa nhầm ví tài khoản hoặc danh mục khi đang có dòng tiền phát sinh, loại bỏ hoàn toàn rủi ro sai lệch tổng tài sản (Net Worth).

2. **Cách ly Dữ liệu Người dùng (`CASCADE`):**
   * Mọi bảng nghiệp vụ phụ thuộc đều kết nối trực tiếp đến `users(user_id)` qua `ON DELETE CASCADE`. Điều này tuân thủ nghiêm ngặt tiêu chuẩn bảo mật dữ liệu người dùng (User Data Isolation), giúp việc xóa dữ liệu người dùng diễn ra triệt để, không để lại dữ liệu rác (orphan records) trong cơ sở dữ liệu.

3. **Bảo tồn Dữ liệu Huấn luyện AI (`SET NULL`):**
   * Hai bảng nhật ký học máy `ai_scan_logs` và `ai_product_logs` liên kết với `transactions(transaction_id)` qua ràng buộc `ON DELETE SET NULL`. Nhờ vậy, ngay cả khi người dùng quyết định hủy hoặc xóa một giao dịch thu chi, tập dữ liệu thô (Raw OCR text, Bounding box JSON, confidence score) vẫn được bảo toàn nguyên vẹn trong kho dữ liệu, tạo nguồn dataset phong phú phục vụ tinh chỉnh (Fine-tuning) và tái huấn luyện (Active Retraining) các mô hình AI sau này.
