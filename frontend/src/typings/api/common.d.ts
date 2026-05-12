/**
 * Namespace Api
 *
 * All backend api type
 */
declare namespace Api {
  namespace Common {
    /** common params of paginating */
    interface PaginatingCommonParams {
      /** current page number */
      current: number;
      /** page size */
      size: number;
      /** total count */
      total: number;
    }

    /** common params of paginating query list data */
    interface PaginatingQueryRecord<T = any> extends PaginatingCommonParams {
      records: T[];
    }

    /** 分页结果 */
    interface PageResult<T = any> {
      records: T[];
      total: number;
      current: number;
      size: number;
    }

    /** common search params of table */
    type CommonSearchParams = Pick<Common.PaginatingCommonParams, 'current' | 'size'>;

    /**
     * enable status
     *
     * - "1": enabled
     * - "2": disabled
     */
    type EnableStatus = '1' | '2';

    /** common record */
    type CommonRecord<T = any> = {
      /** record id */
      id: number;
      /** record creator */
      createBy: string;
      /** record create time */
      createTime: string;
      /** record updater */
      updateBy: string;
      /** record update time */
      updateTime: string;
      /** record status */
      status: EnableStatus | undefined;
    } & T;
  }

  /** 组织架构相关类型 */
  namespace Organization {
    /** 统一组织节点 */
    interface OrgUnit {
      id: number;
      parentId?: number;
      unitType: number; // 1-集团，2-公司，3-部门
      unitCode: string;
      unitName: string;
      shortName?: string;
      leaderId?: number;
      leaderName?: string;
      phone?: string;
      email?: string;
      address?: string;
      attendanceAddress?: string;
      attendanceLatitude?: number;
      attendanceLongitude?: number;
      attendanceRange?: number;
      description?: string;
      sortOrder?: number;
      unitTypeName?: string;
      createdTime?: string;
      updatedTime?: string;
      children?: OrgUnit[];
      /** 员工数量（用于带数量的组织树） */
      employeeCount?: number;
    }

    /** 组织节点表单 */
    interface OrgUnitForm {
      parentId?: number;
      unitType: number;
      unitCode: string;
      unitName: string;
      shortName?: string;
      leaderId?: number;
      phone?: string;
      email?: string;
      address?: string;
      attendanceAddress?: string;
      attendanceLatitude?: number;
      attendanceLongitude?: number;
      attendanceRange?: number;
      description?: string;
      sortOrder?: number;
    }

    /** 公司（兼容旧接口） */
    interface Company {
      id: number;
      parentId?: number;
      companyCode: string;
      companyName: string;
      shortName?: string;
      legalPerson?: string;
      taxNumber?: string;
      address?: string;
      phone?: string;
      email?: string;
      website?: string;
      description?: string;
      sortOrder?: number;
      status: number;
      createdTime?: string;
      updatedTime?: string;
    }

    /** 公司表单 */
    interface CompanyForm {
      parentId?: number;
      companyCode: string;
      companyName: string;
      shortName?: string;
      legalPerson?: string;
      taxNumber?: string;
      address?: string;
      phone?: string;
      email?: string;
      website?: string;
      description?: string;
      sortOrder?: number;
      status?: number;
    }

    /** 部门 */
    interface Department {
      id: number;
      companyId: number;
      parentId?: number;
      deptCode: string;
      deptName: string;
      leaderId?: number;
      phone?: string;
      email?: string;
      description?: string;
      sortOrder?: number;
      status: number;
      createdTime?: string;
      updatedTime?: string;
      children?: Department[];
    }

    /** 部门表单 */
    interface DepartmentForm {
      companyId: number;
      parentId?: number;
      deptCode: string;
      deptName: string;
      leaderId?: number;
      phone?: string;
      email?: string;
      description?: string;
      sortOrder?: number;
      status?: number;
    }

    /** 组织统计数据 */
    interface OrgStatistics {
      /** 员工总数 */
      totalCount: number;
      /** 学历分布 */
      educationDistribution: DistributionItem[];
      /** 性别分布 */
      genderDistribution: DistributionItem[];
      /** 年龄分布 */
      ageDistribution: AgeDistributionItem[];
      /** 在职状态分布 */
      statusDistribution: DistributionItem[];
      /** 员工类型分布 */
      employeeTypeDistribution: DistributionItem[];
    }

    /** 分布项 */
    interface DistributionItem {
      /** 名称 */
      name: string;
      /** 数值 */
      value: number;
    }

    /** 年龄分布项 */
    interface AgeDistributionItem {
      /** 年龄段 */
      range: string;
      /** 人数 */
      count: number;
    }
  }

