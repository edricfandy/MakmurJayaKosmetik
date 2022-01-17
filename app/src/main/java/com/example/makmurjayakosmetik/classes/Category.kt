package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    var name: String,
    var desc: String
) : Parcelable {
    var selected: Boolean = false
    var totalProduct: Int = 0
}