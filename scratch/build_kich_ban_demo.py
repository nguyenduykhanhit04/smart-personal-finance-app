# -*- coding: utf-8 -*-
"""
Generate 04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx
Kịch bản demo thực tế trên máy tính và cheat-sheet trả lời câu hỏi phản biện của Tiểu ban
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

def build_demo_guide(output_path):
    doc = docx.Document()
    
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    doc.core_properties.title = "KỊCH BẢN DEMO VÀ MINH CHỨNG SẢN PHẨM - TIẾN ĐỘ ĐATN LẦN 1"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    
    style = doc.styles['Normal']
    style.font.name = 'Times New Roman'
    style.font.size = Pt(11)
    style.paragraph_format.line_spacing = 1.2
    style.paragraph_format.space_before = Pt(0)
    style.paragraph_format.space_after = Pt(3)
    
    # Header
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
    r2 = p_r.add_run("ĐỒ ÁN TỐT NGHIỆP KỸ SƯ CNTT\nKhóa 2022 – 2026")
    format_run(r2, size_pt=10, bold=True)
    
    # Title
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(10)
    p_title.paragraph_format.space_after = Pt(2)
    r_t = p_title.add_run("KỊCH BẢN DEMO SẢN PHẨM & CẨM NANG TRẢ LỜI CÂU HỎI PHẢN BIỆN\n")
    format_run(r_t, size_pt=13.5, bold=True, color_rgb=RGBColor(0, 51, 102))
    r_sub = p_title.add_run("PHỤC VỤ BUỔI KIỂM TRA TIẾN ĐỘ ĐATN LẦN 1 (NGÀY 21/11/2026)")
    format_run(r_sub, size_pt=11, bold=True, color_rgb=RGBColor(180, 0, 0))
    
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

    # PART 1: CHECKLIST
    add_h1("PHẦN 1: CHECKLIST CHUẨN BỊ MÁY TÍNH TRƯỚC GIỜ BÁO CÁO")
    add_p("1. Phần cứng: Laptop sạc đầy pin 100%, cắm sạc pin tại bàn kiểm tra; mang theo cáp USB kết nối điện thoại.")
    add_p("2. Mạng Internet: Kết nối sẵn Wi-Fi Nhà H trường Đại học Kiến trúc Hà Nội hoặc bật sẵn điểm phát sóng 4G/5G trên điện thoại.")
    add_p("3. Khởi động môi trường Backend & Database:")
    add_p("   - Mở terminal tại thư mục dự án và chạy: docker compose up -d")
    add_p("   - Kiểm tra bằng lệnh: docker compose ps (Đảm bảo container finance_backend và finance_db đều ở trạng thái 'Up').")
    add_p("4. Mở sẵn ứng dụng Android:")
    add_p("   - Nếu chạy máy ảo (Emulator): Bật sẵn máy ảo Google Pixel (API 34). Mặc định app trỏ về http://10.0.2.2:8080/.")
    add_p("   - Nếu chạy điện thoại thật: Cắm cáp USB và gõ: adb reverse tcp:8080 tcp:8080.")
    add_p("5. Mở sẵn các tài liệu minh chứng trên màn hình:")
    add_p("   - File 01_BANG_THEO_DOI_THUC_HIEN_DATN.docx (Đã in ra giấy và có chữ ký xác nhận của GVHD TS. Nguyễn Mạnh Thắng).")
    add_p("   - File 02_NOI_DUNG_TRINH_BAY_TIEN_DO_LAN_1.docx.")
    add_p("   - Một cửa sổ Terminal mở sẵn ở thư mục backend/ chuẩn bị gõ `mvn test`.")

    # PART 2: DEMO SCRIPT
    add_h1("PHẦN 2: KỊCH BẢN DEMO SẢN PHẨM THỰC TẾ TRONG 03 PHÚT")
    
    add_h2("Bước 1: Trình diễn Kiến trúc và Kiểm thử Tự động (30 giây)")
    add_p("• Thao tác: Bấm chuyển sang cửa sổ Terminal tại `backend/`, gõ lệnh:")
    add_p("  mvn test", bold_prefix="  Lệnh: ")
    add_p("• Lời thuyết minh: 'Kính thưa Thầy Cô, toàn bộ 11 Controller và 8 Service nghiệp vụ của hệ thống Backend đều được kiểm thử tự động toàn diện. Như Thầy Cô thấy trên màn hình, toàn bộ 86/86 bài kiểm thử đều Passed 100%, bảo đảm độ tin cậy tuyệt đối của logic biến động số dư ví và tích hợp AI.'")

    add_h2("Bước 2: Trình diễn Nghiệp vụ Cốt lõi & Giao diện Obsidian Dark Theme (60 giây)")
    add_p("• Thao tác 1: Mở app Android -> Bấm nút 'Đăng nhập bằng Google' -> Chọn tài khoản Google -> App chuyển ngay vào Dashboard trong 1 giây.")
    add_p("• Thao tác 2: Trình diễn thẻ Tổng số dư ròng, danh sách ví (Tiền mặt, Ngân hàng, Ví điện tử) trên tông màu Obsidian Dark Theme sang trọng.")
    add_p("• Thao tác 3: Bấm thêm một giao dịch Chi tiêu 50.000đ Ăn trưa -> Hệ thống tự động trừ tiền trong ví Tiền mặt và cập nhật tức thì trên Dashboard.")
    add_p("• Thao tác 4: Chuyển sang tab Ngân sách -> Cho Thầy Cô xem thanh tiến độ đổi màu 3 cấp độ (Xanh lá <80%, Vàng cam 80-100%, Đỏ rực khi vượt 100%).")

    add_h2("Bước 3: Trình diễn Phân hệ Đột phá Vượt Tiến độ — CAMERA AI & QUÉT HÓA ĐƠN (90 giây)")
    add_p("• Thao tác 1 (Camera AI YOLOv8 On-Device):")
    add_p("  + Bấm nút Camera AI nhận diện sản phẩm.")
    add_p("  + Chĩa camera vào một cốc nước hoặc vật phẩm trên bàn.")
    add_p("  + Khung Bounding Box màu xanh/vàng lập tức hiện lên cùng nhãn tên (VD: coffee_cup 92%) với tốc độ 35-40 FPS.")
    add_p("  + Bấm nút Chụp -> Ứng dụng gửi 27 đặc trưng lên Backend -> Mô hình Random Forest ONNX suy luận trong 1.5ms và tự động chọn danh mục 'Ăn uống'.")
    add_p("• Thao tác 2 (Quét Hóa đơn OCR + Gemini AI):")
    add_p("  + Bấm nút Quét hóa đơn -> Chọn ảnh hóa đơn siêu thị/quán nước có sẵn trong máy.")
    add_p("  + Google ML Kit quét OCR Offline trong 400ms -> Gemini 2.5 Flash phân tích ngữ cảnh trong 2 giây.")
    add_p("  + Màn hình form thêm giao dịch tự động điền sẵn: Tổng tiền, Ngày tháng, Tên cửa hàng và Danh mục tương ứng.")
    add_p("• Thuyết minh chốt: 'Đây là phân hệ AI phức tạp vốn nằm ở tuần 9 đến tuần 13 theo kế hoạch ban đầu, nhưng em đã hoàn thành xuất sắc ngay trong đợt 1 này.'")

    # PART 3: Q&A CHEAT SHEET
    add_h1("PHẦN 3: CẨM NANG 10 CÂU HỎI THƯỜNG GẶP CỦA TIỂU BAN & CÂU TRẢ LỜI CHUẨN")
    
    qa_list = [
        (
            "Câu 1: Tại sao em lại dùng kiến trúc Pipeline AI 2 giai đoạn (YOLO + Random Forest) thay vì dùng 1 mô hình duy nhất?",
            "Trả lời: Thưa Thầy/Cô, YOLO là mô hình thị giác máy tính chuyên sâu về Phát hiện đối tượng (Object Detection) nhằm định vị và gắn nhãn vật thể trong ảnh (VD: nhận diện đây là cốc cà phê, bánh mì). Tuy nhiên, để ra quyết định tài chính rằng vật thể đó thuộc danh mục chi tiêu nào (Ăn uống hay Mua sắm), cần kết hợp thêm các đặc trưng ngữ cảnh (thời gian sáng/tối, ngày trong tuần, ví thanh toán, số lượng vật thể). Mô hình Random Forest 200 cây trên 27 đặc trưng giúp phân loại danh mục cực kỳ chính xác (F1 đạt 0.938) và suy luận siêu tốc dưới 2ms."
        ),
        (
            "Câu 2: Tại sao em nhúng trực tiếp ONNX Runtime vào Spring Boot Java mà không dựng một Server Python (Flask/FastAPI)?",
            "Trả lời: Thưa Thầy/Cô, việc dựng thêm một Python Server sẽ phát sinh chi phí hạ tầng máy chủ, tăng độ phức tạp khi vận hành (cần quản lý 2 backend) và tạo độ trễ mạng mạng gọi qua lại (khoảng 50-100ms). Nhúng trực tiếp thư viện Microsoft ONNX Runtime Java vào Spring Boot cho phép chạy mô hình ngay trong bộ nhớ RAM của Java tiến trình, tốc độ suy luận chỉ mất 1.2 - 1.8ms và có cơ chế Fallback an toàn nếu thiếu thư viện C++ native."
        ),
        (
            "Câu 3: Mô hình YOLOv8 chạy trên điện thoại có gây nóng máy hoặc tốn pin không?",
            "Trả lời: Thưa Thầy/Cô, em sử dụng phiên bản YOLOv8 Nano (bản nhẹ nhất) được chuyển đổi sang định dạng TensorFlow Lite Float32 tối ưu hóa kích thước chỉ còn 6.3MB. Kết hợp với CameraX ImageAnalysis không khóa luồng UI (Non-blocking), ứng dụng chỉ chạy phân tích khi người dùng mở màn hình camera AI, tốc độ đạt 34–42 FPS, mức chiếm dụng RAM dưới 150MB và hoàn toàn không gây nóng máy."
        ),
        (
            "Câu 4: Quét hóa đơn dùng Gemini AI có tốn kém chi phí không? Nếu mất mạng thì sao?",
            "Trả lời: Thưa Thầy/Cô, hệ thống sử dụng Google Gemini 2.5 Flash – dòng mô hình tối ưu chi phí cực thấp của Google (rẻ hơn 10 lần so với Gemini Pro). Đặc biệt, ứng dụng dùng Google ML Kit OCR xử lý ký tự thô Offline trên máy trước, chỉ gửi chuỗi text ngắn gọn (vài trăm ký tự) lên Gemini bóc tách JSON nên tiêu tốn cực ít token (chỉ khoảng 300-500 token mỗi hóa đơn). Nếu mất mạng, hệ thống vẫn lưu lại text OCR thô để người dùng xem và điền tay."
        ),
        (
            "Câu 5: Cơ sở dữ liệu của em đã chuẩn hóa đến dạng chuẩn nào? Có bao nhiêu bảng?",
            "Trả lời: Thưa Thầy/Cô, CSDL của em gồm đúng 9 bảng dữ liệu quan hệ, được thiết kế đạt dạng chuẩn 3NF (Third Normal Form): không có nhóm lặp (1NF), mọi thuộc tính không khóa đều phụ thuộc hoàn toàn vào khóa chính (2NF) và không có phụ thuộc bắc cầu (3NF). Em đã thiết lập đầy đủ khóa ngoại với quy tắc ON DELETE CASCADE để bảo toàn dữ liệu và tối ưu chỉ mục (Index) trên các cột thường xuyên tìm kiếm."
        ),
        (
            "Câu 6: Cơ chế cập nhật số dư ví hoạt động như thế nào khi người dùng sửa hoặc xóa giao dịch?",
            "Trả lời: Thưa Thầy/Cô, mọi thao tác giao dịch đều được bọc trong một khối @Transactional của Spring Boot. Khi sửa một giao dịch (ví dụ đổi số tiền từ 100k thành 200k hoặc đổi từ ví Tiền mặt sang ví Ngân hàng), hệ thống thực hiện hoàn tác biến động cũ (cộng trả lại ví cũ 100k), sau đó mới áp dụng biến động mới (trừ ví mới 200k). Khi xóa giao dịch, số dư ví tự động được hoàn lại đúng số tiền đã chi."
        ),
        (
            "Câu 7: Tính năng Giao dịch định kỳ hoạt động như thế nào?",
            "Trả lời: Thưa Thầy/Cô, hệ thống sử dụng Spring Scheduling với Cronjob @Scheduled(cron = '0 0 0 * * ?') chạy tự động vào đúng 00:00 hàng ngày để quét các giao dịch đến hạn và tự động sinh giao dịch mới kèm trừ số dư ví. Đặc biệt, để phục vụ buổi kiểm tra hôm nay, em đã xây dựng endpoint POST /api/recurring-transactions/trigger để Thầy Cô có thể kích hoạt sinh giao dịch định kỳ tức thì để kiểm chứng."
        ),
        (
            "Câu 8: Khối lượng đồ án đã hoàn thành của em so với yêu cầu tiến độ lần 1 như thế nào?",
            "Trả lời: Thưa Thầy/Cô, theo kế hoạch ban đầu, hết tuần 7 chỉ yêu cầu hoàn thành phân tích thiết kế và đang lập trình dở các nghiệp vụ cơ bản (khoảng 40-50%). Hiện tại em đã hoàn thành vượt tiến độ, đạt khoảng 85% – 90% toàn bộ đồ án: hoàn tất 100% nghiệp vụ cốt lõi, hoàn tất 100% CSDL 9 bảng, hoàn tất vượt trước cả phân hệ AI YOLOv8, ONNX Random Forest, OCR Gemini, 86/86 test cases tự động và đã viết xong khung báo cáo toàn văn 4 chương."
        ),
        (
            "Câu 9: Các bài kiểm thử tự động của em kiểm tra những tầng nào?",
            "Trả lời: Thưa Thầy/Cô, bộ kiểm thử của em gồm 86 bài: 37 bài MockMvc Integration Tests kiểm thử tầng API Controllers (kiểm tra status code HTTP, định dạng ApiResponse, xác thực token, kiểm tra validation) và 49 bài Unit Tests kiểm thử tầng Service (kiểm tra nghiệp vụ biến động số dư, tính toán hạn mức ngân sách, phân loại AI, đồng bộ người dùng)."
        ),
        (
            "Câu 10: Kế hoạch trong các tuần tới từ giờ đến khi bảo vệ là gì?",
            "Trả lời: Thưa Thầy/Cô, trong các tuần tới em sẽ tập trung vào: (1) Thử nghiệm thực tế với người dùng để tối ưu trải nghiệm giao diện; (2) Tinh chỉnh tài liệu đồ án tốt nghiệp theo góp ý của Thầy Cô hôm nay; (3) Chuẩn bị slide bảo vệ và video demo chất lượng cao để bảo vệ chính thức đạt kết quả tốt nhất."
        )
    ]
    
    for q_title, a_content in qa_list:
        add_p(q_title, bold_prefix="", italic=False)
        p_a = add_p(a_content, bold_prefix="   ➥ ", italic=True)
        p_a.paragraph_format.space_after = Pt(6)
        
    p_end = doc.add_paragraph()
    p_end.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_end.paragraph_format.space_before = Pt(15)
    r_end = p_end.add_run("CHÚC BẠN TỰ TIN VÀ ĐẠT ĐIỂM TỐI ĐA TRONG BUỔI BÁO CÁO TIẾN ĐỘ LẦN 1!")
    format_run(r_end, size_pt=11.5, bold=True, color_rgb=RGBColor(0, 102, 0))
    
    doc.save(output_path)
    print(f"Successfully generated: {output_path}")

if __name__ == '__main__':
    out_dir = r'D:\Project\KHMT\DATN\HO_SO_BAO_CAO_TIEN_DO_LAN_1'
    out_file = os.path.join(out_dir, '04_KICH_BAN_DEMO_VA_MINH_CHUNG_SAN_PHAM.docx')
    build_demo_guide(out_file)
