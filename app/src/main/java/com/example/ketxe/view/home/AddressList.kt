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
        private val addressText: TextView by lazy { itemView.findViewById<TextView>(R.id.address_text) }
        private val infoText: TextView by lazy { itemView.findViewById<TextView>(R.id.info_text) }
        private val btnDelete: ImageButton by lazy { itemView.findViewById<ImageButton>(R.id.btn_remove_address) }

        fun config(position: Int, item: HomeAddressRow, deleteAction: (Address) -> Unit) {
            addressText.text = item.address.description
            infoText.text = "- ${item.serious} điểm kẹt xe \n- ${item.noSerious} điểm đông xe"
            btnDelete.setOnClickListener {
                deleteAction.invoke(item.address)
            }
        }
    }

    class Adapter(context: Context?) : RecyclerView.Adapter<ViewHolder>() {
        private var data = ArrayList<HomeAddressRow>()
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

        fun update(data: List<HomeAddressRow>) {
            this.data.clear()
            this.data.addAll(data)
            this.notifyDataSetChanged()
        }
    }
}