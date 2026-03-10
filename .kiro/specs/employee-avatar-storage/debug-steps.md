# 员工照片不回显问题 - 调试步骤

## 当前状态
- ✅ 实体类字段已定义：`avatar`, `idCardFront`, `idCardBack`
- ✅ Mapper XML 已修复：显式列出所有字段
- ❌ 编辑员工时照片仍不回显

## 问题分析

### 关键发现
`getEmployeeDetail` 方法使用的是 MyBatis-Plus 的 `getById(id)` 方法：

```java
public HrEmployee getEmployeeDetail(Long id) {
    HrEmployee employee = getById(id);  // ← 这里使用的是默认查询
    if (employee != null) {
        // 加载教育经历、家庭成员等...
    }
    return employee;
}
```

**`getById()` 不会使用我们修改的 `selectPageWithDetails` 查询！**

## 立即排查步骤

### 步骤1：检查数据库中是否有照片数据

在数据库中执行：
```sql
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE employee_no = '003';
```

**预期结果：**
- 如果字段为 `NULL`，说明照片从未上传成功
- 如果有值（如 `/employee_photo/003.jpg`），说明数据存在，是查询问题

### 步骤2：检查物理文件是否存在

检查以下文件是否存在：
```
D:\rzphoto\employee_photo\003.jpg
D:\rzphoto\id_card_front\003.jpg
D:\rzphoto\id_card_back\003.jpg
```

### 步骤3：测试直接访问图片URL

在浏览器中访问：
```
http://localhost:8080/api/employee_photo/003.jpg
http://localhost:8080/api/id_card_front/003.jpg
http://localhost:8080/api/id_card_back/003.jpg
```

如果能看到图片，说明后端配置正确。

### 步骤4：检查前端API响应

1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签
3. 点击编辑员工（工号 003）
4. 查找 `/employee/` 开头的请求
5. 查看响应数据中是否包含 `avatar`, `idCardFront`, `idCardBack` 字段

**预期响应：**
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

## 可能的原因

### 原因1：照片从未上传成功
**症状**：数据库字段为 `NULL`

**解决方案**：
1. 确保填写了工号
2. 重新上传照片
3. 检查后端日志是否有上传成功的消息

### 原因2：数据库字段名不匹配
**症状**：数据库有数据，但 `getById()` 查询不到

**解决方案**：
检查数据库表结构：
```sql
DESCRIBE hr_employee;
```

确认字段名是：
- `avatar`
- `id_card_front`
- `id_card_back`

### 原因3：MyBatis-Plus 配置问题
**症状**：`getById()` 不返回某些字段

**解决方案**：
检查 `application.yml` 或 `application.properties` 中的 MyBatis-Plus 配置：
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 确保开启驼峰命名转换
```

### 原因4：前端没有正确处理照片字段
**症状**：后端返回了数据，但前端不显示

**解决方案**：
检查前端 `handleEdit` 方法：
```typescript
async function handleEdit(row: Api.Hr.Employee) {
  const res = await fetchEmployeeById(row.id!);
  if (res.data) {
    const employee = res.data;
    editingData.value = { ...employee };  // ← 确保包含照片字段
    console.log('员工数据:', employee);  // 添加日志查看
    console.log('头像:', employee.avatar);
    console.log('身份证正面:', employee.idCardFront);
    console.log('身份证反面:', employee.idCardBack);
  }
}
```

## 下一步行动

请按照以下顺序排查：

1. **先查数据库** - 确认数据是否存在
2. **再查文件** - 确认物理文件是否存在
3. **测试URL** - 确认后端映射是否正确
4. **查看Network** - 确认API响应是否包含照片字段
5. **添加日志** - 在前端添加 console.log 查看数据

完成后告诉我结果，我会根据具体情况提供解决方案。
