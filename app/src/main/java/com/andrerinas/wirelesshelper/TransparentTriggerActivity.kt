package com.andrerinas.wirelesshelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.andrerinas.wirelesshelper.strategy.BaseStrategy

class TransparentTriggerActivity : AppCompatActivity() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BaseStrategy.ACTION_TRIGGER_INTENT) {
                val targetIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra("intent", Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra("intent")
                }
                
                targetIntent?.let {
                    startActivity(it)
                }
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Invisible activity
        window.setBackgroundDrawableResource(android.R.color.transparent)
        
        val serviceIntent = Intent(this, WirelessHelperService::class.java).apply {
            action = WirelessHelperService.ACTION_START
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        ContextCompat.registerReceiver(this, receiver, IntentFilter(BaseStrategy.ACTION_TRIGGER_INTENT), ContextCompat.RECEIVER_NOT_EXPORTED)
        
        // Safety timeout: if nothing happens in 20s, close
        android.os.Handler(mainLooper).postDelayed({
            if (!isFinishing) finish()
        }, 20000)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) {}
    }
}
