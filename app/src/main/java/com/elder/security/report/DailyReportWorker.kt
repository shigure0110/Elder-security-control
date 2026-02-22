package com.elder.security.report

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elder.security.billing.BillRepository
import com.elder.security.billing.BillingDatabase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * 每日汇总：今日总支出、前3大类、异常单笔。
 */
class DailyReportWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val repo = BillRepository(BillingDatabase.get(applicationContext).billDao())
        val bills = repo.getDaily(start, end)

        val totalExpense = bills.sumOf { it.amount }
        val top3 = bills
            .groupBy { categorize(it.merchant) }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(3)

        val avg = bills.map { it.amount }.average().takeIf { !it.isNaN() } ?: 0.0
        val anomaly = bills.firstOrNull { it.amount > avg * 2 && it.amount > 100.0 }

        val report = buildString {
            append("[日账单汇总] ${today}\n")
            append("今日总支出：%.2f\n".format(totalExpense))
            append("前3大类：")
            append(top3.joinToString { "${it.first} %.2f".format(it.second) })
            append("\n")
            append("异常单笔：")
            if (anomaly == null) {
                append("无")
            } else {
                append("${anomaly.merchant} %.2f @ ${formatTime(anomaly.timestamp)}".format(anomaly.amount))
            }
        }

        // 这里可将报告通过 API 发往家庭后端（server/report-dispatcher）。
        println(report)
        return Result.success()
    }

    private fun categorize(merchant: String): String {
        return when {
            merchant.contains("超市") || merchant.contains("便利") -> "日用"
            merchant.contains("医院") || merchant.contains("药") -> "医疗"
            merchant.contains("滴滴") || merchant.contains("地铁") -> "出行"
            else -> "其他"
        }
    }

    private fun formatTime(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalTime().toString()
    }

    companion object {
        private const val WORK_NAME = "daily_bill_report"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyReportWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(10, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
