package com.elder.security.billing

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.regex.Pattern

/**
 * 通知采集：从微信支付/支付宝通知中提取金额、商户、时间。
 */
class NotificationCollectorService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return
        if (packageName !in SUPPORTED_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val mergedText = listOf(title, text, bigText).joinToString("\n").trim()
        if (mergedText.isBlank()) return

        val parsed = parsePaymentNotification(packageName, mergedText) ?: return
        serviceScope.launch {
            val repo = BillRepository(BillingDatabase.get(applicationContext).billDao())
            repo.upsert(parsed)
        }
    }

    private fun parsePaymentNotification(packageName: String, body: String): BillRecord? {
        val amount = AMOUNT_PATTERN.find(body)?.groupValues?.get(1)?.toDoubleOrNull() ?: return null
        val merchant = findMerchant(body)
        val source = when (packageName) {
            WECHAT_PACKAGE -> "wechat_notification"
            ALIPAY_PACKAGE -> "alipay_notification"
            else -> return null
        }

        return BillRecord(
            source = source,
            amount = amount,
            merchant = merchant,
            timestamp = System.currentTimeMillis(),
            rawText = body,
            confidence = if (merchant != UNKNOWN_MERCHANT) 0.92 else 0.78,
        )
    }

    private fun findMerchant(body: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                return matcher.group(1)?.trim().orEmpty().ifBlank { UNKNOWN_MERCHANT }
            }
        }
        return UNKNOWN_MERCHANT
    }

    companion object {
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val ALIPAY_PACKAGE = "com.eg.android.AlipayGphone"
        private val SUPPORTED_PACKAGES = setOf(WECHAT_PACKAGE, ALIPAY_PACKAGE)
        private const val UNKNOWN_MERCHANT = "未知商户"

        // 匹配￥12.34 或 12.34元
        private val AMOUNT_PATTERN = Regex("(?:￥|¥)?([0-9]+(?:\\.[0-9]{1,2})?)(?:元)?", RegexOption.IGNORE_CASE)

        private val MERCHANT_PATTERNS = listOf(
            Pattern.compile("(?:收款方|商户|付款给)[:：]\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("向([^\\n]+?)付款", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([^\\n]+?)收款", Pattern.CASE_INSENSITIVE),
        )
    }
}
