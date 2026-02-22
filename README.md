# Elder Security Control

一个面向家庭场景的“老人防诈骗 + 消费守护”安卓项目说明文档。

> 你现在是小白也没关系，这份文档按 **从 0 到可安装 APK** 的顺序写，照着做就可以测试。

## 1. 你现在仓库里有什么？

目前这个仓库还没有正式 Android 工程代码，先有一个需求想法。

所以你要走两步：

1. 先把这份仓库同步到自己的 GitHub（留档 + 版本管理）。
2. 再创建 Android 项目并打包 APK 做测试。

---

## 2. 把代码推到你的 GitHub 仓库（最稳妥做法）

以下命令在你自己的电脑终端执行（Windows 可用 Git Bash，Mac 用 Terminal）。

### 2.1 创建 GitHub 空仓库

1. 打开 GitHub，点 `New repository`。
2. 仓库名建议：`elder-security-control`。
3. 选择 `Public` 或 `Private` 都行。
4. **不要勾选** `Add a README`（因为本地已有 README）。
5. 创建后会看到仓库地址，例如：

```bash
git@github.com:你的用户名/elder-security-control.git
```

或

```bash
https://github.com/你的用户名/elder-security-control.git
```

### 2.2 本地关联远程并推送

假设你已经在本地有这个项目目录：

```bash
cd elder-security-control

git init
git add .
git commit -m "docs: add beginner guide for GitHub push and APK build"

git branch -M main
git remote add origin git@github.com:你的用户名/elder-security-control.git
# 如果你不用 SSH，就改成 https 地址

git push -u origin main
```

如果你看到 `Enumerating objects...` 然后 `main -> main`，就说明成功了。

---

## 3. 变成 APK：最简单路线（Android Studio）

## 3.1 安装工具

1. 安装 Android Studio（官网下载安装）。
2. 首次启动按默认安装 SDK、Build Tools、Android Emulator。

## 3.2 创建项目

1. 打开 Android Studio。
2. 选择 `New Project` -> `Empty Activity`。
3. 配置：
   - Name: `ElderSecurityControl`
   - Package name: 例如 `com.yourname.eldersecurity`
   - Language: `Kotlin`
   - Minimum SDK: 建议 `Android 8.0 (API 26)`
4. 点 `Finish`。

## 3.3 先跑通一个“最小可测试版本”

你可以先只做这 3 个功能（最快验证价值）：

1. 通知读取（抓微信支付/支付宝支付通知文本）。
2. 本地保存消费记录（Room 数据库）。
3. 每天 20:00 生成汇总并提醒（先本地通知，不急着自动发微信群）。

> 说明：直接“自动发微信群消息”通常要走企业微信机器人/服务端中转，更稳定。

## 3.4 打包 Debug APK（用于自己测试）

在 Android Studio 菜单里：

- `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`

完成后右下角会提示 `APK(s) generated successfully`，点击 `locate` 可找到 APK。

常见路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

把这个 `app-debug.apk` 传到手机安装即可测试。

---

## 4. 给家人测试：建议的实际流程

1. 先在你自己的备用机安装 APK（不要直接上老人主力机）。
2. 验证 3 天：
   - 通知抓取是否稳定。
   - 金额识别是否正确。
   - 每日汇总是否可读。
3. 再加“审批门禁”能力：
   - 新安装 App 审批。
   - 高风险支付二次确认。
4. 最后才上“微信加好友审批”（这个最复杂，且容易受微信版本影响）。

---

## 5. 你现在就能执行的下一步（最重要）

如果你希望我继续帮你，我建议下一步是：

1. 我先给你生成一个 **可编译的 Android 项目骨架**（含通知抓取、消费记录、每日汇总）。
2. 你把它 push 到 GitHub。
3. 我再告诉你如何一键打包 APK 并在手机安装测试。

这样你最快 1~2 天就能看到第一个可运行版本。

---

## 6. 常见报错速查

- `SDK location not found`：Android Studio 没正确下载 SDK，去 `Settings -> Android SDK` 安装。
- `Gradle sync failed`：先点 `Try Again`，不行就检查网络和 Gradle 版本。
- 手机上装不上：
  - 开启“允许安装未知来源应用”。
  - 检查是否签名冲突（同包名旧版未卸载）。

---

## 7. 安全与合规提醒（务必看）

- 这类 App 涉及老人个人隐私，必须让老人和直系家属知情。
- 默认只采集“必要信息”（金额、时间、类型），避免采集聊天正文。
- 对“阻断操作”要给清晰提示，避免老人恐慌。

