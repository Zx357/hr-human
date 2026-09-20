export default {
  envName: 'local',
  baseUrl: 'http://127.0.0.1:8080/api',
  // 高德地图配置:用于 H5 端打卡页 <map> 组件渲染,需与 manifest.json 的 h5.sdkConfigs.maps.amap.key 保持一致
  amap: {
    key: 'YOUR_AMAP_KEY'
  },
  // 旧配置项保留兼容读取(代码优先读 amap)
  googleMaps: {
    apiKey: 'YOUR_GOOGLE_MAPS_API_KEY',
    language: 'zh-CN',
    region: 'CN'
  },
  appInfo: {
    name: 'KAdmin',
    version: '1.0.0',
    logo: '/static/logo.png',
    siteUrl: 'http://localhost:9090',
    site_url: 'http://localhost:9090',
    agreements: [
      {
        title: '隐私政策',
        url: 'http://localhost:9090/protocol.html'
      },
      {
        title: '用户服务协议',
        url: 'http://localhost:9090/protocol.html'
      }
    ]
  }
}
