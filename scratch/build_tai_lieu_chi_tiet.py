# -*- coding: utf-8 -*-
"""
Generate comprehensive Word documentation for Milestones 1 to 4:
TAI_LIEU_THIET_KE_VA_TRIEN_KHAI_DATN.docx
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

def format_run(run, font_name="Times New Roman", size_pt=12, bold=False, italic=False, color_rgb=None):
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

def add_p(doc, text="", bold_prefix="", space_after=3, line_spacing=1.2, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
    p = doc.add_paragraph()
    p.alignment = align
    p.paragraph_format.line_spacing = line_spacing
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.space_before = Pt(0)
    if bold_prefix:
        r_pre = p.add_run(bold_prefix + " ")
        format_run(r_pre, size_pt=12, bold=True)
    if text:
        r_txt = p.add_run(text)
        format_run(r_txt, size_pt=12, bold=False)
    return p

def add_bullet(doc, text, bold_prefix=""):
    p = doc.add_paragraph(style='List Bullet')
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.line_spacing = 1.15
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.space_before = Pt(0)
    if bold_prefix:
        r_pre = p.add_run(bold_prefix + ": ")
        format_run(r_pre, size_pt=11.5, bold=True)
    if text:
        r_txt = p.add_run(text)
        format_run(r_txt, size_pt=11.5, bold=False)
    return p

def add_h1(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    format_run(r, size_pt=14, bold=True, color_rgb=RGBColor(0, 32, 96))
    return p

def add_h2(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    format_run(r, size_pt=13, bold=True, color_rgb=RGBColor(0, 32, 96))
    return p

def add_h3(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    format_run(r, size_pt=12, bold=True)
    return p

def build_full_docx(output_path):
    doc = docx.Document()
    
    # Page Setup
    sec = doc.sections[0]
    sec.page_width = Cm(21.0)
    sec.page_height = Cm(29.7)
    sec.top_margin = Cm(2.0)
    sec.bottom_margin = Cm(2.0)
    sec.left_margin = Cm(2.5)
    sec.right_margin = Cm(2.0)
    
    # Core props
    doc.core_properties.title = "TÀI LIỆU TOÀN VĂN THIẾT KẾ VÀ TRIỂN KHAI HỆ THỐNG (MỤC 1 - 4)"
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.last_modified_by = "Nguyễn Duy Khánh"
    doc.core_properties.subject = "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"
    
    # Title
    p_main = doc.add_paragraph()
    p_main.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_main.paragraph_format.space_before = Pt(12)
    p_main.paragraph_format.space_after = Pt(4)
    r = p_main.add_run("BÁO CÁO KỸ THUẬT VÀ TÀI LIỆU TOÀN VĂN THIẾT KẾ TRIỂN KHAI\n(CÁC MỤC 1, 2, 3, 4 THEO KẾ HOẠCH ĐATN)")
    format_run(r, size_pt=15, bold=True, color_rgb=RGBColor(0, 32, 96))
    
    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(12)
    r = p_sub.add_run("Đề tài: XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID\nTÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO\nSinh viên: Nguyễn Duy Khánh – Mã SV: 2255010114 – Lớp: 22CN4\nCán bộ hướng dẫn: TS. Nguyễn Mạnh Thắng")
    format_run(r, size_pt=11.5, italic=True)
    
    # MỤC 1
    add_h1(doc, "MỤC 1: NGHIÊN CỨU TỔNG QUAN BÀI TOÁN & CƠ SỞ CÔNG NGHỆ (TUẦN 1)")
    add_p(doc, "Quản lý tài chính cá nhân là quá trình lập kế hoạch, theo dõi thu nhập, chi tiêu, quản lý các tài khoản ngân sách và hoạch định các mục tiêu tài chính tương lai của một cá nhân hoặc hộ gia đình. Trong bối cảnh kinh tế hiện đại với sự phổ biến của thanh toán không dùng tiền mặt (thẻ ngân hàng, ví điện tử MoMo/ZaloPay, mã VietQR), các giao dịch phát sinh diễn ra liên tục, phân tán trên nhiều nền tảng, khiến người dùng rất dễ rơi vào tình trạng mất kiểm soát chi tiêu.", "1.1. Tổng quan bài toán:")
    add_p(doc, "Khảo sát thực tế cho thấy tỷ lệ người dùng duy trì thói quen ghi chép liên tục trên 3 tháng chỉ đạt dưới 20%. Nguyên nhân cốt lõi là do sự phiền toái, mất thời gian và tính lặp đi lặp lại của thao tác nhập liệu thủ công bằng bàn phím ảo. Để giải quyết triệt để vấn đề này, đề tài nghiên cứu giải pháp tự động hóa ghi chép thông qua công nghệ Thị giác Máy tính (Computer Vision), Trí tuệ Nhân tạo (AI) On-Device và Nhận dạng ký tự quang học (OCR).", "Thực trạng rào cản:")
    
    add_h2(doc, "1.2. Khảo sát các giải pháp tương tự trên thị trường")
    add_p(doc, "Qua khảo sát các ứng dụng phổ biến như Money Lover, Sổ Thu Chi MISA và Spendee, đồ án đúc kết các ưu nhược điểm làm cơ sở thiết kế giải pháp đột phá:")
    add_bullet(doc, "Giao diện thân thiện nhưng hơn 90% thao tác ghi chép phụ thuộc hoàn toàn vào bàn phím ảo, gây cảm giác nhàm chán.", "Hạn chế thao tác")
    add_bullet(doc, "Tính năng quét hóa đơn (nếu có) thường là dịch vụ tính phí định kỳ rất đắt đỏ và dễ nhận diện sai lệch với mẫu hóa đơn nhiệt tại Việt Nam.", "Hạn chế OCR")
    add_bullet(doc, "Hoàn toàn chưa có ứng dụng nào tích hợp thị giác máy tính On-Device để nhận diện các mặt hàng tiêu dùng trực tiếp qua camera theo thời gian thực và tự động phân loại danh mục chi tiêu.", "Chưa có Camera AI")

    add_h2(doc, "1.3. Nghiên cứu các công nghệ cốt lõi")
    add_p(doc, "Mô hình nhận diện vật thể hiện đại của Ultralytics loại bỏ Anchor Box, sử dụng kiến trúc phân tách nhánh phân loại và nhánh hồi quy tọa độ. Mô hình được huấn luyện và chuyển đổi tối ưu sang TensorFlow Lite Float32 (6.3MB) để chạy trực tiếp trên chip di động mà không cần kết nối mạng.", "Mô hình YOLOv8:")
    add_p(doc, "Thuật toán học máy tập hợp gồm 200 cây quyết định được huấn luyện trên 27 đặc trưng kỹ thuật, xuất sang chuẩn Microsoft ONNX Runtime và nhúng trực tiếp vào RAM Java của Spring Boot, cho thời gian suy luận phân loại danh mục dưới 2ms.", "Random Forest & ONNX:")
    add_p(doc, "Sự kết hợp giữa Google ML Kit OCR quét văn bản offline trên thiết bị (400ms) và Google Gemini 2.5 Flash LLM bóc tách JSON trên server giúp hệ thống trích xuất chính xác tổng tiền, ngày giờ và tên cửa hàng từ hóa đơn trong vòng 2.45 giây, tiết kiệm 70% chi phí token.", "OCR + Gemini 2.5 Flash:")
    add_p(doc, "Kiến trúc Android MVVM với Kotlin Coroutines chạy trên Dispatchers.IO và ViewBinding bảo đảm giao diện không giật lag. Máy chủ Spring Boot 3 trên Java 21 kết nối MySQL 8.0 qua HikariCP và bảo mật Stateless JWT với Firebase Auth.", "Android MVVM & Spring Boot 3:")

    add_h2(doc, "1.4. Xác định phạm vi 5 module chính của hệ thống")
    add_bullet(doc, "Đăng nhập Email/Google 1 chạm, quản lý ví Tiền mặt, Ngân hàng, Ví điện tử, chuyển tiền nội bộ.", "Module 1: Xác thực & Ví")
    add_bullet(doc, "Ghi chép thu chi, tự động cộng/trừ số dư tức thì, đính kèm ảnh chứng từ, quản lý danh mục chuẩn tiếng Việt.", "Module 2: Giao dịch & Danh mục")
    add_bullet(doc, "Hạn mức ngân sách tháng với thanh tiến độ đổi màu 3 cấp (<80%, 80-100%, >100%), lịch lặp định kỳ tự động qua Cronjob.", "Module 3: Ngân sách & Định kỳ")
    add_bullet(doc, "Camera AI nhận diện 18 lớp vật thể (YOLOv8 On-Device 34-42 FPS), Random Forest ONNX gợi ý danh mục, quét hóa đơn thông minh OCR+Gemini.", "Module 4: Trí tuệ Nhân tạo")
    add_bullet(doc, "Biểu đồ cơ cấu danh mục PieChart Donut và tương quan thu chi 12 tháng BarChart bằng thư viện MPAndroidChart.", "Module 5: Báo cáo Thống kê")

    # MỤC 2
    add_h1(doc, "MỤC 2: PHÂN TÍCH NGHIỆP VỤ & THIẾT KẾ HỆ THỐNG (TUẦN 2 - 3)")
    add_p(doc, "Hệ thống đáp ứng đầy đủ 7 nhóm yêu cầu chức năng (FR-01: Xác thực người dùng, FR-02: Quản lý ví, FR-03: Giao dịch thu chi, FR-04: Quét hóa đơn OCR+AI, FR-05: Camera AI nhận diện vật thể, FR-06: Ngân sách cảnh báo đổi màu, FR-07: Giao dịch định kỳ & Báo cáo biểu đồ) cùng các yêu cầu phi chức năng khắt khe về thời gian đáp ứng API dưới 200ms, giao tiếp mã hóa HTTPS và độ khả dụng 99.9%.", "Phân tích yêu cầu:")
    
    add_h2(doc, "2.1. Thiết kế kiến trúc hệ thống 3 tầng")
    add_p(doc, "Hệ thống áp dụng kiến trúc phân tầng 3 lớp hoàn chỉnh:")
    add_bullet(doc, "Ứng dụng Android Native (Kotlin), kiến trúc MVVM, giao diện Obsidian Dark Theme, tích hợp CameraX, TFLite Float32 và ML Kit OCR.", "Tầng 1 - Client Di động")
    add_bullet(doc, "Máy chủ Spring Boot 3 (Java 21), Spring Security Stateless, bộ lọc FirebaseAuthFilter, mô hình Random Forest nhúng qua ONNX Runtime Java, tác vụ lập lịch Cronjob.", "Tầng 2 - Dịch vụ Máy chủ")
    add_bullet(doc, "Hệ quản trị CSDL MySQL 8.0 (InnoDB, utf8mb4), dịch vụ Google Firebase Authentication và Google Gemini 2.5 Flash API.", "Tầng 3 - Dữ liệu & Đám mây")

    add_h2(doc, "2.2. Thiết kế luồng tích hợp AI và Xác thực")
    add_p(doc, "Pipeline AI 2 giai đoạn (Two-Stage Pipeline): Giai đoạn 1 nạp khung hình CameraX vào YOLOv8n TFLite chạy On-Device để định vị vật thể và vẽ bounding box trực tiếp tại 34–42 FPS; Giai đoạn 2 trích xuất vector 27 đặc trưng gửi về Backend qua API RESTful để mô hình Random Forest ONNX Runtime phân loại ra ID danh mục chi tiêu trong thời gian dưới 2ms kèm cơ chế Fallback an toàn.", "Pipeline AI:")
    add_p(doc, "Luồng xác thực áp dụng kiến trúc Stateless Token: Ứng dụng Android xác thực với Firebase và nhận về ID Token JWT; mọi yêu cầu API gửi lên Backend đều đính kèm header 'Authorization: Bearer <token>'; lớp lọc FirebaseAuthFilter gọi Firebase Admin SDK để giải mã danh tính và thiết lập SecurityContext mà không lưu session trên server.", "Luồng xác thực:")

    add_h2(doc, "2.3. Thiết kế cơ sở dữ liệu quan hệ chuẩn 3NF (ERD)")
    add_p(doc, "Cơ sở dữ liệu gồm 9 bảng chuẩn hóa 3NF: users (thông tin người dùng), accounts (tài khoản ví), categories (danh mục thu chi), transactions (giao dịch thu/chi/chuyển tiền), budgets (hạn mức ngân sách), recurring_transactions (giao dịch định kỳ), transaction_images (ảnh chứng từ đính kèm), ai_product_logs (nhật ký nhận diện sản phẩm AI) và ai_scan_logs (nhật ký quét hóa đơn OCR). Toàn bộ các bảng đều thiết lập ràng buộc toàn vẹn khóa ngoại ON DELETE CASCADE và chỉ mục tối ưu truy vấn.", "Thiết kế CSDL:")

    # MỤC 3
    add_h1(doc, "MỤC 3: CÀI ĐẶT MÔI TRƯỜNG & KHỞI TẠO KHUNG DỰ ÁN (TUẦN 4)")
    add_p(doc, "Thiết lập môi trường phát triển đồng bộ: Android Studio Koala (AGP 8.5.1, Gradle 8.7, Android SDK Platform 34 Target SDK 34, Min SDK 24); IntelliJ IDEA 2024.1 với Java JDK 21 LTS; Docker Desktop và Docker Compose v2.", "Công cụ phát triển:")
    add_p(doc, "Đóng gói toàn bộ máy chủ Spring Boot và cơ sở dữ liệu MySQL 8.0 trong Docker Compose. Tệp docker-compose.yml tự động cấu hình network bridge, phân vùng volume lưu trữ bền vững mysql_data, khởi tạo bộ mã utf8mb4_unicode_ci và tự động nạp tập lệnh SQL init.sql chứa đầy đủ 9 bảng dữ liệu cùng seed data danh mục chuẩn tiếng Việt.", "Đóng gói Docker Compose:")
    add_p(doc, "Cấu hình Firebase Console: Kích hoạt Provider Email/Password và Google Sign-In; tải google-services.json đặt vào thư mục app/ của dự án Android; tải serviceAccountKey.json và cấu hình Firebase Admin SDK cho Backend Spring Boot; xây dựng bộ lọc FirebaseAuthFilter kiểm soát an ninh đa tầng.", "Cấu hình Firebase Auth:")
    add_p(doc, "Dựng khung dự án theo đúng chuẩn Clean Architecture: Dự án Android tổ chức theo các package data, ui, viewmodel, ai, utils; dự án Backend tổ chức theo các package controller, service, repository, model, security, ai, scheduler, exception.", "Khung dự án:")

    # MỤC 4
    add_h1(doc, "MỤC 4: HIỆN THỰC HÓA CÁC CHỨC NĂNG NGHIỆP VỤ CỐT LÕI (TUẦN 5 - 8)")
    add_p(doc, "Lập trình hoàn chỉnh cơ chế đăng ký bằng Email, đăng nhập mật khẩu và đăng nhập Google One-Tap 1 chạm. Khi người dùng mới đăng nhập lần đầu, hệ thống tự động đồng bộ hồ sơ vào CSDL MySQL và tự động khởi tạo ngay ví mặc định 'Tiền mặt' với số dư ban đầu 0 VNĐ.", "Module 1 - Xác thực:")
    add_p(doc, "Quản lý 3 loại ví tiêu chuẩn (Tiền mặt, Ngân hàng, Ví điện tử). Tự động cộng/trừ số dư theo thời gian thực khi phát sinh giao dịch thu chi. Nghiệp vụ chuyển tiền nội bộ giữa các ví được thực thi trong một Database Transaction duy nhất (@Transactional), bảo đảm tính nhất quán dữ liệu tuyệt đối.", "Module 2 - Ví tài chính:")
    add_p(doc, "Ghi chép giao dịch thu chi trực quan, chọn danh mục, đính kèm ảnh chứng từ, lọc theo ngày/tháng/ví và quản lý danh mục phân cấp có icon/màu sắc nhận diện.", "Module 3 & 4 - Giao dịch & Danh mục:")
    add_p(doc, "Thiết lập hạn mức chi tiêu tháng. Xây dựng thuật toán thanh tiến độ đổi màu 3 cấp độ: Màu Xanh lá (<80% - An toàn), Màu Cam vàng (80-100% - Cảnh báo sắp chạm hạn mức), Màu Đỏ (>100% - Báo động bội chi ngân sách).", "Module 5 - Ngân sách cảnh báo:")
    add_p(doc, "Lên lịch các khoản chi cố định (tiền nhà, tiền điện, internet) theo chu kỳ ngày/tuần/tháng. Tác vụ Cronjob @Scheduled(cron = '0 0 0 * * ?') tự động quét và sinh giao dịch vào đúng 00:00 hàng ngày, kèm API kích hoạt tức thì POST /api/recurring/trigger-now phục vụ kiểm thử.", "Module 6 - Giao dịch định kỳ:")
    add_p(doc, "Toàn bộ giao diện được thiết kế theo chuẩn Obsidian Dark Theme: Nền Deep Obsidian #121212, thẻ Card Elevated Grey #1E1E1E, điểm nhấn Emerald Green #00D09C, cảnh báo Amber Orange #F59E0B và báo động Danger Red #EF4444. Ứng dụng vận hành mượt mà, không giật lag trên cả Android Virtual Device (AVD) và thiết bị thật.", "Giao diện Obsidian Dark Theme:")

    doc.save(output_path)
    print(f"Generated docx successfully: {output_path}")

if __name__ == "__main__":
    out_docx = r"d:\Project\KHMT\DATN\TAI_LIEU_THIET_KE_VA_TRIEN_KHAI_DATN.docx"
    out_dl = r"C:\Users\ADMIN\Downloads\TAI_LIEU_THIET_KE_VA_TRIEN_KHAI_DATN.docx"
    build_full_docx(out_docx)
    try:
        shutil.copy2(out_docx, out_dl)
        print(f"Copied to downloads: {out_dl}")
    except Exception as e:
        print(f"Could not copy to downloads: {e}")
