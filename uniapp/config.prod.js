module.exports = {
  // H5 部署到宝塔时统一走站点 /api 反向代理
  baseUrl: '/api',

  appInfo: {
    name: 'KAdmin',
    version: '1.0.0',
    logo: '/static/logo.png',
    site_url: 'http://112.126.27.123',
    agreements: [
      {
        title: '隐私政策',
        url: 'http://112.126.27.123/protocol.html'
      },
      {
        title: '用户服务协议',
        url: 'http://112.126.27.123/protocol.html'
      }
    ]
  }
}
