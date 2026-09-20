import request from '@/utils/request'
import config from '@/config'
import { getToken } from '@/utils/auth'

export function getEmployeeList(params = {}) {
  return request({
    url: '/employee/list',
    method: 'get',
    params
  })
}

export function getEmployeeDetail(id) {
  return request({
    url: `/employee/${id}`,
    method: 'get'
  })
}

/**
 * 上传员工头像
 * 后端接口: POST /file/upload/employee/avatar (multipart: file + employeeNo)
 * @param {string} filePath 本地临时文件路径
 * @param {string} employeeNo 员工工号
 * @returns {Promise<string>} 上传后的头像相对路径(/employee_photo/xxx.jpg)
 */
export function uploadEmployeeAvatar(filePath, employeeNo) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: config.baseUrl + '/file/upload/employee/avatar',
      filePath,
      name: 'file',
      formData: { employeeNo },
      header: {
        Authorization: 'Bearer ' + getToken()
      },
      success(res) {
        let data = res.data
        try {
          data = JSON.parse(res.data)
        } catch (error) {
          reject('上传返回数据异常')
          return
        }
        if (res.statusCode === 200 && (data.code === 200 || data.code === '200' || data.code === '0000')) {
          resolve(data.data)
          return
        }
        reject(data.msg || '上传失败')
      },
      fail(error) {
        reject(error.errMsg || '上传失败')
      }
    })
  })
}
