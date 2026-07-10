package com.legalknowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.legalknowledge.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 自带 CRUD：insert / updateById / selectById / selectList / deleteById ...
    // 复杂 SQL 在这里加 @Select / @Update 或写 XML 文件
}
