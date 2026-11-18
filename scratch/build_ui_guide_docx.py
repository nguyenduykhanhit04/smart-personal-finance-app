import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

doc = docx.Document()

# Page Margins: 2cm ~ 0.79 inch
for section in doc.sections:
    section.top_margin = Inches(0.8)
    section.bottom_margin = Inches(0.8)
    section.left_margin = Inches(0.8)
    section.right_margin = Inches(0.8)

# Base Styles
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

def add_header(text, level, space_before=14, space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    if level == 1:
        run.font.size = Pt(15)
        run.font.color.rgb = RGBColor(13, 71, 161) # Navy Blue
    elif level == 2:
        run.font.size = Pt(12.5)
        run.font.color.rgb = RGBColor(21, 101, 192)
    elif level == 3:
        run.font.size = Pt(11.5)
        run.font.color.rgb = RGBColor(40, 53, 147)
    return p

def add_image_with_caption(img_path, caption, width=Inches(3.0)):
    if not os.path.exists(img_path):
        p_err = doc.add_paragraph(f"[Image not found: {img_path}]")
        p_err.runs[0].font.color.rgb = RGBColor(200, 0, 0)
        return
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    run = p.add_run()
    run.add_picture(img_path, width=width)
    
    cap_p = doc.add_paragraph()
    cap_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    cap_p.paragraph_format.space_before = Pt(2)
    cap_p.paragraph_format.space_after = Pt(8)
    c_run = cap_p.add_run(caption)
    c_run.font.size = Pt(9.5)
    c_run.italic = True
    c_run.font.color.rgb = RGBColor(84, 110, 122)

def add_action_box(title, action_desc, next_screen_desc):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "F0F4F8")
    set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
    
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
    
    r_t = p.add_run(f"👉 {title}\n")
    r_t.bold = True
    r_t.font.size = Pt(10.5)
    r_t.font.color.rgb = RGBColor(13, 71, 161)
    
    r_act = p.add_run("• Thao tác người dùng: ")
    r_act.bold = True
    r_act.font.size = Pt(10)
    r_act_val = p.add_run(f"{action_desc}\n")
    r_act_val.font.size = Pt(10)
    
    r_next = p.add_run("• Phản hồi hệ thống / Màn hình tiếp theo: ")
    r_next.bold = True
    r_next.font.size = Pt(10)
    r_next_val = p.add_run(f"{next_screen_desc}")
    r_next_val.font.size = Pt(10)
    
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

img_dir = r"D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1\minh_chung_hinh_anh"

# ==================== DOCUMENT HEADER ====================
title_p = doc.add_paragraph()
title_p.paragraph_format.space_before = Pt(0)
title_p.paragraph_format.space_after = Pt(4)
title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
t_run = title_p.add_run("TÀI LIỆU HƯỚNG DẪN GIAO DIỆN & LUỒNG HOẠT ĐỘNG")
t_run.bold = True
t_run.font.size = Pt(18)
t_run.font.color.rgb = RGBColor(13, 71, 161)

sub_p = doc.add_paragraph()
sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub_p.paragraph_format.space_after = Pt(14)
sub_run = sub_p.add_run("Mô tả chi tiết từng bước thao tác giao diện (Step-by-Step UI/UX Navigation Flow)")
sub_run.font.size = Pt(11.5)
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
    set_cell_margins(c0, top=50, bottom=50, left=100, right=100)
    set_cell_margins(c1, top=50, bottom=50, left=100, right=100)
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

# ==================== PHẦN 1 ====================
add_header("1. TỔNG QUAN PHONG CÁCH THIẾT KẾ & ĐIỀU HƯỚNG ỨNG DỤNG", 1)
p = doc.add_paragraph()
p.add_run("Ứng dụng quản lý tài chính cá nhân được thiết kế theo ngôn ngữ hiện đại **Obsidian Dark Theme** (tông nền xám đen sang trọng, tối ưu cho mắt và tiết kiệm pin màn hình OLED/AMOLED), kết hợp chuẩn **Material Design 3** của Google.\n"
          "Kiến trúc điều hướng cốt lõi bao gồm:\n"
          "• Thanh điều hướng dưới đáy (Bottom Navigation Bar): 4 màn hình chính (Trang chủ, Lịch sử, Báo cáo thống kê, Cài đặt cá nhân).\n"
          "• Nút hành động nổi ở trung tâm [ + ]: Mở thực đơn chọn thao tác nhập liệu thông minh (BottomSheet).\n"
          "• Cơ chế phản hồi tức thì (Real-time UI Update): Áp dụng mô hình MVVM (Model-View-ViewModel) kết hợp LiveData và StateFlow giúp số dư và danh sách giao dịch cập nhật ngay lập tức mà không cần vuốt tải lại trang.")

