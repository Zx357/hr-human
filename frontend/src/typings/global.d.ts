export {};

declare global {
  export interface Window {
    /** NProgress instance */
    NProgress?: import('nprogress').NProgress;
    /** MessageBox instance */
    $messageBox?: import('element-plus').IElMessageBox;
    /** Message instance */
    $message?: import('element-plus').Message;
    /** Notification instance */
    $notification?: import('element-plus').Notify;
    /** AMap JS API security config */
    _AMapSecurityConfig?: {
      securityJsCode?: string;
      serviceHost?: string;
    };
  }

  /** Build time of the project */
  export const BUILD_TIME: string;
}
