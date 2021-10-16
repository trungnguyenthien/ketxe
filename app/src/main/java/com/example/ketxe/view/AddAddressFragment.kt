package com.example.ketxe.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.ketxe.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddAddressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAddressFragment(val listener: Listener): Fragment() {

    interface Listener {
        fun onSaveClick(address: String, startTime: LocalDateTime?, endTime: LocalDateTime?)
    }

    private val backButton: Button get() = view!!.findViewById(R.id.btn_back)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_address, container, false)
    }

    override fun onResume() {
        super.onResume()
        backButton.setOnClickListener {
            val activity = activity ?: return@setOnClickListener
            activity.supportFragmentManager.popBackStack()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddAddressFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(listener: Listener) = AddAddressFragment(listener)
    }
}