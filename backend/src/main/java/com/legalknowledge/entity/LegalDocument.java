package com.legalknowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("legal_documents")
public class LegalDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 法规名称 */
    private String title;

    /** 文号，如"主席令第28号" */
    @TableField("document_number")
    private String documentNumber;

    /** 发布机关，如"全国人民代表大会常务委员会" */
    @TableField("issuing_authority")
    private String issuingAuthority;

    /** 发布日期 */
    @TableField("publish_date")
    private LocalDate publishDate;

    /** 实施日期 */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /** 法规分类 */
    private String category;

    /** 法规层级：法律、行政法规、司法解释、部门规章 */
    private String level;

    /** 时效性：现行有效、已修改、已废止 */
    private String status;

    /** 法规全文 */
    private String content;

    /** 目录/章节结构，JSON 数组 */
    private String outline;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }
    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOutline() { return outline; }
    public void setOutline(String outline) { this.outline = outline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
