package com.google.zenithtv.fragments

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.google.zenithtv.activities.PlayerActivity
import com.google.zenithtv.models.Channel
import com.google.zenithtv.services.ApiService
import kotlinx.coroutines.*

class MainFragment : BrowseSupportFragment() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var allChannels = listOf<Channel>()
    private val catOrder = listOf("MUNDIAL 2026","EVENTOS","ARGENTINA","ARGENTINA INTERIOR","ARGENTINA 2","DEPORTES","DEPORTES 2","NOTICIAS","NOTICIAS 2","MÚSICA","MÚSICA 2","RELIGIÓN","INFANTILES","INFANTILES 2","CANALES 24/7","CANALES 24/7 2","CINE","CINE 2","SERIES","SERIES 2","INTERNACIONAL","INTERNACIONAL 2","COLOMBIA","CHILE","MEXICO","BRASIL","URUGUAY","DOCUMENTALES","PLUTOTV")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "ZENITH TV"
        headersState = HEADERS_DISABLED
        brandColor = 0xFF030810.toInt()
        searchAffordanceColor = 0xFF00E5FF.toInt()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, row ->
            if (item is Channel) {
                val rowAdapter = (row as ListRow).adapter as ArrayObjectAdapter
                val list = (0 until rowAdapter.size()).map { rowAdapter.get(it) as Channel }
                val idx = list.indexOf(item).coerceAtLeast(0)
                startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
                    putExtra(PlayerActivity.EXTRA_CHANNELS, ArrayList(list))
                    putExtra(PlayerActivity.EXTRA_INDEX, idx)
                })
            }
        }
        loadChannels()
    }

    fun filterCategory(category: String?) {
        val filtered = if (category == null) allChannels
                       else allChannels.filter { it.category == category || it.category == "$category 2" }
        buildRows(filtered)
    }

    fun filterCategories(cats: List<String>) {
        buildRows(allChannels.filter { it.category in cats })
    }

    fun loadChannels() {
        scope.launch {
            allChannels = withContext(Dispatchers.IO) {
                try { ApiService.getChannels() } catch (e: Exception) { emptyList() }
            }
            buildRows(allChannels)
        }
    }

    private fun buildRows(channels: List<Channel>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val grouped = channels.filter { it.category != "ADULTOS" }.groupBy { it.category }
        val sorted = catOrder.mapNotNull { grouped[it]?.let { chs -> it to chs } } +
                     grouped.filter { it.key !in catOrder }.map { it.key to it.value }
        sorted.forEach { (cat, chs) ->
            val adapter = ArrayObjectAdapter(ChannelPresenter())
            chs.forEach { adapter.add(it) }
            rowsAdapter.add(ListRow(HeaderItem(cat), adapter))
        }
        adapter = rowsAdapter
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}

class ChannelPresenter : Presenter() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
        val ctx = parent.context
        val dp = ctx.resources.displayMetrics.density
        val view = android.widget.FrameLayout(ctx).apply {
            layoutParams = android.view.ViewGroup.LayoutParams((140*dp).toInt(), (100*dp).toInt())
            setBackgroundColor(0xFF060E1A.toInt())
            isFocusable = true; isFocusableInTouchMode = true
        }
        val logo = android.widget.ImageView(ctx).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams((80*dp).toInt(), (58*dp).toInt()).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.TOP
                topMargin = (8*dp).toInt()
            }
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE; tag = "logo"
        }
        val nameBar = android.widget.LinearLayout(ctx).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, (28*dp).toInt()).apply { gravity = android.view.Gravity.BOTTOM }
            setBackgroundColor(0xEE030810.toInt())
            setPadding((8*dp).toInt(), 0, (8*dp).toInt(), 0)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val name = android.widget.TextView(ctx).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            setTextColor(0xFFFFFFFF.toInt()); textSize = 10f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            maxLines = 1; ellipsize = android.text.TextUtils.TruncateAt.END; tag = "name"
        }
        nameBar.addView(name); view.addView(logo); view.addView(nameBar)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(vh: ViewHolder, item: Any) {
        val ch = item as Channel
        val view = vh.view as android.widget.FrameLayout
        view.findViewWithTag<android.widget.TextView>("name")?.text = ch.name
        val logo = view.findViewWithTag<android.widget.ImageView>("logo")
        if (ch.logoUrl.isNotEmpty()) Glide.with(view).load(ch.logoUrl).into(logo!!)
        else logo?.setImageDrawable(null)
    }

    override fun onUnbindViewHolder(vh: ViewHolder) {
        (vh.view as android.widget.FrameLayout).findViewWithTag<android.widget.ImageView>("logo")?.setImageDrawable(null)
    }
}
