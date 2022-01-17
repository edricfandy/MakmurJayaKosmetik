package com.example.makmurjayakosmetik.recyclerview

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.makmurjayakosmetik.R
import com.example.makmurjayakosmetik.classes.Account

class RVAccount(private val list : ArrayList<Account>) : RecyclerView.Adapter<RVAccount.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAvatar : TextView = itemView.findViewById(R.id.layoutAccountView_txtAvatar)
        val txtName : TextView = itemView.findViewById(R.id.layoutAccountView_txtName)
        val txtUsername : TextView = itemView.findViewById(R.id.layoutAccountView_txtUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_account_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = list[position]
        holder.apply {
            txtAvatar.text = account.name[0].toString()
            txtName.text = account.name
            txtUsername.text = account.username

            itemView.setOnClickListener {
                val intent = Intent("CHOOSE_ACCOUNT")
                intent.putExtra("CHOOSED_ACCOUNT_USERNAME", account.username)
                itemView.context.sendBroadcast(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}