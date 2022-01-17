package com.example.makmurjayakosmetik.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.dialogs.ManageTransactionProductModalBottomSheet
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class RVProductNoPicture(private val listProduct: ArrayList<Product>, private val activity: FragmentActivity, private val chooser: Boolean = false, private val transactionType: String = "") : RecyclerView.Adapter<RVProductNoPicture.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var product : Product
        val currencyFormat : NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val txtId : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtId)
        val txtName : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtName)
        val txtStock : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtTotalStock)
        val txtCapitalPrice : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtCapitalPrice)
        val txtWholesalePrice : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtWholesalePrice)
        val txtRetailPrice : TextView = itemView.findViewById(R.id.layoutProductViewNoPic_txtRetailPrice)
        val layoutSelected : LinearLayout = itemView.findViewById(R.id.layoutProductViewNoPic_layoutSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_view_no_picture, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            product = listProduct[position]

            txtId.text = product.id
            txtName.text = product.name
            txtStock.text = product.total_stock.toString()
            txtCapitalPrice.text = currencyFormat.format(product.capital_price)
            txtWholesalePrice.text = currencyFormat.format(product.wholesale_price)
            txtRetailPrice.text = currencyFormat.format(product.retail_price)

            if (chooser && product.selected)
                layoutSelected.visibility = View.VISIBLE
            else
                layoutSelected.visibility = View.GONE

            itemView.setOnClickListener {
                if (chooser) {
                    val modalBottomSheet = ManageTransactionProductModalBottomSheet()
                    val bundle = Bundle()
                    bundle.putString("PRODUCT_ID", product.id)
                    bundle.putString("TRANSACTION_TYPE", transactionType)
                    if (product.selected) {
                        bundle.putBoolean("SELECTED", true)
                        bundle.putInt("AMOUNT", product.totalItems)
                        bundle.putInt("PRICE", product.customPrice)
                        if (product.variants.size > 0)
                            bundle.putSerializable("VARIANTS_AMOUNT", product.variantsChoosed)
                    }
                    modalBottomSheet.arguments = bundle
                    modalBottomSheet.show(activity.supportFragmentManager,"Choose_Item")
                } else {
                    val intent = Intent("OPEN_PRODUCT_DETAIL")
                    intent.putExtra("PRODUCT_ID", product.id)
                    it.context.sendBroadcast(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listProduct.size
    }
}