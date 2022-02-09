package com.example.makmurjayakosmetik.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.BarcodeScannerActivity
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.recyclerview.RVProductImage
import com.example.makmurjayakosmetik.recyclerview.RVVariantAmount
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Category
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.classes.Supplier
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddEditProductDialog(private val mode: String) : DialogFragment() {
    private var product: Product? = null
    private lateinit var db : DBHelper
    private lateinit var listCategory : ArrayList<String>
    private lateinit var listSupplier : ArrayList<String>
    private lateinit var listVariantAmount : ArrayList<Pair<String, Int>>
    private var listImagePath = arrayListOf("")
    private lateinit var numberFormat : NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private var photoPath: String = ""
    private var photoFile: File? = null

    private lateinit var etId : EditText
    private lateinit var rvVariantAmount : RecyclerView
    private lateinit var rvImage : RecyclerView
    private lateinit var txtTotalStock : TextView

    private val chooseImageReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "OPEN_CAMERA") {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    requestPermissionCamera.launch(android.Manifest.permission.CAMERA)
                openCamera()
            } else if (p1?.action == "OPEN_GALLERY") {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    requestPermissionGallery.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                openGallery()
            }
        }
    }

    private val stockAmountReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "COUNT_TOTAL_STOCK") {
                var totalStock = 0
                listVariantAmount = (rvVariantAmount.adapter as RVVariantAmount).retriveData()
                for (i in listVariantAmount)
                    totalStock += i.second
                txtTotalStock.text = totalStock.toString()
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
        product = db.getProductById(arguments?.getString("PRODUCT_ID") ?: "")
        if (mode == "edit" && product?.id?.isEmpty() == true) {
            Toast.makeText(requireContext(), "Cannot Edit because product information is incomplete. Contact the developer.", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        listCategory = arrayListOf()
        for (i in db.getAllCategories()) {
            listCategory.add(i.name)
        }
        listSupplier = arrayListOf()
        for (i in db.getAllSuppliers()) {
            listSupplier.add("${i.id} - ${i.name} (${i.city})")
        }
        listVariantAmount = product?.variants ?: arrayListOf(Pair("", 0))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))

        val intentFilter = IntentFilter()
        intentFilter.addAction("OPEN_CAMERA")
        intentFilter.addAction("OPEN_GALLERY")
        requireContext().registerReceiver(chooseImageReceiver, intentFilter)

        val intentFilter2 = IntentFilter()
        intentFilter2.addAction("COUNT_TOTAL_STOCK")
        requireContext().registerReceiver(stockAmountReceiver, intentFilter2)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_edit_product, container, false)

        val toolbar : MaterialToolbar = view.findViewById(R.id.dialogAddEditProduct_toolbar)
        val btnConfirm : MaterialButton = view.findViewById(R.id.dialogAddEditProduct_btnConfirm)

        val txtHeader = view.findViewById<TextView>(R.id.dialogAddEditProduct_txtHeading)
        val btnScanBarcode = view.findViewById<ImageButton>(R.id.dialogAddEditProduct_btnScanBarcode)
        val etName = view.findViewById<EditText>(R.id.dialogAddEditProduct_etName)
        val ddCategory = view.findViewById<AutoCompleteTextView>(R.id.dialogAddEditProduct_ddCategory)
        val ddSupplier = view.findViewById<AutoCompleteTextView>(R.id.dialogAddEditProduct_ddSupplier)
        val etCapitalPrice = view.findViewById<EditText>(R.id.dialogAddEditProduct_etCapitalPrice)
        val etWholesalePrice = view.findViewById<EditText>(R.id.dialogAddEditProduct_etWholesalePrice)
        val etRetailPrice = view.findViewById<EditText>(R.id.dialogAddEditProduct_etRetailPrice)
        val layoutEtTotalStock = view.findViewById<TextInputLayout>(R.id.dialogAddEditProduct_layoutEtTotalStock)
        val etTotalStock = view.findViewById<EditText>(R.id.dialogAddEditProduct_etTotalStock)
        val btnEnableVariant = view.findViewById<MaterialButton>(R.id.dialogAddEditProduct_btnEnableVariant)
        val layoutEtVariantName = view.findViewById<TextInputLayout>(R.id.dialogAddEditProduct_layoutEtVariantName)
        val etVariantName = view.findViewById<EditText>(R.id.dialogAddEditProduct_etVariantName)
        val layoutVariant = view.findViewById<LinearLayout>(R.id.dialogAddEditProduct_layoutVariant)
        val txtVariantName = view.findViewById<TextView>(R.id.dialogAddEditProduct_txtVariantName)
        val btnAddVariant = view.findViewById<MaterialButton>(R.id.dialogAddEditProduct_btnAddVariant)
        val layoutImage = view.findViewById<LinearLayout>(R.id.dialogAddEditProduct_layoutImage)

        etId = view.findViewById(R.id.dialogAddEditProduct_etId)
        txtTotalStock = view.findViewById(R.id.dialogAddEditProduct_txtTotalStock)
        rvVariantAmount = view.findViewById(R.id.dialogAddEditProduct_recyclerViewVariant)
        rvImage = view.findViewById(R.id.dialogAddEditProduct_recyclerViewImage)
        ddCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && ddCategory.text.toString() == "Choose One")
                ddCategory.text.clear()
        }

        if (mode == "add") {
            txtHeader.text = getString(R.string.add_arg, "Product")
            if (arguments?.getString("PRODUCT_ID") != null)
                etId.setText(arguments?.getString("PRODUCT_ID"))
        } else {
            btnConfirm.text = getString(R.string.edit)
            txtHeader.text = getString(R.string.edit_arg, "Product")
            etId.setText(product?.id)
            etName.setText(product?.name)
            ddCategory.setText(product?.category?.name)
            ddSupplier.setText(getString(R.string.supplier_id_name_and_city, product?.supplier?.id, product?.supplier?.name, product?.supplier?.city))
            etCapitalPrice.setText(numberFormat.format(product?.capital_price))
            etRetailPrice.setText(numberFormat.format(product?.retail_price))
            etWholesalePrice.setText(numberFormat.format(product?.wholesale_price))
            if (product?.variant_name?.isEmpty() == true) {
                etTotalStock.setText(product?.total_stock.toString())
            } else {
                layoutEtTotalStock.visibility = View.GONE
                btnEnableVariant.apply{
                    text = getString(R.string.disable_variant)
                    setBackgroundColor(requireContext().getColor(R.color.red))
                }
                layoutEtVariantName.visibility = View.VISIBLE
                etVariantName.setText(product?.variant_name)
                layoutVariant.visibility = View.VISIBLE
                txtVariantName.text = product?.variant_name
                txtTotalStock.text = product?.total_stock.toString()
            }
        }

        if (!SettingsSharedPreferences(requireContext()).use_image)
            layoutImage.visibility = View.GONE

        rvVariantAmount.apply {
            adapter = RVVariantAmount(mode, listVariantAmount, mutableMapOf())
            layoutManager = LinearLayoutManager(requireContext())
        }

        rvImage.apply {
            adapter = RVProductImage(requireActivity(), mode, listImagePath)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        ddCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listCategory))
        ddSupplier.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listSupplier))

        toolbar.setNavigationOnClickListener {
            if (etId.text.isNotBlank() || etName.text.isNotBlank() || ddCategory.text.isNotEmpty() || ddSupplier.text.isNotEmpty() || etCapitalPrice.text.isNotEmpty() || etWholesalePrice.text.isNotBlank() || etRetailPrice.text.isNotBlank()
                || etTotalStock.text.isNotBlank() || (btnEnableVariant.text == "Disable Variant" && (etVariantName.text.isNotBlank() || rvVariantAmount.adapter!!.itemCount > 0))) {
                if (etId.text.toString() == product?.id && etName.text.toString() == product?.name && ddCategory.text.toString() == product?.category?.name
                    && etCapitalPrice.text.toString() == product?.capital_price.toString() && etWholesalePrice.text.toString() == product?.wholesale_price.toString() && etRetailPrice.text.toString() == product?.retail_price.toString()
                    && ((btnEnableVariant.text == "Enable Variant" && etTotalStock.text.toString() == product?.total_stock.toString()) || (btnEnableVariant.text == "Disable Variant"
                            && etVariantName.text.toString() == product?.variant_name))) {
                    dismiss()
                    return@setNavigationOnClickListener
                }

                val cancelingDialog = AlertDialog.Builder(requireContext())
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

        btnScanBarcode.setOnClickListener {
            val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
            scanBarcodeActivity.launch(intent)
        }

        etCapitalPrice.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.isNotEmpty())
                        Thread {
                            var tempText = text.toString()
                            var i = 0
                            do {
                                if (!tempText[i].isDigit()) {
                                    tempText = tempText.replace(tempText[i].toString(), "")
                                    continue
                                }
                                i++
                            } while (i < tempText.length)
                            post {
                                setText(numberFormat.format(tempText.toInt()).toString())
                            }
                        }.start()
                }
                return@setOnFocusChangeListener
            }

            (v as EditText).apply {
                if (text.isNotEmpty())
                    setText((numberFormat.parse(text.toString()) ?: 0).toString())
            }
        }

        etWholesalePrice.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.isNotEmpty())
                        Thread {
                            var tempText = text.toString()
                            var i = 0
                            do {
                                if (!tempText[i].isDigit()) {
                                    tempText = tempText.replace(tempText[i].toString(), "")
                                    continue
                                }
                                i++
                            } while (i < tempText.length)
                            post {
                                setText(numberFormat.format(tempText.toInt()).toString())
                            }
                        }.start()
                }
                return@setOnFocusChangeListener
            }

            (v as EditText).apply {
                if (text.isNotEmpty())
                    setText((numberFormat.parse(text.toString()) ?: 0).toString())
            }
        }

        etRetailPrice.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.isNotEmpty())
                        Thread {
                            var tempText = text.toString()
                            var i = 0
                            do {
                                if (!tempText[i].isDigit()) {
                                    tempText = tempText.replace(tempText[i].toString(), "")
                                    continue
                                }
                                i++
                            } while (i < tempText.length)
                            post {
                                setText(numberFormat.format(tempText.toInt()).toString())
                            }
                        }.start()
                }
                return@setOnFocusChangeListener
            }

            (v as EditText).apply {
                if (text.isNotEmpty())
                    setText((numberFormat.parse(text.toString()) ?: 0).toString())
            }
        }

        etTotalStock.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                (v as EditText).apply {
                    if (text.isNotEmpty())
                        Thread {
                            var tempText = text.toString()
                            var i = 0
                            do {
                                if (!tempText[i].isDigit()) {
                                    tempText = tempText.replace(tempText[i].toString(), "")
                                    continue
                                }
                                i++
                            } while (i < tempText.length)
                            post {
                                setText(numberFormat.format(tempText.toInt()).toString())
                            }
                        }.start()
                }
                return@setOnFocusChangeListener
            }

            (v as EditText).apply {
                if (text.isNotEmpty())
                    setText((numberFormat.parse(text.toString()) ?: 0).toString())
            }
        }


        btnEnableVariant.setOnClickListener {
            (it as MaterialButton).apply {
                if (text == "Enable Variant") {
                    layoutEtTotalStock.visibility = View.GONE
                    text = getString(R.string.disable_variant)
                    setBackgroundColor(requireContext().getColor(R.color.red))
                    layoutEtVariantName.visibility = View.VISIBLE
                    layoutVariant.visibility = View.VISIBLE
                } else {
                    layoutEtTotalStock.visibility = View.VISIBLE
                    etTotalStock.setText(txtTotalStock.text)
                    text = getString(R.string.enable_variant)
                    setBackgroundColor(requireContext().getColor(R.color.green))
                    layoutEtVariantName.visibility = View.GONE
                    layoutVariant.visibility = View.GONE
                }
            }
        }

        etVariantName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etVariantName.text.isEmpty()) txtVariantName.text = getString(R.string.variant_name)
                else txtVariantName.text = etVariantName.text
            }

            override fun afterTextChanged(s: Editable?) {  }
        })

        btnAddVariant.setOnClickListener {
            listVariantAmount.add(Pair("", 0))
            rvVariantAmount.adapter?.notifyItemInserted(listVariantAmount.size - 1)
        }

        btnConfirm.setOnClickListener {
            var variantsInserted = true
            listVariantAmount = (rvVariantAmount.adapter as RVVariantAmount).retriveData()
            for (i in listVariantAmount) {
                if (i.first.isEmpty()) {
                    variantsInserted = false
                    break
                }
            }

            if (etId.text.isEmpty() || etName.text.isEmpty() || ddCategory.text.isEmpty() || ddSupplier.text.isEmpty() || etCapitalPrice.text.isEmpty() || etWholesalePrice.text.isEmpty() || etRetailPrice.text.isEmpty()
                || ((layoutEtTotalStock.visibility == View.VISIBLE && etTotalStock.text.isEmpty()) || (layoutEtVariantName.visibility == View.VISIBLE && (etVariantName.text.isEmpty()
                        || !variantsInserted)))) {
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

            if (listCategory.find { it == ddCategory.text.toString() } == null) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Category Invalid")
                    .setMessage("The category name inserted is not found in the category list. Please insert valid category.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                return@setOnClickListener
            }

            if (listSupplier.find { it == ddSupplier.text.toString() } == null) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Supplier Invalid")
                    .setMessage("The supplier name inserted is not found in the supplier list. Please insert valid supplier.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                return@setOnClickListener
            }

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_product, null)
            val dialogConfirmation = AlertDialog.Builder(context).create()

            if (mode == "add") {
                if (db.checkProductIsExisted(etId.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Product ID already exists")
                        setMessage("The Product ID already used for other id. Please use other Name.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation1).text = getString(
                        R.string.add_confirmation_message_arg, "Product"
                    )
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtId).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtName).text = etName.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCategory).text = ddCategory.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtSupplier).text = ddSupplier.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCapitalPrice).text = currencyFormat.format(numberFormat.parse(etCapitalPrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtWholesalePrice).text = currencyFormat.format(numberFormat.parse(etWholesalePrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtRetailPrice).text = currencyFormat.format(numberFormat.parse(etRetailPrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtTotalStock).text = NumberFormat.getIntegerInstance().format(if (layoutEtTotalStock.visibility == View.VISIBLE) etTotalStock.text.toString().toInt() else txtTotalStock.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation3).text = getString(
                        R.string.add_confirmation_message_arg_2, "Product"
                    )
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationProduct_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val supplierId = ddSupplier.text.toString().substring(0, ddSupplier.text.toString().indexOf('-') - 1)
                        val newProduct = Product(
                            etId.text.toString(),
                            etName.text.toString(),
                            Category(ddCategory.text.toString(), ""),
                            numberFormat.parse(etCapitalPrice.text.toString())?.toInt() ?: 0,
                            numberFormat.parse(etWholesalePrice.text.toString())?.toInt() ?: 0,
                            numberFormat.parse(etRetailPrice.text.toString())?.toInt() ?: 0,
                            if (layoutEtTotalStock.visibility == View.VISIBLE) etTotalStock.text.toString().toInt() else txtTotalStock.text.toString().toInt(),
                            if (layoutEtVariantName.visibility == View.VISIBLE) etVariantName.text.toString() else "",
                            if (layoutVariant.visibility == View.VISIBLE) listVariantAmount else arrayListOf(),
                            db.getSupplierById(supplierId) ?: Supplier("", "", "", "", "", "")
                        )
                        newProduct.images = ArrayList(listImagePath.filter {
                            it.isNotEmpty()
                        })

                        db.insertProduct(newProduct)
                        val intent = Intent("MODIFIED")
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            } else if (mode == "edit") {
                val supplierId = ddSupplier.text.toString().substring(0, ddSupplier.text.toString().indexOf('-') - 1)
                if (etId.text.toString() == product?.id && etName.text.toString() == product?.name && ddCategory.text.toString() == product?.category?.name && supplierId == product?.supplier?.id
                    && etCapitalPrice.text.toString() == product?.capital_price.toString() && etWholesalePrice.text.toString() == product?.wholesale_price.toString() && etRetailPrice.text.toString() == product?.retail_price.toString()
                    && ((btnEnableVariant.text == "Enable Variant" && etTotalStock.text.toString() == product?.total_stock.toString()) || (btnEnableVariant.text == "Disable Variant"
                            && etVariantName.text.toString() == product?.variant_name))) {
                    dismiss()
                    return@setOnClickListener
                }

                if (product?.id != etId.text.toString() && db.checkProductIsExisted(etId.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                    dialogUnavailable.apply {
                        setTitle("Product ID already exists")
                        setMessage("The Product ID already used for other id. Please use other Name.")
                        setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                    }
                    dialogUnavailable.create().show()

                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation1).text = getString(
                        R.string.edit_confirmation_message_arg, "Product"
                    )
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtId).text = product?.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtName).text = product?.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCategory).text = product?.category?.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtSupplier).text = getString(R.string.supplier_id_name_and_city, product?.supplier?.id, product?.supplier?.name, product?.supplier?.city)
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCapitalPrice).text = currencyFormat.format(product?.capital_price)
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtWholesalePrice).text = currencyFormat.format(product?.wholesale_price)
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtRetailPrice).text = currencyFormat.format(product?.retail_price)
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtTotalStock).text = NumberFormat.getIntegerInstance().format(product?.total_stock)
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation2).text = getString(
                        R.string.edit_confirmation_message_arg_2, "Product"
                    )

                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtId2).text = etId.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtName2).text = etName.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCategory2).text = ddCategory.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtSupplier2).text = ddSupplier.text
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCapitalPrice2).text = currencyFormat.format(numberFormat.parse(etCapitalPrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtWholesalePrice2).text = currencyFormat.format(numberFormat.parse(etWholesalePrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtRetailPrice2).text = currencyFormat.format(numberFormat.parse(etRetailPrice.text.toString()))
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtTotalStock2).text = NumberFormat.getIntegerInstance().format(if (layoutEtTotalStock.visibility == View.VISIBLE) etTotalStock.text.toString().toInt() else txtTotalStock.text.toString().toInt())
                    findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation3).text = getString(
                        R.string.edit_confirmation_message_arg_3, "Product"
                    )

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val newProduct = Product(
                            etId.text.toString(),
                            etName.text.toString(),
                            Category(ddCategory.text.toString(), ""),
                            numberFormat.parse(etCapitalPrice.text.toString())?.toInt() ?: 0,
                            numberFormat.parse(etWholesalePrice.text.toString())?.toInt() ?: 0,
                            numberFormat.parse(etRetailPrice.text.toString())?.toInt() ?: 0,
                            if (layoutEtTotalStock.visibility == View.VISIBLE) numberFormat.parse(etTotalStock.text.toString())?.toInt() ?: 0 else numberFormat.parse(txtTotalStock.text.toString())?.toInt() ?: 0,
                            if (layoutEtVariantName.visibility == View.VISIBLE) etVariantName.text.toString() else "",
                            if (layoutVariant.visibility == View.VISIBLE) listVariantAmount else arrayListOf(),
                            db.getSupplierById(supplierId) ?: Supplier("", "", "", "", "", "")
                        )
                        newProduct.images = ArrayList(listImagePath.filter {
                            it.isNotEmpty()
                        })

                        db.updateProduct(product?.id ?: "", newProduct)
                        val intent = Intent("MODIFIED")
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            }

            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }

        return view
    }

    private fun createImageFile(): File {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("in", "ID"))
        val fileName = "JPEG_" + dateFormat.format(Date())
        val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, "$fileName.jpg").apply {
            photoPath = absolutePath
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return

        val openCam = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("Failed to save image", ex.toString())
            null
        }
        if (photoFile != null) {
            val photoUri = FileProvider.getUriForFile(requireContext(), "com.example.makmurjayakosmetik.fileprovider", photoFile!!)
            openCam.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraActivity.launch(openCam)
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryActivity.launch(intent)
    }

    private val requestPermissionCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) openCamera()
        else Toast.makeText(requireContext(), "Tidak dapat membuka kamera karena tidak diberi izin akses", Toast.LENGTH_SHORT).show()
    }

    private val requestPermissionGallery = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) openGallery()
        else Toast.makeText(requireContext(), "Tidak dapat membuka Gallery karena tidak diberi izin akses", Toast.LENGTH_SHORT).show()
    }

    private val cameraActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            listImagePath[listImagePath.size - 1] = photoPath
            rvImage.adapter?.notifyItemChanged(listImagePath.size - 1)
            if (listImagePath.size < 10) {
                listImagePath.add("")
                rvImage.adapter?.notifyItemInserted(listImagePath.size - 1)
            }
        }
    }

    private val galleryActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            listImagePath[listImagePath.size - 1] = it.data!!.data.toString()
            rvImage.adapter?.notifyItemChanged(listImagePath.size - 1)
            if (listImagePath.size < 10) {
                listImagePath.add("")
                rvImage.adapter?.notifyItemInserted(listImagePath.size - 1)
            }
        }
    }

    private val scanBarcodeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null)
            etId.setText(it?.data?.extras?.getString("SCAN_RESULT"))
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(chooseImageReceiver)
        requireContext().unregisterReceiver(stockAmountReceiver)
    }
}