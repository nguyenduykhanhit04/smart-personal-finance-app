# -*- coding: utf-8 -*-
"""
Script to generate BAO_CAO_DO_AN_TOT_NGHIEP.docx using Doan Trang's template as base.
"""
import os
import shutil
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

SRC_TEMPLATE = r"C:\Users\ADMIN\Downloads\Báo cáo mẫu\ĐATN c Doãn Trang.docx"
DST_DOCX = r"d:\Project\KHMT\DATN\BAO_CAO_DO_AN_TOT_NGHIEP.docx"
DOWNLOADS_DST = r"C:\Users\ADMIN\Downloads\BAO_CAO_DO_AN_TOT_NGHIEP.docx"

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def format_run(run, font_name="Times New Roman", size_pt=13, bold=False, italic=False, color_rgb=None):
    run.font.name = font_name
    run.font.size = Pt(size_pt)
    run.bold = bold
    run.italic = italic
    if color_rgb:
        run.font.color.rgb = color_rgb

def add_p(doc, text="", style="Normal", align=WD_ALIGN_PARAGRAPH.JUSTIFY, space_before=0, space_after=6, line_spacing=1.3, bold=False, italic=False, font_size=13, font_color=None):
    p = doc.add_paragraph(style=style)
    p.alignment = align
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = line_spacing
    if text:
        r = p.add_run(text)
        format_run(r, font_name="Times New Roman", size_pt=font_size, bold=bold, italic=italic, color_rgb=font_color)
    return p

def add_h1(doc, text):
    p = doc.add_paragraph(style="Heading 1")
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(18)
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.line_spacing = 1.3
    r = p.add_run(text)
    format_run(r, font_name="Times New Roman", size_pt=16, bold=True, color_rgb=RGBColor(0, 32, 96))
    return p

def add_h2(doc, text):
    p = doc.add_paragraph(style="Heading 2")
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(14)
    p.paragraph_format.space_after = Pt(8)
    p.paragraph_format.line_spacing = 1.3
    r = p.add_run(text)
    format_run(r, font_name="Times New Roman", size_pt=14, bold=True, color_rgb=RGBColor(31, 78, 121))
    return p

def add_h3(doc, text):
    p = doc.add_paragraph(style="Heading 3")
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(10)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.3
    r = p.add_run(text)
    format_run(r, font_name="Times New Roman", size_pt=13, bold=True, italic=False)
    return p

def add_h4(doc, text):
    p = doc.add_paragraph(style="Heading 4")
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.3
    r = p.add_run(text)
    format_run(r, font_name="Times New Roman", size_pt=13, bold=False, italic=True)
    return p

def add_image_note(doc, note_text, caption_text):
    # Image placeholder box
    p_box = doc.add_paragraph(style="Normal")
    p_box.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_box.paragraph_format.space_before = Pt(8)
    p_box.paragraph_format.space_after = Pt(4)
    r_box = p_box.add_run(f"[CẦN BỔ SUNG HÌNH ẢNH: {note_text}]")
    format_run(r_box, font_name="Times New Roman", size_pt=11, bold=True, italic=True, color_rgb=RGBColor(192, 0, 0))
    
    # Caption
    p_cap = doc.add_paragraph(style="Caption")
    p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_cap.paragraph_format.space_before = Pt(2)
    p_cap.paragraph_format.space_after = Pt(12)
    r_cap = p_cap.add_run(caption_text)
    format_run(r_cap, font_name="Times New Roman", size_pt=12, bold=True, italic=True)
    return p_cap

def add_table_custom(doc, caption_text, headers, data, col_widths=None):
    # Caption above table
    p_cap = doc.add_paragraph(style="Caption")
    p_cap.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p_cap.paragraph_format.space_before = Pt(10)
    p_cap.paragraph_format.space_after = Pt(4)
    r_cap = p_cap.add_run(caption_text)
    format_run(r_cap, font_name="Times New Roman", size_pt=12, bold=True)
    
    table = doc.add_table(rows=len(data) + 1, cols=len(headers), style="Table Grid")
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    
    # Header row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], "D9E1F2")
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=150, right=150)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for r in p.runs:
            format_run(r, font_name="Times New Roman", size_pt=11, bold=True)
            
    # Data rows
    for r_idx, row_data in enumerate(data):
        row_cells = table.rows[r_idx + 1].cells
        bg = "F2F2F2" if r_idx % 2 == 1 else "FFFFFF"
        for c_idx, val in enumerate(row_data):
            row_cells[c_idx].text = str(val)
            set_cell_background(row_cells[c_idx], bg)
            set_cell_margins(row_cells[c_idx], top=80, bottom=80, left=120, right=120)
            p = row_cells[c_idx].paragraphs[0]
            # align center for short code / numbers, left for description
            if len(str(val)) <= 8 or c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            else:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for r in p.runs:
                format_run(r, font_name="Times New Roman", size_pt=10.5)
                
    # Column widths
    if col_widths and len(col_widths) == len(headers):
        for row in table.rows:
            for idx, width in enumerate(col_widths):
                row.cells[idx].width = Inches(width)
                
    # Space after table
    p_after = doc.add_paragraph(style="Normal")
    p_after.paragraph_format.space_before = Pt(0)
    p_after.paragraph_format.space_after = Pt(6)
    return table

print("Helper functions defined successfully.")
