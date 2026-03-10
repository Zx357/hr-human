# 员工照片存储路径配置 - 完成总结

## 需求
员工管理的照片要存储在 `D:\rzphoto` 文件夹里，并且以工号命名：
- 员工头像：`D:\rzphoto\employee_photo\{工号}.jpg`
- 身份证正面：`D:\rzphoto\id_card_front\{工号}.jpg`
- 身份证反面：`D:\rzphoto\id_card_back\{工号}.jpg`

## 已完成的工作

### 1. 后端新增员工照片上传接口

#### 文件：`backend/src/main/java/com/kadmin/controller/FileController.java`

**新增接口1**：`POST /file/upload/employee/avatar`
- **功能**：上传员工头像
- **参数**：
  - `file`: 图片文件
  - `employeeNo`: 员工工号
- **存储路径**：`D:\rzphoto\employee_photo`
- **文件命名**：`{工号}.{扩展名}`
- **返回路径**：`/employee_photo/{工号}.{扩展名}`

**新增接口2**：`POST /file/upload/employee/idcard`
- **功能**：上传员工身份证照片
- **参数**：
  - `file`: 图片文件
  - `employeeNo`: 员工工号
  - `type`: 类型（`front`-正面，`back`-反面）
- **存储路径**：
  - 正面：`D:\rzphoto\id_card_front`
  - 反面：`D:\rzphoto\id_card_back`
- **文件命名**：`{工号}.{扩展名}`
- **返回路径**：
  - 正面：`/id_card_front/{工号}.{扩展名}`
  - 反面：`/id_card_back/{工号}.{扩展名}`

**共同特点**：
- 支持的图片格式：jpg, jpeg, png, gif, webp
- 自动覆盖：如果已存在同工号的照片，会先删除旧文件再保存新文件
- 自动创建目录：如果目录不存在会自动创建

### 2. 后端配置静态资源映射

#### 文件：`backend/src/main/java/com/kadmin/config/WebMvcConfig.java`

