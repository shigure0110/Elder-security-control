package com.elder.security

import android.app.Application
import com.elder.security.report.DailyReportWorker

class ElderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 每天定时触发消费汇报
        DailyReportWorker.schedule(this)
    }
}
