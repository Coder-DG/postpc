package com.dginzbourg.postpc

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore

class ConfigureNameActivity : AppCompatActivity() {
    private lateinit var acceptNameButton: Button
    private var name = ""
    private val db = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_name)

        findViewById<Button>(R.id.welcome_skip_button).setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
        acceptNameButton = findViewById(R.id.welcome_accept_name_button)
        acceptNameButton.setOnClickListener {
            if (name.isNotEmpty()) {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
                saveNameToRemoteDB()
            }
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
                        if (s.isNotEmpty()) {
                            acceptNameButton.visibility = View.VISIBLE
                            name = s.toString()
                        } else {
                            acceptNameButton.visibility = View.INVISIBLE
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                    }

                })
        checkNameIfExists()
    }

    private fun checkNameIfExists() {
        db.collection(FIREBASE_DEFAULTS)
                .document(FIREBASE_DEFAULTS_USERNAME_DOC_ID)
                .get()
                .addOnSuccessListener {
                    if ((it[FIREBASE_DEFAULTS_USERNAME_NAME_KEY] as String?)?.isNotEmpty() == true) {
                        finish()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
    }

    private fun saveNameToRemoteDB() {
        val map = HashMap<String, String>(1)
        map[FIREBASE_DEFAULTS_USERNAME_NAME_KEY] = name
        db.collection(FIREBASE_DEFAULTS)
                .document(FIREBASE_DEFAULTS_USERNAME_DOC_ID)
                .set(map)
                .addOnSuccessListener { df ->
                    Log.d("firebase", "DocumentSnapshot added with ID: " +
                            FIREBASE_DEFAULTS_USERNAME_DOC_ID)
                }
                .addOnFailureListener { e ->
                    Log.w("firebase", "Error adding document", e)
                }

    }
}
