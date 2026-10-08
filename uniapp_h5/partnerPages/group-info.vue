<template>
  <view class="group-info-page">
    <tn-navbar fixed home-icon="" :bottom-shadow="false" bg-color="#FFFFFF" :placeholder="false">
      <template #back>
        <view class="nav-back" @click="goBack">
          <tn-icon name="left-arrow"></tn-icon>
        </view>
      </template>
      <view class="nav-center">
        <text class="nav-title">群信息</text>
      </view>
    </tn-navbar>

    <scroll-view
      scroll-y
      class="page-scroll"
      :style="{ paddingTop: vuex_custom_bar_height + 'px' }"
    >
      <view class="page-wrap">
        <!-- 群名片 -->
        <view class="info-card">
          <view class="group-row" @click="isOwner ? openRename() : readonlyNameTip()">
            <image class="group-avatar" :src="groupAvatarUrl" mode="aspectFill" />
            <view class="group-main">
              <view class="group-name-wrap">
                <text class="group-name">{{ groupName || '群聊' }}</text>
                <tn-icon v-if="isOwner" name="edit-write" class="group-edit" />
              </view>
              <text class="group-sub">{{ members.length }} 名成员</text>
            </view>
          </view>
        </view>

        <!-- 成员列表 -->
        <view class="members-card">
          <view class="card-head">
            <text class="card-title">群成员</text>
            <view v-if="isOwner" class="add-btn" @click="openAddMembers">
              <tn-icon name="add" class="add-btn__icon" />
              <text>添加成员</text>
            </view>
          </view>

          <view v-if="loading" class="state-tip">加载中...</view>
          <view v-else-if="loadFailed && !members.length" class="state-tip">
            <view>成员加载失败</view>
            <view class="retry-btn" @click="loadMembers">点击重试</view>
          </view>
          <view v-else-if="!members.length" class="state-tip">暂无成员</view>

          <view
            v-for="item in members"
            :key="item.employeeId"
            class="member-item"
            @longpress="onMemberLongPress(item)"
          >
            <image class="member-avatar" :src="formatAvatar(item.avatar)" mode="aspectFill" />
            <view class="member-main">
              <view class="member-name-wrap">
                <text class="member-name">{{ item.name || '成员' }}</text>
                <view v-if="item.isOwner" class="owner-tag">群主</view>
                <view v-else-if="isSelf(item)" class="self-tag">我</view>
              </view>
            </view>
            <!-- 群主可移除其他成员;自己与群主不可移除 -->
            <view
              v-if="isOwner && !item.isOwner && !isSelf(item)"
              class="remove-btn"
              @click.stop="confirmRemove(item)"
            >
              移除
            </view>
          </view>
        </view>

        <!-- 操作区 -->
        <view class="action-card">
          <!-- 群主:解散群聊 -->
          <view v-if="isOwner" class="action-row action-row--danger" @click="confirmDissolve">
            <text>解散群聊</text>
            <tn-icon name="right" class="action-arrow" />
          </view>
          <!-- 普通成员:退出群聊(群主不可退,后端会拒绝,前端直接提示) -->
          <view v-else class="action-row action-row--danger" @click="confirmLeave">
            <text>退出群聊</text>
            <tn-icon name="right" class="action-arrow" />
          </view>
        </view>

        <view class="tip-text">
          {{ isOwner ? '群主可以修改群名、添加或移除成员、解散群聊' : '群成员可以退出群聊；群管理操作仅群主可用' }}
        </view>
      </view>
    </scroll-view>

    <!-- 添加成员弹层:内嵌选人(复用发起群聊的选人逻辑,排除已在群成员) -->
    <view v-if="addVisible" class="popup-mask" @click="closeAddMembers">
      <view class="popup-panel" @click.stop>
        <view class="popup-head">
          <text class="popup-title">添加成员</text>
          <view class="popup-close" @click="closeAddMembers">
            <tn-icon name="close"></tn-icon>
          </view>
        </view>
        <scroll-view scroll-y class="popup-body">
          <view v-if="addingEmployeesLoading" class="state-tip">加载中...</view>
          <view v-else-if="!addableEmployees.length" class="state-tip">没有可添加的同事</view>
          <view
            v-for="item in addableEmployees"
            :key="item.id"
            class="picker-item"
            @click="togglePick(item)"
          >
            <tn-icon
              :name="pickedIds.includes(item.id) ? 'circle-fill' : 'circle'"
              class="picker-check"
              :style="{ color: pickedIds.includes(item.id) ? '#3668FC' : '#C5CAD5' }"
            />
            <image class="member-avatar" :src="formatAvatar(item.avatar)" mode="aspectFill" />
            <view class="member-main">
              <text class="member-name">{{ item.name || item.employeeName || item.employeeNo || '未命名员工' }}</text>
              <text class="member-sub">{{ item.deptName || item.position || '' }}</text>
            </view>
          </view>
        </scroll-view>
        <view class="popup-foot">
          <tn-button
            width="100%"
            shape="round"
            bg-color="#3668FC"
            text-color="#FFFFFF"
            :font-size="28"
            :loading="submitting"
            :disabled="submitting || !pickedIds.length"
            :custom-style="{ padding: '20rpx 0' }"
            @click="submitAddMembers"
          >
            添加({{ pickedIds.length }})
          </tn-button>
        </view>
      </view>
    </view>

    <!-- 修改群名弹窗 -->
    <view v-if="renameVisible" class="popup-mask" @click="closeRename">
      <view class="rename-panel" @click.stop>
        <view class="popup-title">修改群名称</view>
        <input
          v-model="renameDraft"
          class="rename-input"
          maxlength="30"
          placeholder="请输入新的群名称"
          placeholder-class="rename-placeholder"
        />
        <view class="rename-actions">
          <tn-button
            shape="round"
            bg-color="#F1F3F7"
            text-color="#657189"
            :font-size="26"
            :custom-style="{ padding: '16rpx 0', flex: 1 }"
            @click="closeRename"
          >
            取消
          </tn-button>
          <tn-button
            shape="round"
            bg-color="#3668FC"
            text-color="#FFFFFF"
            :font-size="26"
            :loading="submitting"
            :disabled="submitting"
            :custom-style="{ padding: '16rpx 0', flex: 1, marginLeft: '20rpx' }"
            @click="submitRename"
          >
            确定
          </tn-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useStore } from 'vuex'
