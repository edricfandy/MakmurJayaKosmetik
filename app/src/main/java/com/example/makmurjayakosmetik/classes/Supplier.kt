package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Supplier(
    var id: String,
    var name: String,
    var address: String,
    var city: String,
    var phone_num: String,
    var email: String
) : Parcelable {
    var selected: Boolean = false
}