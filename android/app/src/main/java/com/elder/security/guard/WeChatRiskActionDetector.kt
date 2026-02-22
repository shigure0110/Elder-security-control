package com.elder.security.guard

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.elder.security.approval.ApprovalGatewayClient
import com.elder.security.approval.EmergencyWhitelist
import com.elder.security.approval.RiskType

/**
 * 微信加好友风险操作识别：命中关键页面文案后触发统一审批流程。
 */
class WeChatRiskActionDetector(
    private val approvalGatewayClient: ApprovalGatewayClient,
    private val blockingOverlayService: BlockingOverlayService
) {
    private val riskKeywords = listOf("添加朋友", "通过验证", "发送朋友验证", "新的朋友")

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString().orEmpty()
        if (packageName != WECHAT_PACKAGE) return

        val text = event.text?.joinToString(separator = " ").orEmpty()
        if (riskKeywords.none { text.contains(it) }) return

        if (EmergencyWhitelist.isWhitelisted(text)) {
            Log.i(TAG, "wechat target in emergency whitelist, skip approval")
            return
        }

        approvalGatewayClient.submitRisk(
            riskType = RiskType.WECHAT_ADD_FRIEND,
            target = text,
            reason = "检测到微信加好友高风险动作"
        )
        blockingOverlayService.showIfBlocked(RiskType.WECHAT_ADD_FRIEND, text)
    }

    companion object {
        private const val TAG = "WeChatRiskActionDetector"
        private const val WECHAT_PACKAGE = "com.tencent.mm"
    }
}
