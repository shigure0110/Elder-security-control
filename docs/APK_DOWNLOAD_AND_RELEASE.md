# APK 下载与发布指引

## 先判断：这次 CI 有没有产出 APK
在 GitHub 仓库页面：
1. 打开 **Actions**。
2. 点进一次 `Android APK CI` 运行记录。
3. 在页面右侧/下方查看 **Artifacts** 区域。

### 结果 A：能看到 `app-debug-apk`
说明本次构建成功产出了 APK，可直接下载 zip，解压后得到 `app-debug.apk`。

### 结果 B：Artifacts 是空的
这表示本次构建未产出 APK。常见原因：
- 构建步骤失败（依赖下载失败、编译错误、签名配置问题等）。
- 上传步骤因目标文件不存在而失败。

建议先点进失败步骤查看日志。

---

## 本地/完整工程发布到 release 目录（仓库内约定）
当你在完整 Android 工程里先构建出 APK 后：

1. 构建 release APK：
```bash
gradle assembleRelease
```

2. 调用发布脚本，把包拷贝到 `release/` 并生成校验信息：
```bash
./scripts/publish_release_apk.sh
```

默认会生成：
- `release/elder-security-release.apk`
- `release/RELEASE_METADATA.txt`

3. 若 APK 不在默认路径，可自定义输入输出：
```bash
./scripts/publish_release_apk.sh <input-apk> <output-apk>
```

---

## 下载不到 APK 时的排查顺序
- 先看 `Build debug APK` 是否执行并成功。
- 再看 `Upload debug APK artifact` 是否执行并成功。
- 如果上传步骤失败，检查 `app/build/outputs/apk/debug/app-debug.apk` 是否存在。
