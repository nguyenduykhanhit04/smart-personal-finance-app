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
        run.font.color.rgb = RGBColor(13, 71, 161) # Deep Navy
    elif level == 2:
        run.font.size = Pt(12.5)
        run.font.color.rgb = RGBColor(21, 101, 192)
    elif level == 3:
        run.font.size = Pt(11.5)
        run.font.color.rgb = RGBColor(40, 53, 147)
    return p

def add_callout(quote_text, title='', border_color='1976D2', bg_color='F0F4F8'):
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
    p.paragraph_format.line_spacing = 1.15
    if title:
        run_t = p.add_run(f"{title}\n")
        run_t.bold = True
        run_t.font.size = Pt(10)
        run_t.font.color.rgb = RGBColor(13, 71, 161)
    run = p.add_run(quote_text)
    run.font.size = Pt(10)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

def add_code_block(code_text):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    set_cell_background(cell, "263238") # Dark Blue-Gray
    set_cell_margins(cell, top=80, bottom=80, left=120, right=120)
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    run = p.add_run(code_text)
    run.font.name = 'Consolas'
    run.font.size = Pt(9.5)
    run.font.color.rgb = RGBColor(236, 239, 241)
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

# ==================== DOCUMENT HEADER ====================
title_p = doc.add_paragraph()
title_p.paragraph_format.space_before = Pt(0)
title_p.paragraph_format.space_after = Pt(4)
title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
t_run = title_p.add_run("HƯỚNG DẪN TỪNG BƯỚC CẤU HÌNH FIREBASE, ĐĂNG NHẬP GOOGLE & GEMINI API")
t_run.bold = True
t_run.font.size = Pt(16)
t_run.font.color.rgb = RGBColor(13, 71, 161)

sub_p = doc.add_paragraph()
sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub_p.paragraph_format.space_after = Pt(14)
sub_run = sub_p.add_run("Tài liệu Kỹ thuật Tích hợp Dịch vụ Đám mây & Trí tuệ Nhân tạo")
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
add_header("PHẦN 1: HƯỚNG DẪN CẤU HÌNH FIREBASE TỪ A ĐẾN Z", 1)
p = doc.add_paragraph("Firebase đóng vai trò là hệ thống xác thực người dùng tập trung (Identity Provider) và cầu nối bảo mật giữa Android App với Backend Spring Boot. Toàn bộ tính năng trong hướng dẫn này đều nằm trong gói Spark Plan miễn phí vĩnh viễn của Google.")

add_header("1.1 Tạo Project mới trên Firebase Console", 2)
steps1 = [
    "Truy cập vào trang quản trị: https://console.firebase.google.com/ và đăng nhập bằng tài khoản Google cá nhân.",
    "Nhấp vào nút 'Add project' (hoặc 'Thêm dự án').",
    "Đặt tên dự án (ví dụ: personal-finance-app) -> Nhấp nút 'Continue'.",
    "Ở bước Google Analytics: Có thể tắt để đơn giản hóa -> Nhấp nút 'Create project'.",
    "Chờ 5-10 giây để Google thiết lập hệ thống -> Bấm 'Continue' để vào Dashboard quản trị."
]
for i, s in enumerate(steps1, 1):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run(f"• Bước {i}: ").bold = True
    p.add_run(s)

add_header("1.2 Lấy mã vân tay SHA-1 Fingerprint của máy tính", 2)
add_callout(
    "ĐÂY LÀ BƯỚC BẮT BUỘC ĐỂ GOOGLE SIGN-IN HOẠT ĐỘNG! Google yêu cầu mã băm chứng chỉ ký (SHA-1) của máy tính bạn để xác thực nguồn gốc ứng dụng. Nếu thiếu SHA-1, app sẽ báo lỗi Code 10 hoặc 12500 khi bấm đăng nhập Google.",
    "LƯU Ý QUAN TRỌNG VỀ BẢO MẬT:",
    border_color="E65100",
    bg_color="FFF3E0"
)

