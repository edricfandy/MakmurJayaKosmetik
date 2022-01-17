package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Store(
    var name: String,
    var id: String,
    var platform: String
) : Parcelable {
    var selected: Boolean = false
}