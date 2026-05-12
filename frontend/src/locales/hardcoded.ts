import zhCN from './langs/zh-cn';
import enUS from './langs/en-us';

const customTextMap = {
  '中文': 'Chinese',
  '英文': 'English',
  'github地址：': 'GitHub:',
  '搜索图标': 'Search icons',
  '你什么也找不到': 'No matching icons',
  '点击选择图标': 'Click to select an icon',
  '请选择': 'Please select',
  '请输入': 'Please enter',
  '搜索': 'Search',
  '查询': 'Query',
  '重置': 'Reset',
  '新增': 'Add',
  '添加': 'Add',
  '编辑': 'Edit',
  '修改': 'Modify',
  '删除': 'Delete',
  '保存': 'Save',
  '取消': 'Cancel',
  '确定': 'OK',
  '确认': 'Confirm',
  '提交': 'Submit',
  '返回': 'Back',
  '操作': 'Actions',
  '状态': 'Status',
  '启用': 'Enabled',
  '禁用': 'Disabled',
  '停用': 'Stopped',
  '正常': 'Normal',
  '关闭': 'Closed',
  '显示': 'Show',
  '隐藏': 'Hide',
  '是': 'Yes',
  '否': 'No',
  '全部': 'All',
  '暂无': 'None',
  '暂无数据': 'No data',
  '暂无关联员工': 'No assigned employees',
  '暂无自定义字段': 'No custom fields',
  '加载中': 'Loading',
  '计算中': 'Calculating',
  '操作成功': 'Operation successful',
  '操作失败': 'Operation failed',
  '新增成功': 'Added successfully',
  '更新成功': 'Updated successfully',
  '修改成功': 'Modified successfully',
  '删除成功': 'Deleted successfully',
  '保存成功': 'Saved successfully',
  '删除失败': 'Delete failed',
  '系统提示': 'System Prompt',
  '提示': 'Tip',
  '警告': 'Warning',
  '错误': 'Error',
  '创建时间': 'Created Time',
  '更新时间': 'Updated Time',
  '排序': 'Sort',
  '描述': 'Description',
  '备注': 'Remark',
  '类型': 'Type',
  '类别': 'Category',
  '名称': 'Name',
  '编码': 'Code',
  '标题': 'Title',
  '内容': 'Content',
  '详情': 'Details',
  '密码': 'Password',
  '用户名': 'Username',
  '用户列表': 'User List',
  '用户管理': 'User Management',
  '用户名称': 'Username',
  '用户昵称': 'Nickname',
  '昵称': 'Nickname',
  '手机号': 'Phone',
  '邮箱': 'Email',
  '角色': 'Role',
  '角色列表': 'Role List',
  '角色名称': 'Role Name',
  '角色编码': 'Role Code',
  '数据权限': 'Data Scope',
  '菜单权限': 'Menu Permission',
  '菜单树': 'Menu Tree',
  '菜单列表': 'Menu List',
  '菜单名称': 'Menu Name',
  '菜单编码': 'Menu Code',
  '菜单类型': 'Menu Type',
  '路由地址': 'Route Address',
  '路由路径': 'Route Path',
  '组件路径': 'Component Path',
  '权限标识': 'Permission',
  '菜单状态': 'Menu Status',
  '可见': 'Visible',
  '目录': 'Directory',
  '菜单': 'Menu',
  '按钮': 'Button',
  '主类目': 'Root Category',
  '字典类型': 'Dictionary Type',
  '字典数据': 'Dictionary Data',
  '字典名称': 'Dictionary Name',
  '字典编码': 'Dictionary Code',
  '字典标签': 'Dictionary Label',
  '字典键值': 'Dictionary Value',
  '默认': 'Default',
  '公告': 'Notice',
  '公告列表': 'Notice List',
  '公告标题': 'Notice Title',
  '公告类型': 'Notice Type',
  '发布日期': 'Publish Date',
  '文件路径配置': 'File Path Config',
  '配置键': 'Config Key',
  '配置名称': 'Config Name',
  '配置时间': 'Config Time',
  '路径': 'Path',
  '组织架构': 'Organization Structure',
  '组织': 'Organization',
  '公司': 'Company',
  '公司名称': 'Company Name',
  '部门': 'Department',
  '部门名称': 'Department Name',
  '上级组织': 'Parent Organization',
  '新增顶级组织': 'Add Top Organization',
  '人数': 'Employees',
  '合计': 'Total',
  '员工': 'Employee',
  '员工列表': 'Employee List',
  '员工管理': 'Employee Management',
  '员工报表': 'Employee Report',
  '姓名': 'Name',
  '工号': 'Employee No.',
  '性别': 'Gender',
  '男': 'Male',
  '女': 'Female',
  '年龄': 'Age',
  '年龄段': 'Age Range',
  '年龄分布': 'Age Distribution',
  '民族': 'Ethnicity',
  '籍贯': 'Native Place',
  '生日': 'Birthday',
  '身份证号': 'ID Number',
  '联系电话': 'Contact Phone',
  '联系人': 'Contact',
  '联系信息': 'Contact Info',
  '紧急联系人': 'Emergency Contact',
  '户籍地址': 'Registered Address',
  '现居地址': 'Current Address',
  '入职日期': 'Entry Date',
  '离职日期': 'Resignation Date',
  '在职': 'Active',
  '离职': 'Resigned',
  '工作信息': 'Work Info',
  '基本信息': 'Basic Info',
  '其他信息': 'Other Info',
  '工作经历': 'Work Experience',
  '教育经历': 'Education',
  '家庭成员': 'Family Member',
  '证书': 'Certificate',
  '添加工作经历': 'Add Work Experience',
  '添加教育经历': 'Add Education',
  '添加家庭成员': 'Add Family Member',
  '添加证书': 'Add Certificate',
  '合同列表': 'Contract List',
  '合同编号': 'Contract No.',
  '合同类型': 'Contract Type',
  '合同次数': 'Contract Count',
  '合同期限': 'Contract Term',
  '合同图片': 'Contract Image',
  '签订日期': 'Sign Date',
  '开始日期': 'Start Date',
  '结束日期': 'End Date',
  '过期日期': 'Expire Date',
  '即将到期': 'Expiring Soon',
  '考勤': 'Attendance',
  '考勤管理': 'Attendance Management',
  '考勤报表': 'Attendance Report',
  '日考勤': 'Daily Attendance',
  '月考勤': 'Monthly Attendance',
  '考勤日历规则': 'Attendance Calendar Rules',
  '更新报表': 'Refresh Report',
  '导出明细': 'Export Details',
  '导出月报': 'Export Monthly Report',
  '明细视图': 'Detail View',
  '工时': 'Work Hours',
  '班次管理': 'Shift Management',
  '班次': 'Shift',
  '工作时段': 'Work Period',
  '排班': 'Schedule',
  '员工排班': 'Employee Schedule',
  '批量排班': 'Batch Schedule',
  '清除': 'Clear',
  '联动': 'Cascade',
  '联动选择': 'Cascade Select',
  '跨天': 'Cross Day',
  '打卡记录': 'Clock Records',
  '打卡时间': 'Clock Time',
  '打卡地点': 'Clock Location',
  '打卡地点设置': 'Clock Location Settings',
  '打卡地址': 'Clock Address',
  '打卡半径': 'Clock Radius',
  '地点名称': 'Location Name',
  '地图选点': 'Map Picker',
  '地图选择位置': 'Pick Location on Map',
  '地图加载中': 'Map loading',
  '我的位置': 'My Location',
  '按当前地址定位': 'Locate by Current Address',
  '纬度': 'Latitude',
  '经度': 'Longitude',
  '坐标': 'Coordinates',
  '半径': 'Radius',
  '米': 'm',
  '上班': 'Clock In',
  '下班': 'Clock Out',
  '迟到': 'Late',
  '早退': 'Early Leave',
  '缺卡': 'Missing Clock',
  '旷工': 'Absent',
  '请假': 'Leave',
  '加班': 'Overtime',
  '出差': 'Business Trip',
  '补卡': 'Makeup Clock',
  '换休': 'Exchange Leave',
  '转正': 'Regularization',
  '调动': 'Transfer',
  '奖励': 'Reward',
  '惩罚': 'Punishment',
  '申请': 'Application',
  '申请管理': 'Applications',
  '请假申请': 'Leave Application',
  '加班申请': 'Overtime Application',
  '补卡申请': 'Makeup Clock Application',
  '出差申请': 'Business Trip Application',
  '换休申请': 'Exchange Leave Application',
  '转正申请': 'Regularization Application',
  '调动申请': 'Transfer Application',
  '奖励申请': 'Reward Application',
  '惩罚申请': 'Punishment Application',
  '离职申请': 'Resignation Application',
  '请假申请列表': 'Leave Applications',
  '加班申请列表': 'Overtime Applications',
  '补卡申请列表': 'Makeup Clock Applications',
  '出差申请列表': 'Business Trip Applications',
  '换休申请列表': 'Exchange Leave Applications',
  '转正申请列表': 'Regularization Applications',
  '调动申请列表': 'Transfer Applications',
  '奖惩申请列表': 'Reward & Punishment Applications',
  '离职申请列表': 'Resignation Applications',
  '发起请假申请': 'Create Leave Application',
  '发起加班申请': 'Create Overtime Application',
  '发起补卡申请': 'Create Makeup Clock Application',
  '发起出差申请': 'Create Business Trip Application',
  '发起换休申请': 'Create Exchange Leave Application',
  '发起奖惩申请': 'Create Reward/Punishment Application',
  '发起离职申请': 'Create Resignation Application',
  '新增申请': 'New Application',
  '请假类型': 'Leave Type',
  '请假时间': 'Leave Time',
  '请假小时': 'Leave Hours',
  '请假原因': 'Leave Reason',
  '加班小时': 'Overtime Hours',
  '加班原因': 'Overtime Reason',
  '出差小时': 'Business Trip Hours',
  '出差原因': 'Business Trip Reason',
  '换休日': 'Exchange Date',
  '换休原因': 'Exchange Reason',
  '离职类型': 'Resignation Type',
  '离职原因': 'Resignation Reason',
  '工作交接人': 'Handover Person',
  '交接人': 'Handover Person',
  '审批': 'Approval',
  '审批管理': 'Approval Management',
  '待审批': 'Pending Approval',
  '待审批列表': 'Pending Approvals',
  '我的申请': 'My Applications',
  '审批流程配置': 'Approval Flow Config',
  '流程编码': 'Flow Code',
  '流程名称': 'Flow Name',
  '流程类型': 'Flow Type',
  '节点名称': 'Node Name',
  '节点类型': 'Node Type',
  '审批人': 'Approver',
  '移动端审批权限': 'Mobile Approval Permission',
  '可审批类型': 'Approvable Types',
  '通过': 'Approve',
  '拒绝': 'Reject',
  '撤销': 'Withdraw',
  '已通过': 'Approved',
  '已拒绝': 'Rejected',
  '已撤销': 'Withdrawn',
  '自动通过': 'Auto Approve',
  '免审批': 'No Approval',
  '免审批直接通过': 'Auto approve without review',
  '数据概览': 'Data Overview',
  '常用入口': 'Common Shortcuts',
  '查看全部': 'View All',
  '我的待办': 'My Todos',
  '待办事项': 'Todos',
  '系统动态': 'System News',
  '员工总数': 'Total Employees',
  '本月入职': 'New Hires This Month',
  '本月离职': 'Resignations This Month',
  '欢迎回来': 'Welcome back',
  '欢迎回来！请登录您的账户继续操作': 'Welcome back! Please sign in to continue',
  '构建卓越的现代企业级管理平台': 'Build an excellent modern enterprise management platform',
  '极致纯粹的视觉体验': 'A clean and focused visual experience',
  '人资管理工作台': 'HR Management Workspace',
  '欢迎回到人资管理工作台': 'Welcome back to the HR management workspace',
  '左侧维护打卡地点': 'Manage clock locations on the left',
  '右侧维护当前地点关联的员工': 'manage assigned employees on the right',
  '关联员工': 'Assign Employees',
  '批量移除': 'Batch Remove',
  '选择员工': 'Select Employees',
  '保存关联': 'Save Assignments',
  '当前地点': 'Current Location',
  '请先选择左侧打卡地点': 'Please select a clock location on the left',
  '搜索地点名称或地址': 'Search location name or address',
  '请输入地点名称': 'Please enter a location name',
  '请先选择或填写打卡地址': 'Please select or enter a clock address',
  '请先选择地图点位': 'Please select a point on the map',
  '请输入有效打卡半径': 'Please enter a valid clock radius',
  '请先勾选要移除的员工': 'Please select employees to remove',
  '用于移动端展示': 'Shown on mobile',
  '可手动补充楼栋楼层': 'building and floor can be added manually',
  '选择后会自动回填地址和坐标': 'The address and coordinates will be filled after selection',
  '支持绝对路径': 'Absolute paths are supported',
  '相对路径': 'relative paths are supported',
  '展开': 'Expand',
  '折叠': 'Collapse',
  '展开折叠': 'Expand/Collapse',
  '全选': 'Select All',
  '全不选': 'Unselect All',
  '父子联动': 'Cascade Parent/Child',
  '全部数据': 'All Data',
  '本公司数据': 'Current Company Data',
  '本部门数据': 'Current Department Data',
  '本部门及以下数据': 'Current Department and Below',
  '仅本人数据': 'Self Only',
  '自定义数据': 'Custom Data',
  '快捷功能': 'Quick Features',
  '申请中心': 'Application Center',
  '移动端菜单': 'Mobile Menu',
  '账号信息': 'Account Info',
  '登录账号': 'Login Account',
  '个人资料': 'Profile',
  '基础资料': 'Basic Profile',
  '保存资料': 'Save Profile',
  '资料保存成功': 'Profile saved successfully',
  '安全设置': 'Security Settings',
  '当前密码': 'Current Password',
  '新密码': 'New Password',
  '确认新密码': 'Confirm New Password',
  '请输入当前密码': 'Please enter current password',
  '请输入新密码': 'Please enter new password',
  '请再次输入新密码': 'Please enter new password again',
  '两次输入的新密码不一致': 'The two new passwords do not match',
  '密码长度为 6-18 位': 'Password length must be 6-18 characters',
  '密码修改成功': 'Password changed successfully',
  '所属角色': 'Roles',
  '未分配角色': 'No roles assigned',
  '当前账号资料未加载完成': 'Current account profile is not loaded yet',
  '分组': 'Group',
  '图标': 'Icon',
  '颜色': 'Color',
  '链接': 'Link',
  '首页': 'Home',
  '工作台': 'Workspace',
  '我的': 'Mine'
} as const satisfies Record<string, string>;