p = doc.add_paragraph()
p.add_run("Cách 1: Lấy trực tiếp từ Android Studio (Khuyên dùng):\n").bold = True
p.add_run("1. Mở dự án android-app bằng Android Studio.\n"
          "2. Nhấp vào tab 'Gradle' ở cạnh phải màn hình.\n"
          "3. Mở rộng cây: android-app -> app -> Tasks -> android -> Nhấp đúp chuột vào 'signingReport'.\n"
          "4. Cửa sổ Run bên dưới sẽ in ra mã SHA1 (dạng 20 cặp ký tự phân tách bằng dấu hai chấm). Copy toàn bộ chuỗi này.")

p2 = doc.add_paragraph()
p2.add_run("Cách 2: Chạy lệnh bằng PowerShell:\n").bold = True
add_code_block('keytool -list -v -keystore "$env:USERPROFILE\\.android\\debug.keystore" -alias androiddebugkey -storepass android -keypass android')

add_header("1.3 Thêm ứng dụng Android vào Firebase", 2)
p = doc.add_paragraph("Trên màn hình Firebase Console, nhấp vào biểu tượng Android và điền đúng thông tin sau:")
steps_app = [
    ("Android package name (BẮT BUỘC PHẢI ĐÚNG)", "com.example.personalfinance"),
    ("App nickname (Tên gợi nhớ)", "Finance App Android"),
    ("Debug signing certificate SHA-1", "Dán chuỗi SHA-1 lấy được ở bước 1.2 (Ví dụ: 2B:6A:8B:B3:6C:29:C6:DD:8B:0C:49:25:6E:11:50:C9:CD:32:A0:5B)")
]
for k, v in steps_app:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run("• " + k + ": ").bold = True
    p.add_run(v)
p_reg = doc.add_paragraph("-> Nhấp nút 'Register app' (Đăng ký ứng dụng).")
p_reg.paragraph_format.space_after = Pt(4)

add_header("1.4 Tải và đặt file cấu hình google-services.json", 2)
p = doc.add_paragraph("1. Firebase sẽ hiển thị nút tải file 'Download google-services.json'. Bấm tải file về máy.\n"
                      "2. Sao chép (Copy) file vừa tải và dán đè vào đúng vị trí sau trong dự án:\n"
                      "   DATN/android-app/app/google-services.json\n"
                      "3. Trên web Firebase, bấm Next -> Next -> Continue to console.")

add_header("1.5 Tạo và tải file khoá Backend firebase-service-account.json", 2)
p = doc.add_paragraph("Để Backend Spring Boot có thể liên lạc an toàn với Firebase Admin SDK:\n"
                      "1. Trên Firebase Console, nhấp biểu tượng bánh răng 'Project settings' (Cài đặt dự án).\n"
                      "2. Chuyển sang tab 'Service accounts' (Tài khoản dịch vụ).\n"
                      "3. Đảm bảo ngôn ngữ chọn là Java -> Nhấp nút 'Generate new private key' (Tạo khóa riêng tư mới) -> Xác nhận Generate key.\n"
                      "4. Đổi tên file JSON vừa tải về thành: firebase-service-account.json\n"
                      "5. Dán file này vào đúng vị trí sau của Backend:\n"
                      "   DATN/backend/src/main/resources/firebase-service-account.json")

# ==================== PHẦN 2 ====================
add_header("PHẦN 2: HƯỚNG DẪN CHI TIẾT TÍNH NĂNG ĐĂNG NHẬP GOOGLE", 1)

add_header("2.1 Bật phương thức xác thực Google trên Firebase", 2)
p = doc.add_paragraph("1. Tại Firebase Console, vào menu bên trái: Build -> Authentication -> Tab Sign-in method.\n"
                      "2. Nhấp chọn 'Google' -> Gạt công tắc sang Enable.\n"
                      "3. Tại ô 'Project support email': Chọn email cá nhân của bạn -> Nhấp nút Save.\n"
                      "4. Tương tự, nhấp chọn 'Email/Password' -> Bật Enable -> Nhấp Save.")

