-- MySQL dump 10.13  Distrib 8.0.19, for Win64 (x86_64)
--
-- Host: localhost    Database: class_points
-- ------------------------------------------------------
-- Server version	8.0.19

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `abandoned_pet`
--

DROP TABLE IF EXISTS `abandoned_pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `abandoned_pet` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT 'Class ID',
  `student_id` bigint NOT NULL COMMENT 'Student ID',
  `pet_id` varchar(64) NOT NULL COMMENT 'Pet Route ID that was abandoned',
  `abandoned_at` bigint NOT NULL COMMENT 'Timestamp when pet was abandoned',
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`,`student_id`),
  KEY `idx_student_pet` (`student_id`,`pet_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Abandoned Pets Records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `abandoned_pet`
--

LOCK TABLES `abandoned_pet` WRITE;
/*!40000 ALTER TABLE `abandoned_pet` DISABLE KEYS */;
INSERT INTO `abandoned_pet` VALUES (1,2,12,'v1gfnbz1',1772108608349),(2,2,20,'sys_golden_dragon',1772110374985),(3,2,23,'sys_golden_dragon',1772111319205),(4,2,21,'sys_thunder_wolf',1772111510555),(5,2,22,'sys_golden_dragon',1772111518199),(6,2,22,'sys_thunder_wolf',1772114552414),(7,2,22,'sys_flame_lion',1772114577319),(8,2,24,'sys_thunder_wolf',1772121593649),(9,2,62,'sys_thunder_wolf',1772121631465),(10,2,64,'sys_golden_dragon',1772121989871),(11,2,21,'sys_golden_dragon',1772159392862),(12,2,52,'sys_golden_dragon',1772163510056),(13,2,52,'sys_thunder_wolf',1772163537660),(14,4,78,'sys_thunder_wolf',1772469328058),(15,4,79,'sys_thunder_wolf',1772469734747),(16,4,79,'sys_golden_dragon',1772883347020);
/*!40000 ALTER TABLE `abandoned_pet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activation_code`
--

DROP TABLE IF EXISTS `activation_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activation_code` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `used` tinyint DEFAULT '0',
  `valid_days` int DEFAULT NULL COMMENT 'Account validity days from registration; NULL means permanent',
  `used_by` bigint DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activation_code`
--

LOCK TABLES `activation_code` WRITE;
/*!40000 ALTER TABLE `activation_code` DISABLE KEYS */;
INSERT INTO `activation_code` VALUES (1,'AG-TEST-0001',1,NULL,1,'2026-02-07 18:24:32','2026-02-07 17:40:06'),(2,'CLASSPETS2024',1,NULL,2,'2026-02-07 20:51:13','2026-02-07 20:50:36'),(5,'CP-F6BM-FCHV-AAYL',1,365,3,'2026-02-23 12:18:20','2026-02-23 12:08:13'),(6,'CP-W2B2-BJLX-MANY',1,365,4,'2026-02-24 17:21:44','2026-02-23 12:08:13'),(7,'CP-WKVM-MTDN-VXHZ',0,365,NULL,NULL,'2026-02-23 12:08:19'),(8,'CP-2K3T-VVSP-JSRY',0,365,NULL,NULL,'2026-02-23 12:08:19'),(9,'CP-XFZV-G8JF-BET7',0,365,NULL,NULL,'2026-02-23 12:08:19'),(10,'CP-KZ4T-NN2W-TRR3',0,365,NULL,NULL,'2026-02-23 12:08:19'),(11,'CP-74YZ-K3RQ-YDDN',0,365,NULL,NULL,'2026-02-23 12:08:19'),(12,'CP-79YD-FC4K-4WNP',0,365,NULL,NULL,'2026-02-23 12:08:19'),(13,'CP-KK84-F3FC-2UTM',0,365,NULL,NULL,'2026-02-23 12:08:19'),(14,'CP-LU2X-HNSR-ER5Y',0,365,NULL,NULL,'2026-02-23 12:08:19'),(15,'CP-5BFN-WQ5P-TXET',0,365,NULL,NULL,'2026-02-23 12:08:19'),(16,'CP-4K9B-FVRH-FGSG',0,365,NULL,NULL,'2026-02-23 12:08:19'),(17,'CP-S446-2XBP-8BNE',0,365,NULL,NULL,'2026-02-23 12:08:19'),(18,'CP-Z2PA-Z3S8-7ZHU',0,365,NULL,NULL,'2026-02-23 12:08:19'),(19,'CP-3NW5-X7UA-LMZG',0,365,NULL,NULL,'2026-02-23 12:08:19'),(20,'CP-DACN-3EDH-S9KS',0,365,NULL,NULL,'2026-02-23 12:08:19'),(21,'CP-KJZ2-FHD3-4HED',0,365,NULL,NULL,'2026-02-23 12:08:19'),(22,'CP-6CJY-SJN8-38PX',0,365,NULL,NULL,'2026-02-23 12:08:19'),(23,'CP-EPXJ-2SZF-YXLW',0,365,NULL,NULL,'2026-02-23 12:08:19'),(24,'CP-YACH-2HCU-K9QV',0,365,NULL,NULL,'2026-02-23 12:08:19'),(25,'CP-H8D6-WF5P-FSBJ',0,365,NULL,NULL,'2026-02-23 12:08:19'),(26,'CP-HNAP-CYJQ-2ZQM',0,365,NULL,NULL,'2026-02-23 12:08:19'),(27,'CP-T8CV-M4NF-Z5YL',1,1,5,'2026-03-03 23:51:52','2026-03-03 23:50:43'),(28,'CP-4B5A-G2DR-9FJR',1,1,6,'2026-03-03 23:55:12','2026-03-03 23:54:49');
/*!40000 ALTER TABLE `activation_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_audit_log`
--

DROP TABLE IF EXISTS `admin_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint DEFAULT NULL,
  `operator_username` varchar(64) DEFAULT NULL,
  `action` varchar(64) NOT NULL,
  `target_type` varchar(64) DEFAULT NULL,
  `target_id` varchar(128) DEFAULT NULL,
  `detail_json` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_operator` (`operator_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_audit_log`
--

LOCK TABLES `admin_audit_log` WRITE;
/*!40000 ALTER TABLE `admin_audit_log` DISABLE KEYS */;
INSERT INTO `admin_audit_log` VALUES (1,1,'admin','activation_code_batch_generate','activation_code','batch','{\"count\":2,\"validDays\":365,\"format\":\"CP-XXXX-XXXX-XXXX\"}','2026-02-23 12:01:00'),(2,1,'admin','activation_code_delete','activation_code','4','{\"code\":\"CP-82Y8-F77V-JB5K\"}','2026-02-23 12:01:02'),(3,1,'admin','activation_code_delete','activation_code','3','{\"code\":\"CP-QBRM-CA8F-HNYE\"}','2026-02-23 12:01:03'),(4,1,'admin','activation_code_batch_generate','activation_code','batch','{\"count\":2,\"validDays\":365,\"format\":\"CP-XXXX-XXXX-XXXX\"}','2026-02-23 12:08:13'),(5,1,'admin','activation_code_batch_generate','activation_code','batch','{\"count\":20,\"validDays\":365,\"format\":\"CP-XXXX-XXXX-XXXX\"}','2026-02-23 12:08:19'),(6,1,'admin','activation_code_export_unused','activation_code','YEAR','{\"period\":\"YEAR\",\"count\":22}','2026-02-23 12:16:39'),(7,1,'admin','activation_code_batch_generate','activation_code','batch','{\"count\":1,\"validDays\":1,\"format\":\"CP-XXXX-XXXX-XXXX\"}','2026-03-03 23:50:43'),(8,1,'admin','teacher_license_update','teacher','5','{\"permanent\":false,\"days\":1}','2026-03-03 23:52:38'),(9,1,'admin','teacher_license_update','teacher','5','{\"permanent\":true,\"days\":null}','2026-03-03 23:52:55'),(10,1,'admin','teacher_license_update','teacher','5','{\"permanent\":false,\"days\":1111}','2026-03-03 23:53:11'),(11,1,'admin','teacher_status_update','teacher','5','{\"status\":0}','2026-03-03 23:53:58'),(12,1,'admin','teacher_status_update','teacher','5','{\"status\":1}','2026-03-03 23:54:26'),(13,1,'admin','activation_code_batch_generate','activation_code','batch','{\"count\":1,\"validDays\":1,\"format\":\"CP-XXXX-XXXX-XXXX\"}','2026-03-03 23:54:49');
/*!40000 ALTER TABLE `admin_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_user`
--

DROP TABLE IF EXISTS `admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `status` int NOT NULL DEFAULT '1',
  `last_login_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_user`
--

LOCK TABLES `admin_user` WRITE;
/*!40000 ALTER TABLE `admin_user` DISABLE KEYS */;
INSERT INTO `admin_user` VALUES (1,'admin','$2a$10$OoUKEpnu7GW5qnUXcMr7iuW4jXL.F2yIpHzDKHSB3Lg6dI9atkWfK','系统管理员',1,'2026-03-03 23:50:27','2026-02-23 11:50:54','2026-02-23 11:50:54');
/*!40000 ALTER TABLE `admin_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `batch_record`
--

DROP TABLE IF EXISTS `batch_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `reason` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `points` int NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `timestamp` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_br_class` (`class_id`),
  CONSTRAINT `fk_br_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `batch_record`
--

LOCK TABLES `batch_record` WRITE;
/*!40000 ALTER TABLE `batch_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `batch_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_config`
--

DROP TABLE IF EXISTS `class_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `levels` json DEFAULT NULL,
  `pets` json DEFAULT NULL,
  `evolution` json DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_config_class` (`class_id`),
  CONSTRAINT `fk_config_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_config`
--

LOCK TABLES `class_config` WRITE;
/*!40000 ALTER TABLE `class_config` DISABLE KEYS */;
INSERT INTO `class_config` VALUES (1,2,'{\"items\": [{\"name\": \"等级1\", \"level\": 1, \"threshold\": 0}, {\"name\": \"等级2\", \"level\": 2, \"threshold\": 100}, {\"name\": \"等级3\", \"level\": 3, \"threshold\": 200}, {\"name\": \"等级4\", \"level\": 4, \"threshold\": 450}, {\"name\": \"等级5\", \"level\": 5, \"threshold\": 700}, {\"name\": \"等级6\", \"level\": 6, \"threshold\": 1000}, {\"name\": \"等级7\", \"level\": 7, \"threshold\": 1400}, {\"name\": \"等级8\", \"level\": 8, \"threshold\": 1900}, {\"name\": \"等级9\", \"level\": 9, \"threshold\": 2500}, {\"name\": \"等级10\", \"level\": 10, \"threshold\": 3200}], \"expGainRatio\": 10.0, \"overflowStep\": 800, \"levelThresholds\": [0, 100, 200, 450, 700, 1000, 1400, 1900, 2500, 3200]}','{\"routes\": [{\"id\": \"sys_thunder_wolf\", \"name\": \"雷霆狼\", \"stages\": [{\"image\": \"/uploads/system/thunder_wolf/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/thunder_wolf/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/thunder_wolf/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_golden_dragon\", \"name\": \"金光龙\", \"stages\": [{\"image\": \"/uploads/system/golden_dragon/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/golden_dragon/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/golden_dragon/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_flame_lion\", \"name\": \"烈焰狮王\", \"stages\": [{\"image\": \"/uploads/system/flame_lion/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/flame_lion/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/flame_lion/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_mech_rex\", \"name\": \"机甲暴龙\", \"stages\": [{\"image\": \"/uploads/system/mech_rex/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/mech_rex/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/mech_rex/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_nimble_mouse\", \"name\": \"灵机鼠\", \"stages\": [{\"image\": \"/uploads/system/nimble_mouse/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/nimble_mouse/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/nimble_mouse/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_frost_unicorn\", \"name\": \"寒冰独角兽\", \"stages\": [{\"image\": \"/uploads/system/frost_unicorn/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/frost_unicorn/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/frost_unicorn/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_starry_whale\", \"name\": \"星空鲸\", \"stages\": [{\"image\": \"/uploads/system/starry_whale/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/starry_whale/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/starry_whale/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_moon_deer\", \"name\": \"月光灵鹿\", \"stages\": [{\"image\": \"/uploads/system/moon_deer/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/moon_deer/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/moon_deer/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_sakura_fox\", \"name\": \"樱花九尾狐\", \"stages\": [{\"image\": \"/uploads/system/sakura_fox/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/sakura_fox/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/sakura_fox/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_mountain_ox\", \"name\": \"撼山牛\", \"stages\": [{\"image\": \"/uploads/system/mountain_ox/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/mountain_ox/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/mountain_ox/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_kungfu_panda\", \"name\": \"功夫熊猫\", \"stages\": [{\"image\": \"/uploads/system/kungfu_panda/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/kungfu_panda/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/kungfu_panda/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_flame_tiger\", \"name\": \"烈焰虎\", \"stages\": [{\"image\": \"/uploads/system/flame_tiger/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/flame_tiger/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/flame_tiger/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_ice_crystal_fox\", \"name\": \"冰晶狐\", \"stages\": [{\"image\": \"/uploads/system/ice_crystal_fox/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/ice_crystal_fox/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/ice_crystal_fox/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_frost_dragon_horse\", \"name\": \"寒冰龙马\", \"stages\": [{\"image\": \"/uploads/system/frost_dragon_horse/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/frost_dragon_horse/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/frost_dragon_horse/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_shadow_lion\", \"name\": \"影爪狮\", \"stages\": [{\"image\": \"/uploads/system/shadow_lion/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/shadow_lion/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/shadow_lion/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_flame_kirin\", \"name\": \"炎狱麒麟\", \"stages\": [{\"image\": \"/uploads/system/flame_kirin/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/flame_kirin/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/flame_kirin/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_polar_bear\", \"name\": \"极地熊\", \"stages\": [{\"image\": \"/uploads/system/polar_bear/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/polar_bear/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/polar_bear/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_frost_monn_hound\", \"name\": \"霜月犬\", \"stages\": [{\"image\": \"/uploads/system/frost_monn_hound/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/frost_monn_hound/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/frost_monn_hound/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_nebula_fox\", \"name\": \"星幻狐\", \"stages\": [{\"image\": \"/uploads/system/nebula_fox/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/nebula_fox/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/nebula_fox/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_amethyst_kirin\", \"name\": \"紫晶麒麟\", \"stages\": [{\"image\": \"/uploads/system/amethyst_kirin/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/amethyst_kirin/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/amethyst_kirin/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_azure_mammoth\", \"name\": \"苍火猛犸\", \"stages\": [{\"image\": \"/uploads/system/azure_mammoth/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/azure_mammoth/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/azure_mammoth/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_frost_dew_fox\", \"name\": \"寒霜妖狐\", \"stages\": [{\"image\": \"/uploads/system/frost_dew_fox/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/frost_dew_fox/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/frost_dew_fox/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_rock_crystal_lion\", \"name\": \"岩晶狮\", \"stages\": [{\"image\": \"/uploads/system/rock_crystal_lion/1.png\", \"stage\": 1}, {\"image\": \"/uploads/system/rock_crystal_lion/2.png\", \"stage\": 2}, {\"image\": \"/uploads/system/rock_crystal_lion/3.png\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_warm_dog\", \"name\": \"暖绒金犬\", \"stages\": [{\"image\": \"/uploads/system/warm_dog/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/warm_dog/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/warm_dog/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_prism_fox\", \"name\": \"幻彩灵狐\", \"stages\": [{\"image\": \"/uploads/system/prism_fox/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/prism_fox/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/prism_fox/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_amber_wolf\", \"name\": \"琥珀战狼\", \"stages\": [{\"image\": \"/uploads/system/amber_wolf/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/amber_wolf/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/amber_wolf/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_star_wolf\", \"name\": \"星芒灵狼\", \"stages\": [{\"image\": \"/uploads/system/star_wolf/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/star_wolf/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/star_wolf/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_rain_fox\", \"name\": \"彩光萌狐\", \"stages\": [{\"image\": \"/uploads/system/rain_fox/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/rain_fox/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/rain_fox/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_moon_fox\", \"name\": \"幽月青狐\", \"stages\": [{\"image\": \"/uploads/system/moon_fox/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/moon_fox/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/moon_fox/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_mint_unicorn\", \"name\": \"薄荷独角兽\", \"stages\": [{\"image\": \"/uploads/system/mint_unicorn/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/mint_unicorn/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/mint_unicorn/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_white_flame\", \"name\": \"白焰\", \"stages\": [{\"image\": \"/uploads/system/white_flame/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/white_flame/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/white_flame/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_radiantflame_capybara\", \"name\": \"曜焰水豚\", \"stages\": [{\"image\": \"/uploads/system/radiantflame_capybara/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/radiantflame_capybara/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/radiantflame_capybara/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_frostblade_unicorn\", \"name\": \"霜刃独角兽\", \"stages\": [{\"image\": \"/uploads/system/frostblade_unicorn/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/frostblade_unicorn/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/frostblade_unicorn/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_magma_sugarbeast\", \"name\": \"熔岩糖兽\", \"stages\": [{\"image\": \"/uploads/system/magma_sugarbeast/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/magma_sugarbeast/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/magma_sugarbeast/3.webp\", \"stage\": 3}], \"enabled\": true}, {\"id\": \"sys_creamblossom_sprite\", \"name\": \"奶霜花灵\", \"stages\": [{\"image\": \"/uploads/system/creamblossom_sprite/1.webp\", \"stage\": 1}, {\"image\": \"/uploads/system/creamblossom_sprite/2.webp\", \"stage\": 2}, {\"image\": \"/uploads/system/creamblossom_sprite/3.webp\", \"stage\": 3}], \"enabled\": true}]}','{\"stage1MaxLevel\": 4, \"stage2MaxLevel\": 6, \"stage3StartLevel\": 7}','2026-03-18 23:06:54');
/*!40000 ALTER TABLE `class_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `class_info`
--

DROP TABLE IF EXISTS `class_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `class_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `teacher_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_class_teacher` (`teacher_id`),
  CONSTRAINT `fk_class_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `teacher` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `class_info`
--

LOCK TABLES `class_info` WRITE;
/*!40000 ALTER TABLE `class_info` DISABLE KEYS */;
INSERT INTO `class_info` VALUES (1,1,'默认班级','老师','2026-02-07 20:07:32'),(2,2,'二年级1班','张老师','2026-02-07 21:24:02'),(4,2,'测试','老师','2026-02-21 17:13:23'),(5,2,'测试2','老师','2026-03-04 22:06:41'),(6,2,'111','老师','2026-03-04 22:09:42'),(7,2,'测试班级222','老师','2026-03-08 22:29:16'),(8,2,'111','老师','2026-03-08 22:29:22');
/*!40000 ALTER TABLE `class_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (1,'1.0.0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-02-10 10:07:50',0,1),(2,'1.0.1','store module','SQL','V1.0.1__store_module.sql',1134276062,'root','2026-02-10 10:07:50',61,1),(3,'1.0.2','rule category module','SQL','V1.0.2__rule_category_module.sql',1069176932,'root','2026-02-14 15:52:45',96,1),(4,'1.0.3','device token','SQL','V1.0.3__device_token.sql',467655233,'root','2026-02-20 03:24:36',50,1),(5,'1.0.4','event rank indexes','SQL','V1.0.4__event_rank_indexes.sql',1720161937,'root','2026-02-22 12:38:26',141,1),(6,'1.0.5','license expiry','SQL','V1.0.5__license_expiry.sql',-73294524,'root','2026-02-22 12:38:26',321,1),(7,'1.0.6','screen lock','SQL','V1.0.6__screen_lock.sql',332923963,'root','2026-02-22 12:38:26',307,1),(8,'1.0.7','abandoned pet','SQL','V1.0.7__abandoned_pet.sql',1503426862,'root','2026-02-25 15:57:50',49,1),(9,'1.0.8','pet gallery','SQL','V1.0.8__pet_gallery.sql',1191330961,'root','2026-02-26 10:57:11',44,1),(10,'1.0.9','sanitize pet route names','SQL','V1.0.9__sanitize_pet_route_names.sql',-1438968420,'root','2026-02-26 13:56:04',4,1),(11,'1.1.0','lottery inventory','SQL','V1.1.0__lottery_inventory.sql',-1144908849,'root','2026-02-27 07:13:52',101,1),(12,'1.1.1','student event exp change','SQL','V1.1.1__student_event_exp_change.sql',1014819030,'root','2026-02-27 07:51:06',165,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history_admin`
--

DROP TABLE IF EXISTS `flyway_schema_history_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history_admin` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_general_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_admin_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history_admin`
--

LOCK TABLES `flyway_schema_history_admin` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history_admin` DISABLE KEYS */;
INSERT INTO `flyway_schema_history_admin` VALUES (1,'0.0.1','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-02-23 03:40:39',0,1),(2,'1.0.0','admin schema','SQL','V1.0.0__admin_schema.sql',-1439601442,'root','2026-02-23 03:40:39',53,1);
/*!40000 ALTER TABLE `flyway_schema_history_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_event`
--

DROP TABLE IF EXISTS `group_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  `reason` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `change_value` int NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `timestamp` bigint NOT NULL,
  `revoked` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group_event_gid` (`group_id`),
  KEY `idx_group_event_class_ts` (`class_id`,`timestamp`),
  KEY `idx_group_event_class_revoked_ts` (`class_id`,`revoked`,`timestamp`),
  CONSTRAINT `fk_ge_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`),
  CONSTRAINT `fk_ge_group` FOREIGN KEY (`group_id`) REFERENCES `group_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_event`
--

LOCK TABLES `group_event` WRITE;
/*!40000 ALTER TABLE `group_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `group_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_info`
--

DROP TABLE IF EXISTS `group_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `icon` varchar(16) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `points` int DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_group_info_class_points` (`class_id`,`points`),
  CONSTRAINT `fk_group_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_info`
--

LOCK TABLES `group_info` WRITE;
/*!40000 ALTER TABLE `group_info` DISABLE KEYS */;
INSERT INTO `group_info` VALUES (7,4,'11','🚀',NULL,0,'2026-03-03 22:45:08'),(9,2,'11','🚀',NULL,0,'2026-03-04 19:29:28'),(10,7,'青龙队','👥',NULL,0,'2026-03-08 22:30:35'),(11,7,'白虎机动队','👥',NULL,0,'2026-03-08 22:30:35'),(12,7,'朱雀队','👥',NULL,0,'2026-03-08 22:30:35'),(13,7,'玄武队','👥',NULL,0,'2026-03-08 22:30:35'),(14,7,'麒麟队','👥',NULL,0,'2026-03-08 22:30:35');
/*!40000 ALTER TABLE `group_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory_use_record`
--

DROP TABLE IF EXISTS `inventory_use_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_use_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `inventory_id` bigint NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(128) NOT NULL,
  `target_event_id` bigint DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `create_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_inventory_use_student_time` (`student_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory_use_record`
--

LOCK TABLES `inventory_use_record` WRITE;
/*!40000 ALTER TABLE `inventory_use_record` DISABLE KEYS */;
INSERT INTO `inventory_use_record` VALUES (1,2,52,1,'CANDY','魔法糖果',NULL,NULL,1772177465728),(2,2,52,1,'CANDY','魔法糖果',NULL,NULL,1772177720906),(3,2,52,1,'CANDY','魔法糖果',NULL,NULL,1772177727197),(4,2,52,2,'CLEAN_PASS','劳动豁免券',NULL,NULL,1772177735656),(5,2,52,4,'MEAT','超级烤肉',NULL,NULL,1772177739388),(6,2,52,3,'BALANCE_BAG','余额福袋',NULL,NULL,1772177846870),(7,2,52,3,'BALANCE_BAG','余额福袋',NULL,NULL,1772177855458),(8,2,52,1,'CANDY','魔法糖果',NULL,NULL,1772178750052),(9,2,52,5,'SPEAK_STICK','发言指定棒',NULL,NULL,1772178762052),(10,2,52,5,'SPEAK_STICK','发言指定棒',NULL,NULL,1772178771129),(11,2,52,2,'CLEAN_PASS','劳动豁免券',NULL,NULL,1772178778262),(12,2,52,3,'BALANCE_BAG','余额福袋',NULL,NULL,1772178784270);
/*!40000 ALTER TABLE `inventory_use_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lottery_draw_record`
--

DROP TABLE IF EXISTS `lottery_draw_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lottery_draw_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `prize_code` varchar(64) NOT NULL,
  `prize_name` varchar(128) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `cost_redeem` int NOT NULL DEFAULT '0',
  `reward_redeem` int NOT NULL DEFAULT '0',
  `inventory_item_code` varchar(64) DEFAULT NULL,
  `inventory_item_name` varchar(128) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `create_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lottery_student_time` (`student_id`,`create_time`),
  KEY `idx_lottery_class_time` (`class_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lottery_draw_record`
--

LOCK TABLES `lottery_draw_record` WRITE;
/*!40000 ALTER TABLE `lottery_draw_record` DISABLE KEYS */;
INSERT INTO `lottery_draw_record` VALUES (1,2,52,'BALANCE_BAG','余额福袋','common',20,10,NULL,NULL,NULL,1772176502385),(2,2,52,'CANDY','魔法糖果','common',20,0,'CANDY','魔法糖果',NULL,1772177437665),(3,2,52,'CLEAN_PASS','劳动豁免券','common',20,0,'CLEAN_PASS','劳动豁免券',NULL,1772177654322),(4,2,52,'CLEAN_PASS','劳动豁免券','common',20,0,'CLEAN_PASS','劳动豁免券',NULL,1772177662833),(5,2,52,'CANDY','魔法糖果','common',20,0,'CANDY','魔法糖果',NULL,1772177669077),(6,2,52,'CLEAN_PASS','劳动豁免券','common',20,0,'CLEAN_PASS','劳动豁免券',NULL,1772177676489),(7,2,52,'BALANCE_BAG','余额福袋','common',20,0,'BALANCE_BAG','余额福袋',NULL,1772177682212),(8,2,52,'BALANCE_BAG','余额福袋','common',20,0,'BALANCE_BAG','余额福袋',NULL,1772177688318),(9,2,52,'MEAT','超级烤肉','rare',20,0,'MEAT','超级烤肉',NULL,1772177693398),(10,2,52,'CANDY','魔法糖果','common',20,0,'CANDY','魔法糖果',NULL,1772177700088),(11,2,52,'SPEAK_STICK','发言指定棒','epic',20,0,'SPEAK_STICK','发言指定棒',NULL,1772178703696),(12,2,52,'SPEAK_STICK','发言指定棒','epic',20,0,'SPEAK_STICK','发言指定棒',NULL,1772178710394),(13,2,52,'CLEAN_PASS','劳动豁免券','common',20,0,'CLEAN_PASS','劳动豁免券',NULL,1772178715626),(14,2,52,'BALANCE_BAG','余额福袋','common',20,0,'BALANCE_BAG','余额福袋',NULL,1772178722174),(15,2,52,'BALANCE_BAG','余额福袋','common',20,0,'BALANCE_BAG','余额福袋',NULL,1772178729734),(16,2,52,'CANDY','魔法糖果','common',20,0,'CANDY','魔法糖果',NULL,1772178736343);
/*!40000 ALTER TABLE `lottery_draw_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `type` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `message` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `meta` json DEFAULT NULL,
  `timestamp` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_log_class_ts` (`class_id`,`timestamp`),
  CONSTRAINT `fk_log_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `redeem_record`
--

DROP TABLE IF EXISTS `redeem_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `redeem_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `qty` int DEFAULT '1',
  `cost` int DEFAULT '0',
  `balance_before` int DEFAULT '0',
  `balance_after` int DEFAULT '0',
  `timestamp` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_rr_class_ts` (`class_id`,`timestamp`),
  CONSTRAINT `fk_rr_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `redeem_record`
--

LOCK TABLES `redeem_record` WRITE;
/*!40000 ALTER TABLE `redeem_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `redeem_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `redemption_record`
--

DROP TABLE IF EXISTS `redemption_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `redemption_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT 'Class ID',
  `student_id` bigint NOT NULL COMMENT 'Student ID',
  `item_id` bigint NOT NULL COMMENT 'Item ID',
  `item_name` varchar(64) NOT NULL COMMENT 'Snapshot of item name',
  `cost` int NOT NULL DEFAULT '0' COMMENT 'Points spent',
  `status` varchar(20) NOT NULL DEFAULT 'COMPLETED' COMMENT 'PENDING, COMPLETED, REFUNDED',
  `create_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`,`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Store Redemption Records';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `redemption_record`
--

LOCK TABLES `redemption_record` WRITE;
/*!40000 ALTER TABLE `redemption_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `redemption_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roll_call`
--

DROP TABLE IF EXISTS `roll_call`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roll_call` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `used_ids` json DEFAULT NULL,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_roll_class` (`class_id`),
  CONSTRAINT `fk_roll_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roll_call`
--

LOCK TABLES `roll_call` WRITE;
/*!40000 ALTER TABLE `roll_call` DISABLE KEYS */;
/*!40000 ALTER TABLE `roll_call` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_category`
--

DROP TABLE IF EXISTS `rule_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT 'Class ID',
  `name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Category Name',
  `target_type` int NOT NULL DEFAULT '0' COMMENT '0 student, 1 group',
  `sort` int NOT NULL DEFAULT '0' COMMENT 'Display order',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 enabled, 0 disabled',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_target_name` (`class_id`,`target_type`,`name`),
  KEY `idx_class_target` (`class_id`,`target_type`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Rule Categories';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_category`
--

LOCK TABLES `rule_category` WRITE;
/*!40000 ALTER TABLE `rule_category` DISABLE KEYS */;
INSERT INTO `rule_category` VALUES (8,2,'课堂表现',0,0,1,'2026-02-14 23:52:45'),(9,2,'作业完成',0,0,1,'2026-02-14 23:52:45'),(10,2,'纪律行为',0,0,1,'2026-02-14 23:52:45'),(11,2,'互助合作',0,0,1,'2026-02-14 23:52:45'),(12,2,'课堂表现',1,0,1,'2026-02-14 23:52:45'),(13,2,'测试分类',0,0,1,'2026-03-04 20:39:41'),(14,5,'课堂表现',0,10,1,'2026-03-04 22:06:41'),(15,5,'作业学习',0,20,1,'2026-03-04 22:06:41'),(16,5,'纪律规范',0,30,1,'2026-03-04 22:06:41'),(17,5,'团队协作',1,10,1,'2026-03-04 22:06:41'),(18,5,'测试分类',0,0,1,'2026-03-04 22:06:52'),(19,5,'测试分类2',0,0,1,'2026-03-04 22:08:06'),(20,6,'课堂表现',0,10,1,'2026-03-04 22:09:42'),(21,6,'作业学习',0,20,1,'2026-03-04 22:09:42'),(22,6,'纪律规范',0,30,0,'2026-03-04 22:09:42'),(23,6,'团队协作',1,10,1,'2026-03-04 22:09:42'),(24,7,'课堂表现',0,10,1,'2026-03-08 22:29:16'),(25,7,'作业学习',0,20,1,'2026-03-08 22:29:16'),(26,7,'纪律规范',0,30,1,'2026-03-08 22:29:16'),(27,7,'团队协作',1,10,1,'2026-03-08 22:29:16'),(28,8,'课堂表现',0,10,1,'2026-03-08 22:29:22'),(29,8,'作业学习',0,20,1,'2026-03-08 22:29:22'),(30,8,'纪律规范',0,30,1,'2026-03-08 22:29:22'),(31,8,'团队协作',1,10,1,'2026-03-08 22:29:22'),(32,7,'通用类型',0,0,1,'2026-03-09 09:45:00');
/*!40000 ALTER TABLE `rule_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rule_info`
--

DROP TABLE IF EXISTS `rule_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rule_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `content` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `points` int NOT NULL,
  `type` varchar(16) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `category` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `category_id` bigint DEFAULT NULL COMMENT 'Rule category id',
  `enabled` tinyint DEFAULT '1',
  `cooldown_hours` decimal(6,2) DEFAULT '0.00',
  `stackable` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `target_type` int DEFAULT '0' COMMENT '0: Student, 1: Group',
  PRIMARY KEY (`id`),
  KEY `idx_rule_class` (`class_id`),
  KEY `idx_rule_info_category_id` (`category_id`),
  CONSTRAINT `fk_rule_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rule_info`
--

LOCK TABLES `rule_info` WRITE;
/*!40000 ALTER TABLE `rule_info` DISABLE KEYS */;
INSERT INTO `rule_info` VALUES (2,2,'主动回答问题',5,'add','课堂表现',8,1,0.00,1,'2026-02-07 22:33:32',0),(3,2,'上课专心听讲',3,'add','课堂表现',8,1,0.00,1,'2026-02-07 22:33:32',0),(4,2,'课堂走神',-2,'deduct','课堂表现',8,1,0.00,1,'2026-02-07 22:33:32',0),(5,2,'作业完成优秀',10,'add','作业完成',9,1,0.00,1,'2026-02-07 22:33:32',0),(6,2,'按时交作业',3,'add','作业完成',9,1,0.00,1,'2026-02-07 22:33:32',0),(7,2,'作业未交',-5,'deduct','作业完成',9,1,0.00,1,'2026-02-07 22:33:32',0),(8,2,'课堂纪律良好',3,'add','纪律行为',10,1,0.00,1,'2026-02-07 22:33:32',0),(9,2,'上课迟到',-3,'deduct','纪律行为',10,1,0.00,1,'2026-02-07 22:33:32',0),(10,2,'违反课堂纪律',3,'deduct','课堂表现',8,1,0.00,1,'2026-02-07 22:33:32',0),(11,2,'帮助同学',8,'add','互助合作',11,1,0.00,1,'2026-02-07 22:33:32',0),(12,2,'小组合作优秀',5,'add','互助合作',11,1,0.00,1,'2026-02-07 22:33:32',0),(13,2,'值日认真',3,'add','互助合作',11,1,0.00,1,'2026-02-07 22:33:32',0),(14,2,'小组合作',2,'add','课堂表现',12,1,0.00,1,'2026-02-08 21:51:52',1),(15,2,'测试规则',1,'add','课堂表现',8,1,0.00,1,'2026-03-04 22:06:09',0),(16,5,'主动回答问题',2,'add','课堂表现',14,1,0.00,1,'2026-03-04 22:06:41',0),(17,5,'帮助同学',3,'add','课堂表现',14,1,0.00,1,'2026-03-04 22:06:41',0),(18,5,'上课走神',2,'deduct','课堂表现',14,1,0.00,1,'2026-03-04 22:06:41',0),(19,5,'作业按时完成',2,'add','作业学习',15,1,0.00,1,'2026-03-04 22:06:41',0),(20,5,'作业优秀',3,'add','作业学习',15,1,0.00,1,'2026-03-04 22:06:41',0),(21,5,'作业未完成',3,'deduct','作业学习',15,1,0.00,1,'2026-03-04 22:06:41',0),(22,5,'课堂纪律良好',2,'add','纪律规范',16,1,0.00,1,'2026-03-04 22:06:41',0),(23,5,'课堂喧哗',2,'deduct','纪律规范',16,1,0.00,1,'2026-03-04 22:06:41',0),(24,5,'小组合作优秀',5,'add','团队协作',17,1,0.00,1,'2026-03-04 22:06:41',1),(25,5,'小组任务未完成',4,'deduct','团队协作',17,1,0.00,1,'2026-03-04 22:06:41',1),(26,5,'测试规则',1,'add','测试分类',18,1,0.00,1,'2026-03-04 22:07:01',0),(27,5,'测试规则2',1,'add','测试分类2',19,1,0.00,1,'2026-03-04 22:08:12',0),(28,6,'主动回答问题',2,'add','课堂表现',20,1,0.00,1,'2026-03-04 22:09:42',0),(29,6,'帮助同学',3,'add','课堂表现',20,1,0.00,1,'2026-03-04 22:09:43',0),(30,6,'上课走神',2,'deduct','课堂表现',20,1,0.00,1,'2026-03-04 22:09:43',0),(31,6,'作业按时完成',2,'add','作业学习',21,1,0.00,1,'2026-03-04 22:09:43',0),(32,6,'作业优秀',3,'add','作业学习',21,1,0.00,1,'2026-03-04 22:09:43',0),(33,6,'作业未完成',3,'deduct','作业学习',21,1,0.00,1,'2026-03-04 22:09:43',0),(36,6,'小组合作优秀',5,'add','团队协作',23,1,0.00,1,'2026-03-04 22:09:43',1),(37,6,'小组任务未完成',4,'deduct','团队协作',23,1,0.00,1,'2026-03-04 22:09:43',1),(38,7,'主动回答问题',2,'add','课堂表现',24,1,0.00,1,'2026-03-08 22:29:16',0),(39,7,'帮助同学',3,'add','课堂表现',24,1,0.00,1,'2026-03-08 22:29:16',0),(40,7,'上课走神',2,'deduct','课堂表现',24,1,0.00,1,'2026-03-08 22:29:16',0),(41,7,'作业按时完成',2,'add','作业学习',25,1,0.00,1,'2026-03-08 22:29:16',0),(42,7,'作业优秀',3,'add','作业学习',25,1,0.00,1,'2026-03-08 22:29:16',0),(43,7,'作业未完成',3,'deduct','作业学习',25,1,0.00,1,'2026-03-08 22:29:16',0),(44,7,'课堂纪律良好',3,'add','纪律规范',26,1,0.00,1,'2026-03-08 22:29:16',0),(45,7,'课堂喧哗',2,'deduct','纪律规范',26,1,0.00,1,'2026-03-08 22:29:16',0),(46,7,'小组合作优秀',5,'add','团队协作',27,1,0.00,1,'2026-03-08 22:29:16',1),(47,7,'小组任务未完成',4,'deduct','团队协作',27,1,0.00,1,'2026-03-08 22:29:16',1),(48,8,'主动回答问题',2,'add','课堂表现',28,1,0.00,1,'2026-03-08 22:29:22',0),(49,8,'帮助同学',3,'add','课堂表现',28,1,0.00,1,'2026-03-08 22:29:22',0),(50,8,'上课走神',2,'deduct','课堂表现',28,1,0.00,1,'2026-03-08 22:29:22',0),(51,8,'作业按时完成',2,'add','作业学习',29,1,0.00,1,'2026-03-08 22:29:22',0),(52,8,'作业优秀',3,'add','作业学习',29,1,0.00,1,'2026-03-08 22:29:22',0),(53,8,'作业未完成',3,'deduct','作业学习',29,1,0.00,1,'2026-03-08 22:29:22',0),(54,8,'课堂纪律良好',2,'add','纪律规范',30,1,0.00,1,'2026-03-08 22:29:22',0),(55,8,'课堂喧哗',2,'deduct','纪律规范',30,1,0.00,1,'2026-03-08 22:29:22',0),(56,8,'小组合作优秀',5,'add','团队协作',31,1,0.00,1,'2026-03-08 22:29:22',1),(57,8,'小组任务未完成',4,'deduct','团队协作',31,1,0.00,1,'2026-03-08 22:29:22',1),(58,7,'111',1,'add','通用类型',32,1,0.00,1,'2026-03-09 09:45:00',0);
/*!40000 ALTER TABLE `rule_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shop_item`
--

DROP TABLE IF EXISTS `shop_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cost` int NOT NULL,
  `stock` int DEFAULT '0',
  `icon` varchar(16) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `enabled` tinyint DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shop_class` (`class_id`),
  CONSTRAINT `fk_shop_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shop_item`
--

LOCK TABLES `shop_item` WRITE;
/*!40000 ALTER TABLE `shop_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `shop_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store_item`
--

DROP TABLE IF EXISTS `store_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL COMMENT 'Class ID',
  `name` varchar(64) NOT NULL COMMENT 'Item Name',
  `description` varchar(255) DEFAULT '' COMMENT 'Item Description',
  `cost` int NOT NULL DEFAULT '0' COMMENT 'Point Cost',
  `stock` int NOT NULL DEFAULT '-1' COMMENT 'Stock count, -1 for infinite',
  `icon` varchar(255) DEFAULT '' COMMENT 'Icon identifier or URL',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Whether item is visible in store',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Soft delete flag',
  `create_time` bigint DEFAULT NULL,
  `update_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Class Store Items';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store_item`
--

LOCK TABLES `store_item` WRITE;
/*!40000 ALTER TABLE `store_item` DISABLE KEYS */;
INSERT INTO `store_item` VALUES (1,2,'铅笔','',5,8,'🎁',1,0,NULL,NULL),(2,2,'玩具','',1,0,'🎮',1,0,NULL,NULL),(4,2,'测试','',1,1,'🎁',1,1,NULL,NULL),(5,2,'糖果','',2,-1,'🍬',1,0,NULL,NULL),(6,5,'文具盲盒','可兑换一份学习文具盲盒',30,20,'📦',1,0,1772633201616,1772633201616),(7,5,'免作业券','可抵扣一次当日作业',50,10,'🎫',1,0,1772633201616,1772633201616),(8,5,'课间优先券','下课可优先排队领取奖励',20,-1,'⚡',1,0,1772633201616,1772633201616),(9,5,'班级荣誉贴纸','兑换一张荣誉贴纸',15,-1,'⭐',1,0,1772633201616,1772633201616),(10,5,'神秘礼物','班主任准备的神秘奖励',80,5,'🎁',1,0,1772633201616,1772633201616),(11,6,'文具盲盒','可兑换一份学习文具盲盒',30,20,'📦',1,0,1772633383004,1772633383004),(12,6,'免作业券','可抵扣一次当日作业',50,10,'🎫',1,0,1772633383004,1772633383004),(13,6,'课间优先券','下课可优先排队领取奖励',20,-1,'⚡',1,0,1772633383004,1772633383004),(14,6,'班级荣誉贴纸','兑换一张荣誉贴纸',15,-1,'⭐',1,0,1772633383004,1772633383004),(15,6,'神秘礼物','班主任准备的神秘奖励',80,5,'🎁',1,0,1772633383004,1772633383004),(16,7,'文具盲盒','可兑换一份学习文具盲盒',30,20,'📦',1,0,1772980156489,1772980156489),(17,7,'免作业券','可抵扣一次当日作业',50,10,'🎫',1,0,1772980156489,1772980156489),(18,7,'课间优先券','下课可优先排队领取奖励',20,-1,'⚡',1,0,1772980156489,1772980156489),(19,7,'班级荣誉贴纸','兑换一张荣誉贴纸',15,-1,'⭐',1,0,1772980156489,1772980156489),(20,7,'神秘礼物','班主任准备的神秘奖励',80,5,'🎁',1,0,1772980156489,1772980156489),(21,8,'文具盲盒','可兑换一份学习文具盲盒',30,20,'📦',1,0,1772980162480,1772980162480),(22,8,'免作业券','可抵扣一次当日作业',50,10,'🎫',1,0,1772980162480,1772980162480),(23,8,'课间优先券','下课可优先排队领取奖励',20,-1,'⚡',1,0,1772980162480,1772980162480),(24,8,'班级荣誉贴纸','兑换一张荣誉贴纸',15,-1,'⭐',1,0,1772980162480,1772980162480),(25,8,'神秘礼物','班主任准备的神秘奖励',80,5,'🎁',1,0,1772980162480,1772980162480);
/*!40000 ALTER TABLE `store_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `name` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `student_no` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gender` varchar(8) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `group_id` bigint DEFAULT NULL,
  `total_points` int DEFAULT '0',
  `redeem_points` int DEFAULT '0',
  `exp` int DEFAULT '0',
  `level` int DEFAULT '1',
  `title` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `avatar_image` longtext COLLATE utf8mb4_general_ci,
  `pet_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` bigint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student_class` (`class_id`),
  KEY `idx_student_group` (`group_id`),
  KEY `idx_student_class_total_points` (`class_id`,`total_points`),
  CONSTRAINT `fk_student_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,1,'测试','','女',NULL,25,25,0,1,'星河组',NULL,NULL,1770468154278,'2026-02-07 20:07:44'),(2,1,'测试2','','男',NULL,0,0,0,1,'星河组',NULL,NULL,1770466480056,'2026-02-07 20:14:40'),(21,2,'测试4','','男',NULL,0,0,0,1,'灵兽','','sys_starry_whale',1772871894978,'2026-02-26 20:52:28'),(23,2,'2222','','女',NULL,0,0,0,1,'灵兽','','sys_thunder_wolf',1772159402935,'2026-02-26 21:03:15'),(25,2,'张子轩','20250001','男',NULL,10,3,178,2,'灵兽','','sys_thunder_wolf',1772471513730,'2026-02-26 22:28:54'),(26,2,'李梓涵','20250002','女',NULL,11,11,160,2,'灵兽','','sys_thunder_wolf',1772972309188,'2026-02-26 22:28:55'),(27,2,'王浩然','20250003','女',NULL,10,10,100,2,'灵兽','','sys_thunder_wolf',1772471427487,'2026-02-26 22:28:55'),(28,2,'刘雨桐','20250004','男',NULL,77,77,770,5,'神兽',NULL,'sys_golden_dragon',1773846949449,'2026-02-26 22:28:55'),(29,2,'陈奕辰','20250005','男',NULL,0,0,0,1,'灵兽',NULL,'sys_thunder_wolf',1772871894893,'2026-02-26 22:28:55'),(30,2,'杨思睿','20250006','女',NULL,0,0,0,1,'灵兽',NULL,'sys_golden_dragon',1772871894900,'2026-02-26 22:28:55'),(31,2,'赵欣怡','20250007','男',NULL,4,4,90,1,'灵兽','','sys_thunder_wolf',1772972306786,'2026-02-26 22:28:55'),(32,2,'黄俊熙','20250008','女',NULL,0,0,0,1,'灵兽',NULL,'sys_flame_lion',1772871894903,'2026-02-26 22:28:55'),(33,2,'周梓萱','20250009','男',NULL,0,0,0,1,'灵兽',NULL,'sys_mech_rex',1772871894908,'2026-02-26 22:28:55'),(34,2,'吴嘉诚','20250010','女',NULL,0,0,0,1,'灵兽',NULL,'sys_nimble_mouse',1772871894911,'2026-02-26 22:28:55'),(35,2,'徐子墨','20250011','男',NULL,0,0,0,1,'灵兽',NULL,'sys_frost_unicorn',1772871894914,'2026-02-26 22:28:55'),(36,2,'孙若曦','20250012','男',NULL,0,0,0,1,'灵兽',NULL,'sys_starry_whale',1772871894916,'2026-02-26 22:28:55'),(37,2,'马俊豪','20250013','男',NULL,0,0,0,1,'灵兽',NULL,'sys_moon_deer',1772871894919,'2026-02-26 22:28:55'),(38,2,'朱雨晨','20250014','女',NULL,0,0,0,1,'灵兽',NULL,'sys_sakura_fox',1772871894923,'2026-02-26 22:28:55'),(39,2,'胡宇航','20250015','男',NULL,0,0,0,1,'灵兽',NULL,'sys_mountain_ox',1772871894925,'2026-02-26 22:28:55'),(40,2,'郭欣妍','20250016','男',NULL,0,0,0,1,'灵兽',NULL,'sys_kungfu_panda',1772871894928,'2026-02-26 22:28:55'),(41,2,'何梓轩','20250017','女',NULL,0,0,0,1,'灵兽',NULL,'sys_flame_tiger',1772871894930,'2026-02-26 22:28:55'),(42,2,'林语彤','20250018','女',NULL,0,0,0,1,'灵兽',NULL,'sys_ice_crystal_fox',1772871894936,'2026-02-26 22:28:55'),(43,2,'高子豪','20250019','男',NULL,0,0,0,1,'灵兽',NULL,'sys_frost_dragon_horse',1772871894938,'2026-02-26 22:28:55'),(44,2,'梁欣悦','20250020','男',9,0,0,0,1,'灵兽',NULL,'sys_shadow_lion',1772871894940,'2026-02-26 22:28:55'),(45,2,'郑嘉宁','20250021','男',NULL,0,0,0,1,'灵兽',NULL,'sys_flame_kirin',1772871894944,'2026-02-26 22:28:55'),(46,2,'罗浩宇','20250022','男',9,25,25,250,3,'灵兽',NULL,'sys_sakura_fox',1772881636598,'2026-02-26 22:28:55'),(47,2,'谢梓涵','20250023','女',NULL,0,0,0,1,'灵兽',NULL,'sys_polar_bear',1772871894947,'2026-02-26 22:28:55'),(48,2,'宋佳怡','20250024','男',NULL,0,0,0,1,'灵兽',NULL,'sys_frost_monn_hound',1772871894950,'2026-02-26 22:28:55'),(49,2,'唐宇辰','20250025','男',NULL,0,0,0,1,'灵兽',NULL,'sys_nebula_fox',1772871894952,'2026-02-26 22:28:55'),(50,2,'韩若彤','20250026','男',NULL,0,0,0,1,'灵兽',NULL,'sys_amethyst_kirin',1772871894954,'2026-02-26 22:28:55'),(51,2,'曹俊逸','20250027','女',NULL,0,0,0,1,'灵兽',NULL,'sys_azure_mammoth',1772871894957,'2026-02-26 22:28:55'),(52,2,'彭雨桐','20250028','男',NULL,845,395,40,1,'灵兽','','sys_star_wolf',1773846944414,'2026-02-26 22:28:55'),(53,2,'曾梓豪','20250029','男',NULL,0,0,0,1,'灵兽',NULL,'sys_frost_dew_fox',1772871894958,'2026-02-26 22:28:55'),(54,2,'田语嫣','20250030','男',NULL,6,6,60,1,'灵兽','','sys_golden_dragon',1772549159889,'2026-02-26 22:28:55'),(55,2,'邓嘉豪','20250031','男',NULL,268,238,2178,8,'传说神兽','','sys_thunder_wolf',1773846947238,'2026-02-26 22:28:55'),(56,2,'熊子轩','20250032','女',NULL,32,32,320,3,'灵兽','','sys_thunder_wolf',1772967706327,'2026-02-26 22:28:55'),(57,2,'潘思远','20250033','女',NULL,0,0,0,1,'灵兽',NULL,'sys_rock_crystal_lion',1772871894962,'2026-02-26 22:28:55'),(58,2,'袁若曦','20250034','女',NULL,0,0,0,1,'灵兽',NULL,'sys_thunder_wolf',1772871894964,'2026-02-26 22:28:55'),(59,2,'钟浩然','20250035','男',NULL,1,1,10,1,'灵兽',NULL,'sys_golden_dragon',1772975923143,'2026-02-26 22:28:55'),(60,2,'丁嘉悦','20250036','男',9,0,0,0,1,'灵兽',NULL,'sys_flame_lion',1772871894969,'2026-02-26 22:28:55'),(61,2,'吕宇航','20250037','女',NULL,0,0,0,1,'灵兽',NULL,'sys_mech_rex',1772871894971,'2026-02-26 22:28:55'),(62,2,'许梓涵','20250038','男',NULL,0,0,0,1,'灵兽','','sys_nimble_mouse',1772871894973,'2026-02-26 22:28:55'),(63,2,'赖子墨','20250039','女',NULL,0,0,0,1,'灵兽',NULL,'sys_frost_unicorn',1772871894976,'2026-02-26 22:28:55'),(64,2,'魏欣怡','20250040','男',NULL,159,159,1590,7,'传说神兽','','sys_thunder_wolf',1772975919947,'2026-02-26 22:28:55'),(79,4,'测试2','','男',NULL,8,8,40,1,'灵兽','','sys_moon_deer',1772956777111,'2026-03-03 00:33:51'),(80,4,'测试1','','女',NULL,203,203,2030,8,'传说神兽','','sys_mech_rex',1772956792080,'2026-03-03 00:35:49'),(81,5,'1','','男',NULL,0,0,0,1,'灵兽','','sys_thunder_wolf',1772957557645,'2026-03-08 16:00:53'),(82,5,'2','','男',NULL,1,1,10,1,'灵兽','','sys_golden_dragon',1772960478777,'2026-03-08 16:00:53'),(83,5,'3','','男',NULL,2,2,20,1,'灵兽','','sys_flame_lion',1772960480640,'2026-03-08 16:00:53'),(84,5,'4','','男',NULL,0,0,0,1,'灵兽','','sys_mech_rex',1772957226537,'2026-03-08 16:00:53'),(85,5,'5','','男',NULL,122,2,1385,6,'神兽','','sys_mech_rex',1772961415663,'2026-03-08 16:00:53'),(86,7,'测试学生01','01','男',10,0,0,0,1,'灵兽',NULL,'sys_thunder_wolf',1772980243386,'2026-03-08 22:30:35'),(87,7,'测试学生02','02','女',11,0,0,0,1,'灵兽',NULL,'sys_golden_dragon',1772980243393,'2026-03-08 22:30:35'),(88,7,'测试学生03','03','男',12,0,0,0,1,'灵兽',NULL,'sys_flame_lion',1772980243397,'2026-03-08 22:30:35'),(89,7,'测试学生04','04','女',13,0,0,0,1,'灵兽',NULL,'sys_mech_rex',1772980243400,'2026-03-08 22:30:35'),(90,7,'测试学生05','05','男',14,0,0,0,1,'灵兽',NULL,'sys_nimble_mouse',1772980243404,'2026-03-08 22:30:35'),(91,7,'测试学生06','06','女',10,0,0,0,1,'灵兽',NULL,'sys_frost_unicorn',1772980243408,'2026-03-08 22:30:35'),(92,7,'测试学生07','07','男',11,0,0,0,1,'灵兽',NULL,'sys_starry_whale',1772980243414,'2026-03-08 22:30:35'),(93,7,'测试学生08','08','女',12,0,0,0,1,'灵兽',NULL,'sys_moon_deer',1772980243418,'2026-03-08 22:30:35'),(94,7,'测试学生09','09','男',13,0,0,0,1,'灵兽',NULL,'sys_sakura_fox',1772980243421,'2026-03-08 22:30:35'),(95,7,'测试学生10','10','女',14,0,0,0,1,'灵兽',NULL,'sys_mountain_ox',1772980243425,'2026-03-08 22:30:35'),(96,7,'测试学生11','11','男',10,0,0,0,1,'灵兽',NULL,'sys_kungfu_panda',1772980243429,'2026-03-08 22:30:35'),(97,7,'测试学生12','12','女',11,0,0,0,1,'灵兽',NULL,'sys_flame_tiger',1772980243433,'2026-03-08 22:30:35'),(98,7,'测试学生13','13','男',12,0,0,0,1,'灵兽',NULL,'sys_ice_crystal_fox',1772980243437,'2026-03-08 22:30:35'),(99,7,'测试学生14','14','女',13,0,0,0,1,'灵兽',NULL,'sys_frost_dragon_horse',1772980243442,'2026-03-08 22:30:35'),(100,7,'测试学生15','15','男',14,0,0,0,1,'灵兽',NULL,'sys_shadow_lion',1772980243446,'2026-03-08 22:30:35'),(101,7,'测试学生16','16','女',10,0,0,0,1,'灵兽',NULL,'sys_flame_kirin',1772980243449,'2026-03-08 22:30:35'),(102,7,'测试学生17','17','男',11,0,0,0,1,'灵兽',NULL,'sys_polar_bear',1772980243454,'2026-03-08 22:30:35'),(103,7,'测试学生18','18','女',12,0,0,0,1,'灵兽',NULL,'sys_frost_monn_hound',1772980243458,'2026-03-08 22:30:35'),(104,7,'测试学生19','19','男',13,0,0,0,1,'灵兽',NULL,'sys_nebula_fox',1772980243462,'2026-03-08 22:30:35'),(105,7,'测试学生20','20','女',14,0,0,0,1,'灵兽',NULL,'sys_amethyst_kirin',1772980243465,'2026-03-08 22:30:35'),(106,7,'测试学生21','21','男',10,0,0,0,1,'灵兽',NULL,'sys_azure_mammoth',1772980243469,'2026-03-08 22:30:35'),(107,7,'测试学生22','22','女',11,0,0,0,1,'灵兽',NULL,'sys_frost_dew_fox',1772980243474,'2026-03-08 22:30:35'),(108,7,'测试学生23','23','男',12,0,0,0,1,'灵兽',NULL,'sys_rock_crystal_lion',1772980243477,'2026-03-08 22:30:35'),(109,7,'测试学生24','24','女',13,0,0,0,1,'灵兽',NULL,'sys_warm_dog',1772980243481,'2026-03-08 22:30:35'),(110,7,'测试学生25','25','男',14,0,0,0,1,'灵兽',NULL,'sys_prism_fox',1772980243485,'2026-03-08 22:30:35'),(111,7,'测试学生26','26','女',10,0,0,0,1,'灵兽',NULL,'sys_amber_wolf',1772980243489,'2026-03-08 22:30:35'),(112,7,'测试学生27','27','男',11,0,0,0,1,'灵兽',NULL,'sys_star_wolf',1772980243493,'2026-03-08 22:30:35'),(113,7,'测试学生28','28','女',12,0,0,0,1,'灵兽',NULL,'sys_rain_fox',1772980243496,'2026-03-08 22:30:35'),(114,7,'测试学生29','29','男',13,0,0,0,1,'灵兽',NULL,'sys_moon_fox',1772980243501,'2026-03-08 22:30:35'),(115,7,'测试学生30','30','女',14,0,0,0,1,'灵兽',NULL,'sys_mint_unicorn',1772980243505,'2026-03-08 22:30:35'),(116,7,'测试学生31','31','男',10,10,10,100,2,'灵兽',NULL,'sys_white_flame',1772980288285,'2026-03-08 22:30:35'),(117,7,'测试学生32','32','女',11,0,0,0,1,'灵兽',NULL,'sys_radiantflame_capybara',1772980243511,'2026-03-08 22:30:35'),(118,7,'测试学生33','33','男',12,0,0,0,1,'灵兽',NULL,'sys_frostblade_unicorn',1772980243515,'2026-03-08 22:30:35'),(119,7,'测试学生34','34','女',13,0,0,0,1,'灵兽',NULL,'sys_magma_sugarbeast',1772980243519,'2026-03-08 22:30:35'),(120,7,'测试学生35','35','男',14,0,0,0,1,'灵兽',NULL,'sys_creamblossom_sprite',1772980243523,'2026-03-08 22:30:35'),(121,7,'测试学生36','36','女',10,0,0,0,1,'灵兽',NULL,'sys_thunder_wolf',1772980243526,'2026-03-08 22:30:35'),(122,7,'测试学生37','37','男',11,0,0,0,1,'灵兽',NULL,'sys_golden_dragon',1772980243530,'2026-03-08 22:30:35'),(123,7,'测试学生38','38','女',12,1,1,10,1,'灵兽',NULL,'sys_flame_lion',1772984589391,'2026-03-08 22:30:36'),(124,7,'测试学生39','39','男',13,0,0,0,1,'灵兽',NULL,'sys_mech_rex',1772980243536,'2026-03-08 22:30:36'),(125,7,'测试学生40','40','女',14,0,0,0,1,'灵兽',NULL,'sys_nimble_mouse',1772980243540,'2026-03-08 22:30:36');
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_event`
--

DROP TABLE IF EXISTS `student_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `reason` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `change_value` int NOT NULL,
  `redeem_change` int DEFAULT '0',
  `exp_change` int NOT NULL DEFAULT '0',
  `note` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `timestamp` bigint NOT NULL,
  `revoked` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `rule_id` bigint DEFAULT NULL COMMENT '关联规则ID',
  PRIMARY KEY (`id`),
  KEY `idx_student_event_sid` (`student_id`),
  KEY `idx_student_event_class_ts` (`class_id`,`timestamp`),
  KEY `idx_student_event_class_revoked_ts` (`class_id`,`revoked`,`timestamp`),
  KEY `idx_student_event_rule_id` (`rule_id`),
  KEY `idx_student_event_class_rule_time` (`class_id`,`rule_id`,`timestamp`),
  CONSTRAINT `fk_se_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`),
  CONSTRAINT `fk_se_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=546 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_event`
--

LOCK TABLES `student_event` WRITE;
/*!40000 ALTER TABLE `student_event` DISABLE KEYS */;
INSERT INTO `student_event` VALUES (251,2,55,'手动调整',10,10,0,NULL,1772160926096,0,'2026-02-27 10:55:26',NULL),(252,2,55,'手动调整',10,10,0,NULL,1772160927317,0,'2026-02-27 10:55:27',NULL),(253,2,55,'手动调整',10,10,0,NULL,1772160928562,0,'2026-02-27 10:55:28',NULL),(254,2,55,'按时交作业',3,3,0,NULL,1772160934169,0,'2026-02-27 10:55:34',NULL),(255,2,55,'手动调整',10,10,0,NULL,1772160935686,0,'2026-02-27 10:55:35',NULL),(256,2,55,'手动调整',5,5,0,NULL,1772160938114,0,'2026-02-27 10:55:38',NULL),(257,2,55,'手动调整',5,5,0,NULL,1772160939751,0,'2026-02-27 10:55:39',NULL),(258,2,55,'手动调整',50,50,0,NULL,1772161007936,0,'2026-02-27 10:56:47',NULL),(259,2,55,'手动调整',100,100,0,NULL,1772161039481,0,'2026-02-27 10:57:19',NULL),(260,2,52,'手动调整',10,10,0,NULL,1772163455382,0,'2026-02-27 11:37:35',NULL),(261,2,52,'手动调整',10,10,0,NULL,1772163456587,0,'2026-02-27 11:37:36',NULL),(262,2,52,'手动调整',-10,-10,0,NULL,1772163458973,0,'2026-02-27 11:37:38',NULL),(263,2,52,'手动调整',-10,-10,0,NULL,1772163460524,0,'2026-02-27 11:37:40',NULL),(264,2,52,'手动调整',10,10,0,NULL,1772163466564,0,'2026-02-27 11:37:46',NULL),(265,2,52,'手动调整',10,10,0,NULL,1772163468452,0,'2026-02-27 11:37:48',NULL),(266,2,52,'手动调整',10,10,0,NULL,1772163470098,0,'2026-02-27 11:37:50',NULL),(267,2,52,'手动调整',10,10,0,NULL,1772163471421,0,'2026-02-27 11:37:51',NULL),(268,2,52,'手动调整',10,10,0,NULL,1772163472767,0,'2026-02-27 11:37:52',NULL),(269,2,52,'手动调整',40,40,0,NULL,1772163480802,0,'2026-02-27 11:38:00',NULL),(270,2,52,'手动调整',10,10,0,NULL,1772163482772,0,'2026-02-27 11:38:02',NULL),(271,2,52,'手动调整',10,10,0,NULL,1772163484433,0,'2026-02-27 11:38:04',NULL),(272,2,52,'手动调整',10,10,0,NULL,1772163485546,0,'2026-02-27 11:38:05',NULL),(273,2,52,'手动调整',10,10,0,NULL,1772163486811,0,'2026-02-27 11:38:06',NULL),(274,2,52,'手动调整',10,10,0,NULL,1772163487929,0,'2026-02-27 11:38:07',NULL),(275,2,52,'手动调整',60,60,0,NULL,1772163496041,0,'2026-02-27 11:38:16',NULL),(276,2,52,'手动调整',60,60,0,NULL,1772163500430,0,'2026-02-27 11:38:20',NULL),(277,2,52,'手动调整',10,10,0,NULL,1772163503372,0,'2026-02-27 11:38:23',NULL),(278,2,52,'手动调整',30,30,0,NULL,1772163507105,0,'2026-02-27 11:38:27',NULL),(279,2,52,'手动调整',10,10,0,NULL,1772163508725,0,'2026-02-27 11:38:28',NULL),(280,2,52,'手动调整',10,10,0,NULL,1772163510062,0,'2026-02-27 11:38:30',NULL),(281,2,52,'手动调整',500,500,0,NULL,1772163537677,0,'2026-02-27 11:38:57',NULL),(282,2,25,'手动调整',-1,-1,0,NULL,1772164440635,0,'2026-02-27 11:54:00',NULL),(283,2,64,'小组合作优秀',5,5,0,NULL,1772165295328,0,'2026-02-27 12:08:15',NULL),(284,2,55,'喂小宠物 神秘扭蛋 (EXP+11)',0,-10,0,NULL,1772168726348,0,'2026-02-27 13:05:26',NULL),(285,2,55,'喂小宠物 神秘扭蛋 (EXP-17)',0,-10,0,NULL,1772168731689,0,'2026-02-27 13:05:31',NULL),(286,2,55,'喂小宠物 神秘扭蛋 (EXP+19)',0,-10,0,NULL,1772168741069,0,'2026-02-27 13:05:41',NULL),(287,2,55,'手动调整',1,1,0,NULL,1772171932638,0,'2026-02-27 13:58:52',NULL),(288,2,55,'手动调整',10,10,0,NULL,1772171934107,0,'2026-02-27 13:58:54',NULL),(289,2,55,'手动调整',10,10,0,NULL,1772171935513,0,'2026-02-27 13:58:55',NULL),(290,2,55,'手动调整',10,10,0,NULL,1772171936911,0,'2026-02-27 13:58:56',NULL),(291,2,55,'手动调整',10,10,0,NULL,1772171938521,0,'2026-02-27 13:58:58',NULL),(292,2,55,'手动调整',1,1,0,NULL,1772172383285,0,'2026-02-27 14:06:23',NULL),(293,2,64,'值日认真',3,3,0,NULL,1772172552084,0,'2026-02-27 14:09:12',NULL),(294,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772174898812,0,'2026-02-27 14:48:18',NULL),(295,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772174942636,0,'2026-02-27 14:49:02',NULL),(296,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772174965317,1,'2026-02-27 14:49:25',NULL),(297,2,52,'撤销记录: 九宫格抽奖单抽',0,20,0,'undoFromEventId=296',1772175302069,0,'2026-02-27 14:55:02',NULL),(298,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772176502385,0,'2026-02-27 15:15:02',NULL),(299,2,52,'九宫格奖励：余额福袋',0,10,0,NULL,1772176502386,0,'2026-02-27 15:15:02',NULL),(300,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177437665,0,'2026-02-27 15:30:37',NULL),(301,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177654322,0,'2026-02-27 15:34:14',NULL),(302,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177662833,0,'2026-02-27 15:34:22',NULL),(303,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177669077,0,'2026-02-27 15:34:29',NULL),(304,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177676489,0,'2026-02-27 15:34:36',NULL),(305,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177682212,0,'2026-02-27 15:34:42',NULL),(306,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177688318,0,'2026-02-27 15:34:48',NULL),(307,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177693398,0,'2026-02-27 15:34:53',NULL),(308,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772177700088,0,'2026-02-27 15:35:00',NULL),(309,2,52,'使用道具：魔法糖果 (EXP+25)',0,0,0,NULL,1772177720904,0,'2026-02-27 15:35:20',NULL),(310,2,52,'使用道具：魔法糖果 (EXP+25)',0,0,0,NULL,1772177727196,1,'2026-02-27 15:35:27',NULL),(311,2,52,'使用道具：超级烤肉 (EXP+70)',0,0,0,NULL,1772177739387,1,'2026-02-27 15:35:39',NULL),(312,2,52,'使用道具：余额福袋',0,10,0,NULL,1772177846869,1,'2026-02-27 15:37:26',NULL),(313,2,52,'使用道具：余额福袋',0,10,0,NULL,1772177855457,1,'2026-02-27 15:37:35',NULL),(314,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178703696,0,'2026-02-27 15:51:43',NULL),(315,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178710394,0,'2026-02-27 15:51:50',NULL),(316,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178715626,0,'2026-02-27 15:51:55',NULL),(317,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178722174,0,'2026-02-27 15:52:02',NULL),(318,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178729734,0,'2026-02-27 15:52:09',NULL),(319,2,52,'九宫格抽奖单抽',0,-20,0,NULL,1772178736343,0,'2026-02-27 15:52:16',NULL),(320,2,52,'使用道具：魔法糖果 (EXP+25)',0,0,25,NULL,1772178750051,1,'2026-02-27 15:52:30',NULL),(321,2,52,'使用道具：余额福袋',0,10,0,NULL,1772178784270,1,'2026-02-27 15:53:04',NULL),(322,2,64,'手动调整',5,5,50,NULL,1772249730999,0,'2026-02-28 11:35:31',NULL),(323,2,64,'手动调整',100,100,1000,NULL,1772249738642,0,'2026-02-28 11:35:38',NULL),(324,2,64,'手动调整',10,10,100,NULL,1772249740823,0,'2026-02-28 11:35:40',NULL),(325,2,64,'手动调整',10,10,100,NULL,1772249741727,0,'2026-02-28 11:35:41',NULL),(326,2,64,'手动调整',10,10,100,NULL,1772249742599,0,'2026-02-28 11:35:42',NULL),(327,2,28,'手动调整',10,10,100,NULL,1772252185868,0,'2026-02-28 12:16:25',NULL),(328,2,28,'手动调整',10,10,100,NULL,1772252188452,0,'2026-02-28 12:16:28',NULL),(329,2,28,'手动调整',10,10,100,NULL,1772252189862,0,'2026-02-28 12:16:29',NULL),(330,2,28,'手动调整',10,10,100,NULL,1772252191796,0,'2026-02-28 12:16:31',NULL),(331,2,28,'手动调整',10,10,100,NULL,1772252192847,0,'2026-02-28 12:16:32',NULL),(332,2,28,'手动调整',10,10,100,NULL,1772252205653,0,'2026-02-28 12:16:45',NULL),(333,2,28,'手动调整',10,10,100,NULL,1772252206652,0,'2026-02-28 12:16:46',NULL),(334,2,52,'值日认真',3,3,30,NULL,1772375180417,0,'2026-03-01 22:26:20',NULL),(335,2,52,'上课专心听讲',3,3,30,NULL,1772375182743,0,'2026-03-01 22:26:22',NULL),(336,2,55,'值日认真',3,3,30,NULL,1772381355432,0,'2026-03-02 00:09:15',NULL),(337,2,55,'违反课堂纪律',-5,-5,-50,NULL,1772381367556,0,'2026-03-02 00:09:27',NULL),(338,2,28,'课堂纪律良好',3,3,30,NULL,1772381377064,0,'2026-03-02 00:09:37',NULL),(339,2,56,'值日认真',3,3,30,NULL,1772381953733,0,'2026-03-02 00:19:13',NULL),(340,2,56,'小组合作优秀',5,5,50,NULL,1772382145620,0,'2026-03-02 00:22:25',NULL),(341,2,56,'违反课堂纪律',-5,-5,-50,NULL,1772382181683,0,'2026-03-02 00:23:01',NULL),(342,2,56,'上课迟到',-3,-3,-30,NULL,1772382194281,0,'2026-03-02 00:23:14',NULL),(343,2,56,'上课迟到',-3,-3,-30,NULL,1772382201396,0,'2026-03-02 00:23:21',NULL),(344,2,56,'课堂纪律良好',3,3,30,NULL,1772382209974,0,'2026-03-02 00:23:29',NULL),(345,2,56,'值日认真',3,3,30,NULL,1772382258864,0,'2026-03-02 00:24:18',NULL),(346,2,56,'值日认真',3,3,30,NULL,1772382260708,0,'2026-03-02 00:24:20',NULL),(347,2,56,'小组合作优秀',5,5,50,NULL,1772382262183,0,'2026-03-02 00:24:22',NULL),(348,2,56,'值日认真',3,3,30,NULL,1772382267897,0,'2026-03-02 00:24:27',NULL),(349,2,56,'值日认真',3,3,30,NULL,1772382284565,0,'2026-03-02 00:24:44',NULL),(350,2,56,'上课迟到',-3,-3,-30,NULL,1772382288449,0,'2026-03-02 00:24:48',NULL),(351,2,56,'违反课堂纪律',-5,-5,-50,NULL,1772382292831,0,'2026-03-02 00:24:52',NULL),(352,2,56,'上课迟到',-3,-3,-30,NULL,1772382294682,0,'2026-03-02 00:24:54',NULL),(353,2,56,'作业完成优秀',10,10,100,NULL,1772382309286,0,'2026-03-02 00:25:09',NULL),(354,2,56,'小组合作优秀',5,5,50,NULL,1772382316065,0,'2026-03-02 00:25:16',NULL),(355,2,56,'帮助同学',8,8,80,NULL,1772382317812,0,'2026-03-02 00:25:17',NULL),(356,2,56,'作业完成优秀',10,10,100,NULL,1772382320753,0,'2026-03-02 00:25:20',NULL),(357,2,56,'值日认真',3,3,30,NULL,1772382508417,0,'2026-03-02 00:28:28',NULL),(358,2,56,'课堂走神',-2,-2,-20,NULL,1772382510236,0,'2026-03-02 00:28:30',NULL),(359,2,56,'作业未交',-5,-5,-50,NULL,1772382511700,0,'2026-03-02 00:28:31',NULL),(360,2,56,'小组合作优秀',5,5,50,NULL,1772382513147,0,'2026-03-02 00:28:33',NULL),(361,2,56,'按时交作业',3,3,30,NULL,1772382514319,0,'2026-03-02 00:28:34',NULL),(362,2,52,'上课迟到',-3,-3,-30,NULL,1772382533033,0,'2026-03-02 00:28:53',NULL),(363,2,52,'值日认真',3,3,30,NULL,1772382535416,0,'2026-03-02 00:28:55',NULL),(364,2,52,'上课迟到',-3,-3,-30,NULL,1772382537322,0,'2026-03-02 00:28:57',NULL),(365,2,52,'主动回答问题',5,5,50,NULL,1772382740569,0,'2026-03-02 00:32:20',NULL),(366,2,52,'上课迟到',-3,-3,-30,NULL,1772382742554,0,'2026-03-02 00:32:22',NULL),(367,2,52,'值日认真',3,3,30,NULL,1772382744070,0,'2026-03-02 00:32:24',NULL),(368,2,52,'违反课堂纪律',-5,-5,-50,NULL,1772382745869,0,'2026-03-02 00:32:25',NULL),(369,2,52,'值日认真',3,3,30,NULL,1772382747742,0,'2026-03-02 00:32:27',NULL),(370,2,52,'课堂纪律良好',3,3,30,NULL,1772382749347,0,'2026-03-02 00:32:29',NULL),(371,2,52,'作业未交',-5,-5,-50,NULL,1772382750640,0,'2026-03-02 00:32:30',NULL),(372,2,55,'作业未交',-5,-5,-50,NULL,1772382752074,0,'2026-03-02 00:32:32',NULL),(373,2,64,'课堂纪律良好',3,3,30,NULL,1772382753860,0,'2026-03-02 00:32:33',NULL),(374,2,56,'值日认真',3,3,30,NULL,1772382761410,0,'2026-03-02 00:32:41',NULL),(375,2,56,'上课专心听讲',3,3,30,NULL,1772382762978,0,'2026-03-02 00:32:42',NULL),(376,2,56,'上课迟到',-3,-3,-30,NULL,1772382764117,0,'2026-03-02 00:32:44',NULL),(377,2,56,'课堂走神',-2,-2,-20,NULL,1772382765189,0,'2026-03-02 00:32:45',NULL),(378,2,56,'作业未交',-5,-5,-50,NULL,1772382766421,0,'2026-03-02 00:32:46',NULL),(379,2,56,'违反课堂纪律',-5,-5,-50,NULL,1772382770298,0,'2026-03-02 00:32:50',NULL),(380,2,56,'违反课堂纪律',-5,-5,-50,NULL,1772382778269,0,'2026-03-02 00:32:58',NULL),(381,2,56,'课堂走神',-2,-2,-20,NULL,1772382802649,0,'2026-03-02 00:33:22',NULL),(382,2,56,'课堂走神',-2,-2,-20,NULL,1772382805722,0,'2026-03-02 00:33:25',NULL),(383,2,64,'上课迟到',-3,-3,-30,NULL,1772382869874,0,'2026-03-02 00:34:29',NULL),(384,2,54,'课堂走神',-2,-2,-20,NULL,1772382881143,0,'2026-03-02 00:34:41',NULL),(385,2,64,'课堂走神',-2,-2,-20,NULL,1772382881148,0,'2026-03-02 00:34:41',NULL),(386,2,54,'值日认真',3,3,30,NULL,1772382892131,0,'2026-03-02 00:34:52',NULL),(387,2,64,'值日认真',3,3,30,NULL,1772382892136,0,'2026-03-02 00:34:52',NULL),(388,2,56,'值日认真',3,3,30,NULL,1772383172727,0,'2026-03-02 00:39:32',NULL),(389,2,56,'课堂走神',-2,-2,-20,NULL,1772383174492,0,'2026-03-02 00:39:34',NULL),(390,2,56,'小组合作优秀',5,5,50,NULL,1772383175756,0,'2026-03-02 00:39:35',NULL),(391,2,56,'上课迟到',-3,-3,-30,NULL,1772383177621,0,'2026-03-02 00:39:37',NULL),(392,2,56,'小组合作优秀',5,5,50,NULL,1772383431037,0,'2026-03-02 00:43:51',NULL),(393,2,56,'上课迟到',-3,-3,-30,NULL,1772383432606,0,'2026-03-02 00:43:52',NULL),(394,2,56,'课堂走神',-2,-2,-20,NULL,1772383433878,0,'2026-03-02 00:43:53',NULL),(398,4,79,'手动调整',-5,0,0,'expExact=1',1772469612043,1,'2026-03-03 00:40:12',NULL),(399,4,79,'手动调整',-5,0,0,'expExact=1',1772469621027,0,'2026-03-03 00:40:21',NULL),(400,4,79,'手动调整',10,5,100,'expExact=1',1772469623006,0,'2026-03-03 00:40:23',NULL),(401,4,79,'手动调整',-1,-1,-10,'expExact=1',1772469625928,1,'2026-03-03 00:40:25',NULL),(402,4,79,'手动调整',-10,-4,-90,'expExact=1',1772469634724,1,'2026-03-03 00:40:34',NULL),(403,4,79,'手动调整',-1,-1,-10,'expExact=1',1772469721678,0,'2026-03-03 00:42:01',NULL),(404,4,79,'手动调整',600,600,6000,'expExact=1',1772469734752,1,'2026-03-03 00:42:14',NULL),(405,4,79,'手动调整',-1,-1,0,'expExact=1',1772469748654,1,'2026-03-03 00:42:28',NULL),(406,4,79,'手动调整',-10,-10,0,'expExact=1',1772469760395,1,'2026-03-03 00:42:40',NULL),(407,4,79,'手动调整',-10,-4,0,'expExact=1',1772469841906,1,'2026-03-03 00:44:01',NULL),(408,2,52,'值日认真',3,3,30,'expExact=1',1772471110323,0,'2026-03-03 01:05:10',NULL),(409,2,52,'手动调整',1,1,10,'expExact=1',1772471113331,0,'2026-03-03 01:05:13',NULL),(410,2,52,'值日认真',3,3,30,'expExact=1',1772471182067,0,'2026-03-03 01:06:22',NULL),(411,2,52,'小组合作优秀',5,5,50,'expExact=1',1772471263679,0,'2026-03-03 01:07:43',NULL),(412,2,52,'手动调整',1,1,10,'expExact=1',1772471266322,0,'2026-03-03 01:07:46',NULL),(413,2,52,'值日认真',3,3,30,'expExact=1',1772471358703,0,'2026-03-03 01:09:18',13),(414,2,25,'违反课堂纪律',-5,0,0,'expExact=1',1772471408178,0,'2026-03-03 01:10:08',10),(415,2,25,'帮助同学',8,2,80,'expExact=1',1772471410645,0,'2026-03-03 01:10:10',11),(416,2,31,'违反课堂纪律',-5,0,0,'expExact=1',1772471418405,0,'2026-03-03 01:10:18',10),(417,2,31,'帮助同学',8,3,80,'expExact=1',1772471420259,0,'2026-03-03 01:10:20',11),(418,2,26,'违反课堂纪律',-5,0,0,'expExact=1',1772471423190,0,'2026-03-03 01:10:23',10),(419,2,26,'帮助同学',8,3,80,'expExact=1',1772471425041,0,'2026-03-03 01:10:25',11),(420,2,27,'作业完成优秀',10,10,100,'expExact=1',1772471427490,0,'2026-03-03 01:10:27',5),(421,2,26,'作业未交',-5,-3,-50,'expExact=1',1772471447273,0,'2026-03-03 01:10:47',7),(422,2,26,'上课专心听讲',3,1,30,'expExact=1',1772471460711,0,'2026-03-03 01:11:00',3),(423,2,25,'上课迟到',-3,-2,-30,'expExact=1',1772471476775,0,'2026-03-03 01:11:16',9),(424,2,25,'值日认真',3,2,30,'expExact=1',1772471478810,0,'2026-03-03 01:11:18',13),(425,2,25,'作业未交',-5,-2,-50,'expExact=1',1772471481261,0,'2026-03-03 01:11:21',7),(426,2,25,'作业完成优秀',10,7,100,'expExact=1',1772471485205,0,'2026-03-03 01:11:25',5),(427,2,25,'作业完成优秀',10,10,100,'expExact=1',1772471495817,0,'2026-03-03 01:11:35',5),(428,2,25,'喂小宠物 神秘扭蛋 (EXP+18)',0,-10,18,NULL,1772471499731,0,'2026-03-03 01:11:39',NULL),(429,2,25,'作业未交',-5,-5,-50,'expExact=1',1772471505708,0,'2026-03-03 01:11:45',7),(430,2,25,'作业未交',-5,-2,-50,'expExact=1',1772471507362,0,'2026-03-03 01:11:47',7),(431,2,25,'值日认真',3,3,30,'expExact=1',1772471513734,0,'2026-03-03 01:11:53',13),(432,2,54,'值日认真',3,3,30,'expExact=1',1772549159896,0,'2026-03-03 22:45:59',13),(433,2,46,'手动调整',100,100,1000,'expExact=1',1772634082634,0,'2026-03-04 22:21:22',NULL),(434,2,46,'手动调整',100,100,1000,'expExact=1',1772634107390,0,'2026-03-04 22:21:47',NULL),(435,2,52,'违反课堂纪律',-3,-3,-30,'expExact=1',1772870145220,0,'2026-03-07 15:55:45',10),(436,2,52,'违反课堂纪律',-3,-3,-30,'expExact=1',1772870253946,1,'2026-03-07 15:57:33',10),(437,2,52,'违反课堂纪律',-3,-3,-30,'expExact=1',1772870293156,1,'2026-03-07 15:58:13',10),(438,2,52,'违反课堂纪律',-3,-3,-30,'expExact=1',1772870333451,1,'2026-03-07 15:58:53',10),(439,2,52,'违反课堂纪律',-3,-3,-30,'expExact=1',1772870336327,1,'2026-03-07 15:58:56',10),(440,2,46,'作业未交',-5,-5,-50,'expExact=1',1772881615616,0,'2026-03-07 19:06:55',7),(441,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881616785,0,'2026-03-07 19:06:56',NULL),(442,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881618559,0,'2026-03-07 19:06:58',NULL),(443,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881619519,0,'2026-03-07 19:06:59',NULL),(444,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881620710,0,'2026-03-07 19:07:00',NULL),(445,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881621606,0,'2026-03-07 19:07:01',NULL),(446,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881622473,0,'2026-03-07 19:07:02',NULL),(447,2,46,'手动调整',-10,-10,-100,'expExact=1',1772881629911,0,'2026-03-07 19:07:09',NULL),(448,2,46,'手动调整',-100,-100,-1000,'expExact=1',1772881636605,0,'2026-03-07 19:07:16',NULL),(449,4,80,'手动调整',100,100,1000,'expExact=1',1772882284425,0,'2026-03-07 19:18:04',NULL),(450,4,80,'手动调整',100,100,1000,'expExact=1',1772884091049,0,'2026-03-07 19:48:11',NULL),(451,4,80,'手动调整',-100,-100,-1000,'expExact=1',1772884241873,0,'2026-03-07 19:50:41',NULL),(452,4,80,'手动调整',100,100,1000,'expExact=1',1772884472235,0,'2026-03-07 19:54:32',NULL),(453,4,80,'手动调整',1,1,10,'expExact=1',1772956773311,0,'2026-03-08 15:59:33',NULL),(454,4,79,'手动调整',5,5,50,'expExact=1',1772956775563,0,'2026-03-08 15:59:35',NULL),(455,4,79,'手动调整',-1,-1,-10,'expExact=1',1772956777114,0,'2026-03-08 15:59:37',NULL),(456,4,80,'手动调整',1,1,10,'expExact=1',1772956779206,0,'2026-03-08 15:59:39',NULL),(457,4,80,'手动调整',1,1,10,'expExact=1',1772956792083,0,'2026-03-08 15:59:52',NULL),(458,5,85,'测试规则2',1,1,10,'expExact=1',1772958153816,0,'2026-03-08 16:22:33',27),(459,5,85,'测试规则2',1,1,10,'expExact=1',1772958156934,0,'2026-03-08 16:22:36',27),(460,5,85,'测试规则2',1,1,10,'expExact=1',1772958302949,0,'2026-03-08 16:25:02',27),(461,5,85,'帮助同学',3,3,30,'expExact=1',1772958306877,0,'2026-03-08 16:25:06',17),(462,5,85,'测试规则2',1,1,10,'expExact=1',1772958335576,0,'2026-03-08 16:25:35',27),(463,5,85,'测试规则2',1,1,10,'expExact=1',1772958346680,0,'2026-03-08 16:25:46',27),(464,5,85,'课堂喧哗',-2,-2,-20,'expExact=1',1772958349439,0,'2026-03-08 16:25:49',23),(465,5,85,'手动调整',100,100,1000,'expExact=1',1772958356069,0,'2026-03-08 16:25:56',NULL),(466,5,85,'测试规则2',1,1,10,'expExact=1',1772958538936,0,'2026-03-08 16:28:58',27),(467,5,85,'测试规则2',1,1,10,'expExact=1',1772958542885,0,'2026-03-08 16:29:02',27),(468,5,85,'帮助同学',3,3,30,'expExact=1',1772958719497,0,'2026-03-08 16:31:59',17),(469,5,85,'测试规则2',1,1,10,'expExact=1',1772958730951,0,'2026-03-08 16:32:10',27),(470,5,85,'测试规则2',1,1,10,'expExact=1',1772959521954,0,'2026-03-08 16:45:21',27),(471,5,85,'测试规则2',1,1,10,'expExact=1',1772959528227,0,'2026-03-08 16:45:28',27),(472,5,85,'测试规则2',1,1,10,'expExact=1',1772959532885,0,'2026-03-08 16:45:32',27),(473,5,85,'测试规则2',1,1,10,'expExact=1',1772959812181,0,'2026-03-08 16:50:12',27),(474,5,85,'测试规则2',1,1,10,'expExact=1',1772959830172,0,'2026-03-08 16:50:30',27),(475,5,85,'测试规则2',1,1,10,'expExact=1',1772959833059,0,'2026-03-08 16:50:33',27),(476,5,85,'测试规则2',1,1,10,'expExact=1',1772959837106,0,'2026-03-08 16:50:37',27),(477,5,85,'测试规则2',1,1,10,'expExact=1',1772959922449,0,'2026-03-08 16:52:02',27),(478,5,85,'测试规则2',1,1,10,'expExact=1',1772960472536,0,'2026-03-08 17:01:12',27),(479,5,83,'测试规则2',1,1,10,'expExact=1',1772960476180,0,'2026-03-08 17:01:16',27),(480,5,82,'测试规则2',1,1,10,'expExact=1',1772960478781,0,'2026-03-08 17:01:18',27),(481,5,83,'测试规则2',1,1,10,'expExact=1',1772960480645,0,'2026-03-08 17:01:20',27),(482,5,85,'测试规则2',1,1,10,'expExact=1',1772960483388,0,'2026-03-08 17:01:23',27),(483,5,85,'喂小宠物 魔法糖果 (EXP+25)',0,-20,25,NULL,1772960752819,0,'2026-03-08 17:05:52',NULL),(484,5,85,'喂小宠物 超级烤肉 (EXP+70)',0,-50,70,NULL,1772960756133,0,'2026-03-08 17:05:56',NULL),(485,5,85,'喂小宠物 超级烤肉 (EXP+70)',0,-50,70,NULL,1772961415665,0,'2026-03-08 17:16:55',NULL),(486,2,52,'测试规则',1,1,10,'expExact=1',1772966661624,0,'2026-03-08 18:44:21',15),(487,2,55,'手动调整',1,1,10,'expExact=1',1772966665722,0,'2026-03-08 18:44:25',NULL),(488,2,64,'手动调整',1,1,10,'expExact=1',1772966668115,0,'2026-03-08 18:44:28',NULL),(489,2,55,'测试规则',1,1,10,'expExact=1',1772966873128,0,'2026-03-08 18:47:53',15),(490,2,55,'测试规则',1,1,10,'expExact=1',1772966876091,0,'2026-03-08 18:47:56',15),(491,2,55,'测试规则',1,1,10,'expExact=1',1772966878539,0,'2026-03-08 18:47:58',15),(492,2,55,'测试规则',1,1,10,'expExact=1',1772966880985,0,'2026-03-08 18:48:00',15),(493,2,55,'测试规则',1,1,10,'expExact=1',1772967502460,0,'2026-03-08 18:58:22',15),(494,2,55,'测试规则',1,1,10,'expExact=1',1772967505031,0,'2026-03-08 18:58:25',15),(495,2,55,'测试规则',1,1,10,'expExact=1',1772967580695,0,'2026-03-08 18:59:40',15),(496,2,64,'小组合作优秀',5,5,50,'expExact=1',1772967584050,0,'2026-03-08 18:59:44',12),(497,2,64,'测试规则',1,1,10,'expExact=1',1772967589673,0,'2026-03-08 18:59:49',15),(498,2,64,'违反课堂纪律',-3,-3,-30,'expExact=1',1772967593309,0,'2026-03-08 18:59:53',10),(499,2,55,'测试规则',1,1,10,'expExact=1',1772967698212,0,'2026-03-08 19:01:38',15),(500,2,64,'测试规则',1,1,10,'expExact=1',1772967700966,0,'2026-03-08 19:01:40',15),(501,2,28,'测试规则',1,1,10,'expExact=1',1772967703562,0,'2026-03-08 19:01:43',15),(502,2,56,'测试规则',1,1,10,'expExact=1',1772967706329,0,'2026-03-08 19:01:46',15),(503,2,52,'测试规则',1,1,10,'expExact=1',1772967793081,0,'2026-03-08 19:03:13',15),(504,2,55,'值日认真',3,3,30,'expExact=1',1772967794934,0,'2026-03-08 19:03:14',13),(505,2,64,'小组合作优秀',5,5,50,'expExact=1',1772967797585,0,'2026-03-08 19:03:17',12),(506,2,52,'测试规则',1,1,10,'expExact=1',1772967850213,0,'2026-03-08 19:04:10',15),(507,2,64,'测试规则',1,1,10,'expExact=1',1772968049828,0,'2026-03-08 19:07:29',15),(508,2,55,'测试规则',1,1,10,'expExact=1',1772968335563,0,'2026-03-08 19:12:15',15),(509,2,55,'测试规则',1,1,10,'expExact=1',1772969917884,0,'2026-03-08 19:38:37',15),(510,2,55,'测试规则',1,1,10,'expExact=1',1772971999320,0,'2026-03-08 20:13:19',15),(511,2,64,'测试规则',1,1,10,'expExact=1',1772972001787,0,'2026-03-08 20:13:21',15),(512,2,28,'测试规则',1,1,10,'expExact=1',1772972003781,0,'2026-03-08 20:13:23',15),(513,2,26,'测试规则',1,1,10,'expExact=1',1772972301656,0,'2026-03-08 20:18:21',15),(514,2,26,'测试规则',1,1,10,'expExact=1',1772972303877,0,'2026-03-08 20:18:23',15),(515,2,31,'测试规则',1,1,10,'expExact=1',1772972306790,0,'2026-03-08 20:18:26',15),(516,2,26,'帮助同学',8,8,80,'expExact=1',1772972309191,0,'2026-03-08 20:18:29',11),(517,2,55,'测试规则',1,1,10,'expExact=1',1772973315211,0,'2026-03-08 20:35:15',15),(518,2,55,'测试规则',1,1,10,'expExact=1',1772973317285,0,'2026-03-08 20:35:17',15),(519,2,55,'测试规则',1,1,10,'expExact=1',1772973319550,0,'2026-03-08 20:35:19',15),(520,2,55,'按时交作业',3,3,30,'expExact=1',1772973320547,0,'2026-03-08 20:35:20',6),(521,2,55,'违反课堂纪律',-3,-3,-30,'expExact=1',1772973323369,0,'2026-03-08 20:35:23',10),(522,2,52,'测试规则',1,1,10,'expExact=1',1772973883212,0,'2026-03-08 20:44:43',15),(523,2,55,'测试规则',1,1,10,'expExact=1',1772973885924,0,'2026-03-08 20:44:45',15),(524,2,55,'测试规则',1,1,10,'expExact=1',1772973888167,0,'2026-03-08 20:44:48',15),(525,2,55,'测试规则',1,1,10,'expExact=1',1772975798028,0,'2026-03-08 21:16:38',15),(526,2,64,'测试规则',1,1,10,'expExact=1',1772975800663,0,'2026-03-08 21:16:40',15),(527,2,28,'测试规则',1,1,10,'expExact=1',1772975808439,0,'2026-03-08 21:16:48',15),(528,2,64,'测试规则',1,1,10,'expExact=1',1772975811126,0,'2026-03-08 21:16:51',15),(529,2,64,'测试规则',1,1,10,'expExact=1',1772975919950,0,'2026-03-08 21:18:39',15),(530,2,59,'测试规则',1,1,10,'expExact=1',1772975923147,0,'2026-03-08 21:18:43',15),(531,2,55,'测试规则',1,1,10,'expExact=1',1772975926292,0,'2026-03-08 21:18:46',15),(532,2,55,'测试规则',1,1,10,'expExact=1',1772975928196,0,'2026-03-08 21:18:48',15),(533,2,55,'测试规则',1,1,10,'expExact=1',1772975932677,0,'2026-03-08 21:18:52',15),(534,2,55,'测试规则',1,1,10,'expExact=1',1772975934638,0,'2026-03-08 21:18:54',15),(535,2,52,'喂小宠物 遗忘果实',0,-100,0,'noUndo=1',1772978064410,0,'2026-03-08 21:54:24',NULL),(536,7,116,'课堂纪律良好',2,2,20,'expExact=1',1772980277457,0,'2026-03-08 22:31:17',44),(537,7,116,'课堂纪律良好',2,2,20,'expExact=1',1772980279902,0,'2026-03-08 22:31:19',44),(538,7,116,'课堂纪律良好',2,2,20,'expExact=1',1772980282908,0,'2026-03-08 22:31:22',44),(539,7,116,'课堂纪律良好',2,2,20,'expExact=1',1772980284990,0,'2026-03-08 22:31:24',44),(540,7,116,'课堂纪律良好',2,2,20,'expExact=1',1772980288288,0,'2026-03-08 22:31:28',44),(541,7,123,'1',1,1,10,'expExact=1',1772984589397,0,'2026-03-08 23:43:09',NULL),(542,2,52,'测试规则',1,1,10,'expExact=1',1773846941674,0,'2026-03-18 23:15:41',15),(543,2,52,'值日认真',3,3,30,'expExact=1',1773846944416,0,'2026-03-18 23:15:44',13),(544,2,55,'小组合作优秀',5,5,50,'expExact=1',1773846947242,0,'2026-03-18 23:15:47',12),(545,2,28,'测试规则',1,1,10,'expExact=1',1773846949452,0,'2026-03-18 23:15:49',15);
/*!40000 ALTER TABLE `student_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_inventory`
--

DROP TABLE IF EXISTS `student_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `item_code` varchar(64) NOT NULL,
  `item_name` varchar(128) NOT NULL,
  `rarity` varchar(32) NOT NULL,
  `quantity` int NOT NULL DEFAULT '0',
  `status` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inventory_student_item` (`student_id`,`item_code`),
  KEY `idx_inventory_student` (`student_id`),
  KEY `idx_inventory_class` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_inventory`
--

LOCK TABLES `student_inventory` WRITE;
/*!40000 ALTER TABLE `student_inventory` DISABLE KEYS */;
INSERT INTO `student_inventory` VALUES (1,2,52,'CANDY','魔法糖果','common',0,'USED',1772177437665,1772178750052),(2,2,52,'CLEAN_PASS','劳动豁免券','common',2,'ACTIVE',1772177654322,1772178778262),(3,2,52,'BALANCE_BAG','余额福袋','common',1,'ACTIVE',1772177682212,1772178784270),(4,2,52,'MEAT','超级烤肉','rare',0,'USED',1772177693398,1772177739388),(5,2,52,'SPEAK_STICK','发言指定棒','epic',0,'USED',1772178703696,1772178771129);
/*!40000 ALTER TABLE `student_inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_pet_gallery`
--

DROP TABLE IF EXISTS `student_pet_gallery`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_pet_gallery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `pet_route_id` varchar(64) NOT NULL COMMENT '宠物路线ID',
  `pet_name` varchar(128) NOT NULL COMMENT '宠物名字',
  `unlock_time` bigint NOT NULL COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  KEY `idx_class_student` (`class_id`,`student_id`),
  KEY `idx_student_pet` (`student_id`,`pet_route_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生宠物图鉴表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_pet_gallery`
--

LOCK TABLES `student_pet_gallery` WRITE;
/*!40000 ALTER TABLE `student_pet_gallery` DISABLE KEYS */;
INSERT INTO `student_pet_gallery` VALUES (1,2,7,'sys_golden_dragon','金光龙',1772104133536),(2,2,20,'sys_golden_dragon','金光龙',1772110374981),(3,2,24,'sys_thunder_wolf','雷霆狼',1772121593643),(4,2,62,'sys_thunder_wolf','雷霆狼',1772121631463),(5,2,64,'sys_golden_dragon','金光龙',1772121989867),(6,2,21,'sys_golden_dragon','金光龙',1772159392856),(7,2,52,'sys_golden_dragon','金光龙',1772163510052),(8,2,52,'sys_thunder_wolf','雷霆狼',1772163537645),(9,4,78,'sys_thunder_wolf','雷霆狼',1772469328055),(10,4,79,'sys_thunder_wolf','雷霆狼',1772469734743);
/*!40000 ALTER TABLE `student_pet_gallery` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher`
--

DROP TABLE IF EXISTS `teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `nickname` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` tinyint DEFAULT '1',
  `screen_lock_hash` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'Screen lock password hash',
  `screen_lock_enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether screen lock password is enabled',
  `license_expires_at` datetime DEFAULT NULL COMMENT 'License expires at; NULL means permanent',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher`
--

LOCK TABLES `teacher` WRITE;
/*!40000 ALTER TABLE `teacher` DISABLE KEYS */;
INSERT INTO `teacher` VALUES (1,'123456','eg4L7irf8nIflUcEW17AeA==:8QHwUjYgzC2t6OUrpvrtf+i/Bjq4PpV5Y6Ota4JA+Lg=','123456',1,NULL,0,NULL,'2026-02-07 18:24:31'),(2,'111111','$2a$10$E8ytofUPdp2L2f7JjuN5fOZdrpeIZK8qKtOAd2KIptbbEUu8Is2BW','李老师',1,'$2a$10$Pp1tBRm.cZFPRlWvLm6KIeGOq3ELylHZ.OTi1nszi8TTTv0.xfvlS',1,NULL,'2026-02-07 20:51:12'),(3,'222222','$2a$10$dHwPN2dLz4JKmLak8z2l6uKFZpwJ5tFX8aULCAv0/6Zmt4SPG2QZq','222222',1,NULL,0,'2027-02-23 12:18:21','2026-02-23 12:18:20'),(4,'333333','$2a$10$hmJq3vwoxeRvkd7nvmhj0O80lgZsxAJUqNtBXMF5TxnnZkRj5wHRC','333333',1,NULL,0,'2027-02-24 17:21:45','2026-02-24 17:21:44'),(5,'1234567','$2a$10$mwNMGvGXnQYP2eLfbC56TOt.ChfH3qulBlK1Cr2Zf8e46LynPzM4S','1234567',1,NULL,0,'2029-03-20 23:51:52','2026-03-03 23:51:52'),(6,'200014','$2a$10$5qLKsSQga0an4o7nVvcomOm0guRfpjMFT3rZXjQxlwGewrg.hw12i','200014',1,NULL,0,'2026-03-04 23:55:12','2026-03-03 23:55:12');
/*!40000 ALTER TABLE `teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `teacher_device_token`
--

DROP TABLE IF EXISTS `teacher_device_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `teacher_device_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teacher_id` bigint NOT NULL COMMENT 'Teacher ID',
  `device_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Device Type: PC, MOBILE, TABLET',
  `token` varchar(512) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Active Token',
  `last_login_time` datetime DEFAULT NULL COMMENT 'Last Login Time',
  PRIMARY KEY (`id`),
  KEY `idx_teacher_device` (`teacher_id`,`device_type`)
) ENGINE=InnoDB AUTO_INCREMENT=91 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Teacher Device Login Tokens';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `teacher_device_token`
--

LOCK TABLES `teacher_device_token` WRITE;
/*!40000 ALTER TABLE `teacher_device_token` DISABLE KEYS */;
INSERT INTO `teacher_device_token` VALUES (19,3,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMjIyMjIiLCJ0ZWFjaGVySWQiOjMsImlhdCI6MTc3MTgyMDMwMCwiZXhwIjoxNzcxODM0NzAwfQ.JNaFafCz4jvSAZV8WIe1039SuCRpxPP-cWxy0ObB13o','2026-02-23 12:18:21'),(28,4,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIzMzMzMzMiLCJ0ZWFjaGVySWQiOjQsImlhdCI6MTc3MTkyNDkwNCwiZXhwIjoxNzcxOTM5MzA0fQ.Nj7WK7OGDmhQBtfK0-9KjRqyCZBLiNcoWcl4f2OnJFQ','2026-02-24 17:21:45'),(50,2,'MOBILE','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTExMTEiLCJ0ZWFjaGVySWQiOjIsImlhdCI6MTc3MjI5NzgxNiwiZXhwIjoxNzcyMzEyMjE2fQ.MtmO48XOQcS_qTJVLUQhwPJelcpS20vMdgVSpQyu84w','2026-03-01 00:56:57'),(57,5,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3IiwidGVhY2hlcklkIjo1LCJpYXQiOjE3NzI1NTMxMTIsImV4cCI6MTc3MjU2NzUxMn0.sZkeuAvlaJu6AOc_GHYWF-SS572J9Z4CUEE9NLM53Og','2026-03-03 23:51:52'),(60,6,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMDAwMTQiLCJ0ZWFjaGVySWQiOjYsImlhdCI6MTc3MjU1MzMxMiwiZXhwIjoxNzcyNTY3NzEyfQ.DuTDbI-Kx4RVfRp12NfH-NUfowTccTYqNE-tP5hrX40','2026-03-03 23:55:12'),(61,6,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyMDAwMTQiLCJ0ZWFjaGVySWQiOjYsImlhdCI6MTc3MjU1MzMxNiwiZXhwIjoxNzcyNTY3NzE2fQ.IfvaC0INIFg-88xSjzR4E2apotih2rNRuKaASrzcLEw','2026-03-03 23:55:16'),(89,2,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTExMTEiLCJ0ZWFjaGVySWQiOjIsImlhdCI6MTc3MzIyOTQyMywiZXhwIjoxNzczMjQzODIzfQ.zlo7B2U360b-LyoUPSqR1jfZeSEvkaEGyeV7K1sJECs','2026-03-11 19:43:43'),(90,2,'PC','eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTExMTEiLCJ0ZWFjaGVySWQiOjIsImlhdCI6MTc3Mzg0NjM1MSwiZXhwIjoxNzczODYwNzUxfQ.Ngd5nYhcEE79BqSgCc0KLdSuAU8-cy_zQB_6Gw5ABHY','2026-03-18 23:05:52');
/*!40000 ALTER TABLE `teacher_device_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trash`
--

DROP TABLE IF EXISTS `trash`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `trash` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `class_id` bigint NOT NULL,
  `student_snapshot` json DEFAULT NULL,
  `deleted_at` bigint NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_trash_class` (`class_id`),
  CONSTRAINT `fk_trash_class` FOREIGN KEY (`class_id`) REFERENCES `class_info` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trash`
--

LOCK TABLES `trash` WRITE;
/*!40000 ALTER TABLE `trash` DISABLE KEYS */;
/*!40000 ALTER TABLE `trash` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'class_points'
--

--
-- Dumping routines for database 'class_points'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-20 19:12:20
