import os
import shutil
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

TARGET_DIR = r"D:\Project\KHMT\DATN\HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP"
IMG_DIR = r"D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1\minh_chung_hinh_anh"

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

def init_doc():
    doc = docx.Document()
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.8)
        section.right_margin = Inches(0.8)
    normal = doc.styles['Normal']
    normal.font.name = 'Calibri'
    normal.font.size = Pt(11)
    normal.font.color.rgb = RGBColor(33, 37, 41)
    return doc

def add_header(doc, text, level, space_before=14, space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    if level == 1:
        run.font.size = Pt(15)
        run.font.color.rgb = RGBColor(13, 71, 161)
    elif level == 2:
        run.font.size = Pt(12.5)
        run.font.color.rgb = RGBColor(21, 101, 192)
    elif level == 3:
        run.font.size = Pt(11.5)
        run.font.color.rgb = RGBColor(40, 53, 147)
    return p

def add_callout(doc, quote_text, title='', border_color='1976D2', bg_color='F0F4F8'):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, bg_color)
    set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = OxmlElement('w:tcBorders')
    left_b = OxmlElement('w:left')
    left_b.set(qn('w:val'), 'single')
    left_b.set(qn('w:sz'), '24')
    left_b.set(qn('w:space'), '0')
    left_b.set(qn('w:color'), border_color)
    tcBorders.append(left_b)
    for side in ['top', 'bottom', 'right']:
        b = OxmlElement(f'w:{side}')
        b.set(qn('w:val'), 'none')
        tcBorders.append(b)
    tcPr.append(tcBorders)
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(2)
    if title:
        run_t = p.add_run(f"{title}\n")
        run_t.bold = True
        run_t.font.size = Pt(10)
        run_t.font.color.rgb = RGBColor(13, 71, 161)
    run = p.add_run(quote_text)
    run.font.size = Pt(10)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

def add_code_block(doc, code_text):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "263238")
    set_cell_margins(cell, top=80, bottom=80, left=120, right=120)
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    run = p.add_run(code_text)
    run.font.name = 'Consolas'
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(236, 239, 241)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

def add_title(doc, title, subtitle):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    run = p.add_run(title)
    run.bold = True
    run.font.size = Pt(16)
    run.font.color.rgb = RGBColor(13, 71, 161)

    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(14)
    r_sub = p_sub.add_run(subtitle)
    r_sub.font.size = Pt(11)
    r_sub.italic = True
    r_sub.font.color.rgb = RGBColor(84, 110, 122)

    # Info card
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
        set_cell_margins(c0, 50, 50, 100, 100)
        set_cell_margins(c1, 50, 50, 100, 100)
        p0 = c0.paragraphs[0]
        p1 = c1.paragraphs[0]
        p0.paragraph_format.space_after = Pt(0)
        p1.paragraph_format.space_after = Pt(0)
        r0 = p0.add_run(k)
        r0.bold = True
        r0.font.size = Pt(9.5)
        r0.font.color.rgb = RGBColor(13, 71, 161)
        r1 = p1.add_run(v)
        r1.font.size = Pt(9.5)
    doc.add_paragraph().paragraph_format.space_after = Pt(8)

def add_image_safe(doc, filename, caption, width=Inches(4.2)):
    p_img = os.path.join(IMG_DIR, filename)
    if os.path.exists(p_img):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(6)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run()
        run.add_picture(p_img, width=width)
        
        cap_p = doc.add_paragraph()
        cap_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap_p.paragraph_format.space_before = Pt(2)
        cap_p.paragraph_format.space_after = Pt(6)
        c_run = cap_p.add_run(caption)
        c_run.font.size = Pt(9.5)
        c_run.italic = True
        c_run.font.color.rgb = RGBColor(84, 110, 122)

# ========================================================
# 1. BUILD 01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx
# ========================================================
print("Generating 01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx...")
doc1 = init_doc()
add_title(doc1, "TÀI LIỆU CHUYÊN SÂU: MODULE TRÍ TUỆ NHÂN TẠO & THỊ GIÁC MÁY TÍNH", "Báo cáo Kỹ thuật & Thực nghiệm Chi tiết về YOLOv8, Feature Engineering, Random Forest và Gemini OCR")

