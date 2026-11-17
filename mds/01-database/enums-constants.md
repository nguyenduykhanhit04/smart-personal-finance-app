# DANH MỤC TRẠNG THÁI, ENUMS VÀ HẰNG SỐ DÙNG CHUNG

**Hệ thống:** Personal Finance App  
**Truy vết mã nguồn:** `backend/src/main/resources/`, `database/seed_data.sql`, `backend/src/main/java/com/example/financebackend/`  

---

## 1. Trạng thái và Phân loại (Business Enums & Types)

### 1.1. Loại Giao dịch (`transaction_type`)
* **Truy vết:** `Transaction.java:37`, `RecurringTransaction.java:41`, `schema.sql:60,153`

| Giá trị | Tên hiển thị Tiếng Việt | Mô tả nghiệp vụ | Tác động số dư ví |
| :--- | :--- | :--- | :--- |
| `'expense'` | Chi tiêu | Các khoản tiền chi ra cho tiêu dùng, dịch vụ, mua sắm | `balance = balance - amount` |
| `'income'` | Thu nhập | Các khoản tiền nhận vào như lương, thưởng, lãi đầu tư | `balance = balance + amount` |

---

### 1.2. Loại Ví tài khoản (`account_type`)
* **Truy vết:** `Account.java:29`, `schema.sql:27`

| Giá trị | Tên hiển thị Tiếng Việt | Icon minh họa | Mô tả |
| :--- | :--- | :--- | :--- |
| `'cash'` | Tiền mặt | `ic_cash` | Tiền mặt trong ví, két sắt |
| `'bank'` | Tài khoản Ngân hàng | `ic_bank` | Tài khoản ngân hàng (Vietcombank, Techcombank,...) |
| `'e-wallet'` | Ví điện tử | `ic_wallet` | Ví điện tử (MoMo, ZaloPay, ShopeePay) |

---

### 1.3. Chu kỳ Lặp lại Giao dịch Định kỳ (`repeat_type`)
* **Truy vết:** `RecurringTransaction.java:44`, `schema.sql:155`, `RecurringTransactionService.java:166-180`

| Giá trị | Chu kỳ tính toán | Công thức tính `next_run_date` |
| :--- | :--- | :--- |
| `'daily'` | Hằng ngày | `current.plusDays(repeat_interval)` |
| `'weekly'` | Hằng tuần | `current.plusWeeks(repeat_interval)` |
| `'monthly'` | Hằng tháng | `YearMonth.from(current).plusMonths(interval).atDay(safeDay)` |
| `'yearly'` | Hằng năm | `YearMonth.from(current).plusYears(interval).atDay(safeDay)` |

---

### 1.4. Nhà cung cấp Xác thực (`auth_provider`)
* **Truy vết:** `User.java:43`, `schema.sql:18`, `UserService.java:38-51`

| Giá trị | Phương thức xác thực | Mô tả |
| :--- | :--- | :--- |
| `'firebase'` | Firebase Email/Password | Người dùng đăng ký bằng email và mật khẩu qua Firebase |
| `'google'` | Google OAuth2 Sign-In | Người dùng đăng nhập một chạm bằng tài khoản Google |
| `'demo'` | Tài khoản dùng thử nội bộ | Tài khoản ngoại tuyến thử nghiệm không cần kết nối mạng |

---

## 2. Dữ liệu Danh mục mặc định (11 Default Categories)
* **Truy vết:** `database/seed_data.sql:6-20`, `CategoryService.java:49-70`

| ID (Tự tăng) | Tên danh mục | Loại (`category_type`) | Icon Resource | Mã Màu Hex | Mặc định | Keyword AI |
| :---: | :--- | :--- | :--- | :--- | :---: | :--- |
| 1 | **Ăn uống** | `expense` | `ic_food` | `#FF9800` | TRUE | `food` |
| 2 | **Di chuyển** | `expense` | `ic_transport` | `#2196F3` | TRUE | `transport` |
| 3 | **Mua sắm** | `expense` | `ic_shopping` | `#E91E63` | TRUE | `shopping` |
| 4 | **Hóa đơn** | `expense` | `ic_bill` | `#9C27B0` | TRUE | `bills` |
| 5 | **Giải trí** | `expense` | `ic_entertainment`| `#673AB7` | TRUE | `entertainment` |
| 6 | **Sức khỏe** | `expense` | `ic_health` | `#4CAF50` | TRUE | `health` |
| 7 | **Khác (Chi)** | `expense` | `ic_other` | `#607D8B` | TRUE | `other` |
| 8 | **Lương** | `income` | `ic_salary` | `#4CAF50` | TRUE | - |
| 9 | **Thưởng** | `income` | `ic_bonus` | `#FFC107` | TRUE | - |
| 10 | **Đầu tư** | `income` | `ic_investment` | `#009688` | TRUE | - |
| 11 | **Khác (Thu)**| `income` | `ic_other` | `#607D8B` | TRUE | - |

---

## 3. Hằng số và Cấu hình Hệ thống (System Constants)

### 3.1. Cấu hình AI & Lập lịch
* **Múi giờ hệ thống:** `Asia/Ho_Chi_Minh` (`AppTime.java:8`)
* **Lập lịch chạy Cronjob định kỳ:** `0 5 0 * * ?` (chạy vào 00:05 AM hằng ngày - `RecurringTransactionService.java:119`)
* **Kích thước ảnh nạp vào YOLOv8n:** $640 	imes 640$ pixels
* **Số lớp nhận diện của mô hình YOLOv8n:** 18 lớp (`CLASS_NAMES` trong `ProductClassifierService.java:31-35`):
  `bottled_water`, `bread`, `clothes`, `coffee_cup`, `cosmetic`, `electronic_item`, `fastfood`, `helmet`, `medicine`, `milk_tea`, `motorbike`, `noodle`, `rice_meal`, `shoes`, `snack`, `soft_drink`, `taxi_car`, `toy_game`.
* **Kích thước vector đặc trưng Random Forest:** 27 đặc trưng (`float[1][27]` trong `ProductClassifierService.java:82-90`)
* **Ngưỡng tin cậy cảnh báo thấp (Low confidence threshold):** 0.50 (`ProductClassifierService.java:27`)
* **Thư mục lưu trữ tập tin tải lên:** `uploads/` (`UserController.java:48`, `TransactionImageService.java:39`)

---

### 3.2. Mã phản hồi chuẩn API (ApiResponse Status Codes)
* **Truy vết:** `ApiResponse.java`, `GlobalExceptionHandler.java:18-68`

| HTTP Status | Mã thành công / Lỗi | Tình huống áp dụng |
| :---: | :--- | :--- |
| **200 OK** | `success = true` | Xử lý yêu cầu thành công, trả về dữ liệu trong trường `data` |
| **400 Bad Request** | `success = false` | Sai tham số đầu vào, vi phạm `@Valid`, ngày giao dịch trong tương lai |
| **401 Unauthorized** | `success = false` | Thiếu hoặc sai Firebase JWT ID Token |
| **404 Not Found** | `success = false` | Không tìm thấy User, Account, Category, Transaction, Budget theo ID |
| **500 Server Error**| `success = false` | Ngoại lệ hệ thống chưa xử lý |
