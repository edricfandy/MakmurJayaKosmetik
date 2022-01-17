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
import com.example.makmurjayakosmetik.classes.*
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProduct
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProductNoPicture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddEditPurchaseDialog(private val mode: String) : DialogFragment() {
    private var purchase: Purchase? = null
    private var paymentMethod : String = ""
    private var itemChecked : String = "not_checked"
    private lateinit var db : DBHelper

    private lateinit var listProduct : ArrayList<Product>
    private lateinit var listTextSupplier : ArrayList<String>
    private lateinit var numberFormat: NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private lateinit var dateTimeShowFormat : SimpleDateFormat

    private lateinit var btnCash : MaterialButton
    private lateinit var btnCredit : MaterialButton
    private lateinit var rvItemList : RecyclerView
    private lateinit var rbCash : RadioButton
    private lateinit var rbCredit : RadioButton
    private lateinit var btnItemCheckedStatus : MaterialButton
    private lateinit var txtNoItems : TextView
    private lateinit var txtTotalItem : TextView
    private lateinit var txtTotalPurchase : TextView
    private lateinit var layoutPayment : LinearLayout
    private lateinit var etTotalPaid : EditText

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
            itemChecked = "not_checked"
            changeItemCheckedStatus()
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
        purchase = db.getPurchaseById(arguments?.getString("PURCHASE_ID") ?: "")
        listProduct = purchase?.listProduct ?: arrayListOf()
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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_edit_purchase, container, false)

        val toolbar : MaterialToolbar = view.findViewById(R.id.dialogAddEditPurchase_toolbar)
        val btnConfirm : MaterialButton = view.findViewById(R.id.dialogAddEditPurchase_btnConfirm)

        val txtHeader = view.findViewById<TextView>(R.id.dialogAddEditPurchase_txtHeading)
        val etId = view.findViewById<EditText>(R.id.dialogAddEditPurchase_etId)
        val ddSupplier = view.findViewById<AutoCompleteTextView>(R.id.dialogAddEditPurchase_ddSupplier)
        val btnManageItem = view.findViewById<MaterialButton>(R.id.dialogAddEditPurchase_btnManageItem)

        btnCash = view.findViewById(R.id.dialogAddEditPurchase_btnCash)
        btnCredit = view.findViewById(R.id.dialogAddEditPurchase_btnCredit)
        rvItemList = view.findViewById(R.id.dialogAddEditPurchase_recyclerviewItemList)
        rbCash = view.findViewById(R.id.dialogAddEditPurchase_rbCash)
        rbCredit = view.findViewById(R.id.dialogAddEditPurchase_rbCredit)
        btnItemCheckedStatus = view.findViewById(R.id.dialogAddEditPurchase_btnItemCheckedStatus)
        txtNoItems = view.findViewById(R.id.dialogAddEditPurchase_txtNoItems)
        txtTotalItem = view.findViewById(R.id.dialogAddEditPurchase_txtTotalItem)
        txtTotalPurchase = view.findViewById(R.id.dialogAddEditPurchase_txtTotalPurchase)
        layoutPayment = view.findViewById(R.id.dialogAddEditPurchase_layoutPayment)
        etTotalPaid = view.findViewById(R.id.dialogAddEditPurchase_etTotalPaid)

        listTextSupplier = arrayListOf()
        db.getAllSuppliers().forEach {
            listTextSupplier.add("${it.id} - ${it.name} (${it.city})")
        }
        ddSupplier.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listTextSupplier))
        btnItemCheckedStatus.setOnClickListener {
            changeItemCheckedStatus()
        }

        if (mode == "add") {
            txtHeader.text = getString(R.string.add_arg, "Purchase")
            btnConfirm.text = getString(R.string.add)
            btnItemCheckedStatus.isEnabled = false
        } else {
            txtHeader.text = getString(R.string.edit_arg, "Purchase")
            btnConfirm.text = getString(R.string.edit)
            etId.setText(purchase?.id)
            paymentMethod = purchase?.payment_method ?: ""
            changePaymentMethod()
            itemChecked = purchase?.item_checked ?: "not_checked"
            changeItemCheckedStatus()
            ddSupplier.setText(getString(R.string.supplier_id_name_and_city, purchase?.supplier?.id, purchase?.supplier?.name, purchase?.supplier?.city))

            if (purchase?.listProduct != null) {
                if (purchase!!.listProduct.size > 0) {
                    txtNoItems.visibility = View.GONE
                    rvItemList.visibility = View.VISIBLE
                    txtTotalItem.text = numberFormat.format(purchase?.total_item)
                    txtTotalPurchase.text = currencyFormat.format(purchase?.total_purchase)
                }
            } else {
                txtNoItems.visibility = View.VISIBLE
                rvItemList.visibility = View.GONE
            }
            etTotalPaid.setText(numberFormat.format(purchase?.total_paid))
            db.deletePaymentByPurchaseId(purchase?.id ?: "")
        }

        etTotalPaid.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.isNotEmpty())
                        setText(numberFormat.format(text.toString().toInt()).toString())
                    else
                        setText(0.toString())
                }
                return@setOnFocusChangeListener
            }

            (v as EditText).apply {
                if (text.toString().toInt() == 0)
                    setText("")
                else
                    setText((numberFormat.parse(text.toString()) ?: 0).toString())
            }
        }

        toolbar.setNavigationOnClickListener {
            if (etId.text.isNotBlank() || ddSupplier.text.isNotEmpty() || paymentMethod.isNotEmpty() || listProduct.size > 0 || etTotalPaid.text.isNotEmpty()) {
                if (etId.text.toString() == purchase?.id && ddSupplier.text.toString() == getString(R.string.supplier_id_name_and_city, purchase?.supplier?.id, purchase?.supplier?.name, purchase?.supplier?.city)
                    && purchase?.payment_method == paymentMethod && rbCredit.isChecked && etTotalPaid.text.toString().toInt() == purchase?.total_paid) {
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

        btnCash.setOnClickListener {
            paymentMethod = "cash"
            changePaymentMethod()
        }

        btnCredit.setOnClickListener {
            paymentMethod = "credit"
            changePaymentMethod()
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
            bundle.putString("TRANSACTION_TYPE", "purchase")
            bundle.putSerializable("PRODUCTS_LIST", products)
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager, "Choose Product Dialog")
        }

        btnConfirm.setOnClickListener {
            if (etId.text.isEmpty() || ddSupplier.text.isEmpty() || paymentMethod.isEmpty() || listProduct.size <= 0) {
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

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_purchase, null)
            val dialogConfirmation = AlertDialog.Builder(context).create()

            if (mode == "add") {
                if (db.getPurchaseById(etId.text.toString()) != null) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Purchase ID already exists")
                        setMessage("The Transaction ID already exists. Please use other ID.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation1).text = getString(R.string.add_confirmation_message_arg, "Purchase")
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtDatetime).text = dateTimeShowFormat.format(Date())
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtId).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtSupplier).text = ddSupplier.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtItemCheckedStatus).text = btnItemCheckedStatus.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtPaymentMethod).text = if (paymentMethod == "cash") "Cash" else "Credit"
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPaid).text = currencyFormat.format(etTotalPaid.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalItem).text = txtTotalItem.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPurchase).text = currencyFormat.format(txtTotalPurchase.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation3).text = getString(R.string.add_confirmation_message_arg_2, "Purchase")
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationPurchase_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationPurchase_btnProductList).setOnClickListener {
                        val dialog = SelectedProductListDialog("view", listProduct)
                        dialog.show(requireActivity().supportFragmentManager, "LIST_SELECTED_PRODUCT")
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperationPurchase_btnConfirm).setOnClickListener {
                        val supplierId = ddSupplier.text.toString().substring(0, ddSupplier.text.toString().indexOf('-') - 1)
                        var totalItems = 0
                        var totalPurchase = 0
                        listProduct.forEach {
                            totalItems += it.totalItems
                            totalPurchase += it.totalItems * it.customPrice
                        }

                        val newPurchase = Purchase(
                            etId.text.toString(),
                            db.getSupplierById(supplierId) ?: Supplier("", "", "", "", "", ""),
                            Date(),
                            itemChecked,
                            paymentMethod,
                            etTotalPaid.text.toString().toInt(),
                            totalItems,
                            totalPurchase
                        )
                        newPurchase.listProduct = ArrayList(listProduct)

                        db.insertPurchase(newPurchase)
                        val payment = Payment(
                            "${newPurchase.id}.001",
                            newPurchase.id,
                            newPurchase.datetime,
                            numberFormat.parse(etTotalPaid.text.toString())?.toInt() ?: 0,
                            ""
                        )
                        db.insertPayment(payment)

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
                if (etId.text.toString() == purchase?.id && ddSupplier.text.toString() == getString(R.string.supplier_id_name_and_city, purchase?.supplier?.id, purchase?.supplier?.name, purchase?.supplier?.city) && purchase?.payment_method == paymentMethod
                    && purchase?.item_checked == itemChecked) {
                    dismiss()
                    return@setOnClickListener
                }

                if (purchase?.id != etId.text.toString() && db.getPurchaseById(etId.text.toString()) != null) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Purchase ID already exists")
                        setMessage("The Purchase ID already used for other purchase. Please use other ID.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation1).text = getString(R.string.edit_confirmation_message_arg, "Purchase")
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtDatetime).text = dateTimeShowFormat.format(Date())
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtId).text = purchase?.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtSupplier).text = getString(R.string.supplier_id_name_and_city, purchase?.supplier?.id, purchase?.supplier?.name, purchase?.supplier?.city)
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtItemCheckedStatus).text = purchase?.item_checked
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtPaymentMethod).text = if (purchase?.payment_method == "cash") "Cash" else "Credit"
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPaid).text = currencyFormat.format(purchase?.total_paid)
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalItem).text = purchase?.total_item.toString()
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPurchase).text = currencyFormat.format(purchase?.total_purchase)

                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationPurchase_layoutAdditional).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation3).text = getString(R.string.edit_confirmation_message_arg_2, "Purchase")
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtId2).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtSupplier2).text = ddSupplier.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtItemCheckedStatus2).text = btnItemCheckedStatus.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtPaymentMethod2).text = if (paymentMethod == "cash") "Cash" else "Credit"
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPaid2).text = currencyFormat.format(etTotalPaid.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalItem2).text = txtTotalItem.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPurchase2).text = currencyFormat.format(txtTotalPurchase.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation3).text = getString(R.string.edit_confirmation_message_arg_3, "Purchase")

                    val list = ArrayList(listProduct)
                    val difference = arrayListOf<Int>()
                    list.forEach {
                        difference.add(it.totalItems - (purchase?.listProduct?.find { pd -> pd.id == it.id }?.totalItems ?: 0))
                    }
                    purchase?.listProduct?.forEach {
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
                        val supplierId = ddSupplier.text.toString().substring(0, ddSupplier.text.toString().indexOf('-') - 1)
                        var totalItems = 0
                        var totalPurchase = 0
                        listProduct.forEach {
                            totalItems += it.totalItems
                            totalPurchase += it.totalItems * it.customPrice
                        }

                        val newPurchase = Purchase(
                            etId.text.toString(),
                            db.getSupplierById(supplierId) ?: Supplier("", "", "", "", "", ""),
                            Date(),
                            itemChecked,
                            paymentMethod,
                            etTotalPaid.text.toString().toInt(),
                            totalItems,
                            totalPurchase
                        )
                        newPurchase.listProduct = ArrayList(listProduct)
                        val payment = Payment(
                            "${newPurchase.id}.001",
                            newPurchase.id,
                            newPurchase.datetime,
                            numberFormat.parse(etTotalPaid.text.toString())?.toInt() ?: 0,
                            ""
                        )
                        db.insertPayment(payment)

                        db.updatePurchase(purchase?.id ?: "", newPurchase)
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

    private fun changePaymentMethod() {
        btnCash.apply {
            setTypeface(null, Typeface.NORMAL)
            setTextColor(requireContext().getColor(R.color.light_grey))
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.light_grey))
        }
        btnCredit.apply {
            setTypeface(null, Typeface.NORMAL)
            setTextColor(requireContext().getColor(R.color.light_grey))
            strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.light_grey))
        }
        when (paymentMethod) {
            "cash" -> {
                btnCash.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.blue))
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.blue))
                    layoutPayment.visibility = View.GONE
                }
            }
            "credit" -> {
                btnCredit.apply {
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(requireContext().getColor(R.color.blue))
                    strokeColor = ColorStateList.valueOf(requireContext().getColor(R.color.blue))
                    layoutPayment.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeItemCheckedStatus() {
        if (itemChecked == "not_checked") {
            itemChecked = "checked"
            btnItemCheckedStatus.text = "CHECKED"
            btnItemCheckedStatus.setBackgroundColor(requireContext().getColor(R.color.dark_green))
        } else {
            itemChecked = "not_checked"
            btnItemCheckedStatus.text = "NOT CHECKED"
            btnItemCheckedStatus.setBackgroundColor(requireContext().getColor(R.color.red))
        }
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

    @SuppressLint("SetTextI18n")
    private fun updateTotalItemsAndPurchase() {
        var totalItems = 0
        var totalPurchase = 0
        listProduct.forEach {
            totalItems += it.totalItems
            totalPurchase += it.totalItems * it.customPrice
        }
        txtTotalItem.text = numberFormat.format(totalItems)
        txtTotalPurchase.text = currencyFormat.format(totalPurchase)
        if (rbCash.isChecked) etTotalPaid.setText(totalPurchase)
        if (listProduct.size > 0) {
            btnItemCheckedStatus.apply {
                isEnabled = true
                text = "NOT CHECKED"
                setBackgroundColor(requireContext().getColor(R.color.red))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(choosedProductListBroadcastReceiver)
    }
}