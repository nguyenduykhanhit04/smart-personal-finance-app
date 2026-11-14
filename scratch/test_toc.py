# -*- coding: utf-8 -*-
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def make_toc_p(title, level, page_num, is_first=False):
    fld_begin_xml = """
    <w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
        <w:fldChar w:fldCharType="begin"/>
    </w:r>
    <w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
        <w:instrText xml:space="preserve"> TOC \\o &quot;1-3&quot; \\h \\z \\u </w:instrText>
    </w:r>
    <w:r xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
        <w:fldChar w:fldCharType="separate"/>
    </w:r>
    """ if is_first else ""
    
    b_xml = "<w:b/>" if level == 1 else ""
    sz_val = "24" if level == 1 else "22"
    
    # escape title for xml
    xml_title = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    
    p_xml = f"""
    <w:p {nsdecls('w')}>
        <w:pPr>
            <w:pStyle w:val="TOC{level}"/>
            <w:rPr>
                <w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>
                <w:sz w:val="{sz_val}"/>
            </w:rPr>
        </w:pPr>
        {fld_begin_xml}
        <w:r>
            <w:rPr>{b_xml}</w:rPr>
            <w:t>{xml_title}</w:t>
        </w:r>
        <w:r>
            <w:tab/>
        </w:r>
        <w:r>
            <w:rPr>{b_xml}</w:rPr>
            <w:t>{page_num}</w:t>
        </w:r>
    </w:p>
    """
    return parse_xml(p_xml)

p = make_toc_p("CHƯƠNG 1: CƠ SỞ LÝ THUYẾT", 1, "1", is_first=True)
print("Type:", type(p))
print("Tag:", p.tag)
