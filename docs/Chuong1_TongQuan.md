# CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI

## 1.1. Đặt vấn đề

Trong bối cảnh nền kinh tế số phát triển mạnh mẽ tại Việt Nam, quản lý tài chính cá nhân trở thành nhu cầu thiết yếu của mọi người, đặc biệt là giới trẻ và người đi làm. Tính đến cuối năm 2025, tỷ lệ người trưởng thành có tài khoản ngân hàng đạt 88,96%, số lượng tài khoản thanh toán cá nhân vượt mốc 232 triệu. Xu hướng thanh toán không dùng tiền mặt và sử dụng dịch vụ tài chính số ngày càng phổ biến, kéo theo nhu cầu theo dõi, kiểm soát dòng tiền một cách hiệu quả.

Hiện nay, các ứng dụng quản lý tài chính cá nhân trên di động (Money Lover, MISA MoneyKeeper, Wallet…) đã phần nào đáp ứng được nhu cầu ghi chép thu chi cơ bản. Tuy nhiên, hầu hết các ứng dụng này vẫn tồn tại những hạn chế:

- **Nhập liệu thủ công:** Người dùng phải tự gõ từng giao dịch, dễ quên và gây phiền toái, dẫn đến bỏ dở thói quen quản lý chi tiêu.
- **Phân loại danh mục cứng nhắc:** Danh mục chi tiêu được gán cố định bởi người dùng, không có khả năng tự động gợi ý dựa trên ngữ cảnh giao dịch thực tế.
- **Chưa tận dụng Trí tuệ Nhân tạo (AI):** Thiếu khả năng nhận diện sản phẩm mua sắm qua camera hoặc tự động trích xuất thông tin từ hóa đơn.

Xuất phát từ thực trạng trên, đề tài **"Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android"** được đề xuất nhằm giải quyết các hạn chế nêu trên, hướng tới việc tự động hóa quá trình ghi nhận và phân loại giao dịch chi tiêu bằng công nghệ AI hiện đại.

---

## 1.2. Mục tiêu đề tài

**Mục tiêu tổng quát:**

Xây dựng một ứng dụng quản lý tài chính cá nhân trên nền tảng Android, tích hợp các kỹ thuật Trí tuệ Nhân tạo (AI) và Thị giác Máy tính (Computer Vision) nhằm tự động hóa việc ghi nhận, phân loại giao dịch chi tiêu, giúp người dùng quản lý dòng tiền hiệu quả hơn.

**Mục tiêu cụ thể:**

1. Xây dựng hệ thống quản lý thu chi toàn diện với khả năng theo dõi dòng tiền, quản lý nhiều tài khoản ví (Tiền mặt, Ngân hàng, Ví điện tử), thiết lập ngân sách và cảnh báo vượt hạn mức.
2. Tích hợp mô hình YOLOv8 chạy trực tiếp trên thiết bị di động (On-Device) để nhận diện sản phẩm mua sắm qua camera trong thời gian thực.
3. Xây dựng Pipeline AI 2 giai đoạn kết hợp YOLOv8 (phát hiện vật thể) và Random Forest (phân loại danh mục tài chính) nhằm tự động gợi ý danh mục chi tiêu.
4. Tích hợp công nghệ OCR (Google ML Kit) và Mô hình Ngôn ngữ Lớn (Google Gemini AI) để tự động quét và trích xuất thông tin từ hóa đơn giấy.
5. Xây dựng module báo cáo trực quan với biểu đồ thống kê đa chiều giúp người dùng nắm bắt tổng quan tình hình tài chính.

---

## 1.3. Phạm vi đề tài

Ứng dụng được xây dựng với 5 module chức năng chính:

| STT | Module | Mô tả |
|:---:|:---|:---|
| 1 | **Quản lý thu chi** | Ghi nhận giao dịch thu/chi, quản lý tài khoản ví, danh mục, giao dịch định kỳ |
| 2 | **Quét hóa đơn thông minh** | Sử dụng OCR + Gemini AI để trích xuất thông tin hóa đơn tự động |
| 3 | **Nhận diện sản phẩm Camera AI** | Nhận diện 18 loại sản phẩm qua camera và gợi ý danh mục chi tiêu |
| 4 | **Ngân sách & Cảnh báo** | Thiết lập hạn mức chi tiêu theo danh mục/tháng, cảnh báo vượt ngân sách |
| 5 | **Báo cáo trực quan** | Biểu đồ PieChart, BarChart thống kê cơ cấu thu chi theo tháng/năm |

**Đối tượng sử dụng:** Người dùng cá nhân có nhu cầu quản lý chi tiêu hàng ngày trên thiết bị Android.

