# 🖥️ Backend Module - Smart Personal Finance App

Dịch vụ RESTful API trung tâm của ứng dụng Quản lý Tài chính Cá nhân Thông minh, xây dựng trên nền tảng **Java 21** và **Spring Boot 3.2.5**. Chịu trách nhiệm xử lý nghiệp vụ tài chính, xác thực người dùng, tích hợp mô hình AI ONNX Runtime và lưu trữ dữ liệu vào MySQL.

---

## 🚀 Công nghệ & Thư viện sử dụng

- **Ngôn ngữ:** Java 21 (LTS).
- **Framework chính:** Spring Boot 3.2.5.
- **Bảo mật & Xác thực:** Spring Security 6, Firebase Admin SDK 9.2.0 (Xác thực Firebase ID Token).
- **Truy xuất dữ liệu:** Spring Data JPA, Hibernate ORM 6.4, HikariCP, MySQL Connector/J 8.3.
- **Data Validation:** Spring Boot Starter Validation (Hibernate Validator, `@Valid`, `@NotNull`, `@NotBlank`, `@PositiveOrZero`).
- **AI & Machine Learning Inference:**
  - Microsoft ONNX Runtime Java (`ai.onnxruntime:1.17.1`) - Nạp và dự đoán trực tiếp mô hình Random Forest (`random_forest_model.onnx`).
  - Smile Machine Learning (`com.github.haifengl:smile-core:3.0.2`).
- **Build tool & Containerization:** Apache Maven 3.9+, Docker, Docker Compose (Multi-stage build / JRE 21 Temurin).

---

