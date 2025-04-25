package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VendorAdapter(
    private var vendorList: List<Pair<String, String>>,  // Pair<userId, email>
    private val onSelect: (Int) -> Unit
) : RecyclerView.Adapter<VendorAdapter.VendorViewHolder>() {

    private var selectedPosition = -1

    inner class VendorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vendorEmail: TextView = view.findViewById(R.id.vendorEmail)
        val radioButton: RadioButton = view.findViewById(R.id.selectVendor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vendor, parent, false)
        return VendorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val (_, email) = vendorList[position]
        holder.vendorEmail.text = email
        holder.radioButton.isChecked = position == selectedPosition

        holder.radioButton.setOnClickListener {
            val prevPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(prevPosition)
            notifyItemChanged(selectedPosition)
            onSelect(selectedPosition)
        }
    }

    override fun getItemCount(): Int = vendorList.size

    fun updateList(newList: List<Pair<String, String>>) {
        vendorList = newList
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
