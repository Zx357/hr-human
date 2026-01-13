import request from '@/utils/request'

// 移动端登录方法（工号登录）
export function login(employeeNo, password) {
  const data = {
    employeeNo,
    password: password || ''
  }
  return request({
    'url': '/auth/mobile/login',
    headers: {
      isToken: false
    },
    'method': 'post',
    'data': data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    'url': '/auth/info',
    'method': 'get'
  })
}

// 获取当前登录员工信息（移动端使用）
export function getCurrentEmployee() {
  return request({
    'url': '/employee/current',
    'method': 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    'url': '/auth/logout',
    'method': 'post'
  })
}
