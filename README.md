# 💰 Smart Personal Finance App

> **Ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo & Thị giác Máy tính (AI & Computer Vision)**  
> Tự động quét hóa đơn (OCR), nhận diện sản phẩm mua sắm qua camera và phân loại danh mục chi tiêu theo thời gian thực.

---

## 🌟 Tính năng nổi bật

- 📊 **Quản lý thu chi toàn diện:** Theo dõi dòng tiền, thu nhập, chi phí và số dư các tài khoản/ví điện tử (Tiền mặt, Ngân hàng, Ví điện tử) theo thời gian thực.
- 🧾 **Quét hóa đơn thông minh (OCR):** Tích hợp Google ML Kit Text Recognition bóc tách tức thì tổng tiền, ngày tháng và nội dung hóa đơn, tự động điền form thêm giao dịch.
- 📸 **Nhận diện sản phẩm bằng Camera AI:** 
  - Mô hình **YOLOv8** phát hiện 18 nhóm mặt hàng/sản phẩm trực tiếp qua camera với khung nhận diện (Bounding Box) thời gian thực.
  - Mô hình **Random Forest** (triển khai qua Microsoft ONNX Runtime Java) phân tích 27 đặc trưng đa chiều để gợi ý chuẩn xác danh mục chi tiêu.
- 🎯 **Quản lý ngân sách & Cảnh báo vượt hạn mức:** Thiết lập hạn mức chi tiêu theo danh mục/tháng. Thanh tiến độ trực quan tự động đổi màu cảnh báo khi tiệm cận hoặc vượt quá ngân sách.
- 🔄 **Giao dịch định kỳ (Recurring Transactions):** Tự động lên lịch và ghi nhận các khoản chi cố định (tiền thuê nhà, hóa đơn điện nước, dịch vụ định kỳ).
- 📈 **Báo cáo & Phân tích trực quan:** Biểu đồ cơ cấu chi tiêu (PieChart) và xu hướng tài chính (BarChart) đa góc nhìn (theo tháng, theo năm) với MPAndroidChart.
- 🎨 **Giao diện Obsidian Dark Theme cao cấp:** Thiết kế tối màu hiện đại, tối ưu trải nghiệm người dùng và tiết kiệm pin OLED.
- 🔐 **Bảo mật & Đăng nhập đa nền tảng:** Xác thực qua Firebase Authentication (Email/Mật khẩu và Google Sign-In 1 chạm).

---

## 🏗️ Kiến trúc Hệ thống Tổng quan

