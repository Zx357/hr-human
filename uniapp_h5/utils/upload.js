import config from '@/config'
import { getToken } from '@/utils/auth'

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

/**
 * 选择图片并上传到后端
 * @param {number} count 选择张数
 * @returns {Promise<string|string[]>} 上传后的 URL，单张返回字符串
 */
export function chooseAndUpload(count = 1) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      success: async (res) => {
        const paths = res.tempFilePaths || []
        try {
          uni.showLoading({ title: '上传中', mask: true })
          const urls = []
          for (const path of paths) {
            urls.push(await uploadImageToServer(path))
          }
          uni.hideLoading()
          resolve(count === 1 ? urls[0] : urls)
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: typeof error === 'string' ? error : '上传失败', icon: 'none' })
          reject(error)
        }
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}
