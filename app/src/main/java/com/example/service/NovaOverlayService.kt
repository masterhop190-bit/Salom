package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NovaOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        _isOverlayActive.value = true
        if (Settings.canDrawOverlays(this)) {
            setupFloatingBubble()
        }
    }

    private fun setupFloatingBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 300
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.TRANSPARENT)

            // 1. Floating Assistant Bubble
            val iconView = ImageView(this@NovaOverlayService).apply {
                setImageResource(R.mipmap.ic_launcher)
                layoutParams = LinearLayout.LayoutParams(130, 130)
                setPadding(8, 8, 8, 8)
            }
            addView(iconView)

            // 2. Delete / Dismiss Close Button
            val closeBtn = ImageView(this@NovaOverlayService).apply {
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                setColorFilter(0xFFFF5252.toInt())
                setBackgroundColor(0xCC101828.toInt())
                layoutParams = LinearLayout.LayoutParams(50, 50).apply {
                    setMargins(4, 0, 0, 0)
                }
                setPadding(4, 4, 4, 4)
                setOnClickListener {
                    // Close and delete floating overlay button
                    stopSelf()
                }
            }
            addView(closeBtn)
        }

        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(container, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // Tap action: Launch Nova Voice HUD or Screen Vision
                            val intent = Intent(this@NovaOverlayService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                putExtra("TRIGGER_VOICE_IMMEDIATELY", true)
                            }
                            startActivity(intent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView = container
        try {
            windowManager?.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isOverlayActive.value = false
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // ignore
            }
        }
        floatingView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private val _isOverlayActive = MutableStateFlow(false)
        val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                context.startService(Intent(context, NovaOverlayService::class.java))
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NovaOverlayService::class.java))
        }
    }
}