## 📁 Cấu trúc thư mục mã nguồn

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/financebackend/
│   │   │   ├── config/               # Cấu hình Spring Security, CORS, Firebase Admin SDK
│   │   │   │   ├── FirebaseConfig.java
│   │   │   │   ├── FirebaseAuthFilter.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/           # 11 REST Controllers tiếp nhận HTTP requests
│   │   │   │   ├── AccountController.java
│   │   │   │   ├── AiScanController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── BudgetController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── ProductClassifierController.java
│   │   │   │   ├── RecurringTransactionController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   ├── TestController.java
│   │   │   │   ├── TransactionController.java
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   │   ├── request/          # DTO nhận từ Client có tích hợp Bean Validation
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── TransactionRequest.java
│   │   │   │   │   ├── BudgetRequest.java
│   │   │   │   │   └── ...
│   │   │   │   └── response/         # Chuẩn hóa ApiResponse<T> trả về cho Client
│   │   │   │       ├── ApiResponse.java
│   │   │   │       └── ...
│   │   │   ├── entity/               # Các JPA Entities ánh xạ tới bảng MySQL
│   │   │   ├── exception/            # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── BadRequestException.java
│   │   │   ├── repository/           # Spring Data JPA Repositories
│   │   │   └── service/              # Tầng nghiệp vụ xử lý logic tài chính
│   │   │       ├── ai/               # Dịch vụ phân tích AI (ONNX & Rule-based)
│   │   │       │   ├── ProductClassifierService.java
│   │   │       │   └── ExpenseClassifierService.java
│   │   │       └── impl/             # Các Service triển khai cụ thể
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── firebase-service-account.json # Chứng chỉ Firebase Admin SDK
│   │       └── random_forest_model.onnx      # Mô hình Random Forest đã biên dịch ONNX
│   └── test/                         # Unit Tests & Integration Tests (21/21 passed)
├── Dockerfile                        # Dockerfile đóng gói ứng dụng trên nền JRE 21
├── pom.xml                           # Quản lý dependencies Maven
└── README.md
```

---

## 🛡️ Thiết kế Kiến trúc & Chuẩn hóa API

### 1. Chuẩn hóa phản hồi API (`ApiResponse<T>`)
Mọi phản hồi từ Backend đều tuân thủ định dạng thống nhất:
```json
{
  "success": true,
  "status": "success",
  "message": "Thao tác thành công",
  "data": { ... },
  "errors": null
}
```
Khi có lỗi xác thực hoặc lỗi hệ thống, mã lỗi HTTP chuẩn (400, 401, 403, 404, 500) được trả về cùng mô tả chi tiết:
```json
{
  "success": false,
  "status": "error",
  "message": "Dữ liệu yêu cầu không hợp lệ",
  "data": null,
  "errors": {
    "amount": "Số tiền phải lớn hơn 0",
    "accountId": "Tài khoản không được để trống"
  }
}
```

### 2. Tầng lọc bảo mật (`FirebaseAuthFilter`)
- Mọi request (ngoại trừ `/api/auth/**` và `/uploads/**`) đều yêu cầu Header: `Authorization: Bearer <Firebase_ID_Token>`.
- Filter tự động xác minh tính hợp lệ của Token qua Firebase Admin SDK, giải mã `uid` và gắn danh tính người dùng vào `SecurityContextHolder`.

---

## 📡 Danh sách các API Endpoints

### 🔐 1. Xác thực & Đăng nhập (`/api/auth`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/auth/firebase-login` | Public | Đăng nhập/Đồng bộ tài khoản Firebase vào MySQL |

### 👤 2. Quản lý Người dùng (`/api/users`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/users/{id}` | Token | Lấy thông tin người dùng theo ID |
| `GET` | `/api/users/firebase/{uid}` | Token | Lấy thông tin người dùng theo Firebase UID |
| `PUT` | `/api/users/{id}` | Token | Cập nhật thông tin cá nhân |

### 💳 3. Quản lý Ví & Tài khoản (`/api/accounts`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/accounts?userId={id}` | Token | Lấy danh sách ví/tài khoản của người dùng |
| `GET` | `/api/accounts/{id}` | Token | Xem chi tiết số dư tài khoản |
| `POST` | `/api/accounts` | Token | Tạo ví mới (Tiền mặt, Ngân hàng, Ví điện tử) |
| `PUT` | `/api/accounts/{id}` | Token | Sửa tên ví hoặc cập nhật số dư |
| `DELETE` | `/api/accounts/{id}` | Token | Xóa tài khoản |

### 💸 4. Giao dịch Thu Chi (`/api/transactions`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/transactions?userId={id}` | Token | Lấy lịch sử giao dịch (hỗ trợ lọc ngày/tháng) |
| `GET` | `/api/transactions/{id}` | Token | Chi tiết giao dịch |
| `POST` | `/api/transactions` | Token | Thêm giao dịch thu/chi mới (Tự động cập nhật số dư ví) |
| `PUT` | `/api/transactions/{id}` | Token | Sửa thông tin giao dịch |
| `DELETE` | `/api/transactions/{id}` | Token | Xóa giao dịch (Tự động hoàn lại số dư ví) |
| `POST` | `/api/transactions/{id}/images` | Token | Tải lên ảnh hóa đơn/chứng từ đính kèm (Multipart) |

### 🏷️ 5. Danh mục Thu Chi (`/api/categories`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/categories?userId={id}` | Token | Lấy danh mục mặc định hệ thống + danh mục người dùng |
| `POST` | `/api/categories` | Token | Tạo danh mục chi tiêu/thu nhập mới |
| `PUT` | `/api/categories/{id}` | Token | Cập nhật tên, màu sắc hoặc icon danh mục |
| `DELETE` | `/api/categories/{id}` | Token | Xóa danh mục |

### 🎯 6. Ngân sách & Hạn mức (`/api/budgets`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/budgets?userId={id}&month={m}&year={y}` | Token | Lấy danh sách ngân sách tháng |
| `POST` | `/api/budgets` | Token | Thiết lập hạn mức chi tiêu theo danh mục |
| `PUT` | `/api/budgets/{id}` | Token | Điều chỉnh hạn mức ngân sách |
| `DELETE` | `/api/budgets/{id}` | Token | Xóa thiết lập ngân sách |

### ⏰ 7. Giao dịch Định kỳ (`/api/recurring-transactions`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/recurring-transactions?userId={id}` | Token | Lấy danh sách khoản chi cố định định kỳ |
| `POST` | `/api/recurring-transactions` | Token | Tạo lịch định kỳ mới (Ngày/Tuần/Tháng/Năm) |
| `PUT` | `/api/recurring-transactions/{id}` | Token | Sửa lịch định kỳ hoặc tạm dừng |
| `DELETE` | `/api/recurring-transactions/{id}` | Token | Hủy lịch định kỳ |

### 🤖 8. Dịch vụ Trí tuệ Nhân tạo (`/api/ai`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `POST` | `/api/ai/scan-bill` | Token | Trích xuất thông tin hóa đơn (Tổng tiền, ngày, đơn vị) |
| `POST` | `/api/ai/classify-product` | Token | Nhận danh sách vật thể từ YOLO -> Phân loại danh mục qua ONNX Random Forest |

### 📊 9. Báo cáo & Thống kê (`/api/reports`)
| Method | Endpoint | Yêu cầu Auth | Mô tả |
| :--- | :--- | :---: | :--- |
| `GET` | `/api/reports/monthly?userId={id}&month={m}&year={y}` | Token | Thống kê tổng thu, tổng chi và tỷ lệ danh mục theo tháng |
| `GET` | `/api/reports/annual?userId={id}&year={y}` | Token | Biểu đồ biến động thu chi 12 tháng trong năm |

---

## 🧠 Tích hợp Mô hình AI ONNX Runtime

Lớp `ProductClassifierService` xử lý thông minh kết quả nhận diện từ Camera thiết bị:
1. Tiếp nhận danh sách nhận diện từ Mobile: Nhãn sản phẩm (`coffee_cup`, `fastfood`, `clothes`...) kèm xác suất nhận diện (`confidence`).
2. Vector hóa thành 27 đặc trưng (`RF_FEATURES`): bao gồm cờ nhị phân từng nhãn, số lượng nhóm danh mục và thống kê độ tin cậy.
3. Chạy suy luận qua Microsoft ONNX Runtime Java với mô hình [random_forest_model.onnx](file:///d:/Project/KHMT/DATN/backend/src/main/resources/random_forest_model.onnx).
4. **Cơ chế chịu lỗi (Graceful Fallback):** Nếu môi trường thiếu thư viện native hoặc không tìm thấy mô hình, hệ thống tự động chuyển sang bộ quy tắc suy luận heuristic (Rule-based Fallback), đảm bảo ứng dụng không bao giờ bị dừng đột ngột.

---

## ⚙️ Hướng dẫn Chạy Backend

### Cách 1: Khởi chạy nhanh bằng Docker (Khuyên dùng)
Tại thư mục gốc dự án:
```bash
docker compose up -d --build backend
```
- Server sẽ chạy tại: `http://localhost:8080`.
- Xem logs ứng dụng: `docker logs -f finance_backend`.

### Cách 2: Khởi chạy bằng Maven trên máy phát triển
Yêu cầu: JDK 21 trở lên, MySQL 8.0 đang chạy.
```bash
cd backend
mvn clean spring-boot:run
```

### Chạy kiểm thử tự động (Unit Tests)
```bash
mvn test
```
*(Toàn bộ 21/21 Unit Tests về Controller, Service, Validation và Exception Handling đều được kiểm thử thành công).*
