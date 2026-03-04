package com.elder.security.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
 * iOS 风格偏好的老人端首页（大字号/高对比/大按钮）。
 */
@Composable
fun ElderHomeScreen(
    elderName: String,
    todayRiskCount: Int,
    onPayClicked: () -> Unit,
    onCallFamilyClicked: () -> Unit,
    onCheckUpdateClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "您好，$elderName",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = "今天已拦截/提醒风险 $todayRiskCount 次",
            fontSize = 22.sp,
            color = Color(0xFF374151)
        )

        RiskNoticeCard()
        ActionButtons(onPayClicked, onCallFamilyClicked)
        UpdateCard(onCheckUpdateClicked)
    }
}

@Composable
private fun RiskNoticeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("安全提醒", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "遇到“转账到安全账户”“验证码”请先联系家属确认。",
                fontSize = 22.sp,
                lineHeight = 30.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onPayClicked: () -> Unit,
    onCallFamilyClicked: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onPayClicked,
            modifier = Modifier
                .weight(1f)
                .height(68.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("发起支付", fontSize = 24.sp)
        }
        Button(
            onClick = onCallFamilyClicked,
            modifier = Modifier
                .weight(1f)
                .height(68.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("联系家属", fontSize = 24.sp)
        }
    }
}

@Composable
private fun UpdateCard(onCheckUpdateClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("版本更新", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("建议连接 Wi-Fi 后更新到最新版本。", fontSize = 20.sp, color = Color(0xFF1F2937))
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onCheckUpdateClicked, shape = RoundedCornerShape(18.dp)) {
                Text("检查并下载更新", fontSize = 22.sp)
            }
        }
    }
}
