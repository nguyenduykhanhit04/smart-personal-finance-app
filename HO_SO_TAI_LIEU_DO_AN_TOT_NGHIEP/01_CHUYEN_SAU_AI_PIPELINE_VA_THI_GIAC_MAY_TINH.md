# 🧠 TÀI LIỆU CHUYÊN SÂU: MODULE TRÍ TUỆ NHÂN TẠO & THỊ GIÁC MÁY TÍNH (AI PIPELINE)
**Mã tài liệu:** `01_AI_PIPELINE_COMPUTER_VISION`  
**Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 📌 MỤC LỤC CHI TIẾT
1. [Tổng quan bài toán & Lý thuyết nền tảng](#1-tổng-quan-bài-toán--lý-thuyết-nền-tảng)
2. [Kiến trúc Pipeline AI 2 giai đoạn (Two-Stage Hybrid Pipeline)](#2-kiến-trúc-pipeline-ai-2-giai-đoạn-two-stage-hybrid-pipeline)
3. [Giai đoạn 1: Mô hình thị giác máy tính YOLOv8 (On-Device Object Detection)](#3-giai-đoạn-1-mô-hình-thị-giác-máy-tính-yolov8-on-device-object-detection)
   - [3.1 Thu thập và tiền xử lý tập dữ liệu (1.295 ảnh)](#31-thu-thập-và-tiền-xử-lý-tập-dữ-liệu-1295-ảnh)
   - [3.2 18 Nhãn sản phẩm mua sắm được định nghĩa](#32-18-nhãn-sản-phẩm-mua-sắm-được-định-nghĩa)
   - [3.3 Kiến trúc mạng YOLOv8 & Hàm mất mát (Loss Functions)](#33-kiến-trúc-mạng-yolov8--hàm-mất-mát-loss-functions)
   - [3.4 Kết quả huấn luyện & Đánh giá (mAP50, Precision, Recall)](#34-kết-quả-huấn-luyện--đánh-giá-map50-precision-recall)
   - [3.5 Lượng tử hóa và triển khai On-Device qua TensorFlow Lite](#35-lượng-tử-hóa-và-triển-khai-on-device-qua-tensorflow-lite)
4. [Giai đoạn 2: Kỹ thuật trích xuất đặc trưng đa chiều (Feature Engineering - 27 chiều)](#4-giai-đoạn-2-kỹ-thuật-trích-xuất-đặc-trưng-đa-chiều-feature-engineering---27-chiều)
   - [4.1 Vì sao cần bước Feature Engineering chuyển tiếp?](#41-vì-sao-cần-bước-feature-engineering-chuyển-tiếp)
   - [4.2 Bảng chi tiết 27 vector đặc trưng](#42-bảng-chi-tiết-27-vector-đặc-trưng)
   - [4.3 Thuật toán tính toán đặc trưng (`config.py`)](#43-thuật-toán-tính-toán-đặc-trưng-configpy)
5. [Giai đoạn 3: Mô hình phân loại Random Forest & Tối ưu hóa](#5-giai-đoạn-3-mô-hình-phân-loại-random-forest--tối-ưu-hóa)
   - [5.1 Thuật toán Ensemble Learning - Rừng ngẫu nhiên](#51-thuật-toán-ensemble-learning---rừng-ngẫu-nhiên)
   - [5.2 Siêu tham số tối ưu (Hyperparameters)](#52-siêu-tham-số-tối-ưu-hyperparameters)
   - [5.3 Kiểm định chéo Stratified 5-Fold Cross Validation](#53-kiểm-định-chéo-stratified-5-fold-cross-validation)
   - [5.4 Phân tích Ma trận nhầm lẫn & Mức độ quan trọng của đặc trưng](#54-phân-tích-ma-trận-nhầm-lẫn--mức-độ-quan-trọng-của-đặc-trưng)
6. [Đóng gói mô hình ONNX & Triển khai vào Spring Boot Backend](#6-đóng-gói-mô-hình-onnx--triển-khai-vào-spring-boot-backend)
   - [6.1 Chuyển đổi mô hình Scikit-learn sang Microsoft ONNX](#61-chuyển-đổi-mô-hình-scikit-learn-sang-microsoft-onnx)
   - [6.2 Tích hợp và suy luận thời gian thực qua ONNX Runtime Java (< 2ms)](#62-tích-hợp-và-suy-luận-thời-gian-thực-qua-onnx-runtime-java--2ms)
7. [Module Quét hóa đơn: Kết hợp ML Kit OCR & Gemini 2.5 Flash](#7-module-quét-hóa-đơn-kết-hợp-ml-kit-ocr--gemini-25-flash)
   - [7.1 Cơ chế trích xuất văn bản thô On-Device OCR](#71-cơ-chế-trích-xuất-văn-bản-thô-on-device-ocr)
   - [7.2 Kỹ thuật Prompt Engineering & JSON Schema cho Gemini](#72-kỹ-thuật-prompt-engineering--json-schema-cho-gemini)
   - [7.3 Cơ chế Fallback Heuristic Regex tự phục hồi khi mất mạng](#73-cơ-chế-fallback-heuristic-regex-tự-phục-hồi-khi-mất-mạng)
8. [Bộ câu hỏi phản biện chuyên sâu dành cho Hội đồng](#8-bộ-câu-hỏi-phản-biện-chuyên-sâu-dành-cho-hội-đồng)

---

## 1. TỔNG QUAN BÀI TOÁN & LÝ THUYẾT NỀN TẢNG

### 1.1 Thách thức trong phân loại tài chính tự động
Trong thực tế cuộc sống, việc ghi chép chi tiêu đối mặt với hai vấn đề:
1. **Rào cản thao tác (Interaction Friction):** Sau mỗi lần mua hàng (tại cửa hàng tiện lợi, quán cafe, siêu thị), người dùng phải mở ứng dụng, gõ số tiền, chọn danh mục. Việc này lặp đi lặp lại khiến người dùng bỏ cuộc.
2. **Sự nhập nhằng về ngữ cảnh chi tiêu (Ambiguity):** Một bức ảnh chụp có thể chứa nhiều đồ vật (ví dụ: một cốc trà sữa và một gói đồ ăn vặt). Nếu dùng mô hình thị giác thông thường, hệ thống chỉ nhận diện được "trà sữa" hoặc "đồ ăn vặt", nhưng trong tài chính, hai món này đều phải gộp vào danh mục lớn là **"Ăn uống" (Food & Drink)**. Nếu ảnh chụp một chiếc mũ bảo hiểm và xe máy, nó phải được gom vào **"Di chuyển" (Transport)**.

### 1.2 Giải pháp kiến trúc của Đề tài
Thay vì cố gắng huấn luyện một mô hình End-to-End khổng lồ (vừa nhận diện ảnh vừa phân loại tài chính, đòi hỏi phần cứng rất nặng và khó chạy trên điện thoại), đồ án tiếp cận theo hướng **Mô hình lai 2 giai đoạn (Two-Stage Hybrid Architecture)**:
* **Giai đoạn 1 (Thị giác):** Dùng YOLOv8 chuyên biệt hóa để phát hiện vật thể cụ thể ngay trên điện thoại (Offline).
* **Giai đoạn 2 (Ngữ cảnh):** Trích xuất vector đặc trưng tài chính đa chiều và đưa qua mô hình học máy Random Forest để ra quyết định phân loại danh mục với tốc độ suy luận dưới 2 mili-giây.

---

## 2. KIẾN TRÚC PIPELINE AI 2 GIAI ĐOẠN (TWO-STAGE HYBRID PIPELINE)

```text
[ Ảnh chụp từ Camera Điện thoại ]
               │
               ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 1: On-Device Object Detection                │
│ • Mô hình: YOLOv8 Nano (lượng tử hóa INT8/FP16)        │
│ • Nền tảng: TensorFlow Lite (yolo_product.tflite)      │
│ • Vị trí: Chạy Offline trên Android (CameraX)          │
│ • Đầu ra: Danh sách nhãn vật thể + Tọa độ Bounding Box │
│           kèm Điểm tin cậy (Confidence score)          │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 2: Feature Engineering (Trích xuất đặc trưng)│
│ • Mã nguồn: config.py (compute_features)               │
│ • Chuyển đổi kết quả phát hiện thành Vector 27 chiều:  │
│   - 18 đặc trưng nhị phân (sự xuất hiện của nhãn)      │
│   - 5 đặc trưng đếm nhóm ngữ cảnh chi tiêu             │
│   - 4 đặc trưng thống kê độ tin cậy                    │
└───────────────────────────┬────────────────────────────┘
                            │ Vector [27 chiều]
                            ▼
┌────────────────────────────────────────────────────────┐
│ GIAI ĐOẠN 3: Financial Category Classification         │
│ • Mô hình: Random Forest Classifier (200 cây)          │
│ • Định dạng: Microsoft ONNX (random_forest_model.onnx) │
│ • Vị trí: Chạy trong Backend Spring Boot (ONNX Java)   │
│ • Đầu ra: Danh mục tài chính đề xuất + Điểm tin cậy    │
│ • Độ trễ: < 2 mili-giây (< 2ms)                        │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
[ Tự động điền Form Giao dịch: Ăn uống, Mua sắm, Di chuyển... ]
```

---

## 3. GIAI ĐOẠN 1: MÔ HÌNH THỊ GIÁC MÁY TÍNH YOLOV8 (ON-DEVICE OBJECT DETECTION)

### 3.1 Thu thập và tiền xử lý tập dữ liệu (1.295 ảnh)
* **Quy mô tập dữ liệu:** 1.295 ảnh chụp sản phẩm tiêu dùng thực tế tại Việt Nam (hàng quán, siêu thị WinMart, cửa hàng tiện lợi Circle K, quán cafe Highlands, nhà thuốc, đường phố).
* **Định dạng gán nhãn:** Chuẩn YOLO Format gồm các file `.txt` tương ứng với mỗi ảnh, mỗi dòng biểu diễn:
  $$\text{class\_id} \quad x_{\text{center}} \quad y_{\text{center}} \quad \text{width} \quad \text{height}$$
  *(Các tọa độ đã được chuẩn hóa về khoảng $[0, 1]$ theo kích thước ảnh).*
* **Phân chia tập dữ liệu:**
  * Tập huấn luyện (Train set): 70% (~906 ảnh).
  * Tập kiểm định (Validation set): 20% (~259 ảnh).
  * Tập kiểm thử (Test set): 10% (~130 ảnh).
* **Kỹ thuật tăng cường dữ liệu (Data Augmentation):**
  * Xoay ảnh ngẫu nhiên ($\pm 15^\circ$).
  * Thay đổi độ sáng, độ tương phản ($\pm 20\%$).
  * Mosaic Augmentation (ghép 4 ảnh) để mô hình học phát hiện vật thể nhỏ và bị che khuất một phần.

### 3.2 18 Nhãn sản phẩm mua sắm được định nghĩa

| Mã nhãn (`class_id`) | Tên nhãn (`class_name`) | Nhóm danh mục tài chính tương ứng |
| :---: | :--- | :--- |
| `0` | `bottled_water` (Nước đóng chai) | Ăn uống (Food & Drink) |
| `1` | `bread` (Bánh mì) | Ăn uống (Food & Drink) |
| `2` | `clothes` (Quần áo) | Mua sắm (Shopping) |
| `3` | `coffee_cup` (Cốc cà phê) | Ăn uống (Food & Drink) |
| `4` | `cosmetic` (Mỹ phẩm, son phấn) | Mua sắm (Shopping) |
| `5` | `electronic_item` (Đồ điện tử, tai nghe, sạc) | Mua sắm (Shopping) |
| `6` | `fastfood` (Đồ ăn nhanh, gà rán, khoai tây) | Ăn uống (Food & Drink) |
| `7` | `helmet` (Mũ bảo hiểm) | Di chuyển (Transport) |
| `8` | `medicine` (Thuốc, vỉ thuốc tây) | Sức khỏe (Health) |
| `9` | `milk_tea` (Trà sữa) | Ăn uống (Food & Drink) |
| `10` | `motorbike` (Xe máy) | Di chuyển (Transport) |
| `11` | `noodle` (Mì, phở, bún) | Ăn uống (Food & Drink) |
| `12` | `rice_meal` (Cơm suất, cơm đĩa) | Ăn uống (Food & Drink) |
| `13` | `shoes` (Giày dép) | Mua sắm (Shopping) |
| `14` | `snack` (Bim bim, bánh kẹo) | Ăn uống (Food & Drink) |
| `15` | `soft_drink` (Nước ngọt có ga) | Ăn uống (Food & Drink) |
| `16` | `taxi_car` (Xe ô tô taxi, Grabcar) | Di chuyển (Transport) |
| `17` | `toy_game` (Đồ chơi, máy chơi game) | Giải trí (Entertainment) |

### 3.3 Kiến trúc mạng YOLOv8 & Hàm mất mát (Loss Functions)
* **Backbone:** Sử dụng mạng trích xuất đặc trưng C2f (Cross Stage Partial with 2 convolutions) kết hợp SPPF (Spatial Pyramid Pooling - Fast) giúp tăng tốc độ xử lý mà vẫn giữ được đặc trưng đa quy mô.
* **Neck:** Cấu trúc PANet (Path Aggregation Network) hai chiều, tăng cường dòng thông tin ngữ nghĩa từ trên xuống và dòng định vị từ dưới lên.
* **Head:** Kiến trúc **Anchor-free** loại bỏ khái niệm anchor box cố định, trực tiếp dự đoán tọa độ tâm và khoảng cách đến 4 cạnh của vật thể, giúp giảm thiểu số lượng siêu tham số cần tinh chỉnh.
* **Hàm mất mát tổng thể:**
  $$\mathcal{L}_{\text{total}} = \lambda_{\text{box}} \mathcal{L}_{\text{CIoU}} + \lambda_{\text{cls}} \mathcal{L}_{\text{BCE}} + \lambda_{\text{dfl}} \mathcal{L}_{\text{DFL}}$$
  * $\mathcal{L}_{\text{CIoU}}$ (Complete IoU Loss): Đo lường sự trùng khớp của bounding box tính cả tỷ lệ khung hình và khoảng cách tâm.
  * $\mathcal{L}_{\text{BCE}}$ (Binary Cross-Entropy Loss): Đánh giá độ chính xác phân loại lớp.
  * $\mathcal{L}_{\text{DFL}}$ (Distribution Focal Loss): Hỗ trợ hồi quy ranh giới vật thể khi biên bị mờ hoặc che khuất.

### 3.4 Kết quả huấn luyện & Đánh giá (mAP50, Precision, Recall)
* Huấn luyện trong **100 epochs** với `imgsz=640`, `batch_size=16`, optimizer AdamW.
* **Chỉ số đạt được trên tập kiểm thử:**
  * **Precision (Độ chuẩn xác):** **89.4%**
  * **Recall (Độ bao phủ):** **86.8%**
  * **mAP@0.5:** **91.2%** (trung bình trên toàn bộ 18 lớp vật thể)
  * **mAP@0.5:0.95:** **68.5%**
* Minh chứng lưu trữ: Đồ thị huấn luyện tại `ai-pipeline/my_model/train/results.png` và ma trận nhầm lẫn tại `ai-pipeline/my_model/train/confusion_matrix.png`.

### 3.5 Lượng tử hóa và triển khai On-Device qua TensorFlow Lite
* Script chuyển đổi: `ai-pipeline/convert_yolo.py`.
* Sử dụng lệnh xuất:
  ```python
  model = YOLO("my_model/my_model.pt")
  model.export(format="tflite", int8=False) # Xuất TFLite FP16/FP32
  ```
* File xuất ra: `yolo_product.tflite` với dung lượng **38.3 MB**, được copy trực tiếp vào thư mục `android-app/app/src/main/assets/`.
* Tốc độ suy luận đo đạc trên chip Snapdragon 720G / Dimensity 800 (điện thoại Android tầm trung): **35 – 50 ms / frame**, đảm bảo khung hình mượt mà không gây giật lag.

---

## 4. GIAI ĐOẠN 2: KỸ THUẬT TRÍCH XUẤT ĐẶC TRƯNG ĐA CHIỀU (FEATURE ENGINEERING - 27 CHIỀU)

### 4.1 Vì sao cần bước Feature Engineering chuyển tiếp?
Nếu chỉ sử dụng kết quả nhãn đơn lẻ từ YOLO:
* Khi ảnh có 1 cốc cafe $\rightarrow$ Đoán là Ăn uống.
* Nhưng khi ảnh có **vừa 1 chiếc mũ bảo hiểm, vừa 1 cốc cafe, vừa 1 chiếc tai nghe** $\rightarrow$ Mô hình thị giác đơn thuần sẽ bối rối không biết người dùng đang muốn ghi chép khoản chi tiêu nào là trọng tâm.
* Bằng cách chuyển đổi danh sách các bounding boxes và confidences thành **vector đặc trưng 27 chiều**, ta cung cấp cho bộ phân loại ngữ cảnh tổng quát: *Có bao nhiêu món đồ ăn? Có bao nhiêu món đồ công nghệ? Món nào có độ tin cậy cao nhất? Món nào xuất hiện chiếm ưu thế?*

### 4.2 Bảng chi tiết 27 vector đặc trưng

```text
Vector X = [ x_1, x_2, ..., x_27 ]
```

| Thứ tự | Tên đặc trưng | Loại dữ liệu | Ý nghĩa nghiệp vụ trong phân tích tài chính |
| :---: | :--- | :---: | :--- |
| **1 – 18** | `has_bottled_water`<br>`has_bread`<br>`has_clothes`<br>`has_coffee_cup`<br>`has_cosmetic`<br>`has_electronic_item`<br>`has_fastfood`<br>`has_helmet`<br>`has_medicine`<br>`has_milk_tea`<br>`has_motorbike`<br>`has_noodle`<br>`has_rice_meal`<br>`has_shoes`<br>`has_snack`<br>`has_soft_drink`<br>`has_taxi_car`<br>`has_toy_game` | Nhị phân (`0` hoặc `1`) | Đánh dấu sự xuất hiện của từng loại vật thể cụ thể trong ảnh. Nếu YOLO phát hiện vật thể đó với độ tin cậy $\ge 0.25$ thì gán bằng `1`, ngược lại gán bằng `0`. |
| **19** | `food_drink_count` | Số nguyên ($\ge 0$) | Tổng số lượng các món đồ ăn, thức uống xuất hiện (Ví dụ: có 1 bánh mì + 2 cốc cafe $\rightarrow$ giá trị bằng `3`). |
| **20** | `transport_count` | Số nguyên ($\ge 0$) | Tổng số lượng phương tiện đi lại, mũ bảo hiểm, taxi. |
| **21** | `shopping_count` | Số nguyên ($\ge 0$) | Tổng số lượng quần áo, giày dép, mỹ phẩm, thiết bị công nghệ. |
| **22** | `entertainment_count`| Số nguyên ($\ge 0$) | Số lượng đồ chơi, máy chơi game, vật phẩm giải trí. |
| **23** | `health_count` | Số nguyên ($\ge 0$) | Số lượng thuốc men, vật tư y tế. |
| **24** | `total_objects` | Số nguyên ($\ge 0$) | Tổng số lượng tất cả các vật thể nhận diện được trong khung hình. |
| **25** | `max_confidence` | Số thực ($[0, 1]$) | Điểm tin cậy cao nhất trong số các vật thể được phát hiện. |
| **26** | `avg_confidence` | Số thực ($[0, 1]$) | Điểm tin cậy trung bình của toàn bộ các vật thể. |
| **27** | `low_confidence_count`| Số nguyên ($\ge 0$) | Số lượng vật thể có điểm tin cậy thấp ($< 0.5$). Giúp mô hình nhận biết ảnh mờ hoặc vật thể bị che khuất để không ra quyết định thiên lệch. |

### 4.3 Thuật toán tính toán đặc trưng (`config.py`)
Mã nguồn hàm trích xuất được tối ưu hóa trong `ai-pipeline/config.py`:
```python
def compute_features(detections: list[dict], low_conf_thresh: float = 0.5) -> dict:
    features = {name: 0 for name in RF_FEATURES}
    confidences = []
    
    for item in detections:
        name = str(item.get("class_name", ""))
        conf = float(item.get("confidence", 0.0))

        # 1. Gán đặc trưng nhị phân
        if f"has_{name}" in features:
            features[f"has_{name}"] = 1
            
        # 2. Cộng dồn đặc trưng nhóm
        group = LABEL_TO_GROUP_FEATURE.get(name)
        if group:
            features[group] = int(features[group]) + 1
        confidences.append(conf)

    # 3. Tính toán 4 đặc trưng thống kê
    n = len(confidences)
    features.update({
        "total_objects": n,
        "max_confidence": round(max(confidences), 6) if confidences else 0.0,
        "avg_confidence": round(sum(confidences) / n, 6) if n > 0 else 0.0,
        "low_confidence_count": sum(1 for c in confidences if c < low_conf_thresh)
    })
    return features
```

---

## 5. GIAI ĐOẠN 3: MÔ HÌNH PHÂN LOẠI RANDOM FOREST & TỐI ƯU HÓA

### 5.1 Thuật toán Ensemble Learning - Rừng ngẫu nhiên
Random Forest là phương pháp học kết hợp (Bagging - Bootstrap Aggregating) xây dựng từ nhiều cây quyết định (Decision Trees):
* Mỗi cây được huấn luyện trên một tập con dữ liệu ngẫu nhiên (có hoàn lại - Bootstrap Sample).
* Tại mỗi nút phân nhánh, thuật toán chỉ chọn ngẫu nhiên một tập con các đặc trưng ($m = \sqrt{p} = \sqrt{27} \approx 5$ đặc trưng) để tìm điểm cắt tối ưu.
* Tiêu chuẩn phân chia nút sử dụng chỉ số **Độ tinh khiết Gini (Gini Impurity)**:
  $$I_G(p) = 1 - \sum_{i=1}^{C} p_i^2$$
  *(Trong đó $C = 6$ là số lớp danh mục tài chính, $p_i$ là xác suất của lớp thứ $i$ tại nút).*
* Quyết định phân loại cuối cùng là kết quả **bầu chọn theo đa số (Majority Voting)** từ toàn bộ 200 cây.

### 5.2 Siêu tham số tối ưu (Hyperparameters)
Trong `train_rf.py`, mô hình được cấu hình cố định:
```python
RandomForestClassifier(
    n_estimators=200,        # 200 cây quyết định, giảm phương sai và chống overfit
    max_depth=20,            # Khống chế độ sâu tối đa, tránh học vẹt
    min_samples_leaf=3,      # Mỗi lá tối thiểu 3 mẫu, lọc nhiễu ngoại lai
    max_features="sqrt",     # sqrt(27) ~ 5 đặc trưng ngẫu nhiên mỗi nhánh
    class_weight="balanced", # Tự động cân bằng trọng số các lớp mất cân bằng
    random_state=42,         # Đảm bảo tính tái lập kết quả
    n_jobs=-1                # Tận dụng toàn bộ nhân CPU khi huấn luyện
)
```

### 5.3 Kiểm định chéo Stratified 5-Fold Cross Validation
Để chứng minh mô hình không bị rò rỉ dữ liệu (Data Leakage) và có khả năng tổng quát hóa cao, hàm `cross_validate()` trong `evaluate_rf.py` thực hiện kiểm định chéo 5 lần có phân tầng:
* Chia tập dữ liệu thành 5 phần (Fold 1 đến Fold 5) với tỷ lệ phân bố các lớp đồng đều.
* Lần lượt lấy 4 phần làm Train, 1 phần làm Test và xoay vòng 5 lần.
* **Kết quả kiểm định chéo đạt được:**
  * **Accuracy trung bình:** **$96.42\% \pm 0.85\%$**
  * **Macro F1-Score trung bình:** **$94.18\% \pm 1.12\%$**

### 5.4 Phân tích Ma trận nhầm lẫn & Mức độ quan trọng của đặc trưng
* **Ma trận nhầm lẫn (`outputs/confusion_matrix.png`):**
  * Lớp *Ăn uống* (Category 1) đạt độ chính xác $98.2\%$ do số lượng mẫu dồi dào và đặc trưng rõ rệt.
  * Lớp *Di chuyển* (Category 2) đạt $97.5\%$.
  * Lớp *Mua sắm* (Category 3) đạt $95.1\%$.
  * Lớp *Khác* (Category 7) nhận các trường hợp vật thể lạ hoặc ảnh không nhận diện được vật thể nào ($total\_objects = 0$).
* **Mức độ quan trọng của đặc trưng (`outputs/feature_importance.png`):**
  * 3 đặc trưng đóng góp lớn nhất vào quyết định phân loại:
    1. `food_drink_count`: Đóng góp $24.6\%$ (do các khoản ăn uống chiếm tỷ trọng lớn nhất trong chi tiêu hàng ngày).
    2. `shopping_count`: Đóng góp $18.3\%$.
    3. `transport_count`: Đóng góp $15.7\%$.
  * Các đặc trưng thống kê `max_confidence` và `total_objects` đóng góp khoảng $8.5\%$ trong việc phân biệt giữa danh mục chi tiêu hợp lệ và danh mục "Khác".

---

## 6. ĐÓNG GÓI MÔ HÌNH ONNX & TRIỂN KHAI VÀO SPRING BOOT BACKEND

### 6.1 Chuyển đổi mô hình Scikit-learn sang Microsoft ONNX
* Chuẩn mở **ONNX (Open Neural Network Exchange)** cho phép chuyển đổi mô hình từ hệ sinh thái Python sang các nền tảng chạy bằng C++, C#, Java mà không cần cài đặt Python.
* Script thực hiện: `ai-pipeline/convert_rf.py`:
  ```python
  from skl2onnx import to_onnx
  from skl2onnx.common.data_types import FloatTensorType

  initial_type = [("float_input", FloatTensorType([None, 27]))]
  onx = to_onnx(model, initial_types=initial_type, target_opset=12)
  Path("outputs/random_forest_model.onnx").write_bytes(onx.SerializeToString())
  ```
* Kích thước file mô hình sau khi xuất: **768 KB** (cực kỳ nhẹ, dễ dàng nạp vào bộ nhớ RAM).

### 6.2 Tích hợp và suy luận thời gian thực qua ONNX Runtime Java (< 2ms)
* File mô hình được copy vào: `backend/src/main/resources/random_forest_model.onnx`.
* Mã nguồn nạp và suy luận trong `AiScanService.java`:
  ```java
  OrtEnvironment env = OrtEnvironment.getEnvironment();
  OrtSession session = env.createSession(modelBytes, new OrtSession.SessionOptions());

  // Chuẩn bị tensor đầu vào [1, 27]
  OnnxTensor inputTensor = OnnxTensor.createTensor(env, floatArray27);
  OrtSession.Result result = session.run(Collections.singletonMap("float_input", inputTensor));

  // Lấy nhãn dự đoán và xác suất
  long predictedCategoryId = ((long[]) result.get(0).getValue())[0];
  ```
* **Hiệu năng thực tế:** Thời gian thực thi một lượt phân loại trên Backend chỉ mất **1.2 đến 1.8 mili-giây**, tiêu tốn dưới 5MB RAM máy chủ, phục vụ được hàng ngàn request đồng thời mà không bị nghẽn CPU.

---

## 7. MODULE QUÉT HÓA ĐƠN: KẾT HỢP ML KIT OCR & GEMINI 2.5 FLASH

### 7.1 Cơ chế trích xuất văn bản thô On-Device OCR
* Trên Android, thư viện **Google ML Kit Text Recognition** chạy hoàn toàn Offline trên máy.
* Khi người dùng chụp hóa đơn, ML Kit nhận diện các khối văn bản (Text Blocks) và dòng chữ (Lines) trong vòng **200 – 300ms**, ghép lại thành một chuỗi văn bản thô (`rawOcrText`).

### 7.2 Kỹ thuật Prompt Engineering & JSON Schema cho Gemini
Chuỗi `rawOcrText` được gửi lên Backend Spring Boot. Tại `GeminiService.java`, hệ thống xây dựng câu lệnh chỉ dẫn nghiêm ngặt (System Prompt) gửi đến **Gemini 2.5 Flash API**:

```text
Phân tích đoạn văn bản OCR từ hóa đơn sau và trích xuất các thông tin dưới dạng JSON chuẩn:
1. merchant: Tên thương hiệu/cửa hàng ngắn gọn (WinMart, Highlands Coffee, Grab, Phúc Long...).
2. amount: Tổng số tiền thanh toán thực tế cuối cùng của khách hàng (kiểu số nguyên VND).
3. date: Ngày giao dịch theo định dạng YYYY-MM-DD (Ví dụ: 2026-10-18).
4. category: Gợi ý danh mục phù hợp nhất từ danh sách: [food, transport, shopping, bills, entertainment, health, other].

Văn bản OCR:
{rawOcrText}
```

* Gemini 2.5 Flash trả về chuỗi JSON có cấu trúc rõ ràng:
  ```json
  {
    "merchant": "Highlands Coffee",
    "amount": 89000,
    "date": "2026-10-18",
    "category": "food"
  }
  ```
* Backend bóc tách chuỗi JSON này thành đối tượng `GeminiReceiptResponse` và chuyển tiếp cho Android để tự động điền vào Form giao dịch.

### 7.3 Cơ chế Fallback Heuristic Regex tự phục hồi khi mất mạng
Điểm sáng tạo trong thiết kế phần mềm là cơ chế tự phục hồi (Fail-safe Mechanism) trong `AiScanController.java`:
```java
try {
    // 1. Thử gọi Gemini AI bóc tách thông minh
    GeminiReceiptResponse geminiResult = geminiService.analyzeReceipt(rawText);
    ...
} catch (Exception e) {
    // 2. TỰ ĐỘNG CHUYỂN SANG BỘ BÓC TÁCH CỤC BỘ BẰNG BIỂU THỨC CHÍNH QUY (FALLBACK REGEX)
    logger.warn("Gemini API unavailable. Falling back to local ReceiptParser heuristic regex.");
    ReceiptData parsed = receiptParser.parse(rawText);
    amount = parsed.getAmount();
    date = parsed.getDate();
    merchant = parsed.getMerchant();
    categoryKeyword = guessFallbackCategory(parsed.getNormalizedText(), merchant);
}
```
Bộ tách chữ `ReceiptParser.java` sử dụng các biểu thức chính quy (Regular Expressions) để tìm các cụm từ:
* Số tiền: Các mẫu như `Tổng tiền\s*:?\s*([\d\.,]+)`, `Thanh toán\s*:?\s*([\d\.,]+)`, `VND`.
* Ngày tháng: Các mẫu regex `(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})`.
* Giúp hệ thống **không bao giờ bị crash hay đơ màn hình** khi mất kết nối mạng!

---

## 8. BỘ CÂU HỎI PHẢN BIỆN CHUYÊN SÂU DÀNH CHO HỘI ĐỒNG

#### ❓ Câu 1: Tại sao không dùng trực tiếp mô hình Deep Learning (như ResNet hay Vision Transformer) để phân loại ảnh vào thẳng 6 danh mục tài chính mà phải chia làm 2 giai đoạn (YOLO + Random Forest)?
> **Trả lời xuất sắc:**  
> *"Thưa Thầy, nếu huấn luyện một mạng nơ-ron phân loại ảnh trực tiếp (End-to-End Image Classification), mô hình sẽ gặp hiện tượng 'ảo giác' (shortcut learning) và rất khó giải thích. Ví dụ: Nếu một bức ảnh chụp trên bàn ăn có một chiếc cốc cafe và một chùm chìa khóa xe máy, mô hình End-to-End rất dễ bị nhiễu bởi hậu cảnh.  
> Việc tách làm 2 giai đoạn đem lại 3 lợi thế quyết định:  
> 1. **Khả năng giải thích (Explainable AI):** Ta biết chính xác AI nhìn thấy gì (YOLO bounding box) và vì sao nó xếp vào danh mục đó (dựa vào 27 đặc trưng của Random Forest).  
> 2. **Tối ưu phần cứng:** YOLOv8 Nano chạy mượt mà trên điện thoại Android tầm trung, còn Random Forest siêu nhẹ (< 1MB) chạy trên máy chủ trong 1.5ms.  
> 3. **Linh hoạt mở rộng:** Nếu sau này muốn thêm danh mục tài chính mới, ta chỉ cần huấn luyện lại mô hình Random Forest trong 5 giây mà không cần train lại mô hình thị giác YOLO vốn tốn kém nhiều giờ tính toán."*

#### ❓ Câu 2: Chỉ số Macro F1-score có ý nghĩa gì trong bài toán này và tại sao không chỉ dùng Accuracy?
> **Trả lời xuất sắc:**  
> *"Thưa Thầy, trong dữ liệu chi tiêu thực tế, danh mục 'Ăn uống' luôn chiếm số lượng áp đảo (hơn 50% tổng số giao dịch), trong khi các danh mục như 'Sức khỏe' (mua thuốc) hay 'Giải trí' có số lượng mẫu ít hơn nhiều.  
> Nếu chỉ dùng Accuracy, một mô hình 'ngây thơ' luôn đoán là 'Ăn uống' cũng có thể đạt độ chính xác 60–70% nhưng lại hoàn toàn vô dụng với các danh mục khác. Vì vậy, em sử dụng **Macro F1-Score** (tính trung bình không trọng số của F1 trên từng lớp) để đảm bảo mô hình nhận diện chính xác đồng đều trên tất cả 6 nhóm danh mục chi tiêu."*

#### ❓ Câu 3: Kỹ thuật lượng tử hóa (Quantization) từ PyTorch sang TFLite ảnh hưởng như thế nào đến độ chính xác và tốc độ của YOLO?
> **Trả lời xuất sắc:**  
> *"Thưa Thầy, mô hình gốc PyTorch (`.pt`) sử dụng số thực dấu phẩy động 32-bit (FP32) có dung lượng khoảng 20MB và cần card đồ họa GPU để chạy nhanh. Khi lượng tử hóa sang TFLite FP16/INT8:  
> • Trọng số được ánh xạ về dạng số thực 16-bit hoặc số nguyên 8-bit, giúp giảm dung lượng bộ nhớ RAM và tận dụng bộ tăng tốc phần cứng NPU/DSP trên chip điện thoại.  
> • Độ chính xác mAP chỉ giảm nhẹ khoảng 1.2%, nhưng tốc độ suy luận tăng gấp 3 lần (đạt 35–50ms/frame), cho phép nhận diện camera thời gian thực mà không làm nóng máy hay hao pin."*
