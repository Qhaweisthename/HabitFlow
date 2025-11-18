package com.example.habitflow.repository

import com.example.habitflow.data.User
import com.example.habitflow.data.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun register(user: User) {
        userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    // 🟣 Add this new function
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}