# HỒ SƠ THIẾT KẾ SƠ ĐỒ LUỒNG DỮ LIỆU (DFD)
## ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TÍCH HỢP THỊ GIÁC MÁY TÍNH VÀ AI
**Sinh viên thực hiện:** Nguyễn Duy Khánh - MSV: 2255010114 - Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội  
**Phiên bản:** v1.2 (Tháng 11/2026 - Chuẩn DFD Gane-Sarson / Yourdon)

---

## 1. CÁC THÀNH PHẦN CỦA HỆ THỐNG DFD

### 1.1. Tác nhân ngoài (External Entities)

| Mã | Tên Tác nhân | Phân loại | Mô tả luồng dữ liệu vào / ra |
| :---: | :--- | :--- | :--- |
| **E1** | **Người dùng cá nhân (End User)** | Thực thể chính | Gửi dữ liệu đăng ký/đăng nhập, giao dịch thu chi, ngân sách, ảnh hóa đơn, ảnh vật phẩm. Nhận kết quả xác thực, thông báo cảnh báo ngân sách, gợi ý AI điền sẵn form, biểu đồ báo cáo tài chính. |
| **E2** | **Firebase Auth Service** | Dịch vụ ngoài | Nhận yêu cầu xác thực JWT. Trả về thông tin danh tính, Token định danh duy nhất (`firebase_uid`). |
| **E3** | **Google Gemini AI Service** | Dịch vụ AI đám mây | Nhận chuỗi văn bản OCR thô và Prompt bóc tách. Trả về đối tượng JSON cấu trúc hóa gồm: `merchant`, `amount`, `date`. |

### 1.2. Danh mục Kho dữ liệu (Data Stores - 9 Bảng MySQL)

| Mã Kho | Tên Bảng MySQL | Mục đích và nội dung lưu trữ |
| :---: | :--- | :--- |
| **D1** | `users` | Hồ sơ người dùng: `user_id`, `firebase_uid`, `email`, `full_name`, `avatar_url`, `auth_provider`. |
| **D2** | `accounts` | Danh sách ví tài khoản: `account_id`, `user_id`, `account_name`, `account_type`, `balance`, `currency`. |
| **D3** | `categories` | Danh mục thu/chi: `category_id`, `user_id`, `category_name`, `category_type`, `icon`, `color`, `is_default`. |
| **D4** | `transactions` | Lịch sử giao dịch: `transaction_id`, `user_id`, `account_id`, `category_id`, `amount`, `transaction_type`, `date`. |
| **D5** | `budgets` | Hạn mức chi tiêu: `budget_id`, `user_id`, `category_id`, `amount_limit`, `daily_amount_limit`, `spent_amount`, `start/end_date`. |
| **D6** | `recurring_transactions` | Cấu hình lặp lại: `recurring_id`, `user_id`, `account_id`, `category_id`, `amount`, `repeat_type`, `next_run_date`. |
| **D7** | `transaction_images` | Ảnh hóa đơn: `image_id`, `transaction_id`, `image_url`, `ocr_text`, `uploaded_at`. |
| **D8** | `ai_scan_logs` | Nhật ký bóc tách OCR: `ai_scan_log_id`, `raw_ocr_text`, `detected_merchant`, `detected_amount`, `suggested_category_id`. |
| **D9** | `ai_product_logs` | Nhật ký nhận diện vật phẩm: `ai_product_log_id`, `raw_yolo_json`, `suggested_category_id`. |

---

## 2. DFD MỨC NGỮ CẢNH (CONTEXT LEVEL 0)

Sơ đồ ngữ cảnh mô tả mối quan hệ trao đổi thông tin tổng quát giữa toàn bộ hệ thống phần mềm với các thực thể bên ngoài:

```mermaid
flowchart LR
    E1["👤 E1: Người dùng cá nhân"]
    E2["🔥 E2: Firebase Auth Service"]
    E3["✨ E3: Google Gemini 2.5 Flash"]

    P0(("0.0<br/>HỆ THỐNG QUẢN LÝ<br/>TÀI CHÍNH CÁ NHÂN<br/>TÍCH HỢP AI"))

    %% Luong E1 -> P0
    E1 -->|1. Yêu cầu ĐK / ĐN<br/>2. Thông tin Ví & Danh mục<br/>3. Giao dịch Thu / Chi<br/>4. Cấu hình Ngân sách & Định kỳ<br/>5. Ảnh Hóa đơn / Ảnh Vật phẩm<br/>6. Yêu cầu xem Báo cáo| P0

    %% Luong P0 -> E1
    P0 -->|1. Token xác thực, Trạng thái phiên<br/>2. Số dư ví, Lịch sử giao dịch<br/>3. Cảnh báo vượt ngân sách 80%/100%<br/>4. Form giao dịch điền sẵn từ AI<br/>5. Biểu đồ thu chi Pie / Bar Chart| E1

    %% Luong P0 <-> E2
    P0 -->|Gửi Token yêu cầu xác minh| E2
    E2 -->|Trả về Firebase UID & Profile| P0

    %% Luong P0 <-> E3
    P0 -->|Gửi Raw OCR Text + Prompt JSON| E3
    E3 -->|Trả về JSON bóc tách {merchant, amount, date}| P0
```

