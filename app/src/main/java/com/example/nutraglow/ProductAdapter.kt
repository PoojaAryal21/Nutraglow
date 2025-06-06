package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val isCart: Boolean,
    private val showDescription: Boolean = true,
    private val userId: String,
    private val onItemClick: ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var cartKeyMap: Map<String, String> = emptyMap()
    private val cartRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("carts").child(userId).child("products")
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
            cartRef.addValueEventListener(object : ValueEventListener {
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
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_product, parent, false)
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

        // If vendor is viewing their own product, show delete button, hide Add to Cart
        if (product.owner == userId) {
            holder.deleteProductButton.visibility = View.VISIBLE
            holder.addToCartButton.visibility = View.GONE
        } else {
            holder.deleteProductButton.visibility = View.GONE
            setupProductItem(holder, product)
        }

        if (isCart) {
            setupCartItem(holder, product, position)
        } else {
            holder.removeFromCartButton.visibility = View.GONE
            holder.quantityText.visibility = View.GONE
            holder.increaseQuantity.visibility = View.GONE
            holder.decreaseQuantity.visibility = View.GONE
        }

        holder.deleteProductButton.setOnClickListener {
            onDeleteClick?.invoke(product)
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(product)
        }
    }

    private fun setupProductItem(holder: ProductViewHolder, product: Product) {
        // Only show "Add to Cart" if not vendor's own product
        if (product.owner != userId) {
            holder.addToCartButton.visibility = View.VISIBLE
            if (cartProductIds.contains(product.productId)) {
                holder.addToCartButton.text = "Added"
                holder.addToCartButton.isEnabled = false
            } else {
                holder.addToCartButton.text = "Add to Cart"
                holder.addToCartButton.isEnabled = true
            }

            holder.addToCartButton.setOnClickListener {
                val productId = product.productId ?: return@setOnClickListener

                if (cartProductIds.contains(productId)) {
                    Toast.makeText(holder.itemView.context, "Already in Cart", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val cartItemId = cartRef.push().key ?: return@setOnClickListener
                val cartItem = product.copy(quantity = 1)

                cartRef.child(cartItemId).setValue(cartItem)
                    .addOnSuccessListener {
                        cartProductIds.add(productId)
                        holder.addToCartButton.text = "Added"
                        holder.addToCartButton.isEnabled = false
                        Toast.makeText(holder.itemView.context, "Added to Cart", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Failed to add to Cart", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            holder.addToCartButton.visibility = View.GONE
        }
    }

    private fun setupCartItem(holder: ProductViewHolder, product: Product, position: Int) {
        holder.addToCartButton.visibility = View.GONE
        holder.removeFromCartButton.visibility = View.VISIBLE
        holder.quantityText.visibility = View.VISIBLE
        holder.increaseQuantity.visibility = View.VISIBLE
        holder.decreaseQuantity.visibility = View.VISIBLE

        holder.increaseQuantity.setOnClickListener {
            val currentQty = product.quantity
            val newQty = currentQty + 1
            product.quantity = newQty
            holder.quantityText.text = newQty.toString()

            cartKeyMap[product.productId]?.let { key ->
                cartRef.child(key).child("quantity").setValue(newQty)
            }
        }

        holder.decreaseQuantity.setOnClickListener {
            val currentQty = product.quantity
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
    }

    override fun getItemCount(): Int = productList.size

    fun updateCartKeys(newCartKeyMap: Map<String, String>) {
        cartKeyMap = newCartKeyMap
    }
}
