# -*- coding: utf-8 -*-
"""
Script to generate standard BẢNG KẾ HOẠCH DỰ KIẾN THỰC HIỆN ĐỒ ÁN TỐT NGHIỆP
Student: Nguyễn Duy Khánh - 2255010114 - 22CN4
Supervisor: TS. Nguyễn Mạnh Thắng
"""
import os
import shutil
import docx
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
    """Set inner cell padding in twips (1 pt = 20 twips)."""
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_cell_background(cell, color_hex):
    """Set background color of a cell."""
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{color_hex}"/>')
    tcPr.append(shd)

def set_table_borders(table, color="B0B0B0", sz="4", val="single"):
    """Set subtle, professional borders for a table."""
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
    """Remove borders from table."""
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

def format_run(run, font_name="Times New Roman", size_pt=11.5, bold=False, italic=False, color_rgb=None):
    run.font.name = font_name
    run.font.size = Pt(size_pt)
    run.bold = bold
    run.italic = italic
    if color_rgb:
        run.font.color.rgb = color_rgb
    # ensure EastAsia/Complex script fonts match
    rPr = run._r.get_or_add_rPr()
    rFonts = rPr.find(qn('w:rFonts'))
    if rFonts is not None:
        rFonts.set(qn('w:ascii'), font_name)
        rFonts.set(qn('w:hAnsi'), font_name)
        rFonts.set(qn('w:cs'), font_name)

