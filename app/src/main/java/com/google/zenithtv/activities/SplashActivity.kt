package com.google.zenithtv.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zenithtv.services.ApiService
import com.google.zenithtv.utils.AutoUpdater
import com.google.zenithtv.utils.Prefs
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scope.launch {
            delay(1200)
            val token = Prefs.getToken(this@SplashActivity)
            if (token.isNotEmpty()) {
                ApiService.token = token
                try {
                    val ver = withContext(Dispatchers.IO) { ApiService.getVersion() }
                    if (ver != null) AutoUpdater.check(this@SplashActivity, "1.0.0", ver)
                } catch (_: Exception) {}
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
