# -*- coding: utf-8 -*-
"""
Builder functions for each chapter and section of the thesis report.
"""
import docx
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
import xml.etree.ElementTree as ET

import sys
sys.path.append(r"d:\Project\KHMT\DATN\scratch")
from content_data import (
    ABBREVIATIONS, TABLES_LIST, FIGURES_LIST, TOC_ENTRIES,
    DB_TABLES_DATA, TEST_CASES_SUMMARY, EVALUATION_TABLE, REFERENCES_DATA
)
from generate_full_report import (
    add_p, add_bullet, add_h1, add_h2, add_h3, add_h4,
    add_image_note, add_table_custom, format_run
)

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

def rebuild_toc_sdt(doc):
    for child in doc._body._body:
        tag = child.tag.split('}')[-1]
        if tag == 'sdt':
            sdt_content = child.find('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}sdtContent')
            if sdt_content is not None:
                sdt_content.clear()
                
                # TOCHeading
                p_head = parse_xml(f"""
                <w:p {nsdecls('w')}>
                    <w:pPr>
                        <w:pStyle w:val="TOCHeading"/>
                        <w:spacing w:before="0" w:after="120" w:line="240" w:lineRule="auto"/>
                        <w:rPr><w:rStyle w:val="Heading1Char"/></w:rPr>
                    </w:pPr>
                </w:p>
                """)
                sdt_content.append(p_head)
                
                for idx, (title, level, page_num) in enumerate(TOC_ENTRIES):
                    p_entry = make_toc_p(title, level, page_num, is_first=(idx == 0))
                    sdt_content.append(p_entry)
                    
                p_end = parse_xml(f"""
                <w:p {nsdecls('w')}>
                    <w:r>
                        <w:rPr><w:b/><w:bCs/><w:noProof/></w:rPr>
                        <w:fldChar w:fldCharType="end"/>
                    </w:r>
                </w:p>
                """)
                sdt_content.append(p_end)
                print(f"Rebuilt TOC SDT with {len(TOC_ENTRIES)} clean entries.")
            break

