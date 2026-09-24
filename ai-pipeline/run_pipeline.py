# CÂU LỆNH CHẠY PIPELINE QUÉT TOÀN BỘ 1295 ẢNH:
# python run_pipeline.py

import json
from pathlib import Path
import joblib
import pandas as pd
from PIL import Image
from ultralytics import YOLO
from openpyxl import Workbook
from openpyxl.drawing.image import Image as ExcelImage

from config import RF_FEATURES, YOLO_LABELS, CATEGORY_ID_TO_NAME, compute_features


def run_pipeline() -> None:
    img_dir = Path("dataset/images")
    out_dir = Path("outputs")
    rf_path = Path("outputs/random_forest_model.joblib")
    
    out_dir.mkdir(parents=True, exist_ok=True)
    ann_dir = out_dir / "annotated"
    ann_dir.mkdir(parents=True, exist_ok=True)
    
    # 1. Nạp các mô hình
    print("Loading models...")
    yolo = YOLO("my_model/my_model.pt")
    rf = joblib.load(rf_path) if rf_path.exists() else None
    
    # Tìm kiếm toàn bộ ảnh trong thư mục dataset/images
    image_paths = sorted([
        p for p in img_dir.rglob("*")
        if p.is_file() and p.suffix.lower() in {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
    ])
    print(f"Found {len(image_paths)} images. Processing...")

    records = []
    
    # 2. Xử lý từng ảnh
    for idx, p in enumerate(image_paths, 1):
        # Chạy YOLO
        results = yolo(p, conf=0.25, verbose=False)
        r = results[0]
        
        detections = []
        for box in r.boxes:
            cls_id = int(box.cls[0].item())
            conf = float(box.conf[0].item())
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            detections.append({
                "class_id": cls_id,
                "class_name": YOLO_LABELS[cls_id],
                "confidence": conf,
                "bbox": [x1, y1, x2, y2]
            })
        
        # Lưu ảnh đã vẽ bounding box
        ann_path = ann_dir / f"{p.stem}_bbox.jpg"
        Image.fromarray(r.plot()[..., ::-1]).save(ann_path)
        
        # Trích xuất đặc trưng
        feats = compute_features(detections)
        rec = {
            "image_name": p.name,
            "input_image_path": p.as_posix(),
            "annotated_image_path": ann_path.as_posix(),
            "detections": detections,
            "features": feats,
            "category_id": ""
        }
        
        # Dự đoán nhãn phân loại bằng Random Forest
        if rf:
            vec = [feats[f] for f in RF_FEATURES]
            pred = int(rf.predict(pd.DataFrame([vec], columns=RF_FEATURES))[0])
            rec["category_id"] = pred
            print(f"[{idx}/{len(image_paths)}] {p.name} -> Category: {pred} ({CATEGORY_ID_TO_NAME.get(pred)})")
        else:
            print(f"[{idx}/{len(image_paths)}] {p.name} -> (YOLO detected {len(detections)} objects)")
        
        records.append(rec)
        
    # 3. Lưu kết quả ra file JSON
    with open(out_dir / "review_records.json", "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
        
    # 4. Ghi trực tiếp ra file Excel kèm chèn ảnh
    wb = Workbook()
    ws = wb.active
    ws.title = "Review_Data"
    ws.append(["input_image", "bbox_output", "image_name", "json_detection"] + RF_FEATURES + ["category_id"])
    
    for idx, r in enumerate(records, start=2):
        row = ["", "", r["image_name"], json.dumps(r["detections"], ensure_ascii=False)]
        row.extend(r["features"].get(f, 0) for f in RF_FEATURES)
        row.append(r.get("category_id", ""))
        ws.append(row)
        ws.row_dimensions[idx].height = 88
        
        for col, path in [("A", r["input_image_path"]), ("B", r["annotated_image_path"])]:
            if Path(path).exists():
                ex_img = ExcelImage(str(path))
                ratio = min(110 / ex_img.width, 110 / ex_img.height, 1.0)
                ex_img.width, ex_img.height = int(ex_img.width * ratio), int(ex_img.height * ratio)
                ws.add_image(ex_img, f"{col}{idx}")
                
    wb.save(out_dir / "review_dataset.xlsx")
    print(f"\n[SUCCESS] Excel review sheet saved to: {out_dir}/review_dataset.xlsx")


if __name__ == "__main__":
    run_pipeline()
