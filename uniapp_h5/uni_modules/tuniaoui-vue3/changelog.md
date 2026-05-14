## 1.0.23（2025-04-28）
`TnWaterFall`新增自定义插槽
## 1.0.22 (2024-12-27)
修复`TnSteps`横线错位的问题
优化`TnPicker`无法动态修改多列选择数据的问题
修复微信小程序`getSystemInfoSync`问题
修复微信小程序获取胶囊信息出错的问题
`TnSwiper`插槽新增返回`index`参数
`TnPicker`新增`allow-confirm-before-scroll-end`处理滚动结束前确认
`TnPicker`新增`indicator-height`参数设置指示器高度
`TnTabbar`新增`hidden-fixed-tabbar`参数设置隐藏固定底部栏
`TnTabbar`新增`bulge-max-size`和`bulge-size-percent`设置中间凸起按钮的尺寸信息
`TnPicker`新增`immediate-change`参数
`TnPicker`新增`pickstart`事件
`TnPicker`新增`pickend`事件
`TnKeyboard`新增`static`参数设置是否静态显示
`TnTabbarItem`新增`content`插槽## 1.0.21（2024-07-18）
修复`TnInput`在部分环境下无法使用的问题
修复`TnPicker`在没有选中任何值得情况下多列无法选中得问题
修复`TnPicker`在多列选择时初始化值为字符串下选中不正确的问题
`TnActionSheer`新增`maskClosable`参数设置点击遮罩是否关闭
`TnPopup`新增`overlay-click`遮罩点击事件
## 1.0.20（2024-02-01）
- 修复`TnInput`在`select`模式下`H5`端无法点击的问题
- 修复`TnInput`在`textarea`模式下无法换行的问题
- 修复`TnInput`在`number`模式下手动删除数据会出现`NaN`的问题
- 修复`TnTabbar`无法初始化选择`TabbarItem`的问题
- 修复`TnInput`在`textarea`模式下初始化值的时候无法统计字数的问题
- 修复`TnPopup`初始化时会触发`close`和`open`方法的问题
- `TnDateTimePicker` `mode`新增`datetimeNoSecond`和`timeNoSecond`模式
- `TnButton`新增`click-modifiers`参数设置点击事件的修饰符
- `TnInput`新增`underline`参数
