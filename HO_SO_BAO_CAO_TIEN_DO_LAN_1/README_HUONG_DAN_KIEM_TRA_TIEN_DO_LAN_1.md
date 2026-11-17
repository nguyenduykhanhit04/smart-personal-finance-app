# 🎯 HƯỚNG DẪN BÁO CÁO KIỂM TRA TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP LẦN 1

### KHOA CÔNG NGHỆ THÔNG TIN — TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI
>
> **Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo  
> **Sinh viên thực hiện:** Nguyễn Duy Khánh — **Mã SV:** `2255010114` — **Lớp:** `22CN4`  
> **Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
> **Thời gian bắt đầu đồ án:** Từ đầu tháng 10 (`05/10/2026`)  
> **Thời điểm kiểm tra tiến độ lần 1:** **8h00 Thứ 7, ngày 21/11/2026 (Nhà H, ĐH Kiến trúc Hà Nội)**

---

## 📁 1. Cấu trúc Hồ sơ Tài liệu trong Thư mục này

Toàn bộ các tài liệu đã được biên soạn và hiệu chỉnh theo chiến lược **VƯỢT TIẾN ĐỘ VỪA PHẢI (~60% – 65%)** — an toàn, khiêm tốn, thuyết phục và khoa học:
`D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1\`

| Tên tệp tin | Định dạng | Mục đích sử dụng trong buổi kiểm tra |
| :--- | :---: | :--- |
| [01_BANG_THEO_DOI_THUC_HIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/01_BANG_THEO_DOI_THUC_HIEN_DATN.docx) | Word | **BẮT BUỘC IN RA GIẤY:** Bảng theo dõi tiến độ thực hiện ĐATN 7 tuần (05/10 – 21/11/2026), có nhận xét và chữ ký xác nhận của GVHD TS. Nguyễn Mạnh Thắng. Đánh giá: Vượt tiến độ dự kiến, hoàn thành 60%–65% khối lượng. |
| [02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx) | Word | **NỘI DUNG TRÌNH BÀY (KHÔNG CẦN SLIDE):** Kịch bản phát biểu 5–7 phút trước Tiểu ban, tự tin, nhấn mạnh việc hoàn thành sớm nghiệp vụ cốt lõi và khởi động sớm phân hệ AI. |
| [03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx) | Word | **BÁO CÁO TIẾN ĐỘ TOÀN VĂN:** Bản báo cáo khoa học có trang bìa chuẩn, bảng đối chiếu tiến độ cam kết vs thực tế, và chèn sẵn hình ảnh minh chứng. |
| [04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx) | Word | **HƯỚNG DẪN DEMO & CẨM NANG PHẢN BIỆN:** Checklist máy tính, kịch bản demo 3 phút thực tế + 10 câu hỏi phản biện thường gặp và câu trả lời chuẩn. |
| [05_BANG_KE_HOACH_DU_KIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/05_BANG_KE_HOACH_DU_KIEN_DATN.docx) | Word | **BẢN KẾ HOẠCH DỰ KIẾN 15 TUẦN ĐÃ NỘP TẠI KHOA:** Lưu sẵn để mở đối chiếu nếu thầy cô yêu cầu xem lại. |
| `minh_chung_hinh_anh/` | Thư mục ảnh | Chứa 13 ảnh chụp màn hình app Android, đồ thị huấn luyện YOLOv8, ma trận Random Forest. |

---


## 🎯 2. Chiến lược Báo cáo: "VƯỢT TIẾN ĐỘ VỪA PHẢI (60% – 65%)"

Việc báo cáo vượt tiến độ ở mức vừa phải (~60% – 65%) là chiến lược an toàn và khôn ngoan nhất:

1. **Không bị nghi ngờ/xoáy sâu:** Thầy cô sẽ không đặt câu hỏi hoài nghi như *"Nếu xong 90% rồi thì 8 tuần còn lại làm gì?"* hoặc nghi ngờ đề tài làm từ trước.
2. **Thấy rõ sinh viên làm việc nghiêm túc, tích cực:** Theo chuẩn tuần 7 chỉ cần 40%–45%, việc sinh viên đạt 60%–65% là minh chứng rõ ràng cho tinh thần chủ động, vượt tiến độ trước 1–2 tuần.
3. **Có kế hoạch 8 tuần tới rõ ràng:** Để lại 35%–40% khối lượng cho giai đoạn sau (tối ưu hóa mô hình AI, mở rộng dữ liệu, thử nghiệm người dùng UAT, hoàn thiện báo cáo và slide).


### Cụ thể khối lượng báo cáo

* **Giai đoạn 1 & 2 (Khảo sát & Thiết kế):** Đạt **100%** (ERD 9 bảng chuẩn 3NF, Use Case, Kiến trúc 3 tầng).
- **Giai đoạn 3 (Hạ tầng & CSDL):** Đạt **100%** (Docker Compose, MySQL 8 utf8mb4, Firebase Auth).
- **Giai đoạn 4 (Nghiệp vụ cốt lõi - vốn thuộc Tuần 5–8):** Đạt **100%** (Hoàn thành sớm trước 1 tuần: Auth Google 1 chạm, Quản lý ví, Thu chi bù trừ tự động, Ngân sách đổi màu 3 cấp, Giao dịch định kỳ, Obsidian Dark Theme).
- **Giai đoạn 5 (AI & Thị giác máy tính - vốn thuộc Tuần 9–13):** Đã **Khởi động sớm & hoàn thành bản thử nghiệm bước đầu (Prototype AI)** (YOLOv8 TFLite trên Camera, Random Forest ONNX Java trên Spring Boot, OCR bóc tách hóa đơn, 86/86 automated tests).
- **Tổng kết khối lượng:** **Đạt khoảng 60% – 65%** ➔ **Đủ điều kiện xuất sắc, điểm cao mà vẫn an toàn tuyệt đối.**

---

## ⚡ 3. Checklist Chuẩn bị Trước Giờ G (Sáng Thứ 7, 21/11/2026)

### 📌 Việc 1: Giấy tờ hồ sơ (Làm trước thứ Sáu, 20/11)

- [ ] Mở file [01_BANG_THEO_DOI_THUC_HIEN_DATN.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/01_BANG_THEO_DOI_THUC_HIEN_DATN.docx), in ra 01 bản giấy A4.
- [ ] Gặp thầy **TS. Nguyễn Mạnh Thắng** để xin chữ ký xác nhận của GVHD vào bảng theo dõi tiến độ.
- [ ] In 01 bản [02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx](file:///d:/Project/KHMT/DATN/HO_SO_BAO_CAO_TIEN_DO_LAN_1/02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx) để cầm trên tay khi đứng báo cáo.

### 📌 Việc 2: Chuẩn bị máy tính cá nhân

- [ ] Laptop sạc đầy pin 100%, cắm sạc tại bàn kiểm tra.
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

## ⏱️ 4. Tóm tắt Kịch bản Phát biểu 5 Phút Trước Tiểu ban

1. **Lời chào (30s):** Giới thiệu tên Nguyễn Duy Khánh, 22CN4, đề tài, GVHD TS. Nguyễn Mạnh Thắng. Bắt đầu từ 05/10/2026.
2. **Yêu cầu theo kế hoạch (45s):** Đến tuần 7 yêu cầu đạt khoảng 40%–45% (thiết kế và đang lập trình nghiệp vụ).
3. **Kết quả thực tế đạt được (2.5 phút):**
   - Đạt khoảng 60% – 65% (vượt trước 1–2 tuần).
   - Hoàn thành sớm toàn bộ 7 phân hệ nghiệp vụ cốt lõi và kiểm thử 86/86 tests.
   - Đã khởi động sớm và bước đầu có bản demo thử nghiệm tích hợp AI (YOLOv8, ONNX Random Forest, OCR hóa đơn).
4. **Minh chứng trên laptop (1 phút):** Chạy `mvn test`, mở app Android demo thêm giao dịch và bật thử camera AI.
5. **Kế hoạch 8 tuần tới (45s):** Tập trung mở rộng dữ liệu AI, tối ưu hóa mô hình, thử nghiệm người dùng UAT và hoàn thiện báo cáo tốt nghiệp. Cảm ơn thầy cô.
