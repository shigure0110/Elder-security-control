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
- 新增可行性与启动任务文档：`docs/FEASIBILITY_AND_STARTUP_TASKS.md`（覆盖微信群日报、安装/加好友家庭审批边界与实现路线）。

## 打包 APK 说明
当前仓库已提供 Android Gradle 构建脚本，CI 会同时产出 `debug` 与 `release-unsigned` 两种 APK。

本地可执行：

```bash
gradle assembleDebug assembleRelease
```

生成路径通常为：
`app/build/outputs/apk/release/app-release-unsigned.apk`

兼容性说明：当前 `minSdk=21`（Android 5.0+）。


## 在哪里下载 APK / 如何发布
- 在 GitHub Actions 的某次 `Android APK CI` 运行页面，下载 `Artifacts`：
  - `app-debug-apk`（可直接安装，包名后缀 `.debug`）
  - `app-release-unsigned-apk`（发布前需签名）
- 注意下载的是 zip 工件，先解压再安装 `.apk`。
- 如果 `Artifacts` 为空，通常表示本次构建失败；请先查看日志定位失败步骤。
- 完整步骤见：`docs/APK_DOWNLOAD_AND_RELEASE.md`。

## Release 出包（按你的要求）
仓库已新增 `release/` 目录和发布脚本 `scripts/publish_release_apk.sh`，用于把 `app-release.apk` 放到 `release/elder-security-release.apk` 并生成校验元数据。

```bash
./scripts/publish_release_apk.sh
```

> 说明：CI 默认产出 debug APK（`app-debug-apk` artifact）；发布包建议使用 `gradle assembleRelease` 后再执行发布脚本。
