package com.kadmin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kadmin.entity.SysMenu;
import com.kadmin.entity.SysNotice;
import com.kadmin.entity.SysUser;
import com.kadmin.mapper.SysMenuMapper;
import com.kadmin.mapper.SysNoticeMapper;
import com.kadmin.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    @Override
    public void run(String... args) {
        ensureAdminUser();
        ensureFeedbackTable();
        ensureMobileMomentTables();
        ensureMobileContactTables();
        ensureFeedbackMenu();
        ensureDefaultSystemNotice();
        ensureDefaultMomentPosts();
        ensureDefaultMobileGroup();
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
        } else if (!passwordEncoder.matches("admin123", admin.getPassword())) {
            log.info("Updating admin password...");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
            log.info("Admin password updated successfully. Password: admin123");
        } else {
            log.info("Admin user already exists with correct password.");
        }

        log.info("===========================================");
        log.info("Default admin credentials:");
        log.info("Username: admin");
        log.info("Password: admin123");
        log.info("===========================================");
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
    }

    private void ensureMobileContactTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS mobile_contact_request (
                  id BIGINT NOT NULL AUTO_INCREMENT,
                  requester_id BIGINT NOT NULL,
                  target_id BIGINT NOT NULL,
                  status TINYINT NOT NULL DEFAULT 0,
                  remark VARCHAR(255) DEFAULT NULL,
                  handled_time DATETIME DEFAULT NULL,
                  created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  created_by BIGINT DEFAULT NULL,
                  updated_by BIGINT DEFAULT NULL,
                  PRIMARY KEY (id),
                  KEY idx_mobile_contact_request_target (target_id, status),
                  KEY idx_mobile_contact_request_requester (requester_id, status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='移动端联系人申请'
                """);

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
        jdbcTemplate.update("""
                INSERT INTO mobile_chat_group
                    (group_name, owner_id, member_count, last_message, last_message_time, status)
                VALUES (?, ?, ?, ?, NOW(), 1)
                """,
                "默认工作群",
                ownerId,
                employeeIds.size(),
                "欢迎加入默认工作群");

        Long groupId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
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
