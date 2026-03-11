<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAppStore } from '@/store/modules/app';

defineOptions({ name: 'CreativityBanner' });

const router = useRouter();
const appStore = useAppStore();

const columns = computed(() => (appStore.isMobile ? 2 : 3));

type QuickAction = {
  title: string;
  desc: string;
  icon: string;
  routeName: string;
  iconColor: string;
  iconBg: string;
};

const actions = computed<QuickAction[]>(() => [
  {
    title: '员工管理',
    desc: '入转调离与档案',
    icon: 'mdi:account-multiple',
    routeName: 'hr_employee',
    iconColor: '#6366f1',
    iconBg: 'rgba(99,102,241,0.1)'
  },
  {
    title: '组织架构',
    desc: '公司/部门结构',
    icon: 'mdi:office-building',
    routeName: 'organization_org-structure',
    iconColor: '#06b6d4',
    iconBg: 'rgba(6,182,212,0.1)'
  },
  {
    title: '待审批',
    desc: '快速处理申请',
    icon: 'mdi:file-clock-outline',
    routeName: 'approval_pending',
    iconColor: '#f59e0b',
    iconBg: 'rgba(245,158,11,0.1)'
  },
  {
    title: '打卡记录',
    desc: '查询与补录',
    icon: 'mdi:fingerprint',
    routeName: 'attendance_clock',
    iconColor: '#10b981',
    iconBg: 'rgba(16,185,129,0.1)'
  },
  {
    title: '日考勤',
    desc: '异常处理与锁定',
    icon: 'mdi:calendar-today',
    routeName: 'attendance_daily',
    iconColor: '#8b5cf6',
    iconBg: 'rgba(139,92,246,0.1)'
  },
  {
    title: '报表中心',
    desc: '考勤与员工分析',
    icon: 'mdi:chart-line',
    routeName: 'report_attendance',
    iconColor: '#ec4899',
    iconBg: 'rgba(236,72,153,0.1)'
  }
]);

function go(name: string) {
  router.push({ name });
}
</script>

<template>
  <ElCard class="shortcut-card h-full">
    <template #header>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-8px">
          <div class="header-dot" style="background: #8b5cf6"></div>
          <span class="font-medium">常用入口</span>
        </div>
        <span class="text-12px text-#9ca3af">一键直达高频功能</span>
      </div>
    </template>

    <ElRow :gutter="12">
      <ElCol v-for="item in actions" :key="item.routeName" :span="24 / columns">
        <div class="action-card" @click="go(item.routeName)">
          <div class="action-icon" :style="{ background: item.iconBg }">
            <SvgIcon :icon="item.icon" class="text-20px" :style="{ color: item.iconColor }" />
          </div>
          <div class="action-text">
            <div class="action-title">{{ item.title }}</div>
            <div class="action-desc">{{ item.desc }}</div>
          </div>
        </div>
      </ElCol>
    </ElRow>
  </ElCard>
</template>

<style scoped lang="scss">
.shortcut-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-extra-light);
  }
}

.header-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-bottom: 12px;
  border: 1px solid transparent;

  &:hover {
    background: var(--el-fill-color-light);
    border-color: var(--el-border-color-lighter);
    transform: translateY(-1px);
  }
}

.action-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-text {
  min-width: 0;
}

.action-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