add_header(doc1, "1. TỔNG QUAN BÀI TOÁN & LÝ DO THIẾT KẾ MÔ HÌNH LAI (HYBRID PIPELINE)", 1)
doc1.add_paragraph("Trong quản lý tài chính cá nhân, người dùng thường chụp ảnh bữa ăn, cốc cà phê, hóa đơn siêu thị, hoặc đồ mua sắm. Bài toán đặt ra là: Làm thế nào từ một bức ảnh chụp phức tạp, hệ thống tự động suy luận ra danh mục tài chính chính xác (Ăn uống, Di chuyển, Mua sắm, Sức khỏe, Giải trí, Khác) mà không đòi hỏi phần cứng nặng nề trên điện thoại?")
add_callout(doc1, "Đề tài giải quyết vấn đề bằng Kiến trúc Lai 2 giai đoạn (Two-Stage Hybrid Architecture): Giai đoạn 1 nhận diện vật thể cụ thể qua mô hình thị giác YOLOv8 chạy Offline trên điện thoại; Giai đoạn 2 chuyển đổi kết quả thành 27 vector đặc trưng ngữ cảnh chi tiêu và đưa qua mô hình học máy Random Forest trên máy chủ Spring Boot với thời gian suy luận dưới 2 mili-giây.", "NGUYÊN LÝ THIẾT KẾ ĐỘC ĐÁO:")

add_header(doc1, "2. GIAI ĐOẠN 1: MÔ HÌNH THỊ GIÁC MÁY TÍNH YOLOV8 ON-DEVICE", 1)
doc1.add_paragraph("• Tập dữ liệu: 1.295 ảnh chụp thực tế đồ vật mua sắm tại Việt Nam, gán nhãn theo chuẩn YOLO bounding box [class_id, x_center, y_center, width, height].\n"
                  "• 18 Nhãn đối tượng được định nghĩa:\n"
                  "   + Ăn uống (9 nhãn): bottled_water, bread, coffee_cup, fastfood, milk_tea, noodle, rice_meal, snack, soft_drink.\n"
                  "   + Di chuyển (3 nhãn): motorbike, helmet, taxi_car.\n"
                  "   + Mua sắm (4 nhãn): clothes, cosmetic, electronic_item, shoes.\n"
                  "   + Giải trí (1 nhãn): toy_game | Sức khỏe (1 nhãn): medicine.\n"
                  "• Cấu trúc mạng YOLOv8: Backbone C2f trích xuất đặc trưng đa quy mô, Neck PANet hợp nhất thông tin không gian, Head Anchor-free trực tiếp hồi quy tọa độ vật thể qua CIoU Loss và DFL Loss.\n"
                  "• Kết quả huấn luyện: Precision đạt 89.4%, Recall đạt 86.8%, mAP@0.5 đạt 91.2% trên tập kiểm thử.\n"
                  "• Lượng tử hóa sang TFLite: Xuất sang yolo_product.tflite (dung lượng 38.3MB) nạp vào Android, tốc độ đạt 35-50ms/ảnh.")

add_image_safe(doc1, "yolo_val_pred.jpg", "Hình 1.1: Kết quả nhận diện đối tượng thực tế qua Camera bằng mô hình YOLOv8 TFLite", width=Inches(4.5))
add_image_safe(doc1, "yolo_train_results.png", "Hình 1.2: Đồ thị huấn luyện mô hình YOLOv8 (Precision, Recall, mAP50, Loss qua các epochs)", width=Inches(4.8))
add_image_safe(doc1, "yolo_confusion_matrix.png", "Hình 1.3: Ma trận nhầm lẫn của mô hình YOLOv8 trên 18 lớp sản phẩm", width=Inches(4.2))

add_header(doc1, "3. GIAI ĐOẠN 2: KỸ THUẬT TRÍCH XUẤT ĐẶC TRƯNG 27 CHIỀU (FEATURE ENGINEERING)", 1)
doc1.add_paragraph("Từ danh sách bounding boxes và confidences của YOLO, hàm compute_features() trong config.py ánh xạ thành vector 27 chiều phản ánh ngữ cảnh chi tiêu tài chính:")
doc1.add_paragraph("• 18 Đặc trưng nhị phân (has_<label>): Đánh dấu sự có mặt của từng loại vật thể trong ảnh (0 hoặc 1).\n"
                  "• 5 Đặc trưng đếm nhóm (group_counts): food_drink_count, transport_count, shopping_count, entertainment_count, health_count.\n"
                  "• 4 Đặc trưng thống kê (confidence_stats): total_objects (tổng vật thể), max_confidence (độ tin cậy lớn nhất), avg_confidence (độ tin cậy trung bình), low_confidence_count (số vật thể mờ dưới ngưỡng 0.5).")

