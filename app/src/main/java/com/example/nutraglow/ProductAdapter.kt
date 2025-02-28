package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(private val productList: MutableList<Product>, private val isCart: Boolean) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product1Name)
        val price: TextView = itemView.findViewById(R.id.product1Price)
        val description: TextView = itemView.findViewById(R.id.product1Description)
        val image: ImageView = itemView.findViewById(R.id.product1Image)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
        val removeFromCartButton: Button = itemView.findViewById(R.id.removeFromCartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        if (position >= productList.size) return  // Prevent index errors

        val product = productList[position]

        // Ensure productId is valid
        if (product.productId.isNullOrEmpty()) {
            Log.e("ProductAdapter", "Error: productId is null or empty for product: ${product.name}")
            return
        }

        holder.name.text = product.name
        holder.price.text = holder.itemView.context.getString(R.string.product_price, product.price)
        holder.description.text = holder.itemView.context.getString(R.string.product_description, product.description)

        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .into(holder.image)

        val cartRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("cart")

        if (isCart) {
            holder.addToCartButton.visibility = View.GONE
            holder.removeFromCartButton.visibility = View.VISIBLE

            holder.removeFromCartButton.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val productId = productList[pos].productId
                if (!productId.isNullOrEmpty()) {
                    cartRef.child(productId).removeValue()
                        .addOnSuccessListener {
                            if (pos < productList.size) {
                                productList.removeAt(pos)
                                notifyItemRemoved(pos)
                                notifyItemRangeChanged(pos, productList.size)
                                Log.d("FirebaseSuccess", "Removed product from cart: $productId")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseError", "Failed to remove item from cart: ${exception.message}")
                        }
                }
            }
        } else {
            holder.addToCartButton.visibility = View.VISIBLE
            holder.removeFromCartButton.visibility = View.GONE

            holder.addToCartButton.setOnClickListener {
                val pos = holder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

                val productId = productList[pos].productId
                if (!productId.isNullOrEmpty()) {
                    val cartItem = Product(productId, product.name, product.price, product.description, product.imageUrl)

                    cartRef.child(productId).setValue(cartItem)
                        .addOnSuccessListener {
                            holder.addToCartButton.text = "Added"
                            holder.addToCartButton.isEnabled = false
                            Log.d("FirebaseSuccess", "Product added to cart successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseError", "Failed to add item to cart: ${exception.message}")
                        }
                }
            }
        }
    }

    override fun getItemCount() = productList.size
}
