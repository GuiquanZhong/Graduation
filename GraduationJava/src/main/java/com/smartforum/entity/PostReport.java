package com.smartforum.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post_report")
public class PostReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long reporterId;

    private String reason;

    private String description;

    private String status;

    private String handleNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    @TableField(exist = false)
    private String reporterName;

    @TableField(exist = false)
    private String postTitle;

    @TableField(exist = false)
    private String reporterAvatar;
}
