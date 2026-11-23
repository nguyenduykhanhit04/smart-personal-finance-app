from docx import Document
from docx.enum.text import WD_COLOR_INDEX
from docx.oxml import OxmlElement
from docx.text.paragraph import Paragraph
from pathlib import Path

src = Path('documents/Báo cáo chuẩn/22CN4_Nhóm03_TH4311_11.6.2026.docx')
out = src.with_name(src.stem + '_danh_dau_can_sua.docx')
doc = Document(src)

# Each rule marks a report paragraph whose project-specific claim no longer matches the code.
rules = [
 (lambda t: 'Java được sử dụng làm ngôn ngữ lập trình chính để phát triển ứng dụng Android' in t,
  'CẦN SỬA: Ứng dụng Android hiện viết bằng Kotlin. Đổi các mô tả Java thành Kotlin và cập nhật ví dụ/ảnh chụp mã nguồn theo code hiện tại.'),
 (lambda t: 'Android sử dụng ngôn ngữ Java' in t and ('MVVM' in t or 'Client' in t),
  'CẦN SỬA: Đổi Java thành Kotlin. Chỉ mô tả MVVM ở mức các thành phần thực sự có trong app (Activity/Fragment, ViewModel, Repository); tránh khẳng định toàn bộ cấu trúc tuân thủ MVVM nếu chưa đối chiếu.'),
 (lambda t: 'Frontend của hệ thống được xây dựng trên nền tảng Android bằng ngôn ngữ Java' in t,
  'CẦN SỬA: Đổi Java thành Kotlin; cập nhật phần công nghệ giao diện theo Android hiện tại.'),
 (lambda t: 'Front-end được phát triển bằng Android Studio sử dụng ngôn ngữ Java' in t or 'Frontend được phát triển bằng Android Studio sử dụng ngôn ngữ Java' in t,
  'CẦN SỬA: Đổi ngôn ngữ triển khai Android từ Java sang Kotlin.'),
 (lambda t: 'dự báo chi tiêu' in t.lower() and ('random forest' in t.lower() or 'Random Forest' in t),
  'CẦN SỬA: Source hiện không triển khai dự báo xu hướng chi tiêu bằng Random Forest. Random Forest ONNX hiện phân loại danh mục chi tiêu từ các vật thể YOLO; hãy thay nội dung dự báo bằng luồng YOLO TFLite trên Android → phân loại ONNX ở backend.'),
 (lambda t: 'dự đoán xu hướng chi tiêu' in t.lower() and 'random forest' in t.lower(),
  'CẦN SỬA: Random Forest trong source dùng để phân loại danh mục chi tiêu từ kết quả nhận diện vật phẩm, không dự đoán xu hướng chi tiêu theo lịch sử giao dịch.'),
 (lambda t: 'phân tích dữ liệu giao dịch trong quá khứ và dự đoán' in t.lower(),
  'CẦN SỬA: Thay chức năng dự đoán này bằng chức năng đang có: YOLO nhận diện vật phẩm, backend dùng Random Forest ONNX để đề xuất danh mục giao dịch.'),
 (lambda t: 'Facebook Login' in t or ('Google và Facebook' in t and 'đăng nhập' in t.lower()),
  'CẦN SỬA: Kiểm tra lại phương thức đăng nhập được cấu hình trong app. Source hiện thể hiện Email/Password và Google; bỏ Facebook nếu dự án không tích hợp.'),
 (lambda t: 'Thông tin người dùng được lưu trữ trên Firebase' in t,
  'CẦN SỬA: Phân biệt Firebase Authentication (xác thực) với dữ liệu hồ sơ ứng dụng. Hồ sơ và dữ liệu tài chính được lưu qua backend/MySQL, không mô tả là lưu trên Firebase.'),
 (lambda t: 'Firebase' in t and 'dự báo tài chính' in t.lower(),
  'CẦN SỬA: Bỏ “dự báo tài chính” khỏi danh sách chức năng được bảo vệ nếu không có chức năng dự báo trong source; thay bằng các chức năng đang triển khai như ví, giao dịch, ngân sách, định kỳ và báo cáo.'),
 (lambda t: 'Firebase Authentication' in t and 'đăng ký và đăng nhập' in t.lower() and ('Google hoặc Facebook' in t or 'Facebook' in t),
  'CẦN SỬA: Chỉ liệt kê phương thức đăng nhập có trong app (Email/Password và Google, nếu cấu hình thực tế xác nhận).'),
 (lambda t: 'Dự báo chi tiêu.' in t,
  'CẦN SỬA: Đây không phải chức năng được xác nhận trong source hiện tại. Thay bằng “Nhận diện vật phẩm và đề xuất danh mục chi tiêu bằng YOLO + Random Forest” hoặc xóa nếu không thuộc phạm vi.'),
 (lambda t: 'Dự báo chi tiêu bằng Random Forest' in t,
  'CẦN SỬA: Đổi thành “Phân loại danh mục chi tiêu bằng Random Forest ONNX từ kết quả nhận diện YOLO”; xóa mô tả phân tích lịch sử để dự đoán tương lai.'),
]

marked = []
for p in list(doc.paragraphs):
    text = p.text.strip()
    if not text:
        continue
    note = next((msg for test, msg in rules if test(text)), None)
    if not note:
        continue
    # Avoid duplicate notes if multiple rules match the same paragraph.
    for run in p.runs:
        if run.text.strip():
            run.font.highlight_color = WD_COLOR_INDEX.YELLOW
    el = OxmlElement('w:p')
    p._p.addnext(el)
    note_para = Paragraph(el, p._parent)
    note_run = note_para.add_run(note)
    note_run.bold = True
    note_run.font.color.rgb = __import__('docx').shared.RGBColor(192, 0, 0)
    note_para.paragraph_format.space_after = p.paragraph_format.space_after
    marked.append(text[:100])

doc.save(out)
print(f'OUTPUT={out}')
print(f'MARKED={len(marked)}')
for item in marked:
    print(item)

