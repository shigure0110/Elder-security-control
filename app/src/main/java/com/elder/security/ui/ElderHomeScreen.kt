package com.elder.security.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 老人端首页：大字号、高对比度、大按钮。
 * 展示今日消费总额、待审批提醒、安全风险计数。
 */
@Composable
fun ElderHomeScreen(
    elderName: String,
    todayRiskCount: Int,
    todaySpendingYuan: Double,
    pendingApprovalCount: Int,
    onCallFamilyClicked: () -> Unit,
    onCheckUpdateClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text       = "您好，$elderName",
            fontSize   = 34.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )

        SpendingCard(todaySpendingYuan)

        if (pendingApprovalCount > 0) {
            ApprovalWarningCard(pendingApprovalCount)
        }

        RiskNoticeCard(todayRiskCount)

        Button(
            onClick  = onCallFamilyClicked,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
        ) {
            Text("发送今日消费汇报给家属", fontSize = 22.sp, color = Color.White)
        }

        UpdateCard(onCheckUpdateClicked)
    }
}

@Composable
private fun SpendingCard(totalYuan: Double) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("今日消费", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = "¥ %.2f".format(totalYuan),
                fontSize   = 40.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF2E7D32)
            )
            Text("支付宝 + 微信支付合计", fontSize = 18.sp, color = Color(0xFF388E3C))
        }
    }
}

@Composable
private fun ApprovalWarningCard(count: Int) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = "⚠️  需要家属确认",
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text       = "发现 $count 项新安装的应用或好友申请，\n请联系家属审批后再继续使用。",
                fontSize   = 20.sp,
                lineHeight = 30.sp,
                color      = Color(0xFFBF360C)
            )
        }
    }
}

@Composable
private fun RiskNoticeCard(riskCount: Int) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("安全提醒", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("今天已拦截/提醒风险 $riskCount 次", fontSize = 22.sp, color = Color(0xFF374151))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = "遇到"转账到安全账户""验证码""退款"，\n请先联系家属确认，不要轻易操作。",
                fontSize   = 20.sp,
                lineHeight = 30.sp,
                color      = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun UpdateCard(onCheckUpdateClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("版本更新", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("建议连接 Wi-Fi 后更新到最新版本。", fontSize = 18.sp, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onCheckUpdateClicked, shape = RoundedCornerShape(18.dp)) {
                Text("检查并下载更新", fontSize = 20.sp)
            }
        }
    }
}
