from ultralytics import YOLO

def convert_yolo() -> None:
    print("Loading YOLO model from my_model/my_model.pt...")
    model = YOLO("my_model/my_model.pt")
    
    print("Exporting to TFLite format...")
    # Export to TFLite format (saved inside my_model/my_model_saved_model/)
    output_path = model.export(format="tflite")
    print(f"[SUCCESS] Export completed: {output_path}")

if __name__ == "__main__":
    convert_yolo()
