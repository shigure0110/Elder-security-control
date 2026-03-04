# Release 目录

该目录不再提交 APK 二进制文件（`release/*.apk` 已被 `.gitignore` 忽略），避免仓库更新失败。

请通过 GitHub Actions 下载 CI 产物：
1. 进入 `Android APK CI` 工作流
2. 下载 artifact `app-debug-apk`
3. 解压获取 `app-debug.apk`