import { useCustomBarHeight, useGoBack } from '@/libs/composables'
import config from '@/config'
import { getEmployeeList } from '@/api/employee'
import {
  getGroupMembers,
  addGroupMembers,
  removeGroupMember,
  leaveGroup,
  renameGroup,
  dissolveGroup
} from '@/api/chat'
import { toastRequestError } from '@/utils/common'

const { vuex_custom_bar_height } = useCustomBarHeight()
const { goBack } = useGoBack()
const store = useStore()

const groupId = ref(null)
const groupName = ref('')
const groupAvatar = ref('')
const members = ref([])
const loading = ref(false)
const loadFailed = ref(false)
const submitting = ref(false)

// 添加成员弹层
const addVisible = ref(false)
const addingEmployeesLoading = ref(false)
const employees = ref([])
const pickedIds = ref([])

// 修改群名弹窗
const renameVisible = ref(false)
const renameDraft = ref('')

const myEmployeeId = computed(() => {
  const info = store.getters.employeeInfo || store.state.user?.employeeInfo || uni.getStorageSync('userInfo') || {}
  return store.getters.id || info.id || null
})

// 当前登录人是否群主(成员列表 isOwner 标识)
const isOwner = computed(() => {
  return members.value.some((item) => item.isOwner && String(item.employeeId) === String(myEmployeeId.value))
})

const isSelf = (item) => String(item.employeeId) === String(myEmployeeId.value)

const groupAvatarUrl = computed(() => formatAvatar(groupAvatar.value))

// 可添加的同事:排除已在群内的成员与自己
const addableEmployees = computed(() => {
  const inGroup = new Set(members.value.map((item) => String(item.employeeId)))
  return employees.value.filter((item) => !inGroup.has(String(item.id)) && String(item.id) !== String(myEmployeeId.value))
})

