package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: MutableList<User>, private val onDeleteClick: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userEmail: TextView = itemView.findViewById(R.id.userEmail)
        val userRole: TextView = itemView.findViewById(R.id.userRole)
        val deleteUserButton: Button = itemView.findViewById(R.id.deleteUserButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userEmail.text = user.email
        holder.userRole.text = user.role

        holder.deleteUserButton.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount() = userList.size
}
