package com.elder.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.elder.security.data.AppDatabase
import com.elder.security.data.SpendingRecord
import com.elder.security.ui.ElderHomeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MainActivity : ComponentActivity() {

    /** 收到 SpendingMonitorService 广播时刷新界面 */
    private val approvalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { loadAndRender() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAndRender()
        ContextCompat.registerReceiver(
            this,
            approvalReceiver,
            IntentFilter("com.elder.security.PENDING_APPROVAL"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()
        loadAndRender()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(approvalReceiver)
    }

    // ── 数据加载 ──────────────────────────────────────────────────────────────

    private fun loadAndRender() {
        lifecycleScope.launch {
            val db      = AppDatabase.getInstance(applicationContext)
            val elderId = "default_elder"
            val dayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
            val dayAgo   = Instant.now().epochSecond - 86_400L

            val records   = withContext(Dispatchers.IO) { db.spendingDao().getRecordsSince(elderId, dayStart) }
            val pending   = withContext(Dispatchers.IO) { db.approvalDao().getPending(elderId) }
            val riskCount = withContext(Dispatchers.IO) {
                db.riskEventDao().latestEvents(elderId, 200).count { it.createdAtEpoch >= dayAgo }
            }

            val todayYuan = records.sumOf { it.amountFen } / 100.0

            setContent {
                ElderHomeScreen(
                    elderName            = "奶奶",
                    todayRiskCount       = riskCount,
                    todaySpendingYuan    = todayYuan,
                    pendingApprovalCount = pending.size,
                    onCallFamilyClicked  = { shareToFamily(buildSummary(records, pending.size)) },
                    onCheckUpdateClicked = { /* 未来：版本检查 */ }
                )
            }
        }
    }

    // ── 分享到家庭群 ──────────────────────────────────────────────────────────

    private fun shareToFamily(text: String) {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                },
                "发送到家庭群"
            )
        )
    }

    private fun buildSummary(records: List<SpendingRecord>, pendingCount: Int): String {
        val total = records.sumOf { it.amountFen } / 100.0
        return buildString {
            appendLine("【今日消费汇报】")
            appendLine("共 ${records.size} 笔，合计 ¥%.2f".format(total))
            records.take(10).forEach { r ->
                val src = if (r.source == "ALIPAY") "支付宝" else "微信支付"
                appendLine("· $src ¥%.2f  ${r.merchant}".format(r.amountFen / 100.0))
            }
            if (pendingCount > 0) appendLine("⚠️ 待家属审批 $pendingCount 项")
        }
    }
}
