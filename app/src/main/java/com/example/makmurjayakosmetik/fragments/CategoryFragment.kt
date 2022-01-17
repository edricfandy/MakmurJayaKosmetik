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
import com.example.makmurjayakosmetik.recyclerview.RVCategory
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Category
import com.example.makmurjayakosmetik.dialogs.AddEditCategoryModalBottomSheet
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoryFragment : Fragment() {
    private lateinit var db: DBHelper

    private lateinit var etSearch : EditText
    private lateinit var btnAddItem : MaterialButton
    private lateinit var txtMsgEmptyItem : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var hiddenLayout: LinearLayout
    private lateinit var fabAdd : FloatingActionButton
    private lateinit var modalBottomSheet: AddEditCategoryModalBottomSheet
    private var itemSelectedActionMode: ActionMode? = null

    private lateinit var listCategory: ArrayList<Category>
    private var filteredCategory : ArrayList<Category> = arrayListOf()

    private val manageCategoryBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context?, p1: Intent?) {
            if (p1?.action == "ADD_CATEGORY") {
                val category = p1.extras?.getParcelable<Category>("CATEGORY")
                if (category != null) {
                    db.insertCategory(category)
                    updateRecyclerView()
                }
            } else if (p1?.action == "EDIT_CATEGORY") {
                val oldCategoryName = p1.extras?.getString("OLD_CATEGORY_NAME")
                val category = p1.extras?.getParcelable<Category>("CATEGORY")
                if (oldCategoryName != null && category != null) {
                    db.updateCategory(oldCategoryName, category)
                    updateRecyclerView()
                }
            } else if (p1?.action == "DELETE_CATEGORY") {
                val category = p1.extras?.getParcelable<Category>("CATEGORY")
                if (category != null) {
                    db.deleteCategory(category.name)
                    updateRecyclerView()
                }
            } else if (p1?.action == "CATEGORY_SELECTED") {
                if (p1.extras?.get("SELECTED_STATE") as Boolean) {
                    if (itemSelectedActionMode == null)
                        itemSelectedActionMode = activity?.startActionMode(actionModeCallback)
                    (listCategory.find { it.name == p1.extras?.get("SELECTED_CATEGORY_NAME") as String } as Category).selected = true
                    val selected = listCategory.filter { it.selected }.size
                    itemSelectedActionMode?.title = "$selected selected"
                } else {
                    (listCategory.find { it.name == p1.extras?.get("SELECTED_CATEGORY_NAME") as String } as Category).selected = false
                    val selected = listCategory.filter { it.selected }.size
                    if (selected > 0)
                        itemSelectedActionMode?.title = "$selected selected"
                    else {
                        itemSelectedActionMode?.finish()
                        itemSelectedActionMode = null
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
            } else if (p1?.action == "OPEN_EDIT_CATEGORY") {
                val category = p1.extras?.getParcelable<Category>("CATEGORY")
                if (category != null) {
                    val bundle = Bundle()
                    bundle.putParcelable("CATEGORY", category)
                    modalBottomSheet = AddEditCategoryModalBottomSheet("edit")
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
                    listCategory.forEach { it.selected = true }
                    itemSelectedActionMode?.title = "${listCategory.size} selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_deselectAll -> {
                    listCategory.forEach { it.selected = false }
                    itemSelectedActionMode?.title = "0 selected"
                    recyclerView.adapter?.notifyDataSetChanged()
                    true
                }
                R.id.menuActionMode_delete -> {
                    val listCategoryToBeDeleted = listCategory.filter { it.selected }
                    val listTextCategory : ArrayList<String> = arrayListOf()
                    listCategoryToBeDeleted.forEach {
                        listTextCategory.add(it.name)
                    }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listTextCategory)

                    val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
                    val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation_category_many, null)

                    layoutConfirmation.apply {
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation1).text = getString(
                            R.string.delete_confirmation_message_many_arg, listCategoryToBeDeleted.size.toString(), "categories", "Category list")
                        findViewById<ListView>(R.id.dialogConfirmationOperationMany_listView).apply {
                            setAdapter(adapter)
                            divider = null
                        }
                        findViewById<TextView>(R.id.dialogConfirmationOperationMany_msgConfirmation2).text = getString(
                            R.string.delete_confirmation_message_many_arg_2, "categories")

                        findViewById<MaterialButton>(R.id.dialogConfirmationOperationMany_btnConfirm).setOnClickListener {
                            try {
                                db.writableDatabase.beginTransaction()
                                for (i in listCategoryToBeDeleted)
                                    db.deleteCategory(i.name)
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
            listCategory.forEach { it.selected = false }
            recyclerView.adapter = RVCategory(listCategory, requireActivity())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DBHelper(requireContext())
        val intentFilter = IntentFilter()

        listCategory = db.getAllCategoriesWithAmount()
        filteredCategory.addAll(listCategory)
        intentFilter.addAction("ADD_CATEGORY")
        intentFilter.addAction("EDIT_CATEGORY")
        intentFilter.addAction("DELETE_CATEGORY")
        intentFilter.addAction("CATEGORY_SELECTED")
        intentFilter.addAction("OPEN_EDIT_CATEGORY")

        requireContext().registerReceiver(manageCategoryBroadcastReceiver, intentFilter)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        etSearch = view.findViewById(R.id.manageCategory_etSearch)
        recyclerView = view.findViewById(R.id.manageCategory_recyclerView)
        hiddenLayout = view.findViewById(R.id.manageCategory_layoutEmptyItem)
        txtMsgEmptyItem = view.findViewById(R.id.manageCategory_msgEmptyItem)
        btnAddItem = view.findViewById(R.id.manageCategory_btnAddItem)
        fabAdd = view.findViewById(R.id.manageCategory_fabAdd)
        val txtNoItemMatches = view.findViewById<TextView>(R.id.manageCategory_txtNoItemMatches)

        if (listCategory.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Category")
            btnAddItem.text = getString(R.string.add_arg, "Category")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etSearch.text.isNotEmpty()) {
                    filteredCategory.clear()
                    filteredCategory.addAll(listCategory.filter { it.name.lowercase().contains(etSearch.text.toString().lowercase()) })
                    if (filteredCategory.isEmpty()) txtNoItemMatches.visibility = View.VISIBLE
                    else txtNoItemMatches.visibility = View.GONE
                    recyclerView.adapter?.notifyDataSetChanged()
                    return
                }

                filteredCategory.clear()
                filteredCategory.addAll(listCategory)
                txtNoItemMatches.visibility = View.GONE
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) { }
        })

        recyclerView.apply {
            adapter = RVCategory(filteredCategory, requireActivity())
            layoutManager = LinearLayoutManager(context)
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            addItemDecoration(divider)
        }

        btnAddItem.setOnClickListener {
            modalBottomSheet = AddEditCategoryModalBottomSheet("add")
            modalBottomSheet.show(requireActivity().supportFragmentManager, null)
        }

        fabAdd.setOnClickListener {
            modalBottomSheet = AddEditCategoryModalBottomSheet("add")
            modalBottomSheet.show(requireActivity().supportFragmentManager, null)
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        requireContext().unregisterReceiver(manageCategoryBroadcastReceiver)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecyclerView() {
        listCategory.clear()
        listCategory = db.getAllCategoriesWithAmount()
        filteredCategory.clear()
        filteredCategory.addAll(listCategory)
        if (listCategory.size <= 0) {
            hiddenLayout.visibility = View.VISIBLE
            txtMsgEmptyItem.text = getString(R.string.item_empty_message, "Category")
            btnAddItem.text = getString(R.string.add_arg, "Category")
            recyclerView.visibility = View.GONE
            fabAdd.visibility = View.GONE
        } else if (listCategory.size > 0 && hiddenLayout.visibility == View.VISIBLE) {
            hiddenLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            fabAdd.visibility = View.VISIBLE
        }

        recyclerView.adapter?.notifyDataSetChanged()
    }
}