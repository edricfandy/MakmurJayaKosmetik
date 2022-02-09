package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Return(
    var id: String,
    var supplier: Supplier,
    var datetime: Date,
    var item_status: String,
    var compensate_status: String,
    var compensate_method: String,
    var total_compensate: Int,
    var total_item: Int,
    var total_return: Int
) : Parcelable {
    var listProduct: ArrayList<Product> = arrayListOf()
}