CREATE TABLE `student_pet_gallery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `pet_route_id` varchar(64) NOT NULL COMMENT '宠物路线ID',
  `pet_name` varchar(128) NOT NULL COMMENT '宠物名字',
  `unlock_time` bigint NOT NULL COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`, `student_id`),
  KEY `idx_student_pet` (`student_id`, `pet_route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生宠物图鉴表';
