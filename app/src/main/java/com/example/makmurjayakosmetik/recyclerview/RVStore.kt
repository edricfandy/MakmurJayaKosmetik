package com.example.makmurjayakosmetik.recyclerview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Store
import com.example.makmurjayakosmetik.dialogs.StoreInfoModalBottomSheet
import com.google.android.material.card.MaterialCardView

class RVStore(val list: ArrayList<Store>, val activity: FragmentActivity) : RecyclerView.Adapter<RVStore.ViewHolder>() {
    private var countSelected: Int = 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var store: Store

        val card : MaterialCardView = itemView.findViewById(R.id.layoutStoreView_cardView)
        val txtName : TextView = itemView.findViewById(R.id.layoutStoreView_txtName)
        val txtId : TextView = itemView.findViewById(R.id.layoutStoreView_txtId)
        val txtPlatform : TextView = itemView.findViewById(R.id.layoutStoreView_txtPlatform)
        val imgPlatform : ImageView = itemView.findViewById(R.id.layoutStoreView_imgPlatform)

        fun broadcastSelectedItem() {
            val intent = Intent("STORE_SELECTED")
            intent.putExtra("SELECTED_STATE", store.selected)
            intent.putExtra("SELECTED_STORE", store)
            itemView.context.sendBroadcast(intent)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context)
            .inflate(R.layout.layout_store_view, p0, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.apply {
            store = list[p1]
            txtPlatform.text = store.platform
            when (store.platform) {
                "Offline Store" -> {
                    txtPlatform.visibility = View.VISIBLE
                    txtPlatform.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_store), null, null, null)
                }
                "Facebook" -> {
                    txtPlatform.visibility = View.VISIBLE
                    txtPlatform.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_facebook), null, null, null)
                }
                "Instagram" -> {
                    txtPlatform.visibility = View.VISIBLE
                    txtPlatform.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_instagram), null, null, null)
                }
                "WhatsApp" -> {
                    txtPlatform.visibility = View.VISIBLE
                    txtPlatform.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_whatsapp), null, null, null)
                }
                "Shopee" -> {
                    imgPlatform.visibility = View.VISIBLE
                    imgPlatform.setImageResource(R.mipmap.ic_shopee)
                }
                "Tokopedia" -> {
                    imgPlatform.visibility = View.VISIBLE
                    imgPlatform.setImageResource(R.mipmap.ic_tokopedia)
                }
                else -> txtPlatform.visibility = View.VISIBLE
            }

            txtName.text = store.name
            if (store.id.isEmpty()) txtId.visibility = View.GONE
            else {
                txtId.visibility = View.VISIBLE
                txtId.text = store.id
            }
            card.setChecked(store.selected)

            card.setOnClickListener {
                if (countSelected > 0) {
                    store.selected = !store.selected
                    if (store.selected) countSelected++ else countSelected--
                    card.setChecked(store.selected)
                    broadcastSelectedItem()
                    return@setOnClickListener
                }

                val modalBottomSheet = StoreInfoModalBottomSheet()
                val bundle = Bundle()
                bundle.putParcelable("STORE", store)
                modalBottomSheet.arguments = bundle
                modalBottomSheet.show(activity.supportFragmentManager, null)
            }

            card.setOnLongClickListener {
                store.selected = !store.selected
                if (store.selected) countSelected++ else countSelected--
                card.setChecked(store.selected)
                broadcastSelectedItem()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}