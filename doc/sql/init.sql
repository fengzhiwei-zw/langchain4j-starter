-- ================================================
-- 数据库初始化脚本
-- 对应实体：User、AiAuditLog
-- ================================================

CREATE DATABASE IF NOT EXISTS ai_mysql
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ai_mysql;

-- ------------------------------------------------
-- 用户表
-- 对应实体：com.feng.langchain4jstarter.pojo.User
-- ------------------------------------------------
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名（登录账号）',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname`    VARCHAR(64)  NOT NULL COMMENT '昵称',
    `email`       VARCHAR(128) NOT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（0=正常，1=禁用）',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0=未删除，1=已删除）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status_deleted` (`status`, `is_deleted`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '用户表';


-- ------------------------------------------------
-- AI 审计日志表
-- 对应实体：com.feng.langchain4jstarter.pojo.AiAuditLog
-- ------------------------------------------------
CREATE TABLE IF NOT EXISTS `ai_audit_log`
(
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT            DEFAULT NULL COMMENT '发起请求的用户ID',
    `prompt`       TEXT              DEFAULT NULL COMMENT '用户输入的问题',
    `response`     TEXT              DEFAULT NULL COMMENT 'AI 返回的回答',
    `total_tokens` INT               DEFAULT NULL COMMENT '本次对话消耗的 Token 总数',
    `latency_ms`   BIGINT            DEFAULT NULL COMMENT '响应耗时（毫秒）',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = 'AI 对话审计日志表';