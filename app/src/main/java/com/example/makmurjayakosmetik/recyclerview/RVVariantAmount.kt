package com.example.makmurjayakosmetik.recyclerview

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class RVVariantAmount(val mode: String, var listVariantAmount: ArrayList<Pair<String, Int>>, var listVariantSelected: MutableMap<String, Int>) : RecyclerView.Adapter<RVVariantAmount.ViewHolder>() {
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val numberFormat : NumberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        val layoutInput : LinearLayout = itemView.findViewById(R.id.layoutVariantAmountView_layoutInput)
        val layoutInfo : LinearLayout = itemView.findViewById(R.id.layoutVariantAmountView_layoutInfo)
        val layoutEditAmount : LinearLayout = itemView.findViewById(R.id.layoutVariantAmountView_layoutEditAmount)

        val etVariant : EditText = itemView.findViewById(R.id.layoutVariantAmountView_etVariant)
        val etAmount : EditText = itemView.findViewById(R.id.layoutVariantAmountView_etAmount)
        val btnRemove : ImageView = itemView.findViewById(R.id.layoutVariantAmountView_btnRemove)
        val txtVariant : TextView = itemView.findViewById(R.id.layoutVariantAmountView_txtVariant)
        val txtAmount : TextView = itemView.findViewById(R.id.layoutVariantAmountView_txtAmount)
        val txtAmountEdit : TextView = itemView.findViewById(R.id.layoutVariantAmountView_txtTotalItemEdit)
        val btnPlus : MaterialButton = itemView.findViewById(R.id.layoutVariantAmountView_btnPlus)
        val btnMin : MaterialButton = itemView.findViewById(R.id.layoutVariantAmountView_btnMin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_variant_amount_view, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val ins = listVariantAmount[position]
            if (mode == "add" || mode == "edit") {
                layoutInfo.visibility = View.GONE
                if (position == 0)
                    btnRemove.visibility = View.INVISIBLE

                etVariant.setText(ins.first)
                etVariant.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        listVariantAmount[layoutPosition] = Pair(p0.toString(), etAmount.text.toString().toInt())
                    }

                    override fun afterTextChanged(p0: Editable?) { }
                })

                etAmount.setText(numberFormat.format(ins.second))
                etAmount.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        if (p0.isNullOrEmpty()) listVariantAmount[layoutPosition] = Pair(etVariant.text.toString(), 0)
                        else listVariantAmount[layoutPosition] = Pair(etVariant.text.toString(), p0.toString().toInt())
                        val intent = Intent("COUNT_TOTAL_STOCK")
                        itemView.context.sendBroadcast(intent)
                    }

                    override fun afterTextChanged(p0: Editable?) { }
                })
                etAmount.setOnFocusChangeListener { v, hasFocus ->
                    if (!hasFocus) {
                        (v as EditText).apply {
                            if (text.isNotEmpty())
                                setText(numberFormat.format(text.toString().toInt()).toString())
                        }
                        return@setOnFocusChangeListener
                    }

                    (v as EditText).apply {
                        if (text.isNotEmpty())
                            setText((numberFormat.parse(text.toString()) ?: 0).toString())
                    }
                }

                btnRemove.setOnClickListener {
                    listVariantAmount.removeAt(layoutPosition)
                    notifyItemRemoved(layoutPosition)
                }
            } else if (mode == "select") {
                layoutInput.visibility = View.GONE
                txtAmount.visibility = View.GONE
                layoutEditAmount.visibility = View.VISIBLE
                txtVariant.text = "${ins.first}(${ins.second})"

                val amountSelected = listVariantSelected[ins.first]
                txtAmountEdit.text = numberFormat.format(amountSelected ?: 0)
                btnMin.setOnClickListener {
                    listVariantSelected[ins.first] = (listVariantSelected[ins.first] ?: 0) - 1
                    if ((listVariantSelected[ins.first] ?: 0) <= 0)
                        listVariantSelected[ins.first] = 0
                    txtAmountEdit.text = numberFormat.format(listVariantSelected[ins.first])
                    val intent = Intent("CHANGE_AMOUNT")
                    intent.putExtra("AMOUNT", countAmount())
                    itemView.context.sendBroadcast(intent)
                }
                btnPlus.setOnClickListener {
                    listVariantSelected[ins.first] = (listVariantSelected[ins.first] ?: 0) + 1
                    if ((listVariantSelected[ins.first] ?: ins.second) >= ins.second)
                        listVariantSelected[ins.first] = ins.second
                    txtAmountEdit.text = numberFormat.format(listVariantSelected[ins.first])
                    val intent = Intent("CHANGE_AMOUNT")
                    intent.putExtra("AMOUNT", countAmount())
                    itemView.context.sendBroadcast(intent)
                }
            } else {
                layoutInput.visibility = View.GONE
                txtVariant.text = ins.first
                txtAmount.text = ins.second.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return listVariantAmount.size
    }

    private fun countAmount() : Int {
        var amount = 0
        listVariantSelected.forEach {
            amount += it.value
        }
        return amount
    }

    fun retriveData() : ArrayList<Pair<String, Int>> {
        return listVariantAmount
    }

    fun retriveSelectedAmountData() : MutableMap<String, Int> {
        return listVariantSelected
    }
}