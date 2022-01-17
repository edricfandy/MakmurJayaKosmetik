package com.example.makmurjayakosmetik.recyclerview

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Payment
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RVPayment(private val list: ArrayList<Payment>) : RecyclerView.Adapter<RVPayment.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var payment: Payment
        val dateFormat : SimpleDateFormat = SimpleDateFormat("E, dd MMMM yyyy HH:mm:ss", Locale("in", "ID"))
        val currencyFormat : NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        val txtPaymentId : TextView = itemView.findViewById(R.id.layoutPaymentView_txtPaymentId)
        val txtPurchaseId : TextView = itemView.findViewById(R.id.layoutPaymentView_txtPurchaseId)
        val txtDatetime : TextView = itemView.findViewById(R.id.layoutPaymentView_txtDatetime)
        val txtTotalPayment : TextView = itemView.findViewById(R.id.layoutPaymentView_txtTotalPayment)
        val layoutHidden : LinearLayout = itemView.findViewById(R.id.layoutPaymentView_layoutHidden)
        val txtPaymentMessage : TextView = itemView.findViewById(R.id.layoutPaymentView_txtPaymentMessage)
        val btnDeletePayment : MaterialButton = itemView.findViewById(R.id.layoutPaymentView_btnDeletePayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_payment_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            payment = list[position]
            txtPaymentId.text = payment.id
            txtPurchaseId.text = payment.purchase_id
            txtDatetime.text = dateFormat.format(payment.datetime)
            txtTotalPayment.text = currencyFormat.format(payment.total_paid)
            txtPaymentMessage.text = if (payment.message.isEmpty()) "No Message." else payment.message
            btnDeletePayment.setOnClickListener {
                val dialog = AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Payment")
                    .setMessage("Are you sure want to delete this Payment? The purchase payment status would be revert to 'In Debt', " +
                            "and the amount of this payment would be added to the total debt of the purchase.")
                    .setIcon(AppCompatResources.getDrawable(itemView.context, R.drawable.ic_warning))
                    .setPositiveButton("CONFIRM") { dialogInterface : DialogInterface, _: Int ->
                        val intent = Intent("DELETE_PAYMENT")
                        intent.putExtra("PAYMENT_ID", payment.id)
                        itemView.context.sendBroadcast(intent)
                        Toast.makeText(itemView.context, "Payment successfully deleted.", Toast.LENGTH_SHORT).show()
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton("CANCEL") { dialogInterface : DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
            }

            itemView.setOnClickListener {
                if (layoutHidden.visibility == View.VISIBLE) layoutHidden.visibility = View.GONE
                else layoutHidden.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}