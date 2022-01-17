package com.example.makmurjayakosmetik.recyclerview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Product
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class RVSelectedProductNoPicture(val mode: String, private val products: ArrayList<Product> = arrayListOf(), private val difference: ArrayList<Int>? = null) : RecyclerView.Adapter<RVSelectedProductNoPicture.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currencyFormat : NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val txtId : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtId)
        val txtName : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtName)
        val txtPrice : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtPrice)
        val txtTotalItem : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtTotalItem)
        val txtSubtotalPrice : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtSubtotalPrice)

        val txtVariantName : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtVariantName)
        val txtSelectedVariant : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtSelectedVariant)
        val txtVariantNameEdit : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtVariantName2)
        val rvVariantAmount : RecyclerView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_rvVariantAmount)

        val layoutEditPrice : LinearLayout = itemView.findViewById(R.id.layoutProductViewLongNoPicture_layoutEditPrice)
        val etPrice : EditText = itemView.findViewById(R.id.layoutProductViewLongNoPicture_etPrice)

        val layoutEditTotalItem : LinearLayout = itemView.findViewById(R.id.layoutProductViewLongNoPicture_layoutEditTotalItem)
        val txtTotalItemEdit : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtTotalItemEdit)
        val btnMin : MaterialButton = itemView.findViewById(R.id.layoutProductViewLongNoPicture_btnMin)
        val btnPlus : MaterialButton = itemView.findViewById(R.id.layoutProductViewLongNoPicture_btnPlus)

        val txtEditedTotal : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtEditedTotal)
        val txtProductRemoved : TextView = itemView.findViewById(R.id.layoutProductViewLongNoPicture_txtProductRemoved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_product_view_long_no_picture, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.apply {
            txtId.text = product.id
            txtName.text = product.name
            if (mode == "view") {
                txtPrice.text = currencyFormat.format(product.customPrice)
                txtTotalItem.text = product.totalItems.toString()
                txtSubtotalPrice.text = currencyFormat.format(product.totalItems * product.customPrice)
                if (product.variants.size > 0) {
                    itemView.findViewById<LinearLayout>(R.id.layoutProductViewLongNoPicture_layoutVariantAmount).visibility = View.VISIBLE
                    txtVariantName.text = "${product.variant_name}: "
                    var stringSelectedVariant = ""
                    product.variantsChoosed.forEach {
                        if (it.value > 0)
                            stringSelectedVariant += "${it.key}(${it.value}), "
                    }
                    txtSelectedVariant.text = stringSelectedVariant.substring(0, stringSelectedVariant.length - 2)
                }

                if (difference != null) {
                    if (difference[position] > 0)
                        txtEditedTotal.apply {
                            visibility = View.VISIBLE
                            setBackgroundColor(itemView.context.getColor(R.color.green))
                            text = "+${difference[position]}"
                        }
                    else if (difference[position] == 0)
                        txtEditedTotal.apply {
                            visibility = View.VISIBLE
                            setBackgroundColor(itemView.context.getColor(R.color.white))
                            setTextColor(itemView.context.getColor(R.color.black))
                            text = "+${difference[position]}"
                        }
                    else {
                        txtEditedTotal.apply {
                            visibility = View.VISIBLE
                            setBackgroundColor(itemView.context.getColor(R.color.red))
                            text = "-${difference[position]}"
                        }
                        if (product.totalItems > 0)
                            txtProductRemoved.visibility = View.VISIBLE
                    }
                }
            } else {
                txtPrice.visibility = View.GONE
                layoutEditPrice.visibility = View.VISIBLE
                etPrice.setText(product.customPrice.toString())
                etPrice.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (etPrice.text.isEmpty()) product.customPrice = 0
                        else product.customPrice = etPrice.text.toString().toInt()
                        txtSubtotalPrice.text = currencyFormat.format(product.customPrice * product.totalItems)
                    }

                    override fun afterTextChanged(s: Editable?) {  }
                })

                btnMin.setOnClickListener {
                    if (--product.totalItems <= 0) {
                        val dialog = AlertDialog.Builder(itemView.context)
                            .setTitle("Remove Item")
                            .setIcon(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_warning))
                            .setMessage("Are you sure want to remove this item from list?")
                            .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                                val cancelIntent = Intent("PRODUCT_REMOVED")
                                cancelIntent.putExtra("PRODUCT_ID", product.id)
                                itemView.context.sendBroadcast(cancelIntent)
                                dialogInterface.dismiss()
                            }
                            .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                                dialogInterface.dismiss()
                                product.totalItems++
                            }
                        dialog.show()
                        return@setOnClickListener
                    }
                    txtTotalItemEdit.text = product.totalItems.toString()
                    txtSubtotalPrice.text = currencyFormat.format(product.customPrice * product.totalItems)
                    itemView.context.sendBroadcast(Intent("PRODUCT_AMOUNT_CHANGE"))
                }
                btnPlus.setOnClickListener {
                    product.totalItems++
                    txtTotalItemEdit.text = product.totalItems.toString()
                    txtSubtotalPrice.text = currencyFormat.format(product.customPrice * product.totalItems)
                    itemView.context.sendBroadcast(Intent("PRODUCT_AMOUNT_CHANGE"))
                }
                txtSubtotalPrice.text = currencyFormat.format(product.customPrice * product.totalItems)
                itemView.findViewById<TextView>(R.id.layoutProductViewLongNoPicture_lblTotalItem).visibility = View.GONE
                txtTotalItem.visibility = View.GONE

                if (product.variants.size > 0) {
                    itemView.findViewById<LinearLayout>(R.id.layoutProductViewLongNoPicture_layoutEditVariantAmount).visibility = View.VISIBLE
                    txtVariantNameEdit.text = product.variant_name
                    rvVariantAmount.apply {
                        visibility = View.VISIBLE
                        adapter = RVVariantAmount("select", product.variants, product.variantsChoosed)
                        layoutManager = LinearLayoutManager(itemView.context)
                    }
                } else {
                    layoutEditTotalItem.visibility = View.VISIBLE
                    txtTotalItemEdit.text = product.totalItems.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }
}