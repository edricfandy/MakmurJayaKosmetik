package com.example.makmurjayakosmetik.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.classes.Sales
import com.example.makmurjayakosmetik.classes.Store
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProduct
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProductNoPicture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddEditSalesDialog(private val mode: String) : DialogFragment() {
    private var sales: Sales? = null
    private var type: String = ""
    private lateinit var db : DBHelper

    private lateinit var listProduct : ArrayList<Product>
    private lateinit var listTextStore : ArrayList<String>
    private lateinit var numberFormat: NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private lateinit var dateTimeShowFormat : SimpleDateFormat

    private lateinit var rvItemList : RecyclerView
    private lateinit var btnWholesale : MaterialButton
    private lateinit var btnRetail : MaterialButton
    private lateinit var txtNoItems : TextView
    private lateinit var txtTotalItem : TextView
    private lateinit var txtTotalPurchase : TextView

    private lateinit var dateFormat : SimpleDateFormat

    private val choosedProductListBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "PRODUCT_CHOOSED_LIST") {
                val products = intent.extras?.getSerializable("PRODUCTS_LIST")
                val productsWithVariant = intent.extras?.getSerializable("PRODUCTS_LIST_WITH_VARIANTS")
                if (products != null) {
                    if ((products as HashMap<*, *>).size <= 0) {
                        rvItemList.visibility = View.GONE
                        txtNoItems.visibility = View.VISIBLE
                        return
                    }

                    rvItemList.visibility = View.VISIBLE
                    txtNoItems.visibility = View.GONE
                    listProduct.clear()
                    products.forEach {
                        val product = db.getProductById(it.key as String)
                        if (product != null) {
                            product.totalItems = (it.value as Array<*>)[0] as Int
                            product.customPrice = (it.value as Array<*>)[1] as Int
                            if (productsWithVariant != null) {
                                val variantsAmount = (productsWithVariant as HashMap<*, *>)[product.id]
                                if (variantsAmount != null)
                                    (variantsAmount as HashMap<*, *>).forEach { v ->
                                        product.variantsChoosed[v.key as String] = v.value as Int
                                    }
                            }
                            listProduct.add(product)
                        }
                    }
                    updateRecyclerView()
                    updateTotalItemsAndPurchase()
                }
            } else if (intent?.action == "PRODUCT_AMOUNT_CHANGE") {
                updateTotalItemsAndPurchase()
            } else if (intent?.action == "PRODUCT_REMOVED") {
                val productId = intent.extras?.getString("PRODUCT_ID")
                if (productId != null) {
                    listProduct.find { it.id == productId }?.selected = false
                    listProduct.removeAt(listProduct.indexOf(listProduct.find { it.id == productId }))
                    updateRecyclerView()
                    updateTotalItemsAndPurchase()
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
        sales = db.getSalesById(arguments?.getString("SALES_ID") ?: "")
        listProduct = sales?.listProduct ?: arrayListOf()
        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        dateTimeShowFormat = SimpleDateFormat("E, dd MMM yyyy HH:mm:ss", Locale("in", "ID"))

        val intentFilter = IntentFilter()
        intentFilter.addAction("PRODUCT_CHOOSED_LIST")
        intentFilter.addAction("PRODUCT_REMOVED")
        intentFilter.addAction("PRODUCT_AMOUNT_CHANGE")
        requireContext().registerReceiver(choosedProductListBroadcastReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_edit_sales, container, false)

        val toolbar : MaterialToolbar = view.findViewById(R.id.dialogAddEditSales_toolbar)
        val btnConfirm : MaterialButton = view.findViewById(R.id.dialogAddEditSales_btnConfirm)

        val txtHeader = view.findViewById<TextView>(R.id.dialogAddEditSales_txtHeading)
        val etId = view.findViewById<EditText>(R.id.dialogAddEditSales_etId)
        val cbAutoGenId = view.findViewById<CheckBox>(R.id.dialogAddEditSales_cbAutoGenId)
        val ddStore = view.findViewById<AutoCompleteTextView>(R.id.dialogAddEditSales_ddStore)
        val txtNote = view.findViewById<TextView>(R.id.dialogAddEditSales_txtNote)
        val btnManageItem = view.findViewById<MaterialButton>(R.id.dialogAddEditSales_btnManageItem)

        rvItemList = view.findViewById(R.id.dialogAddEditSales_recyclerviewItemList)
        btnWholesale = view.findViewById(R.id.dialogAddEditSales_btnWholesale)
        btnRetail = view.findViewById(R.id.dialogAddEditSales_btnRetail)
        txtNoItems = view.findViewById(R.id.dialogAddEditSales_txtNoItems)
        txtTotalItem = view.findViewById(R.id.dialogAddEditSales_txtTotalItem)
        txtTotalPurchase = view.findViewById(R.id.dialogAddEditSales_txtTotalPurchase)

        listTextStore = arrayListOf()
        db.getAllStore().forEach {
            listTextStore.add("${it.name} (${it.platform})")
        }
        ddStore.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listTextStore))

        cbAutoGenId.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etId.isEnabled = false
                etId.setText(db.generateSalesId())
            } else {
                etId.isEnabled = true
            }
        }

        if (mode == "add") {
            txtHeader.text = getString(R.string.add_arg, "Sales Transaction")
            btnConfirm.text = getString(R.string.add)
            cbAutoGenId.isChecked = true
        } else {
            txtHeader.text = getString(R.string.edit_arg, "Sales Transaction")
            btnConfirm.text = getString(R.string.edit)
            etId.setText(sales?.id)
            cbAutoGenId.apply {
                isChecked = false
                isEnabled = false
            }
            type = sales?.type ?: ""
            changeTransactionType()
            ddStore.setText(getString(R.string.store_name_and_platform, sales?.store?.name, sales?.store?.platform))
            txtNote.visibility = View.VISIBLE

            if (sales?.listProduct != null) {
                if (sales!!.listProduct.size > 0) {
                    txtNoItems.visibility = View.GONE
                    rvItemList.visibility = View.VISIBLE
                    txtTotalItem.text = numberFormat.format(sales?.total_item)
                    txtTotalPurchase.text = currencyFormat.format(sales?.total_purchase)
                }
            } else {
                txtNoItems.visibility = View.VISIBLE
                rvItemList.visibility = View.GONE
            }
        }

        toolbar.setNavigationOnClickListener {
            if ((!cbAutoGenId.isChecked && etId.text.isNotBlank()) || type.isNotEmpty() || ddStore.text.isNotEmpty() || listProduct.size > 0) {
                if (etId.text.toString() == sales?.id && ddStore.text.toString() == getString(R.string.store_name_and_platform, sales?.store?.name, sales?.store?.platform)) {
                    dismiss()
                    return@setNavigationOnClickListener
                }

                val cancelingDialog = AlertDialog.Builder(context)
                    .setTitle("Discard the data")
                    .setMessage("Are you sure want to cancel the operation and discard all the input?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("CONFIRM") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        dismiss()
                    }
                    .setNegativeButton("NO") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }

                cancelingDialog.show()
                return@setNavigationOnClickListener
            }
            dismiss()
        }

        btnWholesale.setOnClickListener {
            type = "wholesale"
            changeTransactionType(true)
        }

        btnRetail.setOnClickListener {
            type = "retail"
            changeTransactionType(true)
        }

        rvItemList.apply {
            if (SettingsSharedPreferences(requireContext()).use_image) {
                adapter = RVSelectedProduct("view", listProduct)
                layoutManager = LinearLayoutManager(requireContext())
            } else {
                adapter = RVSelectedProductNoPicture("view", listProduct)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        btnManageItem.setOnClickListener {
            val products = HashMap<String, Array<Int>>()

            listProduct.forEach {
                products[it.id] = arrayOf(it.totalItems, it.customPrice)
            }

            val dialog = ChooseProductDialog()
            val bundle = Bundle()
            bundle.putString("TRANSACTION_TYPE", if (type.isEmpty()) "retail" else type)
            bundle.putSerializable("PRODUCTS_LIST", products)
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager, "Choose Product Dialog")
        }

        btnConfirm.setOnClickListener {
            if (etId.text.isEmpty() || ddStore.text.isEmpty() || type.isEmpty() || listProduct.size <= 0) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Data Incomplete")
                    .setMessage("Some field is still empty. Please make sure every field is filled with the data.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                return@setOnClickListener
            }

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_sales, null)
            val dialogConfirmation = AlertDialog.Builder(context).create()

            if (mode == "add") {
                if (db.getSalesById(etId.text.toString()) != null) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Sales ID already exists")
                        setMessage("The Sales ID already exists. Please use other ID.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation1).text = getString(R.string.add_confirmation_message_arg, "Sales Transaction")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtDatetimeAndStore).text = getString(R.string.datetime_and_store, dateTimeShowFormat.format(
                        Date()
                    ), ddStore.text)
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtId).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtType).text = if (type == "wholesale") "Wholesale" else "Retail"
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalItem).text = txtTotalItem.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalPrice).text = txtTotalPurchase.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation3).text = getString(R.string.add_confirmation_message_arg_2, "Sales Transaction")
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationSales_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnProductList).setOnClickListener {
                        val dialog = SelectedProductListDialog("view", listProduct)
                        dialog.show(requireActivity().supportFragmentManager, "LIST_SELECTED_PRODUCT")
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnConfirm).setOnClickListener {
                        val storeName: String
                        val storePlatform: String
                        ddStore.text.toString().apply {
                            val index = indexOf('(')
                            storeName = substring(0, index - 1)
                            storePlatform = substring(index + 1, length - 1)
                        }

                        var totalItems = 0
                        var totalPurchase = 0
                        listProduct.forEach {
                            totalItems += it.totalItems
                            totalPurchase += it.totalItems * it.customPrice
                        }

                        val newSales = Sales(
                            etId.text.toString(),
                            db.getStoreByNameAndPlatform(storeName, storePlatform) ?: Store("", "", ""),
                            type,
                            "not_paid_yet",
                            "waiting_for_pickup_shipping",
                            Date(),
                            totalItems,
                            totalPurchase
                        )
                        newSales.listProduct = ArrayList(listProduct)

                        db.insertSales(newSales)
                        val intent = Intent("MODIFIED")
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            } else if (mode == "edit") {
                if (etId.text.toString() == sales?.id && ddStore.text.toString() == getString(R.string.store_name_and_platform, sales?.store?.name, sales?.store?.platform) && sales?.type == type) {
                    dismiss()
                    return@setOnClickListener
                }

                if (sales?.id != etId.text.toString() && db.getSalesById(etId.text.toString()) != null) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Sales ID already exists")
                        setMessage("The Sales ID already used for other id. Please use other ID.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation1).text = getString(R.string.edit_confirmation_message_arg, "Sales Transaction")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtDatetimeAndStore).text = getString(R.string.datetime_and_store, dateTimeShowFormat.format(sales?.datetime ?: Date()), getString(R.string.store_name_and_platform, sales?.store?.name, sales?.store?.platform))
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtId).text = sales?.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtType2).text = if (sales?.type == "wholesale") "Wholesale" else "Retail"
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalItem2).text = numberFormat.format(sales?.total_item)
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalPrice2).text = currencyFormat.format(sales?.total_purchase)

                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationSales_layoutAdditional).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation2).text = getString(R.string.edit_confirmation_message_arg_2, "Sales Transaction")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtDatetimeAndStore).text = getString(R.string.datetime_and_store, dateTimeShowFormat.format(
                        Date()
                    ), ddStore.text)
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtId2).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtType2).text = if (type == "wholesale") "Wholesale" else "Retail"
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalItem2).text = txtTotalItem.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalPrice2).text = txtTotalPurchase.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation3).text = getString(R.string.edit_confirmation_message_arg_3, "Sales Transaction")

                    val list = ArrayList(listProduct)
                    val difference = arrayListOf<Int>()
                    list.forEach {
                        difference.add(it.totalItems - (sales?.listProduct?.find { pd -> pd.id == it.id }?.totalItems ?: 0))
                    }
                    sales?.listProduct?.forEach {
                        if (list.find { pd -> pd.id == it.id } == null) {
                            list.add(it)
                            list[list.size - 1].totalItems = 0
                            difference.add(-it.totalItems)
                        }
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnProductList).setOnClickListener {
                        val dialog = SelectedProductListDialog("view", list)
                        dialog.show(requireActivity().supportFragmentManager, "LIST_SELECTED_PRODUCT")
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnConfirm).setOnClickListener {
                        val storeName: String
                        val storePlatform: String
                        ddStore.text.toString().apply {
                            val index = indexOf('(')
                            storeName = substring(0, index - 2)
                            storePlatform = substring(index + 1, length - 2)
                        }

                        var totalItems = 0
                        var totalPurchase = 0
                        listProduct.forEach {
                            totalItems += it.totalItems
                            totalPurchase += it.totalItems * it.customPrice
                        }

                        val newSales = Sales(
                            etId.text.toString(),
                            db.getStoreByNameAndPlatform(storeName, storePlatform) ?: Store("", "", ""),
                            type,
                            "not_paid_yet",
                            "waiting_for_pickup_shipping",
                            Date(),
                            totalItems,
                            totalPurchase
                        )
                        newSales.listProduct = ArrayList(listProduct)

                        db.updateSales(sales?.id ?: "", newSales)
                        val intent = Intent("MODIFIED")
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            }

            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }

        return view
    }

    private fun changeTransactionType(changePrice: Boolean = false) {
        btnWholesale.apply {
            setTypeface(null, Typeface.NORMAL)
            setTextColor(requireContext().getColor(R.color.light_grey))
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.light_grey))
        }
        btnRetail.apply {
            setTypeface(null, Typeface.NORMAL)
            setTextColor(requireContext().getColor(R.color.light_grey))
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.light_grey))
        }
        when (type) {
            "wholesale" -> {
                btnWholesale.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.blue))
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.blue))
                }
                if (changePrice)
                    listProduct.forEach {
                        it.customPrice = it.wholesale_price
                    }
            }
            "retail" -> {
                btnRetail.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.blue))
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.blue))
                }
                if (changePrice)
                    listProduct.forEach {
                        it.customPrice = it.retail_price
                    }
            }
        }
        updateRecyclerView()
        updateTotalItemsAndPurchase()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        rvItemList.adapter?.notifyDataSetChanged()
        if (listProduct.size <= 0) {
            txtNoItems.visibility = View.VISIBLE
            rvItemList.visibility = View.GONE
        } else {
            txtNoItems.visibility = View.GONE
            rvItemList.visibility = View.VISIBLE
        }
    }

    private fun updateTotalItemsAndPurchase() {
        var totalItems = 0
        var totalPurchase = 0
        listProduct.forEach {
            totalItems += it.totalItems
            totalPurchase += it.totalItems * it.customPrice
        }
        txtTotalItem.text = numberFormat.format(totalItems)
        txtTotalPurchase.text = currencyFormat.format(totalPurchase)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(choosedProductListBroadcastReceiver)
    }
}