const formatAvatar = (avatar) => {
  if (!avatar) return '/static/author.jpg'
  if (/^https?:\/\//.test(avatar) || avatar.startsWith('/static')) return avatar
  return config.baseUrl + avatar
}

// ===== 成员列表 =====

async function loadMembers() {
  if (!groupId.value) return
  loading.value = true
  loadFailed.value = false
  try {
    const res = await getGroupMembers(groupId.value)
    const list = Array.isArray(res.data) ? res.data : []
    members.value = list.map((item) => ({
      employeeId: item.employeeId ?? item.id,
      name: item.name || item.employeeName || '成员',
      avatar: item.avatar,
      isOwner: Number(item.isOwner) === 1 || item.isOwner === true
    }))
  } catch (error) {
    loadFailed.value = true
    toastRequestError(error, '加载群成员失败')
  } finally {
    loading.value = false
  }
}

// ===== 添加成员 =====

function openAddMembers() {
  if (!isOwner.value) return
  addVisible.value = true
  pickedIds.value = []
  loadEmployees()
}

function closeAddMembers() {
  if (submitting.value) return
  addVisible.value = false
}

async function loadEmployees() {
  if (employees.value.length) return
  addingEmployeesLoading.value = true
  try {
    const res = await getEmployeeList({ status: 1 })
    employees.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    toastRequestError(error, '加载同事列表失败')
  } finally {
    addingEmployeesLoading.value = false
  }
}

function togglePick(item) {
  if (!item?.id) return
  if (pickedIds.value.includes(item.id)) {
    pickedIds.value = pickedIds.value.filter((id) => id !== item.id)
    return
  }
  pickedIds.value = [...pickedIds.value, item.id]
}

async function submitAddMembers() {
  if (!pickedIds.value.length || submitting.value) return
  submitting.value = true
  try {
    await addGroupMembers(groupId.value, pickedIds.value)
    uni.showToast({ icon: 'none', title: '添加成功' })
    addVisible.value = false
    pickedIds.value = []
    loadMembers()
  } catch (error) {
    toastRequestError(error, '添加成员失败')
  } finally {
    submitting.value = false
  }
}

// ===== 移除成员 =====

function onMemberLongPress(item) {
  if (!isOwner.value || item.isOwner || isSelf(item)) return
  confirmRemove(item)
}

function confirmRemove(item) {
  uni.showModal({
    title: '移除成员',
    content: `确定将「${item.name}」移出群聊吗？`,
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await removeGroupMember(groupId.value, item.employeeId)
        uni.showToast({ icon: 'none', title: '已移除' })
        loadMembers()
      } catch (error) {
        toastRequestError(error, '移除失败')
      }
    }
  })
}

// ===== 修改群名(群主) =====

function openRename() {
  renameDraft.value = groupName.value || ''
  renameVisible.value = true
}

function closeRename() {
  if (submitting.value) return
  renameVisible.value = false
}

function readonlyNameTip() {
  uni.showToast({ icon: 'none', title: '仅群主可修改群名称' })
}

async function submitRename() {
  const name = renameDraft.value.trim()
  if (!name) {
    uni.showToast({ icon: 'none', title: '请输入群名称' })
    return
  }
  if (name === groupName.value) {
    renameVisible.value = false
    return
  }
  submitting.value = true
  try {
    await renameGroup(groupId.value, name)
    groupName.value = name
    renameVisible.value = false
    uni.showToast({ icon: 'none', title: '群名已更新' })
    // 通知聊天页同步标题(后端会下发系统消息)
    uni.$emit('group-renamed', { groupId: groupId.value, name })
  } catch (error) {
    toastRequestError(error, '修改群名失败')
  } finally {
    submitting.value = false
  }
}

// ===== 退出/解散 =====

// 普通成员退出群聊
function confirmLeave() {
  uni.showModal({
    title: '退出群聊',
    content: '退出后将不再接收该群消息，确定退出吗？',
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await leaveGroup(groupId.value)
        uni.showToast({ icon: 'none', title: '已退出群聊' })
        setTimeout(() => uni.navigateBack(), 400)
      } catch (error) {
        toastRequestError(error, '退出失败')
      }
    }
  })
}

// 群主解散群聊
function confirmDissolve() {
  uni.showModal({
    title: '解散群聊',
    content: '解散后所有成员将退出该群，且不可恢复，确定解散吗？',
    confirmColor: '#FB6A67',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await dissolveGroup(groupId.value)
        uni.showToast({ icon: 'none', title: '群聊已解散' })
        uni.$emit('group-dissolved', { groupId: groupId.value })
        setTimeout(() => uni.navigateBack(), 400)
      } catch (error) {
        toastRequestError(error, '解散失败')
      }
    }
  })
}

onLoad((options) => {
  groupId.value = options?.groupId || options?.targetId || null
  if (options?.name) {
    groupName.value = decodeURIComponent(options.name)
  }
  if (!groupId.value) {
    uni.showToast({ icon: 'none', title: '缺少群信息' })
    return
  }
  loadMembers()
})
</script>

<style lang="scss" scoped>
.group-info-page {
  max-width: 640px;
  min-height: 100vh;
  margin: 0 auto;
  background: #F7F8FA;
}

.nav-back {
  width: 72rpx;
  height: 54rpx;
  margin-left: 18rpx;
  border-radius: 999rpx;
  background: rgba(29, 37, 65, 0.08);
  color: #1d2541;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  color: #1d2541;
  font-size: 34rpx;
  font-weight: 700;
}

.page-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.page-wrap {
  padding: 24rpx 24rpx 60rpx;
}

/* 群名片 */
.info-card,
.members-card,
.action-card {
  background: #ffffff;
  border-radius: 16rpx;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
}

.info-card {
  padding: 28rpx;
}