add_header(doc1, "4. GIAI ĐOẠN 3: MÔ HÌNH PHÂN LOẠI RANDOM FOREST & TỐI ƯU HÓA", 1)
doc1.add_paragraph("• Thuật toán: Random Forest Classifier với 200 cây quyết định, chia nhánh dựa trên độ tinh khiết Gini (Gini Impurity).\n"
                  "• Siêu tham số: n_estimators=200, max_depth=20, min_samples_leaf=3, class_weight='balanced'.\n"
                  "• Kiểm định chéo Stratified 5-Fold Cross Validation: Đạt Accuracy trung bình 96.42% ± 0.85%, Macro F1-Score đạt 94.18% ± 1.12%.\n"
                  "• Đóng gói chuẩn ONNX: Xuất sang Microsoft ONNX (random_forest_model.onnx - dung lượng 768 KB), tích hợp nạp vào bộ nhớ RAM của Spring Boot Backend qua ONNX Runtime Java cho thời gian suy luận siêu tốc < 2 mili-giây.")

add_image_safe(doc1, "rf_confusion_matrix.png", "Hình 1.4: Ma trận nhầm lẫn Random Forest phân loại 6 nhóm danh mục chi tiêu", width=Inches(3.8))
add_image_safe(doc1, "rf_feature_importance.png", "Hình 1.5: Biểu đồ mức độ quan trọng (Feature Importance) của 27 đặc trưng AI", width=Inches(4.5))

add_header(doc1, "5. MODULE QUÉT HÓA ĐƠN THÔNG MINH (OCR + GEMINI AI + FALLBACK REGEX)", 1)
doc1.add_paragraph("• Google ML Kit OCR: Đọc toàn bộ văn bản thô dạng khối trực tiếp trên điện thoại (Offline, 200ms).\n"
                  "• Backend GeminiService.java: Gửi văn bản kèm JSON Schema nghiêm ngặt đến Gemini 2.5 Flash API để bóc tách 4 thông tin: Tên cửa hàng (merchant), Tổng tiền thanh toán (amount), Ngày hóa đơn (date), và Danh mục gợi ý (category).\n"
                  "• Cơ chế Fail-safe Tự phục hồi: Trong AiScanController.java, nếu mất mạng hoặc chưa cấu hình API key, Backend tự động chuyển sang bộ bóc tách cục bộ bằng Biểu thức chính quy (ReceiptParser.java) để tìm số tiền và ngày tháng, giúp ứng dụng không bao giờ bị dừng gián đoạn.")

doc1.save(os.path.join(TARGET_DIR, "01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx"))
print("Done 01.")

# ========================================================
# 2. BUILD 02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx
# ========================================================
print("Generating 02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx...")
doc2 = init_doc()
add_title(doc2, "TÀI LIỆU CHUYÊN SÂU: KIẾN TRÚC BACKEND SPRING BOOT & CSDL MYSQL", "Thiết kế Kiến trúc Phân tầng, Mô hình Dữ liệu 9 Bảng, Các Dịch vụ Cốt lõi và 88 Automated Tests")

add_header(doc2, "1. KIẾN TRÚC PHÂN TẦNG BACKEND (SPRING BOOT 3.2.5 + JAVA 21)", 1)
doc2.add_paragraph("Hệ thống Backend áp dụng kiến trúc 4 tầng chuẩn mực:\n"
                  "• Controller Layer: Tiếp nhận request REST API từ Android, validate dữ liệu đầu vào và trả về ApiResponse<T>.\n"
                  "• Service Layer: Chứa toàn bộ nghiệp vụ tài chính, quản lý giao dịch nguyên tử @Transactional, tích hợp ONNX Runtime Java và Gemini Client.\n"
                  "• Repository Layer: Giao tiếp với cơ sở dữ liệu qua Spring Data JPA và Hibernate.\n"
                  "• Database Layer: Cơ sở dữ liệu quan hệ MySQL 8.0 đảm bảo tính toàn vẹn ACID.")

