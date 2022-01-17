package com.example.makmurjayakosmetik.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.ProductDetailsActivity
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.dialogs.AddEditProductDialog
import com.example.makmurjayakosmetik.recyclerview.RVProduct
import com.example.makmurjayakosmetik.recyclerview.RVProductNoPicture
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductFragment : Fragment() {
    private lateinit var db : DBHelper
    private lateinit var listProduct : ArrayList<Product>
    private var filteredProduct : ArrayList<Product> = arrayListOf()

    private lateinit var etSearch : EditText
    private lateinit var btnAddItem : MaterialButton
    private lateinit var txtMsgEmptyItem : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton

    private val manageProductBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "MODIFIED")
                updateRecyclerView()
            else if (p1?.action == "OPEN_PRODUCT_DETAIL") {
                val id = p1.extras?.getString("PRODUCT_ID") ?: ""
                val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
                intent.putExtra("PRODUCT_ID", id)
                Log.e("PRODUCT_ID", id)
                productDetailActivity.launch(intent)
            } else if (p1?.action == "CHANGE_PRODUCT_VIEW")
                updateRecyclerView(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DBHelper(requireContext())

        listProduct = db.getAllProducts()
        Log.e("TOTAL_PRODUCTS", listProduct.size.toString())
        filteredProduct.addAll(listProduct)
        val intentFilter = IntentFilter()
        intentFilter.addAction("MODIFIED")
        intentFilter.addAction("OPEN_PRODUCT_DETAIL")
        intentFilter.addAction("CHANGE_PRODUCT_VIEW")
        requireContext().registerReceiver(manageProductBroadcastReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product, container, false)

        recyclerView = view.findViewById(R.id.manageProduct_recyclerView)
        hiddenLayout = view.findViewById(R.id.manageProduct_layoutEmptyItem)
        txtMsgEmptyItem = view.findViewById(R.id.manageProduct_msgEmptyItem)
        btnAddItem = view.findViewById(R.id.manageProduct_btnAddItem)
        fabAdd = view.findViewById(R.id.manageProduct_fabAdd)
        etSearch = view.findViewById(R.id.manageProduct_etSearch)
        val txtNoItemMatches = view.findViewById<TextView>(R.id.manageProduct_txtNoItemMatches)

        if (listProduct.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Product")
            btnAddItem.text = getString(R.string.add_arg, "Product")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etSearch.text.isNotEmpty()) {
                    filteredProduct.clear()
                    filteredProduct.addAll(listProduct.filter { it.name.lowercase().contains(etSearch.text.toString().lowercase()) || it.id.lowercase().contains(etSearch.text.toString().lowercase()) })
                    if (filteredProduct.isEmpty()) txtNoItemMatches.visibility = View.VISIBLE
                    else txtNoItemMatches.visibility = View.GONE
                    recyclerView.adapter?.notifyDataSetChanged()
                    return
                }

                filteredProduct.clear()
                filteredProduct.addAll(listProduct)
                txtNoItemMatches.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

        recyclerView.apply {
            if (SettingsSharedPreferences(requireContext()).use_image) {
                adapter =  RVProduct(filteredProduct, requireActivity())
                layoutManager = GridLayoutManager(context, 2)
            } else {
                adapter =  RVProductNoPicture(filteredProduct, requireActivity())
                layoutManager = LinearLayoutManager(context)
            }
        }

        btnAddItem.setOnClickListener {
            val dialog = AddEditProductDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        fabAdd.setOnClickListener {
            val dialog = AddEditProductDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        return view
    }

    private val productDetailActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == 0)
            updateRecyclerView()
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(manageProductBroadcastReceiver)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(changeView : Boolean = false) {
        if (changeView) {
            recyclerView.apply {
                adapter = null
                if (SettingsSharedPreferences(requireContext()).use_image) {
                    adapter =  RVProduct(filteredProduct, requireActivity())
                    layoutManager = GridLayoutManager(context, 2)
                } else {
                    adapter =  RVProductNoPicture(filteredProduct, requireActivity())
                    layoutManager = LinearLayoutManager(context)
                }
            }
            return
        }

        listProduct.clear()
        listProduct.addAll(db.getAllProducts())
        filteredProduct.clear()
        filteredProduct.addAll(listProduct)
        if (listProduct.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Product")
            btnAddItem.text = getString(R.string.add_arg, "Product")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        } else if (listProduct.size > 0 && hiddenLayout.visibility == View.VISIBLE) {
            hiddenLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAdd.visibility = View.VISIBLE
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
    }
}