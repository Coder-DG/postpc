package com.dginzbourg.postpc

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log

class SettingsActivity : AppCompatActivity() {

    private val permissionArray = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.SEND_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        validatePermissionsGranted()
    }

    private fun validatePermissionsGranted() {
        for (permission in permissionArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                Log.d("permissions request", "Requested permission $permission")
            requestPermissions(permissionArray, 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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

    fun getAlertDialog(
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
