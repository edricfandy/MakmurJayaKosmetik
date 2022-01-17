package com.example.makmurjayakosmetik.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProduct
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProductNoPicture
import com.google.android.material.appbar.MaterialToolbar

class SelectedProductListDialog(private val mode: String, private val productChoosed : ArrayList<Product>) : DialogFragment() {
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.apply {
                setLayout(width, height)
                setWindowAnimations(R.style.FullScreenDialogAnimation)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_transaction_selected_product_list, container, false)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.dialogTransactionSelectedProductList_toolbar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.dialogTransactionSelectedProductList_recyclerView)
        val txtNoItems = view.findViewById<TextView>(R.id.dialogTransactionSelectedProductList_txtNoItems)

        toolbar.setNavigationOnClickListener {
            dismiss()
        }

        if (productChoosed.size <= 0) {
            txtNoItems.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.apply {
                adapter = null
                if (SettingsSharedPreferences(requireContext()).use_image)
                    adapter = RVSelectedProduct(mode, productChoosed)
                else
                    adapter = RVSelectedProductNoPicture(mode, productChoosed)
                layoutManager = LinearLayoutManager(requireContext())
            }
            txtNoItems.visibility = View.GONE
        }

        return view
    }
}