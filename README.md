# Elder-security-control

## 当前完成情况（核心能力）
- 本地风险规则引擎：电话 / 短信 / 通知文本关键词识别。
- 支付复合风险：高额转账 + 陌生收款方 + 短时间重复支付。
- 高风险二次确认：家庭约定口令或家属语音确认。
- 风险事件落库：`RiskEvent` 表 + 趋势查询（家属端可看趋势）。

## 新增体验能力
- 新增老人友好首页 UI（大字号、强对比、大按钮，接近 iOS 简洁风格）。
- 新增应用内更新管理器（检查版本后下载 APK，并触发安装）。

## 打包 APK 说明
当前仓库尚未包含完整 Android Gradle 工程（缺少 `gradlew` / `build.gradle` / `settings.gradle`），因此无法在本仓库直接产出可安装 APK。

如需我帮你直接打包 APK，请补充完整 Android 工程文件后可执行：

```bash
./gradlew assembleRelease
```

生成路径通常为：
`app/build/outputs/apk/release/app-release.apk`
