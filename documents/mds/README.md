# TÀI LIỆU KỸ THUẬT VÀ ĐẶC TẢ HỆ THỐNG
## ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TÍCH HỢP COMPUTER VISION VÀ AI

**Sinh viên thực hiện:** Nguyễn Duy Khánh - MSV: 2255010114 - Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội  
**Hệ thống:** Personal Finance App (`personal_finance_app`)  
**Mục tiêu tài liệu:** Cung cấp tài liệu phân tích kỹ thuật chuẩn mực, truy vết chính xác 100% về source code thật, phục vụ việc phát triển, bảo trì, viết test case và hội đồng bảo vệ đồ án tốt nghiệp.

---

## 1. Cấu trúc thư mục tài liệu

```text
mds/
├── README.md                  # Mục lục + cách đọc tài liệu
├── _progress.md               # Bảng theo dõi tiến độ viết tài liệu
├── 00-overview.md             # Kiến trúc tổng thể, Actor, danh sách chức năng, sơ đồ module
├── 01-database/               # NGUỒN SỰ THẬT DUY NHẤT (Single Source of Truth) CHO DATABASE
│   ├── erd.md                 # Sơ đồ ERD toàn hệ thống (Mermaid erDiagram 9 bảng)
│   ├── tables.md              # Từ điển dữ liệu chi tiết 9 bảng: cột, kiểu, khóa, index, ràng buộc
│   └── enums-constants.md     # Danh mục trạng thái, Enum, Seed data và hằng số
├── 02-features/               # ĐẶC TẢ CHI TIẾT TỪNG CHỨC NĂNG (MỖI CHỨC NĂNG 1 FILE)
│   ├── F01-dang-nhap-firebase-dong-bo-user.md
│   ├── F02-dang-nhap-demo.md
│   ├── F03-quan-ly-ho-so-ca-nhan.md
│   ├── F04-cap-nhat-avatar-nguoi-dung.md
│   ├── F05-quan-ly-vi-tai-chinh.md
│   ├── F06-quan-ly-danh-muc-thu-chi.md
│   ├── F07-ghi-chep-quan-ly-giao-dich.md
│   ├── F08-dinh-kem-anh-hoa-don-giao-dich.md
│   ├── F09-thiet-lap-giam-sat-ngan-sach.md
│   ├── F10-quan-ly-giao-dich-dinh-ky.md
│   ├── F11-tu-dong-thuc-thi-giao-dich-dinh-ky.md
│   ├── F12-bao-cao-thong-ke-tai-chinh.md
│   ├── F13-quet-boc-tach-hoa-don-ocr-gemini.md
│   ├── F14-phan-hoi-xac-nhan-hoa-don-ai.md
│   ├── F15-nhan-dien-do-vat-yolo-random-forest.md
│   └── F16-phan-hoi-nhan-dien-do-vat-ai.md
├── 03-cross-cutting/          # CÁC THÀNH PHẦN DÙNG CHUNG TOÀN HỆ THỐNG
│   ├── auth-authorization.md  # Xác thực JWT Bearer, Firebase Filter & Demo token
│   ├── error-handling.md      # Cơ chế xử lý ngoại lệ toàn cục GlobalExceptionHandler
│   ├── file-upload.md         # Cơ chế lưu trữ và quản lý tập tin tải lên (uploads/)
│   └── scheduler.md           # Lập lịch tác vụ định kỳ Spring Task Scheduling
└── traceability-matrix.md     # Ma trận truy vết: Chức năng × Bảng DB, Chức năng × API
```

---

## 2. Hướng dẫn đọc tài liệu

1. **Dành cho Hội đồng chấm đồ án / Giảng viên hướng dẫn:**
   - Xem [00-overview.md](00-overview.md) để nắm toàn cảnh kiến trúc 3 tầng, tech stack và luồng AI.
   - Xem [01-database/erd.md](01-database/erd.md) và [traceability-matrix.md](traceability-matrix.md) để đánh giá tính toàn vẹn và mức độ bao phủ của hệ thống.
2. **Dành cho Developer mới vào dự án:**
   - Bắt đầu từ [00-overview.md](00-overview.md), sau đó xem cấu trúc bảng tại [01-database/tables.md](01-database/tables.md).
   - Đọc các cơ chế xuyên suốt tại [03-cross-cutting/](03-cross-cutting/).
   - Khi tiếp nhận tính năng cụ thể, đọc file tương ứng trong [02-features/](02-features/). Mọi phương thức, lớp và controller đều có trích dẫn `file:dòng` chính xác.
3. **Dành cho QA / Tester viết Test Case:**
   - Đọc trực tiếp **Mục 2 (Use case)**, **Mục 8 (Business rules)**, **Mục 10 (Xử lý lỗi)**, **Mục 12 (Edge cases)** và **Mục 13 (Gợi ý test case)** trong từng file chức năng tại [02-features/](02-features/).
