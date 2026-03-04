package com.elder.security.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.elder.security.risk.RiskLevel

@Entity(tableName = "RiskEvent")
data class RiskEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "elder_id") val elderId: String,
    @ColumnInfo(name = "event_type") val eventType: String,
    @ColumnInfo(name = "risk_level") val riskLevel: RiskLevel,
    @ColumnInfo(name = "risk_score") val riskScore: Int,
    @ColumnInfo(name = "details") val details: String,
    @ColumnInfo(name = "created_at_epoch") val createdAtEpoch: Long
)

data class RiskTrendPoint(
    @ColumnInfo(name = "day_bucket") val dayBucket: String,
    @ColumnInfo(name = "total_events") val totalEvents: Int,
    @ColumnInfo(name = "high_risk_events") val highRiskEvents: Int
)

@Dao
interface RiskEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: RiskEvent)

    @Query(
        """
        SELECT strftime('%Y-%m-%d', datetime(created_at_epoch, 'unixepoch')) AS day_bucket,
               COUNT(*) AS total_events,
               SUM(CASE WHEN risk_level IN ('HIGH', 'CRITICAL') THEN 1 ELSE 0 END) AS high_risk_events
        FROM RiskEvent
        WHERE elder_id = :elderId
        GROUP BY day_bucket
        ORDER BY day_bucket DESC
        """
    )
    fun getTrend(elderId: String): List<RiskTrendPoint>

    @Query("SELECT * FROM RiskEvent WHERE elder_id = :elderId ORDER BY created_at_epoch DESC LIMIT :limit")
    fun latestEvents(elderId: String, limit: Int = 50): List<RiskEvent>
}
