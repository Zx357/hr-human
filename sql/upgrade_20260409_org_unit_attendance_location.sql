SET NAMES utf8mb4;

ALTER TABLE `org_unit`
  ADD COLUMN IF NOT EXISTS `attendance_address` varchar(255) NULL DEFAULT NULL COMMENT '打卡地址' AFTER `address`;

ALTER TABLE `org_unit`
  ADD COLUMN IF NOT EXISTS `attendance_latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '打卡纬度(GCJ-02)' AFTER `attendance_address`;

ALTER TABLE `org_unit`
  ADD COLUMN IF NOT EXISTS `attendance_longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '打卡经度(GCJ-02)' AFTER `attendance_latitude`;

ALTER TABLE `org_unit`
  ADD COLUMN IF NOT EXISTS `attendance_range` int NULL DEFAULT NULL COMMENT '打卡半径(米)' AFTER `attendance_longitude`;
