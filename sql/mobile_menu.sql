-- 移动端菜单表
DROP TABLE IF EXISTS `sys_mobile_menu`;
CREATE TABLE `sys_mobile_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_code` varchar(50) NOT NULL COMMENT '菜单编码',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `icon_bg_color` varchar(20) DEFAULT '#2d8cf0' COMMENT '图标背景色',
  `path` varchar(200) NOT NULL COMMENT '路由路径',
  `menu_group` varchar(50) DEFAULT 'apply' COMMENT '菜单分组：quick-快捷功能，apply-申请中心',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `created_by` bigint DEFAULT NULL COMMENT '创建人',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端菜单表';

-- 初始化移动端菜单数据
INSERT INTO `sys_mobile_menu` (`menu_name`, `menu_code`, `icon`, `icon_bg_color`, `path`, `menu_group`, `sort_order`, `status`) VALUES
('请假申请', 'leave', 'calendar', '#5cadff', '/pages/apply/leave/index', 'quick', 1, 1),
('加班申请', 'overtime', 'clock', '#19be6b', '/pages/apply/overtime/index', 'quick', 2, 1),
('补卡申请', 'card', 'checkbox', '#ff9900', '/pages/apply/card/index', 'quick', 3, 1),
('出差申请', 'travel', 'location', '#ed4014', '/pages/apply/travel/index', 'quick', 4, 1),
('转正申请', 'regular', 'auth', '#2d8cf0', '/pages/apply/regular/index', 'apply', 1, 1),
('调动申请', 'transfer', 'redo', '#9254de', '/pages/apply/transfer/index', 'apply', 2, 1),
('离职申请', 'resign', 'closeempty', '#f5222d', '/pages/apply/resign/index', 'apply', 3, 1),
('换休申请', 'exchange', 'refreshempty', '#fa8c16', '/pages/apply/exchange/index', 'apply', 4, 1);

-- 在系统管理下添加"移动端菜单"菜单项（parent_id=8 是系统管理）
INSERT INTO `sys_menu` (`parent_id`, `menu_type`, `menu_code`, `menu_name`, `menu_name_en`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`) VALUES
(8, 2, 'system_mobile-menu', '移动端菜单', 'Mobile Menu', '/system/mobile-menu', 'view.system_mobile-menu', NULL, 'mdi:cellphone', 5, 1, 1);

-- 获取刚插入的菜单ID，添加按钮权限
SET @mobile_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`parent_id`, `menu_type`, `menu_code`, `menu_name`, `menu_name_en`, `path`, `component`, `permission`, `icon`, `sort_order`, `visible`, `status`) VALUES
(@mobile_menu_id, 3, 'system_mobile-menu_list', '查询', 'List', NULL, NULL, 'system:mobile-menu:list', NULL, 1, 1, 1),
(@mobile_menu_id, 3, 'system_mobile-menu_add', '新增', 'Add', NULL, NULL, 'system:mobile-menu:add', NULL, 2, 1, 1),
(@mobile_menu_id, 3, 'system_mobile-menu_edit', '编辑', 'Edit', NULL, NULL, 'system:mobile-menu:edit', NULL, 3, 1, 1),
(@mobile_menu_id, 3, 'system_mobile-menu_delete', '删除', 'Delete', NULL, NULL, 'system:mobile-menu:delete', NULL, 4, 1, 1);
