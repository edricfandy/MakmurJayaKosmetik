package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Account(
    var username: String,
    var name : String
) : Parcelable {
    var password: String = ""
    var hasPassword: Boolean = false
}