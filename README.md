# Smart Personal Finance App

Ứng dụng quản lý tài chính cá nhân thông minh kết hợp trí tuệ nhân tạo (AI & Computer Vision), hỗ trợ tự động quét hóa đơn (OCR), nhận diện sản phẩm mua sắm và tự động phân loại danh mục chi tiêu theo thời gian thực.

---

## 🚀 Tính năng nổi bật

- **Quản lý thu chi toàn diện:** Theo dõi dòng tiền, thu nhập, chi phí và số dư các tài khoản/ví điện tử theo thời gian thực.
- **Tự động nhận diện & phân loại bằng AI:**
  - **Quét hóa đơn (OCR):** Tích hợp Google ML Kit Text Recognition đọc tức thì ngày tháng, số tiền và nội dung hóa đơn.
  - **Nhận diện sản phẩm bằng Computer Vision:** Tích hợp mô hình **YOLO** nhận diện vật thể/hàng hóa trực tiếp qua camera.
  - **Gợi ý danh mục thông minh:** Phân loại tự động khoản chi tiêu vào các danh mục (Ăn uống, Di chuyển, Mua sắm, Giải trí, Hóa đơn, Sức khỏe...) bằng thuật toán **Random Forest**.
- **Quản lý ngân sách & Cảnh báo vượt hạn mức:** Thiết lập hạn mức chi tiêu theo danh mục/tháng, tự động cảnh báo khi chi tiêu tiệm cận hoặc vượt quá ngân sách.
- **Giao dịch định kỳ (Recurring Transactions):** Tự động lên lịch các khoản chi cố định (tiền thuê nhà, hóa đơn điện nước, dịch vụ đăng ký định kỳ).
- **Báo cáo & Phân tích trực quan:** Biểu đồ phân tích cơ cấu chi tiêu trực quan, đa góc nhìn (theo tháng, theo năm, toàn thời gian) với MPAndroidChart.
- **Giao diện hiện đại (Modern Obsidian Dark Theme):** Thiết kế giao diện tối ưu trải nghiệm người dùng, hỗ trợ đính kèm hình ảnh giao dịch và xem chi tiết ảnh.
- **Bảo mật & Đăng nhập đa nền tảng:** Xác thực qua Firebase Authentication (Email/Password, Google Sign-In).

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

### 📱 Android Application (Mobile Client)
- **Ngôn ngữ:** **Kotlin (100% Codebase)**
- **Kiến trúc & Jetpack:** MVVM (Model-View-ViewModel), LiveData, ViewBinding, ViewModel, Lifecycle-runtime KTX.
- **Networking & API:** Retrofit 2, OkHttp 3 / OkHttp 4 (Multipart upload), Gson.
- **Computer Vision & On-Device AI:** CameraX, Google ML Kit OCR, Custom Random Forest Classifier.
- **Data Visualization:** MPAndroidChart.
- **Authentication:** Firebase Auth, Google Play Services Auth.

### 🖥️ Backend API Service
- **Nền tảng:** Java 21, Spring Boot 3.2.x
- **Bảo mật & CSDL:** Spring Security, Spring Data JPA, Hibernate, MySQL.
- **AI Inference Runtime:** Microsoft ONNX Runtime Java, Smile Machine Learning.
- **Cloud & Notification:** Firebase Admin SDK.

### 🧠 AI & Computer Vision Pipeline
- **Ngôn ngữ & Môi trường:** Python 3.10+
- **Thư viện:** Ultralytics YOLOv8, Scikit-learn, Pandas, NumPy, Joblib, ONNX.

---

## 🏗️ Kiến trúc hệ thống tổng quan

```text
[ Người dùng / Camera ]
         │
         ▼
[ Android Client (Kotlin) ]
  ├── CameraX & Google ML Kit (OCR)
  ├── YOLO & Random Forest (On-Device Inference)
  ├── Material UI & MPAndroidChart
  └── Retrofit 2 Client
         │
         │  RESTful API / JSON / Multipart
         ▼
[ Spring Boot Backend (Java 21) ]
  ├── Security & JWT / Firebase Authentication
  ├── Financial Business Logic & Budget Alerts
  ├── ONNX Model Inference Runtime
  └── Spring Data JPA
         │
         ▼
    [ MySQL DB ]
```

---

## 📁 Cấu trúc thư mục dự án

```text
smart-personal-finance-app/
├── android-app/            # Mã nguồn ứng dụng Android (Kotlin 100%, Material 3, CameraX)
│   └── app/src/main/
│       ├── java/.../       # Toàn bộ mã Kotlin: Activities, Fragments, ViewModels, Repositories...
│       └── res/            # Giao diện XML, màu sắc, bố cục, tài nguyên đồ họa
├── backend/                # Dịch vụ REST API Backend (Spring Boot 3, JPA, Security)
├── database/               # Kịch bản cơ sở dữ liệu MySQL (schema.sql, seed_data.sql)
├── ai-pipeline/            # Pipeline huấn luyện, kiểm thử và xuất mô hình AI (YOLO + Random Forest)
├── .gitignore              # Cấu hình bỏ qua các tệp bảo mật, build cache, credentials
└── README.md               # Tài liệu hướng dẫn dự án
```

---

## ⚙️ Hướng dẫn cài đặt & Khởi chạy

### 1. Cơ sở dữ liệu (Database)
1. Cài đặt và khởi chạy máy chủ **MySQL** (phiên bản 8.0 trở lên).
2. Tạo cơ sở dữ liệu:
   ```sql
   CREATE DATABASE personal_finance_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Chạy lần lượt các script trong thư mục `database/`:
   - `database/schema.sql`: Khởi tạo bảng và ràng buộc khóa ngoại.
   - `database/seed_data.sql`: Dữ liệu mẫu ban đầu (danh mục mặc định, dữ liệu thử nghiệm).

### 2. Backend API (Spring Boot)
1. Mở thư mục `backend/` bằng IntelliJ IDEA hoặc Eclipse.
2. Cấu hình thông số kết nối cơ sở dữ liệu trong file `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/personal_finance_app?useSSL=false&serverTimezone=UTC
   spring.datasource.username=your_db_username
   spring.datasource.password=your_db_password
   ```
3. Đặt tệp cấu hình Firebase Admin SDK (`serviceAccountKey.json`) vào `src/main/resources/`.
4. Chạy ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```
   Backend sẽ lắng nghe tại cổng mặc định `http://localhost:8080`.

### 3. Mobile App (Android)
1. Mở thư mục `android-app/` bằng **Android Studio** (Koala / Ladybug hoặc mới hơn).
2. Đặt tệp cấu hình `google-services.json` vào thư mục `android-app/app/`.
3. Kiểm tra địa chỉ API Backend tại `com.example.personalfinance.api.RetrofitClient`:
   - Đối với máy ảo Android Emulator: dùng `http://10.0.2.2:8080/`
   - Đối với thiết bị thật: dùng địa chỉ IP nội bộ của máy chủ (ví dụ `http://192.168.1.x:8080/`) hoặc Ngrok domain.
4. Nhấn **Sync Project with Gradle Files** và chạy ứng dụng (`Run 'app'`).

---

## 🔒 Bản quyền & Đóng góp
Dự án được phát triển nhằm mục đích nghiên cứu và xây dựng giải pháp quản lý tài chính cá nhân thông minh kết hợp công nghệ Trí tuệ nhân tạo.
