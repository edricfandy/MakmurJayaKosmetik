package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Purchase(
    var id: String,
    var supplier: Supplier,
    var datetime: Date,
    var item_checked: String,
    var payment_method: String,
    var total_paid: Int,
    var total_item: Int,
    var total_purchase: Int
) : Parcelable {
    @IgnoredOnParcel
    var listProduct: ArrayList<Product> = arrayListOf()
    @IgnoredOnParcel
    var lastEdited: Date = Date()
    @IgnoredOnParcel
    var lastItemCheckedStatusUpdated: Date = Date()
    @IgnoredOnParcel
    var lastItemListUpdated: Date = Date()
}