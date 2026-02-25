CREATE DATABASE IF NOT EXISTS class_points DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE class_points;

-- 老师账号
CREATE TABLE IF NOT EXISTS teacher (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 激活码（一人一码）
CREATE TABLE IF NOT EXISTS activation_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL UNIQUE,
  used TINYINT DEFAULT 0,
  used_by BIGINT DEFAULT NULL,
  used_at DATETIME DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 班级
CREATE TABLE IF NOT EXISTS class_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  teacher_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  teacher_name VARCHAR(64) DEFAULT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id)
);

-- 班级配置（成长/宠物/进化）
CREATE TABLE IF NOT EXISTS class_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  levels JSON,
  pets JSON,
  evolution JSON,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_config_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 小组
CREATE TABLE IF NOT EXISTS group_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  icon VARCHAR(16),
  note VARCHAR(255),
  points INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_group_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 学生
CREATE TABLE IF NOT EXISTS student (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  name VARCHAR(64) NOT NULL,
  student_no VARCHAR(32),
  gender VARCHAR(8),
  group_id BIGINT DEFAULT NULL,
  total_points INT DEFAULT 0,
  redeem_points INT DEFAULT 0,
  exp INT DEFAULT 0,
  level INT DEFAULT 1,
  title VARCHAR(64),
  avatar_image LONGTEXT,
  pet_id VARCHAR(64),
  update_time BIGINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 学生积分流水
CREATE TABLE IF NOT EXISTS student_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  reason VARCHAR(128),
  change_value INT NOT NULL,
  redeem_change INT DEFAULT 0,
  note VARCHAR(255),
  timestamp BIGINT NOT NULL,
  revoked TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_se_class FOREIGN KEY (class_id) REFERENCES class_info(id),
  CONSTRAINT fk_se_student FOREIGN KEY (student_id) REFERENCES student(id)
);

-- 小组积分流水
CREATE TABLE IF NOT EXISTS group_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  reason VARCHAR(128),
  change_value INT NOT NULL,
  note VARCHAR(255),
  timestamp BIGINT NOT NULL,
  revoked TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_ge_class FOREIGN KEY (class_id) REFERENCES class_info(id),
  CONSTRAINT fk_ge_group FOREIGN KEY (group_id) REFERENCES group_info(id)
);

-- 规则库
CREATE TABLE IF NOT EXISTS rule_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  content VARCHAR(128),
  points INT NOT NULL,
  type VARCHAR(16),
  category VARCHAR(64),
  enabled TINYINT DEFAULT 1,
  cooldown_hours DECIMAL(6,2) DEFAULT 0,
  stackable TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rule_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 商城商品
CREATE TABLE IF NOT EXISTS shop_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  name VARCHAR(128),
  cost INT NOT NULL,
  stock INT DEFAULT 0,
  icon VARCHAR(16),
  enabled TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_shop_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 兑换记录
CREATE TABLE IF NOT EXISTS redeem_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  qty INT DEFAULT 1,
  cost INT DEFAULT 0,
  balance_before INT DEFAULT 0,
  balance_after INT DEFAULT 0,
  timestamp BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rr_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 批量操作记录
CREATE TABLE IF NOT EXISTS batch_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  reason VARCHAR(128),
  points INT NOT NULL,
  note VARCHAR(255),
  timestamp BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_br_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 回收站
CREATE TABLE IF NOT EXISTS trash (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  student_snapshot JSON,
  deleted_at BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_trash_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 操作日志
CREATE TABLE IF NOT EXISTS operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  type VARCHAR(32),
  message VARCHAR(255),
  meta JSON,
  timestamp BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

-- 点名池
CREATE TABLE IF NOT EXISTS roll_call (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  class_id BIGINT NOT NULL,
  used_ids JSON,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_roll_class FOREIGN KEY (class_id) REFERENCES class_info(id)
);

CREATE INDEX idx_student_class ON student(class_id);
CREATE INDEX idx_student_group ON student(group_id);
CREATE INDEX idx_student_event_sid ON student_event(student_id);
CREATE INDEX idx_student_event_class_ts ON student_event(class_id, timestamp);
CREATE INDEX idx_group_event_gid ON group_event(group_id);
CREATE INDEX idx_group_event_class_ts ON group_event(class_id, timestamp);
CREATE INDEX idx_rule_class ON rule_info(class_id);
CREATE INDEX idx_shop_class ON shop_item(class_id);
CREATE INDEX idx_rr_class_ts ON redeem_record(class_id, timestamp);
CREATE INDEX idx_log_class_ts ON operation_log(class_id, timestamp);
