package com.example.nutraglow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(private val productList: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product1Name)
        val price: TextView = itemView.findViewById(R.id.product1Price)
        val description: TextView = itemView.findViewById(R.id.product1Description)
        val image: ImageView = itemView.findViewById(R.id.product1Image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.name.text = product.name
        holder.price.text = holder.itemView.context.getString(R.string.product_price, product.price)
        holder.description.text = holder.itemView.context.getString(R.string.product_description, product.description)
        Glide.with(holder.itemView.context)
            .load(product.imageUrl)
            .into(holder.image)
    }

    override fun getItemCount() = productList.size
}
