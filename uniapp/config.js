// 应用全局配置
module.exports = {
  // 后端接口地址
  baseUrl: 'http://localhost:8080/api',
  // 应用信息
  appInfo: {
    // 应用名称
    name: "KAdmin",
    // 应用版本
    version: "1.0.0",
    // 应用logo
    logo: "/static/logo.png",
    // 官方网站
    site_url: "http://localhost:9527",
    // 政策协议
    agreements: [{
        title: "隐私政策",
        url: "http://localhost:9527/protocol.html"
      },
      {
        title: "用户服务协议",
        url: "http://localhost:9527/protocol.html"
      }
    ]
  }
}
