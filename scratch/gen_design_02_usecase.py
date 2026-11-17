# -*- coding: utf-8 -*-
"""
Generate:
1. 02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.docx
2. 02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.md
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

def build_usecase_doc(docx_path, md_path):
    doc = docx.Document()
    
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    doc.core_properties.title = "SƠ ĐỒ USE CASE TỔNG QUÁT VÀ CHI TIẾT"
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
    r_t = p_title.add_run("TÀI LIỆU THIẾT KẾ MÔ HÌNH USE CASE\nSƠ ĐỒ TỔNG QUÁT & ĐẶC TẢ CHI TIẾT TỪNG PHÂN HỆ\n")
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

    add_h1("1. XÁC ĐỊNH TÁC NHÂN (ACTORS) VÀ PHẠM VI HỆ THỐNG")
    add_p("Hệ thống có 3 tác nhân tham gia tương tác trực tiếp:")
    add_p("1. Người dùng cá nhân (End User - Tác nhân chính): ", bold_prefix="• ")
    add_p("Người trực tiếp sử dụng ứng dụng di động để quản lý tài khoản, theo dõi số dư các ví, ghi nhận thu chi, quét hóa đơn bằng camera, chụp vật phẩm mua sắm và xem báo cáo tài chính.")
    add_p("2. Dịch vụ Firebase Authentication (Tác nhân hệ thống ngoài): ", bold_prefix="• ")
    add_p("Cung cấp dịch vụ đăng ký, đăng nhập tài khoản bằng Email/Password hoặc Google Sign-In 1 chạm; quản lý phiên và cấp phát ID Token (JWT) có chữ ký số.")
    add_p("3. Dịch vụ Google Gemini AI (Tác nhân hệ thống ngoài): ", bold_prefix="• ")
    add_p("Tiếp nhận chuỗi văn bản hóa đơn đã nhận dạng quang học (OCR) để phân tích ngữ cảnh ngôn ngữ tự nhiên và trích xuất cấu trúc JSON (tổng tiền, ngày, cửa hàng, danh mục).")

    add_h1("2. SƠ ĐỒ USE CASE TỔNG QUÁT TOÀN HỆ THỐNG")
    add_p("Sơ đồ Use Case tổng thể mô tả mối quan hệ giữa người dùng và 9 ca sử dụng chính của hệ thống:")
    
    t_uc_ov = doc.add_table(rows=10, cols=3)
    t_uc_ov.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_uc_ov, color="888888", sz="4")
    col_w_uc = [Cm(2.0), Cm(5.5), Cm(9.0)]
    
    uc_headers = ["Mã UC", "Tên Use Case", "Tác nhân tương tác & Mục đích"]
    for c_idx, h in enumerate(uc_headers):
        cell = t_uc_ov.rows[0].cells[c_idx]
        cell.width = col_w_uc[c_idx]
        set_cell_margins(cell, top=50, bottom=50, left=45, right=45)
        set_cell_background(cell, "003366")
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        format_run(r, size_pt=9.5, bold=True, color_rgb=RGBColor(255, 255, 255))
        
    uc_list = [
        ("UC-01", "Đăng nhập / Đăng ký", "Người dùng & Firebase Auth: Xác thực tài khoản Email hoặc Google 1 chạm."),
        ("UC-02", "Quản lý Ví tài chính", "Người dùng: Tạo ví, cập nhật số dư, xem tổng số dư ròng."),
        ("UC-03", "Quản lý Danh mục Thu Chi", "Người dùng: Quản lý danh mục chi tiêu/thu nhập, tùy biến Icon/Màu sắc."),
        ("UC-04", "Ghi nhận & Quản lý Giao dịch", "Người dùng: Thêm/Sửa/Xóa giao dịch thu chi, bù trừ số dư ví tự động."),
        ("UC-05", "Thiết lập & Theo dõi Ngân sách", "Người dùng: Thiết lập hạn mức tháng, theo dõi thanh đổi màu 3 cấp."),
        ("UC-06", "Quản lý Giao dịch Định kỳ", "Người dùng & Hệ thống Cron: Đặt lịch thu chi tự động định kỳ."),
        ("UC-07", "Quét Hóa đơn Thông minh AI", "Người dùng, Google ML Kit & Gemini AI: Chụp hóa đơn bóc tách dữ liệu tự động."),
        ("UC-08", "Nhận diện Sản phẩm Camera AI", "Người dùng: Nhận diện vật thể YOLOv8 thời gian thực và gợi ý danh mục."),
        ("UC-09", "Xem Báo cáo & Thống kê", "Người dùng: Xem biểu đồ tròn cơ cấu chi tiêu, biểu đồ cột biến động 12 tháng.")
    ]
    for r_idx, r_data in enumerate(uc_list, start=1):
        row = t_uc_ov.rows[r_idx]
        for c_idx, val in enumerate(r_data):
            cell = row.cells[c_idx]
            cell.width = col_w_uc[c_idx]
            set_cell_margins(cell, top=40, bottom=40, left=45, right=45)
            if r_idx % 2 == 1:
                set_cell_background(cell, "F9FBFC")
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True, color_rgb=RGBColor(0, 51, 102))
            elif c_idx == 1:
                r = p.add_run(val)
                format_run(r, size_pt=9, bold=True)
            else:
                r = p.add_run(val)
                format_run(r, size_pt=9)

    add_h1("3. SƠ ĐỒ USE CASE PHÂN RÃ CHI TIẾT THEO PHÂN HỆ")

    add_h2("3.1. Phân hệ Tài chính & Giao dịch")
    add_p("• Tác nhân: Người dùng cá nhân.")
    add_p("• Các Use Case thành phần: Thêm giao dịch, Sửa giao dịch, Xóa giao dịch, Đính kèm ảnh chứng từ, Xem lịch sử giao dịch, Quản lý ví tiền, Tự động cập nhật số dư ví.")
    add_p("• Quan hệ <<include>>: Thêm/Sửa/Xóa giao dịch luôn bao gồm việc Tự động cập nhật số dư ví.")
    add_p("• Quan hệ <<extend>>: Đính kèm ảnh chứng từ là trường hợp mở rộng của việc Thêm giao dịch.")

    add_h2("3.2. Phân hệ Trí tuệ Nhân tạo & Thị giác Máy tính")
    add_p("• Tác nhân: Người dùng cá nhân, Dịch vụ Google Gemini AI.")
    add_p("• Các Use Case thành phần: Quét hóa đơn camera, Trích xuất chữ OCR On-Device, Bóc tách hóa đơn Gemini AI, Phát hiện vật thể YOLOv8, Phân loại danh mục Random Forest ONNX, Điền tự động form giao dịch.")
    add_p("• Quan hệ <<include>>:")
    add_p("   - Quét hóa đơn <<include>> Trích xuất OCR On-Device <<include>> Bóc tách hóa đơn Gemini AI <<include>> Điền tự động form giao dịch.")
    add_p("   - Phát hiện vật thể YOLOv8 <<include>> Phân loại danh mục Random Forest <<include>> Điền tự động form giao dịch.")

    add_h2("3.3. Phân hệ Ngân sách & Báo cáo Thống kê")
    add_p("• Tác nhân: Người dùng cá nhân.")
    add_p("• Các Use Case thành phần: Thiết lập hạn mức ngân sách, Giám sát tiến độ ngân sách, Cảnh báo vượt hạn mức đổi màu, Xem báo cáo thu chi tháng, Xem biểu đồ xu hướng năm, Xem cơ cấu chi tiêu PieChart.")
    add_p("• Quan hệ <<extend>>: Giám sát tiến độ ngân sách mở rộng thành Cảnh báo vượt hạn mức khi tỷ lệ chi tiêu vượt 80% hoặc 100%.")

    add_h1("4. ĐẶC TẢ CHI TIẾT CÁC USE CASE TRỌNG TÂM")

    # UC-01
    add_h2("Đặc tả UC-01: Ghi nhận giao dịch thu chi thủ công")
    t_uc1 = doc.add_table(rows=7, cols=2)
    t_uc1.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_uc1, color="B0C4DE", sz="4")
    col_w_dt = [Cm(4.0), Cm(12.5)]
    uc1_specs = [
        ("Mã Use Case & Tên:", "UC-01: Ghi nhận giao dịch thu chi thủ công"),
        ("Tác nhân chính:", "Người dùng cá nhân (đã đăng nhập)"),
        ("Điều kiện tiên quyết:", "Người dùng đã đăng nhập thành công và đã sở hữu ít nhất một ví tài chính"),
        ("Luồng sự kiện chính (Basic Flow):", 
         "1. Người dùng bấm nút (+) trên màn hình chính để mở form Thêm giao dịch.\n"
         "2. Người dùng chọn loại giao dịch (Chi tiêu hoặc Thu nhập).\n"
         "3. Người dùng nhập số tiền giao dịch qua bàn phím số tùy biến.\n"
         "4. Người dùng chọn Danh mục chi tiêu/thu nhập tương ứng.\n"
         "5. Người dùng chọn Ví tài khoản thanh toán và ngày phát sinh giao dịch.\n"
         "6. Người dùng nhập ghi chú và có thể đính kèm ảnh chụp chứng từ (tùy chọn).\n"
         "7. Người dùng nhấn nút 'Lưu giao dịch'.\n"
         "8. Hệ thống kiểm tra tính hợp lệ của dữ liệu đầu vào (@Valid).\n"
         "9. Hệ thống lưu bản ghi vào bảng transactions và tự động bù trừ số dư ví trong bảng accounts (@Transactional).\n"
         "10. Hệ thống hiển thị thông báo thành công và cập nhật tức thì trên Dashboard."),
        ("Luồng ngoại lệ (Alternative Flow):", 
         "- Dữ liệu thiếu hoặc số tiền <= 0: Hệ thống hiển thị cảnh báo đỏ và không cho gửi request.\n"
         "- Mất kết nối mạng: Thông báo lỗi mạng và yêu cầu kiểm tra lại kết nối."),
        ("Hậu điều kiện:", "Bản ghi giao dịch mới được lưu trữ và số dư ví liên quan được cập nhật chính xác.")
    ]
    for idx, (lbl, val) in enumerate(uc1_specs):
        row = t_uc1.rows[idx]
        row.cells[0].width = col_w_dt[0]
        row.cells[1].width = col_w_dt[1]
        set_cell_margins(row.cells[0], top=35, bottom=35, left=45, right=45)
        set_cell_margins(row.cells[1], top=35, bottom=35, left=45, right=45)
        set_cell_background(row.cells[0], "F4F8FA")
        r0 = row.cells[0].paragraphs[0].add_run(lbl)
        format_run(r0, size_pt=9.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        r1 = row.cells[1].paragraphs[0].add_run(val)
        format_run(r1, size_pt=9.5)

    # UC-02
    add_h2("Đặc tả UC-02: Quét hóa đơn tự động bóc tách bằng AI (OCR + Gemini)")
    t_uc2 = doc.add_table(rows=6, cols=2)
    t_uc2.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_uc2, color="B0C4DE", sz="4")
    uc2_specs = [
        ("Mã Use Case & Tên:", "UC-02: Quét hóa đơn tự động bóc tách bằng AI"),
        ("Tác nhân:", "Người dùng cá nhân, Dịch vụ Google Gemini AI"),
        ("Điều kiện tiên quyết:", "Thiết bị có quyền Camera và có kết nối Internet"),
        ("Luồng sự kiện chính (Basic Flow):", 
         "1. Người dùng mở tính năng Quét hóa đơn trên app Android.\n"
         "2. Người dùng hướng camera chụp ảnh hóa đơn hoặc chọn từ thư viện ảnh.\n"
         "3. Thư viện Google ML Kit trích xuất toàn bộ văn bản thô OCR Offline trên thiết bị (400ms).\n"
         "4. Ứng dụng gửi raw OCR text lên API /api/ai/scan-bill của Backend.\n"
         "5. GeminiService gọi Google Gemini 2.5 Flash API bóc tách ngữ nghĩa JSON: tổng tiền, ngày, đơn vị bán lẻ, danh mục.\n"
         "6. Backend lưu nhật ký quét vào bảng ai_scan_logs và trả dữ liệu về Mobile.\n"
         "7. Ứng dụng mở form Thêm giao dịch với các trường đã được điền tự động.\n"
         "8. Người dùng kiểm tra, chỉnh sửa nếu cần và nhấn 'Lưu giao dịch'."),
        ("Luồng ngoại lệ:", 
         "- Hóa đơn mờ không đọc được chữ: Thông báo ảnh không rõ nét và yêu cầu chụp lại.\n"
         "- Gemini không nhận diện được số tiền: Để trống trường số tiền để người dùng nhập tay."),
        ("Hậu điều kiện:", "Tạo giao dịch thành công từ hóa đơn mà không cần gõ phím thủ công.")
    ]
    for idx, (lbl, val) in enumerate(uc2_specs):
        row = t_uc2.rows[idx]
        row.cells[0].width = col_w_dt[0]
        row.cells[1].width = col_w_dt[1]
        set_cell_margins(row.cells[0], top=35, bottom=35, left=45, right=45)
        set_cell_margins(row.cells[1], top=35, bottom=35, left=45, right=45)
        set_cell_background(row.cells[0], "F4F8FA")
        r0 = row.cells[0].paragraphs[0].add_run(lbl)
        format_run(r0, size_pt=9.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        r1 = row.cells[1].paragraphs[0].add_run(val)
        format_run(r1, size_pt=9.5)

    # UC-03
    add_h2("Đặc tả UC-03: Nhận diện sản phẩm qua Camera AI (YOLOv8 + ONNX Random Forest)")
    t_uc3 = doc.add_table(rows=6, cols=2)
    t_uc3.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_uc3, color="B0C4DE", sz="4")
    uc3_specs = [
        ("Mã Use Case & Tên:", "UC-03: Nhận diện sản phẩm mua sắm qua Camera AI"),
        ("Tác nhân:", "Người dùng cá nhân"),
        ("Điều kiện tiên quyết:", "Thiết bị được cấp quyền Camera"),
        ("Luồng sự kiện chính (Basic Flow):", 
         "1. Người dùng mở màn hình Camera AI nhận diện sản phẩm.\n"
         "2. CameraX hoạt động, mô hình YOLOv8n TFLite On-Device phân tích liên tục các frame ảnh.\n"
         "3. Ứng dụng vẽ khung nhận diện Bounding Box màu sắc và nhãn lớp thời gian thực (35-40 FPS).\n"
         "4. Người dùng bấm nút Chụp để chọn vật thể cần ghi chép.\n"
         "5. Ứng dụng gửi danh sách phát hiện lên API /api/ai/classify-product của Backend.\n"
         "6. Backend trích xuất 27 đặc trưng và chạy suy luận qua Random Forest ONNX Runtime Java (< 2ms).\n"
         "7. Backend trả về danh mục dự đoán có xác suất cao nhất (Ví dụ: coffee_cup -> Ăn uống).\n"
         "8. Mở form Thêm giao dịch với danh mục đã được chọn sẵn tự động."),
        ("Luồng ngoại lệ:", 
         "- Không phát hiện vật thể nào: Giữ nguyên form trống để người dùng chọn danh mục thủ công.\n"
         "- Thiếu thư viện native C++ ONNX: Tự động chuyển sang Rule-based Fallback an toàn."),
        ("Hậu điều kiện:", "Danh mục chi tiêu được phân loại tự động và chuẩn xác từ ảnh chụp đồ vật.")
    ]
    for idx, (lbl, val) in enumerate(uc3_specs):
        row = t_uc3.rows[idx]
        row.cells[0].width = col_w_dt[0]
        row.cells[1].width = col_w_dt[1]
        set_cell_margins(row.cells[0], top=35, bottom=35, left=45, right=45)
        set_cell_margins(row.cells[1], top=35, bottom=35, left=45, right=45)
        set_cell_background(row.cells[0], "F4F8FA")
        r0 = row.cells[0].paragraphs[0].add_run(lbl)
        format_run(r0, size_pt=9.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        r1 = row.cells[1].paragraphs[0].add_run(val)
        format_run(r1, size_pt=9.5)

    doc.save(docx_path)
    print(f"Successfully generated DOCX: {docx_path}")

    # Generate Markdown
    md_content = f"""# TÀI LIỆU THIẾT KẾ MÔ HÌNH USE CASE
