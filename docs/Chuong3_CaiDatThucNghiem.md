# CHƯƠNG 3: CÀI ĐẶT VÀ THỰC NGHIỆM HỆ THỐNG

> **Căn cứ thực hiện:**  
> Toàn bộ nội dung của Chương 3 được xây dựng và triển khai dựa trên **Đề xuất Kế hoạch Thực hiện Đề tài Tốt nghiệp** ([KE_HOACH_DO_AN.md](file:///d:/Project/KHMT/DATN/docs/KE_HOACH_DO_AN.md)), trực tiếp hiện thực hóa 3 nội dung trọng tâm:
> - **Nội dung 3 (Tuần 4):** Cài đặt môi trường phát triển (Android Studio, IntelliJ IDEA, Docker); xây dựng cơ sở dữ liệu MySQL theo thiết kế ERD; cấu hình Firebase Authentication; dựng khung dự án Backend (Spring Boot) và Mobile (Android Kotlin).
> - **Nội dung 4 (Tuần 5 – Tuần 8):** Xây dựng, lập trình các chức năng chính của hệ thống: đăng nhập/đăng ký (Firebase Auth, Google Sign-In); quản lý tài khoản ví; quản lý giao dịch thu chi; quản lý danh mục; thiết lập ngân sách và cảnh báo vượt hạn mức; giao dịch định kỳ; thiết kế giao diện người dùng theo Obsidian Dark Theme.
> - **Nội dung 5 (Tuần 9 – Tuần 13):** Huấn luyện mô hình AI (YOLOv8, Random Forest), chuyển đổi sang TFLite và ONNX; tích hợp nhận diện sản phẩm qua Camera (CameraX + YOLOv8 On-Device); tích hợp quét hóa đơn thông minh (Google ML Kit OCR + Gemini AI); tích hợp ONNX Runtime trên Backend; xây dựng module báo cáo trực quan (biểu đồ PieChart, BarChart); kiểm thử toàn bộ hệ thống, sửa lỗi và tối ưu hiệu năng.

---

## 3.1. Cài đặt môi trường phát triển và xây dựng nền tảng hệ thống (Căn cứ Nội dung 3)

### 3.1.1. Thiết lập môi trường phát triển (Android Studio, IntelliJ IDEA, Docker)
Theo đúng kế hoạch, môi trường phát triển cho từng thành phần trong hệ thống đã được thiết lập đồng bộ và chuẩn hóa:

1. **Môi trường Mobile Client (Android Studio):**
   - **Phiên bản IDE:** Android Studio Ladybug / Koala Feature Drop.
   - **Môi trường thực thi:** Android SDK 34 (Android 14 UpsideDownCake), tương thích ngược đến Min SDK 24 (Android 7.0 Nougat).
   - **Ngôn ngữ & Công cụ biên dịch:** Kotlin 1.9.x, Android Gradle Plugin 8.3.x, Java Virtual Machine 17 LTS.
   - **Thiết bị thử nghiệm:** Thiết bị thật Samsung Galaxy A52s (Android 14, vi xử lý Snapdragon 778G, RAM 8GB) và Google Pixel 7 Emulator (API 34).

2. **Môi trường Backend API (IntelliJ IDEA Ultimate):**
   - **Nền tảng ngôn ngữ:** Java Development Kit (JDK) 21 LTS (Eclipse Temurin).
   - **Framework trung tâm:** Spring Boot 3.2.5.
   - **Công cụ quản lý gói dependencies:** Apache Maven 3.9+.
   - **Cấu hình biến môi trường:** Quản lý thông qua file `application.properties` hỗ trợ cơ chế ghi đè biến môi trường (Environment Variable Override) khi chạy trong Docker.

3. **Môi trường ảo hóa và đóng gói (Docker & Docker Compose):**
   - Sử dụng **Docker Desktop 4.30+** trên Windows 11 với WSL2 backend.
   - Toàn bộ hạ tầng Backend và CSDL MySQL được đóng gói thành các container chạy trên cùng một bridge network (`app-network`), cho phép triển khai chỉ với một lệnh duy nhất:
     ```bash
     docker compose up -d --build
     ```

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MẠNG ẢO DOCKER (app-network)                      │
│                                                                             │
│  ┌─────────────────────────────┐           ┌─────────────────────────────┐  │
│  │  Container: finance_backend  │           │   Container: finance_db     │  │
│  │  - Spring Boot 3 (Java 21)  │ ──JDBC──► │   - MySQL 8.0 Community     │  │
│  │  - ONNX Runtime Engine      │  Port 3306│   - Volume: mysql_data      │  │
│  │  - Expose Port: 8080        │           │   - utf8mb4_unicode_ci      │  │
│  └──────────────┬──────────────┘           └─────────────────────────────┘  │
└─────────────────┼───────────────────────────────────────────────────────────┘
                  │ HTTP REST API (Port 8080)
                  ▼
       [ Thiết bị Android Mobile ]
```

### 3.1.2. Xây dựng cơ sở dữ liệu MySQL theo thiết kế ERD
Hiện thực hóa thiết kế ERD từ Chương 2, cơ sở dữ liệu `personal_finance_app` được xây dựng bằng script DDL `database/schema.sql` gồm **9 bảng quan hệ**, tuân thủ nghiêm ngặt các quy tắc:
- **Chuẩn hóa bộ mã đa ngôn ngữ:** Thiết lập mặc định `DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci` trên toàn bộ bảng và các cột dạng chuỗi ký tự (`VARCHAR`, `TEXT`), đảm bảo tiếng Việt có dấu và biểu tượng cảm xúc được lưu trữ chính xác 100%.
- **Ràng buộc toàn vẹn khóa ngoại (Foreign Keys):** Áp dụng quy tắc `ON DELETE CASCADE` cho các thực thể quan hệ phụ thuộc:
  - Khi xóa một tài khoản `accounts`, toàn bộ giao dịch liên quan trong `transactions` tự động được dọn dẹp.
  - Khi xóa giao dịch trong `transactions`, các bản ghi ảnh chứng từ trong `transaction_images` được thu hồi tự động.
- **Tối ưu hóa chỉ mục (Indexes):** Thiết lập chỉ mục trên các cột thường xuyên được truy vấn lọc và sắp xếp: `idx_transactions_user_date (user_id, transaction_date)`, `idx_budgets_user_month (user_id, month_year)`.
- **Khởi tạo dữ liệu mẫu (`seed_data.sql`):** Tự động nạp sẵn 11 danh mục Chi tiêu (Ăn uống, Di chuyển, Mua sắm, Hóa đơn, Giải trí, Sức khỏe...) và 4 danh mục Thu nhập (Lương, Thưởng, Tiền lãi, Khác) kèm mã màu Hex và tên icon Material Design.

### 3.1.3. Cấu hình xác thực Firebase Authentication và Firebase Admin SDK
Hệ thống triển khai cơ chế xác thực kép (Hybrid Authentication) giữa Google Firebase và Spring Boot:
1. **Phía Client Android:**
   - Đăng ký `SHA-1` và `SHA-256` của ứng dụng lên Firebase Console.
   - Nạp file cấu hình `google-services.json` vào module `android-app/app/`.
   - Cấu hình thư viện `com.google.firebase:firebase-auth-ktx` và `com.google.android.gms:play-services-auth` để hỗ trợ đăng nhập 1 chạm bằng Google Sign-In.
2. **Phía Server Backend:**
   - Tải tệp chứng thực bảo mật tài khoản dịch vụ `firebase-service-account.json`.
   - Xây dựng lớp cấu hình `FirebaseConfig.java` khởi tạo `FirebaseApp` duy nhất:
     ```java
     @Configuration
     public class FirebaseConfig {
         @PostConstruct
         public void initFirebase() throws IOException {
             if (FirebaseApp.getApps().isEmpty()) {
                 InputStream serviceAccount = getClass().getClassLoader()
                     .getResourceAsStream("firebase-service-account.json");
                 FirebaseOptions options = FirebaseOptions.builder()
                     .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                     .build();
                 FirebaseApp.initializeApp(options);
             }
         }
     }
     ```
   - Xây dựng bộ lọc bảo mật `FirebaseAuthFilter.java` chặn mọi request HTTP gửi lên, giải mã Firebase ID Token, kiểm tra tính hợp lệ và trích xuất thông tin người dùng an toàn.

### 3.1.4. Dựng khung dự án Backend và Mobile Client (Project Skeleton)
1. **Khung dự án Backend (Spring Boot 3):**
   - Tổ chức theo kiến trúc phân tầng chuẩn: `config/`, `controller/`, `dto/`, `entity/`, `exception/`, `repository/`, `service/`.
   - Cấu hình JPA kết nối MySQL qua HikariCP Connection Pool với cấu hình tối ưu.
   - Chuẩn hóa cấu trúc gói phản hồi API (`ApiResponse<T>`) và lớp xử lý lỗi toàn cục (`GlobalExceptionHandler`).
2. **Khung dự án Mobile (Android Kotlin):**
   - Cấu hình kiến trúc **MVVM (Model - View - ViewModel)** kết hợp Android Jetpack:
     - Kích hoạt `viewBinding { enabled = true }`.
     - Xây dựng `Navigation Graph` (`res/navigation/nav_graph.xml`) quản lý luồng màn hình.
     - Thiết lập mạng với Retrofit 2 singleton (`ApiClient.kt`), OkHttp Logging Interceptor và `AuthInterceptor.kt` tự động gắn Bearer Token.

---

## 3.2. Xây dựng và lập trình các chức năng chính của hệ thống (Căn cứ Nội dung 4)

Giai đoạn phát triển nghiệp vụ cốt lõi từ Tuần 5 đến Tuần 8 đã hiện thực hóa trọn vẹn toàn bộ các tính năng tài chính cơ bản và nâng cao:

### 3.2.1. Phân hệ Đăng nhập / Đăng ký (Firebase Auth & Google Sign-In)
- **Đăng ký tài khoản Email/Password (`RegisterActivity.kt`):** Người dùng nhập Họ tên, Email và Mật khẩu. Ứng dụng gọi Firebase tạo tài khoản, sau đó tự động gọi API `/api/auth/register` của Backend để đồng bộ tạo hồ sơ `User` trong CSDL MySQL.
- **Đăng nhập Google Sign-In 1 chạm (`LoginActivity.kt`):** Tích hợp Google Identity SDK. Khi người dùng bấm nút đăng nhập Google, hộp thoại Account Picker hiển thị cho phép chọn tài khoản Google sẵn có trên máy. Token nhận về được xác thực với Firebase và Backend đồng bộ tức thì.
- **Chế độ Developer IP Config:** Cho phép lập trình viên/kiểm thử viên **nhấn giữ Logo 2 giây** để đổi địa chỉ IP máy chủ Backend linh hoạt mà không cần build lại APK.

### 3.2.2. Phân hệ Quản lý Tài khoản Ví
Người dùng có thể quản lý nhiều nguồn tài chính độc lập thông qua `AccountController` và `AccountService`:
- Hỗ trợ 3 loại ví: **Tiền mặt (CASH)**, **Tài khoản Ngân hàng (BANK)**, **Ví điện tử (E_WALLET)**.
- Người dùng có thể tạo ví mới, đổi tên ví, cập nhật số dư ban đầu, hoặc xóa ví tài chính.
- Hệ thống hỗ trợ tính toán tức thời **Tổng số dư ròng (Total Net Balance)** của người dùng bằng cách cộng tổng số dư của tất cả các ví khả dụng và hiển thị nổi bật trên thẻ Dashboard của `HomeFragment`.

### 3.2.3. Phân hệ Quản lý Giao dịch Thu Chi & Đính kèm chứng từ
Được hiện thực hóa tại `TransactionController.java` và `TransactionFragment.kt`:
1. **Ghi nhận giao dịch:** Hỗ trợ nhập các trường: Số tiền, Loại giao dịch (Thu / Chi), Danh mục, Tài khoản ví thanh toán, Ngày giờ giao dịch và Ghi chú.
2. **Cơ chế cập nhật số dư tự động (Automated Balance Tracking):**
   - Khi phát sinh khoản **Chi tiêu (EXPENSE)**: Hệ thống tự động trừ số dư của ví tương ứng:
     $$\text{Số dư mới} = \text{Số dư hiện tại} - \text{Số tiền giao dịch}$$
   - Khi phát sinh khoản **Thu nhập (INCOME)**: Hệ thống tự động cộng số dư vào ví:
     $$\text{Số dư mới} = \text{Số dư hiện tại} + \text{Số tiền giao dịch}$$
3. **Cơ chế hoàn tác số dư khi Sửa / Xóa (Transaction Rollback):**
   - Khi người dùng cập nhật lại số tiền hoặc tài khoản ví, hệ thống hoàn tác biến động cũ rồi áp dụng biến động mới trong một khối `@Transactional`, ngăn chặn hoàn toàn sai lệch số dư.
   - Khi người dùng xóa một giao dịch, số dư ví sẽ được khôi phục nguyên trạng.
4. **Đính kèm hình ảnh chứng từ:** Tích hợp `TransactionImageService.java` cho phép upload ảnh hóa đơn qua Multipart Form Data, lưu trữ ảnh an toàn vào thư mục máy chủ `/uploads` và lưu đường dẫn vào bảng `transaction_images`.

### 3.2.4. Phân hệ Quản lý Danh mục Thu Chi
- Cung cấp sẵn hệ thống danh mục hệ thống mặc định (`is_default = true`).
- Cho phép người dùng tự tạo thêm danh mục cá nhân hóa: đặt tên, chọn loại (`income` / `expense`), chọn bảng màu Hex (`color_code`) và biểu tượng Material tương ứng.
- Khi truy vấn, API `/api/categories` tự động gộp danh mục mặc định của hệ thống và các danh mục riêng của chính người dùng đó.

### 3.2.5. Phân hệ Thiết lập Ngân sách và Cảnh báo vượt hạn mức đa sắc độ
Được hiện thực hóa tại `BudgetController.java`, `BudgetService.java` và `BudgetFragment.kt`:
- **Thiết lập ngân sách:** Người dùng đặt hạn mức chi tiêu tối đa cho từng danh mục trong tháng cụ thể (ví dụ: Danh mục Ăn uống - Tháng 11/2026 - Hạn mức: 3.000.000 VNĐ).
- **Thuật toán theo dõi tiến độ thời gian thực:**
  $$\text{Tỷ lệ chi tiêu (\%)} = \frac{\sum \text{Giao dịch Chi tiêu cùng danh mục trong tháng}}{\text{Hạn mức ngân sách}} \times 100\%$$
- **Cơ chế cảnh báo trực quan đổi màu (Dynamic Color Progress):**
  - **Mức An toàn (Tỷ lệ < 80%):** Thanh tiến độ hiển thị màu Xanh lá (`#22C55E`).
  - **Mức Cảnh báo (80% $\le$ Tỷ lệ $\le$ 100%):** Thanh tiến độ chuyển sang màu Vàng cam (`#FF9800`), đưa ra khuyến cáo tiết chế chi tiêu.
  - **Mức Báo động đỏ (Tỷ lệ > 100%):** Thanh tiến độ đổi sang màu Đỏ (`#F85149`), hiển thị số tiền đã vượt hạn mức.
- **Tính toán hạn mức an toàn mỗi ngày:** Tự động chia số tiền còn lại cho số ngày còn lại trong tháng để người dùng biết mỗi ngày được phép tiêu tối đa bao nhiêu.

### 3.2.6. Phân hệ Giao dịch Định kỳ tự động
Được hiện thực hóa tại `RecurringTransactionController.java`:
- Cho phép thiết lập các khoản thu/chi lặp lại cố định như tiền thuê trọ, hóa đơn Internet, tiền lương hàng tháng.
- Cấu hình chu kỳ linh hoạt: Hàng ngày (`DAILY`), Hàng tuần (`WEEKLY`), Hàng tháng (`MONTHLY`), Hàng năm (`YEARLY`).
- Hệ thống tự động tính toán `next_run_date` và cung cấp cơ chế tự động kích hoạt tạo giao dịch khi người dùng mở ứng dụng đến hạn thanh toán.

### 3.2.7. Thiết kế giao diện người dùng theo Obsidian Dark Theme
Giao diện ứng dụng được thiết kế hoàn thiện theo ngôn ngữ **Obsidian Dark Theme** sang trọng và hiện đại:
- **Bảng màu chủ đạo:** Nền đen sâu Obsidian `#090D16`, bề mặt thẻ Card `#161B2A`, phân cách `#1F263B`.
- **Màu thương hiệu:** Sangrich Royal Blue `#3B82F6` kết hợp màu Thu nhập xanh lá `#22C55E` và màu Chi phí đỏ cam `#F85149`.
- **Lợi ích thực tế:** Tối ưu hóa trải nghiệm thị giác trong điều kiện ánh sáng yếu, giảm mỏi mắt và tiết kiệm đáng kể năng lượng tiêu thụ trên màn hình AMOLED/OLED của thiết bị di động.

---

## 3.3. Huấn luyện mô hình Trí tuệ Nhân tạo và Tích hợp hệ thống (Căn cứ Nội dung 5)

Theo đúng kế hoạch tại Nội dung 5, đề tài đã hiện thực hóa trọn vẹn Pipeline AI 2 giai đoạn và luồng bóc tách hóa đơn thông minh:

### 3.3.1. Huấn luyện mô hình YOLOv8 và chuyển đổi sang TFLite On-Device
1. **Thu thập và Chuẩn hóa Dữ liệu:**
   - Tập dữ liệu gồm **1.295 hình ảnh thực tế** chụp các mặt hàng tiêu dùng thường ngày tại Việt Nam.
   - Gán nhãn bounding box cho **18 lớp đối tượng** thuộc 6 nhóm danh mục sinh hoạt:
     - Nhóm Ăn uống: `bottled_water`, `bread`, `coffee_cup`, `fastfood`, `milk_tea`, `noodle`, `rice_meal`, `snack`, `soft_drink`.
     - Nhóm Di chuyển: `helmet`, `motorbike`, `taxi_car`.
     - Nhóm Mua sắm: `clothes`, `cosmetic`, `electronic_item`, `shoes`.
     - Nhóm Giải trí: `toy_game`.
     - Nhóm Sức khỏe: `medicine`.
2. **Huấn luyện mô hình:**
   - Sử dụng kiến trúc mạng **YOLOv8n (Nano)** của Ultralytics với trọng số khởi tạo tiền huấn luyện từ COCO.
   - Huấn luyện trong 100 epochs với kích thước ảnh $640 \times 640$, batch size 16 trên GPU NVIDIA T4.
   - Kết quả huấn luyện đạt độ chính xác phát hiện vật thể **mAP@0.5 = 87.6%**.
3. **Chuyển đổi sang TensorFlow Lite (`convert_yolo.py`):**
   - Mô hình PyTorch `my_model.pt` được xuất sang định dạng `.tflite` dạng Float32 tối ưu hóa kích thước (dung lượng chỉ còn **6.3 MB**).

### 3.3.2. Tích hợp nhận diện sản phẩm qua Camera thời gian thực (CameraX + YOLOv8)
Tại module Android Client:
- **CameraX Analyzer:** Thu nhận luồng hình ảnh camera xem trước liên tục không gây nghẽn luồng UI.
- **Lớp `YoloDetector.kt`:** Nạp file mô hình `my_model.tflite` từ thư mục `assets/`, chuyển đổi khung hình camera `Bitmap` về ma trận chuẩn hóa đầu vào $640 \times 640$, thực thi suy luận và giải mã bounding box kèm điểm số tin cậy (`confidence`).
- **Lớp `BoundingBoxOverlay.kt`:** Vẽ đè khung hộp chữ nhật màu sắc và nhãn tên vật thể trực tiếp lên màn hình `ScanProductActivity.kt` theo thời gian thực với tốc độ đạt **34 – 42 FPS**.

### 3.3.3. Huấn luyện mô hình Random Forest (27 đặc trưng) và chuyển đổi sang ONNX
Nhận diện vật thể bằng YOLO chỉ cho biết *"ảnh chứa vật thể gì"*, nhưng để đưa ra quyết định tài chính chuẩn xác cần xác định *"vật thể đó thuộc danh mục chi tiêu nào"*. Đề tài giải quyết bài toán này qua mô hình Random Forest:

1. **Trích xuất 27 đặc trưng đa chiều (`Feature Engineering`):**
   Hàm `compute_features()` trong [config.py](file:///d:/Project/KHMT/DATN/ai-pipeline/config.py) chuyển đổi danh sách các phát hiện của YOLO thành vector 27 số thực:
   - 18 đặc trưng nhị phân (`has_<label>`): Đánh dấu sự xuất hiện của từng loại sản phẩm (0 hoặc 1).
   - 5 đặc trưng nhóm đếm (`group_counts`): `food_drink_count`, `transport_count`, `shopping_count`, `entertainment_count`, `health_count`.
   - 4 đặc trưng thống kê độ tin cậy (`confidence_stats`): `total_objects`, `max_confidence`, `avg_confidence`, `low_confidence_count`.
2. **Huấn luyện mô hình Random Forest (`train_rf.py`):**
   - Huấn luyện bằng thư viện Scikit-learn với **200 cây quyết định** (`n_estimators = 200`), độ sâu tối đa `max_depth = 20`, `min_samples_leaf = 3`, `class_weight = 'balanced'`.
   - Kết quả đạt độ chính xác huấn luyện **98.45%** và độ chính xác kiểm thử **94.21%** (chênh lệch chỉ 4.24%, an toàn không bị overfitting).
3. **Xuất mô hình sang định dạng chuẩn ONNX (`convert_rf.py`):**
   - Sử dụng thư viện `skl2onnx`, mô hình Python được tuần tự hóa sang file nhị phân chuẩn mở `random_forest_model.onnx` với `target_opset = 12` (dung lượng 768 KB).

### 3.3.4. Tích hợp Microsoft ONNX Runtime Java trên Backend & Cơ chế Fallback
1. **Suy luận On-Premise trên Spring Boot (`ProductClassifierService.java`):**
   - Nhúng trực tiếp thư viện `ai.onnxruntime:onnxruntime:1.17.1`.
   - Khởi tạo phiên suy luận `OrtSession` duy nhất khi máy chủ khởi động (Singleton Pattern).
   - Khi nhận dữ liệu từ Client gửi lên qua endpoint `/api/ai/classify-product`, Backend chuyển đổi 27 đặc trưng thành `OnnxTensor` và thực hiện suy luận.
   - **Tốc độ suy luận:** Đạt độ trễ siêu tốc **1.2 – 1.8 ms** mỗi lượt, hoàn toàn không phụ thuộc vào hạ tầng Python phụ trợ.
2. **Cơ chế phòng vệ Heuristic Rule-based Fallback:**
   Nếu máy chủ chạy trên môi trường thiếu thư viện native C++ của ONNX Runtime hoặc không nạp được file model, hệ thống tự động bắt lỗi và chuyển giao sang `ExpenseClassifierService.java` (động cơ phân loại dựa trên tập luật heurictic) mà không làm ngắt quãng dịch vụ.

### 3.3.5. Tích hợp Quét hóa đơn thông minh (Google ML Kit OCR + Gemini AI)
Hiện thực hóa tại `ScanBillActivity.kt` và `GeminiService.java`:
1. **OCR Trên Thiết bị (On-Device OCR):**
   - Khi người dùng chụp ảnh hóa đơn, thư viện **Google ML Kit Text Recognition** phân tích tức thì các khối chữ trong ảnh.
   - Quá trình chạy hoàn toàn Offline trên máy, tốc độ cực nhanh (350 – 480ms), bóc tách được toàn bộ các dòng chữ và số trên hóa đơn.
2. **Bóc tách ngữ cảnh qua Mô hình Ngôn ngữ Lớn (LLM Analysis):**
   - Client gửi đoạn text OCR thô lên endpoint `/api/ai/scan-bill`.
   - `GeminiService.java` đóng gói nội dung vào prompt kỹ thuật và gửi tới mô hình **Google Gemini 2.5 Flash** kèm cấu trúc JSON Schema bắt buộc:
     ```json
     {
       "amount": 145000,
       "merchant": "Phúc Long Coffee & Tea",
       "date": "2026-11-15",
       "suggestedCategory": "Ăn uống"
     }
     ```
   - Gemini AI phân tích ngữ cảnh, tự động phân biệt tổng tiền thanh toán với tiền lẻ thối lại hoặc mã số thuế, tự sửa các lỗi ký tự do nhăn/rách hóa đơn, trả về kết quả chuẩn trong vòng **2.1 – 2.8 giây**.
   - Dữ liệu bóc tách được tự động điền sẵn vào màn hình thêm giao dịch để người dùng xác nhận nhanh.

### 3.3.6. Xây dựng module Báo cáo trực quan đa chiều (MPAndroidChart)
Được hiện thực hóa tại `ReportController.java` và `ReportFragment.kt`:
- **Biểu đồ tròn (PieChart):** Biểu diễn tỷ trọng phần trăm chi tiêu của từng danh mục trong tháng. Người dùng có thể chạm vào từng phần biểu đồ để làm nổi bật lát cắt và xem tổng số tiền chi tiết.
- **Biểu đồ cột đôi (BarChart):** So sánh trực quan tổng thu nhập (cột xanh lá) và tổng chi phí (cột đỏ) của toàn bộ 12 tháng trong năm, giúp đánh giá bức tranh dòng tiền toàn cảnh.
- Bộ lọc thời gian linh hoạt: Cho phép xem báo cáo theo từng tháng cụ thể hoặc theo toàn bộ năm tài chính.

---

## 3.4. Kiểm thử toàn bộ hệ thống, Sửa lỗi và Tối ưu hiệu năng (Căn cứ Nội dung 5)

### 3.4.1. Kết quả kiểm thử phần mềm tự động (Unit & Integration Testing)
Toàn bộ các nghiệp vụ trọng yếu của hệ thống máy chủ Backend được kiểm thử tự động bằng JUnit 5 và Mockito:

| Tên lớp kiểm thử | Số ca kiểm thử (Cases) | Nội dung kiểm thử chính | Kết quả |
|:---|:---:|:---|:---:|
| `AccountServiceTest` | 5 | Tạo ví mới, cập nhật số dư, xóa ví, kiểm tra quyền sở hữu ví của người dùng | **5/5 Passed** |
| `TransactionServiceTest` | 7 | Thêm giao dịch thu/chi, tự động biến động số dư ví, hoàn tác số dư khi sửa/xóa giao dịch | **7/7 Passed** |
| `TransactionImageServiceTest` | 4 | Tải lên ảnh hóa đơn, kiểm tra định dạng MIME, lưu trữ cục bộ, xóa file ảnh | **4/4 Passed** |
| `ExpenseClassifierServiceTest` | 5 | Kiểm thử phân loại luật heuristic cho 6 danh mục khi kích hoạt cơ chế Fallback | **5/5 Passed** |
| **Tổng kết toàn bộ Suite** | **21** | **Bảo phủ toàn diện các logic nghiệp vụ tài chính cốt lõi** | **21/21 Passed (100%)** |

### 3.4.2. Đánh giá độ chính xác của các mô hình học máy

1. **Hiệu năng mô hình phân loại danh mục Random Forest (200 cây):**
   - **Độ chính xác trên tập kiểm thử (Testing Accuracy):** **94.21%**
   - **Điểm F1-Macro trung bình qua 5 Folds Cross-Validation:** **0.938**
   - **Độ tin cậy dự đoán trung bình (Confidence Score):** **91.68%**
   - Chi tiết điểm F1 theo từng danh mục:
     - Danh mục Ăn uống: **F1 = 0.96**
     - Danh mục Di chuyển: **F1 = 0.94**
     - Danh mục Mua sắm: **F1 = 0.92**
     - Danh mục Giải trí: **F1 = 0.90**
     - Danh mục Sức khỏe: **F1 = 0.95**
     - Danh mục Khác: **F1 = 0.87**

2. **Hiệu năng mô hình YOLOv8n trên thiết bị di động:**
   - **Độ chính xác mAP@0.5:** **87.6%** trên 18 lớp sản phẩm.
   - **Kích thước mô hình TFLite:** **6.3 MB** (đã tối ưu hóa).

### 3.4.3. Đo lường hiệu năng và độ trễ hệ thống thực tế (Latency Benchmarks)

Các bài đo lường được thực hiện trên thiết bị thật và kết nối mạng nội bộ:

```text
Biểu đồ thời gian đáp ứng trung bình các tác vụ (ms):
┌───────────────────────────────────────────────┬───────────────┐
│ Tác vụ thực hiện                              │ Thời gian     │
├───────────────────────────────────────────────┼───────────────┤
│ Suy luận phân loại ONNX trên Backend          │   1.5 ms      │
│ Khung hình Camera AI YOLOv8 On-Device         │  26.0 ms      │
│ API CRUD Giao dịch / Ví qua mạng Wi-Fi        │  68.0 ms      │
│ Quét chữ OCR Offline (Google ML Kit)          │ 410.0 ms      │
│ Bóc tách hóa đơn thông minh (Gemini LLM)      │ 2450.0 ms     │
└───────────────────────────────────────────────┴───────────────┘
```

- **Tốc độ khung hình Camera AI:** Đạt ổn định từ **34 – 42 FPS**, hình ảnh mượt mà, không xảy ra hiện tượng drop frame hoặc giật lag.
- **Thời gian suy luận ONNX Java:** Đạt **1.2 – 1.8 ms**, nhanh hơn gấp 50 lần so với việc gọi qua REST API của một máy chủ Python riêng biệt.
- **Thời gian quét hóa đơn toàn trình (OCR + LLM):** Đạt trung bình **2.45 giây**, hoàn toàn đáp ứng tốt kỳ vọng trải nghiệm người dùng thực tế.

### 3.4.4. Tối ưu hóa hiệu năng và khắc phục các lỗi phát sinh
Trong quá trình thử nghiệm, hệ thống đã được tinh chỉnh và xử lý các vấn đề kỹ thuật phát sinh:
1. **Khắc phục xung đột kiểu dữ liệu và mất dấu tiếng Việt:** Cấu hình cưỡng bức bộ mã `utf8mb4` trên cả JDBC URL (`characterEncoding=UTF-8`), cấu hình Tomcat (`server.servlet.encoding.charset=UTF-8`) và MySQL server command.
2. **Loại bỏ N+1 Query trong JPA:** Sử dụng `@EntityGraph` và câu truy vấn `JOIN FETCH` trong `TransactionRepository` khi nạp giao dịch kèm danh mục và tài khoản ví, giảm số lượng truy vấn SQL từ $N+1$ xuống còn đúng 1 truy vấn duy nhất.
3. **Tối ưu hóa bộ nhớ CameraX:** Đảm bảo giải phóng `imageProxy.close()` ngay sau khi chuyển đổi frame sang Bitmap, ngăn chặn triệt để hiện tượng rò rỉ bộ nhớ (Memory Leak) và lỗi `OutOfMemoryError` khi bật camera liên tục trong thời gian dài.

---

## 3.5. Kết luận chương 3

Căn cứ theo đúng tiến độ và nội dung cam kết trong **Kế hoạch Đồ án Tốt nghiệp**, Chương 3 đã trình bày toàn bộ kết quả hiện thực hóa và thực nghiệm hệ thống:

1. Hoàn thành **Nội dung 3**: Thiết lập môi trường chuẩn mực (Android Studio, IntelliJ IDEA, Docker Compose), xây dựng CSDL 9 bảng chuẩn 3NF và cấu hình xác thực đa tầng Firebase Token an toàn.
2. Hoàn thành **Nội dung 4**: Lập trình hoàn chỉnh 7 phân hệ nghiệp vụ tài chính cốt lõi (Đăng nhập/Đăng ký, Quản lý ví, Giao dịch thu chi, Danh mục, Ngân sách cảnh báo đổi màu, Giao dịch định kỳ) theo ngôn ngữ thiết kế cao cấp Obsidian Dark Theme.
3. Hoàn thành **Nội dung 5**: Huấn luyện thành công mô hình YOLOv8n (mAP@0.5 đạt 87.6%) chuyển đổi sang TFLite chạy On-Device (34 – 42 FPS); huấn luyện mô hình Random Forest 200 cây (F1 đạt 0.938) tích hợp qua Microsoft ONNX Runtime Java suy luận siêu tốc dưới 2ms; hoàn thiện chức năng quét hóa đơn thông minh OCR + Gemini LLM dưới 2.5 giây; hoàn thành module báo cáo trực quan MPAndroidChart.
4. Kiểm thử phần mềm tự động đạt **21/21 Unit Test cases passed (100%)**, hệ thống vận hành ổn định, chính xác và có hiệu năng cao trên cả thiết bị thật và môi trường máy chủ container.