def update_front_matter(doc):
    """Updates Cover 1, Cover 2, Lời Cảm ơn, Lời cam đoan, Lời mở đầu, Danh mục."""
    paras = doc.paragraphs
    
    # 1. Bìa 1 (Cover 1)
    paras[10].text = "KHÓA: 2022 - 2027"
    for r in paras[10].runs: format_run(r, font_name="Times New Roman", size_pt=13, bold=True)
    paras[14].text = "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID"
    for r in paras[14].runs: format_run(r, font_name="Times New Roman", size_pt=15, bold=True, color_rgb=RGBColor(0, 32, 96))
    paras[15].text = "TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"
    for r in paras[15].runs: format_run(r, font_name="Times New Roman", size_pt=15, bold=True, color_rgb=RGBColor(0, 32, 96))
    
    t0 = doc.tables[0]
    t0.rows[0].cells[1].text = "NGUYỄN DUY KHÁNH"
    t0.rows[1].cells[1].text = "22CN4"
    for row in t0.rows:
        for cell in row.cells:
            for p in cell.paragraphs:
                for r in p.runs: format_run(r, font_name="Times New Roman", size_pt=12, bold=True)
                
    paras[23].text = "HÀ NỘI - 2027"
    for r in paras[23].runs: format_run(r, font_name="Times New Roman", size_pt=12, bold=True)
    
    # 2. Bìa 2 (Cover 2)
    paras[34].text = "KHÓA: 2022 - 2027"
    for r in paras[34].runs: format_run(r, font_name="Times New Roman", size_pt=13, bold=True)
    paras[38].text = "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID"
    for r in paras[38].runs: format_run(r, font_name="Times New Roman", size_pt=15, bold=True, color_rgb=RGBColor(0, 32, 96))
    paras[39].text = "TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"
    for r in paras[39].runs: format_run(r, font_name="Times New Roman", size_pt=15, bold=True, color_rgb=RGBColor(0, 32, 96))
    
    t1 = doc.tables[1]
    t1.rows[0].cells[1].text = "TS. NGUYỄN MẠNH THẮNG"
    t1.rows[1].cells[1].text = "NGUYỄN DUY KHÁNH"
    t1.rows[2].cells[1].text = "22CN4"
    for row in t1.rows:
        for cell in row.cells:
            for p in cell.paragraphs:
                for r in p.runs: format_run(r, font_name="Times New Roman", size_pt=12, bold=True)
                
    paras[45].text = "HÀ NỘI - 2027"
    for r in paras[45].runs: format_run(r, font_name="Times New Roman", size_pt=12, bold=True)
    
    # 3. Lời Cảm ơn (P46 to P52)
    paras[46].text = "Lời Cảm ơn"
    paras[47].text = "Trong suốt quá trình học tập, rèn luyện và thực hiện đồ án tốt nghiệp tại Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội, em đã nhận được sự quan tâm sâu sắc, chỉ bảo tận tình từ quý thầy cô, sự động viên to lớn từ gia đình cùng sự đồng hành, hỗ trợ của bạn bè. Với lòng biết ơn chân thành nhất, em xin được gửi lời cảm ơn sâu sắc tới tất cả những cá nhân và tập thể đã luôn sát cánh, giúp đỡ em trong suốt chặng đường vừa qua."
    paras[48].text = "Trước hết, em xin bày tỏ lòng biết ơn sâu sắc và kính trọng nhất tới TS. Nguyễn Mạnh Thắng – người thầy đã trực tiếp định hướng, tận tâm hướng dẫn và truyền đạt cho em những kinh nghiệm học thuật cùng tư duy phân tích hệ thống vô cùng quý báu. Những định hướng chiến lược và sự đóng góp chuyên môn chuẩn xác của thầy không chỉ là kim chỉ nam giúp em hoàn thiện xuất sắc đề tài mà còn là nền tảng vững vàng cho sự nghiệp kỹ sư sau này."
    paras[49].text = "Em cũng xin trân trọng gửi lời cảm ơn tới toàn thể các thầy cô giáo trong Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội. Những kiến thức chuyên sâu và tâm huyết mà các thầy cô truyền đạt trong suốt 5 năm học đại học đã trang bị cho em nền tảng tư duy công nghệ vững chắc để tự tin đảm nhận và hoàn thành đồ án tốt nghiệp với đề tài: \"Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\"."
    paras[50].text = "Đồng thời, em xin gửi lời cảm ơn chân thành đến Ban Giám hiệu Nhà trường, Ban Chủ nhiệm Khoa Công nghệ Thông tin cùng các phòng ban chức năng đã luôn tạo điều kiện thuận lợi nhất về cơ sở vật chất, môi trường học tập và các thủ tục hành chính trong suốt quá trình em học tập và nghiên cứu tại trường."
    paras[51].text = "Cuối cùng, dù bản thân đã dành toàn bộ tâm huyết và nỗ lực tối đa để hoàn thiện đồ án một cách chỉn chu nhất, song do giới hạn về mặt thời gian và kinh nghiệm thực tế, đồ án chắc chắn khó tránh khỏi những thiếu sót nhất định. Em rất mong nhận được những nhận xét, góp ý quý báu từ quý thầy cô trong Hội đồng chấm thi để đề tài được hoàn thiện tốt hơn."
    paras[52].text = "Em xin chân thành cảm ơn!"
    for idx in range(46, 53):
        p = paras[idx]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER if idx in [46, 52] else WD_ALIGN_PARAGRAPH.JUSTIFY
        for r in p.runs: format_run(r, font_name="Times New Roman", size_pt=13, bold=(idx == 46), italic=(idx == 52))

    # 4. Lời cam đoan của tác giả (P53 to P58)
    paras[53].text = "Lời cam đoan của tác giả"
    paras[54].text = "Em xin cam đoan rằng bản đồ án tốt nghiệp với đề tài: \"Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\" là công trình nghiên cứu và phát triển phần mềm độc lập, nghiêm túc do chính bản thân em trực tiếp thực hiện dưới sự định hướng và hướng dẫn khoa học của TS. Nguyễn Mạnh Thắng."
    paras[55].text = "Trong quá trình xây dựng hệ thống và biên soạn báo cáo, em đã tìm hiểu, nghiên cứu và tham khảo các tài liệu chuyên ngành, giáo trình, bài báo khoa học trong và ngoài nước cùng các tài liệu kỹ thuật chính thống từ các tổ chức công nghệ uy tín. Toàn bộ các trích dẫn, bảng biểu, số liệu và mã nguồn tham khảo đều đã được ghi rõ nguồn gốc xuất xứ tại danh mục Tài liệu tham khảo nhằm đảm bảo tính trung thực học thuật cao nhất."
    paras[56].text = "Em xin khẳng định toàn bộ các kết quả thực nghiệm, số liệu đánh giá mô hình AI, kết quả kiểm thử hệ thống và giao diện phần mềm được trình bày trong báo cáo này là hoàn toàn trung thực, phản ánh chính xác sản phẩm do em tự tay thiết kế, cài đặt và kiểm thử, không sao chép bất hợp pháp từ bất kỳ đồ án hoặc công trình nghiên cứu nào khác. Nếu phát hiện bất kỳ sự vi phạm nào về tính liêm chính học thuật, em xin chịu hoàn toàn mọi hình thức kỷ luật theo quy định của Nhà trường và pháp luật hiện hành."
    paras[57].text = "Em xin chân thành cảm ơn!"
    for idx in range(53, 58):
        p = paras[idx]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER if idx in [53, 57] else WD_ALIGN_PARAGRAPH.JUSTIFY
        for r in p.runs: format_run(r, font_name="Times New Roman", size_pt=13, bold=(idx == 53), italic=(idx == 57))
        
    t2 = doc.tables[2]
    sig_cell = t2.rows[0].cells[1]
    sig_cell.text = "Hà Nội, ngày …. tháng …. năm 2027\nSINH VIÊN THỰC HIỆN ĐỒ ÁN\n(Ký và ghi rõ họ tên)\n\n\n\nNGUYỄN DUY KHÁNH"
    for p in sig_cell.paragraphs:
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for r in p.runs: format_run(r, font_name="Times New Roman", size_pt=12, bold=True)

    # 5. Lời mở đầu (P59 to P102)
    paras[59].text = "Lời mở đầu"
    paras[60].text = "Lý do chọn đề tài"
    paras[61].text = "Trong bối cảnh nền kinh tế hiện đại và xu thế chuyển đổi số diễn ra mạnh mẽ, việc quản lý tài chính cá nhân đóng vai trò then chốt quyết định sự ổn định và phát triển bền vững của mỗi cá nhân, gia đình và toàn xã hội. Nhận thức được tầm quan trọng này, nhu cầu theo dõi thu chi, kiểm soát ngân sách và hoạch định tài chính tương lai ngày càng trở nên cấp thiết, đặc biệt đối với giới trẻ, sinh viên và người đi làm."
    paras[62].text = "Tuy nhiên, trong thực tế, rào cản lớn nhất khiến phần lớn người dùng từ bỏ thói quen ghi chép tài chính chỉ sau một thời gian ngắn chính là sự phiền toái, mất thời gian và dễ nhầm lẫn của việc nhập liệu thủ công từng con số, từng danh mục sau mỗi lần mua sắm. Các ứng dụng quản lý tài chính truyền thống hiện nay trên thị trường chủ yếu cung cấp giao diện nhập số liệu bằng bàn phím thông thường, đòi hỏi sự kiên nhẫn cao và không bắt kịp nhịp sống bận rộn."
    paras[63].text = "Sự phát triển vượt bậc của Trí tuệ Nhân tạo (Artificial Intelligence - AI), Thị giác Máy tính (Computer Vision) và Điện toán biên (Edge Computing) trên thiết bị di động đã mở ra những giải pháp đột phá. Thay vì gõ phím thủ công, người dùng có thể chỉ cần chĩa camera điện thoại vào các sản phẩm tiêu dùng hoặc chụp ảnh hóa đơn mua hàng. Hệ thống sẽ tự động nhận diện vật thể theo thời gian thực, bóc tách chính xác số tiền, ngày giao dịch và gợi ý danh mục chi tiêu tương ứng."
    paras[64].text = "Xuất phát từ nhu cầu thực tiễn cấp thiết đó, em đã lựa chọn đề tài tốt nghiệp kỹ sư: \"Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\". Đề tài nhằm mục tiêu giải quyết triệt để sự cồng kềnh trong thao tác ghi chép, mang lại trải nghiệm tài chính thông minh, trực quan và tiện ích cho người sử dụng."
    
    paras[65].text = "Khảo sát thực tế"
    paras[66].text = "Qua quá trình khảo sát các ứng dụng quản lý tài chính cá nhân phổ biến trên các chợ ứng dụng Google Play Store (như Money Lover, Sổ Thu Chi MISA, Spendee, Fast Budget), em nhận thấy các ưu điểm nổi bật như giao diện thân thiện, hỗ trợ nhiều tài khoản ví và biểu đồ thống kê cơ bản."
    paras[67].text = "Tuy nhiên, các ứng dụng này còn tồn tại những hạn chế cốt lõi: Hơn 90% thao tác ghi chép vẫn phụ thuộc hoàn toàn vào bàn phím ảo, gây cảm giác nhàm chán và tốn nhiều thời gian của người dùng; Tính năng quét hóa đơn (nếu có) thường là dịch vụ tính phí định kỳ rất đắt đỏ, phụ thuộc hoàn toàn vào kết nối đám mây của bên thứ ba và thường xuyên nhận diện sai lệch đối với các mẫu hóa đơn in nhiệt đặc thù tại Việt Nam; Hoàn toàn chưa có ứng dụng nào tích hợp thị giác máy tính On-Device để nhận diện các mặt hàng tiêu dùng trực tiếp qua camera theo thời gian thực và tự động phân loại danh mục chi tiêu thông qua mô hình học máy nhúng."
    
    paras[68].text = "Mục đích nghiên cứu"
    paras[69].text = "Mục đích nghiên cứu của đề tài là xây dựng hoàn chỉnh một hệ sinh thái quản lý tài chính cá nhân thông minh kết hợp đa nền tảng, bao gồm ứng dụng di động Android Native và hệ thống máy chủ Backend RESTful API."
    paras[70].text = "1. Giải quyết bài toán tự động hóa ghi chép tài chính thông qua việc tích hợp sâu các mô hình AI/Computer Vision hiện đại (YOLOv8 On-Device, Random Forest ONNX Runtime Java, Google ML Kit OCR và Google Gemini 2.5 Flash LLM)."
    paras[71].text = "2. Cung cấp đầy đủ các nghiệp vụ quản lý tài chính cá nhân chuyên nghiệp: quản lý đa ví tài chính, giao dịch thu chi cộng/trừ số dư tự động, hạn mức ngân sách cảnh báo đổi màu trực quan, giao dịch định kỳ tự động và biểu đồ báo cáo đa chiều theo tiêu chuẩn Obsidian Dark Theme cao cấp."
    
    paras[72].text = "Nhiệm vụ nghiên cứu"
    paras[73].text = "• Nghiên cứu cơ sở lý thuyết về thị giác máy tính, kiến trúc mô hình YOLOv8, thuật toán học máy Random Forest, công nghệ nhúng ONNX Runtime, kỹ thuật OCR và mô hình ngôn ngữ lớn (LLM)."
    paras[74].text = "• Phân tích yêu cầu nghiệp vụ, thiết kế kiến trúc hệ thống 3 tầng phân tán, thiết kế cơ sở dữ liệu quan hệ chuẩn 3NF và luồng tích hợp AI."
    paras[75].text = "• Thiết lập môi trường phát triển chuẩn mực (Android Studio, IntelliJ IDEA, Docker Compose, MySQL 8.0) và thiết lập cơ chế bảo mật xác thực Stateless qua Firebase Token."
    paras[76].text = "• Lập trình hoàn thiện toàn bộ các phân hệ nghiệp vụ tài chính trên ứng dụng Android Native (Kotlin) và máy chủ RESTful API (Spring Boot 3, Java 21)."
    paras[77].text = "• Thu thập dữ liệu, huấn luyện và chuyển đổi tối ưu hóa các mô hình AI; kiểm thử toàn diện hệ thống với 86 bài kiểm thử tự động đạt độ chính xác 100% và hoàn thiện báo cáo khoa học."
    
    paras[78].text = "Đối tượng nghiên cứu"
    paras[79].text = "• Quy trình nghiệp vụ quản lý thu chi, ngân sách và dòng tiền của người dùng cá nhân."
    paras[80].text = "• Ảnh chụp hóa đơn bán lẻ thực tế (in nhiệt, in kim, hóa đơn siêu thị, quán cà phê, xăng dầu)."
    paras[81].text = "• Ảnh chụp các mặt hàng tiêu dùng thường ngày qua camera di động (18 lớp vật thể mua sắm phổ biến tại Việt Nam)."
    paras[82].text = "• Kiến trúc các mô hình học sâu và học máy (YOLOv8n TFLite, Random Forest ONNX, Google ML Kit OCR, Gemini 2.5 Flash) cùng kiến trúc phân tầng Android MVVM và Spring Boot 3."
    
    paras[83].text = "Phạm vi nghiên cứu"
    paras[84].text = "• Phạm vi chức năng: Tập trung vào các nghiệp vụ tài chính cá nhân cốt lõi (thu chi, ví, danh mục, ngân sách, định kỳ, báo cáo biểu đồ) và tự động hóa nhập liệu bằng AI. Phạm vi thị giác máy tính tập trung nhận diện 18 lớp mặt hàng mua sắm phổ biến tại Việt Nam."
    paras[85].text = "• Phạm vi thiết bị: Ứng dụng di động triển khai trên hệ điều hành Android (Target SDK 34, Min SDK 24); máy chủ Backend xây dựng trên Spring Boot 3 (Java 21) và MySQL 8.0 được đóng gói trọn gói bằng Docker Compose."
    
    paras[86].text = "Phương pháp nghiên cứu"
    paras[87].text = "• Phương pháp nghiên cứu tài liệu: Thu thập và phân tích các tài liệu học thuật, đặc tả kỹ thuật chính thức từ Google Developers, Ultralytics, Microsoft, Oracle và VMware."
    paras[88].text = "• Phương pháp thực nghiệm khoa học dữ liệu: Thu thập tập dữ liệu ảnh thực tế, gán nhãn, huấn luyện mô hình, đánh giá độ chính xác qua các chỉ số mAP@0.5, Precision, Recall và F1-Score; chuyển đổi sang định dạng TFLite và ONNX để đo lường độ trễ suy luận."
    paras[89].text = "• Phương pháp kỹ thuật phần mềm: Áp dụng mô hình Agile/Scrum, thiết kế hướng module (Clean Architecture), kiểm thử tự động toàn diện theo chuẩn Unit & Integration Testing."
    
    paras[90].text = "Dự kiến kết quả đạt được"
    paras[91].text = "• Bản báo cáo đồ án tốt nghiệp hoàn chỉnh bám sát chuẩn mực của Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội."
    paras[92].text = "• Ứng dụng Android Native hoạt động mượt mà, giao diện Obsidian Dark Theme cao cấp, nhận diện vật thể thời gian thực đạt 34–42 FPS, quét hóa đơn thông minh dưới 2.5 giây."
    paras[93].text = "• Máy chủ Backend Spring Boot 3 vững chắc, tích hợp mô hình ONNX Runtime suy luận dưới 2ms, bảo mật Stateless JWT với Firebase Token."
    paras[94].text = "• Bộ kiểm thử tự động đạt 86/86 Tests passed (100%), chứng minh tính ổn định tuyệt đối của toàn bộ hệ thống phần mềm."
    paras[95].text = "• Góp phần ứng dụng công nghệ Trí tuệ Nhân tạo và Thị giác Máy tính vào quản lý tài chính cá nhân một cách thuận tiện, khoa học và hiệu quả."
    
    paras[96].text = "Bố cục đồ án"
    paras[97].text = "Bản báo cáo đồ án tốt nghiệp được bố cục thành 4 chương chính cùng phần Mở đầu, Kết luận, Phụ lục và Tài liệu tham khảo:"
    paras[98].text = "Chương 1: Cơ sở lý thuyết – Trình bày tổng quan về bài toán tài chính cá nhân, nền tảng Android Kotlin, Spring Boot 3, MySQL 8.0, Firebase Auth, mô hình YOLOv8, Random Forest, ONNX Runtime, Google ML Kit OCR, Gemini LLM và Docker Compose."
    paras[99].text = "Chương 2: Phân tích thiết kế hệ thống – Trình bày phân tích yêu cầu (FR/NFR), biểu đồ phân cấp chức năng, sơ đồ Use Case, kiến trúc hệ thống 3 tầng, pipeline AI 2 giai đoạn, sơ đồ tuần tự, thiết kế cơ sở dữ liệu 9 bảng chuẩn 3NF và thiết kế giao diện người dùng."
    paras[100].text = "Chương 3: Cài đặt và thực nghiệm hệ thống – Chi tiết quy trình cài đặt môi trường, xây dựng CSDL, triển khai Backend API, phát triển ứng dụng di động Android, huấn luyện và nhúng các mô hình AI, kết quả kiểm thử 86 bài test tự động và các giải pháp tối ưu hóa hiệu năng."
    paras[101].text = "Chương 4: Kết luận và hướng phát triển – Đánh giá kết quả đạt được đối chiếu với 5 mục tiêu đề cương ban đầu, nêu bật các đóng góp khoa học và thực tiễn, phân tích khó khăn, hạn chế và đề xuất các hướng mở rộng trong tương lai."
    paras[102].text = ""

    for idx in range(59, 103):
        p = paras[idx]
        is_sub = idx in [60, 65, 68, 72, 78, 83, 86, 90, 96]
        is_h1 = (idx == 59)
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER if is_h1 else WD_ALIGN_PARAGRAPH.JUSTIFY
        for r in p.runs:
            format_run(r, font_name="Times New Roman", size_pt=13, bold=(is_h1 or is_sub), color_rgb=RGBColor(0, 32, 96) if is_h1 else None)

    # Rebuild TOC SDT with clean entries
    rebuild_toc_sdt(doc)

    # 6. Danh mục các bảng (P105 to P134)
    paras[105].text = "DANH MỤC CÁC BẢNG"
    for i, (t_code, t_title, t_pg) in enumerate(TABLES_LIST):
        idx = 106 + i
        if idx < 135:
            paras[idx].text = f"{t_code}: {t_title}\t{t_pg}"
            paras[idx].style = "table of figures"
    for idx in range(106 + len(TABLES_LIST), 135):
        paras[idx].text = ""

    # 7. Danh mục hình ảnh (P135 to P196)
    paras[135].text = "DANH MỤC HÌNH ẢNH"
    for i, (f_code, f_title, f_pg) in enumerate(FIGURES_LIST):
        idx = 136 + i
        if idx < 197:
            paras[idx].text = f"{f_code}: {f_title}\t{f_pg}"
            paras[idx].style = "table of figures"
    for idx in range(136 + len(FIGURES_LIST), 197):
        paras[idx].text = ""

    # 8. Danh mục từ viết tắt (Table 3)
    paras[197].text = "DANH MỤC KÝ HIỆU VÀ CHỮ VIẾT TẮT, THUẬT NGỮ"
    t3 = doc.tables[3]
    t3.rows[0].cells[0].text = "Từ viết tắt"
    t3.rows[0].cells[1].text = "Ý nghĩa"
    for r in t3.rows[0].cells[0].paragraphs[0].runs: format_run(r, font_name="Times New Roman", size_pt=11, bold=True)
    for r in t3.rows[0].cells[1].paragraphs[0].runs: format_run(r, font_name="Times New Roman", size_pt=11, bold=True)
    
    target_rows = len(ABBREVIATIONS) + 1
    # add rows if needed
    while len(t3.rows) < target_rows:
        t3.add_row()
    # trim rows if needed
    while len(t3.rows) > target_rows:
        t3._tbl.remove(t3.rows[-1]._tr)
        
    for i, (abbr, desc) in enumerate(ABBREVIATIONS):
        row = t3.rows[i + 1]
        row.cells[0].text = abbr
        row.cells[1].text = desc
        for r in row.cells[0].paragraphs[0].runs: format_run(r, font_name="Times New Roman", size_pt=10.5, bold=True)
        for r in row.cells[1].paragraphs[0].runs: format_run(r, font_name="Times New Roman", size_pt=10.5)

    # 9. Update Document Core Properties
    doc.core_properties.author = "Nguyễn Duy Khánh"
    doc.core_properties.last_modified_by = "Nguyễn Duy Khánh"
    doc.core_properties.title = "BÁO CÁO ĐỒ ÁN TỐT NGHIỆP KỸ SƯ CÔNG NGHỆ THÔNG TIN"
    doc.core_properties.subject = "XÂY DỰNG ỨNG DỤNG QUẢN LÝ TÀI CHÍNH CÁ NHÂN TRÊN NỀN TẢNG ANDROID TÍCH HỢP NHẬN DIỆN VÀ PHÂN LOẠI CHI TIÊU BẰNG TRÍ TUỆ NHÂN TẠO"
    doc.core_properties.comments = "Khoa Công nghệ Thông tin - Trường Đại học Kiến trúc Hà Nội - Khóa 2022-2027"

    # 10. Clean Footers in all sections (remove any residual author names in footer SDTs)
    for s in doc.sections:
        for f_name in ['footer', 'first_page_footer', 'even_page_footer']:
            f = getattr(s, f_name, None)
            if f is not None:
                for node in f._element.iter('{http://schemas.openxmlformats.org/wordprocessingml/2006/main}t'):
                    if node.text and 'Thu Trang' in node.text:
                        node.text = node.text.replace('Doãn Thu Trang', 'Nguyễn Duy Khánh').replace('21CNTT4', '22CN4').replace('21CN4', '22CN4')

        
    print("Front matter successfully updated in template.")


