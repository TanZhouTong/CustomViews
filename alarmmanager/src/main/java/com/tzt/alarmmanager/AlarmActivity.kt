package com.tzt.alarmmanager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * @Description
 * @Author tanzhoutong
 * @Date 2025/11/5 17:47
 * https://www.jianshu.com/p/8a2ce9d02640
 */
class AlarmActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var beginButton: Button
    lateinit var stopButton: Button
    val alarmManager: AlarmManager by lazy { applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        initView()
    }

    private fun initView() {
        beginButton = findViewById<Button>(R.id.begin_alarm).apply {
            setOnClickListener(this@AlarmActivity)
        }
        stopButton = findViewById<Button>(R.id.stop_alarm).apply {
            setOnClickListener(this@AlarmActivity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.begin_alarm -> {
                initAlarm()
            }

            R.id.stop_alarm -> {
                stopAlarm()
            }
        }
    }

    private fun initAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000 * 10L,
                getPendingIntent(applicationContext, this::class.java)
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000 * 10L,
                getPendingIntent(applicationContext, this::class.java)
            )
        }
    }

    private fun stopAlarm() {
        alarmManager.cancel(getPendingIntent(applicationContext, this::class.java))
    }

    private fun getPendingIntent(context: Context, activity: Class<*>): PendingIntent {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 声明可变性,systemUi中需要回传额外数据
            flags = flags or PendingIntent.FLAG_IMMUTABLE
        }
        val currentIntent = Intent(context, activity)
        return PendingIntent.getActivity(
            context,
            Process.myPid(),    // request code设为pid
            currentIntent,
            flags
        )
    }
}