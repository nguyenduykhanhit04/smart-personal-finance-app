# THƯ MỤC HỒ SƠ THIẾT KẾ HỆ THỐNG
## DỰ ÁN TỐT NGHIỆP: ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TÍCH HỢP COMPUTER VISION VÀ AI

**Sinh viên thực hiện:** Nguyễn Duy Khánh - Mã SV: 2255010114 - Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội  
**Ngày cập nhật:** Tháng 11/2026 (Phiên bản v1.2 - Chuẩn hóa theo mã nguồn thực tế)

---

### DANH MỤC CÁC TÀI LIỆU THIẾT KẾ TRONG THƯ MỤC

Thư mục này được tổ chức thành từng file riêng biệt theo đúng chuẩn hồ sơ phân tích và thiết kế phần mềm chuyên nghiệp. Mỗi tài liệu đều được xuất bản song song ở 2 định dạng:
- **Định dạng Microsoft Word (`.docx`):** Định dạng chuẩn văn bản học thuật (Font Times New Roman, tiêu đề Navy, bảng kẻ viền chuẩn chỉnh), sẵn sàng in ấn hoặc nộp cho Hội đồng / Giảng viên hướng dẫn.
- **Định dạng Markdown (`.md`):** Tích hợp sẵn mã nguồn biểu đồ trực quan bằng **Mermaid Diagrams** (BFC, Use Case, DFD Context/Level 1/Level 2, ERD quan hệ 9 bảng).

| STT | Tên tài liệu | Định dạng Word (.docx) | Định dạng Markdown (.md) | Nội dung chi tiết |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Phân tích Yêu cầu & Biểu đồ Phân cấp Chức năng (BFC)** | `01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.docx` | `01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.md` | - Phân tích yêu cầu chức năng (FR-01 đến FR-09)<br>- Yêu cầu phi chức năng (NFR-01 đến NFR-04)<br>- Sơ đồ BFC Mức 0 tổng thể (5 phân hệ)<br>- 5 Sơ đồ BFC phân rã chi tiết (Level 1 & Level 2) |
| **2** | **Sơ đồ Use Case Tổng quát & Phân rã Chi tiết** | `02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.docx` | `02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.md` | - Xác định 3 tác nhân: Người dùng, Firebase Auth, Gemini AI<br>- Sơ đồ Use Case tổng quát<br>- Sơ đồ Use Case phân rã 5 phân hệ<br>- Bảng đặc tả Use Case chi tiết (Normal Flow & Exception Flow) |
| **3** | **Sơ đồ Luồng Dữ liệu (DFD)** | `03_SO_DO_LUONG_DU_LIEU_DFD.docx` | `03_SO_DO_LUONG_DU_LIEU_DFD.md` | - Danh mục 3 Thực thể ngoài (E1-E3) và 9 Kho dữ liệu (D1-D9)<br>- Sơ đồ DFD Mức ngữ cảnh (Context Level 0)<br>- Sơ đồ DFD Mức đỉnh (Level 1)<br>- Sơ đồ DFD Mức dưới đỉnh (Level 2 cho Giao dịch, Ngân sách và AI Pipeline) |
| **4** | **Thiết kế Cơ sở Dữ liệu Chi tiết** | `04_THIET_KE_CO_SO_DU_LIEU_CHI_TIET.docx` | `04_THIET_KE_CO_SO_DU_LIEU_CHI_TIET.md` | - Đặc tả chi tiết 9 bảng dữ liệu (tên cột, kiểu dữ liệu, PK/FK, Null/Not Null, Default, ý nghĩa nghiệp vụ)<br>- Danh mục 8 B-Tree Indexes tối ưu hiệu năng<br>- Danh mục 11 danh mục thu chi mặc định (Seed data) |
| **5** | **Sơ đồ Thực thể Quan hệ (ERD) & Ràng buộc** | `05_SO_DO_ERD_DAY_DU_VA_RANG_BUOC_QUAN_HE.docx` | `05_SO_DO_ERD_DAY_DU_VA_RANG_BUOC_QUAN_HE.md` | - Sơ đồ ERD đầy đủ 9 thực thể (erDiagram Mermaid)<br>- Ma trận bản số quan hệ (Cardinality: 1:1, 1:N)<br>- Quy tắc toàn vẹn dữ liệu: CASCADE, RESTRICT, SET NULL |

---

### TÓM TẮT THIẾT KẾ HỆ THỐNG THỰC TẾ

1. **Hệ quản trị CSDL:** MySQL 8.0, InnoDB Storage Engine, bảng mã `utf8mb4` / `utf8mb4_unicode_ci`.
2. **9 Bảng dữ liệu chính xác theo mã nguồn `database/schema.sql`:**
   - `users`: Thông tin tài khoản và định danh Firebase.
   - `accounts`: Ví tài chính, số dư và loại ví.
   - `categories`: Phân loại thu chi (11 danh mục mặc định + danh mục cá nhân).
   - `transactions`: Nhật ký giao dịch thu chi phát sinh.
   - `budgets`: Thiết lập hạn mức ngân sách tháng và theo dõi chi tiêu.
   - `recurring_transactions`: Quản lý giao dịch tự động lặp lại theo chu kỳ.
   - `transaction_images`: Hình ảnh hóa đơn và kết quả OCR thô.
   - `ai_scan_logs`: Nhật ký bóc tách hóa đơn, kết quả phân loại Random Forest.
   - `ai_product_logs`: Nhật ký nhận diện vật phẩm thời gian thực YOLOv8n.
3. **Mô hình Trí tuệ Nhân tạo & Thị giác máy tính:**
   - **Google ML Kit Text Recognition:** OCR on-device trích xuất chữ thô từ ảnh hóa đơn.
   - **Vietnamese Invoice Regex Engine:** Bóc tách tổng tiền, ngày, cửa hàng bằng biểu thức chính quy.
   - **Google Gemini 2.5 Flash API:** Cơ chế dự phòng (Fallback) khi Regex không tìm thấy trường dữ liệu.
   - **Mô hình YOLOv8n TFLite:** Nhận diện thời gian thực 18 lớp vật phẩm tài chính trên CameraX.
   - **Mô hình Random Forest (200 trees):** Chạy qua ONNX Runtime Java với 27 vector đặc trưng phân loại tự động danh mục chi tiêu.
