import { ref } from 'vue';
import dayjs from 'dayjs';
import { fetchEmployeePage } from '@/service/api/hr';
import { fetchPendingPage } from '@/service/api/application';
import { fetchClockRecordPage, fetchDailyRecordPage } from '@/service/api/attendance';

/** 异常考勤状态：2-迟到 3-早退 4-旷工 7-迟到+早退 */
const ABNORMAL_STATUSES = [2, 3, 4, 7];

/** 缓存有效期（毫秒） */
const CACHE_TTL = 10 * 1000;

export interface HomeStats {
  employeeTotal: number;
  pendingTotal: number;
  todayClockTotal: number;
  todayAbnormal: number;
}

const EMPTY_STATS: HomeStats = {
  employeeTotal: 0,
  pendingTotal: 0,
  todayClockTotal: 0,
  todayAbnormal: 0
};

/** 模块级单例缓存：card-data 与 header-banner 共用，避免重复请求 */
let cache: { stats: HomeStats; fetchedAt: number } | null = null;
let inflight: Promise<HomeStats> | null = null;

async function requestHomeStats(): Promise<HomeStats> {
  const today = dayjs().format('YYYY-MM-DD');
  const [empRes, pendingRes, clockRes, ...abnormalResList] = await Promise.all([
    fetchEmployeePage({ pageNum: 1, pageSize: 1, status: 1 }),
    fetchPendingPage({ pageNum: 1, pageSize: 1 }),
    fetchClockRecordPage({ page: 1, size: 1, startDate: today, endDate: today }),
    // 按异常状态查询 total 汇总，避免全量拉取后前端过滤
    ...ABNORMAL_STATUSES.map(status =>
      fetchDailyRecordPage({ page: 1, size: 1, startDate: today, endDate: today, status })
    )
  ]);

  return {
    employeeTotal: empRes?.data?.total ?? 0,
    pendingTotal: pendingRes?.data?.total ?? 0,
    todayClockTotal: clockRes?.data?.total ?? 0,
    todayAbnormal: abnormalResList.reduce((sum, res) => sum + (res?.data?.total ?? 0), 0)
  };
}

/**
 * 首页统计数据 composable
 *
 * 模块级单例缓存（10s TTL），并合并并发请求（inflight 去重），
 * card-data 与 header-banner 共用同一份数据，消除重复请求。
 */
export function useHomeStats() {
  const loading = ref(false);
  const stats = ref<HomeStats>({ ...EMPTY_STATS });

  async function load() {
    const now = Date.now();

    // 命中缓存直接返回
    if (cache && now - cache.fetchedAt < CACHE_TTL) {
      stats.value = cache.stats;
      return;
    }

    loading.value = true;
    try {
      if (!inflight) {
        inflight = requestHomeStats()
          .then(result => {
            cache = { stats: result, fetchedAt: Date.now() };
            return result;
          })
          .catch(() => ({ ...EMPTY_STATS }))
          .finally(() => {
            inflight = null;
          });
      }
      stats.value = await inflight;
    } catch {
      // ignore: keep zeroes for dashboard
    } finally {
      loading.value = false;
    }
  }

  return { loading, stats, load };
}
