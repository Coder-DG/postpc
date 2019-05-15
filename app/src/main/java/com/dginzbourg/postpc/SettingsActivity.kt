package com.dginzbourg.postpc

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {

    private val permissionArray = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.SEND_SMS
    )
    private lateinit var phoneNoEditText: EditText
    private lateinit var smsPrefixEditText: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageTextView: TextView
    private lateinit var currThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        validatePermissionsGranted()
        sharedPreferences = this.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        phoneNoEditText = findViewById(R.id.phone_no_edit_text)
        smsPrefixEditText = findViewById(R.id.sms_prefix_edit_text)
        messageTextView = findViewById(R.id.message_text_view)
        messageTextView.setText(R.string.ready)

        phoneNoEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getHandleOnTextChanged(phoneNoEditText)(s)
            }

        })
        smsPrefixEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getHandleOnTextChanged(smsPrefixEditText)(s)
            }

        })
    }

    private fun getHandleOnTextChanged(editText: EditText): (CharSequence?) -> Unit {
        return { s: CharSequence? ->
            checkEditTextValidity(s?.toString(), editText)
        }
    }

    private fun checkEditTextValidity(s: String?, editText: EditText) {
        if (s?.isEmpty() == true) {
            editText.error = getString(R.string.required)
            messageTextView.setText(R.string.missing_inputs)
        } else {
            editText.error = null
            messageTextView.setText(R.string.ready)
        }
    }

    override fun onResume() {
        super.onResume()
        currThread = Thread {
            with(sharedPreferences) {
                val phonNoString = getString(PHONE_NO_KEY, "") ?: ""
                val smsPrefixString = getString(SMS_PREFIX, "") ?: ""

                runOnUiThread {
                    phoneNoEditText.setText(phonNoString)
                    smsPrefixEditText.setText(smsPrefixString)
                    checkEditTextValidity(phonNoString, phoneNoEditText)
                    checkEditTextValidity(smsPrefixString, smsPrefixEditText)
                }
            }
        }
        currThread.start()
    }

    override fun onPause() {
        super.onPause()
        Thread {
            currThread.join()
            with(sharedPreferences.edit()) {
                putString(PHONE_NO_KEY, phoneNoEditText.text.toString())
                putString(SMS_PREFIX, smsPrefixEditText.text.toString())
                apply()
            }
        }.start()
    }

    private fun validatePermissionsGranted() {
        for (permission in permissionArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                Log.d("permissions request", "Requested permission $permission")
            requestPermissions(permissionArray, 123)
            break
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
            getAlertDialog(
                this,
                "Please grant the requested permissions",
                "Grant Permissions",
                "Grant",
                "Close App",
                { dialog, _ ->
                    askForPermissions()
                    dialog.cancel()
                },
                { _, _ -> finish() }
            )?.show()
        }
    }

    private fun askForPermissions() {
        validatePermissionsGranted()
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

}
