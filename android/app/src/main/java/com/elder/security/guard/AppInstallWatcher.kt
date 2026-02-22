package com.elder.security.guard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.elder.security.approval.ApprovalGatewayClient
import com.elder.security.approval.EmergencyWhitelist
import com.elder.security.approval.RiskType

/**
 * 监听安装事件与安装器页面特征，发现高风险安装时触发审批。
 */
class AppInstallWatcher(
    private val approvalGatewayClient: ApprovalGatewayClient
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_PACKAGE_ADDED) return
        val packageName = intent.data?.schemeSpecificPart.orEmpty()
        if (packageName.isBlank()) return

        if (EmergencyWhitelist.isWhitelisted(packageName)) {
            Log.i(TAG, "package in emergency whitelist: $packageName")
            return
        }

        approvalGatewayClient.submitRisk(
            riskType = RiskType.APP_INSTALL,
            target = packageName,
            reason = "检测到新安装应用"
        )
    }

    fun onInstallerUiEvent(event: AccessibilityEvent) {
        val title = event.text?.joinToString(separator = " ").orEmpty()
        val installerSignals = listOf("安装", "继续安装", "安装未知应用", "允许来自此来源")
        val hit = installerSignals.any { title.contains(it) }
        if (!hit) return

        approvalGatewayClient.submitRisk(
            riskType = RiskType.APP_INSTALL,
            target = "installer-ui",
            reason = "检测到安装流程界面特征: $title"
        )
    }

    companion object {
        private const val TAG = "AppInstallWatcher"
    }
}
