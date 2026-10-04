# 🧠 AI Pipeline Module - Smart Personal Finance App

Module Trí tuệ Nhân tạo & Thị giác Máy tính (AI & Computer Vision) chịu trách nhiệm nhận diện mặt hàng mua sắm qua camera và tự động phân loại giao dịch vào danh mục tài chính phù hợp (Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Khác).

---

## 🏗️ Kiến trúc Pipeline AI 2 giai đoạn (Two-Stage AI Pipeline)

```text
[ Ảnh chụp từ Camera ]
         │
         ▼
┌──────────────────────────────────────────────┐
│ Giai đoạn 1: On-Device Object Detection      │
│ Model: YOLOv8 (TFLite trên Android)          │
│ Nhiệm vụ: Phát hiện và định vị 18 loại đồ vật │
└──────────────────────┬───────────────────────┘
                       │ Danh sách nhãn (class_name) + độ tin cậy (confidence)
                       ▼
┌──────────────────────────────────────────────┐
│ Giai đoạn 2: Feature Engineering             │
│ Trích xuất 27 đặc trưng tài chính đa chiều   │
│ (One-hot presence, Group counts, Stats)      │
└──────────────────────┬───────────────────────┘
                       │ Vector đặc trưng [27 chiều]
                       ▼
┌──────────────────────────────────────────────┐
│ Giai đoạn 3: Financial Category Inference    │
│ Model: Random Forest Classifier (ONNX Java)  │
│ Nhiệm vụ: Dự đoán danh mục chi tiêu & score  │
└──────────────────────┬───────────────────────┘
                       │
                       ▼
     [ Danh mục đề xuất: Ăn uống, Mua sắm... ]
```

---

## 🏷️ 18 Nhãn Sản phẩm & Phân nhóm Danh mục

Mô hình nhận diện **18 lớp đối tượng mua sắm phổ biến**:

| Nhóm danh mục | Mã danh mục (`category_id`) | Các nhãn đồ vật nhận diện (YOLO Labels) |
| :--- | :---: | :--- |
| **Ăn uống (Food & Drink)** | `1` | `bottled_water`, `bread`, `coffee_cup`, `fastfood`, `milk_tea`, `noodle`, `rice_meal`, `snack`, `soft_drink` |
| **Di chuyển (Transport)** | `2` | `helmet`, `motorbike`, `taxi_car` |
| **Mua sắm (Shopping)** | `3` | `clothes`, `cosmetic`, `electronic_item`, `shoes` |
| **Giải trí (Entertainment)**| `5` | `toy_game` |
| **Sức khỏe (Health)** | `6` | `medicine` |
| **Khác (Other)** | `7` | Các vật thể không thuộc nhóm trên |

---

## 🔬 Kỹ thuật Kỹ thuật Đặc trưng (Feature Engineering - 27 Features)

Từ kết quả phát hiện của YOLO, hàm `compute_features()` trong [config.py](file:///d:/Project/KHMT/KHMT&CNPM%20-%20NHOM%203/ai-pipeline/config.py) chuyển đổi thành vector 27 chiều:

1. **18 Đặc trưng Nhị phân (`has_<label>`):** Đánh dấu có sự xuất hiện của từng loại sản phẩm trong khung hình (Giá trị `0` hoặc `1`).
2. **5 Đặc trưng Nhóm đếm (`group_counts`):**
   - `food_drink_count`: Tổng số món đồ ăn/uống nhận diện được.
   - `transport_count`: Số lượng phương tiện/mũ bảo hiểm.
   - `shopping_count`: Số lượng quần áo, giày dép, mỹ phẩm, đồ công nghệ.
   - `entertainment_count`: Số lượng đồ chơi, thiết bị game.
   - `health_count`: Số lượng thuốc men, vật phẩm y tế.
3. **4 Đặc trưng Thống kê Độ tin cậy (`confidence_stats`):**
   - `total_objects`: Tổng số đồ vật nhận diện được trong ảnh.
   - `max_confidence`: Độ tin cậy cao nhất trong các vật thể.
   - `avg_confidence`: Độ tin cậy trung bình.
   - `low_confidence_count`: Số lượng vật thể có độ tin cậy thấp (`< 0.5`).

---

## 📁 Cấu trúc thư mục mã nguồn

```text
ai-pipeline/
├── config.py             # Cấu hình danh mục, 18 nhãn YOLO và hàm trích xuất 27 đặc trưng
├── train_rf.py           # Script huấn luyện Random Forest bằng Scikit-learn
├── evaluate_rf.py        # Đánh giá mô hình: Accuracy, Precision, Recall, F1, Confusion Matrix
├── convert_rf.py         # Chuyển đổi mô hình Random Forest sang định dạng chuẩn Microsoft ONNX
├── convert_yolo.py       # Chuyển đổi YOLOv8 PyTorch (.pt) sang TensorFlow Lite (.tflite)
├── run_pipeline.py       # Kịch bản chạy toàn diện (Train -> Evaluate -> Export ONNX)
├── my_model/             # Thư mục chứa trọng số mô hình YOLO đã huấn luyện
├── outputs/              # File xuất ra sau khi huấn luyện:
│   ├── random_forest_model.joblib  # Mô hình Python Scikit-learn
│   └── random_forest_model.onnx    # Mô hình ONNX sẵn sàng cho Spring Boot Backend
└── README.md
```

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy Pipeline

### 1. Cài đặt Môi trường Python
Khuyến nghị sử dụng Python 3.10 hoặc 3.11:

```bash
# Tạo môi trường ảo (tùy chọn)
python -m venv venv
# Kích hoạt trên Windows:
venv\Scripts\activate

# Cài đặt các thư viện phụ thuộc:
pip install ultralytics scikit-learn skl2onnx onnx onnxruntime pandas numpy joblib
```

### 2. Chạy toàn bộ Pipeline với 1 lệnh duy nhất
```bash
cd ai-pipeline
python run_pipeline.py
```

Lệnh này sẽ tự động thực hiện:
1. Nạp và tiền xử lý dữ liệu đặc trưng.
2. Huấn luyện mô hình Random Forest (`n_estimators=100`, `class_weight='balanced'`).
3. Đánh giá độ chính xác (Accuracy đạt trên **95%**, Macro F1-score cao trên toàn bộ các lớp chi tiêu).
4. Xuất mô hình ra định dạng chuẩn `random_forest_model.onnx`.

### 3. Đưa mô hình vào triển khai (Deployment)
- **Cho Backend Spring Boot:** Sao chép file `outputs/random_forest_model.onnx` vào:
  ```text
  backend/src/main/resources/random_forest_model.onnx
  ```
- **Cho Android Mobile App:** Sao chép file TFLite của YOLO vào thư mục assets của app:
  ```text
  android-app/app/src/main/assets/yolov8n_float32.tflite
  ```

---

## 📊 Đánh giá Hiệu năng Mô hình

- **Thời gian suy luận (Inference Latency):**
  - YOLO On-Device (TFLite): ~35-50ms / frame trên điện thoại Android tầm trung.
  - Random Forest (ONNX Java): **< 2ms** cho một lần phân loại trên Spring Boot Backend.
- **Tính ổn định:** Tích hợp cơ chế Fallback tự động sang Rule-based Engine nếu dữ liệu đầu vào có độ tin cậy thấp hoặc môi trường không tải được native runtime.
