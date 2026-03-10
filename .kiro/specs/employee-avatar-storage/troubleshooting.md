# 员工照片回显问题排查指南

## 问题描述
编辑员工时,照片没有回显。

## 已修复的问题

### ✅ 后端Mapper查询缺少照片字段
**问题**：`EmployeeMapper.xml` 中的 `selectPageWithDetails` 查询使用了 `e.*`，在某些数据库配置下可能不会正确返回所有字段。

**修复**：已将 `e.*` 改为显式列出所有字段，包括 `avatar`, `id_card_front`, `id_card_back`。

**验证方法**：
```sql
-- 查看员工详情是否包含照片字段
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE id = {你要编辑的员工ID};
```

## 可能的原因

### 1. 数据库中的照片路径格式不对
**检查方法**：
```sql
SELECT id, employee_no, name, avatar, id_card_front, id_card_back 
FROM hr_employee 
WHERE id = {你要编辑的员工ID};
```

**预期结果**：
- 新格式（正确）：
  - `avatar`: `/employee_photo/001.jpg`
  - `id_card_front`: `/id_card_front/001.jpg`
  - `id_card_back`: `/id_card_back/001.jpg`

- 旧格式（需要更新）：
  - `avatar`: `/uploads/images/2025/01/24/xxxxx.jpg`
  - `id_card_front`: `/uploads/images/2025/01/24/xxxxx.jpg`
  - `id_card_back`: `/uploads/images/2025/01/24/xxxxx.jpg`

**解决方案**：
如果是旧格式，需要：
1. 重新上传照片（会自动使用新路径）
2. 或者手动更新数据库中的路径

### 2. 照片字段为空
**检查方法**：
查看上面的SQL查询结果，如果 `avatar`, `id_card_front`, `id_card_back` 字段为 `NULL`，说明从未上传过照片。

**解决方案**：
上传照片即可。

### 3. 物理文件不存在
**检查方法**：
检查以下目录是否存在文件：
- `D:\rzphoto\employee_photo\{工号}.jpg`
- `D:\rzphoto\id_card_front\{工号}.jpg`
- `D:\rzphoto\id_card_back\{工号}.jpg`

**解决方案**：
如果文件不存在，需要重新上传。

### 4. 后端服务未重启
**检查方法**：
查看后端启动日志，应该看到：
```
静态资源映射: /employee_photo/** -> file:D:\rzphoto\employee_photo\
静态资源映射: /id_card_front/** -> file:D:\rzphoto\id_card_front\
静态资源映射: /id_card_back/** -> file:D:\rzphoto\id_card_back\
```

**解决方案**：
重启后端服务。

### 5. 前端缓存问题
**检查方法**：
1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签
3. 编辑员工，查看是否有图片请求
4. 检查图片请求的URL是否正确

**预期URL格式**：
- `http://localhost:8080/api/employee_photo/001.jpg`
- `http://localhost:8080/api/id_card_front/001.jpg`
- `http://localhost:8080/api/id_card_back/001.jpg`

**解决方案**：
- 清除浏览器缓存
- 或者使用无痕模式测试

## 测试步骤

### 步骤1：新增员工测试
1. 点击"新增员工"
2. 填写员工工号（如：`TEST001`）
3. 填写其他必填信息
4. 上传头像
5. 上传身份证正面
6. 上传身份证反面
7. 保存

**预期结果**：
- 照片保存到：
  - `D:\rzphoto\employee_photo\TEST001.jpg`
  - `D:\rzphoto\id_card_front\TEST001.jpg`
  - `D:\rzphoto\id_card_back\TEST001.jpg`
- 数据库保存路径：
  - `avatar`: `/employee_photo/TEST001.jpg`
  - `id_card_front`: `/id_card_front/TEST001.jpg`
  - `id_card_back`: `/id_card_back/TEST001.jpg`

### 步骤2：编辑员工测试
1. 在员工列表中找到刚才新增的员工
2. 点击"编辑"
3. 检查照片是否正确显示

**预期结果**：
- 头像、身份证正面、身份证反面都应该正确显示

### 步骤3：修改照片测试
1. 在编辑页面，重新上传头像
2. 保存

**预期结果**：
- 旧的 `TEST001.jpg` 被删除
- 新的 `TEST001.jpg` 被保存
- 再次编辑时显示新照片

## 调试技巧

### 1. 查看后端日志
后端上传成功时会输出日志：
```
员工头像上传成功: 工号=TEST001, 文件=D:\rzphoto\employee_photo\TEST001.jpg
员工身份证照片上传成功: 工号=TEST001, 类型=front, 文件=D:\rzphoto\id_card_front\TEST001.jpg
员工身份证照片上传成功: 工号=TEST001, 类型=back, 文件=D:\rzphoto\id_card_back\TEST001.jpg
```

### 2. 查看前端控制台
前端上传成功时会显示提示：
```
头像上传成功
身份证正面上传成功
身份证反面上传成功
```

### 3. 直接访问图片URL
在浏览器中直接访问：
```
http://localhost:8080/api/employee_photo/TEST001.jpg
http://localhost:8080/api/id_card_front/TEST001.jpg
http://localhost:8080/api/id_card_back/TEST001.jpg
```

如果能正常显示图片，说明后端配置正确。

### 4. 检查数据库
```sql
-- 查看最近新增的员工
SELECT id, employee_no, name, avatar, id_card_front, id_card_back, created_time
FROM hr_employee
WHERE deleted = 0
ORDER BY created_time DESC
LIMIT 10;
```

## 常见问题

### Q1: 上传时提示"请先填写员工工号"
**原因**：上传照片前必须先填写工号。
**解决**：先填写工号，再上传照片。

### Q2: 上传成功但编辑时不显示
**原因**：可能是数据库中的路径格式不对。
**解决**：
1. 查看数据库中的路径
2. 如果是旧格式，重新上传照片

### Q3: 图片显示404
**原因**：
1. 物理文件不存在
2. 后端静态资源映射未生效

**解决**：
1. 检查文件是否存在
2. 重启后端服务
3. 查看后端启动日志确认映射配置

### Q4: 修改工号后照片不显示
**原因**：照片文件名是以工号命名的，修改工号后文件名不匹配。
**解决**：
1. 手动重命名物理文件
2. 或者重新上传照片

## 数据迁移脚本（如果需要）

如果你有旧数据需要迁移到新的存储方式，可以使用以下脚本：

```sql
-- 查看需要迁移的数据
SELECT id, employee_no, name, avatar, id_card_front, id_card_back
FROM hr_employee
WHERE deleted = 0
  AND (
    avatar LIKE '/uploads/%' 
    OR id_card_front LIKE '/uploads/%' 
    OR id_card_back LIKE '/uploads/%'
  );

-- 注意：这个脚本只更新数据库路径，不会移动物理文件
-- 建议重新上传照片，让系统自动处理
```

## 总结

照片回显问题通常是因为：
1. **数据库路径格式不对**（最常见）
2. **物理文件不存在**
3. **后端服务未重启**

建议的排查顺序：
1. 先用新员工测试（新增 → 编辑）
2. 如果新员工正常，说明代码没问题，旧数据需要重新上传
3. 如果新员工也不正常，检查后端日志和静态资源映射
