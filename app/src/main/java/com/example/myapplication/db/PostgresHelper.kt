package com.example.myapplication.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object PostgresHelper {
    private const val DB_URL = "jdbc:postgresql://54.83.176.251:5432/modelodb"
    private const val USER = "gerosadmin"
    private const val PASSWORD = "5MKBugVjJ6M66V7VPLwQ3hhVv3"

    fun connect(): Connection? {
        return try {
            // Forzar la carga del driver JDBC de PostgreSQL
            Class.forName("org.postgresql.Driver")
            val connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)

            if (connection.isValid(2)) {
                println("Conexión establecida correctamente.")
            } else {
                println("La conexión no es válida.")
            }

            connection
        } catch (e: Exception) {
            println("Error al conectar a la base de datos: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun testConnection(): Boolean {
        val connection = connect()
        return if (connection != null) {
            println("Conexión exitosa a PostgreSQL 🎉")
            connection.close()
            true
        } else {
            println("Fallo al conectar a PostgreSQL ❌")
            false
        }
    }
}