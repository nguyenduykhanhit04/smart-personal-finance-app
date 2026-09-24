from pathlib import Path
import joblib
from skl2onnx import to_onnx
from skl2onnx.common.data_types import FloatTensorType
from config import RF_FEATURES

def convert_rf() -> None:
    model_in = Path("outputs/random_forest_model.joblib")
    onnx_out = Path("outputs/random_forest_model.onnx")
    
    if not model_in.exists():
        print(f"[ERROR] Model file not found: {model_in}. Run train_rf.py first.")
        return
        
    print(f"Loading RandomForest model: {model_in}...")
    model = joblib.load(model_in)
    
    print("Converting to ONNX format...")
    initial_type = [("float_input", FloatTensorType([None, len(RF_FEATURES)]))]
    onx = to_onnx(model, initial_types=initial_type, target_opset=12)
    
    onnx_out.parent.mkdir(parents=True, exist_ok=True)
    onnx_out.write_bytes(onx.SerializeToString())
    print(f"[SUCCESS] Saved ONNX model to: {onnx_out}")

if __name__ == "__main__":
    convert_rf()
