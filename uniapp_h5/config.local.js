export default {
  envName: 'local',
  baseUrl: 'http://127.0.0.1:8080/api',
  // 高德地图配置:H5 端打卡页 <map> 组件的 Key/安全密钥优先从后台「系统管理-参数配置」读取
  // (接口 /config/public 的 map.amap.key / map.amap.security-code,运行时注入,无需配置 manifest.json),
  // 此处仅为后端未配置/不可用时的本地兜底
  amap: {
    key: 'YOUR_AMAP_KEY'
  }
}
