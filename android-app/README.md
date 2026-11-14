# 📱 Android Application Module - Smart Personal Finance App

Ứng dụng di động Android xây dựng bằng **Kotlin**, áp dụng kiến trúc **MVVM (Model-View-ViewModel)** chuẩn của Google, tích hợp trí tuệ nhân tạo thị giác máy tính On-Device AI (CameraX, YOLOv8 TFLite, Google ML Kit OCR) và kết nối RESTful API với Backend.

---

## 🛠️ Công nghệ & Thư viện sử dụng

- **Ngôn ngữ:** Kotlin.
- **Target SDK:** Android 14 (API 34), Min SDK: Android 7.0 (API 24).
- **Kiến trúc:** MVVM (Model - View - ViewModel), Repository Pattern.
- **Android Jetpack & Bất đồng bộ:**
  - Kotlin Coroutines & LifecycleScope (`Dispatchers.IO`, `Dispatchers.Main` - Xử lý bất đồng bộ, lifecycle-aware concurrency, tự hủy luồng chống rò rỉ bộ nhớ).
  - ViewBinding (Thay thế hoàn toàn findViewById, an toàn kiểu dữ liệu).
  - ViewModel & LiveData (Quản lý trạng thái UI theo vòng đời).
  - Navigation Component (Điều hướng linh hoạt giữa các màn hình và Fragment).
- **Giao diện & Đồ họa:**
  - Modern Obsidian Dark Theme (Thiết kế tối màu cao cấp, bảo vệ mắt và tiết kiệm pin OLED).
  - Material Components 3 (Material You cards, buttons, text fields).
  - MPAndroidChart (Biểu đồ tròn PieChart cơ cấu chi tiêu, biểu đồ cột BarChart biến động thu chi).
- **Mạng & Gọi API:**
  - Retrofit 2 & OkHttp 4 (Kết nối RESTful, Multipart tải ảnh).
  - Gson Converter.
  - HttpLoggingInterceptor (Ghi log request/response khi debug).
- **Bảo mật & Xác thực:**
  - Firebase Authentication (Đăng nhập Email/Mật khẩu và Google Sign-In 1 chạm).
  - Google Play Services Auth.
- **Trí tuệ Nhân tạo Trên Thiết bị (On-Device AI):**
  - CameraX (Điều khiển camera mượt mà, phân tích khung hình thời gian thực).
  - Google ML Kit Text Recognition (OCR offline trên thiết bị, kết hợp **Google Gemini 2.5 Flash** phân tích JSON trên Backend).
  - TensorFlow Lite (TFLite) & Ultralytics YOLOv8 (Nhận diện đồ vật/sản phẩm trực tiếp qua camera).

---

## 📁 Cấu trúc thư mục mã nguồn

```text
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/personalfinance/
│   │   │   ├── activities/           # Các màn hình chính (Activity)
│   │   │   │   ├── SplashActivity.kt         # Màn hình chờ, tự động điều hướng
│   │   │   │   ├── LoginActivity.kt          # Đăng nhập (Email/Mật khẩu, Google)
│   │   │   │   ├── RegisterActivity.kt       # Đăng ký tài khoản mới
│   │   │   │   ├── MainActivity.kt           # Khung chứa Bottom Navigation chính
│   │   │   │   ├── ScanBillActivity.kt       # Quét hóa đơn (OCR + Gemini AI)
│   │   │   │   └── ScanProductActivity.kt    # Camera AI nhận diện sản phẩm YOLO
│   │   │   ├── fragments/            # Các màn hình chức năng con (Fragment)
│   │   │   │   ├── home/                     # Trang chủ tổng quan số dư, thu chi gần đây
│   │   │   │   ├── transaction/              # Lịch sử giao dịch, chi tiết ngày, thêm mới
│   │   │   │   ├── budget/                   # Quản lý ngân sách & tiến độ hạn mức
│   │   │   │   ├── category/                 # Cài đặt hạn mức & quản lý danh mục
│   │   │   │   ├── account/                  # Chi tiết ví/tài khoản
│   │   │   │   ├── recurring/                # Giao dịch định kỳ
│   │   │   │   └── profile/                  # Thông tin cá nhân & cài đặt
│   │   │   ├── adapters/             # RecyclerView Adapters hiển thị danh sách
│   │   │   ├── api/                  # Tầng kết nối mạng RESTful API
│   │   │   │   ├── ApiClient.kt              # Cấu hình Retrofit, tự động chuyển đổi URL
│   │   │   │   ├── ApiService.kt             # Định nghĩa các endpoint Retrofit
│   │   │   │   └── AuthInterceptor.kt        # Tự động đính kèm Firebase Bearer Token
│   │   │   ├── firebase/             # Helper xử lý Firebase Auth & Google Sign-In
│   │   │   ├── ml/yolo/              # Engine nhận diện YOLOv8 TFLite & Custom View Overlay
│   │   │   │   ├── BoundingBoxOverlay.kt     # Vẽ khung nhận diện vật thể thời gian thực
│   │   │   │   └── YoloDetector.kt           # Nạp model TFLite và xử lý bounding box
│   │   │   ├── models/               # Data models, DTOs và Domain Entities
│   │   │   ├── repositories/         # Repository pattern xử lý gọi API & cache dữ liệu
│   │   │   ├── utils/                # Tiện ích định dạng tiền tệ, ngày tháng, SharedPreferences
│   │   │   └── viewmodels/           # ViewModels xử lý logic và cung cấp LiveData cho UI
│   │   ├── res/                      # Tài nguyên giao diện XML, hình ảnh, màu sắc
│   │   └── AndroidManifest.xml       # Khai báo quyền Camera, Internet và các Activity
│   ├── google-services.json          # Cấu hình dự án Firebase
│   └── build.gradle                  # Cấu hình dependencies và Android Gradle Plugin
├── gradle.properties
└── build.gradle
```

