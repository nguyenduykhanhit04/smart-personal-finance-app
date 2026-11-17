# -*- coding: utf-8 -*-
"""
Generate 03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx
Bản Báo cáo Tiến độ ĐATN Lần 1 chuẩn khoa học kèm hình ảnh minh chứng
Student: Nguyễn Duy Khánh - 2255010114 - 22CN4
Supervisor: TS. Nguyễn Mạnh Thắng
"""
import os
import docx
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def set_cell_margins(cell, top=70, bottom=70, left=100, right=100):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_cell_background(cell, color_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>')
    tcPr.append(shd)

def set_table_borders(table, color="A0A0A0", sz="4", val="single"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(f'''
    <w:tblBorders {nsdecls("w")}>
        <w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
        <w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
        <w:left w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
        <w:right w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
        <w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
        <w:insideV w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>
    </w:tblBorders>
    ''')
    tblPr.append(borders)

def set_no_borders(table):
    tblPr = table._tbl.tblPr
    borders = parse_xml(f'''
    <w:tblBorders {nsdecls("w")}>
        <w:top w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        <w:bottom w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        <w:left w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        <w:right w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        <w:insideH w:val="none" w:sz="0" w:space="0" w:color="auto"/>
        <w:insideV w:val="none" w:sz="0" w:space="0" w:color="auto"/>
    </w:tblBorders>
    ''')
    tblPr.append(borders)

def format_run(run, font_name="Times New Roman", size_pt=11, bold=False, italic=False, color_rgb=None):
    run.font.name = font_name
    run.font.size = Pt(size_pt)
    run.bold = bold
    run.italic = italic
    if color_rgb:
        run.font.color.rgb = color_rgb
    rPr = run._r.get_or_add_rPr()
    rFonts = rPr.find(qn('w:rFonts'))
    if rFonts is not None:
        rFonts.set(qn('w:ascii'), font_name)
        rFonts.set(qn('w:hAnsi'), font_name)
        rFonts.set(qn('w:cs'), font_name)

def build_report_doc(output_path, img_dir):
    doc = docx.Document()
    
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    doc.core_properties.title = "BÁO CÁO TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP - LẦN 1"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.subject = "Báo cáo tiến độ ĐATN lần 1 - 21/11/2026"
    
    style = doc.styles['Normal']
    style.font.name = 'Times New Roman'
    style.font.size = Pt(11)
    style.paragraph_format.line_spacing = 1.2
    style.paragraph_format.space_before = Pt(0)
    style.paragraph_format.space_after = Pt(3)
    
    # ==========================================
    # COVER PAGE
    # ==========================================
    p_c1 = doc.add_paragraph()
    p_c1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_c1.paragraph_format.space_before = Pt(10)
    p_c1.paragraph_format.space_after = Pt(2)
    r = p_c1.add_run("BỘ GIÁO DỤC VÀ ĐÀO TẠO — BỘ XÂY DỰNG\n")
    format_run(r, size_pt=11, bold=True)
    r2 = p_c1.add_run("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI\n")
    format_run(r2, size_pt=12, bold=True)
    r3 = p_c1.add_run("KHOA CÔNG NGHỆ THÔNG TIN\n")
    format_run(r3, size_pt=12, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_line = p_c1.add_run("------------------------------------")
    format_run(r_line, size_pt=10, bold=True, color_rgb=RGBColor(120, 120, 120))
    
    p_sp1 = doc.add_paragraph()
    p_sp1.paragraph_format.space_before = Pt(40)
    p_sp1.paragraph_format.space_after = Pt(10)
    
    p_ct = doc.add_paragraph()
    p_ct.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_ct.paragraph_format.space_after = Pt(8)
    r_ct = p_ct.add_run("BÁO CÁO TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP\n")
    format_run(r_ct, size_pt=18, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_sub = p_ct.add_run("(KIỂM TRA LẦN 1 — ĐỢT 1 NĂM HỌC 2026 – 2027)\n")
    format_run(r_sub, size_pt=13, bold=True, color_rgb=RGBColor(180, 0, 0))
    
    p_top = doc.add_paragraph()
    p_top.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_top.paragraph_format.space_before = Pt(15)
    p_top.paragraph_format.space_after = Pt(10)
    r_t_lbl = p_top.add_run("ĐỀ TÀI:\n")
    format_run(r_t_lbl, size_pt=12, bold=True)
    r_top = p_top.add_run("XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO")
    format_run(r_top, size_pt=14, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    p_sp2 = doc.add_paragraph()
    p_sp2.paragraph_format.space_before = Pt(40)
    p_sp2.paragraph_format.space_after = Pt(10)
    
    # Metadata Table Cover
    t_c_meta = doc.add_table(rows=5, cols=2)
    t_c_meta.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_c_meta)
    for row in t_c_meta.rows:
        row.cells[0].width = Cm(6.0)
        row.cells[1].width = Cm(10.0)
        set_cell_margins(row.cells[0], top=30, bottom=30, left=40, right=40)
        set_cell_margins(row.cells[1], top=30, bottom=30, left=40, right=40)
        
    c_meta_data = [
        ("Ngành đào tạo:", "Công nghệ thông tin"),
        ("Sinh viên thực hiện:", "Nguyễn Duy Khánh"),
        ("Mã sinh viên:", "2255010114          Lớp: 22CN4"),
        ("Giảng viên hướng dẫn:", "TS. Nguyễn Mạnh Thắng"),
        ("Thời gian thực hiện:", "05/10/2026 – 18/01/2027 (Thời điểm báo cáo: 21/11/2026)")
    ]
    for idx, (lbl, val) in enumerate(c_meta_data):
        r0 = t_c_meta.rows[idx].cells[0].paragraphs[0].add_run(lbl)
        format_run(r0, size_pt=11.5, bold=True)
        r1 = t_c_meta.rows[idx].cells[1].paragraphs[0].add_run(val)
        format_run(r1, size_pt=11.5, bold=(idx in [1, 3]))
        
    p_bot_c = doc.add_paragraph()
    p_bot_c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_bot_c.paragraph_format.space_before = Pt(60)
    r_bot = p_bot_c.add_run("Hà Nội, Tháng 11 năm 2026")
    format_run(r_bot, size_pt=11.5, bold=True)
    
    doc.add_page_break()
    
    # ==========================================
    # BODY CONTENT
    # ==========================================
    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(text)
        format_run(r, size_pt=12.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(text)
        format_run(r, size_pt=11.5, bold=True, color_rgb=RGBColor(0, 70, 140))
        return p

    def add_p(text, bold_prefix="", italic=False):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(3)
        if bold_prefix:
            r0 = p.add_run(bold_prefix)
            format_run(r0, size_pt=11, bold=True)
        r = p.add_run(text)
        format_run(r, size_pt=11, italic=italic)
        return p

    def add_img(img_name, caption, width_cm=14.0):
        img_path = os.path.join(img_dir, img_name)
        if os.path.exists(img_path):
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(6)
            p.paragraph_format.space_after = Pt(2)
            doc.add_picture(img_path, width=Cm(width_cm))
            p_cap = doc.add_paragraph()
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p_cap.paragraph_format.space_after = Pt(6)
            r = p_cap.add_run(f"Hình: {caption}")
            format_run(r, size_pt=9.5, italic=True)

    # 1. TỔNG QUAN VÀ KẾ HOẠCH
    add_h1("1. TỔNG QUAN ĐỀ TÀI VÀ CĂN CỨ THỰC HIỆN")
    add_p("Đề tài tốt nghiệp mang tên: ", bold_prefix="Tên đề tài: ")
    add_p("\"Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\".", italic=True)
    add_p("• Sinh viên thực hiện: Nguyễn Duy Khánh — Mã sinh viên: 2255010114 — Lớp: 22CN4.")
    add_p("• Giảng viên hướng dẫn: TS. Nguyễn Mạnh Thắng.")
    add_p("• Cơ sở đào tạo: Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội.")
    add_p("• Thời gian giao nhận đồ án: Ngày 05/10/2026 (Kế hoạch 15 tuần: 05/10/2026 – 18/01/2027).")
    add_p("• Thời điểm kiểm tra tiến độ Lần 1: Ngày 21/11/2026 (Tròn 07 tuần kể từ thời điểm nhận đề tài).")
    
    add_p("Trong bối cảnh nền kinh tế số và thanh toán không tiền mặt bùng nổ, việc ghi chép chi tiêu thủ công bằng bàn phím ảo khiến trên 80% người dùng bỏ cuộc sau 2-4 tuần. Đề tài tập trung giải quyết bài toán cốt lõi: tự động hóa tối đa quy trình ghi nhận chi tiêu bằng cách kết hợp Thị giác máy tính (YOLOv8 On-Device) và Trí tuệ nhân tạo (Random Forest ONNX + Google Gemini 2.5 Flash LLM).", bold_prefix="Tính cấp thiết & Mục tiêu: ")

    # 2. KẾT QUẢ THỰC HIỆN TÍNH ĐẾN THỜI ĐIỂM 21/11/2026
    add_h1("2. NỘI DUNG VÀ KẾT QUẢ THỰC HIỆN ĐẾN THỜI ĐIỂM KIỂM TRA LẦN 1")
    add_p("Trong 07 tuần vừa qua (từ 05/10/2026 đến 21/11/2026), sinh viên đã làm việc với tinh thần tập trung cao độ, hoàn thành vượt mức toàn bộ các nội dung cam kết và tiến xa hơn kế hoạch ban đầu:")

    add_h2("2.1. Phân tích nghiệp vụ và Thiết kế Kiến trúc Hệ thống")
    add_p("• Hoàn thành Sơ đồ phân rã chức năng (BFC) xác định 5 nhóm chức năng cốt lõi: Quản lý ví, Giao dịch thu chi, Quản lý ngân sách cảnh báo, Trí tuệ nhân tạo (Camera AI & OCR), Báo cáo thống kê.")
    add_p("• Xây dựng Sơ đồ Use Case tổng quát và chi tiết cho người dùng và các hệ thống tác tử bên ngoài.")
    add_p("• Thiết kế Kiến trúc hệ thống 3 tầng chuẩn mực:")
    add_p("   + Tầng Mobile Client: Android Kotlin Native, kiến trúc MVVM, Navigation Component, ViewBinding, LiveData, Coroutines, CameraX.")
    add_p("   + Tầng Backend API: Spring Boot 3.2.5 (Java 21 LTS), Spring Security + Firebase Token Filter, Hibernate ORM, Bean Validation, HikariCP.")
    add_p("   + Tầng Trí tuệ nhân tạo: Microsoft ONNX Runtime Java nhúng thẳng trong Spring Boot; Google ML Kit OCR chạy On-Device; Google Gemini 2.5 Flash LLM bóc tách ngữ cảnh JSON.")
    add_p("   + Tầng CSDL & Container: MySQL 8.0 utf8mb4, đóng gói toàn bộ qua Docker Compose.")

    add_h2("2.2. Thiết kế Cơ sở Dữ liệu Quan hệ (ERD 9 bảng chuẩn 3NF)")
    add_p("Cơ sở dữ liệu personal_finance_app được thiết kế chặt chẽ gồm 9 bảng quan hệ:")
    
    t_db = doc.add_table(rows=10, cols=4)
    t_db.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_db, color="888888", sz="4")
    col_w_db = [Cm(0.9), Cm(3.8), Cm(4.5), Cm(7.3)]
    
    db_headers = ["STT", "Tên bảng CSDL", "Khóa chính / Khóa ngoại", "Mô tả chức năng"]
    for c_idx, h in enumerate(db_headers):
        cell = t_db.rows[0].cells[c_idx]
        cell.width = col_w_db[c_idx]
        set_cell_margins(cell, top=50, bottom=50, left=50, right=50)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    db_rows = [
        ("1", "users", "user_id (PK), firebase_uid (UK)", "Quản lý tài khoản, đồng bộ Firebase Auth"),
        ("2", "accounts", "account_id (PK), user_id (FK)", "Quản lý ví tài chính (Tiền mặt, Ngân hàng, Ví điện tử)"),
        ("3", "categories", "category_id (PK), user_id (FK)", "Quản lý danh mục thu/chi (11 danh mục mặc định)"),
        ("4", "transactions", "transaction_id (PK), user_id, account_id", "Nhật ký biến động giao dịch thu chi tài chính"),
        ("5", "budgets", "budget_id (PK), user_id, category_id", "Hạn mức chi tiêu và theo dõi tiến độ ngân sách"),
        ("6", "recurring_transactions", "recurring_id (PK), user_id, account_id", "Lịch giao dịch định kỳ (ngày, tuần, tháng, năm)"),
        ("7", "transaction_images", "image_id (PK), transaction_id (FK)", "Lưu trữ hình ảnh chứng từ và hóa đơn đính kèm"),
        ("8", "ai_scan_logs", "ai_scan_log_id (PK), user_id (FK)", "Nhật ký OCR và kết quả trích xuất hóa đơn Gemini"),
        ("9", "ai_product_logs", "ai_product_log_id (PK), user_id (FK)", "Nhật ký nhận diện vật thể YOLO và gợi ý Random Forest")
    ]
    for r_idx, r_data in enumerate(db_rows, start=1):
        row = t_db.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_db[c_idx]
            set_cell_margins(cell, top=40, bottom=40, left=45, right=45)
            if r_idx % 2 == 1:
                set_cell_background(cell, "F9FBFC")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 51, 102))
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    add_h2("2.3. Hiện thực hóa các Phân hệ Nghiệp vụ Cốt lõi")
    add_p("• Phân hệ Đăng nhập / Đăng ký: Đăng nhập Google Sign-In 1 chạm và Email/Password của Firebase. Tích hợp tính năng phím tắt nhấn giữ logo 2 giây để tùy biến IP Server.")
    add_p("• Phân hệ Quản lý Tài khoản Ví: Quản lý 3 loại ví độc lập, tính toán tự động Tổng số dư ròng hiển thị trên Dashboard.")
    add_p("• Phân hệ Giao dịch Thu Chi: Thêm, sửa, xóa giao dịch; tự động cộng/trừ số dư ví; cơ chế bù trừ và rollback số dư hoàn hảo khi chỉnh sửa giao dịch.")
    add_p("• Phân hệ Ngân sách Đổi màu 3 cấp độ: Tự động đổi màu Xanh (<80%) -> Vàng cam (80-100%) -> Đỏ (>100%) và tính hạn mức chi tiêu an toàn hàng ngày.")
    add_p("• Phân hệ Giao dịch Định kỳ: Tự động phát sinh giao dịch qua Cronjob 00:00 hàng ngày và bổ sung API kích hoạt tức thì `/api/recurring-transactions/trigger` phục vụ demo kiểm thử.")
    add_p("• Ngôn ngữ thiết kế Obsidian Dark Theme: Giao diện nền đen sâu `#090D16`, thẻ Card `#161B2A`, điểm nhấn Royal Blue `#3B82F6` sang trọng và tiết kiệm pin OLED.")

    add_h2("2.4. ĐIỂM ĐỘT PHÁ VƯỢT TIẾN ĐỘ: Phân hệ Trí tuệ Nhân tạo & Thị giác Máy tính")
    add_p("Mặc dù theo kế hoạch ban đầu, phân hệ AI thuộc giai đoạn Tuần 9 đến Tuần 13, nhưng đến thời điểm Tuần 7 hiện tại, sinh viên đã hoàn thành 100% phân hệ này:", bold_prefix="Hoàn thành trước hạn 4-5 tuần: ")
    add_p("1. Nhận diện sản phẩm On-Device (YOLOv8n TFLite): Huấn luyện thành công mô hình trên 1.295 hình ảnh thực tế với 18 lớp mặt hàng mua sắm (cốc cà phê, bánh mì, đồ điện tử, quần áo, mũ bảo hiểm, đồ chơi...). Độ chính xác mAP@0.5 đạt 87.6%. Chuyển đổi sang TensorFlow Lite Float32 (6.3MB), chạy mượt mà trên CameraX với tốc độ 34–42 FPS.")
    add_p("2. Pipeline AI 2 giai đoạn (Random Forest qua ONNX Runtime Java): Trích xuất vector 27 đặc trưng kỹ thuật, nạp vào mô hình Random Forest (200 cây quyết định, độ chính xác kiểm thử 94.21%, F1-Macro 0.938). Mô hình được biên dịch sang file ONNX (768KB) và nhúng trực tiếp vào Spring Boot qua Microsoft ONNX Runtime Java, suy luận siêu tốc dưới 2ms kèm cơ chế Heuristic Fallback.")
    add_p("3. Quét hóa đơn thông minh Hybrid (Google ML Kit OCR + Gemini 2.5 Flash LLM): Nhận dạng chữ Offline trên máy trong 400ms, Google Gemini 2.5 Flash phân tích ngữ cảnh trả về cấu trúc JSON (tổng tiền, ngày, đơn vị bán lẻ, danh mục) trong 2.45s.")
    add_p("4. Báo cáo trực quan đa chiều: Biểu đồ tròn PieChart cơ cấu chi tiêu và biểu đồ cột BarChart 12 tháng bằng thư viện MPAndroidChart.")

    add_h2("2.5. Kiểm thử Tự động Phần mềm (100% Passed)")
    add_p("Hệ thống máy chủ Backend đã được kiểm thử tự động toàn diện bằng JUnit 5, Mockito và MockMvc với tổng cộng 86 ca kiểm thử bao phủ toàn bộ 11 Controller và 8 Service. Toàn bộ 86/86 bài kiểm thử đều chạy thành công 100% (0 errors, 0 failures).")

    # 3. BẢNG ĐỐI CHIẾU TIẾN ĐỘ VƯỢT MỨC
    add_h1("3. BẢNG ĐỐI CHIẾU TIẾN ĐỘ CAM KẾT VÀ KẾT QUẢ THỰC TẾ")
    
    t_comp = doc.add_table(rows=7, cols=5)
    t_comp.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_comp, color="888888", sz="4")
    col_w_comp = [Cm(0.9), Cm(4.0), Cm(3.2), Cm(5.2), Cm(3.2)]
    
    comp_headers = ["STT", "Nội dung theo Kế hoạch", "Thời gian dự kiến", "Kết quả thực tế (21/11/2026)", "Đánh giá tiến độ"]
    for c_idx, h in enumerate(comp_headers):
        cell = t_comp.rows[0].cells[c_idx]
        cell.width = col_w_comp[c_idx]
        set_cell_margins(cell, top=50, bottom=50, left=45, right=45)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    comp_rows = [
        ("1", "Khảo sát tổng quan, công nghệ (YOLOv8, RF, OCR, Gemini, Spring Boot)", "Tuần 1 (1 tuần)", "Hoàn thành 100% báo cáo khảo sát và đề cương nghiên cứu", "Đúng tiến độ (100%)"),
        ("2", "Phân tích thiết kế (ERD 9 bảng, Use Case, Kiến trúc 3 tầng, Pipeline AI)", "Tuần 2–3 (2 tuần)", "Hoàn thành 100% hồ sơ thiết kế, ERD chuẩn 3NF, Chương 2", "Đúng tiến độ (100%)"),
        ("3", "Cài đặt môi trường (Docker, MySQL, Spring Boot, Android Studio, Firebase)", "Tuần 4 (1 tuần)", "Hoàn thành 100% Dockerfile, docker-compose, khung dự án", "Đúng tiến độ (100%)"),
        ("4", "Lập trình chức năng cốt lõi (Auth, ví, giao dịch, ngân sách, định kỳ, theme)", "Tuần 5–8 (4 tuần)", "Hoàn thành 100% toàn bộ 7 phân hệ nghiệp vụ tài chính", "VƯỢT TIẾN ĐỘ (Hoàn thành sớm 1 tuần)"),
        ("5", "Huấn luyện AI (YOLOv8, RF ONNX), OCR Gemini, Báo cáo biểu đồ, Test", "Tuần 9–13 (5 tuần)", "HOÀN THÀNH 100%: YOLO 18 lớp (87.6%), RF ONNX (<2ms), OCR Gemini, 86/86 test", "VƯỢT XA TIẾN ĐỘ (Hoàn thành trước 4 tuần)"),
        ("6", "Hoàn thiện báo cáo toàn văn ĐATN, chuẩn bị bảo vệ", "Tuần 14–15 (2 tuần)", "Đã hoàn thành khung toàn văn báo cáo 4 chương đầy đủ", "VƯỢT TIẾN ĐỘ (Đã xong bản thảo)")
    ]
    for r_idx, r_data in enumerate(comp_rows, start=1):
        row = t_comp.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_comp[c_idx]
            set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
            if r_idx in [4, 5]:
                set_cell_background(cell, "F0F7FF")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx in [0, 2]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=(c_idx == 0))
            elif c_idx == 4:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 102, 0) if "VƯỢT" in val else RGBColor(0, 51, 102))
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=(c_idx == 1 and r_idx == 5))

    p_eval_box = doc.add_paragraph()
    p_eval_box.paragraph_format.space_before = Pt(8)
    p_eval_box.paragraph_format.space_after = Pt(4)
    r_ev = p_eval_box.add_run("TỔNG KẾT KHỐI LƯỢNG HOÀN THÀNH: ĐẠT 85% – 90% TOÀN BỘ ĐỒ ÁN TỐT NGHIỆP.\n(Vượt trước tiến độ quy định khoảng 04 đến 05 tuần so với kế hoạch ban đầu).")
    format_run(r_ev, size_pt=11, bold=True, color_rgb=RGBColor(180, 0, 0))

    # 4. HÌNH ẢNH MINH CHỨNG
    add_h1("4. HÌNH ẢNH MINH CHỨNG KẾT QUẢ THỰC NGHIỆM")
    add_p("Dưới đây là một số hình ảnh chụp trực tiếp từ ứng dụng Android, kết quả huấn luyện mô hình AI và kết quả kiểm thử tự động:")
    
    add_img("login_screen.png", "Giao diện Đăng nhập Google Sign-In 1 chạm và tính năng IP Config", width_cm=7.5)
    add_img("main_screen.png", "Màn hình Dashboard Obsidian Dark Theme, thẻ số dư ròng và danh sách ví", width_cm=7.5)
    add_img("screen_sheet.png", "Màn hình Quản lý giao dịch thu chi và lọc dữ liệu đa chiều", width_cm=7.5)
    add_img("screen_manual.png", "Giao diện Thêm giao dịch thu chi với bàn phím tùy chỉnh", width_cm=7.5)
    add_img("rf_feature_importance.png", "Đồ thị 27 đặc trưng kỹ thuật quan trọng nhất trong mô hình Random Forest", width_cm=13.0)
    add_img("yolo_train_results.png", "Đồ thị kết quả huấn luyện mô hình YOLOv8n (mAP@0.5 đạt 87.6%)", width_cm=14.0)

    # 5. KẾ HOẠCH TIẾP THEO
    add_h1("5. KẾ HOẠCH GIAI ĐOẠN TIẾP THEO VÀ KẾT LUẬN")
    add_p("Trong các tuần tiếp theo (Tuần 8 đến Tuần 15), sinh viên sẽ tập trung vào các công tác hoàn thiện chuyên sâu:")
    add_p("1. Thử nghiệm trên nhóm người dùng mẫu: Đánh giá độ hài lòng về giao diện Obsidian Dark Theme và tốc độ bóc tách hóa đơn.")
    add_p("2. Tối ưu hóa mô hình và bảo mật: Tinh chỉnh trọng số YOLO, kiểm tra rà soát các lỗ hổng API.")
    add_p("3. Hoàn thiện quyển Báo cáo Đồ án tốt nghiệp: Chỉnh sửa văn phong học thuật, nộp bản thảo cho GVHD xem xét.")
    add_p("4. Chuẩn bị bảo vệ: Xây dựng slide báo cáo chuyên nghiệp, làm video demo sản phẩm và luyện tập thuyết trình.")
    
    add_p("Đồ án tốt nghiệp của sinh viên Nguyễn Duy Khánh đã được thực hiện với tinh thần nghiêm túc, khoa học và đạt kết quả vượt trội. Toàn bộ các yêu cầu của đợt kiểm tra tiến độ lần 1 đã được đáp ứng xuất sắc.", bold_prefix="Kết luận: ")

    # Signature Block
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(12)
    
    t_sig = doc.add_table(rows=1, cols=2)
    t_sig.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_sig)
    for r in t_sig.rows:
        r.cells[0].width = Cm(8.5)
        r.cells[1].width = Cm(8.5)
        
    p_sl = t_sig.rows[0].cells[0].paragraphs[0]
    p_sl.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_sl1 = p_sl.add_run("XÁC NHẬN CỦA GIẢNG VIÊN HƯỚNG DẪN\n")
    format_run(r_sl1, size_pt=11, bold=True)
    r_sl2 = p_sl.add_run("(Ký và ghi rõ họ tên)\n\n\n\n\n")
    format_run(r_sl2, size_pt=10, italic=True)
    r_sl3 = p_sl.add_run("TS. Nguyễn Mạnh Thắng")
    format_run(r_sl3, size_pt=11, bold=True)
    
    p_sr = t_sig.rows[0].cells[1].paragraphs[0]
    p_sr.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_sr0 = p_sr.add_run("Hà Nội, ngày 21 tháng 11 năm 2026\n")
    format_run(r_sr0, size_pt=10.5, italic=True)
    r_sr1 = p_sr.add_run("SINH VIÊN BÁO CÁO\n")
    format_run(r_sr1, size_pt=11, bold=True)
    r_sr2 = p_sr.add_run("(Ký và ghi rõ họ tên)\n\n\n\n\n")
    format_run(r_sr2, size_pt=10, italic=True)
    r_sr3 = p_sr.add_run("Nguyễn Duy Khánh")
    format_run(r_sr3, size_pt=11, bold=True)
    
    doc.save(output_path)
    print(f"Successfully generated: {output_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1'
    img_dir = os.path.join(out_dir, 'minh_chung_hinh_anh')
    out_file = os.path.join(out_dir, '03_BAO_CAO_TIEN_DO_DATN_LAN_1.docx')
    build_report_doc(out_file, img_dir)
