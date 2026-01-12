import { request } from '../request';

/** get constant routes */
export function fetchGetConstantRoutes() {
  // 常量路由从后端获取，返回格式为 { routes: [...], home: "home" }
  // 我们只需要routes部分
  return request<Api.Route.UserRoute>({ url: '/system/menu/routes' }).then(res => {
    if (res.data && res.data.routes) {
      return { ...res, data: res.data.routes };
    }
    return { ...res, data: [] };
  });
}

/** get user routes */
export function fetchGetUserRoutes() {
  return request<Api.Route.UserRoute>({ url: '/system/menu/routes' });
}

/**
 * whether the route is exist
 *
 * @param routeName route name
 */
export function fetchIsRouteExist(routeName: string) {
  return request<boolean>({ url: '/system/menu/isRouteExist', params: { routeName } });
}