---

## 🌟 Các chức năng chính & Hướng dẫn sử dụng

### 1. Đăng ký & Đăng nhập linh hoạt
- **Đăng nhập Google:** 1 chạm với tài khoản Google có sẵn trên thiết bị.
- **Đăng ký tài khoản Email:** Nhập Họ tên, Email và Mật khẩu để tạo tài khoản mới. Hệ thống sẽ tự động đồng bộ tài khoản giữa Firebase và cơ sở dữ liệu MySQL của Backend.
- **Tính năng độc quyền cho Tester/Developer:** Tại màn hình Đăng nhập, **nhấn giữ vào Logo app khoảng 2 giây** để mở hộp thoại cấu hình nhanh địa chỉ IP Server Backend.

### 2. Trang chủ & Quản lý Tài khoản (Dashboard)
- Hiển thị thẻ tổng quan số dư tài chính cá nhân.
- Thống kê nhanh tổng thu và tổng chi trong tháng.
- Xem danh sách các ví/tài khoản (Tiền mặt, Ngân hàng, Ví điện tử).
- Danh sách các giao dịch phát sinh gần nhất.

### 3. Ghi chép Thu Chi Toàn diện
- Thêm giao dịch thủ công: chọn loại (Thu / Chi), số tiền, danh mục, tài khoản nguồn, ngày giờ và ghi chú.
- Đính kèm hình ảnh hóa đơn hoặc hình chụp mặt hàng.
- Lọc giao dịch theo từng ngày cụ thể hoặc theo tháng.

### 4. Quét Hóa đơn Thông minh (OCR + Gemini AI)
- Mở camera chụp hóa đơn hoặc phiếu thu/chi.
- Google ML Kit phân tích tức thì các khối văn bản (offline), gửi text thô lên Backend.
- **Google Gemini 2.5 Flash LLM** phân tích ngữ cảnh và bóc tách chính xác:
  - **Tổng tiền thanh toán**.
  - **Ngày phát sinh hóa đơn**.
  - **Tên cửa hàng / đơn vị cung cấp**.
  - **Gợi ý danh mục chi tiêu** (Thực phẩm, Điện, Nước...).
- Tự động điền trước các thông tin này vào form thêm giao dịch giúp tiết kiệm tối đa thời gian nhập liệu.

### 5. Nhận diện Sản phẩm Mua sắm (YOLO AI Camera)
- Chĩa camera vào đồ vật, đồ ăn, nước uống (cốc cà phê, trà sữa, bánh mì, đồ điện tử, quần áo, mũ bảo hiểm...).
- Mô hình YOLOv8 On-Device vẽ khung nhận diện màu sắc theo thời gian thực (Bounding Box).
- Khi người dùng chụp, thông tin vật thể được gửi lên Server để mô hình Random Forest gợi ý ngay danh mục chi tiêu chuẩn xác nhất.

