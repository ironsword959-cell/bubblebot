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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            runCatching {
                AskClient.ensureSignedIn()
                binding.statusText.text = "terhubung · siap"
            }.onFailure {
                binding.statusText.text = "gagal auth: ${it.message}"
            }
        }

        binding.btnOverlayPerm.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
        }
        binding.btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Aktifkan izin overlay dulu", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
            moveTaskToBack(true)
        }
        binding.btnStop.setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
            binding.statusText.text = "berhenti"
        }
    }
}
