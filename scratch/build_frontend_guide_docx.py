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
t_run = title_p.add_run("HƯỚNG DẪN TỪNG BƯỚC LẬP TRÌNH FRONTEND ANDROID KOTLIN")
t_run.bold = True
t_run.font.size = Pt(16)
t_run.font.color.rgb = RGBColor(13, 71, 161)

sub_p = doc.add_paragraph()
sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
sub_p.paragraph_format.space_after = Pt(14)
sub_run = sub_p.add_run("Quy trình thiết kế XML, gán ID, liên kết ViewBinding, ViewModel & Bắt sự kiện")
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
add_header("1. TỔNG QUAN KIẾN TRÚC GIAO DIỆN ANDROID (MÔ HÌNH MVVM)", 1)
p = doc.add_paragraph()
p.add_run("Ứng dụng quản lý tài chính cá nhân áp dụng kiến trúc chuẩn khuyến nghị bởi Google: **MVVM (Model - View - ViewModel)**. Mục tiêu cốt lõi là phân tách rõ ràng trách nhiệm giữa phần hiển thị và phần logic nghiệp vụ:\n"
          "• View (Layout XML + Activity / Fragment Kotlin): Chỉ chịu trách nhiệm hiển thị giao diện, bắt thao tác chạm/click của người dùng và cập nhật giao diện theo dữ liệu mới.\n"
          "• ViewModel: Bộ não điều phối màn hình, nắm giữ trạng thái giao diện (không bị mất dữ liệu khi người dùng xoay ngang màn hình) và gọi Repository qua Coroutines ngầm.\n"
          "• Model / Repository: Đảm nhiệm việc tải/lưu dữ liệu từ Backend Spring Boot (qua Retrofit) hoặc lưu bộ nhớ đệm (SharedPreferences, Room).")

# ==================== PHẦN 2 ====================
add_header("2. QUY TRÌNH 6 BƯỚC CHUẨN TỪ XML SANG MÃ NGUỒN KOTLIN", 1)

add_header("Bước 1: Thiết kế giao diện bằng XML Layout & Quy tắc đặt ID", 2)
p = doc.add_paragraph("Mọi giao diện được thiết kế trong thư mục res/layout/. Để mã nguồn Kotlin đọc hiểu dễ dàng, dự án áp dụng Quy tắc đặt tên ID (Naming Convention) chuẩn mực theo từng loại View:")

id_tbl = doc.add_table(rows=7, cols=3)
id_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
id_headers = ["Loại View trong XML", "Tiền tố chuẩn", "Ví dụ thực tế trong code của bạn"]
for j, h in enumerate(id_headers):
    c = id_tbl.cell(0, j)
    set_cell_background(c, "1565C0")
    set_cell_margins(c, 80, 80, 100, 100)
    p = c.paragraphs[0]
    p.paragraph_format.space_after = Pt(0)
    r = p.add_run(h)
    r.bold = True
    r.font.size = Pt(9.5)
    r.font.color.rgb = RGBColor(255, 255, 255)

id_rows = [
    ("MaterialButton / Button", "btn", "btnLogin, btnGoogleLogin, btnDemoLogin, btnSave"),
    ("TextInputEditText / EditText", "et", "etEmail, etPassword, etAmount, etNote"),
    ("TextView", "tv", "tvTitle, tvTotalBalance, tvExpenseMonth, tvRegisterLink"),
    ("ImageView / ImageButton", "iv", "ivLogo, ivAvatar, ivCategoryIcon, ivClose"),
    ("RecyclerView", "rv", "rvTransactions, rvAccounts, rvCategoryStats"),
    ("ProgressBar", "progressBar / pb", "progressBar (vòng xoay chờ mạng)")
]
for i, row in enumerate(id_rows, start=1):
    for j, val in enumerate(row):
        c = id_tbl.cell(i, j)
        set_cell_background(c, "F9FAFB" if i % 2 == 1 else "FFFFFF")
        set_cell_margins(c, 60, 60, 100, 100)
        p = c.paragraphs[0]
        p.paragraph_format.space_after = Pt(0)
        r = p.add_run(val)
        r.font.size = Pt(9.5)
        if j == 0:
            r.bold = True

doc.add_paragraph().paragraph_format.space_after = Pt(6)

add_header("Bước 2: Kích hoạt công nghệ ViewBinding trong build.gradle", 2)
p = doc.add_paragraph("Thay vì phương pháp truyền thống findViewById (dễ văng app do NullPointerException khi gõ sai ID), dự án bật ViewBinding trong file app/build.gradle:")
add_code_block("android {\n    ...\n    buildFeatures {\n        viewBinding true  // Kích hoạt ViewBinding\n    }\n}")

