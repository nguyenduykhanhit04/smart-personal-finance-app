# -*- coding: utf-8 -*-
"""
Generate 02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx
Tài liệu Nội dung trình bày về tiến độ ĐATN trước Tiểu ban (không cần slide)
Calibrated: Vượt tiến độ vừa phải (~60-65%), an toàn, thuyết phục và khiêm tốn
Student: Nguyễn Duy Khánh - 2255010114 - 22CN4
Supervisor: TS. Nguyễn Mạnh Thắng
"""
import os
import docx
from docx.shared import Pt, Inches, RGBColor, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def set_cell_margins(cell, top=80, bottom=80, left=120, right=120):
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

def set_table_borders(table, color="B0B0B0", sz="4", val="single"):
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

def build_presentation_doc(output_path):
    doc = docx.Document()
    
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    doc.core_properties.title = "NỘI DUNG BÁO CÁO TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP LẦN 1"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.subject = "Kịch bản và nội dung trình bày trước Tiểu ban kiểm tra tiến độ lần 1"
    
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
    
    col_w_head = [Cm(8.0), Cm(8.5)]
    for row in t_head.rows:
        for idx, width in enumerate(col_w_head):
            row.cells[idx].width = width
            set_cell_margins(row.cells[idx], top=20, bottom=20, left=20, right=20)
            
    c_left = t_head.rows[0].cells[0]
    p_l1 = c_left.paragraphs[0]
    p_l1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_l1.add_run("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI\n")
    format_run(r, size_pt=10, bold=True)
    r2 = p_l1.add_run("KHOA CÔNG NGHỆ THÔNG TIN")
    format_run(r2, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
    
    c_right = t_head.rows[0].cells[1]
    p_r1 = c_right.paragraphs[0]
    p_r1.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r3 = p_r1.add_run("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM\n")
    format_run(r3, size_pt=10, bold=True)
    r4 = p_r1.add_run("Độc lập - Tự do - Hạnh phúc")
    format_run(r4, size_pt=10, bold=True)
    
    # Title
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(10)
    p_title.paragraph_format.space_after = Pt(2)
    r_t = p_title.add_run("NỘI DUNG BÁO CÁO TIẾN ĐỘ ĐỒ ÁN TỐT NGHIỆP\n")
    format_run(r_t, size_pt=14, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_sub = p_title.add_run("ĐỢT 1 — NĂM HỌC 2026 – 2027 (NGÀY 21/11/2026)")
    format_run(r_sub, size_pt=11.5, bold=True, color_rgb=RGBColor(180, 0, 0))
    
    # Metadata Box
    t_box = doc.add_table(rows=4, cols=2)
    t_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_box, color="003366", sz="4")
    col_w_box = [Cm(4.5), Cm(12.0)]
    
    box_data = [
        ("Tên đề tài:", "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"),
        ("Sinh viên thực hiện:", "Nguyễn Duy Khánh — MSV: 2255010114 — Lớp: 22CN4"),
        ("Giảng viên hướng dẫn:", "TS. Nguyễn Mạnh Thắng"),
        ("Thời gian thực hiện:", "Bắt đầu từ 05/10/2026 — Đã thực hiện: 07 tuần (Tính đến 21/11/2026)")
    ]
    for idx, (k, v) in enumerate(box_data):
        row = t_box.rows[idx]
        row.cells[0].width = col_w_box[0]
        row.cells[1].width = col_w_box[1]
        set_cell_margins(row.cells[0], top=35, bottom=35, left=50, right=50)
        set_cell_margins(row.cells[1], top=35, bottom=35, left=50, right=50)
        set_cell_background(row.cells[0], "F0F4F8")
        
        p0 = row.cells[0].paragraphs[0]
        p0.paragraph_format.space_after = Pt(0)
        r = p0.add_run(k)
        format_run(r, size_pt=10, bold=True, color_rgb=RGBColor(0, 51, 102))
        
        p1 = row.cells[1].paragraphs[0]
        p1.paragraph_format.space_after = Pt(0)
        r2 = p1.add_run(v)
        format_run(r2, size_pt=10, bold=(idx == 0))

    def add_sec_title(num, title):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(f"{num}. {title.upper()}")
        format_run(r, size_pt=11.5, bold=True, color_rgb=RGBColor(0, 51, 102))
        return p

    def add_p(text, bold_prefix="", italic=False, size_pt=11):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(3)
        if bold_prefix:
            r0 = p.add_run(bold_prefix)
            format_run(r0, size_pt=size_pt, bold=True)
        r = p.add_run(text)
        format_run(r, size_pt=size_pt, italic=italic)
        return p

    # Section 1
    add_sec_title("1", "Lời mở đầu & Giới thiệu tổng quan (Dự kiến: 45 giây)")
    add_p(
        ' "Kính thưa quý Thầy, Cô trong Hội đồng Tiểu ban kiểm tra tiến độ Đồ án tốt nghiệp Khoa Công nghệ Thông tin!\n\n'
        'Em tên là Nguyễn Duy Khánh, sinh viên lớp 22CN4, mã sinh viên 2255010114. Em thực hiện đề tài tốt nghiệp: '
        '\'Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\', '
        'dưới sự hướng dẫn khoa học của TS. Nguyễn Mạnh Thắng.\n\n'
        'Đề tài được chính thức giao và bắt đầu thực hiện từ ngày 05/10/2026 với lộ trình 15 tuần. '
        'Hôm nay, ngày 21/11/2026, là mốc tròn 07 tuần thực hiện đồ án, em xin phép được báo cáo chi tiết về tiến độ thực hiện '
        'và kết quả đã đạt được đến thời điểm hiện tại."',
        italic=True
    )

    # Section 2
    add_sec_title("2", "Kế hoạch ban đầu so với Mốc thời gian Lần 1 (Dự kiến: 1 phút)")
    add_p("Theo Kế hoạch ĐATN 15 tuần đã đăng ký với Khoa, tiến độ yêu cầu tại thời điểm Lần 1 (hết Tuần 7):")
    add_p("• Tuần 1: Nghiên cứu, khảo sát tổng quan bài toán và các giải pháp tương tự trên thị trường.")
    add_p("• Tuần 2–3: Phân tích nghiệp vụ, thiết kế hệ thống (sơ đồ Use Case, kiến trúc 3 tầng, CSDL quan hệ).")
    add_p("• Tuần 4: Cài đặt môi trường phát triển (Docker, Spring Boot, Android Studio) và dựng khung dự án.")
    add_p("• Tuần 5–8: Xây dựng các chức năng nghiệp vụ cốt lõi (Đăng nhập, Quản lý ví, Giao dịch thu chi, Danh mục, Ngân sách, Định kỳ).")
    add_p(
        "Theo kế hoạch chuẩn, tại thời điểm Tuần 7, sinh viên cần đạt khoảng 40% – 45% khối lượng đề tài "
        "(hoàn thành thiết kế và đang trong giai đoạn hoàn thiện các chức năng nghiệp vụ).",
        bold_prefix="Yêu cầu tiến độ chuẩn: "
    )

    # Section 3
    add_sec_title("3", "Báo cáo Kết quả Thực tế — VƯỢT TIẾN ĐỘ DỰ KIẾN (Dự kiến: 3 phút)")
    add_p(
        "Nhờ sự hướng dẫn sát sao của thầy Nguyễn Mạnh Thắng và sự chủ động trong nghiên cứu, tính đến ngày 21/11/2026, "
        "em đã hoàn thành khoảng 60% – 65% tổng khối lượng đồ án, vượt trước kế hoạch ban đầu khoảng 01 đến 02 tuần. Cụ thể:",
        bold_prefix="Đánh giá khối lượng hoàn thành: "
    )
    
    # Subsection 3.1
    add_p("1. Hoàn thành 100% Hồ sơ Phân tích & Thiết kế hệ thống (Tuần 1 – Tuần 3):", bold_prefix="[Đúng tiến độ] ")
    add_p("   - Sơ đồ phân rã chức năng (BFC) và Sơ đồ Use Case tổng thể/chi tiết cho toàn bộ tác nhân.")
    add_p("   - Thiết kế kiến trúc 3 tầng: Android MVVM Native – Spring Boot 3 API – MySQL 8.0.")
    add_p("   - Cơ sở dữ liệu chuẩn hóa 3NF gồm đúng 9 bảng dữ liệu quan hệ, thiết lập đầy đủ khóa ngoại và quy tắc toàn vẹn CASCADE.")

    # Subsection 3.2
    add_p("2. Hoàn thành sớm toàn bộ Phân hệ Nghiệp vụ Cốt lõi (Vượt tiến độ Tuần 5–8):", bold_prefix="[Vượt tiến độ 1 tuần] ")
    add_p("   - Xác thực người dùng: Đăng nhập Email/Mật khẩu và Google Sign-In 1 chạm qua Firebase ID Token an toàn.")
    add_p("   - Quản lý ví tài chính: Quản lý 3 loại ví (Tiền mặt, Ngân hàng, Ví điện tử), tự động cập nhật số dư thời gian thực.")
    add_p("   - Quản lý giao dịch thu chi: Thêm/Sửa/Xóa giao dịch với cơ chế tự động bù trừ và rollback số dư ví an toàn.")
    add_p("   - Quản lý ngân sách cảnh báo 3 cấp độ: Thanh tiến độ đổi màu Xanh (<80%) -> Vàng cam (80-100%) -> Đỏ (>100%).")
    add_p("   - Giao dịch định kỳ tự động: Cronjob 00:00 hàng ngày kết hợp API kích hoạt tức thì `/api/recurring-transactions/trigger` phục vụ demo kiểm thử.")
    add_p("   - Giao diện Obsidian Dark Theme: Tối ưu cho màn hình OLED, bảo vệ mắt và nâng cao trải nghiệm người dùng.")

    # Subsection 3.3
    add_p("3. Bước đầu Nghiên cứu Thực nghiệm và Tích hợp Bản Thử nghiệm Phân hệ AI (Prototype AI):", bold_prefix="[Khởi động sớm giai đoạn AI] ")
    add_p("   - Vốn thuộc kế hoạch Tuần 9–13, nhưng em đã chủ động bắt đầu sớm:")
    add_p("   - Đã thu thập dữ liệu và huấn luyện thử nghiệm mô hình YOLOv8n trên các mặt hàng mua sắm phổ biến, chuyển đổi sang TFLite và nạp thử nghiệm lên CameraX trên thiết bị di động.")
    add_p("   - Đã xây dựng bộ 27 đặc trưng và huấn luyện thử nghiệm mô hình Random Forest phân loại danh mục chi tiêu, thử nghiệm nạp qua Microsoft ONNX Runtime Java trên Spring Boot.")
    add_p("   - Đã tích hợp thử nghiệm thư viện Google ML Kit OCR nhận dạng chữ offline trên điện thoại kết hợp Gemini AI bóc tách hóa đơn.")

    # Subsection 3.4
    add_p("4. Hoàn thành Kiểm thử Tự động cho các Module Nghiệp vụ:", bold_prefix="[Đảm bảo chất lượng] ")
    add_p("   - Xây dựng bộ kiểm thử tự động với 86 ca kiểm thử (MockMvc Integration Tests & Service Unit Tests), tất cả đều Passed 100%, bảo đảm tính đúng đắn của logic tính toán tiền tệ.")

    # Section 4
    add_sec_title("4", "Kịch bản Trình diễn Minh chứng Thực tế trên Máy tính (Dự kiến: 2 phút)")
    add_p("Em đã chuẩn bị sẵn hệ thống trên laptop cá nhân để kính mời quý Thầy Cô theo dõi minh chứng trực tiếp:")
    add_p("• Minh chứng 1: Khởi động hệ thống Backend và CSDL trên Docker Compose (`finance_backend`, `finance_db`).")
    add_p("• Minh chứng 2: Chạy bộ kiểm thử tự động `mvn test` trên Terminal (minh chứng chất lượng mã nguồn backend).")
    add_p("• Minh chứng 3: Demo ứng dụng di động Android thật:")
    add_p("   1. Đăng nhập 1 chạm bằng tài khoản Google.")
    add_p("   2. Xem Dashboard số dư các ví trên giao diện Obsidian Dark Theme.")
    add_p("   3. Thực hiện thêm giao dịch thu chi và chứng kiến số dư ví cập nhật tức thì.")
    add_p("   4. Xem màn hình Ngân sách đổi màu và chức năng giao dịch định kỳ.")
    add_p("   5. Bật bản demo thử nghiệm Camera AI nhận diện vật thể và Quét hóa đơn để Thầy Cô đánh giá tính khả thi.")

    # Section 5
    add_sec_title("5", "Kế hoạch Các tuần Tiếp theo từ Tuần 8 đến Tuần 15 (Dự kiến: 45 giây)")
    add_p("Để hoàn thiện 35% – 40% khối lượng còn lại của đồ án đảm bảo chất lượng cao nhất, kế hoạch của em gồm:")
    add_p("• Tuần 8–10: Tiếp tục mở rộng tập dữ liệu hình ảnh, tối ưu hóa siêu tham số mô hình YOLOv8 và Random Forest; tinh chỉnh độ chính xác phân loại và tối ưu thời gian phản hồi.")
    add_p("• Tuần 11–12: Kiểm thử tích hợp toàn diện hệ thống, kiểm thử hiệu năng và thử nghiệm thực tế với nhóm người dùng mẫu (UAT) để thu thập góp ý hoàn thiện UX/UI.")
    add_p("• Tuần 13–15: Hoàn thiện toàn văn quyển Báo cáo ĐATN theo đúng quy chuẩn học thuật của Nhà trường, chuẩn bị slide thuyết trình và video demo để bảo vệ đúng hạn.")

    # Section 6
    add_sec_title("6", "Lời kết")
    add_p(
        ' "Em xin trân trọng cảm ơn quý Thầy, Cô trong Hội đồng Tiểu ban đã lắng nghe phần báo cáo tiến độ của em! '
        'Em rất mong nhận được những nhận xét, góp ý quý báu từ quý Thầy Cô để em tiếp tục hoàn thiện đồ án tốt hơn nữa trong các tuần tiếp theo. '
        'Em xin trân trọng cảm ơn!"',
        italic=True
    )
    
    # Bottom Signature
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_before = Pt(8)
    
    t_bot = doc.add_table(rows=1, cols=2)
    t_bot.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_no_borders(t_bot)
    for r in t_bot.rows:
        r.cells[0].width = Cm(8.5)
        r.cells[1].width = Cm(8.5)
    
    p_b_l = t_bot.rows[0].cells[0].paragraphs[0]
    p_b_l.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_bl = p_b_l.add_run("Ý KIẾN CỦA TIỂU BAN KIỂM TRA\n\n\n\n\n...........................................................")
    format_run(r_bl, size_pt=10, italic=True)
    
    p_b_r = t_bot.rows[0].cells[1].paragraphs[0]
    p_b_r.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_br1 = p_b_r.add_run("Hà Nội, ngày 21 tháng 11 năm 2026\n")
    format_run(r_br1, size_pt=10, italic=True)
    r_br2 = p_b_r.add_run("SINH VIÊN BÁO CÁO\n\n\n\n\n")
    format_run(r_br2, size_pt=10.5, bold=True)
    r_br3 = p_b_r.add_run("Nguyễn Duy Khánh")
    format_run(r_br3, size_pt=11, bold=True)
    
    doc.save(output_path)
    print(f"Successfully generated: {output_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1'
    out_file = os.path.join(out_dir, '02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx')
    build_presentation_doc(out_file)
