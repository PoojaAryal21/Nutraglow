package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerAdapter(
    private var customerList: List<Pair<String, String>>,  // Pair<userId, email>
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    private var selectedPosition = -1

    inner class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val customerEmail: TextView = view.findViewById(R.id.customerEmail)
        val radioButton: RadioButton = view.findViewById(R.id.selectCustomer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val (_, email) = customerList[position]
        holder.customerEmail.text = email
        holder.radioButton.isChecked = position == selectedPosition

        holder.radioButton.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onSelect(selectedPosition)
        }
    }

    override fun getItemCount(): Int = customerList.size

    fun updateList(newList: List<Pair<String, String>>) {
        customerList = newList
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedUserId(): String? = customerList.getOrNull(selectedPosition)?.first
}
