package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Account
import com.google.android.material.button.MaterialButton
import java.math.BigInteger
import java.security.MessageDigest

class ChangePasswordDialog : DialogFragment() {
    private lateinit var db: DBHelper
    private var account : Account? = null

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

        db = DBHelper(requireContext())
        account = db.getAccountByUsername(arguments?.getString("ACCOUNT_USERNAME") ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_change_password, container, false)

        val etCurrentPassword = view.findViewById<EditText>(R.id.dialogChangePassword_etOldPassword)
        val etNewPassword = view.findViewById<EditText>(R.id.dialogChangePassword_etNewPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.dialogChangePassword_etConfirmPassword)
        val btnCancel = view.findViewById<MaterialButton>(R.id.dialogChangePassword_btnCancel)
        val btnConfirm = view.findViewById<MaterialButton>(R.id.dialogChangePassword_btnConfirm)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            if (etCurrentPassword.text.isEmpty() || etNewPassword.text.isEmpty() || etConfirmPassword.text.isEmpty()) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Data incomplete")
                    .setMessage("Please fill all the field.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }

            if (etNewPassword.text.toString() != etConfirmPassword.text.toString()) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("False Operation")
                    .setMessage("The new password and the confirmation password doesn't match.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }

            account?.password = etCurrentPassword.text.toString()
            if (!db.checkLoginCredentials(account ?: Account("", ""))) {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("False Operation")
                    .setMessage("Current password wrong.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }

            val newPassword = BigInteger(1, MessageDigest.getInstance("MD5").digest(etNewPassword.text.toString().toByteArray())).toString().padStart(32, '0')
            db.changeAccountPassword(account ?: Account("", ""), newPassword)
            Toast.makeText(requireContext(), "Password change successfully", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return view
    }
}