package com.dginzbourg.postpc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText

class ConfigureNameActivity : AppCompatActivity() {
    private lateinit var acceptNameButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_name)

        acceptNameButton = findViewById(R.id.welcome_accept_name_button)
        acceptNameButton.setOnClickListener {

        }

        findViewById<EditText>(R.id.welcome_name_edit_text)
                .addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (s == null) {
                            acceptNameButton.visibility = View.INVISIBLE
                            return
                        }
                        if (!s.isEmpty()) {
                            acceptNameButton.visibility = View.VISIBLE
                        } else {
                            acceptNameButton.visibility = View.INVISIBLE
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }

                })
    }
}
