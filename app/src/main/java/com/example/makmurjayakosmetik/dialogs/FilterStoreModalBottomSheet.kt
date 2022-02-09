package com.example.makmurjayakosmetik.dialogs

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.makmurjayakosmetik.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FilterStoreModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var cbAll : CheckBox
    private lateinit var cbOfflineStore : CheckBox
    private lateinit var cbFacebook : CheckBox
    private lateinit var cbInstagram : CheckBox
    private lateinit var cbWhatsApp : CheckBox
    private lateinit var cbShopee : CheckBox
    private lateinit var cbTokopedia : CheckBox
    private lateinit var cbOther : CheckBox
    private lateinit var btnName : MaterialButton
    private lateinit var btnAddressId : MaterialButton
    private lateinit var layoutOrder : LinearLayout
    private lateinit var btnOrder : MaterialButton
    private lateinit var btnApply : MaterialButton
    private lateinit var btnReset : MaterialButton

    private var sortBy = ""
    private var order = "asc"
    private val filterArray = arrayOf(true, false, false, false, false, false, false, false)

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_filter_store, container, false)

        sortBy = arguments?.getString("SORT_BY") ?: ""
        order = arguments?.getString("ORDER") ?: "asc"
        if (arguments?.getSerializable("FILTER_ARRAY") != null) {
            (arguments?.getSerializable("FILTER_ARRAY") as Array<*>).apply {
                for (i in this.indices)
                    filterArray[i] = this[i] as Boolean
            }
        }

        cbAll = view.findViewById(R.id.dialogFilterStore_cbAll)
        cbOfflineStore = view.findViewById(R.id.dialogFilterStore_cbOfflineStore)
        cbFacebook = view.findViewById(R.id.dialogFilterStore_cbFacebook)
        cbInstagram = view.findViewById(R.id.dialogFilterStore_cbInstagram)
        cbWhatsApp = view.findViewById(R.id.dialogFilterStore_cbWhatsApp)
        cbShopee = view.findViewById(R.id.dialogFilterStore_cbShopee)
        cbTokopedia = view.findViewById(R.id.dialogFilterStore_cbTokopedia)
        cbOther = view.findViewById(R.id.dialogFilterStore_cbOther)
        btnName = view.findViewById(R.id.dialogFilterStore_btnName)
        btnAddressId = view.findViewById(R.id.dialogFilterStore_btnAddressId)
        layoutOrder = view.findViewById(R.id.dialogFilterStore_layoutOrder)
        btnOrder = view.findViewById(R.id.dialogFilterStore_btnOrder)
        btnApply = view.findViewById(R.id.dialogFilterStore_btnApply)
        btnReset = view.findViewById(R.id.dialogFilterStore_btnReset)

        cbAll.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[0] = true
                    uncheckAll()
                    enableDisableAll(false)
                    if (sortBy.isEmpty()) btnReset.visibility = View.GONE
                } else {
                    filterArray[0] = false
                    enableDisableAll(true)
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                }
            }
            isChecked = filterArray[0]
        }

        cbOfflineStore.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[1] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[1] = false
            }
            isChecked = filterArray[1]
        }

        cbFacebook.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[2] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[2] = false
            }
            isChecked = filterArray[2]
        }

        cbInstagram.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[3] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[3] = false
            }
            isChecked = filterArray[3]
        }

        cbWhatsApp.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[4] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[4] = false
            }
            isChecked = filterArray[4]
        }

        cbShopee.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[5] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[5] = false
            }
            isChecked = filterArray[5]
        }

        cbTokopedia.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[6] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[6] = false
            }
            isChecked = filterArray[6]
        }

        cbOther.apply {
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    filterArray[7] = true
                    if (btnReset.visibility == View.GONE) btnReset.visibility = View.VISIBLE
                    if (allChecked()) cbAll.isChecked = true
                } else filterArray[7] = false
            }
            isChecked = filterArray[7]
        }

        btnName.setOnClickListener {
            sortBy = "name"
            setSortBy()
        }

        btnAddressId.setOnClickListener {
            sortBy = "address_id"
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
                cbAll.isChecked = true
                sortBy = ""
                setSortBy()
                order = "asc"
                layoutOrder.visibility = View.GONE
                visibility = View.GONE
            }
        }

        btnApply.setOnClickListener {
            if (filterArray.find { bl -> bl } == null) filterArray[0] = true
            val intent = Intent()
            intent.action = if (filterArray[0] && sortBy.isEmpty() && order == "asc") "RESET_FILTER" else "FILTER_STORE"

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
        btnAddressId.apply {
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
            "address_id" -> {
                btnAddressId.apply {
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

    private fun uncheckAll() {
        cbOfflineStore.isChecked = false
        cbFacebook.isChecked = false
        cbInstagram.isChecked = false
        cbWhatsApp.isChecked = false
        cbShopee.isChecked = false
        cbTokopedia.isChecked = false
        cbOther.isChecked = false
    }

    private fun enableDisableAll(enable: Boolean) {
        cbOfflineStore.isEnabled = enable
        cbFacebook.isEnabled = enable
        cbInstagram.isEnabled = enable
        cbWhatsApp.isEnabled = enable
        cbShopee.isEnabled = enable
        cbTokopedia.isEnabled = enable
        cbOther.isEnabled = enable
    }

    private fun allChecked() : Boolean {
        return cbOfflineStore.isChecked
                && cbFacebook.isChecked
                && cbInstagram.isChecked
                && cbWhatsApp.isChecked
                && cbShopee.isChecked
                && cbTokopedia.isChecked
                && cbOther.isChecked
    }
}