package com.kadmin.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 员工视图对象
 */
@Data
public class EmployeeVO {

    private Long id;

    /**
     * 人员编号
     */
    private String employeeNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 人员头像照片
     */
    private String avatar;

    /**
     * 身份证正面
     */
    private String idCardFront;

    /**
     * 身份证反面
     */
    private String idCardBack;

    /**
     * 性别（字典值）
     */
    private String gender;

    /**
     * 最高学历（字典值）
     */
    private String highestEducation;

    /**
     * 入职部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 民族（字典值）
     */
    private String nation;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 员工类别（字典值）
     */
    private String employeeType;

    /**
     * 入职日期
     */
    private LocalDate entryDate;

    /**
     * 转正日期
     */
    private LocalDate regularDate;

    /**
     * 职务（字典值）
     */
    private String duty;

    /**
     * 职位（字典值）
     */
    private String position;

    /**
     * 岗位（字典值）
     */
    private String post;

    /**
     * 职能（字典值）
     */
    private String jobFunction;

    /**
     * 职级（字典值）
     */
    private String jobLevel;

    /**
     * 职责（字典值）
     */
    private String jobResponsibility;

    /**
     * 职权（字典值）
     */
    private String jobAuthority;

    /**
     * 职称（字典值）
     */
    private String jobTitle;

    /**
     * 职业（字典值）
     */
    private String occupation;

    /**
     * 婚姻状况（字典值）
     */
    private String maritalStatus;

    /**
     * 政治面貌（字典值）
     */
    private String politicalStatus;

    /**
     * 籍贯
     */
    private String nativePlace;

    /**
     * 派出所
     */
    private String policeStation;

    /**
     * 户籍地址
     */
    private String registeredAddress;

    /**
     * 家庭地址
     */
    private String homeAddress;

    /**
     * 现居住地
     */
    private String currentAddress;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人关系（字典值）
     */
    private String emergencyRelation;

    /**
     * 紧急联系人电话
     */
    private String emergencyPhone;

    /**
     * 离职日期
     */
    private LocalDate leaveDate;

    /**
     * 状态：1-在职，2-离职
     */
    private Integer status;

    /**
     * 所属公司ID
     */
    private Long companyId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 教育经历列表
     */
    private List<EducationVO> educationList;

    /**
     * 家庭成员列表
     */
    private List<FamilyMemberVO> familyMemberList;

    /**
     * 工作经历列表
     */
    private List<WorkExperienceVO> workExperienceList;

    /**
     * 证书列表
     */
    private List<CertificateVO> certificateList;
}