const punctuationMap = {
  '，': ', ',
  '。': '.',
  '？': '?',
  '！': '!',
  '：': ': ',
  '；': '; ',
  '（': '(',
  '）': ')',
  '、': ', ',
  '“': '"',
  '”': '"',
  '《': '"',
  '》': '"'
} as const;

function isRecord(value: unknown): value is Record<string, unknown> {
  return Object.prototype.toString.call(value) === '[object Object]';
}

function collectLocalePairs(zh: unknown, en: unknown, pairs = new Map<string, string>()) {
  if (typeof zh === 'string' && typeof en === 'string' && zh && en) {
    pairs.set(zh, en);
    return pairs;
  }

  if (!isRecord(zh) || !isRecord(en)) return pairs;

  Object.entries(zh).forEach(([key, zhValue]) => {
    collectLocalePairs(zhValue, en[key], pairs);
  });

  return pairs;
}

const exactTextMap = collectLocalePairs(zhCN, enUS);

Object.entries(customTextMap).forEach(([zh, en]) => {
  exactTextMap.set(zh, en);
});

const replacementEntries = [...exactTextMap.entries()]
  .filter(([zh]) => zh.length > 1)
  .sort((a, b) => b[0].length - a[0].length);

const dynamicRules: Array<[RegExp, (...matches: string[]) => string]> = [
  [/^第(.+)次$/, value => `No. ${value}`],
  [/^节点\s*(\d+)$/, value => `Node ${value}`],
  [/^工作经历\s*(\d+)$/, value => `Work Experience ${value}`],
  [/^教育经历\s*(\d+)$/, value => `Education ${value}`],
  [/^家庭成员\s*(\d+)$/, value => `Family Member ${value}`],
  [/^证书\s*(\d+)$/, value => `Certificate ${value}`],
  [/^已选择\s*(.+)\s*人$/, value => `${value} selected`],
  [/^共\s*(.+)\s*人$/, value => `${value} people total`],
  [/^(.+)\s*人$/, value => `${value} people`],
  [/^当前共\s*(.+)\s*条$/, value => `Total ${value}`],
  [/^(.+)\s*条待处理$/, value => `${value} pending`],
  [/^(.+)\s*小时$/, value => `${value} h`],
  [/^(.+)\s*分钟$/, value => `${value} min`],
  [/^(.+)\s*分$/, value => `${value} min`],
  [/^(.+)\s*天$/, value => `${value} days`],
  [/^(.+)\s*米$/, value => `${value} m`],
  [/^当前地点：(.+)$/, value => `Current Location: ${value}`],
  [/^(.+)图表$/, value => `${value} Chart`]
];

