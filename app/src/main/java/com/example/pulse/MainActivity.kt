package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.sql.Connection
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegisterLink = findViewById<TextView>(R.id.txtRegisterLink)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show()
            } else {
                // 2. Call the connection logic
                performLogin(email, password)
            }
        }

        txtRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(email: String, password: String) {
        val executor = Executors.newSingleThreadExecutor()

        executor.execute {
            val connectionClass = ConnectionClass()
            connection = connectionClass.CONN()

            if (connection == null) {
                runOnUiThread {
                    Toast.makeText(this, "Check your network connection", Toast.LENGTH_SHORT).show()
                }
            } else {
                try {

                    val sqlQuery = "SELECT * FROM Users WHERE email = '$email' AND password = '$password'"
                    val statement = connection!!.createStatement()
                    val resultSet = statement.executeQuery(sqlQuery)

                    if (resultSet.next()) {
                        runOnUiThread {
                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // No user found
                        runOnUiThread {
                            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }
}