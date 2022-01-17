package com.example.makmurjayakosmetik.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Supplier
import com.example.makmurjayakosmetik.dialogs.AddEditSupplierDialog
import com.example.makmurjayakosmetik.recyclerview.RVSupplier
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SupplierFragment : Fragment() {
    private lateinit var db: DBHelper

    private lateinit var etSearch : EditText
    private lateinit var btnAddItem : MaterialButton
    private lateinit var txtMsgEmptyItem : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton
    private lateinit var dialog: AddEditSupplierDialog
    private var itemSelectedActionMode: ActionMode? = null

    private lateinit var listSupplier: ArrayList<Supplier>
    private var filteredSupplier : ArrayList<Supplier> = arrayListOf()

    private val manageSupplierBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "MODIFIED")
                updateRecyclerView()
            else if (p1?.action == "DELETE_SUPPLIER") {
                val supplierId = p1.extras?.getString("SUPPLIER_ID")
                if (supplierId != null) {
                    db.deleteSupplier(supplierId)
                    updateRecyclerView()
                }
            } else if (p1?.action == "SUPPLIER_SELECTED") {
                val supplierId = p1.extras?.getString("SELECTED_SUPPLIER_ID")
                if (p1.extras?.getBoolean("SELECTED_STATE") == true) {
                    if (itemSelectedActionMode == null)
                        itemSelectedActionMode = activity?.startActionMode(actionModeCallback)
                    listSupplier.find { it.id == supplierId }?.selected = true
                    val selected = listSupplier.filter { it.selected }.size
                    itemSelectedActionMode?.title = "$selected selected"
                } else {
                    listSupplier.find { it.id == supplierId }?.selected = false
                    val selected = listSupplier.filter { it.selected }.size
                    if (selected > 0)
                        itemSelectedActionMode?.title = "$selected selected"
                    else {
                        itemSelectedActionMode?.finish()
                        itemSelectedActionMode = null
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            } else if (p1?.action == "OPEN_EDIT_SUPPLIER") {
                val id = p1.extras?.getString("SUPPLIER_ID")
                if (id != null) {
                    val bundle = Bundle()
                    bundle.putString("SUPPLIER_ID", id)
                    dialog = AddEditSupplierDialog("edit")
                    dialog.arguments = bundle
                    dialog.show(requireActivity().supportFragmentManager, null)
                }

            }
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            activity?.menuInflater?.inflate(R.menu.menu_action_mode, p1)
            return true
        }

        override fun onPrepareActionMode(p0: ActionMode?, p1: Menu?): Boolean {
            return false
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onActionItemClicked(p0: ActionMode?, p1: MenuItem?): Boolean {
            etSearch.text.clear()
            return when(p1?.itemId) {
                R.id.menuActionMode_selectAll -> {
                    listSupplier.forEach { it.selected = true }
                    itemSelectedActionMode?.title = "${listSupplier.size} selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_deselectAll -> {
                    listSupplier.forEach { it.selected = false }
                    itemSelectedActionMode?.title = "0 selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_delete -> {
                    val listSupplierToBeDeleted = listSupplier.filter { it.selected }
                    val listTextSupplier : ArrayList<String> = arrayListOf()
                    listSupplierToBeDeleted.forEach {
                        listTextSupplier.add("${it.name} (${it.city})")
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listTextSupplier)

                    val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
                    val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_category_many, null)

                    layoutConfirmation.apply {
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation1).text = getString(
                            R.string.delete_confirmation_message_many_arg, listSupplierToBeDeleted.size.toString(), "supplier(s)", "Supplier list")
                        findViewById<ListView>(R.id.dialogConfirmationOperationMany_listView).apply {
                            setAdapter(adapter)
                            divider = null
                        }
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation2).text = getString(
                            R.string.delete_confirmation_message_many_arg_2, "supplier(s)")

                        findViewById<MaterialButton>(R.id.dialogConfirmationOperationMany_btnConfirm).setOnClickListener {
                            try {
                                db.writableDatabase.beginTransaction()
                                for (i in listSupplierToBeDeleted)
                                    db.deleteSupplier(i.id)
                                db.writableDatabase.setTransactionSuccessful()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                db.writableDatabase.endTransaction()
                            }
                            updateRecyclerView()
                            dialogConfirmation.dismiss()
                        }

                        findViewById<MaterialButton>(R.id.dialogConfirmationOperationMany_btnCancel).setOnClickListener {
                            dialogConfirmation.dismiss()
                        }
                    }

                    dialogConfirmation.setView(layoutConfirmation)
                    dialogConfirmation.show()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(p0: ActionMode?) {
            itemSelectedActionMode = null
            listSupplier.forEach { it.selected = false }
            recyclerView.adapter = RVSupplier(listSupplier, requireActivity())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DBHelper(requireContext())

        listSupplier = db.getAllSuppliers()
        filteredSupplier.addAll(listSupplier)
        val intentFilter = IntentFilter()
        intentFilter.addAction("MODIFIED")
        intentFilter.addAction("DELETE_SUPPLIER")
        intentFilter.addAction("SUPPLIER_SELECTED")
        intentFilter.addAction("OPEN_EDIT_SUPPLIER")
        requireContext().registerReceiver(manageSupplierBroadcastReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_supplier, container, false)

        etSearch = view.findViewById(R.id.manageSupplier_etSearch)
        txtMsgEmptyItem = view.findViewById(R.id.manageSupplier_msgEmptyItem)
        btnAddItem = view.findViewById(R.id.manageSupplier_btnAddItem)
        recyclerView = view.findViewById(R.id.manageSupplier_recyclerView)
        hiddenLayout = view.findViewById(R.id.manageSupplier_layoutEmptyItem)
        fabAdd = view.findViewById(R.id.manageSupplier_fabAdd)
        val txtNoItemMatches = view.findViewById<TextView>(R.id.manageSupplier_txtNoItemMatches)

        if (listSupplier.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Supplier")
            btnAddItem.text = getString(R.string.add_arg, "Supplier")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etSearch.text.isNotEmpty()) {
                    filteredSupplier.clear()
                    filteredSupplier.addAll(listSupplier.filter { it.name.lowercase().contains(etSearch.text.toString().lowercase()) || it.id.lowercase().contains(etSearch.text.toString().lowercase()) })
                    if (filteredSupplier.isEmpty()) txtNoItemMatches.visibility = View.VISIBLE
                    else txtNoItemMatches.visibility = View.GONE
                    recyclerView.adapter?.notifyDataSetChanged()
                    return
                }

                filteredSupplier.clear()
                filteredSupplier.addAll(listSupplier)
                txtNoItemMatches.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

        recyclerView.apply {
            adapter = RVSupplier(filteredSupplier, requireActivity())
            layoutManager = LinearLayoutManager(context)
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            addItemDecoration(divider)
        }

        btnAddItem.setOnClickListener {
            val dialog = AddEditSupplierDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        fabAdd.setOnClickListener {
            val dialog = AddEditSupplierDialog("add")
            dialog.show(childFragmentManager.beginTransaction(), null)
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(manageSupplierBroadcastReceiver)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        listSupplier.clear()
        listSupplier.addAll(db.getAllSuppliers())
        filteredSupplier.clear()
        filteredSupplier.addAll(listSupplier)
        if (listSupplier.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Supplier")
            btnAddItem.text = getString(R.string.add_arg, "Supplier")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        } else if (listSupplier.size > 0 && hiddenLayout.visibility == View.VISIBLE) {
            hiddenLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAdd.visibility = View.VISIBLE
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }
}