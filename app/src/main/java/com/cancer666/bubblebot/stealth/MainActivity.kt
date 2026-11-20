package com.cancer666.bubblebot.stealth

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cancer666.bubblebot.stealth.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        lifecycleScope.launch {
            runCatching {
                AskClient.ensureSignedIn()
                b.statusText.text = "terhubung · siap"
            }.onFailure {
                b.statusText.text = "gagal auth: ${it.message}"
            }
        }

        b.btnOverlayPerm.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        b.btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Aktifkan izin overlay dulu", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val i = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
            moveTaskToBack(true)
        }

        b.btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            b.statusText.text = "berhenti"
        }
    }
}
