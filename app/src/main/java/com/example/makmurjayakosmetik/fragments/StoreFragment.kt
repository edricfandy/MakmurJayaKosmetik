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
import com.example.makmurjayakosmetik.classes.Store
import com.example.makmurjayakosmetik.dialogs.AddEditStoreModalBottomSheet
import com.example.makmurjayakosmetik.recyclerview.RVStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StoreFragment : Fragment() {
    private lateinit var db: DBHelper

    private lateinit var etSearch : EditText
    private lateinit var btnAddItem : MaterialButton
    private lateinit var txtMsgEmptyItem : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton
    private lateinit var modalBottomSheet: AddEditStoreModalBottomSheet
    private var itemSelectedActionMode: ActionMode? = null

    private lateinit var listStore: ArrayList<Store>
    private var filteredStore: ArrayList<Store> = arrayListOf()

    private val manageStoreBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "ADD_STORE") {
                val store = p1.extras?.getParcelable<Store>("STORE")
                if (store != null) {
                    db.insertStore(store)
                    updateRecyclerView()
                }
            } else if (p1?.action == "EDIT_STORE") {
                val oldStore = p1.extras?.getParcelable<Store>("OLD_STORE")
                val newStore = p1.extras?.getParcelable<Store>("NEW_STORE")
                if (oldStore != null && newStore != null) {
                    db.updateStore(oldStore, newStore)
                    updateRecyclerView()
                }
            } else if (p1?.action == "DELETE_STORE") {
                val store = p1.extras?.getParcelable<Store>("STORE")
                if (store != null) {
                    db.deleteStore(store)
                    updateRecyclerView()
                }
            } else if (p1?.action == "STORE_SELECTED") {
                val store = p1.extras?.getParcelable<Store>("SELECTED_STORE")
                if (p1.extras?.getBoolean("SELECTED_STATE") == true) {
                    if (itemSelectedActionMode == null)
                        itemSelectedActionMode = activity?.startActionMode(actionModeCallback)
                    listStore.find { it.name == store?.name && it.id == store.id && it.platform == store.platform }?.selected = true
                    val selected = listStore.filter { it.selected }.size
                    itemSelectedActionMode?.title = "$selected selected"
                } else {
                    listStore.find { it.name == store?.name && it.id == store.id && it.platform == store.platform }?.selected = false
                    val selected = listStore.filter { it.selected }.size
                    if (selected > 0)
                        itemSelectedActionMode?.title = "$selected selected"
                    else {
                        itemSelectedActionMode?.finish()
                        itemSelectedActionMode = null
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            } else if (p1?.action == "OPEN_EDIT_STORE") {
                val store = p1.extras?.getParcelable<Store>("STORE")
                if (store != null) {
                    val bundle = Bundle()
                    bundle.putParcelable("STORE", store)
                    modalBottomSheet = AddEditStoreModalBottomSheet("edit")
                    modalBottomSheet.arguments = bundle
                    modalBottomSheet.show(requireActivity().supportFragmentManager, null)
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
                    listStore.forEach { it.selected = true }
                    itemSelectedActionMode?.title = "${listStore.size} selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_deselectAll -> {
                    listStore.forEach { it.selected = false }
                    itemSelectedActionMode?.title = "0 selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_delete -> {
                    val listStoreToBeDeleted = listStore.filter { it.selected }
                    val listTextStore : ArrayList<String> = arrayListOf()
                    listStoreToBeDeleted.forEach {
                        listTextStore.add("${it.name} (${it.platform})")
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listTextStore)

                    val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
                    val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_category_many, null)

                    layoutConfirmation.apply {
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation1).text = getString(
                            R.string.delete_confirmation_message_many_arg, listStoreToBeDeleted.size.toString(), "store(s)", "Store list")
                        findViewById<ListView>(R.id.dialogConfirmationOperationMany_listView).apply {
                            setAdapter(adapter)
                            divider = null
                        }
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation2).text = getString(
                            R.string.delete_confirmation_message_many_arg_2, "store(s)")

                        findViewById<MaterialButton>(R.id.dialogConfirmationOperationMany_btnConfirm).setOnClickListener {
                            try {
                                db.writableDatabase.beginTransaction()
                                for (i in listStoreToBeDeleted)
                                    db.deleteStore(i)
                                db.writableDatabase.setTransactionSuccessful()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                db.writableDatabase.endTransaction()
                            }

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
            listStore.forEach { it.selected = false }
            recyclerView.adapter = RVStore(listStore, requireActivity())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DBHelper(requireContext())
        val intentFilter = IntentFilter()

        listStore = db.getAllStore()
        filteredStore.addAll(listStore)
        intentFilter.addAction("ADD_STORE")
        intentFilter.addAction("EDIT_STORE")
        intentFilter.addAction("DELETE_STORE")
        intentFilter.addAction("STORE_SELECTED")
        intentFilter.addAction("OPEN_EDIT_STORE")

        requireContext().registerReceiver(manageStoreBroadcastReceiver, intentFilter)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        etSearch = view.findViewById(R.id.manageStore_etSearch)
        recyclerView = view.findViewById(R.id.manageStore_recyclerView)
        hiddenLayout = view.findViewById(R.id.manageStore_layoutEmptyItem)
        txtMsgEmptyItem = view.findViewById(R.id.manageStore_msgEmptyItem)
        btnAddItem = view.findViewById(R.id.manageStore_btnAddItem)
        fabAdd = view.findViewById(R.id.manageStore_fabAdd)
        val txtNoItemMatches = view.findViewById<TextView>(R.id.manageStore_txtNoItemMatches)

        if (listStore.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Store")
            btnAddItem.text = getString(R.string.add_arg, "Store")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etSearch.text.isNotEmpty()) {
                    filteredStore.clear()
                    filteredStore.addAll(listStore.filter { it.name.lowercase().contains(etSearch.text.toString().lowercase()) || it.id.lowercase().contains(etSearch.text.toString().lowercase()) })
                    if (filteredStore.isEmpty()) txtNoItemMatches.visibility = View.VISIBLE
                    else txtNoItemMatches.visibility = View.GONE
                    recyclerView.adapter?.notifyDataSetChanged()
                    return
                }

                filteredStore.clear()
                filteredStore.addAll(listStore)
                txtNoItemMatches.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

        recyclerView.apply {
            adapter = RVStore(filteredStore, requireActivity())
            layoutManager = LinearLayoutManager(context)
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            addItemDecoration(divider)
        }

        btnAddItem.setOnClickListener {
            modalBottomSheet = AddEditStoreModalBottomSheet("add")
            modalBottomSheet.show(childFragmentManager.beginTransaction(), null)
        }

        fabAdd.setOnClickListener {
            modalBottomSheet = AddEditStoreModalBottomSheet("add")
            modalBottomSheet.show(childFragmentManager.beginTransaction(), null)
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(manageStoreBroadcastReceiver)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        listStore.clear()
        listStore.addAll(db.getAllStore())
        filteredStore.clear()
        filteredStore.addAll(listStore)
        if (listStore.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Store")
            btnAddItem.text = getString(R.string.add_arg, "Store")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        } else if (listStore.size > 0 && hiddenLayout.visibility == View.VISIBLE) {
            hiddenLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAdd.visibility = View.VISIBLE
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }
}