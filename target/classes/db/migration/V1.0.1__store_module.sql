-- Store Module Tables

CREATE TABLE IF NOT EXISTS `store_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL COMMENT 'Class ID',
  `name` varchar(64) NOT NULL COMMENT 'Item Name',
  `description` varchar(255) DEFAULT '' COMMENT 'Item Description',
  `cost` int(11) NOT NULL DEFAULT '0' COMMENT 'Point Cost',
  `stock` int(11) NOT NULL DEFAULT '-1' COMMENT 'Stock count, -1 for infinite',
  `icon` varchar(255) DEFAULT '' COMMENT 'Icon identifier or URL',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Whether item is visible in store',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Soft delete flag',
  `create_time` bigint(20) DEFAULT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Class Store Items';

CREATE TABLE IF NOT EXISTS `redemption_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL COMMENT 'Class ID',
  `student_id` bigint(20) NOT NULL COMMENT 'Student ID',
  `item_id` bigint(20) NOT NULL COMMENT 'Item ID',
  `item_name` varchar(64) NOT NULL COMMENT 'Snapshot of item name',
  `cost` int(11) NOT NULL DEFAULT '0' COMMENT 'Points spent',
  `status` varchar(20) NOT NULL DEFAULT 'COMPLETED' COMMENT 'PENDING, COMPLETED, REFUNDED',
  `create_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Store Redemption Records';