  /** 人事管理相关类型 */
  namespace Hr {
    /** 员工 */
    interface Employee {
      id: number;
      /** 人员编号 */
      employeeNo: string;
      /** 姓名 */
      name: string;
      /** Mini app login password */
      password?: string;
      /** Mini app password returned by admin detail API */
      miniAppPassword?: string;
      /** 人员头像照片 */
      avatar?: string;
      /** 身份证正面 */
      idCardFront?: string;
      /** 身份证反面 */
      idCardBack?: string;
      /** 性别（字典值） */
      gender?: string;
      /** 最高学历（字典值） */
      highestEducation?: string;
      /** 入职部门ID */
      deptId?: number;
      /** 民族（字典值） */
      nation?: string;
      /** 身份证号 */
      idCard?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 电话 */
      phone?: string;
      /** 邮箱 */
      email?: string;
      /** 员工类别（字典值） */
      employeeType?: string;
      /** 入职日期 */
      entryDate?: string;
      /** 转正日期 */
      regularDate?: string;
      /** 职务（字典值） */
      duty?: string;
      /** 职位（字典值） */
      position?: string;
      /** 岗位（字典值） */
      post?: string;
      /** 职能（字典值） */
      jobFunction?: string;
      /** 职级（字典值） */
      jobLevel?: string;
      /** 职责（字典值） */
      jobResponsibility?: string;
      /** 职权（字典值） */
      jobAuthority?: string;
      /** 职称（字典值） */
      jobTitle?: string;
      /** 职业（字典值） */
      occupation?: string;
      /** 婚姻状况（字典值） */
      maritalStatus?: string;
      /** 政治面貌（字典值） */
      politicalStatus?: string;
      /** 籍贯 */
      nativePlace?: string;
      /** 派出所 */
      policeStation?: string;
      /** 户籍地址 */
      registeredAddress?: string;
      /** 家庭地址 */
      homeAddress?: string;
      /** 现居住地 */
      currentAddress?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系人关系（字典值） */
      emergencyRelation?: string;
      /** 紧急联系人电话 */
      emergencyPhone?: string;
      /** 离职日期 */
      leaveDate?: string;
      /** 状态：1-在职，2-离职 */
      status?: number;
      /** 所属公司ID */
      companyId?: number;
      /** 部门名称 */
      deptName?: string;
      /** 公司名称 */
      companyName?: string;
      /** 创建时间 */
      createdTime?: string;
      /** 更新时间 */
      updatedTime?: string;
      /** 教育经历列表 */
      educationList?: Education[];
      /** 家庭成员列表 */
      familyMemberList?: FamilyMember[];
      /** 工作经历列表 */
      workExperienceList?: WorkExperience[];
      /** 证书列表 */
      certificateList?: Certificate[];
      /** 扩展字段列表 */
      extraFieldList?: ExtraField[];
    }

    /** 员工表单 */
    interface EmployeeForm {
      id?: number;
      /** 人员编号 */
      employeeNo: string;
      /** 姓名 */
      name: string;
      /** Mini app login password */
      password?: string;
      /** 人员头像照片 */
      avatar?: string;
      /** 身份证正面 */
      idCardFront?: string;
      /** 身份证反面 */
      idCardBack?: string;
      /** 性别（字典值） */
      gender?: string;
      /** 最高学历（字典值） */
      highestEducation?: string;
      /** 入职部门ID */
      deptId?: number;
      /** 民族（字典值） */
      nation?: string;
      /** 身份证号 */
      idCard?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 电话 */
      phone?: string;
      /** 邮箱 */
      email?: string;
      /** 员工类别（字典值） */
      employeeType?: string;
      /** 入职日期 */
      entryDate?: string;
      /** 转正日期 */
      regularDate?: string;
      /** 职务（字典值） */
      duty?: string;
      /** 职位（字典值） */
      position?: string;
      /** 岗位（字典值） */
      post?: string;
      /** 职能（字典值） */
      jobFunction?: string;
      /** 职级（字典值） */
      jobLevel?: string;
      /** 职责（字典值） */
      jobResponsibility?: string;
      /** 职权（字典值） */
      jobAuthority?: string;
      /** 职称（字典值） */
      jobTitle?: string;
      /** 职业（字典值） */
      occupation?: string;
      /** 婚姻状况（字典值） */
      maritalStatus?: string;
      /** 政治面貌（字典值） */
      politicalStatus?: string;
      /** 籍贯 */
      nativePlace?: string;
      /** 派出所 */
      policeStation?: string;
      /** 户籍地址 */
      registeredAddress?: string;
      /** 家庭地址 */
      homeAddress?: string;
      /** 现居住地 */
      currentAddress?: string;
      /** 紧急联系人 */
      emergencyContact?: string;
      /** 紧急联系人关系（字典值） */
      emergencyRelation?: string;
      /** 紧急联系人电话 */
      emergencyPhone?: string;
      /** 离职日期 */
      leaveDate?: string;
      /** 状态：1-在职，2-离职 */
      status?: number;
      /** 所属公司ID */
      companyId?: number;
      /** 教育经历列表 */
      educationList?: Education[];
      /** 家庭成员列表 */
      familyMemberList?: FamilyMember[];
      /** 工作经历列表 */
      workExperienceList?: WorkExperience[];
      /** 证书列表 */
      certificateList?: Certificate[];
      /** 扩展字段列表 */
      extraFieldList?: ExtraField[];
    }

