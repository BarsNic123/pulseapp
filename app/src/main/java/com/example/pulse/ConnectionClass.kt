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

            // Use this exact format to avoid syntax errors
            val conUrl = "jdbc:jtds:sqlserver://$ip:$port/$db;ssl=request;encrypt=false;trustServerCertificate=true;"

            conn = DriverManager.getConnection(conUrl, un, password)
        } catch (e: Exception) {
            android.util.Log.e("DB_ERROR", "Message: ${e.message}")
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
