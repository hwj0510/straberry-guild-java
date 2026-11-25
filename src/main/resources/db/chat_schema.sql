-- 私聊功能建表脚本（幂等，可重复执行）
-- 会话表：A、B 双方共用一条记录，user_a_id < user_b_id 保证唯一性
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_a_id` BIGINT NOT NULL COMMENT '较小的用户ID',
  `user_b_id` BIGINT NOT NULL COMMENT '较大的用户ID',
  `last_message_id` BIGINT DEFAULT NULL,
  `last_message_content` VARCHAR(500) DEFAULT NULL,
  `last_message_time` DATETIME DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pair` (`user_a_id`, `user_b_id`),
  KEY `idx_a` (`user_a_id`),
  KEY `idx_b` (`user_b_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话';

-- 消息明细表
CREATE TABLE IF NOT EXISTS `private_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT NOT NULL,
  `from_user_id` BIGINT NOT NULL,
  `to_user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `type` VARCHAR(20) NOT NULL DEFAULT 'text',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '0-未读 1-已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conv` (`conversation_id`, `created_at`),
  KEY `idx_to_unread` (`to_user_id`, `is_read`),
  KEY `idx_from` (`from_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息';
