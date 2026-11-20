# CƠ CHẾ XỬ LÝ NGOẠI LỆ TOÀN CỤC (GLOBAL ERROR HANDLING)

**Hệ thống:** Personal Finance App  
**Truy vết mã nguồn:**  
- `backend/src/main/java/com/example/financebackend/exception/GlobalExceptionHandler.java:1-69`  
- `backend/src/main/java/com/example/financebackend/exception/BadRequestException.java:1-12`  
- `backend/src/main/java/com/example/financebackend/exception/ResourceNotFoundException.java:1-12`  
- `backend/src/main/java/com/example/financebackend/dto/response/ApiResponse.java:1-35`  

---

## 1. Chuẩn hóa Định dạng Phản hồi (`ApiResponse<T>`)

Mọi phản hồi từ REST API của hệ thống (dù thành công hay thất bại) đều được bao bọc trong đối tượng JSON đồng nhất:

```json
{
  "success": true,
  "message": "Transaction created successfully",
  "data": { ... }
}
```

Khi có lỗi xảy ra:
```json
{
  "success": false,
  "message": "Không thể tạo giao dịch cho ngày trong tương lai",
  "data": null
}
```

Trường hợp lỗi Validation `@Valid`:
```json
{
  "success": false,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "data": {
    "amount": "Số tiền giao dịch phải lớn hơn 0",
    "title": "Tiêu đề không được để trống"
  }
}
```

---

## 2. Các Handler ngoại lệ trong `GlobalExceptionHandler`

| Loại ngoại lệ | Handler Method | HTTP Status | Xử lý & Thông báo | File:dòng |
| :--- | :--- | :---: | :--- | :--- |
| `ResourceNotFoundException` | `handleResourceNotFound` | **404 Not Found** | Ghi log WARN, trả về `ApiResponse.error(e.getMessage())` | `GlobalExceptionHandler.java:21-25` |
| `BadRequestException` | `handleBadRequestException` | **400 Bad Request** | Ghi log WARN, trả về thông báo lỗi nghiệp vụ cụ thể | `GlobalExceptionHandler.java:27-31` |
| `IllegalArgumentException` | `handleIllegalArgument` | **400 Bad Request** | Bắt các lỗi tham số (ví dụ: File is empty) | `GlobalExceptionHandler.java:33-37` |
| `MethodArgumentNotValidException` | `handleValidationException` | **400 Bad Request** | Duyệt danh sách `FieldError`, map `[tên_trường: thông_báo]` vào `data` | `GlobalExceptionHandler.java:39-47` |
| `ClientAbortException` | `handleClientAbortException` | **Bỏ qua** | Ghi log WARN khi client đóng kết nối sớm / timeout; không ghi stack trace 500 | `GlobalExceptionHandler.java:49-52` |
| `Exception` (Mọi lỗi còn lại) | `handleGeneralException` | **500 Server Error** | Kiểm tra nếu bị bọc ClientAbortException thì bỏ qua; ngược lại ghi log ERROR và trả về thông báo lỗi chung | `GlobalExceptionHandler.java:54-68` |

---

## 3. Quy tắc ứng xử và Rollback Transaction

1. Mọi ngoại lệ kế thừa từ `RuntimeException` (như `BadRequestException`, `ResourceNotFoundException`) khi ném ra trong các phương thức có `@Transactional` sẽ kích hoạt **tự động Rollback** toàn bộ các thao tác CSDL phát sinh trước đó trong cùng phiên Transaction.
2. Các lỗi hủy kết nối từ phía Client (`ClientAbortException`) được phát hiện và bỏ qua âm thầm, tránh gây nhiễu log server và tránh lãng phí tài nguyên máy chủ.
