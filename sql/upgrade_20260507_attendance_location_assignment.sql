-- Independent attendance clock locations and employee assignments.
-- Run this after the original schema and previous org_unit attendance-location upgrade.

CREATE TABLE IF NOT EXISTS `att_location` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `location_name` varchar(100) NOT NULL COMMENT '打卡地点名称',
  `address` varchar(255) DEFAULT NULL COMMENT '打卡地址',
  `latitude` decimal(10,6) NOT NULL COMMENT '纬度 GCJ-02',
  `longitude` decimal(10,6) NOT NULL COMMENT '经度 GCJ-02',
  `clock_range` int NOT NULL DEFAULT 300 COMMENT '打卡半径，单位米',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡地点';

CREATE TABLE IF NOT EXISTS `att_location_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `location_id` bigint NOT NULL COMMENT '打卡地点ID',
  `employee_id` bigint NOT NULL COMMENT '员工ID',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_att_location_employee_location_employee` (`location_id`, `employee_id`),
  KEY `idx_att_location_employee_location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤打卡地点员工分配';

-- Allow one employee to be assigned to multiple clock locations.
SET @old_att_location_employee_key_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'att_location_employee'
    AND index_name = 'uk_att_location_employee_employee'
);
SET @drop_old_att_location_employee_key_sql := IF(
  @old_att_location_employee_key_exists > 0,
  'ALTER TABLE `att_location_employee` DROP INDEX `uk_att_location_employee_employee`',
  'SELECT 1'
);
PREPARE drop_old_att_location_employee_key_stmt FROM @drop_old_att_location_employee_key_sql;
EXECUTE drop_old_att_location_employee_key_stmt;
DEALLOCATE PREPARE drop_old_att_location_employee_key_stmt;

SET @new_att_location_employee_key_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'att_location_employee'
    AND index_name = 'uk_att_location_employee_location_employee'
);
SET @add_new_att_location_employee_key_sql := IF(
  @new_att_location_employee_key_exists = 0,
  'ALTER TABLE `att_location_employee` ADD UNIQUE KEY `uk_att_location_employee_location_employee` (`location_id`, `employee_id`)',
  'SELECT 1'
);
PREPARE add_new_att_location_employee_key_stmt FROM @add_new_att_location_employee_key_sql;
EXECUTE add_new_att_location_employee_key_stmt;
DEALLOCATE PREPARE add_new_att_location_employee_key_stmt;

-- Preserve existing company attendance points as independent locations.
INSERT INTO `att_location` (
  `location_name`,
  `address`,
  `latitude`,
  `longitude`,
  `clock_range`,
  `status`,
  `remark`,
  `created_time`,
  `updated_time`
)
SELECT
  ou.unit_name,
  COALESCE(NULLIF(ou.attendance_address, ''), ou.address),
  ou.attendance_latitude,
  ou.attendance_longitude,
  COALESCE(ou.attendance_range, 300),
  1,
  '由公司打卡配置迁移',
  NOW(),
  NOW()
FROM org_unit ou
WHERE ou.unit_type = 2
  AND ou.attendance_latitude IS NOT NULL
  AND ou.attendance_longitude IS NOT NULL
  AND ou.attendance_range IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM att_location al
    WHERE al.location_name = ou.unit_name
      AND al.latitude = ou.attendance_latitude
      AND al.longitude = ou.attendance_longitude
  );

-- Assign existing employees under the migrated company point.
INSERT IGNORE INTO `att_location_employee` (`location_id`, `employee_id`, `created_time`, `updated_time`)
SELECT
  al.id,
  e.id,
  NOW(),
  NOW()
FROM hr_employee e
LEFT JOIN org_unit d ON e.dept_id = d.id
LEFT JOIN org_unit p1 ON d.parent_id = p1.id
LEFT JOIN org_unit p2 ON p1.parent_id = p2.id
LEFT JOIN org_unit p3 ON p2.parent_id = p3.id
INNER JOIN org_unit company ON company.id = CASE
  WHEN d.unit_type = 2 THEN d.id
  WHEN p1.unit_type = 2 THEN p1.id
  WHEN p2.unit_type = 2 THEN p2.id
  WHEN p3.unit_type = 2 THEN p3.id
  ELSE NULL
END
INNER JOIN att_location al
  ON al.location_name = company.unit_name
 AND al.latitude = company.attendance_latitude
 AND al.longitude = company.attendance_longitude
WHERE e.status = 1
  AND company.attendance_latitude IS NOT NULL
  AND company.attendance_longitude IS NOT NULL;

-- Add backend menu under Attendance Management.
INSERT INTO `sys_menu` (
  `id`,
  `parent_id`,
  `menu_type`,
  `menu_code`,
  `menu_name`,
  `menu_name_en`,
  `path`,
  `component`,
  `permission`,
  `icon`,
  `sort_order`,
  `visible`,
  `status`,
  `created_time`,
  `updated_time`,
  `created_by`,
  `updated_by`
)
SELECT
  1240,
  4,
  2,
  'attendance_location',
  '打卡地点',
  'Clock Locations',
  '/attendance/location',
  'view.attendance_location',
  NULL,
  'mdi:map-marker-radius',
  3,
  1,
  1,
  NOW(),
  NOW(),
  NULL,
  NULL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 1240 OR `menu_code` = 'attendance_location');

-- Grant this menu to built-in administrator roles.
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `created_time`)
SELECT
  r.id,
  1240,
  NOW()
FROM `sys_role` r
WHERE r.role_code IN ('ROLE_ADMIN', 'admin')
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.role_id = r.id
      AND rm.menu_id = 1240
  );
