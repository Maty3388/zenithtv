package com.google.zenithtv.activities

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.zenithtv.R
import com.google.zenithtv.fragments.MainFragment
import com.google.zenithtv.services.ApiService
import com.google.zenithtv.utils.AutoUpdater
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private var mainFragment: MainFragment? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainFragment = MainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, mainFragment!!)
            .commit()
        checkUpdate()
    }

    private fun checkUpdate() {
        scope.launch {
            try {
                val ver = withContext(Dispatchers.IO) { ApiService.getVersion() }
                if (ver != null) AutoUpdater.check(this@MainActivity, "1.0.0", ver)
            } catch (_: Exception) {}
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Salir").setMessage("¿Querés salir de Zenith TV?")
                .setPositiveButton("Salir") { _,_ -> finish() }
                .setNegativeButton("Cancelar", null).show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
