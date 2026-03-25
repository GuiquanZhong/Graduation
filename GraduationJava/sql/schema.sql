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
    `role`       VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色(user-普通用户,admin-管理员)',
    `status`     VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态(active-正常,banned-封禁)',
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
    `is_top`     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否,1-是)',
    `view_count` INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    `daily_view_count` INT     NOT NULL DEFAULT 0 COMMENT '今日浏览量',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_is_top` (`is_top`)
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

-- =============== 迁移语句（已有数据库执行） ===============
-- ALTER TABLE `post` ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除(0-正常,1-已删除)' AFTER `summary`;
-- ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'user' COMMENT '角色(user-普通用户,admin-管理员)' AFTER `avatar`;
-- ALTER TABLE `user` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态(active-正常,banned-封禁)' AFTER `role`;
-- ALTER TABLE `post` ADD COLUMN `is_top` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否,1-是)' AFTER `is_deleted`;
-- ALTER TABLE `post` ADD COLUMN `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量' AFTER `is_top`;
-- ALTER TABLE `post` ADD COLUMN `daily_view_count` INT NOT NULL DEFAULT 0 COMMENT '今日浏览量' AFTER `view_count`;
-- ALTER TABLE `post` ADD INDEX `idx_is_top` (`is_top`);
-- UPDATE `user` SET `role` = 'admin' WHERE `id` = 1;  -- 将ID=1的用户设为管理员
