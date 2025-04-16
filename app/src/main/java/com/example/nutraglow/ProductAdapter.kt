package com.example.nutraglow

import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val isCart: Boolean,
    private val showDescription: Boolean = true,
    private val onItemClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var cartKeyMap: Map<String, String> = emptyMap()
    private val cartRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("cart")
    private val cartProductIds = mutableSetOf<String>()

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val description: TextView = itemView.findViewById(R.id.productDescription)
        val image: ImageView = itemView.findViewById(R.id.productImage)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
        val removeFromCartButton: Button = itemView.findViewById(R.id.removeFromCartButton)
        val deleteProductButton: Button = itemView.findViewById(R.id.deleteProductButton)
        val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        val increaseQuantity: Button = itemView.findViewById(R.id.increaseQuantity)
        val decreaseQuantity: Button = itemView.findViewById(R.id.decreaseQuantity)

    }

    init {
        if (!isCart) {
            FirebaseDatabase.getInstance().getReference("cart")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cartProductIds.clear()
                        for (cartSnapshot in snapshot.children) {
                            val product = cartSnapshot.getValue(Product::class.java)
                            product?.productId?.let { cartProductIds.add(it) }
                        }
                        notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.activity_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.name
        holder.price.text = "Rs. ${product.price}"

        if (showDescription) {
            holder.description.visibility = View.VISIBLE
            holder.description.text = product.description
        } else {
            holder.description.visibility = View.GONE
        }

        holder.quantityText.text = product.quantity.toString()
        Glide.with(holder.itemView.context).load(product.imageUrl).into(holder.image)

        if (isCart) {
            holder.addToCartButton.visibility = View.GONE
            holder.removeFromCartButton.visibility = View.VISIBLE
            holder.quantityText.visibility = View.VISIBLE
            holder.increaseQuantity.visibility = View.VISIBLE
            holder.decreaseQuantity.visibility = View.VISIBLE

            holder.increaseQuantity.setOnClickListener {
                val currentQty = product.quantity ?: 1
                val newQty = currentQty + 1
                product.quantity = newQty
                holder.quantityText.text = newQty.toString()
                cartKeyMap[product.productId]?.let { key ->
                    cartRef.child(key).child("quantity").setValue(newQty)
                }
            }

            holder.decreaseQuantity.setOnClickListener {
                val currentQty = product.quantity ?: 1
                if (currentQty > 1) {
                    val newQty = currentQty - 1
                    product.quantity = newQty
                    holder.quantityText.text = newQty.toString()
                    cartKeyMap[product.productId]?.let { key ->
                        cartRef.child(key).child("quantity").setValue(newQty)
                    }
                } else {
                    cartKeyMap[product.productId]?.let { key ->
                        cartRef.child(key).removeValue().addOnSuccessListener {
                            if (position >= 0 && position < productList.size) {
                                productList.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, productList.size)
                            }
                        }
                    }
                }
            }

            holder.removeFromCartButton.setOnClickListener {
                cartKeyMap[product.productId]?.let { key ->
                    cartRef.child(key).removeValue().addOnSuccessListener {
                        if (position >= 0 && position < productList.size) {
                            productList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, productList.size)
                        }
                    }
                }
            }
        } else {
            holder.addToCartButton.visibility = View.VISIBLE
            holder.removeFromCartButton.visibility = View.GONE
            holder.quantityText.visibility = View.GONE
            holder.increaseQuantity.visibility = View.GONE
            holder.decreaseQuantity.visibility = View.GONE

            holder.addToCartButton.setBackgroundResource(android.R.drawable.btn_default)
            holder.addToCartButton.text = "Add to Cart"
            holder.addToCartButton.isEnabled = true

            holder.addToCartButton.setOnClickListener {
                val cartItemId = cartRef.push().key ?: return@setOnClickListener
                val cartItem = product.copy(quantity = 1)

                cartRef.child(cartItemId).setValue(cartItem).addOnSuccessListener {
                    cartProductIds.add(product.productId!!)
                    holder.addToCartButton.text = "Added"
                    holder.addToCartButton.setBackgroundResource(android.R.drawable.btn_default)

                    Handler().postDelayed({
                        holder.addToCartButton.text = "Add to Cart"
                        holder.addToCartButton.setBackgroundResource(android.R.drawable.btn_default)
                    }, 1000)
                }
            }
        }

        holder.deleteProductButton.setOnClickListener {
            onDeleteClick?.invoke(product)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }


    }

    override fun getItemCount() = productList.size

    fun updateCartKeys(newCartKeyMap: Map<String, String>) {
        cartKeyMap = newCartKeyMap
    }
}
