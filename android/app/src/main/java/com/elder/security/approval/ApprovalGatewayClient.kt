package com.elder.security.approval

import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 设备侧审批网关客户端：负责创建审批单、接收推送结果、维护短时解锁窗口。
 */
class ApprovalGatewayClient(
    private val deviceId: String
) {
    private val unlockWindows = ConcurrentHashMap<RiskType, Long>()

    fun submitRisk(
        riskType: RiskType,
        target: String,
        reason: String
    ): ApprovalRequest {
        val request = ApprovalRequest(
            requestId = UUID.randomUUID().toString(),
            deviceId = deviceId,
            riskType = riskType,
            target = target,
            reason = reason
        )
        // 实际环境中调用 server/approval-service API。
        Log.i(TAG, "submit approval request=$request")
        return request
    }

    fun onDecision(decision: ApprovalDecision) {
        Log.i(TAG, "receive decision=$decision")
        if (decision.status == ApprovalStatus.APPROVED) {
            val expireAt = System.currentTimeMillis() + decision.unlockWindowSeconds * 1000
            unlockWindows[RiskType.APP_INSTALL] = expireAt
            unlockWindows[RiskType.WECHAT_ADD_FRIEND] = expireAt
        }
    }

    fun isTemporarilyUnlocked(riskType: RiskType): Boolean {
        val expireAt = unlockWindows[riskType] ?: return false
        return expireAt > System.currentTimeMillis()
    }

    companion object {
        private const val TAG = "ApprovalGatewayClient"
    }
}