def build_plan_doc(output_path):
    doc = docx.Document()
    
    # Page setup: A4, Margins
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(1.8)
    sec.bottom_margin = Cm(1.8)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(1.8)
    
    # Document Core Properties
    doc.core_properties.title = "BẢNG KẾ HOẠCH DỰ KIẾN THỰC HIỆN ĐỒ ÁN TỐT NGHIỆP"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.last_modified_by = "Nguyễn Duy Khánh"
    doc.core_properties.subject = "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"
    
    # Normal style
    style = doc.styles['Normal']
    style.font.name = 'Times New Roman'
    style.font.size = Pt(11)
    style.paragraph_format.line_spacing = 1.15
    style.paragraph_format.space_before = Pt(0)
    style.paragraph_format.space_after = Pt(2)
    
    # ==========================================
    # 1. HEADER TABLE: Quốc hiệu & Đơn vị đào tạo
    # ==========================================
    t_head = doc.add_table(rows=1, cols=2)
    t_head.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_head)
    
    col_widths_head = [Cm(7.4), Cm(9.3)]
    for row in t_head.rows:
        for idx, width in enumerate(col_widths_head):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=30, bottom=30, left=40, right=40)
            
    c_left = t_head.rows[0].cells[0]
    p_l1 = c_left.paragraphs[0]
    p_l1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_l1.paragraph_format.space_after = Pt(1)
    r = p_l1.add_run("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI")
    format_run(r, size_pt=10.5, bold=True)
    
    p_l2 = c_left.add_paragraph()
    p_l2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_l2.paragraph_format.space_after = Pt(2)
    r = p_l2.add_run("KHOA CÔNG NGHỆ THÔNG TIN")
    format_run(r, size_pt=11, bold=True)
    
    p_l3 = c_left.add_paragraph()
    p_l3.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_l3.paragraph_format.space_after = Pt(0)
    r = p_l3.add_run("-------------------")
    format_run(r, size_pt=9, bold=False)
    
    c_right = t_head.rows[0].cells[1]
    p_r1 = c_right.paragraphs[0]
    p_r1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_r1.paragraph_format.space_after = Pt(1)
    r = p_r1.add_run("CỘNG HOÀ XÃ HỘI CHỦ NGHĨA VIỆT NAM")
    format_run(r, size_pt=10.5, bold=True)
    
    p_r2 = c_right.add_paragraph()
    p_r2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_r2.paragraph_format.space_after = Pt(2)
    r = p_r2.add_run("Độc lập - Tự do - Hạnh phúc")
    format_run(r, size_pt=11.5, bold=True)
    
    p_r3 = c_right.add_paragraph()
    p_r3.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_r3.paragraph_format.space_after = Pt(0)
    r = p_r3.add_run("-----------------------------")
    format_run(r, size_pt=9, bold=False)
    
    # ==========================================
    # 2. TIÊU ĐỀ KẾ HOẠCH
    # ==========================================
    p_title1 = doc.add_paragraph()
    p_title1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title1.paragraph_format.space_before = Pt(10)
    p_title1.paragraph_format.space_after = Pt(2)
    r = p_title1.add_run("BẢNG KẾ HOẠCH DỰ KIẾN THỰC HIỆN ĐỒ ÁN TỐT NGHIỆP")
    format_run(r, size_pt=13.5, bold=True, color_rgb=RGBColor(0, 32, 96))
    
    p_title2 = doc.add_paragraph()
    p_title2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title2.paragraph_format.space_before = Pt(0)
    p_title2.paragraph_format.space_after = Pt(10)
    r = p_title2.add_run("ĐỢT 1 / NĂM HỌC 2026 - 2027")
    format_run(r, size_pt=11.5, bold=True)
    
    # ==========================================
    # 3. THÔNG TIN SINH VIÊN & ĐỀ TÀI (Table không viền)
    # ==========================================
    t_info = doc.add_table(rows=5, cols=2)
    t_info.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_info)
    
    col_w_info = [Cm(11.0), Cm(5.7)]
    for row in t_info.rows:
        for idx, width in enumerate(col_w_info):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=30, bottom=30, left=40, right=40)
            
    # Row 0: Họ tên & Mã SV
    p = t_info.rows[0].cells[0].paragraphs[0]
    p.paragraph_format.space_after = Pt(1.5)
    r = p.add_run("Họ và tên sinh viên: "); format_run(r, size_pt=11)
    r = p.add_run("Nguyễn Duy Khánh"); format_run(r, size_pt=11, bold=True)
    
    p = t_info.rows[0].cells[1].paragraphs[0]
    p.paragraph_format.space_after = Pt(1.5)
    r = p.add_run("Mã SV: "); format_run(r, size_pt=11)
    r = p.add_run("2255010114"); format_run(r, size_pt=11, bold=True)
    
    # Row 1: Lớp, SĐT & Ngành
    p = t_info.rows[1].cells[0].paragraphs[0]
    p.paragraph_format.space_after = Pt(1.5)
    r = p.add_run("Lớp: "); format_run(r, size_pt=11)
    r = p.add_run("22CN4"); format_run(r, size_pt=11, bold=True)
    r = p.add_run("    Số điện thoại: "); format_run(r, size_pt=11)
    r = p.add_run("03388921728"); format_run(r, size_pt=11, bold=True)
    
    p = t_info.rows[1].cells[1].paragraphs[0]
    p.paragraph_format.space_after = Pt(1.5)
    r = p.add_run("Ngành: "); format_run(r, size_pt=11)
    r = p.add_run("Công nghệ thông tin"); format_run(r, size_pt=11, bold=True)
    
    # Row 2: Đề tài (merged 2 cols)
    t_info.rows[2].cells[0].merge(t_info.rows[2].cells[1])
    p = t_info.rows[2].cells[0].paragraphs[0]
    p.paragraph_format.space_after = Pt(2)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    r = p.add_run("Đề tài: "); format_run(r, size_pt=11)
    r = p.add_run("XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"); format_run(r, size_pt=11, bold=True)
    
    # Row 3: GVHD (merged 2 cols)
    t_info.rows[3].cells[0].merge(t_info.rows[3].cells[1])
    p = t_info.rows[3].cells[0].paragraphs[0]
    p.paragraph_format.space_after = Pt(1.5)
    r = p.add_run("Giáo viên hướng dẫn: "); format_run(r, size_pt=11)
    r = p.add_run("TS. Nguyễn Mạnh Thắng"); format_run(r, size_pt=11, bold=True)
    
    # Row 4: Thời gian thực hiện (merged 2 cols)
    t_info.rows[4].cells[0].merge(t_info.rows[4].cells[1])
    p = t_info.rows[4].cells[0].paragraphs[0]
    p.paragraph_format.space_after = Pt(6)
    r = p.add_run("Thời gian thực hiện: "); format_run(r, size_pt=11)
    r = p.add_run("15 tuần (105 ngày)"); format_run(r, size_pt=11, bold=True)
    r = p.add_run("            Từ ngày: "); format_run(r, size_pt=11)
    r = p.add_run("05/10/2026 – 18/01/2027"); format_run(r, size_pt=11, bold=True)
    
    # ==========================================
    # 4. TIÊU ĐỀ BẢNG KẾ HOẠCH
    # ==========================================
    p_tbl_title = doc.add_paragraph()
    p_tbl_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_tbl_title.paragraph_format.space_before = Pt(4)
    p_tbl_title.paragraph_format.space_after = Pt(6)
    r = p_tbl_title.add_run("ĐỀ XUẤT KẾ HOẠCH DỰ KIẾN THỰC HIỆN ĐỀ TÀI TỐT NGHIỆP")
    format_run(r, size_pt=11.5, bold=True)
    
    # ==========================================
    # 5. BẢNG TIẾN ĐỘ THỰC HIỆN ĐỀ TÀI (6 Giai đoạn)
    # ==========================================
    PLAN_ROWS = [
        (
            "1",
            "Nghiên cứu, tìm hiểu tổng quan về bài toán quản lý tài chính cá nhân trên thiết bị di động; khảo sát các ứng dụng tương tự hiện có trên thị trường; nghiên cứu các công nghệ liên quan: mô hình YOLOv8, OCR, Random Forest, ONNX Runtime, kiến trúc MVVM và RESTful API.",
            "1 tuần",
            "Nắm rõ yêu cầu đề tài, xác định phạm vi 5 module chính cần xây dựng; hoàn thành báo cáo tổng quan và khảo sát các giải pháp hiện có."
        ),
        (
            "2",
            "Phân tích nghiệp vụ, thiết kế hệ thống: lập sơ đồ ERD, Use Case Diagram; thiết kế kiến trúc hệ thống 3 tầng (Android MVVM – Spring Boot API – MySQL); thiết kế luồng tích hợp AI và luồng xác thực người dùng.",
            "2 tuần",
            "Lập được sơ đồ ERD, Use Case và danh sách chi tiết các chức năng của hệ thống; có bản thiết kế kiến trúc tổng thể rõ ràng."
        ),
        (
            "3",
            "Cài đặt môi trường phát triển (Android Studio, IntelliJ IDEA, Docker); xây dựng cơ sở dữ liệu MySQL theo thiết kế ERD; cấu hình Firebase Authentication; dựng khung dự án Backend (Spring Boot) và Mobile (Android Kotlin).",
            "1 tuần",
            "Có môi trường chuẩn và cơ sở dữ liệu sẵn sàng để lập trình hệ thống."
        ),
        (
            "4",
            "Xây dựng, lập trình các chức năng chính của hệ thống: đăng nhập/đăng ký (Firebase Auth, Google Sign-In); quản lý tài khoản ví; quản lý giao dịch thu chi; quản lý danh mục; thiết lập ngân sách và cảnh báo vượt hạn mức; giao dịch định kỳ; thiết kế giao diện người dùng theo Obsidian Dark Theme.",
            "4 tuần",
            "Xây dựng các chức năng nghiệp vụ cốt lõi như thiết kế ban đầu; ứng dụng hoạt động ổn định trên Android Emulator và thiết bị thật."
        ),
        (
            "5",
            "Huấn luyện mô hình AI (YOLOv8, Random Forest), chuyển đổi sang TFLite và ONNX; tích hợp nhận diện sản phẩm qua Camera (CameraX + YOLOv8 On-Device); tích hợp quét hóa đơn thông minh (Google ML Kit OCR + Gemini AI); tích hợp ONNX Runtime trên Backend; xây dựng module báo cáo trực quan (biểu đồ PieChart, BarChart). Kiểm thử toàn bộ hệ thống, sửa lỗi và tối ưu hiệu năng.",
            "5 tuần",
            "Các chức năng chạy ổn định, không lỗi; module AI hoạt động đúng quy trình; biểu đồ báo cáo hiển thị chính xác; hoàn thiện khung báo cáo tổng quan."
        ),
        (
            "6",
            "Hoàn thiện báo cáo đồ án tốt nghiệp, chuẩn bị bảo vệ.",
            "2 tuần",
            "Triển khai hoàn chỉnh hệ thống và hoàn thành báo cáo đồ án tốt nghiệp."
        ),
    ]
    
    t_plan = doc.add_table(rows=len(PLAN_ROWS) + 2, cols=4)
    t_plan.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_plan, color="808080", sz="4", val="single")
    
    col_widths_plan = [Cm(0.9), Cm(7.3), Cm(2.2), Cm(6.3)]
    for row in t_plan.rows:
        for idx, width in enumerate(col_widths_plan):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=80, bottom=80, left=100, right=100)
            
    # Set header row
    headers = ["TT", "Nội dung thực hiện", "Thời gian (tuần)", "Kết quả dự kiến"]
    hdr_row = t_plan.rows[0]
    
    # Enable cantSplit and tblHeader for header row
    trPr = hdr_row._tr.get_or_add_trPr()
    trPr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
    trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
    
    for i, h_text in enumerate(headers):
        c = hdr_row.cells[i]
        set_cell_background(c, "EAECEF")
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(3)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(h_text)
        format_run(r, size_pt=10.5, bold=True)
        
    # Populate data rows
    for r_idx, (tt, content, duration, result) in enumerate(PLAN_ROWS):
        row = t_plan.rows[r_idx + 1]
        
        # prevent row splitting across pages
        r_trPr = row._tr.get_or_add_trPr()
        r_trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
        
        # Col 0: TT
        c0 = row.cells[0]
        c0.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p0 = c0.paragraphs[0]
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p0.paragraph_format.space_before = Pt(2)
        p0.paragraph_format.space_after = Pt(2)
        r = p0.add_run(tt)
        format_run(r, size_pt=10.5, bold=True)
        
        # Col 1: Content
        c1 = row.cells[1]
        p1 = c1.paragraphs[0]
        p1.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p1.paragraph_format.line_spacing = 1.15
        p1.paragraph_format.space_before = Pt(2)
        p1.paragraph_format.space_after = Pt(2)
        r = p1.add_run(content)
        format_run(r, size_pt=10)
        
        # Col 2: Duration
        c2 = row.cells[2]
        c2.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p2 = c2.paragraphs[0]
        p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p2.paragraph_format.space_before = Pt(2)
        p2.paragraph_format.space_after = Pt(2)
        r = p2.add_run(duration)
        format_run(r, size_pt=10.5, bold=True)
        
        # Col 3: Result
        c3 = row.cells[3]
        p3 = c3.paragraphs[0]
        p3.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p3.paragraph_format.line_spacing = 1.15
        p3.paragraph_format.space_before = Pt(2)
        p3.paragraph_format.space_after = Pt(2)
        r = p3.add_run(result)
        format_run(r, size_pt=10)

    # Summary Row
    sum_row = t_plan.rows[-1]
    s_trPr = sum_row._tr.get_or_add_trPr()
    s_trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
    
    for c in sum_row.cells:
        set_cell_background(c, "F5F6F8")
        
    c0 = sum_row.cells[0]
    c0.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    p = c0.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("TỔNG"); format_run(r, size_pt=10, bold=True)
    
    c1 = sum_row.cells[1]
    p = c1.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    r = p.add_run("Toàn bộ 6 giai đoạn thực hiện đề tài tốt nghiệp"); format_run(r, size_pt=10, bold=True)
    
    c2 = sum_row.cells[2]
    c2.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    p = c2.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("15 tuần\n(105 ngày)"); format_run(r, size_pt=10, bold=True)
    
    c3 = sum_row.cells[3]
    p = c3.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    r = p.add_run("Hoàn thiện toàn bộ hệ sinh thái phần mềm di động, dịch vụ máy chủ Backend AI, 86 bài kiểm thử tự động và hoàn thành bản báo cáo ĐATN."); format_run(r, size_pt=10, bold=True)
    
    # ==========================================
    # 6. PHẦN CHỮ KÝ XÁC NHẬN
    # ==========================================
    p_date = doc.add_paragraph()
    p_date.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_date.paragraph_format.space_before = Pt(12)
    p_date.paragraph_format.space_after = Pt(4)
    r = p_date.add_run("Hà Nội, ngày …. tháng 10 năm 2026")
    format_run(r, size_pt=11, italic=True)
    
    t_sig = doc.add_table(rows=2, cols=2)
    t_sig.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_sig)
    
    col_w_sig = [Cm(8.35), Cm(8.35)]
    for row in t_sig.rows:
        for idx, width in enumerate(col_w_sig):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=20, bottom=20, left=40, right=40)
            
    # Row 0: Titles
    p_left_title = t_sig.rows[0].cells[0].paragraphs[0]
    p_left_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_left_title.paragraph_format.space_after = Pt(2)
    r = p_left_title.add_run("GIÁO VIÊN HƯỚNG DẪN")
    format_run(r, size_pt=11.5, bold=True)
    
    p_left_sub = t_sig.rows[0].cells[0].add_paragraph()
    p_left_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_left_sub.paragraph_format.space_after = Pt(0)
    r = p_left_sub.add_run("(Ký và ghi rõ họ tên)")
    format_run(r, size_pt=10.5, italic=True)
    
    p_right_title = t_sig.rows[0].cells[1].paragraphs[0]
    p_right_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_right_title.paragraph_format.space_after = Pt(2)
    r = p_right_title.add_run("SINH VIÊN THỰC HIỆN ĐỀ TÀI")
    format_run(r, size_pt=11.5, bold=True)
    
    p_right_sub = t_sig.rows[0].cells[1].add_paragraph()
    p_right_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_right_sub.paragraph_format.space_after = Pt(0)
    r = p_right_sub.add_run("(Ký và ghi rõ họ tên)")
    format_run(r, size_pt=10.5, italic=True)
    
    # Row 1: Signatures names with vertical space
    p_left_name = t_sig.rows[1].cells[0].paragraphs[0]
    p_left_name.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_left_name.paragraph_format.space_before = Pt(55) # spacing for actual signature
    p_left_name.paragraph_format.space_after = Pt(0)
    r = p_left_name.add_run("TS. Nguyễn Mạnh Thắng")
    format_run(r, size_pt=11.5, bold=True)
    
    p_right_name = t_sig.rows[1].cells[1].paragraphs[0]
    p_right_name.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_right_name.paragraph_format.space_before = Pt(55)
    p_right_name.paragraph_format.space_after = Pt(0)
    r = p_right_name.add_run("Nguyễn Duy Khánh")
    format_run(r, size_pt=11.5, bold=True)

    # ==========================================
    # 7. PHỤ LỤC TIẾN ĐỘ CHI TIẾT 15 TUẦN (WEEKLY MILESTONES)
    # ==========================================
    doc.add_page_break()
    
    p_app_title = doc.add_paragraph()
    p_app_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_app_title.paragraph_format.space_before = Pt(10)
    p_app_title.paragraph_format.space_after = Pt(3)
    r = p_app_title.add_run("PHỤ LỤC: LỊCH TRÌNH CHI TIẾT 15 TUẦN THỰC HIỆN ĐỒ ÁN")
    format_run(r, size_pt=13, bold=True, color_rgb=RGBColor(0, 32, 96))
    
    p_app_sub = doc.add_paragraph()
    p_app_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_app_sub.paragraph_format.space_before = Pt(0)
    p_app_sub.paragraph_format.space_after = Pt(10)
    r = p_app_sub.add_run("(Kèm theo Bảng kế hoạch Đồ án tốt nghiệp Kỹ sư CNTT - Khóa 2022 - 2027)")
    format_run(r, size_pt=10.5, italic=True)
    
    WEEKLY_SCHEDULE = [
        ("Tuần 1", "05/10/2026 - 11/10/2026", "Khảo sát bài toán quản lý tài chính cá nhân; nghiên cứu cơ sở lý thuyết YOLOv8, OCR, Random Forest, ONNX Runtime; xác định 5 module phần mềm cốt lõi.", "Báo cáo tổng quan lý thuyết và đề cương nghiên cứu chi tiết."),
        ("Tuần 2", "12/10/2026 - 18/10/2026", "Phân tích yêu cầu chức năng (FR) và phi chức năng (NFR); xây dựng biểu đồ phân cấp chức năng (BFC); thiết kế sơ đồ Use Case toàn hệ thống.", "Tài liệu đặc tả yêu cầu và sơ đồ Use Case chi tiết."),
        ("Tuần 3", "19/10/2026 - 25/10/2026", "Thiết kế kiến trúc hệ thống 3 tầng; thiết kế Pipeline AI 2 giai đoạn; thiết kế cơ sở dữ liệu quan hệ chuẩn 3NF (ERD 9 bảng dữ liệu).", "Bản vẽ thiết kế kiến trúc hệ thống, sơ đồ tuần tự và ERD CSDL."),
        ("Tuần 4", "26/10/2026 - 01/11/2026", "Cài đặt môi trường phát triển (Android Studio, IntelliJ IDEA, Docker Compose); thiết lập CSDL MySQL 8.0 utf8mb4; cấu hình Firebase ID Token; khởi tạo khung dự án.", "Môi trường phát triển sẵn sàng; máy chủ và CSDL kết nối thông suốt."),
        ("Tuần 5", "02/11/2026 - 08/11/2026", "Lập trình phân hệ Người dùng và Xác thực (Firebase Auth, Google Sign-In 1 chạm, JWT Stateless); xây dựng phân hệ Quản lý tài khoản ví tài chính (Tiền mặt, Ngân hàng, Ví điện tử).", "Hoàn thành API xác thực, API quản lý ví; giao diện đăng nhập và ví hoạt động tốt."),
        ("Tuần 6", "09/11/2026 - 15/11/2026", "Lập trình phân hệ Quản lý Giao dịch thu chi (CRUD giao dịch, tự động cộng/trừ số dư, lọc theo thời gian, đính kèm chứng từ); quản lý Danh mục chi tiêu chuẩn tiếng Việt.", "Hoàn thiện nghiệp vụ ghi chép thu chi cốt lõi trên cả Backend và Mobile."),
        ("Tuần 7", "16/11/2026 - 22/11/2026", "Lập trình phân hệ Quản lý Hạn mức ngân sách với cơ chế cảnh báo đổi màu 3 cấp độ (<80% Xanh, 80-100% Cam, >100% Đỏ); lập trình giao dịch định kỳ (Recurring Transaction) với Cronjob.", "Ngân sách cảnh báo thời gian thực và cronjob tự động tạo giao dịch định kỳ."),
        ("Tuần 8", "23/11/2026 - 29/11/2026", "Thiết kế và chuẩn hóa toàn bộ giao diện người dùng theo chuẩn Obsidian Dark Theme cao cấp trên Android; tối ưu hóa ViewBinding và Navigation Component.", "Giao diện Android hoàn thiện đồng bộ, trực quan, chuyên nghiệp."),
        ("Tuần 9", "30/11/2026 - 06/12/2026", "Thu thập và gán nhãn tập dữ liệu ảnh sản phẩm tiêu dùng; huấn luyện mô hình YOLOv8n trên GPU; đánh giá chỉ số mAP@0.5, Loss; xuất sang mô hình tối ưu TFLite Float32 (6.3MB).", "Mô hình YOLOv8n TFLite đạt mAP@0.5 = 89.2% sẵn sàng tích hợp On-Device."),
        ("Tuần 10", "07/12/2026 - 13/12/2026", "Tích hợp CameraX và YOLOv8n TFLite vào ứng dụng Android, xử lý vẽ Bounding Box thời gian thực; huấn luyện Random Forest 200 cây và nhúng vào Backend bằng Microsoft ONNX Runtime.", "Camera AI nhận diện vật thể đạt 34-42 FPS; Backend phân loại danh mục dưới 2ms."),
        ("Tuần 11", "14/12/2026 - 20/12/2026", "Tích hợp Google ML Kit Text Recognition OCR bóc tách văn bản offline; tích hợp Google Gemini 2.5 Flash LLM trích xuất JSON hóa đơn thông minh; tự điền form giao dịch.", "Tính năng quét hóa đơn tự động hoàn thiện, thời gian xử lý trung bình dưới 2.5s."),
        ("Tuần 12", "21/12/2026 - 27/12/2026", "Tích hợp thư viện MPAndroidChart xây dựng phân hệ Báo cáo thống kê: biểu đồ cơ cấu chi tiêu PieChart Donut và biểu đồ xu hướng thu chi BarChart 12 tháng.", "Module báo cáo tài chính trực quan, đa chiều hoạt động chính xác."),
        ("Tuần 13", "28/12/2026 - 03/01/2027", "Xây dựng bộ kiểm thử tự động toàn diện (86 bài test JUnit/Mockito/MockMvc); đo kiểm hiệu năng CPU, RAM, pin; tối ưu hóa truy vấn HikariCP và bộ nhớ đệm.", "86/86 bài kiểm thử tự động đạt 100% Passed; hệ thống vận hành ổn định."),
        ("Tuần 14", "04/01/2027 - 10/01/2027", "Biên soạn toàn văn Báo cáo Đồ án Tốt nghiệp Kỹ sư CNTT theo đúng cấu trúc 4 chương, chuẩn format Khoa CNTT - ĐH Kiến trúc Hà Nội; xuất bản tài liệu hướng dẫn cài đặt Docker.", "Bản thảo báo cáo đồ án hoàn chỉnh, đầy đủ hình ảnh, bảng biểu và phụ lục."),
        ("Tuần 15", "11/01/2027 - 18/01/2027", "Rà soát kiểm tra kỹ thuật lần cuối cùng GVHD; đóng gói mã nguồn, Docker Compose và file APK; thiết kế Slide thuyết trình PowerPoint và chuẩn bị hồ sơ bảo vệ ĐATN.", "Hoàn tất 100% hồ sơ đồ án tốt nghiệp, sẵn sàng bảo vệ trước Hội đồng chấm thi.")
    ]
    
    t_week = doc.add_table(rows=len(WEEKLY_SCHEDULE) + 1, cols=4)
    t_week.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_week, color="808080", sz="4", val="single")
    
    col_w_week = [Cm(1.4), Cm(3.3), Cm(6.5), Cm(5.5)]
    for row in t_week.rows:
        for idx, width in enumerate(col_w_week):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=70, bottom=70, left=90, right=90)
            
    # Week Header
    hdr_week = t_week.rows[0]
    w_trPr = hdr_week._tr.get_or_add_trPr()
    w_trPr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
    w_trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
    
    week_headers = ["Tuần", "Mốc thời gian", "Nội dung triển khai chi tiết", "Sản phẩm / Kết quả bàn giao"]
    for i, h_text in enumerate(week_headers):
        c = hdr_week.cells[i]
        set_cell_background(c, "EAECEF")
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = c.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(3)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(h_text)
        format_run(r, size_pt=10, bold=True)
        
    for r_idx, (w_num, w_time, w_content, w_output) in enumerate(WEEKLY_SCHEDULE):
        row = t_week.rows[r_idx + 1]
        r_trPr = row._tr.get_or_add_trPr()
        r_trPr.append(parse_xml(f'<w:cantSplit {nsdecls("w")}/>'))
        
        # Col 0: Week
        c0 = row.cells[0]
        c0.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = c0.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(w_num); format_run(r, size_pt=9.5, bold=True)
        
        # Col 1: Time
        c1 = row.cells[1]
        c1.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = c1.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(w_time); format_run(r, size_pt=9, bold=False)
        
        # Col 2: Content
        c2 = row.cells[2]
        p = c2.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p.paragraph_format.line_spacing = 1.15
        r = p.add_run(w_content); format_run(r, size_pt=9.5)
        
        # Col 3: Output
        c3 = row.cells[3]
        p = c3.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p.paragraph_format.line_spacing = 1.15
        r = p.add_run(w_output); format_run(r, size_pt=9.5)

    doc.save(output_path)
    print(f"Generated successfully: {output_path}")

if __name__ == "__main__":
    dst_workspace = r"d:\Project\KHMT\DATN\KE_HOACH_THUC_HIEN_DATN.docx"
    dst_downloads = r"C:\Users\ADMIN\Downloads\KE_HOACH_THUC_HIEN_DATN.docx"
    dst_kh_original = r"C:\Users\ADMIN\Downloads\KH DATN - Nguyễn Duy Khánh - 2255010114.docx"
    
    build_plan_doc(dst_workspace)
    
    # copy to downloads
    try:
        shutil.copy2(dst_workspace, dst_downloads)
        print(f"Copied to: {dst_downloads}")
    except Exception as e:
        print(f"Could not copy to {dst_downloads}: {e}")
        
    try:
        shutil.copy2(dst_workspace, dst_kh_original)
        print(f"Updated original file: {dst_kh_original}")
    except Exception as e:
        print(f"Could not update {dst_kh_original}: {e}")