---

## 3. DFD MỨC ĐỈNH (LEVEL 1)

Ở mức đỉnh, hệ thống được phân rã thành 5 tiến trình cốt lõi và kết nối với 9 kho dữ liệu:

```mermaid
flowchart TD
    E1["👤 E1: Người dùng"]
    E2["🔥 E2: Firebase Auth"]
    E3["✨ E3: Gemini API"]

    subgraph DataStores["CÁC KHO DỮ LIỆU (MYSQL)"]
        D1[("D1: users")]
        D2[("D2: accounts")]
        D3[("D3: categories")]
        D4[("D4: transactions")]
        D5[("D5: budgets")]
        D6[("D6: recurring_transactions")]
        D7[("D7: transaction_images")]
        D8[("D8: ai_scan_logs")]
        D9[("D9: ai_product_logs")]
    end

    subgraph Processes["TIẾN TRÌNH CỐT LÕI MỨC 1"]
        P1(("1.0<br/>Xác thực &<br/>Quản lý User"))
        P2(("2.0<br/>Quản lý Ví &<br/>Giao dịch"))
        P3(("3.0<br/>Quản lý Ngân sách<br/>& Định kỳ"))
        P4(("4.0<br/>Xử lý AI &<br/>Thị giác máy tính"))
        P5(("5.0<br/>Báo cáo &<br/>Phân tích Tài chính"))
    end

    %% P1 Flows
    E1 -->|Thông tin đăng nhập / Profile| P1
    P1 -->|Xác thực Token| E2
    E2 -->|Thông tin định danh UID| P1
    P1 <-->|Ghi / Đọc Profile| D1
    P1 -->|Trạng thái đăng nhập, JWT| E1

    %% P2 Flows
    E1 -->|Thao tác Ví & Giao dịch| P2
    P2 <-->|Cập nhật số dư| D2
    P2 <-->|Tra cứu danh mục| D3
    P2 <-->|Lưu / Đọc giao dịch| D4
    P2 -->|Lịch sử & Số dư ví| E1
    P2 -->|Thông tin chi tiêu mới| P3

    %% P3 Flows
    E1 -->|Cấu hình Ngân sách / Định kỳ| P3
    P3 <-->|Cập nhật spent_amount| D5
    P3 <-->|Lưu cấu hình lặp lại| D6
    P3 -->|Cảnh báo vượt ngưỡng 80%, 100%| E1
    P3 -.->|Tự động kích hoạt định kỳ| P2

    %% P4 Flows
    E1 -->|Ảnh Hóa đơn / Ảnh Vật phẩm| P4
    P4 -->|Gửi OCR Text Fallback| E3
    E3 -->|Trả về JSON bóc tách| P4
    P4 -->|Lưu ảnh hóa đơn| D7
    P4 -->|Lưu log scan OCR| D8
    P4 -->|Lưu log nhận diện YOLO| D9
    P4 -->|Điền sẵn form giao dịch AI| E1
    P4 -.->|Gợi ý giao dịch đã bóc tách| P2

    %% P5 Flows
    E1 -->|Yêu cầu khoảng thời gian thống kê| P5
    P5 <---|Đọc số dư| D2
    P5 <---|Đọc danh mục| D3
    P5 <---|Đọc dữ liệu thu chi| D4
    P5 <---|Đọc hạn mức| D5
    P5 -->|Biểu đồ cơ cấu & Báo cáo số dư| E1
```

---

## 4. DFD MỨC DƯỚI ĐỈNH (LEVEL 2)

### 4.1. DFD Mức 2 cho Tiến trình 2.0: Quản lý Ví và Giao dịch Thu Chi