# ==================== PHẦN 2: LUỒNG ĐĂNG NHẬP ====================
add_header("2. LUỒNG 1: KHỞI ĐỘNG ỨNG DỤNG & XÁC THỰC NGƯỜI DÙNG", 1)
p = doc.add_paragraph("Khi người dùng mở ứng dụng lần đầu (hoặc sau khi đăng xuất), hệ thống sẽ khởi chạy `LoginActivity`.")

add_image_with_caption(
    os.path.join(img_dir, "login_screen.png"),
    "Hình 2.1: Màn hình Đăng nhập (LoginActivity) với các phương thức xác thực linh hoạt",
    width=Inches(2.8)
)

add_action_box(
    "Bước 1.1: Đăng nhập bằng Email và Mật khẩu",
    "Người dùng nhập Email và Mật khẩu vào ô tương ứng -> Nhấp nút 'Đăng nhập'.",
    "Hệ thống kiểm tra định dạng email và mật khẩu qua Firebase Auth. Nếu hợp lệ, hệ thống đồng bộ người dùng với Backend Spring Boot và chuyển tiếp vào Màn hình chính (MainActivity)."
)

add_action_box(
    "Bước 1.2: Đăng nhập 1 chạm bằng Google (Google Sign-In)",
    "Người dùng nhấp vào nút 'Đăng nhập với Google' (biểu tượng chữ G).",
    "Cửa sổ Google Play Services hiển thị danh sách tài khoản Gmail -> Người dùng chọn tài khoản -> Google cấp ID Token -> Firebase xác thực -> Tự động tạo tài khoản MySQL trên Backend và đưa vào Trang chủ."
)

add_action_box(
    "Bước 1.3: Đăng nhập nhanh chế độ Demo (Quick Demo Login)",
    "Người dùng nhấp nút 'Đăng nhập Demo' (Quick Demo).",
    "Hệ thống tự động kích hoạt phiên làm việc Demo đã tích hợp sẵn dữ liệu mẫu (ví tiền, giao dịch mẫu, biểu đồ thống kê) mà không cần nhập mật khẩu hay kết nối tài khoản Google. Rất tiện lợi khi bảo vệ đồ án trước Thầy cô."
)

add_action_box(
    "Bước 1.4: Cấu hình địa chỉ IP máy chủ Backend",
    "Ấn giữ (Long-click) vào Logo ứng dụng trên đỉnh màn hình đăng nhập.",
    "Hộp thoại 'Cấu hình IP Server' bật lên -> Cho phép người dùng hoặc Giảng viên nhập địa chỉ IP LAN cục bộ (Ví dụ: 192.168.1.5:8080) để ứng dụng kết nối trực tiếp với máy tính chạy Spring Boot."
)

# ==================== PHẦN 3: MÀN HÌNH CHÍNH ====================
add_header("3. LUỒNG 2: MÀN HÌNH TỔNG QUAN TÀI CHÍNH (DASHBOARD)", 1)
p = doc.add_paragraph("Sau khi xác thực thành công, màn hình chính `MainActivity` (Fragment Home) sẽ xuất hiện.")

add_image_with_caption(
    os.path.join(img_dir, "main_screen.png"),
    "Hình 3.1: Màn hình Trang chủ (Dashboard) hiển thị tổng số dư, thu/chi và giao dịch gần đây",
    width=Inches(2.8)
)

