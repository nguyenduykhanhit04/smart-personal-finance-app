# -*- coding: utf-8 -*-
"""
Main assembly execution script for Graduation Thesis Report.
"""
import os
import shutil
import docx

import sys
sys.path.append(r"d:\Project\KHMT\DATN\scratch")
from chapters_builder import (
    update_front_matter, build_chapter1, build_chapter2,
    build_chapter3, build_chapter4, build_appendix, build_references
)

SRC_TEMPLATE = r"C:\Users\ADMIN\Downloads\Báo cáo mẫu\ĐATN c Doãn Trang.docx"
DST_DOCX = r"d:\Project\KHMT\DATN\BAO_CAO_DO_AN_TOT_NGHIEP.docx"
DOWNLOADS_DST = r"C:\Users\ADMIN\Downloads\BAO_CAO_DO_AN_TOT_NGHIEP.docx"

def main():
    print(f"1. Copying template from {SRC_TEMPLATE} to {DST_DOCX}...")
    shutil.copy2(SRC_TEMPLATE, DST_DOCX)
    print(f"   Original template size: {os.path.getsize(DST_DOCX):,} bytes")
    
    print("2. Opening document...")
    doc = docx.Document(DST_DOCX)
    print(f"   Initial state: {len(doc.sections)} sections, {len(doc.paragraphs)} paragraphs, {len(doc.tables)} tables.")
    
    print("3. Updating Front Matter (Cover 1, Cover 2, Lời cảm ơn, Lời cam đoan, Lời mở đầu, Danh mục)...")
    update_front_matter(doc)
    
    print("4. Clearing old Section 2 body elements while preserving Section 2 properties (last_sectPr)...")
    body = doc._body._body
    last_sectPr = body[-1]
    
    # Locate start of Chapter 1 in body dynamically
    p_chap1 = None
    for p in doc.paragraphs:
        if "CHƯƠNG 1: CƠ SỞ LÝ THUYẾT" in p.text:
            p_chap1 = p
            break
    if p_chap1 is not None:
        start_idx = body.index(p_chap1._p)
    else:
        start_idx = body.index(doc.paragraphs[220]._p)
    print(f"   Found start of Chapter 1 at body index {start_idx} (out of {len(body)})")
    
    # Remove all elements from start_idx up to the element before last_sectPr
    remove_count = 0
    while body[-2] != body[start_idx - 1]:
        body.remove(body[start_idx])
        remove_count += 1
    print(f"   Removed {remove_count} old elements from Section 2.")
    
    print("5. Building Chapter 1: Cơ sở lý thuyết...")
    build_chapter1(doc)
    
    print("6. Building Chapter 2: Phân tích và thiết kế hệ thống...")
    build_chapter2(doc)
    
    print("7. Building Chapter 3: Cài đặt và thực nghiệm hệ thống...")
    build_chapter3(doc)
    
    print("8. Building Chapter 4: Kết luận và hướng phát triển...")
    build_chapter4(doc)
    
    print("9. Building Appendix (Phụ lục)...")
    build_appendix(doc)
    
    print("10. Building References (Tài liệu tham khảo)...")
    build_references(doc)
    
    print("11. Saving BAO_CAO_DO_AN_TOT_NGHIEP.docx...")
    doc.save(DST_DOCX)
    print(f"   Saved to {DST_DOCX}. File size: {os.path.getsize(DST_DOCX):,} bytes.")
    
    print("12. Creating duplicate copy in Downloads for easy access...")
    try:
        shutil.copy2(DST_DOCX, DOWNLOADS_DST)
        print(f"   Saved copy to {DOWNLOADS_DST}.")
    except Exception as e:
        print(f"   Note: Could not copy to Downloads: {e}")
        
    print("13. Verification check on generated document...")
    v_doc = docx.Document(DST_DOCX)
    print(f"   Verification passed! Total sections: {len(v_doc.sections)}, Paragraphs: {len(v_doc.paragraphs)}, Tables: {len(v_doc.tables)}.")
    for i, sec in enumerate(v_doc.sections):
        print(f"     Section {i}: Top={sec.top_margin.cm:.1f}cm, Bottom={sec.bottom_margin.cm:.1f}cm, Left={sec.left_margin.cm:.1f}cm, Right={sec.right_margin.cm:.1f}cm")

if __name__ == "__main__":
    main()
