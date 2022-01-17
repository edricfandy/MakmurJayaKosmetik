package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Payment(
    val id: String,
    val purchase_id: String,
    val datetime: Date,
    val total_paid: Int,
    val message: String
) : Parcelable