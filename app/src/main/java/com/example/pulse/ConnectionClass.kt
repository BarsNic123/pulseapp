package com.example.pulse

import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager

class ConnectionClass {

    private val ip = "10.0.2.2"
    private val port = "3307"
    private val db = "pulseapp"
    private val username = "root"
    private val password = ""

    fun CONN(): Connection? {
        var conn: Connection? = null
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            Class.forName("com.mysql.jdbc.Driver")
            val connString = "jdbc:mysql://$ip:$port/$db?useSSL=false&allowPublicKeyRetrieval=true"
            conn = DriverManager.getConnection(connString, username, password)
            Log.i("SQLConnection", "Connection Successful!")
        } catch (e: Exception) {
            Log.e("SQLConnection", "Connection Error: ${e.message}")
            e.printStackTrace()
        }
        return conn
    }
}