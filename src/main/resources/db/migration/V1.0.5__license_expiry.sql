-- License expiry support: existing teachers remain permanent

SET @db_name := DATABASE();

-- activation_code.valid_days (NULL = permanent, e.g. 365 = one year)
SET @has_activation_code_valid_days := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'activation_code'
    AND COLUMN_NAME = 'valid_days'
);
SET @sql_add_activation_code_valid_days := IF(
  @has_activation_code_valid_days = 0,
  'ALTER TABLE `activation_code` ADD COLUMN `valid_days` INT NULL COMMENT ''Account validity days from registration; NULL means permanent'' AFTER `used`',
  'SELECT 1'
);
PREPARE stmt_add_activation_code_valid_days FROM @sql_add_activation_code_valid_days;
EXECUTE stmt_add_activation_code_valid_days;
DEALLOCATE PREPARE stmt_add_activation_code_valid_days;

-- teacher.license_expires_at (NULL = permanent)
SET @has_teacher_license_expires_at := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db_name
    AND TABLE_NAME = 'teacher'
    AND COLUMN_NAME = 'license_expires_at'
);
SET @sql_add_teacher_license_expires_at := IF(
  @has_teacher_license_expires_at = 0,
  'ALTER TABLE `teacher` ADD COLUMN `license_expires_at` DATETIME NULL COMMENT ''License expires at; NULL means permanent'' AFTER `status`',
  'SELECT 1'
);
PREPARE stmt_add_teacher_license_expires_at FROM @sql_add_teacher_license_expires_at;
EXECUTE stmt_add_teacher_license_expires_at;
DEALLOCATE PREPARE stmt_add_teacher_license_expires_at;