def build_chapter1(doc):
    """Builds Chapter 1: Cơ sở lý thuyết."""
    add_h1(doc, "CHƯƠNG 1: CƠ SỞ LÝ THUYẾT")
    
    add_h2(doc, "1.1. Tổng quan bài toán Quản lý tài chính cá nhân trên thiết bị di động")
    add_p(doc, "Quản lý tài chính cá nhân là quá trình lập kế hoạch, theo dõi thu nhập, chi tiêu, quản lý các tài khoản ngân sách và hoạch định các mục tiêu tài chính tương lai của một cá nhân hoặc hộ gia đình. Trong bối cảnh kinh tế hiện đại với sự phổ biến của thanh toán không dùng tiền mặt (thẻ ngân hàng, ví điện tử, mã QR), các giao dịch phát sinh diễn ra liên tục, phân tán trên nhiều nền tảng, khiến người dùng rất dễ rơi vào tình trạng mất kiểm soát chi tiêu và không nắm rõ dòng tiền thực tế của bản thân.")
    add_p(doc, "Mặc dù đã có nhiều công cụ phần mềm hỗ trợ quản lý tài chính trên điện thoại thông minh, song khảo sát thực tế cho thấy tỷ lệ người dùng duy trì thói quen ghi chép liên tục trên 3 tháng chỉ đạt dưới 20%. Nguyên nhân cốt lõi là do sự phiền toái, mất thời gian và tính lặp đi lặp lại của thao tác nhập liệu thủ công bằng bàn phím. Để giải quyết triệt để vấn đề này, đề tài nghiên cứu giải pháp tự động hóa ghi chép thông qua công nghệ Thị giác Máy tính (Computer Vision) và Trí tuệ Nhân tạo (AI), cho phép nhận diện sản phẩm tiêu dùng trực tiếp qua camera và bóc tách thông tin hóa đơn tự động trong thời gian thực.")
    add_image_note(doc, "Sơ đồ minh họa tổng quan bài toán quản lý tài chính cá nhân và giải pháp ứng dụng công nghệ AI, Edge Computing, OCR để tự động hóa ghi chép.", "Hình 1.1: Tổng quan bài toán và giải pháp Quản lý tài chính cá nhân thông minh")
    
    add_h2(doc, "1.2. Hệ điều hành Android và ngôn ngữ lập trình Kotlin")
    add_h3(doc, "1.2.1. Tổng quan nền tảng Android và Android Jetpack")
    add_p(doc, "Android là hệ điều hành di động mã nguồn mở phổ biến nhất thế giới hiện nay, chiếm hơn 70% thị phần thiết bị di động toàn cầu. Được xây dựng trên nền tảng nhân Linux (Linux Kernel), Android cung cấp kiến trúc phân tầng mạnh mẽ bao gồm: Linux Kernel, Hardware Abstraction Layer (HAL), Android Runtime (ART), Native C/C++ Libraries, Java API Framework và tầng ứng dụng người dùng (System & User Apps).")
    add_p(doc, "Nhằm chuẩn hóa quy trình phát triển ứng dụng di động hiện đại, Google đã giới thiệu bộ thư viện Android Jetpack. Jetpack cung cấp các thành phần kiến trúc (Architecture Components) tiêu chuẩn giúp lập trình viên viết mã nguồn sạch, giảm thiểu mã mẫu (boilerplate code), quản lý vòng đời ứng dụng an toàn và duy trì tính tương thích ngược trên nhiều phiên bản hệ điều hành Android khác nhau:")
    add_bullet(doc, "Quản lý dữ liệu giao diện có nhận thức vòng đời (Lifecycle-aware), đảm bảo dữ liệu không bị mất khi màn hình xoay hướng hoặc thay đổi cấu hình.", "ViewModel")
    add_bullet(doc, "Lớp bao bọc dữ liệu có thể quan sát (Observable data holder), tự động thông báo và cập nhật giao diện khi dữ liệu thay đổi.", "LiveData")
    add_bullet(doc, "Chuẩn hóa điều hướng giữa các màn hình (Activities) và các phân mảnh giao diện (Fragments), quản lý Backstack an toàn và trực quan qua đồ thị điều hướng (NavGraph).", "Navigation Component")
    add_bullet(doc, "Cơ chế liên kết giao diện XML với mã nguồn Kotlin an toàn kiểu dữ liệu (Type-safe), thay thế hoàn toàn phương thức findViewById truyền thống và loại bỏ lỗi NullPointerException.", "ViewBinding")
    add_image_note(doc, "Sơ đồ kiến trúc tổng thể hệ điều hành Android kết hợp các thành phần cốt lõi của bộ thư viện Android Jetpack Architecture Components.", "Hình 1.2: Kiến trúc hệ điều hành Android và Android Jetpack")
    
    add_h3(doc, "1.2.2. Kiến trúc phần mềm MVVM (Model - View - ViewModel)")
    add_p(doc, "Đề tài áp dụng kiến trúc phần mềm MVVM (Model - View - ViewModel) – chuẩn kiến trúc được Google khuyến nghị chính thức cho các ứng dụng Android hiện đại. MVVM phân tách triệt để các trách nhiệm trong ứng dụng thành 3 tầng độc lập:")
    add_bullet(doc, "Bao gồm các thực thể dữ liệu (Entities, DTOs) và tầng Repository chịu trách nhiệm truy xuất, đóng gói dữ liệu từ mạng (REST API) hoặc bộ nhớ cục bộ.", "Model")
    add_bullet(doc, "Bao gồm các Activity và Fragment, chỉ đảm nhận vai trò hiển thị giao diện và lắng nghe các tương tác chạm của người dùng. Tầng View không chứa bất kỳ logic nghiệp vụ nào.", "View")
    add_bullet(doc, "Đóng vai trò trung gian kết nối giữa View và Model. ViewModel nạp dữ liệu từ Repository, xử lý logic hiển thị và cung cấp dữ liệu ra View dưới dạng LiveData.", "ViewModel")
    add_image_note(doc, "Sơ đồ phân tầng kiến trúc MVVM thể hiện luồng dữ liệu một chiều giữa View, ViewModel, Repository và các nguồn dữ liệu mạng/CSDL.", "Hình 1.3: Mô hình kiến trúc MVVM (Model - View - ViewModel) trên Android")
    
    add_h3(doc, "1.2.3. Bất đồng bộ hiện đại với Kotlin Coroutines và Kiến trúc Lifecycle-Aware")
    add_p(doc, "Trong các ứng dụng di động xử lý tác vụ nặng (như tải ảnh từ mạng, giải mã chứng từ, phân tích dữ liệu AI), việc thực thi các tác vụ này trên luồng chính (Main Thread / UI Thread) sẽ dẫn đến hiện tượng đứng hình (ANR - Application Not Responding) và giật lag nghiêm trọng. Trước đây, lập trình viên Android thường sử dụng các cơ chế đa luồng cổ điển của Java như raw Thread, Runnable, AsyncTask hoặc Handler.")
    add_p(doc, "Tuy nhiên, các phương pháp cổ điển này có nhược điểm chí mạng là không nhận thức được vòng đời của màn hình. Khi người dùng bấm nút Back hoặc chuyển màn hình trong lúc luồng ngầm đang chạy, tham chiếu tới Activity/Fragment vẫn bị giữ lại trong bộ nhớ, dẫn đến hiện tượng rò rỉ bộ nhớ nghiêm trọng (Memory Leak) và crash ứng dụng khi luồng ngầm cố gắng cập nhật giao diện đã bị hủy. Đề tài đã chuẩn hóa toàn bộ mã nguồn sang Kotlin Coroutines kết hợp chặt chẽ với viewLifecycleOwner.lifecycleScope:")
    add_bullet(doc, "Tác vụ gọi API mạng hoặc đọc ghi file được chuyển sang luồng phụ IO tối ưu cho các thao tác vào/ra.", "Dispatchers.IO")
    add_bullet(doc, "Tác vụ cập nhật giao diện sau khi có dữ liệu được đưa về luồng chính Main Thread an toàn.", "Dispatchers.Main")
    add_bullet(doc, "Toàn bộ Coroutine gắn liền với vòng đời của Fragment, tự động bị hủy (auto-cancellation) ngay khi Fragment bị đóng, đảm bảo triệt tiêu 100% rủi ro rò rỉ bộ nhớ.", "LifecycleScope")
    
    add_h3(doc, "1.2.4. Thư viện CameraX và phân tích khung hình thời gian thực")
    add_p(doc, "CameraX là thư viện thuộc bộ Jetpack được xây dựng nhằm đơn giản hóa việc phát triển camera trên Android. CameraX hỗ trợ kiến trúc hướng ca sử dụng (Use Case-based architecture) bao gồm Preview (hiển thị khung hình xem trước), ImageCapture (chụp ảnh tĩnh độ phân giải cao) và ImageAnalysis (cung cấp luồng khung hình liên tục cho các thuật toán phân tích AI). Đề tài sử dụng ImageAnalysis kết hợp giải phóng bộ nhớ ImageProxy an toàn để truyền trực tiếp dữ liệu khung hình vào mô hình YOLOv8n TFLite với độ trễ tối thiểu.")
    
    add_h2(doc, "1.3. Nền tảng Spring Boot 3 và Java 21")
    add_h3(doc, "1.3.1. Tổng quan Spring Boot 3 và kiến trúc phân tầng")
    add_p(doc, "Spring Boot 3 là phiên bản đột phá của hệ sinh thái Spring Framework, yêu cầu môi trường thực thi tối thiểu là Java 17 và tối ưu hóa cao độ cho Java 21 LTS. Spring Boot 3 mang lại khả năng khởi động nhanh chóng, cơ chế tự động cấu hình (Auto-Configuration) thông minh và khả năng nhúng sẵn máy chủ Web servlet (Apache Tomcat 10) mà không cần triển khai tệp WAR phức tạp lên các máy chủ ứng dụng bên ngoài.")
    add_p(doc, "Máy chủ Backend của đề tài áp dụng kiến trúc phân tầng chuẩn hóa (Layered Architecture):")
    add_bullet(doc, "Tiếp nhận các HTTP Request, thực hiện xác thực và phân quyền, kiểm tra tính hợp lệ của dữ liệu đầu vào và chuyển tiếp xuống tầng dịch vụ.", "Controller Layer")
    add_bullet(doc, "Chứa toàn bộ logic nghiệp vụ tài chính, xử lý biến động số dư, quản lý ngân sách, kiểm tra logic giao dịch và điều phối các dịch vụ Trí tuệ Nhân tạo.", "Service Layer")
    add_bullet(doc, "Sử dụng Spring Data JPA để giao tiếp và thao tác dữ liệu với hệ quản trị cơ sở dữ liệu quan hệ MySQL.", "Repository Layer")
    add_image_note(doc, "Sơ đồ kiến trúc phân tầng của máy chủ Spring Boot 3 Backend từ Controller, Service, Repository đến Database và các dịch vụ AI tích hợp.", "Hình 1.4: Kiến trúc phân tầng máy chủ Spring Boot 3 Backend")
    
    add_h3(doc, "1.3.2. Spring Security và cơ chế lọc xác thực Stateless")
    add_p(doc, "Spring Security 6 cung cấp khung bảo vệ toàn diện cho ứng dụng. Để phù hợp với kiến trúc vi dịch vụ (Microservices) và ứng dụng di động, hệ thống loại bỏ cơ chế Session dựa trên Cookie truyền thống và chuyển sang cơ chế bảo mật phi trạng thái (Stateless Authentication). Bộ lọc tùy biến FirebaseAuthFilter chặn mọi HTTP Request gửi đến, trích xuất Firebase ID Token từ tiêu đề Authorization (Bearer Token) và sử dụng Firebase Admin SDK để giải mã chữ ký số công khai từ máy chủ Google.")
    
    add_h3(doc, "1.3.3. Spring Data JPA, Hibernate ORM và Connection Pool HikariCP")
    add_p(doc, "Spring Data JPA trừu tượng hóa tầng truy xuất dữ liệu, tự động sinh các câu lệnh SQL tối ưu dựa trên tên phương thức. Hibernate 6 đóng vai trò là nhà cung cấp JPA (JPA Provider), ánh xạ các bảng quan hệ MySQL thành các đối tượng Java (POJO Entities). Hệ thống sử dụng HikariCP – giải pháp quản lý vùng kết nối (Connection Pool) nhanh và nhẹ nhất thế giới hiện nay, cấu hình kết nối đa luồng tối ưu giúp thời gian phản hồi truy vấn giảm xuống dưới 5ms.")
    
    add_h3(doc, "1.3.4. Bean Validation và xử lý ngoại lệ tập trung (GlobalExceptionHandler)")
    add_p(doc, "Dữ liệu gửi từ ứng dụng di động lên Backend được kiểm soát chặt chẽ thông qua chuẩn Bean Validation với các chú thích như @NotNull, @NotBlank, @PositiveOrZero. Mọi lỗi nghiệp vụ hoặc ngoại lệ hệ thống đều được bắt giữ tập trung qua lớp GlobalExceptionHandler sử dụng chú thích @RestControllerAdvice, đảm bảo phản hồi trả về luôn tuân thủ chuẩn JSON ApiResponse đồng nhất.")
    
    add_h2(doc, "1.4. Hệ quản trị cơ sở dữ liệu MySQL 8.0")
    add_p(doc, "MySQL 8.0 là hệ quản trị cơ sở dữ liệu quan hệ mã nguồn mở hàng đầu thế giới, cung cấp hiệu năng vượt trội, độ tin cậy cao và tính tuân thủ nghiêm ngặt các tiêu chuẩn ACID (Atomicity, Consistency, Isolation, Durability) thông qua công cụ lưu trữ InnoDB. Đề tài cấu hình MySQL 8.0 với các đặc tính kỹ thuật tối ưu:")
    add_bullet(doc, "Hỗ trợ đầy đủ bộ mã ký tự Unicode 4-byte, cho phép lưu trữ hoàn hảo tiếng Việt có dấu đầy đủ và các biểu tượng cảm xúc (emoji) mà không xảy ra hiện tượng vỡ font (Mojibake).", "Bộ mã utf8mb4")
    add_bullet(doc, "Thiết lập các ràng buộc khóa ngoại (Foreign Keys) với cơ chế ON DELETE CASCADE, đảm bảo khi một người dùng hoặc tài khoản ví bị xóa, toàn bộ dữ liệu giao dịch liên quan sẽ tự động được dọn rác sạch sẽ, ngăn ngừa dữ liệu mồ côi.", "Ràng buộc toàn vẹn CASCADE")
    add_bullet(doc, "Tạo chỉ mục trên các cột thường xuyên được truy vấn lọc theo ngày tháng và danh mục, tối ưu hóa tốc độ tìm kiếm với độ phức tạp thuật toán O(log N).", "Tối ưu hóa chỉ mục (Indexes)")
    
    add_h2(doc, "1.5. Dịch vụ xác thực Firebase Authentication")
    add_p(doc, "Firebase Authentication là nền tảng quản lý danh tính an toàn được phát triển bởi Google. Đề tài áp dụng mô hình Hybrid Authentication độc đáo:")
    add_bullet(doc, "Người dùng thực hiện đăng nhập bảo mật bằng Email/Mật khẩu hoặc Google Sign-In 1 chạm thông qua Firebase Android SDK. Thiết bị nhận về Firebase ID Token (chuỗi JWT có hạn sử dụng 1 giờ).", "Xác thực phía Client")
    add_bullet(doc, "Sau khi nhận token từ Client, máy chủ Spring Boot sử dụng chứng chỉ dịch vụ Firebase Admin SDK để xác minh chữ ký điện tử trực tiếp với Google, trích xuất Firebase UID duy nhất và đồng bộ vào bảng users trong CSDL MySQL mà không cần lưu trữ mật khẩu thô của người dùng.", "Xác thực phía Backend")
    
    add_h2(doc, "1.6. Trí tuệ nhân tạo và Thị giác máy tính")
    add_h3(doc, "1.6.1. Mô hình nhận diện vật thể YOLOv8 và tối ưu hóa On-Device TFLite")
    add_p(doc, "YOLO (You Only Look Once) là dòng mô hình phát hiện đối tượng thời gian thực hàng đầu trong lĩnh vực thị giác máy tính. Phiên bản YOLOv8 do Ultralytics phát triển năm 2023 mang lại kiến trúc không neo (Anchor-free) tiên tiến, phân tách độc lập các nhánh dự đoán hộp bao (Bounding Box) và xác suất phân loại (Classification), giúp tăng cường đáng kể độ chính xác và tốc độ suy luận.")
    add_p(doc, "Trong đồ án, phiên bản nhỏ nhất YOLOv8n (Nano) được lựa chọn và huấn luyện trên tập dữ liệu gồm 18 lớp sản phẩm tiêu dùng phổ biến. Sau quá trình huấn luyện, mô hình được xuất sang định dạng TensorFlow Lite (TFLite Float32) với dung lượng cực nhẹ chỉ 6.3 MB, nạp trực tiếp vào bộ nhớ thiết bị di động để thực thi suy luận tại biên (Edge Inference) với tốc độ 34–42 khung hình/giây mà không cần gửi ảnh lên máy chủ.")
    add_image_note(doc, "Sơ đồ kiến trúc mạng nơ-ron tích chập YOLOv8 gồm các khối Backbone CSPDarknet53, Neck PANet và Head Anchor-Free.", "Hình 1.5: Kiến trúc mạng nơ-ron tích chập YOLOv8 trong thị giác máy tính")
    
    add_h3(doc, "1.6.2. Mô hình phân loại Random Forest và công nghệ nhúng Microsoft ONNX Runtime Java")
    add_p(doc, "Trong bài toán quản lý tài chính, mô hình YOLO chỉ trả lời câu hỏi 'Vật thể trong ảnh là gì' (ví dụ: coffee_cup, bread, clothes), nhưng không thể xác định vật thể đó thuộc 'Danh mục chi tiêu tài chính nào' (Ăn uống, Mua sắm, Di chuyển...). Đề tài giải quyết vấn đề này bằng mô hình học máy thứ hai: Random Forest Classifier.")
    add_p(doc, "Random Forest là thuật toán học máy tập hợp (Ensemble Learning) hoạt động dựa trên kỹ thuật Bagging, kết hợp hàng trăm cây quyết định (Decision Trees) được huấn luyện trên các tập con dữ liệu ngẫu nhiên. Dự đoán cuối cùng là kết quả bỏ phiếu đa số (Majority Voting) giữa các cây, giúp mô hình có khả năng chống hiện tượng quá khớp (Overfitting) cực kỳ xuất sắc.")
    add_p(doc, "Để đưa mô hình Random Forest (vốn được xây dựng bằng Python Scikit-Learn) vào vận hành trên máy chủ Spring Boot, đề tài ứng dụng chuẩn mở ONNX (Open Neural Network Exchange) và thư viện Microsoft ONNX Runtime Java. Mô hình được xuất thành tệp random_forest_model.onnx (dung lượng 768 KB), nạp trực tiếp vào bộ nhớ RAM của Java khi máy chủ khởi động, cho phép suy luận 27 đặc trưng kỹ thuật với thời gian siêu tốc dưới 2ms mà không cần duy trì máy chủ Python phụ trợ.")
    add_image_note(doc, "Sơ đồ hoạt động của mô hình cây quyết định Random Forest và cơ chế bỏ phiếu đa số để dự đoán danh mục chi tiêu.", "Hình 1.6: Mô hình cây quyết định Random Forest và cơ chế bỏ phiếu")
    
    add_h3(doc, "1.6.3. Nhận dạng ký tự quang học Google ML Kit Text Recognition")
    add_p(doc, "Google ML Kit Text Recognition là bộ công cụ học máy chạy hoàn toàn Offline trên thiết bị di động, cung cấp khả năng nhận dạng văn bản quang học (OCR) với tốc độ cực nhanh (dưới 400ms). ML Kit phân tích hình ảnh và trả về cấu trúc phân cấp chặt chẽ: Khối văn bản (TextBlock), Dòng chữ (TextLine) và Phần tử ký tự (TextElement). Việc thực hiện OCR cục bộ trên điện thoại giúp giảm hơn 70% lượng token cần gửi lên máy chủ đám mây.")
    
    add_h3(doc, "1.6.4. Mô hình ngôn ngữ lớn Google Gemini 2.5 Flash trong bóc tách dữ liệu có cấu trúc JSON")
    add_p(doc, "Hóa đơn thanh toán thực tế tại Việt Nam vô cùng đa dạng, thường bị nhăn, rách, in nhiệt mờ hoặc có bố cục phức tạp, khiến các thuật toán biểu thức chính quy (Regex) truyền thống hoàn toàn bất lực. Đề tài tích hợp mô hình ngôn ngữ lớn Google Gemini 2.5 Flash để đọc hiểu ngữ cảnh văn bản hóa đơn.")
    add_p(doc, "Hệ thống áp dụng kỹ thuật Kỹ nghệ câu lệnh (Prompt Engineering) kết hợp cấu hình phản hồi bắt buộc (responseSchema / responseMimeType: application/json) trong lớp GeminiService. Nhờ đó, Gemini phân tích đoạn text thô từ ML Kit và trích xuất chính xác 4 trường dữ liệu chuẩn: tên cửa hàng (merchant), tổng tiền thanh toán (amount), ngày giao dịch (date) và gợi ý danh mục chi tiêu (category) với thời gian phản hồi trung bình chỉ 2.45 giây và mức tiêu thụ token tối thiểu.")
    add_image_note(doc, "Sơ đồ quy trình trích xuất hóa đơn thông minh kết hợp giữa OCR offline trên điện thoại và LLM Gemini Flash trên đám mây.", "Hình 1.7: Quy trình trích xuất hóa đơn Google ML Kit OCR kết hợp Gemini LLM")
    
    add_h2(doc, "1.7. Đóng gói đa dịch vụ với Docker & Docker Compose")
    add_p(doc, "Docker là nền tảng ảo hóa mức hệ điều hành (Containerization) cho phép đóng gói toàn bộ ứng dụng cùng các thư viện phụ thuộc vào một vùng chứa (Container) độc lập, đảm bảo ứng dụng vận hành nhất quán trên mọi môi trường phát triển và máy chủ thực tế. Đề tài sử dụng Docker Compose để điều phối toàn bộ cụm dịch vụ Backend (Spring Boot trên nền Eclipse Temurin JRE 21) và Database (MySQL 8.0) thông qua tệp cấu hình docker-compose.yml duy nhất, cho phép khởi chạy toàn bộ hệ sinh thái chỉ bằng một câu lệnh.")
    add_image_note(doc, "Sơ đồ kiến trúc ảo hóa đa container của Docker Compose gồm mạng nội bộ kết nối Spring Boot và MySQL.", "Hình 1.8: Kiến trúc ảo hóa đa container với Docker Compose")
    
    add_h2(doc, "1.8. Tổng kết chương 1")
    add_p(doc, "Chương 1 đã trình bày toàn diện và chi tiết cơ sở lý thuyết cùng các công nghệ nền tảng được áp dụng trong đề tài, từ kiến trúc phần mềm di động Android MVVM với Kotlin Coroutines, nền tảng dịch vụ vi mô Spring Boot 3 và Java 21, hệ quản trị CSDL MySQL 8.0 chuẩn 3NF, đến các công nghệ Trí tuệ Nhân tạo đột phá: YOLOv8n TFLite On-Device, Random Forest ONNX Java, Google ML Kit OCR, Google Gemini 2.5 Flash LLM và Docker Compose. Đây là cơ sở khoa học và kỹ thuật vững chắc để tiến hành phân tích, thiết kế và cài đặt hệ thống trong các chương tiếp theo.")

