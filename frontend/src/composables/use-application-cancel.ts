import { ElMessageBox } from 'element-plus';
import type { FormItemRule } from 'element-plus';
import { appTypeLabel } from '@/constants/application';
import { useAuthStore } from '@/store/modules/auth';
import { isAdminRole } from '@/hooks/business/auth';
import { $t } from '@/locales';

/** 申请单行数据中撤销校验所需的最小字段 */
interface CancelableApplication {
  employeeId?: number;
  status?: number;
}

/** 撤销确认弹窗展示的单据上下文 */
interface CancelConfirmContext {
  employeeName?: string;
  appType?: string;
}

/**
 * 是否允许撤销该申请单（与后端 ApplicationService.cancel 规则一致）：
 * 仅「待审批」状态可撤销；管理员（ROLE_ADMIN）不受本人限制，普通用户仅可撤销自己的申请
 */
export function canCancelApplication(row: CancelableApplication): boolean {
  if (row.status !== 0) {
    return false;
  }

  if (isAdminRole()) {
    return true;
  }

  const { userInfo } = useAuthStore();
  // 后端以 employeeId（未关联员工时回退 userId）作为归属比对依据
  const selfId = userInfo.employeeId ?? Number(userInfo.userId);

  return row.employeeId !== undefined && row.employeeId === selfId;
}

/**
 * 撤销确认弹窗（带单据上下文：申请人姓名 + 单据标题/类型）
 *
 * @returns true=确认撤销 false=取消
 */
export async function confirmCancelApplication(row: CancelConfirmContext, titleText?: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(
      $t('application.common.withdrawConfirmMessage', {
        name: row.employeeName || '-',
        title: titleText || appTypeLabel(row.appType)
      }),
      $t('application.common.withdrawalConfirmation'),
      {
        type: 'warning',
        confirmButtonText: $t('application.common.confirmWithdrawal'),
        cancelButtonText: $t('common.cancel')
      }
    );
    return true;
  } catch {
    return false;
  }
}

/**
 * 日期范围必填校验（ElForm rule validator），供申请弹窗的 dateRange 类字段复用
 */
export function dateRangeRule(messageKey: App.I18n.I18nKey): FormItemRule {
  return {
    required: true,
    trigger: 'change',
    validator: (_rule, value: unknown, callback: (error?: Error) => void) => {
      if (Array.isArray(value) && value.length === 2 && value[0] && value[1]) {
        callback();
      } else {
        callback(new Error($t(messageKey)));
      }
    }
  };
}