```mermaid
flowchart TD
    E1["👤 Người dùng"]
    
    P21(("2.1<br/>Quản lý<br/>Ví tài khoản"))
    P22(("2.2<br/>Tiếp nhận &<br/>Kiểm tra GD"))
    P23(("2.3<br/>Ghi nhận<br/>Giao dịch"))
    P24(("2.4<br/>Điều chỉnh<br/>Số dư Ví"))
    P25(("2.5<br/>Kích hoạt<br/>Đồng bộ Ngân sách"))

    D2[("D2: accounts")]
    D3[("D3: categories")]
    D4[("D4: transactions")]
    P3_Target(("3.0: Ngân sách"))

    E1 -->|Thông tin ví mới / sửa ví| P21
    P21 <-->|Ghi / Đọc thông tin ví| D2

    E1 -->|Nhập giao dịch Thu/Chi| P22
    P22 <---|Tra cứu danh mục| D3
    P22 -->|Dữ liệu giao dịch hợp lệ| P23
    P23 -->|Lưu bản ghi giao dịch| D4
    
    P23 -->|Lệnh thay đổi số dư| P24
    P24 <-->|Cập nhật balance (+/-)| D2
    P24 -->|Thông báo số dư mới| E1

    P23 -->|Chuyển tiếp (category_id, amount)| P25
    P25 -->|Kích hoạt cập nhật chi tiêu| P3_Target
```

---

### 4.2. DFD Mức 2 cho Tiến trình 3.0: Quản lý Ngân sách và Giao dịch Định kỳ

```mermaid
flowchart TD
    E1["👤 Người dùng"]
    Cron["⏰ Spring Scheduler (00:00 Daily)"]

    P31(("3.1<br/>Thiết lập<br/>Hạn mức"))
    P32(("3.2<br/>Cộng dồn<br/>Chi tiêu Ngân sách"))
    P33(("3.3<br/>Kiểm tra Ngưỡng<br/>& Cảnh báo"))
    P34(("3.4<br/>Cấu hình<br/>GD Định kỳ"))
    P35(("3.5<br/>Thực thi Định kỳ<br/>Tự động"))

    D5[("D5: budgets")]
    D6[("D6: recurring_transactions")]
    P2_Target(("2.0: Quản lý Ví & GD"))
    From_P2(("Chi tiêu mới từ 2.0"))

    E1 -->|Nhập hạn mức tháng / ngày| P31
    P31 -->|Lưu ngân sách| D5

    From_P2 -->|Giao dịch chi tiêu mới| P32
    P32 <-->|Cập nhật spent_amount| D5
    P32 -->|Dữ liệu chi tiêu tích lũy| P33
    P33 -->|Cảnh báo vượt 80% (Vàng) / 100% (Đỏ)| E1

    E1 -->|Cấu hình chu kỳ định kỳ| P34
    P34 -->|Lưu cấu hình lặp lại| D6

    Cron -->|Kích hoạt quét hằng ngày| P35
    P35 <-->|Đọc các bản ghi đến hẹn & cập nhật next_run_date| D6
    P35 -->|Lệnh tạo giao dịch tự động| P2_Target
```

---

### 4.3. DFD Mức 2 cho Tiến trình 4.0: AI Pipeline & Thị giác máy tính

```mermaid
flowchart TD
    E1["👤 Người dùng"]
    E3["✨ Gemini 2.5 Flash"]

    subgraph INVOICE_PIPELINE["Nhánh A: Xử lý Hóa đơn (OCR)"]
        P41(("4.1<br/>Tiền xử lý ảnh"))
        P42(("4.2<br/>ML Kit OCR<br/>On-device"))
        P43(("4.3<br/>Bộ lọc Regex<br/>Tài chính"))
        P44(("4.4<br/>Gemini Fallback<br/>Bóc tách JSON"))
        P45(("4.5<br/>Trích xuất 27 Vector<br/>& Phân loại RF"))
    end

    subgraph YOLO_PIPELINE["Nhánh B: Nhận diện Đồ vật"]
        P46(("4.6<br/>Camera Frame<br/>640x640"))
        P47(("4.7<br/>YOLOv8n TFLite<br/>18 Classes"))
    end

    P48(("4.8<br/>Điền sẵn Form<br/>& Lưu Logs"))

    D7[("D7: transaction_images")]
    D8[("D8: ai_scan_logs")]
    D9[("D9: ai_product_logs")]

    %% Invoice Flow
    E1 -->|Ảnh chụp hóa đơn| P41
    P41 -->|Lưu ảnh gốc| D7
    P41 -->|Ảnh chuẩn hóa| P42
    P42 -->|Raw OCR Text| P43
    P43 -->|Nếu thiếu tổng tiền| P44
    P44 <-->|Gọi Prompt trích xuất| E3
    P43 -->|Văn bản & Số tiền| P45
    P44 -->|Dữ liệu chuẩn JSON| P45

    %% YOLO Flow
    E1 -->|Quét camera trực tiếp| P46
    P46 -->|Khung hình chuẩn hóa| P47
    P47 -->|Nhãn vật phẩm + Bounding Box| P48
    P47 -->|Lưu kết quả nhận diện| D9

    %% Form Fill
    P45 -->|Gợi ý Category ID + OCR data| P48
    P48 -->|Lưu log scan OCR| D8
    P48 -->|Form giao dịch tự động điền sẵn| E1
```
