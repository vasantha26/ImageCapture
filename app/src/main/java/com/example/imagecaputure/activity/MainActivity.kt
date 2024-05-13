package com.example.imagecaputure.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.imagecaputure.R
import com.example.imagecaputure.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.buttonregister.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity, RegisterActivity::class.java
                )
            )
        }

        binding.buttonrecognize.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity, RecognitionActivity::class.java
                )
            )
        }

    }
}