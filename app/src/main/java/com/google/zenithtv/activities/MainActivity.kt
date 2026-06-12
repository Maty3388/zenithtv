package com.google.zenithtv.activities

import android.os.Bundle
import android.view.KeyEvent
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
        // TV - todos
        binding.btnTv.setOnClickListener {
            setActiveBtn(0)
            mainFragment?.filterCategory(null)
        }
        // Películas
        binding.btnPeliculas.setOnClickListener {
            setActiveBtn(1)
            mainFragment?.filterCategory("CINE")
        }
        // Series
        binding.btnSeries.setOnClickListener {
            setActiveBtn(2)
            mainFragment?.filterCategory("SERIES")
        }
        // Adultos
        binding.btnAdultos.setOnClickListener {
            setActiveBtn(3)
            mainFragment?.filterCategory("ADULTOS")
        }
        // Borrar caché
        binding.btnClearCache.setOnClickListener {
            cacheDir.deleteRecursively()
            Toast.makeText(this, "Caché borrado", Toast.LENGTH_SHORT).show()
        }
        // Cerrar sesión
        binding.btnLogout.setOnClickListener {
            Prefs.logout(this)
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        setActiveBtn(0)
    }

    private fun setActiveBtn(idx: Int) {
        val cyan = getColor(R.color.primary)
        val hint = getColor(R.color.text_hint)
        val activeBg = getColor(R.color.surface2)
        val normalBg = getColor(R.color.surface)
        listOf(binding.btnTv, binding.btnPeliculas, binding.btnSeries, binding.btnAdultos).forEachIndexed { i, btn ->
            btn.setColorFilter(if (i == idx) cyan else hint)
            btn.setBackgroundColor(if (i == idx) activeBg else normalBg)
        }
    }

    private fun checkUpdate() {
        scope.launch {
            try {
                val ver = withContext(Dispatchers.IO) { ApiService.getVersion() }
                if (ver != null) AutoUpdater.check(this@MainActivity, "1.0.3", ver)
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
