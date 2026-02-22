import express from 'express';
import { WebSocketServer } from 'ws';
import { createServer } from 'http';
import { randomUUID } from 'crypto';

const app = express();
app.use(express.json());

const REQUEST_TTL_MS = 3 * 60 * 1000;
const SHORT_UNLOCK_MS = 3 * 60 * 1000;

/** @type {Map<string, any>} */
const approvals = new Map();
/** @type {Array<any>} */
const auditLogs = [];
/** @type {Map<string, Set<any>>} */
const deviceSockets = new Map();

function requiredVotes(totalGuardians) {
  return Math.max(1, Math.ceil(totalGuardians / 3));
}

function appendAudit(action, payload) {
  auditLogs.push({
    id: randomUUID(),
    action,
    payload,
    timestamp: new Date().toISOString()
  });
}

function notifyDevice(deviceId, message) {
  const sockets = deviceSockets.get(deviceId);
  if (!sockets) return;

  for (const ws of sockets) {
    if (ws.readyState === ws.OPEN) {
      ws.send(JSON.stringify(message));
    }
  }
}

app.post('/approvals', (req, res) => {
  const { deviceId, riskType, target, reason, guardians = [] } = req.body;
  if (!deviceId || !riskType || !target) {
    return res.status(400).json({ error: 'deviceId, riskType, target are required' });
  }

  const id = randomUUID();
  const now = Date.now();
  const approval = {
    id,
    deviceId,
    riskType,
    target,
    reason,
    guardians,
    votes: {},
    status: 'PENDING',
    requiredVotes: requiredVotes(guardians.length || 1),
    createdAt: now,
    expiresAt: now + REQUEST_TTL_MS,
    unlockUntil: null
  };

  approvals.set(id, approval);
  appendAudit('APPROVAL_CREATED', { id, deviceId, riskType, target, reason });
  res.status(201).json(approval);
});

app.post('/approvals/:id/vote', (req, res) => {
  const { id } = req.params;
  const { guardianId, approve } = req.body;
  const approval = approvals.get(id);

  if (!approval) return res.status(404).json({ error: 'approval not found' });
  if (approval.status !== 'PENDING') {
    return res.status(409).json({ error: `approval already ${approval.status}` });
  }
  if (Date.now() > approval.expiresAt) {
    approval.status = 'EXPIRED';
    appendAudit('APPROVAL_EXPIRED', { id });
    return res.status(410).json({ error: 'approval expired' });
  }

  approval.votes[guardianId] = Boolean(approve);
  const yesVotes = Object.values(approval.votes).filter(Boolean).length;

  appendAudit('GUARDIAN_VOTED', { id, guardianId, approve });

  if (yesVotes >= approval.requiredVotes) {
    approval.status = 'APPROVED';
    approval.unlockUntil = Date.now() + SHORT_UNLOCK_MS;
    const approvedBy = Object.entries(approval.votes)
      .filter(([, ok]) => ok)
      .map(([guardian]) => guardian);

    const decision = {
      requestId: id,
      status: 'APPROVED',
      approvedBy,
      decidedAt: new Date().toISOString(),
      unlockWindowSeconds: SHORT_UNLOCK_MS / 1000,
      riskType: approval.riskType
    };

    appendAudit('APPROVAL_APPROVED', decision);
    notifyDevice(approval.deviceId, { type: 'APPROVAL_DECISION', data: decision });
  }

  res.json(approval);
});

app.get('/approvals/:id', (req, res) => {
  const approval = approvals.get(req.params.id);
  if (!approval) return res.status(404).json({ error: 'approval not found' });

  if (approval.status === 'PENDING' && Date.now() > approval.expiresAt) {
    approval.status = 'EXPIRED';
    appendAudit('APPROVAL_EXPIRED', { id: approval.id });
  }

  res.json(approval);
});

app.get('/audit-logs', (_req, res) => {
  res.json(auditLogs);
});

const server = createServer(app);
const wss = new WebSocketServer({ server, path: '/ws' });

wss.on('connection', (ws, req) => {
  const url = new URL(req.url, 'http://localhost');
  const deviceId = url.searchParams.get('deviceId');
  if (!deviceId) {
    ws.close(1008, 'deviceId required');
    return;
  }

  const set = deviceSockets.get(deviceId) || new Set();
  set.add(ws);
  deviceSockets.set(deviceId, set);
  appendAudit('DEVICE_CONNECTED', { deviceId });

  ws.on('close', () => {
    const sockets = deviceSockets.get(deviceId);
    if (!sockets) return;
    sockets.delete(ws);
    if (sockets.size === 0) deviceSockets.delete(deviceId);
  });
});

const port = process.env.PORT || 7070;
server.listen(port, () => {
  console.log(`approval-service listening on ${port}`);
});
