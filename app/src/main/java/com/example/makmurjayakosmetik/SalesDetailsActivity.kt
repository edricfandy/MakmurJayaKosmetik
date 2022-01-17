package com.example.makmurjayakosmetik

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.classes.Sales
import com.example.makmurjayakosmetik.classes.Store
import com.example.makmurjayakosmetik.dialogs.AddEditSalesDialog
import com.example.makmurjayakosmetik.dialogs.SelectedProductListDialog
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProduct
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProductNoPicture
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SalesDetailsActivity : AppCompatActivity() {
    private lateinit var sales : Sales
    private lateinit var id : String
    private lateinit var db : DBHelper
    private lateinit var numberFormat: NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private lateinit var dateFormat : SimpleDateFormat

    private lateinit var toolbar : MaterialToolbar
    private lateinit var txtId : TextView
    private lateinit var txtType : MaterialButton
    private lateinit var txtPaymentStatus : MaterialButton
    private lateinit var txtItemStatus : MaterialButton
    private lateinit var txtStore : TextView
    private lateinit var txtDatetime : TextView
    private lateinit var txtTotalItem : TextView
    private lateinit var txtTotalPurchase : TextView
    private lateinit var rvItemList : RecyclerView
    private lateinit var btnChangePaymentStatus : MaterialButton
    private lateinit var btnChangeItemStatus : MaterialButton
    private lateinit var btnEdit : MaterialButton
    private lateinit var btnCancel : MaterialButton

    private val editSalesBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "MODIFIED") {
                id = p1.extras?.getString("MODIFIED_SALES_ID") ?: id
                updateSalesData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_details)

        db = DBHelper(this)
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        dateFormat = SimpleDateFormat("EE, dd MMMM yyyy HH:mm:ss", Locale("in", "ID"))
        val intentFilter = IntentFilter("MODIFIED")
        registerReceiver(editSalesBroadcastReceiver, intentFilter)

        toolbar = findViewById(R.id.salesDetail_toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        txtId = findViewById(R.id.salesDetail_txtId)
        txtType = findViewById(R.id.salesDetail_txtType)
        txtPaymentStatus = findViewById(R.id.salesDetail_txtPaymentStatus)
        txtItemStatus = findViewById(R.id.salesDetail_txtItemStatus)
        txtStore = findViewById(R.id.salesDetail_txtStore)
        txtDatetime = findViewById(R.id.salesDetail_txtDatetime)
        txtTotalItem = findViewById(R.id.salesDetail_txtTotalItem)
        txtTotalPurchase = findViewById(R.id.salesDetail_txtTotalPurchase)
        rvItemList = findViewById(R.id.salesDetail_recyclerView)

        btnChangePaymentStatus = findViewById(R.id.salesDetail_btnChangePaymentStatus)
        btnChangeItemStatus = findViewById(R.id.salesDetail_btnChangeItemStatus)
        btnEdit = findViewById(R.id.salesDetail_btnEdit)
        btnCancel = findViewById(R.id.salesDetail_btnCancel)

        id = intent.extras?.getString("SALES_ID") ?: ""
        updateSalesData()

        btnChangePaymentStatus.setOnClickListener {
            val dialog = AlertDialog.Builder(this@SalesDetailsActivity)
                .setTitle("Change Payment Status")
                .setMessage("Are you sure want to change the payment status of this sale to \"Paid\"?")
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                    db.updateSalesPaymentStatus(sales.id, "paid")
                    updateSalesData()
                    dialogInterface.dismiss()
                }
                .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        btnChangeItemStatus.setOnClickListener {
            val dialog = AlertDialog.Builder(this@SalesDetailsActivity)
                .setTitle("Change Item Status")
                .setMessage("Are you sure want to change the item status of this sale to \"Picked Up/Shipped\"?")
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                    db.updateSalesItemStatus(sales.id, "picked_up_shipped")
                    updateSalesData()
                    dialogInterface.dismiss()
                }
                .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        btnEdit.setOnClickListener {
            if (AccountSharedPreferences(this).username != "admin") {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Access Not Allowed")
                    .setMessage("This account doesn't have the access permission for editing sales transactions. Contact the 'admin' account for requesting permission.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }

            val dialog = AddEditSalesDialog("edit")
            val bundle = Bundle()
            bundle.putString("SALES_ID", sales.id)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager.beginTransaction(), null)
        }

        btnCancel.setOnClickListener {
            if (sales.payment_status != "not_paid_yet" || sales.item_status != "waiting_for_pickup_shipping") {
                val dialogUnable = AlertDialog.Builder(this)
                    .setTitle("Unable to Cancel Transaction")
                    .setMessage("This sales transaction cannot be canceled anymore because payment or item status had been changed.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialogUnable.show()
                return@setOnClickListener
            }

            val dialogConfirmation = AlertDialog.Builder(this).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_sales, null)

            layoutConfirmation.apply {
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation1).text = getString(R.string.cancel_confirmation_message_arg, "Sales")
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtDatetimeAndStore).text = getString(R.string.datetime_and_store, dateFormat.format(
                    Date()
                ), sales.store.name)
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtId).text = sales.id
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtType).text = if (sales.type == "wholesale") "Wholesale" else "Retail"
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalItem).text = numberFormat.format(sales.total_item)
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_txtTotalPrice).text = currencyFormat.format(sales.total_purchase)
                findViewById<TextView>(R.id.dialogConfirmationOperationSales_msgConfirmation3).text = getString(
                    R.string.cancel_confirmation_message_arg_2, "Sales transaction"
                )
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationSales_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnProductList).setOnClickListener {
                    val dialog = SelectedProductListDialog("view", sales.listProduct)
                    dialog.show(supportFragmentManager.beginTransaction(), "LIST_SELECTED_PRODUCT")
                }

                findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnConfirm).setOnClickListener {
                    db.cancelSales(sales.id)
                    dialogConfirmation.dismiss()
                    Toast.makeText(
                        this@SalesDetailsActivity,
                        "Sales transaction has been canceled.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                findViewById<MaterialButton>(R.id.dialogConfirmationOperationSales_btnCancel).setOnClickListener {
                    dialogConfirmation.dismiss()
                }
            }
            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSalesData() {
        sales = db.getSalesById(id) ?: Sales("", Store("", "", ""), "", "", "", Date(), 0, 0)
        sales.listProduct = db.getSalesDetailsById(sales.id)

        toolbar.title = "Sales Details (ID: ${sales.id})"
        txtId.text = sales.id
        txtType.text = if (sales.type == "wholesale") "Wholesale" else "Retail"
        txtPaymentStatus.apply {
            when (sales.payment_status) {
                "canceled" -> {
                    text = "Canceled"
                    setBackgroundColor(getColor(R.color.dark_red))
                    setTextColor(getColor(R.color.white))
                }
                "not_paid_yet" -> {
                    text = "Not Paid Yet"
                    setBackgroundColor(getColor(R.color.middle_yellow))
                    setTextColor(getColor(R.color.dark_brown))
                }
                "paid" -> {
                    text = "Paid"
                    setBackgroundColor(getColor(R.color.dark_green))
                    setTextColor(getColor(R.color.white))
                }
            }
        }

        txtItemStatus.apply {
            when (sales.item_status) {
                "canceled" -> visibility = View.GONE
                "waiting_for_pickup_shipping" -> {
                    text = "Waiting for Pickup/Shipping"
                    setBackgroundColor(getColor(R.color.middle_yellow))
                    setTextColor(getColor(R.color.dark_brown))
                }
                "picked_up_shipped" -> {
                    text = "Picked Up/Shipped"
                    setBackgroundColor(getColor(R.color.dark_green))
                    setTextColor(getColor(R.color.white))
                }
            }
        }

        txtStore.apply {
            text = getString(R.string.store_name_and_platform, sales.store.name, sales.store.platform)
            when (sales.store.platform) {
                "Offline Store" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.drawable.ic_store), null, null, null)
                "WhatsApp" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.drawable.ic_whatsapp), null, null, null)
                "Facebook" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.drawable.ic_facebook), null, null, null)
                "Instagram" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.drawable.ic_instagram), null, null, null)
                "Shopee" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.mipmap.ic_shopee), null, null, null)
                "Tokopedia" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(this@SalesDetailsActivity, R.mipmap.ic_tokopedia), null, null, null)
            }
        }

        txtDatetime.text = dateFormat.format(sales.datetime)
        txtTotalItem.text = numberFormat.format(sales.total_item)
        txtTotalPurchase.text = currencyFormat.format(sales.total_purchase)
        rvItemList.apply {
            adapter = if (SettingsSharedPreferences(this@SalesDetailsActivity).use_image)
                RVSelectedProduct("view", sales.listProduct)
            else
                RVSelectedProductNoPicture("view", sales.listProduct)
            layoutManager = LinearLayoutManager(this@SalesDetailsActivity)
        }
        if (sales.payment_status != "not_paid_yet") {
            btnChangePaymentStatus.isEnabled = false
            if (sales.payment_status == "paid") btnChangePaymentStatus.text = "Payment status already \"Paid\""
            else if (sales.payment_status == "canceled") btnChangePaymentStatus.text = "The sales transaction had been Canceled"
        }
        if (sales.item_status != "waiting_for_pickup_shipping") {
            btnChangeItemStatus.isEnabled = false
            if (sales.item_status == "picked_up_shipped") btnChangeItemStatus.text = "Item already \"Picked Up / Shipped\""
            else if (sales.item_status == "canceled") btnChangePaymentStatus.text = "The sales transaction had been Canceled"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(editSalesBroadcastReceiver)
    }
}