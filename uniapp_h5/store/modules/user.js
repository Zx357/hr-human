import config from '@/config'
import storage from '@/utils/storage'
import constant from '@/utils/constant'
import { login, logout, getCurrentEmployee } from '@/api/login'
import { getToken, removeToken, setToken } from '@/utils/auth'

const baseUrl = config.baseUrl
const defAva = '/static/author.jpg'

function clearLocalSession(commit) {
  commit('SET_TOKEN', '')
  commit('SET_ID', '')
  commit('SET_NAME', '')
  commit('SET_AVATAR', '')
  commit('SET_ROLES', [])
  commit('SET_PERMISSIONS', [])
  commit('SET_EMPLOYEE_INFO', null)
  removeToken()
  storage.clean()
  uni.removeStorageSync('userInfo')
}

const user = {
  state: () => ({
    token: getToken(),
    id: storage.get(constant.id),
    name: storage.get(constant.name),
    avatar: storage.get(constant.avatar),
    roles: storage.get(constant.roles),
    permissions: storage.get(constant.permissions),
    employeeInfo: storage.get('employeeInfo')
  }),

  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
    },
    SET_ID(state, id) {
      state.id = id
      storage.set(constant.id, id)
    },
    SET_NAME(state, name) {
      state.name = name
      storage.set(constant.name, name)
    },
    SET_AVATAR(state, avatar) {
      state.avatar = avatar
      storage.set(constant.avatar, avatar)
    },
    SET_ROLES(state, roles) {
      state.roles = roles
      storage.set(constant.roles, roles)
    },
    SET_PERMISSIONS(state, permissions) {
      state.permissions = permissions
      storage.set(constant.permissions, permissions)
    },
    SET_EMPLOYEE_INFO(state, info) {
      state.employeeInfo = info
      storage.set('employeeInfo', info)
      uni.setStorageSync('userInfo', info)
    }
  },

  actions: {
    Login({ commit }, userInfo) {
      const employeeNo = (userInfo.employeeNo || '').trim()
      const password = userInfo.password || ''
      return new Promise((resolve, reject) => {
        login(employeeNo, password)
          .then((res) => {
            if (res.code === 200 && res.data && res.data.token) {
              setToken(res.data.token)
              commit('SET_TOKEN', res.data.token)
              resolve()
            } else {
              reject(res.msg || '登录失败')
            }
          })
          .catch(reject)
      })
    },

    GetInfo({ commit }) {
      return new Promise((resolve) => {
        getCurrentEmployee()
          .then((res) => {
            if (res.code === 200 && res.data) {
              const employee = res.data
              commit('SET_ID', employee.id)
              commit('SET_NAME', employee.name)

              let avatarUrl = employee.avatar
              if (avatarUrl && !avatarUrl.startsWith('http')) {
                avatarUrl = baseUrl + avatarUrl
              }
              commit('SET_AVATAR', avatarUrl || defAva)
              commit('SET_ROLES', ['ROLE_EMPLOYEE'])
              commit('SET_PERMISSIONS', [])
              commit('SET_EMPLOYEE_INFO', employee)
              resolve(res)
              return
            }

            commit('SET_ROLES', ['ROLE_EMPLOYEE'])
            commit('SET_NAME', '员工')
            commit('SET_AVATAR', defAva)
            resolve(res)
          })
          .catch(() => {
            commit('SET_ROLES', ['ROLE_EMPLOYEE'])
            commit('SET_NAME', '员工')
            commit('SET_AVATAR', defAva)
            resolve({})
          })
      })
    },

    ClearSession({ commit }) {
      clearLocalSession(commit)
      return Promise.resolve()
    },

    LogOut({ commit }) {
      return logout()
        .catch(() => {})
        .then(() => {
          clearLocalSession(commit)
        })
    }
  }
}

export default user
