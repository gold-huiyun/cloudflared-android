
package com.example.tunnel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TunnelService : Service() {
    private var process: Process? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("token") ?: return START_NOT_STICKY

        val binDir = applicationInfo.nativeLibraryDir
        val exe = "$binDir/libcloudflared.so"

        val f = File(exe)
        if (!f.exists()) {
            startForeground(1, buildNotification("cloudflared 不存在: $exe"))
            stopSelf(); return START_NOT_STICKY
        }
        if (!f.canExecute()) {
            startForeground(1, buildNotification("cloudflared 不可执行: $exe"))
            stopSelf(); return START_NOT_STICKY
        }

        // 先展示前台通知，避免 Android 14+ 类型检查抛异常时没有可见反馈
        startForeground(1, buildNotification("Cloudflared 正在连接…"))

        val logFile = File(filesDir, "cloudflared.log").absolutePath
        val cmd = listOf(
            exe,
            "tunnel", "run",
            "--token", token,
            "--no-autoupdate",
            "--edge-ip-version", "4",
            "--loglevel", "info",
            "--logfile", logFile
        )

        try {
            val pb = ProcessBuilder(cmd).redirectErrorStream(true)
            process = pb.start()

            Thread {
                val exit = try { process?.waitFor() ?: -1 } catch (t: Throwable) { -1 }
                val msg = "Cloudflared 退出，code=$exit；日志: $logFile"
                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(1, buildNotification(msg))
                stopSelf()
            }.start()
        } catch (t: Throwable) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1, buildNotification("启动失败: ${t.message}"))
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        process?.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        val channelId = "tunnel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Cloudflare Tunnel", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cloudflare Tunnel")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
    }
}
