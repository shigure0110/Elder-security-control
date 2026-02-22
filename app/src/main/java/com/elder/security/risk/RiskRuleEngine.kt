package com.elder.security.risk

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * 本地关键词与规则引擎，覆盖电话、短信、通知文本，并包含支付复合风险规则。
 */
class RiskRuleEngine(
    private val familyNotifier: FamilyNotifier,
    private val riskEventRecorder: RiskEventRecorder,
    private val strongAlerter: StrongAlerter,
    private val highTransferThreshold: Long = 50_000L,
    private val repeatedWindowMinutes: Long = 10,
    private val repeatedCountThreshold: Int = 3,
    private val compositeRiskThreshold: Int = 100
) {
    private val textKeywords: Map<ChannelType, Map<String, Int>> = mapOf(
        ChannelType.PHONE to mapOf(
            "安全账户" to 60,
            "公检法" to 45,
            "验证码" to 35,
            "远程控制" to 45
        ),
        ChannelType.SMS to mapOf(
            "点击链接" to 30,
            "银行卡冻结" to 55,
            "验证码" to 30,
            "立即转账" to 45
        ),
        ChannelType.NOTIFICATION to mapOf(
            "退款失败" to 35,
            "账户异常" to 40,
            "补贴领取" to 30,
            "中奖" to 35
        )
    )

    private val payeePaymentHistory = ConcurrentHashMap<String, MutableList<Instant>>()

    fun evaluateTextRisk(input: TextRiskInput): RiskDecision {
        val channelKeywords = textKeywords[input.channel].orEmpty()
        val hits = mutableListOf<KeywordHit>()
        channelKeywords.forEach { (keyword, score) ->
            if (input.content.contains(keyword, ignoreCase = true)) {
                hits += KeywordHit(keyword, score)
            }
        }

        val totalScore = hits.sumOf { it.score }
        val level = scoreToLevel(totalScore)
        val reasons = hits.map { "命中关键词: ${it.keyword}(+${it.score})" }

        val decision = RiskDecision(
            level = level,
            score = totalScore,
            reasons = reasons,
            shouldStrongAlert = level >= RiskLevel.HIGH,
            shouldNotifyFamilyGroup = level >= RiskLevel.HIGH,
            eventType = "TEXT_${input.channel.name}"
        )

        persistAndNotify(
            elderId = input.elderId,
            decision = decision,
            detail = "channel=${input.channel}, content=${input.content.take(120)}"
        )
        return decision
    }

    /**
     * 复合规则：高额转账 + 陌生收款方 + 短时间重复支付。
     */
    fun evaluatePaymentRisk(input: PaymentRiskInput): RiskDecision {
        val reasons = mutableListOf<String>()
        var score = 0

        if (input.amount >= highTransferThreshold) {
            score += 45
            reasons += "高额转账(+45)"
        }

        if (!input.isKnownPayee) {
            score += 30
            reasons += "陌生收款方(+30)"
        }

        val repeatedCount = updateAndCountRecentPayments(input)
        if (repeatedCount >= repeatedCountThreshold) {
            score += 35
            reasons += "短时间重复支付${repeatedCount}次(+35)"
        }

        if (input.amount >= highTransferThreshold && !input.isKnownPayee && repeatedCount >= repeatedCountThreshold) {
            score += 30
            reasons += "触发复合规则加权(+30)"
        }

        val level = scoreToLevel(score)
        val hitCompositeThreshold = score >= compositeRiskThreshold
        val decision = RiskDecision(
            level = level,
            score = score,
            reasons = reasons,
            shouldStrongAlert = hitCompositeThreshold,
            shouldNotifyFamilyGroup = hitCompositeThreshold,
            eventType = "PAYMENT"
        )

        persistAndNotify(
            elderId = input.elderId,
            decision = decision,
            detail = "payer=${input.payerId}, payee=${input.payeeName}, amount=${input.amount}, knownPayee=${input.isKnownPayee}"
        )

        return decision
    }

    private fun persistAndNotify(elderId: String, decision: RiskDecision, detail: String) {
        riskEventRecorder.record(
            RiskEventPayload(
                elderId = elderId,
                eventType = decision.eventType,
                level = decision.level,
                score = decision.score,
                details = "reasons=${decision.reasons.joinToString("; ")}; $detail"
            )
        )

        if (decision.shouldStrongAlert) {
            strongAlerter.showStrongAlert(
                elderId,
                "高风险行为提醒",
                "检测到${decision.eventType}风险，分值${decision.score}。请立即核验。"
            )
        }

        if (decision.shouldNotifyFamilyGroup) {
            familyNotifier.pushToFamilyGroup(
                elderId,
                "家属群风险通知",
                "检测到${decision.eventType}风险，等级=${decision.level}，原因=${decision.reasons.joinToString()}"
            )
        }
    }

    private fun updateAndCountRecentPayments(input: PaymentRiskInput): Int {
        val key = "${input.elderId}:${input.payeeId}"
        val history = payeePaymentHistory.computeIfAbsent(key) { mutableListOf() }
        val windowStart = input.timestamp.minus(Duration.ofMinutes(repeatedWindowMinutes))

        synchronized(history) {
            history.removeIf { it.isBefore(windowStart) }
            history += input.timestamp
            return history.size
        }
    }

    private fun scoreToLevel(score: Int): RiskLevel = when {
        score >= 100 -> RiskLevel.CRITICAL
        score >= 70 -> RiskLevel.HIGH
        score >= 40 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
}
