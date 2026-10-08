-- =============================================================================
-- 数据库升级 2026-10-08
-- 1. 修正 sys_mobile_menu 种子中的历史遗留路径(/pages/apply/* → /workPages/*)
--    旧路径在 uniapp 中不存在,导致后台菜单配置对 6 个申请入口无效
-- =============================================================================

UPDATE sys_mobile_menu SET path = '/workPages/leave',     icon = 'calendar-fill'      WHERE menu_code = 'leave'     AND path LIKE '/pages/apply/%';
UPDATE sys_mobile_menu SET path = '/workPages/overtime',  icon = 'time-fill'          WHERE menu_code = 'overtime'  AND path LIKE '/pages/apply/%';
UPDATE sys_mobile_menu SET path = '/workPages/replace',   icon = 'edit-form'          WHERE menu_code = 'card'      AND path LIKE '/pages/apply/%';
UPDATE sys_mobile_menu SET path = '/workPages/travel',    icon = 'suitcase-fill'      WHERE menu_code = 'travel'    AND path LIKE '/pages/apply/%';
UPDATE sys_mobile_menu SET path = '/workPages/resign',    icon = 'reduce-circle-fill' WHERE menu_code = 'resign'    AND path LIKE '/pages/apply/%';
UPDATE sys_mobile_menu SET path = '/workPages/exchange',  icon = 'menu-grille-fill'   WHERE menu_code = 'exchange'  AND path LIKE '/pages/apply/%';

-- =============================================================================
-- 数据库升级 2026-10-08（第二批）
-- 2. 移动端聊天：消息撤回状态 + 会话置顶/隐藏
--    （会话个人状态实际存放于 mobile_chat_read_state：每个员工在每个会话一行）
-- =============================================================================

-- 2.1 消息状态：0-正常 1-已撤回（撤回后历史查询内容替换为"xxx 撤回了一条消息"）
ALTER TABLE mobile_chat_message
  ADD COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态：0-正常 1-已撤回' AFTER msg_type;

-- 2.2 会话置顶/隐藏（仅隐藏会话列表入口，不删除消息记录，发新消息自动取消隐藏）
ALTER TABLE mobile_chat_read_state
  ADD COLUMN sticky TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否 1-是' AFTER last_read_message_id,
  ADD COLUMN hidden TINYINT NOT NULL DEFAULT 0 COMMENT '是否从会话列表隐藏：0-否 1-是' AFTER sticky;

-- =============================================================================
-- 3. 薪资档案明细：个人覆盖标记
--    换绑方案时保留个人覆盖金额（custom_flag=1 的明细项金额不被新方案默认值覆盖）
-- =============================================================================

ALTER TABLE sal_salary_archive_item
  ADD COLUMN custom_flag TINYINT NOT NULL DEFAULT 0 COMMENT '个人覆盖标记：0-方案默认值 1-个人覆盖(换绑方案时保留)' AFTER amount;

-- =============================================================================
-- 4. 权限码规范化：sys_menu 种子中驼峰权限码改为 kebab-case
--    （与其余 system:* 权限码命名及 FileConfigController 实际校验保持一致）
-- =============================================================================

UPDATE sys_menu SET permission = 'system:file-config:list'
 WHERE id = 1226 AND permission = 'system:fileConfig:list';

-- =============================================================================
-- 5. 系统参数配置模块（sys_config）
--    地图Key / 微信AppID 等运行期可调参数；公开接口仅返回 is_public=1 且启用的键
-- =============================================================================

-- 5.1 表结构
CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  config_key VARCHAR(100) NOT NULL COMMENT '配置键',
  config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
  config_value VARCHAR(500) DEFAULT NULL COMMENT '配置值',
  config_group VARCHAR(50) DEFAULT 'basic' COMMENT '配置分组：map_config-地图配置，wechat_config-微信配置，basic-基础',
  is_public TINYINT DEFAULT 1 COMMENT '是否公开：1-登录用户可通过公开接口读取，0-仅管理端可见（防敏感配置泄露）',
  remark VARCHAR(255) DEFAULT NULL COMMENT '说明',
  status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-停用（停用的配置公开接口不返回）',
  sort_order INT DEFAULT 0 COMMENT '排序',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  created_by BIGINT DEFAULT NULL COMMENT '创建人',
  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统参数配置';

-- 5.2 预置种子（幂等：键已存在则跳过，不覆盖已配置的值）
INSERT INTO sys_config (config_key, config_name, config_value, config_group, is_public, remark, status, sort_order)
SELECT 'map.amap.key', '高德地图Key', NULL, 'map_config', 1,
       'PC端考勤地点选点与移动端H5打卡地图使用；在高德开放平台申请「Web端(JS API)」类型', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'map.amap.key');

INSERT INTO sys_config (config_key, config_name, config_value, config_group, is_public, remark, status, sort_order)
SELECT 'map.amap.security-code', '高德安全密钥', NULL, 'map_config', 1,
       '与高德Key配套的安全密钥(jscode)；2021-12-02之后申请的Key必填，之前申请的旧Key可留空', 1, 2
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'map.amap.security-code');

-- 地图统一切换为高德：删除已弃用的 Google 地图配置项（含历史填写的值）
DELETE FROM sys_config WHERE config_key = 'map.google.key';

INSERT INTO sys_config (config_key, config_name, config_value, config_group, is_public, remark, status, sort_order)
SELECT 'wechat.appid', '微信小程序AppID', NULL, 'wechat_config', 1,
       '仅作统一记录；小程序AppID为编译期配置（manifest.json），修改后需重新编译发布小程序', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wechat.appid');

-- 5.3 「参数配置」菜单（挂系统管理目录 id=8 下，紧随 路径管理 sort=60 之后）+ 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_type, menu_code, menu_name, menu_name_en, path, component, permission, icon, sort_order, visible, status, created_time, updated_time)
SELECT 1280, 8, 2, 'system_config', '参数配置', 'System Config', '/system/config', 'view.system_config', 'system:config:list', 'mdi:tune', 61, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1280 OR menu_code = 'system_config');

INSERT INTO sys_menu (id, parent_id, menu_type, menu_code, menu_name, menu_name_en, path, component, permission, icon, sort_order, visible, status, created_time, updated_time)
SELECT 1281, 1280, 3, 'system_config_list', '查询', '查询', NULL, NULL, 'system:config:list', NULL, 1, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1281 OR menu_code = 'system_config_list');

INSERT INTO sys_menu (id, parent_id, menu_type, menu_code, menu_name, menu_name_en, path, component, permission, icon, sort_order, visible, status, created_time, updated_time)
SELECT 1282, 1280, 3, 'system_config_add', '新增', '新增', NULL, NULL, 'system:config:add', NULL, 2, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1282 OR menu_code = 'system_config_add');

INSERT INTO sys_menu (id, parent_id, menu_type, menu_code, menu_name, menu_name_en, path, component, permission, icon, sort_order, visible, status, created_time, updated_time)
SELECT 1283, 1280, 3, 'system_config_edit', '编辑', '编辑', NULL, NULL, 'system:config:edit', NULL, 3, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1283 OR menu_code = 'system_config_edit');

INSERT INTO sys_menu (id, parent_id, menu_type, menu_code, menu_name, menu_name_en, path, component, permission, icon, sort_order, visible, status, created_time, updated_time)
SELECT 1284, 1280, 3, 'system_config_delete', '删除', '删除', NULL, NULL, 'system:config:delete', NULL, 4, 1, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1284 OR menu_code = 'system_config_delete');

-- 5.4 管理员角色授权新菜单+按钮（幂等：按 menu_code 关联，已授权的跳过）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
JOIN sys_menu m ON m.menu_code IN ('system_config', 'system_config_list', 'system_config_add',
                                   'system_config_edit', 'system_config_delete')
WHERE (r.role_code IN ('admin', 'ROLE_ADMIN') OR r.role_name LIKE '%管理员%')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id);
