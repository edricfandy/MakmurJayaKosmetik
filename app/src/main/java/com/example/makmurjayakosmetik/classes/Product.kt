package com.example.makmurjayakosmetik.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product (
    var id: String,
    var name: String,
    var category: Category,
    var capital_price: Int,
    var wholesale_price: Int,
    var retail_price: Int,
    var total_stock: Int,
    var variant_name: String,
    var variants: ArrayList<Pair<String, Int>>,
    var supplier: Supplier
) : Parcelable {
    var images = arrayListOf("")
    var selected = false
    var totalItems = 0
    var customPrice = 0
    var variantsChoosed : HashMap<String, Int> = hashMapOf()
}