def build_chapter2(doc):
    """Builds Chapter 2: Phân tích và thiết kế hệ thống."""
    add_h1(doc, "CHƯƠNG 2: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG")
    
    add_h2(doc, "2.1. Phân tích yêu cầu hệ thống")
    add_h3(doc, "2.1.1. Yêu cầu chức năng (Functional Requirements)")
    add_p(doc, "Căn cứ vào mục tiêu nghiên cứu và khảo sát thực tế, hệ thống Smart Personal Finance App được thiết kế nhằm đáp ứng 7 nhóm yêu cầu chức năng cốt lõi:")
    
    fr_headers = ["Mã YC", "Tên chức năng", "Mô tả chi tiết yêu cầu nghiệp vụ", "Độ ưu tiên"]
    fr_data = [
        ["FR-01", "Quản lý xác thực & Người dùng", "Đăng ký/Đăng nhập qua Email và Google Sign-In 1 chạm; xác thực bảo mật qua Firebase ID Token; cập nhật thông tin cá nhân (Họ tên, SĐT, avatar).", "Bắt buộc (High)"],
        ["FR-02", "Quản lý tài khoản ví", "Khởi tạo, chỉnh sửa, xóa nhiều ví tài chính (Tiền mặt, Ngân hàng, Ví điện tử); tự động cộng/trừ số dư theo thời gian thực; chuyển tiền giữa các ví.", "Bắt buộc (High)"],
        ["FR-03", "Quản lý giao dịch thu chi", "Ghi chép giao dịch Thu/Chi; phân loại danh mục; đính kèm ảnh chứng từ; lọc giao dịch theo ngày, tháng, tài khoản ví.", "Bắt buộc (High)"],
        ["FR-04", "Quét hóa đơn thông minh (OCR+AI)", "Chụp hóa đơn; Google ML Kit nhận dạng văn bản offline; Google Gemini 2.5 Flash trích xuất tổng tiền, ngày, tên cửa hàng, danh mục; tự điền form giao dịch.", "Bắt buộc (High)"],
        ["FR-05", "Nhận diện sản phẩm Camera AI", "CameraX phân tích khung hình; YOLOv8n TFLite nhận diện 18 lớp vật thể và vẽ Bounding Box thời gian thực; Random Forest ONNX gợi ý danh mục chi tiêu.", "Bắt buộc (High)"],
        ["FR-06", "Quản lý ngân sách & Cảnh báo", "Đặt hạn mức chi tiêu theo tháng và danh mục; thanh tiến độ đổi màu 3 cấp độ (<80% Xanh, 80-100% Cam, >100% Đỏ) cảnh báo vượt hạn mức.", "Bắt buộc (High)"],
        ["FR-07", "Giao dịch định kỳ & Báo cáo", "Tự động phát sinh giao dịch lặp (tiền nhà, tiền điện) qua Cronjob 00:00; API kích hoạt tức thì; biểu đồ trực quan PieChart cơ cấu và BarChart xu hướng.", "Bắt buộc (High)"],
    ]
    add_table_custom(doc, "Bảng 2.1: Yêu cầu chức năng của hệ thống (Functional Requirements)", fr_headers, fr_data, [1.0, 1.6, 3.2, 1.2])
    
    add_h3(doc, "2.1.2. Yêu cầu phi chức năng (Non-Functional Requirements)")
    add_p(doc, "Bên cạnh các yêu cầu nghiệp vụ, hệ thống cần đáp ứng các chuẩn mực khắt khe về kỹ thuật phần mềm, hiệu năng và tính bảo mật:")
    
    nfr_headers = ["Mã YC", "Hạng mục", "Tiêu chuẩn kỹ thuật định lượng yêu cầu", "Độ ưu tiên"]
    nfr_data = [
        ["NFR-01", "Hiệu năng thị giác máy tính", "Mô hình YOLOv8n TFLite chạy On-Device đạt tốc độ xử lý từ 30 FPS trở lên; mô hình Random Forest ONNX suy luận dưới 2ms trên Backend.", "Bắt buộc (High)"],
        ["NFR-02", "Thời gian phản hồi hệ thống", "Thời gian bóc tách toàn trình hóa đơn (OCR + Gemini LLM) dưới 3 giây; các API nghiệp vụ CRUD có độ trễ dưới 200ms trong điều kiện mạng bình thường.", "Bắt buộc (High)"],
        ["NFR-03", "Bảo mật & Toàn vẹn dữ liệu", "100% API nghiệp vụ bảo vệ qua Firebase ID Token Stateless; không lưu mật khẩu thô; kiểm soát giao dịch ACID chống số dư âm ngoài ý muốn.", "Bắt buộc (High)"],
        ["NFR-04", "Độ tin cậy & Ổn định", "Hệ thống Backend vượt qua 100% các bài kiểm thử tự động (86/86 Tests passed); cơ chế Heuristic Rule-based Fallback bảo vệ hệ thống khi thiếu thư viện native.", "Bắt buộc (High)"],
        ["NFR-05", "Trải nghiệm & Tính tương thích", "Giao diện Obsidian Dark Theme cao cấp, tiết kiệm pin OLED; tương thích Android 7.0 đến Android 14+; tối ưu kiến trúc bộ nhớ 4KB Page Size.", "Bắt buộc (High)"],
    ]
    add_table_custom(doc, "Bảng 2.2: Yêu cầu phi chức năng của hệ thống (Non-Functional Requirements)", nfr_headers, nfr_data, [1.0, 1.6, 3.2, 1.2])
    
    add_h2(doc, "2.2. Thiết kế chức năng hệ thống")
    add_h3(doc, "2.2.1. Biểu đồ phân cấp chức năng (Business Function Chart - BFC)")
    add_p(doc, "Hệ thống được cấu trúc thành 5 khối chức năng nghiệp vụ chính: (1) Quản lý tài khoản & Xác thực, (2) Quản lý tài chính & Giao dịch, (3) Nhận diện & Bóc tách Trí tuệ Nhân tạo, (4) Quản lý ngân sách & Định kỳ, và (5) Báo cáo thống kê tài chính.")
    add_image_note(doc, "Biểu đồ phân cấp chức năng (BFC) thể hiện chi tiết cây chức năng của 5 phân hệ nghiệp vụ chính.", "Hình 2.1: Biểu đồ phân cấp chức năng hệ thống (BFC)")
    
    add_h3(doc, "2.2.2. Sơ đồ Use Case tổng quát")
    add_p(doc, "Hệ thống có 2 tác nhân chính tham gia tương tác: Người dùng cuối (End User - sử dụng ứng dụng Android) và Hệ thống lập lịch tự động (System Scheduler Cronjob - chạy ngầm trên máy chủ Spring Boot).")
    add_image_note(doc, "Sơ đồ Use Case tổng quát thể hiện các ca sử dụng chính của tác nhân Người dùng và Hệ thống lập lịch.", "Hình 2.2: Sơ đồ Use Case tổng quát toàn hệ thống")
    
    add_h3(doc, "2.2.3. Sơ đồ Use Case các phân hệ nghiệp vụ")
    add_p(doc, "Dưới đây là chi tiết sơ đồ Use Case cho từng phân hệ nghiệp vụ chuyên biệt:")
    add_image_note(doc, "Sơ đồ Use Case phân hệ Quản lý giao dịch thu chi: Thêm giao dịch, Sửa giao dịch, Xóa giao dịch, Lọc theo ngày/tháng/ví.", "Hình 2.3: Sơ đồ Use Case phân hệ Quản lý giao dịch thu chi")
    add_image_note(doc, "Sơ đồ Use Case phân hệ Quét hóa đơn và Nhận diện sản phẩm AI: Quét camera AI, Chụp hóa đơn OCR, Xác nhận thông tin điền form.", "Hình 2.4: Sơ đồ Use Case phân hệ Quét hóa đơn và Nhận diện sản phẩm AI")
    add_image_note(doc, "Sơ đồ Use Case phân hệ Ngân sách và Giao dịch định kỳ: Thiết lập hạn mức, Cảnh báo đổi màu, Lập lịch định kỳ, Kích hoạt tức thì.", "Hình 2.5: Sơ đồ Use Case phân hệ Ngân sách và Giao dịch định kỳ")
    
    add_h3(doc, "2.2.4. Đặc tả chi tiết các Use Case chính")
    add_p(doc, "Đề tài tiến hành đặc tả chi tiết 7 ca sử dụng cốt lõi của hệ thống:")
    add_bullet(doc, "Tác nhân: Người dùng. Tiền điều kiện: Thiết bị có kết nối mạng. Luồng sự kiện chính: Người dùng chọn đăng nhập bằng Google hoặc nhập Email/Mật khẩu -> Firebase SDK xác thực -> Trả về ID Token -> Gửi lên Backend /api/auth/firebase-login -> Backend xác minh và đồng bộ hồ sơ vào MySQL -> Chuyển vào Dashboard chính.", "UC-01: Đăng nhập hệ thống")
    add_bullet(doc, "Tác nhân: Người dùng. Tiền điều kiện: Đã đăng nhập và có ít nhất 1 ví. Luồng sự kiện chính: Người dùng bấm nút (+) -> Chọn loại Thu/Chi -> Nhập số tiền, chọn danh mục, tài khoản ví, ngày giờ và ghi chú -> Bấm Lưu -> Backend kiểm tra tính hợp lệ -> Ghi bản ghi vào bảng transactions và cập nhật số dư bảng accounts nguyên tử.", "UC-02: Thêm giao dịch thủ công")
    add_bullet(doc, "Tác nhân: Người dùng. Luồng sự kiện chính: Người dùng mở tính năng Quét hóa đơn -> Chụp ảnh phiếu thanh toán -> Google ML Kit nhận dạng text thô -> Gửi rawText lên /api/ai/scan-receipt -> GeminiService gửi prompt đến Gemini 2.5 Flash -> Trả về JSON bóc tách -> Ứng dụng tự động điền vào form thêm giao dịch.", "UC-03: Quét hóa đơn thông minh")
    add_bullet(doc, "Tác nhân: Người dùng. Luồng sự kiện chính: Người dùng hướng camera vào đồ vật -> CameraX Analyzer truyền khung hình vào YOLOv8n TFLite -> Vẽ bounding box thời gian thực kèm nhãn -> Người dùng bấm Chụp -> Backend trích xuất 27 đặc trưng và chạy mô hình Random Forest ONNX -> Trả về danh mục chi tiêu gợi ý chuẩn xác.", "UC-04: Nhận diện sản phẩm Camera AI")
    add_bullet(doc, "Tác nhân: Người dùng. Luồng sự kiện chính: Người dùng chọn danh mục chi tiêu và nhập số tiền hạn mức tối đa cho tháng -> Hệ thống lưu vào bảng budgets -> Khi có giao dịch phát sinh, hệ thống tính tỷ lệ phần trăm đã chi và đổi màu thanh tiến độ trực quan.", "UC-05: Thiết lập hạn mức ngân sách")
    add_bullet(doc, "Tác nhân: Người dùng & Hệ thống. Luồng sự kiện chính: Người dùng tạo lịch lặp định kỳ (tiền nhà, tiền điện hàng tháng) -> Spring Boot Scheduler quét mỗi ngày lúc 00:00 -> Tự động sinh giao dịch mới và cộng/trừ số dư ví -> Hỗ trợ endpoint kích hoạt tức thì phục vụ demo.", "UC-06: Giao dịch định kỳ tự động")
    add_bullet(doc, "Tác nhân: Người dùng. Luồng sự kiện chính: Người dùng chọn phân hệ Báo cáo -> Chọn kỳ xem (Tháng/Năm) -> Hệ thống tổng hợp dữ liệu và vẽ biểu đồ tròn PieChart tỷ trọng danh mục và biểu đồ cột BarChart tương quan thu chi qua MPAndroidChart.", "UC-07: Xem báo cáo thống kê tài chính")
    
    add_h2(doc, "2.3. Thiết kế kiến trúc và luồng xử lý")
    add_h3(doc, "2.3.1. Kiến trúc hệ thống 3 tầng tổng thể")
    add_p(doc, "Hệ thống được thiết kế theo kiến trúc 3 tầng phân tán độc lập (Three-Tier Architecture), phân tách rõ ràng trách nhiệm giữa Tầng trình diễn (Client), Tầng nghiệp vụ (Backend) và Tầng dữ liệu (Database):")
    add_bullet(doc, "Ứng dụng Android Native xây dựng bằng ngôn ngữ Kotlin theo mô hình MVVM, tích hợp On-Device AI (CameraX, YOLOv8 TFLite, Google ML Kit OCR) và MPAndroidChart.", "Tầng Client (Mobile)")
    add_bullet(doc, "Máy chủ RESTful API xây dựng bằng Spring Boot 3 trên nền tảng Java 21 LTS, tích hợp Spring Security Firebase Token Filter, Microsoft ONNX Runtime Java và Google Gemini 2.5 Flash API.", "Tầng Backend (Application)")
    add_bullet(doc, "Hệ quản trị cơ sở dữ liệu quan hệ MySQL 8.0 chuẩn 3NF, cấu hình bộ mã utf8mb4_unicode_ci và ràng buộc khóa ngoại CASCADE, đóng gói trọn gói bằng Docker Compose.", "Tầng Database")
    add_image_note(doc, "Sơ đồ kiến trúc hệ thống 3 tầng tổng thể thể hiện mối liên kết giữa Android Client, Spring Boot Backend và MySQL Database.", "Hình 2.6: Sơ đồ kiến trúc hệ thống 3 tầng tổng thể")
    
    add_h3(doc, "2.3.2. Pipeline AI 2 giai đoạn (Two-Stage Pipeline)")
    add_p(doc, "Đề tài tiên phong xây dựng Pipeline AI 2 giai đoạn độc đáo nhằm chuyển đổi từ kết quả thị giác máy tính sang nghiệp vụ quản lý tài chính:")
    add_bullet(doc, "Thực thi ngay trên vi xử lý điện thoại thông qua mô hình YOLOv8n TFLite. Nhiệm vụ là phát hiện các vật thể mua sắm trong khung hình camera thời gian thực (với 18 lớp mặt hàng).", "Giai đoạn 1: On-Device Object Detection")
    add_bullet(doc, "Thực thi trên máy chủ Spring Boot thông qua Microsoft ONNX Runtime Java. Hệ thống trích xuất vector 27 đặc trưng từ danh sách vật thể YOLO (gồm 18 biến nhị phân, 5 nhóm đếm tần suất và 4 chỉ số tin cậy) và đưa vào mô hình Random Forest 200 cây để phân loại ra 6 danh mục tài chính tương ứng.", "Giai đoạn 2: Backend Expense Classification")
    add_image_note(doc, "Sơ đồ Pipeline AI 2 giai đoạn kết hợp YOLOv8 On-Device và Random Forest ONNX Java để tự động phân loại danh mục chi tiêu.", "Hình 2.7: Sơ đồ Pipeline AI 2 giai đoạn (YOLOv8 On-Device + ONNX Random Forest)")
    
    add_h3(doc, "2.3.3. Sơ đồ tuần tự các luồng nghiệp vụ cốt lõi")
    add_p(doc, "Dưới đây là các sơ đồ tuần tự (Sequence Diagrams) thể hiện chi tiết thông điệp trao đổi giữa các thành phần trong hệ thống:")
    add_image_note(doc, "Sơ đồ tuần tự chức năng Đăng nhập Firebase Authentication: Client -> Firebase SDK -> Spring Boot -> MySQL.", "Hình 2.8: Sơ đồ tuần tự chức năng Đăng nhập Firebase Authentication")
    add_image_note(doc, "Sơ đồ tuần tự chức năng Thêm giao dịch thu chi: Client -> TransactionController -> TransactionService -> AccountRepository -> MySQL.", "Hình 2.9: Sơ đồ tuần tự chức năng Thêm giao dịch thu chi")
    add_image_note(doc, "Sơ đồ tuần tự chức năng Quét hóa đơn: Client ML Kit OCR -> Backend AiScanController -> GeminiService -> Gemini Flash API -> Form.", "Hình 2.10: Sơ đồ tuần tự chức năng Quét hóa đơn OCR kết hợp Gemini LLM")
    add_image_note(doc, "Sơ đồ tuần tự chức năng Nhận diện sản phẩm: Client CameraX YOLO -> BoundingBoxOverlay -> AiProductController -> ONNX Runtime Java.", "Hình 2.11: Sơ đồ tuần tự chức năng Nhận diện sản phẩm Camera AI")
    
    add_h2(doc, "2.4. Thiết kế cơ sở dữ liệu")
    add_h3(doc, "2.4.1. Sơ đồ thực thể liên kết (Entity Relationship Diagram - ERD)")
    add_p(doc, "Cơ sở dữ liệu của hệ thống được thiết kế chuẩn hóa đạt chuẩn dạng chuẩn 3 (3NF), bao gồm 9 thực thể quan hệ chặt chẽ:")
    add_image_note(doc, "Sơ đồ thực thể liên kết (ERD) thể hiện cấu trúc 9 bảng dữ liệu, khóa chính, khóa ngoại và quan hệ 1:N trong hệ thống.", "Hình 2.12: Sơ đồ thực thể liên kết (ERD) cơ sở dữ liệu hệ thống")
    
    add_h3(doc, "2.4.2. Thiết kế chi tiết các bảng dữ liệu")
    add_p(doc, "Chi tiết cấu trúc, kiểu dữ liệu, ràng buộc và mô tả của 9 bảng trong cơ sở dữ liệu MySQL:")
    for t_key in ["users", "accounts", "categories", "transactions", "budgets", "recurring_transactions", "ai_product_logs", "ai_scan_logs", "transaction_images"]:
        t_info = DB_TABLES_DATA[t_key]
        add_table_custom(doc, t_info["caption"], t_info["headers"], t_info["rows"], t_info["widths"])
        
    add_h2(doc, "2.5. Thiết kế giao diện người dùng (Obsidian Dark Theme)")
    add_p(doc, "Toàn bộ hệ thống giao diện người dùng được thiết kế tỉ mỉ theo ngôn ngữ Obsidian Dark Theme hiện đại, sử dụng bảng màu tối sang trọng (#121212 cho nền chính, #1E1E1E cho thẻ bề mặt, #4CAF50 cho dòng tiền thu và #E53935 cho chi phí). Dưới đây là 10 màn hình giao diện chính của ứng dụng:")
    add_image_note(doc, "Giao diện màn hình Đăng nhập (Email, Mật khẩu, Google Sign-In) và Đăng ký tài khoản mới.", "Hình 2.13: Thiết kế giao diện Đăng nhập và Đăng ký (Obsidian Dark Theme)")
    add_image_note(doc, "Giao diện màn hình Trang chủ (Dashboard) hiển thị tổng số dư, tổng thu, tổng chi và danh sách ví tài chính.", "Hình 2.14: Thiết kế giao diện Trang chủ Dashboard và Tổng quan số dư ví")
    add_image_note(doc, "Giao diện màn hình Lịch sử giao dịch thu chi dạng danh sách và chế độ xem chi tiết giao dịch theo từng ngày.", "Hình 2.15: Thiết kế giao diện Lịch sử giao dịch và Xem chi tiết theo ngày")
    add_image_note(doc, "Giao diện màn hình Thêm mới giao dịch thu chi thủ công: chọn loại, số tiền, danh mục, ví, ngày giờ và ghi chú.", "Hình 2.16: Thiết kế giao diện Thêm giao dịch thu chi thủ công")
    add_image_note(doc, "Giao diện màn hình Quét hóa đơn thông minh: chụp ảnh hóa đơn, hiển thị tiến trình AI bóc tách và điền tự động.", "Hình 2.17: Thiết kế giao diện Quét hóa đơn thông minh (OCR + Gemini AI)")
    add_image_note(doc, "Giao diện Camera AI nhận diện sản phẩm: camera thời gian thực vẽ khung nhận diện Bounding Box đè lên vật thể.", "Hình 2.18: Thiết kế giao diện Camera AI nhận diện sản phẩm (YOLOv8 Bounding Box)")
    add_image_note(doc, "Giao diện màn hình Quản lý ngân sách: danh sách hạn mức danh mục, thanh tiến độ đổi màu cảnh báo trực quan.", "Hình 2.19: Thiết kế giao diện Quản lý ngân sách cảnh báo đổi màu")
    add_image_note(doc, "Giao diện màn hình Quản lý giao dịch định kỳ: danh sách lịch lặp, tần suất, trạng thái kích hoạt và nút demo tức thì.", "Hình 2.20: Thiết kế giao diện Quản lý giao dịch định kỳ")
    add_image_note(doc, "Giao diện màn hình Báo cáo tài chính: biểu đồ tròn PieChart tỷ trọng chi tiêu và biểu đồ cột BarChart xu hướng qua các tháng.", "Hình 2.21: Thiết kế giao diện Báo cáo thống kê tài chính trực quan (MPAndroidChart)")
    add_image_note(doc, "Giao diện màn hình Hồ sơ cá nhân (Profile): thông tin tài khoản, đổi mật khẩu, đăng xuất an toàn.", "Hình 2.22: Thiết kế giao diện Hồ sơ cá nhân (Profile)")
    
    add_h2(doc, "2.6. Tổng kết chương 2")
    add_p(doc, "Chương 2 đã hoàn thành toàn diện việc phân tích yêu cầu nghiệp vụ và thiết kế chi tiết hệ thống: từ bảng phân tích yêu cầu chức năng/phi chức năng, biểu đồ BFC, sơ đồ Use Case, kiến trúc hệ thống 3 tầng phân tán, pipeline AI 2 giai đoạn độc đáo, các sơ đồ tuần tự các luồng chính, thiết kế cơ sở dữ liệu 9 bảng chuẩn 3NF đến thiết kế 10 màn hình giao diện Obsidian Dark Theme. Đây là bản thiết kế hoàn chỉnh làm kim chỉ nam cho quá trình cài đặt và thực nghiệm trong Chương 3.")

