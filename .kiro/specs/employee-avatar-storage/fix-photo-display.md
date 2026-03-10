# 员工照片回显问题修复

## 问题
用户反馈：编辑员工时，照片没有回显。

## 根本原因
`backend/src/main/resources/mapper/EmployeeMapper.xml` 中的 `selectPageWithDetails` 查询使用了 `e.*` 通配符来选择所有字段。

在某些情况下（特别是有 JOIN 操作时），使用 `*` 可能导致字段映射不正确或字段被覆盖。

## 修复方案
将 `e.*` 改为显式列出所有需要的字段，确保照片字段（`avatar`, `id_card_front`, `id_card_back`）被正确查询和映射。

### 修改前
```xml
<select id="selectPageWithDetails" resultMap="EmployeeResultMap">
    SELECT 
        e.*,
        d.unit_name as dept_name,
        ...
    FROM hr_employee e
    ...
</select>
```

### 修改后
```xml
<select id="selectPageWithDetails" resultMap="EmployeeResultMap">
    SELECT 
        e.id, e.employee_no, e.name, e.avatar, e.id_card_front, e.id_card_back,
        e.gender, e.highest_education, e.dept_id, e.nation, e.id_card, e.birth_date,
        e.phone, e.email, e.employee_type, e.entry_date, e.regular_date,
        e.duty, e.position, e.post, e.job_function, e.job_level, e.job_responsibility,
        e.job_authority, e.job_title, e.occupation, e.marital_status, e.political_status,
        e.native_place, e.police_station, e.registered_address, e.home_address,
        e.current_address, e.emergency_contact, e.emergency_relation, e.emergency_phone,
        e.leave_date, e.status, e.deleted, e.created_time, e.updated_time,
        d.unit_name as dept_name,
        ...
    FROM hr_employee e
    ...
</select>
```

## 测试步骤

### 1. 重启后端服务
修改了 Mapper XML 文件后，需要重启后端服务使更改生效。

```bash
cd backend
run.bat
```

### 2. 测试新增员工
1. 打开员工管理页面
2. 点击"新增员工"
3. 填写工号（如：`TEST001`）
4. 上传头像、身份证正面、身份证反面
5. 填写其他必填信息并保存

**预期结果**：
- 照片成功上传到 `D:\rzphoto\` 对应文件夹
- 数据库中保存了正确的路径（如：`/employee_photo/TEST001.jpg`）

### 3. 测试编辑员工（关键测试）
1. 在员工列表中找到刚才新增的员工
2. 点击"编辑"
3. 检查照片是否正确显示

**预期结果**：
- ✅ 头像正确显示
- ✅ 身份证正面正确显示
- ✅ 身份证反面正确显示

### 4. 测试旧数据
如果有旧员工数据（使用旧路径格式的），需要：
1. 编辑旧员工
2. 重新上传照片
3. 保存后再次编辑，检查照片是否显示

## 验证方法

### 方法1：查看数据库
```sql
-- 查看最近新增的员工
SELECT id, employee_no, name, avatar, id_card_front, id_card_back, created_time
FROM hr_employee
WHERE deleted = 0
ORDER BY created_time DESC
LIMIT 10;
```

**预期结果**：
- 新格式：`/employee_photo/001.jpg`
- 旧格式：`/uploads/images/2025/01/24/xxxxx.jpg`

### 方法2：直接访问图片URL
在浏览器中访问：
```
http://localhost:8080/api/employee_photo/TEST001.jpg
http://localhost:8080/api/id_card_front/TEST001.jpg
http://localhost:8080/api/id_card_back/TEST001.jpg
```

如果能正常显示图片，说明后端配置正确。

### 方法3：查看浏览器开发者工具
1. 打开浏览器开发者工具（F12）
2. 切换到 Network 标签
3. 编辑员工
4. 查看是否有图片请求
5. 检查图片请求的状态码（应该是 200）

## 注意事项

### 1. 旧数据迁移
如果有旧员工数据使用旧路径格式（`/uploads/images/...`），这些照片不会自动显示。

**解决方案**：
- 方案A：重新上传照片（推荐）
- 方案B：手动迁移物理文件并更新数据库路径

### 2. 文件权限
确保 `D:\rzphoto\` 及其子目录有读写权限。

### 3. 后端服务重启
修改 Mapper XML 文件后，必须重启后端服务才能生效。

### 4. 浏览器缓存
如果修改后照片仍不显示，尝试：
- 清除浏览器缓存
- 使用无痕模式测试
- 强制刷新（Ctrl + F5）

## 相关文件
- `backend/src/main/resources/mapper/EmployeeMapper.xml` - 修复了查询语句
- `backend/src/main/java/com/kadmin/controller/FileController.java` - 照片上传接口
- `backend/src/main/java/com/kadmin/config/WebMvcConfig.java` - 静态资源映射
- `frontend/src/views/hr/employee/index.vue` - 前端员工管理页面
- `frontend/src/service/api/file.ts` - 前端上传API

## 总结
通过显式列出所有字段而不是使用 `*` 通配符，确保了照片字段在 JOIN 查询中被正确映射，解决了编辑员工时照片不回显的问题。