p_vb_desc = doc.add_paragraph()
p_vb_desc.add_run("Quy tắc tự sinh Class Binding: ").bold = True
p_vb_desc.add_run("Mỗi file layout XML tên dạng snake_case sẽ tự động sinh class PascalCase thêm đuôi Binding:\n"
                  "• activity_login.xml -> Tự sinh ActivityLoginBinding\n"
                  "• activity_main.xml -> Tự sinh ActivityMainBinding\n"
                  "• fragment_add_transaction.xml -> Tự sinh FragmentAddTransactionBinding\n"
                  "• item_transaction.xml -> Tự sinh ItemTransactionBinding")

add_header("Bước 3: Khởi tạo ViewBinding trong Activity và Fragment", 2)
p = doc.add_paragraph()
p.add_run("1. Khởi tạo trong Activity (Ví dụ: LoginActivity.kt):\n").bold = True
add_code_block("class LoginActivity : AppCompatActivity() {\n    private lateinit var binding: ActivityLoginBinding\n\n    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n        binding = ActivityLoginBinding.inflate(layoutInflater)\n        setContentView(binding.root)\n        // Từ đây, gọi trực tiếp ID qua biến binding!\n        binding.btnLogin.text = \"Đăng nhập\"\n    }\n}")

p = doc.add_paragraph()
p.add_run("2. Khởi tạo trong Fragment (Ví dụ: AddTransactionFragment.kt):\n").bold = True
p.add_run("Trong Fragment, View có thể bị hủy trong khi Fragment vẫn sống trong bộ nhớ. Để chống rò rỉ RAM (Memory Leak), Google quy định dùng biến đệm _binding và gán null tại onDestroyView():")
add_code_block("class AddTransactionFragment : DialogFragment() {\n    private var _binding: FragmentAddTransactionBinding? = null\n    private val binding get() = _binding!!\n\n    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {\n        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)\n        return binding.root\n    }\n\n    override fun onDestroyView() {\n        super.onDestroyView()\n        _binding = null // Giải phóng bộ nhớ View (Cực kỳ quan trọng!)\n    }\n}")

add_header("Bước 4: Bắt sự kiện người dùng (Click Listeners & Validation)", 2)
p = doc.add_paragraph("Mọi ID đặt trong XML đều trở thành thuộc tính an toàn kiểu của đối tượng binding. Dưới đây là mẫu bắt click và kiểm tra dữ liệu (Validation) chuẩn:")
add_code_block("// 1. Bắt click nút đăng nhập\nbinding.btnLogin.setOnClickListener {\n    handleLogin()\n}\n\nprivate fun handleLogin() {\n    val email = binding.etEmail.text.toString().trim()\n    val password = binding.etPassword.text.toString().trim()\n\n    // Kiểm tra rỗng và định dạng Email\n    if (email.isEmpty()) {\n        binding.etEmail.error = \"Vui lòng nhập Email\"\n        binding.etEmail.requestFocus()\n        return\n    }\n    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {\n        binding.etEmail.error = \"Email không đúng định dạng\"\n        binding.etEmail.requestFocus()\n        return\n    }\n    if (password.length < 6) {\n        binding.etPassword.error = \"Mật khẩu phải từ 6 ký tự trở lên\"\n        binding.etPassword.requestFocus()\n        return\n    }\n\n    // Khóa nút để tránh spam click và chuyển cho ViewModel xử lý\n    binding.btnLogin.isEnabled = false\n    viewModel.signIn(email, password, authHelper)\n}")

add_header("Bước 5: Kết nối ViewModel & Lắng nghe LiveData", 2)
p = doc.add_paragraph("Giao diện (View) giữ vai trò thụ động, chỉ quan sát (observe) các dòng dữ liệu LiveData từ ViewModel để cập nhật giao diện bất đồng bộ:")
add_code_block("// Khởi tạo ViewModel\nviewModel = ViewModelProvider(this)[AuthViewModel::class.java]\n\n// Lắng nghe trạng thái Loading để ẩn/hiện ProgressBar\nviewModel.isLoading.observe(this) { isLoading ->\n    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE\n    binding.btnLogin.isEnabled = !isLoading\n}\n\n// Lắng nghe khi đăng nhập thành công\nviewModel.syncedUser.observe(this) { user ->\n    if (user != null) {\n        Toast.makeText(this, \"Chào mừng ${user.fullName}!\", Toast.LENGTH_SHORT).show()\n        startActivity(Intent(this, MainActivity::class.java))\n        finish()\n    }\n}\n\n// Lắng nghe thông báo lỗi\nviewModel.errorMessage.observe(this) { errorMsg ->\n    if (!errorMsg.isNullOrEmpty()) {\n        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()\n    }\n}")

