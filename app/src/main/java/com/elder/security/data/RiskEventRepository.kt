package com.elder.security.data

import com.elder.security.risk.RiskEventPayload
import com.elder.security.risk.RiskEventRecorder

class RiskEventRepository(
    private val dao: RiskEventDao
) : RiskEventRecorder {
    override fun record(payload: RiskEventPayload) {
        dao.insert(
            RiskEvent(
                elderId = payload.elderId,
                eventType = payload.eventType,
                riskLevel = payload.level,
                riskScore = payload.score,
                details = payload.details,
                createdAtEpoch = payload.createdAt.epochSecond
            )
        )
    }

    fun trendForFamily(elderId: String): List<RiskTrendPoint> = dao.getTrend(elderId)

    fun latestForFamily(elderId: String, limit: Int = 50): List<RiskEvent> = dao.latestEvents(elderId, limit)
}
