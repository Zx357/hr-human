package com.kadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kadmin.entity.SysApprovalFlow;
import com.kadmin.entity.SysApprovalNode;
import com.kadmin.mapper.SysApprovalFlowMapper;
import com.kadmin.mapper.SysApprovalNodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalFlowService extends ServiceImpl<SysApprovalFlowMapper, SysApprovalFlow> {
    private final SysApprovalNodeMapper nodeMapper;

    public List<SysApprovalFlow> listAll() {
        List<SysApprovalFlow> flows = list(new LambdaQueryWrapper<SysApprovalFlow>().eq(SysApprovalFlow::getDeleted, 0).orderByAsc(SysApprovalFlow::getId));
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
