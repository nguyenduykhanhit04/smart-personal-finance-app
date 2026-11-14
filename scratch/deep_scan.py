# -*- coding: utf-8 -*-
import zipfile
import re

kws = ['doãn', 'trang', '21cn', '2155010259', 'calories', 'bmr', 'tdee', 'efficientnet', 'món ăn', 'mai vũ', 'quốc huy']
legit = ['trang cá nhân', 'trang chủ', 'trang thiết bị', 'trang trí', 'số trang', 'ngắt trang', 'đầu trang', 'trang web', 'trang phục', 'trang trọng', 'tình trạng', 'trân trọng', 'trang bộ nhớ']

found = 0
with zipfile.ZipFile(r'd:\Project\KHMT\DATN\BAO_CAO_DO_AN_TOT_NGHIEP.docx', 'r') as z:
    for name in sorted(z.namelist()):
        if name.endswith('.xml') or name.endswith('.rels'):
            data = z.read(name).decode('utf-8', errors='ignore')
            for kw in kws:
                for m in re.finditer(re.escape(kw), data, re.IGNORECASE):
                    start = max(0, m.start() - 50)
                    end = min(len(data), m.end() + 50)
                    snippet = data[start:end].replace('\n', ' ')
                    if kw == 'trang' and any(l in snippet.lower() for l in legit):
                        continue
                    print(f'[{name}] match "{kw}": {snippet}')
                    found += 1

print(f'\nTotal suspicious matches found: {found}')
