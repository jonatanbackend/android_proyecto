// RutaDao.kt
package com.example.myapplication.data.ruta

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RutaDao {

    @Insert
    fun insertRuta(ruta: RutaEntity)

    @Query("SELECT * FROM rutas")
    fun getAllRutas(): List<RutaEntity>
}
