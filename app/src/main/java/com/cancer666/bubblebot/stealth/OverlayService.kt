package com.cancer666.bubblebot.stealth

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var answerView: TextView? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val trigger = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_TRIGGER) runPipeline()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(1, buildNotification())
        val filter = IntentFilter(ACTION_TRIGGER)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(trigger, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(trigger, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        runCatching { unregisterReceiver(trigger) }
        answerView?.let { runCatching { windowManager.removeView(it) } }
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, BubbleApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("BubbleBot stealth aktif")
            .setContentText("Long-press status bar untuk trigger")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun runPipeline() {
        scope.launch {
            showAnswer("…")
            val bitmap = RootCapture.capture().getOrElse {
                showAnswer("[capture error] ${it.message}")
                return@launch
            }
            val encoded = RootCapture.encode(bitmap, 70)
            bitmap.recycle()
            val answer = runCatching { AskClient.askImage(encoded) }
                .getOrElse { "[ai error] ${it.message}" }
            showAnswer(answer)
        }
    }

    private fun showAnswer(text: String) {
        if (answerView == null) {
            val textView = TextView(this).apply {
                setTextColor(0xFFFFFFFF.toInt())
                setBackgroundColor(0xCC000000.toInt())
                textSize = 13f
                setPadding(24, 24, 24, 24)
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                x = 24
                y = 240
            }
            windowManager.addView(textView, params)
            answerView = textView
        }
        answerView?.text = text
    }

    companion object {
        const val ACTION_TRIGGER = "com.cancer666.bubblebot.TRIGGER"
    }
}
