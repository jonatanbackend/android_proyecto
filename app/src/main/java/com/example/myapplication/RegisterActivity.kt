package com.example.myapplication

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val scrollView = findViewById<ScrollView>(R.id.scrollView) // Asegúrate de que el ID del ScrollView sea correcto

        scrollView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            scrollView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = scrollView.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            if (keyboardHeight > screenHeight * 0.15) { // Si el teclado ocupa más del 15% de la pantalla
                scrollView.post {
                    scrollView.smoothScrollTo(0, scrollView.bottom) // Mueve el ScrollView hacia abajo suavemente
                }
            }
        }
    }
}
