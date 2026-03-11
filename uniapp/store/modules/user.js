import config from '@/config'
import storage from '@/utils/storage'
import constant from '@/utils/constant'
import { isHttp, isEmpty } from "@/utils/validate"
import { login, logout, getCurrentEmployee } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import defAva from '@/static/images/profile.jpg'

const baseUrl = config.baseUrl

const user = {
  state: {
    token: getToken(),
    id: storage.get(constant.id),
    name: storage.get(constant.name),
    avatar: storage.get(constant.avatar),
    roles: storage.get(constant.roles),
    permissions: storage.get(constant.permissions),
    employeeInfo: storage.get('employeeInfo')
  },

  mutations: {
    SET_TOKEN: (state, token) => {
      state.token = token
    },
    SET_ID: (state, id) => {
      state.id = id
      storage.set(constant.id, id)
    },
    SET_NAME: (state, name) => {
      state.name = name
      storage.set(constant.name, name)
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
      storage.set(constant.avatar, avatar)
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
      storage.set(constant.roles, roles)
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
      storage.set(constant.permissions, permissions)
    },
    SET_EMPLOYEE_INFO: (state, info) => {
      state.employeeInfo = info
      storage.set('employeeInfo', info)
      // 同时保存到 userInfo 供申请页面使用
      uni.setStorageSync('userInfo', info)
    }
  },

  actions: {
    // 登录（工号登录）
    Login({ commit }, userInfo) {
      const employeeNo = (userInfo.employeeNo || '').trim()
      const password = userInfo.password || ''
      return new Promise((resolve, reject) => {
        console.log('调用登录接口', employeeNo, password)
        login(employeeNo, password).then(res => {
          console.log('登录接口返回', res)
          if (res.code === 200 && res.data && res.data.token) {
            setToken(res.data.token)
            commit('SET_TOKEN', res.data.token)
            console.log('Token已保存', res.data.token)
            resolve()
          } else {
            reject(res.msg || '登录失败')
          }
        }).catch(error => {
          console.log('登录接口异常', error)
          reject(error)
        })
      })
    },

    // 获取用户信息（移动端获取员工信息）
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getCurrentEmployee().then(res => {
          console.log('获取员工信息返回', res)
          if (res.code === 200 && res.data) {
            const employee = res.data
            commit('SET_ID', employee.id)
            commit('SET_NAME', employee.name)
            // 头像：如果是相对路径则拼接后端地址
            let avatarUrl = employee.avatar
            if (avatarUrl && !avatarUrl.startsWith('http')) {
              avatarUrl = baseUrl + avatarUrl
            }
            commit('SET_AVATAR', avatarUrl || defAva)
            commit('SET_ROLES', ['ROLE_EMPLOYEE'])
            commit('SET_PERMISSIONS', [])
            commit('SET_EMPLOYEE_INFO', employee)
            resolve(res)
          } else {
            console.log('获取员工信息失败，使用默认值', res)
            // 即使获取失败也不阻塞，设置默认值
            commit('SET_ROLES', ['ROLE_EMPLOYEE'])
            commit('SET_NAME', '员工')
            commit('SET_AVATAR', defAva)
            resolve(res)
          }
        }).catch(error => {
          console.log('获取员工信息异常', error)
          // 获取用户信息失败，设置默认值
          commit('SET_ROLES', ['ROLE_EMPLOYEE'])
          commit('SET_NAME', '员工')
          commit('SET_AVATAR', defAva)
          resolve({})
        })
      })
    },

    // 退出系统
    LogOut({ commit, state }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          commit('SET_TOKEN', '')
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          commit('SET_EMPLOYEE_INFO', null)
          removeToken()
          storage.clean()
          uni.removeStorageSync('userInfo')
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default user
