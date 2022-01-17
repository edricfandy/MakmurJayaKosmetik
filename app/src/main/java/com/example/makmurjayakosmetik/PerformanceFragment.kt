package com.example.makmurjayakosmetik

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.makmurjayakosmetik.classes.Sales
import java.text.NumberFormat

class PerformanceFragment : Fragment() {
    private lateinit var db : DBHelper
    private lateinit var listSales : ArrayList<Sales>
    private lateinit var numberFormat : NumberFormat
    private lateinit var currencyFormat : NumberFormat

    private lateinit var txtTotalSales : TextView
    private lateinit var txtWholesales : TextView
    private lateinit var txtRetailSales : TextView
    private lateinit var txtTotalItemSold : TextView
    private lateinit var txtTotalRevenue : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        listSales = db.getAllSales()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_performance, container, false)

        txtTotalSales = view.findViewById(R.id.manageSales_txtTotalSales)
        txtWholesales = view.findViewById(R.id.manageSales_txtWholesale)
        txtRetailSales = view.findViewById(R.id.manageSales_txtRetail)
        txtTotalItemSold = view.findViewById(R.id.manageSales_txtItemSold)
        txtTotalRevenue = view.findViewById(R.id.manageSales_txtTotalRevenue)

        updateTransactionsInfo()

        return view
    }

    private fun updateTransactionsInfo() {
        var totalItem = 0
        var totalRevenue = 0

        txtTotalSales.text = listSales.size.toString()
        txtWholesales.text = listSales.count { it.type == "wholesale" }.toString()
        txtRetailSales.text = listSales.count { it.type == "retail" }.toString()
        listSales.forEach {
            totalItem += it.total_item
            totalRevenue += it.total_purchase
        }
        txtTotalItemSold.text = numberFormat.format(totalItem)
        txtTotalRevenue.text = currencyFormat.format(totalRevenue)
    }
}