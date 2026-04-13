# 宝塔反向代理模板

这目录放的是给 `KAdmin` 项目准备的宝塔 `Nginx` 配置模板：

- `admin-site.conf`：PC 管理端站点
- `h5-site.conf`：uniapp H5 站点
- `112.126.27.123-single-site.conf`：单 IP 共用一个站点时的现成配置，管理端走 `/`，H5 走 `/h5/`

使用前请先替换下面这些占位值：

- `admin.example.com` -> 你的管理端域名
- `m.example.com` -> 你的 H5 域名
- `/www/wwwroot/kadmin-sites/admin` -> 管理端 `dist` 上传后的目录
- `/www/wwwroot/kadmin-sites/h5` -> uniapp H5 上传后的目录

后端默认反代到：

- `http://127.0.0.1:8086`

对应当前生产配置文件：

- `backend/src/main/resources/application-prod.yml`

建议部署方式：

1. 宝塔里创建两个站点
2. 把对应模板粘到站点配置文件中
3. 给两个站点都申请 SSL
4. 后端 Jar 用 `Supervisor` 或 `Java项目` 单独启动

如果当前只有服务器 IP、还没准备两个独立域名，可以直接用：

- `112.126.27.123-single-site.conf`

它已经按下面这组目录写好了：

- 管理端：`/www/wwwroot/renZi/admin/dist`
- H5：`/www/wwwroot/renZi/uniapp/web`

额外提醒：

- 管理端是 `history` 路由，所以必须保留 `try_files ... /index.html`
- H5 当前是 `hash` 路由，保留 `try_files` 也没问题
- H5 打卡定位在浏览器里基本要求 `HTTPS`
- 如果地图搜索要正常用，高德控制台还要放行正式域名
