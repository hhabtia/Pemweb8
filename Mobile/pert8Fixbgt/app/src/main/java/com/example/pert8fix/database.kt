package com.example.pert8fix.database

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.pert8fix.CustomViewFollowing
import com.example.pert8fix.koneksi
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

object database {
    fun getNamaPengguna(): String? {
        var result: String? = null
        AsyncTask.execute {
            val connection = koneksi.connection()
            connection?.let {
                try {
                    val statement = it.createStatement()
                    val resultSet: ResultSet = statement.executeQuery("SELECT name FROM pengguna WHERE id = 1")
                    if (resultSet.next()) {
                        result = resultSet.getString("name")
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseError", "Error fetching data", e)
                } finally {
                    connection.close()
                }
            }
        }
        return result
    }
    fun insertPostingan(id_pengguna: Int, content: String): Boolean {
        val conn = koneksi.connection()
        if (conn == null) {
            Log.d("DB_RESULT", "Koneksi gagal.")
            return false  // Mengembalikan false ketika koneksi gagal
        }
        val sql = "INSERT INTO post (id_pengguna, content) VALUES (?, ?)"
        return try {
            val statement: PreparedStatement? = conn?.prepareStatement(sql)
            statement?.setInt(1, id_pengguna)
            statement?.setString(2, content)
            val rowsAffected = statement?.executeUpdate()
            conn?.close()

            // Mengecek apakah baris data berhasil ditambahkan
            rowsAffected != null && rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false  // Jika ada error, return false
        }
    }
    fun getFollowersOfUser(userId: Int, callback: (List<CustomViewFollowing.User>) -> Unit) {
        AsyncTask.execute {
            val followers = mutableListOf<CustomViewFollowing.User>()
            val connection = koneksi.connection()

            connection?.let {
                try {
                    val query = """
                    SELECT p.name, p.username, p.bio
                    FROM follows f
                    JOIN pengguna p ON f.follower_id = p.id
                    WHERE f.following_id = ?
                """.trimIndent()

                    val preparedStatement = it.prepareStatement(query)
                    preparedStatement.setInt(1, userId)
                    val resultSet = preparedStatement.executeQuery()

                    while (resultSet.next()) {
                        val name = resultSet.getString("name")
                        val username = resultSet.getString("username")
                        val bio = resultSet.getString("bio")
                        followers.add(com.example.pert8fix.CustomViewFollowing.User(name, username, bio))
                    }

                    Handler(Looper.getMainLooper()).post {
                        callback(followers)
                    }

                } catch (e: Exception) {
                    Log.e("DatabaseError", "Error fetching followers", e)
                } finally {
                    connection.close()
                }
            }
        }
    }
}
