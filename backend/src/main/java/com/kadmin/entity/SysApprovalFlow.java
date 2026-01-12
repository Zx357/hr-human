package com.kadmin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kadmin.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_approval_flow")
public class SysApprovalFlow extends BaseEntity {
    private String flowCode;
    private String flowName;
    private String flowType; // leave, overtime, business, makeup, exchange, regularization, resignation, transfer, reward
    private String description;
    private Integer status; // 1-启用, 0-停用
    private Integer autoPass; // 1-免审批直接通过, 0-需要审批

    @TableField(exist = false)
    private List<SysApprovalNode> nodes;
}
