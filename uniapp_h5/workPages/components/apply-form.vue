<template>
  <view class="apply-page">
    <tn-navbar fixed home-icon="" :placeholder="false" :bottom-shadow="false" bg-color="#FFFFFF">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-title">
        <text>{{ pageConfig.title }}</text>
      </view>
    </tn-navbar>

    <view class="apply-content" :style="{ paddingTop: vuex_custom_bar_height + 28 + 'px' }">
      <view class="hero-card" :style="{ background: pageConfig.gradient }">
        <view class="hero-icon">
          <tn-icon :name="pageConfig.icon"></tn-icon>
        </view>
        <view class="hero-copy">
          <view class="hero-title">{{ pageConfig.title }}</view>
          <view class="hero-desc">{{ pageConfig.desc }}</view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-title" :style="{ borderColor: pageConfig.accent }">申请人信息</view>
        <view class="info-grid">
          <view class="info-item">
            <text class="info-label">姓名</text>
            <text class="info-value">{{ userName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">部门</text>
            <text class="info-value">{{ deptName }}</text>
          </view>
          <view class="info-item" v-if="type === 'resign'">
            <text class="info-label">入职日期</text>
            <text class="info-value">{{ entryDate }}</text>
          </view>
          <view class="info-item" v-if="type === 'resign'">
            <text class="info-label">在职时长</text>
            <text class="info-value">{{ workDaysText }}</text>
          </view>
        </view>
      </view>

      <view class="section-card">
        <view class="section-title" :style="{ borderColor: pageConfig.accent }">{{ pageConfig.formTitle }}</view>

        <template v-if="type === 'leave'">
          <picker :range="leaveTypeLabels" :value="leaveTypeIndex" @change="setLeaveType($event)">
            <view class="field-row">
              <view>
                <view class="field-label">请假类型 <text>*</text></view>
                <view class="field-value" :class="{ placeholder: !form.leaveType }">{{ leaveTypeText || '请选择' }}</view>
              </view>
              <tn-icon name="right"></tn-icon>
            </view>
          </picker>
          <view class="field-row" v-if="leaveBalanceText">
            <view>
              <view class="field-label">剩余额度</view>
              <view class="field-value quota-value">{{ leaveBalanceText }}</view>
            </view>
          </view>
          <FDateTimeRow label="开始时间" :date="form.startDate" :time="form.startTime" @date="form.startDate = $event" @time="form.startTime = $event" />
          <FDateTimeRow label="结束时间" :date="form.endDate" :time="form.endTime" @date="form.endDate = $event" @time="form.endTime = $event" />
          <FDurationBox :loading="calculating" :value="leaveHours" unit="小时" />
          <FTextAreaRow label="请假事由" v-model="form.reason" placeholder="请填写请假原因" />
        </template>

        <template v-if="type === 'overtime'">
          <FDateRow label="加班日期" v-model="form.overtimeDate" />
          <FTimeRow label="开始时间" v-model="form.startTime" />
          <FTimeRow label="结束时间" v-model="form.endTime" />
          <FDurationBox :loading="calculating" :value="overtimeHours" unit="小时" />
          <FTextAreaRow label="加班事由" v-model="form.reason" placeholder="请填写加班原因" />
        </template>

        <template v-if="type === 'replace'">
          <FDateRow label="补卡日期" v-model="form.cardDate" />
          <picker :range="cardTypes" :value="selectedIndex(cardTypes, form.cardType)" @change="setSelect('cardType', cardTypes, $event)">
            <view class="field-row">
              <view>
                <view class="field-label">补卡类型 <text>*</text></view>
                <view class="field-value" :class="{ placeholder: !form.cardType }">{{ form.cardType || '请选择' }}</view>
              </view>
              <tn-icon name="right"></tn-icon>
            </view>
          </picker>
          <FTimeRow label="补卡时间" v-model="form.cardTime" />
          <FTextAreaRow label="补卡原因" v-model="form.reason" placeholder="请填写补卡原因，如忘记打卡、设备异常等" />
        </template>

        <template v-if="type === 'travel'">
          <FInputRow label="出差地点" v-model="form.destination" placeholder="请输入出差目的地" />
          <FDateRow label="开始日期" v-model="form.startDate" />
          <FDateRow label="结束日期" v-model="form.endDate" />
          <FDurationBox :value="travelDays" unit="天" />
          <FTextAreaRow label="出差事由" v-model="form.reason" placeholder="请填写出差事由" />
        </template>

        <template v-if="type === 'resign'">
          <picker :range="resignTypes" :value="selectedIndex(resignTypes, form.resignType)" @change="setSelect('resignType', resignTypes, $event)">
            <view class="field-row">
              <view>
                <view class="field-label">离职类型 <text>*</text></view>
                <view class="field-value" :class="{ placeholder: !form.resignType }">{{ form.resignType || '请选择' }}</view>
              </view>
              <tn-icon name="right"></tn-icon>
            </view>
          </picker>
          <FDateRow label="最后工作日" v-model="form.lastWorkDate" />
          <picker :range="resignReasons" :value="selectedIndex(resignReasons, form.resignReason)" @change="setSelect('resignReason', resignReasons, $event)">
            <view class="field-row">
              <view>
                <view class="field-label">离职原因 <text>*</text></view>
                <view class="field-value" :class="{ placeholder: !form.resignReason }">{{ form.resignReason || '请选择' }}</view>
              </view>
              <tn-icon name="right"></tn-icon>
            </view>
          </picker>
          <FTextAreaRow label="详细说明" v-model="form.reason" placeholder="请详细说明离职原因" :maxlength="500" />
          <FTextAreaRow label="工作交接" v-model="form.handover" placeholder="请说明工作交接安排及交接人" :required="false" />
        </template>

        <template v-if="type === 'exchange'">
          <FDateRow label="原工作日" v-model="form.startDate" />
          <FDateRow label="换休日" v-model="form.endDate" />
          <FTextAreaRow label="换休原因" v-model="form.reason" placeholder="请填写换休原因" />
        </template>
      </view>

      <view class="section-card tips-card" v-if="type === 'resign'">
        <view class="tips-title">温馨提示</view>
        <view class="tips-line">离职申请提交后需要上级审批。</view>
        <view class="tips-line">请提前安排好工作交接事项。</view>
      </view>
    </view>

    <view class="footer-bar">
      <tn-button
        width="48%"
        height="88"
        shape="round"
        bg-color="#EEF3F8"
        text-color="#66758D"
        :font-size="30"
        bold
        @click="handleReset"
      >
        <tn-icon name="refresh-simple" class="footer-icon"></tn-icon>
        <text>重置</text>
      </tn-button>
      <tn-button
        width="48%"
        height="88"
        shape="round"
        :bg-color="pageConfig.accent"
        text-color="#FFFFFF"
        :font-size="30"
        bold
        shadow
        :loading="submitting"
        :disabled="submitting"
        @click="handleSubmit"
      >
        <tn-icon name="success-circle-fill" class="footer-icon"></tn-icon>
        <text>{{ submitting ? '提交中...' : '提交申请' }}</text>
      </tn-button>
    </view>
  </view>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { calculateLeaveHours, calculateOvertimeHours, submitApplication } from '@/api/application'
import { getMyLeaveQuota } from '@/api/attendance'
import { leaveTypeLabel } from '@/utils/common'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import FDateRow from './form-date-row.vue'
import FTimeRow from './form-time-row.vue'
import FDateTimeRow from './form-date-time-row.vue'
import FInputRow from './form-input-row.vue'
import FTextAreaRow from './form-text-area-row.vue'
import FDurationBox from './form-duration-box.vue'

const props = defineProps({
  type: {
    type: String,
    default: 'leave'
  },
  // 从考勤日历跳入时预填的补卡日期（yyyy-MM-dd）
  defaultCardDate: {
    type: String,
    default: ''
  }
})

const DateRow = defineComponent({
  props: {
    modelValue: { type: String, default: '' },
    label: { type: String, required: true }
  },
  emits: ['update:modelValue'],
  setup(rowProps, { emit }) {
    return () =>
      h(
        'picker',
        {
          mode: 'date',
          value: rowProps.modelValue,
          onChange: (event) => emit('update:modelValue', event.detail.value)
        },
        [
          h('view', { class: 'field-row' }, [
            h('view', {}, [
              h('view', { class: 'field-label' }, [rowProps.label, h('text', {}, '*')]),
              h('view', { class: ['field-value', { placeholder: !rowProps.modelValue }] }, rowProps.modelValue || '请选择')
            ]),
            h('view', { class: 'field-arrow tn-icon-right' })
          ])
        ]
      )
  }
})

const TimeRow = defineComponent({
  props: {
    modelValue: { type: String, default: '' },
    label: { type: String, required: true }
  },
  emits: ['update:modelValue'],
  setup(rowProps, { emit }) {
    return () =>
      h(
        'picker',
        {
          mode: 'time',
          value: rowProps.modelValue,
          onChange: (event) => emit('update:modelValue', event.detail.value)
        },
        [
          h('view', { class: 'field-row' }, [
            h('view', {}, [
              h('view', { class: 'field-label' }, [rowProps.label, h('text', {}, '*')]),
              h('view', { class: ['field-value', { placeholder: !rowProps.modelValue }] }, rowProps.modelValue || '请选择')
            ]),
            h('view', { class: 'field-arrow tn-icon-time' })
          ])
        ]
      )
  }
})

const DateTimeRow = defineComponent({
  props: {
    label: { type: String, required: true },
    date: { type: String, default: '' },
    time: { type: String, default: '' }
  },
  emits: ['date', 'time'],
  setup(rowProps, { emit }) {
    return () =>
      h('view', { class: 'date-time-row' }, [
        h('view', { class: 'date-time-label' }, [rowProps.label, h('text', {}, '*')]),
        h('view', { class: 'date-time-controls' }, [
          h(
            'picker',
            {
              class: 'date-time-picker',
              mode: 'date',
              value: rowProps.date,
              onChange: (event) => emit('date', event.detail.value)
            },
            [h('view', { class: ['date-time-value', { placeholder: !rowProps.date }] }, rowProps.date || '日期')]
          ),
          h(
            'picker',
            {
              class: 'date-time-picker date-time-picker--time',
              mode: 'time',
              value: rowProps.time,
              onChange: (event) => emit('time', event.detail.value)
            },
            [h('view', { class: ['date-time-value', { placeholder: !rowProps.time }] }, rowProps.time || '时间')]
          )
        ])
      ])
  }
})

const InputRow = defineComponent({
  props: {
    modelValue: { type: String, default: '' },
    label: { type: String, required: true },
    placeholder: { type: String, default: '请输入' }
  },
  emits: ['update:modelValue'],
  setup(rowProps, { emit }) {
    return () =>
      h('view', { class: 'input-row' }, [
        h('view', { class: 'field-label' }, [rowProps.label, h('text', {}, '*')]),
        h('input', {
          class: 'plain-input tn-apply-input',
          value: rowProps.modelValue,
          placeholder: rowProps.placeholder,
          'placeholder-style': 'color:#AEB8C8',
          onInput: (event) => emit('update:modelValue', event.detail.value)
        })
      ])
  }
})

const TextAreaRow = defineComponent({
  props: {
    modelValue: { type: String, default: '' },
    label: { type: String, required: true },
    placeholder: { type: String, default: '请输入' },
    maxlength: { type: Number, default: 200 },
    required: { type: Boolean, default: true }
  },
  emits: ['update:modelValue'],
  setup(rowProps, { emit }) {
    return () =>
      h('view', { class: 'textarea-row' }, [
        h('view', { class: 'textarea-head' }, [
          h('view', { class: 'field-label' }, rowProps.required ? [rowProps.label, h('text', {}, '*')] : rowProps.label),
          h('text', {}, `${rowProps.maxlength}字内`)
        ]),
        h('textarea', {
          class: 'plain-textarea tn-apply-textarea',
          value: rowProps.modelValue,
          maxlength: rowProps.maxlength,
          placeholder: rowProps.placeholder,
          'placeholder-style': 'color:#AEB8C8',
          onInput: (event) => emit('update:modelValue', event.detail.value)
        })
      ])
  }
})

const DurationBox = defineComponent({
  props: {
    loading: Boolean,
    value: { type: Number, default: 0 },
    unit: { type: String, default: '小时' }
  },
  setup(boxProps) {
    return () =>
      h('view', { class: 'duration-box' }, [
        h('view', { class: 'duration-label' }, '预计时长'),
        h(
          'view',
          { class: ['duration-value', { placeholder: !boxProps.value && !boxProps.loading }] },
          boxProps.loading ? '计算中...' : boxProps.value > 0 ? `${boxProps.value} ${boxProps.unit}` : '选择时间后自动计算'
        )
      ])
  }
})

const store = useStore()
const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()

const pageMap = {
  leave: {
    title: '请假申请',
    formTitle: '请假信息',
    desc: '填写请假时间和事由，提交后等待审批',
    icon: 'calendar-fill',
    accent: '#5E7FAE',
    gradient: 'linear-gradient(135deg, #91AFD3 0%, #6F8FB9 48%, #516E9F 100%)'
  },
  overtime: {
    title: '加班申请',
    formTitle: '加班信息',
    desc: '记录加班日期、时段和原因',
    icon: 'time-fill',
    accent: '#D99B20',
    gradient: 'linear-gradient(135deg, #F3C35D 0%, #E8A835 52%, #C68816 100%)'
  },
  replace: {
    title: '补卡申请',
    formTitle: '补卡信息',
    desc: '忘记打卡或异常打卡时提交补卡',
    icon: 'edit-form',
    accent: '#22A873',
    gradient: 'linear-gradient(135deg, #67DFA4 0%, #31B87E 52%, #1C9162 100%)'
  },
  travel: {
    title: '出差申请',
    formTitle: '出差信息',
    desc: '填写出差目的地、日期和事由',
    icon: 'suitcase-fill',
    accent: '#6B81AA',
    gradient: 'linear-gradient(135deg, #93A8C9 0%, #7088B0 50%, #526A95 100%)'
  },
  resign: {
    title: '离职申请',
    formTitle: '离职信息',
    desc: '提交离职申请，请妥善安排工作交接',
    icon: 'reduce-circle-fill',
    accent: '#C9656A',
    gradient: 'linear-gradient(135deg, #EAA0A1 0%, #D57578 52%, #B4565E 100%)'
  },
  exchange: {
    title: '换休申请',
    formTitle: '换休信息',
    desc: '使用工作日或加班时长兑换休息时间',
    icon: 'menu-grille-fill',
    accent: '#4A9AB7',
    gradient: 'linear-gradient(135deg, #88D3EA 0%, #58AFCD 50%, #3C89A8 100%)'
  }
}

// 请假类型:label 用于展示,value 为后端字典值(与 PC 端提交口径一致,title 存字典值)
const leaveTypes = [
  { label: '事假', value: '2' },
  { label: '病假', value: '3' },
  { label: '年假', value: '1' },
  { label: '婚假', value: '4' },
  { label: '产假', value: '5' },
  { label: '陪产假', value: '6' },
  { label: '丧假', value: '7' }
]
const cardTypes = ['上班补卡', '下班补卡']
const resignTypes = ['主动离职', '协商离职']
const resignReasons = ['个人发展', '薪资待遇', '工作环境', '家庭原因', '健康原因', '其他原因']

const storageUser = ref({})
const submitting = ref(false)
const calculating = ref(false)
const leaveHours = ref(0)
const overtimeHours = ref(0)

const form = reactive(getInitialForm())

// 考勤日历「申请补卡」跳入时预填日期
watch(
  () => props.defaultCardDate,
  (val) => {
    if (val && !form.cardDate) form.cardDate = val
  },
  { immediate: true }
)

const pageConfig = computed(() => pageMap[props.type] || pageMap.leave)
const employeeInfo = computed(() => store.getters.employeeInfo || storageUser.value || {})
const employeeId = computed(() => store.getters.id || employeeInfo.value.id || '')
const userName = computed(() => store.getters.name || employeeInfo.value.name || '员工')
const deptName = computed(() => employeeInfo.value.deptName || employeeInfo.value.dept?.deptName || '-')
const entryDate = computed(() => employeeInfo.value.entryDate || employeeInfo.value.entry_date || '-')

const travelDays = computed(() => {
  if (!form.startDate || !form.endDate) return 0
  const start = parseDate(form.startDate)
  const end = parseDate(form.endDate)
  if (!start || !end) return 0
  const diff = Math.floor((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1
  return diff > 0 ? diff : 0
})

const workDaysText = computed(() => {
  if (!entryDate.value || entryDate.value === '-') return '-'
  const entry = parseDate(entryDate.value)
  if (!entry) return '-'
  const days = Math.max(0, Math.floor((Date.now() - entry.getTime()) / (1000 * 60 * 60 * 24)))
  if (days >= 365) {
    const years = Math.floor(days / 365)
    const months = Math.floor((days % 365) / 30)
    return `${years}年${months}个月`
  }
  const months = Math.floor(days / 30)
  return months > 0 ? `${months}个月` : `${days}天`
})

watch(
  () => [props.type, form.leaveType, form.overtimeDate, form.startDate, form.startTime, form.endDate, form.endTime, employeeId.value],
  () => {
    if (props.type === 'leave') calculateLeaveDuration()
    if (props.type === 'overtime') calculateOvertimeDuration()
  }
)

onMounted(() => {
  storageUser.value = uni.getStorageSync('userInfo') || {}
  loadLeaveQuota()
})

function getInitialForm() {
  return {
    leaveType: '',
    startDate: '',
    startTime: '',
    endDate: '',
    endTime: '',
    reason: '',
    overtimeDate: '',
    cardDate: '',
    cardType: '',
    cardTime: '',
    destination: '',
    resignType: '',
    resignReason: '',
    lastWorkDate: '',
    handover: ''
  }
}

function selectedIndex(options, value) {
  const index = options.indexOf(value)
  return index >= 0 ? index : 0
}

function setSelect(field, options, event) {
  form[field] = options[Number(event.detail.value)] || ''
}

// ===== 请假类型(label/value)与额度余额 =====
const leaveQuotas = ref([])

const leaveTypeLabels = computed(() => leaveTypes.map((item) => item.label))
const leaveTypeIndex = computed(() => {
  const index = leaveTypes.findIndex((item) => item.value === form.leaveType)
  return index >= 0 ? index : 0
})
const leaveTypeText = computed(() => leaveTypeLabel(form.leaveType))

function setLeaveType(event) {
  const selected = leaveTypes[Number(event.detail.value)]
  form.leaveType = selected ? selected.value : ''
}

// 当前所选类型的额度:接口只返回配置了额度的记录,未配置则不展示余额行
const leaveQuotaOfType = computed(() => {
  if (!form.leaveType) return null
  return leaveQuotas.value.find((item) => String(item.leaveType) === String(form.leaveType)) || null
})

const leaveBalanceText = computed(() => {
  if (!leaveQuotaOfType.value) return ''
  const total = Number(leaveQuotaOfType.value.totalHours || 0)
  const used = Number(leaveQuotaOfType.value.usedHours || 0)
  const remain = Math.max(0, Math.round((total - used) * 100) / 100)
  return `${remain} 小时（共 ${total} 小时）`
})

function loadLeaveQuota() {
  if (props.type !== 'leave') return
  getMyLeaveQuota(new Date().getFullYear()).then((res) => {
    leaveQuotas.value = Array.isArray(res.data) ? res.data : []
  }).catch(() => {
    // 额度接口失败不影响申请表单,只是不显示余额行
    leaveQuotas.value = []
  })
}

function parseDate(value) {
  if (!value) return null
  const date = new Date(String(value).replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

function normalizeTime(value) {
  if (!value) return ''
  const parts = String(value).split(':')
  if (parts.length === 2) return `${parts[0]}:${parts[1]}:00`
  if (parts.length >= 3) return `${parts[0]}:${parts[1]}:${parts[2]}`
  return value
}

function dateTime(date, time) {
  if (!date || !time) return ''
  return `${date} ${normalizeTime(time)}`
}

function localHours(startTime, endTime) {
  const start = parseDate(startTime)
  const end = parseDate(endTime)
  if (!start || !end) return 0
  const diff = (end.getTime() - start.getTime()) / (1000 * 60 * 60)
  return diff > 0 ? Math.round(diff * 10) / 10 : 0
}

async function calculateLeaveDuration() {
  const start = dateTime(form.startDate, form.startTime)
  const end = dateTime(form.endDate, form.endTime)
  if (!start || !end || localHours(start, end) <= 0) {
    leaveHours.value = 0
    return
  }

  if (!employeeId.value) {
    leaveHours.value = localHours(start, end)
    return
  }

  calculating.value = true
  try {
    const res = await calculateLeaveHours(employeeId.value, start, end)
    leaveHours.value = Number(res.data) || 0
  } catch (error) {
    leaveHours.value = localHours(start, end)
  } finally {
    calculating.value = false
  }
}

async function calculateOvertimeDuration() {
  const start = dateTime(form.overtimeDate, form.startTime)
  const end = dateTime(form.overtimeDate, form.endTime)
  if (!start || !end || localHours(start, end) <= 0) {
    overtimeHours.value = 0
    return
  }

  if (!employeeId.value) {
    overtimeHours.value = localHours(start, end)
    return
  }

  calculating.value = true
  try {
    const res = await calculateOvertimeHours(employeeId.value, start, end)
    overtimeHours.value = Number(res.data) || 0
  } catch (error) {
    overtimeHours.value = localHours(start, end)
  } finally {
    calculating.value = false
  }
}

function showMessage(title) {
  uni.showToast({ icon: 'none', title })
}

function handleReset() {
  Object.assign(form, getInitialForm())
  leaveHours.value = 0
  overtimeHours.value = 0
}

function validateForm() {
  if (props.type === 'leave') {
    if (!form.leaveType) return '请选择请假类型'
    if (!form.startDate || !form.startTime || !form.endDate || !form.endTime) return '请选择请假时间'
    if (localHours(dateTime(form.startDate, form.startTime), dateTime(form.endDate, form.endTime)) <= 0) return '结束时间不能早于开始时间'
    if (!form.reason.trim()) return '请填写请假事由'
  }

  if (props.type === 'overtime') {
    if (!form.overtimeDate || !form.startTime || !form.endTime) return '请选择加班时间'
    if (localHours(dateTime(form.overtimeDate, form.startTime), dateTime(form.overtimeDate, form.endTime)) <= 0) return '结束时间不能早于开始时间'
    if (!form.reason.trim()) return '请填写加班事由'
  }

  if (props.type === 'replace') {
    if (!form.cardDate) return '请选择补卡日期'
    if (!form.cardType) return '请选择补卡类型'
    if (!form.cardTime) return '请选择补卡时间'
    if (!form.reason.trim()) return '请填写补卡原因'
  }

  if (props.type === 'travel') {
    if (!form.destination.trim()) return '请输入出差地点'
    if (!form.startDate || !form.endDate) return '请选择出差日期'
    if (travelDays.value <= 0) return '结束日期不能早于开始日期'
    if (!form.reason.trim()) return '请填写出差事由'
  }

  if (props.type === 'resign') {
    if (!form.resignType) return '请选择离职类型'
    if (!form.lastWorkDate) return '请选择最后工作日'
    if (!form.resignReason) return '请选择离职原因'
    if (!form.reason.trim()) return '请填写详细说明'
  }

  if (props.type === 'exchange') {
    if (!form.startDate) return '请选择原工作日'
    if (!form.endDate) return '请选择换休日'
    if (!form.reason.trim()) return '请填写换休原因'
  }

  return ''
}

function buildPayload() {
  const base = {
    employeeId: employeeId.value,
    status: 0
  }

  if (props.type === 'leave') {
    return {
      ...base,
      appType: 'leave',
      title: form.leaveType,
      startTime: dateTime(form.startDate, form.startTime),
      endTime: dateTime(form.endDate, form.endTime),
      duration: leaveHours.value || localHours(dateTime(form.startDate, form.startTime), dateTime(form.endDate, form.endTime)),
      reason: form.reason
    }
  }

  if (props.type === 'overtime') {
    return {
      ...base,
      appType: 'overtime',
      title: '加班申请',
      startTime: dateTime(form.overtimeDate, form.startTime),
      endTime: dateTime(form.overtimeDate, form.endTime),
      duration: overtimeHours.value || localHours(dateTime(form.overtimeDate, form.startTime), dateTime(form.overtimeDate, form.endTime)),
      reason: form.reason
    }
  }

  if (props.type === 'replace') {
    const cardDateTime = dateTime(form.cardDate, form.cardTime)
    return {
      ...base,
      appType: 'makeup',
      title: `${form.cardType}申请`,
      startTime: cardDateTime,
      endTime: cardDateTime,
      reason: form.reason,
      category: form.cardType
    }
  }

  if (props.type === 'travel') {
    return {
      ...base,
      appType: 'business',
      title: `出差申请-${form.destination}`,
      startTime: `${form.startDate} 00:00:00`,
      endTime: `${form.endDate} 23:59:59`,
      duration: travelDays.value,
      reason: form.reason,
      remark: form.destination
    }
  }

  if (props.type === 'resign') {
    return {
      ...base,
      appType: 'resignation',
      title: '离职申请',
      resignType: form.resignType,
      lastWorkDate: form.lastWorkDate,
      reason: `离职原因: ${form.resignReason}\n详细说明: ${form.reason}`,
      remark: form.handover ? `工作交接: ${form.handover}` : ''
    }
  }

  return {
    ...base,
    appType: 'exchange',
    title: '换休申请',
    startTime: `${form.startDate} 00:00:00`,
    endTime: `${form.endDate} 00:00:00`,
    reason: form.reason
  }
}

async function handleSubmit() {
  const error = validateForm()
  if (error) {
    showMessage(error)
    return
  }

  if (!employeeId.value) {
    showMessage('请先登录')
    return
  }

  submitting.value = true
  uni.showLoading({ title: '提交中...' })
  const promptTitle = encodeURIComponent(pageConfig.value.title)
  try {
    const res = await submitApplication(buildPayload())
    uni.hideLoading()
    if (res.code === 200) {
      showMessage('提交成功')
      setTimeout(() => {
        uni.redirectTo({ url: `/workPages/prompt?result=success&title=${promptTitle}` })
      }, 600)
    } else {
      showMessage(res.msg || '提交失败')
      setTimeout(() => {
        uni.redirectTo({ url: `/workPages/prompt?result=fail&title=${promptTitle}` })
      }, 1200)
    }
  } catch (error) {
    uni.hideLoading()
    showMessage(error?.message || error || '提交失败')
    setTimeout(() => {
      uni.redirectTo({ url: `/workPages/prompt?result=fail&title=${promptTitle}` })
    }, 1200)
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss">
.apply-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #f3f6fa;
  color: #16233a;
}

.nav-back {
  width: 72rpx;
  height: 52rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: #eef3f8;
  color: #5d6f89;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.nav-title {
  width: 100%;
  text-align: center;
  color: #111827;
  font-size: 34rpx;
  font-weight: 800;
}

.apply-content {
  padding: 0 24rpx 180rpx;
  box-sizing: border-box;
}

.hero-card {
  display: flex;
  align-items: center;
  min-height: 160rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  color: #ffffff;
  box-shadow: 0 24rpx 54rpx rgba(78, 103, 142, 0.16);
}

.hero-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 26rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  flex-shrink: 0;
}

.hero-copy {
  margin-left: 24rpx;
}

.hero-title {
  font-size: 38rpx;
  font-weight: 900;
}

.hero-desc {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.86;
}

.section-card {
  margin-top: 24rpx;
  padding: 28rpx 28rpx 6rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 18rpx 50rpx rgba(69, 87, 116, 0.08);
}

.section-title {
  margin-bottom: 18rpx;
  padding-left: 16rpx;
  border-left: 6rpx solid #6f8fb9;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 1.2;
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
}

.info-item {
  width: 50%;
  padding: 14rpx 0 24rpx;
  box-sizing: border-box;
}

.info-label {
  display: block;
  color: #8b98aa;
  font-size: 24rpx;
}

.info-value {
  display: block;
  margin-top: 8rpx;
  color: #1d2b44;
  font-size: 28rpx;
  font-weight: 700;
}

.field-row,
.input-row,
.date-time-row,
.textarea-row,
.duration-box {
  padding: 26rpx 0;
  border-top: 1rpx solid #eef2f7;
}

.field-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label,
.date-time-label,
.duration-label {
  color: #1b2740;
  font-size: 28rpx;
  font-weight: 800;
}

.field-label text,
.date-time-label text {
  padding-left: 6rpx;
  color: #e85b65;
}

.field-value {
  margin-top: 10rpx;
  color: #1f2e48;
  font-size: 28rpx;
}

.placeholder {
  color: #aeb8c8;
  font-weight: 400;
}

.quota-value {
  color: #22a873;
  font-weight: 700;
}

.field-arrow {
  color: #b1bccb;
  font-family: tuniaoFont;
  font-size: 34rpx;
}

.date-time-controls {
  display: flex;
  gap: 18rpx;
  margin-top: 18rpx;
}

.date-time-picker {
  flex: 1;
}

.date-time-picker--time {
  width: 180rpx;
  flex: none;
}

.date-time-value,
.plain-input,
.plain-textarea {
  width: 100%;
  min-height: 78rpx;
  padding: 0 22rpx;
  border-radius: 16rpx;
  background: #f5f7fa;
  box-sizing: border-box;
  color: #1f2e48;
  font-size: 28rpx;
  line-height: 78rpx;
}

.plain-input {
  margin-top: 18rpx;
}

.textarea-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.textarea-head text {
  color: #9aa6b6;
  font-size: 24rpx;
}

.plain-textarea {
  height: 190rpx;
  margin-top: 18rpx;
  padding: 22rpx;
  line-height: 1.55;
}

.duration-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.duration-value {
  color: #526f9f;
  font-size: 30rpx;
  font-weight: 900;
}

.tips-card {
  padding-bottom: 28rpx;
  background: #fffaf0;
}

.tips-title {
  color: #c18117;
  font-size: 28rpx;
  font-weight: 800;
}

.tips-line {
  margin-top: 10rpx;
  color: #8d7b59;
  font-size: 24rpx;
}

.footer-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  display: flex;
  gap: 20rpx;
  max-width: 640px;
  margin: 0 auto;
  padding: 20rpx 28rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -12rpx 36rpx rgba(70, 84, 110, 0.08);
  box-sizing: border-box;
}

.footer-icon {
  margin-right: 8rpx;
}

.footer-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 999rpx;
  border: 0;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 88rpx;
}

.footer-btn--ghost {
  background: #eef3f8;
  color: #66758d;
}

.footer-btn--primary {
  color: #ffffff;
  box-shadow: 0 16rpx 32rpx rgba(82, 111, 159, 0.18);
}

.footer-btn[disabled] {
  opacity: 0.58;
}
</style>
