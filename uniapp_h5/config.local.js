export default {
  envName: 'local',
  baseUrl: 'http://127.0.0.1:8080/api',
  // 高德地图配置:用于 H5 端打卡页 <map> 组件渲染,需与 manifest.json 的 h5.sdkConfigs.maps.amap.key 保持一致
  amap: {
    key: 'YOUR_AMAP_KEY'
  }
}
