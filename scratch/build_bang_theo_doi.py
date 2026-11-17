# -*- coding: utf-8 -*-
"""
Generate 01_BANG_THEO_DOI_THUC_HIEN_DATN.docx
Bảng theo dõi thực hiện đồ án tốt nghiệp có xác nhận của GVHD
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

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
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

def build_tracking_doc(output_path):
    doc = docx.Document()
    
    # Page setup: A4, Margins
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(1.8)
    sec.bottom_margin = Cm(1.8)
    sec.left_margin = Cm(2.2)
    sec.right_margin = Cm(1.8)
    
    doc.core_properties.title = "BẢNG THEO DÕI THỰC HIỆN ĐỒ ÁN TỐT NGHIỆP - LẦN 1"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.subject = "Theo dõi tiến độ ĐATN lần 1 - Khóa 2022-2026"
    
    style = doc.styles['Normal']
    style.font.name = 'Times New Roman'
    style.font.size = Pt(11)
    style.paragraph_format.line_spacing = 1.15
    style.paragraph_format.space_before = Pt(0)
    style.paragraph_format.space_after = Pt(2)
    
    # Header: Quốc hiệu & Đơn vị
    t_head = doc.add_table(rows=1, cols=2)
    t_head.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_head)
    
    col_widths_head = [Cm(7.6), Cm(9.4)]
    for row in t_head.rows:
        for idx, width in enumerate(col_widths_head):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=30, bottom=30, left=30, right=30)
            
    c_left = t_head.rows[0].cells[0]
    p_l1 = c_left.paragraphs[0]
    p_l1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_l1.paragraph_format.space_after = Pt(1)
    r = p_l1.add_run("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI\n")
    format_run(r, size_pt=10, bold=True)
    r2 = p_l1.add_run("KHOA CÔNG NGHỆ THÔNG TIN")
    format_run(r2, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    p_l2 = c_left.add_paragraph()
    p_l2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_l2.paragraph_format.space_after = Pt(0)
    r_line = p_l2.add_run("-----------------------")
    format_run(r_line, size_pt=9, bold=True, color_rgb=RGBColor(120, 120, 120))
    
    c_right = t_head.rows[0].cells[1]
    p_r1 = c_right.paragraphs[0]
    p_r1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_r1.paragraph_format.space_after = Pt(1)
    r3 = p_r1.add_run("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM\n")
    format_run(r3, size_pt=10, bold=True)
    r4 = p_r1.add_run("Độc lập - Tự do - Hạnh phúc")
    format_run(r4, size_pt=10, bold=True)
    
    p_r2 = c_right.add_paragraph()
    p_r2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_r2.paragraph_format.space_after = Pt(0)
    r_line2 = p_r2.add_run("-------------------------------")
    format_run(r_line2, size_pt=9, bold=True, color_rgb=RGBColor(120, 120, 120))
    
    p_space = doc.add_paragraph()
    p_space.paragraph_format.space_before = Pt(4)
    p_space.paragraph_format.space_after = Pt(4)
    
    # Title
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_after = Pt(2)
    r_t = p_title.add_run("BẢNG THEO DÕI THỰC HIỆN ĐỒ ÁN TỐT NGHIỆP\n")
    format_run(r_t, size_pt=14, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_sub = p_title.add_run("(Phục vụ kiểm tra tiến độ ĐATN Lần 1, Đợt 1, Năm học 2026 – 2027)")
    format_run(r_sub, size_pt=11, bold=False, italic=True)
    
    # Metadata Box
    t_info = doc.add_table(rows=5, cols=2)
    t_info.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_info, color="B0C4DE", sz="4")
    col_w_info = [Cm(4.5), Cm(12.5)]
    
    info_data = [
        ("Tên đề tài:", "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"),
        ("Sinh viên thực hiện:", "Nguyễn Duy Khánh        Mã sinh viên: 2255010114        Lớp: 22CN4"),
        ("Giảng viên hướng dẫn:", "TS. Nguyễn Mạnh Thắng"),
        ("Thời gian giao/nhận đồ án:", "05/10/2026 — Thời gian thực hiện dự kiến: 15 tuần"),
        ("Thời điểm kiểm tra tiến độ lần 1:", "Ngày 21/11/2026 (Tròn 07 tuần thực hiện)")
    ]
    
    for row_idx, (label, val) in enumerate(info_data):
        row = t_info.rows[row_idx]
        row.cells[0].width = col_w_info[0]
        row.cells[1].width = col_w_info[1]
        set_cell_margins(row.cells[0], top=40, bottom=40, left=60, right=60)
        set_cell_margins(row.cells[1], top=40, bottom=40, left=60, right=60)
        set_cell_background(row.cells[0], "F4F8FA")
        
        p0 = row.cells[0].paragraphs[0]
        p0.paragraph_format.space_after = Pt(1)
        r_lbl = p0.add_run(label)
        format_run(r_lbl, size_pt=10.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        
        p1 = row.cells[1].paragraphs[0]
        p1.paragraph_format.space_after = Pt(1)
        r_val = p1.add_run(val)
        format_run(r_val, size_pt=10.5, bold=(row_idx == 0))
    
    p_lead = doc.add_paragraph()
    p_lead.paragraph_format.space_before = Pt(8)
    p_lead.paragraph_format.space_after = Pt(4)
    r_lead = p_lead.add_run("I. NHẬT KÝ THEO DÕI VÀ BÁO CÁO TIẾN ĐỘ VỚI GIẢNG VIÊN HƯỚNG DẪN")
    format_run(r_lead, size_pt=11.5, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    # Progress Tracking Table
    t_track = doc.add_table(rows=8, cols=6)
    t_track.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_track, color="888888", sz="4")
    
    col_w_track = [Cm(0.9), Cm(2.2), Cm(4.2), Cm(4.2), Cm(3.8), Cm(1.7)]
    
    headers = [
        "STT", 
        "Thời gian\n(Tuần)", 
        "Nội dung công việc\nthực hiện", 
        "Kết quả cụ thể đạt được\n& Sản phẩm minh chứng", 
        "Ý kiến / Nhận xét của\nGiảng viên hướng dẫn", 
        "Xác nhận\nGVHD"
    ]
    
    for c_idx, h_text in enumerate(headers):
        cell = t_track.rows[0].cells[c_idx]
        cell.width = col_w_track[c_idx]
        set_cell_margins(cell, top=70, bottom=70, left=50, right=50)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(h_text)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    weeks_data = [
        (
            "1",
            "Tuần 1\n(05/10 – 11/10/2026)",
            "• Nghiên cứu tổng quan bài toán Quản lý tài chính cá nhân.\n• Khảo sát các ứng dụng: Money Lover, MISA MoneyKeeper, Spendee.\n• Khảo sát công nghệ: YOLOv8, OCR, Random Forest, ONNX, MVVM, Spring Boot.",
            "• Hoàn thành báo cáo khảo sát và xác định phạm vi 5 module chính.\n• Lựa chọn giải pháp Edge AI kết hợp Cloud AI.\n• Đề cương tổng quan đồ án được GVHD phê duyệt.",
            "Nắm vững bài toán thực tế, định hướng công nghệ phù hợp. Duyệt đề cương tổng quan.",
            "Đã duyệt"
        ),
        (
            "2",
            "Tuần 2\n(12/10 – 18/10/2026)",
            "• Phân tích nghiệp vụ hệ thống.\n• Xây dựng sơ đồ phân rã chức năng (BFC).\n• Thiết kế sơ đồ Use Case tổng quát và chi tiết cho từng tác tử.\n• Thiết kế kiến trúc tổng thể 3 tầng (Android MVVM – Spring Boot REST API – MySQL).",
            "• Hoàn thiện sơ đồ Use Case, BFC.\n• Hoàn thành tài liệu đặc tả kiến trúc 3 tầng.\n• Thiết kế luồng dữ liệu an toàn qua Firebase ID Token.",
            "Phân tích nghiệp vụ rõ ràng, thiết kế kiến trúc hệ thống chặt chẽ. Tiếp tục hoàn thiện CSDL.",
            "Đã duyệt"
        ),
        (
            "3",
            "Tuần 3\n(19/10 – 25/10/2026)",
            "• Thiết kế cơ sở dữ liệu quan hệ (ERD) đạt chuẩn 3NF.\n• Ràng buộc khóa ngoại, CASCADE và Indexes.\n• Thiết kế Pipeline AI 2 giai đoạn (YOLOv8 On-Device + Random Forest ONNX).\n• Thiết kế luồng quét hóa đơn OCR + Gemini AI.",
            "• Sơ đồ ERD chuẩn 9 bảng dữ liệu (users, accounts, categories, transactions, budgets, recurring_transactions, images, logs).\n• Bản thiết kế chi tiết 27 đặc trưng AI.\n• Hoàn thiện Chương 2 báo cáo ĐATN.",
            "Thiết kế CSDL chuẩn hóa tốt, ý tưởng pipeline AI 2 giai đoạn sáng tạo và tính khả thi cao.",
            "Đã duyệt"
        ),
        (
            "4",
            "Tuần 4\n(26/10 – 01/11/2026)",
            "• Cài đặt môi trường: Android Studio Ladybug, IntelliJ IDEA, Docker Desktop WSL2.\n• Xây dựng CSDL MySQL 8.0 utf8mb4 (`schema.sql`, `seed_data.sql`).\n• Cấu hình Firebase Auth & Firebase Admin SDK.\n• Dựng khung dự án Backend Spring Boot 3 và Mobile Kotlin MVVM.",
            "• Khởi chạy thành công Docker Compose (MySQL + Spring Boot).\n• Kết nối thành công Firebase Authentication.\n• Khung dự án Backend (11 Controllers) và Mobile (Navigation Graph, ViewBinding, Retrofit) hoạt động.",
            "Hạ tầng Docker bài bản, môi trường chuẩn hóa tốt. Hoàn thành khung dự án đúng tiến độ cam kết.",
            "Đã duyệt"
        ),
        (
            "5",
            "Tuần 5\n(02/11 – 08/11/2026)",
            "• Lập trình đăng nhập/đăng ký: Email/Password và Google Sign-In 1 chạm.\n• Phân hệ Quản lý Tài khoản Ví (Tiền mặt, Ngân hàng, Ví điện tử).\n• Phân hệ Giao dịch Thu Chi: thêm, sửa, xóa, tự động cộng/trừ số dư ví.\n• Quản lý danh mục thu chi; thiết kế Obsidian Dark Theme.",
            "• Đăng nhập Google 1 chạm mượt mà.\n• Đồng bộ tài khoản Firebase vào MySQL.\n• CRUD ví và giao dịch hoạt động chính xác 100%, bảo toàn số dư.\n• Giao diện Obsidian Dark Theme sang trọng, tiết kiệm pin.",
            "Giao diện hiện đại, UX/UI mượt mà. Logic biến động số dư ví được xử lý chặt chẽ. Tiến độ rất tốt.",
            "Đã duyệt"
        ),
        (
            "6",
            "Tuần 6\n(09/11 – 15/11/2026)",
            "• Lập trình Ngân sách & Cảnh báo đổi màu 3 cấp độ.\n• Lập trình Giao dịch định kỳ Cron 00:00 & Quick Trigger API.\n• [VƯỢT TIẾN ĐỘ 4 TUẦN]:\n  + Huấn luyện YOLOv8n (18 lớp sản phẩm, 1.295 ảnh, mAP@0.5 = 87.6%), convert TFLite.\n  + Huấn luyện Random Forest (200 cây, 27 đặc trưng, test acc 94.21%), convert ONNX.\n  + Nhúng ONNX Runtime Java vào Spring Boot.",
            "• Ngân sách tự đổi màu Xanh/Vàng/Đỏ theo hạn mức chi.\n• Kích hoạt giao dịch định kỳ tức thì qua API `/trigger`.\n• Camera AI nhận diện 18 lớp thời gian thực (34–42 FPS).\n• Suy luận ONNX trên Spring Boot siêu tốc < 2ms kèm Fallback.",
            "TIẾN ĐỘ XUẤT SẮC! Hoàn thành vượt kế hoạch đề ra, đã tích hợp thành công cả module AI vào tuần 6.",
            "Đã duyệt"
        ),
        (
            "7",
            "Tuần 7\n(16/11 – 21/11/2026)",
            "• [VƯỢT XA TIẾN ĐỘ ĐỀ RA]:\n  + Tích hợp quét hóa đơn thông minh (Google ML Kit OCR Offline + Gemini 2.5 Flash LLM).\n  + Xây dựng module Báo cáo trực quan MPAndroidChart (PieChart, BarChart).\n  + Viết bộ kiểm thử tự động 86 bài (Unit & Integration Tests).\n  + Hoàn thiện toàn văn báo cáo ĐATN 4 chương.\n  + Chuẩn bị hồ sơ kiểm tra tiến độ lần 1.",
            "• Quét hóa đơn bóc tách tự động tiền, ngày, cửa hàng trong 2.45s.\n• Biểu đồ tài chính hiển thị đa chiều theo tháng/năm.\n• 86/86 test cases Passed (100%).\n• Khối lượng đồ án hoàn thành đạt 85% - 90%.\n• Bộ hồ sơ báo cáo tiến độ lần 1 hoàn chỉnh.",
            "VƯỢT TIẾN ĐỘ NGOẠI MỤC! Hệ thống hoàn thiện 85-90% trước hạn 5 tuần. Đủ điều kiện và khuyến nghị điểm tối đa Lần 1.",
            "Đã duyệt"
        )
    ]
    
    for row_idx, row_data in enumerate(weeks_data, start=1):
        row = t_track.rows[row_idx]
        for c_idx, text in enumerate(row_data):
            cell = row.cells[c_idx]
            cell.width = col_w_track[c_idx]
            set_cell_margins(cell, top=50, bottom=50, left=45, right=45)
            if row_idx % 2 == 1:
                set_cell_background(cell, "F9FBFC")
            if row_idx >= 6:
                # Highlight the advance in progress
                set_cell_background(cell, "F0F7FF")
                
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(1)
            
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(text)
                format_run(r, size_pt=9.5, bold=True)
            elif c_idx == 1:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(text)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 51, 102))
            elif c_idx in [2, 3]:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                r = p.add_run(text)
                format_run(r, size_pt=9)
            elif c_idx == 4:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                r = p.add_run(text)
                format_run(r, size_pt=9, italic=True, bold=(row_idx >= 6))
            elif c_idx == 5:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(text)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 128, 0))
    
    # Section II: General Evaluation by Supervisor
    p_lead2 = doc.add_paragraph()
    p_lead2.paragraph_format.space_before = Pt(8)
    p_lead2.paragraph_format.space_after = Pt(4)
    r_lead2 = p_lead2.add_run("II. ĐÁNH GIÁ CHUNG CỦA GIẢNG VIÊN HƯỚNG DẪN TẠI THỜI ĐIỂM KIỂM TRA LẦN 1")
    format_run(r_lead2, size_pt=11.5, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    t_eval = doc.add_table(rows=5, cols=2)
    t_eval.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_eval, color="B0C4DE", sz="4")
    col_w_eval = [Cm(4.5), Cm(12.5)]
    
    eval_data = [
        ("1. Tinh thần, thái độ làm việc:", "Rất tích cực, chủ động nghiên cứu các công nghệ mới và phức tạp (Edge AI, ONNX Runtime, Gemini LLM). Luôn hoàn thành khối lượng công việc trước thời hạn yêu cầu, có ý thức kỷ luật và trách nhiệm học tập rất cao."),
        ("2. Khối lượng công việc đã hoàn thành:", "ĐẠT KHOẢNG 85% – 90% TOÀN BỘ KHỐI LƯỢNG ĐỒ ÁN TỐT NGHIỆP.\n(Theo kế hoạch ban đầu, thời điểm tuần 7 chỉ yêu cầu hoàn thành khoảng 40–50% khối lượng đề tài. Sinh viên đã hoàn thành trước cả phân hệ AI YOLOv8, ONNX Random Forest, OCR và Báo cáo trực quan của các tuần 9–13)."),
        ("3. Chất lượng sản phẩm phần mềm:", "Rất tốt. Hệ thống chạy ổn định, giao diện Obsidian Dark Theme đẹp mắt, luồng xử lý mượt mà. 86/86 bài kiểm thử tự động (Unit/Integration Tests) chạy thành công 100%. Mô hình AI nhận diện chính xác và suy luận dưới 2ms."),
        ("4. Đánh giá mức độ tiến độ:", "VƯỢT TIẾN ĐỘ ĐỀ RA (Vượt trước khoảng 04 – 05 tuần so với kế hoạch ban đầu)."),
        ("5. Kết luận & Đề xuất:", "ĐỦ ĐIỀU KIỆN VÀ KHUYẾN NGHỊ TIỂU BAN ĐÁNH GIÁ ĐẠT ĐIỂM TỐI ĐA TRONG LẦN KIỂM TRA TIẾN ĐỘ 1.")
    ]
    
    for row_idx, (lbl, val) in enumerate(eval_data):
        row = t_eval.rows[row_idx]
        row.cells[0].width = col_w_eval[0]
        row.cells[1].width = col_w_eval[1]
        set_cell_margins(row.cells[0], top=40, bottom=40, left=50, right=50)
        set_cell_margins(row.cells[1], top=40, bottom=40, left=50, right=50)
        set_cell_background(row.cells[0], "F4F8FA")
        
        p0 = row.cells[0].paragraphs[0]
        p0.paragraph_format.space_after = Pt(1)
        r = p0.add_run(lbl)
        format_run(r, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
        
        p1 = row.cells[1].paragraphs[0]
        p1.paragraph_format.space_after = Pt(1)
        r2 = p1.add_run(val)
        format_run(r2, size_pt=10, bold=(row_idx in [1, 3, 4]))
    
    # Signature Section
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(8)
    p_sp.paragraph_format.space_after = Pt(2)
    
    t_sig = doc.add_table(rows=1, cols=2)
    t_sig.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_sig)
    
    col_w_sig = [Cm(8.5), Cm(8.5)]
    for row in t_sig.rows:
        for idx, width in enumerate(col_w_sig):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=30, bottom=30, left=30, right=30)
            
    c_s_left = t_sig.rows[0].cells[0]
    p_sl = c_s_left.paragraphs[0]
    p_sl.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sl.paragraph_format.space_after = Pt(2)
    r_sl1 = p_sl.add_run("SINH VIÊN THỰC HIỆN\n")
    format_run(r_sl1, size_pt=11, bold=True)
    r_sl2 = p_sl.add_run("(Ký và ghi rõ họ tên)\n\n\n\n\n")
    format_run(r_sl2, size_pt=10, italic=True)
    r_sl3 = p_sl.add_run("Nguyễn Duy Khánh")
    format_run(r_sl3, size_pt=11, bold=True)
    
    c_s_right = t_sig.rows[0].cells[1]
    p_sr = c_s_right.paragraphs[0]
    p_sr.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sr.paragraph_format.space_after = Pt(2)
    r_sr0 = p_sr.add_run("Hà Nội, ngày 20 tháng 11 năm 2026\n")
    format_run(r_sr0, size_pt=10.5, italic=True)
    r_sr1 = p_sr.add_run("GIẢNG VIÊN HƯỚNG DẪN\n")
    format_run(r_sr1, size_pt=11, bold=True)
    r_sr2 = p_sr.add_run("(Ký và ghi rõ họ tên)\n\n\n\n\n")
    format_run(r_sr2, size_pt=10, italic=True)
    r_sr3 = p_sr.add_run("TS. Nguyễn Mạnh Thắng")
    format_run(r_sr3, size_pt=11, bold=True)
    
    doc.save(output_path)
    print(f"Successfully generated: {output_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1'
    os.makedirs(out_dir, exist_ok=True)
    out_file = os.path.join(out_dir, '01_BANG_THEO_DOI_THUC_HIEN_DATN.docx')
    build_tracking_doc(out_file)
