-- =============================================================================
-- 数据库清理 2026-10-06
-- 依据:全库字段/表使用情况比对(实际库 kadmin @ MySQL 8.0.46 vs backend/frontend/uniapp 代码)
-- 有数据的表已先归档至 sql/archive/obsolete_tables_backup_20261006.sql
-- =============================================================================

-- 1. 删除死表(20张,无实体/无原生SQL引用,均无业务数据)
DROP TABLE IF EXISTS app_business_trip;
DROP TABLE IF EXISTS app_card_replacement;
DROP TABLE IF EXISTS app_comp_leave;
DROP TABLE IF EXISTS app_leave;
DROP TABLE IF EXISTS app_overtime;
DROP TABLE IF EXISTS att_holiday;
DROP TABLE IF EXISTS att_monthly_summary;
DROP TABLE IF EXISTS hr_dept_change;
DROP TABLE IF EXISTS hr_position_change;
DROP TABLE IF EXISTS hr_reward_punishment;
DROP TABLE IF EXISTS mobile_contact_request;
DROP TABLE IF EXISTS sys_dict_data_i18n;
DROP TABLE IF EXISTS sys_dict_type_i18n;
DROP TABLE IF EXISTS sys_language;
DROP TABLE IF EXISTS sys_menu_i18n;
DROP TABLE IF EXISTS sys_role_dept;
DROP TABLE IF EXISTS wf_approval_record;
DROP TABLE IF EXISTS wf_process_definition;
DROP TABLE IF EXISTS wf_process_instance;
DROP TABLE IF EXISTS wf_process_node;

-- 2. 删除死列(实体映射同步清理,见对应 commit)
ALTER TABLE att_schedule        DROP COLUMN is_rest;
ALTER TABLE att_shift           DROP COLUMN rest_start_time;
ALTER TABLE att_shift           DROP COLUMN rest_end_time;
ALTER TABLE hr_contract         DROP COLUMN attachment;
ALTER TABLE hr_contract         DROP COLUMN sign_company;
ALTER TABLE mobile_chat_message DROP COLUMN status;
ALTER TABLE sys_dict_data       DROP COLUMN css_class;
ALTER TABLE sys_dict_data       DROP COLUMN list_class;
ALTER TABLE sys_dict_data       DROP COLUMN is_default;
-- 注:sys_menu.visible 初版被误判为死列,后经核实 SysMenuService.convertToRoutes 依赖它生成
-- hideInMenu、DataInitializer 也写入该列,已在本次清理中恢复,请勿删除。

-- 3. 清理组织架构旧设计的按钮权限(页面已统一使用 org:unit:*,1255-1260 为 company/department 遗留)
DELETE FROM sys_role_menu WHERE menu_id IN (1255, 1256, 1257, 1258, 1259, 1260);
DELETE FROM sys_menu      WHERE id      IN (1255, 1256, 1257, 1258, 1259, 1260);
