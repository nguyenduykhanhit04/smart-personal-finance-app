# MA TRẬN TRUY VẾT HỆ THỐNG (TRACEABILITY MATRIX)

**Hệ thống:** Personal Finance App  
**Tác giả:** Senior Business Analyst & Software Architect  

---

## 1. Ma trận: Chức năng × Bảng Cơ sở dữ liệu (CRUD Matrix)

| Mã CN | Tên Chức năng | `users` | `accounts` | `categories` | `transactions` | `budgets` | `recurring_transactions` | `transaction_images` | `ai_scan_logs` | `ai_product_logs` |
| :---: | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **F01** | Đăng nhập Firebase & Sync User | **C / R / U** | **C / R** | - | - | - | - | - | - | - |
| **F02** | Đăng nhập Demo | **C / R** | **C / R** | - | - | - | - | - | - | - |
| **F03** | Quản lý Hồ sơ cá nhân | **R / U** | - | - | - | - | - | - | - | - |
| **F04** | Cập nhật Avatar người dùng | **U** | - | - | - | - | - | - | - | - |
| **F05** | Quản lý Ví tài chính | **R** | **C / R / U / D** | - | **R** (FK check) | - | **R** (FK check) | - | - | - |
| **F06** | Quản lý Danh mục thu chi | **R** | - | **C / R / U / D** | **R** (FK check) | - | - | - | - | - |
| **F07** | Ghi chép & Quản lý giao dịch | **R** | **R / U** | **R** | **C / R / U / D** | - | - | **R** | - | - |
| **F08** | Đính kèm ảnh hóa đơn giao dịch | - | - | - | **R** | - | - | **C / R / U** | - | - |
| **F09** | Thiết lập & Giám sát ngân sách | **R** | - | **R** | **R** (spent calc) | **C / R / U / D** | - | - | - | - |
| **F10** | Quản lý Giao dịch định kỳ | **R** | **R** | **R** | - | - | **C / R / U / D** | - | - | - |
| **F11** | Tự động thực thi GD định kỳ | - | **U** | - | **C** | - | **R / U** | - | - | - |
| **F12** | Báo cáo & Thống kê tài chính | - | - | **R** | **R** | - | - | - | - | - |
| **F13** | Quét bóc tách hóa đơn OCR | **R** | - | **R** | - | - | - | - | **C** | - |
| **F14** | Phản hồi xác nhận hóa đơn AI | - | - | **R** | **R** | - | - | - | **R / U** | - |
| **F15** | Nhận diện đồ vật YOLO & RF | **R** | - | **R** | - | - | - | - | - | **C** |
| **F16** | Phản hồi nhận diện đồ vật AI | - | - | - | **R** | - | - | - | - | **R / U** |

*Ghi chú:* **C** = Create (Tạo mới), **R** = Read (Truy vấn), **U** = Update (Cập nhật), **D** = Delete (Xóa).

---

## 2. Ma trận: Chức năng × API Endpoints

