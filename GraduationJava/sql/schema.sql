CREATE DATABASE IF NOT EXISTS smart_forum
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smart_forum;

SET FOREIGN_KEY_CHECKS = 0;

-- ================= 用户表 =================
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色(user-普通用户,admin-管理员)',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态(active-正常,banned-封禁)',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- ================= 文章表 =================
CREATE TABLE IF NOT EXISTS `post` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id`    BIGINT       NOT NULL COMMENT '作者ID',
    `title`      VARCHAR(200) NOT NULL COMMENT '标题',
    `content`    LONGTEXT     NOT NULL COMMENT '内容',
    `summary`    TEXT         DEFAULT NULL COMMENT 'AI生成摘要',
    `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已删除',
    `is_top`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `view_count` INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    `daily_view_count` INT    NOT NULL DEFAULT 0 COMMENT '今日浏览量',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_top` (`is_top`),
    CONSTRAINT `fk_post_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';


-- ================= 评论表 =================
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `user_id`    BIGINT   NOT NULL COMMENT '评论者ID',
    `content`    TEXT     NOT NULL COMMENT '评论内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_comment_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';


-- ================= 点赞表 =================
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`),
    CONSTRAINT `fk_like_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_like_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';


-- ================= 收藏表 =================
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`),
    CONSTRAINT `fk_fav_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_fav_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏表';


-- ================= 关注表 =================
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id` BIGINT   NOT NULL COMMENT '关注者ID',
    `followed_id` BIGINT   NOT NULL COMMENT '被关注者ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_followed` (`follower_id`, `followed_id`),
    KEY `idx_followed_id` (`followed_id`),
    CONSTRAINT `fk_follow_follower`
        FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_follow_followed`
        FOREIGN KEY (`followed_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

CREATE TABLE `ai_chat_session` (
                                   `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                                   `user_id`    BIGINT       NOT NULL COMMENT '用户ID',
                                   `title`      VARCHAR(200) NOT NULL DEFAULT '新对话',
                                   `messages`   LONGTEXT     NOT NULL,
                                   `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE
                                               CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`id`),
                                   KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天会话表';


-- ================= 举报表 =================
CREATE TABLE IF NOT EXISTS `post_report` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '举报ID',
    `post_id`     BIGINT       NOT NULL COMMENT '被举报帖子ID',
    `reporter_id` BIGINT       NOT NULL COMMENT '举报人ID',
    `reason`      VARCHAR(50)  NOT NULL COMMENT '举报原因(spam-垃圾广告,porn-色情低俗,fake-虚假信息,abuse-辱骂攻击,other-其他)',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '举报补充说明',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '处理状态(pending-待处理,approved-举报成立,rejected-举报驳回)',
    `handle_note` VARCHAR(500) DEFAULT NULL COMMENT '管理员处理备注',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `handled_at`  DATETIME     DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reporter_post` (`reporter_id`, `post_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_report_post`
        FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_report_reporter`
        FOREIGN KEY (`reporter_id`) REFERENCES `user` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子举报表';


SET FOREIGN_KEY_CHECKS = 1;