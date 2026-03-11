package com.elder.security.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RiskEvent::class, SpendingRecord::class, PendingApproval::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(RiskConverters::class, AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun riskEventDao(): RiskEventDao
    abstract fun spendingDao(): SpendingDao
    abstract fun approvalDao(): ApprovalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elder_security.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
