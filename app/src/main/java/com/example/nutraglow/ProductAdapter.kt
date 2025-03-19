package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val isCart: Boolean,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val description: TextView = itemView.findViewById(R.id.productDescription)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
        val removeFromCartButton: Button = itemView.findViewById(R.id.removeFromCartButton)
        val deleteProductButton: Button = itemView.findViewById(R.id.deleteProductButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_product, parent, false) // ✅ Ensure correct layout file
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.name
        holder.price.text = "$${product.price}"  // ✅ Fix price formatting
        holder.description.text = product.description

        // ✅ Ensure image loads properly
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .into(holder.image)

        if (isCart) {
            holder.addToCartButton.visibility = View.GONE
            holder.removeFromCartButton.visibility = View.VISIBLE
            holder.removeFromCartButton.setOnClickListener {
                val cartRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("cart")
                cartRef.child(product.productId!!).removeValue().addOnSuccessListener {
                    productList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, productList.size)
                }
            }
        } else {
            holder.addToCartButton.visibility = View.VISIBLE
            holder.removeFromCartButton.visibility = View.GONE
            holder.addToCartButton.setOnClickListener {
                val cartRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("cart")
                val cartItemId = cartRef.push().key ?: return@setOnClickListener
                val cartItem = Product(product.productId, product.name, product.price, product.description, product.imageUrl)

                cartRef.child(cartItemId).setValue(cartItem).addOnSuccessListener {
                    holder.addToCartButton.text = "Added"
                    holder.addToCartButton.isEnabled = false
                }
            }
        }

        holder.deleteProductButton.setOnClickListener {
            onDeleteClick?.invoke(product)
        }
    }

    override fun getItemCount() = productList.size
}
