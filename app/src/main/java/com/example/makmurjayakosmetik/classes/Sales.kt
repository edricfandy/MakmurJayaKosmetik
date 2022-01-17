package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Sales (
    var id: String,
    var store: Store,
    var type: String,
    var payment_status: String,
    var item_status: String,
    var datetime: Date,
    var total_item: Int,
    var total_purchase: Int
) : Parcelable {
    var listProduct : ArrayList<Product> = arrayListOf()
}