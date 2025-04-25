package com.example.myapplication.data.repository

import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.UserEntity

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun getUser(email: String, password: String): UserEntity? {
        return userDao.getUser(email, password)
    }

    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }
}
