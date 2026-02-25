-- Initial Schema - All base tables
-- These tables existed before Flyway was introduced

CREATE TABLE IF NOT EXISTS `teacher` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `activation_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `used` int(11) NOT NULL DEFAULT '0',
  `used_by` bigint(20) DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `class_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `teacher_name` varchar(64) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `class_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `levels` text DEFAULT NULL,
  `pets` text DEFAULT NULL,
  `evolution` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `student` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `student_no` varchar(32) DEFAULT NULL,
  `gender` varchar(8) DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `total_points` int(11) NOT NULL DEFAULT '0',
  `redeem_points` int(11) NOT NULL DEFAULT '0',
  `exp` int(11) NOT NULL DEFAULT '0',
  `level` int(11) NOT NULL DEFAULT '0',
  `title` varchar(64) DEFAULT NULL,
  `avatar_image` varchar(255) DEFAULT NULL,
  `pet_id` varchar(64) DEFAULT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `group_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `name` varchar(64) NOT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `points` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rule_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `content` varchar(255) NOT NULL,
  `points` int(11) NOT NULL DEFAULT '0',
  `type` varchar(16) NOT NULL DEFAULT 'add',
  `target_type` int(11) DEFAULT '0' COMMENT '0: Student, 1: Group',
  `category` varchar(64) DEFAULT NULL,
  `enabled` int(11) NOT NULL DEFAULT '1',
  `cooldown_hours` decimal(10,2) DEFAULT NULL,
  `stackable` int(11) DEFAULT '1',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `student_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `change_value` int(11) NOT NULL DEFAULT '0',
  `redeem_change` int(11) DEFAULT '0',
  `note` varchar(255) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `revoked` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `group_event` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `change_value` int(11) NOT NULL DEFAULT '0',
  `note` varchar(255) DEFAULT NULL,
  `timestamp` bigint(20) DEFAULT NULL,
  `revoked` int(11) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_class_group` (`class_id`, `group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
