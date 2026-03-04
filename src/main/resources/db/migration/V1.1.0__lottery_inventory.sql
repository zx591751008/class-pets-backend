CREATE TABLE IF NOT EXISTS `lottery_draw_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `prize_code` varchar(64) NOT NULL,
  `prize_name` varchar(128) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `cost_redeem` int(11) NOT NULL DEFAULT '0',
  `reward_redeem` int(11) NOT NULL DEFAULT '0',
  `inventory_item_code` varchar(64) DEFAULT NULL,
  `inventory_item_name` varchar(128) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `create_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lottery_student_time` (`student_id`, `create_time`),
  KEY `idx_lottery_class_time` (`class_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `student_inventory` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(128) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `create_time` bigint(20) NOT NULL,
  `update_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_student_item` (`student_id`, `item_code`),
  KEY `idx_inventory_student` (`student_id`),
  KEY `idx_inventory_class` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `inventory_use_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `inventory_id` bigint(20) NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(128) NOT NULL,
  `target_event_id` bigint(20) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `create_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inventory_use_student_time` (`student_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
