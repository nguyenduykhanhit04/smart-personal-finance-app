# Smart Personal Finance App

Ứng dụng quản lý tài chính cá nhân thông minh tích hợp trí tuệ nhân tạo (AI/Computer Vision) hỗ trợ tự động nhận diện và phân loại chi tiêu từ hóa đơn/hình ảnh thực tế.

---

## 🚀 Tính năng nổi bật
* **Quản lý thu chi:** Theo dõi chi tiêu, nguồn thu, số dư tài khoản theo thời gian thực.
* **Tự động nhận diện & phân loại chi tiêu bằng AI:**
  * Sử dụng mô hình **YOLO** nhận diện vật thể/chi tiết trong hóa đơn hoặc hình ảnh mua sắm.
  * Trích xuất đặc trưng và phân loại tự động vào các danh mục (Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Khác) bằng mô hình **Random Forest**.
* **Đọc văn bản hóa đơn (OCR):** Tích hợp Google ML Kit Text Recognition.
* **Báo cáo & Thống kê:** Trực quan hóa các khoản thu/chi qua biểu đồ tương tác MPAndroidChart.
* **Xác thực an toàn:** Tích hợp Firebase Authentication (Google/Facebook/Email).

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

### 📱 Mobile App (Frontend)
* **Nền tảng:** Android (Java 17, Target SDK 34)
* **Kiến trúc & Thư viện:** Android Jetpack (ViewModel, LiveData, ViewBinding), Material Components
* **AI & Camera on-device:** CameraX, Google ML Kit OCR, TensorFlow Lite
* **Networking & Auth:** Retrofit 2, OkHttp 3, Firebase Auth, Google/Facebook Sign-in

### 🖥️ Backend API
* **Nền tảng:** Java 21, Spring Boot 3.2.5
* **Bảo mật & CSDL:** Spring Security, Spring Data JPA, MySQL
* **AI Inference:** Microsoft ONNX Runtime Java, Smile Machine Learning
* **Cloud & Auth:** Firebase Admin SDK

### 🧠 AI / Machine Learning Pipeline
* **Ngôn ngữ:** Python 3
* **Thư viện:** Ultralytics YOLO, Scikit-Learn, Pandas, NumPy, Joblib, ONNX

---

## 📁 Cấu trúc thư mục

```text
├── android-app/        # Ứng dụng Android Client (Java 17, Material 3, CameraX)
├── backend/            # REST API Backend (Spring Boot 3, MySQL, ONNX)
├── database/           # Script CSDL MySQL (schema, seed data)
├── ai-pipeline/        # Pipeline huấn luyện & xuất model AI (YOLO + Random Forest)
├── .gitignore
└── README.md
```

---

## ⚙️ Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Cơ sở dữ liệu (Database)
* Cài đặt MySQL và tạo database `personal_finance_app`.
* Import file `database/schema.sql` và `database/seed_data.sql`.

### 2. Backend (Spring Boot)
* Mở thư mục `backend` trong IntelliJ IDEA hoặc Eclipse.
* Cập nhật thông tin kết nối MySQL và cấu hình Firebase trong `src/main/resources/application.properties`.
* Chạy ứng dụng với lệnh Maven:
  ```bash
  ./mvnw spring-boot:run
  ```

### 3. Mobile App (Android)
* Mở thư mục `android-app` bằng Android Studio.
* Thêm file `google-services.json` vào thư mục `app/`.
* Đồng bộ Gradle (Sync Project with Gradle Files) và chạy ứng dụng trên thiết bị thật hoặc máy ảo.

