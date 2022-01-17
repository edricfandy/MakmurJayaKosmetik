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
import com.example.makmurjayakosmetik.classes.Category
import com.example.makmurjayakosmetik.dialogs.CategoryInfoModalBottomSheet
import com.google.android.material.card.MaterialCardView

class RVCategory(val list: ArrayList<Category>, val activity: FragmentActivity) : RecyclerView.Adapter<RVCategory.ViewHolder>() {
    private var countSelected: Int = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var category: Category

        val card = itemView.findViewById<MaterialCardView>(R.id.layoutCategoryView_cardView)
        val txtName = itemView.findViewById<TextView>(R.id.layoutCategoryView_txtName)
        val txtTotalProducts = itemView.findViewById<TextView>(R.id.layoutCategoryView_txtTotalProducts)

        fun broadcastSelectedItem() {
            val intent = Intent("CATEGORY_SELECTED")
            intent.putExtra("SELECTED_STATE", category.selected)
            intent.putExtra("SELECTED_CATEGORY_NAME", category.name)
            itemView.context.sendBroadcast(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_category_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            category = list[position]
            txtName.text = category.name
            txtTotalProducts.text = itemView.context.getString(R.string.total_amount, category.totalProduct.toString())
            card.setChecked(category.selected)

            card.setOnClickListener {
                if (countSelected > 0) {
                    category.selected = !category.selected
                    if (category.selected) countSelected++ else countSelected--
                    card.setChecked(category.selected)
                    broadcastSelectedItem()
                    return@setOnClickListener
                }

                val modalBottomSheet = CategoryInfoModalBottomSheet()
                val bundle = Bundle()
                bundle.putParcelable("CATEGORY", category)
                modalBottomSheet.arguments = bundle
                modalBottomSheet.show(activity.supportFragmentManager, null)
            }

            card.setOnLongClickListener {
                category.selected = !category.selected
                if (category.selected) countSelected++ else countSelected--
                card.setChecked(category.selected)
                broadcastSelectedItem()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}