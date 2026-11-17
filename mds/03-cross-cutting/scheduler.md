# CƠ CHẾ LẬP LỊCH TÁC VỤ ĐỊNH KỲ (SPRING TASK SCHEDULING)

**Hệ thống:** Personal Finance App  
**Truy vết mã nguồn:**  
- `backend/src/main/java/com/example/financebackend/FinanceBackendApplication.java:7` (`@EnableScheduling`)  
- `backend/src/main/java/com/example/financebackend/service/RecurringTransactionService.java:119-195`  
- `backend/src/main/java/com/example/financebackend/util/AppTime.java:1-20`  

---

## 1. Cấu hình Lập lịch Hệ thống

Hệ thống kích hoạt tính năng lập lịch tác vụ nền của Spring Framework thông qua annotation `@EnableScheduling` tại lớp khởi chạy ứng dụng:

```java
// FinanceBackendApplication.java:7-8
@SpringBootApplication
@EnableScheduling
public class FinanceBackendApplication { ... }
```

---

## 2. Cấu hình Cron Job Tự động hóa Giao dịch Định kỳ

* **Biểu thức Cron:** `0 5 0 * * ?`
* **Múi giờ:** `zone = AppTime.TIME_ZONE` (`"Asia/Ho_Chi_Minh"`)
* **Thời điểm kích hoạt:** Đúng **00:05 AM (0 giờ 5 phút sáng)** mỗi ngày theo giờ Việt Nam (UTC+7).
* **Mục đích:** Đảm bảo toàn bộ các hóa đơn cố định, tiền thuê nhà, tiền lương đến hạn trong ngày sẽ tự động được ghi nhận ngay vào đầu ngày mà không cần người dùng phải mở ứng dụng.

---

## 3. Thuật toán Dời ngày an toàn (Safe Date Leap Calculation)

Hệ thống xử lý bài toán lịch phức tạp (tháng 2 có 28/29 ngày, tháng có 30 hoặc 31 ngày) bằng hàm `calculateMonthlyNextRunDate` (`RecurringTransactionService.java:182-187`):

```java
private LocalDate calculateMonthlyNextRunDate(LocalDate current, LocalDate startDate, int interval) {
    int targetDay = startDate.getDayOfMonth();
    YearMonth nextMonth = YearMonth.from(current).plusMonths(interval);
    int safeDay = Math.min(targetDay, nextMonth.lengthOfMonth());
    return nextMonth.atDay(safeDay);
}
```
* **Ví dụ thực tế:**
  - Lịch định kỳ tạo ngày **31/01/2026** chu kỳ 1 tháng/lần.
  - Kỳ tiếp theo vào tháng 2/2026 (chỉ có 28 ngày): `nextMonth.lengthOfMonth() = 28` -> `safeDay = Math.min(31, 28) = 28` -> Giao dịch chạy vào ngày **28/02/2026**, hoàn toàn không bị lỗi `DateTimeException`.
