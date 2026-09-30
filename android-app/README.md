# 📱 Smart Personal Finance - Android Application

Ứng dụng quản lý tài chính cá nhân thông minh (*Smart Personal Finance App*) trên nền tảng Android, xây dựng theo kiến trúc chuẩn **MVVM (Model - View - ViewModel)** bằng ngôn ngữ **Kotlin**. Ứng dụng tích hợp công nghệ AI/ML tiên tiến (Google ML Kit Text Recognition, YOLOv8 TFLite, Random Forest Classifier) để tự động hóa việc nhận diện hóa đơn và phân loại chi tiêu sản phẩm.

---

## 📑 Mục lục
1. [Tính năng chính](#-tính-năng-chính)
2. [Công nghệ & Thư viện sử dụng](#-công-nghệ--thư-viện-sử-dụng)
3. [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
4. [Cấu trúc thư mục & Giải thích chi tiết](#-cấu-trúc-thư-mục--giải-thích-chi-tiết)
5. [Cấu trúc Tài nguyên Giao diện (res/)](#-cấu-trúc-tài-nguyên-giao-diện-res)
6. [Luồng hoạt động chính (Data Flow)](#-luồng-hoạt-động-chính-data-flow)
7. [Tích hợp Trí tuệ Nhân tạo (ML / AI)](#-tích-hợp-trí-tuệ-nhân-tạo-ml--ai)
8. [Mạng & Giao tiếp Backend (Network Layer)](#-mạng--giao-tiếp-backend-network-layer)
9. [Hướng dẫn cài đặt & Chạy ứng dụng](#-hướng-dẫn-cài-đặt--chạy-ứng-dụng)

---

## 🌟 Tính năng chính

| Phân hệ | Tính năng chi tiết |
| :--- | :--- |
| **Xác thực (Auth)** | • Đăng nhập / Đăng ký qua Firebase Authentication (Email/Password)<br>• Hỗ trợ xác thực bằng tài khoản Google & Facebook<br>• Tự động đồng bộ tài khoản người dùng về Backend qua JWT Bearer Token |
| **Giao dịch (Transactions)** | • Thêm, sửa, xóa, xem danh sách giao dịch thu/chi<br>• Lọc giao dịch theo ví tài khoản, danh mục, thời gian<br>• Phân loại thu nhập (INCOME) / chi phí (EXPENSE)<br>• Đính kèm và xem chi tiết ảnh chụp hóa đơn/giao dịch |
| **Giao dịch định kỳ (Recurring)** | • Thiết lập các khoản thu/chi tự động lặp lại theo chu kỳ (Hàng ngày, Hàng tuần, Hàng tháng, Hàng năm)<br>• Bật/tắt trạng thái hoạt động của từng khoản định kỳ |
| **Quản lý Tài khoản / Ví (Accounts)** | • Quản lý nhiều tài khoản/ví (Tiền mặt, Tài khoản ngân hàng, Thẻ tín dụng, Ví điện tử)<br>• Theo dõi số dư từng ví và tổng tài sản thời gian thực |
| **Ngân sách (Budgets)** | • Thiết lập hạn mức chi tiêu cho từng danh mục theo tháng<br>• Cảnh báo trực quan khi chi tiêu vượt hoặc sắp chạm ngưỡng ngân sách |
| **Danh mục (Categories)** | • Phân loại chi tiêu đa dạng (Ăn uống, Mua sắm, Di chuyển, Hóa đơn...) với icon và màu sắc trực quan |
| **Báo cáo & Thống kê (Reports)** | • Biểu đồ tròn (PieChart) phân tích tỷ trọng chi tiêu (MPAndroidChart)<br>• Thống kê chi tiết thu/chi theo ngày, tuần, tháng, danh mục |
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
  - Custom Interceptors (`AuthInterceptor` đính kèm Firebase JWT, dynamic base URL bypass ngrok)
- **Bảo mật & Xác thực**:
  - Firebase Authentication (Email, Google Sign-In, Facebook Login)
  - `SharedPrefManager` quản lý cache người dùng & IP máy chủ
- **Camera & Machine Learning (On-device)**:
  - CameraX (v1.3.x): Camera điều khiển linh hoạt, tương thích đa thiết bị
  - Google ML Kit Text Recognition: Nhận diện chữ tiếng Việt / Latin trên hóa đơn
  - TensorFlow Lite (TFLite 2.14.0): Suy luận mô hình YOLOv8 trực tiếp trên thiết bị Android (`yolo_product.tflite`)
  - Random Forest Classifier tùy chỉnh (phân loại nhãn chi tiêu từ 27 feature vectors)

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
│  ApiClient + OkHttpClient │ │    SharedPrefManager     │
│   (AuthInterceptor JWT)   │ │  (SharedPreferences/Gson)│
└───────────────────────────┘ └──────────────────────────┘
```

---

## 📂 Cấu trúc thư mục & Giải thích chi tiết

Mã nguồn Kotlin tại thư mục: `app/src/main/java/com/example/personalfinance/`

```
com.example.personalfinance/
├── activities/                  # Các màn hình Activity chính và luồng Camera
│   ├── BaseActivity.kt          # Lớp cơ sở cho Activity (hỗ trợ cấu hình chung)
│   ├── LoginActivity.kt         # Màn hình đăng nhập (Firebase / Google / Facebook, đổi IP server)
│   ├── MainActivity.kt          # Màn hình chính (chứa BottomNavigation & Container Fragment)
│   ├── RegisterActivity.kt      # Màn hình đăng ký tài khoản mới
│   ├── ScanBillActivity.kt      # Màn hình CameraX quét hóa đơn qua Google ML Kit OCR
│   ├── ScanProductActivity.kt   # Màn hình CameraX quét vật thể bằng YOLOv8 TFLite
│   └── SplashActivity.kt        # Màn hình khởi động, kiểm tra session đăng nhập
│
├── adapters/                    # Bộ chuyển đổi dữ liệu hiển thị lên RecyclerView
│   ├── CalendarGridAdapter.kt   # Hiển thị lịch dạng lưới theo ngày và chỉ số chi tiêu
│   ├── CategoryStatsAdapter.kt  # Hiển thị thống kê danh mục chi tiêu kèm thanh tỷ lệ
│   ├── DayTransactionsAdapter.kt# Danh sách chi tiết giao dịch trong một ngày cụ thể
│   ├── HorizontalAccountAdapter.kt # Hiển thị danh sách thẻ ví vuốt ngang
│   └── TransactionAdapter.kt    # Hiển thị lịch sử các giao dịch thu/chi chính
│
├── api/                         # Tầng kết nối mạng chuẩn hóa (REST API Client)
│   ├── ApiCallExtensions.kt     # Extension functions hỗ trợ gọi Retrofit Call an toàn & ApiCallback
│   ├── ApiClient.kt             # Singleton Retrofit Client, cấu hình Timeout, Converter, dynamic Base URL
│   ├── ApiService.kt            # Interface khai báo toàn bộ các REST endpoints Backend
│   └── AuthInterceptor.kt       # OkHttp Interceptor tự động lấy Firebase JWT Token và gán Bearer Header
│
├── firebase/                    # Tầng tích hợp dịch vụ Firebase
│   ├── FirebaseAuthCallback.kt  # Interface nhận kết quả đăng nhập / đăng ký Firebase
│   └── FirebaseAuthHelper.kt    # Quản lý đăng nhập Email, Google, Facebook & lấy ID Token
│
├── fragments/                   # Các màn hình con (UI Fragments)
│   ├── account/                 # Phân hệ quản lý Tài khoản / Ví
│   │   ├── AccountDetailsFragment.kt # Xem chi tiết và lịch sử giao dịch của một tài khoản ví
│   │   └── AddAccountFragment.kt     # Form thêm/sửa tài khoản ví
│   ├── budget/                  # Phân hệ Ngân sách
│   │   └── AddBudgetFragment.kt      # Form tạo và thiết lập hạn mức ngân sách
│   ├── category/                # Phân hệ Danh mục chi tiêu
│   │   └── CategoryLimitFragment.kt  # Quản lý hạn mức và danh mục chi tiêu
│   ├── home/                    # Màn hình trang chủ & thống kê
│   │   └── HomeFragment.kt           # Tổng quan số dư, giao dịch gần đây, biểu đồ tròn
│   ├── profile/                 # Phân hệ người dùng cá nhân
│   │   └── ProfileFragment.kt        # Thông tin cá nhân, cập nhật avatar/tên, cài đặt và đăng xuất
│   ├── recurring/               # Phân hệ Giao dịch định kỳ
│   │   └── RecurringListFragment.kt  # Danh sách, bật/tắt và quản lý giao dịch lặp lại
│   └── transaction/             # Phân hệ Giao dịch
│       ├── AddRecurringFragment.kt   # BottomSheet thêm giao dịch định kỳ
│       ├── AddTransactionFragment.kt # Form tạo/sửa giao dịch (nhận dữ liệu từ OCR / YOLO)
│       ├── DayDetailFragment.kt      # Chi tiết thu/chi trong ngày
│       ├── DayTransactionsBottomSheet.kt # Bảng trượt xem danh sách giao dịch ngày
│       ├── TransactionFragment.kt    # Màn hình thống kê và báo cáo giao dịch tổng quan
│       ├── TransactionListFragment.kt# Danh sách lịch sử tất cả các giao dịch
│       └── TransactionPhotoDetailDialog.kt # Hộp thoại xem chi tiết ảnh hóa đơn đính kèm
│
├── ml/                          # Tích hợp Machine Learning trên thiết bị
│   ├── ProductRandomForestClassifier.kt # Trích xuất 27 features & phân loại Random Forest
│   ├── ProductRandomForestModel.kt      # Cây quyết định (Decision Trees) nhúng cục bộ
│   └── yolo/                            # Module nhận diện vật thể YOLOv8
│       ├── BoundingBoxOverlay.kt        # Custom View vẽ khung chữ nhật bao quanh vật thể thời gian thực
│       └── YoloDetector.kt              # Nạp model TFLite, tiền xử lý ảnh 640x640 & thuật toán NMS
│
├── models/                      # Mô hình dữ liệu
│   ├── domain/                  # Các thực thể nghiệp vụ thuần (Business Entities)
│   │   ├── Account.kt               # Thực thể Ví / Tài khoản
│   │   ├── Budget.kt                # Thực thể Ngân sách
│   │   ├── CalendarDay.kt           # Dữ liệu ngày trên lịch giao dịch
│   │   ├── Category.kt              # Thực thể Danh mục thu/chi
│   │   ├── RecurringTransaction.kt  # Thực thể Giao dịch định kỳ
│   │   ├── Transaction.kt           # Thực thể Giao dịch thu/chi
│   │   └── User.kt                  # Thông tin người dùng
│   └── dto/                     # Data Transfer Objects (trao đổi dữ liệu với REST API)
│       ├── AiProductResult.kt       # Kết quả phân tích sản phẩm từ AI
│       ├── AiScanResult.kt          # Kết quả trích xuất thông tin hóa đơn từ AI
│       ├── ApiResponse.kt           # Generic wrapper phản hồi từ Backend (`success`, `data`, `message`)
│       ├── LoginRequest.kt          # Payload đăng nhập Firebase Token
│       ├── OcrRequest.kt            # Payload gửi văn bản OCR hóa đơn
│       ├── ProductClassificationRequest.kt # Payload gửi danh sách vật thể YOLO
│       ├── ProductFeedbackRequest.kt# Payload gửi phản hồi đánh giá sản phẩm
│       ├── ReportDTO.kt             # DTO báo cáo thống kê thu chi theo kỳ
│       └── ScanFeedbackRequest.kt   # Payload gửi phản hồi kết quả quét hóa đơn
│
├── repositories/                # Tầng quản lý dữ liệu (Repository Layer)
│   ├── AccountRepository.kt     # Tương tác API ví tài khoản & danh mục
│   ├── AuthRepository.kt        # Tương tác API xác thực và đồng bộ User
│   ├── BudgetRepository.kt      # Tương tác API ngân sách
│   └── TransactionRepository.kt # Tương tác API giao dịch, báo cáo & gửi feedback AI
│
├── utils/                       # Các hàm tiện ích
│   ├── Constants.kt             # Các hằng số dùng chung trong ứng dụng
│   ├── CurrencyFormatter.kt     # Định dạng tiền tệ VND (phân tách hàng nghìn)
│   ├── DateUtils.kt             # Xử lý chuỗi ngày tháng (ISO 8601, format hiển thị)
│   └── SharedPrefManager.kt     # Lưu trữ Session, User Profile, Server IP qua SharedPreferences
│
└── viewmodels/                  # Tầng ViewModel (State Management)
    ├── AccountViewModel.kt      # Quản lý trạng thái và danh sách ví tài khoản
    ├── AuthViewModel.kt         # Quản lý trạng thái xác thực và đăng nhập
    ├── BudgetViewModel.kt       # Quản lý trạng thái và dữ liệu ngân sách
    ├── HomeViewModel.kt         # Quản lý số liệu trang chủ và biểu đồ
    └── TransactionViewModel.kt  # Quản lý lịch sử giao dịch và thêm/xóa/sửa
```

---

## 🎨 Cấu trúc Tài nguyên Giao diện (`res/`)

Thư mục tài nguyên tại `app/src/main/res/` được chuẩn hóa theo quy tắc tiền tố (**Prefix Naming Convention**) của Google Android, giúp các tệp cùng nhóm tự động gom cạnh nhau theo bảng chữ cái:

```
app/src/main/res/
├── drawable/                    # 37 tệp: Icon vector, background shape bo góc, logo
│   ├── bg_*                     # Background shapes, viền, gradient (bg_button_rounded.xml, bg_input_field.xml...)
│   ├── ic_*                     # Icon vector (ic_home.xml, ic_scan.xml, ic_budget.xml, ic_add.xml...)
│   └── ic_app_logo_glow.png     # Logo ứng dụng chính thức
│
├── layout/                      # 40 tệp: Giao diện XML phân loại theo quy chuẩn tiền tố
│   ├── activity_*               # Giao diện màn hình chính (activity_main, activity_login...)
│   ├── fragment_*               # Giao diện màn hình con (fragment_home, fragment_transaction...)
│   ├── item_*                   # Thiết kế từng dòng danh sách RecyclerView (item_transaction, item_calendar_day...)
│   ├── dialog_*                 # Hộp thoại pop-up (dialog_change_password, dialog_month_year_picker...)
│   └── bottom_sheet_*           # Bảng trượt từ đáy màn hình (bottom_sheet_add_options, bottom_sheet_day_transactions...)
│
├── menu/                        # Menu điều hướng
│   └── bottom_nav_menu.xml      # Định nghĩa 5 tab trên thanh điều hướng Bottom Navigation
│
└── values/                      # Định nghĩa giá trị hệ thống
    ├── colors.xml               # Bảng màu chủ đạo, màu nền thẻ, màu trạng thái thu/chi
    ├── strings.xml              # Chuỗi văn bản hiển thị toàn ứng dụng (hỗ trợ bản địa hóa)
    └── themes.xml               # Thiết lập chủ đề Material 3 (NoActionBar)
```

> [!NOTE]
> Các thư mục rỗng `mipmap-*` tự sinh ban đầu từ template Android Studio đã được dọn dẹp sạch sẽ do biểu tượng ứng dụng đã được cấu hình trực tiếp qua `@drawable/ic_app_logo_glow` trong `AndroidManifest.xml`.

---

## 🔄 Luồng hoạt động chính (Data Flow)

### 1. Luồng Xác thực (Authentication Flow)
```
[User] ──(Email/Password/Google/FB)──► [LoginActivity]
                   │
                   ▼
          [FirebaseAuthHelper]
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
 (Firebase Auth SDK)  (Firebase ID Token)
         │                   │
         ▼                   ▼
  Đăng nhập thành công   [AuthInterceptor]
         │                   │
         └─────────┬─────────┘
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
              [Google ML Kit Text Recognition (On-device)]
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
- Chạy **on-device (hoàn toàn ngoại tuyến)** không cần kết nối mạng để đọc chữ từ ảnh hóa đơn chụp qua CameraX.
- Trích xuất văn bản thô và gửi lên backend AI Service (`/api/ai-scan/classify`) để phân tích cú pháp (regex + rule-based) trích xuất ngày, số tiền và loại hóa đơn tự động.

### 2. Nhận diện vật thể & phân loại chi tiêu (YOLOv8 + Random Forest)
- **Model Object Detection**: YOLOv8 định dạng TFLite (`yolo_product.tflite` trong `assets/`), kích thước đầu vào `640x640`.
- Hỗ trợ 18 danh mục sản phẩm tiêu dùng phổ biến.
- **Random Forest Classifier**: Trích xuất vector 27 thuộc tính từ vật thể được nhận diện (kích thước bounding box, tỉ lệ khung hình, độ tin cậy, phân bố không gian...) để đưa ra gợi ý phân loại ban đầu trước khi đồng bộ backend (`/api/ai-product/classify`).

---

## 🌐 Mạng & Giao tiếp Backend (Network Layer)

- **Base URL cấu hình động**: Được quản lý tập trung tại [ApiClient.kt](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/android-app/app/src/main/java/com/example/personalfinance/api/ApiClient.kt). Hỗ trợ nhấn giữ logo ở màn hình Login để đổi IP server cục bộ tức thời khi kiểm thử.
- **Xác thực tự động**: [AuthInterceptor.kt](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/android-app/app/src/main/java/com/example/personalfinance/api/AuthInterceptor.kt) tự động lấy Firebase JWT Token mới nhất và thêm vào header:
  ```http
  Authorization: Bearer <FIREBASE_ID_TOKEN>
  ngrok-skip-browser-warning: true
  ```
- **Xử lý Callbacks**: Áp dụng Extension Function [ApiCallExtensions.kt](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/android-app/app/src/main/java/com/example/personalfinance/api/ApiCallExtensions.kt) giúp code ngắn gọn, tự động bắt lỗi mạng và parse response chuẩn.

### Các Endpoints chính trên Backend
- `POST /api/auth/sync`: Đồng bộ thông tin người dùng từ Firebase về cơ sở dữ liệu.
- `GET /api/accounts`: Lấy danh sách ví của người dùng.
- `POST /api/accounts`: Tạo mới ví tài khoản.
- `GET /api/transactions`: Lấy danh sách giao dịch (hỗ trợ phân trang, lọc).
- `POST /api/transactions`: Thêm giao dịch thu/chi mới.
- `GET /api/budgets`: Lấy danh sách ngân sách và tiến độ chi tiêu.
- `POST /api/budgets`: Thiết lập ngân sách mới.
- `GET /api/categories`: Lấy danh mục thu chi.
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
3. Kiểm tra Base URL trong file `ApiClient.kt` trỏ tới địa chỉ server backend đang chạy (ngrok hoặc IP mạng LAN).
4. Nhấn **Run (Shift + F10)** để cài đặt và chạy ứng dụng trên thiết bị / máy ảo.
