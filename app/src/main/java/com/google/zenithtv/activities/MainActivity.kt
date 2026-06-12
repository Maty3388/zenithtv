package com.google.zenithtv.activities

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zenithtv.R
import com.google.zenithtv.databinding.ActivityMainBinding
import com.google.zenithtv.fragments.MainFragment
import com.google.zenithtv.services.ApiService
import com.google.zenithtv.utils.AutoUpdater
import com.google.zenithtv.utils.Prefs
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mainFragment: MainFragment? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var sidebarVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainFragment = MainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, mainFragment!!)
            .commit()

        setupSidebar()
        showSidebar()
        checkUpdate()
    }

    private fun setupSidebar() {
        binding.btnTv.setOnClickListener { mainFragment?.filterCategory(null) }
        binding.btnPeliculas.setOnClickListener { mainFragment?.filterCategory("CINE") }
        binding.btnSeries.setOnClickListener { mainFragment?.filterCategory("SERIES") }
        binding.btnAdultos.setOnClickListener { mainFragment?.filterCategory("ADULTOS") }
        binding.btnClearCache.setOnClickListener {
            cacheDir.deleteRecursively()
            android.widget.Toast.makeText(this, "Caché borrado", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.btnLogout.setOnClickListener {
            Prefs.logout(this)
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun showSidebar() {
        sidebarVisible = true
        binding.sidebar.visibility = View.VISIBLE
        binding.sidebarBorder.visibility = View.VISIBLE
        binding.btnTv.requestFocus()
    }

    private fun hideSidebar() {
        sidebarVisible = false
        binding.sidebar.visibility = View.GONE
        binding.sidebarBorder.visibility = View.GONE
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
        if (keyCode == KeyEvent.KEYCODE_MENU) { showSidebar(); return true }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
