package com.example.makmurjayakosmetik.recyclerview

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SalesDetailsActivity
import com.example.makmurjayakosmetik.classes.Sales
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RVSales(private val sales: ArrayList<Sales>) : RecyclerView.Adapter<RVSales.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var sales: Sales
        val dateFormatter = SimpleDateFormat("E, dd MMMM yyyy HH:mm:ss", Locale("in", "ID"))
        val numberFormat : NumberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val currencyFormatter : NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val txtId : TextView = itemView.findViewById(R.id.layoutSalesView_txtId)
        val txtDatetime : TextView = itemView.findViewById(R.id.layoutSalesView_txtDatetime)
        val txtMedia : TextView = itemView.findViewById(R.id.layoutSalesView_txtStore)
        val txtTotalItem : TextView = itemView.findViewById(R.id.layoutSalesView_txtTotalItem)
        val txtType : TextView = itemView.findViewById(R.id.layoutSalesView_txtType)
        val txtPaymentStatus : MaterialButton = itemView.findViewById(R.id.layoutSalesView_txtPaymentStatus)
        val txtItemStatus : MaterialButton = itemView.findViewById(R.id.layoutSalesView_txtItemStatus)
        val txtTotal : TextView = itemView.findViewById(R.id.layoutSalesView_txtTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_sales_view, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            sales = this@RVSales.sales[position]
            txtId.text = sales.id
            txtDatetime.text = dateFormatter.format(sales.datetime)
            txtType.text = if (sales.type == "wholesale") "Wholesale" else "Retail"
            txtMedia.apply {
                text = context.getString(R.string.store_name_and_platform, sales.store.name, sales.store.platform)
                when (sales.store.platform) {
                    "Offline Store" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.drawable.ic_store), null, null, null)
                    "WhatsApp" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.drawable.ic_whatsapp), null, null, null)
                    "Facebook" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.drawable.ic_facebook), null, null, null)
                    "Instagram" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.drawable.ic_instagram), null, null, null)
                    "Shopee" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.mipmap.ic_shopee), null, null, null)
                    "Tokopedia" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.mipmap.ic_tokopedia), null, null, null)
                }
            }
            txtPaymentStatus.apply {
                when (sales.payment_status) {
                    "canceled" -> {
                        text = "Canceled"
                        setBackgroundColor(itemView.context.getColor(R.color.dark_red))
                        setTextColor(itemView.context.getColor(R.color.white))
                    }
                    "not_paid_yet" -> {
                        text = "Not Paid Yet"
                        setBackgroundColor(itemView.context.getColor(R.color.middle_yellow))
                        setTextColor(itemView.context.getColor(R.color.dark_brown))
                    }
                    "paid" -> {
                        text = "Paid"
                        setBackgroundColor(itemView.context.getColor(R.color.dark_green))
                        setTextColor(itemView.context.getColor(R.color.white))
                    }
                }
            }
            txtItemStatus.apply {
                when (sales.item_status) {
                    "canceled" -> visibility = View.GONE
                    "waiting_for_pickup_shipping" -> {
                        text = "Waiting for Pickup/Shipping"
                        setBackgroundColor(itemView.context.getColor(R.color.middle_yellow))
                        setTextColor(itemView.context.getColor(R.color.dark_brown))
                    }
                    "picked_up_shipped" -> {
                        text = "Picked Up/Shipped"
                        setBackgroundColor(itemView.context.getColor(R.color.dark_green))
                        setTextColor(itemView.context.getColor(R.color.white))
                    }
                }
            }
            txtTotalItem.text = itemView.context.getString(R.string.total_items, numberFormat.format(sales.total_item))
            txtTotal.text = currencyFormatter.format(sales.total_purchase)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, SalesDetailsActivity::class.java)
                intent.putExtra("SALES_ID", sales.id)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return sales.size
    }
}