-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: kadmin
-- ------------------------------------------------------
-- Server version	8.0.46

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
-- Table structure for table `mobile_contact_request`
--

DROP TABLE IF EXISTS `mobile_contact_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_contact_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `requester_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `handled_time` datetime DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mobile_contact_request_target` (`target_id`,`status`) USING BTREE,
  KEY `idx_mobile_contact_request_requester` (`requester_id`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端联系人申请';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_contact_request`
--

LOCK TABLES `mobile_contact_request` WRITE;
/*!40000 ALTER TABLE `mobile_contact_request` DISABLE KEYS */;
INSERT INTO `mobile_contact_request` VALUES (1,1,7,0,'申请添加为好友',NULL,'2026-09-09 10:56:22','2026-09-09 10:56:22',1,1);
/*!40000 ALTER TABLE `mobile_contact_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data_i18n`
--

DROP TABLE IF EXISTS `sys_dict_data_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data_i18n` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_data_id` bigint NOT NULL COMMENT '字典数据ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_data_lang` (`dict_data_id`,`lang_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典数据多语言表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data_i18n`
--

LOCK TABLES `sys_dict_data_i18n` WRITE;
/*!40000 ALTER TABLE `sys_dict_data_i18n` DISABLE KEYS */;
INSERT INTO `sys_dict_data_i18n` VALUES (1,1,'zh-CN','男','2025-12-22 21:19:11','2025-12-22 21:19:11'),(2,2,'zh-CN','女','2025-12-22 21:19:11','2025-12-22 21:19:11'),(3,3,'zh-CN','试用','2025-12-22 21:19:11','2025-12-22 21:19:11'),(4,4,'zh-CN','正式','2025-12-22 21:19:11','2025-12-22 21:19:11'),(5,5,'zh-CN','离职','2025-12-22 21:19:11','2025-12-22 21:19:11'),(6,6,'zh-CN','固定期限','2025-12-22 21:19:11','2025-12-22 21:19:11'),(7,7,'zh-CN','无固定期限','2025-12-22 21:19:11','2025-12-22 21:19:11'),(8,8,'zh-CN','完成任务','2025-12-22 21:19:11','2025-12-22 21:19:11'),(9,9,'zh-CN','年假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(10,10,'zh-CN','事假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(11,11,'zh-CN','病假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(12,12,'zh-CN','婚假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(13,13,'zh-CN','产假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(14,14,'zh-CN','陪产假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(15,15,'zh-CN','丧假','2025-12-22 21:19:11','2025-12-22 21:19:11'),(16,16,'zh-CN','工作日加班','2025-12-22 21:19:11','2025-12-22 21:19:11'),(17,17,'zh-CN','周末加班','2025-12-22 21:19:11','2025-12-22 21:19:11'),(18,18,'zh-CN','节假日加班','2025-12-22 21:19:11','2025-12-22 21:19:11'),(19,19,'zh-CN','高中','2025-12-22 21:19:11','2025-12-22 21:19:11'),(20,20,'zh-CN','大专','2025-12-22 21:19:11','2025-12-22 21:19:11'),(21,21,'zh-CN','本科','2025-12-22 21:19:11','2025-12-22 21:19:11'),(22,22,'zh-CN','硕士','2025-12-22 21:19:11','2025-12-22 21:19:11'),(23,23,'zh-CN','博士','2025-12-22 21:19:11','2025-12-22 21:19:11'),(24,24,'zh-CN','未婚','2025-12-22 21:19:11','2025-12-22 21:19:11'),(25,25,'zh-CN','已婚','2025-12-22 21:19:11','2025-12-22 21:19:11'),(26,26,'zh-CN','离异','2025-12-22 21:19:11','2025-12-22 21:19:11'),(27,27,'zh-CN','群众','2025-12-22 21:19:11','2025-12-22 21:19:11'),(28,28,'zh-CN','共青团员','2025-12-22 21:19:11','2025-12-22 21:19:11'),(29,29,'zh-CN','中共党员','2025-12-22 21:19:11','2025-12-22 21:19:11'),(30,30,'zh-CN','民主党派','2025-12-22 21:19:11','2025-12-22 21:19:11'),(31,31,'zh-CN','晋升','2025-12-22 21:19:11','2025-12-22 21:19:11'),(32,32,'zh-CN','降级','2025-12-22 21:19:11','2025-12-22 21:19:11'),(33,33,'zh-CN','平调','2025-12-22 21:19:11','2025-12-22 21:19:11'),(34,34,'zh-CN','奖励','2025-12-22 21:19:11','2025-12-22 21:19:11'),(35,35,'zh-CN','惩罚','2025-12-22 21:19:11','2025-12-22 21:19:11'),(36,36,'zh-CN','主动离职','2025-12-22 21:19:11','2025-12-22 21:19:11'),(37,37,'zh-CN','辞退','2025-12-22 21:19:11','2025-12-22 21:19:11'),(38,38,'zh-CN','合同到期','2025-12-22 21:19:11','2025-12-22 21:19:11'),(39,1,'en-US','Male','2025-12-22 21:19:11','2025-12-22 21:19:11'),(40,2,'en-US','Female','2025-12-22 21:19:11','2025-12-22 21:19:11'),(41,3,'en-US','Probation','2025-12-22 21:19:11','2025-12-22 21:19:11'),(42,4,'en-US','Regular','2025-12-22 21:19:11','2025-12-22 21:19:11'),(43,5,'en-US','Resigned','2025-12-22 21:19:11','2025-12-22 21:19:11'),(44,6,'en-US','Fixed Term','2025-12-22 21:19:11','2025-12-22 21:19:11'),(45,7,'en-US','Open-ended','2025-12-22 21:19:11','2025-12-22 21:19:11'),(46,8,'en-US','Task-based','2025-12-22 21:19:11','2025-12-22 21:19:11'),(47,9,'en-US','Annual Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(48,10,'en-US','Personal Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(49,11,'en-US','Sick Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(50,12,'en-US','Marriage Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(51,13,'en-US','Maternity Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(52,14,'en-US','Paternity Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(53,15,'en-US','Bereavement Leave','2025-12-22 21:19:11','2025-12-22 21:19:11'),(54,16,'en-US','Weekday Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11'),(55,17,'en-US','Weekend Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11'),(56,18,'en-US','Holiday Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11'),(57,19,'en-US','High School','2025-12-22 21:19:11','2025-12-22 21:19:11'),(58,20,'en-US','College','2025-12-22 21:19:11','2025-12-22 21:19:11'),(59,21,'en-US','Bachelor','2025-12-22 21:19:11','2025-12-22 21:19:11'),(60,22,'en-US','Master','2025-12-22 21:19:11','2025-12-22 21:19:11'),(61,23,'en-US','Doctor','2025-12-22 21:19:11','2025-12-22 21:19:11'),(62,24,'en-US','Single','2025-12-22 21:19:11','2025-12-22 21:19:11'),(63,25,'en-US','Married','2025-12-22 21:19:11','2025-12-22 21:19:11'),(64,26,'en-US','Divorced','2025-12-22 21:19:11','2025-12-22 21:19:11'),(65,27,'en-US','Masses','2025-12-22 21:19:11','2025-12-22 21:19:11'),(66,28,'en-US','League Member','2025-12-22 21:19:11','2025-12-22 21:19:11'),(67,29,'en-US','Party Member','2025-12-22 21:19:11','2025-12-22 21:19:11'),(68,30,'en-US','Democratic Party','2025-12-22 21:19:11','2025-12-22 21:19:11'),(69,31,'en-US','Promotion','2025-12-22 21:19:11','2025-12-22 21:19:11'),(70,32,'en-US','Demotion','2025-12-22 21:19:11','2025-12-22 21:19:11'),(71,33,'en-US','Transfer','2025-12-22 21:19:11','2025-12-22 21:19:11'),(72,34,'en-US','Reward','2025-12-22 21:19:11','2025-12-22 21:19:11'),(73,35,'en-US','Punishment','2025-12-22 21:19:11','2025-12-22 21:19:11'),(74,36,'en-US','Voluntary Resignation','2025-12-22 21:19:11','2025-12-22 21:19:11'),(75,37,'en-US','Dismissal','2025-12-22 21:19:11','2025-12-22 21:19:11'),(76,38,'en-US','Contract Expiration','2025-12-22 21:19:11','2025-12-22 21:19:11');
/*!40000 ALTER TABLE `sys_dict_data_i18n` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_language`
--

DROP TABLE IF EXISTS `sys_language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_language` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `lang_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `lang_code` (`lang_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='语言配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_language`
--

LOCK TABLES `sys_language` WRITE;
/*!40000 ALTER TABLE `sys_language` DISABLE KEYS */;
INSERT INTO `sys_language` VALUES (1,'zh-CN','简体中文',NULL,1,1,1,'2025-12-22 21:19:11','2025-12-22 21:19:11'),(2,'en-US','English',NULL,2,1,0,'2025-12-22 21:19:11','2025-12-22 21:19:11');
/*!40000 ALTER TABLE `sys_language` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu_i18n`
--

DROP TABLE IF EXISTS `sys_menu_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu_i18n` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_menu_lang` (`menu_id`,`lang_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=184 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='菜单多语言表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu_i18n`
--

LOCK TABLES `sys_menu_i18n` WRITE;
/*!40000 ALTER TABLE `sys_menu_i18n` DISABLE KEYS */;
INSERT INTO `sys_menu_i18n` VALUES (147,1,'en-US','Home','2025-12-25 14:10:05','2025-12-25 14:10:05'),(148,2,'en-US','Organization','2025-12-25 14:10:05','2025-12-25 14:10:05'),(149,3,'en-US','HR Management','2025-12-25 14:10:05','2025-12-25 14:10:05'),(150,4,'en-US','Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05'),(151,5,'en-US','Application','2025-12-25 14:10:05','2025-12-25 14:10:05'),(152,6,'en-US','Approval','2025-12-25 14:10:05','2025-12-25 14:10:05'),(153,7,'en-US','Report','2025-12-25 14:10:05','2025-12-25 14:10:05'),(154,8,'en-US','System','2025-12-25 14:10:05','2025-12-25 14:10:05'),(155,24,'en-US','Organization Structure','2025-12-25 14:10:05','2025-12-25 14:10:05'),(156,21,'en-US','Company','2025-12-25 14:10:05','2025-12-25 14:10:05'),(157,22,'en-US','Department','2025-12-25 14:10:05','2025-12-25 14:10:05'),(158,23,'en-US','Position','2025-12-25 14:10:05','2025-12-25 14:10:05'),(159,31,'en-US','Employee','2025-12-25 14:10:05','2025-12-25 14:10:05'),(160,32,'en-US','Contract','2025-12-25 14:10:05','2025-12-25 14:10:05'),(161,33,'en-US','Regularization','2025-12-25 14:10:05','2025-12-25 14:10:05'),(162,34,'en-US','Transfer','2025-12-25 14:10:05','2025-12-25 14:10:05'),(163,35,'en-US','Reward & Punishment','2025-12-25 14:10:05','2025-12-25 14:10:05'),(164,36,'en-US','Resignation','2025-12-25 14:10:05','2025-12-25 14:10:05'),(165,41,'en-US','Shift','2025-12-25 14:10:05','2025-12-25 14:10:05'),(166,42,'en-US','Schedule','2025-12-25 14:10:05','2025-12-25 14:10:05'),(167,43,'en-US','Holiday','2025-12-25 14:10:05','2025-12-25 14:10:05'),(168,44,'en-US','Daily Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05'),(169,45,'en-US','Monthly Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05'),(170,51,'en-US','Leave','2025-12-25 14:10:05','2025-12-25 14:10:05'),(171,52,'en-US','Overtime','2025-12-25 14:10:05','2025-12-25 14:10:05'),(172,53,'en-US','Makeup','2025-12-25 14:10:05','2025-12-25 14:10:05'),(173,54,'en-US','Exchange','2025-12-25 14:10:05','2025-12-25 14:10:05'),(174,55,'en-US','Business Trip','2025-12-25 14:10:05','2025-12-25 14:10:05'),(175,61,'en-US','Pending','2025-12-25 14:10:05','2025-12-25 14:10:05'),(176,62,'en-US','My Applications','2025-12-25 14:10:05','2025-12-25 14:10:05'),(177,63,'en-US','Flow Config','2025-12-25 14:10:05','2025-12-25 14:10:05'),(178,71,'en-US','Employee Report','2025-12-25 14:10:05','2025-12-25 14:10:05'),(179,72,'en-US','Attendance Report','2025-12-25 14:10:05','2025-12-25 14:10:05'),(180,81,'en-US','User','2025-12-25 14:10:05','2025-12-25 14:10:05'),(181,82,'en-US','Role','2025-12-25 14:10:05','2025-12-25 14:10:05'),(182,83,'en-US','Menu','2025-12-25 14:10:05','2025-12-25 14:10:05'),(183,84,'en-US','Dictionary','2025-12-25 14:10:05','2025-12-25 14:10:05');
/*!40000 ALTER TABLE `sys_menu_i18n` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_process_definition`
--

DROP TABLE IF EXISTS `wf_process_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_process_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID，为空表示全局',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程编码',
  `process_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程名称',
  `process_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='流程定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_definition`
--

LOCK TABLES `wf_process_definition` WRITE;
/*!40000 ALTER TABLE `wf_process_definition` DISABLE KEYS */;
INSERT INTO `wf_process_definition` VALUES (1,NULL,'LEAVE','请假审批流程','leave','请假申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(2,NULL,'OVERTIME','加班审批流程','overtime','加班申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(3,NULL,'CARD_REPLACEMENT','补卡审批流程','card_replacement','补卡申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(4,NULL,'COMP_LEAVE','换休审批流程','comp_leave','换休申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(5,NULL,'BUSINESS_TRIP','出差审批流程','business_trip','出差申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(6,NULL,'REGULARIZATION','转正审批流程','regularization','转正申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(7,NULL,'RESIGNATION','离职审批流程','resignation','离职申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(8,NULL,'DEPT_CHANGE','部门变更审批流程','dept_change','部门变更申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(9,NULL,'POSITION_CHANGE','职位变更审批流程','position_change','职位变更申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL),(10,NULL,'REWARD_PUNISHMENT','奖惩审批流程','reward_punishment','奖惩申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
/*!40000 ALTER TABLE `wf_process_definition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_process_node`
--

DROP TABLE IF EXISTS `wf_process_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_process_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `process_id` bigint NOT NULL COMMENT '流程ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_type` tinyint DEFAULT NULL COMMENT '节点类型：1-开始，2-审批，3-抄送，4-结束',
  `node_order` int NOT NULL COMMENT '节点顺序',
  `approver_type` tinyint DEFAULT NULL COMMENT '审批人类型：1-指定人员，2-指定角色，3-部门负责人，4-上级领导',
  `approver_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批人ID列表',
  `multi_approve_type` tinyint DEFAULT '1' COMMENT '多人审批方式：1-或签，2-会签',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_process_id` (`process_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='流程节点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_node`
--

LOCK TABLES `wf_process_node` WRITE;
/*!40000 ALTER TABLE `wf_process_node` DISABLE KEYS */;
INSERT INTO `wf_process_node` VALUES (1,1,'开始',1,1,NULL,NULL,NULL,'2025-12-22 21:19:11','2025-12-22 21:19:11'),(2,1,'部门负责人审批',2,2,3,NULL,1,'2025-12-22 21:19:11','2025-12-22 21:19:11'),(3,1,'HR审批',2,3,2,NULL,1,'2025-12-22 21:19:11','2025-12-22 21:19:11'),(4,1,'结束',4,4,NULL,NULL,NULL,'2025-12-22 21:19:11','2025-12-22 21:19:11');
/*!40000 ALTER TABLE `wf_process_node` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- ---------------------------------------------------------------------------
-- mobile_chat_message.status 列删除前的历史值存档（该列无代码引用）
-- (id, status)
  -- 3	1
  -- 4	1
  -- 5	1
  -- 6	1
  -- 8	1
