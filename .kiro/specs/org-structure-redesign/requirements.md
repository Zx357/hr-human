# Requirements Document

## Introduction

重新设计组织架构页面，采用左右分栏布局。左侧显示组织树形结构，右侧显示选中组织的统计图表（人数、学历分布、年龄分布等）。

## Glossary

- **Org_Tree**: 组织架构树形组件，显示集团、公司、部门的层级结构
- **Statistics_Panel**: 统计面板，显示选中组织的员工统计图表
- **ECharts**: 图表库，用于渲染饼图、柱状图等统计图表

## Requirements

### Requirement 1: 左右分栏布局

**User Story:** As a 管理员, I want 左右分栏的组织架构页面, so that 我可以同时查看组织结构和统计信息。

#### Acceptance Criteria

1. THE Page_Layout SHALL display a left panel (30% width) for organization tree and a right panel (70% width) for statistics
2. WHEN the page loads, THE Org_Tree SHALL display the complete organization hierarchy
3. THE Left_Panel SHALL be resizable with a minimum width of 250px

### Requirement 2: 组织树形结构

**User Story:** As a 管理员, I want 在左侧看到完整的组织树, so that 我可以快速浏览和选择组织。

#### Acceptance Criteria

1. THE Org_Tree SHALL display organizations in a hierarchical tree structure
2. WHEN a user clicks on a tree node, THE System SHALL highlight the selected node
3. THE Org_Tree SHALL show organization type icons (集团/公司/部门) for each node
4. THE Org_Tree SHALL support expand/collapse functionality
5. THE Org_Tree SHALL display employee count badge next to each organization name

### Requirement 3: 统计面板

**User Story:** As a 管理员, I want 查看选中组织的员工统计图表, so that 我可以了解组织的人员构成。

#### Acceptance Criteria

1. WHEN a user selects an organization, THE Statistics_Panel SHALL display statistics for that organization
2. THE Statistics_Panel SHALL show total employee count as a prominent number
3. THE Statistics_Panel SHALL display a pie chart for education distribution (学历分布)
4. THE Statistics_Panel SHALL display a pie chart for gender distribution (性别分布)
5. THE Statistics_Panel SHALL display a bar chart for age distribution (年龄分布)
6. THE Statistics_Panel SHALL display a pie chart for employment status distribution (在职状态分布)
7. WHEN no organization is selected, THE Statistics_Panel SHALL show overall company statistics

### Requirement 4: 后端统计接口

**User Story:** As a 系统, I want 提供组织统计数据接口, so that 前端可以获取并展示统计图表。

#### Acceptance Criteria

1. THE Backend SHALL provide an API endpoint `/org-unit/{id}/statistics` to return organization statistics
2. THE Statistics_API SHALL return employee count, education distribution, gender distribution, age distribution, and status distribution
3. WHEN id is 0 or not provided, THE Statistics_API SHALL return overall statistics for all organizations
4. THE Statistics_API SHALL include child organization employees in the count (recursive)

### Requirement 5: 组织管理操作

**User Story:** As a 管理员, I want 在左侧面板管理组织, so that 我可以新增、编辑、删除组织。

#### Acceptance Criteria

1. THE Org_Tree SHALL provide right-click context menu for add/edit/delete operations
2. WHEN a user right-clicks on a node, THE System SHALL show context menu with available actions
3. THE Add_Action SHALL open a drawer form for creating new organization
4. THE Edit_Action SHALL open a drawer form for editing organization details
5. THE Delete_Action SHALL show confirmation dialog before deletion
