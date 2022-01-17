package com.example.makmurjayakosmetik.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Store
import com.example.makmurjayakosmetik.classes.Supplier
import com.example.makmurjayakosmetik.dialogs.StoreInfoModalBottomSheet
import com.example.makmurjayakosmetik.dialogs.SupplierInfoModalBottomSheet
import com.google.android.material.card.MaterialCardView

class RVSupplier(val list: ArrayList<Supplier>, val activity: FragmentActivity) : RecyclerView.Adapter<RVSupplier.ViewHolder>() {
    private var countSelected: Int = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var supplier: Supplier

        val card = itemView.findViewById<MaterialCardView>(R.id.layoutSupplierView_cardView)
        val txtName = itemView.findViewById<TextView>(R.id.layoutSupplierView_txtName)
        val txtPhoneNum = itemView.findViewById<TextView>(R.id.layoutSupplierView_txtPhoneNum)
        val txtCity = itemView.findViewById<TextView>(R.id.layoutSupplierView_txtCity)

        fun broadcastSelectedItem() {
            val intent = Intent("SUPPLIER_SELECTED")
            intent.putExtra("SELECTED_STATE", supplier.selected)
            intent.putExtra("SELECTED_SUPPLIER_ID", supplier.id)
            itemView.context.sendBroadcast(intent)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.layout_supplier_view, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.apply {
            supplier = list[p1]
            txtName.text = supplier.name
            txtPhoneNum.text = if (supplier.phone_num.isEmpty()) "Phone number not set" else supplier.phone_num
            txtCity.text = supplier.city
            card.setChecked(supplier.selected)

            card.setOnClickListener {
                if (countSelected > 0) {
                    supplier.selected = !supplier.selected
                    if (supplier.selected) countSelected++ else countSelected--
                    card.setChecked(supplier.selected)
                    broadcastSelectedItem()
                    return@setOnClickListener
                }

                val modalBottomSheet = SupplierInfoModalBottomSheet()
                val bundle = Bundle()
                bundle.putParcelable("SUPPLIER", supplier)
                modalBottomSheet.arguments = bundle
                modalBottomSheet.show(activity.supportFragmentManager, null)
            }

            card.setOnLongClickListener {
                supplier.selected = !supplier.selected
                if (supplier.selected) countSelected++ else countSelected--
                card.setChecked(supplier.selected)
                broadcastSelectedItem()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}