**Giới hạn đề tài:**
- Ứng dụng phát triển trên nền tảng Android (SDK 34), chưa hỗ trợ iOS.
- Mô hình nhận diện sản phẩm tập trung vào 18 lớp mặt hàng phổ biến trong sinh hoạt hàng ngày.
- Ứng dụng yêu cầu kết nối mạng để đồng bộ dữ liệu với Backend và sử dụng dịch vụ Gemini AI.

---

## 1.4. Khảo sát các ứng dụng quản lý tài chính cá nhân hiện có

### 1.4.1. Money Lover

**Money Lover** là ứng dụng quản lý tài chính cá nhân phổ biến nhất tại Việt Nam, được phát triển bởi Finsify (Việt Nam).

**Tính năng chính:**
- Ghi chép thu chi nhanh chóng, hỗ trợ kết nối tài khoản ngân hàng để tự động cập nhật biến động số dư.
- Lập ngân sách, nhắc nhở hóa đơn định kỳ, quản lý khoản vay/nợ.
- Báo cáo chi tiêu bằng biểu đồ trực quan.
- Giao diện thân thiện, phù hợp cả người mới bắt đầu.

**Hạn chế:**
- Không có tính năng nhận diện sản phẩm qua camera.
- Không tích hợp AI để tự động phân loại giao dịch từ hình ảnh.
- Tính năng quét hóa đơn bị giới hạn hoặc yêu cầu nâng cấp gói trả phí.

### 1.4.2. MISA MoneyKeeper (Sổ Thu Chi MISA)

**MISA MoneyKeeper** được phát triển bởi công ty MISA – đơn vị có uy tín trong lĩnh vực phần mềm kế toán tại Việt Nam.

**Tính năng chính:**
- Ghi chép thu chi chi tiết, độ chính xác cao.
- Quản lý sổ nợ, theo dõi các khoản vay rõ ràng.
- Danh mục thu chi được thiết kế phù hợp thói quen người Việt.
- Báo cáo tài chính chuyên sâu.

**Hạn chế:**
- Hoàn toàn nhập liệu thủ công, không hỗ trợ quét hóa đơn tự động.
- Không tích hợp AI hay Computer Vision.
- Giao diện ít hiện đại hơn so với các đối thủ cạnh tranh.

### 1.4.3. Wallet by BudgetBakers

**Wallet** là ứng dụng quản lý tài chính quốc tế, có giao diện hiện đại và hỗ trợ đa nền tảng.

**Tính năng chính:**
- Kết nối ngân hàng tự động (hỗ trợ hơn 5.000 ngân hàng toàn cầu).
- Lập kế hoạch tài chính, dự báo dòng tiền.
- Đồng bộ dữ liệu đa thiết bị.

**Hạn chế:**
- Hỗ trợ ngân hàng Việt Nam còn hạn chế.
- Không có tính năng AI nhận diện sản phẩm hay quét hóa đơn thông minh.
- Nhiều tính năng yêu cầu gói Premium trả phí.

### 1.4.4. Bảng so sánh tổng hợp

| Tiêu chí | Money Lover | MISA MoneyKeeper | Wallet | **Đề tài (Smart Finance)** |
|:---|:---:|:---:|:---:|:---:|
| Ghi chép thu chi | ✅ | ✅ | ✅ | ✅ |
| Quản lý ngân sách | ✅ | ✅ | ✅ | ✅ |
| Biểu đồ báo cáo | ✅ | ✅ | ✅ | ✅ |
| Giao dịch định kỳ | ✅ | ✅ | ✅ | ✅ |
| Kết nối ngân hàng | ✅ | ❌ | ✅ | ❌ |
| Quét hóa đơn OCR | ⚠️ (giới hạn) | ❌ | ❌ | ✅ (OCR + Gemini AI) |
| Nhận diện sản phẩm Camera AI | ❌ | ❌ | ❌ | ✅ (YOLOv8 On-Device) |
| AI phân loại danh mục tự động | ❌ | ❌ | ❌ | ✅ (Random Forest ONNX) |
| Xử lý AI trên thiết bị (Edge AI) | ❌ | ❌ | ❌ | ✅ (TFLite + CameraX) |

**Nhận xét:** Điểm khác biệt cốt lõi của đề tài so với các ứng dụng hiện có trên thị trường là khả năng tích hợp Thị giác Máy tính (Computer Vision) và Pipeline AI 2 giai đoạn để tự động nhận diện sản phẩm mua sắm qua camera và gợi ý phân loại danh mục chi tiêu – tính năng chưa có ứng dụng quản lý tài chính cá nhân nào tại Việt Nam triển khai.

