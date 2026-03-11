package com.elder.security.monitor

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.elder.security.data.AppDatabase
import com.elder.security.data.ApprovalType
import com.elder.security.data.PendingApproval
import com.elder.security.data.SpendingRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.regex.Pattern

/**
 * 后台运行的通知监听服务，同时承担三项职责：
 *  1. 捕获支付宝 / 微信支付通知，解析金额写入 SpendingRecord。
 *  2. 捕获微信好友申请通知，写入 PendingApproval 等待家属确认。
 *  3. 动态注册 BroadcastReceiver 监听新应用安装，写入 PendingApproval。
 *
 * 使用前需在系统"通知使用权限"页面授权本服务。
 */
class SpendingMonitorService : NotificationListenerService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val elderId = "default_elder"

    private var receiverRegistered = false
    private val installReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
            // 跳过应用更新（升级不算新安装）
            if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
            val pkgName = intent.data?.schemeSpecificPart ?: return
            val appName = runCatching {
                ctx.packageManager.getApplicationLabel(
                    ctx.packageManager.getApplicationInfo(pkgName, 0)
                ).toString()
            }.getOrDefault(pkgName)
            recordApprovalNeeded(ApprovalType.APP_INSTALL, appName, "包名: $pkgName")
        }
    }

    companion object {
        private const val PKG_ALIPAY = "com.eg.android.AlipayGphone"
        private const val PKG_WECHAT = "com.tencent.mm"

        // 匹配 ¥ 或 ￥ 后的金额
        private val AMOUNT_RE = Pattern.compile("""[¥￥](\d+(?:\.\d{1,2})?)""")

        private val ALIPAY_KEYWORDS = setOf("付款成功", "收款到账", "转账成功", "扣款成功", "支出")
        private val WECHAT_PAY_KEYWORDS = setOf("微信支付", "收款成功", "付款成功", "转账成功")

        // 微信好友申请关键词
        private val WECHAT_FRIEND_RE = Regex("请求添加你为朋友|向你发送了朋友验证|好友申请|添加你为好友")
    }

    // ── 服务生命周期 ──────────────────────────────────────────────────────────

    override fun onListenerConnected() {
        super.onListenerConnected()
        if (!receiverRegistered) {
            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply { addDataScheme("package") }
            registerReceiver(installReceiver, filter)
            receiverRegistered = true
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (receiverRegistered) {
            runCatching { unregisterReceiver(installReceiver) }
            receiverRegistered = false
        }
    }

    // ── 通知处理 ─────────────────────────────────────────────────────────────

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg    = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return
        val title  = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text   = extras.getString(Notification.EXTRA_TEXT).orEmpty()
        val full   = "$title $text"

        when (pkg) {
            PKG_ALIPAY -> handlePayment(full, "ALIPAY", ALIPAY_KEYWORDS)
            PKG_WECHAT -> {
                handlePayment(full, "WECHAT_PAY", WECHAT_PAY_KEYWORDS)
                if (WECHAT_FRIEND_RE.containsMatchIn(full)) {
                    // 提取好友昵称：取"请求"/"向你"之前的部分
                    val name = full
                        .substringBefore("请求")
                        .substringBefore("向你")
                        .trim()
                        .take(20)
                        .ifEmpty { "未知用户" }
                    recordApprovalNeeded(ApprovalType.WECHAT_FRIEND, name, full.take(200))
                }
            }
        }
    }

    // ── 私有辅助 ─────────────────────────────────────────────────────────────

    private fun handlePayment(text: String, source: String, keywords: Set<String>) {
        if (keywords.none { text.contains(it) }) return
        val m = AMOUNT_RE.matcher(text)
        if (!m.find()) return
        val amountFen = (m.group(1).toDoubleOrNull() ?: return).times(100).toLong()
        // 取金额符号前的文字作为简单商户名
        val merchant = text
            .substringBefore("¥")
            .substringBefore("￥")
            .trim()
            .takeLast(20)
            .ifEmpty { "未知商户" }
        scope.launch {
            AppDatabase.getInstance(applicationContext).spendingDao().insert(
                SpendingRecord(
                    elderId        = elderId,
                    source         = source,
                    amountFen      = amountFen,
                    merchant       = merchant,
                    rawText        = text.take(200),
                    createdAtEpoch = Instant.now().epochSecond
                )
            )
        }
    }

    private fun recordApprovalNeeded(type: ApprovalType, subject: String, detail: String) {
        scope.launch {
            AppDatabase.getInstance(applicationContext).approvalDao().insert(
                PendingApproval(
                    elderId        = elderId,
                    approvalType   = type,
                    subject        = subject,
                    detail         = detail,
                    createdAtEpoch = Instant.now().epochSecond
                )
            )
            // 通知 MainActivity 刷新界面
            sendBroadcast(
                Intent("com.elder.security.PENDING_APPROVAL").setPackage(packageName)
            )
        }
    }
}
