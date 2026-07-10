package com.legalknowledge.service;

import com.legalknowledge.dto.response.LegalDocumentDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PdfService {

    public byte[] generatePdf(LegalDocumentDTO doc) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // 中文字体
            BaseFont bf = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bf, 20, Font.BOLD);
            Font metaFont = new Font(bf, 11, Font.NORMAL);
            Font contentFont = new Font(bf, 13, Font.NORMAL);

            // 标题
            Paragraph title = new Paragraph(doc.getTitle(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(16);
            document.add(title);

            // 元信息
            StringBuilder meta = new StringBuilder();
            if (doc.getDocumentNumber() != null) meta.append("文号：").append(doc.getDocumentNumber()).append("    ");
            if (doc.getIssuingAuthority() != null) meta.append("发布机关：").append(doc.getIssuingAuthority()).append("\n");
            if (doc.getPublishDate() != null) meta.append("发布日期：").append(doc.getPublishDate()).append("    ");
            if (doc.getEffectiveDate() != null) meta.append("实施日期：").append(doc.getEffectiveDate()).append("\n");
            if (doc.getStatus() != null) meta.append("时效性：").append(doc.getStatus()).append("    ");
            if (doc.getCategory() != null) meta.append("分类：").append(doc.getCategory());

            Paragraph metaPara = new Paragraph(meta.toString(), metaFont);
            metaPara.setSpacingAfter(20);
            document.add(metaPara);

            // 分隔线
            document.add(new Paragraph("　　＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿", metaFont));
            document.add(Chunk.NEWLINE);

            // 正文
            String[] lines = doc.getContent().split("\n");
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    document.add(Chunk.NEWLINE);
                } else {
                    Paragraph p = new Paragraph("　　" + trimmed, contentFont);
                    p.setSpacingAfter(6);
                    p.setLeading(24);
                    document.add(p);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF 生成失败: " + e.getMessage(), e);
        }
    }
}