add_header("2.2 Cơ chế hoạt động của Google Sign-In trong mã nguồn", 2)
p = doc.add_paragraph("Hệ thống hoạt động theo 4 bước khép kín:")
g_flow = [
    ("Bước 1: Khởi tạo Google Client", "App đọc Web Client ID (client_type: 3) từ google-services.json qua lệnh authHelper.initGoogleSignIn()."),
    ("Bước 2: Hiển thị hộp thoại Google Play", "Khi người dùng nhấp 'Đăng nhập với Google', app khởi chạy signInIntent của Google Play Services để chọn tài khoản Gmail."),
    ("Bước 3: Xác thực Firebase Credential", "Google cấp ID Token. App chuyển đổi thành GoogleAuthProvider.getCredential(idToken) và đăng nhập vào Firebase Auth."),
    ("Bước 4: Đồng bộ CSDL Backend", "Firebase trả về FirebaseUser. App gửi HTTP POST đến /api/auth/firebase-login. Backend Spring Boot kiểm tra và lưu người dùng vào bảng users MySQL, tạo ví tiền mặc định và trả về phiên đăng nhập.")
]
for title, desc in g_flow:
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run("• " + title + ": ").bold = True
    p.add_run(desc)

add_header("2.3 Các mã lỗi phổ biến và cách xử lý triệt để", 2)
err_tbl = doc.add_table(rows=4, cols=3)
err_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
headers = ["Mã lỗi (Status Code)", "Nguyên nhân thực tế", "Cách khắc phục 100%"]
for j, h in enumerate(headers):
    c = err_tbl.cell(0, j)
    set_cell_background(c, "1565C0")
    set_cell_margins(c, 80, 80, 100, 100)
    p = c.paragraphs[0]
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run(h)
    r.bold = True
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(255, 255, 255)

err_data = [
    ("Mã lỗi 10 (DEVELOPER_ERROR)", "Sai mã SHA-1 giữa máy tính hiện tại và Firebase, hoặc sai package name.", "Lấy lại mã SHA-1 của máy tính dán vào Firebase Console -> Tải lại google-services.json đặt vào app/."),
    ("Mã lỗi 12500 (SIGN_IN_FAILED)", "Chưa kích hoạt Google Provider trên Firebase Console hoặc chưa chọn Support Email.", "Vào Firebase -> Authentication -> Sign-in method -> Bật Google và chọn email hỗ trợ."),
    ("Mã lỗi 7 (NETWORK_ERROR)", "Mất kết nối mạng hoặc máy ảo (Emulator) không có Google Play Services.", "Bật mạng, hoặc chọn tạo thiết bị máy ảo có biểu tượng Google Play Store.")
]
for i, row in enumerate(err_data, start=1):
    for j, val in enumerate(row):
        c = err_tbl.cell(i, j)
        set_cell_background(c, "F9FAFB" if i % 2 == 1 else "FFFFFF")
        set_cell_margins(c, 60, 60, 100, 100)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(val)
        r.font.size = Pt(9)
        if j == 0:
            r.bold = True

doc.add_paragraph().paragraph_format.space_after = Pt(6)

add_header("2.4 Cơ chế dự phòng an toàn: Chế độ Đăng nhập Demo", 2)
add_callout(
    "Khi đi bảo vệ đồ án tốt nghiệp trước Hội đồng, nếu mạng Wifi trường chập chờn hoặc máy ảo bị lag Google Services, bạn chỉ cần nhấp vào nút 'Đăng nhập Demo' (Quick Demo Login) ngay trên màn hình. App sẽ vào thẳng Dashboard với đầy đủ số dư ví, các giao dịch và biểu đồ tài chính mẫu để bạn thuyết trình mượt mà 100%!",
    "BÍ QUYẾT BẢO VỆ AN TOÀN:",
    border_color="2E7D32",
    bg_color="E8F5E9"
)

# ==================== PHẦN 3 ====================
add_header("PHẦN 3: HƯỚNG DẪN ĐĂNG KÝ & TÍCH HỢP GEMINI API BÓC TÁCH HÓA ĐƠN", 1)
p = doc.add_paragraph("Gemini AI đóng vai trò là khối xử lý ngôn ngữ tự nhiên thông minh: đọc hiểu nội dung hóa đơn do camera quét được (qua Google ML Kit OCR) để tự động trích xuất các thông tin tài chính.")

