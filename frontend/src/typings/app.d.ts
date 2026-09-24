/** The global namespace for the app */
declare namespace App {
  /** Theme namespace */
  namespace Theme {
    type ColorPaletteNumber = import('@sa/color').ColorPaletteNumber;

    /** Theme setting */
    interface ThemeSetting {
      /** Theme scheme */
      themeScheme: UnionKey.ThemeScheme;
      /** grayscale mode */
      grayscale: boolean;
      /** colour weakness mode */
      colourWeakness: boolean;
      /** Whether to recommend color */
      recommendColor: boolean;
      /** Theme color */
      themeColor: string;
      /** Other color */
      otherColor: OtherColor;
      /** Whether info color is followed by the primary color */
      isInfoFollowPrimary: boolean;
      /** Layout */
      layout: {
        /** Layout mode */
        mode: UnionKey.ThemeLayoutMode;
        /** Scroll mode */
        scrollMode: UnionKey.ThemeScrollMode;
        /**
         * Whether to reverse the horizontal mix
         *
         * if true, the vertical child level menus in left and horizontal first level menus in top
         */
        reverseHorizontalMix: boolean;
      };
      /** Page */
      page: {
        /** Whether to show the page transition */
        animate: boolean;
        /** Page animate mode */
        animateMode: UnionKey.ThemePageAnimateMode;
      };
      /** Header */
      header: {
        /** Header height */
        height: number;
        /** Header breadcrumb */
        breadcrumb: {
          /** Whether to show the breadcrumb */
          visible: boolean;
          /** Whether to show the breadcrumb icon */
          showIcon: boolean;
        };
        /** Multilingual */
        multilingual: {
          /** Whether to show the multilingual */
          visible: boolean;
        };
        /** Global search */
        globalSearch: {
          /** Whether to show the global search */
          visible: boolean;
        };
      };
      /** Tab */
      tab: {
        /** Whether to show the tab */
        visible: boolean;
        /**
         * Whether to cache the tab
         *
         * If cache, the tabs will get from the local storage when the page is refreshed
         */
        cache: boolean;
        /** Tab height */
        height: number;
        /** Tab mode */
        mode: UnionKey.ThemeTabMode;
      };
      /** Fixed header and tab */
      fixedHeaderAndTab: boolean;
      /** Sider */
      sider: {
        /** Inverted sider */
        inverted: boolean;
        /** Sider width */
        width: number;
        /** Collapsed sider width */
        collapsedWidth: number;
        /** Sider width when the layout is 'vertical-mix' or 'horizontal-mix' */
        mixWidth: number;
        /** Collapsed sider width when the layout is 'vertical-mix' or 'horizontal-mix' */
        mixCollapsedWidth: number;
        /** Child menu width when the layout is 'vertical-mix' or 'horizontal-mix' */
        mixChildMenuWidth: number;
      };
      /** Footer */
      footer: {
        /** Whether to show the footer */
        visible: boolean;
        /** Whether fixed the footer */
        fixed: boolean;
        /** Footer height */
        height: number;
        /** Whether float the footer to the right when the layout is 'horizontal-mix' */
        right: boolean;
      };
      /** Watermark */
      watermark: {
        /** Whether to show the watermark */
        visible: boolean;
        /** Watermark text */
        text: string;
        /** Whether to use user name as watermark text */
        enableUserName: boolean;
      };
      /** define some theme settings tokens, will transform to css variables */
      tokens: {
        light: ThemeSettingToken;
        dark?: {
          [K in keyof ThemeSettingToken]?: Partial<ThemeSettingToken[K]>;
        };
      };
    }

    interface OtherColor {
      info: string;
      success: string;
      warning: string;
      error: string;
    }

    interface ThemeColor extends OtherColor {
      primary: string;
    }

    type ThemeColorKey = keyof ThemeColor;

    type ThemePaletteColor = {
      [key in ThemeColorKey | `${ThemeColorKey}-${ColorPaletteNumber}`]: string;
    };

    type BaseToken = Record<string, Record<string, string>>;

    interface ThemeSettingTokenColor {
      /** the progress bar color, if not set, will use the primary color */
      nprogress?: string;
      container: string;
      layout: string;
      inverted: string;
      'base-text': string;
    }

    interface ThemeSettingTokenBoxShadow {
      header: string;
      sider: string;
      tab: string;
    }

    interface ThemeSettingToken {
      colors: ThemeSettingTokenColor;
      boxShadow: ThemeSettingTokenBoxShadow;
    }

    type ThemeTokenColor = ThemePaletteColor & ThemeSettingTokenColor;

    /** Theme token CSS variables */
    type ThemeTokenCSSVars = {
      colors: ThemeTokenColor & { [key: string]: string };
      boxShadow: ThemeSettingTokenBoxShadow & { [key: string]: string };
    };
  }

  /** Global namespace */
  namespace Global {
    type VNode = import('vue').VNode;
    type RouteLocationNormalizedLoaded = import('vue-router').RouteLocationNormalizedLoaded;
    type RouteKey = import('@elegant-router/types').RouteKey;
    type RouteMap = import('@elegant-router/types').RouteMap;
    type RoutePath = import('@elegant-router/types').RoutePath;
    type LastLevelRouteKey = import('@elegant-router/types').LastLevelRouteKey;

    /** The global header props */
    interface HeaderProps {
      /** Whether to show the logo */
      showLogo?: boolean;
      /** Whether to show the menu toggler */
      showMenuToggler?: boolean;
      /** Whether to show the menu */
      showMenu?: boolean;
    }

    /** The global menu */
    type Menu = {
      /**
       * The menu key
       *
       * Equal to the route key
       */
      key: string;
      /** The menu label */
      label: string;
      /** The menu i18n key */
      i18nKey?: I18n.I18nKey | null;
      /** The route key */
      routeKey: RouteKey;
      /** The route path */
      routePath: RoutePath;
      /** The menu icon */
      icon?: () => VNode;
      /** The menu children */
      children?: Menu[];
    };

    type Breadcrumb = Omit<Menu, 'children'> & {
      options?: Breadcrumb[];
    };

    /** Tab route */
    type TabRoute = Pick<RouteLocationNormalizedLoaded, 'name' | 'path' | 'meta'> &
      Partial<Pick<RouteLocationNormalizedLoaded, 'fullPath' | 'query' | 'matched'>>;

    /** The global tab */
    type Tab = {
      /** The tab id */
      id: string;
      /** The tab label */
      label: string;
      /**
       * The new tab label
       *
       * If set, the tab label will be replaced by this value
       */
      newLabel?: string;
      /**
       * The old tab label
       *
       * when reset the tab label, the tab label will be replaced by this value
       */
      oldLabel?: string;
      /** The tab route key */
      routeKey: LastLevelRouteKey;
      /** The tab route path */
      routePath: RouteMap[LastLevelRouteKey];
      /** The tab route full path */
      fullPath: string;
      /** The tab fixed index */
      fixedIndex?: number | null;
      /**
       * Tab icon
       *
       * Iconify icon
       */
      icon?: string;
      /**
       * Tab local icon
       *
       * Local icon
       */
      localIcon?: string;
      /** I18n key */
      i18nKey?: I18n.I18nKey | null;
      /** Original route title */
      title?: string;
      /** English route title */
      titleEn?: string;
    };

    /** Form rule */
    type FormRule = import('element-plus').FormItemRule;

    /** The global dropdown key */
    type DropdownKey = 'closeCurrent' | 'closeOther' | 'closeLeft' | 'closeRight' | 'closeAll';
  }

  /**
   * I18n namespace
   *
   * Locales type
   */
  namespace I18n {
    type RouteKey = import('@elegant-router/types').RouteKey;

    type LangType = 'en-US' | 'zh-CN';

    type LangOption = {
      label: string;
      key: LangType;
    };

    type I18nRouteKey = Exclude<RouteKey, 'root' | 'not-found'>;

    type FormMsg = {
      required: string;
      invalid: string;
    };