**新增映射**：
1. `/employee_photo/** -> D:\rzphoto\employee_photo\`
2. `/id_card_front/** -> D:\rzphoto\id_card_front\`
3. `/id_card_back/** -> D:\rzphoto\id_card_back\`

**访问方式**：
- 头像：`http://localhost:8080/api/employee_photo/001.jpg`
- 身份证正面：`http://localhost:8080/api/id_card_front/001.jpg`
- 身份证反面：`http://localhost:8080/api/id_card_back/001.jpg`

### 3. 前端新增员工照片上传API

#### 文件：`frontend/src/service/api/file.ts`

**新增函数1**：`uploadEmployeeAvatar(file, employeeNo)`
- **功能**：上传员工头像
- **参数**：
  - `file`: File - 图片文件对象
  - `employeeNo`: string - 员工工号
- **返回**：Promise<string> - 返回图片的相对路径

**新增函数2**：`uploadEmployeeIdCard(file, employeeNo, type)`
- **功能**：上传员工身份证照片
- **参数**：
  - `file`: File - 图片文件对象
  - `employeeNo`: string - 员工工号
  - `type`: 'front' | 'back' - 照片类型
- **返回**：Promise<string> - 返回图片的相对路径

### 4. 前端员工管理页面更新

#### 文件：`frontend/src/views/hr/employee/index.vue`

**修改内容**：
1. 导入新的上传函数：`uploadEmployeeAvatar`, `uploadEmployeeIdCard`
2. 修改 `handleImageUpload` 函数：
   - 所有照片上传前都检查是否已填写工号
   - 头像使用 `uploadEmployeeAvatar` 接口
   - 身份证正面使用 `uploadEmployeeIdCard(file, employeeNo, 'front')`
   - 身份证反面使用 `uploadEmployeeIdCard(file, employeeNo, 'back')`

**上传逻辑**：
```typescript
async function handleImageUpload(file: File, type: 'avatar' | 'idCardFront' | 'idCardBack') {
  // 检查工号
  if (!editingData.value.employeeNo) {
    ElMessage.warning('请先填写员工工号');
    return false;
  }

  if (type === 'avatar') {
    // 头像 -> D:\rzphoto\employee_photo\{工号}.jpg
    const res = await uploadEmployeeAvatar(file, editingData.value.employeeNo);
  } else if (type === 'idCardFront') {
    // 身份证正面 -> D:\rzphoto\id_card_front\{工号}.jpg
    const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'front');
  } else if (type === 'idCardBack') {
    // 身份证反面 -> D:\rzphoto\id_card_back\{工号}.jpg
    const res = await uploadEmployeeIdCard(file, editingData.value.employeeNo, 'back');
  }
}
```

## 使用说明

### 上传员工照片的步骤

1. **填写员工工号**（必须先填写）
2. **点击对应的照片上传区域**（头像/身份证正面/身份证反面）
3. **选择图片文件**（支持 jpg, jpeg, png, gif, webp）
4. **自动上传并保存**

### 文件存储示例

假设员工工号为 `001`：

| 照片类型 | 存储位置 | 数据库保存 | 前端访问URL |
|---------|---------|-----------|------------|
| 头像 | `D:\rzphoto\employee_photo\001.jpg` | `/employee_photo/001.jpg` | `http://localhost:8080/api/employee_photo/001.jpg` |
| 身份证正面 | `D:\rzphoto\id_card_front\001.jpg` | `/id_card_front/001.jpg` | `http://localhost:8080/api/id_card_front/001.jpg` |
| 身份证反面 | `D:\rzphoto\id_card_back\001.jpg` | `/id_card_back/001.jpg` | `http://localhost:8080/api/id_card_back/001.jpg` |

### 文件覆盖规则

- 如果员工 `001` 已有头像 `001.jpg`
- 再次上传新头像时，会先删除旧的 `001.jpg`
- 然后保存新的头像文件
- 确保每个员工每种照片只有一个文件

## 目录结构

```
D:\rzphoto\
├── employee_photo\      # 员工头像
│   ├── 001.jpg
│   ├── 002.jpg
│   └── ...
├── id_card_front\       # 身份证正面
│   ├── 001.jpg
│   ├── 002.jpg
│   └── ...
└── id_card_back\        # 身份证反面
    ├── 001.jpg
    ├── 002.jpg
    └── ...
```

## 技术实现细节

### 1. 文件命名策略
- **优点**：
  - 以工号命名，便于管理和查找
  - 同一员工的照片会自动覆盖，不会产生冗余文件
  - 文件名与员工信息直接关联
  - 不同类型照片分文件夹存储，结构清晰

### 2. 存储路径固定
- **路径**：`D:\rzphoto\{类型文件夹}`
- **优点**：
  - 独立于项目目录，便于备份和迁移
  - 多个项目可以共享同一个照片库
  - 不受项目部署位置影响
  - 分类存储，便于管理

### 3. 静态资源映射
- **映射规则**：
  - `/employee_photo/** -> D:\rzphoto\employee_photo\`
  - `/id_card_front/** -> D:\rzphoto\id_card_front\`
  - `/id_card_back/** -> D:\rzphoto\id_card_back\`
- **访问方式**：通过HTTP直接访问图片
- **安全性**：只映射特定目录，不会暴露其他系统文件

### 4. 前端显示
- 使用 `getFileUrl()` 函数自动拼接完整URL
- 支持相对路径和绝对路径
- 自动添加后端API地址前缀

## 注意事项

1. **工号必填**：上传任何照片前必须先填写员工工号
2. **目录权限**：确保 `D:\rzphoto` 及其子目录有读写权限
3. **文件格式**：只支持图片格式（jpg, jpeg, png, gif, webp）
4. **文件大小**：建议不超过 5MB
5. **文件覆盖**：同一工号的照片会自动覆盖旧文件
6. **路径分隔符**：代码中使用 `File.separator` 确保跨平台兼容
7. **新增和编辑**：新增和编辑员工时都使用相同的上传逻辑

## 与其他图片上传的区别

### 员工照片上传（新）
- **接口**：
  - `/file/upload/employee/avatar` - 头像
  - `/file/upload/employee/idcard` - 身份证
- **存储路径**：`D:\rzphoto\{类型文件夹}`
- **文件命名**：`{工号}.{扩展名}`
- **用途**：员工头像、身份证照片专用

### 普通图片上传
- **接口**：`/file/upload/image`
- **存储路径**：`{项目目录}/uploads/images/{年/月/日}`
- **文件命名**：`{UUID}.{扩展名}`
- **用途**：合同图片、其他图片等

### 普通文件上传
- **接口**：`/file/upload`
- **存储路径**：`{项目目录}/uploads/{年/月/日}`
- **文件命名**：`{UUID}.{扩展名}`
- **用途**：文档、附件等

## 后续优化建议

1. **图片压缩**：上传时自动压缩图片，减少存储空间
2. **缩略图生成**：自动生成不同尺寸的缩略图
3. **备份机制**：定期备份照片文件到其他位置
4. **CDN支持**：支持将图片上传到CDN服务
5. **批量导入**：支持批量导入员工照片
6. **历史记录**：保留照片修改历史记录
7. **OCR识别**：自动识别身份证信息并填充表单
8. **人脸识别**：验证头像与身份证照片的一致性