function normalizeSpaces(value: string) {
  return value.replace(/\s+/g, ' ').trim();
}

function tidyEnglishSpacing(value: string) {
  return normalizeSpaces(value)
    .replace(/\s+([,.;:!?])/g, '$1')
    .replace(/([:])(?=\S)/g, '$1 ')
    .replace(/\s*\(\s*/g, ' (')
    .replace(/\s*\)\s*/g, ')');
}

function applyPunctuation(value: string) {
  return Object.entries(punctuationMap).reduce((text, [zh, en]) => text.split(zh).join(en), value);
}

function translateDynamicText(value: string) {
  for (const [pattern, replacer] of dynamicRules) {
    const match = value.match(pattern);

    if (match) {
      return replacer(...match.slice(1).map(normalizeSpaces));
    }
  }

  return '';
}

function translateByFragments(value: string) {
  let translated = value;

  replacementEntries.forEach(([zh, en]) => {
    if (translated.includes(zh)) {
      translated = translated.split(zh).join(en);
    }
  });

  translated = applyPunctuation(translated);

  return translated === value ? '' : tidyEnglishSpacing(translated);
}

function withOriginalWhitespace(original: string, translated: string) {
  const leading = original.match(/^\s*/)?.[0] || '';
  const trailing = original.match(/\s*$/)?.[0] || '';

  return `${leading}${translated}${trailing}`;
}

export function hasChineseText(value: string) {
  return /[\u4e00-\u9fff]/.test(value);
}

export function translateHardCodedText(value: string) {
  if (!hasChineseText(value)) return value;

  const normalized = normalizeSpaces(value);
  const translated =
    exactTextMap.get(normalized) || translateDynamicText(normalized) || translateByFragments(normalized) || normalized;

  return withOriginalWhitespace(value, translated);
}