## SƠ ĐỒ TỔNG QUÁT VÀ ĐẶC TẢ CHI TIẾT TỪNG PHÂN HỆ

**Đề tài:** XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Mã SV: 2255010114 — Lớp: 22CN4  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Khoa:** Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 1. XÁC ĐỊNH TÁC NHÂN (ACTORS) VÀ PHẠM VI HỆ THỐNG

1. **Người dùng cá nhân (End User - Tác nhân chính):** Thao tác trên thiết bị di động để quản lý tài khoản, theo dõi số dư các ví, ghi nhận thu chi, quét hóa đơn bằng camera, chụp vật phẩm mua sắm và xem báo cáo tài chính.
2. **Dịch vụ Firebase Authentication (Tác nhân hệ thống ngoài):** Quản lý đăng ký, đăng nhập tài khoản bằng Email/Password hoặc Google Sign-In 1 chạm; cấp phát Firebase ID Token (JWT).
3. **Dịch vụ Google Gemini AI (Tác nhân hệ thống ngoài):** Tiếp nhận dữ liệu văn bản OCR để phân tích ngữ cảnh ngữ nghĩa và trích xuất cấu trúc dữ liệu JSON hoàn chỉnh (tổng tiền, ngày, cửa hàng, danh mục).

---

## 2. SƠ ĐỒ USE CASE TỔNG QUÁT TOÀN HỆ THỐNG

