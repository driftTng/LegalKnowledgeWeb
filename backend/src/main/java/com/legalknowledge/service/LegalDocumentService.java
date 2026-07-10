package com.legalknowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.legalknowledge.dto.response.LegalDocumentDTO;
import com.legalknowledge.mapper.LegalDocumentMapper;
import com.legalknowledge.entity.LegalDocument;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LegalDocumentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final LegalDocumentMapper mapper;

    public LegalDocumentService(LegalDocumentMapper mapper) {
        this.mapper = mapper;
    }

    /** 全部法规，支持标题搜索和分类筛选，按发布日期倒序 */
    public List<LegalDocumentDTO> listAll(String keyword, String category) {
        LambdaQueryWrapper<LegalDocument> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            qw.like(LegalDocument::getTitle, keyword);
        }
        if (category != null && !category.isBlank()) {
            qw.eq(LegalDocument::getCategory, category);
        }
        qw.orderByDesc(LegalDocument::getPublishDate);
        return mapper.selectList(qw).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** 获取所有分类 */
    public List<String> getCategories() {
        return mapper.findAllCategories();
    }

    public LegalDocumentDTO getById(Long id) {
        LegalDocument doc = mapper.selectById(id);
        if (doc == null) throw new RuntimeException("法规不存在");
        return toDTO(doc);
    }

    // region 管理员操作

    public LegalDocumentDTO create(LegalDocumentDTO dto) {
        LegalDocument doc = toEntity(dto, new LegalDocument());
        mapper.insert(doc);
        return toDTO(doc);
    }

    public LegalDocumentDTO update(Long id, LegalDocumentDTO dto) {
        LegalDocument doc = mapper.selectById(id);
        if (doc == null) throw new RuntimeException("法规不存在");
        toEntity(dto, doc);
        mapper.updateById(doc);
        return toDTO(doc);
    }

    public void delete(Long id) {
        if (mapper.selectById(id) == null) throw new RuntimeException("法规不存在");
        mapper.deleteById(id);
    }

    private LegalDocument toEntity(LegalDocumentDTO dto, LegalDocument doc) {
        doc.setTitle(dto.getTitle());
        doc.setDocumentNumber(dto.getDocumentNumber());
        doc.setIssuingAuthority(dto.getIssuingAuthority());
        if (dto.getPublishDate() != null) doc.setPublishDate(java.time.LocalDate.parse(dto.getPublishDate()));
        if (dto.getEffectiveDate() != null) doc.setEffectiveDate(java.time.LocalDate.parse(dto.getEffectiveDate()));
        doc.setCategory(dto.getCategory());
        doc.setLevel(dto.getLevel());
        doc.setStatus(dto.getStatus());
        doc.setContent(dto.getContent());
        doc.setOutline(dto.getOutline());
        return doc;
    }

    // endregion

    private LegalDocumentDTO toDTO(LegalDocument doc) {
        LegalDocumentDTO dto = new LegalDocumentDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setDocumentNumber(doc.getDocumentNumber());
        dto.setIssuingAuthority(doc.getIssuingAuthority());
        dto.setPublishDate(doc.getPublishDate() != null ? doc.getPublishDate().format(DATE_FMT) : null);
        dto.setEffectiveDate(doc.getEffectiveDate() != null ? doc.getEffectiveDate().format(DATE_FMT) : null);
        dto.setCategory(doc.getCategory());
        dto.setLevel(doc.getLevel());
        dto.setStatus(doc.getStatus());
        dto.setContent(doc.getContent());
        dto.setOutline(doc.getOutline());
        dto.setCreatedAt(doc.getCreatedAt() != null ? doc.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        return dto;
    }
}
