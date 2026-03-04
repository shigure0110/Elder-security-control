package com.elder.security.orchestrator

import com.elder.security.risk.PaymentRiskInput
import com.elder.security.risk.RiskLevel
import com.elder.security.risk.RiskRuleEngine
import com.elder.security.risk.SecondaryChallengeResult
import com.elder.security.risk.SecondaryConfirmationService

sealed class PaymentDecision {
    data class Allowed(val reason: String) : PaymentDecision()
    data class Blocked(val reason: String) : PaymentDecision()
}

/**
 * 支付风控编排：
 * 1) 先跑支付风险规则
 * 2) 高风险时触发二次确认挑战
 * 3) 未通过则阻断支付
 */
class PaymentProtectionOrchestrator(
    private val riskRuleEngine: RiskRuleEngine,
    private val secondaryConfirmationService: SecondaryConfirmationService
) {
    fun evaluateAndAuthorize(
        paymentInput: PaymentRiskInput,
        inputPassphrase: String?,
        familyMemberId: String?
    ): PaymentDecision {
        val decision = riskRuleEngine.evaluatePaymentRisk(paymentInput)
        if (decision.level < RiskLevel.HIGH) {
            return PaymentDecision.Allowed("风险等级${decision.level}，无需二次确认")
        }

        return when (
            secondaryConfirmationService.verifyBeforeHighRiskPayment(
                elderId = paymentInput.elderId,
                inputPassphrase = inputPassphrase,
                familyMemberId = familyMemberId
            )
        ) {
            is SecondaryChallengeResult.Approved -> PaymentDecision.Allowed("高风险支付已通过二次确认")
            is SecondaryChallengeResult.Rejected -> PaymentDecision.Blocked("高风险支付未通过二次确认，已阻断")
        }
    }
}
