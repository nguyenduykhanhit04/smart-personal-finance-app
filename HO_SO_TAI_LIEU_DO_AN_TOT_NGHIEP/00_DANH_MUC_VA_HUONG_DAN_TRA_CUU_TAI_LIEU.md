# 📚 HỒ SƠ TÀI LIỆU TOÀN DIỆN ĐỒ ÁN TỐT NGHIỆP CNTT
**Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 📌 HỆ THỐNG DANH MỤC TÀI LIỆU (ĐƯỢC ĐÁNH SỐ & QUY HOẠCH KHOA HỌC)

Toàn bộ tài liệu kỹ thuật, kiến trúc hệ thống, hướng dẫn lập trình và minh chứng thực nghiệm của đồ án được tổ chức tập trung tại thư mục:  
📁 `D:\Project\KHMT\DATN\HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP\`

Tất cả tài liệu đều được cung cấp song song ở cả hai định dạng:
* **Định dạng Microsoft Word (`.docx`):** Đã căn lề chuẩn 2cm, chia bảng biểu, nhúng sẵn toàn bộ hình ảnh thực tế, biểu đồ và định dạng màu sắc trang trọng sẵn sàng để in ấn nộp Thầy cô hoặc xem trên điện thoại/laptop.
* **Định dạng Markdown (`.md`):** Dành cho việc tra cứu nhanh, đọc trực tiếp trên VS Code / GitHub / IDE.

---

### 📋 BẢNG TRA CỨU HỒ SƠ TÀI LIỆU

| Mã tài liệu | Tên file Word (`.docx`) & Markdown (`.md`) | Nội dung nghiệp vụ trọng tâm |
| :---: | :--- | :--- |
| **00** | [00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.docx](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.docx)<br>[00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.md](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.md) | **Tổng quan & Danh mục Tra cứu:** Giới thiệu cấu trúc hệ thống tài liệu và hướng dẫn cách đọc, cách tìm nhanh tài liệu theo nhu cầu. |
| **01** | [01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx)<br>[01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.md](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.md) | **Chuyên sâu Module Trí tuệ Nhân tạo & Thị giác Máy tính (AI Pipeline):**<br>• Tập dữ liệu 1.295 ảnh và 18 nhãn sản phẩm mua sắm.<br>• Mô hình YOLOv8 TFLite (35-50ms) chạy Offline trên Android.<br>• Kỹ thuật Feature Engineering trích xuất vector 27 đặc trưng đa chiều.<br>• Mô hình Random Forest ONNX (đạt Accuracy > 95%, suy luận < 2ms trên Spring Boot).<br>• Module quét hóa đơn: Google ML Kit OCR + Gemini 2.5 Flash API + Fallback Regex.<br>• Bộ câu hỏi phản biện chuyên sâu dành cho Hội đồng. |
| **02** | [02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx)<br>[02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.md](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.md) | **Chuyên sâu Backend Spring Boot 3.2.5 & CSDL MySQL 8.0:**<br>• Kiến trúc phân tầng 4 lớp (Controller, Service, Repository, Database).<br>• Thiết kế thực thể liên kết 9 bảng CSDL (ERD).<br>• Chi tiết 8 Service nghiệp vụ cốt lõi: Ví tiền (@Transactional), Giao dịch Thu/Chi, Danh mục chống trùng lặp, Cảnh báo ngân sách (80%-100%), Cron job định kỳ @Scheduled, Báo cáo thống kê, AI ONNX.<br>• Bảo mật Stateless: Firebase Admin SDK + FirebaseAuthFilter + JWT.<br>• Bảng đặc tả toàn bộ RESTful API Endpoints.<br>• Bộ kiểm thử tự động toàn diện: 88 Test Cases (Đạt 100%).<br>• Đóng gói Docker Compose (Multi-stage build). |
| **03** | [03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.docx](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.docx)<br>[03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.md](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.md) | **Chuyên sâu Frontend Android Kotlin & Luồng Giao diện:**<br>• Nhúng toàn bộ 13 hình ảnh giao diện thực tế của ứng dụng.<br>• Hướng dẫn chi tiết 6 luồng thao tác người dùng (Step-by-step: Nhấp nút này $\rightarrow$ Màn hình kia mở lên).<br>• Quy tắc đặt tên ID trong XML (`btn`, `et`, `tv`, `iv`, `rv`).<br>• Kỹ thuật liên kết ViewBinding trong Activity vs Fragment (chống rò rỉ RAM).<br>• Bắt sự kiện Click, TextWatcher, DatePicker và kết nối ViewModel/LiveData. |
| **04** | [04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.docx](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.docx)<br>[04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.md](file:///d:/Project/KHMT/DATN/HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP/04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.md) | **Hướng dẫn Cấu hình Cloud, Firebase & Gemini API:**<br>• Tạo dự án Firebase Console miễn phí 100% (Spark Plan).<br>• Cách lấy mã SHA-1 của máy tính dán vào Firebase Console.<br>• Nạp `google-services.json` (Android) và `firebase-service-account.json` (Backend).<br>• Bật xác thực Google Sign-In, bảng tra cứu sửa lỗi Code 10, Code 12500.<br>• Đăng ký API Key Gemini 2.5 Flash miễn phí tại Google AI Studio.<br>• Cơ chế bóc tách hóa đơn và dự phòng an toàn Fallback Heuristic Regex. |
| **05** | [HUONG_DAN_TRINH_BAY_AI_PIPELINE.docx](file:///d:/Project/KHMT/DATN/HUONG_DAN_TRINH_BAY_AI_PIPELINE.docx)<br>[HUONG_DAN_TRINH_BAY_AI_PIPELINE.md](file:///d:/Project/KHMT/DATN/docs/HUONG_DAN_TRINH_BAY_AI_PIPELINE.md) | **Kịch bản Thuyết trình & Bộ Câu hỏi Phản biện:**<br>• Kịch bản nói mở đầu 30 giây đầy tự tin và chuyên nghiệp.<br>• Luận điểm kỹ thuật cốt lõi giúp bạn tự tin đạt điểm xuất sắc khi báo cáo tiến độ và bảo vệ đồ án tốt nghiệp. |
