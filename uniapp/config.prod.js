module.exports = {
  // H5 部署到宝塔时统一走站点 /api 反向代理
  baseUrl: '/api',
  googleMaps: {
    apiKey: 'AIzaSyCn3NI3N3xbhH9a4Z8DhL75B7BA9zRMMcM',
    language: 'zh-CN',
    region: 'CN'
  },

  appInfo: {
    name: 'KAdmin',
    version: '1.0.0',
    logo: '/static/logo.png',
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
