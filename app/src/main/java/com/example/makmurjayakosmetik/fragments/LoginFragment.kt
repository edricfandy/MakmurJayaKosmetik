package com.example.makmurjayakosmetik.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.makmurjayakosmetik.AccountSharedPreferences
import com.example.makmurjayakosmetik.DBHelper
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Account
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.math.BigInteger
import java.security.MessageDigest

class LoginFragment(private val username: String) : Fragment() {
    private lateinit var db: DBHelper
    private var account: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DBHelper(requireContext())
        account = db.getAccountByUsername(username)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.loginFragment_toolbar)
        val txtName = view.findViewById<TextView>(R.id.loginFragment_txtName)
        val txtUsername = view.findViewById<TextView>(R.id.loginFragment_txtUsername)
        val etPassword = view.findViewById<EditText>(R.id.loginFragment_etPassword)
        val btnLogin = view.findViewById<MaterialButton>(R.id.loginFragment_btnLogin)

        toolbar.setNavigationOnClickListener {
            requireContext().sendBroadcast(Intent("BACK_TO_CHOOSE_ACCOUNT"))
        }

        txtName.text = account?.name
        txtUsername.text = account?.username

        btnLogin.setOnClickListener {
            val md5 = MessageDigest.getInstance("MD5")
            val password = BigInteger(1, md5.digest(etPassword.text.toString().toByteArray())).toString().padStart(32, '0')
            account?.password = password
            if (db.checkLoginCredentials(account ?: Account("",""))) {
                Toast.makeText(requireContext(), "Successfully login to ${account?.name}", Toast.LENGTH_SHORT).show()
                val sharedPreferences = AccountSharedPreferences(requireContext())
                sharedPreferences.username = account?.username
                requireContext().sendBroadcast(Intent("LOGIN_SUCCESSFUL"))
            } else {
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Login Failed")
                    .setMessage("Password incorrect. Login failed. Please input proper password.")
                    .setPositiveButton("CONFIRM") { dialogInterface: DialogInterface, _: Int ->
                        dialogInterface.dismiss()
                    }
                    .create()
                dialog.show()
            }
        }

        return view
    }
}