---

## 1.5. Nghiên cứu các công nghệ liên quan

### 1.5.1. Nền tảng Android và ngôn ngữ Kotlin

**Android** là hệ điều hành di động phổ biến nhất thế giới với hơn 70% thị phần toàn cầu. Android SDK (Software Development Kit) cung cấp bộ công cụ đầy đủ cho phép lập trình viên xây dựng ứng dụng di động với giao diện phong phú và truy cập phần cứng thiết bị (camera, cảm biến, GPS…).

**Kotlin** là ngôn ngữ lập trình chính thức cho phát triển Android (được Google công nhận từ năm 2019). So với Java truyền thống, Kotlin có cú pháp ngắn gọn hơn, hỗ trợ null-safety, coroutines (lập trình bất đồng bộ) và tương thích hoàn toàn với hệ sinh thái Java.

Đề tài sử dụng **Android SDK 34** với ngôn ngữ **Kotlin**, kết hợp các thành phần kiến trúc hiện đại:
- **MVVM (Model–View–ViewModel):** Tách biệt rõ ràng lớp giao diện, lớp logic và lớp dữ liệu, dễ bảo trì và kiểm thử.
- **ViewBinding:** Truy cập thành phần giao diện an toàn kiểu dữ liệu.
- **LiveData:** Theo dõi dữ liệu có vòng đời (lifecycle-aware), tự động cập nhật giao diện.
- **Navigation Component:** Quản lý điều hướng giữa các màn hình (Fragment).
- **Coroutines:** Xử lý tác vụ bất đồng bộ (gọi API, truy vấn CSDL) mà không chặn luồng chính (UI Thread).

### 1.5.2. Spring Boot và kiến trúc RESTful API

**Spring Boot** là framework phát triển ứng dụng Java phổ biến nhất hiện nay, cung cấp khả năng cấu hình tự động (auto-configuration) giúp rút ngắn thời gian dựng ứng dụng Backend. Spring Boot tích hợp sẵn máy chủ nhúng (Embedded Tomcat), hỗ trợ đầy đủ các module cần thiết: Spring MVC (xây dựng REST API), Spring Security (bảo mật), Spring Data JPA (truy xuất cơ sở dữ liệu).

**RESTful API (Representational State Transfer)** là kiến trúc phổ biến cho giao tiếp giữa ứng dụng di động (client) và máy chủ (server). Các đặc điểm chính:
- Sử dụng các phương thức HTTP chuẩn: GET, POST, PUT, DELETE.
- Dữ liệu trao đổi ở định dạng JSON.
- Stateless: Mỗi request độc lập, không phụ thuộc trạng thái phiên.

Đề tài sử dụng **Java 21** kết hợp **Spring Boot 3.2.5** để xây dựng Backend API, gồm:
- **Spring Security + Firebase Admin SDK:** Xác thực Token JWT tập trung.
- **Spring Data JPA + Hibernate ORM:** Quản lý truy xuất CSDL MySQL.
- **Bean Validation (@Valid):** Kiểm soát dữ liệu đầu vào.
- **HikariCP:** Connection Pool hiệu năng cao.

### 1.5.3. Cơ sở dữ liệu MySQL

**MySQL** là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) mã nguồn mở, được sử dụng rộng rãi nhất trên thế giới. MySQL hỗ trợ đầy đủ chuẩn SQL, ràng buộc toàn vẹn dữ liệu (Foreign Keys, CASCADE), chỉ mục (Indexes) và giao dịch (Transactions).

Đề tài sử dụng **MySQL 8.0** với bộ mã **utf8mb4** (hỗ trợ đầy đủ tiếng Việt có dấu), thiết kế **9 bảng** dữ liệu chuẩn hóa: `users`, `accounts`, `categories`, `transactions`, `budgets`, `recurring_transactions`, `ai_product_logs`, `ai_scan_logs`, `transaction_images`.

### 1.5.4. Firebase Authentication

**Firebase Authentication** là dịch vụ xác thực người dùng của Google, cung cấp hệ thống đăng nhập an toàn, hỗ trợ nhiều phương thức: Email/Password, Google Sign-In, Facebook, số điện thoại… Firebase tạo ra **ID Token (JWT)** có chữ ký số xác minh bởi Google, giúp Backend xác thực danh tính người dùng mà không cần tự quản lý mật khẩu.