| Mã CN | HTTP Method | Endpoint URL | Controller Handler | File mã nguồn : Dòng |
| :---: | :---: | :--- | :--- | :--- |
| **F01** | `POST` | `/api/auth/firebase-login` | `AuthController.firebaseLogin` | `AuthController.java:24-42` |
| **F02** | `POST` | `/api/auth/demo-login` | `AuthController.demoLogin` | `AuthController.java:45-49` |
| **F03** | `GET` | `/api/users/{id}` | `UserController.getUserById` | `UserController.java:28-32` |
| **F03** | `PUT` | `/api/users/{id}` | `UserController.updateUser` | `UserController.java:34-38` |
| **F04** | `POST` | `/api/users/{id}/avatar` | `UserController.uploadAvatar` | `UserController.java:40-84` |
| **F05** | `GET` | `/api/accounts` | `AccountController.getAccountsByUserId` | `AccountController.java:22-26` |
| **F05** | `POST` | `/api/accounts` | `AccountController.createAccount` | `AccountController.java:28-32` |
| **F05** | `PUT` | `/api/accounts/{id}` | `AccountController.updateAccount` | `AccountController.java:34-38` |
| **F05** | `DELETE` | `/api/accounts/{id}` | `AccountController.deleteAccount` | `AccountController.java:40-44` |
| **F06** | `GET` | `/api/categories` | `CategoryController.getCategoriesByUserId` | `CategoryController.java:22-26` |
| **F06** | `POST` | `/api/categories` | `CategoryController.createCategory` | `CategoryController.java:28-32` |
| **F06** | `PUT` | `/api/categories/{id}` | `CategoryController.updateCategory` | `CategoryController.java:34-38` |
| **F06** | `DELETE` | `/api/categories/{id}` | `CategoryController.deleteCategory` | `CategoryController.java:40-44` |
| **F07** | `GET` | `/api/transactions` | `TransactionController.getTransactions` | `TransactionController.java:24-33` |
| **F07** | `GET` | `/api/transactions/{id}` | `TransactionController.getTransactionById` | `TransactionController.java:35-39` |
| **F07** | `POST` | `/api/transactions` | `TransactionController.createTransaction` | `TransactionController.java:41-45` |
| **F07** | `PUT` | `/api/transactions/{id}` | `TransactionController.updateTransaction` | `TransactionController.java:47-51` |
| **F07** | `DELETE` | `/api/transactions/{id}` | `TransactionController.deleteTransaction` | `TransactionController.java:53-56` |
| **F08** | `POST` | `/api/transaction-images/upload` | `TransactionImageController.uploadImage` | `TransactionImageController.java:21-25` |
| **F09** | `GET` | `/api/budgets` | `BudgetController.getBudgets` | `BudgetController.java:24-32` |
| **F09** | `GET` | `/api/budgets/{id}` | `BudgetController.getBudgetById` | `BudgetController.java:34-38` |
| **F09** | `POST` | `/api/budgets` | `BudgetController.createBudget` | `BudgetController.java:40-44` |
| **F09** | `PUT` | `/api/budgets/{id}` | `BudgetController.updateBudget` | `BudgetController.java:46-50` |
| **F09** | `DELETE` | `/api/budgets/{id}` | `BudgetController.deleteBudget` | `BudgetController.java:52-56` |
| **F10** | `GET` | `/api/recurring-transactions` | `RecurringTransactionController.getRecurringTransactions` | `RecurringTransactionController.java:22-26` |
| **F10** | `POST` | `/api/recurring-transactions` | `RecurringTransactionController.createRecurringTransaction` | `RecurringTransactionController.java:28-32` |
| **F10** | `PUT` | `/api/recurring-transactions/{id}` | `RecurringTransactionController.updateRecurringTransaction` | `RecurringTransactionController.java:34-38` |
| **F10** | `DELETE` | `/api/recurring-transactions/{id}` | `RecurringTransactionController.deleteRecurringTransaction` | `RecurringTransactionController.java:40-44` |
| **F11** | `@Scheduled` | `0 5 0 * * ?` | `RecurringTransactionService.processRecurringTransactions` | `RecurringTransactionService.java:121-164` |
| **F11** | `POST` | `/api/recurring-transactions/trigger` | `RecurringTransactionController.triggerRecurringTransactions` | `RecurringTransactionController.java:47-51` |
| **F12** | `GET` | `/api/reports/daily` | `ReportController.getDailyReport` | `ReportController.java:22-28` |
| **F12** | `GET` | `/api/reports/monthly` | `ReportController.getMonthlyReport` | `ReportController.java:30-37` |
| **F12** | `GET` | `/api/reports/by-category` | `ReportController.getByCategory` | `ReportController.java:39-45` |
| **F13** | `POST` | `/api/ai-scan/classify` | `AiScanController.classifyBill` | `AiScanController.java:49-116` |
| **F14** | `POST` | `/api/ai-scan/feedback` | `AiScanController.saveFeedback` | `AiScanController.java:118-126` |
| **F15** | `POST` | `/api/ai-product/classify` | `AiProductController.classifyProduct` | `AiProductController.java:41-68` |
| **F16** | `POST` | `/api/ai-product/feedback` | `AiProductController.saveFeedback` | `AiProductController.java:70-75` |
