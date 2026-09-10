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
INSERT INTO `att_location` VALUES (1,'华水工业园','中国江苏省苏州市昆山市红杨路1100号 邮政编码: 215316',31.435435,120.920273,500,1,'由公司打卡配置迁移','2026-05-07 13:59:11','2026-05-07 13:59:11',NULL,1);
INSERT INTO `att_location` VALUES (2,'测试2','中国北京市东城区南菜园中国国家博物馆 邮政编码: 100051',39.907441,116.406830,500,1,'','2026-05-07 14:13:22','2026-05-07 14:13:22',1,1);
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
INSERT INTO `att_location_employee` VALUES (34,2,5,'2026-05-13 08:42:04','2026-05-13 08:42:04',1,1);
INSERT INTO `att_location_employee` VALUES (35,2,6,'2026-05-13 08:42:04','2026-05-13 08:42:04',1,1);
INSERT INTO `att_location_employee` VALUES (36,1,5,'2026-05-13 08:42:14','2026-05-13 08:42:14',1,1);
INSERT INTO `att_location_employee` VALUES (37,1,1,'2026-05-13 08:42:14','2026-05-13 08:42:14',1,1);
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
INSERT INTO `att_schedule` VALUES (2,1,'2025-12-22',1,0,NULL,'2025-12-28 18:03:55','2025-12-28 18:03:55',NULL,NULL);
INSERT INTO `att_schedule` VALUES (6,2,'2025-12-29',1,0,NULL,'2025-12-28 18:07:18','2025-12-28 18:07:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (11,2,'2025-12-28',1,0,NULL,'2025-12-28 20:58:39','2025-12-28 20:58:39',NULL,NULL);
INSERT INTO `att_schedule` VALUES (12,1,'2025-12-23',1,0,NULL,'2025-12-28 21:01:55','2025-12-28 21:01:55',NULL,NULL);
INSERT INTO `att_schedule` VALUES (13,2,'2025-12-23',1,0,NULL,'2025-12-28 21:01:56','2025-12-28 21:01:56',NULL,NULL);
INSERT INTO `att_schedule` VALUES (14,1,'2025-12-24',2,0,NULL,'2025-12-28 21:01:58','2025-12-28 21:01:58',NULL,NULL);
INSERT INTO `att_schedule` VALUES (16,1,'2025-12-08',1,0,NULL,'2025-12-28 21:02:43','2025-12-28 21:02:43',NULL,NULL);
INSERT INTO `att_schedule` VALUES (17,1,'2025-12-09',1,0,NULL,'2025-12-28 21:02:44','2025-12-28 21:02:44',NULL,NULL);
INSERT INTO `att_schedule` VALUES (22,1,'2025-12-06',1,0,NULL,'2025-12-28 21:03:28','2025-12-28 21:03:28',NULL,NULL);
INSERT INTO `att_schedule` VALUES (23,1,'2025-12-10',1,0,NULL,'2025-12-28 21:04:03','2025-12-28 21:04:03',NULL,NULL);
INSERT INTO `att_schedule` VALUES (26,1,'2025-12-11',1,0,NULL,'2025-12-28 21:20:49','2025-12-28 21:20:49',NULL,NULL);
INSERT INTO `att_schedule` VALUES (29,1,'2025-12-13',2,0,NULL,'2025-12-28 21:21:03','2025-12-28 21:21:03',NULL,NULL);
INSERT INTO `att_schedule` VALUES (30,1,'2025-12-12',2,0,NULL,'2025-12-28 21:21:04','2025-12-28 21:21:04',NULL,NULL);
INSERT INTO `att_schedule` VALUES (32,1,'2025-12-14',1,0,NULL,'2025-12-28 21:23:46','2025-12-28 21:23:46',NULL,NULL);
INSERT INTO `att_schedule` VALUES (44,1,'2025-12-15',1,0,NULL,'2025-12-28 21:24:10','2025-12-28 21:24:10',NULL,NULL);
INSERT INTO `att_schedule` VALUES (45,1,'2025-12-16',1,0,NULL,'2025-12-28 21:24:11','2025-12-28 21:24:11',NULL,NULL);
INSERT INTO `att_schedule` VALUES (52,1,'2025-12-17',1,0,NULL,'2025-12-30 10:25:58','2025-12-30 10:25:58',NULL,NULL);
INSERT INTO `att_schedule` VALUES (53,1,'2025-12-18',1,0,NULL,'2025-12-30 10:25:59','2025-12-30 10:25:59',NULL,NULL);
INSERT INTO `att_schedule` VALUES (54,1,'2025-12-21',2,0,NULL,'2025-12-30 10:26:03','2025-12-30 10:26:03',NULL,NULL);
INSERT INTO `att_schedule` VALUES (56,2,'2025-12-30',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL);
INSERT INTO `att_schedule` VALUES (57,2,'2025-12-31',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL);
INSERT INTO `att_schedule` VALUES (58,2,'2026-01-01',1,0,NULL,'2025-12-30 10:26:23','2025-12-30 10:26:23',NULL,NULL);
INSERT INTO `att_schedule` VALUES (62,1,'2025-12-04',1,0,NULL,'2025-12-30 10:26:39','2025-12-30 10:26:39',NULL,NULL);
INSERT INTO `att_schedule` VALUES (63,1,'2025-12-05',1,0,NULL,'2025-12-30 10:26:39','2025-12-30 10:26:39',NULL,NULL);
INSERT INTO `att_schedule` VALUES (64,1,'2025-12-01',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL);
INSERT INTO `att_schedule` VALUES (65,1,'2025-12-02',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL);
INSERT INTO `att_schedule` VALUES (66,1,'2025-12-03',2,0,NULL,'2025-12-30 10:26:50','2025-12-30 10:26:50',NULL,NULL);
INSERT INTO `att_schedule` VALUES (67,1,'2025-12-30',1,0,NULL,'2025-12-30 10:29:03','2025-12-30 10:29:03',NULL,NULL);
INSERT INTO `att_schedule` VALUES (68,1,'2025-12-29',1,0,NULL,'2025-12-30 10:29:04','2025-12-30 10:29:04',NULL,NULL);
INSERT INTO `att_schedule` VALUES (69,1,'2025-12-31',1,0,NULL,'2025-12-30 10:29:05','2025-12-30 10:29:05',NULL,NULL);
INSERT INTO `att_schedule` VALUES (179,5,'2026-01-01',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (180,5,'2026-01-02',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (181,5,'2026-01-03',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (182,5,'2026-01-04',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (183,5,'2026-01-05',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (184,5,'2026-01-06',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (185,5,'2026-01-07',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (186,5,'2026-01-08',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (187,5,'2026-01-09',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (188,5,'2026-01-10',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (189,5,'2026-01-11',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (190,5,'2026-01-12',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (191,5,'2026-01-13',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (192,5,'2026-01-14',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (210,1,'2026-01-01',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (211,1,'2026-01-02',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (212,1,'2026-01-03',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (213,1,'2026-01-04',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (214,1,'2026-01-05',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (215,1,'2026-01-06',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (216,1,'2026-01-07',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (217,1,'2026-01-08',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (218,1,'2026-01-09',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (219,1,'2026-01-10',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (220,1,'2026-01-11',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (221,1,'2026-01-12',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (222,1,'2026-01-13',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (223,1,'2026-01-14',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (224,1,'2026-01-15',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (225,1,'2026-01-16',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (226,1,'2026-01-17',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (227,1,'2026-01-18',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (228,1,'2026-01-19',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (229,1,'2026-01-20',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (230,1,'2026-01-21',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (231,1,'2026-01-22',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (232,1,'2026-01-23',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (233,1,'2026-01-24',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (234,1,'2026-01-25',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (235,1,'2026-01-26',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (236,1,'2026-01-27',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (237,1,'2026-01-28',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (238,1,'2026-01-29',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (239,1,'2026-01-30',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (240,1,'2026-01-31',1,0,NULL,'2026-01-14 17:17:38','2026-01-14 17:17:38',NULL,NULL);
INSERT INTO `att_schedule` VALUES (241,5,'2026-01-15',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (242,5,'2026-01-16',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (243,5,'2026-01-17',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (244,5,'2026-01-18',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (245,5,'2026-01-19',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (246,5,'2026-01-20',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (247,5,'2026-01-21',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (248,5,'2026-01-22',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (249,5,'2026-01-23',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (250,5,'2026-01-24',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (251,5,'2026-01-25',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (252,5,'2026-01-26',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (253,5,'2026-01-27',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (254,5,'2026-01-28',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (255,5,'2026-01-29',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (256,5,'2026-01-30',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (257,5,'2026-01-31',2,0,NULL,'2026-01-14 17:17:57','2026-01-14 17:17:57',NULL,NULL);
INSERT INTO `att_schedule` VALUES (261,1,'2026-03-14',2,0,NULL,'2026-03-10 09:21:02','2026-03-10 09:21:41',NULL,NULL);
INSERT INTO `att_schedule` VALUES (262,1,'2026-03-09',1,0,NULL,'2026-03-10 09:21:13','2026-03-10 09:21:41',NULL,NULL);
INSERT INTO `att_schedule` VALUES (263,1,'2026-03-12',1,0,NULL,'2026-03-13 11:31:45','2026-03-13 11:31:45',NULL,NULL);
INSERT INTO `att_schedule` VALUES (264,1,'2026-03-13',NULL,0,NULL,'2026-03-13 11:31:45','2026-03-13 11:46:52',NULL,NULL);
INSERT INTO `att_schedule` VALUES (265,1,'2026-03-11',1,0,NULL,'2026-03-13 11:31:46','2026-03-13 11:31:46',NULL,NULL);
INSERT INTO `att_schedule` VALUES (266,1,'2026-03-10',1,0,NULL,'2026-03-13 11:31:47','2026-03-13 11:31:47',NULL,NULL);
INSERT INTO `att_schedule` VALUES (267,1,'2026-03-16',1,0,NULL,'2026-03-13 11:46:52','2026-03-13 11:46:52',NULL,NULL);
INSERT INTO `att_schedule` VALUES (269,1,'2026-03-18',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (270,1,'2026-03-19',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (271,1,'2026-03-20',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (272,1,'2026-03-21',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (273,1,'2026-03-22',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (274,1,'2026-03-23',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (275,1,'2026-03-24',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (276,1,'2026-03-25',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (277,1,'2026-03-26',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (278,1,'2026-03-27',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (279,1,'2026-03-28',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (280,1,'2026-03-29',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (281,1,'2026-03-30',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (282,1,'2026-03-31',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (283,1,'2026-04-01',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (284,1,'2026-04-02',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (285,1,'2026-04-03',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (286,1,'2026-04-04',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (287,1,'2026-04-05',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (288,1,'2026-04-06',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (289,1,'2026-04-07',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (290,1,'2026-04-08',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (291,1,'2026-04-09',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (292,1,'2026-04-10',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (293,1,'2026-04-11',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (294,1,'2026-04-12',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (295,1,'2026-04-13',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (296,1,'2026-04-14',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (297,1,'2026-04-15',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (298,1,'2026-04-16',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (299,1,'2026-04-17',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (300,1,'2026-04-18',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (301,1,'2026-04-19',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (302,1,'2026-04-20',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (303,1,'2026-04-21',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (304,1,'2026-04-22',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (305,1,'2026-04-23',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (306,1,'2026-04-24',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (307,1,'2026-04-25',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (308,1,'2026-04-26',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (309,1,'2026-04-27',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (310,1,'2026-04-28',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (311,1,'2026-04-29',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (312,1,'2026-04-30',1,0,NULL,'2026-03-18 14:37:18','2026-03-18 14:37:18',NULL,NULL);
INSERT INTO `att_schedule` VALUES (313,1,'2026-09-15',2,0,NULL,'2026-09-08 16:45:15','2026-09-08 16:45:15',NULL,NULL);
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
INSERT INTO `att_shift` VALUES (1,NULL,'01','白班','09:00:00','18:00:00',0,0,8.00,0,NULL,NULL,1,NULL,'2025-12-28 17:18:24','2025-12-28 17:18:24',NULL,NULL);
INSERT INTO `att_shift` VALUES (2,NULL,'002','晚班','09:00:00','23:00:00',0,0,12.50,0,NULL,NULL,1,NULL,'2025-12-28 21:01:47','2025-12-28 21:01:47',NULL,NULL);
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
INSERT INTO `att_shift_period` VALUES (39,2,'上午','09:00','12:00',0,1,1,1);
INSERT INTO `att_shift_period` VALUES (40,2,'下午','13:00','18:00',0,2,1,1);
INSERT INTO `att_shift_period` VALUES (41,2,'加班','18:30','23:00',0,3,1,1);
INSERT INTO `att_shift_period` VALUES (42,1,'第一段','09:00','12:00',0,1,1,0);
INSERT INTO `att_shift_period` VALUES (43,1,'第二段','13:00','18:00',0,2,0,1);
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
/*!40000 ALTER TABLE `hr_application` ENABLE KEYS */;
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
INSERT INTO `hr_employee` VALUES (1,'001','zzx','123456','/employee_photo/001.jpg','/id_card_front/001.jpg','/id_card_back/001.jpg','1','bachelor',12,'han','859865464','2025-12-18','156251645','156251645','regular','2024-12-25','2026-03-11','dm','gm',NULL,NULL,'p3',NULL,NULL,NULL,NULL,'single','league_member',NULL,NULL,NULL,NULL,'212',NULL,NULL,NULL,'2026-03-13',1,1,'2025-12-25 18:23:28',1,'2026-09-09 11:14:36');
INSERT INTO `hr_employee` VALUES (2,'002','小明',NULL,'','','','1','college',2,'han','454454545','2025-12-29','156251645','156251645','regular','2025-12-17','2025-12-29','dgm','gm',NULL,NULL,'p3',NULL,NULL,NULL,NULL,'single',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2025-12-29',2,1,'2025-12-28 15:56:19',1,'2025-12-29 15:16:04');
INSERT INTO `hr_employee` VALUES (4,'test1','test1',NULL,'','','','',NULL,12,NULL,NULL,NULL,'','','intern',NULL,'2026-01-06',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2026-01-06',2,1,'2026-01-06 09:38:43',1,'2026-01-06 16:51:40');
INSERT INTO `hr_employee` VALUES (5,'test2','test2','123456','','','','','college',13,NULL,NULL,'2005-03-10','','','regular',NULL,'2026-03-11',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-01-06 16:52:55',1,'2026-05-11 11:14:42');
INSERT INTO `hr_employee` VALUES (6,'003','003','123456','/employee_photo/003.jpg','/id_card_front/003.png','/id_card_back/003.jpg','','junior_high',12,NULL,NULL,'2026-03-02','','','regular',NULL,'2026-03-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-01-24 22:41:42',1,'2026-05-11 11:14:37');
INSERT INTO `hr_employee` VALUES (7,'K20260512001','test03','123456','','','','',NULL,13,NULL,NULL,NULL,'','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,'2026-05-12 16:25:10',1,'2026-05-12 16:25:10');
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
/*!40000 ALTER TABLE `hr_family_member` ENABLE KEYS */;
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
INSERT INTO `mobile_chat_group` VALUES (1,'默认工作群',1,NULL,4,'111','2026-09-09 10:56:01',1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,1);
INSERT INTO `mobile_chat_group` VALUES (2,'工作群(5人)',1,NULL,4,'111','2026-09-09 11:08:26',1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1);
INSERT INTO `mobile_chat_group` VALUES (3,'工作群(3人)',1,NULL,3,'群聊已创建','2026-09-09 11:08:31',1,'2026-09-09 11:08:31','2026-09-09 11:08:31',1,1);
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
INSERT INTO `mobile_chat_group_member` VALUES (1,1,1,1,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL);
INSERT INTO `mobile_chat_group_member` VALUES (2,1,5,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL);
INSERT INTO `mobile_chat_group_member` VALUES (3,1,6,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL);
INSERT INTO `mobile_chat_group_member` VALUES (4,1,7,2,1,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL);
INSERT INTO `mobile_chat_group_member` VALUES (5,2,1,1,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (6,2,6,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (7,2,5,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (8,2,7,2,1,'2026-09-09 11:08:16','2026-09-09 11:08:16',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (9,3,1,1,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (10,3,5,2,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1);
INSERT INTO `mobile_chat_group_member` VALUES (11,3,6,2,1,'2026-09-09 11:08:32','2026-09-09 11:08:32',1,1);
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
  `status` tinyint DEFAULT '1',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mobile_chat_msg_group` (`group_id`,`created_time`) USING BTREE,
  KEY `idx_mobile_chat_msg_single` (`from_employee_id`,`peer_employee_id`,`created_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='移动端聊天消息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mobile_chat_message`
--

LOCK TABLES `mobile_chat_message` WRITE;
/*!40000 ALTER TABLE `mobile_chat_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `mobile_chat_message` ENABLE KEYS */;
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
/*!40000 ALTER TABLE `mobile_contact_request` ENABLE KEYS */;
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
INSERT INTO `mobile_moment_post` VALUES (1,1,'欢迎来到时光，这里会展示团队动态、活动记录和同事分享。','团队,公告','https://resource.tuniaokj.com/images/swiper/banner-animate3.png',1,1,2,8,'2026-05-14 09:15:27','2026-05-14 09:15:27',NULL,NULL);
INSERT INTO `mobile_moment_post` VALUES (2,5,'今天完成了移动端首页、时光和通讯录的接口联调，后面可以继续补评论和消息提醒。','研发,移动端','https://resource.tuniaokj.com/images/simple/image3.jpg,https://resource.tuniaokj.com/images/simple/image8.jpg',1,1,4,12,'2026-05-14 09:15:27','2026-05-14 09:15:55',NULL,NULL);
INSERT INTO `mobile_moment_post` VALUES (3,6,'组织架构和通讯录已经可以读取真实员工数据，群聊与好友申请也有了后端存储。','人事,通讯录','',1,1,1,6,'2026-05-14 09:15:27','2026-05-14 09:15:52',NULL,NULL);
INSERT INTO `mobile_moment_post` VALUES (4,1,'111','随性分享','',1,1,0,1,'2026-05-14 09:20:19','2026-09-09 10:16:31',1,1);
INSERT INTO `mobile_moment_post` VALUES (5,1,'34','','',1,1,0,0,'2026-09-09 10:10:36','2026-09-09 10:10:36',1,1);
INSERT INTO `mobile_moment_post` VALUES (6,1,'0000','','',1,1,0,0,'2026-09-09 10:10:42','2026-09-09 10:10:42',1,1);
/*!40000 ALTER TABLE `mobile_moment_post` ENABLE KEYS */;
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
INSERT INTO `org_unit` VALUES (10,0,1,'K001','集团公司','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:42:44',1,'2026-01-05 14:42:44');
INSERT INTO `org_unit` VALUES (11,10,2,'G001','公司一','',NULL,'','','','中国江苏省苏州市昆山市红杨路1100号 邮政编码: 215316',31.435435,120.920273,500,'',0,1,'2026-01-05 14:43:53',1,'2026-05-07 09:28:16');
INSERT INTO `org_unit` VALUES (12,11,3,'B001','部门一','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:44:16',1,'2026-01-05 14:44:16');
INSERT INTO `org_unit` VALUES (13,11,3,'B002','部门二','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-05 14:44:30',1,'2026-01-06 09:45:04');
INSERT INTO `org_unit` VALUES (16,10,2,'G002','公司二','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:45:23',1,'2026-01-06 09:45:23');
INSERT INTO `org_unit` VALUES (17,16,3,'B003','部门三','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:45:54',1,'2026-01-06 09:45:54');
INSERT INTO `org_unit` VALUES (18,16,3,'B004','部门四','',NULL,'','','',NULL,NULL,NULL,NULL,'',0,1,'2026-01-06 09:46:05',1,'2026-01-06 09:46:05');
/*!40000 ALTER TABLE `org_unit` ENABLE KEYS */;
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
INSERT INTO `sys_approval_flow` VALUES (7,'Z001','转正管理','regularization','',1,0,1,'2026-01-06 14:14:26',1,'2026-01-06 14:21:52');
INSERT INTO `sys_approval_flow` VALUES (8,'D001','调动申请','transfer','',1,0,1,'2026-01-06 15:16:24',1,'2026-01-06 15:16:24');
INSERT INTO `sys_approval_flow` VALUES (9,'J001','奖惩申请','reward','',1,0,1,'2026-01-06 16:37:02',1,'2026-01-06 16:37:02');
INSERT INTO `sys_approval_flow` VALUES (10,'L001','离职申请','resignation','',1,0,1,'2026-01-06 16:51:35',1,'2026-01-06 16:51:35');
INSERT INTO `sys_approval_flow` VALUES (11,'Q001','请假申请','leave','',1,0,1,'2026-01-06 17:52:21',1,'2026-01-06 17:52:21');
INSERT INTO `sys_approval_flow` VALUES (14,'jb001','加班申请','overtime','',1,0,1,'2026-01-13 09:48:28',1,'2026-01-13 09:48:28');
INSERT INTO `sys_approval_flow` VALUES (15,'bk001','补卡申请','makeup','',1,0,1,'2026-01-13 09:49:15',1,'2026-01-13 09:49:15');
INSERT INTO `sys_approval_flow` VALUES (16,'hx','换休申请','exchange','',1,0,1,'2026-01-13 09:49:40',1,'2026-01-13 09:49:40');
INSERT INTO `sys_approval_flow` VALUES (17,'cc001','出差申请','business','',1,0,1,'2026-01-13 09:50:00',1,'2026-01-13 09:50:00');
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
INSERT INTO `sys_approval_node` VALUES (3,2,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (4,3,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (5,3,'总经理审批',1,3,NULL,2,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (6,4,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (7,5,'部门负责人审批',1,2,NULL,1,NULL,'2025-12-28 16:49:43',NULL,'2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (8,1,'指定人员',1,1,NULL,1,1,'2025-12-28 16:57:30',1,'2026-01-06 14:05:19');
INSERT INTO `sys_approval_node` VALUES (9,6,'001',1,1,NULL,1,1,'2026-01-06 13:57:53',1,'2026-01-06 14:05:19');
INSERT INTO `sys_approval_node` VALUES (12,7,'审批',1,1,1,1,1,'2026-01-06 14:14:26',1,'2026-01-06 14:14:26');
INSERT INTO `sys_approval_node` VALUES (13,8,'申请',1,1,1,1,1,'2026-01-06 15:16:24',1,'2026-01-06 15:16:24');
INSERT INTO `sys_approval_node` VALUES (14,9,'审批',1,1,1,1,1,'2026-01-06 16:37:02',1,'2026-01-06 16:37:02');
INSERT INTO `sys_approval_node` VALUES (15,10,'审批',1,1,1,1,1,'2026-01-06 16:51:35',1,'2026-01-06 16:51:35');
INSERT INTO `sys_approval_node` VALUES (16,11,'审批',1,1,1,1,1,'2026-01-06 17:52:21',1,'2026-01-06 17:52:21');
INSERT INTO `sys_approval_node` VALUES (17,14,'申请',1,1,1,1,1,'2026-01-13 09:48:28',1,'2026-01-13 09:48:28');
INSERT INTO `sys_approval_node` VALUES (18,15,'补卡申请',1,1,1,1,1,'2026-01-13 09:49:15',1,'2026-01-13 09:49:15');
INSERT INTO `sys_approval_node` VALUES (19,16,'换休申请',1,1,1,1,1,'2026-01-13 09:49:40',1,'2026-01-13 09:49:40');
INSERT INTO `sys_approval_node` VALUES (20,17,'出差申请',1,1,1,1,1,'2026-01-13 09:50:00',1,'2026-01-13 09:50:00');
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
INSERT INTO `sys_dict_data` VALUES (45,1,'男','Male','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (46,1,'女','Female','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (47,1,'未知','Unknown','0',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 21:33:37',NULL,1);
INSERT INTO `sys_dict_data` VALUES (48,2,'试用','Probation','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (49,2,'正式','Regular','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (50,2,'离职','Resigned','3',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (51,3,'固定期限','Fixed Term','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (52,3,'无固定期限','Open-ended','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (55,4,'高中','High School','high_school',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (56,4,'大专','College','college',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (57,4,'本科','Bachelor','bachelor',NULL,NULL,5,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (58,4,'硕士','Master','master',NULL,NULL,6,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (59,4,'博士','Doctor','doctor',NULL,NULL,7,1,0,'2025-12-25 14:42:50','2025-12-25 22:09:57',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (60,5,'年假','Annual Leave','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (61,5,'事假','Personal Leave','2',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (62,5,'病假','Sick Leave','3',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (63,5,'婚假','Marriage Leave','4',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (64,5,'产假','Maternity Leave','5',NULL,NULL,5,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (65,5,'陪产假','Paternity Leave','6',NULL,NULL,6,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (66,5,'丧假','Bereavement Leave','7',NULL,NULL,7,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (67,6,'待审批','Pending','0',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (68,6,'已通过','Approved','1',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (69,6,'已驳回','Rejected','2',NULL,NULL,3,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (70,6,'已撤销','Withdrawn','3',NULL,NULL,4,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:37',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (71,7,'启用','Enabled','1',NULL,NULL,1,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (72,7,'禁用','Disabled','0',NULL,NULL,2,1,0,'2025-12-25 14:42:50','2025-12-25 16:13:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (76,4,'初中','Junior High School','junior_high',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (78,4,'中专','Secondary Vocational','secondary_vocational',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (83,15,'汉族','Han','han',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (84,15,'满族','Manchu','manchu',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (85,15,'蒙古族','Mongolian','mongolian',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (86,15,'回族','Hui','hui',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (87,15,'藏族','Tibetan','tibetan',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (88,15,'维吾尔族','Uyghur','uyghur',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (89,15,'苗族','Miao','miao',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (90,15,'彝族','Yi','yi',NULL,NULL,8,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (91,15,'壮族','Zhuang','zhuang',NULL,NULL,9,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (92,15,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (93,16,'正式员工','Regular Employee','regular',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (94,16,'试用员工','Probation Employee','probation',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (95,16,'实习生','Intern','intern',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (96,16,'兼职','Part-time','part_time',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (97,16,'外包','Outsourcing','outsourcing',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (98,17,'未婚','Single','single',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (99,17,'已婚','Married','married',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (100,17,'离异','Divorced','divorced',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (101,17,'丧偶','Widowed','widowed',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (102,18,'群众','Masses','masses',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (103,18,'共青团员','Communist Youth League Member','league_member',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (104,18,'中共党员','CPC Member','party_member',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (105,18,'中共预备党员','CPC Probationary Member','probationary_member',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (106,18,'民主党派','Democratic Party','democratic_party',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (107,19,'父亲','Father','father',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (108,19,'母亲','Mother','mother',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (109,19,'配偶','Spouse','spouse',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (110,19,'子女','Child','child',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (111,19,'兄弟','Brother','brother',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (112,19,'姐妹','Sister','sister',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (113,19,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (114,20,'是','Yes','yes',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (115,20,'否','No','no',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (116,21,'职业资格证书','Professional Qualification','professional',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (117,21,'技能等级证书','Skill Level Certificate','skill',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (118,21,'学历证书','Academic Certificate','academic',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (119,21,'语言证书','Language Certificate','language',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (120,21,'其他','Other','other',NULL,NULL,99,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (121,22,'初级','Primary','primary',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (122,22,'中级','Intermediate','intermediate',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (123,22,'高级','Advanced','advanced',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (124,22,'特级','Expert','expert',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (125,23,'董事长','Chairman','chairman',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (126,23,'总经理','General Manager','gm',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (127,23,'副总经理','Deputy General Manager','dgm',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (128,23,'部门经理','Department Manager','dm',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (129,23,'主管','Supervisor','supervisor',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (130,23,'组长','Team Leader','team_leader',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (131,23,'员工','Staff','staff',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (132,24,'P1','P1','p1',NULL,NULL,1,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (133,24,'P2','P2','p2',NULL,NULL,2,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (134,24,'P3','P3','p3',NULL,NULL,3,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (135,24,'P4','P4','p4',NULL,NULL,4,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (136,24,'P5','P5','p5',NULL,NULL,5,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (137,24,'M1','M1','m1',NULL,NULL,6,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (138,24,'M2','M2','m2',NULL,NULL,7,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (139,24,'M3','M3','m3',NULL,NULL,8,1,0,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (142,27,'总经理','General Manager','gm',NULL,NULL,1,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (143,27,'副总经理','Deputy General Manager','dgm',NULL,NULL,2,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (144,27,'技术总监','CTO','cto',NULL,NULL,3,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (145,27,'产品总监','CPO','cpo',NULL,NULL,4,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (146,27,'技术经理','Technical Manager','tech_manager',NULL,NULL,5,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (147,27,'产品经理','Product Manager','product_manager',NULL,NULL,6,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (148,27,'项目经理','Project Manager','project_manager',NULL,NULL,7,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (149,27,'高级软件工程师','Senior Software Engineer','senior_engineer',NULL,NULL,8,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (150,27,'软件工程师','Software Engineer','engineer',NULL,NULL,9,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (151,27,'初级软件工程师','Junior Software Engineer','junior_engineer',NULL,NULL,10,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (152,27,'测试工程师','Test Engineer','test_engineer',NULL,NULL,11,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (153,27,'运维工程师','DevOps Engineer','devops_engineer',NULL,NULL,12,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (154,27,'UI设计师','UI Designer','ui_designer',NULL,NULL,13,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (155,27,'人事经理','HR Manager','hr_manager',NULL,NULL,14,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (156,27,'人事专员','HR Specialist','hr_specialist',NULL,NULL,15,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (157,27,'财务经理','Finance Manager','finance_manager',NULL,NULL,16,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (158,27,'财务专员','Finance Specialist','finance_specialist',NULL,NULL,17,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (159,27,'行政专员','Admin Specialist','admin_specialist',NULL,NULL,18,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (160,27,'销售经理','Sales Manager','sales_manager',NULL,NULL,19,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (161,27,'销售代表','Sales Representative','sales_rep',NULL,NULL,20,1,0,'2025-12-25 22:07:42','2025-12-25 22:07:42',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (163,28,'部门调动','Department Transfer','1',NULL,NULL,1,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (164,28,'职位变更','Position Change','2',NULL,NULL,2,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (165,28,'部门+职位变更','Dept Position Change','3',NULL,NULL,3,1,0,'2025-12-28 16:29:08','2025-12-28 16:29:08',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (166,29,'主动离职','Voluntary','1',NULL,NULL,1,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (167,29,'被动离职','Involuntary','2',NULL,NULL,2,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (168,29,'合同到期','Contract Expired','3',NULL,NULL,3,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (169,29,'退休','Retirement','4',NULL,NULL,4,1,0,'2025-12-28 16:29:45','2025-12-28 16:29:45',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (170,30,'优秀员工','Outstanding Employee','1',NULL,NULL,1,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (171,30,'项目奖金','Project Bonus','2',NULL,NULL,2,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (172,30,'年终奖','Year-end Bonus','3',NULL,NULL,3,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (173,30,'创新奖','Innovation Award','4',NULL,NULL,4,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (174,30,'其他奖励','Other Reward','5',NULL,NULL,5,1,0,'2025-12-28 16:30:15','2025-12-28 16:30:15',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (175,31,'警告','Warning','1',NULL,NULL,1,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (176,31,'记过','Demerit','2',NULL,NULL,2,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (177,31,'降级','Demotion','3',NULL,NULL,3,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (178,31,'罚款','Fine','4',NULL,NULL,4,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (179,31,'其他处罚','Other Punishment','5',NULL,NULL,5,1,0,'2025-12-28 16:30:25','2025-12-28 16:30:25',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (180,33,'爱好',NULL,'hobby',NULL,NULL,1,1,0,'2025-12-29 13:39:19','2025-12-29 13:39:19',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (181,33,'特长',NULL,'specialty',NULL,NULL,2,1,0,'2025-12-29 13:39:26','2025-12-29 13:39:26',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (182,34,'集团',NULL,'1',NULL,NULL,1,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (183,34,'公司',NULL,'2',NULL,NULL,2,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
INSERT INTO `sys_dict_data` VALUES (184,34,'部门',NULL,'3',NULL,NULL,3,1,0,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
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
INSERT INTO `sys_dict_data_i18n` VALUES (1,1,'zh-CN','男','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (2,2,'zh-CN','女','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (3,3,'zh-CN','试用','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (4,4,'zh-CN','正式','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (5,5,'zh-CN','离职','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (6,6,'zh-CN','固定期限','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (7,7,'zh-CN','无固定期限','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (8,8,'zh-CN','完成任务','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (9,9,'zh-CN','年假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (10,10,'zh-CN','事假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (11,11,'zh-CN','病假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (12,12,'zh-CN','婚假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (13,13,'zh-CN','产假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (14,14,'zh-CN','陪产假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (15,15,'zh-CN','丧假','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (16,16,'zh-CN','工作日加班','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (17,17,'zh-CN','周末加班','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (18,18,'zh-CN','节假日加班','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (19,19,'zh-CN','高中','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (20,20,'zh-CN','大专','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (21,21,'zh-CN','本科','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (22,22,'zh-CN','硕士','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (23,23,'zh-CN','博士','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (24,24,'zh-CN','未婚','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (25,25,'zh-CN','已婚','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (26,26,'zh-CN','离异','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (27,27,'zh-CN','群众','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (28,28,'zh-CN','共青团员','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (29,29,'zh-CN','中共党员','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (30,30,'zh-CN','民主党派','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (31,31,'zh-CN','晋升','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (32,32,'zh-CN','降级','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (33,33,'zh-CN','平调','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (34,34,'zh-CN','奖励','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (35,35,'zh-CN','惩罚','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (36,36,'zh-CN','主动离职','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (37,37,'zh-CN','辞退','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (38,38,'zh-CN','合同到期','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (39,1,'en-US','Male','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (40,2,'en-US','Female','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (41,3,'en-US','Probation','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (42,4,'en-US','Regular','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (43,5,'en-US','Resigned','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (44,6,'en-US','Fixed Term','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (45,7,'en-US','Open-ended','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (46,8,'en-US','Task-based','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (47,9,'en-US','Annual Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (48,10,'en-US','Personal Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (49,11,'en-US','Sick Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (50,12,'en-US','Marriage Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (51,13,'en-US','Maternity Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (52,14,'en-US','Paternity Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (53,15,'en-US','Bereavement Leave','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (54,16,'en-US','Weekday Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (55,17,'en-US','Weekend Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (56,18,'en-US','Holiday Overtime','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (57,19,'en-US','High School','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (58,20,'en-US','College','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (59,21,'en-US','Bachelor','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (60,22,'en-US','Master','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (61,23,'en-US','Doctor','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (62,24,'en-US','Single','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (63,25,'en-US','Married','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (64,26,'en-US','Divorced','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (65,27,'en-US','Masses','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (66,28,'en-US','League Member','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (67,29,'en-US','Party Member','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (68,30,'en-US','Democratic Party','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (69,31,'en-US','Promotion','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (70,32,'en-US','Demotion','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (71,33,'en-US','Transfer','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (72,34,'en-US','Reward','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (73,35,'en-US','Punishment','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (74,36,'en-US','Voluntary Resignation','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (75,37,'en-US','Dismissal','2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (76,38,'en-US','Contract Expiration','2025-12-22 21:19:11','2025-12-22 21:19:11');
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
INSERT INTO `sys_dict_type` VALUES (1,'gender','性别','Gender','性别字典',1,'2025-12-25 14:42:50','2025-12-25 18:19:15',NULL,1);
INSERT INTO `sys_dict_type` VALUES (2,'employee_status','员工状态','Employee Status','员工在职状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (3,'contract_type','合同类型','Contract Type','劳动合同类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (4,'education','学历','Education','学历类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (5,'leave_type','请假类型','Leave Type','请假申请类型',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (6,'approval_status','审批状态','Approval Status','审批流程状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (7,'sys_status','系统状态','System Status','通用启用禁用状态',1,'2025-12-25 14:42:50','2025-12-25 16:14:48',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (15,'nation','民族','Nation',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (16,'employee_type','员工类别','Employee Type',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (17,'marital_status','婚姻状况','Marital Status',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (18,'political_status','政治面貌','Political Status',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (19,'family_relation','家庭成员关系','Family Relation',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (20,'full_time','是否全日制','Full Time',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (21,'cert_type','证书类型','Certificate Type',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (22,'cert_level','证书等级','Certificate Level',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (23,'duty','职务','Duty',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (24,'job_level','职级','Job Level',NULL,1,'2025-12-25 16:38:11','2025-12-25 16:38:11',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (27,'position','职位','Position','员工职位',1,'2025-12-25 22:07:17','2025-12-25 22:07:17',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (28,'transfer_type','调动类型','Transfer Type','员工调动类型',1,'2025-12-28 16:28:41','2025-12-28 16:28:41',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (29,'resign_type','离职类型','Resignation Type','员工离职类型',1,'2025-12-28 16:29:19','2025-12-28 16:29:19',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (30,'reward_category','奖励类别','Reward Category','奖励类别',1,'2025-12-28 16:29:53','2025-12-28 16:29:53',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (31,'punish_category','惩罚类别','Punishment Category','惩罚类别',1,'2025-12-28 16:29:53','2025-12-28 16:29:53',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (33,'employee_extra_field','员工扩展字段',NULL,'用于定义员工的额外自定义字段',1,'2025-12-29 13:38:21','2025-12-29 13:38:21',NULL,NULL);
INSERT INTO `sys_dict_type` VALUES (34,'org_unit_type','组织类型',NULL,'组织架构节点类型',1,'2026-01-05 14:08:38','2026-01-05 14:08:38',NULL,NULL);
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
INSERT INTO `sys_file_config` VALUES (2,'employee_avatar_path','员工头像路径','D:/rzphoto/employee_photo','员工头像照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL);
INSERT INTO `sys_file_config` VALUES (3,'id_card_front_path','身份证正面路径','D:/rzphoto/id_card_front','员工身份证正面照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL);
INSERT INTO `sys_file_config` VALUES (4,'id_card_back_path','身份证反面路径','D:/rzphoto/id_card_back','员工身份证反面照片存储目录','2026-03-09 09:49:55','2026-03-09 09:49:55',NULL,NULL);
INSERT INTO `sys_file_config` VALUES (5,'contract_photo_path','合同照片路径','D:/rzphoto/Pic_Contract','合同照片存储目录','2026-03-09 10:17:22','2026-03-09 10:17:22',NULL,NULL);
INSERT INTO `sys_file_config` VALUES (6,'diploma_photo_path','毕业证照片路径','D:/rzphoto/Pic_Diploma','毕业证照片存储目录','2026-03-09 11:08:07','2026-03-09 11:08:07',NULL,NULL);
INSERT INTO `sys_file_config` VALUES (7,'cert_photo_path','证书照片路径','D:/rzphoto/Pic_Certificate','证书照片存储目录','2026-03-09 11:08:07','2026-03-09 11:08:07',NULL,NULL);
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
INSERT INTO `sys_language` VALUES (1,'zh-CN','简体中文',NULL,1,1,1,'2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `sys_language` VALUES (2,'en-US','English',NULL,2,1,0,'2025-12-22 21:19:11','2025-12-22 21:19:11');
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
) ENGINE=InnoDB AUTO_INCREMENT=1247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,0,1,'home','首页','Home','/home','layout.base$view.home',NULL,'mdi:monitor-dashboard',1,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (2,0,1,'organization','组织架构','Organization','/organization','layout.base',NULL,'mdi:office-building',2,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (3,0,1,'hr','人事管理','HR Management','/hr','layout.base',NULL,'mdi:account-group',3,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (4,0,1,'attendance','考勤管理','Attendance','/attendance','layout.base',NULL,'mdi:clock-outline',4,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (5,0,1,'application','申请管理','Applications','/application','layout.base',NULL,'mdi:file-document-multiple',5,1,1,'2025-12-25 14:10:05','2025-12-25 16:28:29',NULL,NULL);
INSERT INTO `sys_menu` VALUES (6,0,1,'approval','审批管理','Approval','/approval','layout.base',NULL,'mdi:check-decagram',6,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (7,0,1,'report','报表管理','Reports','/report','layout.base',NULL,'mdi:chart-bar',7,1,1,'2025-12-25 14:10:05','2025-12-25 16:28:29',NULL,NULL);
INSERT INTO `sys_menu` VALUES (8,0,1,'system','系统管理','System','/system','layout.base',NULL,'mdi:cog',8,1,1,'2025-12-25 14:10:05','2025-12-25 14:10:05',NULL,NULL);
INSERT INTO `sys_menu` VALUES (24,2,2,'organization_org-structure','组织架构','Organization Structure','/organization/org-structure','view.organization_org-structure',NULL,'mdi:file-tree',0,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (31,3,2,'hr_employee','员工管理','Employee Management','/hr/employee','view.hr_employee',NULL,'mdi:account-multiple',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (32,3,2,'hr_contract','合同管理','Contract Management','/hr/contract','view.hr_contract',NULL,'mdi:file-document-edit',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (41,4,2,'attendance_shift','班次管理','Shift Management','/attendance/shift','view.attendance_shift',NULL,'mdi:clock-time-four',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (42,4,2,'attendance_schedule','员工排班','Employee Schedule','/attendance/schedule','view.attendance_schedule',NULL,'mdi:calendar-clock',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (43,4,2,'attendance_holiday','节假日管理','Holiday Management','/attendance/holiday','view.attendance_holiday',NULL,'mdi:calendar-star',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (44,4,2,'attendance_daily','日考勤','Daily Attendance','/attendance/daily','view.attendance_daily',NULL,'mdi:calendar-today',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (45,4,2,'attendance_monthly','月考勤','Monthly Attendance','/attendance/monthly','view.attendance_monthly',NULL,'mdi:calendar-month',5,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (51,5,2,'application_leave','请假申请','Leave Application','/application/leave','view.application_leave',NULL,'mdi:beach',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (52,5,2,'application_overtime','加班申请','Overtime Application','/application/overtime','view.application_overtime',NULL,'mdi:clock-plus',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (53,5,2,'application_makeup','补卡申请','Makeup Application','/application/makeup','view.application_makeup',NULL,'mdi:clock-check',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (54,5,2,'application_exchange','换休申请','Exchange Leave','/application/exchange','view.application_exchange','','mdi:swap-horizontal-circle',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (55,5,2,'application_business','出差申请','Business Trip','/application/business','view.application_business',NULL,'mdi:airplane',5,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (61,6,2,'approval_pending','待审批','Pending Approval','/approval/pending','view.approval_pending',NULL,'mdi:clock-alert',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (62,6,2,'approval_mine','我的申请','My Applications','/approval/mine','view.approval_mine',NULL,'mdi:file-document-check',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (63,6,2,'approval_flow','审批流程配置','Approval Flow Config','/approval/flow','view.approval_flow',NULL,'mdi:sitemap',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (71,7,2,'report_employee','员工报表','Employee Report','/report/employee','view.report_employee',NULL,'mdi:chart-pie',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (72,7,2,'report_attendance','考勤报表','Attendance Report','/report/attendance','view.report_attendance',NULL,'mdi:chart-line',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (81,8,2,'system_user','用户管理','User Management','/system/user','view.system_user','','mdi:account',1,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (82,8,2,'system_role','角色管理','Role Management','/system/role','view.system_role',NULL,'mdi:account-key',2,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (83,8,2,'system_menu','菜单管理','Menu Management','/system/menu','view.system_menu',NULL,'mdi:menu',3,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (84,8,2,'system_dict','字典管理','Dictionary Management','/system/dict','view.system_dict',NULL,'mdi:book-alphabet',4,1,1,'2025-12-25 14:10:05','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (85,1246,2,'system_notice','公告管理','Notice Management','/system/notice','view.system_notice','system:notice:list','mdi:bullhorn',1,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1086,31,3,'hr_employee_add','新增','Add',NULL,NULL,'hr:employee:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1087,31,3,'hr_employee_edit','编辑','Edit',NULL,NULL,'hr:employee:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1088,31,3,'hr_employee_delete','删除','Delete',NULL,NULL,'hr:employee:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1089,31,3,'hr_employee_export','导出','Export',NULL,NULL,'hr:employee:export',NULL,4,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1091,32,3,'hr_contract_add','新增','Add',NULL,NULL,'hr:contract:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1092,32,3,'hr_contract_edit','编辑','Edit',NULL,NULL,'hr:contract:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1093,32,3,'hr_contract_delete','删除','Delete',NULL,NULL,'hr:contract:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1127,81,3,'system_user_add','新增','Add',NULL,NULL,'system:user:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1128,81,3,'system_user_edit','编辑','Edit',NULL,NULL,'system:user:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1129,81,3,'system_user_delete','删除','Delete',NULL,NULL,'system:user:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1130,81,3,'system_user_reset','重置密码','Reset Password',NULL,NULL,'system:user:reset',NULL,4,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1132,82,3,'system_role_add','新增','Add',NULL,NULL,'system:role:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1133,82,3,'system_role_edit','编辑','Edit',NULL,NULL,'system:role:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1134,82,3,'system_role_delete','删除','Delete',NULL,NULL,'system:role:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1136,83,3,'system_menu_add','新增','Add',NULL,NULL,'system:menu:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1137,83,3,'system_menu_edit','编辑','Edit',NULL,NULL,'system:menu:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1138,83,3,'system_menu_delete','删除','Delete',NULL,NULL,'system:menu:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1140,84,3,'system_dict_add','新增','Add',NULL,NULL,'system:dict:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1141,84,3,'system_dict_edit','编辑','Edit',NULL,NULL,'system:dict:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1142,84,3,'system_dict_delete','删除','Delete',NULL,NULL,'system:dict:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1144,41,3,'attendance_shift_add','新增','Add',NULL,NULL,'attendance:shift:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1145,41,3,'attendance_shift_edit','编辑','Edit',NULL,NULL,'attendance:shift:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1146,41,3,'attendance_shift_delete','删除','Delete',NULL,NULL,'attendance:shift:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1148,42,3,'attendance_schedule_add','新增','Add',NULL,NULL,'attendance:schedule:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1149,42,3,'attendance_schedule_edit','编辑','Edit',NULL,NULL,'attendance:schedule:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1150,42,3,'attendance_schedule_delete','删除','Delete',NULL,NULL,'attendance:schedule:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1152,43,3,'attendance_holiday_add','新增','Add',NULL,NULL,'attendance:holiday:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1153,43,3,'attendance_holiday_edit','编辑','Edit',NULL,NULL,'attendance:holiday:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1154,43,3,'attendance_holiday_delete','删除','Delete',NULL,NULL,'attendance:holiday:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1156,44,3,'attendance_daily_export','导出','Export',NULL,NULL,'attendance:daily:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1158,45,3,'attendance_monthly_export','导出','Export',NULL,NULL,'attendance:monthly:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1160,51,3,'application_leave_add','新增','Add',NULL,NULL,'application:leave:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1161,51,3,'application_leave_edit','编辑','Edit',NULL,NULL,'application:leave:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1162,51,3,'application_leave_delete','删除','Delete',NULL,NULL,'application:leave:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1164,52,3,'application_overtime_add','新增','Add',NULL,NULL,'application:overtime:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1165,52,3,'application_overtime_edit','编辑','Edit',NULL,NULL,'application:overtime:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1166,52,3,'application_overtime_delete','删除','Delete',NULL,NULL,'application:overtime:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1168,53,3,'application_makeup_add','新增','Add',NULL,NULL,'application:makeup:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1169,53,3,'application_makeup_edit','编辑','Edit',NULL,NULL,'application:makeup:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1170,53,3,'application_makeup_delete','删除','Delete',NULL,NULL,'application:makeup:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1172,54,3,'application_exchange_add','新增','Add',NULL,NULL,'application:exchange:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1173,54,3,'application_exchange_edit','编辑','Edit',NULL,NULL,'application:exchange:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1174,54,3,'application_exchange_delete','删除','Delete',NULL,NULL,'application:exchange:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1176,55,3,'application_business_add','新增','Add',NULL,NULL,'application:business:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1177,55,3,'application_business_edit','编辑','Edit',NULL,NULL,'application:business:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1178,55,3,'application_business_delete','删除','Delete',NULL,NULL,'application:business:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1180,61,3,'approval_pending_approve','审批','Approve',NULL,NULL,'approval:pending:approve',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1181,61,3,'approval_pending_reject','驳回','Reject',NULL,NULL,'approval:pending:reject',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1184,63,3,'approval_flow_add','新增','Add',NULL,NULL,'approval:flow:add',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1185,63,3,'approval_flow_edit','编辑','Edit',NULL,NULL,'approval:flow:edit',NULL,2,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1186,63,3,'approval_flow_delete','删除','Delete',NULL,NULL,'approval:flow:delete',NULL,3,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1188,71,3,'report_employee_export','导出','Export',NULL,NULL,'report:employee:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1190,72,3,'report_attendance_export','导出','Export',NULL,NULL,'report:attendance:export',NULL,1,1,1,'2025-12-25 14:10:14','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1191,4,2,'attendance_clock','打卡记录','Clock Record','/attendance/clock','view.attendance_clock',NULL,NULL,6,1,1,'2025-12-29 09:55:42','2026-01-12 20:42:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1193,1191,3,NULL,'新增','Add',NULL,NULL,'attendance:clock:add',NULL,2,1,1,'2025-12-29 10:01:52','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1194,1191,3,NULL,'删除','Delete',NULL,NULL,'attendance:clock:delete',NULL,3,1,1,'2025-12-29 10:01:52','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1195,3,2,'application_regularization','转正申请','Regularization','/application/regularization','view.application_regularization','','mdi:account-check',6,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (1196,3,2,'application_transfer','调动申请','Transfer','/application/transfer','view.application_transfer','','mdi:swap-horizontal',7,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (1197,3,2,'application_reward','奖惩申请','Reward & Punishment','/application/reward','view.application_reward','','mdi:medal',8,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (1198,3,2,'application_resignation','离职申请','Resignation','/application/resignation','view.application_resignation','','mdi:account-remove',9,1,1,'2025-12-29 15:34:20','2026-01-12 20:42:52',NULL,1);
INSERT INTO `sys_menu` VALUES (1202,1195,3,NULL,'新增','Add',NULL,NULL,'application:regularization:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1203,1195,3,NULL,'编辑','Edit',NULL,NULL,'application:regularization:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1204,1195,3,NULL,'删除','Delete',NULL,NULL,'application:regularization:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1205,1196,3,NULL,'新增','Add',NULL,NULL,'application:transfer:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1206,1196,3,NULL,'编辑','Edit',NULL,NULL,'application:transfer:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1207,1196,3,NULL,'删除','Delete',NULL,NULL,'application:transfer:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1208,1197,3,NULL,'新增','Add',NULL,NULL,'application:reward:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1209,1197,3,NULL,'编辑','Edit',NULL,NULL,'application:reward:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1210,1197,3,NULL,'删除','Delete',NULL,NULL,'application:reward:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1211,1198,3,NULL,'新增','Add',NULL,NULL,'application:resignation:add',NULL,1,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1212,1198,3,NULL,'编辑','Edit',NULL,NULL,'application:resignation:edit',NULL,2,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1213,1198,3,NULL,'删除','Delete',NULL,NULL,'application:resignation:delete',NULL,3,1,1,'2026-01-05 14:36:56','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1214,24,3,NULL,'新增','Add',NULL,NULL,'org:unit:add',NULL,1,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1215,24,3,NULL,'编辑','Edit',NULL,NULL,'org:unit:edit',NULL,2,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1216,24,3,NULL,'删除','Delete',NULL,NULL,'org:unit:delete',NULL,3,1,1,'2026-01-05 14:37:34','2026-01-12 20:43:01',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1217,1245,2,'system_mobile-menu','移动端菜单','Mobile Menu','/system/mobile-menu','view.system_mobile-menu','system:mobile-menu:list','mdi:cellphone-text',1,1,1,'2026-01-12 20:37:46','2026-01-12 20:37:46',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1222,1217,3,'system_mobile-menu_list','查询','List',NULL,NULL,'system:mobile-menu:list',NULL,1,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1223,1217,3,'system_mobile-menu_add','新增移动菜单','新增移动菜单',NULL,NULL,'system:mobile-menu:add',NULL,1,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1224,1217,3,'system_mobile-menu_edit','编辑移动菜单','编辑移动菜单',NULL,NULL,'system:mobile-menu:edit',NULL,2,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1225,1217,3,'system_mobile-menu_delete','删除移动菜单','删除移动菜单',NULL,NULL,'system:mobile-menu:delete',NULL,3,1,1,'2026-01-12 20:38:22','2026-01-12 20:38:22',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1226,8,2,'system_file-config','路径管理','File Config','/system/file-config','view.system_file-config','system:fileConfig:list','mdi:folder-cog-outline',60,1,1,'2026-03-09 10:00:37','2026-03-09 10:00:37',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1227,1245,2,'approval_mobile-approver','移动端审批权限','Mobile Approval Permission','/approval/mobile-approver','view.approval_mobile-approver','','mdi:cellphone-check',4,1,1,'2026-03-11 17:24:20','2026-05-14 13:57:55',NULL,1);
INSERT INTO `sys_menu` VALUES (1230,85,3,'system_notice_add','新增','Add',NULL,NULL,'system:notice:add',NULL,1,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1231,85,3,'system_notice_edit','编辑','Edit',NULL,NULL,'system:notice:edit',NULL,2,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1232,85,3,'system_notice_delete','删除','Delete',NULL,NULL,'system:notice:delete',NULL,3,1,1,'2026-03-12 10:36:21','2026-03-12 10:36:21',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1240,4,2,'attendance_location','打卡地点','Clock Locations','/attendance/location','view.attendance_location',NULL,'mdi:map-marker-radius',3,1,1,'2026-05-07 13:59:11','2026-05-07 13:59:52',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1241,1246,2,'system_feedback','意见反馈','Feedback','/system/feedback','view.system_feedback','system:feedback:list','mdi:message-alert-outline',2,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1242,1241,3,'system_feedback_reply','回复反馈','回复反馈',NULL,NULL,'system:feedback:reply',NULL,1,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1243,1241,3,'system_feedback_delete','删除反馈','删除反馈',NULL,NULL,'system:feedback:delete',NULL,2,1,1,'2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1245,0,1,'mobile','移动管理','Mobile Management','/mobile','layout.base',NULL,'mdi:cellphone-cog',10,1,1,'2026-05-14 13:57:12','2026-05-14 13:57:12',NULL,NULL);
INSERT INTO `sys_menu` VALUES (1246,0,1,'notification','通知管理','Notification Management','/notification','layout.base',NULL,'mdi:bell-cog',9,1,1,'2026-05-14 14:03:42','2026-05-14 14:03:42',NULL,NULL);
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
INSERT INTO `sys_menu_i18n` VALUES (147,1,'en-US','Home','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (148,2,'en-US','Organization','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (149,3,'en-US','HR Management','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (150,4,'en-US','Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (151,5,'en-US','Application','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (152,6,'en-US','Approval','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (153,7,'en-US','Report','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (154,8,'en-US','System','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (155,24,'en-US','Organization Structure','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (156,21,'en-US','Company','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (157,22,'en-US','Department','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (158,23,'en-US','Position','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (159,31,'en-US','Employee','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (160,32,'en-US','Contract','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (161,33,'en-US','Regularization','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (162,34,'en-US','Transfer','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (163,35,'en-US','Reward & Punishment','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (164,36,'en-US','Resignation','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (165,41,'en-US','Shift','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (166,42,'en-US','Schedule','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (167,43,'en-US','Holiday','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (168,44,'en-US','Daily Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (169,45,'en-US','Monthly Attendance','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (170,51,'en-US','Leave','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (171,52,'en-US','Overtime','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (172,53,'en-US','Makeup','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (173,54,'en-US','Exchange','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (174,55,'en-US','Business Trip','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (175,61,'en-US','Pending','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (176,62,'en-US','My Applications','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (177,63,'en-US','Flow Config','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (178,71,'en-US','Employee Report','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (179,72,'en-US','Attendance Report','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (180,81,'en-US','User','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (181,82,'en-US','Role','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (182,83,'en-US','Menu','2025-12-25 14:10:05','2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (183,84,'en-US','Dictionary','2025-12-25 14:10:05','2025-12-25 14:10:05');
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
INSERT INTO `sys_mobile_menu` VALUES (1,'请假申请','leave','calendar','#5cadff','/pages/apply/leave/index','apply',1,1,NULL,'2026-01-12 20:35:13',1,'2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (2,'加班申请','overtime','checkbox','#19be6b','/pages/apply/overtime/index','apply',2,1,NULL,'2026-01-12 20:35:13',1,'2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (3,'补卡申请','card','checkbox','#ff9900','/pages/apply/card/index','apply',3,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (4,'出差申请','travel','location','#ed4014','/pages/apply/travel/index','apply',4,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (7,'离职申请','resign','closeempty','#f5222d','/pages/apply/resign/index','apply',3,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:35:13');
INSERT INTO `sys_mobile_menu` VALUES (8,'换休申请','exchange','refreshempty','#fa8c16','/pages/apply/exchange/index','apply',4,1,NULL,'2026-01-12 20:35:13',NULL,'2026-01-12 20:35:13');
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
INSERT INTO `sys_notice` VALUES (1,'111',2,'222',1,'2026-03-01 00:00:00','2026-03-12 10:40:13','2026-03-12 10:46:42',1,1);
INSERT INTO `sys_notice` VALUES (2,'系统公告',1,'欢迎使用人资OA移动端。\n\n目前已上线登录、工作台、考勤打卡、请假/加班/补卡/离职/出差/换休申请、待办审批、通知公告、个人信息和意见反馈等功能。\n\n如遇到登录、定位、审批或资料显示问题，请先在“我的 - 意见反馈”提交问题，管理员会在后台系统管理中查看并处理。\n',1,'2026-05-13 00:00:00','2026-05-13 17:00:35','2026-05-13 17:00:35',NULL,NULL);
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
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
INSERT INTO `sys_role` VALUES (1,'ROLE_ADMIN','系统管理员','',1,1,'2025-12-22 21:19:11','2026-03-12 15:31:12',NULL,8,1);
INSERT INTO `sys_role` VALUES (10,'nhsys','管理员','',1,0,'2026-01-19 18:45:50','2026-03-12 15:31:33',1,8,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=4242 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (4051,1,1,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4052,1,2,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4053,1,24,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4054,1,1214,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4055,1,1215,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4056,1,1216,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4057,1,3,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4058,1,31,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4059,1,1086,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4060,1,1087,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4061,1,1088,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4062,1,1089,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4063,1,32,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4064,1,1091,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4065,1,1092,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4066,1,1093,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4067,1,1195,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4068,1,1202,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4069,1,1203,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4070,1,1204,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4071,1,1196,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4072,1,1205,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4073,1,1206,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4074,1,1207,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4075,1,1197,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4076,1,1208,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4077,1,1209,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4078,1,1210,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4079,1,1198,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4080,1,1211,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4081,1,1212,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4082,1,1213,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4083,1,4,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4084,1,41,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4085,1,1144,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4086,1,1145,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4087,1,1146,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4088,1,42,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4089,1,1148,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4090,1,1149,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4091,1,1150,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4092,1,43,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4093,1,1152,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4094,1,1153,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4095,1,1154,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4096,1,44,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4097,1,1156,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4098,1,45,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4099,1,1158,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4100,1,1191,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4101,1,1193,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4102,1,1194,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4103,1,5,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4104,1,51,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4105,1,1160,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4106,1,1161,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4107,1,1162,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4108,1,52,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4109,1,1164,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4110,1,1165,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4111,1,1166,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4112,1,53,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4113,1,1168,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4114,1,1169,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4115,1,1170,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4116,1,54,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4117,1,1172,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4118,1,1173,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4119,1,1174,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4120,1,55,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4121,1,1176,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4122,1,1177,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4123,1,1178,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4124,1,6,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4125,1,61,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4126,1,1180,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4127,1,1181,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4128,1,62,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4129,1,63,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4130,1,1184,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4131,1,1185,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4132,1,1186,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4133,1,1227,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4134,1,7,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4135,1,71,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4136,1,1188,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4137,1,72,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4138,1,1190,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4139,1,8,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4140,1,81,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4141,1,1127,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4142,1,1128,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4143,1,1129,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4144,1,1130,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4145,1,82,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4146,1,1132,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4147,1,1133,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4148,1,1134,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4149,1,83,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4150,1,1136,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4151,1,1137,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4152,1,1138,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4153,1,84,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4154,1,1140,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4155,1,1141,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4156,1,1142,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4157,1,85,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4158,1,1230,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4159,1,1231,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4160,1,1232,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4161,1,1217,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4162,1,1222,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4163,1,1223,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4164,1,1224,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4165,1,1225,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4166,1,1226,'2026-03-12 15:31:11');
INSERT INTO `sys_role_menu` VALUES (4167,10,1,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4168,10,2,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4169,10,24,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4170,10,1214,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4171,10,1215,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4172,10,1216,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4173,10,3,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4174,10,31,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4175,10,1086,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4176,10,1087,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4177,10,1088,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4178,10,1089,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4179,10,32,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4180,10,1091,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4181,10,1092,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4182,10,1093,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4183,10,1195,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4184,10,1202,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4185,10,1203,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4186,10,1204,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4187,10,1196,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4188,10,1205,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4189,10,1206,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4190,10,1207,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4191,10,1197,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4192,10,1208,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4193,10,1209,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4194,10,1210,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4195,10,1198,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4196,10,1211,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4197,10,1212,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4198,10,1213,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4199,10,6,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4200,10,61,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4201,10,1180,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4202,10,1181,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4203,10,62,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4204,10,63,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4205,10,1184,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4206,10,1185,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4207,10,1186,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4208,10,1227,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4209,10,81,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4210,10,1127,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4211,10,1128,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4212,10,1129,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4213,10,1130,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4214,10,82,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4215,10,1132,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4216,10,1133,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4217,10,1134,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4218,10,83,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4219,10,1136,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4220,10,1137,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4221,10,1138,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4222,10,84,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4223,10,1140,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4224,10,1141,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4225,10,1142,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4226,10,8,'2026-03-12 15:31:32');
INSERT INTO `sys_role_menu` VALUES (4227,1,1240,'2026-05-07 14:07:42');
INSERT INTO `sys_role_menu` VALUES (4228,10,1240,'2026-05-07 14:07:42');
INSERT INTO `sys_role_menu` VALUES (4229,1,1241,'2026-05-13 17:00:35');
INSERT INTO `sys_role_menu` VALUES (4230,10,1241,'2026-05-13 17:00:35');
INSERT INTO `sys_role_menu` VALUES (4231,1,1245,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4232,10,1245,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4234,10,1217,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4235,10,1223,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4236,10,1224,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4237,10,1225,'2026-05-14 13:57:12');
INSERT INTO `sys_role_menu` VALUES (4238,1,1246,'2026-05-14 14:03:42');
INSERT INTO `sys_role_menu` VALUES (4239,10,1246,'2026-05-14 14:03:42');
INSERT INTO `sys_role_menu` VALUES (4241,10,85,'2026-05-14 14:03:42');
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$O.gQasdj6qcXGwDqDOKZOOet.DzedNhQAP78Ku/GiOtPQfNsLLepe','系统管理员',NULL,NULL,NULL,0,1,NULL,NULL,NULL,'2025-12-22 21:19:11','2026-01-24 22:22:44',NULL,1);
INSERT INTO `sys_user` VALUES (8,'nhsys','$2a$10$EtjBMrV6a0.qQ3Axhho6YeShIA0PCNDjm/wfL.AqaFKh60NUfStIi','管理员','','',NULL,0,1,NULL,NULL,NULL,'2026-01-19 18:46:03','2026-03-12 15:31:45',1,8);
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
INSERT INTO `sys_user_role` VALUES (17,1,1,'2026-01-12 20:47:03');
INSERT INTO `sys_user_role` VALUES (24,8,10,'2026-03-12 15:31:44');
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
INSERT INTO `wf_process_definition` VALUES (1,NULL,'LEAVE','请假审批流程','leave','请假申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (2,NULL,'OVERTIME','加班审批流程','overtime','加班申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (3,NULL,'CARD_REPLACEMENT','补卡审批流程','card_replacement','补卡申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (4,NULL,'COMP_LEAVE','换休审批流程','comp_leave','换休申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (5,NULL,'BUSINESS_TRIP','出差审批流程','business_trip','出差申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (6,NULL,'REGULARIZATION','转正审批流程','regularization','转正申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (7,NULL,'RESIGNATION','离职审批流程','resignation','离职申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (8,NULL,'DEPT_CHANGE','部门变更审批流程','dept_change','部门变更申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (9,NULL,'POSITION_CHANGE','职位变更审批流程','position_change','职位变更申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
INSERT INTO `wf_process_definition` VALUES (10,NULL,'REWARD_PUNISHMENT','奖惩审批流程','reward_punishment','奖惩申请审批流程',1,'2025-12-22 21:19:11','2025-12-22 21:19:11',NULL,NULL);
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
INSERT INTO `wf_process_node` VALUES (1,1,'开始',1,1,NULL,NULL,NULL,'2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (2,1,'部门负责人审批',2,2,3,NULL,1,'2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (3,1,'HR审批',2,3,2,NULL,1,'2025-12-22 21:19:11','2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (4,1,'结束',4,4,NULL,NULL,NULL,'2025-12-22 21:19:11','2025-12-22 21:19:11');
/*!40000 ALTER TABLE `wf_process_node` ENABLE KEYS */;
UNLOCK TABLES;

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

-- Dump completed on 2026-09-09 11:55:19
