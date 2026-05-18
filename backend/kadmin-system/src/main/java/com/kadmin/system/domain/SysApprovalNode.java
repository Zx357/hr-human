package com.kadmin.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_node")
public class SysApprovalNode extends BaseEntity {
    private Long flowId;
    private String nodeName;
    private Integer nodeType; // 1-审批, 2-抄送
    private Integer approverType; // 1-指定角色
    private Long roleId; // 指定角色ID

    @TableField(exist = false)
    private String roleName; // 角色名称
    private Integer sortOrder;
}
