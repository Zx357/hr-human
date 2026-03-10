# 合同管理功能增强 - 完成总结

## 已完成的工作

### 1. 后端实体更新
- **文件**: `backend/src/main/java/com/kadmin/entity/HrContract.java`
- **修改内容**:
  - 添加 `contractImages` 字段：存储合同图片路径（多张，逗号分隔）
  - 添加 `contractCount` 字段：存储合同次数（第几次合同）

### 2. 前端接口类型更新
- **文件**: `frontend/src/service/api/contract.ts`
- **修改内容**:
  - 在 `Contract` 接口中添加 `contractImages?: string` 字段
  - 在 `Contract` 接口中添加 `contractCount?: number` 字段
  - 在 `fetchContractPage` 参数中添加 `employeeId?: number` 支持按员工ID查询

### 3. 前端页面功能实现
- **文件**: `frontend/src/views/hr/contract/index.vue`
- **新增功能**:

#### 3.1 合同次数自动计算 ✅
- **移除手动输入**：删除了合同次数的输入框
- **自动计算逻辑**：
  - 当用户选择员工时，自动查询该员工的历史合同数量
  - 合同次数 = 该员工现有合同数 + 1
  - 新增时自动计算，编辑时保持原值
- **显示方式**：在表单中以只读方式显示"第X次"
- **实现函数**：`calculateContractCount(employeeId)`

#### 3.2 合同图片上传 ✅
- 支持多图上传（最多9张）
- 每张图片大小限制：5MB
- 图片预览功能
- 支持删除已上传的图片
- 使用 `ElUpload` 组件配合 `uploadImage` API
- 图片路径以逗号分隔存储

#### 3.3 图片回显修复 ✅
- **问题**：编辑时图片不显示
- **原因**：后端SQL查询未返回 `contract_images` 字段
- **解决方案**：
  - 更新 `HrContractMapper.xml` 的 SELECT 语句，添加 `c.contract_images, c.contract_count` 字段
  - 前端 `handleEdit` 和 `handleView` 函数已正确解析图片列表

### 4. 后端更新

#### 4.1 Controller层
- **文件**: `backend/src/main/java/com/kadmin/controller/ContractController.java`
- **修改**：在 `page` 方法中添加 `employeeId` 参数

#### 4.2 Service层
- **文件**: `backend/src/main/java/com/kadmin/service/ContractService.java`
- **修改**：在 `getContractPage` 方法中添加 `employeeId` 参数

#### 4.3 Mapper层
- **文件**: `backend/src/main/java/com/kadmin/mapper/HrContractMapper.java`
- **修改**：在 `selectPageWithEmployee` 方法中添加 `employeeId` 参数

#### 4.4 Mapper XML
- **文件**: `backend/src/main/resources/mapper/HrContractMapper.xml`
- **修改**：
  - SELECT 语句添加 `c.contract_images, c.contract_count` 字段
  - WHERE 条件添加 `employeeId` 过滤

### 5. 数据库迁移脚本
- **文件**: `sql/add_contract_images_and_count.sql`
- **内容**:
  - 添加 `contract_images` 字段（TEXT类型）
  - 添加 `contract_count` 字段（INT类型）

## 使用说明

### 数据库更新
在使用新功能前，需要执行数据库迁移脚本：

```sql
-- 在MySQL中执行
source sql/add_contract_images_and_count.sql;
```

或者直接执行：
```bash
mysql -u your_username -p kadmin < sql/add_contract_images_and_count.sql
```

### 功能使用

#### 合同次数自动计算
1. 在新增合同时，选择员工后自动计算合同次数
2. 系统会查询该员工的历史合同数量，自动填充为"第X次"
3. 编辑合同时，保持原有的合同次数不变
4. 在列表中显示为"第X次"格式

#### 上传合同图片
1. 在新增/编辑合同时，找到"合同图片"字段
2. 点击"+"图标选择图片文件
3. 支持上传最多9张图片
4. 每张图片不超过5MB
5. 可以点击图片右上角的"×"删除图片
6. 编辑时会自动回显已上传的图片

## 技术实现细节

### 合同次数自动计算流程
1. 用户在新增合同时选择员工
2. 触发 `handleSelectEmployee` 函数
3. 调用 `calculateContractCount(employeeId)` 函数
4. 查询该员工的所有合同（通过 `fetchContractPage` 传入 `employeeId`）
5. 计算：合同次数 = 查询结果总数 + 1
6. 自动填充到 `editingData.contractCount`
7. 表单中以只读方式显示"第X次"

### 图片存储方式
- 前端上传图片到后端 `/file/upload/image` 接口
- 后端返回图片相对路径
- 多张图片路径用逗号分隔存储在 `contract_images` 字段
- 显示时使用 `getFileUrl()` 函数获取完整URL

### 图片回显流程
1. 编辑/查看合同时，从后端获取合同数据（包含 `contractImages` 字段）
2. `handleEdit` 或 `handleView` 函数解析 `contractImages` 字符串
3. 使用 `split(',')` 分割为数组
4. 过滤空字符串：`filter(img => img)`
5. 赋值给 `contractImageList` 数组
6. 模板中遍历显示图片

### 数据流转
1. 用户选择图片 → `beforeImageUpload` 验证
2. 验证通过 → `handleContractImageUpload` 上传
3. 上传成功 → 路径添加到 `contractImageList`
4. 保存表单 → `contractImageList.join(',')` 转为字符串
5. 编辑/查看 → `contractImages.split(',')` 解析为数组

## 注意事项

1. **数据库迁移必须先执行**：否则后端会报字段不存在错误
2. **图片上传依赖后端接口**：确保 `/file/upload/image` 接口正常工作
3. **图片存储路径**：确保后端配置的图片存储目录有写入权限
4. **合同次数自动计算**：仅在新增合同时生效，编辑时保持原值
5. **图片数量限制**：最多9张，超过后不显示上传按钮
6. **图片回显**：需要后端SQL查询返回 `contract_images` 字段

## 修复的问题

### 问题1：合同次数需要手动输入 ✅
- **解决方案**：改为自动计算，选择员工后自动查询该员工的合同数量+1

### 问题2：编辑时图片不回显 ✅
- **原因**：后端SQL查询未返回 `contract_images` 字段
- **解决方案**：更新 `HrContractMapper.xml` 的 SELECT 语句，添加该字段

## 后续优化建议

1. 添加图片预览大图功能（点击图片查看大图）
2. 支持图片拖拽排序
3. 添加图片压缩功能（减少存储空间）
4. 支持批量上传图片
5. 添加合同续签功能（自动创建下一次合同）
6. 添加合同模板下载功能
