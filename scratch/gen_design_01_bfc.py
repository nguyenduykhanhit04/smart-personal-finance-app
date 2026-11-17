# -*- coding: utf-8 -*-
"""
Generate:
1. 01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.docx
2. 01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.md
"""
import os
import docx
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
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

def build_bfc_doc(docx_path, md_path):
    doc = docx.Document()
    
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    doc.core_properties.title = "PHÂN TÍCH YÊU CẦU HỆ THỐNG VÀ BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG (BFC)"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    
    style = doc.styles['Normal']
    style.font.name = 'Times New Roman'
    style.font.size = Pt(11)
    style.paragraph_format.line_spacing = 1.2
    style.paragraph_format.space_before = Pt(0)
    style.paragraph_format.space_after = Pt(3)
    
    # Header Table
    t_head = doc.add_table(rows=1, cols=2)
    t_head.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_head)
    for r in t_head.rows:
        r.cells[0].width = Cm(8.0)
        r.cells[1].width = Cm(8.5)
    p_l = t_head.rows[0].cells[0].paragraphs[0]
    p_l.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_l.add_run("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI\nKHOA CÔNG NGHỆ THÔNG TIN")
    format_run(r, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    p_r = t_head.rows[0].cells[1].paragraphs[0]
    p_r.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r2 = p_r.add_run("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM\nĐộc lập - Tự do - Hạnh phúc")
    format_run(r2, size_pt=10, bold=True)
    
    # Title
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(12)
    p_title.paragraph_format.space_after = Pt(2)
    r_t = p_title.add_run("TÀI LIỆU PHÂN TÍCH YÊU CẦU HỆ THỐNG\nVÀ BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG (BFC)\n")
    format_run(r_t, size_pt=14, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_sub = p_title.add_run("HỆ THỐNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN THÔNG MINH (SMART PERSONAL FINANCE APP)")
    format_run(r_sub, size_pt=11, bold=True, color_rgb=RGBColor(180, 0, 0))
    
    # Metadata
    t_meta = doc.add_table(rows=4, cols=2)
    t_meta.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_meta, color="B0C4DE", sz="4")
    for r in t_meta.rows:
        r.cells[0].width = Cm(4.5)
        r.cells[1].width = Cm(12.0)
        set_cell_margins(r.cells[0], top=35, bottom=35, left=50, right=50)
        set_cell_margins(r.cells[1], top=35, bottom=35, left=50, right=50)
        set_cell_background(r.cells[0], "F0F4F8")
        
    meta_info = [
        ("Đề tài tốt nghiệp:", "Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo"),
        ("Sinh viên thực hiện:", "Nguyễn Duy Khánh — MSV: 2255010114 — Lớp: 22CN4"),
        ("Giảng viên hướng dẫn:", "TS. Nguyễn Mạnh Thắng"),
        ("Đơn vị đào tạo:", "Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội")
    ]
    for idx, (k, v) in enumerate(meta_info):
        r0 = t_meta.rows[idx].cells[0].paragraphs[0].add_run(k)
        format_run(r0, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
        r1 = t_meta.rows[idx].cells[1].paragraphs[0].add_run(v)
        format_run(r1, size_pt=10, bold=(idx == 0))

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(text)
        format_run(r, size_pt=12, bold=True, color_rgb=RGBColor(0, 51, 102))
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after = Pt(2)
        r = p.add_run(text)
        format_run(r, size_pt=11, bold=True, color_rgb=RGBColor(0, 70, 140))
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

    add_h1("1. PHÂN TÍCH YÊU CẦU HỆ THỐNG")
    add_h2("1.1. Yêu cầu chức năng (Functional Requirements - FR)")
    add_p("Hệ thống được thiết kế bao gồm 9 nhóm yêu cầu chức năng cốt lõi:")
    
    t_fr = doc.add_table(rows=10, cols=4)
    t_fr.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_fr, color="888888", sz="4")
    col_w_fr = [Cm(1.8), Cm(4.2), Cm(8.5), Cm(2.0)]
    
    fr_headers = ["Mã FR", "Tên phân hệ / Chức năng", "Mô tả chi tiết nghiệp vụ", "Ưu tiên"]
    for c_idx, h in enumerate(fr_headers):
        cell = t_fr.rows[0].cells[c_idx]
        cell.width = col_w_fr[c_idx]
        set_cell_margins(cell, top=50, bottom=50, left=45, right=45)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    fr_data = [
        ("FR-01", "Quản lý Xác thực & Người dùng", "Đăng ký tài khoản Email; Đăng nhập bằng Email hoặc Google Sign-In 1 chạm; Đồng bộ Firebase Auth với MySQL; Cập nhật hồ sơ cá nhân.", "Cao (High)"),
        ("FR-02", "Quản lý Tài khoản Ví", "Tạo nhiều loại ví (Tiền mặt, Ngân hàng, Ví điện tử); Theo dõi số dư tức thời; Tự động tăng/giảm số dư khi có giao dịch; Tính tổng số dư ròng.", "Cao (High)"),
        ("FR-03", "Quản lý Danh mục Thu Chi", "Cung cấp sẵn 11 danh mục chuẩn; Tạo danh mục cá nhân hóa; Tùy chỉnh màu sắc Hex và biểu tượng icon Material Design; Phân loại Thu/Chi.", "Cao (High)"),
        ("FR-04", "Quản lý Giao dịch Thu Chi", "Thêm, sửa, xóa giao dịch; Tự động bù trừ và rollback số dư ví an toàn; Đính kèm ảnh chứng từ; Lọc và tìm kiếm giao dịch theo ngày/tháng.", "Cao (High)"),
        ("FR-05", "Quản lý Ngân sách & Cảnh báo", "Đặt hạn mức chi tiêu theo tháng cho danh mục; Theo dõi tiến độ; Cảnh báo trực quan 3 cấp màu: Xanh (<80%), Vàng cam (80-100%), Đỏ (>100%).", "Cao (High)"),
        ("FR-06", "Quản lý Giao dịch Định kỳ", "Lên lịch lặp tự động (ngày, tuần, tháng, năm); Cronjob 00:00 tự động sinh giao dịch; Endpoint `/trigger` kích hoạt tức thì phục vụ demo.", "Trung bình"),
        ("FR-07", "Quét Hóa đơn Thông minh (AI)", "Chụp ảnh hóa đơn; Google ML Kit OCR nhận dạng chữ offline trên máy; Google Gemini 2.5 Flash bóc tách JSON (tiền, ngày, quán); Tự điền form.", "Cao (High)"),
        ("FR-08", "Nhận diện Sản phẩm Camera AI", "CameraX + YOLOv8n TFLite vẽ Bounding Box 18 lớp thời gian thực (35-40 FPS); Trích xuất 27 đặc trưng; Random Forest ONNX gợi ý danh mục < 2ms.", "Cao (High)"),
        ("FR-09", "Báo cáo & Thống kê Tài chính", "Thống kê tổng thu, tổng chi và dòng tiền theo tháng/năm; Biểu đồ tròn PieChart tỷ trọng danh mục; Biểu đồ cột BarChart so sánh 12 tháng.", "Cao (High)")
    ]
    for r_idx, r_data in enumerate(fr_data, start=1):
        row = t_fr.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_fr[c_idx]
            set_cell_margins(cell, top=40, bottom=40, left=45, right=45)
            if r_idx % 2 == 1:
                set_cell_background(cell, "F9FBFC")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 51, 102))
            elif c_idx == 3:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(180, 0, 0) if "Cao" in val else RGBColor(0, 102, 0))
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    add_h2("1.2. Yêu cầu phi chức năng (Non-Functional Requirements - NFR)")
    add_p("• NFR-01 (Hiệu năng): YOLOv8 chạy trên điện thoại đạt 30–45 FPS; Suy luận Random Forest ONNX trên Java Backend < 2ms; OCR offline < 500ms; Bóc tách Gemini LLM < 2.5s.")
    add_p("• NFR-02 (Bảo mật): Xác thực không trạng thái (Stateless) qua Firebase ID Token; Không lưu mật khẩu thô trong MySQL; Lớp lọc `FirebaseAuthFilter` bảo vệ toàn bộ API nghiệp vụ.")
    add_p("• NFR-03 (Độ tin cậy): Tự động chuyển sang Rule-based Fallback khi không nạp được mô hình ONNX native; Đóng gói Docker Compose có tự động khởi động lại `restart: unless-stopped`.")
    add_p("• NFR-04 (Giao diện): Obsidian Dark Theme nền đen sâu `#090D16`, bề mặt thẻ `#161B2A`, điểm nhấn Royal Blue `#3B82F6` bảo vệ mắt và tiết kiệm pin OLED.")

    add_h1("2. BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG TỔNG QUÁT (BFC LEVEL 0)")
    add_p("Biểu đồ phân cấp chức năng tổng quát (BFC Level 0) chia toàn bộ hệ thống Smart Personal Finance App thành 5 phân hệ lớn:")
    
    # BFC Level 0 table/diagram representation
    t_bfc0 = doc.add_table(rows=6, cols=3)
    t_bfc0.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc0, color="003366", sz="4")
    col_w_bfc0 = [Cm(1.8), Cm(6.0), Cm(8.7)]
    
    bfc0_headers = ["Mã", "Tên Phân hệ Cấp 1", "Các chức năng cốt lõi đảm nhiệm"]
    for c_idx, h in enumerate(bfc0_headers):
        cell = t_bfc0.rows[0].cells[c_idx]
        cell.width = col_w_bfc0[c_idx]
        set_cell_margins(cell, top=50, bottom=50, left=45, right=45)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    bfc0_data = [
        ("1.0", "Quản lý Tài khoản & Xác thực", "Đăng ký, Đăng nhập (Email, Google 1 chạm), Quản lý hồ sơ, Cấu hình IP Server."),
        ("2.0", "Quản lý Tài chính & Giao dịch", "Quản lý ví tài chính, Danh mục thu chi, Ghi nhận thu/chi, Bù trừ số dư ví, Đính kèm ảnh."),
        ("3.0", "Quản lý Ngân sách & Định kỳ", "Thiết lập hạn mức chi, Giám sát tiến độ, Cảnh báo đổi màu 3 cấp, Giao dịch định kỳ tự động."),
        ("4.0", "Trí tuệ Nhân tạo & Thị giác Máy tính", "Quét hóa đơn OCR + Gemini AI, Nhận diện sản phẩm qua Camera YOLOv8, Phân loại ONNX Random Forest."),
        ("5.0", "Báo cáo & Thống kê Tài chính", "Thống kê tổng thu/chi theo tháng/năm, Biểu đồ tròn PieChart cơ cấu chi, Biểu đồ cột BarChart 12 tháng.")
    ]
    for r_idx, r_data in enumerate(bfc0_data, start=1):
        row = t_bfc0.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_bfc0[c_idx]
            set_cell_margins(cell, top=40, bottom=40, left=45, right=45)
            if r_idx % 2 == 1:
                set_cell_background(cell, "F9FBFC")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(0, 51, 102))
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9.5, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    add_h1("3. CÁC BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG CON CHI TIẾT (BFC LEVEL 1 & LEVEL 2)")
    add_p("Toàn bộ 5 phân hệ lớn ở trên được phân rã chi tiết thành các biểu đồ phân cấp con (Level 1 và Level 2) dưới đây:")

    # BFC 1.0
    add_h2("3.1. Phân cấp Chức năng 1.0: Quản lý Tài khoản & Xác thực")
    t_bfc1 = doc.add_table(rows=5, cols=3)
    t_bfc1.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc1, color="B0B0B0", sz="4")
    col_w_sub = [Cm(1.8), Cm(6.0), Cm(8.7)]
    for c_idx, h in enumerate(["Mã", "Chức năng Cấp 2", "Chi tiết chức năng Cấp 3"]):
        cell = t_bfc1.rows[0].cells[c_idx]
        cell.width = col_w_sub[c_idx]
        set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
        set_cell_background(cell, "004080")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(255, 255, 255))
    bfc1_rows = [
        ("1.1", "Đăng ký tài khoản", "1.1.1 Đăng ký Email/Mật khẩu; 1.1.2 Kiểm tra độ mạnh mật khẩu; 1.1.3 Đồng bộ hồ sơ vào CSDL MySQL."),
        ("1.2", "Đăng nhập hệ thống", "1.2.1 Đăng nhập Email/Mật khẩu; 1.2.2 Đăng nhập Google Sign-In 1 chạm; 1.2.3 Lấy Firebase ID Token."),
        ("1.3", "Quản lý hồ sơ người dùng", "1.3.1 Xem thông tin tài khoản; 1.3.2 Cập nhật Họ tên & SĐT; 1.3.3 Tải ảnh đại diện; 1.3.4 Đăng xuất an toàn."),
        ("1.4", "Tiện ích cấu hình IP Server", "1.4.1 Nhấn giữ Logo app 2 giây; 1.4.2 Tùy biến địa chỉ IP Server Backend không cần build lại APK.")
    ]
    for r_idx, r_data in enumerate(bfc1_rows, start=1):
        row = t_bfc1.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_sub[c_idx]
            set_cell_margins(cell, top=35, bottom=35, left=40, right=40)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    # BFC 2.0
    add_h2("3.2. Phân cấp Chức năng 2.0: Quản lý Tài chính & Giao dịch")
    t_bfc2 = doc.add_table(rows=4, cols=3)
    t_bfc2.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc2, color="B0B0B0", sz="4")
    for c_idx, h in enumerate(["Mã", "Chức năng Cấp 2", "Chi tiết chức năng Cấp 3"]):
        cell = t_bfc2.rows[0].cells[c_idx]
        cell.width = col_w_sub[c_idx]
        set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
        set_cell_background(cell, "004080")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(255, 255, 255))
    bfc2_rows = [
        ("2.1", "Quản lý Ví tài chính", "2.1.1 Thêm ví (Tiền mặt, Ngân hàng, Ví điện tử); 2.1.2 Đổi tên ví & cập nhật số dư; 2.1.3 Xóa ví liên đới; 2.1.4 Tính tổng số dư ròng Dashboard."),
        ("2.2", "Quản lý Danh mục Thu Chi", "2.2.1 Xem 11 danh mục mặc định hệ thống; 2.2.2 Thêm danh mục cá nhân; 2.2.3 Tùy biến mã màu Hex & Icon Material; 2.2.4 Phân loại Thu/Chi."),
        ("2.3", "Quản lý Giao dịch Thu Chi", "2.3.1 Thêm giao dịch thu/chi; 2.3.2 Tự động trừ/cộng số dư ví; 2.3.3 Sửa giao dịch & bù trừ số dư; 2.3.4 Xóa giao dịch & rollback số dư; 2.3.5 Đính kèm ảnh hóa đơn/chứng từ; 2.3.6 Lọc theo ngày/tháng.")
    ]
    for r_idx, r_data in enumerate(bfc2_rows, start=1):
        row = t_bfc2.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_sub[c_idx]
            set_cell_margins(cell, top=35, bottom=35, left=40, right=40)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    # BFC 3.0
    add_h2("3.3. Phân cấp Chức năng 3.0: Quản lý Ngân sách & Định kỳ")
    t_bfc3 = doc.add_table(rows=4, cols=3)
    t_bfc3.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc3, color="B0B0B0", sz="4")
    for c_idx, h in enumerate(["Mã", "Chức năng Cấp 2", "Chi tiết chức năng Cấp 3"]):
        cell = t_bfc3.rows[0].cells[c_idx]
        cell.width = col_w_sub[c_idx]
        set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
        set_cell_background(cell, "004080")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(255, 255, 255))
    bfc3_rows = [
        ("3.1", "Thiết lập Ngân sách", "3.1.1 Đặt hạn mức chi theo danh mục/tháng; 3.1.2 Đặt ngân sách tổng; 3.1.3 Sửa/Xóa hạn mức ngân sách."),
        ("3.2", "Giám sát & Cảnh báo đa cấp", "3.2.1 Theo dõi số tiền đã tiêu; 3.2.2 Tính hạn mức an toàn mỗi ngày; 3.2.3 Thanh tiến độ đổi màu 3 cấp (Xanh <80%, Vàng 80-100%, Đỏ >100%)."),
        ("3.3", "Quản lý Giao dịch Định kỳ", "3.3.1 Tạo lịch định kỳ (ngày, tuần, tháng, năm); 3.3.2 Tạm dừng/Kích hoạt lại lịch; 3.3.3 Cronjob 00:00 tự động sinh giao dịch; 3.3.4 Kích hoạt tức thì qua API `/trigger`.")
    ]
    for r_idx, r_data in enumerate(bfc3_rows, start=1):
        row = t_bfc3.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_sub[c_idx]
            set_cell_margins(cell, top=35, bottom=35, left=40, right=40)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    # BFC 4.0
    add_h2("3.4. Phân cấp Chức năng 4.0: Trí tuệ Nhân tạo & Thị giác Máy tính")
    t_bfc4 = doc.add_table(rows=4, cols=3)
    t_bfc4.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc4, color="B0B0B0", sz="4")
    for c_idx, h in enumerate(["Mã", "Chức năng Cấp 2", "Chi tiết chức năng Cấp 3"]):
        cell = t_bfc4.rows[0].cells[c_idx]
        cell.width = col_w_sub[c_idx]
        set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
        set_cell_background(cell, "004080")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(255, 255, 255))
    bfc4_rows = [
        ("4.1", "Quét Hóa đơn Thông minh (OCR + LLM)", "4.1.1 Chụp/chọn ảnh hóa đơn; 4.1.2 Google ML Kit OCR trích xuất chữ offline (400ms); 4.1.3 Google Gemini 2.5 Flash bóc tách JSON tiền, ngày, cửa hàng; 4.1.4 Điền tự động form giao dịch."),
        ("4.2", "Nhận diện Sản phẩm qua Camera AI", "4.2.1 CameraX thu nhận khung hình; 4.2.2 YOLOv8n TFLite nhận diện 18 lớp On-Device; 4.2.3 Vẽ Bounding Box thời gian thực 35-40 FPS; 4.2.4 Gửi danh sách vật thể lên Backend."),
        ("4.3", "Phân loại Danh mục Tự động (Machine Learning)", "4.3.1 Trích xuất vector 27 đặc trưng kỹ thuật; 4.3.2 Suy luận qua Random Forest 200 cây nạp bằng Microsoft ONNX Runtime Java (< 2ms); 4.3.3 Tự động chọn danh mục gợi ý; 4.3.4 Rule-based Fallback an toàn.")
    ]
    for r_idx, r_data in enumerate(bfc4_rows, start=1):
        row = t_bfc4.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_sub[c_idx]
            set_cell_margins(cell, top=35, bottom=35, left=40, right=40)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    # BFC 5.0
    add_h2("3.5. Phân cấp Chức năng 5.0: Báo cáo & Thống kê Tài chính")
    t_bfc5 = doc.add_table(rows=3, cols=3)
    t_bfc5.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_bfc5, color="B0B0B0", sz="4")
    for c_idx, h in enumerate(["Mã", "Chức năng Cấp 2", "Chi tiết chức năng Cấp 3"]):
        cell = t_bfc5.rows[0].cells[c_idx]
        cell.width = col_w_sub[c_idx]
        set_cell_margins(cell, top=40, bottom=40, left=40, right=40)
        set_cell_background(cell, "004080")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(255, 255, 255))
    bfc5_rows = [
        ("5.1", "Thống kê Thu Chi Dòng tiền", "5.1.1 Thống kê tổng thu, tổng chi và số dư theo tháng; 5.1.2 Thống kê biến động dòng tiền 12 tháng theo năm tài chính; 5.1.3 Lọc theo khoảng thời gian tùy chọn."),
        ("5.2", "Trực quan hóa Đa chiều với MPAndroidChart", "5.2.1 Biểu đồ tròn PieChart tỷ trọng từng danh mục chi tiêu; 5.2.2 Chạm để làm nổi bật lát cắt và xem số tiền; 5.2.3 Biểu đồ cột BarChart so sánh đối sánh thu/chi 12 tháng.")
    ]
    for r_idx, r_data in enumerate(bfc5_rows, start=1):
        row = t_bfc5.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_sub[c_idx]
            set_cell_margins(cell, top=35, bottom=35, left=40, right=40)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    doc.save(docx_path)
    print(f"Successfully generated DOCX: {docx_path}")

    # Generate Markdown
    md_content = f"""# TÀI LIỆU PHÂN TÍCH YÊU CẦU HỆ THỐNG VÀ BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG (BFC)

**Đề tài:** XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Mã SV: 2255010114 — Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 1. PHÂN TÍCH YÊU CẦU HỆ THỐNG

### 1.1. Yêu cầu chức năng (Functional Requirements - FR)
Hệ thống gồm 9 nhóm yêu cầu chức năng cốt lõi:

| Mã FR | Tên phân hệ / Chức năng | Mô tả chi tiết nghiệp vụ | Mức ưu tiên |
|:---:|:---|:---|:---:|
| **FR-01** | **Quản lý Xác thực & Người dùng** | Đăng ký Email; Đăng nhập Email hoặc Google Sign-In 1 chạm; Đồng bộ Firebase Auth với MySQL; Cập nhật hồ sơ cá nhân. | Bắt buộc (High) |
| **FR-02** | **Quản lý Tài khoản Ví** | Tạo nhiều loại ví (Tiền mặt, Ngân hàng, Ví điện tử); Theo dõi số dư tức thời; Tự động tăng/giảm số dư; Tính tổng số dư ròng. | Bắt buộc (High) |
| **FR-03** | **Quản lý Danh mục Thu Chi** | Cung cấp sẵn 11 danh mục chuẩn; Tạo danh mục cá nhân hóa; Tùy chỉnh màu sắc Hex và icon Material Design; Phân loại Thu/Chi. | Bắt buộc (High) |
| **FR-04** | **Quản lý Giao dịch Thu Chi** | Thêm, sửa, xóa giao dịch; Tự động bù trừ và rollback số dư ví an toàn; Đính kèm ảnh chứng từ; Lọc và tìm kiếm giao dịch theo ngày/tháng. | Bắt buộc (High) |
| **FR-05** | **Quản lý Ngân sách & Cảnh báo** | Đặt hạn mức chi theo tháng cho danh mục; Theo dõi tiến độ; Cảnh báo trực quan 3 cấp màu: Xanh (<80%), Vàng cam (80-100%), Đỏ (>100%). | Bắt buộc (High) |
| **FR-06** | **Quản lý Giao dịch Định kỳ** | Lên lịch lặp tự động (ngày, tuần, tháng, năm); Cronjob 00:00 tự động sinh giao dịch; Endpoint `/trigger` kích hoạt tức thì phục vụ demo. | Khuyến nghị (Medium) |
| **FR-07** | **Quét Hóa đơn Thông minh (AI)** | Chụp ảnh hóa đơn; Google ML Kit OCR nhận dạng chữ offline trên máy; Google Gemini 2.5 Flash bóc tách JSON (tiền, ngày, quán); Tự điền form. | Bắt buộc (High) |
| **FR-08** | **Nhận diện Sản phẩm Camera AI** | CameraX + YOLOv8n TFLite vẽ Bounding Box 18 lớp thời gian thực (35-40 FPS); Trích xuất 27 đặc trưng; Random Forest ONNX gợi ý danh mục < 2ms. | Bắt buộc (High) |
| **FR-09** | **Báo cáo & Thống kê Tài chính** | Thống kê tổng thu, tổng chi và dòng tiền theo tháng/năm; Biểu đồ tròn PieChart tỷ trọng danh mục; Biểu đồ cột BarChart so sánh 12 tháng. | Bắt buộc (High) |

### 1.2. Yêu cầu phi chức năng (Non-Functional Requirements - NFR)
- **NFR-01 (Hiệu năng):** YOLOv8 chạy trên điện thoại đạt 30–45 FPS; Suy luận Random Forest ONNX trên Java Backend < 2ms; OCR offline < 500ms; Bóc tách Gemini LLM < 2.5s.
- **NFR-02 (Bảo mật):** Xác thực không trạng thái (Stateless) qua Firebase ID Token; Không lưu mật khẩu thô trong MySQL; Lớp lọc `FirebaseAuthFilter` bảo vệ toàn bộ API nghiệp vụ.
- **NFR-03 (Độ tin cậy):** Tự động chuyển sang Rule-based Fallback khi không nạp được mô hình ONNX native; Đóng gói Docker Compose có tự động khởi động lại `restart: unless-stopped`.
- **NFR-04 (Giao diện):** Obsidian Dark Theme nền đen sâu `#090D16`, bề mặt thẻ `#161B2A`, điểm nhấn Royal Blue `#3B82F6` bảo vệ mắt và tiết kiệm pin OLED.

---

## 2. BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG TỔNG QUÁT (BFC LEVEL 0)

```mermaid
graph TD
    Root["HỆ THỐNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN THÔNG MINH (SMART PERSONAL FINANCE APP)"]
    
    F1["1.0 QUẢN LÝ TÀI KHOẢN & XÁC THỰC"]
    F2["2.0 QUẢN LÝ TÀI CHÍNH & GIAO DỊCH"]
    F3["3.0 QUẢN LÝ NGÂN SÁCH & ĐỊNH KỲ"]
    F4["4.0 TRÍ TUỆ NHÂN TẠO & THỊ GIÁC MÁY TÍNH"]
    F5["5.0 BÁO CÁO & THỐNG KÊ TÀI CHÍNH"]
    
    Root --> F1
    Root --> F2
    Root --> F3
    Root --> F4
    Root --> F5
```

---

## 3. CÁC BIỂU ĐỒ PHÂN CẤP CHỨC NĂNG CON CHI TIẾT (BFC LEVEL 1 & LEVEL 2)

### 3.1. Phân cấp Chức năng 1.0: Quản lý Tài khoản & Xác thực

```mermaid
graph TD
    F1["1.0 QUẢN LÝ TÀI KHOẢN & XÁC THỰC"]
    
    F11["1.1 Đăng ký tài khoản"]
    F12["1.2 Đăng nhập hệ thống"]
    F13["1.3 Quản lý hồ sơ người dùng"]
    F14["1.4 Cấu hình IP Server"]
    
    F1 --> F11
    F1 --> F12
    F1 --> F13
    F1 --> F14
    
    F11 --> F111["1.1.1 Đăng ký Email/Password"]
    F11 --> F112["1.1.2 Kiểm tra trùng lặp email"]
    F11 --> F113["1.1.3 Đồng bộ Firebase & MySQL"]
    
    F12 --> F121["1.2.1 Đăng nhập Email/Password"]
    F12 --> F122["1.2.2 Đăng nhập Google Sign-In 1 chạm"]
    F12 --> F123["1.2.3 Cấp phát Firebase Bearer Token"]
    
    F13 --> F131["1.3.1 Xem thông tin tài khoản"]
    F13 --> F132["1.3.2 Cập nhật Họ tên & SĐT"]
    F13 --> F133["1.3.3 Tải ảnh đại diện"]
    F13 --> F134["1.3.4 Đăng xuất an toàn"]
    
    F14 --> F141["1.4.1 Nhấn giữ Logo app 2 giây"]
    F14 --> F142["1.4.2 Tùy biến IP Server Backend"]
```

### 3.2. Phân cấp Chức năng 2.0: Quản lý Tài chính & Giao dịch

```mermaid
graph TD
    F2["2.0 QUẢN LÝ TÀI CHÍNH & GIAO DỊCH"]
    
    F21["2.1 Quản lý Ví tài chính"]
    F22["2.2 Quản lý Danh mục Thu Chi"]
    F23["2.3 Quản lý Giao dịch Thu Chi"]
    
    F2 --> F21
    F2 --> F22
    F2 --> F23
    
    F21 --> F211["2.1.1 Tạo ví (Tiền mặt, Ngân hàng, Ví điện tử)"]
    F21 --> F212["2.1.2 Cập nhật tên ví & số dư ban đầu"]
    F21 --> F213["2.1.3 Xóa ví tài chính"]
    F21 --> F214["2.1.4 Tính tổng số dư ròng Dashboard"]
    
    F22 --> F221["2.2.1 Xem 11 danh mục mặc định hệ thống"]
    F22 --> F222["2.2.2 Thêm danh mục cá nhân hóa"]
    F22 --> F223["2.2.3 Tùy biến mã màu Hex & Icon Material"]
    F22 --> F224["2.2.4 Phân loại danh mục Thu/Chi"]
    
    F23 --> F231["2.3.1 Thêm giao dịch thu/chi"]
    F23 --> F232["2.3.2 Tự động trừ/cộng số dư ví"]
    F23 --> F233["2.3.3 Sửa giao dịch & bù trừ số dư"]
    F23 --> F234["2.3.4 Xóa giao dịch & rollback số dư ví"]
    F23 --> F235["2.3.5 Đính kèm ảnh hóa đơn/chứng từ"]
    F23 --> F236["2.3.6 Lọc giao dịch theo ngày/tháng"]
```

### 3.3. Phân cấp Chức năng 3.0: Quản lý Ngân sách & Định kỳ

```mermaid
graph TD
    F3["3.0 QUẢN LÝ NGÂN SÁCH & ĐỊNH KỲ"]
    
    F31["3.1 Thiết lập Ngân sách"]
    F32["3.2 Giám sát & Cảnh báo đa cấp"]
    F33["3.3 Quản lý Giao dịch Định kỳ"]
    
    F3 --> F31
    F3 --> F32
    F3 --> F33
    
    F31 --> F311["3.1.1 Đặt hạn mức chi theo danh mục/tháng"]
    F31 --> F312["3.1.2 Đặt ngân sách tổng"]
    F31 --> F313["3.1.3 Sửa/Xóa hạn mức ngân sách"]
    
    F32 --> F321["3.2.1 Theo dõi số tiền đã chi thời gian thực"]
    F32 --> F322["3.2.2 Tính hạn mức an toàn mỗi ngày"]
    F32 --> F323["3.2.3 Thanh tiến độ đổi màu 3 cấp độ (Xanh/Vàng/Đỏ)"]
    
    F33 --> F331["3.3.1 Tạo lịch định kỳ (ngày, tuần, tháng, năm)"]
    F33 --> F332["3.3.2 Tạm dừng / Kích hoạt lại lịch"]
    F33 --> F333["3.3.3 Cronjob 00:00 tự động sinh giao dịch"]
    F33 --> F334["3.3.4 Endpoint /trigger kích hoạt tức thì"]
```

### 3.4. Phân cấp Chức năng 4.0: Trí tuệ Nhân tạo & Thị giác Máy tính

```mermaid
graph TD
    F4["4.0 TRÍ TUỆ NHÂN TẠO & THỊ GIÁC MÁY TÍNH"]
    
    F41["4.1 Quét Hóa đơn Thông minh"]
    F42["4.2 Nhận diện Sản phẩm Camera AI"]
    F43["4.3 Phân loại Danh mục ONNX"]
    
    F4 --> F41
    F4 --> F42
    F4 --> F43
    
    F41 --> F411["4.1.1 Chụp/chọn ảnh hóa đơn từ thư viện"]
    F41 --> F412["4.1.2 Google ML Kit OCR nhận dạng chữ offline (400ms)"]
    F41 --> F413["4.1.3 Google Gemini 2.5 Flash bóc tách JSON (tiền, ngày, quán)"]
    F41 --> F414["4.1.4 Tự động điền dữ liệu vào form giao dịch"]
    
    F42 --> F421["4.2.1 CameraX thu nhận khung hình liên tục"]
    F42 --> F422["4.2.2 YOLOv8n TFLite nhận diện 18 lớp On-Device"]
    F42 --> F423["4.2.3 Vẽ Bounding Box thời gian thực 35-40 FPS"]
    F42 --> F424["4.2.4 Gửi danh sách nhãn & confidence lên Backend"]
    
    F43 --> F431["4.3.1 Trích xuất vector 27 đặc trưng kỹ thuật"]
    F43 --> F432["4.3.2 Suy luận qua Random Forest ONNX Java Backend (< 2ms)"]
    F43 --> F433["4.3.3 Gợi ý danh mục chi tiêu chuẩn xác"]
    F43 --> F434["4.3.4 Heuristic Rule-based Fallback an toàn"]
```

### 3.5. Phân cấp Chức năng 5.0: Báo cáo & Thống kê Tài chính

```mermaid
graph TD
    F5["5.0 BÁO CÁO & THỐNG KÊ TÀI CHÍNH"]
    
    F51["5.1 Thống kê Dòng tiền"]
    F52["5.2 Trực quan hóa MPAndroidChart"]
    
    F5 --> F51
    F5 --> F52
    
    F51 --> F511["5.1.1 Thống kê tổng thu, tổng chi và số dư theo tháng"]
    F51 --> F512["5.1.3 Thống kê biến động dòng tiền 12 tháng theo năm"]
    F51 --> F513["5.1.3 Lọc báo cáo theo khoảng thời gian tùy chọn"]
    
    F52 --> F521["5.2.1 Biểu đồ tròn PieChart tỷ trọng từng danh mục"]
    F52 --> F522["5.2.2 Chạm vào lát cắt để xem chi tiết số tiền"]
    F52 --> F523["5.2.3 Biểu đồ cột BarChart so sánh đối sánh thu/chi 12 tháng"]
```
"""
    with open(md_path, 'w', encoding='utf-8') as f:
        f.write(md_content)
    print(f"Successfully generated MD: {md_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_THIET_KE_HE_THONG'
    os.makedirs(out_dir, exist_ok=True)
    d_path = os.path.join(out_dir, '01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.docx')
    m_path = os.path.join(out_dir, '01_PHAN_TICH_YEU_CAU_VA_BIEU_DO_PHAN_CAP_CHUC_NANG_BFC.md')
    build_bfc_doc(d_path, m_path)
