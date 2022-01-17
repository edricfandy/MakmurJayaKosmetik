package com.example.makmurjayakosmetik.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Payment
import com.example.makmurjayakosmetik.classes.Purchase
import com.example.makmurjayakosmetik.recyclerview.RVPayment
import com.example.makmurjayakosmetik.recyclerview.RVPurchase

class PaymentFragment : Fragment() {
    private lateinit var db: DBHelper
    private lateinit var listPurchaseInDebt: ArrayList<Purchase>
    private lateinit var listPayment: ArrayList<Payment>

    private lateinit var rvPurchaseInDebt: RecyclerView
    private lateinit var rvPaymentHistory: RecyclerView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "DELETE_PAYMENT") {
                val id = intent.extras?.getString("PAYMENT_ID")
                if (id != null) {
                    db.deletePayment(id)
                    updatePaymentHistory()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        listPurchaseInDebt = db.getPurchasesInDebt()
        listPayment = db.getAllPayment()

        val intentFilter = IntentFilter()
        intentFilter.addAction("DELETE_PAYMENT")
        requireContext().registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        rvPurchaseInDebt = view.findViewById(R.id.managePayment_rvPurchaseInDebt)
        rvPaymentHistory = view.findViewById(R.id.managePayment_rvPaymentHistory)

        rvPurchaseInDebt.apply {
            adapter = RVPurchase(listPurchaseInDebt)
            layoutManager = LinearLayoutManager(requireContext())
        }

        rvPaymentHistory.apply {
            adapter = RVPayment(listPayment)
            layoutManager = LinearLayoutManager(requireContext())
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateListPurchaseInDebt() {
        listPurchaseInDebt.clear()
        listPurchaseInDebt.addAll(db.getPurchasesInDebt())
        rvPurchaseInDebt.adapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePaymentHistory() {
        listPayment.clear()
        listPayment.addAll(db.getAllPayment())
        rvPaymentHistory.adapter?.notifyDataSetChanged()
    }
}