package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Category
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class CategoryInfoModalBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_category_info, container, false)

        val category = arguments?.getParcelable<Category>("CATEGORY")

        val txtName = view.findViewById<TextView>(R.id.categoryInfo_txtName)
        val txtDesc = view.findViewById<TextView>(R.id.categoryInfo_txtDesc)
        val txtTotalProduct = view.findViewById<TextView>(R.id.categoryInfo_txtTotalProduct)
        val btnEdit = view.findViewById<MaterialButton>(R.id.categoryInfo_btnEdit)
        val btnDelete = view.findViewById<MaterialButton>(R.id.categoryInfo_btnDelete)

        txtName.text = category?.name
        txtDesc.text = if (category?.desc?.isEmpty() == true) "No Description" else category?.desc
        txtTotalProduct.text = category?.totalProduct.toString()

        btnEdit.setOnClickListener {
            val intent = Intent("OPEN_EDIT_CATEGORY")
            intent.putExtra("CATEGORY", category)
            requireContext().sendBroadcast(intent)
            dismiss()
        }

        btnDelete.setOnClickListener {
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)

            layoutConfirmation.apply {
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationCategory_layout).visibility = View.VISIBLE
                findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation1).text = getString(
                    R.string.delete_confirmation_message_arg, "Category")
                findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtName).text = category?.name
                findViewById<TextView>(R.id.dialogConfirmationOperationCategory_txtDesc).text = if (category?.desc?.isEmpty() == true) "-" else category?.desc
                findViewById<TextView>(R.id.dialogConfirmationOperationCategory_msgConfirmation3).text = getString(
                    R.string.delete_confirmation_message_arg_2, "Category")
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationCategory_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                    val intent = Intent("DELETE_CATEGORY")
                    intent.putExtra("CATEGORY", category)
                    context.sendBroadcast(intent)
                    dialogConfirmation.dismiss()
                    dismiss()
                }

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                    dialogConfirmation.dismiss()
                }
            }

            dialogConfirmation.setView(layoutConfirmation)
            dialogConfirmation.show()
        }

        return view
    }
}