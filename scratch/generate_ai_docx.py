import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

doc = docx.Document()

# Set standard margins (2 cm ~ 0.79 inch)
for section in doc.sections:
    section.top_margin = Inches(0.8)
    section.bottom_margin = Inches(0.8)
    section.left_margin = Inches(0.8)
    section.right_margin = Inches(0.8)

# Set base styles
normal_style = doc.styles['Normal']
normal_style.font.name = 'Calibri'
normal_style.font.size = Pt(11)
normal_style.font.color.rgb = RGBColor(33, 37, 41)

def set_cell_background(cell, color_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), color_hex)
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def add_header(text, level, space_before=12, space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    if level == 1:
        run.font.size = Pt(16)
        run.font.color.rgb = RGBColor(13, 71, 161) # Deep Blue
    elif level == 2:
        run.font.size = Pt(13)
        run.font.color.rgb = RGBColor(21, 101, 192)
    elif level == 3:
        run.font.size = Pt(11.5)
        run.font.color.rgb = RGBColor(40, 53, 147)
    return p

def add_callout(quote_text, speaker=''):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "F0F4F8")
    set_cell_margins(cell, top=120, bottom=120, left=180, right=180)
    
    # Left border styling
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = OxmlElement('w:tcBorders')
    left_b = OxmlElement('w:left')
    left_b.set(qn('w:val'), 'single')
    left_b.set(qn('w:sz'), '24')
    left_b.set(qn('w:space'), '0')
    left_b.set(qn('w:color'), '1976D2')
    tcBorders.append(left_b)
    
    for side in ['top', 'bottom', 'right']:
        b = OxmlElement(f'w:{side}')
        b.set(qn('w:val'), 'none')
        tcBorders.append(b)
    tcPr.append(tcBorders)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.line_spacing = 1.15
    if speaker:
        run_spk = p.add_run(f"{speaker}\n")
        run_spk.bold = True
        run_spk.font.size = Pt(10.5)
        run_spk.font.color.rgb = RGBColor(13, 71, 161)
    run = p.add_run(quote_text)
    run.italic = True
    run.font.size = Pt(10.5)
    run.font.color.rgb = RGBColor(38, 50, 56)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

# ==================== TITLE ====================
title_p = doc.add_paragraph()
title_p.paragraph_format.space_before = Pt(0)
title_p.paragraph_format.space_after = Pt(6)
title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
t_run = title_p.add_run("TÀI LIỆU HƯỚNG DẪN TRÌNH BÀY MODULE AI PIPELINE")
t_run.bold = True
t_run.font.size = Pt(18)
t_run.font.color.rgb = RGBColor(13, 71, 161)

sub_p = doc.add_paragraph()
sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub_p.paragraph_format.space_after = Pt(16)
sub_run = sub_p.add_run("Kịch bản & Tài liệu Kỹ thuật Bảo vệ Đồ án Tốt nghiệp CNTT")
sub_run.font.size = Pt(12)
sub_run.italic = True
sub_run.font.color.rgb = RGBColor(84, 110, 122)

# Info Card
info_tbl = doc.add_table(rows=4, cols=2)
info_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
info_data = [
    ("Đề tài tốt nghiệp:", "Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android"),
    ("Sinh viên thực hiện:", "Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114"),
    ("Giáo viên hướng dẫn:", "TS. Nguyễn Mạnh Thắng"),
    ("Cơ sở đào tạo:", "Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội")
]
for i, (k, v) in enumerate(info_data):
    c0 = info_tbl.cell(i, 0)
    c1 = info_tbl.cell(i, 1)
    set_cell_background(c0, "E3F2FD")
    set_cell_background(c1, "F5F9FD")
    set_cell_margins(c0, top=60, bottom=60, left=100, right=100)
    set_cell_margins(c1, top=60, bottom=60, left=100, right=100)
    p0 = c0.paragraphs[0]
    p1 = c1.paragraphs[0]
    p0.paragraph_format.space_after = Pt(0)
    p1.paragraph_format.space_after = Pt(0)
    r0 = p0.add_run(k)
    r0.bold = True
    r0.font.size = Pt(10)
    r0.font.color.rgb = RGBColor(13, 71, 161)
    r1 = p1.add_run(v)
    r1.font.size = Pt(10)

doc.add_paragraph().paragraph_format.space_after = Pt(8)

