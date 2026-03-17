-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_forum;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`     VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 文章表
CREATE TABLE IF NOT EXISTS `post` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `user_id`    BIGINT        NOT NULL COMMENT '作者ID',
    `title`      VARCHAR(200)  NOT NULL COMMENT '标题',
    `content`    LONGTEXT      NOT NULL COMMENT '内容',
    `summary`    TEXT          DEFAULT NULL COMMENT 'AI生成摘要',
    `is_deleted` TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否已删除(0-正常,1-已删除)',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `user_id`    BIGINT   NOT NULL COMMENT '评论者ID',
    `content`    TEXT     NOT NULL COMMENT '评论内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `post_like` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`    BIGINT   NOT NULL COMMENT '文章ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏表';

-- 关注表
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id` BIGINT   NOT NULL COMMENT '关注者ID',
    `followed_id` BIGINT   NOT NULL COMMENT '被关注者ID',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_followed` (`follower_id`, `followed_id`),
    KEY `idx_followed_id` (`followed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- 若已存在旧版post表，执行以下迁移语句添加逻辑删除字段（新建数据库无需执行）
-- ALTER TABLE `post` ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0-正常,1-已删除)' AFTER `summary`;
