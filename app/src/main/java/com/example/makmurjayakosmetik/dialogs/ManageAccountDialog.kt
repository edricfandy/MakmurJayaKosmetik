package com.example.makmurjayakosmetik.dialogs

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.makmurjayakosmetik.AccountSharedPreferences
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.SettingsSharedPreferences
import com.example.makmurjayakosmetik.classes.Account
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class ManageAccountDialog : DialogFragment() {
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
        account = db.getAccountByUsername(arguments?.getString("ACCOUNT_ID") ?: "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_manage_account, container, false)
        val settingsSharedPref = SettingsSharedPreferences(requireContext())

        val layoutAccountInfo = view.findViewById<View>(R.id.dialogManageAccount_layoutAccountInfo)
        val txtAvatar = layoutAccountInfo.findViewById<TextView>(R.id.layoutAccountView_txtAvatar)
        val txtName = layoutAccountInfo.findViewById<TextView>(R.id.layoutAccountView_txtName)
        val txtUsername = layoutAccountInfo.findViewById<TextView>(R.id.layoutAccountView_txtUsername)
        val swUseImage = view.findViewById<SwitchMaterial>(R.id.dialogManageAccount_swUseImage)
        val btnChangePassword = view.findViewById<TextView>(R.id.dialogManageAccount_btnChangePassword)
        val btnLogout = view.findViewById<TextView>(R.id.dialogManageAccount_btnLogout)

        txtAvatar.text = account?.username?.get(0)?.toString()
        txtName.text = account?.name
        txtUsername.text = account?.username
        swUseImage.isChecked = settingsSharedPref.use_image

        btnChangePassword.setOnClickListener {
            val dialog = ChangePasswordDialog()
            val bundle = Bundle()
            bundle.putString("ACCOUNT_USERNAME", account?.username)
            dialog.arguments = bundle
            dialog.show(requireActivity().supportFragmentManager.beginTransaction(), "CHANGE_PASSWORD")
            dismiss()
        }

        swUseImage.setOnCheckedChangeListener { buttonView, isChecked ->
            settingsSharedPref.use_image = isChecked
            requireContext().sendBroadcast(Intent("CHANGE_PRODUCT_VIEW"))
        }

        btnLogout.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Logout Account")
                .setMessage("Are you sure want to logout from this account?")
                .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, i: Int ->
                    AccountSharedPreferences(requireContext()).username = ""
                    Toast.makeText(requireContext(), "Successfully logout from account.", Toast.LENGTH_SHORT).show()
                    requireContext().sendBroadcast(Intent("LOG_OUT"))
                    dialogInterface.dismiss()
                }
                .setNegativeButton("CANCEL") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
                .create()
            dialog.show()
        }

        return view
    }
}