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
每次 push/PR 会自动执行：

```bash
./gradlew assembleDebug
```

并上传 APK artifact：
- 名称：`app-debug-apk`
- 文件：`app/build/outputs/apk/debug/app-debug.apk`

### 下载 APK
1. 打开 GitHub 仓库 → **Actions**
2. 进入任意一次 `Android APK CI` 成功构建
3. 在 Artifacts 区域下载 `app-debug-apk`
4. 解压后即可获得 `app-debug.apk` 安装包


### CI 兼容说明（防止 `gradlew` 缺失报错）
工作流已兼容两种构建方式：
- 优先使用 `./gradlew assembleDebug`（仓库包含 wrapper 时）
- 若 `gradlew` 不存在，自动回退到 `gradle assembleDebug`

这样即使 `main` 分支暂时缺少 wrapper 文件，Actions 也不会在 `chmod +x gradlew` 处直接失败。


### 二进制限制说明
为避免“拉取失败/不支持二进制文件”，仓库不再跟踪 `gradle/wrapper/gradle-wrapper.jar`。CI 会优先使用 `./gradlew`（若存在可用 wrapper），否则回退到 `gradle assembleDebug`。
