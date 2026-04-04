package com.smartforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 关联查询时填充的评论者昵称，非数据库字段 */
    @TableField(exist = false)
    private String authorName;

    /** 关联查询时填充的评论者头像，非数据库字段 */
    @TableField(exist = false)
    private String authorAvatar;

    /** 关联查询时填充的帖子标题，非数据库字段 */
    @TableField(exist = false)
    private String postTitle;
}
