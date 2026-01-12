# Implementation Plan: 组织架构页面重设计

## Overview

实现左右分栏的组织架构管理页面，左侧显示组织树，右侧显示 ECharts 统计图表。

## Tasks

- [x] 1. 后端：创建统计数据 DTO 和接口
  - [x] 1.1 创建 OrgStatisticsDTO 类
    - 包含 totalCount, educationDistribution, genderDistribution, ageDistribution, statusDistribution, employeeTypeDistribution
    - _Requirements: 4.2_
  - [x] 1.2 在 OrgUnitMapper 添加统计查询方法
    - 添加按部门ID统计员工数量的SQL
    - 添加学历、性别、年龄、状态分布统计SQL
    - _Requirements: 4.1, 4.4_
  - [x] 1.3 在 OrgUnitService 添加统计方法
    - 实现 getStatistics(Long orgId) 方法
    - 支持递归统计子组织员工
    - _Requirements: 4.3, 4.4_
  - [x] 1.4 在 OrgUnitController 添加统计接口
    - 添加 GET /org-unit/{id}/statistics 端点
    - _Requirements: 4.1_

- [x] 2. 后端：添加组织树带员工数量接口
  - [x] 2.1 在 OrgUnitMapper 添加员工数量统计
    - 添加按组织ID统计员工数量的SQL
    - _Requirements: 2.5_
  - [x] 2.2 在 OrgUnitService 添加带数量的树方法
    - 实现 getOrgTreeWithCount() 方法
    - _Requirements: 2.5_
  - [x] 2.3 在 OrgUnitController 添加接口
    - 添加 GET /org-unit/tree-with-count 端点
    - _Requirements: 2.5_

- [x] 3. 前端：添加 API 接口定义
  - [x] 3.1 在 organization.ts 添加统计接口
    - 添加 fetchOrgStatistics(id) 方法
    - 添加 fetchOrgTreeWithCount() 方法
    - 添加 OrgStatistics 类型定义
    - _Requirements: 3.1, 4.1_

- [x] 4. 前端：重构组织架构页面布局
  - [x] 4.1 改为左右分栏布局
    - 使用 flex 布局，左侧30%，右侧70%
    - _Requirements: 1.1_
  - [x] 4.2 实现左侧组织树面板
    - 使用 ElTree 组件
    - 显示组织类型图标和员工数量徽章
    - 支持展开/收起
    - _Requirements: 2.1, 2.3, 2.4, 2.5_
  - [x] 4.3 实现节点选中功能
    - 点击节点高亮并加载统计数据
    - _Requirements: 2.2, 3.1_
  - [x] 4.4 实现右键菜单
    - 添加新增、编辑、删除操作
    - _Requirements: 5.1, 5.2_

- [x] 5. 前端：实现统计面板
  - [x] 5.1 创建统计面板组件结构
    - 顶部显示员工总数
    - 下方显示图表网格
    - _Requirements: 3.2_
  - [x] 5.2 实现学历分布饼图
    - 使用 ECharts 饼图
    - _Requirements: 3.3_
  - [x] 5.3 实现性别分布饼图
    - 使用 ECharts 饼图
    - _Requirements: 3.4_
  - [x] 5.4 实现年龄分布柱状图
    - 使用 ECharts 柱状图
    - _Requirements: 3.5_
  - [x] 5.5 实现在职状态分布饼图
    - 使用 ECharts 饼图
    - _Requirements: 3.6_
  - [x] 5.6 实现员工类型分布饼图
    - 使用 ECharts 饼图
    - _Requirements: 3.6_

- [x] 6. 前端：保留组织管理功能
  - [x] 6.1 保留新增/编辑抽屉表单
    - 复用现有表单逻辑
    - _Requirements: 5.3, 5.4_
  - [x] 6.2 保留删除确认功能
    - 复用现有删除逻辑
    - _Requirements: 5.5_

- [x] 7. Checkpoint - 验证功能
  - 确保后端编译通过
  - 确保前端无语法错误
  - 验证 API 接口正常工作

## Notes

- 使用 ECharts 渲染图表，需要安装 echarts 依赖
- 统计数据需要递归计算子组织的员工
- 年龄分布按 20-25, 26-30, 31-35, 36-40, 41-45, 46-50, 50+ 分段
