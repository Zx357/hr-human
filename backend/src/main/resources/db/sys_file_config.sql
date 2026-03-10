-- ==========================================
-- 文件路径配置表
-- ==========================================
CREATE TABLE IF NOT EXISTS `sys_file_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值(路径)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `deleted` TINYINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件路径配置表';

-- 初始化路径配置数据
INSERT INTO `sys_file_config` (`config_key`, `config_name`, `config_value`, `remark`) VALUES
('upload_base_path', '文件上传基础路径', './uploads', '通用文件和图片上传的基础目录'),
('employee_avatar_path', '员工头像路径', 'D:/rzphoto/employee_photo', '员工头像照片存储目录'),
('id_card_front_path', '身份证正面路径', 'D:/rzphoto/id_card_front', '员工身份证正面照片存储目录'),
('id_card_back_path', '身份证反面路径', 'D:/rzphoto/id_card_back', '员工身份证反面照片存储目录');

-- ==========================================
-- 添加路径管理菜单
-- 注意：parent_id 需要替换为实际的"系统管理"菜单 ID
-- 执行前先查询：SELECT id FROM sys_menu WHERE menu_code = 'system_manage' AND deleted = 0;
-- ==========================================
INSERT INTO `sys_menu` (`parent_id`, `menu_type`, `menu_code`, `menu_name`, `menu_name_en`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`, `deleted`)
SELECT id, 2, 'system_file-config', '路径管理', 'File Config', '/system/file-config', 'view.system_file-config', 'system:fileConfig:list', 'mdi:folder-cog-outline', 60, 1, 1, 0
FROM sys_menu WHERE menu_code = 'system' AND deleted = 0 LIMIT 1;