    type Schema = {
      headerNotice: {
        pending: string;
        notices: string;
        viewAll: string;
        noPending: string;
        noNotice: string;
      };
      salary: {
        common: {
          /** 方向 */
          direction: string;
          /** 取值类型 */
          valueType: string;
          /** 收入 */
          income: string;
          /** 扣款 */
          deduction: string;
          valueTypeFixed: string;
          valueTypeRatio: string;
          valueTypeAttendance: string;
          valueTypeManual: string;
          enabled: string;
          disabled: string;
          sortOrder: string;
        },
        item: {
          title: string;
          addItem: string;
          editItem: string;
          itemName: string;
          itemCode: string;
          codePattern: string;
          pleaseInputName: string;
          pleaseInputCode: string;
          config: string;
          ratioBaseCode: string;
          ratioBaseCodeTip: string;
          ratioValue: string;
          attRule: string;
          unitPrice: string;
          tolerance: string;
          manualTip: string;
          confirmDelete: string;
          ruleLateTimes: string;
          ruleLateMinutes: string;
          ruleAbsentDays: string;
          rulePersonalLeaveHours: string;
          ruleOvertimeHours: string;
          ruleFullAttendance: string;
          ruleAttendDays: string;
        },
        scheme: {
          title: string;
          keyword: string;
          keywordTip: string;
          addScheme: string;
          editScheme: string;
          schemeName: string;
          schemeCode: string;
          schemeCodeTip: string;
          pleaseInputName: string;
          itemCount: string;
          configItems: string;
          confirmDelete: string;
          items: string;
          selectItemToAdd: string;
          defaultAmount: string;
          moveUp: string;
          moveDown: string;
          orderTip: string;
        },
        archive: {
          title: string;
          archivedOnly: string;
          scheme: string;
          notBound: string;
          effectiveDate: string;
          bind: string;
          rebind: string;
          adjustItems: string;
          unbind: string;
          confirmUnbind: string;
          bindDialogTitle: string;
          pleaseSelectScheme: string;
          bindTip: string;
          amount: string;
          itemsDialogTitle: string;
        },
        payroll: {
          title: string;
          createBatch: string;
          createAndCompute: string;
          createSuccess: string;
          employeeCount: string;
          totalGross: string;
          totalNet: string;
          viewPayslips: string;
          payslipList: string;
          viewDetail: string;
          payslipDetail: string;
          compute: string;
          computeConfirm: string;
          computeSuccess: string;
          confirm: string;
          confirmTip: string;
          publish: string;
          publishTip: string;
          publishSuccess: string;
          unlock: string;
          unlockTip: string;
          /** 删除批次将同时删除其下所有工资条明细，确定删除？ */
          deleteTip: string;
          statusComputing: string;
          statusComputed: string;
          statusConfirmed: string;
          statusPublished: string;
          grossPay: string;
          totalDeduction: string;
          netPay: string;
        }
      },

      system: {
        title: string;
        updateTitle: string;
        updateContent: string;
        updateConfirm: string;
        updateCancel: string;
      };
      common: {
        action: string;
        add: string;
        addSuccess: string;
        backToHome: string;
        batchDelete: string;
        cancel: string;
        close: string;
        check: string;
        expandColumn: string;
        columnSetting: string;
        config: string;
        confirm: string;
        delete: string;
        deleteSuccess: string;
        confirmDelete: string;
        edit: string;
        warning: string;
        error: string;
        index: string;
        keywordSearch: string;
        logout: string;
        logoutConfirm: string;
        lookForward: string;
        modify: string;
        modifySuccess: string;
        noData: string;
        operate: string;
        pleaseCheckValue: string;
        refresh: string;
        reset: string;
        search: string;
        switch: string;
        tip: string;
        trigger: string;
        update: string;
        updateSuccess: string;
        userCenter: string;
        yesOrNo: {
          yes: string;
          no: string;
        };
        /** 状态 */
        status: string;
        /** 工号 */
        employeeNo: string;
        /** 部门 */
        department: string;
        /** 公司 */
        company: string;
        /** 姓名 */
        name: string;
        /** 已撤销 */
        withdrawn: string;
        /** 员工姓名 */
        employeeName: string;
        /** 启用 */
        enable: string;
        /** 至 */
        to: string;
        /** 序号 */
        index2: string;
        /** 请选择状态 */
        pleaseSelectStatus: string;
        /** 待审批 */
        pendingApproval: string;
        /** 请选择员工 */
        pleaseSelectEmployees: string;
        /** 禁用 */
        disable: string;
        /** 选择员工 */
        selectEmployees: string;
        /** 详情 */
        details: string;
        /** 开始日期 */
        startDate: string;
        /** 结束日期 */
        endDate: string;
        /** 确定 */
        ok: string;
        /** 请填写必填项 */
        pleaseFillRequired: string;
        /** 类型 */
        type: string;
        /** 请输入员工姓名 */
        pleaseInputEmployeeName: string;
        /** 已拒绝 */
        rejected: string;
        /** 撤销 */
        withdraw: string;
        /** 请输入员工编号 */
        pleaseInputEmployeeNo: string;
        /** 已通过 */
        approved: string;
        /** 选择日期 */
        selectDate: string;
        /** 备注 */
        remark: string;
        /** 开始时间 */
        startTime: string;
        /** 结束时间 */
        endTime: string;
        /** 员工 */
        employee: string;
        /** 入职日期 */
        entryDate: string;
        /** 正常 */
        normal: string;
        /** 请选择类型 */
        pleaseSelectType: string;
        /** 停用 */
        deactivate: string;
        /** 请假 */
        leave: string;
        /** 手机号 */
        phone: string;
        /** 性别 */
        gender: string;
        /** 未知 */
        unknown: string;
        /** 排序 */
        sort: string;
        /** 离职 */
        resigned: string;
        /** 请选择公司 */
        pleaseSelectCompany: string;
        /** 在职 */
        active: string;
        /** 邮箱 */
        email: string;
        /** 迟到 */
        late: string;
        /** 早退 */
        earlyLeave: string;
        /** 旷工 */
        absent: string;
        /** 保存 */
        save: string;
        /** 创建时间 */
        createTime: string;
        /** 小时 */
        hours: string;
        /** 离职类型 */
        resignationType: string;
        /** 奖励 */
        reward: string;
        /** 惩罚 */
        punishment: string;
        /** 类别 */
        category: string;
        /** 描述 */
        description: string;
        /** 请选择部门 */
        pleaseSelectDepartment: string;
        /** 日期范围 */
        dateRange: string;
        /** 请输入姓名 */
        pleaseInputName: string;
        /** 学历 */
        education: string;
        /** 男 */
        male: string;
        /** 女 */
        female: string;
        /** 人数 */
        employees: string;
        /** 请输入昵称 */
        pleaseEnterNickName: string;
        /** 昵称 */
        nickname: string;
        /** 请假类型 */
        leaveType: string;
        /** 金额 */
        amount: string;
        /** 出差 */
        businessTrip: string;
        /** 保存成功 */
        saveSuccess: string;
        /** 导出 */
        export: string;
        /** 请选择组织 */
        pleaseSelectOrganization: string;
        /** 月份 */
        month: string;
        /** 请输入邮箱 */
        pleaseEnterEmail: string;
        /** 联系电话 */
        contactPhone: string;
        /** 请输入新密码 */
        pleaseEnterNewPassword: string;
        /** 计算中... */
        calculatingDot: string;
        /** 选择时间 */
        selectTime: string;
        /** 密度 */
        density: string;
        /** 请选择时间范围 */
        pleaseSelectTimeRange: string;
        /** 下载失败 */
        downloadFailed: string;
        /** 大 */
        sizeLarge: string;
        /** 默认 */
        sizeDefault: string;
        /** 小 */
        sizeSmall: string;
        /** 工作交接人 */
        handoverPerson: string;
        /** 补卡 */
        makeupClock: string;
        /** 通过 */
        approve: string;
        /** 拒绝 */
        reject: string;
        /** 打卡地址 */
        clockAddress: string;
        /** 组织 */
        organization: string;
        /** 年龄分布 */
        ageDistribution: string;
        /** 待入职 */
        pendingEntry: string;
        /** 政治面貌 */
        politicalStatus: string;
        /** 职位 */
        position: string;
        /** 联系人 */
        contact: string;
        /** 时间 */
        time: string;
        /** 员工总数 */
        totalEmployees: string;
        /** 其他 */
        other: string;
        /** 图标 */
        icon: string;
        /** 成功 */
        success: string;
        /** 失败 */
        fail: string;
        /** 用户名 */
        username: string;
        /** 原因 */
        reason: string;
        /** 审批 */
        approval: string;
        /** 请假申请 */
        leaveApplication: string;
        /** 加班申请 */
        overtimeApplication: string;
        /** 出差申请 */
        businessTripApplication: string;
        /** 补卡申请 */
        makeupClockApplication: string;
        /** 换休申请 */
        exchangeLeaveApplication: string;
        /** 转正申请 */
        regularizationApplication: string;
        /** 调动申请 */
        transferApplication: string;
        /** 离职申请 */
        resignationApplication: string;
        /** 加班 */
        overtime: string;
        /** 请输入 */
        pleaseInput: string;
        /** 打卡记录 */
        clockRecords: string;
        /** 上班 */
        clockIn: string;
        /** 下班 */
        clockOut: string;
        /** 全部 */
        all: string;
        /** 日期 */
        date: string;
        /** 缺卡 */
        missingClock: string;
        /** 关联员工 */
        assignEmployees: string;
        /** 合计 */
        total: string;
        /** 联动 */
        cascade: string;
        /** 组织架构 */
        organizationStructure: string;
        /** 查看 */
        view: string;
        /** 紧急联系人 */
        emergencyContact: string;
        /** 身份证号 */
        idNumber: string;
        /** 民族 */
        ethnicity: string;
        /** 婚姻状况 */
        maritalStatus: string;
        /** 职务 */
        jobTitle: string;
        /** 籍贯 */
        nativePlace: string;
        /** 请选择 */
        pleaseSelect: string;
        /** 异常 */
        abnormal: string;
        /** 离职日期 */
        resignationDate: string;
        /** 路径 */
        path: string;
        /** 按钮 */
        button: string;
        /** 显示 */
        show: string;
        /** 隐藏 */
        hide: string;
        /** 请输入手机号 */
        pleaseEnterPhoneNumber: string;
        /** 新密码 */
        newPassword: string;
        /** 交接人 */
        handoverPerson2: string;
        /** 换休 */
        exchangeLeave: string;
        /** 转正 */
        regularization: string;
        /** 调动 */
        transfer: string;
        /** 奖惩 */
        rewardPunishment: string;
        /** 班次 */
        shift: string;
        /** 工时 */
        workHours: string;
        /** 必填 */
        required: string;
        /** 年份 */
        year: string;
        /** 打卡地点 */
        clockLocation: string;
        /** 移除 */
        remove: string;
        /** 联动选择 */
        cascadeSelect: string;
        /** 坐标 */
        coordinates: string;
        /** 纬度 */
        latitude: string;
        /** 经度 */
        longitude: string;
        /** 清除 */
        clear: string;
        /** 日考勤 */
        dailyAttendance: string;
        /** 员工列表 */
        employeeList: string;
        /** 删除确认 */
        deleteConfirmTitle: string;
        /** 负责人 */
        manager: string;
        /** 全部状态 */
        allStatus: string;
        /** 字典数据 */
        dictionaryData: string;
        /** 刷新 */
        refresh2: string;
        /** 关键词 */
        keyword: string;
        /** 更新时间 */
        updateTime: string;
        /** 折叠 */
        collapse: string;
        /** 展开 */
        expand: string;
        /** 分组 */
        group: string;
        /** 内容 */
        content: string;
        /** 展开/折叠 */
        expandCollapse: string;
        /** 全选/全不选 */
        selectAllOrNone: string;
        /** 密码 */
        password: string;
        /** 角色 */
        role: string;
        /** 加载中... */
        loadingDot: string;
        /** 提交 */
        submit: string;
        /** 返回 */
        back: string;
        /** 暂无 */
        none: string;
        /** 加载中 */
        loading: string;
        /** 计算中 */
        calculating: string;
        /** 操作成功 */
        operationSuccess: string;
        /** 操作失败 */
        operationFailed: string;
        /** 系统提示 */
        systemTip: string;
        /** 年龄 */
        age: string;
        /** 全选 */
        selectAll: string;
        /** 全不选 */
        unselectAll: string;
        /** 导入 */
        import: string;
        /** 颜色 */
        color: string;
        /** 链接 */
        link: string;
        /** 确定删除该记录吗？ */
        confirmDeleteRecord: string;
        /** 生日 */
        birthday: string;
        /** 米 */
        meter: string;
        /** 共 */
        totalPrefix: string;
        /** 首页 */
        home: string;
      };
      request: {
        logout: string;
        logoutMsg: string;
        logoutWithModal: string;
        logoutWithModalMsg: string;
        refreshToken: string;
        tokenExpired: string;
        operationFailed: string;
      };
      theme: {
        themeSchema: { title: string } & Record<UnionKey.ThemeScheme, string>;
        grayscale: string;
        colourWeakness: string;
        layoutMode: { title: string; reverseHorizontalMix: string } & Record<UnionKey.ThemeLayoutMode, string>;
        recommendColor: string;
        recommendColorDesc: string;
        themeColor: {
          title: string;
          followPrimary: string;
        } & Theme.ThemeColor;
        scrollMode: { title: string } & Record<UnionKey.ThemeScrollMode, string>;
        page: {
          animate: string;
          mode: { title: string } & Record<UnionKey.ThemePageAnimateMode, string>;
        };
        fixedHeaderAndTab: string;
        header: {
          height: string;
          breadcrumb: {
            visible: string;
            showIcon: string;
          };
          multilingual: {
            visible: string;
          };
          globalSearch: {
            visible: string;
          };
        };
        tab: {
          visible: string;
          cache: string;
          height: string;
          mode: { title: string } & Record<UnionKey.ThemeTabMode, string>;
        };
        sider: {
          inverted: string;
          width: string;
          collapsedWidth: string;
          mixWidth: string;
          mixCollapsedWidth: string;
          mixChildMenuWidth: string;
        };
        footer: {
          visible: string;
          fixed: string;
          height: string;
          right: string;
        };
        watermark: {
          visible: string;
          text: string;
          enableUserName: string;
        };
        themeDrawerTitle: string;
        pageFunTitle: string;
        configOperation: {
          copyConfig: string;
          copySuccessMsg: string;
          resetConfig: string;
          resetSuccessMsg: string;
        };
      };
      route: Record<string, string>;
      page: {
        login: {
          common: {
            loginOrRegister: string;
            loginSubtitle: string;
            userNamePlaceholder: string;
            phonePlaceholder: string;
            codePlaceholder: string;
            passwordPlaceholder: string;
            confirmPasswordPlaceholder: string;
            codeLogin: string;
            confirm: string;
            back: string;
            validateSuccess: string;
            loginSuccess: string;
            welcomeBack: string;
          };
          pwdLogin: {
            title: string;
            rememberMe: string;
            contactAdminReset: string;
            forgetPassword: string;
            register: string;
            otherAccountLogin: string;
            otherLoginMode: string;
            superAdmin: string;
            admin: string;
            user: string;
          };
          codeLogin: {
            title: string;
            getCode: string;
            reGetCode: string;
            sendCodeSuccess: string;
            imageCodePlaceholder: string;
          };
          register: {
            title: string;
            agreement: string;
            protocol: string;
            policy: string;
          };
          resetPwd: {
            title: string;
          };
          bindWeChat: {
            title: string;
          };
        };
        about: {
          title: string;
          introduction: string;
          projectInfo: {
            title: string;
            version: string;
            latestBuildTime: string;
            githubLink: string;
            previewLink: string;
          };
          prdDep: string;
          devDep: string;
        };
        home: {
          branchDesc: string;
          greeting: string;
          weatherDesc: string;
          projectCount: string;
          todo: string;
          message: string;
          downloadCount: string;
          registerCount: string;
          schedule: string;
          study: string;
          work: string;
          rest: string;
          entertainment: string;
          visitCount: string;
          turnover: string;
          dealCount: string;
          projectNews: {
            title: string;
            moreNews: string;
            desc1: string;
            desc2: string;
            desc3: string;
            desc4: string;
            desc5: string;
          };
          creativity: string;
        };
        function: {
          tab: {
            tabOperate: {
              title: string;
              addTab: string;
              addTabDesc: string;
              closeTab: string;
              closeCurrentTab: string;
              closeAboutTab: string;
              addMultiTab: string;
              addMultiTabDesc1: string;
              addMultiTabDesc2: string;
            };
            tabTitle: {
              title: string;
              changeTitle: string;
              change: string;
              resetTitle: string;
              reset: string;
            };
          };
          multiTab: {
            routeParam: string;
            backTab: string;
          };
          toggleAuth: {
            toggleAccount: string;
            authHook: string;
            superAdminVisible: string;
            adminVisible: string;
            adminOrUserVisible: string;
          };
          request: {
            repeatedErrorOccurOnce: string;
            repeatedError: string;
            repeatedErrorMsg1: string;
            repeatedErrorMsg2: string;
          };
        };
        alova: {
          scenes: {
            captchaSend: string;
            autoRequest: string;
            visibilityRequestTips: string;
            pollingRequestTips: string;
            networkRequestTips: string;
            refreshTime: string;
            startRequest: string;
            stopRequest: string;
            requestCrossComponent: string;
            triggerAllRequest: string;
          };
        };
        manage: {
          common: {
            status: {
              enable: string;
              disable: string;
            };
          };
          role: {
            title: string;
            roleName: string;
            roleCode: string;
            roleStatus: string;
            roleDesc: string;
            form: {
              roleName: string;
              roleCode: string;
              roleStatus: string;
              roleDesc: string;
            };
            addRole: string;
            editRole: string;
            menuAuth: string;
            buttonAuth: string;
          };
          user: {
            title: string;
            userName: string;
            userGender: string;
            nickName: string;
            userPhone: string;
            userEmail: string;
            userStatus: string;
            userRole: string;
            form: {
              userName: string;
              userGender: string;
              nickName: string;
              userPhone: string;
              userEmail: string;
              userStatus: string;
              userRole: string;
            };
            addUser: string;
            editUser: string;
            gender: {
              male: string;
              female: string;
            };
          };
          menu: {
            home: string;
            title: string;
            id: string;
            parentId: string;
            menuType: string;
            menuName: string;
            routeName: string;
            routePath: string;
            pathParam: string;
            layout: string;
            page: string;
            i18nKey: string;
            icon: string;
            localIcon: string;
            iconTypeTitle: string;
            order: string;
            constant: string;
            keepAlive: string;
            href: string;
            hideInMenu: string;
            activeMenu: string;
            multiTab: string;
            fixedIndexInTab: string;
            query: string;
            button: string;
            buttonCode: string;
            buttonDesc: string;
            menuStatus: string;
            form: {
              home: string;
              menuType: string;
              menuName: string;
              routeName: string;
              routePath: string;
              pathParam: string;
              layout: string;
              page: string;
              i18nKey: string;
              icon: string;
              localIcon: string;
              order: string;
              keepAlive: string;
              href: string;
              hideInMenu: string;
              activeMenu: string;
              multiTab: string;
              fixedInTab: string;
              fixedIndexInTab: string;
              queryKey: string;
              queryValue: string;
              button: string;
              buttonCode: string;
              buttonDesc: string;
              menuStatus: string;
            };
            addMenu: string;
            editMenu: string;
            addChildMenu: string;
            type: {
              directory: string;
              menu: string;
            };
            iconType: {
              iconify: string;
              local: string;
            };
          };
        };
      };
      form: {
        required: string;
        userName: FormMsg;
        phone: FormMsg;
        pwd: FormMsg;
        confirmPwd: FormMsg;
        code: FormMsg;
        email: FormMsg;
      };
      dropdown: Record<Global.DropdownKey, string>;
      icon: {
        themeConfig: string;
        themeSchema: string;
        lang: string;
        fullscreen: string;
        fullscreenExit: string;
        reload: string;
        collapse: string;
        expand: string;
        pin: string;
        unpin: string;
      };
      datatable: {
        itemCount: string;
      };
      application: {
        types: {
          leave: string;
          overtime: string;
          business: string;
          makeup: string;
          exchange: string;
          regularization: string;
          transfer: string;
          reward: string;
          punish: string;
          resignation: string;
        };
        typeShort: {
          leave: string;
          overtime: string;
          business: string;
          makeup: string;
          exchange: string;
          regularization: string;
          transfer: string;
          reward: string;
          punish: string;
          resignation: string;
        };
        status: {
          pending: string;
          approved: string;
          rejected: string;
          cancelled: string;
        };
        durationUnit: {
          hour: string;
          day: string;
        };
        common: {
          /** 申请时间 */
          applicationTime: string;
          /** 申请人 */
          applicant: string;
          /** 撤销确认 */
          withdrawalConfirmation: string;
          /** 确认撤销 */
          confirmWithdrawal: string;
          /** 提交申请 */
          submitApplication: string;
          /** 申请提交成功 */
          applicationSubmittedSuccessfully: string;
          /** 所属公司 */
          company: string;
          /** 申请类型 */
          applicationType: string;
          /** 审批意见 */
          approvalComment: string;
          /** 新增申请 */
          newApplication: string;
          /** 申请理由 */
          applicationReason: string;
          /** 请输入申请理由 */
          pleaseEnterApplicationReason: string;
          /** 当前账号未关联员工 */
          currentAccountIsNotLinkedToAnEmployee: string;
          /** 申请原因 */
          applicationReason2: string;
          /** 确定撤销 {name} 的申请单「{title}」吗？撤销后不可恢复 */
          withdrawConfirmMessage: string;
        };
        business: {
          /** 确定撤销该申请吗？撤销后不可恢复 */
          areYouSureYouWantToWithdrawThisApplicationThisCannotBeUndone: string;
          /** 发起出差申请 */
          createBusinessTripApplication: string;
          /** 出差地点 */
          tripLocation: string;
          /** 出差事由 */
          tripReason: string;
          /** 出差时间无效，请检查员工排班 */
          invalidTripTimePleaseCheckTheEmployeeSchedule: string;
          /** 出差申请列表 */
          businessTripApplications: string;
          /** 提示：出差小时为0，可能是员工在该时间段没有排班 */
          noteTripHoursAre0TheEmployeeMayHaveNoScheduleInThisPeriod: string;
          /** 请输入出差地点 */
          pleaseEnterTripLocation: string;
          /** 出差时间 */
          tripTime: string;
          /** 出差小时 */
          businessTripHours: string;
          /** 请输入出差事由 */
          pleaseEnterTripReason: string;
        };
        reward: {
          /** 生效日期 */
          effectiveDate: string;
          /** 发起奖惩申请 */
          createRewardPunishmentApplication: string;
          /** 奖惩申请列表 */
          rewardPunishmentApplications: string;
          /** 请选择类别 */
          pleaseSelectACategory: string;
          /** 请选择生效日期 */
          pleaseSelectEffectiveDate: string;
          /** 请输入原因 */
          pleaseEnterReason: string;
        };
        regularization: {
          /** 转正日期 */
          regularizationDate: string;
          /** 试用期评价 */
          probationEvaluation: string;
          /** 转正后类型 */
          typeAfterRegularization: string;
          /** 转正申请列表 */
          regularizationApplications: string;
          /** 新增转正申请 */
          newRegularizationApplication: string;
          /** 选择转正日期 */
          selectRegularizationDate: string;
          /** 请选择员工类型 */
          pleaseSelectEmployeeType: string;
          /** 请输入试用期评价 */
          pleaseEnterProbationEvaluation: string;
        };
        transfer: {
          /** 变更类型 */
          changeType: string;
          /** 原公司 */
          originalCompany: string;
          /** 原部门 */
          originalDepartment: string;
          /** 原职位 */
          originalPosition: string;
          /** 新部门 */
          newDepartment: string;
          /** 新职位 */
          newPosition: string;
          /** 新公司 */
          newCompany: string;
          /** 选择员工后自动显示 */
          autoDisplayedAfterSelectingAnEmployee: string;
          /** 请选择新部门 */
          pleaseSelectNewDepartment: string;
          /** 请选择新职位 */
          pleaseSelectNewPosition: string;
          /** 调动申请列表 */
          transferApplications: string;
          /** 新增调动申请 */
          newTransferApplication: string;
          /** 请选择变更类型 */
          pleaseSelectChangeType: string;
          /** 调动原因 */
          transferReason: string;
          /** 请输入调动原因 */
          pleaseEnterTransferReason: string;
        };
        resignation: {
          /** 最后工作日 */
          lastWorkingDay: string;
          /** 发起离职申请 */
          createResignationApplication: string;
          /** 离职原因 */
          resignationReason: string;
          /** 离职申请列表 */
          resignationApplications: string;
          /** 选择交接人 */
          selectHandoverPerson: string;
          /** 请选择离职类型 */
          pleaseSelectResignationType: string;
          /** 请选择最后工作日 */
          pleaseSelectLastWorkingDay: string;
          /** 请选择交接人 */
          pleaseSelectHandoverPerson: string;
          /** 请输入离职原因 */
          pleaseEnterResignationReason: string;
        };
        makeup: {
          /** 补卡原因 */
          makeupClockReason: string;
          /** 上班补卡 */
          clockInMakeup: string;
          /** 下班补卡 */
          clockOutMakeup: string;
          /** 发起补卡申请 */
          createMakeupClockApplication: string;
          /** 补卡类型 */
          makeupClockType: string;
          /** 补卡时间 */
          makeupClockTime: string;
          /** 补卡申请列表 */
          makeupClockApplications: string;
          /** 请选择补卡类型 */
          pleaseSelectMakeupClockType: string;
          /** 请选择补卡时间 */
          pleaseSelectMakeupClockTime: string;
          /** 请输入补卡原因 */
          pleaseEnterMakeupClockReason: string;
        };
        exchange: {
          /** 发起换休申请 */
          createExchangeLeaveApplication: string;
          /** 原工作日 */
          originalWorkday: string;
          /** 换休日 */
          exchangeDate: string;
          /** 换休原因 */
          exchangeReason: string;
          /** 换休申请列表 */
          exchangeLeaveApplications: string;
          /** 选择原工作日 */
          selectOriginalWorkday: string;
          /** 选择换休日 */
          selectExchangeDate: string;
          /** 请输入换休原因 */
          pleaseEnterExchangeReason: string;
        };
        leave: {
          /** 发起请假申请 */
          createLeaveApplication: string;
          /** 请假原因 */
          leaveReason: string;
          /** 请假时间无效，请检查员工排班 */
          invalidLeaveTimePleaseCheckTheEmployeeSchedule: string;
          /** 请假申请列表 */
          leaveApplications: string;
          /** 提示：请假小时为0，可能是员工在该时间段没有排班 */
          noteLeaveHoursAre0TheEmployeeMayHaveNoScheduleInThisPeriod: string;
          /** 请选择请假类型 */
          pleaseSelectLeaveType: string;
          /** 请假时间 */
          leaveTime: string;
          /** 请假小时 */
          leaveHours: string;
          /** 请输入请假原因 */
          pleaseEnterLeaveReason: string;
        };
        overtime: {
          /** 发起加班申请 */
          createOvertimeApplication: string;
          /** 加班原因 */
          overtimeReason: string;
          /** 加班时间无效，请检查时间范围 */
          invalidOvertimeTimePleaseCheckTheTimeRange: string;
          /** 成功提交 {count} 条加班申请 */
          successfullySubmittedOvertimeApplications: string;
          /** 加班申请列表 */
          overtimeApplications: string;
          /** 提示：加班小时为0，可能是加班时间在正常班次时段内 */
          noteOvertimeHoursAre0TheTimeMayFallWithinRegularShiftHours: string;
          /** 时长(小时) */
          durationHours: string;
          /** 加班时间 */
          overtimeTime: string;
          /** 加班小时 */
          overtimeHours: string;
          /** 请输入加班原因 */
          pleaseEnterOvertimeReason: string;
        };
        detailDrawer: {
          /** 申请详情 */
          applicationDetails: string;
          /** 暂无审批进度记录 */
          noApprovalProgressRecordsYet: string;
        };
      };
      home: {
        educationChart: {
          /** 未填写 */
          notFilled: string;
          /** {b}: {c} 人 ({d}%) */
          people: string;
          /** 学历分布 */
          educationDistribution: string;
          /** 员工学历分布 */
          employeeEducationDistribution: string;
        };
        common: {
          /** 在职员工 */
          activeEmployees: string;
          /** 查看全部 */
          viewAll: string;
        };
        cardData: {
          /** 今日异常 */
          todayAbnormal: string;
          /** 今日打卡 */
          todayClockIns: string;
        };
        lineChart: {
          /** 正常出勤 */
          normalAttendance: string;
          /** 考勤异常 */
          attendanceAbnormal: string;
          /** 柱状：正常出勤 · 折线：考勤异常 */
          barNormalAttendanceLineAttendanceAbnormal: string;
          /** 近 */
          last: string;
          /** 天考勤概览 */
          dayAttendanceOverview: string;
        };
        creativityBanner: {
          /** 员工管理 */
          employeeManagement: string;
          /** 入转调离与档案 */
          employeeLifecycleRecords: string;
          /** 公司/部门结构 */
          companyDepartmentStructure: string;
          /** 快速处理申请 */
          processApplicationsQuickly: string;
          /** 查询与补录 */
          querySupplementaryEntry: string;
          /** 异常处理与锁定 */
          exceptionHandlingLocking: string;
          /** 报表中心 */
          reportCenter: string;
          /** 考勤与员工分析 */
          attendanceEmployeeAnalytics: string;
          /** 常用入口 */
          commonShortcuts: string;
          /** 一键直达高频功能 */
          oneClickAccessToHighFrequencyFeatures: string;
        };
        headerBanner: {
          /** 早安 */
          goodMorning: string;
          /** 上午好 */
          goodMorning2: string;
          /** 中午好 */
          goodAfternoon: string;
          /** 下午好 */
          goodAfternoon2: string;
          /** 晚上好 */
          goodEvening: string;
          /** YYYY年M月D日 dddd */
          ddddMmmDYyyy: string;
          /** · 欢迎回到人资管理工作台 */
          welcomeBackToTheHrManagementWorkspace: string;
        };
        pieChart: {
          /** 员工年龄分布 */
          employeeAgeDistribution: string;
        };
        projectNews: {
          /** 暂无待审批事项，一切顺利！ */
          noPendingApprovalsAllGood: string;
        };
        reminderCard: {
          /** 合同到期 ({count}) */
          contractExpirations: string;
          /** 试用期到期 ({count}) */
          probationExpirations: string;
          /** 证书到期 ({count}) */
          certificateExpirations: string;
          /** 已过期 {days} 天 */
          expiredDaysAgo: string;
          /** 今天到期 */
          expiresToday: string;
          /** {days} 天后到期 */
          expiresInDays: string;
          /** 到期提醒（30 天） */
          expirationReminders30Days: string;
          /** 近期无到期事项 */
          noUpcomingExpirations: string;
          /** 到期提醒 */
          expirationReminders: string;
          /** 到期日期 */
          expirationDate: string;
          /** 剩余时间 */
          timeRemaining: string;
          /** 的 */
          s: string;
          /** 共 */
          total: string;
          /** 条，点击查看全部 */
          itemsClickToViewAll: string;
        };
      };
      sys: {
        common: {
          /** 菜单名称 */
          menuName: string;
          /** 请输入菜单名称 */
          pleaseEnterMenuName: string;
          /** 菜单状态 */
          menuStatus: string;
          /** 菜单编码 */
          menuCode: string;
          /** 目录 */
          directory: string;
          /** 菜单 */
          menu: string;
          /** 菜单类型 */
          menuType: string;
          /** 已禁用 */
          disabled: string;
          /** 角色列表 */
          roleList: string;
        };
        menu: {
          /** 请输入菜单编码 */
          pleaseEnterMenuCode: string;
          /** 权限标识 */
          permission: string;
          /** 组件路径 */
          componentPath: string;
          /** 请输入显示排序 */
          pleaseEnterDisplayOrder: string;
          /** 添加菜单 */
          addMenu: string;
          /** 修改菜单 */
          modifyMenu: string;
          /** 可见 */
          visible: string;
          /** 确认要删除该菜单吗？ */
          areYouSureYouWantToDeleteThisMenu: string;
          /** 上级菜单 */
          parentMenu: string;
          /** 选择上级菜单 */
          selectParentMenu: string;
          /** 菜单图标 */
          menuIcon: string;
          /** 点击选择图标 */
          clickToSelectAnIcon: string;
          /** 输入图标名称 */
          enterIconName: string;
          /** 显示排序 */
          displayOrder: string;
          /** 请输入菜单名称（中文） */
          pleaseEnterMenuNameChinese: string;
          /** 请输入菜单名称（英文） */
          pleaseEnterMenuNameEnglish: string;
          /** 路由地址 */
          routeAddress: string;
          /** 请输入路由地址 */
          pleaseEnterRouteAddress: string;
          /** 请输入组件路径 */
          pleaseEnterComponentPath: string;
          /** 请输入权限标识 */
          pleaseEnterPermission: string;
          /** 显示状态 */
          visibleStatus: string;
          /** 主类目 */
          rootCategory: string;
        };
        feedback: {
          /** 待处理 */
          pending: string;
          /** 处理中 */
          processing: string;
          /** 已处理 */
          processed: string;
          /** 功能建议 */
          featureSuggestion: string;
          /** 问题反馈 */
          issueFeedback: string;
          /** 反馈内容 */
          feedbackContent: string;
          /** 提交时间 */
          submitTime: string;
          /** 处理成功 */
          processedSuccessfully: string;
          /** 状态已更新 */
          statusUpdated: string;
          /** 确定删除这条反馈吗？ */
          areYouSureYouWantToDeleteThisFeedback: string;
          /** 意见反馈 */
          feedback: string;
          /** 处理回复 */
          handleReply: string;
          /** 保存回复并标记已处理 */
          saveReplyAndMarkAsProcessed: string;
          /** 反馈内容/联系人/电话 */
          feedbackContentContactPhone: string;
          /** 反馈详情 */
          feedbackDetails: string;
          /** 填写处理结果或回复内容 */
          enterHandlingResultOrReplyContent: string;
        };
        notice: {
          /** 公告 */
          notice: string;
          /** 通知 */
          notification: string;
          /** 公告标题 */
          noticeTitle: string;
          /** 公告类型 */
          noticeType: string;
          /** 请选择发布日期 */
          pleaseSelectPublishDate: string;
          /** 请输入公告标题 */
          pleaseEnterNoticeTitle: string;
          /** 发布日期 */
          publishDate: string;
          /** 请填写公告标题 */
          pleaseEnterNoticeTitle2: string;
          /** 确定删除该公告吗？ */
          areYouSureYouWantToDeleteThisNotice: string;
          /** 公告列表 */
          noticeList: string;
          /** 公告内容 */
          noticeContent: string;
          /** 请输入公告内容 */
          pleaseEnterNoticeContent: string;
          /** 新增公告 */
          newNotice: string;
          /** 编辑公告 */
          editNotice: string;
        };
        operLog: {
          /** 模块 */
          module: string;
          /** 操作人 */
          operator: string;
          /** 结果 */
          result: string;
          /** 清理日志 */
          cleanLogs: string;
          /** 请求路径 */
          requestPath: string;
          /** 耗时 */
          duration: string;
          /** 将删除该天数之前的所有操作日志 */
          allOperationLogsBeforeThisNumberOfDaysWillBeDeleted: string;
          /** 请输入数字 */
          pleaseEnterANumber: string;
          /** 保留天数不能小于7天 */
          retentionDaysCannotBeLessThan7: string;
          /** 清理成功 */
          cleanedSuccessfully: string;
          /** 操作日志列表 */
          operationLogList: string;
          /** 请输入模块 */
          pleaseEnterModule: string;
          /** 请输入操作人 */
          pleaseEnterOperator: string;
          /** 操作时间 */
          operationTime: string;
          /** 日志详情 */
          logDetails: string;
          /** 请求参数 */
          requestParameters: string;
          /** 错误信息 */
          errorMessage: string;
        };
        role: {
          /** 请输入角色编码 */
          pleaseEnterRoleCode: string;
          /** 请输入角色名称 */
          pleaseEnterRoleName: string;
          /** 角色名称 */
          roleName: string;
          /** 角色编码 */
          roleCode: string;
          /** 全部数据 */
          allData: string;
          /** 状态修改成功 */
          statusUpdatedSuccessfully: string;
          /** 数据权限 */
          dataScope: string;
          /** [目录] */
          directory: string;
          /** [菜单] */
          menu: string;
          /** [按钮] */
          button: string;
          /** 本公司数据 */
          currentCompanyData: string;
          /** 本部门数据 */
          currentDepartmentData: string;
          /** 本部门及以下数据 */
          currentDepartmentAndBelow: string;
          /** 仅本人数据 */
          selfOnly: string;
          /** 自定义数据 */
          customData: string;
          /** 新增角色 */
          addRole: string;
          /** 编辑角色 */
          editRole: string;
          /** 确认删除该角色吗？ */
          areYouSureYouWantToDeleteThisRole: string;
          /** 确认{action}该角色吗？ */
          areYouSureYouWantToThisRole: string;
          /** 父子联动 */
          cascadeParentChild: string;
          /** 请选择数据权限 */
          pleaseSelectDataScope: string;
          /** 菜单权限 */
          menuPermission: string;
        };
        user: {
          /** 请输入用户名 */
          pleaseEnterUserName: string;
          /** 请输入密码 */
          pleaseEnterPassword: string;
          /** 重置密码 */
          resetPassword: string;
          /** 新增用户 */
          addUser: string;
          /** 编辑用户 */
          editUser: string;
          /** 确认删除该用户吗？ */
          areYouSureYouWantToDeleteThisUser: string;
          /** 确认{action}该用户吗？ */
          areYouSureYouWantToThisUser: string;
          /** 密码须为 6-20 位，且同时包含字母和数字 */
          passwordMustBe620CharactersAndContainBothLettersAndDigits: string;
          /** 密码重置成功 */
          passwordResetSuccessfully: string;
          /** 用户列表 */
          userList: string;
        };
        dict: {
          /** 字典名称 */
          dictionaryName: string;
          /** 字典编码 */
          dictionaryCode: string;
          /** 值 */
          value: string;
          /** 英文名称 */
          englishName: string;
          /** 确定删除该字典类型吗？删除后相关字典数据也会被删除 */
          areYouSureYouWantToDeleteThisDictionaryTypeRelatedDictionaryDataWillAlsoBeDeleted: string;
          /** 请先选择字典类型 */
          pleaseSelectADictionaryTypeFirst: string;
          /** 确定删除该字典数据吗？ */
          areYouSureYouWantToDeleteThisDictionaryData: string;
          /** 字典类型 */
          dictionaryType: string;
          /** 请选择左侧字典类型 */
          pleaseSelectADictionaryTypeOnTheLeft: string;
          /** 标签 */
          label: string;
          /** 请输入字典名称（中文） */
          pleaseEnterDictionaryNameChinese: string;
          /** 请输入字典名称（英文） */
          pleaseEnterDictionaryNameEnglish: string;
          /** 请输入字典编码 */
          pleaseEnterDictionaryCode: string;
          /** 标签（中文） */
          labelChinese: string;
          /** 请输入标签（中文） */
          pleaseEnterLabelChinese: string;
          /** 标签（英文） */
          labelEnglish: string;
          /** 请输入标签（英文） */
          pleaseEnterLabelEnglish: string;
          /** 请输入值 */
          pleaseEnterValue: string;
          /** 新增字典类型 */
          newDictionaryType: string;
          /** 编辑字典类型 */
          editDictionaryType: string;
          /** 新增字典数据 */
          newDictionaryData: string;
          /** 编辑字典数据 */
          editDictionaryData: string;
        };
        fileConfig: {
          /** 配置名称 */
          configName: string;
          /** 配置键 */
          configKey: string;
          /** 路径不能为空 */
          pathCannotBeEmpty: string;
          /** 刷新后将重新加载所有路径配置的缓存，上传将立即使用新路径。静态资源映射需要重启后端服务才能生效。 */
          afterRefreshAllPathConfigCachesWillBeReloadedAndUploadsWillUseTheNewPathImmediatelyStaticResourceMappingTakesEffectAfterRestartingTheBackendService: string;
          /** 刷新确认 */
          refreshConfirmation: string;
          /** 确认刷新 */
          confirmRefresh: string;
          /** 缓存刷新成功 */
          cacheRefreshedSuccessfully: string;
          /** 文件路径配置 */
          filePathConfig: string;
          /** 刷新缓存 */
          refreshCache: string;
          /** 支持绝对路径（如 D:/rzphoto/employee_photo）或相对路径（如 ./uploads） */
          absolutePathsEGDRzphotoEmployeePhotoOrRelativePathsEGUploadsAreSupported: string;
          /** 修改路径配置后，新上传的文件将保存到新路径。如果修改了静态资源映射相关路径，需要重启后端服务才能使已有文件在新路径下正常访问。 */
          afterChangingThePathConfigNewFilesAreSavedToTheNewPathIfStaticResourceMappingRelatedPathsChangedTheBackendServiceMustBeRestartedForExistingFilesToBeAccessibleUnderTheNewPath: string;
          /** 编辑路径配置 */
          editPathConfig: string;
          /** 请输入配置名称 */
          pleaseEnterConfigName: string;
          /** 请输入文件存储路径 */
          pleaseEnterFileStoragePath: string;
        };
        mobileMenu: {
          /** 快捷功能 */
          quickFeatures: string;
          /** 申请中心 */
          applicationCenter: string;
          /** 路由路径 */
          routePath: string;
          /** 请输入路由路径 */
          pleaseEnterRoutePath: string;
          /** 请选择菜单分组 */
          pleaseSelectAMenuGroup: string;
          /** 新增移动端菜单 */
          newMobileMenu: string;
          /** 编辑移动端菜单 */
          editMobileMenu: string;
          /** 确认删除该菜单吗？ */
          areYouSureYouWantToDeleteThisMenu: string;
          /** 请选择图标 */
          pleaseSelectAnIcon: string;
          /** 图标背景色 */
          iconBackgroundColor: string;
          /** 如：/pages/apply/leave/index */
          eGPagesApplyLeaveIndex: string;
          /** 菜单分组 */
          menuGroup: string;
        };
      };
      attendance: {
        monthly: {
          /** 总工时 */
          totalWorkHours: string;
          /** 应出勤 */
          requiredAttendance: string;
          /** 实出勤 */
          actualAttendance: string;
          /** 迟到次数 */
          lateCount: string;
          /** 早退次数 */
          earlyLeaveCount: string;
          /** 本月考勤数据量过大，页面可能卡顿，请按公司/部门缩小统计范围 */
          thisMonthHasTooMuchAttendanceDataAndThePageMayLagNarrowTheRangeByCompanyDepartment: string;
          /** 月考勤汇总_{month}.xlsx */
          monthlyAttendanceSummaryXlsx: string;
          /** 月度对账_{month}.xlsx */
          monthlyReconciliationXlsx: string;
          /** 对账导出 */
          exportReconciliation: string;
          /** 选择月份 */
          selectMonth: string;
          /** 迟到(分) */
          lateMin: string;
          /** 早退(分) */
          earlyLeaveMin: string;
          /** 月考勤汇总 - */
          monthlyAttendanceSummary: string;
        };
        clock: {
          /** 开始 */
          start: string;
          /** 结束 */
          end: string;
          /** 打卡时间 */
          clockTime: string;
          /** 考勤机 */
          attendanceMachine: string;
          /** 手动补卡 */
          manualMakeupClock: string;
          /** 请选择打卡时间 */
          pleaseSelectClockTime: string;
          /** 确定删除该打卡记录吗？ */
          areYouSureYouWantToDeleteThisClockRecord: string;
          /** 上班打卡 */
          clockIn: string;
          /** 下班打卡 */
          clockOut: string;
          /** 方式 */
          method: string;
          /** 地点 */
          location: string;
          /** 输入姓名或工号搜索员工 */
          searchEmployeesByNameOrEmployeeNo: string;
        };
        common: {
          /** 导出成功 */
          exportSuccessful: string;
          /** 请选择日期范围 */
          pleaseSelectDateRange: string;
        };
        holiday: {
          /** 规则类型 */
          ruleType: string;
          /** 添加规则 */
          addRule: string;
          /** 单休（周日） */
          singleRestDaySunday: string;
          /** 双休 */
          twoDayWeekend: string;
          /** 单休（周六） */
          singleRestDaySaturday: string;
          /** 法定假日 */
          statutoryHoliday: string;
          /** 调休上班 */
          adjustedWorkday: string;
          /** 请选择规则类型 */
          pleaseSelectRuleType: string;
          /** 确定删除该规则吗？ */
          areYouSureYouWantToDeleteThisRule: string;
          /** 考勤日历规则 */
          attendanceCalendarRules: string;
          /** 清除筛选 */
          clearFilters: string;
          /** 说明：休息规则（单休/双休）每个公司只能设置一条；法定假日和调休上班可设置多条。 */
          noteEachCompanyCanHaveOnlyOneRestRuleSingleTwoDayWeekendStatutoryHolidaysAndAdjustedWorkdaysCanHaveMultipleEntries: string;
          /** `{year}年` */
          year: string;
          /** 如：国庆节 */
          eGNationalDay: string;
          /** 编辑规则 */
          editRule: string;
        };
        shift: {
          /** 工作时段 */
          workPeriod: string;
          /** 新增班次 */
          newShift: string;
          /** (跨天) */
          crossDay: string;
          /** 班次编码 */
          shiftCode: string;
          /** 班次名称 */
          shiftName: string;
          /** 上午 */
          am: string;
          /** 下午 */
          pm: string;
          /** 请填写班次编码和名称 */
          pleaseEnterShiftCodeAndName: string;
          /** 请至少添加一个时段 */
          pleaseAddAtLeastOnePeriod: string;
          /** 请填写完整的时段时间 */
          pleaseFillInCompletePeriodTimes: string;
          /** 班次管理 */
          shiftManagement: string;
          /** 跨天 */
          crossDay2: string;
          /** 上班卡 */
          clockIn: string;
          /** 下班卡 */
          clockOut: string;
          /** 正班时间 */
          regularShiftTime: string;
          /** 确定删除该班次吗？ */
          areYouSureYouWantToDeleteThisShift: string;
          /** 如：DAY01 */
          eGDay01: string;
          /** 如：白班 */
          eGDayShift: string;
          /** 正班时长 */
          regularShiftHours: string;
          /** 编辑班次 */
          editShift: string;
          /** 时段{} */
          period: string;
        };
        daily: {
          /** 迟到+早退 */
          lateEarlyLeave: string;
          /** 日 */
          sun: string;
          /** 一 */
          mon: string;
          /** 二 */
          tue: string;
          /** 三 */
          wed: string;
          /** 四 */
          thu: string;
          /** 五 */
          fri: string;
          /** 六 */
          sat: string;
          /** 指定部门 */
          specifiedDepartment: string;
          /** 锁定 */
          lock: string;
          /** 未处理 */
          unprocessed: string;
          /** 周{day} */
          wk: string;
          /** 计算选中 {count} 人 */
          selected: string;
          /** 工号:{no} */
          no: string;
          /** 姓名:{name} */
          name: string;
          /** 按条件计算 */
          calculateByFilters: string;
          /** 计算全部员工 */
          calculateAllEmployees: string;
          /** 将计算选中的 {count} 名员工的考勤 */
          attendanceWillBeCalculatedForSelectedEmployees: string;
          /** 公司: {name} */
          company: string;
          /** 工号包含: {no} */
          employeeNoContains: string;
          /** 姓名包含: {name} */
          nameContains: string;
          /** 将按以下条件筛选员工计算:\n{filters} */
          employeesWillBeFilteredBy: string;
          /** 将计算所有在职员工的考勤 */
          attendanceWillBeCalculatedForAllActiveEmployees: string;
          /** {tip}，确认执行考勤计算吗？ */
          confirmToRunAttendanceCalculation: string;
          /** 考勤计算确认 */
          attendanceCalculationConfirmation: string;
          /** 确认计算 */
          confirmCalculation: string;
          /** 考勤计算完成 */
          attendanceCalculationCompleted: string;
          /** 请先选择要操作的记录 */
          pleaseSelectRecordsToOperateFirst: string;
          /** 没有可操作的记录 */
          noRecordsAvailable: string;
          /** 确认{action}选中的 {count} 条考勤记录吗？ */
          areYouSureYouWantToTheSelectedAttendanceRecords: string;
          /** 锁定确认 */
          lockConfirmation: string;
          /** 解锁确认 */
          unlockConfirmation: string;
          /** 锁定成功 */
          lockedSuccessfully: string;
          /** 解锁成功 */
          unlockedSuccessfully: string;
          /** 日考勤记录_{start}_{end}.xlsx */
          dailyAttendanceXlsx: string;
          /** 日考勤记录 */
          dailyAttendanceRecords: string;
          /** 解锁 */
          unlock: string;
          /** 提示: 勾选员工计算选中，或按搜索条件筛选计算 */
          tipCheckEmployeesToCalculateTheSelectionOrCalculateBySearchFilters: string;
          /** 星期 */
          weekday: string;
          /** 各时段打卡 */
          clockInsByPeriod: string;
          /** 分 */
          min: string;
          /** 年假: */
          annualLeave: string;
          /** 事假: */
          personalLeave: string;
          /** 病假: */
          sickLeave: string;
          /** 婚假: */
          marriageLeave: string;
          /** 产假: */
          maternityLeave: string;
          /** 陪产假: */
          paternityLeave: string;
          /** 丧假: */
          bereavementLeave: string;
        };
        location: {
          /** 地点名称 */
          locationName: string;
          /** 打卡半径 */
          clockRadius: string;
          /** 编辑打卡地点 */
          editClockLocation: string;
          /** 新增打卡地点 */
          newClockLocation: string;
          /** 确定删除打卡地点“{name}”吗？关联员工也会同步解除。 */
          areYouSureYouWantToDeleteClockLocationAssignedEmployeesWillBeUnassigned: string;
          /** 请输入地点名称 */
          pleaseEnterALocationName: string;
          /** 请先选择或填写打卡地址 */
          pleaseSelectOrEnterAClockAddress: string;
          /** 请先选择地图点位 */
          pleaseSelectAPointOnTheMap: string;
          /** 请输入有效打卡半径 */
          pleaseEnterAValidClockRadius: string;
          /** 请先选择左侧打卡地点 */
          pleaseSelectAClockLocationOnTheLeft: string;
          /** 员工关联已保存 */
          employeeAssignmentsSaved: string;
          /** 确定从当前打卡地点移除 {count} 名员工吗？{names} */
          areYouSureYouWantToRemoveEmployeesFromTheCurrentClockLocation: string;
          /** 已移除关联 */
          assignmentsRemoved: string;
          /** 请先勾选要移除的员工 */
          pleaseSelectEmployeesToRemove: string;
          /** 打卡地点设置 */
          clockLocationSettings: string;
          /** 左侧维护打卡地点，右侧维护当前地点关联的员工。 */
          manageClockLocationsOnTheLeftAndAssignedEmployeesOfTheCurrentLocationOnTheRight: string;
          /** 新增地点 */
          newLocation: string;
          /** 批量移除 */
          batchRemove: string;
          /** 地图选择位置 */
          pickLocationOnMap: string;
          /** 选择后会自动回填地址和 GCJ-02 坐标 */
          theAddressAndGcj02CoordinatesWillBeFilledAutomaticallyAfterSelection: string;
          /** 保存关联 */
          saveAssignments: string;
          /** 搜索地点名称或地址 */
          searchLocationNameOrAddress: string;
          /** 半径 */
          radius: string;
          /** 暂无关联员工 */
          noAssignedEmployees: string;
          /** 例如：华水工业园 1栋5楼 */
          eGHuashuiIndustrialParkBuilding1Floor5: string;
          /** 地图选点 */
          mapPicker: string;
          /** 用于移动端展示，可手动补充楼栋楼层 */
          shownOnMobileBuildingAndFloorCanBeAddedManually: string;
          /** 保存后，所选员工会绑定到当前打卡地点；员工可以同时绑定多个打卡地点。 */
          afterSavingSelectedEmployeesWillBeBoundToTheCurrentClockLocationAnEmployeeCanBindToMultipleLocations: string;
          /** 已选择 */
          selected: string;
        };
        schedule: {
          /** 请选择班次 */
          pleaseSelectAShift: string;
          /** 批量排班 */
          batchSchedule: string;
          /** {year}年{month}月 */
          yearMonth: string;
          /** 请先选择一个班次 */
          pleaseSelectAShiftFirst: string;
          /** 批量排班成功 */
          batchSchedulingSuccessful: string;
          /** 班次: */
          shift: string;
          /** 暂无排班数据 */
          noScheduleData: string;
          /** 选择班次 */
          selectShift: string;
          /** 提示： */
          note: string;
          /** 点击单元格清除排班 */
          clickACellToClearItsSchedule: string;
          /** 点击单元格应用选中班次 */
          clickACellToApplyTheSelectedShift: string;
          /** 请先选择上方班次后才能点击单元格排班 */
          selectAShiftAboveBeforeSchedulingCells: string;
        };
      };
      hr: {
        employee: {
          /** 员工类别 */
          employeeCategory: string;
          /** 请先填写员工工号 */
          pleaseEnterEmployeeNoFirst: string;
          /** 人员编号 */
          personnelNo: string;
          /** 出生日期 */
          dateOfBirth: string;
          /** 电话 */
          telephone: string;
          /** 关系 */
          relationship: string;
          /** 请输入正确的手机号 */
          pleaseEnterAValidPhoneNumber: string;
          /** 请输入正确的邮箱地址 */
          pleaseEnterAValidEmailAddress: string;
          /** 该工号已存在 */
          thisEmployeeNoAlreadyExists: string;
          /** 导入员工 */
          importEmployees: string;
          /** 新增员工 */
          addEmployee: string;
          /** 身份证正面 */
          idCardFront: string;
          /** 身份证反面 */
          idCardBack: string;
          /** 请输入人员编号 */
          pleaseEnterPersonnelNo: string;
          /** 职级 */
          jobGrade: string;
          /** 户籍地址 */
          registeredAddress: string;
          /** 现居住地 */
          currentResidence: string;
          /** 请选择学历 */
          pleaseSelectEducation: string;
          /** 请输入电话 */
          pleaseEnterPhoneNumber: string;
          /** 请选择关系 */
          pleaseSelectRelationship: string;
          /** 请输入联系电话 */
          pleaseEnterContactPhone: string;
          /** 请输入正确的身份证号 */
          pleaseEnterAValidIdNumber: string;
          /** 请输入正确的紧急联系电话 */
          pleaseEnterAValidEmergencyContactPhone: string;
          /** 头像上传成功 */
          avatarUploadedSuccessfully: string;
          /** 身份证正面上传成功 */
          idCardFrontUploadedSuccessfully: string;
          /** 身份证反面上传成功 */
          idCardBackUploadedSuccessfully: string;
          /** 只支持上传 JPG/PNG/GIF/WEBP 格式的图片 */
          onlyJpgPngGifWebpImagesAreSupported: string;
          /** 图片大小不能超过 5MB */
          imageSizeCannotExceed5mb: string;
          /** 员工导入模板.xlsx */
          employeeImportTemplateXlsx: string;
          /** 请选择 .xlsx 文件 */
          pleaseSelectAnXlsxFile: string;
          /** 文件读取失败，请重新选择 */
          failedToReadTheFilePleaseSelectAgain: string;
          /** 成功导入 {count} 名员工 */
          successfullyImportedEmployees: string;
          /** 员工列表_{date}.xlsx */
          employeeListXlsx: string;
          /** 请填写必填项（人员编号、姓名、所属组织） */
          pleaseFillInRequiredFieldsPersonnelNoNameOrganization: string;
          /** 工号已存在，请修改 */
          employeeNoAlreadyExistsPleaseModifyIt: string;
          /** 请检查基本信息中的手机号/邮箱/身份证号等格式 */
          pleaseCheckTheFormatsOfPhoneEmailIdNoInBasicInfo: string;
          /** 人员基本信息 */
          personnelBasicInfo: string;
          /** 头像 */
          avatar: string;
          /** 工作信息 */
          workInfo: string;
          /** 联系信息 */
          contactInfo: string;
          /** 添加教育经历 */
          addEducation: string;
          /** 添加家庭成员 */
          addFamilyMember: string;
          /** 添加工作经历 */
          addWorkExperience: string;
          /** 添加证书 */
          addCertificate: string;
          /** 下载导入模板 */
          downloadImportTemplate: string;
          /** 将 .xlsx 文件拖到此处，或点击选择 */
          dragTheXlsxFileHereOrClickToSelect: string;
          /** 开始导入 */
          startImport: string;
          /** 紧急电话 */
          emergencyPhone: string;
          /** 确定删除该员工吗？ */
          areYouSureYouWantToDeleteThisEmployee: string;
          /** 基本信息 */
          basicInfo: string;
          /** 小程序密码 */
          miniProgramPassword: string;
          /** 请选择性别 */
          pleaseSelectGender: string;
          /** 请输入身份证号 */
          pleaseEnterIdNumber: string;
          /** 请选择民族 */
          pleaseSelectEthnicity: string;
          /** 最高学历 */
          highestEducation: string;
          /** 请选择员工类别 */
          pleaseSelectEmployeeCategory: string;
          /** 请选择婚姻状况 */
          pleaseSelectMaritalStatus: string;
          /** 请选择政治面貌 */
          pleaseSelectPoliticalStatus: string;
          /** 所属组织 */
          organization: string;
          /** 请选择职务 */
          pleaseSelectJobTitle: string;
          /** 请选择职位 */
          pleaseSelectPosition: string;
          /** 请选择职级 */
          pleaseSelectJobGrade: string;
          /** 请输入籍贯 */
          pleaseEnterNativePlace: string;
          /** 请输入户籍地址 */
          pleaseEnterRegisteredAddress: string;
          /** 请输入现居住地 */
          pleaseEnterCurrentResidence: string;
          /** 请输入紧急联系人 */
          pleaseEnterEmergencyContact: string;
          /** 教育经历 */
          education: string;
          /** 学校 */
          school: string;
          /** 请输入学校名称 */
          pleaseEnterSchoolName: string;
          /** 专业 */
          major: string;
          /** 请输入专业 */
          pleaseEnterMajor: string;
          /** 是否全日制 */
          fullTime: string;
          /** 开学时间 */
          enrollmentDate: string;
          /** 毕业时间 */
          graduationDate: string;
          /** 毕业证 */
          diploma: string;
          /** 家庭成员 */
          familyMember: string;
          /** 工作单位 */
          workUnit: string;
          /** 请输入工作单位 */
          pleaseEnterWorkUnit: string;
          /** 工作经历 */
          workExperience: string;
          /** 公司名称 */
          companyName: string;
          /** 请输入公司名称 */
          pleaseEnterCompanyName: string;
          /** 所在部门 */
          department: string;
          /** 请输入所在部门 */
          pleaseEnterDepartment: string;
          /** 请输入职位 */
          pleaseEnterPosition: string;
          /** 证明人 */
          referencePerson: string;
          /** 请输入证明人 */
          pleaseEnterReferencePerson: string;
          /** 证明电话 */
          referencePhone: string;
          /** 请输入证明电话 */
          pleaseEnterReferencePhone: string;
          /** 证书 */
          certificate: string;
          /** 证书名称 */
          certificateName: string;
          /** 请输入证书名称 */
          pleaseEnterCertificateName: string;
          /** 证书类型 */
          certificateType: string;
          /** 请选择证书类型 */
          pleaseSelectCertificateType: string;
          /** 证书等级 */
          certificateLevel: string;
          /** 请选择证书等级 */
          pleaseSelectCertificateLevel: string;
          /** 颁发日期 */
          issueDate: string;
          /** 过期日期 */
          expireDate: string;
          /** 证书照片 */
          certificatePhoto: string;
          /** 其他信息 */
          otherInfo: string;
          /** `请输入{field}` */
          pleaseEnter: string;
          /** 编辑员工 */
          editEmployee: string;
          /** 不修改请留空 */
          leaveBlankToKeepUnchanged: string;
          /** 不填则使用随机初始密码 */
          leaveBlankToUseARandomInitialPassword: string;
          /** 只能选择一个文件 */
          onlyOneFileCanBeSelected: string;
          /** 条 */
          items: string;
        };
        leaveQuota: {
          /** 假期额度 */
          title: string;
          /** 年度 */
          year: string;
          /** 新增额度 */
          addQuota: string;
          /** 编辑额度 */
          editQuota: string;
          /** 请选择年度 */
          pleaseSelectYear: string;
          /** 请选择员工 */
          pleaseSelectEmployee: string;
          /** 请输入额度工时 */
          pleaseInputTotalHours: string;
          /** 额度工时 */
          totalHours: string;
          /** 已用工时 */
          usedHours: string;
          /** 剩余工时 */
          remainHours: string;
          /** 删除后该额度不再参与扣减，确定删除？ */
          confirmDelete: string;
          /** 年假剩余 {hours} 小时 */
          remainingQuotaHours: string;
        };
        contract: {
          /** 上传成功 */
          uploadedSuccessfully: string;
          /** 合同编号 */
          contractNo: string;
          /** 合同类型 */
          contractType: string;
          /** 生效中 */
          inEffect: string;
          /** 即将到期 */
          expiringSoon: string;
          /** 已到期 */
          expired: string;
          /** 已终止 */
          terminated: string;
          /** 新增合同 */
          newContract: string;
          /** 请输入合同编号 */
          pleaseEnterContractNo: string;
          /** 请选择合同类型 */
          pleaseSelectContractType: string;
          /** 签订日期 */
          signDate: string;
          /** 请输入备注 */
          pleaseEnterRemark: string;
          /** 合同列表_{date}.xlsx */
          contractListXlsx: string;
          /** 请选择开始日期 */
          pleaseSelectStartDate: string;
          /** 请选择合同期限 */
          pleaseSelectContractTerm: string;
          /** 只能上传图片文件! */
          onlyImageFilesCanBeUploaded: string;
          /** 图片大小不能超过 5MB! */
          imageSizeCannotExceed5mb: string;
          /** 请先选择员工 */
          pleaseSelectAnEmployeeFirst: string;
          /** 合同列表 */
          contractList: string;
          /** 支持上传最多9张图片，每张不超过5MB */
          upTo9ImagesCanBeUploadedEachWithin5mb: string;
          /** 确定删除该合同吗？ */
          areYouSureYouWantToDeleteThisContract: string;
          /** 续签 */
          renew: string;
          /** 合同续签 */
          renewTitle: string;
          /** 原合同 */
          renewOriginal: string;
          /** 新合同期限 */
          renewTerm: string;
          /** 新合同薪资 */
          renewSalary: string;
          /** 合同次数 */
          contractCount: string;
          /** `第{count}次` */
          no: string;
          /** 合同期限 */
          contractTerm: string;
          /** 选择开始日期 */
          selectStartDate: string;
          /** 合同图片 */
          contractImage: string;
          /** 查看合同 */
          viewContract: string;
          /** 编辑合同 */
          editContract: string;
        };
      };
      org: {
        structure: {
          /** 占比 */
          percentage: string;
          /** 集团 */
          group: string;
          /** 请输入组织名称 */
          pleaseEnterOrganizationName: string;
          /** 年龄段 */
          ageRange: string;
          /** 年龄分析 */
          ageAnalysis: string;
          /** 性别分析 */
          genderAnalysis: string;
          /** 学历分析 */
          educationAnalysis: string;
          /** 在职状态 */
          employmentStatus: string;
          /** 员工类型 */
          employeeType: string;
          /** 新增组织 */
          addOrganization: string;
          /** 编辑组织 */
          editOrganization: string;
          /** {name} - 人员结构分析 */
          workforceAnalysis: string;
          /** 全公司 - 人员结构分析 */
          wholeCompanyWorkforceAnalysis: string;
          /** 全公司 */
          wholeCompany: string;
          /** 当前节点下还有子节点，请先删除子节点 */
          thisNodeHasChildNodesDeleteTheChildNodesFirst: string;
          /** 确认删除“{name}”吗？ */
          areYouSureYouWantToDelete: string;
          /** 请输入组织编码 */
          pleaseEnterOrganizationCode: string;
          /** 添加公司 */
          addCompany: string;
          /** 添加部门 */
          addDepartment: string;
          /** 添加子节点 */
          addChildNode: string;
          /** 顶级节点 */
          topLevelNode: string;
          /** 新增顶级组织 */
          addTopOrganization: string;
          /** 查看全公司 */
          viewWholeCompany: string;
          /** 人 */
          people: string;
          /** 搜索组织名称或编码 */
          searchOrganizationNameOrCode: string;
          /** 男性员工 */
          maleEmployees: string;
          /** 女性员工 */
          femaleEmployees: string;
          /** 暂无统计数据 */
          noStatisticsData: string;
          /** 暂无图表数据 */
          noChartData: string;
          /** 上级组织 */
          parentOrganization: string;
          /** 组织类型 */
          organizationType: string;
          /** 组织编码 */
          organizationCode: string;
          /** 例如：GRP001 / COM001 / DEPT001 */
          eGGrp001Com001Dept001: string;
          /** 组织名称 */
          organizationName: string;
          /** 简称 */
          abbreviation: string;
          /** 请输入简称 */
          pleaseEnterAbbreviation: string;
          /** 请选择负责人 */
          pleaseSelectAManager: string;
          /** 办公地址 */
          officeAddress: string;
          /** 请输入办公地址 */
          pleaseEnterOfficeAddress: string;
          /** 打卡配置 */
          clockConfiguration: string;
          /** 公司创建完成后，请到“考勤管理 -> 打卡地点”维护打卡位置并分配员工。 */
          afterTheCompanyIsCreatedGoToAttendanceClockLocationsToMaintainClockLocationsAndAssignEmployees: string;
          /** 确认新增 */
          confirmAdd: string;
          /** 确认保存 */
          confirmSave: string;
          /** 打卡点已拆分到“考勤管理 -> 打卡地点”，组织架构这里只维护公司和部门基础信息。 */
          clockLocationsHaveMovedToAttendanceClockLocationsOnlyCompanyAndDepartmentBasicInfoIsMaintainedHere: string;
          /** 图表 */
          chart: string;
        };
        mapPicker: {
          /** 我的位置 */
          myLocation: string;
          /** 未获取到详细地址 */
          detailedAddressNotObtained: string;
          /** 已选位置 ({lat}, {lng}) */
          selectedLocation: string;
          /** 当前未配置 Google Maps API Key，请先补充 VITE_GOOGLE_MAPS_API_KEY。 */
          googleMapsApiKeyIsNotConfiguredPleaseSetViteGoogleMapsApiKeyFirst: string;
          /** 未找到匹配地点，或当前 Google Maps Key 未开通 Geocoding / Places 服务。 */
          noMatchingPlaceFoundOrGeocodingPlacesIsNotEnabledForTheCurrentGoogleMapsKey: string;
          /** Google 拒绝了搜索请求，请检查 Key 是否已启用 Places API 和 Geocoding API，并放行当前访问来源。 */
          googleRejectedTheSearchRequestCheckThatPlacesApiAndGeocodingApiAreEnabledForTheKeyAndTheCurrentOriginIsAllowed: string;
          /** 没有找到匹配地点，请尝试输入更完整的地址或直接输入经纬度。 */
          noMatchingPlaceFoundTryAMoreCompleteAddressOrEnterLatitudeLongitudeDirectly: string;
          /** Google Maps 查询额度暂时受限，请稍后再试或检查计费配置。 */
          googleMapsQuotaIsTemporarilyLimitedTryAgainLaterOrCheckBillingSettings: string;
          /** 请输入地点、地址或经纬度后再搜索 */
          enterAPlaceAddressOrLatLngBeforeSearching: string;
          /** 已按经纬度定位，请确认地图点位 */
          locatedByLatLngPleaseConfirmTheMapPoint: string;
          /** 已定位到搜索结果，请确认地图点位 */
          locatedToTheSearchResultPleaseConfirmTheMapPoint: string;
          /** 该位置无法自动解析完整地址，已保留经纬度 */
          theFullAddressOfThisLocationCannotBeResolvedAutomaticallyLatitudeLongitudeKept: string;
          /** 已选中我的位置 */
          myLocationSelected: string;
          /** 无法获取我的位置，请确认浏览器定位权限已开启；线上访问建议使用 HTTPS。 */
          cannotGetYourLocationMakeSureBrowserGeolocationIsEnabledHttpsIsRecommendedInProduction: string;
          /** Google 地图加载失败，请检查 API Key、Places/Geocoding 配置和当前网络是否可访问 maps.googleapis.com */
          failedToLoadGoogleMapsCheckTheApiKeyPlacesGeocodingSettingsAndNetworkAccessToMapsGoogleapisCom: string;
          /** 请先填写办公地址或当前打卡地址 */
          pleaseFillInTheOfficeAddressOrCurrentClockAddressFirst: string;
          /** 请先在地图上选择打卡位置 */
          pleaseSelectAClockLocationOnTheMapFirst: string;
          /** 搜索定位 */
          searchLocate: string;
          /** 按当前地址定位 */
          locateByCurrentAddress: string;
          /** 恢复当前配置 */
          restoreCurrentConfig: string;
          /** 已选地点 */
          selectedLocation2: string;
          /** 纬度（GCJ-02） */
          latitudeGcj02: string;
          /** 经度（GCJ-02） */
          longitudeGcj02: string;
          /** 确认并回填 */
          confirmFill: string;
          /** 选择打卡位置 */
          selectClockLocation: string;
          /** 搜索地址、园区、楼宇、地标，或输入经纬度（经度,纬度） */
          searchAddressParkBuildingOrLandmarkOrEnterLatLngLngLat: string;
          /** 这里使用 Google 地图搜索和选点，点击地图后会自动回填地址；保存到后台时会自动换算回 GCJ-02 坐标。 */
          searchAndPickOnGoogleMapsTheAddressIsFilledAutomaticallyAfterClickingTheMapAndConvertedBackToGcj02WhenSaved: string;
          /** 当前未配置 Google Maps API Key，请在前端环境变量中补充 VITE_GOOGLE_MAPS_API_KEY。 */
          googleMapsApiKeyIsNotConfiguredPleaseAddViteGoogleMapsApiKeyToTheFrontendEnv: string;
          /** 地图加载中... */
          mapLoading: string;
          /** 当前地点：{} */
          currentLocation: string;
        };
      };
      report: {
        employee: {
          /** 离职人数 */
          resignedEmployees: string;
          /** 入职人数 */
          newHires: string;
          /** 净增长 */
          netGrowth: string;
          /** 当前筛选条件下的员工数量 */
          employeesUnderCurrentFilters: string;
          /** 在职人数 */
          activeEmployees: string;
          /** 状态为在职的员工 */
          employeesWithActiveStatus: string;
          /** 状态为离职的员工 */
          employeesWithResignedStatus: string;
          /** 本月入职 */
          newHiresThisMonth: string;
          /** 按入职日期统计 */
          countedByEntryDate: string;
          /** 本月离职 */
          resignationsThisMonth: string;
          /** 按离职日期统计 */
          countedByResignationDate: string;
          /** 平均年龄 */
          averageAge: string;
          /** {age} 岁 */
          yrs: string;
          /** 基于已填写生日的员工 */
          basedOnEmployeesWithBirthdayFilled: string;
          /** 24岁及以下 */
          ageBucketUnder24: string;
          /** 25-29岁 */
          ageBucket25To29: string;
          /** 30-34岁 */
          ageBucket30To34: string;
          /** 35-39岁 */
          ageBucket35To39: string;
          /** 40-44岁 */
          ageBucket40To44: string;
          /** 45岁及以上 */
          ageBucket45AndAbove: string;
          /** {b}: {c}人 ({d}%) */
          people: string;
          /** 试用期人数 */
          employeesOnProbation: string;
          /** 状态为试用期的员工 */
          employeesOnProbation2: string;
          /** 员工数据量过大，仅统计前 2 万条，请添加筛选条件缩小范围 */
          tooManyEmployeesOnlyTheFirst20000AreCountedAddFiltersToNarrowTheRange: string;
          /** 暂无可导出的员工数据 */
          noEmployeeDataToExport: string;
          /** 员工报表_{stamp}.csv */
          employeeReportCsv: string;
          /** 员工报表已导出 */
          employeeReportExported: string;
          /** 查询报表 */
          queryReport: string;
          /** 导出明细 */
          exportDetails: string;
          /** 部门分布 */
          departmentDistribution: string;
          /** 员工类别分布 */
          employeeCategoryDistribution: string;
          /** 近 12 个月入离职趋势 */
          hireResignTrendLast12Months: string;
          /** 按当前筛选范围统计 */
          countedByCurrentFilterRange: string;
          /** 员工明细 */
          employeeDetails: string;
          /** 当前共 */
          total: string;
        };
        attendance: {
          /** 出勤率 */
          attendanceRate: string;
          /** 未分配部门 */
          unassignedDepartment: string;
          /** 参统员工 */
          employeesInStatistics: string;
          /** 参与月度统计人数 */
          employeesInMonthlyStatistics: string;
          /** 平均出勤率 */
          averageAttendanceRate: string;
          /** 实出勤 / 应出勤 */
          actualRequiredAttendance: string;
          /** 异常记录 */
          abnormalRecords: string;
          /** 迟到 / 早退 / 旷工 */
          lateEarlyLeaveAbsent: string;
          /** 所有员工工时汇总 */
          workHoursSummaryOfAllEmployees: string;
          /** 考勤数据量过大，仅统计前 2 万条明细，请缩小时间范围或添加筛选条件 */
          tooMuchAttendanceDataOnlyTheFirst20000DetailsAreCountedNarrowTheTimeRangeOrAddFilters: string;
          /** 暂无可导出的考勤数据 */
          noAttendanceDataToExport: string;
          /** 考勤报表_{month}_{stamp}.csv */
          attendanceReportCsv: string;
          /** 应出勤天数 */
          requiredAttendanceDays: string;
          /** 实出勤天数 */
          actualAttendanceDays: string;
          /** 迟到分钟 */
          lateMinutes: string;
          /** 早退分钟 */
          earlyLeaveMinutes: string;
          /** 旷工天数 */
          absentDays: string;
          /** 请假天数 */
          leaveDays: string;
          /** 考勤报表已导出 */
          attendanceReportExported: string;
          /** 更新报表 */
          refreshReport: string;
          /** 导出月报 */
          exportMonthlyReport: string;
          /** 部门表现 */
          departmentPerformance: string;
          /** 出勤率前 6 */
          top6AttendanceRate: string;
          /** 异常洞察 */
          abnormalityInsights: string;
          /** 最新异常与分布 */
          latestAbnormalitiesDistribution: string;
          /** 明细视图 */
          detailView: string;
          /** 切换部门、员工和异常三个视角 */
          switchAmongDepartmentEmployeeAndAbnormalityViews: string;
          /** 请选择月份 */
          pleaseSelectAMonth: string;
          /** 暂无部门数据 */
          noDepartmentData: string;
          /** 暂无异常记录 */
          noAbnormalRecords: string;
          /** 部门汇总 */
          departmentSummary: string;
          /** 员工数 */
          employees: string;
          /** 员工月报 */
          employeeMonthlyReport: string;
          /** 迟到分 */
          lateMinutes2: string;
          /** 早退分 */
          earlyLeaveMinutes2: string;
          /** 异常明细 */
          abnormalityDetails: string;
          /** 异常类型 */
          abnormalityType: string;
          /** 相关时间 */
          relatedTime: string;
        };
      };
      approval: {
        flow: {
          /** 审批节点 */
          approvalNode: string;
          /** 请输入描述 */
          pleaseEnterDescription: string;
          /** 请输入流程编码 */
          pleaseEnterFlowCode: string;
          /** 请输入流程名称 */
          pleaseEnterFlowName: string;
          /** 请选择流程类型 */
          pleaseSelectFlowType: string;
          /** 已启用 */
          enabled: string;
          /** 流程编码 */
          flowCode: string;
          /** 流程名称 */
          flowName: string;
          /** 流程类型 */
          flowType: string;
          /** 免审批 */
          noApproval: string;
          /** 请选择角色 */
          pleaseSelectARole: string;
          /** 已停用 */
          disabled: string;
          /** 请至少添加一个审批节点 */
          pleaseAddAtLeastOneApprovalNode: string;
          /** 审批流程配置 */
          approvalFlowConfig: string;
          /** 新增流程 */
          newFlow: string;
          /** 免审批直接通过 */
          autoApproveWithoutReview: string;
          /** 开启后申请将自动通过，无需审批 */
          whenEnabledApplicationsWillBeAutoApprovedWithoutReview: string;
          /** 添加节点 */
          addNode: string;
          /** 按流程类型筛选 */
          filterByFlowType: string;
          /** 确定删除该流程吗？ */
          areYouSureYouWantToDeleteThisFlow: string;
          /** 直接通过 */
          directApproval: string;
          /** 需要审批 */
          approvalRequired: string;
          /** 节点名称 */
          nodeName: string;
          /** 如：部门经理审批 */
          eGDepartmentManagerApproval: string;
          /** 节点类型 */
          nodeType: string;
          /** 抄送 */
          cc: string;
          /** 审批角色 */
          approvalRole: string;
          /** 未指定角色 */
          noRoleSpecified: string;
          /** 新增审批流程 */
          newApprovalFlow: string;
          /** 编辑审批流程 */
          editApprovalFlow: string;
          /** 请输入节点名称 */
          pleaseEnterNodeName: string;
          /** 请选择审批角色 */
          pleaseSelectApprovalRole: string;
          /** 节点 */
          node: string;
        };
        common: {
          batchApprove: string;
          batchReject: string;
          pleaseSelectRowsFirst: string;
          batchRejectReasonPrompt: string;
          batchApprovedSummary: string;
          batchRejectedSummary: string;
          /** 奖励申请 */
          rewardApplication: string;
          /** 惩罚申请 */
          punishmentApplication: string;
          /** 审批确认 */
          approvalConfirmation: string;
          /** 再想想 */
          notNow: string;
          /** 审批进度 */
          approvalProgress: string;
          /** 时长 */
          duration: string;
          /** 试用期结束 */
          probationEnd: string;
          /** 转正后类别 */
          categoryAfterRegularization: string;
          /** 申请原因/备注 */
          applicationReasonRemark: string;
          /** 确认通过该申请吗？ */
          areYouSureYouWantToApproveThisApplication: string;
          /** 确认通过 */
          confirmApproval: string;
          /** 审批通过 */
          approved: string;
          /** 请填写拒绝原因 */
          pleaseEnterRejectionReason: string;
          /** 确认拒绝该申请吗？ */
          areYouSureYouWantToRejectThisApplication: string;
          /** 确认拒绝 */
          confirmRejection: string;
          /** 请输入申请人 */
          pleaseEnterApplicant: string;
          /** 申请原因/说明 */
          applicationReasonNotes: string;
          /** 审批详情 */
          approvalDetails: string;
          /** 请输入审批意见（拒绝时必填） */
          pleaseEnterApprovalCommentRequiredWhenRejecting: string;
          /** 条待处理 */
          pending: string;
          /** 转正日期: */
          regularizationDate: string;
          /** 最后工作日: */
          lastWorkingDay: string;
          /** 审批人: */
          approver: string;
        };
        mine: {
          /** 我的申请 */
          myApplications: string;
        };
        mobileApprover: {
          /** 确定要删除该审批权限配置吗？ */
          areYouSureYouWantToDeleteThisApprovalPermissionConfig: string;
          /** 添加成功 */
          addSuccess: string;
          /** 全部类型 */
          allTypes: string;
          /** 移动端审批权限 */
          mobileApprovalPermission: string;
          /** 不勾选则表示可审批全部类型 */
          leaveUncheckedToApproveAllTypes: string;
          /** 可审批类型 */
          approvableTypes: string;
          /** 配置时间 */
          configTime: string;
          /** 审批类型 */
          approvalType: string;
          /** 新增审批权限 */
          newApprovalPermission: string;
          /** 编辑审批权限 */
          editApprovalPermission: string;
        };
        pending: {
          /** 待审批列表 */
          pendingApprovals: string;
        };
      };
      userCenter: {
        /** 请输入当前密码 */
        pleaseEnterCurrentPassword: string;
        /** 请再次输入新密码 */
        pleaseEnterNewPasswordAgain: string;
        /** 登录账号 */
        loginAccount: string;
        /** 密码长度为 6-18 位 */
        passwordLengthMustBe618Characters: string;
        /** 两次输入的新密码不一致 */
        theTwoNewPasswordsDoNotMatch: string;
        /** 当前账号资料未加载完成 */
        currentAccountProfileIsNotLoadedYet: string;
        /** 资料保存成功 */
        profileSavedSuccessfully: string;
        /** 密码修改成功 */
        passwordChangedSuccessfully: string;
        /** 账号信息 */
        accountInfo: string;
        /** 所属角色 */
        roles: string;
        /** 未分配角色 */
        noRolesAssigned: string;
        /** 个人资料 */
        profile: string;
        /** 保存资料 */
        saveProfile: string;
        /** 安全设置 */
        securitySettings: string;
        /** 修改密码 */
        changePassword: string;
        /** 当前密码 */
        currentPassword: string;
        /** 确认新密码 */
        confirmNewPassword: string;
        /** 员工ID */
        employeeId: string;
      };
      component: {
        authImage: {
          /** 图片加载失败 */
          failedToLoadImage: string;
          /** 加载失败（{status}） */
          loadFailed: string;
          /** 暂无图片 */
          noImage: string;
        };
      };
    };

