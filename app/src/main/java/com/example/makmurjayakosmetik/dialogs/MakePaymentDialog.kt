package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Payment
import com.example.makmurjayakosmetik.classes.Purchase
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*

class MakePaymentDialog : DialogFragment() {
    private var purchase : Purchase? = null
    private lateinit var db: DBHelper
    private lateinit var numberFormat: NumberFormat
    private lateinit var currencyFormat: NumberFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        purchase = db.getPurchaseById(arguments?.getString("PURCHASE_ID") ?: "")
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_make_payment, container, false)

        val txtPurchaseId = view.findViewById<TextView>(R.id.dialogMakePayment_txtPurchaseId)
        val txtTotalPurchase = view.findViewById<TextView>(R.id.dialogMakePayment_txtTotalPurchase)
        val txtTotalPaid = view.findViewById<TextView>(R.id.dialogMakePayment_txtTotalPaid)
        val txtDebtRemain = view.findViewById<TextView>(R.id.dialogMakePayment_txtDebtRemain)
        val etPaymentAmount = view.findViewById<EditText>(R.id.dialogMakePayment_etPaymentAmount)
        val etMessage = view.findViewById<EditText>(R.id.dialogMakePayment_etMessage)
        val txtDebtRemainAfterPayment = view.findViewById<TextView>(R.id.dialogMakePayment_txtDebtRemainAfterPayment)
        val btnMakePayment = view.findViewById<MaterialButton>(R.id.dialogMakePayment_btnMakePayment)

        val debtRemain = (purchase?.total_purchase ?: 0) - (purchase?.total_paid ?: 0)

        txtPurchaseId.text = purchase?.id
        txtTotalPurchase.text = currencyFormat.format(purchase?.total_purchase)
        txtTotalPaid.text = currencyFormat.format(purchase?.total_paid)
        txtDebtRemain.text = currencyFormat.format(debtRemain)
        txtDebtRemainAfterPayment.text = txtDebtRemain.text

        etPaymentAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (etPaymentAmount.text.isNotEmpty()) {
                    if (etPaymentAmount.text.toString().toInt() > debtRemain)
                        etPaymentAmount.setText(debtRemain.toString())
                    txtDebtRemainAfterPayment.text = (debtRemain - etPaymentAmount.text.toString().toInt()).toString()
                } else {
                    txtDebtRemainAfterPayment.text = debtRemain.toString()
                }
            }

            override fun afterTextChanged(s: Editable?) {  }
        })

        etPaymentAmount.setOnFocusChangeListener { v, hasFocus ->
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

        btnMakePayment.setOnClickListener {
            if (etPaymentAmount.text.isEmpty()) {
                val invalidDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Form Incomplete")
                    .setMessage("Please fill out the total payment.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                invalidDialog.show()
            }

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Make Payment")
                .setMessage("Are you sure want to make payment for purchase with ID: ${purchase?.id} with total payment: ${currencyFormat.format(etPaymentAmount.text)}")
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                    val payment = Payment(
                        db.generatePaymentIdForPurchase(purchase?.id ?: ""),
                        purchase?.id ?: "",
                        Date(),
                        (numberFormat.parse(etPaymentAmount.text.toString()) ?: 0).toInt(),
                        etMessage.text.toString()
                    )
                    db.insertPayment(payment)
                    dialogInterface.dismiss()
                }
                .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        return view
    }
}