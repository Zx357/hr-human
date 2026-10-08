package com.kadmin.mobile.domain.vo;

import lombok.Data;

/**
 * 群成员视图对象（移动端群成员管理接口返回）
 */
@Data
public class MobileChatGroupMemberVO {

    /** 员工ID */
    private Long employeeId;

    /** 姓名 */
    private String name;

    /** 头像 */
    private String avatar;

    /** 是否群主 */
    private Boolean isOwner;
}