```mermaid
flowchart TD
    User([Người dùng cá nhân])
    FirebaseAuth([Firebase Auth Service])
    GeminiAI([Google Gemini AI])

    subgraph Smart_Personal_Finance_App [Hệ thống Quản lý Tài chính Cá nhân Thông minh]
        UC_Auth([UC-01: Đăng nhập / Đăng ký])
        UC_Account([UC-02: Quản lý Ví & Tài khoản])
        UC_Category([UC-03: Quản lý Danh mục Thu Chi])
        UC_Tx([UC-04: Ghi nhận & Quản lý Giao dịch])
        UC_Budget([UC-05: Thiết lập & Theo dõi Ngân sách])
        UC_Recurring([UC-06: Quản lý Giao dịch Định kỳ])
        UC_ScanBill([UC-07: Quét Hóa đơn Thông minh AI])
        UC_ScanProduct([UC-08: Nhận diện Sản phẩm qua Camera])
        UC_Report([UC-09: Xem Báo cáo & Thống kê])
    end

    User --> UC_Auth
    User --> UC_Account
    User --> UC_Category
    User --> UC_Tx
    User --> UC_Budget
    User --> UC_Recurring
    User --> UC_ScanBill
    User --> UC_ScanProduct
    User --> UC_Report

    UC_Auth -.->|<<interact>>| FirebaseAuth
    UC_ScanBill -.->|<<use>>| GeminiAI
```

