package com.dginzbourg.postpc

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class LoginActivity : AppCompatActivity() {
    private val permissionArray = arrayOf(
        Manifest.permission.INTERNET
    )
    private lateinit var usernameEditText: EditText
    private lateinit var infoTextView: TextView
    private lateinit var setUsernameButton: Button
    private val usernameRegex = "[a-zA-Z0-9]+".toRegex()
    private var isConnected = MutableLiveData<Boolean>().also { it.value = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        validatePermissionsGranted()

        setUsernameButton = findViewById(R.id.loginSetUsername)
        usernameEditText = findViewById(R.id.loginUsername)
        infoTextView = findViewById(R.id.infoTextView)
        loadUserEditTextAttributes(savedInstanceState)
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isEmpty() == true) {
                    usernameEditText.error = getString(R.string.usernameRequired)
                } else if (s != null && !usernameRegex.matches(s)) {
                    usernameEditText.error = getString(R.string.usernameError)
                } else {
                    usernameEditText.error = null
                }

                setUsernameButton.isEnabled = canSetUsername()
            }

        })
        isConnected.observe(this,
            Observer<Boolean> { t ->
                setUsernameButton.isEnabled = canSetUsername()
                infoTextView.text = if (t == true) {
                    getString(R.string.connected)
                } else {
                    getString(R.string.not_connected)
                }
            })
        checkConnection()
    }


    private fun checkConnection() {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

// Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET,
            SERVER_BASE_URL,
            Response.Listener<String> {
                isConnected.value = true
            },
            Response.ErrorListener {
                isConnected.value = false
            })

// Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun canSetUsername(): Boolean = usernameEditText.error == null
            && usernameEditText.text.isNotEmpty()
            && isConnected.value ?: false

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        loadUserEditTextAttributes(savedInstanceState)
        isConnected.value = savedInstanceState?.getBoolean(IS_CONNECTED_KEY, false) ?: false
        setUsernameButton.isEnabled = canSetUsername()
    }

    private fun loadUserEditTextAttributes(savedInstanceState: Bundle?) {
        savedInstanceState?.apply {
            usernameEditText.setText(getString(USERNAME_TEXT, ""))
            usernameEditText.error = getString(USERNAME_ERROR_KEY)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(USERNAME_TEXT, usernameEditText.text.toString())
            putString(USERNAME_ERROR_KEY, usernameEditText.error?.toString())
            putBoolean(IS_CONNECTED_KEY, isConnected.value ?: false)
        }
    }


    private fun validatePermissionsGranted() {
        for (permission in permissionArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                Log.d("permissions request", "Requested permission $permission")
            requestPermissions(permissionArray, GENERAL_PERMISSION_REQUEST_CODE)
            break
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("onRequestPermission", "Called")
        if (requestCode != GENERAL_PERMISSION_REQUEST_CODE)
            return
        if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            getAlertDialog(
                this,
                "Please grant the requested permissions",
                "Grant Permissions",
                "Grant",
                "Close App",
                { dialog, _ ->
                    validatePermissionsGranted()
                    dialog.cancel()
                },
                { _, _ -> finish() }
            )?.show()
        }
    }

    private fun getAlertDialog(
        activity: Activity,
        message: String,
        title: String,
        posButtonText: String,
        negButtonText: String,
        posFunction: (dialog: DialogInterface, id: Int) -> Unit,
        negFunction: (dialog: DialogInterface, id: Int) -> Unit
    ): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(message)
            .setTitle(title)
            .setPositiveButton(posButtonText) { dialog, id -> posFunction(dialog, id) }
            .setNegativeButton(negButtonText) { dialog, id -> negFunction(dialog, id) }
            .setCancelable(false)
        return builder.create()
    }

    companion object {
        const val USERNAME_ERROR_KEY = "username_error"
        const val USERNAME_TEXT = "username_text"
        const val IS_CONNECTED_KEY = "isConnected"
        const val GENERAL_PERMISSION_REQUEST_CODE = 1
    }
}