.group-row {
  display: flex;
  align-items: center;
}

.group-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  background: #f4f5f9;
  flex-shrink: 0;
}

.group-main {
  flex: 1;
  min-width: 0;
  margin-left: 22rpx;
}

.group-name-wrap {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.group-name {
  color: #1d2541;
  font-size: 32rpx;
  font-weight: 800;
  word-break: break-all;
}

.group-edit {
  color: #9aa4b2;
  font-size: 28rpx;
}

.group-sub {
  display: block;
  margin-top: 10rpx;
  color: #9aa4b2;
  font-size: 24rpx;
}

/* 成员列表 */
.members-card {
  margin-top: 20rpx;
  padding: 8rpx 28rpx 16rpx;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f3f2f7;
}

.card-title {
  color: #1d2541;
  font-size: 30rpx;
  font-weight: 700;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  background: rgba(54, 104, 252, 0.1);
  color: #3668fc;
  font-size: 24rpx;
  font-weight: 600;
}

.add-btn__icon {
  font-size: 26rpx;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f3f2f7;
}

.member-item:last-child {
  border-bottom: none;
}

.member-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  background: #f4f5f9;
  flex-shrink: 0;
}

.member-main {
  flex: 1;
  min-width: 0;
  margin-left: 20rpx;
}

.member-name-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.member-name {
  color: #1d2541;
  font-size: 28rpx;
  font-weight: 600;
}

.member-sub {
  display: block;
  margin-top: 6rpx;
  color: #9aa4b2;
  font-size: 22rpx;
}

.owner-tag,
.self-tag {
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 600;
}

.owner-tag {
  color: #ff9f2e;
  background: rgba(255, 172, 0, 0.14);
}

.self-tag {
  color: #3668fc;
  background: rgba(54, 104, 252, 0.1);
}

.remove-btn {
  flex-shrink: 0;
  padding: 8rpx 26rpx;
  border-radius: 999rpx;
  color: #fb6a67;
  font-size: 24rpx;
  background: rgba(251, 106, 103, 0.1);
}

/* 操作区 */
.action-card {
  margin-top: 20rpx;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 28rpx;
  color: #1d2541;
  font-size: 29rpx;
  font-weight: 600;
}

.action-row--danger {
  color: #fb6a67;
}

.action-arrow {
  color: #c4cbd4;
  font-size: 26rpx;
}

.tip-text {
  margin-top: 24rpx;
  padding: 0 8rpx;
  color: #9aa4b2;
  font-size: 23rpx;
  line-height: 1.6;
}

.state-tip {
  padding: 80rpx 0;
  color: #9aa4b2;
  font-size: 26rpx;
  text-align: center;
}

.retry-btn {
  margin: 24rpx auto 0;
  width: fit-content;
  padding: 12rpx 48rpx;
  border-radius: 999rpx;
  color: #3668fc;
  font-size: 25rpx;
  font-weight: 600;
  background: rgba(54, 104, 252, 0.1);
}

/* 弹层通用 */
.popup-mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: flex-end;
}

.popup-panel {
  width: 100%;
  max-width: 640px;
  margin: 0 auto;
  height: 72vh;
  display: flex;
  flex-direction: column;
  border-radius: 28rpx 28rpx 0 0;
  background: #fff;
}

.popup-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 30rpx 20rpx;
}

.popup-title {
  color: #1d2541;
  font-size: 32rpx;
  font-weight: 800;
}

.popup-close {
  width: 62rpx;
  height: 62rpx;
  border-radius: 50%;
  color: #657189;
  background: #f1f3f7;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popup-body {
  flex: 1;
  min-height: 0;
  padding: 0 30rpx;
  box-sizing: border-box;
}

.picker-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f3f2f7;
}

.picker-item:last-child {
  border-bottom: none;
}

.picker-check {
  flex-shrink: 0;
  margin-right: 20rpx;
  font-size: 34rpx;
}

.popup-foot {
  padding: 20rpx 30rpx calc(24rpx + env(safe-area-inset-bottom));
}

/* 修改群名弹窗 */
.rename-panel {
  width: calc(100% - 96rpx);
  max-width: 560px;
  margin: 0 auto 20vh;
  padding: 36rpx 32rpx 28rpx;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 24rpx;
  background: #fff;
  box-sizing: border-box;
}

.rename-input {
  margin-top: 28rpx;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #f7f8fa;
  border: 1rpx solid rgba(17, 31, 46, 0.06);
  font-size: 28rpx;
  box-sizing: border-box;
}

.rename-placeholder {
  color: #c4cbd4;
}

.rename-actions {
  display: flex;
  margin-top: 32rpx;
}
</style>
