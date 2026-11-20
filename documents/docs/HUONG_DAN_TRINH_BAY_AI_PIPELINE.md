# 🧠 TÀI LIỆU HƯỚNG DẪN TRÌNH BÀY MODULE AI PIPELINE
**Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 📌 MỤC LỤC
1. [Tổng quan bài toán & Lý do tích hợp AI](#1-tổng-quan-bài-toán--lý-do-tích-hợp-ai)
2. [Kiến trúc Pipeline AI 2 giai đoạn (Two-Stage AI Pipeline)](#2-kiến-trúc-pipeline-ai-2-giai-đoạn-two-stage-ai-pipeline)
3. [Chi tiết quy trình 5 bước kỹ thuật đã triển khai](#3-chi-tiết-quy-trình-5-bước-kỹ-thuật-đã-triển-khai)
   - [Bước 1: Huấn luyện và lượng tử hóa YOLOv8](#bước-1-huấn-luyện-và-lượng-tử-hóa-yolov8-on-device)
   - [Bước 2: Kỹ thuật trích xuất đặc trưng 27 chiều (Feature Engineering)](#bước-2-kỹ-thuật-trích-xuất-đặc-trưng-27-chiều-feature-engineering)
   - [Bước 3: Huấn luyện Random Forest Classifier](#bước-3-huấn-luyện-random-forest-classifier)
   - [Bước 4: Đánh giá mô hình (Cross-Validation, Accuracy > 95%)](#bước-4-đánh-giá-mô-hình-cross-validation-accuracy--95)
   - [Bước 5: Đóng gói ONNX và triển khai Spring Boot](#bước-5-đóng-gói-onnx-và-triển-khai-spring-boot)
4. [Tích hợp Pipeline quét hóa đơn (OCR + Gemini AI)](#4-tích-hợp-pipeline-quét-hóa-đơn-ocr--gemini-ai)
5. [Bộ câu hỏi phản biện thường gặp của Hội đồng & Câu trả lời chuẩn](#5-bộ-câu-hỏi-phản-biện-thường-gặp-của-hội-đồng--câu-trả-lời-chuẩn)

---

## 1. TỔNG QUAN BÀI TOÁN & LÝ DO TÍCH HỢP AI

### 1.1 Vấn đề thực tế
* Trong quản lý tài chính cá nhân, rào cản lớn nhất khiến hơn **70% người dùng bỏ cuộc sau 1–2 tuần** là:
  1. Thao tác nhập liệu thủ công bằng tay quá rườm rà, tốn thời gian.
  2. Người dùng hay phân vân hoặc phân loại nhầm danh mục chi tiêu (ví dụ: mua cafe không biết xếp vào *Ăn uống* hay *Giao tiếp/Công việc*).

### 1.2 Giải pháp thông minh của Đề tài
* Xây dựng cơ chế tự động hóa bằng cách kết hợp **Thị giác Máy tính (Computer Vision)** và **Học máy (Machine Learning)**:
  * Người dùng chỉ cần giơ camera chụp sản phẩm hoặc quét hóa đơn.
  * Hệ thống tự động nhận diện mặt hàng, tính toán ngữ cảnh chi tiêu và đề xuất danh mục tài chính chính xác mà không cần gõ tay.

---

## 2. KIẾN TRÚC PIPELINE AI 2 GIAI ĐOẠN (TWO-STAGE AI PIPELINE)

Hệ thống sử dụng mô hình kết hợp (Hybrid Architecture) chia làm 3 bước nối tiếp nhau:

```text
[ Ảnh chụp sản phẩm từ Camera ]
             │
             ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 1: On-Device Object Detection                │
│ • Công nghệ: YOLOv8 xuất sang TensorFlow Lite (.tflite) │
│ • Vị trí: Chạy trực tiếp Offline trên điện thoại       │
│ • Nhiệm vụ: Phát hiện 18 loại đồ vật mua sắm phổ biến  │
└───────────────────────────┬────────────────────────────┘
                            │ Danh sách nhãn (labels) + Độ tin cậy (confidences)
                            ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 2: Feature Engineering (Trích xuất đặc trưng)│
│ • Vị trí: config.py (Python) & Mobile/Backend Service  │
│ • Nhiệm vụ: Chuyển đổi kết quả YOLO thành Vector 27    │
│   chiều phản ánh ngữ cảnh chi tiêu đa chiều            │
└───────────────────────────┬────────────────────────────┘
                            │ Vector đặc trưng [27 chiều]
                            ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 3: Financial Category Classification         │
│ • Công nghệ: Random Forest xuất chuẩn Microsoft ONNX   │
│ • Vị trí: Chạy trong Backend Spring Boot (ONNX Java)   │
│ • Nhiệm vụ: Dự đoán chính xác danh mục chi tiêu        │
│ • Tốc độ: Cực nhanh (< 2ms)                            │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
      [ Gợi ý danh mục: Ăn uống, Di chuyển, Mua sắm... ]
```

---

## 3. CHI TIẾT QUY TRÌNH 5 BƯỚC KỸ THUẬT ĐÃ TRIỂN KHAI

### Bước 1: Huấn luyện và lượng tử hóa YOLOv8 (On-Device)
* **Tập dữ liệu (Dataset):** Gồm **1.295 ảnh** đồ vật mua sắm thường gặp được thu thập thực tế và gán nhãn chuẩn hóa.
* **18 Nhãn đối tượng định nghĩa:**
  * **Ăn uống (Food & Drink):** `rice_meal`, `noodle`, `bread`, `fastfood`, `snack`, `coffee_cup`, `milk_tea`, `soft_drink`, `bottled_water`.
  * **Di chuyển (Transport):** `motorbike`, `helmet`, `taxi_car`.
  * **Mua sắm (Shopping):** `clothes`, `shoes`, `cosmetic`, `electronic_item`.
  * **Giải trí (Entertainment):** `toy_game`.
  * **Sức khỏe (Health):** `medicine`.
* **Đóng gói On-Device:** Dùng `convert_yolo.py` để xuất mô hình sang `yolo_product.tflite` (38.3 MB) nạp trực tiếp vào Android Assets, cho tốc độ phát hiện **35–50ms/ảnh**.

### Bước 2: Kỹ thuật trích xuất đặc trưng 27 chiều (Feature Engineering)
Từ kết quả bounding boxes và độ tin cậy của YOLO, hàm `compute_features()` trích xuất thành **vector 27 đặc trưng** gồm 3 nhóm:
1. **18 đặc trưng nhị phân (`has_<label>`):** Thể hiện sự có mặt của từng loại vật thể trong ảnh (`0` hoặc `1`).
2. **5 đặc trưng nhóm số lượng (`group_counts`):**
   * `food_drink_count`: Tổng số đồ ăn/uống xuất hiện.
   * `transport_count`: Số lượng xe cộ/mũ bảo hiểm.
   * `shopping_count`: Số lượng quần áo, giày dép, mỹ phẩm, đồ điện tử.
   * `entertainment_count`: Số lượng đồ chơi, thiết bị game.
   * `health_count`: Số lượng thuốc, hộp thuốc y tế.
3. **4 đặc trưng thống kê độ tin cậy (`confidence_stats`):**
   * `total_objects`: Tổng số vật thể tìm thấy.
   * `max_confidence`: Độ tin cậy cao nhất.
   * `avg_confidence`: Độ tin cậy trung bình.
   * `low_confidence_count`: Số vật thể có độ tin cậy dưới ngưỡng (`< 0.5`).

### Bước 3: Huấn luyện Random Forest Classifier
* Mã nguồn: `train_rf.py`.
* Siêu tham số tối ưu (Hyperparameters):
  * `n_estimators = 200`: Rừng gồm 200 cây quyết định, đảm bảo tính ổn định và chống phương sai.
  * `max_depth = 20`: Kiểm soát độ sâu để tránh học vẹt (overfitting).
  * `min_samples_leaf = 3`: Mỗi nút lá tối thiểu 3 mẫu dữ liệu.
  * `class_weight = 'balanced'`: Tự động cân bằng trọng số giữa các nhóm danh mục.

### Bước 4: Đánh giá mô hình (Cross-Validation, Accuracy > 95%)
* Mã nguồn: `evaluate_rf.py`.
* Phương pháp kiểm định: **Stratified 5-Fold Cross Validation** (Kiểm tra chéo 5 lần có phân tầng để giữ nguyên tỷ lệ phân bố giữa các lớp).
* **Kết quả đạt được:**
  * Độ chính xác tổng thể (**Accuracy**): **> 95%**.
  * Điểm số **Macro F1-Score**: Cân bằng đồng đều trên cả 6 nhóm danh mục chi tiêu.
  * Đã xuất biểu đồ ma trận nhầm lẫn (`confusion_matrix.png`) và mức độ quan trọng của đặc trưng (`feature_importance.png`) trong thư mục `ai-pipeline/outputs/`.

### Bước 5: Đóng gói ONNX và triển khai Spring Boot
* Mã nguồn: `convert_rf.py`.
* Mô hình được chuyển đổi từ Scikit-learn sang chuẩn mở **Microsoft ONNX (`random_forest_model.onnx` - 768 KB)**.
* **Tích hợp Backend:** Đặt tại `backend/src/main/resources/random_forest_model.onnx`, chạy bằng **ONNX Runtime Java**.
* **Ưu điểm vượt trội:**
  * Không phụ thuộc vào môi trường Python trên máy chủ.
  * Thời gian suy luận cực nhanh: **< 2 mili-giây (< 2ms) cho mỗi giao dịch**.

---

## 4. TÍCH HỢP PIPELINE QUÉT HÓA ĐƠN (OCR + GEMINI AI)

Bên cạnh nhận diện sản phẩm, đồ án tích hợp thêm luồng quét hóa đơn:
1. **Google ML Kit OCR (Android On-Device):** Chụp ảnh hóa đơn $\rightarrow$ Nhận diện văn bản thô dạng khối siêu tốc mà không tốn cước mạng.
2. **Backend Spring Boot (`GeminiService.java`):** Gửi văn bản OCR kèm JSON Schema nghiêm ngặt đến **Gemini 2.5 Flash API**.
3. **Trích xuất thông tin cấu trúc:** AI trả về JSON chuẩn gồm: *Tên cửa hàng (merchant), Tổng tiền thanh toán (amount), Ngày hóa đơn (date), và Danh mục gợi ý (category)*.
4. **Cơ chế Fallback thông minh (`ReceiptParser.java`):** Nếu mất mạng hoặc chưa cấu hình Gemini API, Backend tự động chuyển sang bộ tách chữ cục bộ bằng Biểu thức chính quy (Regex Heuristic) mà không gây gián đoạn ứng dụng.

---

## 5. BỘ CÂU HỎI PHẢN BIỆN THƯỜNG GẶP CỦA HỘI ĐỒNG & CÂU TRẢ LỜI CHUẨN

### ❓ Câu 1: Tại sao không để YOLO phân loại luôn Danh mục mà phải nối thêm Random Forest?
> **Trả lời:**  
> *"Thưa Thầy, YOLO là mô hình thị giác chỉ phát hiện được **vật thể vật lý cụ thể** (như bát cơm, cốc cafe, mũ bảo hiểm), trong khi Danh mục tài chính lại là một **khái niệm trừu tượng theo ngữ cảnh chi tiêu**.  
> Hơn nữa, một bức ảnh chụp có thể chứa **nhiều vật thể cùng lúc** (ví dụ: vừa có cốc cafe vừa có đồ ăn vặt). Việc kết hợp Random Forest với 27 đặc trưng thống kê giúp hệ thống tổng hợp thông tin đa chiều để đưa ra quyết định tài chính chuẩn xác nhất, không bị thiên lệch."*

### ❓ Câu 2: Tại sao chọn Random Forest mà không dùng Deep Learning (Mạng nơ-ron sâu MLP)?
> **Trả lời:**  
> *"Thưa Thầy, với dữ liệu dạng bảng số liệu (Tabular Data) 27 đặc trưng thì **Random Forest vượt trội hơn hẳn Mạng nơ-ron** về độ ổn định, không bị overfit với tập dữ liệu vừa phải, và quan trọng nhất là **thời gian suy luận cực nhanh (< 2ms)**, tiêu tốn rất ít RAM của máy chủ và có khả năng giải thích rõ ràng qua Feature Importance."*

### ❓ Câu 3: Hệ thống có chạy được khi điện thoại mất kết nối Internet không?
> **Trả lời:**  
> *"Thưa Thầy, hoàn toàn chạy được. Mô hình YOLOv8 đã được nén thành TFLite nằm trực tiếp trong App Android (`assets/yolo_product.tflite`). Người dùng chụp ảnh là điện thoại tự nhận diện ra vật thể ngay cả khi bật chế độ máy bay."*

### ❓ Câu 4: Nếu AI nhận diện sai hoặc độ tin cậy thấp thì app xử lý thế nào?
> **Trả lời:**  
> *"Thưa Thầy, hệ thống được thiết kế theo nguyên lý lấy người dùng làm trung tâm: AI chỉ đóng vai trò **đề xuất tự động (Auto-suggestion)**. Kết quả dự đoán được hiển thị trên form để người dùng kiểm tra lại và được quyền chọn đổi danh mục bất kỳ trước khi bấm Lưu. Đồng thời hệ thống có cơ chế Fallback sang danh mục 'Khác' nếu độ tin cậy dưới ngưỡng quy định."*
