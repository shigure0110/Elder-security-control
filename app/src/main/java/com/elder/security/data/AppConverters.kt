package com.elder.security.data

import androidx.room.TypeConverter

/** Room TypeConverters for ApprovalType and ApprovalStatus enums. */
class AppConverters {
    @TypeConverter fun toApprovalType(v: String): ApprovalType   = ApprovalType.valueOf(v)
    @TypeConverter fun fromApprovalType(v: ApprovalType): String  = v.name
    @TypeConverter fun toApprovalStatus(v: String): ApprovalStatus = ApprovalStatus.valueOf(v)
    @TypeConverter fun fromApprovalStatus(v: ApprovalStatus): String = v.name
}
