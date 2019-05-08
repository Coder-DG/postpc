package com.dginzbourg.postpc

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DemoActivity3 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo3)

        findViewById<EditText>(R.id.digits_edit_text).addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s?.length == 5) {
                            val intent = Intent(this@DemoActivity3, DemoActivity1::class.java)
                            intent.putExtra("result", s.toString())
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                })
    }
}