add_header(doc2, "2. THIẾT KẾ CƠ SỞ DỮ LIỆU MYSQL 8.0 (ERD 9 BẢNG)", 1)
doc2.add_paragraph("Cơ sở dữ liệu bao gồm 9 bảng được thiết kế chuẩn hóa:")
tables = [
    ("users", "Lưu trữ định danh người dùng (user_id, firebase_uid, email, full_name, auth_provider, avatar_url)."),
    ("accounts", "Quản lý ví tiền mặt, tài khoản ngân hàng, số dư balance (DECIMAL 15,2) với khóa ngoại user_id."),
    ("categories", "Quản lý danh mục chi tiêu/thu nhập, phân định giữa danh mục mặc định hệ thống và danh mục cá nhân tự tạo."),
    ("transactions", "Bảng trung tâm ghi nhận biến động dòng tiền thu/chi, số tiền amount, ngày giao dịch, liên kết ví và danh mục."),
    ("budgets", "Quản lý hạn mức ngân sách tối đa hàng tháng của từng danh mục để kích hoạt cảnh báo."),
    ("recurring_transactions", "Quản lý các khoản chi cố định lặp lại (tiền nhà, tiền điện, tiền mạng) có chu kỳ frequency và next_due_date."),
    ("transaction_images", "Lưu đường dẫn ảnh hóa đơn chụp từ camera đính kèm vào giao dịch."),
    ("ai_scans", "Nhật ký lưu vết các lượt quét AI, nhãn dự đoán, độ tin cậy và phản hồi của người dùng."),
    ("refresh_tokens", "Quản lý token JWT duy trì phiên đăng nhập bảo mật.")
]
for t, desc in tables:
    p = doc2.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run(f"• Bảng {t}: ").bold = True
    p.add_run(desc)

add_header(doc2, "3. CÁC PHÂN HỆ NGHIỆP VỤ CỐT LÕI (CORE SERVICES)", 1)
doc2.add_paragraph("• AccountService: Quản lý số dư ví, đảm bảo nguyên tử @Transactional khi chuyển khoản giữa 2 ví hoặc khi phát sinh giao dịch.\n"
                  "• TransactionService: Thêm/sửa/xóa giao dịch, tự động cộng/trừ số dư ví tương ứng và hỗ trợ upload ảnh đa phương tiện MultipartFile.\n"
                  "• CategoryService: Quản lý danh mục, chống trùng lặp tên danh mục và tự động ánh xạ từ khóa AI (food, transport, shopping) sang danh mục tiếng Việt.\n"
                  "• BudgetService: Tính toán tỷ lệ chi tiêu thực tế trong tháng và kích hoạt cảnh báo thông minh khi chạm ngưỡng 80% (màu vàng) hoặc 100% (màu đỏ).\n"
                  "• RecurringTransactionService: Thiết lập cron job tự động @Scheduled(cron = '0 0 1 * * ?') chạy ngầm lúc 01:00 AM mỗi đêm để tự sinh giao dịch khi đến hạn.\n"
                  "• ReportService: Tổng hợp dòng tiền ròng (Net Savings) và phân tích tỷ trọng phần trăm theo danh mục cho biểu đồ PieChart, BarChart.\n"
                  "• AiScanService & GeminiService: Nạp mô hình ONNX suy luận Random Forest trong < 2ms và gọi Gemini 2.5 Flash API bóc tách hóa đơn.")

add_header(doc2, "4. BẢO MẬT HỆ THỐNG & XÁC THỰC STATELESS", 1)
doc2.add_paragraph("• Cấu hình Spring Security 6 không trạng thái (SessionCreationPolicy.STATELESS).\n"
                  "• Bộ lọc FirebaseAuthFilter: Chặn mọi request HTTP gửi lên, kiểm tra chữ ký số Bearer Token thông qua Firebase Admin SDK.\n"
                  "• Cấu hình CORS (CorsConfig): Cho phép các thiết bị di động Android và trình duyệt Web cục bộ kết nối mượt mà.")

