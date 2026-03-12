/*
 Navicat Premium Data Transfer

 Source Server         : LocalMysql
 Source Server Type    : MySQL
 Source Server Version : 80044
 Source Host           : localhost:3306
 Source Schema         : kadmin

 Target Server Type    : MySQL
 Target Server Version : 80044
 File Encoding         : 65001

 Date: 12/03/2026 10:48:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_business_trip
-- ----------------------------
DROP TABLE IF EXISTS `app_business_trip`;
CREATE TABLE `app_business_trip`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `destination` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出差地点',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `duration` int NOT NULL COMMENT '出差天数',
  `purpose` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '出差目的',
  `companions` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '同行人员',
  `estimated_cost` decimal(12, 2) NULL DEFAULT NULL COMMENT '预计费用',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出差申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_business_trip
-- ----------------------------

-- ----------------------------
-- Table structure for app_card_replacement
-- ----------------------------
DROP TABLE IF EXISTS `app_card_replacement`;
CREATE TABLE `app_card_replacement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `attendance_date` date NOT NULL COMMENT '考勤日期',
  `replacement_type` tinyint NOT NULL COMMENT '补卡类型：1-上班补卡，2-下班补卡',
  `replacement_time` datetime NOT NULL COMMENT '补卡时间',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '补卡原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '补卡申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_card_replacement
-- ----------------------------

-- ----------------------------
-- Table structure for app_comp_leave
-- ----------------------------
DROP TABLE IF EXISTS `app_comp_leave`;
CREATE TABLE `app_comp_leave`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `overtime_id` bigint NULL DEFAULT NULL COMMENT '关联加班记录ID',
  `comp_date` date NOT NULL COMMENT '换休日期',
  `duration` decimal(4, 1) NOT NULL COMMENT '时长（天）',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '换休原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '换休申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_comp_leave
-- ----------------------------

-- ----------------------------
-- Table structure for app_leave
-- ----------------------------
DROP TABLE IF EXISTS `app_leave`;
CREATE TABLE `app_leave`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `leave_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请假类型',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration` decimal(4, 1) NOT NULL COMMENT '时长（天）',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请假原因',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '请假申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_leave
-- ----------------------------

-- ----------------------------
-- Table structure for app_overtime
-- ----------------------------
DROP TABLE IF EXISTS `app_overtime`;
CREATE TABLE `app_overtime`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `overtime_date` date NOT NULL COMMENT '加班日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `duration` decimal(4, 1) NOT NULL COMMENT '时长（小时）',
  `overtime_type` tinyint NULL DEFAULT NULL COMMENT '加班类型：1-工作日，2-周末，3-节假日',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '加班原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-审批中，2-已通过，3-已拒绝，4-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '加班申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_overtime
-- ----------------------------

-- ----------------------------
-- Table structure for att_calendar_rule
-- ----------------------------
DROP TABLE IF EXISTS `att_calendar_rule`;
CREATE TABLE `att_calendar_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID',
  `rule_type` tinyint NULL DEFAULT NULL COMMENT '规则类型',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `rule_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则名称',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company`(`company_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤日历规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_calendar_rule
-- ----------------------------
INSERT INTO `att_calendar_rule` VALUES (1, 11, 2, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for att_clock_record
-- ----------------------------
DROP TABLE IF EXISTS `att_clock_record`;
CREATE TABLE `att_clock_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `clock_time` datetime NULL DEFAULT NULL COMMENT '打卡时间',
  `clock_type` tinyint NULL DEFAULT NULL COMMENT '打卡类型：1-上班，2-下班',
  `clock_method` tinyint NULL DEFAULT NULL COMMENT '打卡方式：1-APP，2-考勤机，3-手动补卡',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '打卡位置',
  `device_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备信息',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_time`(`employee_id` ASC, `clock_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤打卡记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_clock_record
-- ----------------------------
INSERT INTO `att_clock_record` VALUES (2, 2, '2025-12-27 08:55:00', 1, 1, NULL, NULL, '上班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (3, 2, '2025-12-27 18:05:00', 2, 1, NULL, NULL, '下班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (4, 2, '2025-12-26 09:15:00', 1, 1, NULL, NULL, '上班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (5, 2, '2025-12-26 18:00:00', 2, 1, NULL, NULL, '下班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (6, 2, '2025-12-25 08:58:00', 1, 1, NULL, NULL, '上班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (7, 2, '2025-12-25 17:30:00', 2, 1, NULL, NULL, '下班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (8, 2, '2025-12-24 08:50:00', 1, 1, NULL, NULL, '上班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (9, 2, '2025-12-24 18:10:00', 2, 1, NULL, NULL, '下班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (10, 2, '2025-12-29 08:52:00', 1, 1, NULL, NULL, '上班打卡', '2025-12-29 10:40:39');
INSERT INTO `att_clock_record` VALUES (11, 2, '2025-12-29 12:05:00', 2, 3, NULL, NULL, NULL, NULL);
INSERT INTO `att_clock_record` VALUES (12, 1, '2025-12-30 08:00:00', 1, 3, NULL, NULL, NULL, NULL);
INSERT INTO `att_clock_record` VALUES (13, 6, '2026-03-09 00:00:00', 1, 3, NULL, NULL, NULL, NULL);
INSERT INTO `att_clock_record` VALUES (14, 1, '2026-03-10 10:25:59', 1, 1, NULL, NULL, '移动端上班打卡', NULL);
INSERT INTO `att_clock_record` VALUES (15, 1, '2026-03-10 10:26:00', 2, 1, NULL, NULL, '移动端下班打卡', NULL);
INSERT INTO `att_clock_record` VALUES (16, 1, '2026-03-10 10:26:01', 2, 1, NULL, NULL, '移动端下班打卡', NULL);
INSERT INTO `att_clock_record` VALUES (17, 1, '2026-03-10 10:26:03', 2, 1, NULL, NULL, '移动端下班打卡', NULL);

-- ----------------------------
-- Table structure for att_daily_record
-- ----------------------------
DROP TABLE IF EXISTS `att_daily_record`;
CREATE TABLE `att_daily_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `att_date` date NOT NULL COMMENT '考勤日期',
  `shift_id` bigint NULL DEFAULT NULL COMMENT '班次ID',
  `period_id` bigint NULL DEFAULT NULL COMMENT '时段ID',
  `period_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时段名称',
  `scheduled_in` time NULL DEFAULT NULL COMMENT '应上班时间',
  `scheduled_out` time NULL DEFAULT NULL COMMENT '应下班时间',
  `actual_in` time NULL DEFAULT NULL COMMENT '实际上班打卡',
  `actual_out` time NULL DEFAULT NULL COMMENT '实际下班打卡',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-未处理 1-正常 2-迟到 3-早退 4-旷工 5-请假 6-出差 7-迟到+早退',
  `late_minutes` int NULL DEFAULT 0 COMMENT '迟到分钟数',
  `early_minutes` int NULL DEFAULT 0 COMMENT '早退分钟数',
  `work_hours` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '工作时长(小时)',
  `overtime_hours` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '加班时长(小时)',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `locked` tinyint NULL DEFAULT 0 COMMENT '是否锁定 0-否 1-是',
  `locked_by` bigint NULL DEFAULT NULL COMMENT '锁定人',
  `locked_time` datetime NULL DEFAULT NULL COMMENT '锁定时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_date_period`(`employee_id` ASC, `att_date` ASC, `period_id` ASC) USING BTREE,
  INDEX `idx_date`(`att_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '日考勤记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_daily_record
-- ----------------------------
INSERT INTO `att_daily_record` VALUES (1, 2, '2025-12-29', 1, 34, '第一段', '09:00:00', '12:00:00', '08:52:00', '12:05:00', 1, 0, 0, 3.00, 0.00, NULL, 0, 1, '2025-12-29 13:27:58', NULL, NULL);
INSERT INTO `att_daily_record` VALUES (2, 2, '2025-12-29', 1, 35, '第二段', '13:00:00', '18:00:00', '12:05:00', '12:05:00', 3, 0, 355, 0.00, 0.00, NULL, 0, 1, '2025-12-29 13:27:58', NULL, NULL);
INSERT INTO `att_daily_record` VALUES (3, 2, '2025-12-29', 1, 36, '第三段', '19:00:00', '20:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, 1, '2025-12-29 13:27:58', NULL, NULL);
INSERT INTO `att_daily_record` VALUES (4, 1, '2025-12-30', 1, 34, '第一段', '09:00:00', '12:00:00', '08:00:00', NULL, 0, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (5, 1, '2025-12-30', 1, 35, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (6, 1, '2025-12-30', 1, 36, '第三段', '19:00:00', '20:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (7, 1, '2026-01-13', 1, 37, '第一段', '09:00:00', '12:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (8, 1, '2026-01-13', 1, 38, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (9, 5, '2026-01-13', 1, 37, '第一段', '09:00:00', '12:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (10, 5, '2026-01-13', 1, 38, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (11, 1, '2026-01-14', 1, 37, '第一段', '09:00:00', '12:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (12, 1, '2026-01-14', 1, 38, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (13, 5, '2026-01-14', 1, 37, '第一段', '09:00:00', '12:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (14, 5, '2026-01-14', 1, 38, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (15, 1, '2026-03-09', 1, 37, '第一段', '09:00:00', '12:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);
INSERT INTO `att_daily_record` VALUES (16, 1, '2026-03-09', 1, 38, '第二段', '13:00:00', '18:00:00', NULL, NULL, 4, 0, 0, 0.00, 0.00, NULL, 0, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for att_holiday
-- ----------------------------
DROP TABLE IF EXISTS `att_holiday`;
CREATE TABLE `att_holiday`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID，为空表示全局',
  `holiday_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节假日名称',
  `holiday_date` date NOT NULL COMMENT '节假日日期',
  `holiday_type` tinyint NULL DEFAULT NULL COMMENT '类型：1-法定节假日，2-公司假日，3-调休上班',
  `year` int NULL DEFAULT NULL COMMENT '年份',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '考勤节假日表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_holiday
-- ----------------------------

-- ----------------------------
-- Table structure for att_monthly_summary
-- ----------------------------
DROP TABLE IF EXISTS `att_monthly_summary`;
CREATE TABLE `att_monthly_summary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `year` int NOT NULL COMMENT '年份',
  `month` int NOT NULL COMMENT '月份',
  `work_days` int NULL DEFAULT 0 COMMENT '应出勤天数',
  `actual_days` int NULL DEFAULT 0 COMMENT '实际出勤天数',
  `late_count` int NULL DEFAULT 0 COMMENT '迟到次数',
  `early_count` int NULL DEFAULT 0 COMMENT '早退次数',
  `absent_count` int NULL DEFAULT 0 COMMENT '旷工次数',
  `leave_days` decimal(4, 1) NULL DEFAULT 0.0 COMMENT '请假天数',
  `overtime_hours` decimal(6, 2) NULL DEFAULT 0.00 COMMENT '加班时长',
  `business_trip_days` int NULL DEFAULT 0 COMMENT '出差天数',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-未确认，1-已确认',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_year_month`(`employee_id` ASC, `year` ASC, `month` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '月考勤汇总表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_monthly_summary
-- ----------------------------

-- ----------------------------
-- Table structure for att_schedule
-- ----------------------------
DROP TABLE IF EXISTS `att_schedule`;
CREATE TABLE `att_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `shift_id` bigint NULL DEFAULT NULL COMMENT '班次ID',
  `is_rest` tinyint NULL DEFAULT 0 COMMENT '是否休息：0-否，1-是',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_date`(`employee_id` ASC, `schedule_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 263 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_schedule
-- ----------------------------
INSERT INTO `att_schedule` VALUES (2, 1, '2025-12-22', 1, 0, NULL, '2025-12-28 18:03:55', '2025-12-28 18:03:55', NULL, NULL);
INSERT INTO `att_schedule` VALUES (6, 2, '2025-12-29', 1, 0, NULL, '2025-12-28 18:07:18', '2025-12-28 18:07:18', NULL, NULL);
INSERT INTO `att_schedule` VALUES (11, 2, '2025-12-28', 1, 0, NULL, '2025-12-28 20:58:39', '2025-12-28 20:58:39', NULL, NULL);
INSERT INTO `att_schedule` VALUES (12, 1, '2025-12-23', 1, 0, NULL, '2025-12-28 21:01:55', '2025-12-28 21:01:55', NULL, NULL);
INSERT INTO `att_schedule` VALUES (13, 2, '2025-12-23', 1, 0, NULL, '2025-12-28 21:01:56', '2025-12-28 21:01:56', NULL, NULL);
INSERT INTO `att_schedule` VALUES (14, 1, '2025-12-24', 2, 0, NULL, '2025-12-28 21:01:58', '2025-12-28 21:01:58', NULL, NULL);
INSERT INTO `att_schedule` VALUES (16, 1, '2025-12-08', 1, 0, NULL, '2025-12-28 21:02:43', '2025-12-28 21:02:43', NULL, NULL);
INSERT INTO `att_schedule` VALUES (17, 1, '2025-12-09', 1, 0, NULL, '2025-12-28 21:02:44', '2025-12-28 21:02:44', NULL, NULL);
INSERT INTO `att_schedule` VALUES (22, 1, '2025-12-06', 1, 0, NULL, '2025-12-28 21:03:28', '2025-12-28 21:03:28', NULL, NULL);
INSERT INTO `att_schedule` VALUES (23, 1, '2025-12-10', 1, 0, NULL, '2025-12-28 21:04:03', '2025-12-28 21:04:03', NULL, NULL);
INSERT INTO `att_schedule` VALUES (26, 1, '2025-12-11', 1, 0, NULL, '2025-12-28 21:20:49', '2025-12-28 21:20:49', NULL, NULL);
INSERT INTO `att_schedule` VALUES (29, 1, '2025-12-13', 2, 0, NULL, '2025-12-28 21:21:03', '2025-12-28 21:21:03', NULL, NULL);
INSERT INTO `att_schedule` VALUES (30, 1, '2025-12-12', 2, 0, NULL, '2025-12-28 21:21:04', '2025-12-28 21:21:04', NULL, NULL);
INSERT INTO `att_schedule` VALUES (32, 1, '2025-12-14', 1, 0, NULL, '2025-12-28 21:23:46', '2025-12-28 21:23:46', NULL, NULL);
INSERT INTO `att_schedule` VALUES (44, 1, '2025-12-15', 1, 0, NULL, '2025-12-28 21:24:10', '2025-12-28 21:24:10', NULL, NULL);
INSERT INTO `att_schedule` VALUES (45, 1, '2025-12-16', 1, 0, NULL, '2025-12-28 21:24:11', '2025-12-28 21:24:11', NULL, NULL);
INSERT INTO `att_schedule` VALUES (52, 1, '2025-12-17', 1, 0, NULL, '2025-12-30 10:25:58', '2025-12-30 10:25:58', NULL, NULL);
INSERT INTO `att_schedule` VALUES (53, 1, '2025-12-18', 1, 0, NULL, '2025-12-30 10:25:59', '2025-12-30 10:25:59', NULL, NULL);
INSERT INTO `att_schedule` VALUES (54, 1, '2025-12-21', 2, 0, NULL, '2025-12-30 10:26:03', '2025-12-30 10:26:03', NULL, NULL);
INSERT INTO `att_schedule` VALUES (56, 2, '2025-12-30', 1, 0, NULL, '2025-12-30 10:26:23', '2025-12-30 10:26:23', NULL, NULL);
INSERT INTO `att_schedule` VALUES (57, 2, '2025-12-31', 1, 0, NULL, '2025-12-30 10:26:23', '2025-12-30 10:26:23', NULL, NULL);
INSERT INTO `att_schedule` VALUES (58, 2, '2026-01-01', 1, 0, NULL, '2025-12-30 10:26:23', '2025-12-30 10:26:23', NULL, NULL);
INSERT INTO `att_schedule` VALUES (62, 1, '2025-12-04', 1, 0, NULL, '2025-12-30 10:26:39', '2025-12-30 10:26:39', NULL, NULL);
INSERT INTO `att_schedule` VALUES (63, 1, '2025-12-05', 1, 0, NULL, '2025-12-30 10:26:39', '2025-12-30 10:26:39', NULL, NULL);
INSERT INTO `att_schedule` VALUES (64, 1, '2025-12-01', 2, 0, NULL, '2025-12-30 10:26:50', '2025-12-30 10:26:50', NULL, NULL);
INSERT INTO `att_schedule` VALUES (65, 1, '2025-12-02', 2, 0, NULL, '2025-12-30 10:26:50', '2025-12-30 10:26:50', NULL, NULL);
INSERT INTO `att_schedule` VALUES (66, 1, '2025-12-03', 2, 0, NULL, '2025-12-30 10:26:50', '2025-12-30 10:26:50', NULL, NULL);
INSERT INTO `att_schedule` VALUES (67, 1, '2025-12-30', 1, 0, NULL, '2025-12-30 10:29:03', '2025-12-30 10:29:03', NULL, NULL);
INSERT INTO `att_schedule` VALUES (68, 1, '2025-12-29', 1, 0, NULL, '2025-12-30 10:29:04', '2025-12-30 10:29:04', NULL, NULL);
INSERT INTO `att_schedule` VALUES (69, 1, '2025-12-31', 1, 0, NULL, '2025-12-30 10:29:05', '2025-12-30 10:29:05', NULL, NULL);
INSERT INTO `att_schedule` VALUES (179, 5, '2026-01-01', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (180, 5, '2026-01-02', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (181, 5, '2026-01-03', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (182, 5, '2026-01-04', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (183, 5, '2026-01-05', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (184, 5, '2026-01-06', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (185, 5, '2026-01-07', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (186, 5, '2026-01-08', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (187, 5, '2026-01-09', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (188, 5, '2026-01-10', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (189, 5, '2026-01-11', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (190, 5, '2026-01-12', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (191, 5, '2026-01-13', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (192, 5, '2026-01-14', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (210, 1, '2026-01-01', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (211, 1, '2026-01-02', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (212, 1, '2026-01-03', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (213, 1, '2026-01-04', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (214, 1, '2026-01-05', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (215, 1, '2026-01-06', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (216, 1, '2026-01-07', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (217, 1, '2026-01-08', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (218, 1, '2026-01-09', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (219, 1, '2026-01-10', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (220, 1, '2026-01-11', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (221, 1, '2026-01-12', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (222, 1, '2026-01-13', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (223, 1, '2026-01-14', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (224, 1, '2026-01-15', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (225, 1, '2026-01-16', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (226, 1, '2026-01-17', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (227, 1, '2026-01-18', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (228, 1, '2026-01-19', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (229, 1, '2026-01-20', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (230, 1, '2026-01-21', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (231, 1, '2026-01-22', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (232, 1, '2026-01-23', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (233, 1, '2026-01-24', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (234, 1, '2026-01-25', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (235, 1, '2026-01-26', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (236, 1, '2026-01-27', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (237, 1, '2026-01-28', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (238, 1, '2026-01-29', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (239, 1, '2026-01-30', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (240, 1, '2026-01-31', 1, 0, NULL, '2026-01-14 17:17:38', '2026-01-14 17:17:38', NULL, NULL);
INSERT INTO `att_schedule` VALUES (241, 5, '2026-01-15', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (242, 5, '2026-01-16', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (243, 5, '2026-01-17', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (244, 5, '2026-01-18', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (245, 5, '2026-01-19', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (246, 5, '2026-01-20', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (247, 5, '2026-01-21', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (248, 5, '2026-01-22', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (249, 5, '2026-01-23', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (250, 5, '2026-01-24', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (251, 5, '2026-01-25', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (252, 5, '2026-01-26', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (253, 5, '2026-01-27', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (254, 5, '2026-01-28', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (255, 5, '2026-01-29', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (256, 5, '2026-01-30', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (257, 5, '2026-01-31', 2, 0, NULL, '2026-01-14 17:17:57', '2026-01-14 17:17:57', NULL, NULL);
INSERT INTO `att_schedule` VALUES (261, 1, '2026-03-14', 2, 0, NULL, '2026-03-10 09:21:02', '2026-03-10 09:21:41', NULL, NULL);
INSERT INTO `att_schedule` VALUES (262, 1, '2026-03-09', 1, 0, NULL, '2026-03-10 09:21:13', '2026-03-10 09:21:41', NULL, NULL);

-- ----------------------------
-- Table structure for att_shift
-- ----------------------------
DROP TABLE IF EXISTS `att_shift`;
CREATE TABLE `att_shift`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '班次ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID',
  `shift_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次编码',
  `shift_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '班次名称',
  `work_start_time` time NULL DEFAULT NULL COMMENT '上班时间',
  `work_end_time` time NULL DEFAULT NULL COMMENT '下班时间',
  `late_minutes` int NULL DEFAULT 0 COMMENT '迟到容许分钟',
  `early_minutes` int NULL DEFAULT 0 COMMENT '早退容许分钟',
  `work_hours` decimal(4, 2) NULL DEFAULT NULL COMMENT '工作时长',
  `is_next_day` tinyint NULL DEFAULT 0 COMMENT '是否跨天：0-否，1-是',
  `rest_start_time` time NULL DEFAULT NULL COMMENT '休息开始时间',
  `rest_end_time` time NULL DEFAULT NULL COMMENT '休息结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_company_shift_code`(`company_id` ASC, `shift_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_shift
-- ----------------------------
INSERT INTO `att_shift` VALUES (1, NULL, '01', '白班', '09:00:00', '18:00:00', 0, 0, 8.00, 0, NULL, NULL, 1, NULL, '2025-12-28 17:18:24', '2025-12-28 17:18:24', NULL, NULL);
INSERT INTO `att_shift` VALUES (2, NULL, '002', '晚班', '09:00:00', '18:00:00', 0, 0, 8.00, 0, NULL, NULL, 1, NULL, '2025-12-28 21:01:47', '2025-12-28 21:01:47', NULL, NULL);

-- ----------------------------
-- Table structure for att_shift_period
-- ----------------------------
DROP TABLE IF EXISTS `att_shift_period`;
CREATE TABLE `att_shift_period`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `shift_id` bigint NULL DEFAULT NULL COMMENT '班次ID',
  `period_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时段名称',
  `start_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结束时间',
  `cross_day` tinyint NULL DEFAULT NULL COMMENT '是否跨天：0-否，1-是',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `need_clock_in` tinyint NULL DEFAULT 1 COMMENT '上班是否打卡 0-否 1-是',
  `need_clock_out` tinyint NULL DEFAULT 1 COMMENT '下班是否打卡 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shift_id`(`shift_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '班次时段表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of att_shift_period
-- ----------------------------
INSERT INTO `att_shift_period` VALUES (32, 2, '上午', '09:00', '12:00', 0, 1, 1, 1);
INSERT INTO `att_shift_period` VALUES (33, 2, '下午', '13:00', '18:00', 0, 2, 1, 1);
INSERT INTO `att_shift_period` VALUES (37, 1, '第一段', '09:00', '12:00', 0, 1, 1, 1);
INSERT INTO `att_shift_period` VALUES (38, 1, '第二段', '13:00', '18:00', 0, 2, 1, 1);

-- ----------------------------
-- Table structure for hr_application
-- ----------------------------
DROP TABLE IF EXISTS `hr_application`;
CREATE TABLE `hr_application`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `app_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请类型',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration` decimal(10, 2) NULL DEFAULT NULL COMMENT '时长',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `regular_date` date NULL DEFAULT NULL COMMENT '转正日期',
  `probation_end_date` date NULL DEFAULT NULL COMMENT '试用期结束日期',
  `evaluation` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '试用期评价',
  `new_employee_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转正后员工类别',
  `transfer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调动类型',
  `from_company_id` bigint NULL DEFAULT NULL COMMENT '原公司ID',
  `to_company_id` bigint NULL DEFAULT NULL COMMENT '新公司ID',
  `from_dept_id` bigint NULL DEFAULT NULL COMMENT '原部门ID',
  `to_dept_id` bigint NULL DEFAULT NULL COMMENT '新部门ID',
  `from_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原职位',
  `to_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新职位',
  `effect_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `reward_type` int NULL DEFAULT NULL COMMENT '奖惩类型',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '奖惩类别',
  `amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '金额',
  `resign_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离职类型',
  `last_work_date` date NULL DEFAULT NULL COMMENT '最后工作日',
  `handover_to` bigint NULL DEFAULT NULL COMMENT '工作交接人ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_app_type`(`app_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人力资源申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_application
-- ----------------------------
INSERT INTO `hr_application` VALUES (1, 1, 'leave', '1', '2025-12-28 00:00:00', '2025-12-29 00:00:00', 1.50, '', 1, 1, '2025-12-28 16:58:52', '', NULL, 1, '2025-12-28 16:57:50', 2, '2025-12-28 16:58:52', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (2, 2, 'makeup', 'checkin', '2025-12-29 00:00:00', NULL, NULL, '11', 1, 1, '2025-12-29 10:15:22', '', NULL, 1, '2025-12-29 10:15:17', 1, '2025-12-29 10:15:22', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (3, 2, 'leave', '1', '2025-12-29 00:00:00', '2025-12-24 00:00:00', 1.00, '', 1, 1, '2026-01-06 13:59:16', '', NULL, 1, '2025-12-29 15:21:52', 2, '2026-01-06 13:59:16', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (4, 1, 'regularization', NULL, NULL, NULL, NULL, '', 1, 1, '2025-12-29 15:37:30', '', NULL, 1, '2025-12-29 15:37:05', 1, '2025-12-29 15:37:30', '2025-12-29', '2025-12-29', '', 'intern', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (5, 1, 'overtime', '', '2025-12-29 00:00:00', '2025-12-30 00:00:00', 2.00, '55', 1, 1, '2026-01-06 13:59:18', '', NULL, 1, '2025-12-29 16:20:07', 2, '2026-01-06 13:59:18', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (6, 1, 'makeup', 'checkin', '2025-12-22 00:00:00', NULL, NULL, '55', 1, 1, '2026-01-06 13:59:19', '', NULL, 1, '2025-12-29 16:20:17', 2, '2026-01-06 13:59:19', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (7, 1, 'exchange', '', '2025-12-29 00:00:00', '2025-12-31 00:00:00', 1.00, '', 1, 1, '2026-01-06 13:59:20', '', NULL, 1, '2025-12-29 16:20:31', 2, '2026-01-06 13:59:20', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (8, 1, 'business', '45', '2025-12-29 00:00:00', '2025-12-30 00:00:00', 1.00, '', 1, 1, '2026-01-06 13:59:22', '', NULL, 1, '2025-12-29 16:20:45', 2, '2026-01-06 13:59:22', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (9, 1, 'regularization', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 13:59:27', '', NULL, 1, '2026-01-06 13:46:33', 2, '2026-01-06 13:59:27', '2026-01-06', '2026-01-06', '', 'regular', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (10, 1, 'regularization', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 14:22:15', '', NULL, 1, '2026-01-06 14:14:40', 2, '2026-01-06 14:22:15', '2026-01-06', '2026-01-06', '', 'probation', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (11, 4, 'regularization', NULL, NULL, NULL, NULL, '', 3, NULL, NULL, NULL, NULL, 1, '2026-01-06 15:10:06', 1, '2026-01-06 15:11:50', '2026-01-06', NULL, '', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (12, 4, 'regularization', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 15:12:49', '', NULL, 1, '2026-01-06 15:12:43', 1, '2026-01-06 15:12:49', '2026-01-06', NULL, '', 'intern', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (13, 1, 'transfer', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 15:16:30', '', NULL, 1, '2026-01-06 15:15:53', 1, '2026-01-06 15:16:30', NULL, NULL, NULL, NULL, '3', NULL, NULL, 12, 17, 'senior_engineer', 'gm', '2026-01-06', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (14, 1, 'transfer', NULL, NULL, NULL, NULL, '', 3, NULL, NULL, NULL, NULL, 1, '2026-01-06 15:24:32', 1, '2026-01-06 16:31:25', NULL, NULL, NULL, NULL, '3', NULL, 11, 17, 12, 'gm', 'senior_engineer', '2026-01-06', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (15, 1, 'transfer', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 16:35:28', '', NULL, 1, '2026-01-06 16:31:35', 1, '2026-01-06 16:35:28', NULL, NULL, NULL, NULL, '1', 16, 11, 17, 12, 'gm', '', '2026-01-06', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (16, 4, 'reward', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 16:37:07', '', NULL, 1, '2026-01-06 16:34:22', 1, '2026-01-06 16:37:07', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-06', 1, '1', 5.00, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (17, 1, 'punish', NULL, NULL, NULL, NULL, '', 3, NULL, NULL, NULL, NULL, 1, '2026-01-06 16:35:08', 1, '2026-01-06 16:37:12', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-06', 2, '2', 50.00, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (18, 4, 'resignation', NULL, NULL, NULL, NULL, '', 1, 1, '2026-01-06 16:51:40', '', NULL, 1, '2026-01-06 16:40:20', 1, '2026-01-06 16:51:40', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', '2026-01-06', 1);
INSERT INTO `hr_application` VALUES (19, 5, 'overtime', '', '2026-01-06 00:00:00', '2026-01-14 00:00:00', 1.00, '11', 1, 1, '2026-01-13 16:21:28', '', NULL, 1, '2026-01-06 17:45:47', 1, '2026-01-13 16:21:28', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (20, 1, 'overtime', '', '2026-01-06 00:00:00', '2026-01-14 00:00:00', 1.00, '11', 1, 1, '2026-01-13 16:21:29', '', NULL, 1, '2026-01-06 17:45:47', 1, '2026-01-13 16:21:29', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (21, 5, 'leave', '1', '2026-01-06 00:00:00', '2026-01-07 00:00:00', 1.00, '', 1, 1, '2026-01-06 17:52:30', '', NULL, 1, '2026-01-06 17:51:55', 1, '2026-01-06 17:52:30', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (22, 5, 'leave', '事假申请', '2026-01-12 21:28:38', '2026-01-12 21:28:40', 0.00, '111', 1, 1, '2026-01-13 16:21:26', '', NULL, 5, '2026-01-12 21:28:42', 1, '2026-01-13 16:21:26', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '事假', NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (23, 1, 'overtime', '', '2026-01-13 00:00:00', '2026-01-14 00:00:00', 2.00, '', 1, 1, '2026-01-13 16:21:25', '', NULL, 1, '2026-01-13 09:48:43', 1, '2026-01-13 16:21:25', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (24, 5, 'makeup', 'checkout', '2026-01-13 00:00:00', NULL, NULL, '', 1, 1, '2026-01-13 16:21:22', '', NULL, 1, '2026-01-13 09:50:13', 1, '2026-01-13 16:21:22', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (25, 1, 'exchange', '', '2026-01-13 00:00:00', '2026-01-06 00:00:00', 1.00, '', 1, 1, '2026-01-13 16:21:21', '', NULL, 1, '2026-01-13 09:50:28', 1, '2026-01-13 16:21:21', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (26, 5, 'business', '11', '2026-01-13 00:00:00', '2026-01-14 00:00:00', 1.50, '', 1, 1, '2026-01-13 16:21:17', '', NULL, 1, '2026-01-13 09:50:43', 1, '2026-01-13 16:21:17', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (27, 5, 'makeup', 'checkin', '2026-01-13 00:00:00', NULL, NULL, '', 1, 1, '2026-01-13 16:21:15', '', NULL, 1, '2026-01-13 15:57:19', 1, '2026-01-13 16:21:15', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (28, 5, 'exchange', '', '2026-01-13 00:00:00', '2026-01-14 00:00:00', NULL, '', 1, 1, '2026-01-13 16:21:14', '', NULL, 1, '2026-01-13 16:06:47', 1, '2026-01-13 16:21:14', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (29, 5, 'business', '111', '2026-01-13 09:00:00', '2026-01-13 17:00:00', 7.00, '11', 1, 1, '2026-01-13 16:21:12', '', NULL, 1, '2026-01-13 16:13:59', 1, '2026-01-13 16:21:12', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (30, 1, 'exchange', '', '2026-03-09 00:00:00', '2026-03-14 00:00:00', NULL, '111', 1, 1, '2026-03-10 09:01:26', '', NULL, 1, '2026-03-10 09:01:19', 1, '2026-03-10 09:01:26', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (31, 1, 'exchange', '', '2026-03-09 00:00:00', '2026-03-14 00:00:00', NULL, '222', 1, 1, '2026-03-10 09:20:03', '', NULL, 1, '2026-03-10 09:19:59', 1, '2026-03-10 09:20:03', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (32, 1, 'exchange', '', '2026-03-09 00:00:00', '2026-03-14 00:00:00', NULL, '333', 1, 1, '2026-03-10 09:21:02', '', NULL, 1, '2026-03-10 09:20:53', 1, '2026-03-10 09:21:02', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (33, 1, 'exchange', '', '2026-03-09 00:00:00', '2026-03-14 00:00:00', NULL, '888', 1, 1, '2026-03-10 09:21:41', '', NULL, 1, '2026-03-10 09:21:37', 1, '2026-03-10 09:21:41', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (34, 1, 'leave', '事假', '2026-03-11 15:07:00', '2026-03-12 15:07:00', 0.00, '11', 1, 1, '2026-03-11 16:41:10', NULL, NULL, 1, '2026-03-11 15:13:12', 1, '2026-03-11 16:41:10', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (35, 1, 'regularization', '转正申请', NULL, NULL, NULL, '', 1, 1, '2026-03-11 16:40:55', NULL, NULL, 1, '2026-03-11 15:18:25', 1, '2026-03-11 16:40:55', '2026-03-11', '2026-03-25', '111', 'formal', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `hr_application` VALUES (36, 5, 'regularization', NULL, NULL, NULL, NULL, '', 0, NULL, NULL, NULL, NULL, 8, '2026-03-11 16:54:55', 8, '2026-03-11 16:54:55', '2026-03-11', NULL, '', 'regular', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for hr_certificate
-- ----------------------------
DROP TABLE IF EXISTS `hr_certificate`;
CREATE TABLE `hr_certificate`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `cert_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书名称',
  `cert_photo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书照片',
  `cert_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书类型',
  `cert_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证书等级',
  `issue_date` date NULL DEFAULT NULL COMMENT '发证日期',
  `expire_date` date NULL DEFAULT NULL COMMENT '过期日期',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工证书表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_certificate
-- ----------------------------
INSERT INTO `hr_certificate` VALUES (26, 1, '12', '/cert_photo/001_1773025856462.png', 'skill', 'advanced', '2025-12-05', '2025-12-19', NULL, NULL);

-- ----------------------------
-- Table structure for hr_contract
-- ----------------------------
DROP TABLE IF EXISTS `hr_contract`;
CREATE TABLE `hr_contract`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '合同ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `contract_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '合同编号',
  `contract_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '合同类型：字典值 contract_type',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `probation_months` int NULL DEFAULT NULL COMMENT '试用期（月）',
  `salary` decimal(12, 2) NULL DEFAULT NULL COMMENT '合同薪资',
  `sign_date` date NULL DEFAULT NULL COMMENT '签订日期',
  `sign_company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签订公司',
  `status` int NULL DEFAULT 1 COMMENT '状态：1-生效中，2-即将到期，3-已到期，4-已终止',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `contract_images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '合同图片（多张，逗号分隔）',
  `contract_count` int NULL DEFAULT NULL COMMENT '合同次数（第几次合同）',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '劳动合同表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_contract
-- ----------------------------
INSERT INTO `hr_contract` VALUES (9, 1, '005', '1', '2026-01-21', '2026-01-24', NULL, NULL, NULL, NULL, 3, NULL, '', '/contract_photo/001_1773023642394.png', 2, '2026-01-21 21:45:53', '2026-01-21 21:45:53', 1, 1);

-- ----------------------------
-- Table structure for hr_dept_change
-- ----------------------------
DROP TABLE IF EXISTS `hr_dept_change`;
CREATE TABLE `hr_dept_change`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `old_company_id` bigint NULL DEFAULT NULL COMMENT '原公司ID',
  `new_company_id` bigint NULL DEFAULT NULL COMMENT '新公司ID',
  `old_dept_id` bigint NULL DEFAULT NULL COMMENT '原部门ID',
  `new_dept_id` bigint NULL DEFAULT NULL COMMENT '新部门ID',
  `change_date` date NOT NULL COMMENT '变更日期',
  `change_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门变更记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_dept_change
-- ----------------------------

-- ----------------------------
-- Table structure for hr_education
-- ----------------------------
DROP TABLE IF EXISTS `hr_education`;
CREATE TABLE `hr_education`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `school_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学校名称',
  `diploma_photo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历证书照片',
  `is_full_time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否全日制',
  `education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业',
  `start_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开学时间',
  `end_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '毕业时间',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教育经历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_education
-- ----------------------------
INSERT INTO `hr_education` VALUES (30, 1, '4545', '/uploads/images/2025/12/25/6440ed2f586d4d96bc1b1c2a20b617d7.jpg', '', '', '4545', NULL, NULL, NULL, NULL);
INSERT INTO `hr_education` VALUES (32, 6, '11', '/diploma_photo/003_1773026116806.jpg', 'yes', 'junior_high', '11', '2026-03', '2026-03', NULL, NULL);

-- ----------------------------
-- Table structure for hr_employee
-- ----------------------------
DROP TABLE IF EXISTS `hr_employee`;
CREATE TABLE `hr_employee`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录密码',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `id_card_front` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证正面',
  `id_card_back` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证背面',
  `gender` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '性别',
  `highest_education` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最高学历',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `nation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '民族',
  `id_card` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `employee_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '员工类别',
  `entry_date` date NULL DEFAULT NULL COMMENT '入职日期',
  `regular_date` date NULL DEFAULT NULL COMMENT '转正日期',
  `duty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职务',
  `position` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `post` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位',
  `job_function` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职系',
  `job_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职级',
  `job_responsibility` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职类',
  `job_authority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职等',
  `job_title` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `occupation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职业',
  `marital_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '婚姻状况',
  `political_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `native_place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '籍贯',
  `police_station` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '派出所',
  `registered_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '户籍地址',
  `home_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `current_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '现居住址',
  `emergency_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '与本人关系',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系电话',
  `leave_date` date NULL DEFAULT NULL COMMENT '离职日期',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_no`(`employee_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_employee
-- ----------------------------
INSERT INTO `hr_employee` VALUES (1, '001', '赵梓旭', NULL, '/employee_photo/001.jpg', '/id_card_front/001.jpg', '/id_card_back/001.jpg', '1', 'bachelor', 12, 'han', '859865464', '2025-12-18', '156251645', '156251645', 'formal', '2025-12-25', '2026-03-11', 'dm', 'gm', NULL, NULL, 'p3', NULL, NULL, NULL, NULL, 'single', 'league_member', NULL, NULL, NULL, NULL, '212', NULL, NULL, NULL, NULL, 1, 1, '2025-12-25 18:23:28', 1, '2026-03-11 16:40:55');
INSERT INTO `hr_employee` VALUES (2, '002', '小明', NULL, '', '', '', '1', 'college', 2, 'han', '454454545', '2025-12-29', '156251645', '156251645', 'regular', '2025-12-17', '2025-12-29', 'dgm', 'gm', NULL, NULL, 'p3', NULL, NULL, NULL, NULL, 'single', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-12-29', 2, 1, '2025-12-28 15:56:19', 1, '2025-12-29 15:16:04');
INSERT INTO `hr_employee` VALUES (4, 'test1', 'test1', NULL, '', '', '', '', NULL, 12, NULL, NULL, NULL, '', '', 'intern', NULL, '2026-01-06', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-01-06', 2, 1, '2026-01-06 09:38:43', 1, '2026-01-06 16:51:40');
INSERT INTO `hr_employee` VALUES (5, 'test2', 'test2', NULL, '', '', '', '', 'college', 13, NULL, NULL, '2005-03-10', '', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, '2026-01-06 16:52:55', 8, '2026-03-11 13:23:00');
INSERT INTO `hr_employee` VALUES (6, '003', '003', '1234567', '/employee_photo/003.jpg', '/id_card_front/003.png', '/id_card_back/003.jpg', '', 'junior_high', 12, NULL, NULL, '2026-03-02', '', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, 1, '2026-01-24 22:41:42', 8, '2026-03-11 13:22:53');

-- ----------------------------
-- Table structure for hr_employee_extra
-- ----------------------------
DROP TABLE IF EXISTS `hr_employee_extra`;
CREATE TABLE `hr_employee_extra`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `field_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段编码（对应字典值的dictValue）',
  `field_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段值',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_field`(`employee_id` ASC, `field_code` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工扩展信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_employee_extra
-- ----------------------------

-- ----------------------------
-- Table structure for hr_family_member
-- ----------------------------
DROP TABLE IF EXISTS `hr_family_member`;
CREATE TABLE `hr_family_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `relation` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '与本人关系',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生日期',
  `political_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `work_unit` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作单位',
  `occupation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职业',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_family_member
-- ----------------------------
INSERT INTO `hr_family_member` VALUES (28, 1, '12', 'mother', '2025-12-25', 'masses', '12', '', '12', NULL, NULL);

-- ----------------------------
-- Table structure for hr_mobile_approver
-- ----------------------------
DROP TABLE IF EXISTS `hr_mobile_approver`;
CREATE TABLE `hr_mobile_approver`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `app_types` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可审批的申请类型，逗号分隔，空表示全部',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint NULL DEFAULT NULL,
  `updated_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '移动端审批权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_mobile_approver
-- ----------------------------
INSERT INTO `hr_mobile_approver` VALUES (1, 1, 'leave', '2026-03-11 17:28:31', '2026-03-11 17:28:31', 1, 1);

-- ----------------------------
-- Table structure for hr_position_change
-- ----------------------------
DROP TABLE IF EXISTS `hr_position_change`;
CREATE TABLE `hr_position_change`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `old_position_id` bigint NULL DEFAULT NULL COMMENT '原职位ID',
  `new_position_id` bigint NULL DEFAULT NULL COMMENT '新职位ID',
  `change_type` tinyint NULL DEFAULT NULL COMMENT '变更类型：1-晋升，2-降级，3-平调',
  `change_date` date NOT NULL COMMENT '变更日期',
  `change_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更原因',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '职位变更记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_position_change
-- ----------------------------

-- ----------------------------
-- Table structure for hr_regularization
-- ----------------------------
DROP TABLE IF EXISTS `hr_regularization`;
CREATE TABLE `hr_regularization`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `regular_date` date NULL DEFAULT NULL COMMENT '转正日期',
  `probation_end_date` date NULL DEFAULT NULL COMMENT '试用期结束日期',
  `evaluation` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价',
  `new_employee_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转正后员工类别',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '转正表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_regularization
-- ----------------------------
INSERT INTO `hr_regularization` VALUES (4, 2, '2025-12-29', '2025-12-29', '2025-12-29', '', 'regular', 1, 1, '2025-12-29 14:03:56', NULL, '', 1, '2025-12-29 14:03:31', 1, '2025-12-29 14:03:56');

-- ----------------------------
-- Table structure for hr_resignation
-- ----------------------------
DROP TABLE IF EXISTS `hr_resignation`;
CREATE TABLE `hr_resignation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `resign_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '离职类型',
  `apply_date` date NULL DEFAULT NULL COMMENT '申请日期',
  `last_work_date` date NULL DEFAULT NULL COMMENT '最后工作日',
  `handover_to` bigint NULL DEFAULT NULL COMMENT '工作交接人ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '离职表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_resignation
-- ----------------------------
INSERT INTO `hr_resignation` VALUES (3, 2, '1', '2025-12-29', '2025-12-29', NULL, '555', 1, 1, '2025-12-29 15:16:04', NULL, '', 1, '2025-12-29 15:16:02', 1, '2025-12-29 15:16:04');

-- ----------------------------
-- Table structure for hr_reward_punish
-- ----------------------------
DROP TABLE IF EXISTS `hr_reward_punish`;
CREATE TABLE `hr_reward_punish`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `type` int NULL DEFAULT NULL COMMENT '类型',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类别',
  `amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '金额',
  `effect_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '奖惩表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_reward_punish
-- ----------------------------

-- ----------------------------
-- Table structure for hr_reward_punishment
-- ----------------------------
DROP TABLE IF EXISTS `hr_reward_punishment`;
CREATE TABLE `hr_reward_punishment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `type` tinyint NOT NULL COMMENT '类型：1-奖励，2-惩罚',
  `level` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '级别',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `amount` decimal(12, 2) NULL DEFAULT NULL COMMENT '金额',
  `effective_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审批，1-已通过，2-已拒绝',
  `attachment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '奖惩记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_reward_punishment
-- ----------------------------

-- ----------------------------
-- Table structure for hr_transfer
-- ----------------------------
DROP TABLE IF EXISTS `hr_transfer`;
CREATE TABLE `hr_transfer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `transfer_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调动类型',
  `from_company_id` bigint NULL DEFAULT NULL COMMENT '原公司ID',
  `to_company_id` bigint NULL DEFAULT NULL COMMENT '新公司ID',
  `from_dept_id` bigint NULL DEFAULT NULL COMMENT '原部门ID',
  `to_dept_id` bigint NULL DEFAULT NULL COMMENT '新部门ID',
  `from_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原职位',
  `to_position` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新职位',
  `effect_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `approve_by` bigint NULL DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '调动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_transfer
-- ----------------------------
INSERT INTO `hr_transfer` VALUES (7, 2, '3', 1, 2, 1, 2, 'cto', 'gm', '2025-12-29', '111', 1, 1, '2025-12-29 14:59:40', NULL, '', 1, '2025-12-29 14:59:38', 1, '2025-12-29 14:59:40');

-- ----------------------------
-- Table structure for hr_work_experience
-- ----------------------------
DROP TABLE IF EXISTS `hr_work_experience`;
CREATE TABLE `hr_work_experience`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `employee_id` bigint NULL DEFAULT NULL COMMENT '员工ID',
  `company_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司名称',
  `company_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司地址',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职位',
  `witness` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证明人',
  `witness_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '证明人电话',
  `start_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结束日期',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 55 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作经历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of hr_work_experience
-- ----------------------------
INSERT INTO `hr_work_experience` VALUES (53, 1, '121', '', '12', '', '', '12', NULL, NULL, NULL, NULL);
INSERT INTO `hr_work_experience` VALUES (54, 1, '12', '', '', '', '12', '', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for org_unit
-- ----------------------------
DROP TABLE IF EXISTS `org_unit`;
CREATE TABLE `org_unit`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父级ID，0表示顶级',
  `unit_type` int NOT NULL COMMENT '类型：1-集团，2-公司，3-部门',
  `unit_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编码',
  `unit_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简称',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '负责人ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_unit_type`(`unit_type` ASC) USING BTREE,
  INDEX `idx_unit_code`(`unit_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '组织单元表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of org_unit
-- ----------------------------
INSERT INTO `org_unit` VALUES (10, 0, 1, 'K001', '集团公司', '', NULL, '', '', '', '', 0, 1, '2026-01-05 14:42:44', 1, '2026-01-05 14:42:44');
INSERT INTO `org_unit` VALUES (11, 10, 2, 'G001', '公司一', '', NULL, '', '', '', '', 0, 1, '2026-01-05 14:43:53', 1, '2026-01-05 14:43:53');
INSERT INTO `org_unit` VALUES (12, 11, 3, 'B001', '部门一', '', NULL, '', '', '', '', 0, 1, '2026-01-05 14:44:16', 1, '2026-01-05 14:44:16');
INSERT INTO `org_unit` VALUES (13, 11, 3, 'B002', '部门二', '', NULL, '', '', '', '', 0, 1, '2026-01-05 14:44:30', 1, '2026-01-06 09:45:04');
INSERT INTO `org_unit` VALUES (16, 10, 2, 'G002', '公司二', '', NULL, '', '', '', '', 0, 1, '2026-01-06 09:45:23', 1, '2026-01-06 09:45:23');
INSERT INTO `org_unit` VALUES (17, 16, 3, 'B003', '部门三', '', NULL, '', '', '', '', 0, 1, '2026-01-06 09:45:54', 1, '2026-01-06 09:45:54');
INSERT INTO `org_unit` VALUES (18, 16, 3, 'B004', '部门四', '', NULL, '', '', '', '', 0, 1, '2026-01-06 09:46:05', 1, '2026-01-06 09:46:05');

-- ----------------------------
-- Table structure for sys_approval_flow
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_flow`;
CREATE TABLE `sys_approval_flow`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flow_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程编码',
  `flow_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程名称',
  `flow_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '流程类型',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `status` int NULL DEFAULT NULL COMMENT '状态',
  `auto_pass` tinyint NULL DEFAULT NULL COMMENT '是否自动通过：0-否，1-是',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `flow_code`(`flow_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批流程定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_approval_flow
-- ----------------------------
INSERT INTO `sys_approval_flow` VALUES (7, 'Z001', '转正管理', 'regularization', '', 1, 0, 1, '2026-01-06 14:14:26', 1, '2026-01-06 14:21:52');
INSERT INTO `sys_approval_flow` VALUES (8, 'D001', '调动申请', 'transfer', '', 1, 0, 1, '2026-01-06 15:16:24', 1, '2026-01-06 15:16:24');
INSERT INTO `sys_approval_flow` VALUES (9, 'J001', '奖惩申请', 'reward', '', 1, 0, 1, '2026-01-06 16:37:02', 1, '2026-01-06 16:37:02');
INSERT INTO `sys_approval_flow` VALUES (10, 'L001', '离职申请', 'resignation', '', 1, 0, 1, '2026-01-06 16:51:35', 1, '2026-01-06 16:51:35');
INSERT INTO `sys_approval_flow` VALUES (11, 'Q001', '请假申请', 'leave', '', 1, 0, 1, '2026-01-06 17:52:21', 1, '2026-01-06 17:52:21');
INSERT INTO `sys_approval_flow` VALUES (14, 'jb001', '加班申请', 'overtime', '', 1, 0, 1, '2026-01-13 09:48:28', 1, '2026-01-13 09:48:28');
INSERT INTO `sys_approval_flow` VALUES (15, 'bk001', '补卡申请', 'makeup', '', 1, 0, 1, '2026-01-13 09:49:15', 1, '2026-01-13 09:49:15');
INSERT INTO `sys_approval_flow` VALUES (16, 'hx', '换休申请', 'exchange', '', 1, 0, 1, '2026-01-13 09:49:40', 1, '2026-01-13 09:49:40');
INSERT INTO `sys_approval_flow` VALUES (17, 'cc001', '出差申请', 'business', '', 1, 0, 1, '2026-01-13 09:50:00', 1, '2026-01-13 09:50:00');

-- ----------------------------
-- Table structure for sys_approval_node
-- ----------------------------
DROP TABLE IF EXISTS `sys_approval_node`;
CREATE TABLE `sys_approval_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `flow_id` bigint NULL DEFAULT NULL COMMENT '流程ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点名称',
  `node_type` int NULL DEFAULT NULL COMMENT '节点类型',
  `approver_type` int NULL DEFAULT NULL COMMENT '审批人类型',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flow_id`(`flow_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_approval_node
-- ----------------------------
INSERT INTO `sys_approval_node` VALUES (3, 2, '部门负责人审批', 1, 2, NULL, 1, NULL, '2025-12-28 16:49:43', NULL, '2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (4, 3, '部门负责人审批', 1, 2, NULL, 1, NULL, '2025-12-28 16:49:43', NULL, '2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (5, 3, '总经理审批', 1, 3, NULL, 2, NULL, '2025-12-28 16:49:43', NULL, '2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (6, 4, '部门负责人审批', 1, 2, NULL, 1, NULL, '2025-12-28 16:49:43', NULL, '2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (7, 5, '部门负责人审批', 1, 2, NULL, 1, NULL, '2025-12-28 16:49:43', NULL, '2025-12-28 16:49:43');
INSERT INTO `sys_approval_node` VALUES (8, 1, '指定人员', 1, 1, NULL, 1, 1, '2025-12-28 16:57:30', 1, '2026-01-06 14:05:19');
INSERT INTO `sys_approval_node` VALUES (9, 6, '001', 1, 1, NULL, 1, 1, '2026-01-06 13:57:53', 1, '2026-01-06 14:05:19');
INSERT INTO `sys_approval_node` VALUES (12, 7, '审批', 1, 1, 1, 1, 1, '2026-01-06 14:14:26', 1, '2026-01-06 14:14:26');
INSERT INTO `sys_approval_node` VALUES (13, 8, '申请', 1, 1, 1, 1, 1, '2026-01-06 15:16:24', 1, '2026-01-06 15:16:24');
INSERT INTO `sys_approval_node` VALUES (14, 9, '审批', 1, 1, 1, 1, 1, '2026-01-06 16:37:02', 1, '2026-01-06 16:37:02');
INSERT INTO `sys_approval_node` VALUES (15, 10, '审批', 1, 1, 1, 1, 1, '2026-01-06 16:51:35', 1, '2026-01-06 16:51:35');
INSERT INTO `sys_approval_node` VALUES (16, 11, '审批', 1, 1, 1, 1, 1, '2026-01-06 17:52:21', 1, '2026-01-06 17:52:21');
INSERT INTO `sys_approval_node` VALUES (17, 14, '申请', 1, 1, 1, 1, 1, '2026-01-13 09:48:28', 1, '2026-01-13 09:48:28');
INSERT INTO `sys_approval_node` VALUES (18, 15, '补卡申请', 1, 1, 1, 1, 1, '2026-01-13 09:49:15', 1, '2026-01-13 09:49:15');
INSERT INTO `sys_approval_node` VALUES (19, 16, '换休申请', 1, 1, 1, 1, 1, '2026-01-13 09:49:40', 1, '2026-01-13 09:49:40');
INSERT INTO `sys_approval_node` VALUES (20, 17, '出差申请', 1, 1, 1, 1, 1, '2026-01-13 09:50:00', 1, '2026-01-13 09:50:00');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典数据ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签（默认语言）',
  `dict_label_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典标签(英文)',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典值',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CSS样式',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '列表样式',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 185 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (45, 1, '男', 'Male', '1', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (46, 1, '女', 'Female', '2', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (47, 1, '未知', 'Unknown', '0', NULL, NULL, 3, 1, 0, '2025-12-25 14:42:50', '2025-12-25 21:33:37', NULL, 1);
INSERT INTO `sys_dict_data` VALUES (48, 2, '试用', 'Probation', '1', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (49, 2, '正式', 'Regular', '2', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (50, 2, '离职', 'Resigned', '3', NULL, NULL, 3, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (51, 3, '固定期限', 'Fixed Term', '1', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (52, 3, '无固定期限', 'Open-ended', '2', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (55, 4, '高中', 'High School', 'high_school', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 22:09:57', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (56, 4, '大专', 'College', 'college', NULL, NULL, 4, 1, 0, '2025-12-25 14:42:50', '2025-12-25 22:09:57', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (57, 4, '本科', 'Bachelor', 'bachelor', NULL, NULL, 5, 1, 0, '2025-12-25 14:42:50', '2025-12-25 22:09:57', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (58, 4, '硕士', 'Master', 'master', NULL, NULL, 6, 1, 0, '2025-12-25 14:42:50', '2025-12-25 22:09:57', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (59, 4, '博士', 'Doctor', 'doctor', NULL, NULL, 7, 1, 0, '2025-12-25 14:42:50', '2025-12-25 22:09:57', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (60, 5, '年假', 'Annual Leave', '1', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (61, 5, '事假', 'Personal Leave', '2', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (62, 5, '病假', 'Sick Leave', '3', NULL, NULL, 3, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (63, 5, '婚假', 'Marriage Leave', '4', NULL, NULL, 4, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (64, 5, '产假', 'Maternity Leave', '5', NULL, NULL, 5, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (65, 5, '陪产假', 'Paternity Leave', '6', NULL, NULL, 6, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (66, 5, '丧假', 'Bereavement Leave', '7', NULL, NULL, 7, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (67, 6, '待审批', 'Pending', '0', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (68, 6, '已通过', 'Approved', '1', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (69, 6, '已驳回', 'Rejected', '2', NULL, NULL, 3, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (70, 6, '已撤销', 'Withdrawn', '3', NULL, NULL, 4, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:37', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (71, 7, '启用', 'Enabled', '1', NULL, NULL, 1, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (72, 7, '禁用', 'Disabled', '0', NULL, NULL, 2, 1, 0, '2025-12-25 14:42:50', '2025-12-25 16:13:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (76, 4, '初中', 'Junior High School', 'junior_high', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (78, 4, '中专', 'Secondary Vocational', 'secondary_vocational', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (83, 15, '汉族', 'Han', 'han', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (84, 15, '满族', 'Manchu', 'manchu', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (85, 15, '蒙古族', 'Mongolian', 'mongolian', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (86, 15, '回族', 'Hui', 'hui', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (87, 15, '藏族', 'Tibetan', 'tibetan', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (88, 15, '维吾尔族', 'Uyghur', 'uyghur', NULL, NULL, 6, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (89, 15, '苗族', 'Miao', 'miao', NULL, NULL, 7, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (90, 15, '彝族', 'Yi', 'yi', NULL, NULL, 8, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (91, 15, '壮族', 'Zhuang', 'zhuang', NULL, NULL, 9, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (92, 15, '其他', 'Other', 'other', NULL, NULL, 99, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (93, 16, '正式员工', 'Regular Employee', 'regular', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (94, 16, '试用员工', 'Probation Employee', 'probation', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (95, 16, '实习生', 'Intern', 'intern', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (96, 16, '兼职', 'Part-time', 'part_time', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (97, 16, '外包', 'Outsourcing', 'outsourcing', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (98, 17, '未婚', 'Single', 'single', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (99, 17, '已婚', 'Married', 'married', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (100, 17, '离异', 'Divorced', 'divorced', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (101, 17, '丧偶', 'Widowed', 'widowed', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (102, 18, '群众', 'Masses', 'masses', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (103, 18, '共青团员', 'Communist Youth League Member', 'league_member', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (104, 18, '中共党员', 'CPC Member', 'party_member', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (105, 18, '中共预备党员', 'CPC Probationary Member', 'probationary_member', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (106, 18, '民主党派', 'Democratic Party', 'democratic_party', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (107, 19, '父亲', 'Father', 'father', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (108, 19, '母亲', 'Mother', 'mother', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (109, 19, '配偶', 'Spouse', 'spouse', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (110, 19, '子女', 'Child', 'child', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (111, 19, '兄弟', 'Brother', 'brother', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (112, 19, '姐妹', 'Sister', 'sister', NULL, NULL, 6, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (113, 19, '其他', 'Other', 'other', NULL, NULL, 99, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (114, 20, '是', 'Yes', 'yes', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (115, 20, '否', 'No', 'no', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (116, 21, '职业资格证书', 'Professional Qualification', 'professional', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (117, 21, '技能等级证书', 'Skill Level Certificate', 'skill', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (118, 21, '学历证书', 'Academic Certificate', 'academic', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (119, 21, '语言证书', 'Language Certificate', 'language', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (120, 21, '其他', 'Other', 'other', NULL, NULL, 99, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (121, 22, '初级', 'Primary', 'primary', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (122, 22, '中级', 'Intermediate', 'intermediate', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (123, 22, '高级', 'Advanced', 'advanced', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (124, 22, '特级', 'Expert', 'expert', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (125, 23, '董事长', 'Chairman', 'chairman', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (126, 23, '总经理', 'General Manager', 'gm', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (127, 23, '副总经理', 'Deputy General Manager', 'dgm', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (128, 23, '部门经理', 'Department Manager', 'dm', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (129, 23, '主管', 'Supervisor', 'supervisor', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (130, 23, '组长', 'Team Leader', 'team_leader', NULL, NULL, 6, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (131, 23, '员工', 'Staff', 'staff', NULL, NULL, 7, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (132, 24, 'P1', 'P1', 'p1', NULL, NULL, 1, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (133, 24, 'P2', 'P2', 'p2', NULL, NULL, 2, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (134, 24, 'P3', 'P3', 'p3', NULL, NULL, 3, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (135, 24, 'P4', 'P4', 'p4', NULL, NULL, 4, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (136, 24, 'P5', 'P5', 'p5', NULL, NULL, 5, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (137, 24, 'M1', 'M1', 'm1', NULL, NULL, 6, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (138, 24, 'M2', 'M2', 'm2', NULL, NULL, 7, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (139, 24, 'M3', 'M3', 'm3', NULL, NULL, 8, 1, 0, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (142, 27, '总经理', 'General Manager', 'gm', NULL, NULL, 1, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (143, 27, '副总经理', 'Deputy General Manager', 'dgm', NULL, NULL, 2, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (144, 27, '技术总监', 'CTO', 'cto', NULL, NULL, 3, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (145, 27, '产品总监', 'CPO', 'cpo', NULL, NULL, 4, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (146, 27, '技术经理', 'Technical Manager', 'tech_manager', NULL, NULL, 5, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (147, 27, '产品经理', 'Product Manager', 'product_manager', NULL, NULL, 6, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (148, 27, '项目经理', 'Project Manager', 'project_manager', NULL, NULL, 7, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (149, 27, '高级软件工程师', 'Senior Software Engineer', 'senior_engineer', NULL, NULL, 8, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (150, 27, '软件工程师', 'Software Engineer', 'engineer', NULL, NULL, 9, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (151, 27, '初级软件工程师', 'Junior Software Engineer', 'junior_engineer', NULL, NULL, 10, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (152, 27, '测试工程师', 'Test Engineer', 'test_engineer', NULL, NULL, 11, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (153, 27, '运维工程师', 'DevOps Engineer', 'devops_engineer', NULL, NULL, 12, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (154, 27, 'UI设计师', 'UI Designer', 'ui_designer', NULL, NULL, 13, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (155, 27, '人事经理', 'HR Manager', 'hr_manager', NULL, NULL, 14, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (156, 27, '人事专员', 'HR Specialist', 'hr_specialist', NULL, NULL, 15, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (157, 27, '财务经理', 'Finance Manager', 'finance_manager', NULL, NULL, 16, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (158, 27, '财务专员', 'Finance Specialist', 'finance_specialist', NULL, NULL, 17, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (159, 27, '行政专员', 'Admin Specialist', 'admin_specialist', NULL, NULL, 18, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (160, 27, '销售经理', 'Sales Manager', 'sales_manager', NULL, NULL, 19, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (161, 27, '销售代表', 'Sales Representative', 'sales_rep', NULL, NULL, 20, 1, 0, '2025-12-25 22:07:42', '2025-12-25 22:07:42', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (163, 28, '部门调动', 'Department Transfer', '1', NULL, NULL, 1, 1, 0, '2025-12-28 16:29:08', '2025-12-28 16:29:08', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (164, 28, '职位变更', 'Position Change', '2', NULL, NULL, 2, 1, 0, '2025-12-28 16:29:08', '2025-12-28 16:29:08', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (165, 28, '部门+职位变更', 'Dept Position Change', '3', NULL, NULL, 3, 1, 0, '2025-12-28 16:29:08', '2025-12-28 16:29:08', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (166, 29, '主动离职', 'Voluntary', '1', NULL, NULL, 1, 1, 0, '2025-12-28 16:29:45', '2025-12-28 16:29:45', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (167, 29, '被动离职', 'Involuntary', '2', NULL, NULL, 2, 1, 0, '2025-12-28 16:29:45', '2025-12-28 16:29:45', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (168, 29, '合同到期', 'Contract Expired', '3', NULL, NULL, 3, 1, 0, '2025-12-28 16:29:45', '2025-12-28 16:29:45', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (169, 29, '退休', 'Retirement', '4', NULL, NULL, 4, 1, 0, '2025-12-28 16:29:45', '2025-12-28 16:29:45', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (170, 30, '优秀员工', 'Outstanding Employee', '1', NULL, NULL, 1, 1, 0, '2025-12-28 16:30:15', '2025-12-28 16:30:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (171, 30, '项目奖金', 'Project Bonus', '2', NULL, NULL, 2, 1, 0, '2025-12-28 16:30:15', '2025-12-28 16:30:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (172, 30, '年终奖', 'Year-end Bonus', '3', NULL, NULL, 3, 1, 0, '2025-12-28 16:30:15', '2025-12-28 16:30:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (173, 30, '创新奖', 'Innovation Award', '4', NULL, NULL, 4, 1, 0, '2025-12-28 16:30:15', '2025-12-28 16:30:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (174, 30, '其他奖励', 'Other Reward', '5', NULL, NULL, 5, 1, 0, '2025-12-28 16:30:15', '2025-12-28 16:30:15', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (175, 31, '警告', 'Warning', '1', NULL, NULL, 1, 1, 0, '2025-12-28 16:30:25', '2025-12-28 16:30:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (176, 31, '记过', 'Demerit', '2', NULL, NULL, 2, 1, 0, '2025-12-28 16:30:25', '2025-12-28 16:30:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (177, 31, '降级', 'Demotion', '3', NULL, NULL, 3, 1, 0, '2025-12-28 16:30:25', '2025-12-28 16:30:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (178, 31, '罚款', 'Fine', '4', NULL, NULL, 4, 1, 0, '2025-12-28 16:30:25', '2025-12-28 16:30:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (179, 31, '其他处罚', 'Other Punishment', '5', NULL, NULL, 5, 1, 0, '2025-12-28 16:30:25', '2025-12-28 16:30:25', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (180, 33, '爱好', NULL, 'hobby', NULL, NULL, 1, 1, 0, '2025-12-29 13:39:19', '2025-12-29 13:39:19', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (181, 33, '特长', NULL, 'specialty', NULL, NULL, 2, 1, 0, '2025-12-29 13:39:26', '2025-12-29 13:39:26', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (182, 34, '集团', NULL, '1', NULL, NULL, 1, 1, 0, '2026-01-05 14:08:38', '2026-01-05 14:08:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (183, 34, '公司', NULL, '2', NULL, NULL, 2, 1, 0, '2026-01-05 14:08:38', '2026-01-05 14:08:38', NULL, NULL);
INSERT INTO `sys_dict_data` VALUES (184, 34, '部门', NULL, '3', NULL, NULL, 3, 1, 0, '2026-01-05 14:08:38', '2026-01-05 14:08:38', NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_data_i18n
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data_i18n`;
CREATE TABLE `sys_dict_data_i18n`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_data_id` bigint NOT NULL COMMENT '字典数据ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典标签',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_data_lang`(`dict_data_id` ASC, `lang_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典数据多语言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data_i18n
-- ----------------------------
INSERT INTO `sys_dict_data_i18n` VALUES (1, 1, 'zh-CN', '男', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (2, 2, 'zh-CN', '女', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (3, 3, 'zh-CN', '试用', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (4, 4, 'zh-CN', '正式', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (5, 5, 'zh-CN', '离职', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (6, 6, 'zh-CN', '固定期限', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (7, 7, 'zh-CN', '无固定期限', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (8, 8, 'zh-CN', '完成任务', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (9, 9, 'zh-CN', '年假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (10, 10, 'zh-CN', '事假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (11, 11, 'zh-CN', '病假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (12, 12, 'zh-CN', '婚假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (13, 13, 'zh-CN', '产假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (14, 14, 'zh-CN', '陪产假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (15, 15, 'zh-CN', '丧假', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (16, 16, 'zh-CN', '工作日加班', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (17, 17, 'zh-CN', '周末加班', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (18, 18, 'zh-CN', '节假日加班', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (19, 19, 'zh-CN', '高中', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (20, 20, 'zh-CN', '大专', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (21, 21, 'zh-CN', '本科', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (22, 22, 'zh-CN', '硕士', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (23, 23, 'zh-CN', '博士', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (24, 24, 'zh-CN', '未婚', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (25, 25, 'zh-CN', '已婚', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (26, 26, 'zh-CN', '离异', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (27, 27, 'zh-CN', '群众', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (28, 28, 'zh-CN', '共青团员', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (29, 29, 'zh-CN', '中共党员', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (30, 30, 'zh-CN', '民主党派', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (31, 31, 'zh-CN', '晋升', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (32, 32, 'zh-CN', '降级', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (33, 33, 'zh-CN', '平调', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (34, 34, 'zh-CN', '奖励', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (35, 35, 'zh-CN', '惩罚', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (36, 36, 'zh-CN', '主动离职', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (37, 37, 'zh-CN', '辞退', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (38, 38, 'zh-CN', '合同到期', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (39, 1, 'en-US', 'Male', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (40, 2, 'en-US', 'Female', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (41, 3, 'en-US', 'Probation', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (42, 4, 'en-US', 'Regular', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (43, 5, 'en-US', 'Resigned', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (44, 6, 'en-US', 'Fixed Term', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (45, 7, 'en-US', 'Open-ended', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (46, 8, 'en-US', 'Task-based', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (47, 9, 'en-US', 'Annual Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (48, 10, 'en-US', 'Personal Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (49, 11, 'en-US', 'Sick Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (50, 12, 'en-US', 'Marriage Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (51, 13, 'en-US', 'Maternity Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (52, 14, 'en-US', 'Paternity Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (53, 15, 'en-US', 'Bereavement Leave', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (54, 16, 'en-US', 'Weekday Overtime', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (55, 17, 'en-US', 'Weekend Overtime', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (56, 18, 'en-US', 'Holiday Overtime', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (57, 19, 'en-US', 'High School', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (58, 20, 'en-US', 'College', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (59, 21, 'en-US', 'Bachelor', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (60, 22, 'en-US', 'Master', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (61, 23, 'en-US', 'Doctor', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (62, 24, 'en-US', 'Single', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (63, 25, 'en-US', 'Married', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (64, 26, 'en-US', 'Divorced', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (65, 27, 'en-US', 'Masses', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (66, 28, 'en-US', 'League Member', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (67, 29, 'en-US', 'Party Member', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (68, 30, 'en-US', 'Democratic Party', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (69, 31, 'en-US', 'Promotion', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (70, 32, 'en-US', 'Demotion', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (71, 33, 'en-US', 'Transfer', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (72, 34, 'en-US', 'Reward', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (73, 35, 'en-US', 'Punishment', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (74, 36, 'en-US', 'Voluntary Resignation', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (75, 37, 'en-US', 'Dismissal', '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_dict_data_i18n` VALUES (76, 38, 'en-US', 'Contract Expiration', '2025-12-22 21:19:11', '2025-12-22 21:19:11');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典类型ID',
  `dict_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称（默认语言）',
  `dict_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典名称(英文)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `dict_code`(`dict_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, 'gender', '性别', 'Gender', '性别字典', 1, '2025-12-25 14:42:50', '2025-12-25 18:19:15', NULL, 1);
INSERT INTO `sys_dict_type` VALUES (2, 'employee_status', '员工状态', 'Employee Status', '员工在职状态', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (3, 'contract_type', '合同类型', 'Contract Type', '劳动合同类型', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (4, 'education', '学历', 'Education', '学历类型', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (5, 'leave_type', '请假类型', 'Leave Type', '请假申请类型', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (6, 'approval_status', '审批状态', 'Approval Status', '审批流程状态', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (7, 'sys_status', '系统状态', 'System Status', '通用启用禁用状态', 1, '2025-12-25 14:42:50', '2025-12-25 16:14:48', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (15, 'nation', '民族', 'Nation', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (16, 'employee_type', '员工类别', 'Employee Type', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (17, 'marital_status', '婚姻状况', 'Marital Status', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (18, 'political_status', '政治面貌', 'Political Status', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (19, 'family_relation', '家庭成员关系', 'Family Relation', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (20, 'full_time', '是否全日制', 'Full Time', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (21, 'cert_type', '证书类型', 'Certificate Type', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (22, 'cert_level', '证书等级', 'Certificate Level', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (23, 'duty', '职务', 'Duty', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (24, 'job_level', '职级', 'Job Level', NULL, 1, '2025-12-25 16:38:11', '2025-12-25 16:38:11', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (27, 'position', '职位', 'Position', '员工职位', 1, '2025-12-25 22:07:17', '2025-12-25 22:07:17', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (28, 'transfer_type', '调动类型', 'Transfer Type', '员工调动类型', 1, '2025-12-28 16:28:41', '2025-12-28 16:28:41', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (29, 'resign_type', '离职类型', 'Resignation Type', '员工离职类型', 1, '2025-12-28 16:29:19', '2025-12-28 16:29:19', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (30, 'reward_category', '奖励类别', 'Reward Category', '奖励类别', 1, '2025-12-28 16:29:53', '2025-12-28 16:29:53', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (31, 'punish_category', '惩罚类别', 'Punishment Category', '惩罚类别', 1, '2025-12-28 16:29:53', '2025-12-28 16:29:53', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (33, 'employee_extra_field', '员工扩展字段', NULL, '用于定义员工的额外自定义字段', 1, '2025-12-29 13:38:21', '2025-12-29 13:38:21', NULL, NULL);
INSERT INTO `sys_dict_type` VALUES (34, 'org_unit_type', '组织类型', NULL, '组织架构节点类型', 1, '2026-01-05 14:08:38', '2026-01-05 14:08:38', NULL, NULL);

-- ----------------------------
-- Table structure for sys_dict_type_i18n
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type_i18n`;
CREATE TABLE `sys_dict_type_i18n`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `dict_type_id` bigint NOT NULL COMMENT '字典类型ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_type_lang`(`dict_type_id` ASC, `lang_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '字典类型多语言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type_i18n
-- ----------------------------

-- ----------------------------
-- Table structure for sys_file_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_config`;
CREATE TABLE `sys_file_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值(路径)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件路径配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file_config
-- ----------------------------
INSERT INTO `sys_file_config` VALUES (2, 'employee_avatar_path', '员工头像路径', 'D:/rzphoto/employee_photo', '员工头像照片存储目录', '2026-03-09 09:49:55', '2026-03-09 09:49:55', NULL, NULL);
INSERT INTO `sys_file_config` VALUES (3, 'id_card_front_path', '身份证正面路径', 'D:/rzphoto/id_card_front', '员工身份证正面照片存储目录', '2026-03-09 09:49:55', '2026-03-09 09:49:55', NULL, NULL);
INSERT INTO `sys_file_config` VALUES (4, 'id_card_back_path', '身份证反面路径', 'D:/rzphoto/id_card_back', '员工身份证反面照片存储目录', '2026-03-09 09:49:55', '2026-03-09 09:49:55', NULL, NULL);
INSERT INTO `sys_file_config` VALUES (5, 'contract_photo_path', '合同照片路径', 'D:/rzphoto/Pic_Contract', '合同照片存储目录', '2026-03-09 10:17:22', '2026-03-09 10:17:22', NULL, NULL);
INSERT INTO `sys_file_config` VALUES (6, 'diploma_photo_path', '毕业证照片路径', 'D:/rzphoto/Pic_Diploma', '毕业证照片存储目录', '2026-03-09 11:08:07', '2026-03-09 11:08:07', NULL, NULL);
INSERT INTO `sys_file_config` VALUES (7, 'cert_photo_path', '证书照片路径', 'D:/rzphoto/Pic_Certificate', '证书照片存储目录', '2026-03-09 11:08:07', '2026-03-09 11:08:07', NULL, NULL);

-- ----------------------------
-- Table structure for sys_language
-- ----------------------------
DROP TABLE IF EXISTS `sys_language`;
CREATE TABLE `sys_language`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `lang_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言名称',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `lang_code`(`lang_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '语言配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_language
-- ----------------------------
INSERT INTO `sys_language` VALUES (1, 'zh-CN', '简体中文', NULL, 1, 1, 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `sys_language` VALUES (2, 'en-US', 'English', NULL, 2, 1, 0, '2025-12-22 21:19:11', '2025-12-22 21:19:11');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_type` tinyint NULL DEFAULT 1 COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `menu_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单编码',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称（默认语言）',
  `menu_name_en` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称（英文）',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见：0-否，1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1227 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, 1, 'home', '首页', 'Home', '/home', 'layout.base$view.home', NULL, 'mdi:monitor-dashboard', 1, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 0, 1, 'organization', '组织架构', 'Organization', '/organization', 'layout.base', NULL, 'mdi:office-building', 2, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 0, 1, 'hr', '人事管理', 'HR Management', '/hr', 'layout.base', NULL, 'mdi:account-group', 3, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (4, 0, 1, 'attendance', '考勤管理', 'Attendance', '/attendance', 'layout.base', NULL, 'mdi:clock-outline', 4, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (5, 0, 1, 'application', '申请管理', 'Applications', '/application', 'layout.base', NULL, 'mdi:file-document-multiple', 5, 1, 1, '2025-12-25 14:10:05', '2025-12-25 16:28:29', NULL, NULL);
INSERT INTO `sys_menu` VALUES (6, 0, 1, 'approval', '审批管理', 'Approval', '/approval', 'layout.base', NULL, 'mdi:check-decagram', 6, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (7, 0, 1, 'report', '报表管理', 'Reports', '/report', 'layout.base', NULL, 'mdi:chart-bar', 7, 1, 1, '2025-12-25 14:10:05', '2025-12-25 16:28:29', NULL, NULL);
INSERT INTO `sys_menu` VALUES (8, 0, 1, 'system', '系统管理', 'System', '/system', 'layout.base', NULL, 'mdi:cog', 8, 1, 1, '2025-12-25 14:10:05', '2025-12-25 14:10:05', NULL, NULL);
INSERT INTO `sys_menu` VALUES (24, 2, 2, 'organization_org-structure', '组织架构', 'Organization Structure', '/organization/org-structure', 'view.organization_org-structure', NULL, 'mdi:file-tree', 0, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (31, 3, 2, 'hr_employee', '员工管理', 'Employee Management', '/hr/employee', 'view.hr_employee', NULL, 'mdi:account-multiple', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (32, 3, 2, 'hr_contract', '合同管理', 'Contract Management', '/hr/contract', 'view.hr_contract', NULL, 'mdi:file-document-edit', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (41, 4, 2, 'attendance_shift', '班次管理', 'Shift Management', '/attendance/shift', 'view.attendance_shift', NULL, 'mdi:clock-time-four', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (42, 4, 2, 'attendance_schedule', '员工排班', 'Employee Schedule', '/attendance/schedule', 'view.attendance_schedule', NULL, 'mdi:calendar-clock', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (43, 4, 2, 'attendance_holiday', '节假日管理', 'Holiday Management', '/attendance/holiday', 'view.attendance_holiday', NULL, 'mdi:calendar-star', 3, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (44, 4, 2, 'attendance_daily', '日考勤', 'Daily Attendance', '/attendance/daily', 'view.attendance_daily', NULL, 'mdi:calendar-today', 4, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (45, 4, 2, 'attendance_monthly', '月考勤', 'Monthly Attendance', '/attendance/monthly', 'view.attendance_monthly', NULL, 'mdi:calendar-month', 5, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (51, 5, 2, 'application_leave', '请假申请', 'Leave Application', '/application/leave', 'view.application_leave', NULL, 'mdi:beach', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (52, 5, 2, 'application_overtime', '加班申请', 'Overtime Application', '/application/overtime', 'view.application_overtime', NULL, 'mdi:clock-plus', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (53, 5, 2, 'application_makeup', '补卡申请', 'Makeup Application', '/application/makeup', 'view.application_makeup', NULL, 'mdi:clock-check', 3, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (54, 5, 2, 'application_exchange', '换休申请', 'Exchange Leave', '/application/exchange', 'view.application_exchange', '', 'mdi:swap-horizontal-circle', 4, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (55, 5, 2, 'application_business', '出差申请', 'Business Trip', '/application/business', 'view.application_business', NULL, 'mdi:airplane', 5, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (61, 6, 2, 'approval_pending', '待审批', 'Pending Approval', '/approval/pending', 'view.approval_pending', NULL, 'mdi:clock-alert', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (62, 6, 2, 'approval_mine', '我的申请', 'My Applications', '/approval/mine', 'view.approval_mine', NULL, 'mdi:file-document-check', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (63, 6, 2, 'approval_flow', '审批流程配置', 'Approval Flow Config', '/approval/flow', 'view.approval_flow', NULL, 'mdi:sitemap', 3, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (71, 7, 2, 'report_employee', '员工报表', 'Employee Report', '/report/employee', 'view.report_employee', NULL, 'mdi:chart-pie', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (72, 7, 2, 'report_attendance', '考勤报表', 'Attendance Report', '/report/attendance', 'view.report_attendance', NULL, 'mdi:chart-line', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (81, 8, 2, 'system_user', '用户管理', 'User Management', '/system/user', 'view.system_user', '', 'mdi:account', 1, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (82, 8, 2, 'system_role', '角色管理', 'Role Management', '/system/role', 'view.system_role', NULL, 'mdi:account-key', 2, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (83, 8, 2, 'system_menu', '菜单管理', 'Menu Management', '/system/menu', 'view.system_menu', NULL, 'mdi:menu', 3, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (84, 8, 2, 'system_dict', '字典管理', 'Dictionary Management', '/system/dict', 'view.system_dict', NULL, 'mdi:book-alphabet', 4, 1, 1, '2025-12-25 14:10:05', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (85, 8, 2, 'system_notice', '公告管理', 'Notice Management', '/system/notice', 'view.system_notice', NULL, 'mdi:bullhorn', 5, 1, 1, '2026-03-12 10:36:21', '2026-03-12 10:36:21', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1086, 31, 3, 'hr_employee_add', '新增', 'Add', NULL, NULL, 'hr:employee:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1087, 31, 3, 'hr_employee_edit', '编辑', 'Edit', NULL, NULL, 'hr:employee:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1088, 31, 3, 'hr_employee_delete', '删除', 'Delete', NULL, NULL, 'hr:employee:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1089, 31, 3, 'hr_employee_export', '导出', 'Export', NULL, NULL, 'hr:employee:export', NULL, 4, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1091, 32, 3, 'hr_contract_add', '新增', 'Add', NULL, NULL, 'hr:contract:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1092, 32, 3, 'hr_contract_edit', '编辑', 'Edit', NULL, NULL, 'hr:contract:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1093, 32, 3, 'hr_contract_delete', '删除', 'Delete', NULL, NULL, 'hr:contract:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1127, 81, 3, 'system_user_add', '新增', 'Add', NULL, NULL, 'system:user:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1128, 81, 3, 'system_user_edit', '编辑', 'Edit', NULL, NULL, 'system:user:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1129, 81, 3, 'system_user_delete', '删除', 'Delete', NULL, NULL, 'system:user:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1130, 81, 3, 'system_user_reset', '重置密码', 'Reset Password', NULL, NULL, 'system:user:reset', NULL, 4, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1132, 82, 3, 'system_role_add', '新增', 'Add', NULL, NULL, 'system:role:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1133, 82, 3, 'system_role_edit', '编辑', 'Edit', NULL, NULL, 'system:role:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1134, 82, 3, 'system_role_delete', '删除', 'Delete', NULL, NULL, 'system:role:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1136, 83, 3, 'system_menu_add', '新增', 'Add', NULL, NULL, 'system:menu:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1137, 83, 3, 'system_menu_edit', '编辑', 'Edit', NULL, NULL, 'system:menu:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1138, 83, 3, 'system_menu_delete', '删除', 'Delete', NULL, NULL, 'system:menu:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1140, 84, 3, 'system_dict_add', '新增', 'Add', NULL, NULL, 'system:dict:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1141, 84, 3, 'system_dict_edit', '编辑', 'Edit', NULL, NULL, 'system:dict:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1142, 84, 3, 'system_dict_delete', '删除', 'Delete', NULL, NULL, 'system:dict:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1144, 41, 3, 'attendance_shift_add', '新增', 'Add', NULL, NULL, 'attendance:shift:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1145, 41, 3, 'attendance_shift_edit', '编辑', 'Edit', NULL, NULL, 'attendance:shift:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1146, 41, 3, 'attendance_shift_delete', '删除', 'Delete', NULL, NULL, 'attendance:shift:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1148, 42, 3, 'attendance_schedule_add', '新增', 'Add', NULL, NULL, 'attendance:schedule:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1149, 42, 3, 'attendance_schedule_edit', '编辑', 'Edit', NULL, NULL, 'attendance:schedule:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1150, 42, 3, 'attendance_schedule_delete', '删除', 'Delete', NULL, NULL, 'attendance:schedule:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1152, 43, 3, 'attendance_holiday_add', '新增', 'Add', NULL, NULL, 'attendance:holiday:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1153, 43, 3, 'attendance_holiday_edit', '编辑', 'Edit', NULL, NULL, 'attendance:holiday:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1154, 43, 3, 'attendance_holiday_delete', '删除', 'Delete', NULL, NULL, 'attendance:holiday:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1156, 44, 3, 'attendance_daily_export', '导出', 'Export', NULL, NULL, 'attendance:daily:export', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1158, 45, 3, 'attendance_monthly_export', '导出', 'Export', NULL, NULL, 'attendance:monthly:export', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1160, 51, 3, 'application_leave_add', '新增', 'Add', NULL, NULL, 'application:leave:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1161, 51, 3, 'application_leave_edit', '编辑', 'Edit', NULL, NULL, 'application:leave:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1162, 51, 3, 'application_leave_delete', '删除', 'Delete', NULL, NULL, 'application:leave:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1164, 52, 3, 'application_overtime_add', '新增', 'Add', NULL, NULL, 'application:overtime:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1165, 52, 3, 'application_overtime_edit', '编辑', 'Edit', NULL, NULL, 'application:overtime:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1166, 52, 3, 'application_overtime_delete', '删除', 'Delete', NULL, NULL, 'application:overtime:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1168, 53, 3, 'application_makeup_add', '新增', 'Add', NULL, NULL, 'application:makeup:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1169, 53, 3, 'application_makeup_edit', '编辑', 'Edit', NULL, NULL, 'application:makeup:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1170, 53, 3, 'application_makeup_delete', '删除', 'Delete', NULL, NULL, 'application:makeup:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1172, 54, 3, 'application_exchange_add', '新增', 'Add', NULL, NULL, 'application:exchange:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1173, 54, 3, 'application_exchange_edit', '编辑', 'Edit', NULL, NULL, 'application:exchange:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1174, 54, 3, 'application_exchange_delete', '删除', 'Delete', NULL, NULL, 'application:exchange:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1176, 55, 3, 'application_business_add', '新增', 'Add', NULL, NULL, 'application:business:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1177, 55, 3, 'application_business_edit', '编辑', 'Edit', NULL, NULL, 'application:business:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1178, 55, 3, 'application_business_delete', '删除', 'Delete', NULL, NULL, 'application:business:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1180, 61, 3, 'approval_pending_approve', '审批', 'Approve', NULL, NULL, 'approval:pending:approve', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1181, 61, 3, 'approval_pending_reject', '驳回', 'Reject', NULL, NULL, 'approval:pending:reject', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1184, 63, 3, 'approval_flow_add', '新增', 'Add', NULL, NULL, 'approval:flow:add', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1185, 63, 3, 'approval_flow_edit', '编辑', 'Edit', NULL, NULL, 'approval:flow:edit', NULL, 2, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1186, 63, 3, 'approval_flow_delete', '删除', 'Delete', NULL, NULL, 'approval:flow:delete', NULL, 3, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1188, 71, 3, 'report_employee_export', '导出', 'Export', NULL, NULL, 'report:employee:export', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1190, 72, 3, 'report_attendance_export', '导出', 'Export', NULL, NULL, 'report:attendance:export', NULL, 1, 1, 1, '2025-12-25 14:10:14', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1191, 4, 2, 'attendance_clock', '打卡记录', 'Clock Record', '/attendance/clock', 'view.attendance_clock', NULL, NULL, 6, 1, 1, '2025-12-29 09:55:42', '2026-01-12 20:42:52', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1193, 1191, 3, NULL, '新增', 'Add', NULL, NULL, 'attendance:clock:add', NULL, 2, 1, 1, '2025-12-29 10:01:52', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1194, 1191, 3, NULL, '删除', 'Delete', NULL, NULL, 'attendance:clock:delete', NULL, 3, 1, 1, '2025-12-29 10:01:52', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1195, 3, 2, 'application_regularization', '转正申请', 'Regularization', '/application/regularization', 'view.application_regularization', '', 'mdi:account-check', 6, 1, 1, '2025-12-29 15:34:20', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (1196, 3, 2, 'application_transfer', '调动申请', 'Transfer', '/application/transfer', 'view.application_transfer', '', 'mdi:swap-horizontal', 7, 1, 1, '2025-12-29 15:34:20', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (1197, 3, 2, 'application_reward', '奖惩申请', 'Reward & Punishment', '/application/reward', 'view.application_reward', '', 'mdi:medal', 8, 1, 1, '2025-12-29 15:34:20', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (1198, 3, 2, 'application_resignation', '离职申请', 'Resignation', '/application/resignation', 'view.application_resignation', '', 'mdi:account-remove', 9, 1, 1, '2025-12-29 15:34:20', '2026-01-12 20:42:52', NULL, 1);
INSERT INTO `sys_menu` VALUES (1202, 1195, 3, NULL, '新增', 'Add', NULL, NULL, 'application:regularization:add', NULL, 1, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1203, 1195, 3, NULL, '编辑', 'Edit', NULL, NULL, 'application:regularization:edit', NULL, 2, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1204, 1195, 3, NULL, '删除', 'Delete', NULL, NULL, 'application:regularization:delete', NULL, 3, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1205, 1196, 3, NULL, '新增', 'Add', NULL, NULL, 'application:transfer:add', NULL, 1, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1206, 1196, 3, NULL, '编辑', 'Edit', NULL, NULL, 'application:transfer:edit', NULL, 2, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1207, 1196, 3, NULL, '删除', 'Delete', NULL, NULL, 'application:transfer:delete', NULL, 3, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1208, 1197, 3, NULL, '新增', 'Add', NULL, NULL, 'application:reward:add', NULL, 1, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1209, 1197, 3, NULL, '编辑', 'Edit', NULL, NULL, 'application:reward:edit', NULL, 2, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1210, 1197, 3, NULL, '删除', 'Delete', NULL, NULL, 'application:reward:delete', NULL, 3, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1211, 1198, 3, NULL, '新增', 'Add', NULL, NULL, 'application:resignation:add', NULL, 1, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1212, 1198, 3, NULL, '编辑', 'Edit', NULL, NULL, 'application:resignation:edit', NULL, 2, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1213, 1198, 3, NULL, '删除', 'Delete', NULL, NULL, 'application:resignation:delete', NULL, 3, 1, 1, '2026-01-05 14:36:56', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1214, 24, 3, NULL, '新增', 'Add', NULL, NULL, 'org:unit:add', NULL, 1, 1, 1, '2026-01-05 14:37:34', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1215, 24, 3, NULL, '编辑', 'Edit', NULL, NULL, 'org:unit:edit', NULL, 2, 1, 1, '2026-01-05 14:37:34', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1216, 24, 3, NULL, '删除', 'Delete', NULL, NULL, 'org:unit:delete', NULL, 3, 1, 1, '2026-01-05 14:37:34', '2026-01-12 20:43:01', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1217, 8, 2, 'system_mobile-menu', '移动端菜单', 'Mobile Menu', '/system/mobile-menu', 'view.system_mobile-menu', NULL, 'mdi:cellphone', 5, 1, 1, '2026-01-12 20:37:46', '2026-01-12 20:37:46', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1222, 1217, 3, 'system_mobile-menu_list', '查询', 'List', NULL, NULL, 'system:mobile-menu:list', NULL, 1, 1, 1, '2026-01-12 20:38:22', '2026-01-12 20:38:22', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1223, 1217, 3, 'system_mobile-menu_add', '新增', 'Add', NULL, NULL, 'system:mobile-menu:add', NULL, 2, 1, 1, '2026-01-12 20:38:22', '2026-01-12 20:38:22', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1224, 1217, 3, 'system_mobile-menu_edit', '编辑', 'Edit', NULL, NULL, 'system:mobile-menu:edit', NULL, 3, 1, 1, '2026-01-12 20:38:22', '2026-01-12 20:38:22', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1225, 1217, 3, 'system_mobile-menu_delete', '删除', 'Delete', NULL, NULL, 'system:mobile-menu:delete', NULL, 4, 1, 1, '2026-01-12 20:38:22', '2026-01-12 20:38:22', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1226, 8, 2, 'system_file-config', '路径管理', 'File Config', '/system/file-config', 'view.system_file-config', 'system:fileConfig:list', 'mdi:folder-cog-outline', 60, 1, 1, '2026-03-09 10:00:37', '2026-03-09 10:00:37', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1227, 6, 2, 'approval_mobile-approver', '移动端审批权限', 'Mobile Approval Permission', '/approval/mobile-approver', 'view.approval_mobile-approver', NULL, 'mdi:cellphone-check', 4, 1, 1, '2026-03-11 17:24:20', '2026-03-11 17:27:06', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1230, 85, 3, 'system_notice_add', '新增', 'Add', NULL, NULL, 'system:notice:add', NULL, 1, 1, 1, '2026-03-12 10:36:21', '2026-03-12 10:36:21', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1231, 85, 3, 'system_notice_edit', '编辑', 'Edit', NULL, NULL, 'system:notice:edit', NULL, 2, 1, 1, '2026-03-12 10:36:21', '2026-03-12 10:36:21', NULL, NULL);
INSERT INTO `sys_menu` VALUES (1232, 85, 3, 'system_notice_delete', '删除', 'Delete', NULL, NULL, 'system:notice:delete', NULL, 3, 1, 1, '2026-03-12 10:36:21', '2026-03-12 10:36:21', NULL, NULL);

-- ----------------------------
-- Table structure for sys_menu_i18n
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_i18n`;
CREATE TABLE `sys_menu_i18n`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `lang_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '语言编码',
  `menu_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_lang`(`menu_id` ASC, `lang_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 184 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单多语言表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu_i18n
-- ----------------------------
INSERT INTO `sys_menu_i18n` VALUES (147, 1, 'en-US', 'Home', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (148, 2, 'en-US', 'Organization', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (149, 3, 'en-US', 'HR Management', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (150, 4, 'en-US', 'Attendance', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (151, 5, 'en-US', 'Application', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (152, 6, 'en-US', 'Approval', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (153, 7, 'en-US', 'Report', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (154, 8, 'en-US', 'System', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (155, 24, 'en-US', 'Organization Structure', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (156, 21, 'en-US', 'Company', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (157, 22, 'en-US', 'Department', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (158, 23, 'en-US', 'Position', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (159, 31, 'en-US', 'Employee', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (160, 32, 'en-US', 'Contract', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (161, 33, 'en-US', 'Regularization', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (162, 34, 'en-US', 'Transfer', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (163, 35, 'en-US', 'Reward & Punishment', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (164, 36, 'en-US', 'Resignation', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (165, 41, 'en-US', 'Shift', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (166, 42, 'en-US', 'Schedule', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (167, 43, 'en-US', 'Holiday', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (168, 44, 'en-US', 'Daily Attendance', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (169, 45, 'en-US', 'Monthly Attendance', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (170, 51, 'en-US', 'Leave', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (171, 52, 'en-US', 'Overtime', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (172, 53, 'en-US', 'Makeup', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (173, 54, 'en-US', 'Exchange', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (174, 55, 'en-US', 'Business Trip', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (175, 61, 'en-US', 'Pending', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (176, 62, 'en-US', 'My Applications', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (177, 63, 'en-US', 'Flow Config', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (178, 71, 'en-US', 'Employee Report', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (179, 72, 'en-US', 'Attendance Report', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (180, 81, 'en-US', 'User', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (181, 82, 'en-US', 'Role', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (182, 83, 'en-US', 'Menu', '2025-12-25 14:10:05', '2025-12-25 14:10:05');
INSERT INTO `sys_menu_i18n` VALUES (183, 84, 'en-US', 'Dictionary', '2025-12-25 14:10:05', '2025-12-25 14:10:05');

-- ----------------------------
-- Table structure for sys_mobile_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_mobile_menu`;
CREATE TABLE `sys_mobile_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单编码',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `icon_bg_color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#2d8cf0' COMMENT '图标背景色',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由路径',
  `menu_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'apply' COMMENT '菜单分组：quick-快捷功能，apply-申请中心',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_code`(`menu_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '移动端菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_mobile_menu
-- ----------------------------
INSERT INTO `sys_mobile_menu` VALUES (1, '请假申请', 'leave', 'calendar', '#5cadff', '/pages/apply/leave/index', 'apply', 1, 1, NULL, '2026-01-12 20:35:13', 1, '2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (2, '加班申请', 'overtime', 'checkbox', '#19be6b', '/pages/apply/overtime/index', 'apply', 2, 1, NULL, '2026-01-12 20:35:13', 1, '2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (3, '补卡申请', 'card', 'checkbox', '#ff9900', '/pages/apply/card/index', 'apply', 3, 1, NULL, '2026-01-12 20:35:13', NULL, '2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (4, '出差申请', 'travel', 'location', '#ed4014', '/pages/apply/travel/index', 'apply', 4, 1, NULL, '2026-01-12 20:35:13', NULL, '2026-01-12 20:53:21');
INSERT INTO `sys_mobile_menu` VALUES (5, '转正申请', 'regular', 'auth', '#2d8cf0', '/pages/apply/regular/index', 'apply', 1, 1, NULL, '2026-01-12 20:35:13', NULL, '2026-01-12 20:35:13');
INSERT INTO `sys_mobile_menu` VALUES (6, '调动申请', 'transfer', 'redo', '#9254de', '/pages/apply/transfer/index', 'apply', 2, 0, NULL, '2026-01-12 20:35:13', 1, '2026-01-12 20:35:13');
INSERT INTO `sys_mobile_menu` VALUES (7, '离职申请', 'resign', 'closeempty', '#f5222d', '/pages/apply/resign/index', 'apply', 3, 1, NULL, '2026-01-12 20:35:13', NULL, '2026-01-12 20:35:13');
INSERT INTO `sys_mobile_menu` VALUES (8, '换休申请', 'exchange', 'refreshempty', '#fa8c16', '/pages/apply/exchange/index', 'apply', 4, 1, NULL, '2026-01-12 20:35:13', NULL, '2026-01-12 20:35:13');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` tinyint NOT NULL DEFAULT 1 COMMENT '公告类型：1-公告，2-通知',
  `notice_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公告内容',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-关闭，1-正常',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布日期',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (1, '111', 2, '222', 1, '2026-03-01 00:00:00', '2026-03-12 10:40:13', '2026-03-12 10:46:42', 1, 1);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `data_scope` tinyint NULL DEFAULT 1 COMMENT '数据权限范围：1-全部数据，2-本公司数据，3-本部门数据，4-本部门及以下数据，5-仅本人数据，6-自定义数据',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ROLE_ADMIN', '管理员', '', 1, 1, '2025-12-22 21:19:11', '2026-03-11 17:27:47', NULL, 1, 1);
INSERT INTO `sys_role` VALUES (10, 'nhsys', '超级管理员', '', 1, 0, '2026-01-19 18:45:50', '2026-03-11 15:17:29', 1, 1, 1);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_dept`(`role_id` ASC, `dept_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3601 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (3651, 10, 1, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3652, 10, 2, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3653, 10, 24, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3654, 10, 1214, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3655, 10, 1215, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3656, 10, 1216, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3657, 10, 3, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3658, 10, 31, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3659, 10, 1086, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3660, 10, 1087, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3661, 10, 1088, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3662, 10, 1089, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3663, 10, 32, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3664, 10, 1091, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3665, 10, 1092, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3666, 10, 1093, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3667, 10, 1195, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3668, 10, 1202, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3669, 10, 1203, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3670, 10, 1204, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3671, 10, 1196, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3672, 10, 1205, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3673, 10, 1206, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3674, 10, 1207, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3675, 10, 1197, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3676, 10, 1208, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3677, 10, 1209, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3678, 10, 1210, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3679, 10, 1198, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3680, 10, 1211, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3681, 10, 1212, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3682, 10, 1213, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3683, 10, 6, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3684, 10, 61, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3685, 10, 1180, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3686, 10, 1181, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3687, 10, 62, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3688, 10, 63, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3689, 10, 1184, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3690, 10, 1185, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3691, 10, 1186, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3692, 10, 81, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3693, 10, 1127, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3694, 10, 1128, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3695, 10, 1129, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3696, 10, 1130, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3697, 10, 82, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3698, 10, 1132, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3699, 10, 1133, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3700, 10, 1134, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3701, 10, 83, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3702, 10, 1136, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3703, 10, 1137, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3704, 10, 1138, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3705, 10, 84, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3706, 10, 1140, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3707, 10, 1141, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3708, 10, 1142, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3709, 10, 8, '2026-03-11 15:17:28');
INSERT INTO `sys_role_menu` VALUES (3934, 10, 1227, '2026-03-11 17:25:34');
INSERT INTO `sys_role_menu` VALUES (3935, 1, 1, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3936, 1, 2, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3937, 1, 24, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3938, 1, 1214, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3939, 1, 1215, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3940, 1, 1216, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3941, 1, 3, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3942, 1, 31, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3943, 1, 1086, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3944, 1, 1087, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3945, 1, 1088, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3946, 1, 1089, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3947, 1, 32, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3948, 1, 1091, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3949, 1, 1092, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3950, 1, 1093, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3951, 1, 1195, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3952, 1, 1202, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3953, 1, 1203, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3954, 1, 1204, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3955, 1, 1196, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3956, 1, 1205, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3957, 1, 1206, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3958, 1, 1207, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3959, 1, 1197, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3960, 1, 1208, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3961, 1, 1209, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3962, 1, 1210, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3963, 1, 1198, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3964, 1, 1211, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3965, 1, 1212, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3966, 1, 1213, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3967, 1, 4, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3968, 1, 41, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3969, 1, 1144, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3970, 1, 1145, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3971, 1, 1146, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3972, 1, 42, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3973, 1, 1148, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3974, 1, 1149, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3975, 1, 1150, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3976, 1, 43, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3977, 1, 1152, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3978, 1, 1153, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3979, 1, 1154, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3980, 1, 44, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3981, 1, 1156, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3982, 1, 45, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3983, 1, 1158, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3984, 1, 1191, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3985, 1, 1193, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3986, 1, 1194, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3987, 1, 5, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3988, 1, 51, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3989, 1, 1160, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3990, 1, 1161, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3991, 1, 1162, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3992, 1, 52, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3993, 1, 1164, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3994, 1, 1165, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3995, 1, 1166, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3996, 1, 53, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3997, 1, 1168, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3998, 1, 1169, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (3999, 1, 1170, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4000, 1, 54, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4001, 1, 1172, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4002, 1, 1173, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4003, 1, 1174, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4004, 1, 55, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4005, 1, 1176, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4006, 1, 1177, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4007, 1, 1178, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4008, 1, 6, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4009, 1, 61, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4010, 1, 1180, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4011, 1, 1181, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4012, 1, 62, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4013, 1, 63, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4014, 1, 1184, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4015, 1, 1185, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4016, 1, 1186, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4017, 1, 1227, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4018, 1, 7, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4019, 1, 71, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4020, 1, 1188, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4021, 1, 72, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4022, 1, 1190, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4023, 1, 8, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4024, 1, 81, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4025, 1, 1127, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4026, 1, 1128, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4027, 1, 1129, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4028, 1, 1130, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4029, 1, 82, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4030, 1, 1132, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4031, 1, 1133, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4032, 1, 1134, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4033, 1, 83, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4034, 1, 1136, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4035, 1, 1137, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4036, 1, 1138, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4037, 1, 84, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4038, 1, 1140, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4039, 1, 1141, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4040, 1, 1142, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4041, 1, 1217, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4042, 1, 1222, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4043, 1, 1223, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4044, 1, 1224, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4045, 1, 1225, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4046, 1, 1226, '2026-03-11 17:27:47');
INSERT INTO `sys_role_menu` VALUES (4047, 1, 85, '2026-03-12 10:39:12');
INSERT INTO `sys_role_menu` VALUES (4048, 1, 1230, '2026-03-12 10:39:12');
INSERT INTO `sys_role_menu` VALUES (4049, 1, 1231, '2026-03-12 10:39:12');
INSERT INTO `sys_role_menu` VALUES (4050, 1, 1232, '2026-03-12 10:39:12');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `employee_id` bigint NULL DEFAULT NULL COMMENT '关联员工ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$O.gQasdj6qcXGwDqDOKZOOet.DzedNhQAP78Ku/GiOtPQfNsLLepe', '系统管理员', NULL, NULL, NULL, 0, 1, NULL, NULL, NULL, '2025-12-22 21:19:11', '2026-01-24 22:22:44', NULL, 1);
INSERT INTO `sys_user` VALUES (8, 'nhsys', '$2a$10$EtjBMrV6a0.qQ3Axhho6YeShIA0PCNDjm/wfL.AqaFKh60NUfStIi', 'nhsys', '', '', NULL, 0, 1, NULL, NULL, NULL, '2026-01-19 18:46:03', '2026-01-19 18:46:33', 1, 1);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (17, 1, 1, '2026-01-12 20:47:03');
INSERT INTO `sys_user_role` VALUES (18, 8, 10, '2026-01-19 18:46:33');

-- ----------------------------
-- Table structure for wf_approval_record
-- ----------------------------
DROP TABLE IF EXISTS `wf_approval_record`;
CREATE TABLE `wf_approval_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `instance_id` bigint NOT NULL COMMENT '流程实例ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `action` tinyint NULL DEFAULT NULL COMMENT '操作：1-通过，2-拒绝，3-转交',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审批意见',
  `transfer_to` bigint NULL DEFAULT NULL COMMENT '转交给',
  `approval_time` datetime NULL DEFAULT NULL COMMENT '审批时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_instance_id`(`instance_id` ASC) USING BTREE,
  INDEX `idx_approver_id`(`approver_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_approval_record
-- ----------------------------

-- ----------------------------
-- Table structure for wf_process_definition
-- ----------------------------
DROP TABLE IF EXISTS `wf_process_definition`;
CREATE TABLE `wf_process_definition`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `company_id` bigint NULL DEFAULT NULL COMMENT '公司ID，为空表示全局',
  `process_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程编码',
  `process_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程名称',
  `process_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_process_definition
-- ----------------------------
INSERT INTO `wf_process_definition` VALUES (1, NULL, 'LEAVE', '请假审批流程', 'leave', '请假申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (2, NULL, 'OVERTIME', '加班审批流程', 'overtime', '加班申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (3, NULL, 'CARD_REPLACEMENT', '补卡审批流程', 'card_replacement', '补卡申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (4, NULL, 'COMP_LEAVE', '换休审批流程', 'comp_leave', '换休申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (5, NULL, 'BUSINESS_TRIP', '出差审批流程', 'business_trip', '出差申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (6, NULL, 'REGULARIZATION', '转正审批流程', 'regularization', '转正申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (7, NULL, 'RESIGNATION', '离职审批流程', 'resignation', '离职申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (8, NULL, 'DEPT_CHANGE', '部门变更审批流程', 'dept_change', '部门变更申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (9, NULL, 'POSITION_CHANGE', '职位变更审批流程', 'position_change', '职位变更申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);
INSERT INTO `wf_process_definition` VALUES (10, NULL, 'REWARD_PUNISHMENT', '奖惩审批流程', 'reward_punishment', '奖惩申请审批流程', 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11', NULL, NULL);

-- ----------------------------
-- Table structure for wf_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `wf_process_instance`;
CREATE TABLE `wf_process_instance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `process_id` bigint NOT NULL COMMENT '流程定义ID',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `current_node_id` bigint NULL DEFAULT NULL COMMENT '当前节点ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-进行中，1-已通过，2-已拒绝，3-已撤销',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_applicant`(`applicant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for wf_process_node
-- ----------------------------
DROP TABLE IF EXISTS `wf_process_node`;
CREATE TABLE `wf_process_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `process_id` bigint NOT NULL COMMENT '流程ID',
  `node_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点名称',
  `node_type` tinyint NULL DEFAULT NULL COMMENT '节点类型：1-开始，2-审批，3-抄送，4-结束',
  `node_order` int NOT NULL COMMENT '节点顺序',
  `approver_type` tinyint NULL DEFAULT NULL COMMENT '审批人类型：1-指定人员，2-指定角色，3-部门负责人，4-上级领导',
  `approver_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人ID列表',
  `multi_approve_type` tinyint NULL DEFAULT 1 COMMENT '多人审批方式：1-或签，2-会签',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_process_id`(`process_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程节点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_process_node
-- ----------------------------
INSERT INTO `wf_process_node` VALUES (1, 1, '开始', 1, 1, NULL, NULL, NULL, '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (2, 1, '部门负责人审批', 2, 2, 3, NULL, 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (3, 1, 'HR审批', 2, 3, 2, NULL, 1, '2025-12-22 21:19:11', '2025-12-22 21:19:11');
INSERT INTO `wf_process_node` VALUES (4, 1, '结束', 4, 4, NULL, NULL, NULL, '2025-12-22 21:19:11', '2025-12-22 21:19:11');

SET FOREIGN_KEY_CHECKS = 1;