Đề tài áp dụng mô hình **Hybrid Authentication**: Client đăng nhập qua Firebase SDK, Backend xác thực Token bằng Firebase Admin SDK thông qua bộ lọc `FirebaseAuthFilter` trong Spring Security. Cơ chế này đảm bảo:
- Không lưu trữ mật khẩu người dùng trên máy chủ.
- Token có thời hạn (1 giờ), tự động làm mới.
- Kiến trúc Stateless, không cần quản lý session.

### 1.5.5. Mô hình YOLOv8 (You Only Look Once v8)

**YOLO (You Only Look Once)** là họ mô hình phát hiện đối tượng (Object Detection) nổi tiếng trong lĩnh vực Thị giác Máy tính, được biết đến với khả năng nhận diện đối tượng trong thời gian thực. **YOLOv8** là phiên bản mới nhất được phát triển bởi Ultralytics, mang đến sự cân bằng giữa tốc độ xử lý nhanh và độ chính xác cao.

**Kiến trúc YOLOv8** gồm 3 thành phần chính:
- **Backbone (CSPDarknet53):** Trích xuất đặc trưng phân cấp từ ảnh đầu vào, sử dụng kết nối Cross-Stage Partial (CSP) giúp cải thiện luồng gradient và giảm chi phí tính toán.
- **Neck (PANet + C2f Module):** Tổng hợp đặc trưng đa tỷ lệ (multi-scale feature fusion) bằng Path Aggregation Network. Module C2f (CSP Bottleneck with 2 convolutions) thay thế module C3 của YOLOv5, nâng cao khả năng phát hiện vật thể ở nhiều kích thước.
- **Head (Anchor-Free, Decoupled Head):** Không sử dụng anchor box định sẵn, tách biệt hai nhiệm vụ phân loại (Classification) và hồi quy hộp bao (Bounding Box Regression), kết hợp Distribution Focal Loss (DFL) tăng độ chính xác định vị.

Đề tài sử dụng phiên bản **YOLOv8 Nano** (mô hình nhẹ nhất) được huấn luyện nhận diện **18 lớp đối tượng mua sắm phổ biến** và chuyển đổi sang định dạng **TFLite Float32** để chạy trực tiếp trên thiết bị Android với tốc độ 30–45 FPS.

### 1.5.6. Google ML Kit Text Recognition (OCR)

**OCR (Optical Character Recognition – Nhận dạng Ký tự Quang học)** là công nghệ cho phép chuyển đổi hình ảnh chứa văn bản (ảnh chụp, tài liệu scan) thành dữ liệu văn bản có thể xử lý bằng máy tính.

**Google ML Kit Text Recognition** là thư viện OCR chạy trực tiếp trên thiết bị (On-Device), sử dụng mô hình TensorFlow Lite được Google tối ưu hóa. Đặc điểm nổi bật:
- **Xử lý Offline:** Không cần kết nối mạng, đảm bảo tốc độ nhanh và bảo mật dữ liệu.
- **Cấu trúc kết quả phân cấp:** Kết quả được tổ chức thành Blocks (khối) → Lines (dòng) → Elements (từ) → Symbols (ký tự), kèm thông tin tọa độ (bounding box) và độ tin cậy.
- **Hỗ trợ đa ngôn ngữ:** Nhận diện chữ Latin, tiếng Việt, tiếng Trung, tiếng Nhật...

Đề tài sử dụng ML Kit để quét hóa đơn giấy, trích xuất toàn bộ văn bản thô rồi gửi lên Backend để phân tích bằng Gemini AI.

### 1.5.7. Google Gemini AI (Mô hình Ngôn ngữ Lớn – LLM)

**Mô hình Ngôn ngữ Lớn (Large Language Model – LLM)** là mô hình AI được huấn luyện trên lượng dữ liệu văn bản khổng lồ, có khả năng hiểu và sinh ngôn ngữ tự nhiên ở mức độ cao.

**Google Gemini** là nền tảng AI đa phương thức (multimodal) thế hệ mới của Google. Đề tài sử dụng **Gemini 2.5 Flash** – phiên bản tối ưu tốc độ – để phân tích văn bản hóa đơn thô trích xuất từ OCR. Gemini nhận prompt kỹ thuật kèm JSON Schema và trả về dữ liệu có cấu trúc (tên cửa hàng, số tiền, ngày tháng, danh mục), giải quyết triệt để vấn đề hóa đơn bị nhăn, rách, font chữ phức tạp mà regex truyền thống không xử lý được.

### 1.5.8. Thuật toán Random Forest

**Random Forest** là thuật toán học máy có giám sát (Supervised Learning) thuộc nhóm phương pháp học kết hợp (Ensemble Learning), được sử dụng cho cả bài toán phân loại (Classification) và hồi quy (Regression).

