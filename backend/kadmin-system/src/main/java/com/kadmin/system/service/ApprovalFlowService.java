package com.kadmin.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.kadmin.hr.domain.HrApplication;
import com.kadmin.hr.mapper.HrApplicationMapper;
import com.kadmin.system.domain.SysApprovalFlow;
import com.kadmin.system.domain.SysApprovalNode;
import com.kadmin.system.mapper.SysApprovalFlowMapper;
import com.kadmin.system.mapper.SysApprovalNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalFlowService extends ServiceImpl<SysApprovalFlowMapper, SysApprovalFlow> {
    private final SysApprovalNodeMapper nodeMapper;
    private final HrApplicationMapper applicationMapper;

    public List<SysApprovalFlow> listAll() {
        List<SysApprovalFlow> flows = list(
                new LambdaQueryWrapper<SysApprovalFlow>().orderByAsc(SysApprovalFlow::getId));
        flows.forEach(flow -> flow.setNodes(nodeMapper.selectByFlowIdWithApprover(flow.getId())));
        return flows;
    }

    public SysApprovalFlow getDetailById(Long id) {
        SysApprovalFlow flow = getById(id);
        if (flow != null) {
            flow.setNodes(nodeMapper.selectByFlowIdWithApprover(id));
        }
        return flow;
    }

    @Transactional
    public boolean saveFlow(SysApprovalFlow flow) {
        boolean result = save(flow);
        if (result && flow.getNodes() != null) {
            for (int i = 0; i < flow.getNodes().size(); i++) {
                SysApprovalNode node = flow.getNodes().get(i);
                node.setFlowId(flow.getId());
                node.setSortOrder(i + 1);
                nodeMapper.insert(node);
            }
        }
        return result;
    }

    @Transactional
    public boolean updateFlow(SysApprovalFlow flow) {
        SysApprovalFlow existing = getById(flow.getId());
        if (existing == null) {
            throw new IllegalArgumentException("审批流不存在");
        }
        // 节点重建会更换全部节点ID，使在途申请的审批记录失效（流程被判定从头再来）；
        // 类型变更会让旧类型的在途单找不到流程。两者在有在途申请时必须先处理完申请。
        boolean nodeStructureChanged = flow.getNodes() != null;
        boolean appTypeChanged = flow.getFlowType() != null && !flow.getFlowType().equals(existing.getFlowType());
        if (nodeStructureChanged || appTypeChanged) {
            long inFlight = applicationMapper.selectCount(new LambdaQueryWrapper<HrApplication>()
                    .eq(HrApplication::getStatus, 0)
                    .and(w -> w.eq(HrApplication::getAppType, existing.getFlowType())
                            .or(appTypeChanged, x -> x.eq(HrApplication::getAppType, flow.getFlowType()))));
            if (inFlight > 0) {
                throw new IllegalArgumentException(
                        "该流程存在 " + inFlight + " 条在途申请，请先处理完毕再修改流程节点或申请类型（停用不受影响）");
            }
        }
        boolean result = updateById(flow);
        if (result && flow.getNodes() != null) {
            // 删除旧节点
            nodeMapper.delete(new LambdaQueryWrapper<SysApprovalNode>().eq(SysApprovalNode::getFlowId, flow.getId()));
            // 插入新节点
            for (int i = 0; i < flow.getNodes().size(); i++) {
                SysApprovalNode node = flow.getNodes().get(i);
                node.setId(null);
                node.setFlowId(flow.getId());
                node.setSortOrder(i + 1);
                nodeMapper.insert(node);
            }
        }
        return result;
    }

    public boolean updateStatus(Long id, Integer status) {
        SysApprovalFlow flow = new SysApprovalFlow();
        flow.setId(id);
        flow.setStatus(status);
        return updateById(flow);
    }
}