p_desc = doc.add_paragraph()
p_desc.add_run("Các khối chức năng trực quan trên Màn hình chính:\n").bold = True
p_desc.add_run("1. Thẻ Tổng tài sản (Total Balance Card): ").bold = True
p_desc.add_run("Nằm ở vị trí trung tâm trên cùng, hiển thị tổng số dư quy đổi VND của toàn bộ các ví đang quản lý (Tiền mặt, Ngân hàng, Ví điện tử).\n")
p_desc.add_run("2. Thẻ Dòng tiền trong tháng: ").bold = True
p_desc.add_run("Hiển thị 2 ô phân định rõ ràng:\n"
               "   • Thu nhập (Màu xanh lá): Tổng tiền đã thu trong tháng hiện tại.\n"
               "   • Chi tiêu (Màu đỏ cam): Tổng tiền đã chi tiêu trong tháng.\n")
p_desc.add_run("3. Danh sách Giao dịch gần đây (Recent Transactions): ").bold = True
p_desc.add_run("Hiển thị các giao dịch phát sinh mới nhất kèm theo icon danh mục (Ăn uống, Mua sắm, Di chuyển), ngày giờ và số tiền biến động.\n")
p_desc.add_run("4. Thanh điều hướng dưới đáy (Bottom Navigation): ").bold = True
p_desc.add_run("Giữ cố định giúp người dùng chuyển nhanh giữa các phân hệ: Trang chủ, Sổ giao dịch, Báo cáo và Cài đặt.")

# ==================== PHẦN 4: THÊM GIAO DỊCH ====================
add_header("4. LUỒNG 3: QUY TRÌNH THÊM GIAO DỊCH MỚI (STEP-BY-STEP)", 1)
p = doc.add_paragraph("Đây là luồng nghiệp vụ cốt lõi quan trọng nhất của ứng dụng, được tối ưu hóa theo 3 bước mạch lạc:")

add_action_box(
    "Bước 3.1: Nhấp nút hành động mở BottomSheet",
    "Từ bất kỳ màn hình nào, người dùng nhấp vào nút dấu cộng nổi [ + ] ở chính giữa thanh điều hướng dưới cùng.",
    "Bảng chọn phương thức nhập liệu (Modal BottomSheet) trượt mượt mà từ dưới màn hình lên (Hình 4.1)."
)

add_image_with_caption(
    os.path.join(img_dir, "screen_sheet.png"),
    "Hình 4.1: Bảng chọn phương thức nhập liệu (Nhập tay, Quét hóa đơn OCR, Nhận diện Camera AI)",
    width=Inches(2.8)
)

add_action_box(
    "Bước 3.2: Chọn phương thức 'Nhập thủ công'",
    "Người dùng nhấp vào mục 'Nhập giao dịch thủ công' trên bảng BottomSheet.",
    "Màn hình nhập liệu chi tiết (Hình 4.2) mở ra với đầy đủ các trường nghiệp vụ tài chính."
)

add_image_with_caption(
    os.path.join(img_dir, "screen_manual.png"),
    "Hình 4.2: Form nhập giao dịch thủ công (Chọn loại Chi/Thu, Danh mục, Ví, Số tiền, Ghi chú)",
    width=Inches(2.8)
)

p_form = doc.add_paragraph()
p_form.add_run("Các thao tác nhập liệu trên Form:\n").bold = True
p_form.add_run("• Chọn tab 'Chi phí' hoặc 'Thu nhập': ").bold = True
p_form.add_run("Màu sắc giao diện và danh mục tương ứng sẽ đổi tự động (Chi tiêu: tông đỏ; Thu nhập: tông xanh).\n")
p_form.add_run("• Nhập số tiền: ").bold = True
p_form.add_run("Gõ số tiền qua bàn phím số có tự động định dạng dấu phẩy hàng nghìn (Ví dụ: 150,000 đ).\n")
p_form.add_run("• Chọn Danh mục: ").bold = True
p_form.add_run("Nhấp chọn trong danh sách icon (Ăn uống, Di chuyển, Mua sắm, Hóa đơn, Giải trí...).\n")
p_form.add_run("• Chọn Tài khoản/Ví: ").bold = True
p_form.add_run("Chọn nguồn tiền trích xuất (Ví tiền mặt, Tài khoản Vietcombank, MB Bank...).\n")
p_form.add_run("• Nhập ghi chú & Ngày thực hiện: ").bold = True
p_form.add_run("Ghi chú nội dung mua sắm, chọn ngày giao dịch trên DatePicker.")

