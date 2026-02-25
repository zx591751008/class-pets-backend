-- Teacher screen lock support

SET @db_name := DATABASE();

SET @has_teacher_screen_lock_hash := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'teacher'
    AND COLUMN_NAME = 'screen_lock_hash'
);
SET @sql_add_teacher_screen_lock_hash := IF(
  @has_teacher_screen_lock_hash = 0,
  'ALTER TABLE `teacher` ADD COLUMN `screen_lock_hash` VARCHAR(255) NULL COMMENT ''Screen lock password hash'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt_add_teacher_screen_lock_hash FROM @sql_add_teacher_screen_lock_hash;
EXECUTE stmt_add_teacher_screen_lock_hash;
DEALLOCATE PREPARE stmt_add_teacher_screen_lock_hash;

SET @has_teacher_screen_lock_enabled := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'teacher'
    AND COLUMN_NAME = 'screen_lock_enabled'
);
SET @sql_add_teacher_screen_lock_enabled := IF(
  @has_teacher_screen_lock_enabled = 0,
  'ALTER TABLE `teacher` ADD COLUMN `screen_lock_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''Whether screen lock password is enabled'' AFTER `screen_lock_hash`',
  'SELECT 1'
);
PREPARE stmt_add_teacher_screen_lock_enabled FROM @sql_add_teacher_screen_lock_enabled;
EXECUTE stmt_add_teacher_screen_lock_enabled;
DEALLOCATE PREPARE stmt_add_teacher_screen_lock_enabled;