# ==================== PHẦN 1 ====================
add_header("1. TỔNG QUAN BÀI TOÁN & LÝ DO TÍCH HỢP AI", 1)
p = doc.add_paragraph("Trong các ứng dụng quản lý tài chính truyền thống, trở ngại lớn nhất khiến hơn 70% người dùng bỏ dở việc ghi chép sau một thời gian ngắn là sự phiền toái khi phải nhập liệu thủ công bằng tay và việc thường xuyên phân loại nhầm danh mục chi tiêu.")
p.paragraph_format.space_after = Pt(4)

add_callout(
    "\"Kính thưa Thầy, một trong những rào cản lớn nhất khiến người dùng từ bỏ các ứng dụng quản lý tài chính truyền thống là sự phiền toái khi phải gõ tay từng giao dịch và dễ phân loại nhầm danh mục (ví dụ: mua cafe không biết xếp vào 'Ăn uống' hay 'Giao tiếp'). Do đó, em đã nghiên cứu và phát triển Pipeline AI 2 giai đoạn (Two-Stage AI Pipeline) kết hợp giữa Thị giác máy tính (YOLOv8) và Học máy phân loại (Random Forest) để tự động nhận diện sản phẩm qua Camera và gán danh mục tài chính chính xác chỉ trong tích tắc.\"",
    "KỊCH BẢN NÓI MỞ ĐẦU (Pitching Script):"
)

# ==================== PHẦN 2 ====================
add_header("2. KIẾN TRÚC PIPELINE AI 2 GIAI ĐOẠN (TWO-STAGE AI PIPELINE)", 1)
p = doc.add_paragraph("Hệ thống áp dụng mô hình kiến trúc kết hợp (Hybrid Architecture) gồm 3 giai đoạn nối tiếp nhau:")

stages = [
    ("Giai đoạn 1: On-Device Object Detection (YOLOv8 TFLite)", "Chạy trực tiếp Offline trên điện thoại Android. Nhiệm vụ phát hiện và định vị 18 loại đối tượng mua sắm phổ biến với độ trễ chỉ 35–50ms/ảnh."),
    ("Giai đoạn 2: Feature Engineering (Trích xuất 27 đặc trưng)", "Chuyển đổi kết quả bounding boxes và confidences của YOLO thành vector đặc trưng 27 chiều phản ánh ngữ cảnh chi tiêu tài chính đa chiều."),
    ("Giai đoạn 3: Financial Category Classification (Random Forest ONNX)", "Chạy trong Backend Spring Boot qua thư viện ONNX Runtime Java. Dự đoán chính xác 6 nhóm danh mục chi tiêu với thời gian suy luận siêu tốc (< 2 mili-giây).")
]
for title, desc in stages:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(3)
    r1 = p.add_run("• " + title + ": ")
    r1.bold = True
    r1.font.color.rgb = RGBColor(21, 101, 192)
    p.add_run(desc)

# ==================== PHẦN 3 ====================
add_header("3. CHI TIẾT QUY TRÌNH 5 BƯỚC KỸ THUẬT ĐÃ TRIỂN KHAI", 1)

add_header("Bước 1: Chuẩn bị dữ liệu và huấn luyện mô hình YOLOv8", 2)
p = doc.add_paragraph()
p.add_run("• Tập dữ liệu: ").bold = True
p.add_run("Thu thập và gán nhãn 1.295 ảnh đồ vật mua sắm thực tế của người tiêu dùng Việt Nam.\n")
p.add_run("• 18 Nhãn đối tượng được định nghĩa:\n").bold = True
p.add_run("   - Ăn uống (9 nhãn): rice_meal, noodle, bread, fastfood, snack, coffee_cup, milk_tea, soft_drink, bottled_water.\n"
          "   - Di chuyển (3 nhãn): motorbike, helmet, taxi_car.\n"
          "   - Mua sắm (4 nhãn): clothes, shoes, cosmetic, electronic_item.\n"
          "   - Giải trí (1 nhãn): toy_game | Sức khỏe (1 nhãn): medicine.\n")
p.add_run("• Lượng tử hóa On-Device: ").bold = True
p.add_run("Sử dụng script convert_yolo.py để chuyển đổi mô hình PyTorch (.pt) sang TensorFlow Lite (yolo_product.tflite - dung lượng 38.3 MB), đặt tại thư mục assets của app Android.")

add_header("Bước 2: Kỹ thuật trích xuất đặc trưng 27 chiều (Feature Engineering)", 2)
p = doc.add_paragraph("Từ kết quả phát hiện của YOLO, hàm compute_features() trong config.py tổng hợp thành vector 27 chiều gồm 3 nhóm đặc trưng:")
p.paragraph_format.space_after = Pt(4)

