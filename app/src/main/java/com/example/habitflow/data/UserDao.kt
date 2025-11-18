package com.example.habitflow.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET name = :name WHERE email = :email")
    suspend fun updateUserName(email: String, name: String): Int

    @Query("UPDATE users SET photoUri = :photoUri WHERE email = :email")
    suspend fun updateUserPhoto(email: String, photoUri: String?): Int

    @Query("UPDATE users SET password = :password WHERE email = :email")
    suspend fun updateUserPassword(email: String, password: String): Int

    @Query("SELECT coins FROM users WHERE email = :email LIMIT 1")
    suspend fun getCoins(email: String): Int?

    @Query("UPDATE users SET coins = :coins WHERE email = :email")
    suspend fun updateCoins(email: String, coins: Int)

}
