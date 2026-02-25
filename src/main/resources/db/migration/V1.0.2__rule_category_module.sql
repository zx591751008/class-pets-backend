-- Rule Category Module

CREATE TABLE IF NOT EXISTS `rule_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT 'Class ID',
  `name` varchar(64) NOT NULL COMMENT 'Category Name',
  `target_type` int NOT NULL DEFAULT '0' COMMENT '0 student, 1 group',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'Display order',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_target_name` (`class_id`, `target_type`, `name`),
  KEY `idx_class_target` (`class_id`, `target_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Rule Categories';

-- Keep collation aligned with existing legacy columns
ALTER TABLE `rule_category` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Add category_id column in an idempotent way (safe for reruns after partial failure)
SET @db_name := DATABASE();
SET @has_category_id := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'rule_info'
    AND COLUMN_NAME = 'category_id'
);
SET @sql_add_col := IF(
  @has_category_id = 0,
  'ALTER TABLE `rule_info` ADD COLUMN `category_id` bigint NULL COMMENT ''Rule category id'' AFTER `category`',
  'SELECT 1'
);
PREPARE stmt_add_col FROM @sql_add_col;
EXECUTE stmt_add_col;
DEALLOCATE PREPARE stmt_add_col;

-- Add index in an idempotent way
SET @has_idx_category_id := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'rule_info'
    AND INDEX_NAME = 'idx_rule_info_category_id'
);
SET @sql_add_idx := IF(
  @has_idx_category_id = 0,
  'CREATE INDEX `idx_rule_info_category_id` ON `rule_info` (`category_id`)',
  'SELECT 1'
);
PREPARE stmt_add_idx FROM @sql_add_idx;
EXECUTE stmt_add_idx;
DEALLOCATE PREPARE stmt_add_idx;

-- Migrate existing categories from rule_info
INSERT INTO `rule_category` (`class_id`, `name`, `target_type`, `sort`, `enabled`)
SELECT src.class_id, src.name, src.target_type, 0, 1
FROM (
  SELECT DISTINCT
    `class_id` AS class_id,
    TRIM(`category`) AS name,
    IFNULL(`target_type`, 0) AS target_type
  FROM `rule_info`
  WHERE `category` IS NOT NULL
    AND TRIM(`category`) <> ''
) src
LEFT JOIN `rule_category` rc
  ON rc.class_id = src.class_id
  AND rc.target_type = src.target_type
  AND CONVERT(rc.name USING utf8mb4) COLLATE utf8mb4_general_ci = CONVERT(src.name USING utf8mb4) COLLATE utf8mb4_general_ci
WHERE rc.id IS NULL;

-- Backfill category_id for existing rules
UPDATE `rule_info` ri
JOIN `rule_category` rc
  ON rc.class_id = ri.class_id
  AND rc.target_type = IFNULL(ri.target_type, 0)
  AND CONVERT(rc.name USING utf8mb4) COLLATE utf8mb4_general_ci = CONVERT(TRIM(ri.category) USING utf8mb4) COLLATE utf8mb4_general_ci
SET ri.category_id = rc.id
WHERE ri.category IS NOT NULL
  AND TRIM(ri.category) <> ''
  AND ri.category_id IS NULL;