    /** 教育经历 */
    interface Education {
      id?: number;
      employeeId?: number;
      /** 学校名称 */
      schoolName?: string;
      /** 毕业证照片 */
      diplomaPhoto?: string;
      /** 是否全日制（字典值） */
      isFullTime?: string;
      /** 学历（字典值） */
      education?: string;
      /** 专业 */
      major?: string;
      /** 开学时间 */
      startDate?: string;
      /** 毕业时间 */
      endDate?: string;
    }

    /** 家庭成员 */
    interface FamilyMember {
      id?: number;
      employeeId?: number;
      /** 姓名 */
      name?: string;
      /** 关系（字典值） */
      relation?: string;
      /** 出生日期 */
      birthDate?: string;
      /** 政治面貌（字典值） */
      politicalStatus?: string;
      /** 工作单位 */
      workUnit?: string;
      /** 职业 */
      occupation?: string;
      /** 电话 */
      phone?: string;
    }

    /** 工作经历 */
    interface WorkExperience {
      id?: number;
      employeeId?: number;
      /** 公司名称 */
      companyName?: string;
      /** 公司地址 */
      companyAddress?: string;
      /** 所在部门 */
      department?: string;
      /** 职位 */
      position?: string;
      /** 证明人 */
      witness?: string;
      /** 证明电话 */
      witnessPhone?: string;
      /** 开始日期 */
      startDate?: string;
      /** 结束日期 */
      endDate?: string;
    }

    /** 证书 */
    interface Certificate {
      id?: number;
      employeeId?: number;
      /** 证书名称 */
      certName?: string;
      /** 证书照片 */
      certPhoto?: string;
      /** 证书类型（字典值） */
      certType?: string;
      /** 证书等级（字典值） */
      certLevel?: string;
      /** 颁发日期 */
      issueDate?: string;
      /** 过期日期 */
      expireDate?: string;
    }

    /** 扩展字段 */
    interface ExtraField {
      id?: number;
      employeeId?: number;
      /** 字段编码 */
      fieldCode: string;
      /** 字段值 */
      fieldValue?: string;
    }
  }

  /** 系统管理相关类型 */
  namespace System {
    /** 用户 */
    interface User {
      id: number;
      username: string;
      nickname?: string;
      email?: string;
      phone?: string;
      gender?: number;
      status: number;
      employeeId?: number;
      createdTime?: string;
      updatedTime?: string;
    }

    /** 角色 */
    interface Role {
      id: number;
      roleCode: string;
      roleName: string;
      description?: string;
      status: number;
      sortOrder?: number;
      dataScope?: number;
      createdTime?: string;
      updatedTime?: string;
    }

    /** 菜单 */
    interface Menu {
      id: number;
      parentId: number;
      menuType: number;
      menuCode: string;
      menuName: string;
      menuNameEn?: string;
      path?: string;
      component?: string;
      permission?: string;
      icon?: string;
      sortOrder: number;
      visible: number;
      status: number;
      createdTime?: string;
      updatedTime?: string;
      children?: Menu[];
    }

    /** 菜单表单 */
    interface MenuForm {
      id?: number;
      parentId: number;
      menuType: number;
      menuCode: string;
      menuName: string;
      menuNameEn?: string;
      path?: string;
      component?: string;
      permission?: string;
      icon?: string;
      sortOrder?: number;
      visible?: number;
      status?: number;
    }

    /** 字典类型 */
    interface DictType {
      id: number;
      dictCode: string;
      dictName: string;
      dictNameEn?: string;
      description?: string;
      status: number;
      createdTime?: string;
      updatedTime?: string;
    }

    /** 字典类型表单 */
    interface DictTypeForm {
      id?: number;
      dictCode: string;
      dictName: string;
      dictNameEn?: string;
      description?: string;
      status?: number;
    }

    /** 字典类型搜索参数 */
    interface DictTypeSearchParams {
      dictCode?: string;
      dictName?: string;
      status?: number;
      current?: number;
      size?: number;
    }

    /** 字典类型列表 */
    interface DictTypeList {
      records: DictType[];
      total: number;
      current: number;
      size: number;
    }

    /** 字典数据 */
    interface DictData {
      id: number;
      dictTypeId: number;
      dictLabel: string;
      dictLabelEn?: string;
      dictValue: string;
      cssClass?: string;
      listClass?: string;
      sortOrder: number;
      isDefault: number;
      status: number;
      remark?: string;
      createdTime?: string;
      updatedTime?: string;
    }

    /** 字典数据表单 */
    interface DictDataForm {
      id?: number;
      dictTypeId: number;
      dictLabel: string;
      dictLabelEn?: string;
      dictValue: string;
      cssClass?: string;
      listClass?: string;
      sortOrder?: number;
      isDefault?: number;
      status?: number;
      remark?: string;
    }
  }
}
