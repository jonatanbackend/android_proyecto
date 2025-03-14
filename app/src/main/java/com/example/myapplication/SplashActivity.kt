package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Carga el layout splash.xml en lugar de un drawable
        setContentView(R.layout.splash)

        val loadingText: TextView = findViewById(R.id.loading_text)
        val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_out)
        loadingText.startAnimation(fadeAnimation)

        // Espera 4 segundos y luego inicia la MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 4000)
    }
}
