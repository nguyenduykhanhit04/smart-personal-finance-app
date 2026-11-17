# 🎯 HƯỚNG DẪN BÁO CÁO KIỂM TRA TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP LẦN 1
### KHOA CÔNG NGHỆ THÔNG TIN — TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI
> **Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo  
> **Sinh viên thực hiện:** Nguyễn Duy Khánh — **Mã SV:** `2255010114` — **Lớp:** `22CN4`  
> **Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
> **Thời gian bắt đầu đồ án:** Từ đầu tháng 10 (`05/10/2026`)  
> **Thời điểm kiểm tra tiến độ lần 1:** **8h00 Thứ 7, ngày 21/11/2026 (Nhà H, ĐH Kiến trúc Hà Nội)**

---

## 📁 1. Cấu trúc Hồ sơ Tài liệu trong Thư mục này

Toàn bộ các tài liệu cần thiết đã được biên soạn theo đúng biểu mẫu học thuật của Khoa CNTT - Trường ĐH Kiến trúc Hà Nội và lưu trữ đầy đủ tại thư mục:
`D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1\`

| Tên tệp tin | Định dạng | Mục đích sử dụng trong buổi kiểm tra |
| :--- | :---: | :--- |
| [01_BANG_THEO_DOI_THUC_HIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/01_BANG_THEO_DOI_THUC_HIEN_DATN.docx) | Word | **BẮT BUỘC IN RA GIẤY:** Bảng theo dõi tiến độ thực hiện ĐATN 7 tuần (05/10 – 21/11/2026), có nhận xét và chữ ký xác nhận của GVHD TS. Nguyễn Mạnh Thắng. |
| [02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx) | Word | **NỘI DUNG TRÌNH BÀY (KHÔNG CẦN SLIDE):** Kịch bản phát biểu 5–7 phút trước Tiểu ban, bố cục mạch lạc, nhấn mạnh điểm vượt tiến độ. |
| [03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx) | Word | **BÁO CÁO TIẾN ĐỘ TOÀN VĂN:** Bản báo cáo khoa học đầy đủ có trang bìa, bảng so sánh đối chiếu tiến độ, phân tích kỹ thuật và chèn sẵn hình ảnh minh chứng. |
| [04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx) | Word | **HƯỚNG DẪN DEMO & PHẢN BIỆN:** Kịch bản demo 3 phút thực tế trên máy tính + 10 câu hỏi phản biện thường gặp và câu trả lời mẫu. |
| [05_BANG_KE_HOACH_DU_KIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/05_BANG_KE_HOACH_DU_KIEN_DATN.docx) | Word | **BẢN KẾ HOẠCH DỰ KIẾN ĐÃ NỘP Ở KHOA:** Lưu sẵn để mở đối chiếu nếu thầy cô trong tiểu ban yêu cầu xem lại. |
| `minh_chung_hinh_anh/` | Thư mục ảnh | Chứa toàn bộ ảnh chụp app Android (Obsidian Dark Theme), biểu đồ huấn luyện YOLOv8, ma trận nhầm lẫn Random Forest và độ quan trọng đặc trưng. |

---

## 🚀 2. Chiến lược Trình bày "VƯỢT TIẾN ĐỘ XUẤT SẮC"

Thông báo của Khoa nêu rõ: *Tiểu ban đánh giá tiến độ thực hiện ĐATN trên cơ sở bản kế hoạch đã được sinh viên nộp, kết quả thực hiện đến thời điểm kiểm tra.*

### So sánh Tiến độ Cam kết vs Tiến độ Thực tế:
- **Tiến độ cam kết theo kế hoạch ban đầu (hết Tuần 7):**
  - Mới chỉ yêu cầu xong: Khảo sát (Tuần 1) + Phân tích thiết kế hệ thống, CSDL ERD (Tuần 2–3) + Dựng môi trường khung dự án (Tuần 4) + Đang lập trình các chức năng nghiệp vụ cơ bản (Tuần 5–8).
  - Tỷ lệ khối lượng yêu cầu đạt: **Khoảng 40% – 50%**.
- **Tiến độ thực tế sinh viên Nguyễn Duy Khánh đã đạt được (21/11/2026):**
  - Đã hoàn thành 100% Phân tích thiết kế và CSDL 9 bảng chuẩn 3NF.
  - Đã hoàn thành 100% 7 phân hệ nghiệp vụ tài chính cốt lõi (xong trước hạn 1 tuần).
  - **ĐỘT PHÁ VƯỢT TIẾN ĐỘ 4–5 TUẦN (Vốn thuộc tuần 9–13):**
    1. Huấn luyện xong mô hình **YOLOv8n On-Device** (18 lớp sản phẩm, mAP@0.5 = 87.6%), convert sang TFLite chạy thời gian thực 34–42 FPS trên camera.
    2. Xây dựng xong **Pipeline AI 2 giai đoạn** với mô hình **Random Forest (200 cây)** trên 27 đặc trưng, nhúng trực tiếp vào Spring Boot qua **Microsoft ONNX Runtime Java** (< 2ms) phân loại chuẩn 6 danh mục chi tiêu.
    3. Tích hợp xong **Quét hóa đơn thông minh OCR Google ML Kit Offline + Google Gemini 2.5 Flash** bóc tách JSON chuẩn xác.
    4. Xây dựng xong **Báo cáo đa chiều MPAndroidChart** (PieChart, BarChart).
    5. Viết xong **86/86 bài kiểm thử tự động (Unit & Integration Tests) đạt 100% Passed**.
    6. Đã viết xong khung toàn văn báo cáo ĐATN 4 chương đầy đủ.
  - Tỷ lệ khối lượng hoàn thành thực tế: **ĐẠT 85% – 90% TOÀN BỘ ĐỒ ÁN!**

👉 **Kết luận:** Đây là cơ sở vững chắc nhất để Tiểu ban đánh giá đồ án đạt **Điểm Tối Đa (A+)** trong đợt kiểm tra lần 1 này.

---

## 💻 3. Checklist Chuẩn bị Trước Giờ G (Sáng Thứ 7, 21/11/2026)

### 📌 Việc 1: Giấy tờ hồ sơ (Làm trước thứ 6, 20/11)
- [ ] Mở file [01_BANG_THEO_DOI_THUC_HIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/01_BANG_THEO_DOI_THUC_HIEN_DATN.docx), in ra 01 bản giấy A4.
- [ ] Gặp thầy **TS. Nguyễn Mạnh Thắng** để xin chữ ký xác nhận của GVHD vào bảng theo dõi tiến độ.
- [ ] In thêm 01 bản [02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx) để cầm trên tay khi đứng báo cáo.

### 📌 Việc 2: Chuẩn bị máy tính cá nhân
- [ ] Laptop sạc đầy pin 100%, mang theo cục sạc.
- [ ] Bật sẵn Docker Backend & CSDL:
  ```bash
  cd D:\Project\KHMT\DATN
  docker compose up -d
  docker compose ps
  ```
- [ ] Nếu dùng điện thoại thật: Cắm cáp USB (bật USB Debugging) và chạy lệnh:
  ```bash
  adb reverse tcp:8080 tcp:8080
  ```
- [ ] Mở sẵn cửa sổ Terminal ở thư mục `backend/` chuẩn bị sẵn lệnh `mvn test`.
- [ ] Mở sẵn ứng dụng Android trên điện thoại hoặc máy ảo Emulator.

---

## ⏱️ 4. Kịch bản Trình bày 5–7 Phút Trước Tiểu ban

1. **Phút 1 (Mở đầu):** Giới thiệu tên, mã sinh viên, lớp, tên đề tài và giáo viên hướng dẫn TS. Nguyễn Mạnh Thắng. Nêu rõ thời gian nhận đồ án từ ngày 05/10/2026, kế hoạch 15 tuần.
2. **Phút 2–4 (Trọng tâm kết quả):** 
   - Tóm tắt các module nghiệp vụ cốt lõi đã hoàn thành 100% (Ví, Giao dịch bù trừ tự động, Ngân sách đổi màu 3 cấp độ, Giao dịch định kỳ).
   - Nhấn mạnh điểm vượt tiến độ: Đã hoàn thiện xong cả module AI (YOLOv8 TFLite 18 lớp, Random Forest ONNX Java suy luận < 2ms, Quét hóa đơn OCR + Gemini 2.5 Flash, 86/86 automated tests).
3. **Phút 5–6 (Minh chứng trực tiếp trên máy):**
   - Mở Terminal chạy `mvn test` cho thấy 86/86 bài test Passed 100%.
   - Mở app Android thao tác: đăng nhập Google 1 chạm -> chĩa Camera AI nhận diện vật thể -> quét hóa đơn tự điền form trong 2 giây -> xem biểu đồ ngân sách.
4. **Phút 7 (Kết luận & Kế hoạch tiếp theo):** Nêu kế hoạch thử nghiệm người dùng, hoàn thiện báo cáo và sẵn sàng bảo vệ đồ án chính thức đúng hạn. Cảm ơn thầy cô.

---

## 🏆 Cẩm nang Trả lời Câu hỏi Phản biện Nhanh

Xem chi tiết trong tệp: [04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx).
- **Hỏi:** *Tại sao lại dùng ONNX Runtime nhúng vào Java Spring Boot?*  
  **Đáp:** Giúp chạy suy luận trực tiếp trong bộ nhớ JVM chỉ mất 1.2–1.8ms, không cần dựng máy chủ Python phụ trợ, tiết kiệm tài nguyên và có cơ chế Rule-based Fallback an toàn.
- **Hỏi:** *YOLOv8 chạy trên điện thoại có nặng không?*  
  **Đáp:** Dùng bản YOLOv8 Nano tối ưu hóa TFLite Float32 chỉ 6.3MB, chạy qua CameraX ImageAnalysis đa luồng đạt 34–42 FPS mượt mà và không gây nóng máy.
- **Hỏi:** *Hóa đơn quét bằng Gemini có tốn kém không?*  
  **Đáp:** Dùng Gemini 2.5 Flash chi phí rất rẻ, kết hợp Google ML Kit OCR xử lý offline trước nên chỉ gửi chuỗi text ngắn gọn (300-500 token/lần), tiết kiệm tối đa chi phí.
