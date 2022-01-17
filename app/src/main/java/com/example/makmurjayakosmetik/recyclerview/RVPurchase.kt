package com.example.makmurjayakosmetik.recyclerview

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.PurchaseDetailsActivity
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SalesDetailsActivity
import com.example.makmurjayakosmetik.classes.Purchase
import com.example.makmurjayakosmetik.classes.Sales
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RVPurchase(private val purchases: ArrayList<Purchase>) : RecyclerView.Adapter<RVPurchase.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var purchase : Purchase
        val dateFormatter = SimpleDateFormat("E, dd MMMM yyyy HH:mm:ss", Locale("in", "ID"))
        val numberFormat : NumberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val currencyFormatter : NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val txtId : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtId)
        val txtDatetime : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtDatetime)
        val txtSupplierName : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtSupplierName)
        val txtSupplierCity : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtSupplierCity)
        val txtTotalItem : MaterialButton = itemView.findViewById(R.id.layoutPurchaseView_txtTotalItem)
        val txtItemCheckedStatus : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtCheckedStatus)
        val txtPaymentMethod : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtPaymentMethod)
        val txtTotalPurchase : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtTotalPurchase)
        val txtPaymentStatus : TextView = itemView.findViewById(R.id.layoutPurchaseView_txtCurrentPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_purchase_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            purchase = purchases[position]
            txtId.text = purchase.id
            txtDatetime.text = dateFormatter.format(purchase.datetime)
            txtSupplierName.text = purchase.supplier.name
            txtSupplierCity.text = purchase.supplier.city
            txtTotalItem.text = itemView.context.getString(R.string.total_items, numberFormat.format(purchase.total_item))
            txtItemCheckedStatus.apply {
                if (purchase.item_checked == "checked") {
                    text = "Checked"
                    setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_true), null, null, null)
                } else {
                    text = "Not Checked"
                    setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_false), null, null, null)
                }
            }
            txtPaymentMethod.text = if (purchase.payment_method == "cash") "Cash" else "Credit"
            txtTotalPurchase.text = currencyFormatter.format(purchase.total_purchase)
            txtPaymentStatus.apply {
                if (purchase.total_purchase - purchase.total_paid <= 0) {
                    text = "Paid Off"
                    setTextColor(itemView.context.getColor(R.color.dark_green))
                } else if (purchase.total_purchase - purchase.total_paid > 0) {
                    text = "In Debt"
                    setTextColor(itemView.context.getColor(R.color.middle_yellow))
                } else {
                    text = "Canceled"
                    setTextColor(itemView.context.getColor(R.color.red))
                }
            }

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PurchaseDetailsActivity::class.java)
                intent.putExtra("PURCHASE_ID", purchase.id)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return purchases.size
    }
}