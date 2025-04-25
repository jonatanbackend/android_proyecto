package com.example.myapplication.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.db.PostgresHelper
import com.example.myapplication.ui.home.HomeActivity
import com.example.myapplication.ui.register.RegisterActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement

class MainActivity : AppCompatActivity() {

    private lateinit var loginEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Si ya hay sesión activa, ir directo al Home
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val savedUser = sharedPref.getString("usuario", null)
        if (savedUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(login, password)) {
                loginUser(login, password)
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(login: String, password: String): Boolean {
        var isValid = true

        if (login.isEmpty()) {
            loginEditText.error = "El usuario no puede estar vacío"
            isValid = false
        } else if (login.length > 10) {
            loginEditText.error = "Máximo 10 caracteres"
            isValid = false
        } else {
            loginEditText.error = null
        }

        if (password.isEmpty()) {
            passwordEditText.error = "La contraseña no puede estar vacía"
            isValid = false
        } else if (password.length > 10) {
            passwordEditText.error = "Máximo 10 caracteres"
            isValid = false
        } else {
            passwordEditText.error = null
        }

        return isValid
    }

    private fun loginUser(login: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val connection = PostgresHelper.connect()
            if (connection != null) {
                try {
                    val query = """
                        SELECT * FROM geros_usuarios 
                        WHERE login = ? AND password = ?
                    """.trimIndent()

                    val statement: PreparedStatement = connection.prepareStatement(query)
                    statement.setString(1, login)
                    statement.setString(2, password)

                    val result = statement.executeQuery()

                    if (result.next()) {
                        val nombreUsuario = result.getString("login")
                        val cedulaUsuario = result.getString("cedula")

                        Log.d("LOGIN_DEBUG", "Guardando login: '$nombreUsuario', cédula: '$cedulaUsuario'")

                        // Guardar sesión
                        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        sharedPref.edit()
                            .putString("usuario", nombreUsuario)
                            .putString("cedula", cedulaUsuario)
                            .putString("login", nombreUsuario)
                            .apply()

                        withContext(Dispatchers.Main) {
                            showSuccessDialog()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(applicationContext, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    connection.close()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error de conexión a la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Inicio de Sesión")
            .setMessage("Inicio de sesión exitoso. ¡Bienvenido!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .show()
    }
}