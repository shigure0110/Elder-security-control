package com.elder.security.guard

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.elder.security.approval.ApprovalGatewayClient
import com.elder.security.approval.RiskType

/**
 * 风险页面阻断层：在未审批时展示不可关闭遮罩，并提供联系家属入口。
 */
class BlockingOverlayService(
    private val approvalGatewayClient: ApprovalGatewayClient
) : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    fun showIfBlocked(riskType: RiskType, contextText: String) {
        if (approvalGatewayClient.isTemporarilyUnlocked(riskType)) {
            hideOverlay()
            return
        }
        if (overlayView != null) return

        val view = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, null)
        view.findViewById<TextView>(android.R.id.text1).text = "当前操作需要家属审批"
        view.findViewById<TextView>(android.R.id.text2).text = contextText

        val contactButton = Button(this).apply {
            text = "联系家属"
            setOnClickListener {
                startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(view, params)
        windowManager.addView(contactButton, params)
        overlayView = view
    }

    fun hideOverlay() {
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }
}
