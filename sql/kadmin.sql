-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: kadmin
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
-- Current Database: `kadmin`
--

/*!40000 DROP DATABASE IF EXISTS `kadmin`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `kadmin` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `kadmin`;

--
-- Table structure for table `app_business_trip`
--

DROP TABLE IF EXISTS `app_business_trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_business_trip` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `destination` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出差地点',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `duration` int NOT NULL COMMENT '出差天数',
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '出差目的',
  `companions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '同行人员',
  `estimated_cost` decimal(12,2) DEFAULT NULL COMMENT '预计费用',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='出差申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_business_trip`
--

LOCK TABLES `app_business_trip` WRITE;
/*!40000 ALTER TABLE `app_business_trip` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_business_trip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_card_replacement`
--

DROP TABLE IF EXISTS `app_card_replacement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_card_replacement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `replacement_type` tinyint NOT NULL COMMENT '补卡类型：1-上班补卡，2-下班补卡',
  `replacement_time` datetime NOT NULL COMMENT '补卡时间',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '补卡原因',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='补卡申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_card_replacement`
--

LOCK TABLES `app_card_replacement` WRITE;
/*!40000 ALTER TABLE `app_card_replacement` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_card_replacement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_comp_leave`
--

DROP TABLE IF EXISTS `app_comp_leave`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_comp_leave` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `overtime_id` bigint DEFAULT NULL COMMENT '关联加班记录ID',
  `comp_date` date NOT NULL COMMENT '换休日期',
  `duration` decimal(4,1) NOT NULL COMMENT '时长（天）',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '换休原因',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='换休申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_comp_leave`
--

LOCK TABLES `app_comp_leave` WRITE;
/*!40000 ALTER TABLE `app_comp_leave` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_comp_leave` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_leave`
--

DROP TABLE IF EXISTS `app_leave`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_leave` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `leave_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假类型',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration` decimal(4,1) NOT NULL COMMENT '时长（天）',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请假原因',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '附件',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='请假申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_leave`
--

LOCK TABLES `app_leave` WRITE;
/*!40000 ALTER TABLE `app_leave` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_leave` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_overtime`
--

DROP TABLE IF EXISTS `app_overtime`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_overtime` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `overtime_date` date NOT NULL COMMENT '加班日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `duration` decimal(4,1) NOT NULL COMMENT '时长（小时）',
  `overtime_type` tinyint DEFAULT NULL COMMENT '加班类型：1-工作日，2-周末，3-节假日',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '加班原因',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='加班申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_overtime`
--

LOCK TABLES `app_overtime` WRITE;
/*!40000 ALTER TABLE `app_overtime` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_overtime` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_calendar_rule`
--

DROP TABLE IF EXISTS `att_calendar_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_calendar_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `rule_type` tinyint DEFAULT NULL COMMENT '规则类型',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `rule_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规则名称',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_company` (`company_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考勤日历规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_calendar_rule`
--

LOCK TABLES `att_calendar_rule` WRITE;
/*!40000 ALTER TABLE `att_calendar_rule` DISABLE KEYS */;
INSERT INTO `att_calendar_rule` VALUES (1,11,2,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `att_calendar_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_clock_record`
--

DROP TABLE IF EXISTS `att_clock_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_clock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `clock_time` datetime DEFAULT NULL COMMENT '打卡时间',
  `clock_date` date DEFAULT NULL COMMENT '打卡日期（clock_time的日期部分，防重唯一键）',
  `clock_type` tinyint DEFAULT NULL COMMENT '打卡类型：1-上班，2-下班',
  `clock_method` tinyint DEFAULT NULL COMMENT '打卡方式：1-APP，2-考勤机，3-手动补卡',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打卡位置',
  `device_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '设备信息',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_time` (`employee_id`,`clock_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考勤打卡记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_clock_record`
--

LOCK TABLES `att_clock_record` WRITE;
/*!40000 ALTER TABLE `att_clock_record` DISABLE KEYS */;
INSERT INTO `att_clock_record` VALUES (2,2,'2025-12-27 08:55:00','2025-12-27',1,1,NULL,NULL,'上班打卡','2025-12-29 10:40:39'),(3,2,'2025-12-27 18:05:00','2025-12-27',2,1,NULL,NULL,'下班打卡','2025-12-29 10:40:39'),(4,2,'2025-12-26 09:15:00','2025-12-26',1,1,NULL,NULL,'上班打卡','2025-12-29 10:40:39'),(5,2,'2025-12-26 18:00:00','2025-12-26',2,1,NULL,NULL,'下班打卡','2025-12-29 10:40:39'),(6,2,'2025-12-25 08:58:00','2025-12-25',1,1,NULL,NULL,'上班打卡','2025-12-29 10:40:39'),(7,2,'2025-12-25 17:30:00','2025-12-25',2,1,NULL,NULL,'下班打卡','2025-12-29 10:40:39'),(8,2,'2025-12-24 08:50:00','2025-12-24',1,1,NULL,NULL,'上班打卡','2025-12-29 10:40:39'),(9,2,'2025-12-24 18:10:00','2025-12-24',2,1,NULL,NULL,'下班打卡','2025-12-29 10:40:39'),(10,2,'2025-12-29 08:52:00','2025-12-29',1,1,NULL,NULL,'上班打卡','2025-12-29 10:40:39'),(11,2,'2025-12-29 12:05:00','2025-12-29',2,3,NULL,NULL,NULL,NULL),(12,1,'2025-12-30 08:00:00','2025-12-30',1,3,NULL,NULL,NULL,NULL),(13,6,'2026-03-09 00:00:00','2026-03-09',1,3,NULL,NULL,NULL,NULL),(14,1,'2026-03-10 10:25:59','2026-03-10',1,1,NULL,NULL,'移动端上班打卡',NULL),(15,1,'2026-03-10 10:26:00','2026-03-10',2,1,NULL,NULL,'移动端下班打卡',NULL),(16,1,'2026-03-10 10:26:01','2026-03-10',2,1,NULL,NULL,'移动端下班打卡',NULL),(17,1,'2026-03-10 10:26:03','2026-03-10',2,1,NULL,NULL,'移动端下班打卡',NULL),(18,1,'2026-03-12 11:26:08','2026-03-12',1,1,'30.5,114.4',NULL,'移动端上班打卡',NULL),(19,1,'2026-03-12 11:26:09','2026-03-12',2,1,'30.5,114.4',NULL,'移动端下班打卡',NULL),(20,1,'2026-03-17 15:17:57','2026-03-17',1,1,'30.5,114.4',NULL,'移动端上班打卡',NULL),(21,1,'2026-03-17 15:36:30','2026-03-17',2,1,'30.5,114.4',NULL,'移动端下班打卡',NULL),(22,1,'2026-03-18 09:00:00','2026-03-18',1,3,NULL,NULL,NULL,NULL),(23,1,'2026-03-18 14:47:23','2026-03-18',1,1,'30.5,114.4',NULL,'移动端上班打卡',NULL),(24,1,'2026-03-18 14:54:35','2026-03-18',2,1,'30.5,114.4',NULL,'移动端下班打卡',NULL),(25,1,'2026-04-09 15:59:29','2026-04-09',1,1,'30.5,114.4',NULL,'移动端上班打卡',NULL),(26,1,'2026-04-09 15:59:30','2026-04-09',2,1,'30.5,114.4',NULL,'移动端下班打卡',NULL),(27,1,'2026-05-07 09:17:12','2026-05-07',1,1,'31.435539723059076,120.91862117964598',NULL,'移动端上班打卡',NULL),(28,1,'2026-05-07 09:17:13','2026-05-07',2,1,'31.435539723059076,120.91862117964598',NULL,'移动端下班打卡',NULL),(29,1,'2026-05-13 09:03:19','2026-05-13',1,1,'31.43554455939022,120.91870476296431',NULL,'移动端上班打卡',NULL);
/*!40000 ALTER TABLE `att_clock_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_daily_record`
--

DROP TABLE IF EXISTS `att_daily_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_daily_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `att_date` date NOT NULL COMMENT '考勤日期',
  `shift_id` bigint DEFAULT NULL COMMENT '班次ID',
  `period_id` bigint DEFAULT NULL COMMENT '时段ID',
  `period_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '时段名称',
  `scheduled_in` time DEFAULT NULL COMMENT '应上班时间',
  `scheduled_out` time DEFAULT NULL COMMENT '应下班时间',
  `actual_in` time DEFAULT NULL COMMENT '实际上班打卡',
  `actual_out` time DEFAULT NULL COMMENT '实际下班打卡',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退',
  `late_minutes` int DEFAULT '0' COMMENT '迟到分钟数',
  `early_minutes` int DEFAULT '0' COMMENT '早退分钟数',
  `work_hours` decimal(5,2) DEFAULT '0.00' COMMENT '工作时长(小时)',
  `overtime_hours` decimal(5,2) DEFAULT '0.00' COMMENT '加班时长(小时)',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `locked` tinyint DEFAULT '0' COMMENT '是否锁定 0-否 1-是',
  `locked_by` bigint DEFAULT NULL COMMENT '锁定人',
  `locked_time` datetime DEFAULT NULL COMMENT '锁定时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_date_period` (`employee_id`,`att_date`,`period_id`) USING BTREE,
  KEY `idx_date` (`att_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日考勤记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_daily_record`
--

LOCK TABLES `att_daily_record` WRITE;
/*!40000 ALTER TABLE `att_daily_record` DISABLE KEYS */;
INSERT INTO `att_daily_record` VALUES (1,2,'2025-12-29',1,34,'第一段','09:00:00','12:00:00','08:52:00','12:05:00',1,0,0,3.00,0.00,NULL,0,1,'2025-12-29 13:27:58',NULL,NULL),(2,2,'2025-12-29',1,35,'第二段','13:00:00','18:00:00','12:05:00','12:05:00',3,0,355,0.00,0.00,NULL,0,1,'2025-12-29 13:27:58',NULL,NULL),(3,2,'2025-12-29',1,36,'第三段','19:00:00','20:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,1,'2025-12-29 13:27:58',NULL,NULL),(4,1,'2025-12-30',1,34,'第一段','09:00:00','12:00:00','08:00:00',NULL,0,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(5,1,'2025-12-30',1,35,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(6,1,'2025-12-30',1,36,'第三段','19:00:00','20:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(7,1,'2026-01-13',1,37,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(8,1,'2026-01-13',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(9,5,'2026-01-13',1,37,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(10,5,'2026-01-13',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(11,1,'2026-01-14',1,37,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(12,1,'2026-01-14',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(13,5,'2026-01-14',1,37,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(14,5,'2026-01-14',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(15,1,'2026-03-09',1,37,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(16,1,'2026-03-09',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(17,1,'2026-03-12',1,37,'第一段','09:00:00','12:00:00','11:26:08','11:26:09',7,146,33,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(18,1,'2026-03-12',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(19,1,'2026-03-18',1,37,'第一段','09:00:00','12:00:00','09:00:00',NULL,0,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(20,1,'2026-03-18',1,38,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(21,1,'2026-03-18',1,42,'第一段','09:00:00','12:00:00','09:00:00',NULL,0,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(22,1,'2026-03-18',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(23,1,'2026-04-09',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(24,1,'2026-04-09',1,43,'第二段','13:00:00','18:00:00','15:59:29','15:59:30',7,179,120,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(25,1,'2026-04-10',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(26,1,'2026-04-10',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(27,1,'2026-04-11',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(28,1,'2026-04-11',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(29,1,'2026-04-12',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(30,1,'2026-04-12',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(31,1,'2026-04-13',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(32,1,'2026-04-13',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(33,1,'2026-04-14',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(34,1,'2026-04-14',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(35,1,'2026-04-15',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(36,1,'2026-04-15',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(37,1,'2026-04-16',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(38,1,'2026-04-16',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(39,1,'2026-04-17',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(40,1,'2026-04-17',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(41,1,'2026-04-18',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(42,1,'2026-04-18',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(43,1,'2026-04-19',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(44,1,'2026-04-19',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(45,1,'2026-04-20',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(46,1,'2026-04-20',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(47,1,'2026-04-21',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(48,1,'2026-04-21',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(49,1,'2026-04-22',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(50,1,'2026-04-22',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(51,1,'2026-04-23',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(52,1,'2026-04-23',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(53,1,'2026-04-24',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(54,1,'2026-04-24',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(55,1,'2026-04-25',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(56,1,'2026-04-25',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(57,1,'2026-04-26',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(58,1,'2026-04-26',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(59,1,'2026-04-27',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(60,1,'2026-04-27',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(61,1,'2026-04-28',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(62,1,'2026-04-28',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(63,1,'2026-04-29',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(64,1,'2026-04-29',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(65,1,'2026-04-30',1,42,'第一段','09:00:00','12:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL),(66,1,'2026-04-30',1,43,'第二段','13:00:00','18:00:00',NULL,NULL,4,0,0,0.00,0.00,NULL,0,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `att_daily_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_holiday`
--

DROP TABLE IF EXISTS `att_holiday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_holiday` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID，为空表示全局',
  `holiday_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节假日名称',
  `holiday_date` date NOT NULL COMMENT '节假日日期',
  `holiday_type` tinyint DEFAULT NULL COMMENT '类型：1-法定节假日，2-公司假日，3-调休上班',
  `year` int DEFAULT NULL COMMENT '年份',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='考勤节假日表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_holiday`
--

LOCK TABLES `att_holiday` WRITE;
/*!40000 ALTER TABLE `att_holiday` DISABLE KEYS */;
/*!40000 ALTER TABLE `att_holiday` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_location`
--

DROP TABLE IF EXISTS `att_location`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `location_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '??????',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `latitude` decimal(10,6) NOT NULL COMMENT '?? GCJ-02',
  `longitude` decimal(10,6) NOT NULL COMMENT '?? GCJ-02',
  `clock_range` int NOT NULL DEFAULT '300' COMMENT '????????',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '???0-???1-??',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??',
  `created_time` datetime DEFAULT NULL COMMENT '????',
  `updated_time` datetime DEFAULT NULL COMMENT '????',
  `created_by` bigint DEFAULT NULL COMMENT '???',
  `updated_by` bigint DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_location`
--

LOCK TABLES `att_location` WRITE;
/*!40000 ALTER TABLE `att_location` DISABLE KEYS */;
INSERT INTO `att_location` VALUES (1,'华水工业园','中国江苏省苏州市昆山市红杨路1100号 邮政编码: 215316',31.435435,120.920273,500,1,'由公司打卡配置迁移','2026-05-07 13:59:11','2026-05-07 13:59:11',NULL,1),(2,'测试2','中国北京市东城区南菜园中国国家博物馆 邮政编码: 100051',39.907441,116.406830,500,1,'','2026-05-07 14:13:22','2026-05-07 14:13:22',1,1);
/*!40000 ALTER TABLE `att_location` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_location_employee`
--

DROP TABLE IF EXISTS `att_location_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_location_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `location_id` bigint NOT NULL COMMENT '????ID',
  `employee_id` bigint NOT NULL COMMENT '??ID',
  `created_time` datetime DEFAULT NULL COMMENT '????',
  `updated_time` datetime DEFAULT NULL COMMENT '????',
  `created_by` bigint DEFAULT NULL COMMENT '???',
  `updated_by` bigint DEFAULT NULL COMMENT '???',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_att_location_employee_location_employee` (`location_id`,`employee_id`) USING BTREE,
  KEY `idx_att_location_employee_location` (`location_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_location_employee`
--

LOCK TABLES `att_location_employee` WRITE;
/*!40000 ALTER TABLE `att_location_employee` DISABLE KEYS */;
INSERT INTO `att_location_employee` VALUES (34,2,5,'2026-05-13 08:42:04','2026-05-13 08:42:04',1,1),(35,2,6,'2026-05-13 08:42:04','2026-05-13 08:42:04',1,1),(36,1,5,'2026-05-13 08:42:14','2026-05-13 08:42:14',1,1),(37,1,1,'2026-05-13 08:42:14','2026-05-13 08:42:14',1,1);
/*!40000 ALTER TABLE `att_location_employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_monthly_summary`
--

DROP TABLE IF EXISTS `att_monthly_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_monthly_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `year` int NOT NULL COMMENT '年份',
  `month` int NOT NULL COMMENT '月份',
  `work_days` int DEFAULT '0' COMMENT '应出勤天数',
  `actual_days` int DEFAULT '0' COMMENT '实际出勤天数',
  `late_count` int DEFAULT '0' COMMENT '迟到次数',
  `early_count` int DEFAULT '0' COMMENT '早退次数',
  `absent_count` int DEFAULT '0' COMMENT '旷工次数',
  `leave_days` decimal(4,1) DEFAULT '0.0' COMMENT '请假天数',
  `overtime_hours` decimal(6,2) DEFAULT '0.00' COMMENT '加班时长',
  `business_trip_days` int DEFAULT '0' COMMENT '出差天数',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-未确认，1-已确认',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_year_month` (`employee_id`,`year`,`month`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='月考勤汇总表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_monthly_summary`
--

LOCK TABLES `att_monthly_summary` WRITE;
/*!40000 ALTER TABLE `att_monthly_summary` DISABLE KEYS */;
/*!40000 ALTER TABLE `att_monthly_summary` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_schedule`
--

DROP TABLE IF EXISTS `att_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `shift_id` bigint DEFAULT NULL COMMENT '班次ID',
  `is_rest` tinyint DEFAULT '0' COMMENT '是否休息：0-否，1-是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_date` (`employee_id`,`schedule_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=314 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='排班表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_schedule`
--

LOCK TABLES `att_schedule` WRITE;
/*!40000 ALTER TABLE `att_schedule` DISABLE KEYS */;
INSERT INTO `att_schedule` VALUES (2,1,'2025-12-22',1,0,NULL,'2025-12-28 18:03:55','2025-12-28 18:03:55',NULL,NULL),(6,2,'2025-12-29',1,0,NULL,'2025-12-28 18:07:18','2025-12-28 18:07:18',NULL,NULL),(11,2,'2025-12-28',1,0,NULL,'2025-12-28 20:58:39','2025-12-28 20:58:39',NULL,NULL),(12,1,'2025-12-23',1,0,NULL,'2025-12-28 21:01:55','2025-12-28 21:01:55',NULL,NULL),(13,2,'2025-12-23',1,0,NULL,'2025-12-28 21:01:56','2025-12-28 21:01:56',NULL,NULL),(14,1,'2025-12-24',2,0,NULL,'2025-12-28 21:01:58','2025-12-28 21:01:58',NULL,NULL),(16,1,'2025-12-08',1,0,NULL,'2025-12-28 21:02:43','2025-12-28 21:02:43',NULL,NULL),(17,1,'2025-12-09',1,0,NULL,'2025-12-28 21:02:44','2025-12-28 21:02:44',NULL,NULL),(22,1,'2025-12-06',1,0,NULL,'2025-12-28 21:03:28','2025-12-28 21:03:28',NULL,NULL),(23,1,'2025-12-10',1,0,NULL,'2025-12-28 21:04:03','2025-12-28 21:04:03',NULL,NULL),(26,1,'2025-12-11',1,0,NULL,'2025-12-28 21:20:49','2025-12-28 21:20:49',NULL,NULL),(29,1,'2025-12-13',2,0,NULL,'2025-12-28 21:21:03','2025-12-28 21:21:03',NULL,NULL),(30,1,'2025-12-12',2,0,NULL,'2025-12-28 21:21:04','2025-12-28 21:21:04',NULL,NULL),(32,1,'2025-12-14',1,0,NULL,'2025-12-28 21:23:46','2025-12-28 21:23:46',NULL,NULL),(44,1,'2025-12-15',1,0,NULL,'2025-12-28 21:24:10','2025-12-28 21:24:10',NULL,NULL),(45,1,'2025-12-16',1,0,NULL,'2025-12-28 21:24:11','2025-12-28 21:24:11',NULL,NULL),(52,1,'2025-12-17',1,0,NULL,'2025-12-30 10:25:58','2025-12-30 10:25:58',NULL,NULL),(53,1,'2025-12-18',1,0,NULL,'2025-12-30 10:25:59','2025-12-30 10:25:59',NULL,NULL),(54,1,'2025-12-21',2,0,NULL,'2025-12-30 10:26:03','2025-12-30 10:26:03',NULL,NULL),(56,2,'2025-12-30',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL),(57,2,'2025-12-31',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL),(58,2,'2026-01-01',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL),(62,1,'2025-12-04',1,0,NULL,'2025-12-30 10:26:39','2025-12-30 10:26:39',NULL,NULL),(63,1,'2025-12-05',1,0,NULL,'2025-12-30 10:26:39','2025-12-30 10:26:39',NULL,NULL),(64,1,'2025-12-01',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL),(65,1,'2025-12-02',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL),(66,1,'2025-12-03',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL),(67,1,'2025-12-30',1,0,NULL,'2025-12-30 10:29:03','2025-12-30 10:29:03',NULL,NULL),(68,1,'2025-12-29',1,0,NULL,'2025-12-30 10:29:04','2025-12-30 10:29:04',NULL,NULL),(69,1,'2025-12-31',1,0,NULL,'2025-12-30 10:29:05','2025-12-30 10:29:05',NULL,NULL),(179,5,'2026-01-01',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(180,5,'2026-01-02',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(181,5,'2026-01-03',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(182,5,'2026-01-04',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(183,5,'2026-01-05',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(184,5,'2026-01-06',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(185,5,'2026-01-07',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(186,5,'2026-01-08',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(187,5,'2026-01-09',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(188,5,'2026-01-10',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(189,5,'2026-01-11',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(190,5,'2026-01-12',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(191,5,'2026-01-13',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(192,5,'2026-01-14',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(210,1,'2026-01-01',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(211,1,'2026-01-02',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(212,1,'2026-01-03',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(213,1,'2026-01-04',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(214,1,'2026-01-05',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(215,1,'2026-01-06',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(216,1,'2026-01-07',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(217,1,'2026-01-08',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(218,1,'2026-01-09',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(219,1,'2026-01-10',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(220,1,'2026-01-11',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(221,1,'2026-01-12',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(222,1,'2026-01-13',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(223,1,'2026-01-14',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(224,1,'2026-01-15',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(225,1,'2026-01-16',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(226,1,'2026-01-17',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(227,1,'2026-01-18',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(228,1,'2026-01-19',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(229,1,'2026-01-20',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(230,1,'2026-01-21',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(231,1,'2026-01-22',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(232,1,'2026-01-23',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(233,1,'2026-01-24',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(234,1,'2026-01-25',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(235,1,'2026-01-26',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(236,1,'2026-01-27',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(237,1,'2026-01-28',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(238,1,'2026-01-29',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(239,1,'2026-01-30',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(240,1,'2026-01-31',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL),(241,5,'2026-01-15',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(242,5,'2026-01-16',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(243,5,'2026-01-17',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(244,5,'2026-01-18',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(245,5,'2026-01-19',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(246,5,'2026-01-20',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(247,5,'2026-01-21',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(248,5,'2026-01-22',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(249,5,'2026-01-23',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(250,5,'2026-01-24',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(251,5,'2026-01-25',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(252,5,'2026-01-26',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(253,5,'2026-01-27',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(254,5,'2026-01-28',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(255,5,'2026-01-29',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(256,5,'2026-01-30',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(257,5,'2026-01-31',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL),(261,1,'2026-03-14',2,0,NULL,'2026-03-10 09:21:02','2026-03-10 09:21:41',NULL,NULL),(262,1,'2026-03-09',1,0,NULL,'2026-03-10 09:21:13','2026-03-10 09:21:41',NULL,NULL),(263,1,'2026-03-12',1,0,NULL,'2026-03-13 11:31:45','2026-03-13 11:31:45',NULL,NULL),(264,1,'2026-03-13',NULL,0,NULL,'2026-03-13 11:31:45','2026-03-13 11:46:52',NULL,NULL),(265,1,'2026-03-11',1,0,NULL,'2026-03-13 11:31:46','2026-03-13 11:31:46',NULL,NULL),(266,1,'2026-03-10',1,0,NULL,'2026-03-13 11:31:47','2026-03-13 11:31:47',NULL,NULL),(267,1,'2026-03-16',1,0,NULL,'2026-03-13 11:46:52','2026-03-13 11:46:52',NULL,NULL),(269,1,'2026-03-18',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(270,1,'2026-03-19',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(271,1,'2026-03-20',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(272,1,'2026-03-21',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(273,1,'2026-03-22',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(274,1,'2026-03-23',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(275,1,'2026-03-24',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(276,1,'2026-03-25',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(277,1,'2026-03-26',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(278,1,'2026-03-27',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(279,1,'2026-03-28',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(280,1,'2026-03-29',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(281,1,'2026-03-30',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(282,1,'2026-03-31',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(283,1,'2026-04-01',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(284,1,'2026-04-02',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(285,1,'2026-04-03',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(286,1,'2026-04-04',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(287,1,'2026-04-05',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(288,1,'2026-04-06',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(289,1,'2026-04-07',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(290,1,'2026-04-08',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(291,1,'2026-04-09',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(292,1,'2026-04-10',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(293,1,'2026-04-11',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(294,1,'2026-04-12',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(295,1,'2026-04-13',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(296,1,'2026-04-14',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(297,1,'2026-04-15',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(298,1,'2026-04-16',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(299,1,'2026-04-17',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(300,1,'2026-04-18',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(301,1,'2026-04-19',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(302,1,'2026-04-20',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(303,1,'2026-04-21',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(304,1,'2026-04-22',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(305,1,'2026-04-23',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(306,1,'2026-04-24',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(307,1,'2026-04-25',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(308,1,'2026-04-26',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(309,1,'2026-04-27',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(310,1,'2026-04-28',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(311,1,'2026-04-29',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(312,1,'2026-04-30',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL),(313,1,'2026-09-15',2,0,NULL,'2026-09-08 16:45:15','2026-09-08 16:45:15',NULL,NULL);
/*!40000 ALTER TABLE `att_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_shift`
--

DROP TABLE IF EXISTS `att_shift`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班次ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `shift_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次编码',
  `shift_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次名称',
  `work_start_time` time DEFAULT NULL COMMENT '上班时间',
  `work_end_time` time DEFAULT NULL COMMENT '下班时间',
  `late_minutes` int DEFAULT '0' COMMENT '迟到容许分钟',
  `early_minutes` int DEFAULT '0' COMMENT '早退容许分钟',
  `work_hours` decimal(4,2) DEFAULT NULL COMMENT '工作时长',
  `is_next_day` tinyint DEFAULT '0' COMMENT '是否跨天：0-否，1-是',
  `rest_start_time` time DEFAULT NULL COMMENT '休息开始时间',
  `rest_end_time` time DEFAULT NULL COMMENT '休息结束时间',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_company_shift_code` (`company_id`,`shift_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='班次表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_shift`
--

LOCK TABLES `att_shift` WRITE;
/*!40000 ALTER TABLE `att_shift` DISABLE KEYS */;
INSERT INTO `att_shift` VALUES (1,NULL,'01','白班','09:00:00','18:00:00',0,0,8.00,0,NULL,NULL,1,NULL,'2025-12-28 17:18:24','2025-12-28 17:18:24',NULL,NULL),(2,NULL,'002','晚班','09:00:00','23:00:00',0,0,12.50,0,NULL,NULL,1,NULL,'2025-12-28 21:01:47','2025-12-28 21:01:47',NULL,NULL);
/*!40000 ALTER TABLE `att_shift` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `att_shift_period`
--

DROP TABLE IF EXISTS `att_shift_period`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `att_shift_period` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_id` bigint DEFAULT NULL COMMENT '班次ID',
  `period_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '时段名称',
  `start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '开始时间',
  `end_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '结束时间',
  `cross_day` tinyint DEFAULT NULL COMMENT '是否跨天：0-否，1-是',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `need_clock_in` tinyint DEFAULT '1' COMMENT '上班是否打卡 0-否 1-是',
  `need_clock_out` tinyint DEFAULT '1' COMMENT '下班是否打卡 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_shift_id` (`shift_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='班次时段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `att_shift_period`
--

LOCK TABLES `att_shift_period` WRITE;
/*!40000 ALTER TABLE `att_shift_period` DISABLE KEYS */;
INSERT INTO `att_shift_period` VALUES (39,2,'上午','09:00','12:00',0,1,1,1),(40,2,'下午','13:00','18:00',0,2,1,1),(41,2,'加班','18:30','23:00',0,3,1,1),(42,1,'第一段','09:00','12:00',0,1,1,0),(43,1,'第二段','13:00','18:00',0,2,0,1);
/*!40000 ALTER TABLE `att_shift_period` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_application`
--

DROP TABLE IF EXISTS `hr_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_application` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `app_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申请类型',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` decimal(10,2) DEFAULT NULL COMMENT '时长',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT NULL COMMENT '状态',
  `approve_by` bigint DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `regular_date` date DEFAULT NULL COMMENT '转正日期',
  `probation_end_date` date DEFAULT NULL COMMENT '试用期结束日期',
  `evaluation` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '试用期评价',
  `new_employee_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '转正后员工类别',
  `transfer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '调动类型',
  `from_company_id` bigint DEFAULT NULL COMMENT '原公司ID',
  `to_company_id` bigint DEFAULT NULL COMMENT '新公司ID',
  `from_dept_id` bigint DEFAULT NULL COMMENT '原部门ID',
  `to_dept_id` bigint DEFAULT NULL COMMENT '新部门ID',
  `from_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原职位',
  `to_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '新职位',
  `effect_date` date DEFAULT NULL COMMENT '生效日期',
  `reward_type` int DEFAULT NULL COMMENT '奖惩类型',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '奖惩类别',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '金额',
  `resign_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '离职类型',
  `last_work_date` date DEFAULT NULL COMMENT '最后工作日',
  `handover_to` bigint DEFAULT NULL COMMENT '工作交接人ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_app_type` (`app_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='人力资源申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_application`
--

LOCK TABLES `hr_application` WRITE;
/*!40000 ALTER TABLE `hr_application` DISABLE KEYS */;
INSERT INTO `hr_application` VALUES (1,1,'leave','1','2025-12-28 00:00:00','2025-12-29 00:00:00',1.50,'',1,1,'2025-12-28 16:58:52','',NULL,1,'2025-12-28 16:57:50',2,'2025-12-28 16:58:52',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,2,'makeup','checkin','2025-12-29 00:00:00',NULL,NULL,'11',1,1,'2025-12-29 10:15:22','',NULL,1,'2025-12-29 10:15:17',1,'2025-12-29 10:15:22',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,2,'leave','1','2025-12-29 00:00:00','2025-12-24 00:00:00',1.00,'',1,1,'2026-01-06 13:59:16','',NULL,1,'2025-12-29 15:21:52',2,'2026-01-06 13:59:16',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,1,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2025-12-29 15:37:30','',NULL,1,'2025-12-29 15:37:05',1,'2025-12-29 15:37:30','2025-12-29','2025-12-29','','intern',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,1,'overtime','','2025-12-29 00:00:00','2025-12-30 00:00:00',2.00,'55',1,1,'2026-01-06 13:59:18','',NULL,1,'2025-12-29 16:20:07',2,'2026-01-06 13:59:18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,1,'makeup','checkin','2025-12-22 00:00:00',NULL,NULL,'55',1,1,'2026-01-06 13:59:19','',NULL,1,'2025-12-29 16:20:17',2,'2026-01-06 13:59:19',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,1,'exchange','','2025-12-29 00:00:00','2025-12-31 00:00:00',1.00,'',1,1,'2026-01-06 13:59:20','',NULL,1,'2025-12-29 16:20:31',2,'2026-01-06 13:59:20',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,1,'business','45','2025-12-29 00:00:00','2025-12-30 00:00:00',1.00,'',1,1,'2026-01-06 13:59:22','',NULL,1,'2025-12-29 16:20:45',2,'2026-01-06 13:59:22',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,1,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 13:59:27','',NULL,1,'2026-01-06 13:46:33',2,'2026-01-06 13:59:27','2026-01-06','2026-01-06','','regular',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,1,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 14:22:15','',NULL,1,'2026-01-06 14:14:40',2,'2026-01-06 14:22:15','2026-01-06','2026-01-06','','probation',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(11,4,'regularization',NULL,NULL,NULL,NULL,'',3,NULL,NULL,NULL,NULL,1,'2026-01-06 15:10:06',1,'2026-01-06 15:11:50','2026-01-06',NULL,'','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(12,4,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 15:12:49','',NULL,1,'2026-01-06 15:12:43',1,'2026-01-06 15:12:49','2026-01-06',NULL,'','intern',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(13,1,'transfer',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 15:16:30','',NULL,1,'2026-01-06 15:15:53',1,'2026-01-06 15:16:30',NULL,NULL,NULL,NULL,'3',NULL,NULL,12,17,'senior_engineer','gm','2026-01-06',NULL,NULL,NULL,NULL,NULL,NULL),(14,1,'transfer',NULL,NULL,NULL,NULL,'',3,NULL,NULL,NULL,NULL,1,'2026-01-06 15:24:32',1,'2026-01-06 16:31:25',NULL,NULL,NULL,NULL,'3',NULL,11,17,12,'gm','senior_engineer','2026-01-06',NULL,NULL,NULL,NULL,NULL,NULL),(15,1,'transfer',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 16:35:28','',NULL,1,'2026-01-06 16:31:35',1,'2026-01-06 16:35:28',NULL,NULL,NULL,NULL,'1',16,11,17,12,'gm','','2026-01-06',NULL,NULL,NULL,NULL,NULL,NULL),(16,4,'reward',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 16:37:07','',NULL,1,'2026-01-06 16:34:22',1,'2026-01-06 16:37:07',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-06',1,'1',5.00,NULL,NULL,NULL),(17,1,'punish',NULL,NULL,NULL,NULL,'',3,NULL,NULL,NULL,NULL,1,'2026-01-06 16:35:08',1,'2026-01-06 16:37:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-06',2,'2',50.00,NULL,NULL,NULL),(18,4,'resignation',NULL,NULL,NULL,NULL,'',1,1,'2026-01-06 16:51:40','',NULL,1,'2026-01-06 16:40:20',1,'2026-01-06 16:51:40',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'1','2026-01-06',1),(19,5,'overtime','','2026-01-06 00:00:00','2026-01-14 00:00:00',1.00,'11',1,1,'2026-01-13 16:21:28','',NULL,1,'2026-01-06 17:45:47',1,'2026-01-13 16:21:28',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(20,1,'overtime','','2026-01-06 00:00:00','2026-01-14 00:00:00',1.00,'11',1,1,'2026-01-13 16:21:29','',NULL,1,'2026-01-06 17:45:47',1,'2026-01-13 16:21:29',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(21,5,'leave','1','2026-01-06 00:00:00','2026-01-07 00:00:00',1.00,'',1,1,'2026-01-06 17:52:30','',NULL,1,'2026-01-06 17:51:55',1,'2026-01-06 17:52:30',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(22,5,'leave','事假申请','2026-01-12 21:28:38','2026-01-12 21:28:40',0.00,'111',1,1,'2026-01-13 16:21:26','',NULL,5,'2026-01-12 21:28:42',1,'2026-01-13 16:21:26',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'事假',NULL,NULL,NULL,NULL),(23,1,'overtime','','2026-01-13 00:00:00','2026-01-14 00:00:00',2.00,'',1,1,'2026-01-13 16:21:25','',NULL,1,'2026-01-13 09:48:43',1,'2026-01-13 16:21:25',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(24,5,'makeup','checkout','2026-01-13 00:00:00',NULL,NULL,'',1,1,'2026-01-13 16:21:22','',NULL,1,'2026-01-13 09:50:13',1,'2026-01-13 16:21:22',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(25,1,'exchange','','2026-01-13 00:00:00','2026-01-06 00:00:00',1.00,'',1,1,'2026-01-13 16:21:21','',NULL,1,'2026-01-13 09:50:28',1,'2026-01-13 16:21:21',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(26,5,'business','11','2026-01-13 00:00:00','2026-01-14 00:00:00',1.50,'',1,1,'2026-01-13 16:21:17','',NULL,1,'2026-01-13 09:50:43',1,'2026-01-13 16:21:17',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(27,5,'makeup','checkin','2026-01-13 00:00:00',NULL,NULL,'',1,1,'2026-01-13 16:21:15','',NULL,1,'2026-01-13 15:57:19',1,'2026-01-13 16:21:15',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(28,5,'exchange','','2026-01-13 00:00:00','2026-01-14 00:00:00',NULL,'',1,1,'2026-01-13 16:21:14','',NULL,1,'2026-01-13 16:06:47',1,'2026-01-13 16:21:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(29,5,'business','111','2026-01-13 09:00:00','2026-01-13 17:00:00',7.00,'11',1,1,'2026-01-13 16:21:12','',NULL,1,'2026-01-13 16:13:59',1,'2026-01-13 16:21:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(30,1,'exchange','','2026-03-09 00:00:00','2026-03-14 00:00:00',NULL,'111',1,1,'2026-03-10 09:01:26','',NULL,1,'2026-03-10 09:01:19',1,'2026-03-10 09:01:26',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(31,1,'exchange','','2026-03-09 00:00:00','2026-03-14 00:00:00',NULL,'222',1,1,'2026-03-10 09:20:03','',NULL,1,'2026-03-10 09:19:59',1,'2026-03-10 09:20:03',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(32,1,'exchange','','2026-03-09 00:00:00','2026-03-14 00:00:00',NULL,'333',1,1,'2026-03-10 09:21:02','',NULL,1,'2026-03-10 09:20:53',1,'2026-03-10 09:21:02',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(33,1,'exchange','','2026-03-09 00:00:00','2026-03-14 00:00:00',NULL,'888',1,1,'2026-03-10 09:21:41','',NULL,1,'2026-03-10 09:21:37',1,'2026-03-10 09:21:41',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(34,1,'leave','事假','2026-03-11 15:07:00','2026-03-12 15:07:00',0.00,'11',1,1,'2026-03-11 16:41:10',NULL,NULL,1,'2026-03-11 15:13:12',1,'2026-03-11 16:41:10',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(35,1,'regularization','转正申请',NULL,NULL,NULL,'',1,1,'2026-03-11 16:40:55',NULL,NULL,1,'2026-03-11 15:18:25',1,'2026-03-11 16:40:55','2026-03-11','2026-03-25','111','formal',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(36,5,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2026-03-13 11:32:26','',NULL,8,'2026-03-11 16:54:55',1,'2026-03-13 11:32:26','2026-03-11',NULL,'','regular',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(37,1,'leave','病假','2026-03-13 11:31:00','2026-03-13 16:31:00',4.00,'555',1,1,'2026-03-13 11:32:28','',NULL,1,'2026-03-13 11:32:12',1,'2026-03-13 11:32:28',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(38,1,'overtime','加班申请','2026-03-14 11:37:00','2026-03-14 13:37:00',1.00,'555',1,1,'2026-03-13 11:38:13','',NULL,1,'2026-03-13 11:37:59',1,'2026-03-13 11:38:13',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(39,1,'makeup','上班补卡申请','2026-03-13 11:39:00','2026-03-13 11:39:00',NULL,'555',1,1,'2026-03-13 11:39:33','',NULL,1,'2026-03-13 11:39:27',1,'2026-03-13 11:39:33',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'上班补卡',NULL,NULL,NULL,NULL),(40,1,'resignation','离职申请',NULL,NULL,NULL,'离职原因: 薪资待遇\n详细说明: 555',1,1,'2026-03-13 11:39:51','','',1,'2026-03-13 11:39:46',1,'2026-03-13 11:39:51',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'主动离职','2026-03-13',NULL),(41,1,'business','出差申请-555','2026-03-13 00:00:00','2026-03-14 23:59:59',2.00,'1111',1,1,'2026-03-13 11:40:13','','555',1,'2026-03-13 11:40:07',1,'2026-03-13 11:40:13',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(42,1,'exchange','换休申请','2026-03-13 00:00:00','2026-03-16 00:00:00',NULL,'66',1,1,'2026-03-13 11:46:52','',NULL,1,'2026-03-13 11:46:47',1,'2026-03-13 11:46:52',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(43,6,'regularization',NULL,NULL,NULL,NULL,'',1,1,'2026-03-18 14:26:30','',NULL,1,'2026-03-18 14:26:22',1,'2026-03-18 14:26:30','2026-03-18',NULL,'','regular',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `hr_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_approval_record`
--

DROP TABLE IF EXISTS `hr_approval_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `application_id` bigint NOT NULL COMMENT '申请单ID',
  `node_id` bigint DEFAULT NULL COMMENT '审批节点ID',
  `node_name` varchar(100) DEFAULT NULL COMMENT '节点名称',
  `sort_order` int DEFAULT NULL COMMENT '节点顺序',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approver_name` varchar(50) DEFAULT NULL COMMENT '审批人姓名',
  `status` tinyint NOT NULL COMMENT '审批动作：1-通过，2-拒绝',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_approval_record_node` (`application_id`,`node_id`),
  KEY `idx_hr_approval_record_application` (`application_id`),
  KEY `idx_hr_approval_record_node` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='申请审批流转记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_approval_record`
--

LOCK TABLES `hr_approval_record` WRITE;
/*!40000 ALTER TABLE `hr_approval_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_approval_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_certificate`
--

DROP TABLE IF EXISTS `hr_certificate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_certificate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `cert_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证书名称',
  `cert_photo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证书照片',
  `cert_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证书类型',
  `cert_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证书等级',
  `issue_date` date DEFAULT NULL COMMENT '发证日期',
  `expire_date` date DEFAULT NULL COMMENT '过期日期',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工证书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_certificate`
--

LOCK TABLES `hr_certificate` WRITE;
/*!40000 ALTER TABLE `hr_certificate` DISABLE KEYS */;
INSERT INTO `hr_certificate` VALUES (34,1,'12','/cert_photo/001_1773025856462.png','skill','advanced','2025-12-05','2025-12-19',NULL,NULL);
/*!40000 ALTER TABLE `hr_certificate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_contract`
--

DROP TABLE IF EXISTS `hr_contract`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_contract` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `contract_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '合同类型：字典值 contract_type',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `probation_months` int DEFAULT NULL COMMENT '试用期（月）',
  `salary` decimal(12,2) DEFAULT NULL COMMENT '合同薪资',
  `sign_date` date DEFAULT NULL COMMENT '签订日期',
  `sign_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '签订公司',
  `status` int DEFAULT '1' COMMENT '状态：1-生效中，2-即将到期，3-已到期，4-已终止',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '附件',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `contract_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '合同图片（多张，逗号分隔）',
  `contract_count` int DEFAULT NULL COMMENT '合同次数（第几次合同）',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='劳动合同表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_contract`
--

LOCK TABLES `hr_contract` WRITE;
/*!40000 ALTER TABLE `hr_contract` DISABLE KEYS */;
INSERT INTO `hr_contract` VALUES (9,1,'005','1','2026-01-21','2026-01-24',NULL,NULL,NULL,NULL,3,NULL,'','/contract_photo/001_1773023642394.png',2,'2026-01-21 21:45:53','2026-01-21 21:45:53',1,1);
/*!40000 ALTER TABLE `hr_contract` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_dept_change`
--

DROP TABLE IF EXISTS `hr_dept_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_dept_change` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `old_company_id` bigint DEFAULT NULL COMMENT '原公司ID',
  `new_company_id` bigint DEFAULT NULL COMMENT '新公司ID',
  `old_dept_id` bigint DEFAULT NULL COMMENT '原部门ID',
  `new_dept_id` bigint DEFAULT NULL COMMENT '新部门ID',
  `change_date` date NOT NULL COMMENT '变更日期',
  `change_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '变更原因',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门变更记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_dept_change`
--

LOCK TABLES `hr_dept_change` WRITE;
/*!40000 ALTER TABLE `hr_dept_change` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_dept_change` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_education`
--

DROP TABLE IF EXISTS `hr_education`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_education` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `school_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学校名称',
  `diploma_photo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学历证书照片',
  `is_full_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否全日制',
  `education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学历',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '专业',
  `start_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '开学时间',
  `end_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '毕业时间',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='教育经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_education`
--

LOCK TABLES `hr_education` WRITE;
/*!40000 ALTER TABLE `hr_education` DISABLE KEYS */;
INSERT INTO `hr_education` VALUES (37,6,'11','/diploma_photo/003_1773026116806.jpg','yes','junior_high','11','2026-03','2026-03',NULL,NULL),(41,1,'4545','/uploads/images/2025/12/25/6440ed2f586d4d96bc1b1c2a20b617d7.jpg','','','4545',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `hr_education` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_employee`
--

DROP TABLE IF EXISTS `hr_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '登录密码',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像',
  `id_card_front` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证背面',
  `gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '性别',
  `highest_education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '最高学历',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '民族',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '身份证号',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `employee_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '员工类别',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `regular_date` date DEFAULT NULL COMMENT '转正日期',
  `duty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职务',
  `position` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职位',
  `post` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '岗位',
  `job_function` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职系',
  `job_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职级',
  `job_responsibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职类',
  `job_authority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职等',
  `job_title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职称',
  `occupation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职业',
  `marital_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '婚姻状况',
  `political_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '政治面貌',
  `native_place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '籍贯',
  `police_station` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '派出所',
  `registered_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '户籍地址',
  `home_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '家庭住址',
  `current_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '现居住址',
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '与本人关系',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系电话',
  `leave_date` date DEFAULT NULL COMMENT '离职日期',
  `status` int DEFAULT NULL COMMENT '状态',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_no` (`employee_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_employee`
--

LOCK TABLES `hr_employee` WRITE;
/*!40000 ALTER TABLE `hr_employee` DISABLE KEYS */;
INSERT INTO `hr_employee` VALUES (1,'001','zzx','$2a$10$.KVpIPcpF9ThFu.436p4CuT2pJvn7XMp4CvJ5YsiZG9b5z/RL4k7G','/employee_photo/001.jpg','/id_card_front/001.jpg','/id_card_back/001.jpg','1','bachelor',12,'han','859865464','2025-12-18','156251645','156251645','regular','2024-12-25','2026-03-11','dm','gm',NULL,NULL,'p3',NULL,NULL,NULL,NULL,'single','league_member',NULL,NULL,NULL,NULL,'212',NULL,NULL,NULL,'2026-03-13',1,1,'2025-12-25 18:23:28',1,'2026-09-09 11:14:36'),(2,'002','小明','$2a$10$iFTIPKaINNsZnfsTrKS1DuQJ08bhqkhEey1l3I6v79AOZBzp1gN46','','','','1','college',2,'han','454454545','2025-12-29','156251645','156251645','regular','2025-12-17','2025-12-29','dgm','gm',NULL,NULL,'p3',NULL,NULL,NULL,NULL,'single',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-29',2,1,'2025-12-28 15:56:19',1,'2025-12-29 15:16:04'),(4,'test1','test1','$2a$10$0XpoeOlmxYuJuPhCUb3yr.kmcWrFGDj9fjAAOGQ2QyvxZQ.8fdgky','','','','',NULL,12,NULL,NULL,NULL,'','','intern',NULL,'2026-01-06',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-06',2,1,'2026-01-06 09:38:43',1,'2026-01-06 16:51:40'),(5,'test2','test2','$2a$10$4gVMfyM0JlbXvBwlB2FLv.WfVlYojNkomvez8mB6r8fZOo3AB08V2','','','','','college',13,NULL,NULL,'2005-03-10','','','regular',NULL,'2026-03-11',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-01-06 16:52:55',1,'2026-05-11 11:14:42'),(6,'003','003','$2a$10$hRzRDCJ2bqTv6bn/ajzuZeyzQutphN2SaSjRdu/BXPvDKxrCJShCy','/employee_photo/003.jpg','/id_card_front/003.png','/id_card_back/003.jpg','','junior_high',12,NULL,NULL,'2026-03-02','','','regular',NULL,'2026-03-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-01-24 22:41:42',1,'2026-05-11 11:14:37'),(7,'K20260512001','test03','$2a$10$/zSjWRDKYWCpORKd4suKx.ETBYfwI97zGzpPUZlvcg5O85cT4FHJO','','','','',NULL,13,NULL,NULL,NULL,'','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-05-12 16:25:10',1,'2026-05-12 16:25:10');
/*!40000 ALTER TABLE `hr_employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_employee_extra`
--

DROP TABLE IF EXISTS `hr_employee_extra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_employee_extra` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `field_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段编码（对应字典值的dictValue）',
  `field_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '字段值',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_field` (`employee_id`,`field_code`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='员工扩展信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_employee_extra`
--

LOCK TABLES `hr_employee_extra` WRITE;
/*!40000 ALTER TABLE `hr_employee_extra` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_employee_extra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_family_member`
--

DROP TABLE IF EXISTS `hr_family_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_family_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '姓名',
  `relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '与本人关系',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '政治面貌',
  `work_unit` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工作单位',
  `occupation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职业',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='家庭成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_family_member`
--

LOCK TABLES `hr_family_member` WRITE;
/*!40000 ALTER TABLE `hr_family_member` DISABLE KEYS */;
INSERT INTO `hr_family_member` VALUES (36,1,'12','mother','2025-12-25','masses','12','','12',NULL,NULL);
/*!40000 ALTER TABLE `hr_family_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_leave_quota`
--

DROP TABLE IF EXISTS `hr_leave_quota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_leave_quota` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `year` int NOT NULL COMMENT '年度',
  `leave_type` varchar(50) NOT NULL COMMENT '假期类型（字典 leave_type 值）',
  `total_hours` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '额度总时长（小时）',
  `used_hours` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已使用时长（小时）',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hr_leave_quota` (`employee_id`,`year`,`leave_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='假期额度';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_leave_quota`
--

LOCK TABLES `hr_leave_quota` WRITE;
/*!40000 ALTER TABLE `hr_leave_quota` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_leave_quota` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_mobile_approver`
--

DROP TABLE IF EXISTS `hr_mobile_approver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_mobile_approver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `app_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '可审批的申请类型，逗号分隔，空表示全部',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='移动端审批权限';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_mobile_approver`
--

LOCK TABLES `hr_mobile_approver` WRITE;
/*!40000 ALTER TABLE `hr_mobile_approver` DISABLE KEYS */;
INSERT INTO `hr_mobile_approver` VALUES (1,1,'leave,transfer,overtime,regularization,reward','2026-03-11 17:28:31','2026-03-11 17:28:31',1,1);
/*!40000 ALTER TABLE `hr_mobile_approver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_position_change`
--

DROP TABLE IF EXISTS `hr_position_change`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_position_change` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `old_position_id` bigint DEFAULT NULL COMMENT '原职位ID',
  `new_position_id` bigint DEFAULT NULL COMMENT '新职位ID',
  `change_type` tinyint DEFAULT NULL COMMENT '变更类型：1-晋升，2-降级，3-平调',
  `change_date` date NOT NULL COMMENT '变更日期',
  `change_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '变更原因',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='职位变更记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_position_change`
--

LOCK TABLES `hr_position_change` WRITE;
/*!40000 ALTER TABLE `hr_position_change` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_position_change` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_regularization`
--

DROP TABLE IF EXISTS `hr_regularization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_regularization` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `apply_date` date DEFAULT NULL COMMENT '申请日期',
  `regular_date` date DEFAULT NULL COMMENT '转正日期',
  `probation_end_date` date DEFAULT NULL COMMENT '试用期结束日期',
  `evaluation` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '评价',
  `new_employee_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '转正后员工类别',
  `status` int DEFAULT NULL COMMENT '状态',
  `approve_by` bigint DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='转正表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_regularization`
--

LOCK TABLES `hr_regularization` WRITE;
/*!40000 ALTER TABLE `hr_regularization` DISABLE KEYS */;
INSERT INTO `hr_regularization` VALUES (4,2,'2025-12-29','2025-12-29','2025-12-29','','regular',1,1,'2025-12-29 14:03:56',NULL,'',1,'2025-12-29 14:03:31',1,'2025-12-29 14:03:56');
/*!40000 ALTER TABLE `hr_regularization` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_resignation`
--

DROP TABLE IF EXISTS `hr_resignation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_resignation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `resign_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '离职类型',
  `apply_date` date DEFAULT NULL COMMENT '申请日期',
  `last_work_date` date DEFAULT NULL COMMENT '最后工作日',
  `handover_to` bigint DEFAULT NULL COMMENT '工作交接人ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT NULL COMMENT '状态',
  `approve_by` bigint DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='离职表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_resignation`
--

LOCK TABLES `hr_resignation` WRITE;
/*!40000 ALTER TABLE `hr_resignation` DISABLE KEYS */;
INSERT INTO `hr_resignation` VALUES (3,2,'1','2025-12-29','2025-12-29',NULL,'555',1,1,'2025-12-29 15:16:04',NULL,'',1,'2025-12-29 15:16:02',1,'2025-12-29 15:16:04');
/*!40000 ALTER TABLE `hr_resignation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_reward_punish`
--

DROP TABLE IF EXISTS `hr_reward_punish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_reward_punish` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `type` int DEFAULT NULL COMMENT '类型',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '类别',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '金额',
  `effect_date` date DEFAULT NULL COMMENT '生效日期',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT NULL COMMENT '状态',
  `approve_by` bigint DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='奖惩表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_reward_punish`
--

LOCK TABLES `hr_reward_punish` WRITE;
/*!40000 ALTER TABLE `hr_reward_punish` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_reward_punish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_reward_punishment`
--

DROP TABLE IF EXISTS `hr_reward_punishment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_reward_punishment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `type` tinyint NOT NULL COMMENT '类型：1-奖励，2-惩罚',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '级别',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '内容',
  `amount` decimal(12,2) DEFAULT NULL COMMENT '金额',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '附件',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='奖惩记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_reward_punishment`
--

LOCK TABLES `hr_reward_punishment` WRITE;
/*!40000 ALTER TABLE `hr_reward_punishment` DISABLE KEYS */;
/*!40000 ALTER TABLE `hr_reward_punishment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_transfer`
--

DROP TABLE IF EXISTS `hr_transfer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `transfer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '调动类型',
  `from_company_id` bigint DEFAULT NULL COMMENT '原公司ID',
  `to_company_id` bigint DEFAULT NULL COMMENT '新公司ID',
  `from_dept_id` bigint DEFAULT NULL COMMENT '原部门ID',
  `to_dept_id` bigint DEFAULT NULL COMMENT '新部门ID',
  `from_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原职位',
  `to_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '新职位',
  `effect_date` date DEFAULT NULL COMMENT '生效日期',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原因',
  `status` int DEFAULT NULL COMMENT '状态',
  `approve_by` bigint DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='调动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_transfer`
--

LOCK TABLES `hr_transfer` WRITE;
/*!40000 ALTER TABLE `hr_transfer` DISABLE KEYS */;
INSERT INTO `hr_transfer` VALUES (7,2,'3',1,2,1,2,'cto','gm','2025-12-29','111',1,1,'2025-12-29 14:59:40',NULL,'',1,'2025-12-29 14:59:38',1,'2025-12-29 14:59:40');
/*!40000 ALTER TABLE `hr_transfer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hr_work_experience`
--

DROP TABLE IF EXISTS `hr_work_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hr_work_experience` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `company_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司名称',
  `company_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '公司地址',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职位',
  `witness` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证明人',
  `witness_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '证明人电话',
  `start_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '开始日期',
  `end_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '结束日期',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_employee_id` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工作经历表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hr_work_experience`
--

LOCK TABLES `hr_work_experience` WRITE;
/*!40000 ALTER TABLE `hr_work_experience` DISABLE KEYS */;
INSERT INTO `hr_work_experience` VALUES (69,1,'121','','12','','','12',NULL,NULL,NULL,NULL),(70,1,'12','','','','12','',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `hr_work_experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_chat_group`
--

DROP TABLE IF EXISTS `mobile_chat_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `owner_id` bigint NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `member_count` int NOT NULL DEFAULT '0',
  `last_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `last_message_time` datetime DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mobile_chat_group_owner` (`owner_id`) USING BTREE,
  KEY `idx_mobile_chat_group_status_time` (`status`,`last_message_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端群聊';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_chat_group`
--

LOCK TABLES `mobile_chat_group` WRITE;
/*!40000 ALTER TABLE `mobile_chat_group` DISABLE KEYS */;
INSERT INTO `mobile_chat_group` VALUES (1,'默认工作群',1,NULL,4,'111','2026-09-09 10:56:01',1,'2026-05-14 09:15:27','2026-09-20 16:03:44',NULL,1),(2,'工作群(5人)',1,NULL,4,'111','2026-09-09 11:08:26',1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1),(3,'工作群(3人)',1,NULL,3,'群聊已创建','2026-09-09 11:08:31',1,'2026-09-09 11:08:31','2026-09-09 11:08:31',1,1);
/*!40000 ALTER TABLE `mobile_chat_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_chat_group_member`
--

DROP TABLE IF EXISTS `mobile_chat_group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_chat_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `role_type` tinyint NOT NULL DEFAULT '2',
  `status` tinyint NOT NULL DEFAULT '1',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_mobile_chat_group_member` (`group_id`,`employee_id`) USING BTREE,
  KEY `idx_mobile_chat_group_member_employee` (`employee_id`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端群聊成员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_chat_group_member`
--

LOCK TABLES `mobile_chat_group_member` WRITE;
/*!40000 ALTER TABLE `mobile_chat_group_member` DISABLE KEYS */;
INSERT INTO `mobile_chat_group_member` VALUES (1,1,1,1,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL),(2,1,5,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL),(3,1,6,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL),(4,1,7,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL),(5,2,1,1,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1),(6,2,6,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1),(7,2,5,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1),(8,2,7,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1),(9,3,1,1,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1),(10,3,5,2,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1),(11,3,6,2,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1);
/*!40000 ALTER TABLE `mobile_chat_group_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_chat_message`
--

DROP TABLE IF EXISTS `mobile_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chat_type` tinyint NOT NULL DEFAULT '1' COMMENT '会话类型：1-群聊 2-单聊',
  `group_id` bigint DEFAULT NULL COMMENT '群聊ID',
  `peer_employee_id` bigint DEFAULT NULL COMMENT '对方员工ID（单聊）',
  `from_employee_id` bigint NOT NULL COMMENT '发送人员工ID',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `msg_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型：1-文本 2-图片',
  `status` tinyint DEFAULT '1',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mobile_chat_msg_group` (`group_id`,`created_time`) USING BTREE,
  KEY `idx_mobile_chat_msg_single` (`from_employee_id`,`peer_employee_id`,`created_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端聊天消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_chat_message`
--

LOCK TABLES `mobile_chat_message` WRITE;
/*!40000 ALTER TABLE `mobile_chat_message` DISABLE KEYS */;
INSERT INTO `mobile_chat_message` VALUES (3,1,1,NULL,1,'大家好,这是聊天功能测试消息',1,1,'2026-09-09 10:41:26','2026-09-09 10:41:26',1,1),(4,2,NULL,6,1,'003你好,单聊功能测试',1,1,'2026-09-09 10:43:21','2026-09-09 10:43:21',1,1),(5,1,1,NULL,1,'111',1,1,'2026-09-09 10:56:01','2026-09-09 10:56:01',1,1),(6,2,NULL,6,1,'111',1,1,'2026-09-09 10:56:39','2026-09-09 10:56:39',1,1),(8,1,2,NULL,1,'111',1,1,'2026-09-09 11:08:26','2026-09-09 11:08:26',1,1);
/*!40000 ALTER TABLE `mobile_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_chat_read_state`
--

DROP TABLE IF EXISTS `mobile_chat_read_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_chat_read_state` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `chat_type` tinyint NOT NULL,
  `target_id` bigint NOT NULL,
  `last_read_message_id` bigint NOT NULL DEFAULT '0',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile_chat_read_state` (`employee_id`,`chat_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='移动端聊天会话已读状态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_chat_read_state`
--

LOCK TABLES `mobile_chat_read_state` WRITE;
/*!40000 ALTER TABLE `mobile_chat_read_state` DISABLE KEYS */;
/*!40000 ALTER TABLE `mobile_chat_read_state` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Table structure for table `mobile_moment_comment`
--

DROP TABLE IF EXISTS `mobile_moment_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_moment_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `content` varchar(1000) NOT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mobile_moment_comment_post` (`post_id`),
  KEY `idx_mobile_moment_comment_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='移动端时光评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_moment_comment`
--

LOCK TABLES `mobile_moment_comment` WRITE;
/*!40000 ALTER TABLE `mobile_moment_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `mobile_moment_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_moment_like`
--

DROP TABLE IF EXISTS `mobile_moment_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_moment_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL,
  `employee_id` bigint NOT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_mobile_moment_like_post_employee` (`post_id`,`employee_id`) USING BTREE,
  KEY `idx_mobile_moment_like_employee` (`employee_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端时光点赞';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_moment_like`
--

LOCK TABLES `mobile_moment_like` WRITE;
/*!40000 ALTER TABLE `mobile_moment_like` DISABLE KEYS */;
INSERT INTO `mobile_moment_like` VALUES (4,4,1,'2026-09-09 10:16:31','2026-09-09 10:16:31',1,1);
/*!40000 ALTER TABLE `mobile_moment_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_moment_post`
--

DROP TABLE IF EXISTS `mobile_moment_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_moment_post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `labels` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `visibility` tinyint NOT NULL DEFAULT '1',
  `status` tinyint NOT NULL DEFAULT '1',
  `comment_count` int NOT NULL DEFAULT '0',
  `like_count` int NOT NULL DEFAULT '0',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mobile_moment_post_employee` (`employee_id`) USING BTREE,
  KEY `idx_mobile_moment_post_status_time` (`status`,`created_time`) USING BTREE,
  KEY `idx_mobile_moment_post_hot` (`like_count`,`comment_count`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端时光动态';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_moment_post`
--

LOCK TABLES `mobile_moment_post` WRITE;
/*!40000 ALTER TABLE `mobile_moment_post` DISABLE KEYS */;
INSERT INTO `mobile_moment_post` VALUES (1,1,'欢迎来到时光，这里会展示团队动态、活动记录和同事分享。','团队,公告','https://resource.tuniaokj.com/images/swiper/banner-animate3.png',1,1,2,8,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL),(2,5,'今天完成了移动端首页、时光和通讯录的接口联调，后面可以继续补评论和消息提醒。','研发,移动端','https://resource.tuniaokj.com/images/simple/image3.jpg,https://resource.tuniaokj.com/images/simple/image8.jpg',1,1,4,12,'2026-05-14 09:15:27','2026-05-14 09:15:55',NULL,NULL),(3,6,'组织架构和通讯录已经可以读取真实员工数据，群聊与好友申请也有了后端存储。','人事,通讯录','',1,1,1,6,'2026-05-14 09:15:27','2026-05-14 09:15:52',NULL,NULL),(4,1,'111','随性分享','',1,1,0,1,'2026-05-14 09:20:19','2026-09-09 10:16:31',1,1),(5,1,'34','','',1,1,0,0,'2026-09-09 10:10:36','2026-09-09 10:10:36',1,1),(6,1,'0000','','',1,1,0,0,'2026-09-09 10:10:42','2026-09-09 10:10:42',1,1);
/*!40000 ALTER TABLE `mobile_moment_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mobile_moment_view`
--

DROP TABLE IF EXISTS `mobile_moment_view`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mobile_moment_view` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL,
  `last_view_time` datetime DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile_moment_view_employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='移动端时光消息查看时间';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_moment_view`
--

LOCK TABLES `mobile_moment_view` WRITE;
/*!40000 ALTER TABLE `mobile_moment_view` DISABLE KEYS */;
/*!40000 ALTER TABLE `mobile_moment_view` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `org_unit`
--

DROP TABLE IF EXISTS `org_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `org_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0' COMMENT '父级ID，0表示顶级',
  `unit_type` int NOT NULL COMMENT '类型：1-集团，2-公司，3-部门',
  `unit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
  `unit_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '简称',
  `leader_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `attendance_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Attendance address',
  `attendance_latitude` decimal(10,6) DEFAULT NULL COMMENT 'Attendance latitude (GCJ-02)',
  `attendance_longitude` decimal(10,6) DEFAULT NULL COMMENT 'Attendance longitude (GCJ-02)',
  `attendance_range` int DEFAULT NULL COMMENT 'Attendance radius (meters)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_unit_type` (`unit_type`) USING BTREE,
  KEY `idx_unit_code` (`unit_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='组织单元表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `org_unit`
--

LOCK TABLES `org_unit` WRITE;
/*!40000 ALTER TABLE `org_unit` DISABLE KEYS */;
INSERT INTO `org_unit` VALUES (10,0,1,'K001','集团公司','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:42:44',1,'2026-01-05 14:42:44'),(11,10,2,'G001','公司一','',NULL,'','','','中国江苏省苏州市昆山市红杨路1100号 邮政编码: 215316',31.435435,120.920273,500,'',0,1,'2026-01-05 14:43:53',1,'2026-05-07 09:28:16'),(12,11,3,'B001','部门一','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:44:16',1,'2026-01-05 14:44:16'),(13,11,3,'B002','部门二','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:44:30',1,'2026-01-06 09:45:04'),(16,10,2,'G002','公司二','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:45:23',1,'2026-01-06 09:45:23'),(17,16,3,'B003','部门三','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:45:54',1,'2026-01-06 09:45:54'),(18,16,3,'B004','部门四','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:46:05',1,'2026-01-06 09:46:05');
/*!40000 ALTER TABLE `org_unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_payroll_batch`
--

DROP TABLE IF EXISTS `sal_payroll_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_payroll_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pay_month` char(7) NOT NULL COMMENT '核算月份 yyyy-MM',
  `company_id` bigint NOT NULL COMMENT '核算公司ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-核算中 1-已核算 2-已确认 3-已发放',
  `employee_count` int DEFAULT '0' COMMENT '核算人数',
  `total_gross` decimal(14,2) DEFAULT '0.00' COMMENT '应发合计',
  `total_net` decimal(14,2) DEFAULT '0.00' COMMENT '实发合计',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_batch` (`pay_month`,`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工资核算批次';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_payroll_batch`
--

LOCK TABLES `sal_payroll_batch` WRITE;
/*!40000 ALTER TABLE `sal_payroll_batch` DISABLE KEYS */;
INSERT INTO `sal_payroll_batch` VALUES (1,'2026-08',11,3,4,13100.00,11520.00,'','2026-09-24 16:33:40','2026-09-24 16:46:44',1,1),(2,'2026-08',10,3,4,13100.00,11520.00,'E2E自测','2026-09-24 16:43:01','2026-09-24 16:43:42',1,1);
/*!40000 ALTER TABLE `sal_payroll_batch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_payroll_item`
--

DROP TABLE IF EXISTS `sal_payroll_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_payroll_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payslip_id` bigint NOT NULL COMMENT '工资条ID',
  `item_id` bigint DEFAULT NULL COMMENT '薪资项ID',
  `item_code` varchar(50) DEFAULT NULL COMMENT '项编码快照',
  `item_name` varchar(100) DEFAULT NULL COMMENT '项名称快照',
  `direction` tinyint DEFAULT '1' COMMENT '方向：1-收入 2-扣款',
  `value_type` tinyint DEFAULT '1' COMMENT '取值类型快照',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额',
  `source` varchar(200) DEFAULT NULL COMMENT '取值来源说明',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_sal_payroll_item_payslip` (`payslip_id`)
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工资条明细行';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_payroll_item`
--

LOCK TABLES `sal_payroll_item` WRITE;
/*!40000 ALTER TABLE `sal_payroll_item` DISABLE KEYS */;
INSERT INTO `sal_payroll_item` VALUES (45,9,1,'basic_salary','基本工资',1,1,5000.00,'档案金额',1,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(46,9,2,'post_salary','岗位工资',1,1,3000.00,'档案金额',2,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(47,9,6,'full_attendance_allowance','全勤奖',1,3,300.00,'全勤奖励',3,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(48,9,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',4,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(49,9,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',5,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(50,9,8,'personal_leave_deduction','事假扣款',2,3,0.00,'事假0小时×50.00',6,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(51,9,9,'absent_deduction','旷工扣款',2,3,0.00,'旷工0天×500.00',7,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(52,9,10,'pension_insurance','养老保险(个人)',2,2,400.00,'5000×0.08',8,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(53,9,11,'housing_fund','住房公积金(个人)',2,2,600.00,'5000×0.12',9,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(54,10,1,'basic_salary','基本工资',1,1,2900.00,'档案金额',1,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(55,10,2,'post_salary','岗位工资',1,1,1600.00,'档案金额',2,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(56,10,6,'full_attendance_allowance','全勤奖',1,3,300.00,'全勤奖励',3,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(57,10,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',4,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(58,10,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',5,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(59,10,8,'personal_leave_deduction','事假扣款',2,3,0.00,'事假0小时×50.00',6,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(60,10,9,'absent_deduction','旷工扣款',2,3,0.00,'旷工0天×500.00',7,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(61,10,10,'pension_insurance','养老保险(个人)',2,2,232.00,'2900×0.08',8,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(62,10,11,'housing_fund','住房公积金(个人)',2,2,348.00,'2900×0.12',9,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(63,11,3,'hourly_pay','小时工资',1,3,0.00,'工时0小时×25.00',1,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(64,11,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',2,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(65,11,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',3,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(66,12,4,'daily_pay','日结工资',1,3,0.00,'出勤0天×180.00',1,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(67,13,1,'basic_salary','基本工资',1,1,5000.00,'档案金额',1,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(68,13,2,'post_salary','岗位工资',1,1,3000.00,'档案金额',2,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(69,13,6,'full_attendance_allowance','全勤奖',1,3,300.00,'全勤奖励',3,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(70,13,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',4,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(71,13,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',5,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(72,13,8,'personal_leave_deduction','事假扣款',2,3,0.00,'事假0小时×50.00',6,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(73,13,9,'absent_deduction','旷工扣款',2,3,0.00,'旷工0天×500.00',7,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(74,13,10,'pension_insurance','养老保险(个人)',2,2,400.00,'5000×0.08',8,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(75,13,11,'housing_fund','住房公积金(个人)',2,2,600.00,'5000×0.12',9,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(76,14,1,'basic_salary','基本工资',1,1,2900.00,'档案金额',1,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(77,14,2,'post_salary','岗位工资',1,1,1600.00,'档案金额',2,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(78,14,6,'full_attendance_allowance','全勤奖',1,3,300.00,'全勤奖励',3,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(79,14,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',4,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(80,14,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',5,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(81,14,8,'personal_leave_deduction','事假扣款',2,3,0.00,'事假0小时×50.00',6,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(82,14,9,'absent_deduction','旷工扣款',2,3,0.00,'旷工0天×500.00',7,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(83,14,10,'pension_insurance','养老保险(个人)',2,2,232.00,'2900×0.08',8,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(84,14,11,'housing_fund','住房公积金(个人)',2,2,348.00,'2900×0.12',9,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(85,15,3,'hourly_pay','小时工资',1,3,0.00,'工时0小时×25.00',1,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(86,15,5,'overtime_pay','加班费',1,3,0.00,'加班0小时×40.00',2,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(87,15,7,'late_deduction','迟到扣款',2,3,0.00,'迟到0次×50.00（豁免1次）',3,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(88,16,4,'daily_pay','日结工资',1,3,0.00,'出勤0天×180.00',1,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1);
/*!40000 ALTER TABLE `sal_payroll_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_payroll_payslip`
--

DROP TABLE IF EXISTS `sal_payroll_payslip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_payroll_payslip` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `batch_id` bigint NOT NULL COMMENT '批次ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '工号快照',
  `employee_name` varchar(50) DEFAULT NULL COMMENT '姓名快照',
  `gross_pay` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '应发合计',
  `total_deduction` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '扣款合计',
  `net_pay` decimal(14,2) NOT NULL DEFAULT '0.00' COMMENT '实发',
  `read_flag` tinyint DEFAULT '0' COMMENT '员工已读：0-未读 1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `confirm_flag` tinyint DEFAULT '0' COMMENT '员工已确认：0-未确认 1-已确认',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_payslip` (`batch_id`,`employee_id`),
  KEY `idx_sal_payslip_employee` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工工资条';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_payroll_payslip`
--

LOCK TABLES `sal_payroll_payslip` WRITE;
/*!40000 ALTER TABLE `sal_payroll_payslip` DISABLE KEYS */;
INSERT INTO `sal_payroll_payslip` VALUES (9,2,1,'001','zzx',8300.00,1000.00,7300.00,0,NULL,0,NULL,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(10,2,5,'test2','test2',4800.00,580.00,4220.00,0,NULL,0,NULL,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(11,2,6,'003','003',0.00,0.00,0.00,0,NULL,0,NULL,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(12,2,7,'K20260512001','test03',0.00,0.00,0.00,0,NULL,0,NULL,'2026-09-24 16:43:42','2026-09-24 16:43:42',1,1),(13,1,1,'001','zzx',8300.00,1000.00,7300.00,0,NULL,0,NULL,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(14,1,5,'test2','test2',4800.00,580.00,4220.00,0,NULL,0,NULL,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(15,1,6,'003','003',0.00,0.00,0.00,0,NULL,0,NULL,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1),(16,1,7,'K20260512001','test03',0.00,0.00,0.00,0,NULL,0,NULL,'2026-09-24 16:46:19','2026-09-24 16:46:19',1,1);
/*!40000 ALTER TABLE `sal_payroll_payslip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_salary_archive`
--

DROP TABLE IF EXISTS `sal_salary_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_salary_archive` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `scheme_id` bigint NOT NULL COMMENT '薪资方案ID',
  `effective_date` date DEFAULT NULL COMMENT '生效日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_archive_employee` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工薪资档案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_salary_archive`
--

LOCK TABLES `sal_salary_archive` WRITE;
/*!40000 ALTER TABLE `sal_salary_archive` DISABLE KEYS */;
INSERT INTO `sal_salary_archive` VALUES (1,1,1,'2026-01-01','E2E绑定','2026-09-24 16:32:44','2026-09-24 16:32:44',1,1),(2,5,2,'2026-01-01','E2E绑定','2026-09-24 16:32:49','2026-09-24 16:32:49',1,1),(3,6,3,'2026-01-01','E2E绑定','2026-09-24 16:33:06','2026-09-24 16:33:06',1,1),(4,7,4,'2026-09-24',NULL,'2026-09-24 16:33:13','2026-09-24 16:33:13',1,1);
/*!40000 ALTER TABLE `sal_salary_archive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_salary_archive_item`
--

DROP TABLE IF EXISTS `sal_salary_archive_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_salary_archive_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `archive_id` bigint NOT NULL COMMENT '档案ID',
  `item_id` bigint NOT NULL COMMENT '薪资项ID',
  `amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '金额(可覆盖方案默认)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_archive_item` (`archive_id`,`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工薪资档案明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_salary_archive_item`
--

LOCK TABLES `sal_salary_archive_item` WRITE;
/*!40000 ALTER TABLE `sal_salary_archive_item` DISABLE KEYS */;
INSERT INTO `sal_salary_archive_item` VALUES (17,1,1,5000.00,'2026-09-24 16:44:41','2026-09-24 16:44:41',1,1),(18,1,2,3000.00,'2026-09-24 16:44:41','2026-09-24 16:44:41',1,1),(19,2,1,2900.00,'2026-09-24 16:44:41','2026-09-24 16:44:41',1,1),(20,2,2,1600.00,'2026-09-24 16:44:41','2026-09-24 16:44:41',1,1);
/*!40000 ALTER TABLE `sal_salary_archive_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_salary_item_def`
--

DROP TABLE IF EXISTS `sal_salary_item_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_salary_item_def` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_code` varchar(50) NOT NULL COMMENT '薪资项编码(小写字母/数字/下划线)',
  `item_name` varchar(100) NOT NULL COMMENT '薪资项名称',
  `direction` tinyint NOT NULL DEFAULT '1' COMMENT '方向：1-收入 2-扣款',
  `value_type` tinyint NOT NULL DEFAULT '1' COMMENT '取值类型：1-固定 2-比例 3-考勤联动 4-手工 5-公式(预留)',
  `ratio_base_code` varchar(50) DEFAULT NULL COMMENT '比例基项编码(value_type=2)',
  `ratio_value` decimal(8,4) DEFAULT NULL COMMENT '比例值(value_type=2,如0.12)',
  `att_rule` varchar(30) DEFAULT NULL COMMENT '考勤联动规则(value_type=3)',
  `unit_price` decimal(12,2) DEFAULT NULL COMMENT '单价/固定给付(value_type=3)',
  `tolerance` int DEFAULT NULL COMMENT '容忍值(迟到次数/分钟豁免)',
  `formula` varchar(200) DEFAULT NULL COMMENT '公式(value_type=5预留)',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：1-是 0-否',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_item_code` (`item_code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资项定义';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_salary_item_def`
--

LOCK TABLES `sal_salary_item_def` WRITE;
/*!40000 ALTER TABLE `sal_salary_item_def` DISABLE KEYS */;
INSERT INTO `sal_salary_item_def` VALUES (1,'basic_salary','基本工资',1,1,NULL,NULL,NULL,NULL,NULL,NULL,1,10,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(2,'post_salary','岗位工资',1,1,NULL,NULL,NULL,NULL,NULL,NULL,1,20,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(3,'hourly_pay','小时工资',1,3,NULL,NULL,'WORK_HOURS',25.00,NULL,NULL,1,30,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(4,'daily_pay','日结工资',1,3,NULL,NULL,'ATTEND_DAYS',180.00,NULL,NULL,1,31,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(5,'overtime_pay','加班费',1,3,NULL,NULL,'OVERTIME_HOURS',40.00,NULL,NULL,1,40,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(6,'full_attendance_allowance','全勤奖',1,3,NULL,NULL,'FULL_ATTENDANCE',300.00,NULL,NULL,1,50,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(7,'late_deduction','迟到扣款',2,3,NULL,NULL,'LATE_TIMES',50.00,1,NULL,1,60,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(8,'personal_leave_deduction','事假扣款',2,3,NULL,NULL,'PERSONAL_LEAVE_HOURS',50.00,NULL,NULL,1,61,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(9,'absent_deduction','旷工扣款',2,3,NULL,NULL,'ABSENT_DAYS',500.00,NULL,NULL,1,62,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(10,'pension_insurance','养老保险(个人)',2,2,'basic_salary',0.0800,NULL,NULL,NULL,NULL,1,70,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(11,'housing_fund','住房公积金(个人)',2,2,'basic_salary',0.1200,NULL,NULL,NULL,NULL,1,71,NULL,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1);
/*!40000 ALTER TABLE `sal_salary_item_def` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_salary_scheme`
--

DROP TABLE IF EXISTS `sal_salary_scheme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_salary_scheme` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scheme_code` varchar(50) DEFAULT NULL COMMENT '方案编码',
  `scheme_name` varchar(100) NOT NULL COMMENT '方案名称',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：1-是 0-否',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_scheme_code` (`scheme_code`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资方案';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_salary_scheme`
--

LOCK TABLES `sal_salary_scheme` WRITE;
/*!40000 ALTER TABLE `sal_salary_scheme` DISABLE KEYS */;
INSERT INTO `sal_salary_scheme` VALUES (1,'office','办公室方案',1,0,'管理/职能岗：基本+岗位+全勤+加班，五险一金按基本工资比例扣除','2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(2,'line_regular','产线正常班',1,0,'产线正常班：底薪较低+加班费为主，含全勤与考勤扣款、五险一金','2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(3,'hourly','小时工',1,0,'小时工：按月度累计工时×时薪计薪，另有加班费与迟到扣款','2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(4,'daily','日结工',1,0,'日结工：按实际出勤天数×日单价计薪','2026-09-24 16:31:17','2026-09-24 16:31:17',1,1);
/*!40000 ALTER TABLE `sal_salary_scheme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sal_scheme_item`
--

DROP TABLE IF EXISTS `sal_scheme_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sal_scheme_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scheme_id` bigint NOT NULL COMMENT '方案ID',
  `item_id` bigint NOT NULL COMMENT '薪资项ID',
  `default_amount` decimal(12,2) DEFAULT NULL COMMENT 'FIXED项默认金额',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序(计算顺序,比例项只能引用前面的项)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sal_scheme_item` (`scheme_id`,`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='薪资方案明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sal_scheme_item`
--

LOCK TABLES `sal_scheme_item` WRITE;
/*!40000 ALTER TABLE `sal_scheme_item` DISABLE KEYS */;
INSERT INTO `sal_scheme_item` VALUES (1,1,1,5000.00,1,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(2,1,2,3000.00,2,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(3,1,6,NULL,3,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(4,1,5,NULL,4,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(5,1,7,NULL,5,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(6,1,8,NULL,6,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(7,1,9,NULL,7,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(8,1,10,NULL,8,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(9,1,11,NULL,9,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(10,2,1,2900.00,1,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(11,2,2,1600.00,2,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(12,2,6,NULL,3,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(13,2,5,NULL,4,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(14,2,7,NULL,5,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(15,2,8,NULL,6,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(16,2,9,NULL,7,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(17,2,10,NULL,8,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(18,2,11,NULL,9,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(19,3,3,NULL,1,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(20,3,5,NULL,2,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(21,3,7,NULL,3,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1),(22,4,4,NULL,1,'2026-09-24 16:31:17','2026-09-24 16:31:17',1,1);
/*!40000 ALTER TABLE `sal_scheme_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_approval_flow`
--

DROP TABLE IF EXISTS `sys_approval_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_approval_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程编码',
  `flow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程名称',
  `flow_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '流程类型',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `status` int DEFAULT NULL COMMENT '状态',
  `auto_pass` tinyint DEFAULT NULL COMMENT '是否自动通过：0-否，1-是',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `flow_code` (`flow_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='审批流程定义表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_approval_flow`
--

LOCK TABLES `sys_approval_flow` WRITE;
/*!40000 ALTER TABLE `sys_approval_flow` DISABLE KEYS */;
INSERT INTO `sys_approval_flow` VALUES (7,'Z001','转正管理','regularization','',1,0,1,'2026-01-06 14:14:26',1,'2026-01-06 14:21:52'),(8,'D001','调动申请','transfer','',1,0,1,'2026-01-06 15:16:24',1,'2026-01-06 15:16:24'),(9,'J001','奖惩申请','reward','',1,0,1,'2026-01-06 16:37:02',1,'2026-01-06 16:37:02'),(10,'L001','离职申请','resignation','',1,0,1,'2026-01-06 16:51:35',1,'2026-01-06 16:51:35'),(11,'Q001','请假申请','leave','',1,0,1,'2026-01-06 17:52:21',1,'2026-01-06 17:52:21'),(14,'jb001','加班申请','overtime','',1,0,1,'2026-01-13 09:48:28',1,'2026-01-13 09:48:28'),(15,'bk001','补卡申请','makeup','',1,0,1,'2026-01-13 09:49:15',1,'2026-01-13 09:49:15'),(16,'hx','换休申请','exchange','',1,0,1,'2026-01-13 09:49:40',1,'2026-01-13 09:49:40'),(17,'cc001','出差申请','business','',1,0,1,'2026-01-13 09:50:00',1,'2026-01-13 09:50:00');
/*!40000 ALTER TABLE `sys_approval_flow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_approval_node`
--

DROP TABLE IF EXISTS `sys_approval_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_approval_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flow_id` bigint DEFAULT NULL COMMENT '流程ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '节点名称',
  `node_type` int DEFAULT NULL COMMENT '节点类型',
  `approver_type` int DEFAULT NULL COMMENT '审批人类型',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID',
  `sort_order` int DEFAULT NULL COMMENT '排序',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_flow_id` (`flow_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='审批节点表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_approval_node`
--

LOCK TABLES `sys_approval_node` WRITE;
/*!40000 ALTER TABLE `sys_approval_node` DISABLE KEYS */;
INSERT INTO `sys_approval_node` VALUES (3,2,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43'),(4,3,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43'),(5,3,'总经理审批',1,3,NULL,2,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43'),(6,4,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43'),(7,5,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43'),(8,1,'指定人员',1,1,NULL,1,1,'2025-12-28 16:57:30',1,'2026-01-06 14:05:19'),(9,6,'001',1,1,NULL,1,1,'2026-01-06 13:57:53',1,'2026-01-06 14:05:19'),(12,7,'审批',1,1,1,1,1,'2026-01-06 14:14:26',1,'2026-01-06 14:14:26'),(13,8,'申请',1,1,1,1,1,'2026-01-06 15:16:24',1,'2026-01-06 15:16:24'),(14,9,'审批',1,1,1,1,1,'2026-01-06 16:37:02',1,'2026-01-06 16:37:02'),(15,10,'审批',1,1,1,1,1,'2026-01-06 16:51:35',1,'2026-01-06 16:51:35'),(16,11,'审批',1,1,1,1,1,'2026-01-06 17:52:21',1,'2026-01-06 17:52:21'),(17,14,'申请',1,1,1,1,1,'2026-01-13 09:48:28',1,'2026-01-13 09:48:28'),(18,15,'补卡申请',1,1,1,1,1,'2026-01-13 09:49:15',1,'2026-01-13 09:49:15'),(19,16,'换休申请',1,1,1,1,1,'2026-01-13 09:49:40',1,'2026-01-13 09:49:40'),(20,17,'出差申请',1,1,1,1,1,'2026-01-13 09:50:00',1,'2026-01-13 09:50:00');
/*!40000 ALTER TABLE `sys_approval_node` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS `sys_dict_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签（默认语言）',
  `dict_label_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '字典标签(英文)',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'CSS样式',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列表样式',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认：0-否，1-是',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_data`
--

LOCK TABLES `sys_dict_data` WRITE;
/*!40000 ALTER TABLE `sys_dict_data` DISABLE KEYS */;
INSERT INTO `sys_dict_data` VALUES (45,1,'男','Male','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(46,1,'女','Female','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(47,1,'未知','Unknown','0',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 21:33:37',NULL,1),(48,2,'试用','Probation','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(49,2,'正式','Regular','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(50,2,'离职','Resigned','3',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(51,3,'固定期限','Fixed Term','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(52,3,'无固定期限','Open-ended','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(55,4,'高中','High School','high_school',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL),(56,4,'大专','College','college',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL),(57,4,'本科','Bachelor','bachelor',NULL,NULL,5,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL),(58,4,'硕士','Master','master',NULL,NULL,6,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL),(59,4,'博士','Doctor','doctor',NULL,NULL,7,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL),(60,5,'年假','Annual Leave','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(61,5,'事假','Personal Leave','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(62,5,'病假','Sick Leave','3',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(63,5,'婚假','Marriage Leave','4',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(64,5,'产假','Maternity Leave','5',NULL,NULL,5,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(65,5,'陪产假','Paternity Leave','6',NULL,NULL,6,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(66,5,'丧假','Bereavement Leave','7',NULL,NULL,7,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(67,6,'待审批','Pending','0',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(68,6,'已通过','Approved','1',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(69,6,'已驳回','Rejected','2',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(70,6,'已撤销','Withdrawn','3',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL),(71,7,'启用','Enabled','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(72,7,'禁用','Disabled','0',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL),(76,4,'初中','Junior High School','junior_high',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(78,4,'中专','Secondary Vocational','secondary_vocational',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(83,15,'汉族','Han','han',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(84,15,'满族','Manchu','manchu',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(85,15,'蒙古族','Mongolian','mongolian',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(86,15,'回族','Hui','hui',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(87,15,'藏族','Tibetan','tibetan',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(88,15,'维吾尔族','Uyghur','uyghur',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(89,15,'苗族','Miao','miao',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(90,15,'彝族','Yi','yi',NULL,NULL,8,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(91,15,'壮族','Zhuang','zhuang',NULL,NULL,9,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(92,15,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(93,16,'正式员工','Regular Employee','regular',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(94,16,'试用员工','Probation Employee','probation',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(95,16,'实习生','Intern','intern',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(96,16,'兼职','Part-time','part_time',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(97,16,'外包','Outsourcing','outsourcing',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(98,17,'未婚','Single','single',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(99,17,'已婚','Married','married',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(100,17,'离异','Divorced','divorced',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(101,17,'丧偶','Widowed','widowed',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(102,18,'群众','Masses','masses',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(103,18,'共青团员','Communist Youth League Member','league_member',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(104,18,'中共党员','CPC Member','party_member',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(105,18,'中共预备党员','CPC Probationary Member','probationary_member',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(106,18,'民主党派','Democratic Party','democratic_party',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(107,19,'父亲','Father','father',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(108,19,'母亲','Mother','mother',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(109,19,'配偶','Spouse','spouse',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(110,19,'子女','Child','child',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(111,19,'兄弟','Brother','brother',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(112,19,'姐妹','Sister','sister',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(113,19,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(114,20,'是','Yes','yes',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(115,20,'否','No','no',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(116,21,'职业资格证书','Professional Qualification','professional',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(117,21,'技能等级证书','Skill Level Certificate','skill',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(118,21,'学历证书','Academic Certificate','academic',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(119,21,'语言证书','Language Certificate','language',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(120,21,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(121,22,'初级','Primary','primary',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(122,22,'中级','Intermediate','intermediate',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(123,22,'高级','Advanced','advanced',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(124,22,'特级','Expert','expert',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(125,23,'董事长','Chairman','chairman',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(126,23,'总经理','General Manager','gm',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(127,23,'副总经理','Deputy General Manager','dgm',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(128,23,'部门经理','Department Manager','dm',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(129,23,'主管','Supervisor','supervisor',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(130,23,'组长','Team Leader','team_leader',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(131,23,'员工','Staff','staff',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(132,24,'P1','P1','p1',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(133,24,'P2','P2','p2',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(134,24,'P3','P3','p3',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(135,24,'P4','P4','p4',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(136,24,'P5','P5','p5',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(137,24,'M1','M1','m1',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(138,24,'M2','M2','m2',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(139,24,'M3','M3','m3',NULL,NULL,8,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(142,27,'总经理','General Manager','gm',NULL,NULL,1,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(143,27,'副总经理','Deputy General Manager','dgm',NULL,NULL,2,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(144,27,'技术总监','CTO','cto',NULL,NULL,3,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(145,27,'产品总监','CPO','cpo',NULL,NULL,4,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(146,27,'技术经理','Technical Manager','tech_manager',NULL,NULL,5,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(147,27,'产品经理','Product Manager','product_manager',NULL,NULL,6,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(148,27,'项目经理','Project Manager','project_manager',NULL,NULL,7,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(149,27,'高级软件工程师','Senior Software Engineer','senior_engineer',NULL,NULL,8,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(150,27,'软件工程师','Software Engineer','engineer',NULL,NULL,9,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(151,27,'初级软件工程师','Junior Software Engineer','junior_engineer',NULL,NULL,10,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(152,27,'测试工程师','Test Engineer','test_engineer',NULL,NULL,11,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(153,27,'运维工程师','DevOps Engineer','devops_engineer',NULL,NULL,12,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(154,27,'UI设计师','UI Designer','ui_designer',NULL,NULL,13,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(155,27,'人事经理','HR Manager','hr_manager',NULL,NULL,14,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(156,27,'人事专员','HR Specialist','hr_specialist',NULL,NULL,15,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(157,27,'财务经理','Finance Manager','finance_manager',NULL,NULL,16,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(158,27,'财务专员','Finance Specialist','finance_specialist',NULL,NULL,17,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(159,27,'行政专员','Admin Specialist','admin_specialist',NULL,NULL,18,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(160,27,'销售经理','Sales Manager','sales_manager',NULL,NULL,19,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(161,27,'销售代表','Sales Representative','sales_rep',NULL,NULL,20,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL),(163,28,'部门调动','Department Transfer','1',NULL,NULL,1,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL),(164,28,'职位变更','Position Change','2',NULL,NULL,2,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL),(165,28,'部门+职位变更','Dept Position Change','3',NULL,NULL,3,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL),(166,29,'主动离职','Voluntary','1',NULL,NULL,1,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL),(167,29,'被动离职','Involuntary','2',NULL,NULL,2,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL),(168,29,'合同到期','Contract Expired','3',NULL,NULL,3,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL),(169,29,'退休','Retirement','4',NULL,NULL,4,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL),(170,30,'优秀员工','Outstanding Employee','1',NULL,NULL,1,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL),(171,30,'项目奖金','Project Bonus','2',NULL,NULL,2,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL),(172,30,'年终奖','Year-end Bonus','3',NULL,NULL,3,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL),(173,30,'创新奖','Innovation Award','4',NULL,NULL,4,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL),(174,30,'其他奖励','Other Reward','5',NULL,NULL,5,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL),(175,31,'警告','Warning','1',NULL,NULL,1,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL),(176,31,'记过','Demerit','2',NULL,NULL,2,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL),(177,31,'降级','Demotion','3',NULL,NULL,3,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL),(178,31,'罚款','Fine','4',NULL,NULL,4,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL),(179,31,'其他处罚','Other Punishment','5',NULL,NULL,5,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL),(180,33,'爱好',NULL,'hobby',NULL,NULL,1,1,0,'2025-12-29 13:39:19','2025-12-29 13:39:19',NULL,NULL),(181,33,'特长',NULL,'specialty',NULL,NULL,2,1,0,'2025-12-29 13:39:26','2025-12-29 13:39:26',NULL,NULL),(182,34,'集团',NULL,'1',NULL,NULL,1,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL),(183,34,'公司',NULL,'2',NULL,NULL,2,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL),(184,34,'部门',NULL,'3',NULL,NULL,3,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
/*!40000 ALTER TABLE `sys_dict_data` ENABLE KEYS */;
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
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS `sys_dict_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称（默认语言）',
  `dict_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '字典名称(英文)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `dict_code` (`dict_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type`
--

LOCK TABLES `sys_dict_type` WRITE;
/*!40000 ALTER TABLE `sys_dict_type` DISABLE KEYS */;
INSERT INTO `sys_dict_type` VALUES (1,'gender','性别','Gender','性别字典',1,'2025-12-25 14:42:50','2025-12-25 18:19:15',NULL,1),(2,'employee_status','员工状态','Employee Status','员工在职状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(3,'contract_type','合同类型','Contract Type','劳动合同类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(4,'education','学历','Education','学历类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(5,'leave_type','请假类型','Leave Type','请假申请类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(6,'approval_status','审批状态','Approval Status','审批流程状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(7,'sys_status','系统状态','System Status','通用启用禁用状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL),(15,'nation','民族','Nation',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(16,'employee_type','员工类别','Employee Type',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(17,'marital_status','婚姻状况','Marital Status',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(18,'political_status','政治面貌','Political Status',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(19,'family_relation','家庭成员关系','Family Relation',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(20,'full_time','是否全日制','Full Time',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(21,'cert_type','证书类型','Certificate Type',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(22,'cert_level','证书等级','Certificate Level',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(23,'duty','职务','Duty',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(24,'job_level','职级','Job Level',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL),(27,'position','职位','Position','员工职位',1,'2025-12-25 22:07:17','2025-12-25 22:07:17',NULL,NULL),(28,'transfer_type','调动类型','Transfer Type','员工调动类型',1,'2025-12-28 16:28:41','2025-12-28 16:28:41',NULL,NULL),(29,'resign_type','离职类型','Resignation Type','员工离职类型',1,'2025-12-28 16:29:19','2025-12-28 16:29:19',NULL,NULL),(30,'reward_category','奖励类别','Reward Category','奖励类别',1,'2025-12-28 16:29:53','2025-12-28 16:29:53',NULL,NULL),(31,'punish_category','惩罚类别','Punishment Category','惩罚类别',1,'2025-12-28 16:29:53','2025-12-28 16:29:53',NULL,NULL),(33,'employee_extra_field','员工扩展字段',NULL,'用于定义员工的额外自定义字段',1,'2025-12-29 13:38:21','2025-12-29 13:38:21',NULL,NULL),(34,'org_unit_type','组织类型',NULL,'组织架构节点类型',1,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
/*!40000 ALTER TABLE `sys_dict_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict_type_i18n`
--

DROP TABLE IF EXISTS `sys_dict_type_i18n`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict_type_i18n` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_type_lang` (`dict_type_id`,`lang_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典类型多语言表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict_type_i18n`
--

LOCK TABLES `sys_dict_type_i18n` WRITE;
/*!40000 ALTER TABLE `sys_dict_type_i18n` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_dict_type_i18n` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_feedback`
--

DROP TABLE IF EXISTS `sys_feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `feedback_type` tinyint NOT NULL DEFAULT '1' COMMENT '反馈类型：1-功能建议，2-问题反馈，3-其他',
  `feedback_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `employee_id` bigint DEFAULT NULL COMMENT '员工ID',
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '员工工号',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0-待处理，1-处理中，2-已处理',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_feedback_status` (`status`) USING BTREE,
  KEY `idx_sys_feedback_employee_id` (`employee_id`) USING BTREE,
  KEY `idx_sys_feedback_created_time` (`created_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='意见反馈';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_feedback`
--

LOCK TABLES `sys_feedback` WRITE;
/*!40000 ALTER TABLE `sys_feedback` DISABLE KEYS */;
INSERT INTO `sys_feedback` VALUES (1,2,'1212121','001','156251645',1,'001',2,'5555666','2026-05-14 15:42:41','2026-05-13 17:00:53','2026-05-13 17:00:53',1,1);
/*!40000 ALTER TABLE `sys_feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_file_config`
--

DROP TABLE IF EXISTS `sys_file_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值(路径)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key` (`config_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='文件路径配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_config`
--

LOCK TABLES `sys_file_config` WRITE;
/*!40000 ALTER TABLE `sys_file_config` DISABLE KEYS */;
INSERT INTO `sys_file_config` VALUES (2,'employee_avatar_path','员工头像路径','D:/rzphoto/employee_photo','员工头像照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL),(3,'id_card_front_path','身份证正面路径','D:/rzphoto/id_card_front','员工身份证正面照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL),(4,'id_card_back_path','身份证反面路径','D:/rzphoto/id_card_back','员工身份证反面照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL),(5,'contract_photo_path','合同照片路径','D:/rzphoto/Pic_Contract','合同照片存储目录','2026-03-09 10:17:22','2026-03-09 10:17:22',NULL,NULL),(6,'diploma_photo_path','毕业证照片路径','D:/rzphoto/Pic_Diploma','毕业证照片存储目录','2026-03-09 11:08:07','2026-03-09 11:08:07',NULL,NULL),(7,'cert_photo_path','证书照片路径','D:/rzphoto/Pic_Certificate','证书照片存储目录','2026-03-09 11:08:07','2026-03-09 11:08:07',NULL,NULL);
/*!40000 ALTER TABLE `sys_file_config` ENABLE KEYS */;
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
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_type` tinyint DEFAULT '1' COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `menu_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单编码',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称（默认语言）',
  `menu_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单名称（英文）',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `visible` tinyint DEFAULT '1' COMMENT '是否可见：0-否，1-是',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1280 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,0,1,'home','首页','Home','/home','layout.base$view.home',NULL,'mdi:monitor-dashboard',1,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(2,0,1,'organization','组织架构','Organization','/organization','layout.base',NULL,'mdi:office-building',2,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(3,0,1,'hr','人事管理','HR Management','/hr','layout.base',NULL,'mdi:account-group',3,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(4,0,1,'attendance','考勤管理','Attendance','/attendance','layout.base',NULL,'mdi:clock-outline',4,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(5,0,1,'application','申请管理','Applications','/application','layout.base',NULL,'mdi:file-document-multiple',5,1,1,'2025-12-25 14:10:05','2025-12-25 16:28:29',NULL,NULL),(6,0,1,'approval','审批管理','Approval','/approval','layout.base',NULL,'mdi:check-decagram',6,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(7,0,1,'report','报表管理','Reports','/report','layout.base',NULL,'mdi:chart-bar',7,1,1,'2025-12-25 14:10:05','2025-12-25 16:28:29',NULL,NULL),(8,0,1,'system','系统管理','System','/system','layout.base',NULL,'mdi:cog',8,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL),(24,2,2,'organization_org-structure','组织架构','Organization Structure','/organization/org-structure','view.organization_org-structure',NULL,'mdi:file-tree',0,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(31,3,2,'hr_employee','员工管理','Employee Management','/hr/employee','view.hr_employee',NULL,'mdi:account-multiple',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(32,3,2,'hr_contract','合同管理','Contract Management','/hr/contract','view.hr_contract',NULL,'mdi:file-document-edit',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(41,4,2,'attendance_shift','班次管理','Shift Management','/attendance/shift','view.attendance_shift',NULL,'mdi:clock-time-four',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(42,4,2,'attendance_schedule','员工排班','Employee Schedule','/attendance/schedule','view.attendance_schedule',NULL,'mdi:calendar-clock',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(43,4,2,'attendance_holiday','节假日管理','Holiday Management','/attendance/holiday','view.attendance_holiday',NULL,'mdi:calendar-star',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(44,4,2,'attendance_daily','日考勤','Daily Attendance','/attendance/daily','view.attendance_daily',NULL,'mdi:calendar-today',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(45,4,2,'attendance_monthly','月考勤','Monthly Attendance','/attendance/monthly','view.attendance_monthly',NULL,'mdi:calendar-month',5,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(51,5,2,'application_leave','请假申请','Leave Application','/application/leave','view.application_leave',NULL,'mdi:beach',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(52,5,2,'application_overtime','加班申请','Overtime Application','/application/overtime','view.application_overtime',NULL,'mdi:clock-plus',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(53,5,2,'application_makeup','补卡申请','Makeup Application','/application/makeup','view.application_makeup',NULL,'mdi:clock-check',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(54,5,2,'application_exchange','换休申请','Exchange Leave','/application/exchange','view.application_exchange','','mdi:swap-horizontal-circle',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,1),(55,5,2,'application_business','出差申请','Business Trip','/application/business','view.application_business',NULL,'mdi:airplane',5,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(61,6,2,'approval_pending','待审批','Pending Approval','/approval/pending','view.approval_pending',NULL,'mdi:clock-alert',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(62,6,2,'approval_mine','我的申请','My Applications','/approval/mine','view.approval_mine',NULL,'mdi:file-document-check',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(63,6,2,'approval_flow','审批流程配置','Approval Flow Config','/approval/flow','view.approval_flow',NULL,'mdi:sitemap',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(71,7,2,'report_employee','员工报表','Employee Report','/report/employee','view.report_employee',NULL,'mdi:chart-pie',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(72,7,2,'report_attendance','考勤报表','Attendance Report','/report/attendance','view.report_attendance',NULL,'mdi:chart-line',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(81,8,2,'system_user','用户管理','User Management','/system/user','view.system_user','','mdi:account',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,1),(82,8,2,'system_role','角色管理','Role Management','/system/role','view.system_role',NULL,'mdi:account-key',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(83,8,2,'system_menu','菜单管理','Menu Management','/system/menu','view.system_menu',NULL,'mdi:menu',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(84,8,2,'system_dict','字典管理','Dictionary Management','/system/dict','view.system_dict',NULL,'mdi:book-alphabet',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL),(85,1246,2,'system_notice','公告管理','Notice Management','/system/notice','view.system_notice','system:notice:list','mdi:bullhorn',1,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL),(1086,31,3,'hr_employee_add','新增','Add',NULL,NULL,'hr:employee:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1087,31,3,'hr_employee_edit','编辑','Edit',NULL,NULL,'hr:employee:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1088,31,3,'hr_employee_delete','删除','Delete',NULL,NULL,'hr:employee:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1089,31,3,'hr_employee_export','导出','Export',NULL,NULL,'hr:employee:export',NULL,4,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1091,32,3,'hr_contract_add','新增','Add',NULL,NULL,'hr:contract:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1092,32,3,'hr_contract_edit','编辑','Edit',NULL,NULL,'hr:contract:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1093,32,3,'hr_contract_delete','删除','Delete',NULL,NULL,'hr:contract:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1127,81,3,'system_user_add','新增','Add',NULL,NULL,'system:user:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1128,81,3,'system_user_edit','编辑','Edit',NULL,NULL,'system:user:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1129,81,3,'system_user_delete','删除','Delete',NULL,NULL,'system:user:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1130,81,3,'system_user_reset','重置密码','Reset Password',NULL,NULL,'system:user:reset',NULL,4,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1132,82,3,'system_role_add','新增','Add',NULL,NULL,'system:role:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1133,82,3,'system_role_edit','编辑','Edit',NULL,NULL,'system:role:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1134,82,3,'system_role_delete','删除','Delete',NULL,NULL,'system:role:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1136,83,3,'system_menu_add','新增','Add',NULL,NULL,'system:menu:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1137,83,3,'system_menu_edit','编辑','Edit',NULL,NULL,'system:menu:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1138,83,3,'system_menu_delete','删除','Delete',NULL,NULL,'system:menu:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1140,84,3,'system_dict_add','新增','Add',NULL,NULL,'system:dict:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1141,84,3,'system_dict_edit','编辑','Edit',NULL,NULL,'system:dict:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1142,84,3,'system_dict_delete','删除','Delete',NULL,NULL,'system:dict:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1144,41,3,'attendance_shift_add','新增','Add',NULL,NULL,'attendance:shift:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1145,41,3,'attendance_shift_edit','编辑','Edit',NULL,NULL,'attendance:shift:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1146,41,3,'attendance_shift_delete','删除','Delete',NULL,NULL,'attendance:shift:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1148,42,3,'attendance_schedule_add','新增','Add',NULL,NULL,'attendance:schedule:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1149,42,3,'attendance_schedule_edit','编辑','Edit',NULL,NULL,'attendance:schedule:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1150,42,3,'attendance_schedule_delete','删除','Delete',NULL,NULL,'attendance:schedule:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1152,43,3,'attendance_holiday_add','新增','Add',NULL,NULL,'attendance:holiday:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1153,43,3,'attendance_holiday_edit','编辑','Edit',NULL,NULL,'attendance:holiday:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1154,43,3,'attendance_holiday_delete','删除','Delete',NULL,NULL,'attendance:holiday:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1156,44,3,'attendance_daily_export','导出','Export',NULL,NULL,'attendance:daily:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1158,45,3,'attendance_monthly_export','导出','Export',NULL,NULL,'attendance:monthly:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1160,51,3,'application_leave_add','新增','Add',NULL,NULL,'application:leave:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1161,51,3,'application_leave_edit','编辑','Edit',NULL,NULL,'application:leave:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1162,51,3,'application_leave_delete','删除','Delete',NULL,NULL,'application:leave:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1164,52,3,'application_overtime_add','新增','Add',NULL,NULL,'application:overtime:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1165,52,3,'application_overtime_edit','编辑','Edit',NULL,NULL,'application:overtime:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1166,52,3,'application_overtime_delete','删除','Delete',NULL,NULL,'application:overtime:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1168,53,3,'application_makeup_add','新增','Add',NULL,NULL,'application:makeup:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1169,53,3,'application_makeup_edit','编辑','Edit',NULL,NULL,'application:makeup:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1170,53,3,'application_makeup_delete','删除','Delete',NULL,NULL,'application:makeup:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1172,54,3,'application_exchange_add','新增','Add',NULL,NULL,'application:exchange:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1173,54,3,'application_exchange_edit','编辑','Edit',NULL,NULL,'application:exchange:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1174,54,3,'application_exchange_delete','删除','Delete',NULL,NULL,'application:exchange:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1176,55,3,'application_business_add','新增','Add',NULL,NULL,'application:business:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1177,55,3,'application_business_edit','编辑','Edit',NULL,NULL,'application:business:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1178,55,3,'application_business_delete','删除','Delete',NULL,NULL,'application:business:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1180,61,3,'approval_pending_approve','审批','Approve',NULL,NULL,'approval:pending:approve',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1181,61,3,'approval_pending_reject','驳回','Reject',NULL,NULL,'approval:pending:reject',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1184,63,3,'approval_flow_add','新增','Add',NULL,NULL,'approval:flow:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1185,63,3,'approval_flow_edit','编辑','Edit',NULL,NULL,'approval:flow:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1186,63,3,'approval_flow_delete','删除','Delete',NULL,NULL,'approval:flow:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1188,71,3,'report_employee_export','导出','Export',NULL,NULL,'report:employee:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1190,72,3,'report_attendance_export','导出','Export',NULL,NULL,'report:attendance:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL),(1191,4,2,'attendance_clock','打卡记录','Clock Record','/attendance/clock','view.attendance_clock',NULL,NULL,6,1,1,'2025-12-29 09:55:42','2026-01-12 20:42:52',NULL,NULL),(1193,1191,3,NULL,'新增','Add',NULL,NULL,'attendance:clock:add',NULL,2,1,1,'2025-12-29 10:01:52','2026-01-12 20:43:01',NULL,NULL),(1194,1191,3,NULL,'删除','Delete',NULL,NULL,'attendance:clock:delete',NULL,3,1,1,'2025-12-29 10:01:52','2026-01-12 20:43:01',NULL,NULL),(1195,3,2,'application_regularization','转正申请','Regularization','/application/regularization','view.application_regularization','','mdi:account-check',6,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1),(1196,3,2,'application_transfer','调动申请','Transfer','/application/transfer','view.application_transfer','','mdi:swap-horizontal',7,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1),(1197,3,2,'application_reward','奖惩申请','Reward & Punishment','/application/reward','view.application_reward','','mdi:medal',8,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1),(1198,3,2,'application_resignation','离职申请','Resignation','/application/resignation','view.application_resignation','','mdi:account-remove',9,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1),(1202,1195,3,NULL,'新增','Add',NULL,NULL,'application:regularization:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1203,1195,3,NULL,'编辑','Edit',NULL,NULL,'application:regularization:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1204,1195,3,NULL,'删除','Delete',NULL,NULL,'application:regularization:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1205,1196,3,NULL,'新增','Add',NULL,NULL,'application:transfer:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1206,1196,3,NULL,'编辑','Edit',NULL,NULL,'application:transfer:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1207,1196,3,NULL,'删除','Delete',NULL,NULL,'application:transfer:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1208,1197,3,NULL,'新增','Add',NULL,NULL,'application:reward:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1209,1197,3,NULL,'编辑','Edit',NULL,NULL,'application:reward:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1210,1197,3,NULL,'删除','Delete',NULL,NULL,'application:reward:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1211,1198,3,NULL,'新增','Add',NULL,NULL,'application:resignation:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1212,1198,3,NULL,'编辑','Edit',NULL,NULL,'application:resignation:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1213,1198,3,NULL,'删除','Delete',NULL,NULL,'application:resignation:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL),(1214,24,3,NULL,'新增','Add',NULL,NULL,'org:unit:add',NULL,1,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL),(1215,24,3,NULL,'编辑','Edit',NULL,NULL,'org:unit:edit',NULL,2,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL),(1216,24,3,NULL,'删除','Delete',NULL,NULL,'org:unit:delete',NULL,3,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL),(1217,1245,2,'system_mobile-menu','移动端菜单','Mobile Menu','/system/mobile-menu','view.system_mobile-menu','system:mobile-menu:list','mdi:cellphone-text',1,1,1,'2026-01-12 20:37:46','2026-01-12 20:37:46',NULL,NULL),(1222,1217,3,'system_mobile-menu_list','查询','List',NULL,NULL,'system:mobile-menu:list',NULL,1,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL),(1223,1217,3,'system_mobile-menu_add','新增移动菜单','新增移动菜单',NULL,NULL,'system:mobile-menu:add',NULL,1,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL),(1224,1217,3,'system_mobile-menu_edit','编辑移动菜单','编辑移动菜单',NULL,NULL,'system:mobile-menu:edit',NULL,2,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL),(1225,1217,3,'system_mobile-menu_delete','删除移动菜单','删除移动菜单',NULL,NULL,'system:mobile-menu:delete',NULL,3,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL),(1226,8,2,'system_file-config','路径管理','File Config','/system/file-config','view.system_file-config','system:fileConfig:list','mdi:folder-cog-outline',60,1,1,'2026-03-09 10:00:37','2026-03-09 10:00:37',NULL,NULL),(1227,1245,2,'approval_mobile-approver','移动端审批权限','Mobile Approval Permission','/approval/mobile-approver','view.approval_mobile-approver','','mdi:cellphone-check',4,1,1,'2026-03-11 17:24:20','2026-05-14 13:57:55',NULL,1),(1230,85,3,'system_notice_add','新增','Add',NULL,NULL,'system:notice:add',NULL,1,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL),(1231,85,3,'system_notice_edit','编辑','Edit',NULL,NULL,'system:notice:edit',NULL,2,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL),(1232,85,3,'system_notice_delete','删除','Delete',NULL,NULL,'system:notice:delete',NULL,3,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL),(1240,4,2,'attendance_location','打卡地点','Clock Locations','/attendance/location','view.attendance_location',NULL,'mdi:map-marker-radius',3,1,1,'2026-05-07 13:59:11','2026-05-07 13:59:52',NULL,NULL),(1241,1246,2,'system_feedback','意见反馈','Feedback','/system/feedback','view.system_feedback','system:feedback:list','mdi:message-alert-outline',2,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL),(1242,1241,3,'system_feedback_reply','回复反馈','回复反馈',NULL,NULL,'system:feedback:reply',NULL,1,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL),(1243,1241,3,'system_feedback_delete','删除反馈','删除反馈',NULL,NULL,'system:feedback:delete',NULL,2,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL),(1245,0,1,'mobile','移动管理','Mobile Management','/mobile','layout.base',NULL,'mdi:cellphone-cog',10,1,1,'2026-05-14 13:57:12','2026-05-14 13:57:12',NULL,NULL),(1246,0,1,'notification','通知管理','Notification Management','/notification','layout.base',NULL,'mdi:bell-cog',9,1,1,'2026-05-14 14:03:42','2026-05-14 14:03:42',NULL,NULL),(1247,8,2,'system_oper-log','操作日志','Operation Log','/system/oper-log','view.system_oper-log','system:oper-log:list','mdi:text-box-outline',9,1,1,'2026-09-20 13:41:25','2026-09-20 13:41:25',NULL,NULL),(1248,1247,3,'system_oper-log_clean','清理日志','Clean Oper Logs',NULL,NULL,'system:oper-log:clean',NULL,1,1,1,'2026-09-20 13:41:25','2026-09-20 13:41:25',NULL,NULL),(1249,44,3,'attendance_daily_edit','保存日考勤','保存日考勤',NULL,NULL,'attendance:daily:edit',NULL,1,1,1,'2026-09-20 16:41:55','2026-09-20 16:41:55',NULL,NULL),(1250,44,3,'attendance_daily_calculate','重算日考勤','重算日考勤',NULL,NULL,'attendance:daily:calculate',NULL,2,1,1,'2026-09-20 16:41:55','2026-09-20 16:41:55',NULL,NULL),(1251,44,3,'attendance_daily_lock','锁定日考勤','锁定日考勤',NULL,NULL,'attendance:daily:lock',NULL,3,1,1,'2026-09-20 16:41:55','2026-09-20 16:41:55',NULL,NULL),(1252,1240,3,'attendance_location_add','新增打卡点','新增打卡点',NULL,NULL,'attendance:location:add',NULL,1,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1253,1240,3,'attendance_location_edit','编辑打卡点','编辑打卡点',NULL,NULL,'attendance:location:edit',NULL,2,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1254,1240,3,'attendance_location_delete','删除打卡点','删除打卡点',NULL,NULL,'attendance:location:delete',NULL,3,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1255,24,3,'organization_company_add','新增公司','新增公司',NULL,NULL,'org:company:add',NULL,1,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1256,24,3,'organization_company_edit','编辑公司','编辑公司',NULL,NULL,'org:company:edit',NULL,2,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1257,24,3,'organization_company_delete','删除公司','删除公司',NULL,NULL,'org:company:delete',NULL,3,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1258,24,3,'organization_department_add','新增部门','新增部门',NULL,NULL,'org:department:add',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1259,24,3,'organization_department_edit','编辑部门','编辑部门',NULL,NULL,'org:department:edit',NULL,5,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1260,24,3,'organization_department_delete','删除部门','删除部门',NULL,NULL,'org:department:delete',NULL,6,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1261,32,3,'hr_contract_export','导出台账','导出台账',NULL,NULL,'hr:contract:export',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1262,31,3,'hr_employee_import','导入','导入',NULL,NULL,'hr:employee:import',NULL,5,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1263,1195,3,'application_regularization_approve','审批','审批',NULL,NULL,'application:regularization:approve',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1264,1198,3,'application_resignation_approve','审批','审批',NULL,NULL,'application:resignation:approve',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1265,1197,3,'application_reward_approve','审批','审批',NULL,NULL,'application:reward:approve',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1266,1196,3,'application_transfer_approve','审批','审批',NULL,NULL,'application:transfer:approve',NULL,4,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1267,1226,3,'system_file-config_edit','编辑配置','编辑配置',NULL,NULL,'system:file-config:edit',NULL,1,1,1,'2026-09-20 16:41:56','2026-09-20 16:41:56',NULL,NULL),(1268,3,2,'hr_leave_quota','假期额度','Leave Quota','/hr/leave-quota','view.hr_leave-quota','hr:leavequota:list','mdi:calendar-clock',3,1,1,'2026-09-24 16:10:17','2026-09-24 16:10:17',NULL,NULL),(1269,1268,3,'hr_leavequota_manage','额度维护','Manage Quota',NULL,NULL,'hr:leavequota:manage',NULL,1,1,1,'2026-09-24 16:10:17','2026-09-24 16:10:17',NULL,NULL),(1270,0,1,'salary','薪资管理','Salary Management','/salary','layout.base',NULL,'mdi:cash-multiple',10,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1271,1270,2,'salary_item','薪资项定义','Salary Items','/salary/item','view.salary_item','sal:item:list','mdi:ticket-confirmation',1,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1272,1270,2,'salary_scheme','薪资方案','Salary Schemes','/salary/scheme','view.salary_scheme','sal:scheme:list','mdi:clipboard-list-outline',2,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1273,1270,2,'salary_archive','薪资档案','Salary Archives','/salary/archive','view.salary_archive','sal:archive:list','mdi:account-cash',3,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1274,1270,2,'salary_payroll','工资核算','Payroll','/salary/payroll','view.salary_payroll','sal:payroll:list','mdi:calculator-variant',4,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1275,1271,3,'salary_item_manage','维护薪资项','维护薪资项',NULL,NULL,'sal:item:manage',NULL,1,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1276,1272,3,'salary_scheme_manage','维护方案','维护方案',NULL,NULL,'sal:scheme:manage',NULL,1,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1277,1273,3,'salary_archive_manage','维护档案','维护档案',NULL,NULL,'sal:archive:manage',NULL,1,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1278,1274,3,'salary_payroll_manage','核算/确认/发放','核算/确认/发放',NULL,NULL,'sal:payroll:manage',NULL,1,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL),(1279,1274,3,'salary_payroll_export','导出工资表','导出工资表',NULL,NULL,'sal:payroll:export',NULL,2,1,1,'2026-09-24 16:18:01','2026-09-24 16:18:01',NULL,NULL);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
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
-- Table structure for table `sys_mobile_menu`
--

DROP TABLE IF EXISTS `sys_mobile_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_mobile_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单编码',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `icon_bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '#2d8cf0' COMMENT '图标背景色',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由路径',
  `menu_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'apply' COMMENT '菜单分组：quick-快捷功能，apply-申请中心',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_menu_code` (`menu_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_mobile_menu`
--

LOCK TABLES `sys_mobile_menu` WRITE;
/*!40000 ALTER TABLE `sys_mobile_menu` DISABLE KEYS */;
INSERT INTO `sys_mobile_menu` VALUES (1,'请假申请','leave','calendar','#5cadff','/pages/apply/leave/index','apply',1,1,NULL,'2026-01-12 20:35:13',1,'2026-01-12 20:53:21'),(2,'加班申请','overtime','checkbox','#19be6b','/pages/apply/overtime/index','apply',2,1,NULL,'2026-01-12 20:35:13',1,'2026-01-12 20:53:21'),(3,'补卡申请','card','checkbox','#ff9900','/pages/apply/card/index','apply',3,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:53:21'),(4,'出差申请','travel','location','#ed4014','/pages/apply/travel/index','apply',4,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:53:21'),(7,'离职申请','resign','closeempty','#f5222d','/pages/apply/resign/index','apply',3,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:35:13'),(8,'换休申请','exchange','refreshempty','#fa8c16','/pages/apply/exchange/index','apply',4,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:35:13');
/*!40000 ALTER TABLE `sys_mobile_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` tinyint NOT NULL DEFAULT '1' COMMENT '公告类型：1-公告，2-通知',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '公告内容',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-关闭，1-正常',
  `publish_time` datetime DEFAULT NULL COMMENT '发布日期',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='公告通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'111',2,'222',1,'2026-03-01 00:00:00','2026-03-12 10:40:13','2026-03-12 10:46:42',1,1),(2,'系统公告',1,'欢迎使用人资OA移动端。\n\n目前已上线登录、工作台、考勤打卡、请假/加班/补卡/离职/出差/换休申请、待办审批、通知公告、个人信息和意见反馈等功能。\n\n如遇到登录、定位、审批或资料显示问题，请先在“我的 - 意见反馈”提交问题，管理员会在后台系统管理中查看并处理。\n',1,'2026-05-13 00:00:00','2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL);
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice_read`
--

DROP TABLE IF EXISTS `sys_notice_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_read` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notice_id` bigint NOT NULL COMMENT '公告ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_notice_read` (`notice_id`,`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告已读记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice_read`
--

LOCK TABLES `sys_notice_read` WRITE;
/*!40000 ALTER TABLE `sys_notice_read` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice_read` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notification`
--

DROP TABLE IF EXISTS `sys_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `employee_id` bigint NOT NULL COMMENT '接收员工ID',
  `type` varchar(32) NOT NULL COMMENT '通知类型：approval_result-审批结果，approval_todo-待审批，reminder-到期提醒',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `url` varchar(200) DEFAULT NULL COMMENT '移动端跳转路径',
  `read_flag` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_sys_notification_employee` (`employee_id`,`read_flag`),
  KEY `idx_sys_notification_ref` (`type`,`ref_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站内通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notification`
--

LOCK TABLES `sys_notification` WRITE;
/*!40000 ALTER TABLE `sys_notification` DISABLE KEYS */;
INSERT INTO `sys_notification` VALUES (1,1,'salary','工资条发放通知','2026-08 月工资条已发放，实发 7300.00 元，请查看详情',9,'/minePages/payslip',0,NULL,'2026-09-24 16:43:43','2026-09-24 16:43:43',1,1),(2,5,'salary','工资条发放通知','2026-08 月工资条已发放，实发 4220.00 元，请查看详情',10,'/minePages/payslip',0,NULL,'2026-09-24 16:43:43','2026-09-24 16:43:43',1,1),(3,6,'salary','工资条发放通知','2026-08 月工资条已发放，实发 0.00 元，请查看详情',11,'/minePages/payslip',0,NULL,'2026-09-24 16:43:43','2026-09-24 16:43:43',1,1),(4,7,'salary','工资条发放通知','2026-08 月工资条已发放，实发 0.00 元，请查看详情',12,'/minePages/payslip',0,NULL,'2026-09-24 16:43:43','2026-09-24 16:43:43',1,1),(5,1,'salary','工资条发放通知','2026-08 月工资条已发放，实发 7300.00 元，请查看详情',13,'/minePages/payslip',0,NULL,'2026-09-24 16:46:45','2026-09-24 16:46:45',1,1),(6,5,'salary','工资条发放通知','2026-08 月工资条已发放，实发 4220.00 元，请查看详情',14,'/minePages/payslip',0,NULL,'2026-09-24 16:46:45','2026-09-24 16:46:45',1,1),(7,6,'salary','工资条发放通知','2026-08 月工资条已发放，实发 0.00 元，请查看详情',15,'/minePages/payslip',0,NULL,'2026-09-24 16:46:45','2026-09-24 16:46:45',1,1),(8,7,'salary','工资条发放通知','2026-08 月工资条已发放，实发 0.00 元，请查看详情',16,'/minePages/payslip',0,NULL,'2026-09-24 16:46:45','2026-09-24 16:46:45',1,1);
/*!40000 ALTER TABLE `sys_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_oper_log`
--

DROP TABLE IF EXISTS `sys_oper_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型',
  `request_uri` varchar(255) DEFAULT NULL COMMENT '请求方式+路径',
  `user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作人用户名',
  `ip` varchar(64) DEFAULT NULL COMMENT '操作人IP',
  `request_params` text COMMENT '请求参数（脱敏截断）',
  `status` tinyint DEFAULT NULL COMMENT '操作结果：0-失败，1-成功',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `cost_ms` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_oper_log_user` (`user_id`),
  KEY `idx_sys_oper_log_created` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_oper_log`
--

LOCK TABLES `sys_oper_log` WRITE;
/*!40000 ALTER TABLE `sys_oper_log` DISABLE KEYS */;
INSERT INTO `sys_oper_log` VALUES (1,'申请审批','审批','POST /api/hr/application/approve/1',1,'001','0:0:0:0:0:0:0:1','[1,1,null]',0,'该申请已处理，无需重复审批',7,'2026-09-20 16:44:02'),(2,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:19:23'),(3,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:23:38'),(4,'系统登录','登录失败','POST /api/auth/login',NULL,NULL,'0:0:0:0:0:0:0:1',NULL,0,'用户名或密码错误',NULL,'2026-09-24 16:30:28'),(5,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:31:17'),(6,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":11,\"remark\":\"\"}]',1,NULL,136,'2026-09-24 16:33:40'),(7,'系统登录','登录失败','POST /api/auth/login',NULL,NULL,'0:0:0:0:0:0:0:1',NULL,0,'用户名或密码错误',NULL,'2026-09-24 16:43:00'),(8,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:43:01'),(9,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10,\"remark\":\"E2E自测\"}]',1,NULL,104,'2026-09-24 16:43:01'),(10,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:43:17'),(11,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10,\"remark\":\"E2E自测\"}]',0,'该月该公司已存在工资批次，请直接重算',4,'2026-09-24 16:43:17'),(12,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:43:42'),(13,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10,\"remark\":\"E2E自测\"}]',0,'该月该公司已存在工资批次，请直接重算',1,'2026-09-24 16:43:42'),(14,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10}]',0,'该月该公司已存在工资批次，请直接重算',2,'2026-09-24 16:43:42'),(15,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',1,NULL,8,'2026-09-24 16:43:42'),(16,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已核算状态的批次可以确认',2,'2026-09-24 16:43:42'),(17,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',2,'2026-09-24 16:43:42'),(18,'工资核算','删除批次','DELETE /api/salary/payroll/2',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'已确认/已发放的批次不能删除，账本数据须留痕',2,'2026-09-24 16:43:42'),(19,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',1,'2026-09-24 16:43:42'),(20,'工资核算','解锁批次','POST /api/salary/payroll/2/unlock',1,'admin','0:0:0:0:0:0:0:1','[2]',1,NULL,12,'2026-09-24 16:43:42'),(21,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',1,NULL,62,'2026-09-24 16:43:42'),(22,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',1,NULL,8,'2026-09-24 16:43:43'),(23,'工资核算','发放工资','POST /api/salary/payroll/2/publish',1,'admin','0:0:0:0:0:0:0:1','[2]',1,NULL,25,'2026-09-24 16:43:43'),(24,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:44:06'),(25,'系统登录','登录成功','POST /api/auth/login',NULL,'admin','0:0:0:0:0:0:0:1',NULL,1,NULL,NULL,'2026-09-24 16:44:41'),(26,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10,\"remark\":\"E2E自测\"}]',0,'该月该公司已存在工资批次，请直接重算',3,'2026-09-24 16:44:41'),(27,'工资核算','解锁批次','POST /api/salary/payroll/2/unlock',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已确认状态的批次可以解锁',3,'2026-09-24 16:44:41'),(28,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',1,'2026-09-24 16:44:41'),(29,'工资核算','创建批次','POST /api/salary/payroll/create',1,'admin','0:0:0:0:0:0:0:1','[{\"yearMonth\":\"2026-08\",\"companyId\":10}]',0,'该月该公司已存在工资批次，请直接重算',3,'2026-09-24 16:44:41'),(30,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已核算状态的批次可以确认',1,'2026-09-24 16:44:41'),(31,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已核算状态的批次可以确认',2,'2026-09-24 16:44:41'),(32,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',3,'2026-09-24 16:44:41'),(33,'工资核算','删除批次','DELETE /api/salary/payroll/2',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'已确认/已发放的批次不能删除，账本数据须留痕',2,'2026-09-24 16:44:41'),(34,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',2,'2026-09-24 16:44:41'),(35,'工资核算','解锁批次','POST /api/salary/payroll/2/unlock',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已确认状态的批次可以解锁',1,'2026-09-24 16:44:41'),(36,'工资核算','重算批次','POST /api/salary/payroll/2/compute',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'批次已确认，不能重算；请先解锁',3,'2026-09-24 16:44:41'),(37,'工资核算','确认批次','POST /api/salary/payroll/2/confirm',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已核算状态的批次可以确认',3,'2026-09-24 16:44:41'),(38,'工资核算','发放工资','POST /api/salary/payroll/2/publish',1,'admin','0:0:0:0:0:0:0:1','[2]',0,'仅已确认状态的批次可以发放',2,'2026-09-24 16:44:41'),(39,'工资核算','导出工资表','GET /api/salary/payroll/2/export',1,'admin','0:0:0:0:0:0:0:1','[2,\"LifecycleHttpServletResponse\"]',1,NULL,584,'2026-09-24 16:44:42'),(40,'工资核算','重算批次','POST /api/salary/payroll/1/compute',1,'admin','0:0:0:0:0:0:0:1','[1]',1,NULL,116,'2026-09-24 16:46:19'),(41,'工资核算','确认批次','POST /api/salary/payroll/1/confirm',1,'admin','0:0:0:0:0:0:0:1','[1]',1,NULL,11,'2026-09-24 16:46:39'),(42,'工资核算','发放工资','POST /api/salary/payroll/1/publish',1,'admin','0:0:0:0:0:0:0:1','[1]',1,NULL,27,'2026-09-24 16:46:45'),(43,'工资核算','导出工资表','GET /api/salary/payroll/1/export',1,'admin','0:0:0:0:0:0:0:1','[1,\"LifecycleHttpServletResponse\"]',1,NULL,34,'2026-09-24 16:46:55');
/*!40000 ALTER TABLE `sys_oper_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据权限范围：1-全部数据，2-本公司数据，3-本部门数据，4-本部门及以下数据，5-仅本人数据，6-自定义数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `role_code` (`role_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1,'ROLE_ADMIN','系统管理员','',1,1,'2025-12-22 21:19:11','2026-09-21 08:40:41',NULL,1,1),(10,'nhsys','管理员','',1,0,'2026-01-19 18:45:50','2026-03-12 15:31:33',1,8,1);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_dept` (`role_id`,`dept_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色部门关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4611 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (4167,10,1,'2026-03-12 15:31:32'),(4168,10,2,'2026-03-12 15:31:32'),(4169,10,24,'2026-03-12 15:31:32'),(4170,10,1214,'2026-03-12 15:31:32'),(4171,10,1215,'2026-03-12 15:31:32'),(4172,10,1216,'2026-03-12 15:31:32'),(4173,10,3,'2026-03-12 15:31:32'),(4174,10,31,'2026-03-12 15:31:32'),(4175,10,1086,'2026-03-12 15:31:32'),(4176,10,1087,'2026-03-12 15:31:32'),(4177,10,1088,'2026-03-12 15:31:32'),(4178,10,1089,'2026-03-12 15:31:32'),(4179,10,32,'2026-03-12 15:31:32'),(4180,10,1091,'2026-03-12 15:31:32'),(4181,10,1092,'2026-03-12 15:31:32'),(4182,10,1093,'2026-03-12 15:31:32'),(4183,10,1195,'2026-03-12 15:31:32'),(4184,10,1202,'2026-03-12 15:31:32'),(4185,10,1203,'2026-03-12 15:31:32'),(4186,10,1204,'2026-03-12 15:31:32'),(4187,10,1196,'2026-03-12 15:31:32'),(4188,10,1205,'2026-03-12 15:31:32'),(4189,10,1206,'2026-03-12 15:31:32'),(4190,10,1207,'2026-03-12 15:31:32'),(4191,10,1197,'2026-03-12 15:31:32'),(4192,10,1208,'2026-03-12 15:31:32'),(4193,10,1209,'2026-03-12 15:31:32'),(4194,10,1210,'2026-03-12 15:31:32'),(4195,10,1198,'2026-03-12 15:31:32'),(4196,10,1211,'2026-03-12 15:31:32'),(4197,10,1212,'2026-03-12 15:31:32'),(4198,10,1213,'2026-03-12 15:31:32'),(4199,10,6,'2026-03-12 15:31:32'),(4200,10,61,'2026-03-12 15:31:32'),(4201,10,1180,'2026-03-12 15:31:32'),(4202,10,1181,'2026-03-12 15:31:32'),(4203,10,62,'2026-03-12 15:31:32'),(4204,10,63,'2026-03-12 15:31:32'),(4205,10,1184,'2026-03-12 15:31:32'),(4206,10,1185,'2026-03-12 15:31:32'),(4207,10,1186,'2026-03-12 15:31:32'),(4208,10,1227,'2026-03-12 15:31:32'),(4209,10,81,'2026-03-12 15:31:32'),(4210,10,1127,'2026-03-12 15:31:32'),(4211,10,1128,'2026-03-12 15:31:32'),(4212,10,1129,'2026-03-12 15:31:32'),(4213,10,1130,'2026-03-12 15:31:32'),(4214,10,82,'2026-03-12 15:31:32'),(4215,10,1132,'2026-03-12 15:31:32'),(4216,10,1133,'2026-03-12 15:31:32'),(4217,10,1134,'2026-03-12 15:31:32'),(4218,10,83,'2026-03-12 15:31:32'),(4219,10,1136,'2026-03-12 15:31:32'),(4220,10,1137,'2026-03-12 15:31:32'),(4221,10,1138,'2026-03-12 15:31:32'),(4222,10,84,'2026-03-12 15:31:32'),(4223,10,1140,'2026-03-12 15:31:32'),(4224,10,1141,'2026-03-12 15:31:32'),(4225,10,1142,'2026-03-12 15:31:32'),(4226,10,8,'2026-03-12 15:31:32'),(4228,10,1240,'2026-05-07 14:07:42'),(4230,10,1241,'2026-05-13 17:00:35'),(4232,10,1245,'2026-05-14 13:57:12'),(4234,10,1217,'2026-05-14 13:57:12'),(4235,10,1223,'2026-05-14 13:57:12'),(4236,10,1224,'2026-05-14 13:57:12'),(4237,10,1225,'2026-05-14 13:57:12'),(4239,10,1246,'2026-05-14 14:03:42'),(4241,10,85,'2026-05-14 14:03:42'),(4243,10,1247,'2026-09-20 13:41:24'),(4246,10,1248,'2026-09-20 13:41:24'),(4249,10,1249,'2026-09-20 16:41:55'),(4252,10,1250,'2026-09-20 16:41:55'),(4255,10,1251,'2026-09-20 16:41:55'),(4258,10,1252,'2026-09-20 16:41:55'),(4261,10,1253,'2026-09-20 16:41:55'),(4264,10,1254,'2026-09-20 16:41:55'),(4267,10,1255,'2026-09-20 16:41:55'),(4270,10,1256,'2026-09-20 16:41:55'),(4273,10,1257,'2026-09-20 16:41:55'),(4276,10,1258,'2026-09-20 16:41:55'),(4279,10,1259,'2026-09-20 16:41:55'),(4282,10,1260,'2026-09-20 16:41:55'),(4285,10,1261,'2026-09-20 16:41:55'),(4288,10,1262,'2026-09-20 16:41:55'),(4291,10,1263,'2026-09-20 16:41:55'),(4294,10,1264,'2026-09-20 16:41:55'),(4297,10,1265,'2026-09-20 16:41:55'),(4300,10,1266,'2026-09-20 16:41:55'),(4303,10,1267,'2026-09-20 16:41:55'),(4448,1,1,'2026-09-21 08:40:41'),(4449,1,2,'2026-09-21 08:40:41'),(4450,1,24,'2026-09-21 08:40:41'),(4451,1,1214,'2026-09-21 08:40:41'),(4452,1,1255,'2026-09-21 08:40:41'),(4453,1,1215,'2026-09-21 08:40:41'),(4454,1,1256,'2026-09-21 08:40:41'),(4455,1,1216,'2026-09-21 08:40:41'),(4456,1,1257,'2026-09-21 08:40:41'),(4457,1,1258,'2026-09-21 08:40:41'),(4458,1,1259,'2026-09-21 08:40:41'),(4459,1,1260,'2026-09-21 08:40:41'),(4460,1,3,'2026-09-21 08:40:41'),(4461,1,31,'2026-09-21 08:40:41'),(4462,1,1086,'2026-09-21 08:40:41'),(4463,1,1087,'2026-09-21 08:40:41'),(4464,1,1088,'2026-09-21 08:40:41'),(4465,1,1089,'2026-09-21 08:40:41'),(4466,1,1262,'2026-09-21 08:40:41'),(4467,1,32,'2026-09-21 08:40:41'),(4468,1,1091,'2026-09-21 08:40:41'),(4469,1,1092,'2026-09-21 08:40:41'),(4470,1,1093,'2026-09-21 08:40:41'),(4471,1,1261,'2026-09-21 08:40:41'),(4472,1,1195,'2026-09-21 08:40:41'),(4473,1,1202,'2026-09-21 08:40:41'),(4474,1,1203,'2026-09-21 08:40:41'),(4475,1,1204,'2026-09-21 08:40:41'),(4476,1,1263,'2026-09-21 08:40:41'),(4477,1,1196,'2026-09-21 08:40:41'),(4478,1,1205,'2026-09-21 08:40:41'),(4479,1,1206,'2026-09-21 08:40:41'),(4480,1,1207,'2026-09-21 08:40:41'),(4481,1,1266,'2026-09-21 08:40:41'),(4482,1,1197,'2026-09-21 08:40:41'),(4483,1,1208,'2026-09-21 08:40:41'),(4484,1,1209,'2026-09-21 08:40:41'),(4485,1,1210,'2026-09-21 08:40:41'),(4486,1,1265,'2026-09-21 08:40:41'),(4487,1,1198,'2026-09-21 08:40:41'),(4488,1,1211,'2026-09-21 08:40:41'),(4489,1,1212,'2026-09-21 08:40:41'),(4490,1,1213,'2026-09-21 08:40:41'),(4491,1,1264,'2026-09-21 08:40:41'),(4492,1,4,'2026-09-21 08:40:41'),(4493,1,41,'2026-09-21 08:40:41'),(4494,1,1144,'2026-09-21 08:40:41'),(4495,1,1145,'2026-09-21 08:40:41'),(4496,1,1146,'2026-09-21 08:40:41'),(4497,1,42,'2026-09-21 08:40:41'),(4498,1,1148,'2026-09-21 08:40:41'),(4499,1,1149,'2026-09-21 08:40:41'),(4500,1,1150,'2026-09-21 08:40:41'),(4501,1,43,'2026-09-21 08:40:41'),(4502,1,1152,'2026-09-21 08:40:41'),(4503,1,1153,'2026-09-21 08:40:41'),(4504,1,1154,'2026-09-21 08:40:41'),(4505,1,1240,'2026-09-21 08:40:41'),(4506,1,1252,'2026-09-21 08:40:41'),(4507,1,1253,'2026-09-21 08:40:41'),(4508,1,1254,'2026-09-21 08:40:41'),(4509,1,44,'2026-09-21 08:40:41'),(4510,1,1156,'2026-09-21 08:40:41'),(4511,1,1249,'2026-09-21 08:40:41'),(4512,1,1250,'2026-09-21 08:40:41'),(4513,1,1251,'2026-09-21 08:40:41'),(4514,1,45,'2026-09-21 08:40:41'),(4515,1,1158,'2026-09-21 08:40:41'),(4516,1,1191,'2026-09-21 08:40:41'),(4517,1,1193,'2026-09-21 08:40:41'),(4518,1,1194,'2026-09-21 08:40:41'),(4519,1,5,'2026-09-21 08:40:41'),(4520,1,51,'2026-09-21 08:40:41'),(4521,1,1160,'2026-09-21 08:40:41'),(4522,1,1161,'2026-09-21 08:40:41'),(4523,1,1162,'2026-09-21 08:40:41'),(4524,1,52,'2026-09-21 08:40:41'),(4525,1,1164,'2026-09-21 08:40:41'),(4526,1,1165,'2026-09-21 08:40:41'),(4527,1,1166,'2026-09-21 08:40:41'),(4528,1,53,'2026-09-21 08:40:41'),(4529,1,1168,'2026-09-21 08:40:41'),(4530,1,1169,'2026-09-21 08:40:41'),(4531,1,1170,'2026-09-21 08:40:41'),(4532,1,54,'2026-09-21 08:40:41'),(4533,1,1172,'2026-09-21 08:40:41'),(4534,1,1173,'2026-09-21 08:40:41'),(4535,1,1174,'2026-09-21 08:40:41'),(4536,1,55,'2026-09-21 08:40:41'),(4537,1,1176,'2026-09-21 08:40:41'),(4538,1,1177,'2026-09-21 08:40:41'),(4539,1,1178,'2026-09-21 08:40:41'),(4540,1,6,'2026-09-21 08:40:41'),(4541,1,61,'2026-09-21 08:40:41'),(4542,1,1180,'2026-09-21 08:40:41'),(4543,1,1181,'2026-09-21 08:40:41'),(4544,1,62,'2026-09-21 08:40:41'),(4545,1,63,'2026-09-21 08:40:41'),(4546,1,1184,'2026-09-21 08:40:41'),(4547,1,1185,'2026-09-21 08:40:41'),(4548,1,1186,'2026-09-21 08:40:41'),(4549,1,7,'2026-09-21 08:40:41'),(4550,1,71,'2026-09-21 08:40:41'),(4551,1,1188,'2026-09-21 08:40:41'),(4552,1,72,'2026-09-21 08:40:41'),(4553,1,1190,'2026-09-21 08:40:41'),(4554,1,8,'2026-09-21 08:40:41'),(4555,1,81,'2026-09-21 08:40:41'),(4556,1,1127,'2026-09-21 08:40:41'),(4557,1,1128,'2026-09-21 08:40:41'),(4558,1,1129,'2026-09-21 08:40:41'),(4559,1,1130,'2026-09-21 08:40:41'),(4560,1,82,'2026-09-21 08:40:41'),(4561,1,1132,'2026-09-21 08:40:41'),(4562,1,1133,'2026-09-21 08:40:41'),(4563,1,1134,'2026-09-21 08:40:41'),(4564,1,83,'2026-09-21 08:40:41'),(4565,1,1136,'2026-09-21 08:40:41'),(4566,1,1137,'2026-09-21 08:40:41'),(4567,1,1138,'2026-09-21 08:40:41'),(4568,1,84,'2026-09-21 08:40:41'),(4569,1,1140,'2026-09-21 08:40:41'),(4570,1,1141,'2026-09-21 08:40:41'),(4571,1,1142,'2026-09-21 08:40:41'),(4572,1,1247,'2026-09-21 08:40:41'),(4573,1,1248,'2026-09-21 08:40:41'),(4574,1,1226,'2026-09-21 08:40:41'),(4575,1,1267,'2026-09-21 08:40:41'),(4576,1,1246,'2026-09-21 08:40:41'),(4577,1,85,'2026-09-21 08:40:41'),(4578,1,1230,'2026-09-21 08:40:41'),(4579,1,1231,'2026-09-21 08:40:41'),(4580,1,1232,'2026-09-21 08:40:41'),(4581,1,1241,'2026-09-21 08:40:41'),(4582,1,1242,'2026-09-21 08:40:41'),(4583,1,1243,'2026-09-21 08:40:41'),(4584,1,1245,'2026-09-21 08:40:41'),(4585,1,1217,'2026-09-21 08:40:41'),(4586,1,1222,'2026-09-21 08:40:41'),(4587,1,1223,'2026-09-21 08:40:41'),(4588,1,1224,'2026-09-21 08:40:41'),(4589,1,1225,'2026-09-21 08:40:41'),(4590,1,1227,'2026-09-21 08:40:41'),(4591,1,1268,'2026-09-24 16:10:16'),(4592,10,1268,'2026-09-24 16:10:16'),(4594,1,1269,'2026-09-24 16:10:16'),(4595,10,1269,'2026-09-24 16:10:16'),(4597,1,1270,'2026-09-24 16:18:01'),(4598,10,1270,'2026-09-24 16:18:01'),(4600,1,1271,'2026-09-24 16:18:01'),(4601,10,1271,'2026-09-24 16:18:01'),(4603,1,1272,'2026-09-24 16:18:01'),(4604,10,1272,'2026-09-24 16:18:01'),(4606,1,1273,'2026-09-24 16:18:01'),(4607,10,1273,'2026-09-24 16:18:01'),(4609,1,1274,'2026-09-24 16:18:01'),(4610,10,1274,'2026-09-24 16:18:01');
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `employee_id` bigint DEFAULT NULL COMMENT '关联员工ID',
  `company_id` bigint DEFAULT NULL COMMENT '公司ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$O.gQasdj6qcXGwDqDOKZOOet.DzedNhQAP78Ku/GiOtPQfNsLLepe','系统管理员',NULL,NULL,NULL,0,1,NULL,NULL,NULL,'2025-12-22 21:19:11','2026-01-24 22:22:44',NULL,1),(8,'nhsys','$2a$10$EtjBMrV6a0.qQ3Axhho6YeShIA0PCNDjm/wfL.AqaFKh60NUfStIi','管理员','','',NULL,0,1,NULL,NULL,NULL,'2026-01-19 18:46:03','2026-03-12 15:31:45',1,8);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户角色关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (17,1,1,'2026-01-12 20:47:03'),(24,8,10,'2026-03-12 15:31:44');
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_approval_record`
--

DROP TABLE IF EXISTS `wf_approval_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `action` tinyint DEFAULT NULL COMMENT '操作：1-通过，2-拒绝，3-转交',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '审批意见',
  `transfer_to` bigint DEFAULT NULL COMMENT '转交给',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_instance_id` (`instance_id`) USING BTREE,
  KEY `idx_approver_id` (`approver_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='审批记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_approval_record`
--

LOCK TABLES `wf_approval_record` WRITE;
/*!40000 ALTER TABLE `wf_approval_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_approval_record` ENABLE KEYS */;
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
-- Table structure for table `wf_process_instance`
--

DROP TABLE IF EXISTS `wf_process_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_process_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `process_id` bigint NOT NULL COMMENT '流程定义ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '标题',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `current_node_id` bigint DEFAULT NULL COMMENT '当前节点ID',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-进行中，1-已通过，2-已拒绝，3-已撤销',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_business` (`business_type`,`business_id`) USING BTREE,
  KEY `idx_applicant` (`applicant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='流程实例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_process_instance`
--

LOCK TABLES `wf_process_instance` WRITE;
/*!40000 ALTER TABLE `wf_process_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_process_instance` ENABLE KEYS */;
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

--
-- Dumping events for database 'kadmin'
--

--
-- Dumping routines for database 'kadmin'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-06  9:18:34
