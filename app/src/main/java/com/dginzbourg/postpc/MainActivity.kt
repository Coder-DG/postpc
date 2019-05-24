package com.dginzbourg.postpc

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONObject
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var token = MutableLiveData<String>()
    private var picURL = MutableLiveData<String>()
    private var prettyName = MutableLiveData<String>()
    private lateinit var username: String
    private lateinit var profilePic: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var prettyNameEditText: EditText
    private lateinit var updatePrettyNameButton: Button
    private lateinit var requestQueue: RequestQueue
    private val executor = Executors.newCachedThreadPool()

    private lateinit var errorListener: Response.ErrorListener
    private var updateUIFromDataListener = Response.Listener<JSONObject> {
        Log.d("userinfo", "Got data $it")
        if (!it.has(REQUESTS_DATA_KEY)) {
            showErrorToast()
            return@Listener
        }
        val data = it[REQUESTS_DATA_KEY] as JSONObject
        if (!data.has(REQUESTS_PRETTY_NAME_KEY) || !data.has(REQUESTS_IMAGE_URL_KEY)) {
            showErrorToast()
            return@Listener
        }
        picURL.postValue(data[REQUESTS_IMAGE_URL_KEY] as String)
        prettyName.postValue(data[REQUESTS_PRETTY_NAME_KEY] as String)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        username = intent.getStringExtra(DB_USERNAME_KEY)
        restoreData(savedInstanceState)
        errorListener = Response.ErrorListener {
            showErrorToast()
        }
        initUIElements()
        if (!restoreUITexts(savedInstanceState)) {
            showPrettyName()
        }
        setupOnClicks()
        setupListeners()
        if (token.value == null || token.value?.isEmpty() == true) {
            fetchToken()
        }
    }

    private fun setupOnClicks() {
        updatePrettyNameButton.setOnClickListener {
            val jsonRequestBody = HashMap<String, String>(1)
            jsonRequestBody[REQUESTS_PRETTY_NAME_KEY] = prettyNameEditText.text.toString()
            updateInfo(JSONObject(jsonRequestBody))
        }
        profilePic.setOnClickListener {
            showPicSelectorDialog()
        }
    }

    private fun updateInfo(jsonObject: JSONObject) {
        val request = object : JsonObjectRequest(
            "$SERVER_BASE_URL$SERVER_USER_URL$SERVER_EDIT_URL",
            jsonObject,
            updateUIFromDataListener,
            errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return getDefaultHeaders().also { it[REQUESTS_CONTENT_TYPE_HEADER] = REQUESTS_CONTENT_TYPE_JSON }
            }
        }.also {
            it.tag = this
        }

        addRequest(request)
    }

    private fun restoreData(savedInstanceState: Bundle?) {
        savedInstanceState?.apply {
            token.value = getString(TOKEN, null)
            picURL.value = getString(PIC_URL, "")
        }
    }

    private fun setupListeners() {
        token.observe(this, Observer {
            if (prettyNameEditText.text.toString().isBlank()) {
                fetchUserInfo()
            }
        })
        picURL.observe(this, Observer {
            Glide.with(this)
                .load(SERVER_BASE_URL + picURL.value)
                .override(500, 500)
                .into(profilePic)
        })
        prettyName.observe(this, Observer {
            showPrettyName()
        })
    }

    private fun showPrettyName() {
        val text = when {
            prettyName.value?.isNotBlank() == true -> "Welcome back, ${prettyName.value}!"
            else -> "No pretty name has been set."
        }
        usernameTextView.text = text
    }

    private fun showErrorToast() {
        showToast(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
    }

    private fun addRequest(request: JsonObjectRequest) {
        if (!::requestQueue.isInitialized)
            requestQueue = Volley.newRequestQueue(application)
        requestQueue.add(request)
    }

    private fun fetchToken() {
        showToast(this, getString(R.string.loading), Toast.LENGTH_LONG)
        val request = JsonObjectRequest(
            "$SERVER_BASE_URL$SERVER_USERS_URL$username/$SERVER_TOKEN_URL",
            null,
            Response.Listener<JSONObject> {
                if (!it.has(REQUESTS_DATA_KEY)) {
                    showErrorToast()
                    return@Listener
                }
                token.postValue(it[REQUESTS_DATA_KEY] as String)
            },
            errorListener
        ).also {
            it.tag = this
        }

        addRequest(request)
    }

    private fun getDefaultHeaders() = HashMap<String, String>(1).also {
        it[REQUESTS_AUTHORIZATION_HEADER] = REQUESTS_TOKEN_PREFIX + token.value
    }

    private fun fetchUserInfo() {
        val request = object : JsonObjectRequest(
            SERVER_BASE_URL + SERVER_USER_URL,
            null,
            updateUIFromDataListener,
            errorListener
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return getDefaultHeaders()
            }
        }.also {
            it.tag = this
        }

        addRequest(request)
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
            usernameTextView.text = getString(USERNAME_TEXT_VIEW, "") ?: ""
            prettyNameEditText.setText(getString(PRETTY_NAME, "Error!"))
            return true
        }
        return false
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreData(savedInstanceState)
        restoreUITexts(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(USERNAME, username)
            putString(USERNAME_TEXT_VIEW, usernameTextView.text.toString())
            putString(PRETTY_NAME, prettyNameEditText.text.toString())

            putString(TOKEN, token.value)
            putString(PIC_URL, picURL.value)
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::requestQueue.isInitialized)
            requestQueue.cancelAll(this)
        executor.shutdown()
    }

    private fun showPicSelectorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Choose your favorite picture")
            .setItems(
                arrayOf<CharSequence>(*PIC_OPTIONS)
            ) { _, which ->
                val jsonRequestBody = HashMap<String, String>(1)
                jsonRequestBody[REQUESTS_IMAGE_URL_KEY] = PIC_OPTIONS[which]
                updateInfo(JSONObject(jsonRequestBody))
            }
            .create()
            .show()
    }

    companion object {
        private val PIC_OPTIONS = arrayOf(
            "images/crab.png",
            "images/unicorn.png",
            "images/alien.png",
            "images/robot.png",
            "images/octopus.png",
            "images/frog.png"
        )
        const val USERNAME = "username"
        const val USERNAME_TEXT_VIEW = "username_text_view"
        const val PRETTY_NAME = "pretty_name"
        const val PIC_URL = "pic_url"
        const val TOKEN = "token"
    }

}
