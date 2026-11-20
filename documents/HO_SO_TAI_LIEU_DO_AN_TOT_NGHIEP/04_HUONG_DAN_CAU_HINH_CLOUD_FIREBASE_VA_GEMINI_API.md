# 🛠️ HƯỚNG DẪN TỪNG BƯỚC CẤU HÌNH FIREBASE, ĐĂNG NHẬP GOOGLE & GEMINI API
**Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 📌 MỤC LỤC
1. [Phần 1: Hướng dẫn Cấu hình Firebase từ A đến Z](#phần-1-hướng-dẫn-cấu-hình-firebase-từ-a-đến-z)
   - [1.1 Tạo Project trên Firebase Console](#11-tạo-project-mới-trên-firebase-console)
   - [1.2 Lấy mã SHA-1 Fingerprint của máy tính](#12-lấy-mã-vân-tay-sha-1-fingerprint-của-máy-tính)
   - [1.3 Thêm ứng dụng Android vào Firebase](#13-thêm-ứng-dụng-android-vào-firebase)
   - [1.4 Tải và đặt file cấu hình `google-services.json`](#14-tải-và-đặt-file-cấu-hình-google-servicesjson)
   - [1.5 Tạo và tải file khoá Backend `firebase-service-account.json`](#15-tạo-và-tải-file-khoá-backend-firebase-service-accountjson)
2. [Phần 2: Hướng dẫn Chi tiết Tính năng Đăng nhập Google](#phần-2-hướng-dẫn-chi-tiết-tính-năng-đăng-nhập-google)
   - [2.1 Bật phương thức xác thực Google trên Firebase](#21-bật-phương-thức-xác-thực-google-trên-firebase)
   - [2.2 Cơ chế hoạt động của Google Sign-In trong mã nguồn Android](#22-cơ-chế-hoạt-động-của-google-sign-in-trong-mã-nguồn-android)
   - [2.3 Các mã lỗi phổ biến và cách xử lý triệt để](#23-các-mã-lỗi-phổ-biến-và-cách-xử-lý-triệt-để)
   - [2.4 Cơ chế dự phòng an toàn: Chế độ Đăng nhập Demo](#24-cơ-chế-dự-phòng-an-toàn-chế-độ-đăng-nhập-demo)
3. [Phần 3: Hướng dẫn Đăng ký & Tích hợp Gemini API bóc tách Hóa đơn](#phần-3-hướng-dẫn-đăng-ký--tích-hợp-gemini-api-bóc-tách-hóa-đơn)
   - [3.1 Đăng ký Gemini API Key miễn phí tại Google AI Studio](#31-đăng-ký-gemini-api-key-miễn-phí-tại-google-ai-studio)
   - [3.2 Cấu hình API Key vào dự án Backend Spring Boot](#32-cấu-hình-api-key-vào-dự-án-backend-spring-boot)
   - [3.3 Kỹ thuật Prompt Engineering bóc tách 4 trường thông tin](#33-kỹ-thuật-prompt-engineering-bóc-tách-4-trường-thông-tin)
   - [3.4 Cơ chế Fallback Regex tự động khi mất mạng hoặc không có Key](#34-cơ-chế-fallback-regex-tự-động-khi-mất-mạng-hoặc-không-có-key)

---

# PHẦN 1: HƯỚNG DẪN CẤU HÌNH FIREBASE TỪ A ĐẾN Z

Firebase đóng vai trò là hệ thống xác thực tập trung (Identity Provider) quản lý tài khoản người dùng, đồng thời kết nối bảo mật giữa Android App và Backend Spring Boot.

### 1.1 Tạo Project mới trên Firebase Console
1. Mở trình duyệt web, truy cập vào: [https://console.firebase.google.com/](https://console.firebase.google.com/).
2. Đăng nhập bằng tài khoản Google cá nhân.
3. Nhấp vào nút **"Add project"** (hoặc *"Thêm dự án"*).
4. Đặt tên dự án (ví dụ: `personal-finance-app`) $\rightarrow$ Bấm **Continue**.
5. Ở bước *"Google Analytics"*: Có thể bật hoặc tắt (khuyến nghị tắt cho đơn giản) $\rightarrow$ Bấm **Create project**.
6. Chờ vài giây để Google khởi tạo hệ thống $\rightarrow$ Bấm **Continue** để vào màn hình quản trị dự án.

---

### 1.2 Lấy mã vân tay SHA-1 Fingerprint của máy tính
> [!IMPORTANT]
> Đây là bước **quan trọng nhất** để Google cho phép điện thoại của bạn đăng nhập bằng Gmail. Nếu thiếu SHA-1, khi bấm đăng nhập Google sẽ báo lỗi `Code 10` hoặc `12500`.

Bạn có thể lấy mã SHA-1 bằng 1 trong 2 cách sau:

#### Cách 1: Dùng tab Gradle trong Android Studio (Dễ nhất)
1. Mở dự án `android-app` bằng **Android Studio**.
2. Nhìn sang cạnh phải màn hình, nhấp vào tab **Gradle**.
3. Mở rộng cây thư mục: `android-app` $\rightarrow$ `app` $\rightarrow$ `Tasks` $\rightarrow$ `android` $\rightarrow$ Nhấp đúp vào **`signingReport`**.
4. Cửa sổ `Run / Build` bên dưới sẽ in ra kết quả. Bạn tìm dòng:
   ```text
   Variant: debug
   Config: debug
   Store: C:\Users\<Tên_User>\.android\debug.keystore
   Alias: AndroidDebugKey
   SHA1: 2B:6A:8B:B3:6C:29:C6:DD:8B:0C:49:25:6E:11:50:C9:CD:32:A0:5B
   ```
5. Bôi đen và sao chép (Copy) toàn bộ chuỗi **SHA1** này.

#### Cách 2: Dùng dòng lệnh Command Prompt / PowerShell
Mở PowerShell và gõ lệnh sau:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
Tìm dòng có chữ `SHA1:` và copy mã băm 20 cặp ký tự.

---

### 1.3 Thêm ứng dụng Android vào Firebase
1. Trên màn hình chính của Firebase Console, nhấp vào biểu tượng con robot **Android** để thêm app.
2. Điền các trường thông tin:
   * **Android package name (Bắt buộc phải điền chính xác):**
     ```text
     com.example.personalfinance
     ```
   * **App nickname (Biệt danh):** `Finance App Android` (tùy ý).
   * **Debug signing certificate SHA-1 (Dán mã SHA-1 vừa lấy ở bước 1.2 vào đây):**
     ```text
     2B:6A:8B:B3:6C:29:C6:DD:8B:0C:49:25:6E:11:50:C9:CD:32:A0:5B
     ```
3. Nhấp nút **"Register app"** (Đăng ký ứng dụng).

---

### 1.4 Tải và đặt file cấu hình `google-services.json`
1. Sau khi bấm Register, Firebase sẽ hiển thị nút tải file **Download `google-services.json`**.
2. Nhấp nút để tải file về máy.
3. Sao chép (Copy) file `google-services.json` này và dán đè vào đúng vị trí sau trong dự án:
   ```text
   DATN/android-app/app/google-services.json
   ```
4. Trên Firebase Console, bấm **Next** $\rightarrow$ **Next** $\rightarrow$ **Continue to console**.

---

### 1.5 Tạo và tải file khoá Backend `firebase-service-account.json`
Để Backend Spring Boot có quyền kiểm tra tính hợp lệ của token do Firebase cấp, bạn cần nạp Service Account Key:
1. Tại Firebase Console, nhấp vào biểu tượng bánh răng **Cài đặt (Project settings)** ở góc trên bên trái.
2. Chuyển sang tab **Service accounts** (Tài khoản dịch vụ).
3. Đảm bảo ngôn ngữ chọn là **Java**.
4. Nhấp nút **"Generate new private key"** (Tạo khóa riêng tư mới) $\rightarrow$ Một hộp thoại xác nhận hiện ra, bấm **Generate key**.
5. Một file `.json` dài sẽ được tải về máy tính (ví dụ: `personal-finance-app-firebase-adminsdk-xxxx.json`).
6. **Đổi tên file đó thành:**
   ```text
   firebase-service-account.json
   ```
7. Sao chép file này vào đúng thư mục tài nguyên của Backend Spring Boot:
   ```text
   DATN/backend/src/main/resources/firebase-service-account.json
   ```

---

# PHẦN 2: HƯỚNG DẪN CHI TIẾT TÍNH NĂNG ĐĂNG NHẬP GOOGLE

### 2.1 Bật phương thức xác thực Google trên Firebase
1. Vào Firebase Console $\rightarrow$ Menu bên trái chọn **Build** $\rightarrow$ **Authentication**.
2. Nếu chưa từng bật, bấm **Get started**.
3. Chọn tab **Sign-in method** $\rightarrow$ Chọn **Google**:
   * Bật công tắc **Enable** (Kích hoạt).
   * Tại mục **Project support email**: Nhấp chọn email Gmail của bạn.
   * Nhấp nút **Save** (Lưu).
4. Tương tự, nhấp chọn **Email/Password** $\rightarrow$ Bật **Enable** $\rightarrow$ Nhấp **Save**.

---

### 2.2 Cơ chế hoạt động của Google Sign-In trong mã nguồn Android
Tính năng đăng nhập Google được lập trình qua lớp `FirebaseAuthHelper.kt` và `LoginActivity.kt` với 4 bước tự động:

1. **Khởi tạo Google Client với Web Client ID:**
   ```kotlin
   // Trong LoginActivity.kt
   authHelper.initGoogleSignIn(this, getString(R.string.default_web_client_id))
   ```
   *Chuỗi `default_web_client_id` được plugin Google Services tự động bóc tách từ file `google-services.json` (ứng với client có `client_type: 3`).*

2. **Người dùng bấm nút "Đăng nhập Google":**
   App khởi chạy Intent của Google Play Services để hiển thị màn hình chọn tài khoản Gmail trên máy:
   ```kotlin
   startActivityForResult(authHelper.getGoogleSignInIntent(), RC_GOOGLE_SIGN_IN)
   ```

3. **Lấy Token và chuyển đổi sang Firebase Credential:**
   Sau khi người dùng chọn tài khoản, Google trả về mã `idToken`:
   ```kotlin
   val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
   val credential = GoogleAuthProvider.getCredential(account.idToken, null)
   mAuth.signInWithCredential(credential).addOnCompleteListener { ... }
   ```

4. **Đồng bộ thông tin sang Backend Spring Boot:**
   Firebase trả về đối tượng `FirebaseUser` (gồm UID, Email, Họ tên, Ảnh đại diện). App gửi dữ liệu qua API:
   ```http
   POST /api/auth/firebase-login
   Content-Type: application/json

   {
     "firebaseUid": "v3c8x91...",
     "email": "nguyenduykhanh@gmail.com",
     "fullName": "Nguyễn Duy Khánh",
     "avatarUrl": "https://lh3.googleusercontent.com/..."
   }
   ```
   Backend Spring Boot kiểm tra người dùng trong bảng `users` MySQL. Nếu là tài khoản mới, Backend tự động tạo người dùng, khởi tạo sẵn các ví mặc định và danh mục chi tiêu, sau đó trả về Token phiên làm việc.

---

### 2.3 Các mã lỗi phổ biến và cách xử lý triệt để

| Mã lỗi (Status Code) | Nguyên nhân thực tế | Cách khắc phục 100% thành công |
| :---: | :--- | :--- |
| **Mã lỗi 10**<br>`DEVELOPER_ERROR` | • Sai mã SHA-1 fingerprint giữa máy tính đang chạy và Firebase Console.<br>• Hoặc Package Name không khớp `com.example.personalfinance`. | Chạy lệnh `keytool` hoặc `signingReport` trên máy hiện tại, lấy mã SHA-1 mới dán vào Firebase Console $\rightarrow$ Tải lại file `google-services.json` đặt vào `android-app/app/`. |
| **Mã lỗi 12500**<br>`SIGN_IN_FAILED` | Chưa bật Provider Google trong tab Authentication của Firebase Console hoặc chưa chọn Support Email. | Vào Firebase $\rightarrow$ Authentication $\rightarrow$ Sign-in method $\rightarrow$ Bật Google và chọn email hỗ trợ. |
| **Mã lỗi 12501**<br>`SIGN_IN_CANCELLED` | Người dùng chủ động bấm nút Back hoặc quẹt hủy cửa sổ chọn tài khoản Google. | Bình thường, không phải lỗi code. App chỉ cần ẩn thanh loading và cho phép bấm lại. |
| **Mã lỗi 7**<br>`NETWORK_ERROR` | Mất kết nối Internet hoặc thiết bị giả lập (Emulator) chưa cài đặt **Google Play Services**. | Kết nối Wifi cho máy/máy ảo, hoặc chọn máy ảo Android có icon Google Play Store. |

---

### 2.4 Cơ chế dự phòng an toàn: Chế độ Đăng nhập Demo
> [!TIP]
> Trong ngày bảo vệ Đồ án tốt nghiệp trước Thầy cô, nếu Wifi trường chập chờn hoặc máy ảo bị lag Google Services, bạn hãy dùng **Nút "Đăng nhập Demo" (Quick Demo Login)** ngay trên giao diện:
> * Nhấp 1 chạm vào nút **"Đăng nhập Demo"**.
> * Ứng dụng kích hoạt phiên làm việc Demo ngay lập tức, nạp đầy đủ dữ liệu mẫu (ví tiền 15.000.000 đ, danh mục ăn uống, mua sắm, các biểu đồ thống kê trực quan) để bạn demo mượt mà mà không lo rủi ro mạng!

---

# PHẦN 3: HƯỚNG DẪN ĐĂNG KÝ & TÍCH HỢP GEMINI API BÓC TÁCH HÓA ĐƠN

Gemini AI đóng vai trò như một trợ lý thông minh: đọc hiểu ngữ nghĩa của văn bản hóa đơn do Camera quét được để tự động trích xuất các thông tin nghiệp vụ tài chính.

### 3.1 Đăng ký Gemini API Key miễn phí tại Google AI Studio
1. Mở trình duyệt web, truy cập: [https://aistudio.google.com/](https://aistudio.google.com/).
2. Đăng nhập bằng tài khoản Gmail của bạn.
3. Ở góc trên bên trái, nhấp vào nút **"Get API key"**.
4. Nhấp vào nút **"Create API key"** (Tạo khóa API).
5. Chọn dự án Google Cloud có sẵn (hoặc bấm *"Create API key in new project"* để Google tự tạo dự án mới miễn phí).
6. Một chuỗi ký tự API Key cá nhân sẽ xuất hiện trên màn hình (định dạng `AIzaSy...`).
7. Nhấp biểu tượng **Copy** để lưu lại chuỗi khóa này.
> [!NOTE]
> Gói miễn phí của Google AI Studio cho phép gọi tới **15 yêu cầu / phút** và **1.500 yêu cầu / ngày**, hoàn toàn miễn phí và không yêu cầu thẻ ngân hàng.
> [!WARNING]
> Tuyệt đối **không đưa API Key thật** vào các tệp mã nguồn hoặc tài liệu công khai (GitHub/GitLab) để tránh bị lộ hoặc thu hồi hạn ngạch.

---

### 3.2 Cấu hình API Key vào dự án Backend Spring Boot
Hệ thống hỗ trợ 2 phương án cấu hình (khuyến nghị **Cách 1** để bảo mật tuyệt đối):

#### Cách 1: Thiết lập qua biến môi trường hệ thống (Khuyên dùng - Chuẩn bảo mật)
Backend Spring Boot được cấu hình ưu tiên tự động đọc từ biến môi trường `GEMINI_API_KEY`:

- **Trên Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="YOUR_GEMINI_API_KEY_HERE"
```

- **Trên Linux / macOS (Bash):**
```bash
export GEMINI_API_KEY="YOUR_GEMINI_API_KEY_HERE"
```

- **Trong Docker Compose (`docker-compose.yml`):**
```yaml
services:
  backend:
    environment:
      - GEMINI_API_KEY=${GEMINI_API_KEY}
```

#### Cách 2: Điền vào file cấu hình cục bộ khi chạy thử nghiệm
Mở file [backend/src/main/resources/ai-settings.properties](file:///d:/Project/KHMT/DATN/backend/src/main/resources/ai-settings.properties):
```properties
# Điền API Key của bạn thay thế cho placeholder (hoặc giữ nguyên nếu đã đặt biến môi trường):
gemini.api.key=${GEMINI_API_KEY:YOUR_GEMINI_API_KEY_HERE}

# Endpoint model gemini-2.5-flash (tốc độ cao, tối ưu chi phí):
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

---

### 3.3 Kỹ thuật Prompt Engineering bóc tách 4 trường thông tin
Trong mã nguồn [backend/src/main/java/.../GeminiService.java](file:///d:/Project/KHMT/DATN/backend/src/main/java/com/example/financebackend/service/ai/GeminiService.java), hệ thống gửi kèm văn bản OCR cùng một đoạn Prompt chỉ dẫn nghiêm ngặt:

```text
Phân tích đoạn văn bản OCR từ hóa đơn sau và trích xuất các thông tin dưới dạng JSON chuẩn:
1. merchant: Tên cửa hàng/thương hiệu ngắn gọn (Ví dụ: WinMart, Highlands Coffee, Grab).
2. amount: Tổng số tiền thanh toán thực tế cuối cùng của khách hàng (kiểu số nguyên VND).
3. date: Ngày giao dịch theo định dạng YYYY-MM-DD (Ví dụ: 2026-10-18).
4. category: Gợi ý danh mục phù hợp từ danh sách: [food, transport, shopping, bills, entertainment, health, other].
```

**Mẫu JSON cấu trúc phản hồi từ Gemini:**
```json
{
  "merchant": "Highlands Coffee",
  "amount": 89000,
  "date": "2026-10-18",
  "category": "food"
}
```
Backend nhận chuỗi JSON này, chuyển đổi thành đối tượng `GeminiReceiptResponse` và tự động gắn vào form giao dịch trên điện thoại.

---

### 3.4 Cơ chế Fallback Regex tự động khi mất mạng hoặc không có Key
Một điểm cộng kỹ thuật rất lớn trong đề tài là **tính sẵn sàng cao (High Availability)**:
* Trong lớp `AiScanController.java`:
  ```java
  try {
      // Ưu tiên gọi Gemini AI thông minh
      GeminiReceiptResponse geminiResult = geminiService.analyzeReceipt(rawText);
      ...
  } catch (Exception e) {
      // TỰ ĐỘNG CHUYỂN SANG BỘ BÓC TÁCH CỤC BỘ BẰNG BIỂU THỨC CHÍNH QUY (FALLBACK REGEX)
      logger.warn("Gemini API unavailable. Falling back to local ReceiptParser heuristic regex.");
      ReceiptData parsed = receiptParser.parse(rawText);
      ...
  }
  ```
* Nếu Gemini API bị lỗi, hết hạn ngạch hoặc máy tính mất mạng, hệ thống **không bao giờ bị dừng hay crash**: Bộ tách chữ `ReceiptParser.java` nội bộ sẽ tự động tìm kiếm các mẫu regex số tiền (như `Tổng tiền`, `Thanh toán`, `VND`), ngày tháng và từ khóa để điền vào form với độ chính xác cao.