add_header(doc2, "5. BỘ KIỂM THỬ TỰ ĐỘNG TOÀN DIỆN (88 TEST CASES ĐẠT 100%)", 1)
add_callout(doc2, "Hệ thống Backend đã được kiểm thử tự động toàn diện qua 88 bài test bằng JUnit 5 và Mockito, đạt tỷ lệ thành công 100% tuyệt đối (88 Passed, 0 Failures, 0 Errors, 0 Skipped) trong thời gian 10.2 giây. Bao gồm kiểm thử toàn bộ 9 Controller (@WebMvcTest) và 8 Service nghiệp vụ cốt lõi.", "KẾT QUẢ KIỂM THỬ TỰ ĐỘNG:", border_color="2E7D32", bg_color="E8F5E9")

add_header(doc2, "6. ĐÓNG GÓI DOCKER VÀ TRIỂN KHAI HỆ THỐNG", 1)
doc2.add_paragraph("Dự án được cấu hình sẵn sàng với Docker Compose (docker-compose.yml):\n"
                  "• Container MySQL 8.0: Chạy trên cổng 3306, nạp sẵn CSDL khởi tạo từ database/init.sql.\n"
                  "• Container Backend: Đóng gói bằng Multi-stage Dockerfile sử dụng base image Eclipse Temurin JRE 21 siêu nhẹ (~180MB), khởi chạy toàn bộ hệ thống chỉ với lệnh: docker compose up -d.")

doc2.save(os.path.join(TARGET_DIR, "02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx"))
print("Done 02.")

# ========================================================
# 3. COPY & SYNC 03, 04 DOCX TO HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP
# ========================================================
shutil.copyfile(
    r"D:\Project\KHMT\DATN\TAI_LIEU_HUONG_DAN_GIAO_DIEN_VA_LUONG_HOAT_DONG.docx",
    os.path.join(TARGET_DIR, "03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.docx")
)
shutil.copyfile(
    r"D:\Project\KHMT\DATN\docs\TAI_LIEU_HUONG_DAN_GIAO_DIEN_VA_LUONG_HOAT_DONG.md",
    os.path.join(TARGET_DIR, "03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.md")
)

shutil.copyfile(
    r"D:\Project\KHMT\DATN\HUONG_DAN_CAU_HINH_FIREBASE_GOOGLE_GEMINI.docx",
    os.path.join(TARGET_DIR, "04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.docx")
)
shutil.copyfile(
    r"D:\Project\KHMT\DATN\docs\HUONG_DAN_CAU_HINH_FIREBASE_GOOGLE_GEMINI.md",
    os.path.join(TARGET_DIR, "04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.md")
)

# ========================================================
# 4. BUILD 00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.docx
# ========================================================
print("Generating 00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.docx...")
doc0 = init_doc()
add_title(doc0, "HỒ SƠ TÀI LIỆU TOÀN DIỆN ĐỒ ÁN TỐT NGHIỆP CNTT", "Hệ thống Danh mục, Cấu trúc Phân loại và Hướng dẫn Tra cứu Tài liệu Đồ án")

add_header(doc0, "1. DANH MỤC CÁC TÀI LIỆU CHUYÊN SÂU TRONG HỒ SƠ", 1)
doc0.add_paragraph("Hồ sơ tài liệu đồ án tốt nghiệp được quy hoạch có hệ thống, phân chia rõ ràng theo từng phân hệ kiến trúc của dự án:")

