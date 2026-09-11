package com.example.appzetaadmin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appzetaadmin.AdminMenu.ActivityMenu

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(
            Intent(
                this,
                ActivityMenu::class.java
            )
        )

        finish()
    }
}