**Nguyên lý hoạt động:**
1. **Lấy mẫu ngẫu nhiên (Bootstrap Sampling):** Tạo nhiều tập con dữ liệu huấn luyện ngẫu nhiên (có hoàn lại) từ tập dữ liệu gốc.
2. **Xây dựng rừng cây quyết định:** Mỗi tập con được dùng để huấn luyện một cây quyết định (Decision Tree) riêng biệt. Tại mỗi nút phân chia, chỉ một tập con ngẫu nhiên các đặc trưng được xem xét, tạo sự đa dạng giữa các cây.
3. **Bỏ phiếu đa số (Majority Voting):** Kết quả phân loại cuối cùng được quyết định bằng biểu quyết đa số từ toàn bộ các cây trong rừng.

**Ưu điểm:**
- Giảm hiện tượng quá khớp (overfitting) so với sử dụng một cây quyết định đơn lẻ.
- Xử lý tốt dữ liệu nhiều chiều và dữ liệu mất cân bằng.
- Dễ triển khai, hiệu năng ổn định.

Đề tài huấn luyện Random Forest bằng **Scikit-learn** (Python) với **100 cây quyết định** trên **27 đặc trưng đa chiều** được trích xuất từ kết quả nhận diện của YOLOv8, nhằm phân loại giao dịch vào 6 danh mục chi tiêu (Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Khác).

### 1.5.9. ONNX Runtime

**ONNX (Open Neural Network Exchange)** là định dạng chuẩn mở cho mô hình học máy, cho phép huấn luyện mô hình bằng một framework (PyTorch, TensorFlow, Scikit-learn) và triển khai trên một nền tảng khác mà không cần viết lại logic.

**ONNX Runtime** là engine suy luận (inference engine) hiệu năng cao do Microsoft phát triển, hỗ trợ đa nền tảng (Windows, Linux, macOS, Android, iOS) và đa ngôn ngữ (Python, C++, Java, C#, JavaScript). Đặc điểm:
- Tối ưu hóa đồ thị tính toán tự động (graph optimization): gộp toán tử, loại bỏ phép tính dư thừa.
- Hỗ trợ tăng tốc phần cứng qua Execution Providers (CUDA, TensorRT, OpenVINO…).
- Tốc độ suy luận nhanh hơn so với chạy trực tiếp trên framework huấn luyện.

Đề tài tích hợp **Microsoft ONNX Runtime Java** trực tiếp vào Spring Boot để nạp mô hình Random Forest đã xuất sang định dạng `.onnx`, thực hiện suy luận phân loại danh mục chi tiêu với độ trễ **dưới 2ms** mỗi lần. Cơ chế **Fallback** tự động chuyển sang bộ quy tắc Rule-based khi môi trường không tải được thư viện native.

### 1.5.10. Docker và Docker Compose

**Docker** là nền tảng đóng gói ứng dụng thành các container – môi trường chạy độc lập, nhẹ, chứa đầy đủ mã nguồn, thư viện và cấu hình cần thiết. Docker giải quyết vấn đề "chạy được trên máy tôi nhưng không chạy trên máy khác" bằng cách đảm bảo môi trường thực thi đồng nhất.

**Docker Compose** cho phép định nghĩa và quản lý ứng dụng đa container thông qua một file cấu hình duy nhất (`docker-compose.yml`).

Đề tài sử dụng Docker Compose để đóng gói toàn bộ hệ thống Backend (Spring Boot + MySQL) thành 2 container, cho phép khởi chạy bằng một lệnh duy nhất (`docker compose up`), đảm bảo quá trình triển khai nhanh chóng và không phụ thuộc vào cấu hình máy cục bộ.

---

## 1.6. Kết luận chương

Chương này đã trình bày tổng quan về bài toán quản lý tài chính cá nhân trên thiết bị di động, khảo sát các ứng dụng hiện có trên thị trường và nghiên cứu các công nghệ liên quan sẽ được sử dụng trong đề tài. Qua khảo sát cho thấy, các ứng dụng quản lý tài chính hiện tại chủ yếu dựa trên nhập liệu thủ công, chưa tận dụng được tiềm năng của Trí tuệ Nhân tạo và Thị giác Máy tính trong việc tự động hóa quá trình ghi nhận giao dịch. Đề tài đề xuất xây dựng một hệ thống tích hợp đa công nghệ AI hiện đại (YOLOv8, Random Forest, OCR, Gemini AI) trên kiến trúc 3 tầng (Android MVVM – Spring Boot API – MySQL) nhằm mang lại trải nghiệm quản lý tài chính thông minh, tiện lợi và hiệu quả hơn cho người dùng.
