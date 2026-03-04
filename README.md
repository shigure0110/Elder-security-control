# Elder-security-control

这是一个面向老人防诈骗/支付风控的 Android App（本地规则引擎 + 老人友好 UI + 家属联动）。

## 已实现能力
- 本地风险规则引擎：电话/短信/通知关键词识别。
- 支付复合规则：高额转账 + 陌生收款方 + 短时间重复支付。
- 高风险二次确认：家庭口令或家属语音确认。
- `RiskEvent` 落库与趋势查询（Room）。
- 老人友好首页（大字号、高对比、大按钮）。
- 应用内 APK 下载更新流程（`AppUpdateManager` + FileProvider）。

## 工程结构（已补齐）
- Gradle Wrapper：`gradlew` / `gradlew.bat` / `gradle/wrapper/*`
- Android 构建文件：`settings.gradle.kts` / `build.gradle.kts` / `app/build.gradle.kts`
- 可启动 App：`AndroidManifest.xml` + `MainActivity`
- CI：`.github/workflows/android-apk.yml`

## 云端自动打包 APK（GitHub Actions）
每次 push 到 `main` / PR 会自动执行：

```bash
gradle assembleDebug
```

并上传 APK artifact：
- 名称：`app-debug-apk`
- 文件：`app/build/outputs/apk/debug/app-debug.apk`

### 下载 APK
1. 打开 GitHub 仓库 → **Actions**
2. 进入任意一次 `Android APK CI` 成功构建
3. 在 Artifacts 区域下载 `app-debug-apk`
4. 解压后即可获得 `app-debug.apk` 安装包


### CI 兼容说明（防止 wrapper 缺失报错）
工作流固定使用 `gradle/actions/setup-gradle` 安装 Gradle，并执行 `gradle assembleDebug`。

这样即使仓库不跟踪 `gradle-wrapper.jar`，也不会因为 wrapper 缺失而导致 CI 失败。


### 二进制限制说明
为避免“拉取失败/不支持二进制文件”，仓库不再跟踪 `gradle/wrapper/gradle-wrapper.jar`。CI 直接使用 `gradle assembleDebug`（由 `gradle/actions/setup-gradle` 提供 Gradle），不再依赖仓库内 wrapper 二进制。
