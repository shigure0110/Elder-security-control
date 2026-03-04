package com.elder.security.risk

import java.time.Instant

enum class ChannelType {
    PHONE,
    SMS,
    NOTIFICATION,
    PAYMENT
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class KeywordHit(
    val keyword: String,
    val score: Int
)

data class TextRiskInput(
    val elderId: String,
    val content: String,
    val channel: ChannelType,
    val timestamp: Instant = Instant.now()
)

data class PaymentRiskInput(
    val elderId: String,
    val payerId: String,
    val payeeId: String,
    val payeeName: String,
    val amount: Long,
    val isKnownPayee: Boolean,
    val timestamp: Instant = Instant.now()
)

data class RiskDecision(
    val level: RiskLevel,
    val score: Int,
    val reasons: List<String>,
    val shouldStrongAlert: Boolean,
    val shouldNotifyFamilyGroup: Boolean,
    val eventType: String
)

data class RiskEventPayload(
    val elderId: String,
    val eventType: String,
    val level: RiskLevel,
    val score: Int,
    val details: String,
    val createdAt: Instant = Instant.now()
)

interface FamilyNotifier {
    fun pushToFamilyGroup(elderId: String, title: String, message: String)
}

interface RiskEventRecorder {
    fun record(payload: RiskEventPayload)
}

interface StrongAlerter {
    fun showStrongAlert(elderId: String, title: String, message: String)
}
