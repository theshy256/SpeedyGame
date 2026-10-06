package com.example.speedygame

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView

class FloatingService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var panel: View
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        panel = LayoutInflater.from(this).inflate(R.layout.floating_panel, null)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 260
        }

        val tvScale = panel.findViewById<TextView>(R.id.tvScale)
        val seekBar = panel.findViewById<SeekBar>(R.id.seekBar)

        // 0.1x ~ 3.0x，进度 1 ~ 30
        seekBar.max = 30
        seekBar.progress = 10
        tvScale.text = "1.00x"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 1) return
                val scale = progress / 10f
                GameView.timeScale = scale
                tvScale.text = "%.2fx".format(scale)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 拖动把手移动悬浮窗
        panel.findViewById<View>(R.id.dragHandle).setOnTouchListener(object : View.OnTouchListener {
            private var startX = 0
            private var startY = 0
            private var downRawX = 0f
            private var downRawY = 0f

            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = params.x
                        startY = params.y
                        downRawX = e.rawX
                        downRawY = e.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (e.rawX - downRawX).toInt()
                        params.y = startY + (e.rawY - downRawY).toInt()
                        wm.updateViewLayout(panel, params)
                        return true
                    }
                }
                return false
            }
        })

        wm.addView(panel, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::panel.isInitialized) {
            try { wm.removeView(panel) } catch (_: Exception) {}
        }
    }
}
