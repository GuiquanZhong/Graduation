package com.smartforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartforum.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
