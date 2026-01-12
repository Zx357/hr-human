package com.kadmin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.HrApplication;
import com.kadmin.entity.HrEmployee;
import com.kadmin.mapper.EmployeeMapper;
import com.kadmin.mapper.HrApplicationMapper;
import com.kadmin.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService extends ServiceImpl<HrApplicationMapper, HrApplication> {

    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private SysUserMapper sysUserMapper;

    public Page<HrApplication> getPage(int pageNum, int pageSize, String employeeName, String employeeNo, String appType, Integer status, Long employeeId) {
        return baseMapper.selectPageWithEmployee(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType, status, employeeId);
    }
    
    public Page<HrApplication> getPendingPage(int pageNum, int pageSize, String employeeName, String employeeNo, String appType, Long userId) {
        // 获取当前用户的角色ID列表
        List<Long> roleIds = null;
        if (userId != null) {
            roleIds = sysUserMapper.selectRoleIdsByUserId(userId);
        }
        return baseMapper.selectPendingPage(new Page<>(pageNum, pageSize), employeeName, employeeNo, appType, roleIds);
    }

    @Transactional
    public boolean approve(Long id, Integer status, String remark, Long approveBy) {
        HrApplication application = getById(id);
        if (application == null) {
            return false;
        }
        
        // 更新申请状态
        HrApplication entity = new HrApplication();
        entity.setId(id);
        entity.setStatus(status);
        entity.setApproveRemark(remark);
        entity.setApproveBy(approveBy);
        entity.setApproveTime(LocalDateTime.now());
        boolean result = updateById(entity);
        
        // 审批通过时，执行相应的业务逻辑
        if (result && status == 1) {
            String appType = application.getAppType();
            
            if ("regularization".equals(appType)) {
                // 转正：更新员工转正日期和员工类别
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                employee.setRegularDate(application.getRegularDate());
                if (application.getNewEmployeeType() != null && !application.getNewEmployeeType().isEmpty()) {
                    employee.setEmployeeType(application.getNewEmployeeType());
                }
                employeeMapper.updateById(employee);
            } else if ("transfer".equals(appType)) {
                // 调动：更新员工部门、职位（公司通过dept_id向上查找获取）
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                if (application.getToDeptId() != null) {
                    employee.setDeptId(application.getToDeptId());
                }
                if (application.getToPosition() != null && !application.getToPosition().isEmpty()) {
                    employee.setPosition(application.getToPosition());
                }
                employeeMapper.updateById(employee);
            } else if ("resignation".equals(appType)) {
                // 离职：更新员工状态为离职
                HrEmployee employee = new HrEmployee();
                employee.setId(application.getEmployeeId());
                employee.setStatus(2); // 2-离职
                employee.setLeaveDate(application.getLastWorkDate());
                employeeMapper.updateById(employee);
            }
            // 奖励和惩罚暂不需要更新员工信息
        }
        
        return result;
    }
    
    public boolean cancel(Long id) {
        HrApplication entity = new HrApplication();
        entity.setId(id);
        entity.setStatus(3); // 已撤销
        return updateById(entity);
    }
}