---

## 3. SƠ ĐỒ USE CASE PHÂN RÃ CHI TIẾT THEO PHÂN HỆ

### 3.1. Phân hệ Tài chính & Giao dịch

```mermaid
flowchart LR
    User([Người dùng])

    subgraph Sub_Finance [Phân hệ Tài chính & Giao dịch]
        UC_AddTx([Thêm giao dịch mới])
        UC_EditTx([Sửa giao dịch])
        UC_DeleteTx([Xóa giao dịch])
        UC_AttachImg([Đính kèm ảnh chứng từ])
        UC_ViewHistory([Xem lịch sử giao dịch])
        UC_ManageWallet([Tạo / Sửa / Xóa ví tiền])
        UC_AutoBalance([Tự động cập nhật số dư ví])
    end

    User --> UC_AddTx
    User --> UC_EditTx
    User --> UC_DeleteTx
    User --> UC_ViewHistory
    User --> UC_ManageWallet

    UC_AddTx -.->|<<include>>| UC_AutoBalance
    UC_EditTx -.->|<<include>>| UC_AutoBalance
    UC_DeleteTx -.->|<<include>>| UC_AutoBalance
    UC_AddTx -.->|<<extend>>| UC_AttachImg
```

### 3.2. Phân hệ Trí tuệ Nhân tạo & Thị giác Máy tính

