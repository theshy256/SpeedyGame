package com.example.speedygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        // 全局倍速，悬浮窗改的就是它
        @Volatile
        var timeScale: Float = 1f
    }

    private val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5252")
    }
    private val bgPaint = Paint().apply { color = Color.parseColor("#0E1621") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 46f
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8899AA")
        textSize = 34f
    }

    private var x = 200f
    private var y = 200f
    private var vx = 620f
    private var vy = 500f
    private val radius = 42f
    private var lastTime = 0L

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = System.nanoTime()
        if (lastTime == 0L) lastTime = now
        var dt = (now - lastTime) / 1_000_000_000f
        lastTime = now
        if (dt > 0.05f) dt = 0.05f   // 防止切后台回来瞬移

        val scaledDt = dt * timeScale

        x += vx * scaledDt
        y += vy * scaledDt

        if (x - radius < 0) { x = radius; vx = -vx }
        if (x + radius > width) { x = width - radius; vx = -vx }
        if (y - radius < 0) { y = radius; vy = -vy }
        if (y + radius > height) { y = height - radius; vy = -vy }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawCircle(x, y, radius, ballPaint)
        canvas.drawText("倍速: %.2fx".format(timeScale), 40f, 90f, textPaint)
        canvas.drawText("点击屏幕可反弹小球", 40f, 140f, hintPaint)

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            vx = -vx
            vy = -vy
            return true
        }
        return super.onTouchEvent(event)
    }
}
