# CƠ CHẾ LƯU TRỮ VÀ TẢI LÊN TỆP TIN (FILE STORAGE & UPLOADS)

**Hệ thống:** Personal Finance App  
**Truy vết mã nguồn:**  
- `backend/src/main/java/com/example/financebackend/controller/UserController.java:40-84` (Avatar Upload)  
- `backend/src/main/java/com/example/financebackend/service/TransactionImageService.java:32-65` (Bill Image Upload)  
- `backend/src/main/java/com/example/financebackend/config/SecurityConfig.java:27`  
- `backend/src/main/java/com/example/financebackend/config/WebConfig.java`  

---

## 1. Cấu trúc lưu trữ tệp trên đĩa

Tất cả các tập tin ảnh tải lên hệ thống được lưu trữ trong thư mục cục bộ `uploads/` tại thư mục gốc của Backend:

```text
backend/
├── uploads/
│   ├── avatar_1_8f9c2d1e-3a4b-4c5d-9e6f-7a8b9c0d1e2f.jpg    # Ảnh đại diện của User ID 1
│   ├── avatar_2_5e4d3c2b-1a0f-9e8d-7c6b-5a4b3c2d1e0f.png    # Ảnh đại diện của User ID 2
│   ├── 3f8a9c2b-4e1d-4a7b-8c9e-0f1a2b3c4d5e.jpg             # Ảnh hóa đơn đính kèm giao dịch
│   └── 7b2e1a0f-9c8d-4e5f-6a7b-8c9d0e1f2a3b.jpg             # Ảnh hóa đơn khác
```

---

## 2. Quy tắc đặt tên tệp và Dọn dẹp rác (Storage Rules)

1. **Đặt tên duy nhất (UUID Collision Prevention):**
   - Ảnh đại diện: `"avatar_" + userId + "_" + UUID.randomUUID().toString() + extension` (`UserController.java:59`).
   - Ảnh hóa đơn: `UUID.randomUUID().toString() + extension` (`TransactionImageService.java:51`).
2. **Cơ chế tự động dọn dẹp ảnh cũ (Garbage Cleanup):**
   - Khi người dùng tải lên ảnh đại diện mới, hệ thống kiểm tra `oldAvatarUrl`. Nếu là đường dẫn cục bộ `/uploads/...`, hệ thống gọi `Files.deleteIfExists` để xóa tệp cũ trên đĩa (`UserController.java:74-78`).
   - Khi cập nhật lại ảnh hóa đơn cho một giao dịch, tệp ảnh cũ cũng được xóa tương tự trước khi lưu ảnh mới (`TransactionImageService.java:66-71`).
3. **Phục vụ tệp tĩnh (Static Resource Serving):**
   - Đường dẫn URL trả về có dạng: `/uploads/<filename>`.
   - `SecurityConfig.java:27` mở quyền truy cập tĩnh `requestMatchers("/uploads/**").permitAll()` cho phép Mobile Client nạp ảnh trực tiếp qua Glide/Coil mà không bị chặn token.
