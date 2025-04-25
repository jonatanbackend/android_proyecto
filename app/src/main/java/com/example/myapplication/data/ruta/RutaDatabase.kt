package com.example.myapplication.data.ruta

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RutaEntity::class], version = 1)
abstract class RutaDatabase : RoomDatabase() {
    abstract fun rutaDao(): RutaDao
}
