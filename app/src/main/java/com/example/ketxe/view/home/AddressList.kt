package com.example.ketxe.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ketxe.R

class AddressList(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {
    var customAdapter = Adapter(context)

    fun reloadData(list: List<Address>) {
        this.adapter = customAdapter
        customAdapter.update(list)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView by lazy { itemView.findViewById<TextView>(R.id.address_text) }
        fun config(position: Int, item: Address) {
            textView.text = item.description
        }
    }

    class Adapter(context: Context?) : RecyclerView.Adapter<ViewHolder>() {
        private var data = ArrayList<Address>()
        private val mInflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = mInflater.inflate(R.layout.address_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.config(position, data[position])
        }

        override fun getItemCount(): Int  = data.size

        fun update(datas: List<Address>) {
            this.data.clear()
            this.data.addAll(datas)
            this.notifyDataSetChanged()
        }
    }
}