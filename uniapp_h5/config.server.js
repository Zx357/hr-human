export default {
  envName: 'server',
  baseUrl: '/api',
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
    siteUrl: 'https://renzi.zixu.ac.cn',
    site_url: 'https://renzi.zixu.ac.cn',
    agreements: [
      {
        title: '隐私政策',
        url: 'https://renzi.zixu.ac.cn/protocol.html'
      },
      {
        title: '用户服务协议',
        url: 'https://renzi.zixu.ac.cn/protocol.html'
      }
    ]
  }
}
