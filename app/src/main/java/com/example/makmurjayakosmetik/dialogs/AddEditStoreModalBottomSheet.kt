package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.classes.Store
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

class AddEditStoreModalBottomSheet(private val mode: String) : BottomSheetDialogFragment() {
    private var store: Store? = null
    private lateinit var db: DBHelper

    private lateinit var ddPlatform : AutoCompleteTextView
    private lateinit var etPlatform : EditText
    private lateinit var etName : EditText
    private lateinit var etId : EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.bottom_sheet_add_edit_store, container, false)

        db = DBHelper(requireContext())
        store = arguments?.getParcelable("STORE")

        val header = view.findViewById<TextView>(R.id.dialogAddEditStore_txtHeader)
        ddPlatform = view.findViewById(R.id.dialogAddEditStore_ddPlatform)
        etPlatform = view.findViewById(R.id.dialogAddEditStore_etPlatform)
        etName = view.findViewById(R.id.dialogAddEditStore_etName)
        etId = view.findViewById(R.id.dialogAddEditStore_etId)
        val layoutInputPlatform = view.findViewById<TextInputLayout>(R.id.dialogAddEditStore_inputLayoutPlatform)
        val layoutInputId = view.findViewById<TextInputLayout>(R.id.dialogAddEditStore_inputLayoutId)
        val arrayPlatform = arrayOf("Offline Store", "Facebook", "Instagram", "WhatsApp", "Shopee", "Tokopedia", "Other")
        val btnConfirm = view.findViewById<MaterialButton>(R.id.dialogAddEditStore_btnConfirm)
        val btnCancel = view.findViewById<MaterialButton>(R.id.dialogAddEditStore_btnCancel)

        if (mode == "add") {
            header.text = getString(R.string.add_arg, "Store")
            ddPlatform.setText(getString(R.string.choose_one))
        } else {
            header.text = getString(R.string.edit_arg, "Store")
            if (arrayPlatform.contains(store?.platform)) {
                ddPlatform.setText(store?.platform)
                if (store?.platform == "Offline Store") {
                    layoutInputId.hint = getString(R.string.address)
                    layoutInputId.startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location)
                }
            } else {
                ddPlatform.setText(getString(R.string.other))
                layoutInputPlatform.visibility = View.VISIBLE
                etPlatform.setText(store?.platform)
            }
            etName.setText(store?.name)
            etId.setText(store?.id)
            btnConfirm.text = getString(R.string.edit)
        }

        ddPlatform.apply {
            setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayPlatform))
            setOnItemClickListener { parent, _, position, _ ->
                when (parent.getItemAtPosition(position).toString()) {
                    "Offline Store" -> {
                        layoutInputPlatform.visibility = View.GONE
                        layoutInputId.hint = getString(R.string.address)
                        layoutInputId.startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location)
                    }
                    "Other" -> {
                        layoutInputPlatform.visibility = View.VISIBLE
                        layoutInputId.hint = getString(R.string.store_id)
                        layoutInputId.startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_barcode)
                    }
                    else -> {
                        layoutInputPlatform.visibility = View.GONE
                        layoutInputId.hint = getString(R.string.store_id)
                        layoutInputId.startIconDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_barcode)
                    }
                }
            }
        }

        btnConfirm.setOnClickListener {
            if (etName.text.isEmpty() || ddPlatform.text.toString() == "Choose One" || (ddPlatform.text.toString() == "Other" && etPlatform.text.isEmpty())) {
                val dialogDeny = AlertDialog.Builder(requireContext())
                    .setTitle("Data Incomplete")
                    .setMessage("Some field is still empty. Please make sure every required field is filled.")
                    .setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()

                dialogDeny.show()
                if (ddPlatform.text.toString() == "Choose One") etPlatform.error = "Please choose the store's platform."
                if (ddPlatform.text.toString() == "Other" && etPlatform.text.isEmpty()) etPlatform.error = "Please provide platform name."
                if (etName.text.isEmpty()) etName.error = "Please provide the store name."
                return@setOnClickListener
            }

            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()

            if (mode == "add") {
                val newStore = Store(
                    etName.text.toString(),
                    if (etId.text.isEmpty()) "" else etId.text.toString(),
                    if (ddPlatform.text.toString() == "Other") etPlatform.text.toString() else ddPlatform.text.toString()
                )
                if (db.checkStoreIsExisted(newStore)) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Store already exists")
                        .setMessage("The store record already exists in database. Action cannot be done.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationStore_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation1).text = getString(
                        R.string.add_confirmation_message_arg, "Store")
                    if (newStore.platform == "Offline Store") findViewById<TextView>(R.id.dialogConfirmationOperationStore_lblId).text = getString(R.string.address)
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtPlatform).text = newStore.platform
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtName).text = newStore.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtId).text = if (newStore.id.isEmpty()) "-" else newStore.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation3).text = getString(
                        R.string.add_confirmation_message_arg_2, "Store")
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationStore_layoutAdditional).visibility = View.GONE

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val intent = Intent("ADD_STORE")
                        intent.putExtra("STORE", newStore)
                        requireContext().sendBroadcast(intent)
                        dialogConfirmation.dismiss()
                        dismiss()
                    }

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnCancel).setOnClickListener {
                        dialogConfirmation.dismiss()
                    }
                }
            } else if (mode == "edit") {
                if (etName.text.toString() == store?.name && etId.text.toString() == store?.id && (ddPlatform.text.toString() == store?.platform || etPlatform.text.toString() == store?.platform)) {
                    dismiss()
                    return@setOnClickListener
                }

                val newStore = Store(
                    etName.text.toString(),
                    if (etId.text.isEmpty()) "" else etId.text.toString(),
                    if (ddPlatform.text.toString() == "Other") etPlatform.text.toString() else ddPlatform.text.toString()
                )
                if (db.checkStoreIsExisted(newStore)) {
                    val dialogUnavailable = AlertDialog.Builder(requireContext())
                        .setTitle("Store already exists")
                        .setMessage("The store record already exists in database. Action cannot be done.")
                        .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                            dialogInterface.dismiss()
                        }
                        .create()

                    dialogUnavailable.show()
                    return@setOnClickListener
                }

                layoutConfirmation.apply {
                    findViewById<LinearLayout>(R.id.dialogConfirmationOperationStore_layout).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation1).text = getString(
                        R.string.edit_confirmation_message_arg, "Store")
                    if (store?.platform == "Offline Store")
                        findViewById<TextView>(R.id.dialogConfirmationOperationStore_lblId).text = getString(R.string.address)
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtPlatform).text = store?.platform
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtName).text = store?.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtId).text = if (store?.id?.isEmpty() == true) "-" else store?.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation2).text = getString(
                        R.string.edit_confirmation_message_arg_2, "Store")
                    if (newStore.platform == "Offline Store")
                        findViewById<TextView>(R.id.dialogConfirmationOperationStore_lblId2).text = getString(R.string.address)
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtPlatform2).text = newStore.platform
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtName2).text = newStore.name
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtId2).text = if (newStore.id.isEmpty()) "-" else newStore.id
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation3).text = getString(
                        R.string.edit_confirmation_message_arg_3, "Store")

                    findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                        val intent = Intent("EDIT_STORE")
                        intent.putExtra("OLD_STORE", store)
                        intent.putExtra("NEW_STORE", newStore)
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
            if (etName.text.isNotEmpty() || etId.text.isNotEmpty() || ddPlatform.text.toString() != "Choose One" || (ddPlatform.text.toString() == "Other" && etPlatform.text.isNotEmpty())) {
                if (etName.text.toString() == store?.name && etId.text.toString() == store?.id && (ddPlatform.text.toString() == store?.platform || etPlatform.text.toString() == store?.platform)) {
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