package com.elder.security.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * 下载并触发安装 APK 的简化更新管理器。
 */
class AppUpdateManager(
    private val context: Context
) {
    fun checkAndDownload(updateInfo: UpdateInfo, currentVersionCode: Long): Long? {
        if (updateInfo.versionCode <= currentVersionCode) return null

        val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
            .setTitle("老人安全守护更新")
            .setDescription("正在下载 ${updateInfo.versionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(false)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, updateInfo.fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    fun installDownloadedApk(fileName: String) {
        val apkFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val fileName: String = "elder-security-latest.apk"
)
