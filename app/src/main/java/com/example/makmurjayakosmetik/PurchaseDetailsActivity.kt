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
import com.example.makmurjayakosmetik.classes.Purchase
import com.example.makmurjayakosmetik.classes.Supplier
import com.example.makmurjayakosmetik.dialogs.AddEditPurchaseDialog
import com.example.makmurjayakosmetik.dialogs.MakePaymentDialog
import com.example.makmurjayakosmetik.dialogs.SelectedProductListDialog
import com.example.makmurjayakosmetik.recyclerview.RVSelectedProduct
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PurchaseDetailsActivity : AppCompatActivity() {
    private lateinit var purchase : Purchase
    private lateinit var id : String
    private lateinit var db : DBHelper
    private lateinit var numberFormat: NumberFormat
    private lateinit var currencyFormat : NumberFormat
    private lateinit var dateFormat : SimpleDateFormat

    private lateinit var toolbar : MaterialToolbar
    private lateinit var txtId : TextView
    private lateinit var txtPaymentMethod : MaterialButton
    private lateinit var txtPaymentStatus : MaterialButton
    private lateinit var txtSupplier : TextView
    private lateinit var txtDatetime : TextView
    private lateinit var txtItemCheckedStatus : TextView
    private lateinit var txtTotalItem : TextView
    private lateinit var txtTotalPurchase : TextView
    private lateinit var txtTotalPaid : TextView
    private lateinit var txtDebtRemain : TextView
    private lateinit var rvItemList : RecyclerView
    private lateinit var btnChangeStatus : MaterialButton
    private lateinit var btnMakePayment : MaterialButton
    private lateinit var btnEdit : MaterialButton
    private lateinit var btnCancel : MaterialButton

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "MODIFIED") {
                id = p1.extras?.getString("MODIFIED_PURCHASE_ID") ?: id
                updatePurchaseData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_details)

        db = DBHelper(this)
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        dateFormat = SimpleDateFormat("E, dd MMMM yyyy HH:mm:ss", Locale("in", "ID"))
        val intentFilter = IntentFilter("MODIFIED")
        registerReceiver(broadcastReceiver, intentFilter)

        toolbar = findViewById(R.id.purchaseDetail_toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        txtId = findViewById(R.id.purchaseDetail_txtId)
        txtPaymentMethod = findViewById(R.id.purchaseDetail_txtPaymentMethod)
        txtPaymentStatus = findViewById(R.id.purchaseDetail_txtPaymentStatus)
        txtSupplier = findViewById(R.id.purchaseDetail_txtSupplier)
        txtDatetime = findViewById(R.id.purchaseDetail_txtDatetime)
        txtItemCheckedStatus = findViewById(R.id.purchaseDetail_txtItemCheckedStatus)
        txtTotalItem = findViewById(R.id.purchaseDetail_txtTotalItem)
        txtTotalPurchase = findViewById(R.id.purchaseDetail_txtTotalPurchase)
        txtTotalPaid = findViewById(R.id.purchaseDetail_txtTotalPaid)
        txtDebtRemain = findViewById(R.id.purchaseDetail_txtDebtRemain)
        rvItemList = findViewById(R.id.purchaseDetail_recyclerView)

        btnChangeStatus = findViewById(R.id.purchaseDetail_btnChangeItemCheckedStatus)
        btnMakePayment = findViewById(R.id.purchaseDetail_btnMakePayment)
        btnEdit = findViewById(R.id.purchaseDetail_btnEdit)
        btnCancel = findViewById(R.id.purchaseDetail_btnCancel)

        id = intent.extras?.getString("PURCHASE_ID") ?: ""
        updatePurchaseData()

        btnChangeStatus.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
                .setTitle("Change Item Checked Status")
                .setMessage(getString(R.string.change_item_checked_status_confirmation_message, if (purchase.item_checked == "not_checked") "Not Checked" else if (purchase.item_checked == "checked") "Checked" else "Canceled"))
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                    db.updatePurchaseItemCheckedStatus(purchase.id, if (purchase.item_checked == "not_checked") "checked" else if (purchase.item_checked == "checked") "not_checked" else "canceled")
                    updatePurchaseData()
                    dialogInterface.dismiss()
                }
            dialog.show()
        }

        btnMakePayment.setOnClickListener {
            val dialog = MakePaymentDialog()
            val bundle = Bundle()
            bundle.putString("PURCHASE_ID", purchase.id)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "Make_Payment_Dialog")
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

            val dialog = AddEditPurchaseDialog("edit")
            val bundle = Bundle()
            bundle.putString("PURCHASE_ID", purchase.id)
            dialog.arguments = bundle
            dialog.show(supportFragmentManager.beginTransaction(), null)
        }

        btnCancel.setOnClickListener {
            val dialogConfirmation = AlertDialog.Builder(this).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_sales, null)

            layoutConfirmation.apply {
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation1).text = getString(R.string.cancel_confirmation_message_arg, "Purchase")
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtDatetime).text = dateFormat.format(Date())
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtId).text = purchase.id
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtSupplier).text = getString(R.string.supplier_id_name_and_city, purchase.supplier.id, purchase.supplier.name, purchase.supplier.city)
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtItemCheckedStatus).text = if (purchase.item_checked == "checked") "Checked" else "Not Checked"
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtPaymentMethod).text = if (purchase.payment_method == "cash") "Cash" else "Credit"
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalItem).text = numberFormat.format(purchase.total_item)
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_txtTotalPurchase).text = currencyFormat.format(purchase.total_purchase)
                findViewById<TextView>(R.id.dialogConfirmationOperationPurchase_msgConfirmation3).text = getString(
                    R.string.cancel_confirmation_message_arg_2, "Purchase transaction"
                )
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationPurchase_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperationPurchase_btnProductList).setOnClickListener {
                    val dialog = SelectedProductListDialog("view", purchase.listProduct)
                    dialog.show(supportFragmentManager.beginTransaction(), "LIST_SELECTED_PRODUCT")
                }

                findViewById<MaterialButton>(R.id.dialogConfirmationOperationPurchase_btnConfirm).setOnClickListener {
                    db.cancelPurchase(purchase.id)
                    dialogConfirmation.dismiss()
                    Toast.makeText(
                        this@PurchaseDetailsActivity,
                        "Purchase transaction has been canceled.",
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
    private fun updatePurchaseData() {
        purchase = db.getPurchaseById(id) ?: Purchase("", Supplier("", "", "", "", "", ""), Date(), "", "", 0, 0, 0)
        purchase.listProduct = db.getPurchaseDetailsById(purchase.id)

        toolbar.title = "Purchase Details (ID: ${purchase.id})"
        txtId.text = purchase.id
        txtPaymentMethod.text = if (purchase.payment_method == "cash") "Cash" else "Credit"
        txtPaymentStatus.apply {
            when {
                purchase.total_purchase - purchase.total_paid <= 0 -> {
                    text = "Paid Off"
                    setBackgroundColor(getColor(R.color.dark_green))
                    setTextColor(getColor(R.color.white))
                }
                purchase.total_purchase - purchase.total_paid > 0 -> {
                    text = "In Debt"
                    setBackgroundColor(getColor(R.color.middle_yellow))
                    setTextColor(getColor(R.color.dark_brown))
                }
                else -> {
                    text = "Canceled"
                    setBackgroundColor(getColor(R.color.dark_red))
                    setTextColor(getColor(R.color.white))
                }
            }
        }

        txtSupplier.text = getString(R.string.supplier_id_name_and_city, purchase.supplier.id, purchase.supplier.name, purchase.supplier.city)
        txtDatetime.text = dateFormat.format(purchase.datetime)
        txtItemCheckedStatus.apply {
            if (purchase.item_checked == "checked") {
                btnChangeStatus.isEnabled = false
                text = "Checked"
                setTextColor(getColor(R.color.dark_green))
                setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(this@PurchaseDetailsActivity, R.drawable.ic_true), null, null, null)
                btnChangeStatus.text = getString(R.string.change_item_checked_status_btn_msg, "Not Checked")
            } else if (purchase.item_checked == "not_checked") {
                text = "Not Checked"
                setTextColor(getColor(R.color.red))
                setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(this@PurchaseDetailsActivity, R.drawable.ic_false), null, null, null)
                btnChangeStatus.text = getString(R.string.change_item_checked_status_btn_msg, "Checked")
            }
        }

        txtTotalItem.text = numberFormat.format(purchase.total_item)
        txtTotalPurchase.text = currencyFormat.format(purchase.total_purchase)
        txtTotalPaid.text = currencyFormat.format(purchase.total_paid)
        txtDebtRemain.text = currencyFormat.format(purchase.total_purchase - purchase.total_paid)
        rvItemList.apply {
            adapter = RVSelectedProduct("view", purchase.listProduct)
            layoutManager = LinearLayoutManager(this@PurchaseDetailsActivity)
        }
        if (purchase.total_purchase - purchase.total_paid <= 0) btnMakePayment.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}