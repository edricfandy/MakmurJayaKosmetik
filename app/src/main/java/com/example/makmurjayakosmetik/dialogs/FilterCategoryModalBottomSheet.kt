package com.example.makmurjayakosmetik.dialogs

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.makmurjayakosmetik.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
import java.lang.NumberFormatException
import java.text.NumberFormat
import java.util.*

class FilterCategoryModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var sliderItemAmount: RangeSlider
    private lateinit var btnName : MaterialButton
    private lateinit var btnItemAmount : MaterialButton
    private lateinit var layoutOrder : LinearLayout
    private lateinit var btnOrder : MaterialButton
    private lateinit var btnApply : MaterialButton
    private lateinit var btnReset : MaterialButton

    private var sortBy = ""
    private var order = "asc"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_filter_category, container, false)

        val maxItemAmount = arguments?.getInt("MAX_ITEM_AMOUNT") ?: 0
        val range = arrayOf(0.toFloat(), maxItemAmount.toFloat())
        if (arguments?.getSerializable("ITEM_AMOUNT_RANGE") != null) {
            (arguments?.getSerializable("ITEM_AMOUNT_RANGE") as Array<*>).apply {
                range[0] = (this[0] as Int).toFloat()
                range[1] = (this[1] as Int).toFloat()
            }
        }
        sortBy = arguments?.getString("SORT_BY") ?: ""
        order = arguments?.getString("ORDER") ?: "asc"

        sliderItemAmount = view.findViewById(R.id.dialogFilterCategory_sliderItemAmount)
        btnName = view.findViewById(R.id.dialogFilterCategory_btnName)
        btnItemAmount = view.findViewById(R.id.dialogFilterCategory_btnItemAmount)
        layoutOrder = view.findViewById(R.id.dialogFilterCategory_layoutOrder)
        btnOrder = view.findViewById(R.id.dialogFilterCategory_btnOrder)
        btnApply = view.findViewById(R.id.dialogFilterCategory_btnApply)
        btnReset = view.findViewById(R.id.dialogFilterCategory_btnReset)

        sliderItemAmount.apply {
            valueFrom = 0.toFloat()
            valueTo = maxItemAmount.toFloat()
            values = range.toList()
            setLabelFormatter {
                val format = NumberFormat.getNumberInstance(Locale("in", "ID"))
                format.maximumFractionDigits = 0
                format.format(it)
            }
            addOnChangeListener { slider, value, fromUser ->
                range[0] = values[0]
                range[1] = values[1]
            }
        }

        btnName.setOnClickListener {
            sortBy = "name"
            setSortBy()
        }

        btnItemAmount.setOnClickListener {
            sortBy = "item_amount"
            setSortBy()
        }

        btnOrder.setOnClickListener {
            if (order == "asc") {
                order = "desc"
                btnOrder.text = if (sortBy == "name") "Z-A" else "Many to Less"
            } else {
                order = "asc"
                btnOrder.text = if (sortBy == "name") "A-Z" else "Less to Many"
            }
        }

        btnReset.apply {
            if (sortBy.isNotEmpty()) visibility = View.VISIBLE
            setOnClickListener {
                sortBy = ""
                setSortBy()
                order = "asc"
                layoutOrder.visibility = View.GONE
                visibility = View.GONE
            }
        }

        btnApply.setOnClickListener {
            Log.e("RANGE", "${range[0]} ${range[1]}")
            val intent = Intent("FILTER_CATEGORY")
            intent.putExtra("ITEM_AMOUNT_RANGE", range)
            intent.putExtra("SORT_BY", sortBy)
            intent.putExtra("ORDER", order)
            requireContext().sendBroadcast(intent)
            dismiss()
        }
        setSortBy()

        return view
    }

    private fun setSortBy() {
        btnName.apply {
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.grey))
            setTextColor(requireContext().getColor(R.color.grey))
            setBackgroundColor(requireContext().getColor(R.color.white))
        }
        btnItemAmount.apply {
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.grey))
            setTextColor(requireContext().getColor(R.color.grey))
            setBackgroundColor(requireContext().getColor(R.color.white))
        }

        when (sortBy) {
            "name" -> {
                btnName.apply {
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.white))
                    setTextColor(requireContext().getColor(R.color.white))
                    setBackgroundColor(requireContext().getColor(R.color.blue))
                }
            }
            "item_amount" -> {
                btnItemAmount.apply {
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.white))
                    setTextColor(requireContext().getColor(R.color.white))
                    setBackgroundColor(requireContext().getColor(R.color.blue))
                }
            }
            else -> return
        }

        if (layoutOrder.visibility == View.GONE)
            layoutOrder.visibility = View.VISIBLE

        if (order == "asc")
            btnOrder.text = if (sortBy == "name") "A-Z" else "Less to Many"
        else
            btnOrder.text = if (sortBy == "name") "Z-A" else "Many to Less"

        if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
    }
}