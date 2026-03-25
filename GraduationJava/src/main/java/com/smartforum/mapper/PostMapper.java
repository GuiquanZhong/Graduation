package com.smartforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartforum.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Update("UPDATE post SET daily_view_count = 0")
    void resetDailyViewCount();
}
