# APK 下载与发布指引

## 先判断：这次 CI 有没有产出 APK
在 GitHub 仓库页面：
1. 打开 **Actions**。
2. 点进一次 `Android APK CI` 运行记录。
3. 在页面右侧/下方查看 **Artifacts** 区域。

### 结果 A：能看到 Artifact
说明本次构建成功产出了 APK。你会看到：
- `app-debug-apk`：解压后得到 `app-debug.apk`（可直接安装）。
- `app-release-unsigned-apk`：解压后得到 `app-release-unsigned.apk`（发布前需签名）。

> 注意：Artifact 下载下来是 zip，不是直接可安装包，必须先解压。

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

## 两个 Artifact 到底该装哪个（结论）
- **日常直接安装测试：装 `app-debug-apk`**（解压后 `app-debug.apk`）。
- `app-release-unsigned-apk`（解压后 `app-release-unsigned.apk`）**默认不能直接作为正式安装包分发**，因为它是未签名 release 包，需先签名。

一句话：你现在要在手机上马上安装验证，就选 **`app-debug-apk`**。

## 手机提示“安装包无效/不兼容”排查
- 先确认你安装的是 **解压后的 `.apk`**，不是 Artifact zip。
- 查看手机系统版本是否 >= Android 5.0（项目 `minSdk=21`）。
- 若手机已安装同包名旧版本，先卸载后再装。
- 优先安装 `app-debug.apk`（已可安装）；`app-release-unsigned.apk` 需要签名后才可正式分发安装。

## 下载不到 APK 时的排查顺序
- 先看 `Build debug APK` 与 `Build release APK (unsigned)` 是否执行并成功。
- 再看两个 Upload 步骤是否执行并成功。
- 若上传失败，检查对应输出文件是否存在。
