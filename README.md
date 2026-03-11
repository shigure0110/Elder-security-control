# Elder-security-control

## 当前完成情况（核心能力）
- 本地风险规则引擎：电话 / 短信 / 通知文本关键词识别。
- 支付复合风险：高额转账 + 陌生收款方 + 短时间重复支付。
- 高风险二次确认：家庭约定口令或家属语音确认。
- 风险事件落库：`RiskEvent` 表 + 趋势查询（家属端可看趋势）。

## 新增体验能力
- 新增老人友好首页 UI（大字号、强对比、大按钮，接近 iOS 简洁风格）。
- 新增应用内更新管理器（检查版本后下载 APK，并触发安装）。
- 新增风控设置页雏形：可配置平台管控开关、小额账单阈值/汇报时间、目标微信群。
- 新增功能落地规划文档：`docs/IMPLEMENTATION_PLAN.md`。

## 打包 APK 说明
当前仓库尚未包含完整 Android Gradle 工程（缺少 `gradlew` / `build.gradle` / `settings.gradle`），因此无法在本仓库直接产出可安装 APK。

如需我帮你直接打包 APK，请补充完整 Android 工程文件后可执行：

```bash
./gradlew assembleRelease
```

生成路径通常为：
`app/build/outputs/apk/release/app-release.apk`


## Release 出包（按你的要求）
仓库已新增 `release/` 目录和发布脚本 `scripts/publish_release_apk.sh`，用于把 `app-release.apk` 放到 `release/elder-security-release.apk` 并生成校验元数据。

```bash
./scripts/publish_release_apk.sh
```

> 说明：当前仓库快照仍缺失完整 Android 构建工程文件，所以在这里无法直接“编译”出 APK；但一旦你提供完整工程或在你现有工程中构建出 APK，我这套脚本可立即把包放到 release 目录。
