package com.smartforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String summary;

    /** 逻辑删除标志（0-正常，1-已删除） */
    @TableLogic
    private Integer isDeleted;

    /** 是否置顶（0-否，1-是） */
    private Integer isTop;

    /** 浏览量 */
    private Integer viewCount;

    /** 今日浏览量 */
    private Integer dailyViewCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 关联查询时填充的作者昵称，非数据库字段 */
    @TableField(exist = false)
    private String authorName;

    /** 关联查询时填充的作者头像，非数据库字段 */
    @TableField(exist = false)
    private String authorAvatar;

    /** 点赞数 */
    @TableField(exist = false)
    private Long likeCount;

    /** 收藏数 */
    @TableField(exist = false)
    private Long favoriteCount;

    /** 评论数 */
    @TableField(exist = false)
    private Long commentCount;

    /** 当前用户是否已点赞 */
    @TableField(exist = false)
    private Boolean isLiked;

    /** 当前用户是否已收藏 */
    @TableField(exist = false)
    private Boolean isFavorited;

    /** 热度分值（虚拟字段，用于排序） */
    @TableField(exist = false)
    private Double hotScore;
}

