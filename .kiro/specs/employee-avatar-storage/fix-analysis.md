# 员工照片不回显问题 - 完整分析

## 代码检查结果

### ✅ 后端代码
1. **实体类** (`HrEmployee.java`) - 正确定义了照片字段
   ```java
   private String avatar;
   private String idCardFront;
   private String idCardBack;
   ```

2. **Mapper XML** (`EmployeeMapper.xml`) - 已修复，显式列出所有字段
   ```xml
   SELECT e.id, e.employee_no, e.name, e.avatar, e.id_card_front, e.id_card_back, ...
   ```

3. **Service** (`EmployeeService.java`) - 使用 `getById(id)` 查询
   ```java
   public HrEmployee getEmployeeDetail(Long id) {
       HrEmployee employee = getById(id);  // MyBatis-Plus 默认查询
       // ... 加载其他关联数据
       return employee;
   }
   ```

4. **Controller** (`EmployeeController.java`) - 正确返回员工详情
   ```java
   @GetMapping("/{id}")
   public Result<HrEmployee> getDetail(@PathVariable Long id) {
       HrEmployee employee = employeeService.getEmployeeDetail(id);
       return Result.success(employee);
   }
   ```

### ✅ 前端代码
1. **类型定义** (`common.d.ts`) - 正确定义了照片字段
   ```typescript
   interface Employee {
       avatar?: string;
       idCardFront?: string;
       idCardBack?: string;
       // ...
   }
   ```

2. **API 调用** (`hr.ts`) - 正确调用后端接口
   ```typescript
   export function fetchEmployeeById(id: number) {
       return request<Api.Hr.Employee>({
           url: `/employee/${id}`,
           method: 'get'
       });
   }
   ```

3. **编辑逻辑** (`index.vue`) - 使用对象展开复制所有字段
   ```typescript
   async function handleEdit(row: Api.Hr.Employee) {
       const res = await fetchEmployeeById(row.id!);
       if (res.data) {
           const employee = res.data;
           editingData.value = { ...employee };  // ← 应该包含所有字段
       }
   }
   ```

4. **模板显示** (`index.vue`) - 正确使用 v-if 判断
   ```vue
   <ElImage v-if="editingData.avatar" :src="getFileUrl(editingData.avatar)" />
   <ElImage v-if="editingData.idCardFront" :src="getFileUrl(editingData.idCardFront)" />
   <ElImage v-if="editingData.idCardBack" :src="getFileUrl(editingData.idCardBack)" />
   ```

5. **URL 生成** (`file.ts`) - 正确拼接完整 URL
   ```typescript
   export function getFileUrl(path: string | undefined | null): string {
       if (!path) return '';
       if (path.startsWith('http://') || path.startsWith('https://')) {
           return path;
       }
       const baseUrl = import.meta.env.VITE_SERVICE_BASE_URL || '';
       return `${baseUrl}${path}`;
   }
   ```

## 问题定位

代码逻辑都是正确的！问题可能出在以下几个地方：

### 可能原因 1：数据库中没有照片数据
**症状**：`avatar`, `id_card_front`, `id_card_back` 字段为 `NULL`

**排查方法**：
```sql
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE employee_no = '003';
```

**如果字段为 NULL**：
- 照片从未上传成功
- 或者上传时没有保存到数据库

### 可能原因 2：后端返回的数据中没有照片字段
**症状**：API 响应中缺少 `avatar`, `idCardFront`, `idCardBack` 字段

**排查方法**：
1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签
3. 点击编辑员工
4. 找到 `/employee/{id}` 请求
5. 查看 Response 数据

**预期响应**：
```json
{
    "code": 200,
    "data": {
        "id": 1,
        "employeeNo": "003",
        "name": "003",
        "avatar": "/employee_photo/003.jpg",
        "idCardFront": "/id_card_front/003.jpg",
        "idCardBack": "/id_card_back/003.jpg",
        ...
    }
}
```

### 可能原因 3：前端没有正确接收数据
**症状**：后端返回了数据，但前端 `editingData.value` 中没有

**排查方法**：
在 `handleEdit` 函数中添加 console.log：
```typescript
async function handleEdit(row: Api.Hr.Employee) {
    operateType.value = 'edit';
    loading.value = true;
    try {
        const res = await fetchEmployeeById(row.id!);
        if (res.data) {
            const employee = res.data;
            console.log('=== 员工详情数据 ===');
            console.log('完整数据:', employee);
            console.log('头像:', employee.avatar);
            console.log('身份证正面:', employee.idCardFront);
            console.log('身份证反面:', employee.idCardBack);
            
            editingData.value = { ...employee };
            
            console.log('=== editingData 赋值后 ===');
            console.log('editingData.avatar:', editingData.value.avatar);
            console.log('editingData.idCardFront:', editingData.value.idCardFront);
            console.log('editingData.idCardBack:', editingData.value.idCardBack);
            
            // ... 其他代码
        }
    } catch (error) {
        console.error('获取员工详情失败:', error);
    } finally {
        loading.value = false;
    }
}
```

### 可能原因 4：MyBatis-Plus 配置问题
**症状**：`getById()` 不返回某些字段

**排查方法**：
检查 `application.yml` 或 `application.properties`：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 必须开启
  global-config:
    db-config:
      logic-delete-field: deleted  # 逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 可能原因 5：数据库字段名不匹配
**症状**：数据库字段名与实体类不一致

**排查方法**：
```sql
DESCRIBE hr_employee;
```

**确认字段名**：
- `avatar` (不是 `photo` 或其他)
- `id_card_front` (不是 `idcard_front` 或其他)
- `id_card_back` (不是 `idcard_back` 或其他)

## 立即行动方案

### 方案 A：添加前端调试日志（推荐）
修改 `frontend/src/views/hr/employee/index.vue` 的 `handleEdit` 函数，添加上面的 console.log。

然后：
1. 重启前端（如果需要）
2. 打开浏览器控制台（F12 → Console）
3. 点击编辑员工
4. 查看控制台输出

**根据输出判断**：
- 如果 `employee.avatar` 有值，但 `editingData.value.avatar` 没有 → 前端赋值问题
- 如果 `employee.avatar` 没有值 → 后端返回问题
- 如果控制台没有任何输出 → API 调用失败

### 方案 B：检查数据库（最直接）
```sql
-- 1. 检查数据是否存在
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE employee_no = '003';

-- 2. 如果字段为 NULL，检查是否有其他员工有照片
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE avatar IS NOT NULL 
   OR id_card_front IS NOT NULL 
   OR id_card_back IS NOT NULL
LIMIT 5;
```

### 方案 C：测试新员工（验证流程）
1. 新增一个测试员工（工号：`TEST999`）
2. 上传头像、身份证正面、身份证反面
3. 保存
4. 立即编辑该员工
5. 检查照片是否显示

**如果新员工正常**：
- 说明代码没问题
- 旧数据需要重新上传

**如果新员工也不正常**：
- 说明代码有问题
- 需要进一步调试

## 下一步

请选择一个方案执行，然后告诉我结果：
1. 方案 A 的控制台输出
2. 方案 B 的 SQL 查询结果
3. 方案 C 的测试结果

我会根据你的反馈提供精准的解决方案！
