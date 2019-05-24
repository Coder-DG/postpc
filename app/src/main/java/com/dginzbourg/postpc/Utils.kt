package com.dginzbourg.postpc

import android.content.Context
import android.widget.Toast

internal const val SERVER_BASE_URL = "http://hujipostpc2019.pythonanywhere.com/"
internal const val SHARED_PREFERENCES_FILE = "com.dginzbourg.postpc_ex8"
internal const val DB_USERNAME_KEY = "username"
internal const val REQUESTS_DATA_KEY = "data"

fun showToast(context: Context, message: String, duration: Int) {
    val toast = Toast.makeText(context, message, duration)
    toast.show()
}