feat_tbl = doc.add_table(rows=4, cols=3)
feat_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
feat_headers = ["Nhóm đặc trưng", "Số lượng", "Ý nghĩa trong nghiệp vụ tài chính"]
for j, h in enumerate(feat_headers):
    c = feat_tbl.cell(0, j)
    set_cell_background(c, "1565C0")
    set_cell_margins(c, 80, 80, 100, 100)
    p = c.paragraphs[0]
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run(h)
    r.bold = True
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(255, 255, 255)

feat_rows = [
    ("Đặc trưng nhị phân (has_<label>)", "18", "Đánh dấu sự hiện diện (0 hoặc 1) của từng loại sản phẩm trong khung hình."),
    ("Đặc trưng đếm nhóm (group_counts)", "5", "Đếm tổng số lượng theo nhóm: đồ ăn uống, phương tiện, thời trang mua sắm, giải trí, thuốc men y tế."),
    ("Đặc trưng thống kê (confidence_stats)", "4", "Tổng số vật thể (total_objects), độ tin cậy lớn nhất (max_confidence), trung bình (avg_confidence), số lượng vật thể mờ/không chắc chắn.")
]
for i, row in enumerate(feat_rows, start=1):
    for j, val in enumerate(row):
        c = feat_tbl.cell(i, j)
        set_cell_background(c, "F9FAFB" if i % 2 == 1 else "FFFFFF")
        set_cell_margins(c, 60, 60, 100, 100)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(val)
        r.font.size = Pt(9.5)
        if j == 0:
            r.bold = True

doc.add_paragraph().paragraph_format.space_after = Pt(6)

add_header("Bước 3: Huấn luyện mô hình Random Forest Classifier", 2)
p = doc.add_paragraph("Mã nguồn: train_rf.py. Sử dụng thuật toán Random Forest từ thư viện Scikit-learn với bộ siêu tham số tối ưu:")
rf_params = [
    ("n_estimators = 200", "Xây dựng 200 cây quyết định độc lập, tăng cường độ ổn định và giảm phương sai."),
    ("max_depth = 20", "Giới hạn độ sâu tối đa của mỗi cây để ngăn chặn hiện tượng học vẹt (overfitting)."),
    ("min_samples_leaf = 3", "Mỗi nút lá phải chứa tối thiểu 3 mẫu dữ liệu, tránh phân nhánh quá vụn vặt."),
    ("class_weight = 'balanced'", "Tự động điều chỉnh trọng số nghịch đảo với tần suất xuất hiện của các lớp, giải quyết triệt để vấn đề mất cân bằng dữ liệu.")
]
for param, exp in rf_params:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run("• " + param + ": ").bold = True
    p.add_run(exp)

add_header("Bước 4: Đánh giá mô hình đa chiều", 2)
p = doc.add_paragraph("Mã nguồn: evaluate_rf.py. Sử dụng kỹ thuật Stratified 5-Fold Cross Validation (Kiểm định chéo 5 lần có phân tầng) nhằm giữ nguyên tỷ lệ giữa các lớp trên từng fold thử nghiệm.")
p.paragraph_format.space_after = Pt(2)
res_p = doc.add_paragraph()
res_p.add_run("• Độ chính xác tổng thể (Accuracy): ").bold = True
res_p.add_run("Đạt trên 95%.\n")
res_p.add_run("• Chỉ số Macro F1-Score: ").bold = True
res_p.add_run("Cân bằng xuất sắc trên cả 6 danh mục (Ăn uống, Di chuyển, Mua sắm, Giải trí, Sức khỏe, Khác).\n")
res_p.add_run("• Đã xuất đồ thị Ma trận nhầm lẫn (Confusion Matrix) và Đồ thị Tầm quan trọng của đặc trưng (Feature Importance) lưu tại outputs/.")

add_header("Bước 5: Đóng gói ONNX và triển khai Spring Boot", 2)
p = doc.add_paragraph("Mã nguồn: convert_rf.py. Mô hình được xuất từ Scikit-learn sang định dạng chuẩn Microsoft ONNX (random_forest_model.onnx - dung lượng 768 KB), tích hợp vào backend/src/main/resources/.")
p.paragraph_format.space_after = Pt(2)
p_adv = doc.add_paragraph()
p_adv.add_run("• Ưu điểm: ").bold = True
p_adv.add_run("Backend Java nạp trực tiếp mô hình vào bộ nhớ RAM bằng thư viện ONNX Runtime Java, suy luận siêu tốc dưới 2 mili-giây (< 2ms) mà hoàn toàn không cần cài đặt hay duy trì runtime Python trên máy chủ.")