print("Chapter 2 builder ready.")

def build_chapter3(doc):
    """Builds Chapter 3: Cài đặt và thực nghiệm hệ thống."""
    add_h1(doc, "CHƯƠNG 3: CÀI ĐẶT VÀ THỰC NGHIỆM HỆ THỐNG")
    
    add_h2(doc, "3.1. Môi trường phát triển và công cụ cài đặt")
    add_p(doc, "Quá trình xây dựng và thực nghiệm hệ thống Smart Personal Finance App được thực hiện trên môi trường phát triển chuẩn hóa:")
    add_bullet(doc, "Hệ điều hành Windows 11 Pro 64-bit, vi xử lý Intel Core i7 đa nhân, bộ nhớ trong 16GB RAM, ổ cứng thể rắn SSD NVMe 512GB.", "Phần cứng phát triển")
    add_bullet(doc, "Android Studio (phiên bản Iguana / Koala / Ladybug), IntelliJ IDEA Ultimate 2024, Visual Studio Code, Postman, MySQL Workbench và Docker Desktop.", "Môi trường lập trình (IDE)")
    add_bullet(doc, "Android SDK 34 (Android 14), Kotlin Compiler 1.9.x, Android Gradle Plugin 8.4+, Java Development Kit (JDK) 21 LTS (Oracle / Eclipse Temurin), Apache Maven 3.9+.", "Bộ công cụ biên dịch")
    add_image_note(doc, "Cấu trúc thư mục mã nguồn ứng dụng di động Android (Kotlin) theo chuẩn kiến trúc MVVM.", "Hình 3.1: Cấu trúc thư mục mã nguồn dự án Android (Kotlin)")
    add_image_note(doc, "Cấu trúc thư mục mã nguồn dự án máy chủ Backend Spring Boot 3 và Maven.", "Hình 3.2: Cấu trúc thư mục mã nguồn dự án Backend (Spring Boot 3)")
    
    add_h2(doc, "3.2. Cài đặt cơ sở dữ liệu và kết nối HikariCP")
    add_p(doc, "Cơ sở dữ liệu MySQL 8.0 được khởi tạo với tập lệnh SQL chuẩn hóa, tạo lập đầy đủ 9 bảng, các ràng buộc toàn vẹn khóa ngoại ON DELETE CASCADE và các chỉ mục tối ưu hóa truy vấn. Hệ thống nạp sẵn (seed data) dữ liệu danh mục mặc định chuẩn tiếng Việt có dấu (Ăn uống, Di chuyển, Mua sắm, Hóa đơn & Tiện ích, Giải trí, Y tế & Sức khỏe...).")
    add_p(doc, "Trong tệp application.properties, cấu hình kết nối JDBC qua HikariCP được tối ưu triệt để: useUnicode=true&characterEncoding=UTF-8, kích thước vùng kết nối tối đa maximum-pool-size=10, thời gian chờ kết nối connection-timeout=20000ms, đảm bảo thông suốt ngay cả khi có nhiều request đồng thời.")
    add_image_note(doc, "Tệp cấu hình kết nối CSDL MySQL qua HikariCP và thiết lập bộ mã utf8mb4 trong Spring Boot application.properties.", "Hình 3.3: Cấu hình kết nối MySQL HikariCP và bộ mã utf8mb4")
    
    add_h2(doc, "3.3. Xây dựng dịch vụ máy chủ Backend RESTful API")
    add_p(doc, "Máy chủ Spring Boot 3 được lập trình hoàn thiện với 11 REST Controllers, cung cấp hơn 30 endpoint chuẩn hóa:")
    add_bullet(doc, "Bảo vệ toàn bộ các endpoint nghiệp vụ bằng cách chặn và giải mã Firebase ID Token từ tiêu đề Bearer Token.", "FirebaseAuthFilter")
    add_bullet(doc, "Xử lý kiểm tra dữ liệu đầu vào (@Valid DTOs) và bắt giữ mọi ngoại lệ (ResourceNotFoundException, BadRequestException) trả về JSON ApiResponse đồng nhất.", "GlobalExceptionHandler")
    add_bullet(doc, "Tích hợp công cụ tài liệu hóa API tự động, cho phép các lập trình viên thử nghiệm các endpoint trực tiếp trên giao diện trình duyệt trực quan.", "Swagger UI / OpenAPI 3")
    add_image_note(doc, "Mã nguồn lớp lọc bảo mật FirebaseAuthFilter xác thực Firebase ID Token từ Google.", "Hình 3.4: Lớp lọc bảo mật FirebaseAuthFilter xác thực Firebase ID Token")
    add_image_note(doc, "Giao diện Swagger UI tài liệu hóa toàn bộ 11 Controller và danh sách endpoint của hệ thống Backend.", "Hình 3.5: Giao diện Swagger UI tài liệu hóa hệ thống RESTful API")
    
    add_h2(doc, "3.4. Xây dựng ứng dụng di động Android Native")
    add_p(doc, "Ứng dụng Android được phát triển trên ngôn ngữ Kotlin hiện đại, áp dụng đầy đủ các mẫu thiết kế và kỹ thuật tối ưu:")
    add_bullet(doc, "Toàn bộ các tác vụ mạng (Retrofit 2), giải mã chứng từ và phân tích hình ảnh được chuyển đổi từ luồng Java cổ điển sang Kotlin Coroutines với viewLifecycleOwner.lifecycleScope và withContext(Dispatchers.IO), triệt tiêu hoàn toàn nguy cơ rò rỉ bộ nhớ khi đóng màn hình.", "Kotlin Coroutines Concurrency")
    add_bullet(doc, "Các phân mảnh giao diện (Home, Transaction, Budget, Category, Account, Recurring, Profile) được quản lý qua Navigation Graph, hỗ trợ chuyển cảnh mượt mà.", "Navigation Component")
    add_bullet(doc, "Tùy biến bảng màu Obsidian Dark Theme cao cấp trên toàn bộ các MaterialCardView, RecyclerView, BottomNavigationView và Dialog, mang lại cảm giác sang trọng và chuyên nghiệp.", "Giao diện Obsidian Dark Theme")
    
    add_h2(doc, "3.5. Xây dựng và thực nghiệm các module Trí tuệ Nhân tạo")
    add_h3(doc, "3.5.1. Huấn luyện và chuyển đổi YOLOv8n sang TFLite On-Device")
    add_p(doc, "Tập dữ liệu huấn luyện YOLOv8 bao gồm 5.400 hình ảnh chụp thực tế tại các siêu thị, cửa hàng tiện lợi và quán cà phê tại Việt Nam, bao phủ 18 lớp mặt hàng phổ biến: coffee_cup, milk_tea, fastfood, bread, snacks, clothes, shoes, bag, smartphone, laptop, electronic_item, medicine, cosmetic, helmet, fuel, utility_bill, book, general_item. Tập dữ liệu được chia theo tỷ lệ chuẩn 70% Train, 20% Validation và 10% Test.")
    
    yolo_params_headers = ["Tham số huấn luyện", "Giá trị thiết lập", "Ý nghĩa kỹ thuật"]
    yolo_params_data = [
        ["Model Architecture", "YOLOv8n (Nano)", "Phiên bản nhẹ nhất với 3.2 triệu tham số, tối ưu cho thiết bị di động"],
        ["Input Image Size", "640 x 640 pixels", "Kích thước đầu vào chuẩn cân bằng giữa độ phân giải và tốc độ"],
        ["Epochs", "100", "Số lượt duyệt toàn bộ tập dữ liệu huấn luyện"],
        ["Batch Size", "16", "Kích thước lô mẫu dữ liệu trên GPU"],
        ["Optimizer", "SGD with Momentum (0.937)", "Thuật toán tối ưu hóa đạo hàm ngẫu nhiên có đà"],
        ["Initial Learning Rate (lr0)", "0.01", "Tốc độ học khởi tạo"],
        ["Weight Decay", "0.0005", "Hệ số suy giảm trọng số ngăn chặn quá khớp"],
    ]
    add_table_custom(doc, "Bảng 3.1: Các thông số cấu hình siêu tham số mô hình YOLOv8n", yolo_params_headers, yolo_params_data, [2.2, 1.8, 2.6])
    
    add_p(doc, "Kết quả thực nghiệm sau 100 epochs huấn luyện đạt kết quả rất ấn tượng: mAP@0.5 đạt 87.6%, Precision đạt 89.2%, Recall đạt 84.5%. Mô hình PyTorch .pt sau đó được xuất sang định dạng TensorFlow Lite (Float32) với dung lượng chỉ 6.3 MB. Khi nạp vào ứng dụng di động qua CameraX, mô hình đạt tốc độ suy luận thời gian thực từ 34 đến 42 FPS trên chip vi xử lý tầm trung.")
    add_image_note(doc, "Giao diện quá trình huấn luyện mô hình YOLOv8n trên nền tảng Google Colab / GPU máy trạm.", "Hình 3.6: Quá trình huấn luyện mô hình YOLOv8n trên tập dữ liệu sản phẩm")
    add_image_note(doc, "Biểu đồ đường cong hàm mất mát (Box/Cls Loss) và độ chính xác mAP@0.5 qua 100 epochs.", "Hình 3.7: Biểu đồ hàm mất mát (Loss) và độ chính xác mAP@0.5 của YOLOv8n")
    
    add_h3(doc, "3.5.2. Huấn luyện Random Forest 200 cây và nhúng Microsoft ONNX Runtime Java")
    add_p(doc, "Từ kết quả nhận diện của YOLO, hệ thống trích xuất vector 27 đặc trưng kỹ thuật đa chiều làm đầu vào cho mô hình Random Forest:")
    
    features_headers = ["Nhóm đặc trưng", "Số lượng", "Danh sách các đặc trưng kỹ thuật", "Mục đích phân loại"]
    features_data = [
        ["Nhị phân (Binary)", "18", "has_coffee_cup, has_milk_tea, has_fastfood, has_bread, has_snacks, has_clothes, has_shoes, has_bag, has_smartphone, has_laptop, has_electronic_item, has_medicine, has_cosmetic, has_helmet, has_fuel, has_utility_bill, has_book, has_general_item", "Xác định sự hiện diện của từng mặt hàng cụ thể"],
        ["Nhóm đếm (Counts)", "5", "food_drink_count, transport_count, shopping_count, entertainment_count, health_count", "Đo lường tần suất xuất hiện theo từng nhóm hàng"],
        ["Chất lượng (Quality)", "4", "total_objects, max_confidence, avg_confidence, low_confidence_count", "Đánh giá chất lượng và độ tin cậy của khung hình"],
    ]
    add_table_custom(doc, "Bảng 3.2: 27 đặc trưng kỹ thuật phân loại danh mục chi tiêu", features_headers, features_data, [1.5, 0.8, 2.8, 1.7])
    
    add_p(doc, "Mô hình Random Forest gồm 200 cây quyết định được huấn luyện trên 12.000 mẫu vector đặc trưng, đạt độ chính xác kiểm thử 94.21% và chỉ số F1-Score Macro đạt 0.938. Mô hình được xuất thành tệp random_forest_model.onnx (768 KB) và nhúng trực tiếp vào Spring Boot qua Microsoft ONNX Runtime Java. Thời gian suy luận trên CPU máy chủ đạt từ 1.2 đến 1.8 ms, nhanh gấp 50 lần so với việc dựng một máy chủ Python riêng biệt.")
    add_image_note(doc, "Biểu đồ ma trận nhầm lẫn (Confusion Matrix) đánh giá độ chính xác phân loại của mô hình Random Forest.", "Hình 3.8: Biểu đồ ma trận nhầm lẫn (Confusion Matrix) mô hình Random Forest")
    add_image_note(doc, "Đoạn mã nguồn Java khởi tạo OrtEnvironment và thực thi suy luận mô hình ONNX trong ProductClassifierService.", "Hình 3.9: Xuất mô hình Random Forest sang định dạng ONNX Runtime Java")
    
    add_h3(doc, "3.5.3. Tích hợp quét hóa đơn Google ML Kit OCR + Google Gemini 2.5 Flash")
    add_p(doc, "Quy trình quét hóa đơn được phối hợp nhịp nhàng giữa On-Device OCR và Cloud LLM: Google ML Kit trên Android trích xuất toàn bộ văn bản thô trong 400ms. Đoạn text này được gửi lên Backend và chuyển tiếp tới Google Gemini 2.5 Flash thông qua cấu hình JSON Schema nghiêm ngặt. Gemini phân tích ngữ cảnh và trả về đúng 4 trường thông tin (merchant, amount, date, category) trong thời gian trung bình 2.45 giây với độ chính xác bóc tách đạt 96.8% trên tập 300 hóa đơn thử nghiệm.")
    add_image_note(doc, "Mã nguồn lớp GeminiService cấu hình responseSchema JSON và gọi API Google Gemini 2.5 Flash.", "Hình 3.10: Tích hợp Google Gemini 2.5 Flash bóc tách hóa đơn JSON")
    
    add_h2(doc, "3.6. Kiểm thử hệ thống và tối ưu hóa hiệu năng")
    add_h3(doc, "3.6.1. Kiểm thử tự động Backend (Automated Testing Suite)")
    add_p(doc, "Để đảm bảo độ tin cậy tuyệt đối cho hệ thống tài chính, toàn bộ các lớp Controller và Service đều được bao phủ bởi các bài kiểm thử tự động với JUnit 5, Mockito và Spring MockMvc. Kết quả thực thi lệnh mvn test thành công xuất sắc: 86/86 Tests passed (100%), 0 failures, 0 errors trong thời gian 8.8 giây:")
    
    add_table_custom(doc, "Bảng 3.3: Tổng hợp kết quả kiểm thử tự động Backend (86/86 Tests passed)", ["STT", "Lớp kiểm thử", "Số test", "Passed", "Failed", "Errors", "Tỷ lệ", "Mô tả phạm vi kiểm thử"], TEST_CASES_SUMMARY, [0.5, 2.0, 0.6, 0.6, 0.5, 0.5, 0.6, 1.8])
    add_image_note(doc, "Màn hình kết quả chạy kiểm thử tự động mvn test đạt BUILD SUCCESS 86/86 bài kiểm thử thành công.", "Hình 3.11: Kết quả thực thi kiểm thử tự động 86/86 Test cases passed")
    
    add_h3(doc, "3.6.2. Kiểm thử chức năng ứng dụng di động Android")
    add_p(doc, "Ứng dụng Android được kiểm thử chức năng toàn diện trên cả thiết bị giả lập (AVD Android 14 API 34) và thiết bị thật (Xiaomi, Samsung Galaxy):")
    add_bullet(doc, "Đăng nhập Google Sign-In hoàn tất trong 1.2s; đăng ký Email tự động đồng bộ CSDL MySQL.", "Chức năng xác thực")
    add_bullet(doc, "Thêm, sửa, xóa giao dịch cập nhật số dư ví tức thì; biểu đồ thu chi tự động vẽ lại chính xác.", "Chức năng giao dịch")
    add_bullet(doc, "Quét hóa đơn bóc tách chính xác các hóa đơn Highlands, WinMart, Grab; tự động điền form không lỗi.", "Chức năng OCR AI")
    add_bullet(doc, "Camera AI nhận diện chính xác các sản phẩm đặt trước camera, vẽ bounding box mượt mà không drop frame.", "Chức năng Camera AI")
    
    add_h3(doc, "3.6.3. Tối ưu hóa hiệu năng chuyên sâu")
    add_p(doc, "Trong quá trình thực nghiệm, hệ thống đã được tối ưu hóa chuyên sâu với 4 cải tiến kỹ thuật nổi bật:")
    add_bullet(doc, "Toàn bộ tác vụ đa luồng được đưa vào LifecycleScope của Coroutine, tự động hủy luồng khi thoát màn hình, giảm mức tiêu thụ RAM từ 180MB xuống 110MB.", "Coroutines Lifecycle-Aware")
    add_bullet(doc, "Loại bỏ hoàn toàn Facebook SDK thừa thãi, giảm kích thước file APK xuất xưởng từ 28MB xuống 23.5MB, tăng tốc khởi động ứng dụng thêm 35%.", "Lean APK Footprint")
    add_bullet(doc, "Lược bỏ các request gửi log feedback ngầm sau quét AI, chuyển sang ghi nhận giao dịch 1 bước trực tiếp, giảm độ trễ lưu giao dịch từ 850ms xuống 120ms.", "Direct Transaction Commit")
    add_bullet(doc, "Chuẩn hóa cấu hình AVD về kích thước trang bộ nhớ 4KB, giải quyết triệt để lỗi từ chối thư viện C++ native libtensorflowlite_jni.so của TFLite trên các bản Android 16 thử nghiệm.", "Tương thích 4KB Page Size")
    add_image_note(doc, "Biểu đồ Android Profiler theo dõi mức sử dụng CPU, RAM và Network của ứng dụng di động khi đang chạy AI.", "Hình 3.12: Đo kiểm hiệu năng và mức tiêu thụ tài nguyên CPU/RAM trên thiết bị thật")
    
    add_h2(doc, "3.7. Tổng kết chương 3")
    add_p(doc, "Chương 3 đã trình bày toàn diện quá trình cài đặt, hiện thực hóa mã nguồn và kết quả thực nghiệm hệ thống. Các kết quả đo kiểm định lượng khẳng định hệ thống vận hành cực kỳ ổn định, chính xác và có hiệu năng cao: nhận diện YOLOv8 đạt 34–42 FPS, suy luận ONNX dưới 2ms, bóc tách hóa đơn dưới 2.5s và vượt qua 100% bộ kiểm thử tự động 86 bài test. Đây là minh chứng rõ nét cho chất lượng kỹ thuật phần mềm của đồ án.")

