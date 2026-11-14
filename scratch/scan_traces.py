# -*- coding: utf-8 -*-
import docx

doc = docx.Document(r'd:\Project\KHMT\DATN\BAO_CAO_DO_AN_TOT_NGHIEP.docx')

keywords = ['trang', 'doãn', 'doan', 'món ăn', 'mon an', 'calories', 'sức khỏe', 'suc khoe', 'bmr', 'tdee', 'efficientnet', 'asp.net', 'c#', '.net', 'mai vũ', 'quốc huy', '2155010259']

legit_exceptions = ['trang cá nhân', 'trang chủ', 'trang thiết bị', 'trang trí', 'số trang', 'ngắt trang', 'đầu trang', 'trang web', 'trang phục', 'trang trọng', 'tình trạng', 'trân trọng', 'trang bộ nhớ']

def is_clean(text, kw):
    if kw != 'trang':
        return False
    t_lower = text.lower()
    return any(legit in t_lower for legit in legit_exceptions)

print('=== SCANNING CORE PROPERTIES ===')
print('Author:', doc.core_properties.author)
print('Last Modified By:', doc.core_properties.last_modified_by)
print('Title:', doc.core_properties.title)
print('Subject:', doc.core_properties.subject)

print('\n=== SCANNING PARAGRAPHS ===')
for i, p in enumerate(doc.paragraphs):
    t_lower = p.text.lower()
    for kw in keywords:
        if kw in t_lower:
            if is_clean(p.text, kw):
                continue
            print(f'P{i:03d} (matched "{kw}"): {p.text[:100]}')

print('\n=== SCANNING TABLES ===')
for t_idx, t in enumerate(doc.tables):
    for r_idx, row in enumerate(t.rows):
        for c_idx, cell in enumerate(row.cells):
            t_lower = cell.text.lower()
            for kw in keywords:
                if kw in t_lower:
                    if is_clean(cell.text, kw):
                        continue
                    print(f'Table {t_idx} [{r_idx},{c_idx}] (matched "{kw}"): {cell.text[:80]}')

print('\n=== SCANNING HEADERS / FOOTERS ===')
for s_idx, sec in enumerate(doc.sections):
    for p in sec.header.paragraphs:
        for kw in keywords:
            if kw in p.text.lower():
                print(f'Sec {s_idx} Header: {p.text}')
    for p in sec.footer.paragraphs:
        for kw in keywords:
            if kw in p.text.lower():
                print(f'Sec {s_idx} Footer: {p.text}')

print('\n=== SCANNING SDT / TOC ===')
for child in doc._body._body:
    tag = child.tag.split('}')[-1]
    if tag == 'sdt':
        texts = [node.text for node in child.iter() if node.text]
        full_text = " ".join(texts)
        for kw in keywords:
            if kw in full_text.lower():
                for node in child.iter():
                    if node.text and kw in node.text.lower():
                        if is_clean(node.text, kw):
                            continue
                        print(f'TOC SDT text node matched "{kw}": {node.text.strip()}')
