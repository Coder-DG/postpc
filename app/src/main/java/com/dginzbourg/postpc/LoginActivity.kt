package com.dginzbourg.postpc

import android.Manifest
import android.app.Activity
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

class LoginActivity : AppCompatActivity() {
    private val permissionArray = arrayOf(
        Manifest.permission.INTERNET
    )
    private lateinit var usernameEditText: EditText
    private lateinit var setUsernameButton: Button
    private val usernameRegex = "[a-zA-Z0-9]+".toRegex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        validatePermissionsGranted()

        setUsernameButton = findViewById(R.id.loginSetUsername)
        usernameEditText = findViewById(R.id.loginUsername)
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

                setUsernameButton.isEnabled = usernameEditText.error == null
            }

        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        loadUserEditTextAttributes(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putString(USERNAME_TEXT, usernameEditText.text.toString())
            putString(USERNAME_ERROR_KEY, usernameEditText.error?.toString())
        }
    }

    private fun loadUserEditTextAttributes(savedInstanceState: Bundle?) {
        savedInstanceState?.apply {
            usernameEditText.setText(getString(USERNAME_TEXT, ""))
            usernameEditText.error = getString(USERNAME_ERROR_KEY)
        }
        setUsernameButton.isEnabled = usernameEditText.error == null && usernameEditText.text.isNotEmpty()
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
        const val GENERAL_PERMISSION_REQUEST_CODE = 1
    }
}