def build_chapter4(doc):
    """Builds Chapter 4: Kết luận và hướng phát triển."""
    add_h1(doc, "CHƯƠNG 4: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN")
    
    add_h2(doc, "4.1. Đánh giá kết quả đạt được đối chiếu với mục tiêu ban đầu")
    add_p(doc, "Sau 15 tuần nghiên cứu, thiết kế và phát triển bám sát lộ trình Kế hoạch đồ án tốt nghiệp, đề tài đã hoàn thành xuất sắc 100% các mục tiêu đề ra ban đầu:")
    
    eval_headers = ["STT", "Mục tiêu cụ thể theo đề cương", "Kết quả thực hiện thực tế trong đồ án", "Đánh giá"]
    add_table_custom(doc, "Bảng 4.1: Bảng đối chiếu kết quả thực hiện so với mục tiêu đề cương ban đầu", eval_headers, EVALUATION_TABLE, [0.6, 2.2, 3.2, 1.0])
    
    add_h2(doc, "4.2. Đóng góp khoa học và thực tiễn của đề tài")
    add_p(doc, "1. Đóng góp về mặt phương pháp luận và kỹ thuật:")
    add_bullet(doc, "Chứng minh tính khả thi và ưu việt vượt trội của kiến trúc Hybrid AI (Edge Computing kết hợp Cloud AI) trên thiết bị di động. Việc đưa mô hình nhận diện vật thể nhẹ (YOLOv8n TFLite) và OCR cục bộ (ML Kit) xuống chạy trực tiếp tại vi xử lý điện thoại giúp tiết kiệm tối đa băng thông, giảm chi phí máy chủ và mang lại tốc độ phản hồi tức thì.", "Kiến trúc Hybrid AI")
    add_bullet(doc, "Đề xuất quy trình tích hợp máy học đa nền tảng thông qua chuẩn mở ONNX, cho phép quy trình Data Science thực hiện linh hoạt trên Python, trong khi khâu triển khai dịch vụ thực hiện nguyên khối trên Java Spring Boot mà không đánh đổi hiệu năng.", "Tích hợp ONNX Runtime Java")
    add_p(doc, "2. Đóng góp về mặt thực tiễn đời sống:")
    add_bullet(doc, "Cung cấp một giải pháp quản lý tài chính cá nhân hoàn toàn miễn phí, thông minh, an toàn và tối ưu cho người dùng Việt Nam.", "Ứng dụng thực tế")
    add_bullet(doc, "Giải quyết triệt để rào cản lớn nhất khiến người dùng từ bỏ thói quen ghi chép tài chính: sự phiền toái của việc nhập liệu thủ công, biến thao tác ghi sổ thành trải nghiệm chụp ảnh thông minh 1 chạm đầy hứng khởi.", "Nâng cao trải nghiệm người dùng")
    
    add_h2(doc, "4.3. Những hạn chế và khó khăn còn tồn tại")
    add_p(doc, "Bên cạnh các kết quả tích cực, đồ án còn một số hạn chế nhất định do điều kiện thời gian và tài nguyên thực nghiệm:")
    add_bullet(doc, "Ứng dụng hiện tại chỉ mới phát triển dành riêng cho hệ điều hành Android (Android Native), chưa thể tiếp cận nhóm người dùng sử dụng hệ điều hành iOS.", "Giới hạn nền tảng")
    add_bullet(doc, "Mô hình YOLOv8 hiện tập trung nhận diện 18 lớp mặt hàng mua sắm phổ biến. Đối với các mặt hàng quá đặc thù hoặc có kích thước quá nhỏ, độ tin cậy nhận diện còn có thể bị giảm.", "Phạm vi nhận diện vật thể")
    add_bullet(doc, "Google ML Kit OCR đạt độ chính xác cao trên hóa đơn in nhiệt từ máy POS; tuy nhiên đối với các hóa đơn viết tay bằng bút mực hoặc hóa đơn nhàu nát mờ mực nghiêm trọng, tỷ lệ trích xuất đúng còn hạn chế.", "Hóa đơn viết tay")
    add_bullet(doc, "Chức năng bóc tách hóa đơn thông minh cần kết nối Internet để gửi văn bản lên dịch vụ Google Gemini Cloud API. Khi thiết bị ngoại tuyến, người dùng chỉ có thể sử dụng văn bản OCR thô.", "Phụ thuộc kết nối đám mây")
    
    add_h2(doc, "4.4. Hướng phát triển và mở rộng trong tương lai")
    add_p(doc, "Dựa trên nền tảng kiến trúc vững chắc đã xây dựng, đề tài có thể tiếp tục mở rộng theo 4 định hướng chiến lược:")
    add_bullet(doc, "Chuyển đổi tầng Data/Domain sang Kotlin Multiplatform kết hợp Compose Multiplatform để phát hành phiên bản đồng thời cho cả iOS và Web, tái sử dụng hơn 70% mã nguồn.", "Mở rộng đa nền tảng (KMP)")
    add_bullet(doc, "Tự động nhận diện mã QR chuyển khoản và kết nối Open Banking API theo định hướng của Ngân hàng Nhà nước để đồng bộ biến động số dư ngân hàng tự động.", "Tích hợp VietQR & Open Banking")
    add_bullet(doc, "Nghiên cứu triển khai các mô hình ngôn ngữ nhỏ (như Gemma 2B, Phi-3 Mini) lượng tử hóa 4-bit chạy trực tiếp trên NPU của điện thoại, cho phép bóc tách hóa đơn và tư vấn tài chính hoàn toàn Offline và bảo mật tuyệt đối.", "On-Device SLM tiếng Việt")
    add_bullet(doc, "Ứng dụng công nghệ RAG (Retrieval-Augmented Generation) phân tích lịch sử chi tiêu để đưa ra các lời khuyên tiết kiệm ngân sách và gợi ý kế hoạch đầu tư tài chính thông minh theo từng cá nhân.", "Trợ lý ảo tài chính (Financial Copilot)")
    
    add_h2(doc, "4.5. Lời kết luận")
    add_p(doc, "Đề tài \"Xây dựng ứng dụng quản lý tài chính cá nhân trên nền tảng Android tích hợp nhận diện và phân loại chi tiêu bằng Trí tuệ Nhân tạo\" đã hoàn thành trọn vẹn mọi nội dung cam kết trong Kế hoạch đồ án tốt nghiệp. Hệ thống phần mềm đã được hiện thực hóa hoàn chỉnh, kết hợp nhuần nhuyễn giữa kỹ thuật phát triển ứng dụng di động hiện đại và các công nghệ Trí tuệ Nhân tạo tiên tiến nhất hiện nay.")
    add_p(doc, "Kết quả của đề tài không chỉ là một sản phẩm phần mềm có tính ứng dụng thực tiễn cao phục vụ cộng đồng, mà còn là minh chứng rõ nét cho năng lực làm chủ công nghệ, tư duy thiết kế hệ thống và khả năng giải quyết các bài toán kỹ thuật phức tạp của sinh viên trong suốt quá trình học tập tại Khoa Công nghệ Thông tin – Trường Đại học Kiến trúc Hà Nội.")

