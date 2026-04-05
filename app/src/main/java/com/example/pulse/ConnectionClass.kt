package com.example.pulse

import android.os.StrictMode
import java.sql.Connection
import java.sql.DriverManager

class ConnectionClass {
    private var classes = "net.sourceforge.jtds.jdbc.Driver"

    fun CONN(): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        var conn: Connection? = null
        try {
            Class.forName(classes)
            // Fixed URL format for JTDS
            val conUrl = "jdbc:jtds:sqlserver://$ip:$port/$db"
            conn = DriverManager.getConnection(conUrl, un, password)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return conn
    }

    companion object {
        const val ip = "10.0.2.2"
        const val port = "1433"
        const val db = "Pulse"
        const val un = "sa"
        const val password = "12345"
    }
}
