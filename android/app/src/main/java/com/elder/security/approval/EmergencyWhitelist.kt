package com.elder.security.approval

object EmergencyWhitelist {
    private val defaultAllowedTargets = setOf(
        "120", // 急救电话
        "医院", // 医院公众号/小程序关键字
        "家人", // 家庭联系人
        "社区服务" // 社区服务号
    )

    fun isWhitelisted(target: String): Boolean {
        return defaultAllowedTargets.any { keyword ->
            target.contains(keyword, ignoreCase = true)
        }
    }
}
