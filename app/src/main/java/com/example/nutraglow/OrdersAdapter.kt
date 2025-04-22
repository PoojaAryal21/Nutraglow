package com.example.nutraglow

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class OrdersAdapter(
    private val orders: List<Order>,
    private val isAdmin: Boolean,
    private val showCancel: Boolean = false,
    private val onCancelClick: ((Order) -> Unit)? = null,
    private val onTrackClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val customerNameText: TextView = itemView.findViewById(R.id.customerName)
        private val amountText: TextView = itemView.findViewById(R.id.orderAmount)
        private val statusText: TextView = itemView.findViewById(R.id.orderStatus)
        private val shippedButton: Button = itemView.findViewById(R.id.shippedButton)
        private val deliveredButton: Button = itemView.findViewById(R.id.deliveredButton)
        private val trackButton: Button = itemView.findViewById(R.id.trackOrderButton)
        private val cancelButton: Button = itemView.findViewById(R.id.cancelOrderButton)

        fun bind(order: Order) {
            customerNameText.text = order.customerName
            amountText.text = "Rs. ${order.totalAmount}"
            statusText.text = "Status: ${order.status}"

            if (isAdmin) {
                shippedButton.visibility = View.VISIBLE
                deliveredButton.visibility = View.VISIBLE
                cancelButton.visibility = View.GONE
                trackButton.visibility = View.GONE

                shippedButton.setOnClickListener {
                    updateStatus(order.orderId, "Shipped")
                }
                deliveredButton.setOnClickListener {
                    updateStatus(order.orderId, "Delivered")
                }
            } else {
                shippedButton.visibility = View.GONE
                deliveredButton.visibility = View.GONE
                trackButton.visibility = View.VISIBLE
                cancelButton.visibility = View.VISIBLE


                trackButton.setOnClickListener {
                    onTrackClick?.invoke(order)
                }

                cancelButton.setOnClickListener {
                    onCancelClick?.invoke(order)
                }
            }
        }

        private fun updateStatus(orderId: String, newStatus: String) {
            val ref = FirebaseDatabase.getInstance().getReference("orders").child(orderId)

            ref.child("status").setValue(newStatus).addOnSuccessListener {
                Toast.makeText(itemView.context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(itemView.context, "Failed to update status", Toast.LENGTH_SHORT).show()
            }

            val statusUpdate = OrderStatus(status = newStatus)
            ref.child("statusHistory").push().setValue(statusUpdate)
        }
    }
}
