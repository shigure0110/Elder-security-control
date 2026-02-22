# Elder-security-control

面向老人设备的高风险行为审批控制原型，包含：

- Android 守护侧：
  - `AppInstallWatcher` 监听应用安装广播与安装流程页面特征。
  - `WeChatRiskActionDetector` 在辅助功能中识别微信加好友风险动作。
  - `BlockingOverlayService` 在未审批时展示不可关闭阻断层并支持联系家属。
  - `EmergencyWhitelist` 提供医院/家人/社区服务等紧急白名单。
- 服务端 `server/approval-service`：
  - 创建审批单、超时失效。
  - 家属 1/3 通过规则。
  - WebSocket 下发审批结果并支持短时解锁（3 分钟）。
  - 全量审计日志记录（谁批准、何时、操作类型）。
