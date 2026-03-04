package com.elder.security.risk

import java.time.Instant

sealed class SecondaryChallengeResult {
    data class Approved(val method: String, val approvedAt: Instant = Instant.now()) : SecondaryChallengeResult()
    data class Rejected(val reason: String) : SecondaryChallengeResult()
}

interface FamilyVoiceConfirmationGateway {
    fun requestVoiceConfirmation(elderId: String, familyMemberId: String, scene: String): Boolean
}

class SecondaryConfirmationService(
    private val passphraseProvider: (String) -> String,
    private val familyVoiceGateway: FamilyVoiceConfirmationGateway,
    private val riskEventRecorder: RiskEventRecorder
) {
    fun verifyBeforeHighRiskPayment(
        elderId: String,
        inputPassphrase: String?,
        familyMemberId: String?
    ): SecondaryChallengeResult {
        val expected = passphraseProvider(elderId)
        if (!inputPassphrase.isNullOrBlank() && inputPassphrase == expected) {
            riskEventRecorder.record(
                RiskEventPayload(
                    elderId = elderId,
                    eventType = "SECONDARY_CHALLENGE",
                    level = RiskLevel.LOW,
                    score = 0,
                    details = "二次确认通过: 家庭约定口令"
                )
            )
            return SecondaryChallengeResult.Approved(method = "PASS_PHRASE")
        }

        if (!familyMemberId.isNullOrBlank() && familyVoiceGateway.requestVoiceConfirmation(elderId, familyMemberId, "HIGH_RISK_PAYMENT")) {
            riskEventRecorder.record(
                RiskEventPayload(
                    elderId = elderId,
                    eventType = "SECONDARY_CHALLENGE",
                    level = RiskLevel.LOW,
                    score = 0,
                    details = "二次确认通过: 家属语音确认"
                )
            )
            return SecondaryChallengeResult.Approved(method = "FAMILY_VOICE")
        }

        riskEventRecorder.record(
            RiskEventPayload(
                elderId = elderId,
                eventType = "SECONDARY_CHALLENGE",
                level = RiskLevel.HIGH,
                score = 80,
                details = "二次确认失败，阻断高风险支付"
            )
        )
        return SecondaryChallengeResult.Rejected("二次确认未通过")
    }
}