add_header("3.1 Đăng ký Gemini API Key miễn phí tại Google AI Studio", 2)
steps_gemini = [
    "Truy cập vào trang: https://aistudio.google.com/ và đăng nhập bằng tài khoản Gmail của bạn.",
    "Ở thanh điều hướng góc trái, nhấp vào nút 'Get API key'.",
    "Nhấp vào nút 'Create API key' (Tạo khóa API).",
    "Chọn dự án Google Cloud có sẵn hoặc chọn 'Create API key in new project' để hệ thống tự tạo dự án mới.",
    "Chuỗi khóa API Key cá nhân sẽ hiện ra -> Nhấp nút Copy để lưu khóa an toàn (không chia sẻ công khai).",
    "Gói Free tier của Google cho phép gọi 15 lượt/phút và 1.500 lượt/ngày hoàn toàn miễn phí, không yêu cầu thẻ tín dụng."
]
for i, s in enumerate(steps_gemini, 1):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.space_after = Pt(2)
    p.add_run(f"• Bước {i}: ").bold = True
    p.add_run(s)

add_header("3.2 Cấu hình API Key vào dự án Backend Spring Boot", 2)
p = doc.add_paragraph("Khuyến nghị cấu hình qua biến môi trường hệ thống GEMINI_API_KEY để bảo mật tuyệt đối khi deploy:")
add_code_block("# 1. Cách 1: Thiết lập biến môi trường (Khuyên dùng - Chuẩn bảo mật)\n# Trên PowerShell:\n$env:GEMINI_API_KEY=\"YOUR_GEMINI_API_KEY_HERE\"\n# Trên Linux/macOS:\nexport GEMINI_API_KEY=\"YOUR_GEMINI_API_KEY_HERE\"\n\n# 2. Cách 2: Điền trong ai-settings.properties khi chạy cục bộ\ngemini.api.key=${GEMINI_API_KEY:YOUR_GEMINI_API_KEY_HERE}\ngemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")

add_header("3.3 Kỹ thuật Prompt Engineering bóc tách 4 trường thông tin", 2)
p = doc.add_paragraph("Trong file GeminiService.java, hệ thống truyền đoạn chỉ dẫn nghiêm ngặt yêu cầu Gemini trả về đúng 4 trường thông tin:")
prompt_sample = ("{\n"
                 '  "merchant": "Highlands Coffee",\n'
                 '  "amount": 89000,\n'
                 '  "date": "2026-10-18",\n'
                 '  "category": "food"\n'
                 "}")
add_code_block(prompt_sample)

p_exp = doc.add_paragraph()
p_exp.add_run("• merchant: ").bold = True
p_exp.add_run("Tên thương hiệu ngắn gọn (Highlands Coffee, WinMart, Grab...)\n")
p_exp.add_run("• amount: ").bold = True
p_exp.add_run("Tổng số tiền khách hàng phải thanh toán cuối cùng (kiểu số nguyên VND)\n")
p_exp.add_run("• date: ").bold = True
p_exp.add_run("Ngày giao dịch chuẩn định dạng YYYY-MM-DD\n")
p_exp.add_run("• category: ").bold = True
p_exp.add_run("Từ khóa danh mục phù hợp nhất: food, transport, shopping, bills, entertainment, health, other.")

add_header("3.4 Cơ chế Fallback Regex tự động khi mất mạng hoặc không có Key", 2)
add_callout(
    "ĐIỂM SÁNG TẠO VỀ ĐỘ ỔN ĐỊNH HỆ THỐNG: Trong code AiScanController.java, nếu gọi Gemini API thất bại (do mất mạng, hết quota hoặc chưa cấu hình key), hệ thống sẽ TỰ ĐỘNG CHUYỂN SANG BỘ BÓC TÁCH CỤC BỘ (ReceiptParser.java) bằng Biểu thức chính quy (Regex Heuristic). App sẽ tự động bóc tách tổng tiền và ngày tháng mà không bao giờ bị crash hay gián đoạn dịch vụ!",
    "TÍNH NĂNG TỰ PHỤC HỒI (FAIL-SAFE DESIGN):",
    border_color="00897B",
    bg_color="E0F2F1"
)

output_path = r"D:\Project\KHMT\DATN\HUONG_DAN_CAU_HINH_FIREBASE_GOOGLE_GEMINI.docx"
doc.save(output_path)
print(f"SUCCESS: Saved document at {output_path}")
