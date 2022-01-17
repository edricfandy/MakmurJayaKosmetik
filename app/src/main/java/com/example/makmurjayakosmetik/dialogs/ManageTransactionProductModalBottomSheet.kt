package com.example.makmurjayakosmetik.dialogs

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Category
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.classes.Supplier
import com.example.makmurjayakosmetik.recyclerview.RVVariantAmount
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

class ManageTransactionProductModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var db : DBHelper
    private lateinit var numberFormat : NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private lateinit var product : Product
    private lateinit var transactionType : String
    private var totalItems : Int = 0
    private var customPrice : Int = 0

    private lateinit var txtAmount : TextView
    private lateinit var txtSubtotal : TextView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "CHANGE_AMOUNT") {
                val amount = intent.extras?.getInt("AMOUNT")
                if (amount != null) {
                    totalItems = amount
                    txtAmount.text = numberFormat.format(totalItems)
                    updateSubtotal()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        product = db.getProductById(arguments?.getString("PRODUCT_ID") ?: "") ?: Product("", "", Category("", ""), 0, 0, 0, 0,"", arrayListOf(), Supplier("", "", "", "", "", ""))
        transactionType = arguments?.getString("TRANSACTION_TYPE") ?: ""
        if (arguments?.getBoolean("SELECTED") == true) {
            totalItems = arguments?.getInt("AMOUNT") ?: 1
            customPrice = arguments?.getInt("PRICE") ?: product.retail_price
            if (product.variants.size > 0) {
                val variantsChoosed = arguments?.getSerializable("VARIANTS_AMOUNT") as HashMap<*, *>
                variantsChoosed.forEach {
                    product.variantsChoosed[it.key as String] = it.value as Int
                }
            }
        } else {
            totalItems = 1
            customPrice = if (transactionType == "wholesale") product.wholesale_price else if (transactionType == "retail") product.retail_price else product.capital_price
            if (product.variants.size > 0) {
                totalItems = 0
                product.variants.forEach {
                    product.variantsChoosed[it.first] = 0
                }
            }
        }

        val intentFilter = IntentFilter("CHANGE_AMOUNT")
        requireContext().registerReceiver(broadcastReceiver, intentFilter)
    }

    @SuppressLint("CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_transaction_manage_product, container, false)

        val txtId : TextView
        val txtName : TextView
        val etPrice : TextView

        val txtAmountEdit : TextView
        val btnMin : MaterialButton
        val btnPlus : MaterialButton

        val txtVariantName : TextView
        val rvVariantAmount : RecyclerView

        if (SettingsSharedPreferences(requireContext()).use_image) {
            view.findViewById<View>(R.id.bottomSheetManageTransactionProduct_layoutProductViewLongNoPicture).visibility = View.GONE
            val layout = view.findViewById<View>(R.id.bottomSheetManageTransactionProduct_layoutProductViewLong)

            val imageView = layout.findViewById<ImageView>(R.id.layoutProductViewLong_img)
            Thread {
                val bitmap: Bitmap
                try {
                    bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                requireContext().contentResolver,
                                product.images[0].toUri()
                            )
                        )
                    else
                        MediaStore.Images.Media.getBitmap(
                            requireContext().contentResolver,
                            product.images[0].toUri()
                        )
                    imageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.run()

            layout.apply {
                findViewById<TextView>(R.id.layoutProductViewLong_txtPrice).visibility = View.GONE
                findViewById<LinearLayout>(R.id.layoutProductViewLong_layoutEditPrice).visibility = View.VISIBLE
            }
            txtId = layout.findViewById(R.id.layoutProductViewLong_txtId)
            txtName = layout.findViewById(R.id.layoutProductViewLong_txtName)
            etPrice = layout.findViewById(R.id.layoutProductViewLong_etPrice)
            txtSubtotal = layout.findViewById(R.id.layoutProductViewLong_txtSubtotalPrice)
            txtAmount = layout.findViewById(R.id.layoutProductViewLong_txtTotalItem)
            txtAmountEdit = layout.findViewById(R.id.layoutProductViewLong_txtTotalItemEdit)
            btnMin = layout.findViewById(R.id.layoutProductViewLong_btnMin)
            btnPlus = layout.findViewById(R.id.layoutProductViewLong_btnPlus)
            txtVariantName = layout.findViewById(R.id.layoutProductViewLong_txtVariantName2)
            rvVariantAmount = layout.findViewById(R.id.layoutProductViewLong_rvVariantAmount)

            if (product.variants.size > 0) {
                layout.findViewById<LinearLayout>(R.id.layoutProductViewLong_layoutEditVariantAmount).visibility = View.VISIBLE
                rvVariantAmount.visibility = View.VISIBLE
            } else {
                txtAmount.visibility = View.GONE
                layout.findViewById<LinearLayout>(R.id.layoutProductViewLong_layoutEditTotalItem).visibility = View.VISIBLE
            }
        } else {
            view.findViewById<View>(R.id.bottomSheetManageTransactionProduct_layoutProductViewLong).visibility = View.GONE
            val layout = view.findViewById<View>(R.id.bottomSheetManageTransactionProduct_layoutProductViewLongNoPicture)
            layout.apply {
                findViewById<TextView>(R.id.layoutProductViewLongNoPicture_txtPrice).visibility = View.GONE
                findViewById<LinearLayout>(R.id.layoutProductViewLongNoPicture_layoutEditPrice).visibility = View.VISIBLE
            }
            txtId = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtId)
            txtName = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtName)
            etPrice = layout.findViewById(R.id.layoutProductViewLongNoPicture_etPrice)
            txtSubtotal = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtSubtotalPrice)
            txtAmount = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtTotalItem)
            txtAmountEdit = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtTotalItemEdit)
            btnMin = layout.findViewById(R.id.layoutProductViewLongNoPicture_btnMin)
            btnPlus = layout.findViewById(R.id.layoutProductViewLongNoPicture_btnPlus)
            txtVariantName = layout.findViewById(R.id.layoutProductViewLongNoPicture_txtVariantName2)
            rvVariantAmount = layout.findViewById(R.id.layoutProductViewLongNoPicture_rvVariantAmount)

            if (product.variants.size > 0) {
                layout.findViewById<LinearLayout>(R.id.layoutProductViewLongNoPicture_layoutEditVariantAmount).visibility = View.VISIBLE
                rvVariantAmount.visibility = View.VISIBLE
            } else {
                txtAmount.visibility = View.GONE
                layout.findViewById<LinearLayout>(R.id.layoutProductViewLongNoPicture_layoutEditTotalItem).visibility = View.VISIBLE
            }
        }

        val btnConfirm = view.findViewById<MaterialButton>(R.id.bottomSheetManageTransactionProduct_btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.bottomSheetManageTransactionProduct_btnCancel)

        updateSubtotal()
        txtId.text = product.id
        txtName.text = product.name
        etPrice.setText(numberFormat.format(customPrice))
        etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etPrice.text.isNotEmpty()) {
                    customPrice = etPrice.text.toString().toInt()
                    if (etPrice.text.toString()[0] == '0') etPrice.setText(customPrice)
                } else
                    customPrice = 0
                updateSubtotal()
            }

            override fun afterTextChanged(p0: Editable?) { }
        })
        etPrice.setOnFocusChangeListener { v, hasFocus ->
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

        if (product.variants.size > 0) {
            txtAmount.text = numberFormat.format(totalItems)
            txtVariantName.text = product.variant_name
            rvVariantAmount.apply {
                adapter = RVVariantAmount("select", product.variants, product.variantsChoosed)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        txtAmountEdit.text = numberFormat.format(totalItems)
        btnMin.setOnClickListener {
            if (--totalItems <= 0) {
                dismiss()
                return@setOnClickListener
            }
            txtAmountEdit.text = numberFormat.format(totalItems)
            updateSubtotal()
        }
        btnPlus.setOnClickListener {
            totalItems++
            txtAmountEdit.text = numberFormat.format(totalItems)
            updateSubtotal()
        }


        btnConfirm.setOnClickListener {
            if (totalItems <= 0) {
                val cancelIntent = Intent("PRODUCT_REMOVED")
                cancelIntent.putExtra("PRODUCT_ID", product.id)
                requireContext().sendBroadcast(cancelIntent)
                dismiss()
            }

            val intent = Intent("PRODUCT_ADDED")
            intent.putExtra("PRODUCT_ID", product.id)
            intent.putExtra("AMOUNT", totalItems)
            intent.putExtra("PRICE", customPrice)
            if (product.variants.size > 0) {
                intent.putExtra("VARIANTS_AMOUNT", product.variantsChoosed)
            }
            Toast.makeText(requireContext(), "Product successfully added.", Toast.LENGTH_SHORT).show()
            requireContext().sendBroadcast(intent)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun updateSubtotal() {
        val subtotal = customPrice * totalItems
        txtSubtotal.text = currencyFormat.format(subtotal)
    }
}