    type GetI18nKey<T extends Record<string, unknown>, K extends keyof T = keyof T> = K extends string
      ? T[K] extends Record<string, unknown>
        ? `${K}.${GetI18nKey<T[K]>}`
        : K
      : never;

    type I18nKey = GetI18nKey<Schema>;

    type TranslateOptions<Locales extends string> = import('vue-i18n').TranslateOptions<Locales>;

    interface $T {
      (key: I18nKey): string;
      (key: I18nKey, plural: number, options?: TranslateOptions<LangType>): string;
      (key: I18nKey, defaultMsg: string, options?: TranslateOptions<I18nKey>): string;
      (key: I18nKey, list: unknown[], options?: TranslateOptions<I18nKey>): string;
      (key: I18nKey, list: unknown[], plural: number): string;
      (key: I18nKey, list: unknown[], defaultMsg: string): string;
      (key: I18nKey, named: Record<string, unknown>, options?: TranslateOptions<LangType>): string;
      (key: I18nKey, named: Record<string, unknown>, plural: number): string;
      (key: I18nKey, named: Record<string, unknown>, defaultMsg: string): string;
    }
  }

  /** Service namespace */
  namespace Service {
    /** Other baseURL key */
    type OtherBaseURLKey = 'demo';

    interface ServiceConfigItem {
      /** The backend service base url */
      baseURL: string;
      /** The proxy pattern of the backend service base url */
      proxyPattern: string;
    }

    interface OtherServiceConfigItem extends ServiceConfigItem {
      key: OtherBaseURLKey;
    }

    /** The backend service config */
    interface ServiceConfig extends ServiceConfigItem {
      /** Other backend service config */
      other: OtherServiceConfigItem[];
    }

    interface SimpleServiceConfig extends Pick<ServiceConfigItem, 'baseURL'> {
      other: Record<OtherBaseURLKey, string>;
    }

    /** The backend service response data */
    type Response<T = unknown> = {
      /** The backend service response code */
      code: string;
      /** The backend service response message */
      msg: string;
      /** The backend service response data */
      data: T;
    };

    /** The demo backend service response data */
    type DemoResponse<T = unknown> = {
      /** The backend service response code */
      status: string;
      /** The backend service response message */
      message: string;
      /** The backend service response data */
      result: T;
    };
  }
}
