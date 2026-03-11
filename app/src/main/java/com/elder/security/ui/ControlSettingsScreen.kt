package com.elder.security.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PlatformControlState(
    val platformName: String,
    var enabled: Boolean
)

/**
 * 风控设置页：
 * 1) 平台开关（支付宝/微信/抖音/拼多多/淘宝）
 * 2) 小额账单汇总规则
 * 3) 微信群发布目标配置
 */
@Composable
fun ControlSettingsScreen(
    elderName: String,
    onSave: (SettingsSnapshot) -> Unit
) {
    val platformStates = remember {
        mutableStateListOf(
            PlatformControlState("支付宝", true),
            PlatformControlState("微信", true),
            PlatformControlState("抖音", false),
            PlatformControlState("拼多多", false),
            PlatformControlState("淘宝", false)
        )
    }
    val reportEnabled = remember { mutableStateOf(true) }
    val smallBillThreshold = remember { mutableStateOf("200") }
    val reportTime = remember { mutableStateOf("20:30") }
    val wechatGroupName = remember { mutableStateOf("家庭守护群") }
    val wechatGroupId = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "${elderName} 的风控设置",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "按平台选择管控范围，并配置账单在微信群的汇报方式。",
            fontSize = 18.sp
        )

        Card(colors = CardDefaults.cardColors()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("平台管控", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                platformStates.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.platformName, fontSize = 20.sp)
                        Switch(
                            checked = item.enabled,
                            onCheckedChange = { checked -> platformStates[index] = item.copy(enabled = checked) }
                        )
                    }
                    if (index != platformStates.lastIndex) Divider()
                }
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("小额账单汇总", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开启每日汇报", fontSize = 20.sp)
                    Switch(checked = reportEnabled.value, onCheckedChange = { reportEnabled.value = it })
                }
                OutlinedTextField(
                    value = smallBillThreshold.value,
                    onValueChange = { smallBillThreshold.value = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("小额阈值（元，建议 200）") }
                )
                OutlinedTextField(
                    value = reportTime.value,
                    onValueChange = { reportTime.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("每日汇报时间（如 20:30）") }
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("微信群发布目标", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = wechatGroupName.value,
                    onValueChange = { wechatGroupName.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("目标群聊名称") }
                )
                OutlinedTextField(
                    value = wechatGroupId.value,
                    onValueChange = { wechatGroupId.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("目标群聊 ID（可选）") }
                )
                Text("提示：正式接入时建议绑定群聊唯一 ID，避免同名群聊误发。", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = {
                onSave(
                    SettingsSnapshot(
                        enabledPlatforms = platformStates.associate { it.platformName to it.enabled },
                        dailyReportEnabled = reportEnabled.value,
                        smallBillThresholdYuan = smallBillThreshold.value.toIntOrNull() ?: 200,
                        reportTime = reportTime.value,
                        wechatGroupName = wechatGroupName.value,
                        wechatGroupId = wechatGroupId.value.ifBlank { null }
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存设置", fontSize = 20.sp)
        }
    }
}

data class SettingsSnapshot(
    val enabledPlatforms: Map<String, Boolean>,
    val dailyReportEnabled: Boolean,
    val smallBillThresholdYuan: Int,
    val reportTime: String,
    val wechatGroupName: String,
    val wechatGroupId: String?
)
