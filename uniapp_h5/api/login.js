import request from '@/utils/request'

export function login(employeeNo, password) {
  return request({
    url: '/auth/mobile/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: {
      employeeNo,
      password: password || ''
    }
  })
}

export function getInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

export function getCurrentEmployee() {
  return request({
    url: '/employee/current',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 移动端修改密码
export function mobileChangePassword(oldPassword, newPassword) {
  return request({
    url: '/auth/mobile/change-password',
    method: 'post',
    data: { oldPassword, newPassword }
  })
}
