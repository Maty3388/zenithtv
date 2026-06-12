package com.google.zenithtv.activities

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
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
    private var sidebarExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainFragment = MainFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, mainFragment!!)
            .commit()

        setupSidebar()
        checkUpdate()
    }

    private fun setupSidebar() {
        // Íconos colapsados - al clickear expanden el sidebar
        binding.btnTvIcon.setOnClickListener { expandSidebar() }
        binding.btnPeliculasIcon.setOnClickListener { expandSidebar() }
        binding.btnSeriesIcon.setOnClickListener { expandSidebar() }
        binding.btnAdultosIcon.setOnClickListener { expandSidebar() }
        binding.btnClearCacheIcon.setOnClickListener { expandSidebar() }
        binding.btnLogoutIcon.setOnClickListener { expandSidebar() }

        // Botones expandidos
        binding.btnTv.setOnClickListener {
            collapseSidebar()
            mainFragment?.filterCategory(null)
        }
        binding.btnPeliculas.setOnClickListener {
            collapseSidebar()
            mainFragment?.filterCategory("CINE")
        }
        binding.btnSeries.setOnClickListener {
            collapseSidebar()
            mainFragment?.filterCategory("SERIES")
        }
        binding.btnAdultos.setOnClickListener {
            collapseSidebar()
            mainFragment?.filterCategory("ADULTOS")
        }
        binding.btnClearCache.setOnClickListener {
            cacheDir.deleteRecursively()
            collapseSidebar()
            Toast.makeText(this, "Caché borrado", Toast.LENGTH_SHORT).show()
        }
        binding.btnLogout.setOnClickListener {
            Prefs.logout(this)
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun expandSidebar() {
        sidebarExpanded = true
        binding.sidebarCollapsed.visibility = View.GONE
        binding.sidebarExpanded.visibility = View.VISIBLE
        binding.btnTv.requestFocus()
        
        // Navegación entre botones del sidebar
        val sidebarBtns = listOf(binding.btnTv, binding.btnPeliculas, binding.btnSeries, binding.btnAdultos, binding.btnClearCache, binding.btnLogout)
        sidebarBtns.forEachIndexed { i, btn ->
            btn.setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                            sidebarBtns.getOrNull(i + 1)?.requestFocus(); true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                            sidebarBtns.getOrNull(i - 1)?.requestFocus(); true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            collapseSidebar(); true
                        }
                        else -> false
                    }
                } else false
            }
        }
    }

    private fun collapseSidebar() {
        sidebarExpanded = false
        binding.sidebarExpanded.visibility = View.GONE
        binding.sidebarCollapsed.visibility = View.VISIBLE
    }

    private fun checkUpdate() {
        scope.launch {
            try {
                val ver = withContext(Dispatchers.IO) { ApiService.getVersion() }
                if (ver != null) AutoUpdater.check(this@MainActivity, "1.0.4", ver)
            } catch (_: Exception) {}
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (sidebarExpanded) { collapseSidebar(); return true }
            android.app.AlertDialog.Builder(this)
                .setTitle("Salir").setMessage("¿Querés salir de Zenith TV?")
                .setPositiveButton("Salir") { _,_ -> finish() }
                .setNegativeButton("Cancelar", null).show()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && !sidebarExpanded) {
            expandSidebar(); return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}
