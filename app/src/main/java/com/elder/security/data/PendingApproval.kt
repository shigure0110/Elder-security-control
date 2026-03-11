package com.elder.security.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

enum class ApprovalType { APP_INSTALL, WECHAT_FRIEND }
enum class ApprovalStatus { PENDING, APPROVED, REJECTED }

/**
 * 需要家属审批的事项：新安装应用 或 微信好友申请。
 * status 默认 PENDING，家属确认后更新为 APPROVED / REJECTED。
 */
@Entity(tableName = "PendingApproval")
data class PendingApproval(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "elder_id")          val elderId: String,
    @ColumnInfo(name = "approval_type")     val approvalType: ApprovalType,
    @ColumnInfo(name = "subject")           val subject: String,   // 应用名 或 好友昵称
    @ColumnInfo(name = "detail")            val detail: String,
    @ColumnInfo(name = "status")            val status: ApprovalStatus = ApprovalStatus.PENDING,
    @ColumnInfo(name = "created_at_epoch")  val createdAtEpoch: Long,
    @ColumnInfo(name = "resolved_at_epoch") val resolvedAtEpoch: Long? = null
)

@Dao
interface ApprovalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(approval: PendingApproval): Long

    @Update
    suspend fun update(approval: PendingApproval)

    @Query("SELECT * FROM PendingApproval WHERE elder_id = :elderId AND status = 'PENDING' ORDER BY created_at_epoch DESC")
    suspend fun getPending(elderId: String): List<PendingApproval>

    @Query("SELECT * FROM PendingApproval WHERE elder_id = :elderId ORDER BY created_at_epoch DESC LIMIT :limit")
    suspend fun getRecent(elderId: String, limit: Int = 20): List<PendingApproval>
}
