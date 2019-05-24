package com.dginzbourg.postpc

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    internal data class UserInfo(val picURL: String, val prettyName: String)

    private var token = MutableLiveData<String>()
    private var userInfo = MutableLiveData<UserInfo>()
    private lateinit var username: String
    private lateinit var profilePic: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var prettyNameEditText: EditText
    private lateinit var updatePrettyNameButton: Button
    private lateinit var requestQueue: RequestQueue
    private val executor = Executors.newCachedThreadPool()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUIElements()
        if (!restoreUITexts(savedInstanceState)) {
            usernameTextView.text = intent.getStringExtra(DB_USERNAME_KEY)
        }
        fetchToken()
        token.observe(this, Observer { fetchUserInfo() })
    }

    private fun fetchToken() {
        requestQueue = Volley.newRequestQueue(application)
        showToast(this, getString(R.string.loading), Toast.LENGTH_LONG)
        val stringRequest = JsonObjectRequest(
            "$SERVER_BASE_URL$SERVER_USERS_URL$username/$SERVER_TOKEN_URL",
            null,
            Response.Listener<JSONObject> {
                token.postValue(it[REQUESTS_DATA_KEY] as String)
            },
            Response.ErrorListener {
                showToast(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
            }).also {
            it.tag = this
        }

        requestQueue.add(stringRequest)
    }

    private fun fetchUserInfo() {

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

    override fun onStop() {
        super.onStop()
        if (this::requestQueue.isInitialized)
            requestQueue.cancelAll(this)
        executor.shutdown()
    }
}
