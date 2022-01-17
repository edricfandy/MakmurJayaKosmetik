package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class AddEditCategoryModalBottomSheet(private val mode: String) : BottomSheetDialogFragment() {
    private var category: Category? = null
    private lateinit var db: DBHelper

    private lateinit var etName : EditText
    private lateinit var etDesc : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_add_edit_category, container, false)

        db = DBHelper(requireContext())
        category = arguments?.getParcelable("CATEGORY")

        val header = view.findViewById<TextView>(R.id.dialogAddEditCategory_txtHeader)
        etName = view.findViewById(R.id.dialogAddEditCategory_etName)
        etDesc = view.findViewById(R.id.dialogAddEditCategory_etDesc)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.dialogAddEditCategory_btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.dialogAddEditCategory_btnCancel)

        if (mode == "add")
            header.text = getString(R.string.add_arg, "Category")
        else {
            header.text = getString(R.string.edit_arg, "Category")
            etName.setText(category?.name)
            etDesc.setText(category?.desc)
            btnConfirm.text = getString(R.string.edit)
        }

        btnConfirm.setOnClickListener {
            if (etName.text.isEmpty()) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Data Incomplete")
                    .setMessage("Some field is still empty. Please make sure every field is filled with the data.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                etName.setError("Please provide the category name.")
                return@setOnClickListener
            }

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()

            if (mode == "add") {
                if (db.checkCategoryIsExisted(etName.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Category Name already exists")
                        .setMessage("The Category Name already used for other category. Please use other Name.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                val newCategory = Category(etName.text.toString(), etDesc.text.toString())
                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationCategory_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation1).text = getString(
                        R.string.add_confirmation_message_arg, "Category")
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtName).text = newCategory.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtDesc).text = if (newCategory.desc.isEmpty()) "-" else newCategory.desc
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation3).text = getString(
                        R.string.add_confirmation_message_arg_2, "Category")
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationCategory_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val intent = Intent("ADD_CATEGORY")
                        intent.putExtra("CATEGORY", newCategory)
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            } else if (mode == "edit") {
                if (etName.text.toString() == category?.name && etDesc.text.toString() == category?.desc) {
                    dismiss()
                    return@setOnClickListener
                }

                if (db.checkCategoryIsExisted(etName.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Category Name already exists")
                        .setMessage("The Category name already used for other category. Please use other name.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                val newCategory = Category(etName.text.toString(), etDesc.text.toString())
                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationCategory_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation1).text = getString(
                        R.string.edit_confirmation_message_arg, "Category")
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtName).text = category?.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtDesc).text = if (category?.desc?.isEmpty() == true) "-" else category?.desc
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation2).text = getString(
                        R.string.edit_confirmation_message_arg_2, "Category")
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtName2).text = newCategory.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtDesc2).text = if (newCategory.desc.isEmpty()) "-" else newCategory.desc
                    findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation3).text = getString(
                        R.string.edit_confirmation_message_arg_3, "Category")

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val intent = Intent("EDIT_CATEGORY")
                        intent.putExtra("OLD_CATEGORY_NAME", category?.name)
                        intent.putExtra("CATEGORY", newCategory)
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            }

            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }

        btnCancel.setOnClickListener {
            if (etName.text.isNotBlank() || etDesc.text.isNotBlank()) {
                if (etName.text.toString() == category?.name && etDesc.text.toString() == category?.desc) {
                    dismiss()
                    return@setOnClickListener
                }

                val cancelingDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Discard data")
                    .setMessage("Are you sure want to discard all the data inserted?")
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton("CONFIRM") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        dismiss()
                    }
                    .setNegativeButton("NO") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }

                cancelingDialog.show()
                return@setOnClickListener
            }
            dismiss()
        }

        return view
    }
}