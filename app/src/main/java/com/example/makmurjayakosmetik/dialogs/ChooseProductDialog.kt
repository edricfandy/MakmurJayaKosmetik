package com.example.makmurjayakosmetik.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.BarcodeScannerActivity
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.recyclerview.RVProduct
import com.example.makmurjayakosmetik.recyclerview.RVProductNoPicture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChooseProductDialog : DialogFragment() {
    private lateinit var db : DBHelper
    private lateinit var toolbar : MaterialToolbar
    private lateinit var recyclerView : RecyclerView
    private lateinit var listProduct : ArrayList<Product>
    private lateinit var filteredProduct : ArrayList<Product>
    private lateinit var productChoosed : ArrayList<Product>
    private lateinit var transactionType: String

    @SuppressLint("NotifyDataSetChanged")
    private val itemChoosedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "PRODUCT_ADDED") {
                val product = db.getProductById(p1.extras?.getString("PRODUCT_ID") ?: "")
                if (product != null) {
                    product.totalItems = p1.extras?.getInt("AMOUNT") ?: 0
                    product.customPrice = p1.extras?.getInt("PRICE") ?: product.retail_price
                    if (product.variants.size > 0) {
                        (p1.extras?.getSerializable("VARIANTS_AMOUNT") as HashMap<*, *>).forEach {
                            product.variantsChoosed[it.key as String] = it.value as Int
                        }
                    }
                    var existed = false
                    listProduct.find { it.id == product.id }?.apply {
                        selected = true
                        totalItems = product.totalItems
                        customPrice = product.customPrice
                        if (variants.size > 0)
                            variantsChoosed = product.variantsChoosed
                    }

                    productChoosed.forEach {
                        if (it.id == product.id) {
                            it.selected = true
                            it.totalItems = product.totalItems
                            it.customPrice = product.customPrice
                            if (it.variants.size > 0)
                                it.variantsChoosed = product.variantsChoosed
                            existed = true
                        }
                    }
                    if (!existed) productChoosed.add(product)
                    toolbar.title = "${productChoosed.size} selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            } else if (p1?.action == "PRODUCT_REMOVED") {
                val productId = p1.extras?.getString("PRODUCT_ID")
                if (productId != null) {
                    listProduct.find { it.id == productId }?.selected = false
                    productChoosed.removeAt(productChoosed.indexOf(productChoosed.find { it.id == productId }))
                    toolbar.title = "${productChoosed.size} selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                }
            }
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        db = DBHelper(requireContext())
        listProduct = db.getAllProducts()
        filteredProduct = ArrayList(listProduct)
        productChoosed = arrayListOf()
        val products = arguments?.getSerializable("PRODUCTS_LIST")
        if (products != null) {
            if ((products as HashMap<*, *>).size > 0)
                products.forEach {
                    val product = db.getProductById(it.key as String)
                    if (product != null) {
                        product.totalItems = (it.value as Array<*>)[0] as Int
                        product.customPrice = (it.value as Array<*>)[1] as Int
                        productChoosed.add(product)
                        listProduct.find { pd -> pd.id == product.id }?.apply {
                            selected = true
                            totalItems = product.totalItems
                            customPrice = product.customPrice
                        }
                    }
                }
        }

        val productsWithVariants = arguments?.getSerializable("PRODUCTS_LIST_WITH_VARIANT")
        if (productsWithVariants != null)
            if ((productsWithVariants as HashMap<*, *>).size > 0) {
                val variantsAmount = hashMapOf<String, Int>()
                productsWithVariants.forEach {
                    (it.value as HashMap<*, *>).forEach { ins ->
                        variantsAmount[ins.key as String] = ins.value as Int
                    }
                    productChoosed.find { pd -> pd.id == it.key as String }?.variantsChoosed = variantsAmount
                    listProduct.find { pd -> pd.id == it.key as String }?.variantsChoosed = variantsAmount
                }
            }
        transactionType = arguments?.getString("TRANSACTION_TYPE") ?: ""

        val intentFilter = IntentFilter("PRODUCT_ADDED")
        intentFilter.addAction("PRODUCT_REMOVED")
        requireContext().registerReceiver(itemChoosedBroadcastReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_choose_product, container, false)

        toolbar = view.findViewById(R.id.dialogChooseProduct_toolbar)
        recyclerView = view.findViewById(R.id.dialogChooseProduct_recyclerView)

        val etSearch = view.findViewById<EditText>(R.id.dialogChooseProduct_etSearch)
        val txtNoItemMatches = view.findViewById<TextView>(R.id.dialogChooseProduct_txtNoItemMatches)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.dialogChooseProduct_btnConfirm)
        val fabAddProduct = view.findViewById<FloatingActionButton>(R.id.dialogChooseProduct_fabAddProduct)
        val fabOpenList = view.findViewById<FloatingActionButton>(R.id.dialogChooseProduct_fabOpenList)
        val fabScanBarcode = view.findViewById<FloatingActionButton>(R.id.dialogChooseProduct_fabScanBarcode)

        if (transactionType == "purchase") fabAddProduct.visibility = View.VISIBLE

        toolbar.title = "${productChoosed.size} selected"
        toolbar.setNavigationOnClickListener {
            if (productChoosed.size > 0) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Discard added item")
                    .setIcon(
                        AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.ic_warning
                        )
                    )
                    .setMessage("Are you sure want to discard all the items added for the sales transaction?")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                        dismiss()
                    }
                    .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialog.show()
                return@setNavigationOnClickListener
            }
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val products = HashMap<String, Array<Int>>()
            val productsWithVariant = HashMap<String, HashMap<String, Int>>()

            productChoosed.forEach {
                products[it.id] = arrayOf(it.totalItems, it.customPrice)
                if (it.variants.size > 0) {
                    productsWithVariant[it.id] = it.variantsChoosed
                }
            }
            val intent = Intent("PRODUCT_CHOOSED_LIST")
            intent.putExtra("PRODUCTS_LIST", products)
            intent.putExtra("PRODUCTS_LIST_WITH_VARIANTS", productsWithVariant)
            requireContext().sendBroadcast(intent)
            Toast.makeText(requireContext(), "${productChoosed.size} item(s) added to the sales transaction.", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
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

            override fun afterTextChanged(s: Editable?) {  }
        })

        if (listProduct.size <= 0)
            view.apply {
                findViewById<LinearLayout>(R.id.dialogChooseProduct_layoutEmptyItem).visibility = View.VISIBLE
                findViewById<TextView>(R.id.dialogChooseProduct_msgEmptyItem).text = getString(R.string.item_empty_message, "Product")
            }

        recyclerView.apply {
            if (SettingsSharedPreferences(requireContext()).use_image) {
                adapter =  RVProduct(filteredProduct, requireActivity(), true, transactionType)
                layoutManager = GridLayoutManager(context, 2)
            } else {
                adapter =  RVProductNoPicture(filteredProduct, requireActivity(), true, transactionType)
                layoutManager = LinearLayoutManager(context)
            }
        }

        fabAddProduct.setOnClickListener {
            val dialog = AddEditProductDialog("add")
            dialog.show(requireActivity().supportFragmentManager, "ADD_PRODUCT")
        }

        fabOpenList.setOnClickListener {
            val dialog = SelectedProductListDialog("edit", productChoosed)
            dialog.show(requireActivity().supportFragmentManager, "LIST_SELECTED_PRODUCT")
        }

        fabScanBarcode.setOnClickListener {
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            barcodeScannerActivityResult.launch(intent)
        }

        return view
    }

    private val barcodeScannerActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK && it.data != null) {
            val productId = it.data?.extras?.getString("SCAN_RESULT") ?: ""
            val product = listProduct.find { prod -> prod.id == productId }
            if (product == null) {
                if (transactionType == "purchase") {
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Product not found")
                        .setMessage("Product with ID $productId not found on your product list. Add this product to your product list?")
                        .setPositiveButton("CONFIRM") { dialogInterface : DialogInterface, _: Int ->
                            val addProductDialog = AddEditProductDialog("add")
                            val bundle = Bundle()
                            bundle.putString("PRODUCT_ID", productId)
                            bundle.putBoolean("PURCHASE", true)
                            addProductDialog.arguments = bundle
                            addProductDialog.show(requireActivity().supportFragmentManager, "ADD_PRODUCT")
                            dialogInterface.dismiss()
                        }
                        .setNegativeButton("CANCEL") { dialogInterface : DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()
                    dialog.show()
                    return@registerForActivityResult
                }
                Toast.makeText(requireContext(), "The product is not registered in your system. Add the product to your product list before you can include it in your sales transaction.", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            val modalBottomSheet = ManageTransactionProductModalBottomSheet()
            val bundle = Bundle()
            bundle.putString("PRODUCT_ID", productId)
            bundle.putString("TRANSACTION_TYPE", transactionType)
            if (product.selected) {
                bundle.putBoolean("SELECTED", true)
                bundle.putInt("AMOUNT", product.totalItems)
                bundle.putInt("PRICE", product.customPrice)
            }
            modalBottomSheet.arguments = bundle
            modalBottomSheet.show(requireActivity().supportFragmentManager,"Choose_Item")
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(itemChoosedBroadcastReceiver)
    }
}