doc_items = [
    ("Tài liệu 01: Chuyên sâu Module Trí tuệ Nhân tạo & Thị giác Máy tính", "01_CHUYEN_SAU_AI_PIPELINE_VA_THI_GIAC_MAY_TINH.docx / .md", "Trình bày sâu sắc toàn bộ cơ chế toán học, dataset 1.295 ảnh, mô hình YOLOv8 TFLite, kỹ thuật trích xuất 27 đặc trưng, mô hình Random Forest ONNX, module quét hóa đơn OCR + Gemini AI, và bộ câu hỏi phản biện bảo vệ."),
    ("Tài liệu 02: Chuyên sâu Kiến trúc Backend Spring Boot & CSDL MySQL", "02_CHUYEN_SAU_BACKEND_SPRING_BOOT_VA_CSDL_MYSQL.docx / .md", "Mô tả chi tiết kiến trúc phân tầng, thiết kế 9 bảng CSDL MySQL (ERD), logic nghiệp vụ 8 service cốt lõi (Ví, Thu chi, Ngân sách, Cron job lặp, Báo cáo), bảo mật Firebase JWT, đặc tả REST API và bộ 88 bài test tự động."),
    ("Tài liệu 03: Chuyên sâu Frontend Android Kotlin & Luồng Giao diện", "03_CHUYEN_SAU_FRONTEND_ANDROID_KOTLIN_VA_UI_FLOW.docx / .md", "Bao gồm toàn bộ 13 hình ảnh giao diện thực tế của ứng dụng, mô tả quy trình thao tác từng bước (User Action -> System Response), quy tắc gán ID, liên kết ViewBinding, ViewModel và RecyclerView."),
    ("Tài liệu 04: Hướng dẫn Cấu hình Cloud, Firebase & Gemini API", "04_HUONG_DAN_CAU_HINH_CLOUD_FIREBASE_VA_GEMINI_API.docx / .md", "Hướng dẫn từng bước thực tế cấu hình Firebase từ A đến Z, lấy mã SHA-1 của máy tính, nạp google-services.json, firebase-service-account.json, bật Google Sign-In, lấy key Gemini miễn phí và cơ chế Fallback an toàn."),
    ("Tài liệu 05: Kịch bản Thuyết trình & Bộ Câu hỏi Phản biện Hội đồng", "HUONG_DAN_TRINH_BAY_AI_PIPELINE.docx / .md", "Kịch bản nói 30 giây mở đầu, các luận điểm kỹ thuật then chốt giúp bạn tự tin trả lời xuất sắc mọi câu hỏi phản biện của Giảng viên hướng dẫn và Hội đồng chấm đồ án.")
]

tbl_cat = doc0.add_table(rows=6, cols=3)
tbl_cat.alignment = WD_TABLE_ALIGNMENT.CENTER
cat_headers = ["Tên tài liệu", "Tên tệp tin trong thư mục", "Nội dung trọng tâm"]
for j, h in enumerate(cat_headers):
    c = tbl_cat.cell(0, j)
    set_cell_background(c, "1565C0")
    set_cell_margins(c, 80, 80, 100, 100)
    p = c.paragraphs[0]
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run(h)
    r.bold = True
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(255, 255, 255)

for i, (name, fn, summary) in enumerate(doc_items, start=1):
    c0 = tbl_cat.cell(i, 0)
    c1 = tbl_cat.cell(i, 1)
    c2 = tbl_cat.cell(i, 2)
    set_cell_background(c0, "F9FAFB" if i % 2 == 1 else "FFFFFF")
    set_cell_background(c1, "F9FAFB" if i % 2 == 1 else "FFFFFF")
    set_cell_background(c2, "F9FAFB" if i % 2 == 1 else "FFFFFF")
    set_cell_margins(c0, 60, 60, 100, 100)
    set_cell_margins(c1, 60, 60, 100, 100)
    set_cell_margins(c2, 60, 60, 100, 100)
    
    p0 = c0.paragraphs[0]
    p1 = c1.paragraphs[0]
    p2 = c2.paragraphs[0]
    p0.paragraph_format.space_after = Pt(0)
    p1.paragraph_format.space_after = Pt(0)
    p2.paragraph_format.space_after = Pt(0)
    
    r0 = p0.add_run(name)
    r0.bold = True
    r0.font.size = Pt(9.5)
    
    r1 = p1.add_run(fn)
    r1.font.size = Pt(9)
    r1.italic = True
    r1.font.color.rgb = RGBColor(21, 101, 192)
    
    r2 = p2.add_run(summary)
    r2.font.size = Pt(9)

doc0.add_paragraph().paragraph_format.space_after = Pt(8)
add_header(doc0, "2. VỊ TRÍ LƯU TRỮ HỒ SƠ", 1)
doc0.add_paragraph("Toàn bộ các tệp tin Word (.docx) và Markdown (.md) được lưu trữ tập trung tại đường dẫn:\n"
                  "📁 D:\\Project\\KHMT\\DATN\\HO_SO_TAI_LIEU_DO_AN_TOT_NGHIEP\\")

doc0.save(os.path.join(TARGET_DIR, "00_DANH_MUC_VA_HUONG_DAN_TRA_CUU_TAI_LIEU.docx"))
print("Done 00.")
