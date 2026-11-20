# 📱 HƯỚNG DẪN TỪNG BƯỚC LẬP TRÌNH FRONTEND ANDROID KOTLIN
**Đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân thông minh tích hợp Trí tuệ Nhân tạo và Thị giác Máy tính trên nền tảng Android  
**Sinh viên thực hiện:** Nguyễn Duy Khánh — Lớp: 22CN4 — Mã SV: 2255010114  
**Giảng viên hướng dẫn:** TS. Nguyễn Mạnh Thắng  
**Cơ sở đào tạo:** Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội  

---

## 📌 MỤC LỤC
1. [Tổng quan Kiến trúc Giao diện Android (Mô hình MVVM)](#1-tổng-quan-kiến-trúc-giao-diện-android-mô-hình-mvvm)
2. [Quy trình 6 bước chuẩn: Từ thiết kế XML đến mã nguồn Kotlin hoạt động](#2-quy-trình-6-bước-chuẩn-từ-thiết-kế-xml-đến-mã-nguồn-kotlin-hoạt-động)
   - [Bước 1: Thiết kế giao diện bằng XML Layout & Quy tắc đặt ID](#bước-1-thiết-kế-giao-diện-bằng-xml-layout--quy-tắc-đặt-id)
   - [Bước 2: Kích hoạt công nghệ ViewBinding trong build.gradle](#bước-2-kích-hoạt-công-nghệ-viewbinding-trong-buildgradle)
   - [Bước 3: Khởi tạo ViewBinding trong Activity và Fragment](#bước-3-khởi-tạo-viewbinding-trong-activity-và-fragment)
   - [Bước 4: Bắt sự kiện người dùng (Click Listeners & Validation)](#bước-4-bắt-sự-kiện-người-dùng-click-listeners--validation)
   - [Bước 5: Kết nối ViewModel & Lắng nghe LiveData](#bước-5-kết-nối-viewmodel--lắng-nghe-livedata)
   - [Bước 6: Hiển thị danh sách động với RecyclerView & Adapter](#bước-6-hiển-thị-danh-sách-động-với-recyclerview--adapter)
3. [Phân tích 2 màn hình thực tế trong Dự án](#3-phân-tích-2-màn-hình-thực-tế-trong-dự-án)
   - [Màn hình 1: Đăng nhập (`activity_login.xml` ➔ `LoginActivity.kt`)](#màn-hình-1-đăng-nhập-activity_loginxml--loginactivitykt)
   - [Màn hình 2: Thêm giao dịch (`fragment_add_transaction.xml` ➔ `AddTransactionFragment.kt`)](#màn-hình-2-thêm-giao-dịch-fragment_add_transactionxml--addtransactionfragmentkt)
4. [Các lỗi lập trình giao diện thường gặp và cách khắc phục](#4-các-lỗi-lập-trình-giao-diện-thường-gặp-và-cách-khắc-phục)

---

## 1. TỔNG QUAN KIẾN TRÚC GIAO DIỆN ANDROID (MÔ HÌNH MVVM)

Toàn bộ ứng dụng Android trong đồ án được xây dựng theo kiến trúc chuẩn khuyến nghị bởi Google: **MVVM (Model - View - ViewModel)**:

```text
┌────────────────────────────────────────────────────────┐
│                   VIEW (Giao diện)                     │
│  • File XML: Định nghĩa bố cục, màu sắc, font chữ      │
│  • Activity / Fragment (Kotlin): Nhận tương tác user,   │
│    hiển thị dữ liệu, bắt click                           │
└───────────────────────────▲────────────────────────────┘
                            │ Quan sát dữ liệu qua LiveData / StateFlow
                            ▼
┌────────────────────────────────────────────────────────┐
│                 VIEWMODEL (Bộ não UI)                  │
│  • Giữ trạng thái màn hình (không mất khi xoay máy)    │
│  • Xử lý logic nghiệp vụ, gọi Repository qua Coroutines│
└───────────────────────────▲────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                MODEL / REPOSITORY (Dữ liệu)            │
│  • ApiService (Retrofit): Gọi API Backend Spring Boot  │
│  • Local Storage: SharedPreferences, Room Database     │
└────────────────────────────────────────────────────────┘
```

---

## 2. QUY TRÌNH 6 BƯỚC CHUẨN: TỪ THIẾT KẾ XML ĐẾN MÃ NGUỒN KOTLIN HOẠT ĐỘNG

### Bước 1: Thiết kế giao diện bằng XML Layout & Quy tắc đặt ID
* Vị trí lưu trữ: Thư mục `android-app/app/src/main/res/layout/`.
* Các ViewGroup phổ biến:
  * `ConstraintLayout`: Tối ưu hiệu năng, linh hoạt neo các View theo tỷ lệ màn hình (dùng nhiều nhất).
  * `LinearLayout`: Xếp các phần tử theo hàng dọc (`vertical`) hoặc hàng ngang (`horizontal`).
  * `CoordinatorLayout` + `BottomSheet`: Dùng cho các bảng trượt vuốt từ đáy màn hình lên.

#### Quy tắc vàng khi đặt `android:id="@+id/..."`:
Để code Kotlin tường minh, dễ nhớ và không bao giờ bị nhầm lẫn giữa hàng chục View, ta đặt tiền tố viết tắt theo loại component:

| Loại View trong XML | Tiền tố chuẩn | Ví dụ trong dự án của bạn | Mục đích sử dụng |
| :--- | :---: | :--- | :--- |
| **MaterialButton / Button** | `btn` | `btnLogin`, `btnGoogleLogin`, `btnSave` | Nút bấm thao tác |
| **TextInputEditText / EditText** | `et` | `etEmail`, `etPassword`, `etAmount` | Ô người dùng gõ chữ/số |
| **TextView** | `tv` | `tvTitle`, `tvBalance`, `tvRegisterLink` | Nhãn hiển thị chữ tĩnh/động |
| **ImageView** | `iv` | `ivLogo`, `ivAvatar`, `ivCategoryIcon` | Hiển thị ảnh, icon |
| **RecyclerView** | `rv` | `rvTransactions`, `rvAccounts` | Danh sách cuộn động |
| **ProgressBar** | `progressBar` / `pb` | `progressBar` | Vòng xoay khi đang tải dữ liệu |

*Ví dụ một đoạn XML chuẩn (`activity_login.xml`):*
```xml
<!-- Ô nhập Email -->
<com.google.android.material.textfield.TextInputEditText
    android:id="@+id/etEmail"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Email của bạn"
    android:inputType="textEmailAddress" />

<!-- Nút bấm Đăng nhập -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btnLogin"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Đăng nhập" />
```

---

### Bước 2: Kích hoạt công nghệ ViewBinding trong build.gradle
Trước đây, Android dùng hàm `findViewById<Button>(R.id.btnLogin)` rất dễ gây lỗi crash app (`NullPointerException` nếu gõ sai ID, hoặc `ClassCastException` nếu ép sai kiểu).  
Dự án của bạn sử dụng **ViewBinding** - công nghệ hiện đại, tự động sinh mã an toàn kiểu (Type-Safe).

Kích hoạt trong file [android-app/app/build.gradle](file:///d:/Project/KHMT/DATN/android-app/app/build.gradle):
```groovy
android {
    ...
    buildFeatures {
        viewBinding true  // Bật ViewBinding
    }
}
```

#### Cơ chế tự sinh Class của ViewBinding:
Cứ mỗi file layout XML dạng `snake_case`, Android Studio sẽ tự động sinh ra một Class Binding dạng `PascalCase` thêm đuôi `Binding`:
* `activity_login.xml` $\rightarrow$ Tự sinh class **`ActivityLoginBinding`**
* `activity_main.xml` $\rightarrow$ Tự sinh class **`ActivityMainBinding`**
* `fragment_add_transaction.xml` $\rightarrow$ Tự sinh class **`FragmentAddTransactionBinding`**
* `item_transaction.xml` $\rightarrow$ Tự sinh class **`ItemTransactionBinding`**

---

### Bước 3: Khởi tạo ViewBinding trong Activity và Fragment

#### A. Trong Activity (Ví dụ: `LoginActivity.kt`)
Trong Activity, toàn bộ vòng đời gắn liền với màn hình nên chỉ cần khai báo `lateinit var`:
```kotlin
class LoginActivity : AppCompatActivity() {

    // 1. Khai báo biến binding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Nạp layout (inflate)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // 3. Đưa gốc layout (root) vào hiển thị
        setContentView(binding.root)

        // 4. Từ đây, gọi trực tiếp ID thông qua biến binding!
        binding.btnLogin.text = "Đăng nhập ngay"
    }
}
```

#### B. Trong Fragment (Ví dụ: `AddTransactionFragment.kt`)
Trong Fragment, View có thể bị hủy trong khi Fragment vẫn sống trong bộ nhớ (Backstack). Vì vậy, Google quy định kỹ thuật **sử dụng biến đệm `_binding` và dọn dẹp tại `onDestroyView()` để chống rò rỉ RAM (Memory Leak)**:
```kotlin
class AddTransactionFragment : DialogFragment() {

    // 1. Khai báo biến tạm có thể null
    private var _binding: FragmentAddTransactionBinding? = null
    // Getter không null an toàn
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View {
        // 2. Nạp layout
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 3. Gán sự kiện và hiển thị dữ liệu ở đây
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 4. Giải phóng bộ nhớ View khi màn hình đóng lại (RẤT QUAN TRỌNG)
        _binding = null
    }
}
```

---

### Bước 4: Bắt sự kiện người dùng (Click Listeners & Validation)

Sau khi có `binding`, bạn không cần gọi `findViewById`. Mọi ID trong XML đều biến thành thuộc tính của `binding`:

```kotlin
// 1. Bắt sự kiện click nút Đăng nhập
binding.btnLogin.setOnClickListener {
    handleLogin()
}

// 2. Bắt sự kiện chuyển sang màn hình Đăng ký
binding.tvRegisterLink.setOnClickListener {
    startActivity(Intent(this, RegisterActivity::class.java))
}

// 3. Hàm kiểm tra dữ liệu đầu vào (Validation) trước khi gọi API
private fun handleLogin() {
    // Lấy dữ liệu từ EditText và cắt bỏ khoảng trắng thừa
    val email = binding.etEmail.text.toString().trim()
    val password = binding.etPassword.text.toString().trim()

    // Kiểm tra rỗng
    if (email.isEmpty()) {
        binding.etEmail.error = "Vui lòng nhập Email"
        binding.etEmail.requestFocus()
        return
    }

    // Kiểm tra định dạng Email bằng Patterns của Android
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        binding.etEmail.error = "Email không đúng định dạng"
        binding.etEmail.requestFocus()
        return
    }

    // Kiểm tra độ dài mật khẩu
    if (password.length < 6) {
        binding.etPassword.error = "Mật khẩu phải từ 6 ký tự trở lên"
        binding.etPassword.requestFocus()
        return
    }

    // Khóa nút bấm để tránh người dùng nhấn liên tục (Spam click)
    binding.btnLogin.isEnabled = false
    
    // Đã hợp lệ -> Gửi sang ViewModel xử lý
    viewModel.signIn(email, password, authHelper)
}
```

---

### Bước 5: Kết nối ViewModel & Lắng nghe LiveData

Giao diện (View) không bao giờ trực tiếp gọi cơ sở dữ liệu hay mạng. Giao diện chỉ giao tiếp với **ViewModel** và lắng nghe dữ liệu phản hồi qua **LiveData**:

```kotlin
// 1. Khởi tạo ViewModel
viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

// 2. Lắng nghe trạng thái Loading (Vòng xoay tiến trình)
viewModel.isLoading.observe(this) { isLoading ->
    // Ẩn/Hiện ProgressBar tùy theo trạng thái
    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    // Khóa/Mở nút bấm
    binding.btnLogin.isEnabled = !isLoading
}

// 3. Lắng nghe kết quả khi đăng nhập thành công
viewModel.syncedUser.observe(this) { user ->
    if (user != null) {
        Toast.makeText(this, "Chào mừng ${user.fullName}!", Toast.LENGTH_SHORT).show()
        
        // Chuyển sang màn hình chính MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish() // Đóng LoginActivity
    }
}

// 4. Lắng nghe thông báo lỗi
viewModel.errorMessage.observe(this) { errorMsg ->
    if (!errorMsg.isNullOrEmpty()) {
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
    }
}
```

---

### Bước 6: Hiển thị danh sách động với RecyclerView & Adapter

Khi cần hiển thị danh sách các khoản giao dịch (như trên màn hình chính):
1. **Thiết kế layout cho từng dòng:** `item_transaction.xml` (gồm icon danh mục, tên danh mục, ngày, số tiền).
2. **Viết Adapter kết hợp ViewBinding:**
   ```kotlin
   class TransactionAdapter(
       private val list: List<Transaction>,
       private val onItemClick: (Transaction) -> Unit
   ) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

       // ViewHolder nắm giữ ItemTransactionBinding
       inner class ViewHolder(val binding: ItemTransactionBinding) : 
           RecyclerView.ViewHolder(binding.root)

       override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           val binding = ItemTransactionBinding.inflate(
               LayoutInflater.from(parent.context), parent, false
           )
           return ViewHolder(binding)
       }

       override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           val item = list[position]
           // Gán dữ liệu lên từng ID
           holder.binding.tvCategoryName.text = item.categoryName
           holder.binding.tvNote.text = item.note
           holder.binding.tvAmount.text = CurrencyFormatter.formatVND(item.amount)
           
           // Màu sắc theo loại giao dịch
           if (item.type == "EXPENSE") {
               holder.binding.tvAmount.setTextColor(Color.RED)
           } else {
               holder.binding.tvAmount.setTextColor(Color.GREEN)
           }

           holder.itemView.setOnClickListener { onItemClick(item) }
       }

       override fun getItemCount(): Int = list.size
   }
   ```
3. **Gán Adapter vào RecyclerView trong Activity/Fragment:**
   ```kotlin
   val adapter = TransactionAdapter(transactionList) { clickedItem ->
       // Thao tác khi nhấp vào 1 giao dịch
   }
   binding.rvTransactions.layoutManager = LinearLayoutManager(this)
   binding.rvTransactions.adapter = adapter
   ```

---

## 3. PHÂN TÍCH 2 MÀN HÌNH THỰC TẾ TRONG DỰ ÁN

### Màn hình 1: Đăng nhập (`activity_login.xml` ➔ `LoginActivity.kt`)
* **XML cấu hình:**
  * `ivLogo`: Logo app có hỗ trợ sự kiện ấn giữ (Long-click) để mở dialog đổi IP Server.
  * `etEmail`, `etPassword`: Ô nhập thông tin tài khoản.
  * `btnLogin`: Nút đăng nhập tài khoản thường.
  * `btnGoogleLogin`: Nút đăng nhập 1 chạm với Google.
  * `btnDemoLogin`: Nút đăng nhập nhanh chế độ Demo.
  * `progressBar`: Vòng xoay ẩn/hiện tự động khi chờ mạng.
* **Kotlin điều khiển:** `LoginActivity.kt` kết nối `ActivityLoginBinding`, gán sự kiện cho từng nút và dùng `AuthViewModel` để đồng bộ dữ liệu người dùng với Backend.

### Màn hình 2: Thêm giao dịch (`fragment_add_transaction.xml` ➔ `AddTransactionFragment.kt`)
* **XML cấu hình:**
  * `tabExpense`, `tabIncome`: Tab chọn loại giao dịch (Chi tiêu / Thu nhập).
  * `etAmount`: Bàn phím số tự format dấu phẩy hàng nghìn qua `CurrencyFormatter`.
  * `spinnerCategory`, `spinnerAccount`: Chọn danh mục (Ăn uống, Cafe...) và chọn Ví (Tiền mặt, Ngân hàng).
  * `tvDate`: Bấm vào mở hộp thoại lịch `DatePickerDialog`.
  * `btnSave`: Nút gửi giao dịch lên Backend.
* **Kotlin điều khiển:** `AddTransactionFragment.kt` sử dụng `FragmentAddTransactionBinding`, lắng nghe TextWatcher để tính toán ngân sách cảnh báo theo thời gian thực (nếu chi tiêu vượt 80% ngân sách thì cảnh báo đổi màu đỏ ngay trên giao diện).

---

## 4. CÁC LỖI LẬP TRÌNH GIAO DIỆN THƯỜNG GẶP VÀ CÁCH KHẮC PHỤC

| Lỗi thường gặp | Biểu hiện | Cách xử lý chuẩn |
| :--- | :--- | :--- |
| **NullPointerException** khi gọi `binding` | App crash ngay khi vừa mở màn hình hoặc sau khi đóng Fragment. | Trong Fragment, luôn đảm bảo chỉ truy cập `binding` sau `onCreateView` và trước `onDestroyView`. Đã giải quyết bằng cấu trúc `_binding` và `onDestroyView() { _binding = null }`. |
| **Giao diện không cập nhật** sau khi gọi API | Dữ liệu trên server đã đổi nhưng danh sách trên màn hình vẫn cũ. | Dùng `adapter.notifyDataSetChanged()` hoặc sử dụng `ListAdapter` với `DiffUtil` để tự động tính toán mục thay đổi. |
| **Đơ giao diện (ANR - App Not Responding)** | Màn hình bị đứng khi bấm nút xử lý AI hoặc gọi API. | Tuyệt đối không chạy tác vụ nặng trên Main Thread (UI Thread). Mọi cuộc gọi mạng và tính toán AI đều phải chạy trên `Dispatchers.IO` hoặc Coroutine ngầm. |
| **Mất dữ liệu khi xoay màn hình** | Form đang nhập dở thì xoay ngang máy bị xóa sạch chữ. | Lưu trữ dữ liệu trong `ViewModel` thay vì lưu trong biến cục bộ của Activity. |
