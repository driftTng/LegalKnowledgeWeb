package com.legalknowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legalknowledge.entity.LegalDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LegalDocumentMapper extends BaseMapper<LegalDocument> {

    /** 获取所有分类（去重） */
    @Select("SELECT DISTINCT category FROM legal_documents ORDER BY category")
    List<String> findAllCategories();
}
