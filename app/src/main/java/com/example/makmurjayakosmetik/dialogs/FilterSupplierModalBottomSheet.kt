package com.example.makmurjayakosmetik.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.makmurjayakosmetik.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FilterSupplierModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var btnSelectCity : MaterialButton
    private lateinit var txtCity : TextView
    private lateinit var btnName : MaterialButton
    private lateinit var btnCity : MaterialButton
    private lateinit var layoutOrder : LinearLayout
    private lateinit var btnOrder : MaterialButton
    private lateinit var btnApply : MaterialButton
    private lateinit var btnReset : MaterialButton

    private var sortBy = ""
    private var order = "asc"

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_filter_supplier, container, false)

        var cities = arrayOf<String>()
        var filterArray = arrayOf<Boolean>()
        sortBy = arguments?.getString("SORT_BY") ?: ""
        order = arguments?.getString("ORDER") ?: "asc"
        if (arguments?.getSerializable("CITIES") != null) {
            (arguments?.getSerializable("CITIES") as ArrayList<*>).apply {
                cities = Array(this.size) { "" }
                for (i in this.indices)
                    cities[i] = this[i] as String
            }
        }
        if (arguments?.getSerializable("FILTER_ARRAY") != null) {
            (arguments?.getSerializable("FILTER_ARRAY") as Array<*>).apply {
                filterArray = Array(this.size) { true }
                for (i in this.indices)
                    filterArray[i] = this[i] as Boolean
            }
        }

        txtCity = view.findViewById(R.id.dialogFilterSupplier_txtCity)
        btnSelectCity = view.findViewById(R.id.dialogFilterSupplier_btnSelectCity)
        btnName = view.findViewById(R.id.dialogFilterSupplier_btnName)
        btnCity = view.findViewById(R.id.dialogFilterSupplier_btnCity)
        layoutOrder = view.findViewById(R.id.dialogFilterSupplier_layoutOrder)
        btnOrder = view.findViewById(R.id.dialogFilterSupplier_btnOrder)
        btnApply = view.findViewById(R.id.dialogFilterSupplier_btnApply)
        btnReset = view.findViewById(R.id.dialogFilterSupplier_btnReset)

        var cityText = ""
        if (filterArray.find { b -> !b } != null) {
            for (i in filterArray.indices)
                cityText += "${cities[i]}, "
            txtCity.text = cityText.substring(0, cityText.length - 2)
        } else txtCity.text = "All"

        btnSelectCity.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select City")
                .setMultiChoiceItems(cities, filterArray.toBooleanArray()) { _: DialogInterface, i: Int, b: Boolean ->
                    filterArray[i] = b
                }
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    if (filterArray.find { b -> !b } != null) {
                        var text = ""
                        for (i in filterArray.indices)
                            text += if (filterArray[i]) cities[i] + ", " else ""
                        txtCity.text = text.substring(0, text.length - 2)
                    } else txtCity.text = "All"
                }
                .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        btnName.setOnClickListener {
            sortBy = "name"
            setSortBy()
        }

        btnCity.setOnClickListener {
            sortBy = "city"
            setSortBy()
        }

        btnOrder.setOnClickListener {
            if (order == "asc") {
                order = "desc"
                btnOrder.text = "Z-A"
            } else {
                order = "asc"
                btnOrder.text = "A-Z"
            }
        }

        btnReset.apply {
            if (!filterArray[0] || sortBy.isNotEmpty()) visibility = View.VISIBLE
            setOnClickListener {
                for (i in filterArray.indices) filterArray[i] = true
                sortBy = ""
                setSortBy()
                order = "asc"
                layoutOrder.visibility = View.GONE
                visibility = View.GONE
            }
        }

        btnApply.setOnClickListener {
            if (filterArray.find { bl -> bl } == null) filterArray[0] = true
            val intent = Intent("FILTER_SUPPLIER")
            intent.putExtra("FILTER_ARRAY", filterArray)
            intent.putExtra("SORT_BY", sortBy)
            intent.putExtra("ORDER", order)
            requireContext().sendBroadcast(intent)
            dismiss()
        }
        setSortBy()

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun setSortBy() {
        btnName.apply {
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.grey))
            setTextColor(requireContext().getColor(R.color.grey))
            setBackgroundColor(requireContext().getColor(R.color.white))
        }
        btnCity.apply {
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
            "city" -> {
                btnCity.apply {
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.white))
                    setTextColor(requireContext().getColor(R.color.white))
                    setBackgroundColor(requireContext().getColor(R.color.blue))
                }
            }
            else -> return
        }

        if (layoutOrder.visibility == View.GONE) {
            layoutOrder.visibility = View.VISIBLE
            if (order == "asc") btnOrder.text = "A-Z"
            else btnOrder.text = "Z-A"
        }
        if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
    }
}