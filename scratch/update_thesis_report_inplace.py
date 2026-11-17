# -*- coding: utf-8 -*-
import docx
import os

DOCX_PATH = r"D:\Project\KHMT\DATN\BAO_CAO_DO_AN_TOT_NGHIEP.docx"

def update_thesis_docx():
    print("Opening:", DOCX_PATH)
    doc = docx.Document(DOCX_PATH)

    # 1. Replace text in paragraphs
    p_replacements = [
        ("86/86 Tests passed", "78/78 Tests passed"),
        ("86/86 Test cases passed", "78/78 Test cases passed"),
        ("86/86 Tests", "78/78 Tests"),
        ("86/86", "78/78"),
        ("86 ca kiểm thử", "78 ca kiểm thử"),
        ("86 bài kiểm thử", "78 bài kiểm thử"),
        ("86 bài", "78 bài"),
        ("86 test cases", "78 test cases"),
        ("86 Test cases", "78 Test cases"),
    ]

    p_count = 0
    for p in doc.paragraphs:
        for old, new in p_replacements:
            if old in p.text:
                # Replace in runs if possible, or in paragraph text
                p.text = p.text.replace(old, new)
                p_count += 1

    # 2. Replace text in tables
    t_count = 0
    test_suite_map = {
        "AccountControllerTest": "4",
        "AiScanControllerTest": "2",
        "AuthControllerTest": "3",
        "BudgetControllerTest": "6",
        "CategoryControllerTest": "4",
        "RecurringTransactionControllerTest": "5",
        "ReportControllerTest": "3",
        "TransactionControllerTest": "6",
        "UserControllerTest": "2",
        "AccountServiceTest": "4",
        "BudgetServiceTest": "10",
        "CategoryServiceTest": "8",
        "RecurringTransactionServiceTest": "9",
        "ReportServiceTest": "4",
        "TransactionImageServiceTest": "4",
        "TransactionServiceTest": "5",
        "UserServiceTest": "7"
    }

    for table in doc.tables:
        for row in table.rows:
            # Check if this row is part of the test summary table
            first_cell = row.cells[0].text.strip() if len(row.cells) > 0 else ""
            second_cell = row.cells[1].text.strip() if len(row.cells) > 1 else ""
            
            for class_name, test_count in test_suite_map.items():
                if class_name in first_cell or class_name in second_cell:
                    # Update the count cell (usually col 2 or 3)
                    for c_idx in range(len(row.cells)):
                        cell_txt = row.cells[c_idx].text.strip()
                        if cell_txt.isdigit() and int(cell_txt) in [2, 3, 4, 5, 6, 8, 9, 10]:
                            row.cells[c_idx].text = test_count
                            break

            for cell in row.cells:
                for old, new in p_replacements:
                    if old in cell.text:
                        cell.text = cell.text.replace(old, new)
                        t_count += 1
                        
            # Total row in test table
            row_txt = " ".join([c.text for c in row.cells])
            if "TỔNG CỘNG" in row_txt or "Tổng kết" in row_txt:
                for c in row.cells:
                    if "86" in c.text:
                        c.text = c.text.replace("86", "78")

    # 3. Add optimization items if not present in Chapter 3
    opt_found = False
    for p in doc.paragraphs:
        if "Chặn trùng lặp tên danh mục" in p.text:
            opt_found = True
            break
            
    if not opt_found:
        # Find where section 3.4.4 or 3.5 is
        target_idx = -1
        for idx, p in enumerate(doc.paragraphs):
            if "3.5. Kết luận chương 3" in p.text or "3.5. KẾT LUẬN CHƯƠNG 3" in p.text:
                target_idx = idx
                break
        if target_idx != -1:
            # Insert before section 3.5
            p1 = doc.paragraphs[target_idx].insert_paragraph_before(
                "9. Chặn trùng lặp tên danh mục thu chi (Category Name Uniqueness Validation): "
                "Bổ sung logic kiểm tra trùng lặp tên danh mục trong CategoryService.java (cả tạo mới và chỉnh sửa), "
                "so sánh equalsIgnoreCase với các danh mục hiện có của người dùng hoặc danh mục hệ thống mặc định, "
                "ném BadRequestException (HTTP 400) để ngăn chặn rác dữ liệu và mâu thuẫn nghiệp vụ."
            )
            p1.paragraph_format.space_after = docx.shared.Pt(4)
            p2 = doc.paragraphs[target_idx].insert_paragraph_before(
                "10. Tự động dọn dẹp file ảnh vật lý trên đĩa khi xóa giao dịch (Physical Asset Cleanup & Orphan File Prevention): "
                "Tích hợp phương thức kiểm tra và xóa tệp tĩnh vật lý (Files.deleteIfExists) trong TransactionService.java khi xóa giao dịch, "
                "giải quyết triệt để tình trạng file rác tồn đọng trong thư mục uploads/."
            )
            p2.paragraph_format.space_after = docx.shared.Pt(6)
            print("Added optimization items 9 and 10 to Chapter 3.")

    doc.save(DOCX_PATH)
    print(f"Successfully updated {DOCX_PATH}. Paragraph replacements: {p_count}, Table replacements: {t_count}")

if __name__ == "__main__":
    update_thesis_docx()
