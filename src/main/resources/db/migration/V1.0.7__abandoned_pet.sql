-- Abandoned Pets Table
-- 记录学生抛弃的宠物，抛弃后该学生不能再次选择该宠物

CREATE TABLE IF NOT EXISTS `abandoned_pet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class_id` bigint(20) NOT NULL COMMENT 'Class ID',
  `student_id` bigint(20) NOT NULL COMMENT 'Student ID',
  `pet_id` varchar(64) NOT NULL COMMENT 'Pet Route ID that was abandoned',
  `abandoned_at` bigint(20) NOT NULL COMMENT 'Timestamp when pet was abandoned',
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`, `student_id`),
  KEY `idx_student_pet` (`student_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Abandoned Pets Records';
