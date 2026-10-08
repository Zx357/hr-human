export default {
  envName: 'server',
  // H5 生产构建同源部署,用相对路径即可;小程序端由 config.js 解析为 mpBaseUrl(绝对地址)
  baseUrl: '/api',
  // 微信小程序端请求/WS 的绝对地址,需为 https 域名并在小程序后台配置 request/uploadFile 合法域名
  mpBaseUrl: 'https://renzi.zixu.ac.cn/api',
  // 高德地图配置:H5 端打卡页 <map> 组件的 Key/安全密钥优先从后台「系统管理-参数配置」读取
  // (接口 /config/public 的 map.amap.key / map.amap.security-code,运行时注入,无需配置 manifest.json),
  // 此处仅为后端未配置/不可用时的本地兜底
  amap: {
    key: 'YOUR_AMAP_KEY'
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
