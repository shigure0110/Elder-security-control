# Release 目录说明

这个目录用于存放最终可分发 APK：

- 目标文件名：`elder-security-release.apk`
- 元信息文件：`RELEASE_METADATA.txt`（包含 sha256、大小等）

## 放包步骤
1. 在完整 Android 工程中构建：
   ```bash
   gradle assembleRelease
   ```
2. 将构建产物拷贝到 `release/`：
   ```bash
   ./scripts/publish_release_apk.sh
   ```

如 APK 不在默认路径，可指定输入与输出路径：
```bash
./scripts/publish_release_apk.sh <input-apk> <output-apk>
```

## 从 GitHub Actions 下载
- 路径：仓库 -> **Actions** -> 某次 `Android APK CI` -> **Artifacts**。
- `Build debug APK` 与上传步骤成功时，会出现 `app-debug-apk`。
- 若 Artifacts 为空，请在 Actions 日志中检查构建或上传失败原因。


> 提示：CI 上传的 release 工件默认是 `app-release-unsigned.apk`，发布到手机前请先签名。
