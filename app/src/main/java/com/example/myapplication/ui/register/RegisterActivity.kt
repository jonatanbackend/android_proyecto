package com.example.myapplication.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.db.PostgresHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.PreparedStatement
import java.sql.Types

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val loginEditText = findViewById<TextInputEditText>(R.id.loginEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val empresaEditText = findViewById<TextInputEditText>(R.id.empresaEditText)
        val cedulaEditText = findViewById<TextInputEditText>(R.id.cedulaEditText)
        val registerButton = findViewById<MaterialButton>(R.id.registerButton)

        registerButton.setOnClickListener {
            val login = loginEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val empresa = empresaEditText.text.toString().trim()
            val cedula = cedulaEditText.text.toString().trim()

            // Validar campos obligatorios
            if (login.isEmpty() || password.isEmpty() || empresa.isEmpty()) {
                Toast.makeText(this, "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar longitud máxima de cada campo (ajusta los valores según tu tabla)
            if (login.length > 10) {
                Toast.makeText(this, "El usuario no puede tener más de 10 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length > 10) {
                Toast.makeText(this, "La contraseña no puede tener más de 10 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (empresa.length > 10) {
                Toast.makeText(this, "La empresa no puede tener más de 10 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cedula.isNotEmpty() && cedula.length > 10) {
                Toast.makeText(this, "La cédula no puede tener más de 10 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val connection = PostgresHelper.connect()
                if (connection != null) {
                    try {
                        val insertQuery = """
                            INSERT INTO geros_usuarios 
                            (login, password, empresa, habilitado, fecharegistro, soporte, ingreso, validar, us_aplica_inactivar, cedula)
                            VALUES (?, ?, ?, 'S', timezone('utc+05', now()), 2, 8, 7, 7, ?)
                        """.trimIndent()

                        val statement: PreparedStatement = connection.prepareStatement(insertQuery)
                        statement.setString(1, login)
                        statement.setString(2, password)
                        statement.setString(3, empresa)
                        // cedula (opcional)
                        if (cedula.isNotEmpty()) statement.setString(4, cedula) else statement.setNull(4, Types.VARCHAR)

                        val rowsInserted = statement.executeUpdate()
                        withContext(Dispatchers.Main) {
                            if (rowsInserted > 0) {
                                Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@RegisterActivity, "No se pudo registrar", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        connection.close()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}