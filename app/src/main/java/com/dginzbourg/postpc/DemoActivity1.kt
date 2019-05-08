package com.dginzbourg.postpc

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class DemoActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo1)

        val goFishButton = findViewById<Button>(R.id.goFishButton)
        goFishButton.setOnClickListener {
            startActivity(Intent(this, DemoActivity2::class.java))
        }
    }
}
