package com.example.pert8fix

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object koneksi {
    private val URL = "jdbc:postgresql://10.0.2.2:5432/twt6"
    private val USER = "postgres"
    private val PASSWORD = "yupiburger"

    fun connection (): Connection?{
        return try{
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(URL,USER, PASSWORD).also {
            }
        }catch (e: SQLException){
            e.printStackTrace()
            println("Hahahaha")
            null
        }
    }
}