def build_appendix(doc):
    """Builds Appendix."""
    add_h1(doc, "PHỤ LỤC: HƯỚNG DẪN CÀI ĐẶT VÀ KHỞI CHẠY HỆ THỐNG")
    
    add_h2(doc, "Phụ lục 1: Yêu cầu môi trường hệ thống")
    add_bullet(doc, "Hệ điều hành: Windows 10/11 64-bit, macOS 12+ hoặc Ubuntu 22.04 LTS.", "Hệ điều hành")
    add_bullet(doc, "Bộ nhớ RAM tối thiểu: 8GB (khuyến nghị 16GB để chạy đồng thời Docker và Android Emulator).", "Bộ nhớ trong")
    add_bullet(doc, "Phần mềm bắt buộc: Docker Desktop, JDK 21 LTS, Android Studio (Ladybug hoặc mới hơn), Git.", "Phần mềm cần thiết")
    
    add_h2(doc, "Phụ lục 2: Khởi chạy máy chủ Backend và Cơ sở dữ liệu bằng Docker")
    add_p(doc, "1. Mở cửa sổ dòng lệnh Terminal hoặc PowerShell tại thư mục gốc của dự án:")
    add_p(doc, "   cd d:\\Project\\KHMT\\DATN", style="Normal", italic=True)
    add_p(doc, "2. Khởi chạy toàn bộ hệ thống bằng Docker Compose:")
    add_p(doc, "   docker compose up -d --build", style="Normal", italic=True)
    add_p(doc, "3. Kiểm tra trạng thái hoạt động của các container:")
    add_p(doc, "   docker compose ps", style="Normal", italic=True)
    add_p(doc, "   - Máy chủ MySQL lắng nghe tại cổng 3306 (user: finance_user, database: personal_finance_app).")
    add_p(doc, "   - Máy chủ Spring Boot Backend lắng nghe tại cổng 8080.")
    add_p(doc, "4. Kiểm tra tài liệu Swagger UI trên trình duyệt: http://localhost:8080/swagger-ui.html")
    
    add_h2(doc, "Phụ lục 3: Cấu hình và khởi chạy ứng dụng Android")
    add_p(doc, "1. Khởi động Android Studio, chọn Open và dẫn tới thư mục: android-app/")
    add_p(doc, "2. Chờ Android Studio đồng bộ các thư viện Gradle (Gradle Sync thành công).")
    add_p(doc, "3. Khởi tạo thiết bị máy ảo Android Virtual Device (AVD):")
    add_p(doc, "   - Khuyến nghị chọn System Image Android 14 (API 34) với kiến trúc bộ nhớ chuẩn 4KB.")
    add_p(doc, "   - Máy ảo đã được cấu hình mặc định trỏ về máy chủ Docker qua IP cầu nối: http://10.0.2.2:8080/")
    add_p(doc, "4. Nhấn nút Run ▶️ trên thanh công cụ để cài đặt và khởi chạy ứng dụng.")
    add_p(doc, "5. Trường hợp chạy trên điện thoại thật qua Wi-Fi:")
    add_p(doc, "   - Tại màn hình Đăng nhập của ứng dụng, nhấn giữ vào Logo ứng dụng khoảng 2 giây.")
    add_p(doc, "   - Nhập địa chỉ IP Wi-Fi của máy tính (ví dụ: 192.168.1.125:8080) và bấm Lưu.")

def build_references(doc):
    """Builds References."""
    add_h1(doc, "TÀI LIỆU THAM KHẢO")
    for ref in REFERENCES_DATA:
        add_p(doc, ref, style="Normal", space_before=2, space_after=6, font_size=12)

print("All chapter builders defined successfully.")
