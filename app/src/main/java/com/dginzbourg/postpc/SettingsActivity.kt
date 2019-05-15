package com.dginzbourg.postpc

import android.Manifest
import android.app.Activity
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
        for (i in 0..permissionArray.size) {
            if (ContextCompat.checkSelfPermission(this, permissionArray[i]) != PackageManager.PERMISSION_GRANTED)
                Log.d("permissions request", "Requested permission ${permissionArray[i]}")
            requestPermissions(permissionArray, i)
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
                { askForPermissions() },
                { finish() }
            )
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
        posFunction: () -> Unit,
        negFunction: () -> Unit = {}
    ): AlertDialog? {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        builder.setMessage(message)
            .setTitle(title)
            .setPositiveButton(posButtonText) { _, _ -> posFunction() }
            .setNegativeButton(negButtonText) { _, _ -> negFunction() }
        return builder.create()
    }

}
