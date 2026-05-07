module.exports = {
  baseUrl: 'http://127.0.0.1:8080/api',
  googleMaps: {
    apiKey: 'AIzaSyCn3NI3N3xbhH9a4Z8DhL75B7BA9zRMMcM',
    language: 'zh-CN',
    region: 'CN'
  },

  appInfo: {
    name: 'KAdmin',
    version: '1.0.0',
    logo: '/static/logo.png',
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
