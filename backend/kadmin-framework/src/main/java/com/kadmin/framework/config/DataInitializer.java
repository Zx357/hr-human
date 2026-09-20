package com.kadmin.framework.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.system.domain.SysMenu;
import com.kadmin.system.domain.SysNotice;
import com.kadmin.system.domain.SysUser;
import com.kadmin.system.mapper.SysMenuMapper;
import com.kadmin.system.mapper.SysNoticeMapper;
import com.kadmin.system.mapper.SysUserMapper;
import com.kadmin.system.service.FileConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数据初始化器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysNoticeMapper noticeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final FileConfigService fileConfigService;

    @Override
    public void run(String... args) {
        ensureAdminUser();
        migrateEmployeePasswords();
        ensureApprovalRecordTable();
        ensureChatMessageSchema();
        ensureFeedbackTable();
        ensureNotificationTables();
        ensureMobileMomentTables();
        ensureMobileContactTables();
        ensureFeedbackMenu();
        ensureNotificationManagementMenu();
        ensureMobileManagementMenu();
        ensureOperLogMenu();
        ensureDefaultSystemNotice();
        ensureDefaultMomentPosts();
        ensureDefaultMobileGroup();
        renameGuessablePhotoFiles();
    }

    private void ensureAdminUser() {
        SysUser admin = userMapper.selectByUsername("admin");

        if (admin == null) {
            log.info("Creating admin user...");
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setStatus(1);
            userMapper.insert(admin);
            log.info("Admin user created successfully. Username: admin, Password: admin123");
        }
        // 注意：管理员修改密码后重启不再被重置回默认值
    }

    /**
     * 存量员工明文密码迁移为BCrypt（幂等）：
     * 空密码账号初始化为默认密码的密文，其余明文密码原地升级
     */
    private void migrateEmployeePasswords() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, password FROM hr_employee WHERE password IS NULL OR password = '' OR password NOT LIKE '$2%'");
        int migrated = 0;
        for (Map<String, Object> row : rows) {
            String raw = row.get("password") == null ? null : String.valueOf(row.get("password"));
            if (raw == null || raw.isEmpty()) {
                raw = "123456";
            }
            jdbcTemplate.update("UPDATE hr_employee SET password = ? WHERE id = ?",
                    passwordEncoder.encode(raw), row.get("id"));
            migrated++;
        }
        if (migrated > 0) {
            log.info("已将 {} 个员工的明文密码升级为BCrypt加密存储", migrated);
        }
    }

    private void ensureApprovalRecordTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS hr_approval_record (
                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                  application_id BIGINT NOT NULL COMMENT '申请单ID',
                  node_id BIGINT DEFAULT NULL COMMENT '审批节点ID',
                  node_name VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
                  sort_order INT DEFAULT NULL COMMENT '节点顺序',
                  approver_id BIGINT DEFAULT NULL COMMENT '审批人ID',
                  approver_name VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名',
                  status TINYINT NOT NULL COMMENT '审批动作：1-通过，2-拒绝',
                  comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
                  approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  created_by BIGINT DEFAULT NULL COMMENT '创建人',
                  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
                  PRIMARY KEY (id),
                  KEY idx_hr_approval_record_application (application_id),
                  KEY idx_hr_approval_record_node (node_id),
                  UNIQUE KEY uk_hr_approval_record_node (application_id, node_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='申请审批流转记录'
                """);

        // 存量表补唯一键（防并发重复审批记录）
        Integer ukCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'hr_approval_record' AND INDEX_NAME = 'uk_hr_approval_record_node'",
                Integer.class);
        if (ukCount == null || ukCount == 0) {
            try {
                jdbcTemplate.execute("ALTER TABLE hr_approval_record "
                        + "ADD UNIQUE KEY uk_hr_approval_record_node (application_id, node_id)");
                log.info("已为 hr_approval_record 添加节点唯一键");
            } catch (Exception e) {
                log.warn("添加审批记录唯一键失败（可能存在历史重复数据）: {}", e.getMessage());
            }
        }

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_oper_log (
                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                  module VARCHAR(50) DEFAULT NULL COMMENT '操作模块',
                  action VARCHAR(50) DEFAULT NULL COMMENT '操作类型',
                  request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求方式+路径',
                  user_id BIGINT DEFAULT NULL COMMENT '操作人用户ID',
                  username VARCHAR(50) DEFAULT NULL COMMENT '操作人用户名',
                  ip VARCHAR(64) DEFAULT NULL COMMENT '操作人IP',
                  request_params TEXT COMMENT '请求参数（脱敏截断）',
                  status TINYINT DEFAULT NULL COMMENT '操作结果：0-失败，1-成功',
                  error_msg VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
                  cost_ms BIGINT DEFAULT NULL COMMENT '耗时(毫秒)',
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                  PRIMARY KEY (id),
                  KEY idx_sys_oper_log_user (user_id),
                  KEY idx_sys_oper_log_created (created_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志'
                """);
    }

    /**
     * 聊天消息表补 msg_type 列（1-文本 2-图片）
     */
    private void ensureChatMessageSchema() {
        Integer colCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                        + "AND TABLE_NAME = 'mobile_chat_message' AND COLUMN_NAME = 'msg_type'",
                Integer.class);
        if (colCount == null || colCount == 0) {
            jdbcTemplate.execute("ALTER TABLE mobile_chat_message "
                    + "ADD COLUMN msg_type TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型：1-文本 2-图片' AFTER content");
            log.info("已为 mobile_chat_message 添加 msg_type 列");
        }
    }

    /**
     * 证件照等敏感文件名去枚举化：
     * 历史上传使用"工号.jpg"命名，可被未授权枚举下载；
     * 启动时将此类文件重命名为UUID，并同步更新数据库引用（幂等）
     */
    private void renameGuessablePhotoFiles() {
        // 目录URL前缀 -> (表, 字段) 映射
        Map<String, String[]> targets = Map.of(
                "/employee_photo/", new String[] { "hr_employee", "avatar" },
                "/id_card_front/", new String[] { "hr_employee", "id_card_front" },
                "/id_card_back/", new String[] { "hr_employee", "id_card_back" },
                "/diploma_photo/", new String[] { "hr_education", "diploma_photo" },
                "/cert_photo/", new String[] { "hr_certificate", "cert_photo" });

        for (Map.Entry<String, String[]> target : targets.entrySet()) {
            String urlPrefix = target.getKey();
            String table = target.getValue()[0];
            String column = target.getValue()[1];
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT id, " + column + " AS val FROM " + table + " WHERE " + column + " IS NOT NULL");
                for (Map<String, Object> row : rows) {
                    String val = String.valueOf(row.get("val"));
                    if (!val.startsWith(urlPrefix)) {
                        continue;
                    }
                    String fileName = val.substring(urlPrefix.length());
                    // 仅处理"纯工号.扩展名"形式（可枚举），时间戳命名的保持不变
                    if (!fileName.matches("\\d+\\.(jpg|jpeg|png|webp)")) {
                        continue;
                    }
                    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
                    java.nio.file.Path dir = java.nio.file.Paths.get(
                            fileConfigServiceAbsolutePath(urlPrefix.substring(1, urlPrefix.length() - 1)));
                    java.nio.file.Path source = dir.resolve(fileName);
                    if (!java.nio.file.Files.exists(source)) {
                        continue;
                    }
                    String newName = java.util.UUID.randomUUID().toString().replace("-", "") + "." + extension;
                    java.nio.file.Files.move(source, dir.resolve(newName));
                    jdbcTemplate.update("UPDATE " + table + " SET " + column + " = ? WHERE id = ?",
                            urlPrefix + newName, row.get("id"));
                }
            } catch (Exception e) {
                log.warn("重命名敏感照片失败 {}: {}", urlPrefix, e.getMessage());
            }
        }
    }

    /**
     * 通过FileConfigService获取指定目录的绝对路径（目录名与配置key约定一致）
     */
    private String fileConfigServiceAbsolutePath(String folderName) {
        String key = switch (folderName) {
            case "employee_photo" -> com.kadmin.system.service.FileConfigService.KEY_AVATAR;
            case "id_card_front" -> com.kadmin.system.service.FileConfigService.KEY_ID_FRONT;
            case "id_card_back" -> com.kadmin.system.service.FileConfigService.KEY_ID_BACK;
            case "diploma_photo" -> com.kadmin.system.service.FileConfigService.KEY_DIPLOMA_PHOTO;
            case "cert_photo" -> com.kadmin.system.service.FileConfigService.KEY_CERT_PHOTO;
            default -> com.kadmin.system.service.FileConfigService.KEY_UPLOAD_BASE;
        };
        return fileConfigService.getAbsolutePath(key);
    }

    /**
     * 站内通知与公告已读表
     */
    private void ensureNotificationTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_notification (
                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                  employee_id BIGINT NOT NULL COMMENT '接收员工ID',
                  type VARCHAR(32) NOT NULL COMMENT '通知类型：approval_result-审批结果，approval_todo-待审批，reminder-到期提醒',
                  title VARCHAR(100) NOT NULL COMMENT '标题',
                  content VARCHAR(500) DEFAULT NULL COMMENT '内容',
                  ref_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
                  url VARCHAR(200) DEFAULT NULL COMMENT '移动端跳转路径',
                  read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
                  read_time DATETIME DEFAULT NULL COMMENT '已读时间',
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  created_by BIGINT DEFAULT NULL COMMENT '创建人',
                  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
                  PRIMARY KEY (id),
                  KEY idx_sys_notification_employee (employee_id, read_flag),
                  KEY idx_sys_notification_ref (type, ref_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知'
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_notice_read (
                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                  notice_id BIGINT NOT NULL COMMENT '公告ID',
                  employee_id BIGINT NOT NULL COMMENT '员工ID',
                  read_time DATETIME NOT NULL COMMENT '阅读时间',
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  created_by BIGINT DEFAULT NULL COMMENT '创建人',
                  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_sys_notice_read (notice_id, employee_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录'
                """);
    }

    private void ensureFeedbackTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_feedback (
                  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                  feedback_type TINYINT NOT NULL DEFAULT 1 COMMENT '反馈类型：1-功能建议，2-问题反馈，3-其他',
                  feedback_content TEXT NOT NULL COMMENT '反馈内容',
                  contact_name VARCHAR(50) DEFAULT NULL COMMENT '联系人',
                  contact_phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
                  employee_id BIGINT DEFAULT NULL COMMENT '员工ID',
                  employee_no VARCHAR(50) DEFAULT NULL COMMENT '员工工号',
                  status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理，1-处理中，2-已处理',
                  reply_content TEXT COMMENT '回复内容',
                  reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  created_by BIGINT DEFAULT NULL COMMENT '创建人',
                  updated_by BIGINT DEFAULT NULL COMMENT '更新人',
                  PRIMARY KEY (id),
                  KEY idx_sys_feedback_status (status),
                  KEY idx_sys_feedback_employee_id (employee_id),
                  KEY idx_sys_feedback_created_time (created_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈'
                """);
    }

    private void ensureFeedbackMenu() {
        SysMenu systemMenu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system")
                .last("LIMIT 1"));
        if (systemMenu == null) {
            log.warn("System menu not found, skip feedback menu initialization.");
            return;
        }

        SysMenu feedbackMenu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system_feedback")
                .last("LIMIT 1"));
        if (feedbackMenu == null) {
            feedbackMenu = new SysMenu();
            feedbackMenu.setParentId(systemMenu.getId());
            feedbackMenu.setMenuType(2);
            feedbackMenu.setMenuCode("system_feedback");
            feedbackMenu.setMenuName("意见反馈");
            feedbackMenu.setMenuNameEn("Feedback");
            feedbackMenu.setPath("/system/feedback");
            feedbackMenu.setComponent("view.system_feedback");
            feedbackMenu.setPermission("system:feedback:list");
            feedbackMenu.setIcon("mdi:message-alert-outline");
            feedbackMenu.setSortOrder(8);
            feedbackMenu.setVisible(1);
            feedbackMenu.setStatus(1);
            menuMapper.insert(feedbackMenu);
        }

        ensureFeedbackButton(feedbackMenu.getId(), "system_feedback_reply", "回复反馈", "system:feedback:reply", 1);
        ensureFeedbackButton(feedbackMenu.getId(), "system_feedback_delete", "删除反馈", "system:feedback:delete", 2);
        assignMenuToAdminRoles(feedbackMenu.getId());
    }

    private void ensureNotificationManagementMenu() {
        SysMenu notificationRoot = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "notification")
                .last("LIMIT 1"));
        if (notificationRoot == null) {
            notificationRoot = new SysMenu();
            notificationRoot.setParentId(0L);
            notificationRoot.setMenuType(1);
            notificationRoot.setMenuCode("notification");
            notificationRoot.setMenuName("通知管理");
            notificationRoot.setMenuNameEn("Notification Management");
            notificationRoot.setPath("/notification");
            notificationRoot.setComponent("layout.base");
            notificationRoot.setIcon("mdi:bell-cog");
            notificationRoot.setSortOrder(9);
            notificationRoot.setVisible(1);
            notificationRoot.setStatus(1);
            menuMapper.insert(notificationRoot);
        } else {
            boolean changed = false;
            changed |= setMenuField(notificationRoot, "通知管理", notificationRoot.getMenuName(),
                    notificationRoot::setMenuName);
            changed |= setMenuField(notificationRoot, "Notification Management", notificationRoot.getMenuNameEn(),
                    notificationRoot::setMenuNameEn);
            changed |= setMenuField(notificationRoot, "/notification", notificationRoot.getPath(),
                    notificationRoot::setPath);
            changed |= setMenuField(notificationRoot, "layout.base", notificationRoot.getComponent(),
                    notificationRoot::setComponent);
            changed |= setMenuField(notificationRoot, "mdi:bell-cog", notificationRoot.getIcon(),
                    notificationRoot::setIcon);
            changed |= setMenuField(notificationRoot, 0L, notificationRoot.getParentId(),
                    notificationRoot::setParentId);
            changed |= setMenuField(notificationRoot, 1, notificationRoot.getMenuType(),
                    notificationRoot::setMenuType);
            changed |= setMenuField(notificationRoot, 9, notificationRoot.getSortOrder(),
                    notificationRoot::setSortOrder);
            changed |= setMenuField(notificationRoot, 1, notificationRoot.getVisible(),
                    notificationRoot::setVisible);
            changed |= setMenuField(notificationRoot, 1, notificationRoot.getStatus(), notificationRoot::setStatus);
            if (changed) {
                menuMapper.updateById(notificationRoot);
            }
        }

        SysMenu noticeMenu = ensureNotificationChildMenu(notificationRoot.getId(),
                "system_notice",
                "公告管理",
                "Notice Management",
                "/system/notice",
                "view.system_notice",
                "system:notice:list",
                "mdi:bullhorn",
                1);

        SysMenu feedbackMenu = ensureNotificationChildMenu(notificationRoot.getId(),
                "system_feedback",
                "意见反馈",
                "Feedback",
                "/system/feedback",
                "view.system_feedback",
                "system:feedback:list",
                "mdi:message-alert-outline",
                2);

        assignMenuToAdminRoles(notificationRoot.getId());
        assignMenuToAdminRoles(noticeMenu.getId());
        assignMenuToAdminRoles(feedbackMenu.getId());
    }

    private SysMenu ensureNotificationChildMenu(Long parentId, String menuCode, String menuName, String menuNameEn,
            String path, String component, String permission, String icon, int sortOrder) {
        SysMenu menu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, menuCode)
                .last("LIMIT 1"));
        if (menu == null) {
            menu = new SysMenu();
            menu.setParentId(parentId);
            menu.setMenuType(2);
            menu.setMenuCode(menuCode);
            menu.setMenuName(menuName);
            menu.setMenuNameEn(menuNameEn);
            menu.setPath(path);
            menu.setComponent(component);
            menu.setPermission(permission);
            menu.setIcon(icon);
            menu.setSortOrder(sortOrder);
            menu.setVisible(1);
            menu.setStatus(1);
            menuMapper.insert(menu);
            return menu;
        }

        boolean changed = false;
        changed |= setMenuField(menu, parentId, menu.getParentId(), menu::setParentId);
        changed |= setMenuField(menu, 2, menu.getMenuType(), menu::setMenuType);
        changed |= setMenuField(menu, menuName, menu.getMenuName(), menu::setMenuName);
        changed |= setMenuField(menu, menuNameEn, menu.getMenuNameEn(), menu::setMenuNameEn);
        changed |= setMenuField(menu, path, menu.getPath(), menu::setPath);
        changed |= setMenuField(menu, component, menu.getComponent(), menu::setComponent);
        changed |= setMenuField(menu, permission, menu.getPermission(), menu::setPermission);
        changed |= setMenuField(menu, icon, menu.getIcon(), menu::setIcon);
        changed |= setMenuField(menu, sortOrder, menu.getSortOrder(), menu::setSortOrder);
        changed |= setMenuField(menu, 1, menu.getVisible(), menu::setVisible);
        changed |= setMenuField(menu, 1, menu.getStatus(), menu::setStatus);
        if (changed) {
            menuMapper.updateById(menu);
        }
        return menu;
    }

    private void ensureMobileManagementMenu() {
        SysMenu mobileRoot = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "mobile")
                .last("LIMIT 1"));
        if (mobileRoot == null) {
            mobileRoot = new SysMenu();
            mobileRoot.setParentId(0L);
            mobileRoot.setMenuType(1);
            mobileRoot.setMenuCode("mobile");
            mobileRoot.setMenuName("移动管理");
            mobileRoot.setMenuNameEn("Mobile Management");
            mobileRoot.setPath("/mobile");
            mobileRoot.setComponent("layout.base");
            mobileRoot.setIcon("mdi:cellphone-cog");
            mobileRoot.setSortOrder(10);
            mobileRoot.setVisible(1);
            mobileRoot.setStatus(1);
            menuMapper.insert(mobileRoot);
        } else {
            boolean changed = false;
            changed |= setMenuField(mobileRoot, "移动管理", mobileRoot.getMenuName(), mobileRoot::setMenuName);
            changed |= setMenuField(mobileRoot, "Mobile Management", mobileRoot.getMenuNameEn(), mobileRoot::setMenuNameEn);
            changed |= setMenuField(mobileRoot, "/mobile", mobileRoot.getPath(), mobileRoot::setPath);
            changed |= setMenuField(mobileRoot, "layout.base", mobileRoot.getComponent(), mobileRoot::setComponent);
            changed |= setMenuField(mobileRoot, "mdi:cellphone-cog", mobileRoot.getIcon(), mobileRoot::setIcon);
            changed |= setMenuField(mobileRoot, 0L, mobileRoot.getParentId(), mobileRoot::setParentId);
            changed |= setMenuField(mobileRoot, 1, mobileRoot.getMenuType(), mobileRoot::setMenuType);
            changed |= setMenuField(mobileRoot, 10, mobileRoot.getSortOrder(), mobileRoot::setSortOrder);
            changed |= setMenuField(mobileRoot, 1, mobileRoot.getVisible(), mobileRoot::setVisible);
            changed |= setMenuField(mobileRoot, 1, mobileRoot.getStatus(), mobileRoot::setStatus);
            if (changed) {
                menuMapper.updateById(mobileRoot);
            }
        }

        SysMenu mobileMenu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system_mobile-menu")
                .last("LIMIT 1"));
        if (mobileMenu == null) {
            mobileMenu = new SysMenu();
            mobileMenu.setParentId(mobileRoot.getId());
            mobileMenu.setMenuType(2);
            mobileMenu.setMenuCode("system_mobile-menu");
            mobileMenu.setMenuName("移动端菜单");
            mobileMenu.setMenuNameEn("Mobile Menu");
            mobileMenu.setPath("/system/mobile-menu");
            mobileMenu.setComponent("view.system_mobile-menu");
            mobileMenu.setPermission("system:mobile-menu:list");
            mobileMenu.setIcon("mdi:cellphone-text");
            mobileMenu.setSortOrder(1);
            mobileMenu.setVisible(1);
            mobileMenu.setStatus(1);
            menuMapper.insert(mobileMenu);
        } else {
            boolean changed = false;
            changed |= setMenuField(mobileMenu, mobileRoot.getId(), mobileMenu.getParentId(), mobileMenu::setParentId);
            changed |= setMenuField(mobileMenu, 2, mobileMenu.getMenuType(), mobileMenu::setMenuType);
            changed |= setMenuField(mobileMenu, "移动端菜单", mobileMenu.getMenuName(), mobileMenu::setMenuName);
            changed |= setMenuField(mobileMenu, "Mobile Menu", mobileMenu.getMenuNameEn(), mobileMenu::setMenuNameEn);
            changed |= setMenuField(mobileMenu, "/system/mobile-menu", mobileMenu.getPath(), mobileMenu::setPath);
            changed |= setMenuField(mobileMenu, "view.system_mobile-menu", mobileMenu.getComponent(), mobileMenu::setComponent);
            changed |= setMenuField(mobileMenu, "system:mobile-menu:list", mobileMenu.getPermission(), mobileMenu::setPermission);
            changed |= setMenuField(mobileMenu, "mdi:cellphone-text", mobileMenu.getIcon(), mobileMenu::setIcon);
            changed |= setMenuField(mobileMenu, 1, mobileMenu.getSortOrder(), mobileMenu::setSortOrder);
            changed |= setMenuField(mobileMenu, 1, mobileMenu.getVisible(), mobileMenu::setVisible);
            changed |= setMenuField(mobileMenu, 1, mobileMenu.getStatus(), mobileMenu::setStatus);
            if (changed) {
                menuMapper.updateById(mobileMenu);
            }
        }

        SysMenu addButton = ensureMenuButton(mobileMenu.getId(), "system_mobile-menu_add", "新增移动菜单",
                "system:mobile-menu:add", 1);
        SysMenu editButton = ensureMenuButton(mobileMenu.getId(), "system_mobile-menu_edit", "编辑移动菜单",
                "system:mobile-menu:edit", 2);
        SysMenu deleteButton = ensureMenuButton(mobileMenu.getId(), "system_mobile-menu_delete", "删除移动菜单",
                "system:mobile-menu:delete", 3);

        assignMenuToAdminRoles(mobileRoot.getId());
        assignMenuToAdminRoles(mobileMenu.getId());
        assignMenuToAdminRoles(addButton.getId());
        assignMenuToAdminRoles(editButton.getId());
        assignMenuToAdminRoles(deleteButton.getId());
    }

    private void ensureOperLogMenu() {
        SysMenu systemMenu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system")
                .last("LIMIT 1"));
        if (systemMenu == null) {
            log.warn("System menu not found, skip oper-log menu initialization.");
            return;
        }

        SysMenu operLogMenu = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system_oper-log")
                .last("LIMIT 1"));
        if (operLogMenu == null) {
            operLogMenu = new SysMenu();
            operLogMenu.setParentId(systemMenu.getId());
            operLogMenu.setMenuType(2);
            operLogMenu.setMenuCode("system_oper-log");
            operLogMenu.setMenuName("操作日志");
            operLogMenu.setMenuNameEn("Operation Log");
            operLogMenu.setPath("/system/oper-log");
            operLogMenu.setComponent("view.system_oper-log");
            operLogMenu.setPermission("system:oper-log:list");
            operLogMenu.setIcon("mdi:text-box-outline");
            operLogMenu.setSortOrder(9);
            operLogMenu.setVisible(1);
            operLogMenu.setStatus(1);
            menuMapper.insert(operLogMenu);
        }

        SysMenu cleanButton = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, "system_oper-log_clean")
                .last("LIMIT 1"));
        if (cleanButton == null) {
            cleanButton = new SysMenu();
            cleanButton.setParentId(operLogMenu.getId());
            cleanButton.setMenuType(3);
            cleanButton.setMenuCode("system_oper-log_clean");
            cleanButton.setMenuName("清理日志");
            cleanButton.setMenuNameEn("Clean Oper Logs");
            cleanButton.setPermission("system:oper-log:clean");
            cleanButton.setSortOrder(1);
            cleanButton.setVisible(1);
            cleanButton.setStatus(1);
            menuMapper.insert(cleanButton);
        }

        assignMenuToAdminRoles(operLogMenu.getId());
        assignMenuToAdminRoles(cleanButton.getId());
    }

    private void ensureFeedbackButton(Long parentId, String menuCode, String menuName, String permission, int sortOrder) {
        Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getMenuCode, menuCode));
        if (count != null && count > 0) {
            return;
        }

        SysMenu button = new SysMenu();
        button.setParentId(parentId);
        button.setMenuType(3);
        button.setMenuCode(menuCode);
        button.setMenuName(menuName);
        button.setMenuNameEn(menuName);
        button.setPermission(permission);
        button.setSortOrder(sortOrder);
        button.setVisible(1);
        button.setStatus(1);
        menuMapper.insert(button);
    }

    private SysMenu ensureMenuButton(Long parentId, String menuCode, String menuName, String permission, int sortOrder) {
        SysMenu button = menuMapper.selectOne(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getMenuCode, menuCode)
                .last("LIMIT 1"));
        if (button == null) {
            button = new SysMenu();
            button.setParentId(parentId);
            button.setMenuType(3);
            button.setMenuCode(menuCode);
            button.setMenuName(menuName);
            button.setMenuNameEn(menuName);
            button.setPermission(permission);
            button.setSortOrder(sortOrder);
            button.setVisible(1);
            button.setStatus(1);
            menuMapper.insert(button);
            return button;
        }

        boolean changed = false;
        changed |= setMenuField(button, parentId, button.getParentId(), button::setParentId);
        changed |= setMenuField(button, 3, button.getMenuType(), button::setMenuType);
        changed |= setMenuField(button, menuName, button.getMenuName(), button::setMenuName);
        changed |= setMenuField(button, menuName, button.getMenuNameEn(), button::setMenuNameEn);
        changed |= setMenuField(button, permission, button.getPermission(), button::setPermission);
        changed |= setMenuField(button, sortOrder, button.getSortOrder(), button::setSortOrder);
        changed |= setMenuField(button, 1, button.getVisible(), button::setVisible);
        changed |= setMenuField(button, 1, button.getStatus(), button::setStatus);
        if (changed) {
            menuMapper.updateById(button);
        }
        return button;
    }

    private <T> boolean setMenuField(SysMenu menu, T expected, T actual, java.util.function.Consumer<T> setter) {
        if (Objects.equals(expected, actual)) {
            return false;
        }
        setter.accept(expected);
        return true;
    }

    private void assignMenuToAdminRoles(Long menuId) {
        jdbcTemplate.update("""
                INSERT INTO sys_role_menu (role_id, menu_id)
                SELECT r.id, ?
                FROM sys_role r
                WHERE (r.role_code IN ('admin', 'ROLE_ADMIN') OR r.role_name LIKE '%管理员%')
                  AND NOT EXISTS (
                    SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = ?
                  )
                """, menuId, menuId);
    }

    private void ensureDefaultSystemNotice() {
        Long count = noticeMapper.selectCount(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getNoticeTitle, "系统公告"));
        if (count != null && count > 0) {
            return;
        }

        SysNotice notice = new SysNotice();
        notice.setNoticeTitle("系统公告");
        notice.setNoticeType(1);
        notice.setStatus(1);
        notice.setPublishTime(LocalDate.now());
        notice.setNoticeContent("""
                欢迎使用人资OA移动端。

                目前已上线登录、工作台、考勤打卡、请假/加班/补卡/离职/出差/换休申请、待办审批、通知公告、个人信息和意见反馈等功能。

                如遇到登录、定位、审批或资料显示问题，请先在“我的 - 意见反馈”提交问题，管理员会在后台系统管理中查看并处理。
                """);
        noticeMapper.insert(notice);
    }

    private void ensureMobileMomentTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_moment_post (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  employee_id BIGINT NOT NULL,
                  content TEXT,
                  labels VARCHAR(255) DEFAULT NULL,
                  images TEXT,
                  visibility TINYINT NOT NULL DEFAULT 1,
                  status TINYINT NOT NULL DEFAULT 1,
                  comment_count INT NOT NULL DEFAULT 0,
                  like_count INT NOT NULL DEFAULT 0,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  KEY idx_mobile_moment_post_employee (employee_id),
                  KEY idx_mobile_moment_post_status_time (status, created_time),
                  KEY idx_mobile_moment_post_hot (like_count, comment_count)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端时光动态'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_moment_like (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  post_id BIGINT NOT NULL,
                  employee_id BIGINT NOT NULL,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_mobile_moment_like_post_employee (post_id, employee_id),
                  KEY idx_mobile_moment_like_employee (employee_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端时光点赞'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_moment_comment (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  post_id BIGINT NOT NULL,
                  employee_id BIGINT NOT NULL,
                  content VARCHAR(1000) NOT NULL,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  KEY idx_mobile_moment_comment_post (post_id),
                  KEY idx_mobile_moment_comment_employee (employee_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端时光评论'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_moment_view (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  employee_id BIGINT NOT NULL,
                  last_view_time DATETIME DEFAULT NULL,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_mobile_moment_view_employee (employee_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端时光消息查看时间'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_chat_read_state (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  employee_id BIGINT NOT NULL,
                  chat_type TINYINT NOT NULL,
                  target_id BIGINT NOT NULL,
                  last_read_message_id BIGINT NOT NULL DEFAULT 0,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_mobile_chat_read_state (employee_id, chat_type, target_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端聊天会话已读状态'
                """);
    }

    private void ensureMobileContactTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_chat_group (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  group_name VARCHAR(100) NOT NULL,
                  owner_id BIGINT NOT NULL,
                  avatar VARCHAR(255) DEFAULT NULL,
                  member_count INT NOT NULL DEFAULT 0,
                  last_message VARCHAR(255) DEFAULT NULL,
                  last_message_time DATETIME DEFAULT NULL,
                  status TINYINT NOT NULL DEFAULT 1,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  KEY idx_mobile_chat_group_owner (owner_id),
                  KEY idx_mobile_chat_group_status_time (status, last_message_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端群聊'
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_chat_group_member (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  group_id BIGINT NOT NULL,
                  employee_id BIGINT NOT NULL,
                  role_type TINYINT NOT NULL DEFAULT 2,
                  status TINYINT NOT NULL DEFAULT 1,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  UNIQUE KEY uk_mobile_chat_group_member (group_id, employee_id),
                  KEY idx_mobile_chat_group_member_employee (employee_id, status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端群聊成员'
                """);
    }

    private void ensureDefaultMomentPosts() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mobile_moment_post", Long.class);
        if (count != null && count > 0) {
            return;
        }

        List<Long> employeeIds = jdbcTemplate.queryForList("""
                SELECT id FROM hr_employee
                WHERE status = 1
                ORDER BY id ASC
                LIMIT 3
                """, Long.class);
        if (employeeIds.isEmpty()) {
            return;
        }

        Long firstEmployeeId = employeeIds.get(0);
        jdbcTemplate.update("""
                INSERT INTO mobile_moment_post
                    (employee_id, content, labels, images, comment_count, like_count)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                firstEmployeeId,
                "欢迎来到时光，这里会展示团队动态、活动记录和同事分享。",
                "团队,公告",
                "https://resource.tuniaokj.com/images/swiper/banner-animate3.png",
                2,
                8);

        Long secondEmployeeId = employeeIds.size() > 1 ? employeeIds.get(1) : firstEmployeeId;
        jdbcTemplate.update("""
                INSERT INTO mobile_moment_post
                    (employee_id, content, labels, images, comment_count, like_count)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                secondEmployeeId,
                "今天完成了移动端首页、时光和通讯录的接口联调，后面可以继续补评论和消息提醒。",
                "研发,移动端",
                "https://resource.tuniaokj.com/images/simple/image3.jpg,https://resource.tuniaokj.com/images/simple/image8.jpg",
                4,
                12);

        Long thirdEmployeeId = employeeIds.size() > 2 ? employeeIds.get(2) : firstEmployeeId;
        jdbcTemplate.update("""
                INSERT INTO mobile_moment_post
                    (employee_id, content, labels, images, comment_count, like_count)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                thirdEmployeeId,
                "组织架构和通讯录已经可以读取真实员工数据，群聊与好友申请也有了后端存储。",
                "人事,通讯录",
                "",
                1,
                6);
    }

    private void ensureDefaultMobileGroup() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mobile_chat_group", Long.class);
        if (count != null && count > 0) {
            return;
        }

        List<Long> employeeIds = jdbcTemplate.queryForList("""
                SELECT id FROM hr_employee
                WHERE status = 1
                ORDER BY id ASC
                LIMIT 20
                """, Long.class);
        if (employeeIds.isEmpty()) {
            return;
        }

        Long ownerId = employeeIds.get(0);
        org.springframework.jdbc.support.GeneratedKeyHolder keyHolder =
                new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO mobile_chat_group (group_name, owner_id, member_count, last_message, last_message_time, status) VALUES (?, ?, ?, ?, NOW(), 1)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "默认工作群");
            ps.setLong(2, ownerId);
            ps.setInt(3, employeeIds.size());
            ps.setString(4, "欢迎加入默认工作群");
            return ps;
        }, keyHolder);
        Long groupId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
        if (groupId == null) {
            log.warn("默认群聊创建失败：未取到自增主键");
            return;
        }
        for (Long employeeId : employeeIds) {
            jdbcTemplate.update("""
                    INSERT IGNORE INTO mobile_chat_group_member
                        (group_id, employee_id, role_type, status)
                    VALUES (?, ?, ?, 1)
                    """,
                    groupId,
                    employeeId,
                    employeeId.equals(ownerId) ? 1 : 2);
        }
    }

}
