package com.tzt.okdownload

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.liulishuo.okdownload.DownloadContext
import com.liulishuo.okdownload.OkDownload

class DownloadActivity : AppCompatActivity() {
    lateinit var btDownload: Button
    lateinit var etUrl: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_download)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    private fun initView() {
        btDownload = findViewById(R.id.bt_dl)
        etUrl = findViewById(R.id.et_url)
        btDownload.setOnClickListener {
            val url = etUrl.text.toString()
            if (url.isNotEmpty()) {

            }
        }
    }
}