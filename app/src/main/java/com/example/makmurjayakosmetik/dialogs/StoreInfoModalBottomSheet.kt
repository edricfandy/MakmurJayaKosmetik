package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.example.makmurjayakosmetik.DBEntity
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Store
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class StoreInfoModalBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_store_info, container, false)

        val db = DBHelper(requireContext())
        val store = arguments?.getParcelable<Store>("STORE")

        val txtPlatform = view.findViewById<TextView>(R.id.storeInfo_txtPlatform)
        val txtName = view.findViewById<TextView>(R.id.storeInfo_txtName)
        val txtId = view.findViewById<TextView>(R.id.storeInfo_txtId)
        val txtAddedOn = view.findViewById<TextView>(R.id.storeInfo_txtAddedOn)
        val txtLastEditedOn = view.findViewById<TextView>(R.id.storeInfo_txtLastEditedOn)
        val btnEdit = view.findViewById<MaterialButton>(R.id.storeInfo_btnEdit)
        val btnDelete = view.findViewById<MaterialButton>(R.id.storeInfo_btnDelete)

        txtPlatform.apply {
            text = store?.platform
            when (store?.platform) {
                "Offline Store" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_store), null, null, null)
                "Facebook" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_facebook), null, null, null)
                "Instagram" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_instagram), null, null, null)
                "WhatsApp" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_whatsapp), null, null, null)
                "Shopee" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_shopee), null, null, null)
                "Tokopedia" -> setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.mipmap.ic_tokopedia), null, null, null)
            }
        }
        txtName.text = store?.name
        when {
            store?.id?.isEmpty() == true -> txtId.text = if (store.platform == "Offline Store") "No Address added" else "No Store ID / Username added"
            store?.platform == "Offline Store" -> {
                txtId.text = store.id
                txtId.setCompoundDrawablesRelativeWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_location), null, null, null)
            }
            else -> txtId.text = getString(R.string.store_id_with_placeholder, store?.id)
        }
        db.getAllLog()

        val addLog = db.getLog(DBEntity.TableStore.TABLE_NAME, "insert", "${store?.name};${store?.id};${store?.platform}")
        txtAddedOn.text = addLog
        txtLastEditedOn.apply {
            val log = db.getLog(DBEntity.TableStore.TABLE_NAME, "update", "${store?.name};${store?.id};${store?.platform}")
            if (log.isEmpty()) visibility = View.GONE
            else text = log
        }

        btnEdit.setOnClickListener {
            val intent = Intent("OPEN_EDIT_STORE")
            intent.putExtra("STORE", store)
            requireContext().sendBroadcast(intent)
            dismiss()
        }

        btnDelete.setOnClickListener {
            val dialogConfirmation = AlertDialog.Builder(requireContext()).create()
            val layoutConfirmation = layoutInflater.inflate(R.layout.dialog_confirmation_operation, null)

            layoutConfirmation.apply {
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationStore_layout).visibility = View.VISIBLE
                findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation1).text = getString(
                    R.string.delete_confirmation_message_arg, "Store")
                if (store?.platform == "Offline Store")
                    findViewById<TextView>(R.id.dialogConfirmationOperationStore_lblId).text = getString(R.string.address)
                findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtPlatform).text = store?.platform
                findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtName).text = store?.name
                findViewById<TextView>(R.id.dialogConfirmationOperationStore_txtId).text = if (store?.id?.isEmpty() == true) "-" else store?.id
                findViewById<TextView>(R.id.dialogConfirmationOperationStore_msgConfirmation3).text = getString(
                    R.string.delete_confirmation_message_arg_2, "Store")
                findViewById<LinearLayout>(R.id.dialogConfirmationOperationStore_layoutAdditional).visibility = View.GONE

                findViewById<MaterialButton>(R.id.dialogConfirmationOperation_btnConfirm).setOnClickListener {
                    val intent = Intent("DELETE_STORE")
                    intent.putExtra("STORE", store)
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