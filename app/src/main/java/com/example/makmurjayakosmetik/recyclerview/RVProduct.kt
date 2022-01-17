package com.example.makmurjayakosmetik.recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Product
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.example.makmurjayakosmetik.dialogs.ManageTransactionProductModalBottomSheet


class RVProduct(private val listProduct: ArrayList<Product>, private val activity: FragmentActivity, private val chooser: Boolean = false, private val transactionType: String = "") : RecyclerView.Adapter<RVProduct.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var product : Product

        val imgProduct : ImageView = itemView.findViewById(R.id.layoutProductView_img)
        val msgImage : TextView = itemView.findViewById(R.id.layoutProductView_msgImageUnavailable)
        val txtId : TextView = itemView.findViewById(R.id.layoutProductView_txtId)
        val txtName : TextView = itemView.findViewById(R.id.layoutProductView_txtName)
        val txtStock : TextView = itemView.findViewById(R.id.layoutProductView_txtStock)
        val layoutSelected : LinearLayout = itemView.findViewById(R.id.layoutProductView_layoutSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            product = listProduct[position]

            if (product.images[0].isEmpty()) {
                msgImage.visibility = View.VISIBLE
                msgImage.text = itemView.context.getString(R.string.no_pic)
            } else {
                val bitmap: Bitmap
                try {
                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(itemView.context.contentResolver, product.images[0].toUri()))
                    else
                        MediaStore.Images.Media.getBitmap(itemView.context.contentResolver, product.images[0].toUri())
                    imgProduct.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    msgImage.visibility = View.VISIBLE
                }
            }

            txtId.text = product.id
            txtName.text = product.name
            txtStock.text = product.total_stock.toString()
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
