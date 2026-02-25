-- Event and leaderboard query indexes

SET @db_name := DATABASE();

-- student_event: class + revoked + timestamp
SET @has_idx_student_event_class_revoked_ts := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'student_event'
    AND INDEX_NAME = 'idx_student_event_class_revoked_ts'
);
SET @sql_add_idx_student_event_class_revoked_ts := IF(
  @has_idx_student_event_class_revoked_ts = 0,
  'CREATE INDEX `idx_student_event_class_revoked_ts` ON `student_event` (`class_id`, `revoked`, `timestamp`)',
  'SELECT 1'
);
PREPARE stmt_add_idx_student_event_class_revoked_ts FROM @sql_add_idx_student_event_class_revoked_ts;
EXECUTE stmt_add_idx_student_event_class_revoked_ts;
DEALLOCATE PREPARE stmt_add_idx_student_event_class_revoked_ts;

-- group_event: class + revoked + timestamp
SET @has_idx_group_event_class_revoked_ts := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'group_event'
    AND INDEX_NAME = 'idx_group_event_class_revoked_ts'
);
SET @sql_add_idx_group_event_class_revoked_ts := IF(
  @has_idx_group_event_class_revoked_ts = 0,
  'CREATE INDEX `idx_group_event_class_revoked_ts` ON `group_event` (`class_id`, `revoked`, `timestamp`)',
  'SELECT 1'
);
PREPARE stmt_add_idx_group_event_class_revoked_ts FROM @sql_add_idx_group_event_class_revoked_ts;
EXECUTE stmt_add_idx_group_event_class_revoked_ts;
DEALLOCATE PREPARE stmt_add_idx_group_event_class_revoked_ts;

-- student leaderboard: class + total_points
SET @has_idx_student_class_total_points := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'student'
    AND INDEX_NAME = 'idx_student_class_total_points'
);
SET @sql_add_idx_student_class_total_points := IF(
  @has_idx_student_class_total_points = 0,
  'CREATE INDEX `idx_student_class_total_points` ON `student` (`class_id`, `total_points`)',
  'SELECT 1'
);
PREPARE stmt_add_idx_student_class_total_points FROM @sql_add_idx_student_class_total_points;
EXECUTE stmt_add_idx_student_class_total_points;
DEALLOCATE PREPARE stmt_add_idx_student_class_total_points;

-- group leaderboard: class + points
SET @has_idx_group_info_class_points := (
  SELECT COUNT(*)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'group_info'
    AND INDEX_NAME = 'idx_group_info_class_points'
);
SET @sql_add_idx_group_info_class_points := IF(
  @has_idx_group_info_class_points = 0,
  'CREATE INDEX `idx_group_info_class_points` ON `group_info` (`class_id`, `points`)',
  'SELECT 1'
);
PREPARE stmt_add_idx_group_info_class_points FROM @sql_add_idx_group_info_class_points;
EXECUTE stmt_add_idx_group_info_class_points;
DEALLOCATE PREPARE stmt_add_idx_group_info_class_points;