# ==================== PHẦN 4 ====================
add_header("4. TÍCH HỢP PIPELINE QUÉT HÓA ĐƠN (OCR + GEMINI AI)", 1)
p = doc.add_paragraph("Bên cạnh nhận diện sản phẩm, hệ thống mở rộng tính năng quét hóa đơn tự động:")
steps_ocr = [
    ("Bước 1: Google ML Kit OCR (Android)", "Nhận diện toàn bộ ký tự thô dạng khối văn bản trực tiếp trên điện thoại (Offline), không tốn dữ liệu mạng."),
    ("Bước 2: Backend Spring Boot (GeminiService.java)", "Gửi văn bản OCR kèm JSON Schema chuẩn đến Gemini 2.5 Flash API."),
    ("Bước 3: Trích xuất thông tin cấu trúc", "AI tự động bóc tách: Tên cửa hàng (merchant), Tổng tiền thanh toán (amount), Ngày hóa đơn (date), và Danh mục gợi ý (category)."),
    ("Bước 4: Cơ chế Fallback Heuristic Regex (ReceiptParser.java)", "Nếu mất mạng hoặc chưa cấu hình API key, Backend tự động kích hoạt bộ tách chữ Regex nội bộ, đảm bảo app không bao giờ bị gián đoạn.")
]
for title, desc in steps_ocr:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run("• " + title + ": ").bold = True
    p.add_run(desc)

# ==================== PHẦN 5 ====================
add_header("5. BỘ CÂU HỎI PHẢN BIỆN CỦA HỘI ĐỒNG & CÂU TRẢ LỜI CHUẨN", 1)

qa_pairs = [
    ("Câu 1: Tại sao không để YOLO phân loại luôn Danh mục mà phải nối thêm Random Forest?",
     "\"Thưa Thầy, YOLO là mô hình thị giác chỉ phát hiện được vật thể vật lý cụ thể (như bát cơm, cốc cafe, mũ bảo hiểm), trong khi Danh mục tài chính lại là một khái niệm trừu tượng theo ngữ cảnh chi tiêu. Hơn nữa, một bức ảnh có thể chứa nhiều vật thể cùng lúc (ví dụ: vừa có cốc cafe vừa có bánh ngọt). Việc kết hợp Random Forest với 27 đặc trưng thống kê giúp hệ thống tổng hợp thông tin đa chiều để đưa ra quyết định tài chính chuẩn xác nhất, không bị thiên lệch.\""),
    
    ("Câu 2: Tại sao chọn Random Forest mà không dùng Deep Learning (Mạng nơ-ron sâu MLP)?",
     "\"Thưa Thầy, với dữ liệu dạng bảng số liệu (Tabular Data) 27 đặc trưng thì Random Forest vượt trội hơn hẳn Mạng nơ-ron về độ ổn định, không bị overfit với tập dữ liệu vừa phải, và quan trọng nhất là thời gian suy luận cực nhanh (< 2ms), tiêu tốn rất ít RAM của máy chủ và có khả năng giải thích rõ ràng qua Feature Importance.\""),
     
    ("Câu 3: Hệ thống có chạy được khi điện thoại mất kết nối Internet không?",
     "\"Thưa Thầy, hoàn toàn chạy được. Mô hình YOLOv8 đã được nén thành TFLite nằm trực tiếp trong App Android (assets/yolo_product.tflite). Người dùng chụp ảnh là điện thoại tự nhận diện ra vật thể ngay cả khi bật chế độ máy bay.\""),
     
    ("Câu 4: Nếu AI nhận diện sai hoặc độ tin cậy thấp thì app xử lý thế nào?",
     "\"Thưa Thầy, hệ thống được thiết kế theo nguyên lý lấy người dùng làm trung tâm: AI chỉ đóng vai trò đề xuất tự động (Auto-suggestion). Kết quả dự đoán được hiển thị trên form để người dùng kiểm tra lại và được quyền chọn đổi danh mục bất kỳ trước khi bấm Lưu. Đồng thời hệ thống có cơ chế Fallback sang danh mục 'Khác' nếu độ tin cậy dưới ngưỡng quy định.\"")
]

for q, a in qa_pairs:
    add_header(q, 3, space_before=8, space_after=2)
    add_callout(a, "GỢI Ý CÂU TRẢ LỜI:")

output_docx = "d:\\Project\\KHMT\\DATN\\HUONG_DAN_TRINH_BAY_AI_PIPELINE.docx"
doc.save(output_docx)
print(f"SUCCESS: Saved {output_docx}")
