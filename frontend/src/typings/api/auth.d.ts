declare namespace Api {
  /**
   * namespace Auth
   *
   * backend api module: "auth"
   */
  namespace Auth {
    interface LoginToken {
      token: string;
      refreshToken: string;
    }

    interface UserInfo {
      userId: string;
      userName: string;
      roles: string[];
      buttons: string[];
      /** 关联员工 ID（未关联员工时为空，归属比对需回退 userId） */
      employeeId?: number;
      /** 员工头像（相对路径） */
      avatar?: string;
    }
  }
}
