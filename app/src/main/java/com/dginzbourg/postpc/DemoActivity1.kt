package com.dginzbourg.postpc

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DemoActivity1 : AppCompatActivity() {

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo1)

        findViewById<Button>(R.id.go_fish_button).setOnClickListener {
            startActivity(Intent(this, DemoActivity2::class.java))
        }

        textView = findViewById<TextView>(R.id.tap_on_me)
        textView.setOnClickListener {
            val intent = Intent(this, DemoActivity3::class.java)
            startActivityForResult(intent, 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            val res = "you have entered: ${data?.getStringExtra("result")}"
            textView.text = res
        }
    }
}
