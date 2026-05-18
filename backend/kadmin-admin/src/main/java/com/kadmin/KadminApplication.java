package com.kadmin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * KAdmin 人力资源管理系统启动类
 */
@SpringBootApplication
@MapperScan({
        "com.kadmin.system.mapper",
        "com.kadmin.hr.mapper",
        "com.kadmin.attendance.mapper",
        "com.kadmin.organization.mapper",
        "com.kadmin.mobile.mapper"
})
@EnableScheduling
public class KadminApplication {

    public static void main(String[] args) {
        SpringApplication.run(KadminApplication.class, args);
        System.out.println("====================================");
        System.out.println("  KAdmin HR System Started!");
        System.out.println("  API文档: http://localhost:8080/api/doc.html");
        System.out.println("====================================");
    }
}

