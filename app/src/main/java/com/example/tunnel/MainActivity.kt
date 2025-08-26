
package com.example.tunnel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.tunnel.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 可根据是否授权给出提示 */ }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(perm)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        binding.token.setText(prefs.getString("token", ""))

        binding.btnStart.setOnClickListener {
            ensureNotificationPermission()
            val token = binding.token.text.toString().trim()
            if (token.isEmpty()) {
                Toast.makeText(this, "请先粘贴 Tunnel Token", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putString("token", token).apply()
            val i = Intent(this, TunnelService::class.java)
            i.putExtra("token", token)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        }

        binding.btnStop.setOnClickListener {
            stopService(Intent(this, TunnelService::class.java))
        }
    }
}
