package com.example.makmurjayakosmetik

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.makmurjayakosmetik.classes.Category
import com.example.makmurjayakosmetik.classes.Product
import com.example.makmurjayakosmetik.classes.Supplier
import com.example.makmurjayakosmetik.dialogs.AddEditProductDialog
import com.example.makmurjayakosmetik.recyclerview.RVProductImage
import com.example.makmurjayakosmetik.recyclerview.RVVariantAmount
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var product : Product
    private lateinit var id : String
    private lateinit var db : DBHelper
    private lateinit var currencyFormat : NumberFormat
    private lateinit var toolbar: MaterialToolbar
    private lateinit var viewPagerImage : ViewPager2

    private lateinit var txtId : TextView
    private lateinit var txtName : TextView
    private lateinit var txtSupplier : TextView
    private lateinit var txtCapitalPrice : TextView
    private lateinit var txtRetailPrice : TextView
    private lateinit var txtWholesalePrice : TextView
    private lateinit var txtCategoryName : TextView
    private lateinit var txtCategoryDesc : TextView
    private lateinit var layoutVariant : LinearLayout
    private lateinit var txtVariantName : TextView
    private lateinit var txtTotalStock : TextView
    private lateinit var rvVariantAmount : RecyclerView

    private val editProductBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "MODIFIED") {
                id = p1.extras?.getString("MODIFIED_PRODUCT_ID") ?: id
                updateProductData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        db = DBHelper(this)
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val intentFilter = IntentFilter("MODIFIED")
        registerReceiver(editProductBroadcastReceiver, intentFilter)

        toolbar = findViewById(R.id.productDetail_toolbar)
        toolbar.setNavigationOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        txtId = findViewById(R.id.productDetail_txtId)
        txtName = findViewById(R.id.productDetail_txtName)
        txtSupplier = findViewById(R.id.productDetail_txtSupplier)
        txtCapitalPrice = findViewById(R.id.productDetail_txtCapitalPrice)
        txtRetailPrice = findViewById(R.id.productDetail_txtRetailPrice)
        txtWholesalePrice = findViewById(R.id.productDetail_txtWholesalePrice)
        txtCategoryName = findViewById(R.id.productDetail_txtCategoryName)
        txtCategoryDesc = findViewById(R.id.productDetail_txtCategoryDetails)
        layoutVariant = findViewById(R.id.productDetail_layoutVariant)
        txtVariantName = findViewById(R.id.productDetail_txtVariantName)
        txtTotalStock = findViewById(R.id.productDetail_txtTotalStock)
        rvVariantAmount = findViewById(R.id.productDetail_rvVariantAmount)
        viewPagerImage = findViewById(R.id.productDetail_vpImage)

        val btnEdit = findViewById<MaterialButton>(R.id.productDetail_btnEdit)
        val btnDelete = findViewById<MaterialButton>(R.id.productDetail_btnDelete)

        id = intent.extras?.getString("PRODUCT_ID") ?: ""
        viewPagerImage.visibility = if (SettingsSharedPreferences(this).use_image) View.VISIBLE else View.GONE
        updateProductData()

        btnEdit.setOnClickListener {
            val dialog = AddEditProductDialog("edit")
            val bundle = Bundle()
            bundle.putString("PRODUCT_ID", product.id)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager.beginTransaction(), null)
        }

        btnDelete.setOnClickListener {
            val dialogConfirmation = AlertDialog.Builder(this).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_product, null)

            layoutConfirmation.apply {
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation1).text = getString(R.string.delete_confirmation_message_arg, "Product")
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtId).text = product.id
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtName).text = product.name
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCategory).text = product.category.name
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtCapitalPrice).text = currencyFormat.format(product.capital_price)
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtWholesalePrice).text = currencyFormat.format(product.wholesale_price)
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtRetailPrice).text = currencyFormat.format(product.retail_price)
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_txtTotalStock).text = NumberFormat.getIntegerInstance().format(product.total_stock)
                findViewById<TextView>(R.id.dialogConfirmationOperationProduct_msgConfirmation3).text = getString(
                    R.string.delete_confirmation_message_arg_2, "Product"
                )
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationProduct_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                    db.deleteProduct(product.id)
                    Toast.makeText(this@ProductDetailsActivity, "Product successfully deleted.", Toast.LENGTH_SHORT).show()
                    dialogConfirmation.dismiss()
                    setResult(0)
                    finish()
                }

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                    dialogConfirmation.dismiss()
                }
            }
            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }
    }

    private fun updateProductData() {
        product = db.getProductById(id) ?: Product("", "", Category("", ""), 0, 0, 0, 0, "", arrayListOf(), Supplier("", "", "", "", "", ""))

        toolbar.title = "Product Details (ID: ${product.id})"
        viewPagerImage.adapter = RVProductImage(this@ProductDetailsActivity, "slideshow", product.images)
        txtId.text = product.id
        txtName.text = product.name
        txtSupplier.text = if (product.supplier.id.isEmpty()) "Supplier not set." else getString(R.string.supplier_id_name_and_city, product.supplier.id, product.supplier.name, product.supplier.city)
        txtCapitalPrice.text = currencyFormat.format(product.capital_price)
        txtWholesalePrice.text = currencyFormat.format(product.wholesale_price)
        txtRetailPrice.text = currencyFormat.format(product.retail_price)
        txtCategoryName.text = product.category.name
        txtCategoryDesc.text = product.category.desc
        txtTotalStock.text = product.total_stock.toString()
        if (product.variants.size <= 0)
            layoutVariant.visibility = View.GONE
        else {
            layoutVariant.visibility = View.VISIBLE
            txtVariantName.text = product.variant_name
            rvVariantAmount.apply {
                adapter = RVVariantAmount("view", product.variants, mutableMapOf())
                layoutManager = LinearLayoutManager(this@ProductDetailsActivity)
                val divider = DividerItemDecoration(this@ProductDetailsActivity, DividerItemDecoration.VERTICAL)
                addItemDecoration(divider)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(editProductBroadcastReceiver)
    }
}