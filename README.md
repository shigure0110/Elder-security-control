# Elder-security-control

实现了老人支付账单采集与每日汇总的基础骨架：

- Android 通知采集服务：`NotificationCollectorService`
- Room 账单仓库：`BillRepository + BillingDatabase`
- 辅助功能补录解析：`AccessibilityBillParser`
- WorkManager 每日汇总：`DailyReportWorker`
- 设置页完整性提示文案：`DataIntegrityNotice`
- 服务端分发示例：`server/report-dispatcher`
