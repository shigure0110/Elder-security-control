package com.elder.security.approval

import java.time.Instant

enum class RiskType {
    APP_INSTALL,
    WECHAT_ADD_FRIEND,
    UNKNOWN
}

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

data class ApprovalRequest(
    val requestId: String,
    val deviceId: String,
    val riskType: RiskType,
    val target: String,
    val reason: String,
    val createdAt: Instant = Instant.now(),
    val timeoutSeconds: Long = 180
)

data class ApprovalDecision(
    val requestId: String,
    val status: ApprovalStatus,
    val approvedBy: List<String>,
    val decidedAt: Instant = Instant.now(),
    val unlockWindowSeconds: Long = 180
)