```text
                        ┌───────────────────────────────┐
                        │     Người dùng & Camera       │
                        └───────────────┬───────────────┘
                                        │
                                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    MOBILE CLIENT (Android - 100% Kotlin)                     │
│  ├── Kiến trúc MVVM, ViewBinding, LiveData, Coroutines, Navigation Component │
│  ├── CameraX Preview + Google ML Kit Text Recognition (OCR hóa đơn)          │
│  ├── TensorFlow Lite: YOLOv8 Object Detection (Vẽ khung Bounding Box)        │
│  ├── MPAndroidChart: Trực quan hóa dữ liệu thu chi đa chiều                  │
│  └── Retrofit 2 + OkHttp 4: Giao tiếp RESTful & Tự động gắn Firebase Token   │
└───────────────────────────────────────┬──────────────────────────────────────┘
                                        │
                                        │ RESTful API (JSON / Multipart Upload)
                                        │ Header: Bearer <Firebase_ID_Token>
                                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     BACKEND API (Spring Boot 3 - Java 21)                    │
│  ├── Spring Security + FirebaseAuthFilter: Xác thực Token tập trung          │
│  ├── Controller Layer: 11 REST Controllers chuẩn hóa ApiResponse<T>          │
│  ├── Bean Validation: Kiểm soát chặt chẽ dữ liệu đầu vào (@Valid)             │
│  ├── Service Layer: Xử lý toàn bộ logic nghiệp vụ tài chính                  │
│  ├── AI Engine: Microsoft ONNX Runtime Java nạp Random Forest Model          │
│  │   └── Cơ chế Fallback sang Rule-based Engine khi thiếu môi trường native  │
│  └── Spring Data JPA & Hibernate ORM: Quản lý truy xuất dữ liệu              │
└───────────────────────────────────────┬──────────────────────────────────────┘
                                        │
                                        │ JDBC Connection Pool (HikariCP)
                                        │ Charset: utf8mb4 / utf8mb4_unicode_ci
                                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           DATABASE (MySQL 8.0)                               │
│  ├── Chuẩn hóa 9 bảng: users, accounts, categories, transactions, budgets... │
│  ├── Ràng buộc khóa ngoại (Foreign Keys) & Tự động xóa liên đới (CASCADE)   │
│  └── Nạp sẵn dữ liệu danh mục mặc định chuẩn tiếng Việt có dấu               │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc Thư mục & Tài liệu Chi tiết

Mỗi thành phần trong dự án đều được tài liệu hóa chi tiết trong thư mục riêng:

```text
smart-personal-finance-app/
├── android-app/            # [XEM CHI TIẾT](android-app/README.md) - Ứng dụng Android Native Kotlin 100%
├── backend/                # [XEM CHI TIẾT](backend/README.md) - REST API Spring Boot 3 & ONNX Runtime
├── database/               # [XEM CHI TIẾT](database/README.md) - Thiết kế CSDL MySQL 8 & Seed Data
├── ai-pipeline/            # [XEM CHI TIẾT](ai-pipeline/README.md) - Pipeline huấn luyện YOLOv8 & Random Forest
├── INTEGRATIONS.md         # [XEM CHI TIẾT](INTEGRATIONS.md) - Tổng hợp 7 điểm tích hợp công nghệ đặc biệt
├── docker-compose.yml      # Cấu hình khởi chạy toàn bộ hệ thống bằng Docker
└── README.md               # Tài liệu tổng quan dự án
```

---

## ⚡ Hướng dẫn Khởi chạy Nhanh (Quick Start)

### 1. Khởi chạy Backend & Database bằng Docker (Khuyên dùng)
Hệ thống đã được đóng gói hoàn chỉnh bằng **Docker Compose**. Bạn chỉ cần cài đặt Docker Desktop và chạy lệnh duy nhất:

```bash
docker compose up -d --build
```

Kiểm tra trạng thái các container:
```bash
docker compose ps
```
- **Database MySQL (`finance_db`):** Lắng nghe tại cổng `3306`.
- **Backend API (`finance_backend`):** Lắng nghe tại cổng `8080`.

> **Kiểm tra nhanh API:**
> ```bash
> curl -X POST http://localhost:8080/api/auth/firebase-login -H "Content-Type: application/json" -d "{}"
> ```

---

### 2. Khởi chạy Ứng dụng Android trên Android Studio
1. Mở **Android Studio**, chọn **Open** và trỏ đến thư mục:
   ```text
   android-app
   ```
2. Chờ Android Studio sync Gradle (dự án đã được cấu hình và build thành công 100%).
3. Khởi chạy ứng dụng:
   - **Trên Máy ảo (Android Emulator):** Ứng dụng đã được cấu hình mặc định trỏ về `http://10.0.2.2:8080/` (cầu nối trực tiếp vào Docker Backend). Bạn chỉ cần bấm nút **Run ▶️** là app chạy và kết nối được ngay.
   - **Trên Điện thoại thật:**
     - *Dùng Wi-Fi:* Mở app, tại màn hình Đăng nhập **nhấn giữ vào Logo app 2 giây** -> nhập IP máy tính của bạn (VD: `192.168.1.125:8080`) -> bấm **Lưu**.
     - *Dùng cáp USB:* Cắm cáp USB (bật USB Debugging) và chạy lệnh:
       ```bash
       adb reverse tcp:8080 tcp:8080
       ```

---

### 3. Xem và Quản trị Cơ sở Dữ liệu (DBeaver / Navicat)
Bạn có thể kết nối bất kỳ phần mềm quản lý CSDL nào vào cổng `3306`:
- **Host:** `localhost` | **Port:** `3306`
- **Database:** `personal_finance_app`
- **Username:** `root` *(hoặc `finance_user`)*
- **Password:** `rootpassword` *(hoặc `finance_pass`)*
- *(Lưu ý trên MySQL 8: Bật `allowPublicKeyRetrieval = true` và `useSSL = false` trong Driver Properties).*

---

## 🛠️ Công nghệ Sử dụng (Tech Stack)

| Thành phần | Công nghệ chính |
| :--- | :--- |
| **Mobile App** | Kotlin, Android SDK 34, MVVM, ViewBinding, LiveData, CameraX, Retrofit 2, OkHttp 4, MPAndroidChart |
| **Backend API** | Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA, Hibernate, Bean Validation |
| **AI / Machine Learning** | Google ML Kit (OCR), Ultralytics YOLOv8 (TFLite), Scikit-learn Random Forest, Microsoft ONNX Runtime Java |
| **Cơ sở dữ liệu** | MySQL 8.0 Community Server (utf8mb4 / utf8mb4_unicode_ci) |
| **Xác thực & Cloud** | Firebase Authentication, Firebase Admin SDK |
| **DevOps & Triển khai**| Docker, Docker Compose, Multi-stage Docker build, Eclipse Temurin JRE 21 |

---

## 📄 Bản quyền
Dự án được xây dựng và phát triển dưới dạng ứng dụng cá nhân hoàn chỉnh phục vụ nghiên cứu và quản lý tài chính thông minh.