```mermaid
flowchart LR
    User([Người dùng])
    Gemini([Google Gemini LLM])

    subgraph Sub_AI [Phân hệ Trí tuệ Nhân tạo]
        UC_ScanReceipt([Quét hóa đơn bằng Camera])
        UC_LocalOCR([Trích xuất chữ OCR On-Device])
        UC_ExtractBill([Bóc tách cấu trúc hóa đơn])
        UC_DetectObject([Phát hiện vật thể YOLOv8])
        UC_ClassifyCategory([Phân loại danh mục Random Forest])
        UC_PreFillForm([Tự động điền Form Giao dịch])
    end

    User --> UC_ScanReceipt
    User --> UC_DetectObject

    UC_ScanReceipt -.->|<<include>>| UC_LocalOCR
    UC_LocalOCR -.->|<<include>>| UC_ExtractBill
    UC_ExtractBill -.->|<<interact>>| Gemini
    UC_ExtractBill -.->|<<include>>| UC_PreFillForm

    UC_DetectObject -.->|<<include>>| UC_ClassifyCategory
    UC_ClassifyCategory -.->|<<include>>| UC_PreFillForm
```

### 3.3. Phân hệ Ngân sách & Báo cáo Thống kê

```mermaid
flowchart LR
    User([Người dùng])

    subgraph Sub_BudgetReport [Phân hệ Ngân sách & Báo cáo]
        UC_SetBudget([Thiết lập hạn mức ngân sách])
        UC_TrackBudget([Giám sát tiến độ ngân sách])
        UC_WarnBudget([Cảnh báo vượt hạn mức])
        UC_ViewMonthly([Xem báo cáo thu chi tháng])
        UC_ViewAnnual([Xem biểu đồ xu hướng năm])
        UC_PieCategory([Xem cơ cấu chi tiêu PieChart])
    end

    User --> UC_SetBudget
    User --> UC_TrackBudget
    User --> UC_ViewMonthly
    User --> UC_ViewAnnual

    UC_TrackBudget -.->|<<extend>>| UC_WarnBudget
    UC_ViewMonthly -.->|<<include>>| UC_PieCategory
```

