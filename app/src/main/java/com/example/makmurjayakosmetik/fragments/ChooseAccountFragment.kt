package com.example.makmurjayakosmetik.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Account
import com.example.makmurjayakosmetik.recyclerview.RVAccount

class ChooseAccountFragment : Fragment() {
    private lateinit var db: DBHelper
    private lateinit var accounts: ArrayList<Account>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        accounts = db.getAllAccount()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_choose_account, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.chooseAccount_rvAccount)
        recyclerView.apply {
            adapter = RVAccount(accounts)
            layoutManager = LinearLayoutManager(requireContext())
        }

        return view
    }
}