package com.elder.security.data

import androidx.room.TypeConverter
import com.elder.security.risk.RiskLevel

class RiskConverters {
    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = RiskLevel.valueOf(value)

    @TypeConverter
    fun fromRiskLevel(level: RiskLevel): String = level.name
}
