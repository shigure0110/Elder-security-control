# Release 目录说明

这个目录用于存放最终可分发 APK：

- 目标文件名：`elder-security-release.apk`
- 元信息文件：`RELEASE_METADATA.txt`（包含 sha256、大小等）

## 放包步骤
1. 在完整 Android 工程中构建：
   ```bash
   ./gradlew assembleRelease
   ```
2. 将构建产物拷贝到 `release/`：
   ```bash
   ./scripts/publish_release_apk.sh
   ```

如 APK 不在默认路径，可指定输入与输出路径：
```bash
./scripts/publish_release_apk.sh <input-apk> <output-apk>
```
