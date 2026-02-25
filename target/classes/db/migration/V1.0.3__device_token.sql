CREATE TABLE teacher_device_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL COMMENT 'Teacher ID',
    device_type VARCHAR(20) NOT NULL COMMENT 'Device Type: PC, MOBILE, TABLET',
    token VARCHAR(512) NOT NULL COMMENT 'Active Token',
    last_login_time DATETIME COMMENT 'Last Login Time',
    INDEX idx_teacher_device (teacher_id, device_type)
) COMMENT 'Teacher Device Login Tokens';
