package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Supplier
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AddEditSupplierDialog(private val mode: String) : DialogFragment() {
    private var supplier: Supplier? = null
    private lateinit var db: DBHelper

    private lateinit var etId : EditText
    private lateinit var etName : EditText
    private lateinit var etAddress : EditText
    private lateinit var etCity : EditText
    private lateinit var etPhoneNum : EditText
    private lateinit var etEmail : EditText
    private lateinit var cbAutoGenId : CheckBox

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.apply {
                setLayout(width, height)
                setWindowAnimations(R.style.FullScreenDialogAnimation)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)

        db = DBHelper(requireContext())
        supplier = db.getSupplierById(arguments?.getString("SUPPLIER_ID") ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_add_edit_supplier, container, false)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.dialogAddEditSupplier_toolbar)
        val header = view.findViewById<TextView>(R.id.dialogAddEditSupplier_txtHeader)
        etId = view.findViewById(R.id.dialogAddEditSupplier_etId)
        etName = view.findViewById(R.id.dialogAddEditSupplier_etName)
        etAddress = view.findViewById(R.id.dialogAddEditSupplier_etAddress)
        etCity = view.findViewById(R.id.dialogAddEditSupplier_etCity)
        etPhoneNum = view.findViewById(R.id.dialogAddEditSupplier_etPhoneNum)
        etEmail = view.findViewById(R.id.dialogAddEditSupplier_etEmail)
        cbAutoGenId = view.findViewById(R.id.dialogAddEditSupplier_cbAutoGenId)

        val btnConfirm = view.findViewById<MaterialButton>(R.id.dialogAddEditSupplier_btnConfirm)

        if (mode == "add") {
            header.text = getString(R.string.add_arg, "Supplier")
            etId.setText(db.generateSupplierId())
            etId.isEnabled = false
            cbAutoGenId.isChecked = true
        } else {
            header.text = getString(R.string.edit_arg, "Supplier")
            cbAutoGenId.apply {
                isChecked = false
                isEnabled = false
            }
            etId.setText(supplier?.id)
            etName.setText(supplier?.name)
            etAddress.setText(supplier?.address)
            etCity.setText(supplier?.city)
            etPhoneNum.setText(supplier?.phone_num)
            etEmail.setText(supplier?.email)
            btnConfirm.text = getString(R.string.edit)
        }

        cbAutoGenId.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etId.setText(db.generateSupplierId())
                etId.isEnabled = false
                return@setOnCheckedChangeListener
            }
            etId.isEnabled = true
        }

        toolbar.setNavigationOnClickListener {
            if ((!cbAutoGenId.isChecked && etId.text.isNotBlank()) || etName.text.isNotBlank() || etAddress.text.isNotBlank() || etCity.text.isNotBlank() || etPhoneNum.text.isNotBlank() || etEmail.text.isNotBlank()) {
                if (etId.text.toString() == supplier?.id && etName.text.toString() == supplier?.name && etAddress.text.toString() == supplier?.address && etCity.text.toString() == supplier?.city
                    && ((supplier?.phone_num?.isEmpty() == true && etPhoneNum.text.isEmpty()) || (supplier?.phone_num?.isNotEmpty() == true && supplier?.phone_num == "(+62)-${etPhoneNum.text}")) && etEmail.text.toString() == supplier?.email) {
                    dismiss()
                    return@setNavigationOnClickListener
                }

                val cancelingDialog = AlertDialog.Builder(context)
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
                return@setNavigationOnClickListener
            }
            dismiss()
        }

        btnConfirm.setOnClickListener {
            if (etId.text.isEmpty() || etName.text.isEmpty() || etCity.text.isEmpty()) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Data Incomplete")
                    .setMessage("Some required field is still empty. Please make sure every required field is filled.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                return@setOnClickListener
            }

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()

            if (mode == "add") {
                if (db.checkSupplierIsExisted(etId.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Supplier ID already exists")
                        .setMessage("Supplier ID already exists. Please use another ID.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                val phoneNum = if (etPhoneNum.text.isNotEmpty()) "(+62)-" + etPhoneNum.text.toString() else ""
                val newSupplier = Supplier(etId.text.toString(), etName.text.toString(), etAddress.text.toString(), etCity.text.toString(), phoneNum, etEmail.text.toString())
                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationSupplier_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation1).text = getString(
                        R.string.add_confirmation_message_arg, "Supplier")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtId).text = newSupplier.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtName).text = newSupplier.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtAddress).text = if (newSupplier.address.isEmpty()) "-" else newSupplier.address
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtCity).text = newSupplier.city
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtPhoneNum).text = if (newSupplier.phone_num.isEmpty()) "-" else newSupplier.phone_num
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtEmail).text = if (newSupplier.email.isEmpty()) "-" else newSupplier.email
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation3).text = getString(
                        R.string.add_confirmation_message_arg_2, "Supplier")
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationSupplier_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        db.insertSupplier(newSupplier)
                        val intent = Intent("MODIFIED")
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            } else if (mode == "edit") {
                if (etId.text.toString() == supplier?.id && etName.text.toString() == supplier?.name && etAddress.text.toString() == supplier?.address
                    && supplier?.phone_num == "(+62)-${etPhoneNum.text}" && etEmail.text.toString() == supplier?.email) {
                    dismiss()
                    return@setOnClickListener
                }

                if (etId.text.toString() != supplier?.id && db.checkSupplierIsExisted(etId.text.toString())) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Supplier ID already exists")
                        .setMessage("Supplier ID already exists. Please use another ID.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                val newSupplier = Supplier(etId.text.toString(), etName.text.toString(), etAddress.text.toString(), etCity.text.toString(), "(+62)-${etPhoneNum.text}", etEmail.text.toString())
                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationSupplier_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation1).text = getString(
                        R.string.edit_confirmation_message_arg, "Supplier")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtId).text = supplier?.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtName).text = supplier?.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtAddress).text = if (supplier?.address?.isEmpty() == true) "-" else supplier?.address
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtCity).text = supplier?.city
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtPhoneNum).text = if (supplier?.phone_num?.isEmpty() == true) "-" else supplier?.phone_num
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtEmail).text = if (supplier?.email?.isEmpty() == true) "-" else supplier?.email
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation2).text = getString(
                        R.string.edit_confirmation_message_arg_2, "Supplier")
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtId2).text = newSupplier.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtName2).text = newSupplier.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtAddress2).text = if (newSupplier.address.isEmpty()) "-" else newSupplier.address
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtCity2).text = newSupplier.city
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtPhoneNum2).text = if (newSupplier.phone_num.isEmpty()) "-" else newSupplier.phone_num
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_txtEmail2).text = if (newSupplier.email.isEmpty()) "-" else newSupplier.email
                    findViewById<TextView>(R.id.dialogConfirmationOperationSupplier_msgConfirmation3).text = getString(
                        R.string.edit_confirmation_message_arg_3, "Supplier")

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        db.updateSupplier(supplier?.id ?: "", newSupplier)
                        val intent = Intent("MODIFIED")
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

        return view
    }
}