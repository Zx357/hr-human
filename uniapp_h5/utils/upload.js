import config from '@/config'
import { getToken } from '@/utils/auth'
import { handleAuthExpired } from '@/utils/auth-expired'

/**
 * 上传图片到后端 /file/upload/image
 * @param {string} filePath uni.chooseImage 返回的临时文件路径
 * @returns {Promise<string>} 上传后的图片 URL
 */
export function uploadImageToServer(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: config.baseUrl + '/file/upload/image',
      filePath,
      name: 'file',
      timeout: 60000,
      header: {
        Authorization: 'Bearer ' + getToken()
      },
      success(res) {
        // 会话过期:与 request.js 的处理对齐(清会话 + 回登录页)
        if (res.statusCode === 401) {
          handleAuthExpired(reject)
          return
        }
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
        if (data.code === '8888' || data.code === '9999') {
          handleAuthExpired(reject)
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
