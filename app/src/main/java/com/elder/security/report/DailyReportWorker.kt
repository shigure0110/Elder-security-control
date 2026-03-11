package com.elder.security.report

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elder.security.data.AppDatabase
import com.elder.security.data.SpendingRecord
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * 每日定时汇报工人（WorkManager）。
 *
 * 每隔 24 小时执行一次，统计当天的支付宝 / 微信支付消费并弹出通知。
 * 通知带"分享到家庭群"操作按钮，点击后通过系统分享菜单发送给家属微信群。
 */
class DailyReportWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val CHANNEL_ID = "daily_report"
        private const val NOTIF_ID   = 1001
        private const val WORK_TAG   = "daily_spending_report"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<DailyReportWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_TAG, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }

    override suspend fun doWork(): Result {
        val db       = AppDatabase.getInstance(applicationContext)
        val elderId  = "default_elder"
        val today    = LocalDate.now()
        val dayStart = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()

        val records  = db.spendingDao().getRecordsSince(elderId, dayStart)
        val pending  = db.approvalDao().getPending(elderId)
        val totalYuan = records.sumOf { it.amountFen } / 100.0

        val summary = buildSummaryText(today.toString(), records, pending.size, totalYuan)
        showNotification(summary, records.size, totalYuan, pending.size)
        return Result.success()
    }

    private fun buildSummaryText(
        date: String,
        records: List<SpendingRecord>,
        pendingCount: Int,
        totalYuan: Double
    ) = buildString {
        appendLine("【今日消费汇报 $date】")
        appendLine("共 ${records.size} 笔，合计 ¥%.2f".format(totalYuan))

        val alipayYuan  = records.filter { it.source == "ALIPAY"     }.sumOf { it.amountFen } / 100.0
        val wechatYuan  = records.filter { it.source == "WECHAT_PAY" }.sumOf { it.amountFen } / 100.0
        if (alipayYuan > 0) appendLine("  支付宝:  ¥%.2f".format(alipayYuan))
        if (wechatYuan > 0) appendLine("  微信支付: ¥%.2f".format(wechatYuan))

        if (records.isNotEmpty()) {
            appendLine()
            records.take(10).forEach { r ->
                val src = if (r.source == "ALIPAY") "支付宝" else "微信"
                appendLine("· $src ¥%.2f  ${r.merchant}".format(r.amountFen / 100.0))
            }
        }
        if (pendingCount > 0) {
            appendLine()
            appendLine("⚠️ 待家属审批 $pendingCount 项（新安装应用 / 好友申请）")
        }
    }

    private fun showNotification(summary: String, txCount: Int, totalYuan: Double, pendingCount: Int) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "每日消费汇报", NotificationManager.IMPORTANCE_DEFAULT)
        )

        val shareIntent = PendingIntent.getActivity(
            applicationContext, 0,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, summary)
                },
                "分享到家庭群"
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (pendingCount > 0)
            "另有 $pendingCount 项待家属审批"
        else
            "今日消费已汇总，点击可分享给家属群"

        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("今日消费 $txCount 笔 / ¥%.2f".format(totalYuan))
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(summary))
            .addAction(android.R.drawable.ic_menu_share, "分享到家庭群", shareIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIF_ID, notif)
    }
}
