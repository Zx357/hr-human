import constant from './constant'

const storageKey = 'storage_data'
const storageNodeKeys = [
  constant.avatar,
  constant.id,
  constant.name,
  constant.roles,
  constant.permissions,
  'employeeInfo'
]

const storage = {
  set(key, value) {
    if (storageNodeKeys.indexOf(key) !== -1) {
      let tmp = uni.getStorageSync(storageKey)
      tmp = tmp || {}
      tmp[key] = value
      uni.setStorageSync(storageKey, tmp)
    }
  },
  get(key) {
    const storageData = uni.getStorageSync(storageKey) || {}
    return storageData[key] || ''
  },
  remove(key) {
    const storageData = uni.getStorageSync(storageKey) || {}
    delete storageData[key]
    uni.setStorageSync(storageKey, storageData)
  },
  clean() {
    uni.removeStorageSync(storageKey)
  }
}

export default storage
