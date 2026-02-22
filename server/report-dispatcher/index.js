const express = require('express');

const app = express();
app.use(express.json());

/**
 * 示例：接收客户端汇总并转发到企业微信机器人。
 * 实际项目可替换为内部消息网关。
 */
app.post('/reports/daily', async (req, res) => {
  const { familyId, report } = req.body || {};
  if (!familyId || !report) {
    return res.status(400).json({ error: 'familyId and report are required' });
  }

  // TODO: 在此接企业微信机器人 webhook 或其他合规转发渠道。
  console.log(`[dispatch] family=${familyId}\n${report}`);
  return res.status(202).json({ status: 'queued' });
});

const port = process.env.PORT || 8080;
app.listen(port, () => {
  console.log(`report-dispatcher running on :${port}`);
});