add_action_box(
    "Bước 3.3: Bấm Lưu giao dịch và kiểm tra kết quả",
    "Người dùng nhấp nút 'Lưu giao dịch' (Save Transaction) màu xanh ở đáy form.",
    "Hệ thống gửi yêu cầu HTTP POST đến Backend Spring Boot -> Lưu vào cơ sở dữ liệu MySQL -> Tự động tính toán trừ/cộng tiền vào số dư ví -> Đóng form và trở về màn hình danh sách giao dịch (Hình 4.3)."
)

add_image_with_caption(
    os.path.join(img_dir, "screen_added.png"),
    "Hình 4.3: Giao dịch mới hiển thị ngay lập tức trong danh sách và số dư ví được cập nhật",
    width=Inches(2.8)
)

# ==================== PHẦN 5: QUẢN LÝ LỊCH SỬ ====================
add_header("5. LUỒNG 4: QUẢN LÝ & LỌC GIAO DỊCH THEO THỜI GIAN", 1)
p = doc.add_paragraph("Để theo dõi dòng tiền chi tiết, người dùng nhấp vào tab 'Lịch sử giao dịch' (Transaction History) trên thanh điều hướng.")

add_image_with_caption(
    os.path.join(img_dir, "screen.png"),
    "Hình 5.1: Màn hình Danh sách toàn bộ giao dịch theo dòng thời gian Timeline",
    width=Inches(2.8)
)

add_action_box(
    "Bước 4.1: Xem chi tiết theo từng Ngày cụ thể",
    "Người dùng nhấp chọn vào một ngày bất kỳ trên thanh cuộn ngày (Calendar Header Strip).",
    "Danh sách giao dịch tự động lọc chỉ hiển thị các khoản thu/chi phát sinh trong ngày đó, kèm tổng kết số tiền chi tiêu của riêng ngày được chọn (Hình 5.2)."
)

add_image_with_caption(
    os.path.join(img_dir, "screen_day1.png"),
    "Hình 5.2: Giao diện lọc chi tiết các khoản giao dịch phát sinh trong 1 ngày cụ thể",
    width=Inches(2.8)
)

add_action_box(
    "Bước 4.2: Chuyển sang chế độ tổng kết theo Tháng",
    "Người dùng nhấp vào nút chuyển bộ lọc 'Xem theo tháng' ở đầu danh sách.",
    "Ứng dụng chuyển sang giao diện thống kê tổng dòng tiền của cả tháng (Hình 5.3), hiển thị tổng thu, tổng chi và số tiền ròng tiết kiệm được."
)

add_image_with_caption(
    os.path.join(img_dir, "screen_month.png"),
    "Hình 5.3: Giao diện thống kê và đối chiếu biến động dòng tiền theo Tháng",
    width=Inches(2.8)
)

# ==================== PHẦN 6: AI PIPELINE DEMO ====================
add_header("6. LUỒNG 5: NHẬN DIỆN SẢN PHẨM THÔNG MINH QUA CAMERA (YOLOv8 + RANDOM FOREST)", 1)
p = doc.add_paragraph("Tính năng thông minh giúp người dùng không cần gõ tên sản phẩm hay tự chọn danh mục:")

add_action_box(
    "Bước 5.1: Kích hoạt Camera nhận diện sản phẩm",
    "Từ bảng BottomSheet [ + ], người dùng nhấp vào 'Nhận diện sản phẩm qua Camera'.",
    "CameraX trên điện thoại mở lên, mô hình YOLOv8 TFLite bắt đầu phát hiện các đối tượng mua sắm theo thời gian thực (Bounding box trực tiếp trên màn hình)."
)

add_image_with_caption(
    os.path.join(img_dir, "yolo_val_pred.jpg"),
    "Hình 6.1: Kết quả nhận diện đối tượng thực tế qua Camera bằng mô hình YOLOv8 TFLite",
    width=Inches(4.5)
)