---

## 4. ĐẶC TẢ CHI TIẾT CÁC USE CASE TRỌNG TÂM

### Đặc tả UC-01: Ghi nhận giao dịch thu chi thủ công
- **Mã Use Case:** UC-01
- **Tác nhân:** Người dùng cá nhân
- **Luồng sự kiện chính:**
  1. Người dùng bấm nút (+) trên màn hình chính để mở form Thêm giao dịch.
  2. Chọn loại giao dịch (Thu / Chi), nhập số tiền bằng bàn phím số tùy biến.
  3. Chọn Danh mục chi tiêu/thu nhập, chọn Ví thanh toán và Ngày giao dịch.
  4. Nhập ghi chú và có thể đính kèm ảnh chứng từ.
  5. Bấm 'Lưu giao dịch'.
  6. Backend kiểm tra tính hợp lệ dữ liệu (@Valid).
  7. Backend lưu bản ghi vào `transactions` và cập nhật bù trừ số dư ví trong `accounts` (@Transactional).
  8. Dashboard cập nhật biến động số dư tức thì.

### Đặc tả UC-02: Quét hóa đơn tự động bóc tách bằng AI (OCR + Gemini)
- **Mã Use Case:** UC-02
- **Tác nhân:** Người dùng cá nhân, Dịch vụ Google Gemini AI
- **Luồng sự kiện chính:**
  1. Người dùng mở tính năng Quét hóa đơn trên app Android.
  2. Hướng camera chụp hóa đơn hoặc chọn ảnh từ máy.
  3. Thư viện Google ML Kit trích xuất toàn bộ văn bản thô OCR Offline trên thiết bị (400ms).
  4. Ứng dụng gửi raw OCR text lên API `/api/ai/scan-bill`.
  5. `GeminiService` gọi Google Gemini 2.5 Flash API bóc tách JSON: tổng tiền, ngày, cửa hàng, danh mục.
  6. Lưu nhật ký quét vào bảng `ai_scan_logs`.
  7. Tự động điền các trường bóc tách vào form Thêm giao dịch để người dùng xác nhận nhanh.

### Đặc tả UC-03: Nhận diện sản phẩm qua Camera AI (YOLOv8 + ONNX Random Forest)
- **Mã Use Case:** UC-03
- **Tác nhân:** Người dùng cá nhân
- **Luồng sự kiện chính:**
  1. Người dùng mở tính năng Camera AI nhận diện sản phẩm.
  2. CameraX hoạt động, mô hình YOLOv8n TFLite On-Device quét liên tục khung hình (35-40 FPS).
  3. Vẽ Bounding Box màu sắc và nhãn lớp thời gian thực.
  4. Người dùng bấm Chụp -> gửi danh sách vật thể lên API `/api/ai/classify-product`.
  5. Backend trích xuất 27 đặc trưng kỹ thuật và chạy suy luận qua Random Forest ONNX Java (< 2ms).
  6. Tự động gợi ý danh mục chi tiêu chuẩn xác nhất (Ví dụ: `coffee_cup` -> Danh mục Ăn uống).
"""
    with open(md_path, 'w', encoding='utf-8') as f:
        f.write(md_content)
    print(f"Successfully generated MD: {md_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_THIET_KE_HE_THONG'
    d_path = os.path.join(out_dir, '02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.docx')
    m_path = os.path.join(out_dir, '02_SO_DO_USE_CASE_TONG_QUAT_VA_CHI_TIET.md')
    build_usecase_doc(d_path, m_path)
