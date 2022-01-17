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
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Purchase
import com.example.makmurjayakosmetik.dialogs.AddEditPurchaseDialog
import com.example.makmurjayakosmetik.recyclerview.RVPurchase
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class PurchaseFragment : Fragment() {
    private lateinit var db : DBHelper
    private lateinit var listPurchases : ArrayList<Purchase>
    private lateinit var filteredPurchases : ArrayList<Purchase>
    private lateinit var numberFormat : NumberFormat
    private lateinit var currencyFormat : NumberFormat

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtHeading : TextView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton

    private lateinit var btnAll : MaterialButton
    private lateinit var btnCashPurchase : MaterialButton
    private lateinit var btnCreditPurchase : MaterialButton
    private lateinit var btnInDebt : MaterialButton
    private lateinit var btnPaidOff : MaterialButton
    private lateinit var btnCanceled : MaterialButton

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "MODIFIED")
                updateRecyclerView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        listPurchases = db.getAllPurchase()
        filteredPurchases = ArrayList(listPurchases)
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val intentFilter = IntentFilter("MODIFIED")
        requireContext().registerReceiver(broadcastReceiver, intentFilter)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_purchase, container, false)

        recyclerView = view.findViewById(R.id.managePurchase_recyclerView)
        txtHeading = view.findViewById(R.id.managePurchase_txtHeading)
        hiddenLayout = view.findViewById(R.id.managePurchase_layoutEmptyItem)
        fabAdd = view.findViewById(R.id.managePurchase_fabAdd)

        btnAll = view.findViewById(R.id.managePurchase_btnAll)
        btnCashPurchase = view.findViewById(R.id.managePurchase_btnCashPurchase)
        btnCreditPurchase = view.findViewById(R.id.managePurchase_btnCreditPurchase)
        btnInDebt = view.findViewById(R.id.managePurchase_btnInDebt)
        btnPaidOff = view.findViewById(R.id.managePurchase_btnPaidOff)
        btnCanceled = view.findViewById(R.id.managePurchase_btnCanceled)

        if (listPurchases.size <= 0)
            view.apply {
                findViewById<LinearLayout>(R.id.managePurchase_layoutEmptyItem).visibility = View.VISIBLE
                findViewById<TextView>(R.id.managePurchase_msgEmptyItem).text = getString(R.string.item_empty_message, "Purchase")
            }

        recyclerView.apply {
            adapter = RVPurchase(filteredPurchases)
            layoutManager = LinearLayoutManager(context)
        }
        filterRecyclerView("all")
        txtHeading.text = "All Purchase"

        btnAll.setOnClickListener {
            filterRecyclerView("all")
        }

        btnCashPurchase.setOnClickListener {
            filterRecyclerView("cash")
        }

        btnCreditPurchase.setOnClickListener {
            filterRecyclerView("credit")
        }

        btnInDebt.setOnClickListener {
            filterRecyclerView("in_debt")
        }

        btnPaidOff.setOnClickListener {
            filterRecyclerView("paid_off")
        }

        btnCanceled.setOnClickListener {
            filterRecyclerView("canceled")
        }

        fabAdd.setOnClickListener {
            val dialog = AddEditPurchaseDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        listPurchases.clear()
        listPurchases = db.getAllPurchase()
        filterRecyclerView("all")
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun filterRecyclerView(selected: String) {
        btnAll.setBackgroundColor(requireContext().getColor(R.color.white))
        btnAll.setTextColor(requireContext().getColor(R.color.blue))
        btnCashPurchase.setBackgroundColor(requireContext().getColor(R.color.white))
        btnCashPurchase.setTextColor(requireContext().getColor(R.color.blue))
        btnCreditPurchase.setBackgroundColor(requireContext().getColor(R.color.white))
        btnCreditPurchase.setTextColor(requireContext().getColor(R.color.blue))
        btnInDebt.setBackgroundColor(requireContext().getColor(R.color.white))
        btnInDebt.setTextColor(requireContext().getColor(R.color.blue))
        btnPaidOff.setBackgroundColor(requireContext().getColor(R.color.white))
        btnPaidOff.setTextColor(requireContext().getColor(R.color.blue))
        btnCanceled.setBackgroundColor(requireContext().getColor(R.color.white))
        btnCanceled.setTextColor(requireContext().getColor(R.color.blue))

        when (selected) {
            "all" -> {
                btnAll.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnAll.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "All Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases)
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "cash" -> {
                btnCashPurchase.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnCashPurchase.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "Cash Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases.filter { it.payment_method == "cash" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "credit" -> {
                btnCreditPurchase.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnCreditPurchase.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "Credit Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases.filter { it.payment_method == "credit" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "in_debt" -> {
                btnInDebt.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnInDebt.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "In Debt Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases.filter { it.total_purchase - it.total_paid > 0 })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "paid_off" -> {
                btnInDebt.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnInDebt.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "Paid Off Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases.filter { it.total_purchase - it.total_paid == 0 })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "canceled" -> {
                btnCanceled.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnCanceled.setTextColor(requireContext().getColor(R.color.white))
                txtHeading.text = "Canceled Purchase"
                filteredPurchases.clear()
                filteredPurchases.addAll(listPurchases.filter { it.payment_method == "canceled" && it.total_paid < 0 })
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView()
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(broadcastReceiver)
    }
}