p_ai_desc = doc.add_paragraph()
p_ai_desc.add_run("Quy trình xử lý AI tự động diễn ra ngầm sau khi chụp:\n").bold = True
p_ai_desc.add_run("1. Phát hiện vật thể: ").bold = True
p_ai_desc.add_run("Mô hình YOLOv8 phát hiện các nhãn như 'rice_meal', 'coffee_cup', 'snack' kèm độ tin cậy confidence.\n")
p_ai_desc.add_run("2. Trích xuất 27 đặc trưng: ").bold = True
p_ai_desc.add_run("App tính toán vector 27 chiều và gửi lên Backend Spring Boot qua API `/api/ai-scan/classify`.\n")
p_ai_desc.add_run("3. Phân loại Random Forest (ONNX): ").bold = True
p_ai_desc.add_run("Backend chạy mô hình ONNX trong 2ms và trả về kết quả danh mục gợi ý: 'Ăn uống' với độ tin cậy > 95%.\n")
p_ai_desc.add_run("4. Tự động điền Form: ").bold = True
p_ai_desc.add_run("Màn hình thêm giao dịch tự động mở ra, danh mục 'Ăn uống' đã được chọn sẵn, người dùng chỉ cần nhập số tiền và bấm Lưu.")

# Minh chứng huấn luyện
add_header("Minh chứng kỹ thuật huấn luyện mô hình AI (Model Training Proofs):", 2)
p = doc.add_paragraph("Các chỉ số đánh giá thực tế từ quá trình huấn luyện mô hình YOLOv8 và Random Forest:")

add_image_with_caption(
    os.path.join(img_dir, "yolo_train_results.png"),
    "Hình 6.2: Đồ thị huấn luyện mô hình YOLOv8 (Precision, Recall, mAP50, Loss qua các epochs)",
    width=Inches(5.0)
)

add_image_with_caption(
    os.path.join(img_dir, "yolo_confusion_matrix.png"),
    "Hình 6.3: Ma trận nhầm lẫn (Confusion Matrix) của mô hình YOLOv8 trên 18 lớp sản phẩm",
    width=Inches(4.5)
)

add_image_with_caption(
    os.path.join(img_dir, "rf_confusion_matrix.png"),
    "Hình 6.4: Ma trận nhầm lẫn của mô hình Random Forest khi phân loại 6 nhóm danh mục tài chính",
    width=Inches(4.0)
)

add_image_with_caption(
    os.path.join(img_dir, "rf_feature_importance.png"),
    "Hình 6.5: Biểu đồ mức độ quan trọng (Feature Importance) của 27 đặc trưng trong Random Forest",
    width=Inches(4.8)
)

# ==================== PHẦN 7: OCR GEMINI ====================
add_header("7. LUỒNG 6: QUÉT HÓA ĐƠN THÔNG MINH (OCR + GEMINI AI)", 1)
p = doc.add_paragraph("Tính năng quét hóa đơn giúp số hóa toàn bộ hóa đơn mua sắm tại siêu thị, quán ăn, nhà hàng:")

add_action_box(
    "Bước 7.1: Chụp ảnh hóa đơn mua hàng",
    "Từ bảng BottomSheet [ + ], người dùng chọn 'Quét hóa đơn' -> Đưa camera chụp toàn bộ tờ hóa đơn.",
    "Google ML Kit OCR chạy trực tiếp trên thiết bị (Offline), trích xuất toàn bộ các dòng chữ và số trên hóa đơn trong 0.2 giây."
)

add_action_box(
    "Bước 7.2: Phân tích ngữ nghĩa tự nhiên bằng Gemini AI",
    "App gửi văn bản thô lên API Backend Spring Boot -> Backend gọi Gemini 2.5 Flash API.",
    "AI tự động bóc tách 4 thông tin: Tên cửa hàng (Ví dụ: Highlands Coffee), Tổng số tiền (89,000 đ), Ngày hóa đơn (2026-10-18), và Gợi ý danh mục (Ăn uống)."
)

add_action_box(
    "Bước 7.3: Xác nhận và lưu tự động",
    "Form giao dịch tự động điền sẵn đầy đủ các thông tin bóc tách được.",
    "Người dùng kiểm tra lại thông tin trên màn hình trong 2 giây rồi bấm 'Lưu giao dịch'. Hệ thống hoàn tất mà không cần gõ bất kỳ chữ số nào bằng tay!"
)

# Save document
output_path = r"D:\Project\KHMT\DATN\TAI_LIEU_HUONG_DAN_GIAO_DIEN_VA_LUONG_HOAT_DONG.docx"
doc.save(output_path)
print(f"SUCCESS: Generated document at {output_path}")
