package com.example.ketxe.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ketxe.R

class AddressList(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressText: TextView by lazy { itemView.findViewById<TextView>(R.id.address_text) }
        private val closeRoadText: TextView by lazy { itemView.findViewById<TextView>(R.id.closeRoad_text) }
        private val seriousText: TextView by lazy { itemView.findViewById<TextView>(R.id.serious_text) }
        private val noSeriousText: TextView by lazy { itemView.findViewById<TextView>(R.id.noSerious_text) }
        private val btnDelete: ImageButton by lazy { itemView.findViewById<ImageButton>(R.id.btn_remove_address) }
        private val btnDebug by lazy { itemView.findViewById<Button>(R.id.btn_debug) }
        
        fun config(item: HomeAddressRow,
                   deleteAction: (Address) -> Unit,
                   clickAction: (Address) -> Unit,
                   debugAction: (Address) -> Unit
        ) {
            addressText.text = item.address.description
            closeRoadText.text = "+ Có ${item.result.closesRoadsCount} đường bị chặn"
            seriousText.text = "+ Có ${item.result.seriousCount} điểm kẹt xe"
            noSeriousText.text = "+ Có ${item.result.noSeriousCount} điểm đông xe"

            btnDelete.setOnClickListener { deleteAction.invoke(item.address) }
            itemView.setOnClickListener { clickAction.invoke(item.address) }
            btnDebug.setOnClickListener { debugAction.invoke(item.address) }
        }
    }

    class Adapter(context: Context?) : RecyclerView.Adapter<ViewHolder>() {
        private var data = ArrayList<HomeAddressRow>()
        private val mInflater = LayoutInflater.from(context)
        var onDeleteItem: ((Address) -> Unit)? = null
        var onClickAddress: ((Address) -> Unit)? = null
        var onClickDebug: ((Address) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = mInflater.inflate(R.layout.address_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.config(data[position],
                deleteAction = { address -> onDeleteItem?.invoke(address) },
                clickAction = { address -> onClickAddress?.invoke(address) },
                debugAction = { address -> onClickDebug?.invoke(address) }
            )
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