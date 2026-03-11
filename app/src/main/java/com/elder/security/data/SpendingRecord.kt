package com.elder.security.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/** 每一笔通过支付宝/微信支付检测到的消费记录。amount_fen 存分（分/100 = 元）。 */
@Entity(tableName = "SpendingRecord")
data class SpendingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "elder_id")       val elderId: String,
    @ColumnInfo(name = "source")         val source: String,       // "ALIPAY" 或 "WECHAT_PAY"
    @ColumnInfo(name = "amount_fen")     val amountFen: Long,      // 金额（分）
    @ColumnInfo(name = "merchant")       val merchant: String,
    @ColumnInfo(name = "raw_text")       val rawText: String,
    @ColumnInfo(name = "created_at_epoch") val createdAtEpoch: Long
)

data class DailySpendingSummary(
    @ColumnInfo(name = "day_bucket") val dayBucket: String,
    @ColumnInfo(name = "total_fen")  val totalFen: Long,
    @ColumnInfo(name = "tx_count")   val txCount: Int
)

@Dao
interface SpendingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: SpendingRecord): Long

    @Query("""
        SELECT strftime('%Y-%m-%d', datetime(created_at_epoch, 'unixepoch', 'localtime')) AS day_bucket,
               SUM(amount_fen)  AS total_fen,
               COUNT(*)         AS tx_count
        FROM   SpendingRecord
        WHERE  elder_id = :elderId
        GROUP  BY day_bucket
        ORDER  BY day_bucket DESC
        LIMIT  :days
    """)
    suspend fun getDailySummary(elderId: String, days: Int = 30): List<DailySpendingSummary>

    @Query("""
        SELECT * FROM SpendingRecord
        WHERE  elder_id     = :elderId
          AND  created_at_epoch >= :fromEpoch
        ORDER  BY created_at_epoch DESC
    """)
    suspend fun getRecordsSince(elderId: String, fromEpoch: Long): List<SpendingRecord>
}
