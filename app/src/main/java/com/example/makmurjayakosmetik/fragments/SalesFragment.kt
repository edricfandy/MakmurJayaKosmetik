package com.example.makmurjayakosmetik.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import com.example.makmurjayakosmetik.classes.Sales
import com.example.makmurjayakosmetik.dialogs.AddEditSalesDialog
import com.example.makmurjayakosmetik.recyclerview.RVSales
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SalesFragment : Fragment() {
    private lateinit var db : DBHelper
    private lateinit var listSales : ArrayList<Sales>
    private lateinit var filteredSales : ArrayList<Sales>
    private lateinit var numberFormat : NumberFormat
    private lateinit var currencyFormat : NumberFormat

    private lateinit var recyclerView: RecyclerView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton
    private lateinit var ddDatetime : AutoCompleteTextView
    private lateinit var etDate : EditText

    private lateinit var btnAll : MaterialButton
    private lateinit var btnNotPaidYet : MaterialButton
    private lateinit var btnPaid : MaterialButton
    private lateinit var btnWaitingForPickupShipping : MaterialButton
    private lateinit var btnPickedupShipped : MaterialButton
    private lateinit var btnDone : MaterialButton
    private lateinit var btnCanceled : MaterialButton
    private lateinit var txtHeading : TextView

    private val manageSalesBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "MODIFIED")
                updateRecyclerView(Date())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        listSales = db.getAllSales()
        filteredSales = ArrayList(listSales)
        numberFormat = NumberFormat.getNumberInstance(Locale("in", "ID"))
        currencyFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        val intentFilter = IntentFilter("MODIFIED")
        requireContext().registerReceiver(manageSalesBroadcastReceiver, intentFilter)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sales, container, false)

        recyclerView = view.findViewById(R.id.manageSales_recyclerView)
        hiddenLayout = view.findViewById(R.id.manageSales_layoutEmptyItem)
        ddDatetime = view.findViewById(R.id.manageSales_ddDatetime)
        etDate = view.findViewById(R.id.manageSales_etDate)
        fabAdd = view.findViewById(R.id.manageSales_fabAdd)

        btnAll = view.findViewById(R.id.manageSales_btnAll)
        btnNotPaidYet = view.findViewById(R.id.manageSales_btnNotPaidYet)
        btnPaid = view.findViewById(R.id.manageSales_btnPaid)
        btnWaitingForPickupShipping = view.findViewById(R.id.manageSales_btnWaitingForPickupShipping)
        btnPickedupShipped = view.findViewById(R.id.manageSales_btnPickedUpShipped)
        btnDone = view.findViewById(R.id.manageSales_btnDone)
        btnCanceled = view.findViewById(R.id.manageSales_btnCanceled)
        txtHeading = view.findViewById(R.id.manageSales_txtHeading)

        val layoutDate = view.findViewById<LinearLayout>(R.id.manageSales_layoutDate)
        val btnChooseDate = view.findViewById<ImageButton>(R.id.manageSales_btnChooseDate)

        if (listSales.size <= 0)
            view.apply {
                findViewById<LinearLayout>(R.id.manageSales_layoutEmptyItem).visibility = View.VISIBLE
                findViewById<TextView>(R.id.manageSales_msgEmptyItem).text = getString(R.string.item_empty_message, "Transaction")
            }

        recyclerView.apply {
            adapter = RVSales(filteredSales)
            layoutManager = LinearLayoutManager(context)
        }

        val timeList = listOf("Today", "Yesterday", "Last Week", "Custom")
        ddDatetime.setText("Today")
        filterRecyclerView("All")

        ddDatetime.apply {
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, timeList))
            setOnItemClickListener { adapterView, _, i, _ ->
                var date: Date = Calendar.getInstance().time
                when (adapterView.getItemAtPosition(i)) {
                    "Today" -> {
                        layoutDate.visibility = View.GONE
                        date = Calendar.getInstance().time
                    }
                    "Yesterday" -> {
                        layoutDate.visibility = View.GONE
                        val time = Calendar.getInstance()
                        time.add(Calendar.DATE, -1)
                        date = time.time
                    }
                    "Last Week" -> {
                        layoutDate.visibility = View.GONE
                        val time = Calendar.getInstance()
                        time.add(Calendar.DATE, -7)
                        date = time.time
                    }
                    "Custom" -> {
                        layoutDate.visibility = View.VISIBLE
                        val today = Calendar.getInstance()
                        val dialog = DatePickerDialog(requireContext(), { _, i1, i2, i3 ->
                            val pickedDate = Calendar.getInstance().apply {
                                set(Calendar.YEAR, i1)
                                set(Calendar.MONTH, i2)
                                set(Calendar.DAY_OF_MONTH, i3)
                            }
                            date = pickedDate.time
                            etDate.setText(SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")).format(pickedDate.time))
                        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
                        dialog.show()
                    }
                }
                updateRecyclerView(date)
            }
        }

        btnChooseDate.setOnClickListener {
            val today = Calendar.getInstance()
            val dialog = DatePickerDialog(requireContext(), { _, i, i2, i3 ->
                val pickedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, i)
                    set(Calendar.MONTH, i2)
                    set(Calendar.DAY_OF_MONTH, i3)
                }
                etDate.setText(SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID")).format(pickedDate.time))
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        btnAll.setOnClickListener {
            txtHeading.text = "All"
            filterRecyclerView("all")
        }

        btnNotPaidYet.setOnClickListener {
            txtHeading.text = "Not Paid Yet"
            filterRecyclerView("not_paid_yet")
        }

        btnPaid.setOnClickListener {
            txtHeading.text = "Paid"
            filterRecyclerView("paid")
        }

        btnWaitingForPickupShipping.setOnClickListener {
            txtHeading.text = "Waiting for Pickup / Shipping"
            filterRecyclerView("waiting_for_pickup_shipping")
        }

        btnPickedupShipped.setOnClickListener {
            txtHeading.text = "Picked Up / Shipped"
            filterRecyclerView("picked_up_shipped")
        }

        btnDone.setOnClickListener {
            txtHeading.text = "Done"
            filterRecyclerView("done")
        }

        btnCanceled.setOnClickListener {
            txtHeading.text = "Canceled"
            filterRecyclerView("canceled")
        }

        fabAdd.setOnClickListener {
            val dialog = AddEditSalesDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView(time: Date) {
        listSales.clear()
        listSales = db.getSalesByDay(time)
        filterRecyclerView("all")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterRecyclerView(selected: String) {
        btnAll.setBackgroundColor(requireContext().getColor(R.color.white))
        btnAll.setTextColor(requireContext().getColor(R.color.blue))
        btnNotPaidYet.setBackgroundColor(requireContext().getColor(R.color.white))
        btnNotPaidYet.setTextColor(requireContext().getColor(R.color.blue))
        btnPaid.setBackgroundColor(requireContext().getColor(R.color.white))
        btnPaid.setTextColor(requireContext().getColor(R.color.blue))
        btnWaitingForPickupShipping.setBackgroundColor(requireContext().getColor(R.color.white))
        btnWaitingForPickupShipping.setTextColor(requireContext().getColor(R.color.blue))
        btnPickedupShipped.setBackgroundColor(requireContext().getColor(R.color.white))
        btnPickedupShipped.setTextColor(requireContext().getColor(R.color.blue))
        btnDone.setBackgroundColor(requireContext().getColor(R.color.white))
        btnDone.setTextColor(requireContext().getColor(R.color.blue))
        btnCanceled.setBackgroundColor(requireContext().getColor(R.color.white))
        btnCanceled.setTextColor(requireContext().getColor(R.color.blue))

        when (selected) {
            "all" -> {
                btnAll.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnAll.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales)
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "not_paid_yet" -> {
                btnNotPaidYet.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnNotPaidYet.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.payment_status == "not_paid_yet" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "paid" -> {
                btnPaid.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnPaid.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.payment_status == "paid" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "waiting_for_pickup_shipping" -> {
                btnWaitingForPickupShipping.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnWaitingForPickupShipping.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.item_status == "waiting_for_pickup_shipping" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "picked_up_shipped" -> {
                btnPickedupShipped.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnPickedupShipped.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.item_status == "picked_up_shipped" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "done" -> {
                btnDone.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnDone.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.payment_status == "paid" && it.item_status == "picked_up_shipped" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
            "canceled" -> {
                btnCanceled.setBackgroundColor(requireContext().getColor(R.color.blue))
                btnCanceled.setTextColor(requireContext().getColor(R.color.white))
                filteredSales.clear()
                filteredSales.addAll(listSales.filter { it.payment_status == "canceled" && it.item_status == "canceled" })
                recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateRecyclerView(Date())
    }
}