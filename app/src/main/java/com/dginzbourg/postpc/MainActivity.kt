package com.dginzbourg.postpc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var username: String
    private lateinit var profilePic: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var prettyNameEditText: EditText
    private lateinit var updatePrettyNameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUIElements()
        if(!restoreUITexts(savedInstanceState)){
            usernameTextView.text = intent.getStringExtra(DB_USERNAME_KEY)
        }
    }

    private fun initUIElements() {
        profilePic = findViewById(R.id.profilePic)
        usernameTextView = findViewById(R.id.usernameTextView)
        prettyNameEditText = findViewById(R.id.prettyNameEditText)
        updatePrettyNameButton = findViewById(R.id.updatePrettyNameButton)
    }

    private fun restoreUITexts(savedInstanceState: Bundle?): Boolean {
        savedInstanceState?.apply {
            username = getString(USERNAME, null)
            usernameTextView.text = username
            prettyNameEditText.setText(getString(PRETTY_NAME, "Error!"))
            updatePrettyNameButton.isEnabled = getBoolean(UPDATE_BUTTON_IS_ENABLED, false)
            return true
        }
        return false
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreUITexts(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(USERNAME, username)
            putString(PRETTY_NAME, prettyNameEditText.text.toString())
            putBoolean(UPDATE_BUTTON_IS_ENABLED, updatePrettyNameButton.isEnabled)
        }
    }

    companion object {
        const val USERNAME = "username"
        const val PRETTY_NAME = "pretty_name"
        const val UPDATE_BUTTON_IS_ENABLED = "update_button_is_enabled"
    }
}
