package com.elder.security.billing

import android.view.accessibility.AccessibilityNodeInfo

/**
 * 账单补录：在微信/支付宝账单页扫描可见节点，通知缺失时补全账单。
 */
class AccessibilityBillParser {

    fun parseVisibleNodes(sourceApp: String, root: AccessibilityNodeInfo?): List<BillRecord> {
        if (root == null) return emptyList()
        val texts = mutableListOf<String>()
        walk(root, texts)

        val merged = texts.joinToString("\n")
        if (merged.isBlank()) return emptyList()

        val lines = merged.lines().map { it.trim() }.filter { it.isNotBlank() }
        val amountRegex = Regex("([0-9]+(?:\\.[0-9]{1,2})?)")

        return lines.mapNotNull { line ->
            val amount = amountRegex.find(line)?.groupValues?.get(1)?.toDoubleOrNull() ?: return@mapNotNull null
            val merchant = line.substringBefore(" ").ifBlank { "未知商户" }
            BillRecord(
                source = "${sourceApp}_accessibility",
                amount = amount,
                merchant = merchant,
                timestamp = System.currentTimeMillis(),
                rawText = line,
                confidence = 0.65,
            )
        }
    }

    private fun walk(node: AccessibilityNodeInfo, collector: MutableList<String>) {
        node.text?.toString()?.let(collector::add)
        node.contentDescription?.toString()?.let(collector::add)
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            walk(child, collector)
        }
    }
}
