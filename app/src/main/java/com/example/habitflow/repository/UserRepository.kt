package com.example.habitflow.repository

import com.example.habitflow.data.model.User
import com.example.habitflow.data.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun register(user: User) {
        userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUserName(email: String, name: String): Int {
        return userDao.updateUserName(email, name)
    }

    suspend fun updateUserPhoto(email: String, photoUri: String?): Int {
        return userDao.updateUserPhoto(email, photoUri)
    }

    suspend fun updateUserPassword(email: String, password: String): Int {
        return userDao.updateUserPassword(email, password)
    }

    suspend fun getCoins(email: String): Int? {
        return userDao.getCoins(email)
    }

    suspend fun updateCoins(email: String, coins: Int) {
        userDao.updateCoins(email, coins)
    }
}
