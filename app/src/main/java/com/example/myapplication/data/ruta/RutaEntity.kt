// RutaEntity.kt
package com.example.myapplication.data.ruta

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutas")
data class RutaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val fecha: Long,
    val latitudes: String,
    val longitudes: String
)
