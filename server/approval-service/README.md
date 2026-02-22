# approval-service

审批网关服务能力：

- 创建审批单（`POST /approvals`）。
- 超时策略：默认 3 分钟过期（`EXPIRED`）。
- 家属投票：按 `ceil(N/3)` 规则通过（例如 3 人中 1 人同意即可）。
- 审批结果下发：WebSocket `/ws?deviceId=...`（可扩展为 FCM）。
- 审计日志：记录创建、投票、审批结果、设备连接。

## 启动

```bash
cd server/approval-service
npm install
npm start
```