### 6. Quản lý Ngân sách & Cảnh báo Chi tiêu
- Đặt hạn mức chi tiêu cho từng danh mục trong tháng (Ăn uống: 3.000.000đ, Mua sắm: 1.500.000đ...).
- Thanh tiến độ trực quan hiển thị số tiền đã chi và phần trăm còn lại.
- Tự động đổi màu cảnh báo (Xanh lá -> Vàng cam -> Đỏ) khi chi tiêu chạm ngưỡng hoặc vượt hạn mức.

### 7. Báo cáo & Phân tích Đa chiều
- Biểu đồ tròn (Pie Chart) thể hiện tỷ trọng các danh mục chi tiêu.
- Thống kê so sánh xu hướng thu chi theo từng tháng và theo năm.

### 8. Giao dịch Định kỳ & Tiện ích Demo Tức thì
- Thiết lập các giao dịch lặp lại theo chu kỳ: Ngày, Tuần, Tháng, Năm (tiền nhà, hóa đơn điện nước...).
- Hỗ trợ endpoint đặc biệt `POST /api/recurring-transactions/trigger` gửi yêu cầu tới Backend để quét và phát sinh giao dịch định kỳ tức thì, hỗ trợ demo trực quan trước hội đồng chấm thi mà không phải chờ sang ngày hôm sau.

### 9. Tinh giản Dung lượng APK & Tối ưu Luồng Lưu Giao dịch
- **Lean APK Footprint:** Loại bỏ hoàn toàn Facebook SDK thừa, giúp giảm ~3–5 MB dung lượng file APK cài đặt và tối ưu thời gian khởi động ứng dụng.
- **Xác thực tập trung an toàn:** Chuẩn hóa đăng nhập qua Google Sign-In 1 chạm và Email/Password của Firebase Authentication.
- **Direct Commit (Lưu trực tiếp):** Lược bỏ các request mạng âm thầm gửi log phản hồi sau nhận diện AI (`submitScanFeedback`, `submitProductFeedback`), chuyển sang cơ chế lưu 1-step tức thì, tiết kiệm dữ liệu di động và pin thiết bị.
- **Dọn dẹp giao diện Cá nhân (Profile):** Loại bỏ hoàn toàn hiển thị phiên bản tĩnh và các View ẩn không dùng trong layout XML.

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy

### 1. Mở dự án trong Android Studio
1. Khởi động **Android Studio** (bản Iguana / Jellyfish / Koala / Ladybug / Meerkat trở lên).
2. Chọn **Open** và dẫn tới thư mục `android-app/`.
3. Chờ Gradle đồng bộ dependencies (lần đầu có thể mất vài phút do tải TFLite và Firebase).

### 2. Cấu hình kết nối tới Backend

#### Chạy trên Máy ảo Android (Android Emulator):
- Mặc định ứng dụng đã được cấu hình trỏ tới `http://10.0.2.2:8080/`.
- `10.0.2.2` là địa chỉ chuẩn của Android Emulator để tự động chuyển tiếp về máy tính host đang chạy Backend Docker.
- **Bạn chỉ cần bấm nút Run (▶️) là app kết nối được ngay!**
- ⚠️ **Lưu ý về kích thước trang bộ nhớ (Page Size):** Khi tạo AVD, nên chọn các bản System Image chuẩn (Android 14 API 34 hoặc Android 15/16 với kiến trúc bộ nhớ chuẩn 4KB, tránh các bản `ps16k` thử nghiệm) để thư viện C++ native `libtensorflowlite_jni.so` của YOLO TFLite nạp thành công 100%.

#### Chạy trên Điện thoại thật:
- **Cách 1 (Cùng mạng Wi-Fi):** Mở app trên điện thoại -> Tại màn hình Login, **nhấn giữ vào Logo app 2 giây** -> Nhập địa chỉ IP Wi-Fi của máy tính (ví dụ: `192.168.1.125:8080`) -> Bấm **Lưu**.
- **Cách 2 (Cắm cáp USB):** Bật chế độ USB Debugging, cắm cáp và gõ lệnh trong terminal:
  ```bash
  adb reverse tcp:8080 tcp:8080
  ```
  Lúc này điện thoại có thể truy cập Backend như máy tính nội bộ.

### 3. Build file APK từ dòng lệnh (Command Line)
```powershell
cd android-app
.\gradlew assembleDebug
```
File APK cài đặt sẽ được tạo tại:
`android-app/app/build/outputs/apk/debug/app-debug.apk`

---

## 👨‍🎓 Thông tin Đề tài Đồ án Tốt nghiệp
- **Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android
- **Sinh viên thực hiện:** Nguyễn Duy Khánh — Mã SV: 2255010114 — Lớp: 22CN4
- **Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng
- **Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội
