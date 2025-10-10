package com.tzt.customviews

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tzt.custompopupwindow.CustomPopupWindow

class MainActivity : AppCompatActivity(), OnClickListener {
    lateinit var toolbar: Toolbar
    lateinit var navigationBack: ImageView
    lateinit var titleTextView: TextView
    lateinit var languageFromTextView: TextView
    lateinit var languageSwapImage: ImageView
    lateinit var languageToTextView: TextView
    lateinit var translateButton: TextView
    lateinit var toGrid: Button
    lateinit var toLinear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        initView()
        setClickListener()
    }

    private fun initView() {
        navigationBack = findViewById(com.tzt.customtoolbar.R.id.navigation_back)
        titleTextView = findViewById(com.tzt.customtoolbar.R.id.title_tv)
        languageFromTextView = findViewById(com.tzt.customtoolbar.R.id.language_from)
        languageSwapImage = findViewById(com.tzt.customtoolbar.R.id.language_replace)
        languageToTextView = findViewById(com.tzt.customtoolbar.R.id.language_to)
        translateButton = findViewById(com.tzt.customtoolbar.R.id.translate_bt)
        toGrid = findViewById(R.id.bt_to_grid)
        toLinear = findViewById(R.id.bt_to_linear)
    }

    private fun setClickListener() {
        navigationBack.setOnClickListener(this)
        titleTextView.setOnClickListener(this)
        languageFromTextView.setOnClickListener(this)
        languageSwapImage.setOnClickListener(this)
        languageToTextView.setOnClickListener(this)
        translateButton.setOnClickListener(this)
        toGrid.setOnClickListener(this)
        toLinear.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            com.tzt.customtoolbar.R.id.navigation_back, com.tzt.customtoolbar.R.id.title_tv -> onBackPressed()
            com.tzt.customtoolbar.R.id.language_from, com.tzt.customtoolbar.R.id.language_to -> showPopupWindow(v)
            com.tzt.customtoolbar.R.id.language_replace -> swapLanguage()
            com.tzt.customtoolbar.R.id.translate_bt -> doTranslate()
            R.id.bt_to_grid -> toGrid()
            R.id.bt_to_linear -> toLinear()
        }
    }

    private fun showPopupWindow(view: View) {
        Log.d(TAG, "showPopupWindow execute...${view}")
        val list = ArrayList<String>()
        list.add("电脑拜佛阿布共")
        list.add("gap")
        list.add("等我可能Dion还哦对")
        list.add("gap")
        list.add("爱护你覅u哦啊和覅哦")
        list.add("gap")
        list.add("的撒的到几哦")
        list.add("gap")
        list.add("i哦接待oh")
        list.add("gap")
        list.add("克拉吉欧皮带机 ")
        list.add("gap")
        list.add("看到颇为精品")
        list.add("gap")
        list.add("jdwihjdioahdiouhaoi")
        list.add("gap")
        list.add("恐怕降低哦好iu的哈哈哦啊多元化")
        list.add("gap")
        list.add("hi等会我i会丢哦啊")
        list.add("gap")
        list.add("i殴打和我i啊好滴哈")
        CustomPopupWindow(this, com.tzt.custompopupwindow.R.layout.popup_custom, list).showAsDropDown(view)
    }

    private fun swapLanguage() {
        Log.d(TAG, "swapLanguage execute...")
    }

    private fun doTranslate() {
        Log.d(TAG, "doTranslate execute...")
    }

    private fun toGrid() {
        val intent = Intent(this, PageViewActivity::class.java)
        startActivity(intent)
    }

    private fun toLinear() {
        Log.d(TAG, "toLinear TODO")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}