add_header("Bước 6: Hiển thị danh sách động với RecyclerView & Adapter", 2)
p = doc.add_paragraph("Để hiển thị danh sách các khoản thu/chi linh hoạt:")
p_rv = doc.add_paragraph()
p_rv.add_run("1. Tạo layout từng dòng: ").bold = True
p_rv.add_run("item_transaction.xml (chứa tvCategoryName, tvNote, tvAmount, ivCategoryIcon).\n")
p_rv.add_run("2. Viết Adapter kết hợp ViewBinding:\n").bold = True
add_code_block("class TransactionAdapter(\n    private val list: List<Transaction>,\n    private val onItemClick: (Transaction) -> Unit\n) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {\n\n    inner class ViewHolder(val binding: ItemTransactionBinding) : \n        RecyclerView.ViewHolder(binding.root)\n\n    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {\n        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)\n        return ViewHolder(binding)\n    }\n\n    override fun onBindViewHolder(holder: ViewHolder, position: Int) {\n        val item = list[position]\n        holder.binding.tvCategoryName.text = item.categoryName\n        holder.binding.tvNote.text = item.note\n        holder.binding.tvAmount.text = CurrencyFormatter.formatVND(item.amount)\n        holder.itemView.setOnClickListener { onItemClick(item) }\n    }\n\n    override fun getItemCount() = list.size\n}")

p_att = doc.add_paragraph("3. Gắn Adapter vào RecyclerView trong Activity/Fragment:")
add_code_block("val adapter = TransactionAdapter(transactionList) { item -> /* Xử lý khi click item */ }\nbinding.rvTransactions.layoutManager = LinearLayoutManager(this)\nbinding.rvTransactions.adapter = adapter")

# ==================== PHẦN 3 ====================
add_header("3. PHÂN TÍCH 2 MÀN HÌNH THỰC TẾ TRONG DỰ ÁN", 1)

add_header("Màn hình 1: Đăng nhập (activity_login.xml -> LoginActivity.kt)", 2)
p = doc.add_paragraph("• Phía XML: Bố trí ivLogo (hỗ trợ Long-click mở dialog IP Server), etEmail, etPassword, btnLogin, btnGoogleLogin, btnDemoLogin, và progressBar.\n"
                      "• Phía Kotlin: LoginActivity.kt khởi tạo ActivityLoginBinding, gán sự kiện cho từng nút và kết nối với AuthViewModel để xử lý đăng nhập Email, Google hoặc Demo.")

add_header("Màn hình 2: Thêm giao dịch (fragment_add_transaction.xml -> AddTransactionFragment.kt)", 2)
p = doc.add_paragraph("• Phía XML: Tab Chi tiêu / Thu nhập, ô nhập tiền etAmount (tự format dấu phẩy hàng nghìn), danh sách danh mục spinnerCategory, ví tiền spinnerAccount, chọn ngày tvDate, nút lưu btnSave.\n"
                      "• Phía Kotlin: AddTransactionFragment.kt kế thừa DialogFragment, dùng FragmentAddTransactionBinding, lắng nghe thay đổi số tiền qua TextWatcher để kiểm tra hạn mức ngân sách cảnh báo tức thì.")

# ==================== PHẦN 4 ====================
add_header("4. CÁC LỖI LẬP TRÌNH GIAO DIỆN THƯỜNG GẶP VÀ CÁCH KHẮC PHỤC", 1)

err_tbl = doc.add_table(rows=5, cols=3)
err_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
headers = ["Lỗi thường gặp", "Biểu hiện trên ứng dụng", "Cách khắc phục chuẩn kỹ thuật"]
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

errors_info = [
    ("NullPointerException khi gọi binding", "App crash văng ra ngoài ngay khi mở hoặc đóng Fragment.", "Trong Fragment, luôn chỉ truy cập binding sau onCreateView và dọn dẹp _binding = null trong onDestroyView()."),
    ("Giao diện không cập nhật sau khi gọi API", "Backend đã thêm bản ghi nhưng danh sách trên màn hình vẫn cũ.", "Gọi adapter.notifyDataSetChanged() hoặc nâng cấp dùng ListAdapter với DiffUtil để tự động tính toán mục thay đổi."),
    ("Đơ giao diện (Lỗi ANR - App Not Responding)", "Màn hình bị đơ cứng khi bấm nút nhận diện AI hoặc gửi dữ liệu mạng.", "Tuyệt đối không chạy tác vụ mạng/AI trên Main Thread. Luôn sử dụng Coroutines với Dispatchers.IO."),
    ("Mất dữ liệu khi xoay màn hình", "Form đang gõ dở thì xoay máy bị xóa sạch toàn bộ nội dung.", "Lưu trữ dữ liệu trong ViewModel thay vì lưu trong biến cục bộ của Activity.")
]
for i, row in enumerate(errors_info, start=1):
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

output_path = r"D:\Project\KHMT\DATN\HUONG_DAN_LAP_TRINH_FRONTEND_KOTLIN.docx"
doc.save(output_path)
print(f"SUCCESS: Saved document at {output_path}")
