# report-dispatcher

该服务接收 Android 客户端上传的每日账单汇总，再通过合规渠道（企业微信机器人等）分发到家庭群。

## 合规建议
- 客户端不直接自动操作微信 UI。
- 由服务端统一转发到企业微信机器人/短信/邮件等渠道。

## API
`POST /reports/daily`

```json
{
  "familyId": "home-001",
  "report": "[日账单汇总] ..."
}
```
