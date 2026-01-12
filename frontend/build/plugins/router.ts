import type { RouteMeta } from 'vue-router';
import ElegantVueRouter from '@elegant-router/vue/vite';
import type { RouteKey } from '@elegant-router/types';

export function setupElegantRouter() {
  return ElegantVueRouter({
    layouts: {
      base: 'src/layouts/base-layout/index.vue',
      blank: 'src/layouts/blank-layout/index.vue'
    },
    customRoutes: {
      names: []
    },
    routePathTransformer(routeName, routePath) {
      const key = routeName as RouteKey;

      if (key === 'login') {
        const modules: UnionKey.LoginModule[] = ['pwd-login', 'code-login', 'register', 'reset-pwd', 'bind-wechat'];

        const moduleReg = modules.join('|');

        return `/login/:module(${moduleReg})?`;
      }

      return routePath;
    },
    onRouteMetaGen(routeName) {
      const key = routeName as RouteKey;

      const constantRoutes: RouteKey[] = ['login', '403', '404', '500'];

      const meta: Partial<RouteMeta> = {
        title: key,
        i18nKey: `route.${key}` as App.I18n.I18nKey
      };

      if (constantRoutes.includes(key)) {
        meta.constant = true;
      }

      // 设置菜单顺序和图标
      const routeOrderMap: Record<string, number> = {
        home: 1,
        organization: 2,
        hr: 3,
        attendance: 4,
        application: 5,
        approval: 6,
        report: 7,
        system: 8
      };

      const routeIconMap: Record<string, string> = {
        home: 'mdi:monitor-dashboard',
        organization: 'mdi:office-building',
        organization_company: 'mdi:domain',
        organization_department: 'mdi:sitemap',
        hr: 'mdi:account-group',
        hr_employee: 'mdi:account-multiple',
        hr_contract: 'mdi:file-document-edit',
        hr_regularization: 'mdi:account-check',
        hr_transfer: 'mdi:swap-horizontal',
        hr_reward: 'mdi:medal',
        hr_resignation: 'mdi:account-remove',
        attendance: 'mdi:clock-outline',
        attendance_shift: 'mdi:clock-time-four',
        attendance_schedule: 'mdi:calendar-clock',
        attendance_holiday: 'mdi:calendar-star',
        attendance_daily: 'mdi:calendar-today',
        attendance_monthly: 'mdi:calendar-month',
        application: 'mdi:file-document-multiple',
        application_leave: 'mdi:beach',
        application_overtime: 'mdi:clock-plus',
        application_makeup: 'mdi:clock-check',
        application_exchange: 'mdi:swap-horizontal-circle',
        application_business: 'mdi:airplane',
        approval: 'mdi:check-decagram',
        approval_pending: 'mdi:clock-alert',
        approval_mine: 'mdi:file-document-check',
        approval_flow: 'mdi:sitemap',
        report: 'mdi:chart-bar',
        report_employee: 'mdi:chart-pie',
        report_attendance: 'mdi:chart-line',
        system: 'mdi:cog',
        system_menu: 'mdi:menu',
        system_dict: 'mdi:book-alphabet'
      };

      // 设置顺序
      const baseKey = key.split('_')[0];
      if (routeOrderMap[baseKey]) {
        meta.order = routeOrderMap[baseKey];
      }

      // 设置图标
      if (routeIconMap[key]) {
        meta.icon = routeIconMap[key];
      }

      return meta;
    }
  });
}
