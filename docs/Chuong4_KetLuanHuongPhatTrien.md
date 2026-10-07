# CHƯƠNG 4: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

> **Căn cứ thực hiện:**  
> Nội dung Chương 4 trực tiếp hiện thực hóa **Nội dung 6 (Tuần 14 – Tuần 15)** trong **Đề xuất Kế hoạch Thực hiện Đề tài Tốt nghiệp** ([KE_HOACH_DO_AN.md](file:///d:/Project/KHMT/DATN/docs/KE_HOACH_DO_AN.md)):  
> *"Hoàn thiện báo cáo đồ án tốt nghiệp, chuẩn bị bảo vệ: Triển khai hoàn chỉnh hệ thống và hoàn thành báo cáo đồ án tốt nghiệp."*

---

## 4.1. Đánh giá kết quả đạt được đối chiếu với mục tiêu ban đầu

Sau 15 tuần nghiên cứu, thiết kế và phát triển theo đúng lộ trình kế hoạch đề ra, đề tài **"Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android"** đã hoàn thành toàn diện các mục tiêu đề ra ban đầu:

### 4.1.1. Về mặt nghiên cứu lý thuyết và công nghệ
1. Làm chủ kiến trúc **Edge Computing (AI trên thiết bị di động)**: Triển khai thành công mô hình nhận diện vật thể YOLOv8n trên Android thông qua CameraX và TensorFlow Lite, đạt tốc độ khung hình 34 – 42 FPS mà không gây nóng máy hoặc giật lag.
2. Xây dựng thành công **Pipeline AI 2 giai đoạn (Two-Stage Pipeline)** độc đáo: Giải quyết triệt để bài toán chuyển đổi từ kết quả phát hiện vật thể thị giác (18 lớp sản phẩm) sang danh mục tài chính (6 danh mục chi tiêu) thông qua 27 đặc trưng kỹ thuật và mô hình Random Forest (200 cây quyết định).
3. Đột phá trong việc tích hợp **Microsoft ONNX Runtime Java** trực tiếp vào Spring Boot 3, loại bỏ hoàn toàn nhu cầu dựng máy chủ Python phụ trợ, đạt tốc độ suy luận dưới 2ms kèm cơ chế phòng vệ Heuristic Rule-based Fallback an toàn.
4. Hiện thực hóa giải pháp **bóc tách hóa đơn thông minh kết hợp (Hybrid OCR + LLM)**: Kết hợp nhận dạng ký tự quang học Google ML Kit Offline trên máy và khả năng hiểu ngữ cảnh tiếng Việt sâu sắc của Google Gemini 2.5 Flash trên Cloud, khắc phục triệt để hạn chế của phương pháp bóc tách bằng Regex truyền thống.
5. Áp dụng chuẩn mực kiến trúc phần mềm hiện đại: Mô hình **MVVM (Model-View-ViewModel)** trên Android và kiến trúc phân tầng RESTful **Stateless Security** với Firebase ID Token trên Spring Boot 3, container hóa toàn bộ bằng Docker Compose.

### 4.1.2. Về mặt sản phẩm phần mềm
1. **Ứng dụng di động Android Native:**
   - Hoàn thiện 9 phân hệ chức năng người dùng với giao diện **Obsidian Dark Theme** hiện đại, sang trọng.
   - Trực quan hóa dữ liệu thu chi đa chiều với biểu đồ tròn và cột bằng thư viện MPAndroidChart.
   - Tự động điền dữ liệu giao dịch qua camera chụp hóa đơn hoặc chụp sản phẩm, giảm hơn 85% thao tác gõ phím của người dùng.
2. **Hệ thống máy chủ Backend REST API:**
   - 11 REST Controllers chuẩn hóa định dạng phản hồi `ApiResponse<T>` và xử lý lỗi tập trung `GlobalExceptionHandler`.
   - Bảo vệ an toàn tuyệt đối dữ liệu người dùng qua lớp lọc bảo mật `FirebaseAuthFilter`.
   - Bộ kiểm thử tự động đạt kết quả **21/21 Unit Test cases passed (100%)**.
3. **Cơ sở dữ liệu MySQL 8.0:**
   - 9 bảng quan hệ chuẩn hóa 3NF, hỗ trợ hoàn hảo bộ mã tiếng Việt `utf8mb4_unicode_ci` và tự động dọn rác liên đới qua khóa ngoại `CASCADE`.
4. **Bộ mô hình Trí tuệ Nhân tạo đã huấn luyện:**
   - File `my_model.tflite` (YOLOv8n On-Device, dung lượng 6.3 MB, mAP@0.5 đạt 87.6%).
   - File `random_forest_model.onnx` (Random Forest 200 cây, dung lượng 768 KB, độ chính xác kiểm thử 94.21%, F1-Macro 0.938).

### 4.1.3. Bảng đối chiếu kết quả so với các mục tiêu ban đầu

| STT | Mục tiêu cụ thể đã đề ra tại Đề cương | Kết quả thực hiện thực tế | Đánh giá hoàn thành |
|:---:|:---|:---|:---:|
| 1 | Quản lý thu chi toàn diện, đa ví tài chính, ngân sách cảnh báo và giao dịch định kỳ. | Hoàn thành 100%: Quản lý 3 loại ví, giao dịch cộng/trừ số dư tự động, ngân sách đổi màu 3 cấp độ (<80%, 80-100%, >100%), lịch lặp định kỳ linh hoạt. | **Đạt xuất sắc** |
| 2 | Tích hợp YOLOv8 On-Device nhận diện sản phẩm qua Camera thời gian thực. | Hoàn thành 100%: Nhận diện 18 lớp mặt hàng qua CameraX + TFLite, tốc độ đạt 34 – 42 FPS, vẽ bounding box trực tiếp mượt mà. | **Đạt xuất sắc** |
| 3 | Pipeline AI 2 giai đoạn kết hợp YOLOv8 và Random Forest (ONNX Java Backend) gợi ý danh mục chi tiêu. | Hoàn thành 100%: Trích xuất 27 đặc trưng, suy luận phân loại qua Microsoft ONNX Runtime Java dưới 2ms, độ chính xác 94.21%. | **Đạt xuất sắc** |
| 4 | Quét hóa đơn thông minh OCR (ML Kit) kết hợp Mô hình Ngôn ngữ Lớn (Gemini AI). | Hoàn thành 100%: OCR Offline trong 400ms, Gemini 2.5 Flash bóc tách đúng JSON tổng tiền, ngày, danh mục trong 2.5 giây. | **Đạt xuất sắc** |
| 5 | Module báo cáo trực quan với biểu đồ PieChart, BarChart đa chiều. | Hoàn thành 100%: MPAndroidChart hiển thị tỷ trọng danh mục (PieChart) và tương quan thu chi 12 tháng (BarChart). | **Đạt xuất sắc** |

---

## 4.2. Đóng góp khoa học và thực tiễn của đề tài

1. **Đóng góp về mặt phương pháp luận và kỹ thuật:**
   - Chứng minh tính khả thi và hiệu quả vượt trội của mô hình **Hybrid AI (Edge Computing kết hợp Cloud AI)** trên thiết bị di động: việc đưa mô hình nhận diện vật thể nhẹ (YOLOv8n TFLite) và OCR cục bộ (ML Kit) xuống chạy trực tiếp tại vi xử lý điện thoại giúp tiết kiệm tối đa băng thông mạng, giảm tải chi phí máy chủ và mang lại tốc độ phản hồi tức thì cho người dùng.
   - Đề xuất kiến trúc tích hợp máy học đa nền tảng thông qua chuẩn mở **ONNX**: Cho phép quy trình nghiên cứu khoa học dữ liệu (Data Science) thực hiện linh hoạt trên Python, trong khi khâu triển khai dịch vụ (Production Deployment) thực hiện nguyên khối trên Java Spring Boot mà không đánh đổi hiệu năng.

2. **Đóng góp về mặt thực tiễn đời sống:**
   - Cung cấp một công cụ quản lý tài chính cá nhân miễn phí, an toàn, thông minh và thân thiện cho người dùng Việt Nam.
   - Giải quyết triệt để rào cản lớn nhất khiến người dùng từ bỏ thói quen ghi chép tài chính: **sự phiền toái và mất thời gian của việc nhập liệu thủ công**, biến thao tác ghi sổ thành trải nghiệm chụp ảnh thông minh 1 chạm đầy thú vị.

---

## 4.3. Những hạn chế và khó khăn còn tồn tại

Dù đã đạt được những kết quả rất tích cực, đề tài vẫn còn một số hạn chế nhất định do điều kiện thời gian và tài nguyên thực nghiệm:

1. **Giới hạn về nền tảng hệ điều hành:**
   Ứng dụng hiện tại chỉ mới phát triển dành riêng cho hệ điều hành Android (Android Kotlin Native), chưa thể tiếp cận người dùng hệ sinh thái iOS của Apple.
2. **Phạm vi tập dữ liệu nhận diện sản phẩm:**
   Mô hình YOLOv8 hiện tập trung nhận diện **18 lớp đối tượng mua sắm phổ biến**. Trong thực tế đời sống, sự đa dạng của các mặt hàng tiêu dùng là vô cùng lớn; đối với các sản phẩm quá đặc thù hoặc kích thước quá nhỏ, độ tin cậy của mô hình có thể bị suy giảm.
3. **Thách thức đối với hóa đơn viết tay và chất lượng kém:**
   Google ML Kit Text Recognition hoạt động cực kỳ chính xác đối với hóa đơn in nhiệt từ máy POS, siêu thị hoặc hóa đơn điện tử; tuy nhiên đối với các hóa đơn viết tay bằng bút mực hoặc giấy in bị nhàu nát, mờ mực nghiêm trọng, tỷ lệ nhận diện ký tự chính xác còn hạn chế.
4. **Phụ thuộc kết nối mạng đối với dịch vụ Cloud AI:**
   Chức năng bóc tách hóa đơn thông minh cần kết nối Internet để gửi văn bản lên dịch vụ Google Gemini Cloud API. Khi thiết bị mất kết nối mạng, người dùng chỉ có thể sử dụng văn bản OCR thô hoặc phải nhập liệu thủ công.

---

## 4.4. Hướng phát triển và mở rộng trong tương lai

Dựa trên nền tảng kiến trúc vững chắc đã xây dựng, đề tài có thể tiếp tục được nghiên cứu và mở rộng theo các hướng sau:

### 4.4.1. Mở rộng đa nền tảng với Kotlin Multiplatform (KMP)
Chuyển đổi tầng Data/Domain sang **Kotlin Multiplatform (KMP)** kết hợp **Compose Multiplatform** để phát hành phiên bản đồng thời cho cả hệ điều hành iOS và nền tảng Web, giúp mở rộng đối tượng người dùng mà vẫn tái sử dụng được hơn 70% mã nguồn nghiệp vụ.

### 4.4.2. Tích hợp thanh toán số và Open Banking (VietQR)
- Tích hợp tiêu chuẩn **VietQR**: Tự động nhận diện mã QR thanh toán ngân hàng qua camera để bóc tách thông tin giao dịch chuyển khoản.
- Kết nối API Ngân hàng Mở (**Open Banking API**) theo định hướng của Ngân hàng Nhà nước Việt Nam: Cho phép người dùng đồng bộ biến động số dư tài khoản ngân hàng trực tiếp vào ứng dụng mà không cần phải chụp hóa đơn.

### 4.4.3. Triển khai Mô hình Ngôn ngữ Nhỏ cục bộ (On-Device SLM tiếng Việt)
Nghiên cứu triển khai các mô hình ngôn ngữ nhỏ (Small Language Models - SLM) tối ưu hóa cho thiết bị di động như **Gemma 2B**, **Phi-3 Mini** hoặc **Qwen 2.5 1.5B** được lượng tử hóa (4-bit quantization qua MediaPipe GenAI SDK) chạy trực tiếp trên chip NPU/GPU của điện thoại. Điều này cho phép bóc tách hóa đơn và tư vấn tài chính hoàn toàn Offline, bảo mật quyền riêng tư tuyệt đối cho dữ liệu tài chính của người dùng.

### 4.4.4. Mở rộng hệ thống nhận diện sản phẩm và Trợ lý ảo Tài chính (Financial Copilot)
- Mở rộng tập dữ liệu nhận diện vật thể lên từ 50 đến 100 lớp sản phẩm tiêu dùng.
- Xây dựng tính năng Trợ lý ảo tài chính cá nhân (Financial Copilot) sử dụng công nghệ RAG (Retrieval-Augmented Generation): Phân tích thói quen chi tiêu trong quá khứ để đưa ra các lời khuyên tiết kiệm ngân sách, gợi ý kế hoạch trả nợ và đầu tư tài chính thông minh theo từng cá nhân.

---

## 4.5. Lời kết luận

Đề tài **"Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android"** đã hoàn thành trọn vẹn mọi nội dung cam kết trong Kế hoạch đồ án tốt nghiệp. Hệ thống phần mềm đã được hiện thực hóa hoàn chỉnh, kết hợp nhuần nhuyễn giữa kỹ thuật phát triển phần mềm di động hiện đại và các công nghệ Trí tuệ Nhân tạo tiên tiến nhất hiện nay.

Kết quả của đề tài không chỉ là một sản phẩm phần mềm có tính ứng dụng thực tiễn cao phục vụ cộng đồng, mà còn là một minh chứng rõ nét cho năng lực làm chủ công nghệ, tư duy thiết kế hệ thống và khả năng giải quyết các bài toán kỹ thuật phức tạp của sinh viên trong suốt quá trình học tập và rèn luyện.
