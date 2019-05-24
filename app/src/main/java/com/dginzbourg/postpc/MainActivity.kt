package com.dginzbourg.postpc

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        errorListener = Response.ErrorListener {
            showErrorToast()
        }
        initUIElements()
        if (!restoreUITexts(savedInstanceState)) {
            usernameTextView.text = intent.getStringExtra(DB_USERNAME_KEY)
        }
        setupListeners()
        fetchToken()
    }

    private fun setupListeners() {
        prettyNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isTextValid = s?.isNotBlank() == true
                updatePrettyNameButton.isEnabled = isTextValid && prettyName.value != s.toString()
                prettyNameEditText.error = when (isTextValid) {
                    false -> getString(R.string.invalid_pretty_name)
                    else -> null
                }
            }

        })
        token.observe(this, Observer { fetchUserInfo() })
        picURL.observe(this, Observer {
            // TODO use Glide to inflate the image
        })
        prettyName.observe(this, Observer { prettyNameEditText.setText(prettyName.value) })
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

    private fun fetchUserInfo() {
        val request = JsonObjectRequest(
            "$SERVER_BASE_URL$SERVER_USER_URL",
            null,
            Response.Listener<JSONObject> {
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
            },
            errorListener
        ).also {
            it.tag = this
            it.headers[REQUESTS_AUTHORIZATION_HEADER] = REQUESTS_TOKEN_PREFIX + token.value
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
            usernameTextView.text = username
            prettyNameEditText.setText(getString(PRETTY_NAME, "Error!"))
            prettyNameEditText.error = getString(PRETTY_NAME_ERROR, null)
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
            putString(PRETTY_NAME_ERROR, prettyNameEditText.error?.toString())
            putBoolean(UPDATE_BUTTON_IS_ENABLED, updatePrettyNameButton.isEnabled)
        }
    }

    companion object {
        const val USERNAME = "username"
        const val PRETTY_NAME = "pretty_name"
        const val PRETTY_NAME_ERROR = "pretty_name_error"
        const val UPDATE_BUTTON_IS_ENABLED = "update_button_is_enabled"
    }

    override fun onStop() {
        super.onStop()
        if (this::requestQueue.isInitialized)
            requestQueue.cancelAll(this)
        executor.shutdown()
    }
}
