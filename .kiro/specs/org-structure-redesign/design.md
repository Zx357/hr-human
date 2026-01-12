# Design Document: 组织架构页面重设计

## Overview

重新设计组织架构管理页面，采用左右分栏布局。左侧显示组织树形结构（支持展开/收起、右键菜单操作），右侧显示选中组织的员工统计图表（人数、学历分布、性别分布、年龄分布等）。使用 ECharts 渲染图表。

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        组织架构管理页面                           │
├──────────────────────┬──────────────────────────────────────────┤
│                      │                                          │
│   组织树形结构        │           统计面板                        │
│   (30% width)        │           (70% width)                    │
│                      │                                          │
│   ├─ 集团A           │   ┌─────────────────────────────────┐   │
│   │  ├─ 公司1        │   │  员工总数: 128                   │   │
│   │  │  ├─ 部门1     │   └─────────────────────────────────┘   │
│   │  │  └─ 部门2     │                                          │
│   │  └─ 公司2        │   ┌──────────┐  ┌──────────┐            │
│   │     └─ 部门3     │   │ 学历分布  │  │ 性别分布  │            │
│   └─ 集团B           │   │  (饼图)   │  │  (饼图)   │            │
│      └─ ...          │   └──────────┘  └──────────┘            │
│                      │                                          │
│                      │   ┌──────────────────────────┐          │
│                      │   │      年龄分布 (柱状图)     │          │
│                      │   └──────────────────────────┘          │
│                      │                                          │
│                      │   ┌──────────┐  ┌──────────┐            │
│                      │   │ 在职状态  │  │ 员工类型  │            │
│                      │   │  (饼图)   │  │  (饼图)   │            │
│                      │   └──────────┘  └──────────┘            │
│                      │                                          │
└──────────────────────┴──────────────────────────────────────────┘
```

## Components and Interfaces

### Frontend Components

#### 1. OrgStructure Page Component
主页面组件，管理左右分栏布局和状态。

```typescript
interface OrgStructureState {
  treeData: OrgUnit[];           // 组织树数据
  selectedNode: OrgUnit | null;  // 当前选中的节点
  statistics: OrgStatistics;     // 统计数据
  loading: boolean;              // 加载状态
  statsLoading: boolean;         // 统计加载状态
}
```

#### 2. OrgTree Component
左侧组织树组件，使用 Element Plus Tree 组件。

```typescript
interface OrgTreeProps {
  data: OrgUnit[];
  onSelect: (node: OrgUnit) => void;
  onAdd: (parent?: OrgUnit) => void;
  onEdit: (node: OrgUnit) => void;
  onDelete: (node: OrgUnit) => void;
}
```

#### 3. StatisticsPanel Component
右侧统计面板组件，包含多个 ECharts 图表。

```typescript
interface StatisticsPanelProps {
  statistics: OrgStatistics;
  loading: boolean;
  orgName: string;
}
```

### Backend API

#### 1. 获取组织统计数据
```
GET /org-unit/{id}/statistics
```

Response:
```typescript
interface OrgStatistics {
  totalCount: number;           // 员工总数
  educationDistribution: {      // 学历分布
    name: string;               // 学历名称
    value: number;              // 人数
  }[];
  genderDistribution: {         // 性别分布
    name: string;
    value: number;
  }[];
  ageDistribution: {            // 年龄分布
    range: string;              // 年龄段 (如 "20-25")
    count: number;              // 人数
  }[];
  statusDistribution: {         // 在职状态分布
    name: string;
    value: number;
  }[];
  employeeTypeDistribution: {   // 员工类型分布
    name: string;
    value: number;
  }[];
}
```

#### 2. 获取组织树（带员工数量）
```
GET /org-unit/tree-with-count
```

Response: 在现有 OrgUnit 基础上增加 employeeCount 字段

## Data Models

### OrgStatistics DTO
```java
public class OrgStatisticsDTO {
    private Integer totalCount;
    private List<DistributionItem> educationDistribution;
    private List<DistributionItem> genderDistribution;
    private List<AgeDistributionItem> ageDistribution;
    private List<DistributionItem> statusDistribution;
    private List<DistributionItem> employeeTypeDistribution;
}

public class DistributionItem {
    private String name;
    private Integer value;
}

public class AgeDistributionItem {
    private String range;
    private Integer count;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system.*

### Property 1: 统计数据一致性
*For any* 组织节点, 其员工总数应等于各分布项数值之和
**Validates: Requirements 3.2, 3.3, 3.4, 3.5, 3.6**

### Property 2: 树形结构完整性
*For any* 组织树, 所有节点的 employeeCount 应等于其直属员工数加上所有子节点的 employeeCount 之和
**Validates: Requirements 2.5, 4.4**

### Property 3: 选中状态同步
*For any* 用户选择操作, 选中的节点应与统计面板显示的组织名称一致
**Validates: Requirements 2.2, 3.1**

## Error Handling

1. **网络错误**: 显示错误提示，提供重试按钮
2. **空数据**: 显示"暂无数据"占位图
3. **加载状态**: 显示骨架屏或加载动画
4. **统计计算错误**: 后端返回默认值（0），前端显示"-"

## Testing Strategy

### Unit Tests
- 测试统计数据计算逻辑
- 测试树形结构构建
- 测试年龄计算函数

### Integration Tests
- 测试前后端数据交互
- 测试图表渲染

### Property-Based Tests
- 验证统计数据一致性属性
- 验证树形结构完整性属性
