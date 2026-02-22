package com.elder.security.billing

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 统一账单字段定义：source、amount、merchant、timestamp、rawText、confidence。
 */
@Entity(tableName = "bill_records")
data class BillRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "merchant") val merchant: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "raw_text") val rawText: String,
    @ColumnInfo(name = "confidence") val confidence: Double,
)

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BillRecord): Long

    @Query("SELECT * FROM bill_records WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun findByTimeRange(start: Long, end: Long): Flow<List<BillRecord>>

    @Query("SELECT * FROM bill_records WHERE timestamp BETWEEN :start AND :end ORDER BY amount DESC")
    suspend fun findByTimeRangeSync(start: Long, end: Long): List<BillRecord>
}

@Database(entities = [BillRecord::class], version = 1, exportSchema = false)
abstract class BillingDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var instance: BillingDatabase? = null

        fun get(context: Context): BillingDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BillingDatabase::class.java,
                    "billing.db",
                ).build().also { db ->
                    instance = db
                }
            }
        }
    }
}

class BillRepository(private val dao: BillDao) {
    suspend fun upsert(record: BillRecord): Long = dao.insert(record)

    fun observeDaily(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<BillRecord>> {
        return dao.findByTimeRange(startOfDayMillis, endOfDayMillis)
    }

    suspend fun getDaily(startOfDayMillis: Long, endOfDayMillis: Long): List<BillRecord> {
        return dao.findByTimeRangeSync(startOfDayMillis, endOfDayMillis)
    }
}
