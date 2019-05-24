package com.dginzbourg.postpc

import android.content.Context
import android.widget.Toast

internal const val SERVER_BASE_URL = "http://hujipostpc2019.pythonanywhere.com/"
internal const val SERVER_USERS_URL = "users/"
internal const val SERVER_TOKEN_URL = "token/"
internal const val SERVER_USER_URL = "user/"
internal const val SERVER_EDIT_URL = "edit/"
internal const val SHARED_PREFERENCES_FILE = "com.dginzbourg.postpc_ex8"
internal const val DB_USERNAME_KEY = "username"
internal const val REQUESTS_DATA_KEY = "data"
internal const val REQUESTS_PRETTY_NAME_KEY = "pretty_name"
internal const val REQUESTS_IMAGE_URL_KEY = "image_url"

fun showToast(context: Context, message: String, duration: Int) {
    val toast = Toast.makeText(context, message, duration)
    toast.show()
}