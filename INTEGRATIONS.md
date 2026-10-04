# 🚀 Điểm Nhấn Công Nghệ & Các Tích Hợp Đặc Biệt (Project Highlights & Integrations)

> **Smart Personal Finance App** không chỉ là một ứng dụng quản lý thu chi CRUD thông thường, mà là một hệ thống tài chính cá nhân thông minh kết hợp đa nền tảng (**Mobile Native + Microservice Backend + Edge AI + Cloud AI + Containerization**).  
> Tài liệu này tổng hợp toàn bộ **những công nghệ tích hợp chuyên sâu và điểm khác biệt nổi bật** của dự án.

---

## 📑 Mục lục
1. [Sơ đồ Tổng thể các Điểm Tích hợp (Integrations Map)](#1-sơ-đồ-tổng-thể-các-điểm-tích-hợp-integrations-map)
2. [Tích hợp 1: Thị giác Máy tính On-Device (CameraX + YOLOv8 TFLite)](#tích-hợp-1-thị-giác-máy-tính-on-device-camerax--yolov8-tflite)
3. [Tích hợp 2: Pipeline AI 2 giai đoạn & Microsoft ONNX Runtime Java](#tích-hợp-2-pipeline-ai-2-giai-đoạn--microsoft-onnx-runtime-java)
4. [Tích hợp 3: Quét Hóa đơn Thông minh bằng Google ML Kit OCR & LLM Gemini AI](#tích-hợp-3-quét-hóa-đơn-thông-minh-bằng-google-ml-kit-ocr--llm-gemini-ai)
5. [Tích hợp 4: Xác thực Đa tầng Firebase Auth + Spring Security Stateless](#tích-hợp-4-xác-thực-đa-tầng-firebase-auth--spring-security-stateless)
6. [Tích hợp 5: Trực quan hóa Dữ liệu Tài chính Đa chiều (MPAndroidChart)](#tích-hợp-5-trực-quan-hóa-dữ-liệu-tài-chính-đa-chiều-mpandroidchart)
7. [Tích hợp 6: Đóng gói Đa dịch vụ Docker Compose & Xử lý Bộ mã UTF-8](#tích-hợp-6-đóng-gói-đa-dịch-vụ-docker-compose--xử-lý-bộ-mã-utf-8)
8. [Tích hợp 7: Trải nghiệm Nhà phát triển & Tiện ích Tinh chỉnh Nhanh](#tích-hợp-7-trải-nghiệm-nhà-phát-triển--tiện-ích-tinh-chỉnh-nhanh)

---

## 1. Sơ đồ Tổng thể các Điểm Tích hợp (Integrations Map)

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ANDROID MOBILE CLIENT                             │
│                                                                             │
│  [CameraX] ──▶ [YOLOv8 TFLite] ──▶ [BoundingBoxOverlay] (Vẽ khung On-Device)│
│       │                                                                     │
│       ▼                                                                     │
│  [Google ML Kit Text Recognition] (OCR Offline hóa đơn)                     │
│       │                                                                     │
│  [Firebase Auth SDK] ──────────────────────────┐ (Google / Email Token)     │
│       │                                        │                            │
│  [MPAndroidChart] (Trực quan hóa đồ thị)       ▼                            │
└───────┼────────────────────────────── [AuthInterceptor] ────────────────────┘
        │                                        │
        │ HTTP RESTful API                       │ Header: Bearer <ID_Token>
        ▼                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SPRING BOOT 3 BACKEND API                            │
│                                                                             │
│  [FirebaseAuthFilter] ◀── [Firebase Admin SDK] (Xác thực chữ ký số Token)   │
│       │                                                                     │
│  [GeminiService] ───────▶ [Google Gemini 2.5 Flash] (LLM bóc tách JSON)     │
│       │                                                                     │
│  [ProductClassifierService] ──▶ [Microsoft ONNX Runtime Java]               │
│       │                         (Inference Random Forest < 2ms)             │
│       ▼                                                                     │
│  [Spring Data JPA / Hibernate] (HikariCP utf8mb4)                           │
└───────┬─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          DOCKERIZED MYSQL 8.0                               │
│  [utf8mb4_unicode_ci] ──▶ 9 Tables, Foreign Keys CASCADE, Auto-Seed Data    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Tích hợp 1: Thị giác Máy tính On-Device (CameraX + YOLOv8 TFLite)

### Điểm đặc biệt:
- Thay vì gửi toàn bộ video hoặc từng frame ảnh chụp lên máy chủ để xử lý (tốn băng thông và có độ trễ lớn), ứng dụng thực hiện **Edge Computing (Inference ngay trên vi xử lý điện thoại)**.
- Tốc độ xử lý đạt từ **30 - 45 khung hình/giây (FPS)** trên thiết bị di động.

### Cơ chế hoạt động:
1. **CameraX Analyzer:** Thu nhận luồng hình ảnh từ cảm biến camera với độ phân giải tối ưu hóa cho mô hình AI.
2. **YOLOv8 Nano (TFLite Float32):** Nhận diện 18 lớp mặt hàng mua sắm phổ biến (`coffee_cup`, `fastfood`, `bread`, `clothes`, `electronic_item`, `helmet`...).
3. **Custom View `BoundingBoxOverlay`:** Tính toán tọa độ tỉ lệ màn hình và vẽ các khung hộp nhận diện (Bounding Box) kèm nhãn và độ tin cậy (`confidence`) trực tiếp đè lên khung hình xem trước (Camera Preview).

---

## Tích hợp 2: Pipeline AI 2 giai đoạn & Microsoft ONNX Runtime Java

### Điểm đặc biệt:
- **Kiến trúc mô hình kép (Two-Stage Model):** YOLO chỉ phát hiện *vật thể là gì*, nhưng để biết vật thể đó *thuộc danh mục chi tiêu tài chính nào* (Ăn uống, Di chuyển, Mua sắm...) cần mô hình phân loại tài chính.
- Tích hợp **Microsoft ONNX Runtime Java** trực tiếp vào Spring Boot mà không cần dựng thêm một server Python phụ trợ, giúp giảm thiểu chi phí máy chủ và độ trễ mạng.

### Luồng xử lý chi tiết:
1. **Feature Engineering (27 đặc trưng):**
   - 18 nhãn nhị phân: `has_coffee_cup`, `has_bread`, `has_fastfood`...
   - 5 nhóm đếm tần suất: `food_drink_count`, `transport_count`, `shopping_count`, `entertainment_count`, `health_count`.
   - 4 thông số chất lượng nhận diện: `total_objects`, `max_confidence`, `avg_confidence`, `low_confidence_count`.
2. **Suy luận siêu tốc (< 2ms):** Mô hình Random Forest đã được xuất thành [random_forest_model.onnx](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/backend/src/main/resources/random_forest_model.onnx) được nạp vào bộ nhớ RAM của Spring Boot.
3. **Cơ chế phòng vệ thông minh (Graceful Fallback):**
   ```java
   try {
       // Nạp ONNX Runtime native C++ library
       env = OrtEnvironment.getEnvironment();
       session = env.createSession(modelBytes, new OrtSession.SessionOptions());
   } catch (Throwable e) {
       // Tự động chuyển đổi sang bộ quy tắc Rule-based mà không làm gián đoạn hệ thống
       logger.warn("Falling back to rule-based classifier.");
   }
   ```

---

## Tích hợp 3: Quét Hóa đơn Thông minh bằng Google ML Kit OCR & LLM Gemini AI

### Điểm đặc biệt:
- Kết hợp giữa **OCR Offline** (nhận dạng ký tự quang học) và **Generative AI LLM** (Mô hình ngôn ngữ lớn để hiểu ngữ cảnh).
- Hóa đơn thực tế thường bị nhăn, rách, font chữ phức tạp và nhiều dòng gây nhiễu; Gemini AI giải quyết triệt để vấn đề mà các regex truyền thống không làm được.

### Cơ chế hoạt động:
```text
[ Ảnh chụp hóa đơn ]
         │
         ▼
[ Google ML Kit OCR (Android - Offline) ]
  Trích xuất toàn bộ text thô dạng khối (Blocks, Lines, Elements)
         │
         │ Gửi rawText lên Backend
         ▼
[ GeminiService.java (Spring Boot) ]
  Gửi prompt kỹ thuật ngữ cảnh kèm cấu trúc JSON Schema tới Gemini 2.5 Flash
         │
         ▼
[ Google Gemini Generative AI Response ]
  Trả về dữ liệu có cấu trúc 100% chuẩn JSON:
  {
     "merchant": "Highlands Coffee",
     "amount": 89000,
     "date": "2026-10-04",
     "category": "food"
  }
         │
         ▼
[ Điền tự động vào Form Giao dịch trên Android ]
```

---

## Tích hợp 4: Xác thực Đa tầng Firebase Auth + Spring Security Stateless

### Điểm đặc biệt:
- Hệ thống áp dụng mô hình **Hybrid Authentication**: Người dùng đăng nhập phía Client qua Firebase SDK (an toàn, bảo mật tiêu chuẩn Google), nhưng toàn bộ dữ liệu tài chính nghiệp vụ được lưu trữ trên máy chủ MySQL riêng biệt.
- **Không lưu mật khẩu người dùng dạng thô:** Loại bỏ hoàn toàn nguy cơ rò rỉ dữ liệu mật khẩu cá nhân.

### Cơ chế hoạt động:
1. **Phía Client:** Người dùng đăng nhập bằng Email/Password hoặc Google Sign-In -> Nhận `Firebase ID Token` (JWT có hạn dùng 1 giờ).
2. **AuthInterceptor (OkHttp):** Tự động chặn mọi request HTTP gửi đi và đính kèm:
   ```http
   Authorization: Bearer <Firebase_ID_Token>
   ```
3. **FirebaseAuthFilter (Spring Security):**
   - Đón nhận request trước khi vào Controller.
   - Gọi `FirebaseAuth.getInstance().verifyIdToken(token)` để giải mã chữ ký số của Google.
   - Lấy thông tin `uid`, `email` và gán vào `SecurityContextHolder`.

---

## Tích hợp 5: Trực quan hóa Dữ liệu Tài chính Đa chiều (MPAndroidChart)

### Điểm đặc biệt:
- Tích hợp thư viện biểu đồ mạnh mẽ `MPAndroidChart` được tùy biến đồng bộ với bảng màu **Obsidian Dark Theme**.
- Hỗ trợ xem biểu đồ động với hiệu ứng xoay (Animation Spin), chạm để hiển thị chi tiết (Highlighter) và lọc đa chiều theo:
  - **Theo tháng:** Xem cơ cấu chi tiêu từng ngày và tỷ trọng phần trăm từng danh mục (PieChart dạng Donut).
  - **Theo năm:** Biểu đồ cột BarChart so sánh đối sánh dòng tiền thu nhập và chi phí qua 12 tháng.

---

## Tích hợp 6: Đóng gói Đa dịch vụ Docker Compose & Xử lý Bộ mã UTF-8

### Điểm đặc biệt:
- **Zero-Configuration Quickstart:** Toàn bộ hệ sinh thái (Database MySQL 8.0, Spring Boot Backend Java 21) được cấu hình trọn vẹn trong [docker-compose.yml](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/docker-compose.yml).
- **Chuẩn hóa triệt để Tiếng Việt UTF-8 (Chống lỗi Mojibake):**
  - Cấu hình server MySQL: `--character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci`.
  - Khởi tạo script có: `SET NAMES utf8mb4;`.
  - Kết nối JDBC HikariCP: `useUnicode=true&characterEncoding=UTF-8`.
  - Spring Boot Servlet Response: `server.servlet.encoding.charset=UTF-8` và `force=true`.

---

## Tích hợp 7: Trải nghiệm Nhà phát triển & Tiện ích Tinh chỉnh Nhanh

### 1. Phím tắt cấu hình IP Server (Developer Easter Egg)
- Thông thường khi phát triển ứng dụng di động kết nối Backend nội bộ, việc đổi IP từ máy ảo (`10.0.2.2`) sang máy thật (`192.168.x.x`) đòi hỏi phải sửa code và build lại app.
- **Giải pháp trong dự án:** Tại màn hình Đăng nhập ([LoginActivity.kt](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/android-app/app/src/main/java/com/example/personalfinance/activities/LoginActivity.kt)), **nhấn giữ vào Logo app khoảng 2 giây** sẽ kích hoạt hộp thoại tùy biến địa chỉ IP Server ngay lập tức mà không cần sửa một dòng code nào.

### 2. Chuẩn hóa Data Validation & Exception Handling tập trung
- 100% Request DTOs đều được kiểm soát bởi Bean Validation (`@NotNull`, `@NotBlank`, `@PositiveOrZero`).
- Tất cả các lỗi nghiệp vụ, lỗi cú pháp hoặc lỗi cơ sở dữ liệu đều được chuyển đổi qua `GlobalExceptionHandler` thành dạng chuẩn JSON `ApiResponse` thống nhất, giúp ứng dụng Android không bao giờ bị văng/crash khi có lỗi mạng hay lỗi dữ liệu.

---

## 🏆 Tóm tắt Giá trị Công nghệ

| Hạng mục | Giải pháp truyền thống | Giải pháp tích hợp trong Smart Personal Finance App |
| :--- | :--- | :--- |
| **Nhận diện sản phẩm** | Gửi ảnh lên Cloud Server (Độ trễ cao) | **YOLOv8 TFLite On-Device + CameraX** (Thời gian thực 30+ FPS) |
| **Phân loại danh mục** | Gán cứng if-else đơn giản | **Random Forest qua ONNX Runtime Java** (< 2ms) + Fallback |
| **Quét hóa đơn** | Regex trích xuất văn bản thô dễ sai sót | **Google ML Kit OCR + Google Gemini 2.5 Flash LLM** |
| **Xác thực người dùng**| Lưu mật khẩu MD5/Bcrypt tại CSDL cục bộ | **Firebase Auth + JWT Stateless Token Verification** |
| **Triển khai hệ thống**| Cài đặt Java, MySQL, cấu hình thủ công | **1 lệnh Docker Compose tự động hóa 100%** |
