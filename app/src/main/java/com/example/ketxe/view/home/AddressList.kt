package com.example.ketxe.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ketxe.R

class AddressList(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView by lazy { itemView.findViewById<TextView>(R.id.address_text) }
        private val btnDelete: ImageButton by lazy { itemView.findViewById<ImageButton>(R.id.btn_remove_address) }

        fun config(position: Int, item: Address, deleteAction: (Address) -> Unit) {
            textView.text = item.description
            btnDelete.setOnClickListener {
                deleteAction.invoke(item)
            }
        }
    }

    class Adapter(context: Context?) : RecyclerView.Adapter<ViewHolder>() {
        private var data = ArrayList<Address>()
        private val mInflater = LayoutInflater.from(context)
        var onDeleteItem: ((Address) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = mInflater.inflate(R.layout.address_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.config(position, data[position], deleteAction = { address ->
                onDeleteItem?.invoke(address)
            })
        }

        override fun getItemCount(): Int  {
            return data.size
        }

        fun update(data: List<Address>) {
            this.data.clear()
            this.data.addAll(data)
            this.notifyDataSetChanged()
        }
    }
}