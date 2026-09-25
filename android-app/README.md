# 📱 Smart Personal Finance - Android Application

Ứng dụng quản lý tài chính cá nhân thông minh (*Smart Personal Finance App*) trên nền tảng Android, xây dựng theo kiến trúc chuẩn **MVVM (Model - View - ViewModel)** bằng ngôn ngữ **Kotlin**. Ứng dụng tích hợp công nghệ AI/ML tiên tiến (Google ML Kit, YOLOv8 TFLite, Random Forest) để tự động hóa việc nhận diện hóa đơn và phân loại chi tiêu sản phẩm.

---

## 📑 Mục lục
1. [Tính năng chính](#-tính-năng-chính)
2. [Công nghệ & Thư viện sử dụng](#-công-nghệ--thư-viện-sử-dụng)
3. [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
4. [Cấu trúc thư mục & giải thích chi tiết](#-cấu-trúc-thư-mục--giải-thích-chi-tiết)
5. [Luồng hoạt động chính (Data Flow)](#-luồng-hoạt-động-chính-data-flow)
6. [Tích hợp Trí tuệ Nhân tạo (ML / AI)](#-tích-hợp-trí-tuệ-nhân-tạo-ml--ai)
7. [Mạng & Giao tiếp Backend (Network Layer)](#-mạng--giao-tiếp-backend-network-layer)
8. [Hướng dẫn cài đặt & Chạy ứng dụng](#-hướng-dẫn-cài-đặt--chạy-ứng-dụng)

---

## 🌟 Tính năng chính

| Phân hệ | Tính năng chi tiết |
| :--- | :--- |
| **Xác thực (Auth)** | • Đăng nhập / Đăng ký qua Firebase Authentication (Email/Password)<br>• Hỗ trợ xác thực bằng tài khoản Google & Facebook<br>• Tự động đồng bộ tài khoản người dùng về Backend qua JWT Bearer Token |
| **Giao dịch (Transactions)** | • Thêm, sửa, xóa, xem danh sách giao dịch thu/chi<br>• Lọc giao dịch theo ví tài khoản, danh mục, thời gian<br>• Phân loại thu nhập (INCOME) / chi phí (EXPENSE) |
| **Giao dịch định kỳ (Recurring)** | • Thiết lập các khoản thu/chi tự động lặp lại theo chu kỳ (Hàng ngày, Hàng tuần, Hàng tháng, Hàng năm) |
| **Quản lý Tài khoản / Ví (Accounts)** | • Quản lý nhiều tài khoản/ví (Tiền mặt, Tài khoản ngân hàng, Thẻ tín dụng, Ví điện tử)<br>• Theo dõi số dư từng ví và tổng tài sản thời gian thực |
| **Ngân sách (Budgets)** | • Thiết lập hạn mức chi tiêu cho từng danh mục theo tháng<br>• Cảnh báo trực quan khi chi tiêu vượt hoặc sắp chạm ngưỡng ngân sách |
| **Danh mục (Categories)** | • Phân loại chi tiêu đa dạng (Ăn uống, Mua sắm, Di chuyển, Hóa đơn...) với icon và màu sắc trực quan |
| **Báo cáo & Thống kê (Reports)** | • Biểu đồ tròn (PieChart) & Biểu đồ cột phân tích tỷ trọng chi tiêu (MPAndroidChart)<br>• Theo dõi biến động tài chính theo tuần/tháng/năm |
| **AI Quét hóa đơn (OCR Scan Bill)** | • Chụp ảnh hóa đơn qua CameraX<br>• Nhận diện văn bản offline bằng **Google ML Kit Text Recognition**<br>• Gửi text trích xuất lên Backend AI để phân loại và tự động điền form giao dịch |
| **AI Quét sản phẩm (YOLO Scan)** | • Nhận diện vật thể thời gian thực qua CameraX sử dụng **YOLOv8 TFLite** (18 classes)<br>• Trích xuất vector 27 đặc trưng và phân loại sơ bộ qua **Random Forest Classifier**<br>• Gợi ý giá tiền, danh mục và cảnh báo chi tiêu cần thiết hay lãng phí |

---

## 🛠 Công nghệ & Thư viện sử dụng

- **Ngôn ngữ**: Kotlin (100%)
- **Hệ điều hành tối thiểu**: Android 7.0 (API Level 24 - `minSdk 24`)
- **Target SDK**: Android 14 (API Level 34 - `targetSdk 34`)
- **Kiến trúc**: MVVM (Model - View - ViewModel) + Repository Pattern
- **UI Components**:
  - Material Components Android, ViewBinding
  - MPAndroidChart (vẽ biểu đồ báo cáo tài chính)
  - CircleImageView (ảnh đại diện bo tròn)
  - Lottie Animations (hiệu ứng động)
- **Mạng (Networking)**:
  - Retrofit 2.9.0 & OkHttp 4.12.0
  - Gson Converter (Serialize/Deserialize JSON)
  - Custom Interceptors (`TokenInterceptor` đính kèm Firebase JWT, dynamic base URL)
- **Bảo mật & Xác thực**:
  - Firebase Authentication (Email, Google Sign-In, Facebook Login)
  - Encrypted / Custom `SharedPrefManager`
- **Camera & Machine Learning (On-device)**:
  - CameraX (v1.3.x): Camera điều khiển linh hoạt, tương thích đa thiết bị
  - Google ML Kit Text Recognition: Nhận diện chữ tiếng Việt / Latin trên hóa đơn
  - TensorFlow Lite (TFLite 2.14.0): Suy luận mô hình YOLOv8 trực tiếp trên thiết bị Android
  - Model Random Forest tùy chỉnh (phân loại nhãn chi tiêu từ 27 feature vectors)

---

## 🏛 Kiến trúc hệ thống

Ứng dụng tuân thủ nghiêm ngặt mô hình kiến trúc **MVVM** kết hợp với **Repository Pattern**:

```
┌────────────────────────────────────────────────────────┐
│                      UI LAYER                          │
│   Activities & Fragments (ViewBinding, UI Observers)   │
└──────────────────────────┬─────────────────────────────┘
                           │ Lắng nghe LiveData / Gửi event
                           ▼
┌────────────────────────────────────────────────────────┐
│                   VIEWMODEL LAYER                      │
│      ViewModels (Quản lý trạng thái, Coroutines)       │
└──────────────────────────┬─────────────────────────────┘
                           │ Gọi hàm xử lý dữ liệu
                           ▼
┌────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                      │
│  Tập trung hóa xử lý dữ liệu, phân luồng Network/Local │
└─────────────┬────────────────────────────┬─────────────┘
              │                            │
              ▼                            ▼
┌───────────────────────────┐ ┌──────────────────────────┐
│       NETWORK / API       │ │      LOCAL STORAGE       │
│ Retrofit2 + OkHttp Client │ │    SharedPrefManager     │
│   (Firebase Token Auth)   │ │  (SharedPreferences/Gson)│
└───────────────────────────┘ └──────────────────────────┘
```

---

## 📂 Cấu trúc thư mục & Giải thích chi tiết

Mã nguồn tại thư mục: `app/src/main/java/com/example/personalfinance/`

```
com.example.personalfinance/
├── activities/                  # Các màn hình Activity chính và luồng Camera
│   ├── BaseActivity.kt          # Lớp cơ sở cho Activity (hỗ trợ cấu hình chung)
│   ├── LoginActivity.kt         # Màn hình đăng nhập (Firebase / Google / Facebook)
│   ├── MainActivity.kt          # Màn hình chính (chứa BottomNavigation & Container Fragment)
│   ├── RegisterActivity.kt      # Màn hình đăng ký tài khoản mới
│   ├── ScanBillActivity.kt      # Màn hình CameraX quét hóa đơn qua OCR
│   ├── ScanProductActivity.kt   # Màn hình CameraX quét vật thể bằng YOLOv8
│   └── SplashActivity.kt        # Màn hình khởi động, kiểm tra session đăng nhập
│
├── adapters/                    # Bộ chuyển đổi dữ liệu hiển thị lên RecyclerView
│   ├── AccountAdapter.kt        # Hiển thị danh sách ví / tài khoản ngân hàng
│   ├── BudgetAdapter.kt         # Hiển thị danh sách các mục ngân sách & tiến độ chi tiêu
│   ├── CategoryAdapter.kt       # Hiển thị danh mục chi tiêu (kèm icon, màu sắc)
│   ├── RecurringAdapter.kt      # Hiển thị danh sách các khoản thu/chi định kỳ
│   └── TransactionAdapter.kt    # Hiển thị lịch sử các giao dịch thu/chi
│
├── api/                         # Tầng kết nối mạng (REST API Client)
│   ├── ApiCallback.kt           # Interface generic xử lý callback onSuccess / onError
│   ├── ApiCallExtensions.kt     # Extension functions hỗ trợ gọi Retrofit Call an toàn
│   ├── ApiService.kt            # Khai báo toàn bộ các REST endpoints Backend
│   ├── RetrofitClient.kt        # Khởi tạo singleton Retrofit, cấu hình Timeout, Converter
│   └── TokenInterceptor.kt      # Tự động lấy Firebase ID Token và gán vào Header: Bearer <token>
│
├── firebase/                    # Tầng tích hợp dịch vụ Firebase
│   ├── FirebaseAuthCallback.kt  # Interface nhận kết quả đăng nhập / đăng ký Firebase
│   └── FirebaseAuthManager.kt   # Singleton quản lý đăng nhập Email, Google, Facebook & lấy Token
│
├── fragments/                   # Các màn hình con (UI Fragments)
│   ├── account/                 # Phân hệ quản lý Tài khoản / Ví
│   │   ├── AccountFragment.kt       # Danh sách tài khoản & tổng số dư
│   │   └── AddAccountFragment.kt    # Form thêm/sửa tài khoản ví
│   ├── budget/                  # Phân hệ Ngân sách
│   │   ├── AddBudgetFragment.kt     # Form tạo ngân sách chi tiêu
│   │   └── BudgetFragment.kt        # Xem danh sách và tiến độ các ngân sách
│   ├── category/                # Phân hệ Danh mục chi tiêu
│   │   └── CategoryFragment.kt      # Quản lý danh mục (ăn uống, mua sắm...)
│   ├── home/                    # Màn hình trang chủ & thống kê
│   │   ├── HomeFragment.kt          # Tổng quan số dư, giao dịch gần đây, biểu đồ tròn
│   │   └── ReportFragment.kt        # Báo cáo chi tiết thu/chi theo thời gian
│   ├── profile/                 # Phân hệ người dùng cá nhân
│   │   └── ProfileFragment.kt       # Thông tin cá nhân, cài đặt và đăng xuất
│   ├── recurring/               # Phân hệ Giao dịch định kỳ
│   │   ├── AddRecurringFragment.kt  # Form tạo giao dịch tự động lặp lại
│   │   └── RecurringFragment.kt     # Quản lý danh sách giao dịch định kỳ
│   └── transaction/             # Phân hệ Giao dịch
│       ├── AddTransactionFragment.kt # Form tạo/sửa giao dịch (hỗ trợ nhận dữ liệu từ AI Scanner)
│       └── TransactionFragment.kt    # Danh sách lịch sử tất cả các giao dịch
│
├── ml/                          # Tích hợp Machine Learning trên thiết bị
│   ├── ProductRandomForestClassifier.kt # Trích xuất 27 features & phân loại Random Forest
│   ├── TextRecognizerHelper.kt          # Xử lý ảnh CameraX với Google ML Kit OCR
│   └── yolo/                            # Module nhận diện vật thể YOLOv8
│       ├── BoundingBoxOverlay.kt        # View vẽ khung chữ nhật bao quanh vật thể thời gian thực
│       └── YoloDetector.kt              # Nạp model TFLite, xử lý ảnh đầu vào 640x640 & NMS
│
├── models/                      # Mô hình dữ liệu
│   ├── domain/                  # Các đối tượng nghiệp vụ thuần (Entity / Business Data)
│   │   ├── Account.kt               # Thực thể Ví / Tài khoản
│   │   ├── Budget.kt                # Thực thể Ngân sách
│   │   ├── Category.kt              # Thực thể Danh mục
│   │   ├── MonthlyReport.kt         # Dữ liệu báo cáo thống kê tháng
│   │   ├── RecurringTransaction.kt  # Thực thể Giao dịch định kỳ
│   │   ├── Transaction.kt           # Thực thể Giao dịch thu/chi
│   │   └── User.kt                  # Thông tin người dùng
│   └── dto/                     # Data Transfer Objects (trao đổi dữ liệu với REST API)
│       ├── ApiResponse.kt           # Wrapper chuẩn cho response từ server
│       ├── AuthResponse.kt          # Response sau khi xác thực
│       ├── BudgetProgress.kt        # DTO tiến độ hoàn thành ngân sách
│       ├── CreateAccountRequest.kt  # DTO payload tạo ví mới
│       ├── CreateBudgetRequest.kt   # DTO payload tạo ngân sách
│       ├── CreateRecurringRequest.kt# DTO payload tạo giao dịch định kỳ
│       ├── CreateTransactionRequest.kt # DTO payload tạo giao dịch
│       ├── OcrClassifyRequest.kt    # DTO gửi văn bản OCR lên backend
│       └── ProductClassifyRequest.kt# DTO gửi thông tin nhận diện sản phẩm lên backend
│
├── repositories/                # Tầng quản lý dữ liệu (Repository Layer)
│   ├── AccountRepository.kt     # Tương tác API ví tài khoản
│   ├── BudgetRepository.kt      # Tương tác API ngân sách
│   ├── CategoryRepository.kt    # Tương tác API danh mục
│   └── TransactionRepository.kt # Tương tác API giao dịch & báo cáo
│
├── utils/                       # Các hàm tiện ích
│   ├── CurrencyFormatter.kt     # Định dạng tiền tệ (VND, phân tách hàng nghìn)
│   ├── DateUtils.kt             # Xử lý chuỗi ngày tháng (ISO 8601, format hiển thị)
│   ├── NotificationHelper.kt    # Quản lý thông báo đẩy cục bộ
│   └── SharedPrefManager.kt     # Lưu trữ Session, User Profile, Token qua SharedPreferences
│
└── viewmodels/                  # Tầng ViewModel (State Management)
    ├── AccountViewModel.kt      # Quản lý trạng thái và dữ liệu ví
    ├── BudgetViewModel.kt       # Quản lý trạng thái và dữ liệu ngân sách
    ├── CategoryViewModel.kt     # Quản lý trạng thái danh mục
    ├── HomeViewModel.kt         # Quản lý số liệu trang chủ và biểu đồ
    └── TransactionViewModel.kt   # Quản lý lịch sử giao dịch và thêm/xóa/sửa
```

---

## 🔄 Luồng hoạt động chính (Data Flow)

### 1. Luồng Xác thực (Authentication Flow)
```
[User] ──(Email/Password/Google/FB)──► [LoginActivity]
                                              │
                                              ▼
                                   [FirebaseAuthManager]
                                              │
                   ┌──────────────────────────┴──────────────────────────┐
                   ▼                                                     ▼
           (Firebase Auth SDK)                                   (Firebase ID Token)
                   │                                                     │
                   ▼                                                     ▼
        Đăng nhập thành công                                    [TokenInterceptor]
                   │                                                     │
                   └──────────────────────────┬──────────────────────────┘
                                              ▼
                                     [POST /api/auth/sync]
                                              │
                                              ▼
                                    Lưu User vào Local
                                   [SharedPrefManager]
                                              │
                                              ▼
                                       [MainActivity]
```

### 2. Luồng Quét Hóa Đơn AI (OCR Bill Scanning Flow)
```
[User chụp hóa đơn] ──► [ScanBillActivity (CameraX)]
                                 │
                                 ▼
                     [TextRecognizerHelper (ML Kit)]
                                 │ Trích xuất văn bản thô
                                 ▼
                 [POST /api/ai-scan/classify (Retrofit)]
                                 │
                                 ▼
              Backend phân tích (Tổng tiền, Ngày, Danh mục)
                                 │
                                 ▼
                 Prefill dữ liệu sang [AddTransactionFragment]
                                 │
                                 ▼
                         Người dùng xác nhận & Lưu
```

### 3. Luồng Quét Sản Phẩm AI (YOLO Product Scanning Flow)
```
[Camera preview liên tục] ──► [ScanProductActivity]
                                      │
                                      ▼
                        [YoloDetector (TFLite 640x640)]
                                      │ Bounding boxes + Label
                                      ▼
                      [BoundingBoxOverlay (Vẽ khung hình)]
                                      │
                                      ▼
                   [ProductRandomForestClassifier (27 features)]
                                      │
                                      ▼
                   [POST /api/ai-product/classify (Backend)]
                                      │
                                      ▼
                Hiển thị kết quả: Tên món, Giá đề xuất, Cảnh báo
```

---

## 🤖 Tích hợp Trí tuệ Nhân tạo (ML / AI)

### 1. Nhận diện chữ viết hóa đơn (Google ML Kit Text Recognition)
- Chạy **on-device (hoàn toàn ngoại tuyến)** không cần kết nối mạng để đọc chữ.
- Sử dụng camera của CameraX để bắt khung hình chất lượng cao.
- Nhận diện các trường: Tên hóa đơn, ngày tháng, danh sách sản phẩm và tổng tiền thanh toán.
- Gửi kết quả văn bản lên backend AI Service để phân loại chính xác danh mục thu/chi.

### 2. Nhận diện vật thể & phân loại chi tiêu (YOLOv8 + Random Forest)
- **Model Object Detection**: YOLOv8 định dạng TFLite (`best_float32.tflite` / `yolov8.tflite`), kích thước đầu vào `640x640`.
- Hỗ trợ 18 danh mục sản phẩm tiêu dùng phổ biến.
- **Random Forest Classifier**: Trích xuất vector 27 thuộc tính từ vật thể được nhận diện (kích thước bounding box, tỉ lệ khung hình, độ tin cậy, phân bố không gian...) để đưa ra gợi ý phân loại ban đầu trước khi đồng bộ backend.

---

## 🌐 Mạng & Giao tiếp Backend (Network Layer)

- **Base URL cấu hình động**: Được quản lý tập trung tại `RetrofitClient.kt`.
- **Xác thực tự động**: `TokenInterceptor` tự động lấy Firebase JWT Token mới nhất và thêm vào header:
  ```http
  Authorization: Bearer <FIREBASE_ID_TOKEN>
  ```
- **Xử lý Callbacks**: Áp dụng Extension Function [ApiCallExtensions.kt](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/android-app/app/src/main/java/com/example/personalfinance/api/ApiCallExtensions.kt) giúp code ngắn gọn, tự động bắt lỗi mạng và parse response chuẩn.

### Các Endpoints chính trên Backend
- `POST /api/auth/sync`: Đồng bộ thông tin người dùng từ Firebase về cơ sở dữ liệu.
- `GET /api/accounts`: Lấy danh sách ví của người dùng.
- `POST /api/accounts`: Tạo mới ví tài khoản.
- `GET /api/transactions`: Lấy danh sách giao dịch (hỗ trợ phân trang, lọc).
- `POST /api/transactions`: Thêm giao dịch thu/chi mới.
- `GET /api/budgets/progress`: Lấy tiến độ chi tiêu theo ngân sách.
- `GET /api/categories`: Lấy toàn bộ danh mục thu chi.
- `POST /api/ai-scan/classify`: Phân loại nội dung hóa đơn từ văn bản OCR.
- `POST /api/ai-product/classify`: Phân tích sản phẩm quét từ camera và trả về gợi ý ngân sách.

---

## 🚀 Hướng dẫn cài đặt & Chạy ứng dụng

### 1. Yêu cầu môi trường
- **Android Studio**: Hedgehog (2023.1.1) trở lên hoặc Koala / Ladybug
- **JDK**: Java 17
- **Gradle**: 8.x (sử dụng Gradle Wrapper đi kèm dự án)
- Thiết bị thật hoặc máy ảo Android chạy **Android 7.0 (API 24)** trở lên có hỗ trợ Google Play Services (để dùng Firebase & ML Kit) và Camera (để dùng tính năng quét).

### 2. Cấu hình Firebase
1. Truy cập [Firebase Console](https://console.firebase.google.com/).
2. Tạo dự án mới hoặc sử dụng dự án hiện có.
3. Thêm ứng dụng Android với package name: `com.example.personalfinance`.
4. Tải file `google-services.json` và đặt vào thư mục:
   ```
   android-app/app/google-services.json
   ```
5. Kích hoạt tính năng **Authentication**:
   - Bật **Email/Password**
   - Bật **Google Sign-In** (cấu hình mã SHA-1 fingerprint từ máy phát triển)
   - Bật **Facebook Login** (nếu sử dụng)

### 3. Cấu hình Model AI TFLite
Đảm bảo file model đã được đặt trong thư mục assets:
```
android-app/app/src/main/assets/yolo_product.tflite
```

### 4. Build và Chạy ứng dụng
1. Mở thư mục `android-app` bằng Android Studio.
2. Chờ Android Studio đồng bộ Gradle (`Sync Project with Gradle Files`).
3. Kiểm tra Base URL trong file `RetrofitClient.kt` trỏ tới địa chỉ server backend đang chạy (ngrok hoặc IP mạng LAN).
4. Nhấn **Run (Shift + F10)** để cài đặt và chạy ứng dụng trên thiết bị / máy ảo.

