package com.elder.security.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RiskEvent::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RiskConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun riskEventDao(): RiskEventDao
}
