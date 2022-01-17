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
import com.example.makmurjayakosmetik.classes.Supplier
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class SupplierInfoModalBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_supplier_info, container, false)

        val supplier = arguments?.getParcelable<Supplier>("SUPPLIER")

        val txtId = view.findViewById<TextView>(R.id.supplierInfo_txtId)
        val txtName = view.findViewById<TextView>(R.id.supplierInfo_txtName)
        val txtAddress = view.findViewById<TextView>(R.id.supplierInfo_txtAddress)
        val txtPhoneNum = view.findViewById<TextView>(R.id.supplierInfo_txtPhoneNum)
        val txtEmail = view.findViewById<TextView>(R.id.supplierInfo_txtEmail)
        val btnEdit = view.findViewById<MaterialButton>(R.id.supplierInfo_btnEdit)
        val btnDelete = view.findViewById<MaterialButton>(R.id.supplierInfo_btnDelete)

        txtId.text = supplier?.id
        txtName.text = supplier?.name
        txtAddress.text = if (supplier?.address?.isEmpty() == true) supplier.city else getString(R.string.address_and_city, supplier?.address, supplier?.city)
        txtPhoneNum.text = if (supplier?.phone_num?.isEmpty() == true) "Phone number not set" else supplier?.phone_num
        txtEmail.text = if (supplier?.email?.isEmpty() == true) "E-mail address not set" else supplier?.email

        btnEdit.setOnClickListener {
            val intent = Intent("OPEN_EDIT_SUPPLIER")
            intent.putExtra("SUPPLIER_ID", supplier?.id)
            requireContext().sendBroadcast(intent)
            dismiss()
        }

        btnDelete.setOnClickListener {
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)

            layoutConfirmation.apply {
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationSupplier_layout).visibility = View.VISIBLE
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation1).text = getString(
                    R.string.delete_confirmation_message_arg, "Supplier")
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtId).text = supplier?.id
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtName).text = supplier?.name
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtAddress).text = supplier?.address
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtCity).text = supplier?.city
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtPhoneNum).text = supplier?.phone_num
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtEmail).text = supplier?.email
                findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation3).text = getString(
                    R.string.delete_confirmation_message_arg_2, "Supplier")
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationSupplier_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                    val intent = Intent("DELETE_SUPPLIER")
                    intent.putExtra("SUPPLIER_ID", supplier?.id)
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