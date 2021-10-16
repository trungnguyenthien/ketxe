package com.example.ketxe.view

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.FragmentActivity
import com.example.ketxe.R
import java.time.LocalDateTime
import androidx.fragment.app.DialogFragment
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddAddressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddAddressFragment(private val listener: Listener): Fragment() {

    interface Listener {
        fun onSaveClick(address: String, startTimeByMinInDay: Int?, endTimeByMinInDay: Int?)
    }

    private val backButton: Button get() = view!!.findViewById(R.id.btn_back)
    private val saveButton: Button get() = view!!.findViewById(R.id.btn_save)

    private val startButton: Button get() = view!!.findViewById(R.id.btn_start_time)
    private val endButton: Button get() = view!!.findViewById(R.id.btn_end_time)

    private val addressText: EditText get() = view!!.findViewById(R.id.text_address)

    private var startTimeByMinInDay: Int? = null
    private var endTimeByMinInDay: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_address, container, false)
    }

    override fun onResume() {
        super.onResume()
        backButton.setOnClickListener { popSelf() }

        saveButton.setOnClickListener {
            listener.onSaveClick(
                address = addressText.text.toString(),
                startTimeByMinInDay = startTimeByMinInDay,
                endTimeByMinInDay = endTimeByMinInDay
            )
        }

        startButton.setOnClickListener {
            val supportFragmentManager = activity?.supportFragmentManager ?: return@setOnClickListener
            TimePickerFragment { hourOfDay, minute ->
                startTimeByMinInDay = hourOfDay * 60 + minute
                updateTime(button = startButton, hourOfDay, minute)
            }.show(supportFragmentManager, "startButton")
        }

        endButton.setOnClickListener {
            val supportFragmentManager = activity?.supportFragmentManager ?: return@setOnClickListener
            TimePickerFragment { hourOfDay, minute ->
                endTimeByMinInDay = hourOfDay * 60 + minute
                updateTime(button = endButton, hourOfDay, minute)
            }.show(supportFragmentManager, "endButton")
        }
    }

    private fun updateTime(button: Button, hourOfDay: Int, minute: Int) {
        button.text = "${hourOfDay.to00}:${minute.to00}"
    }

    private val Int.to00 get() = when(this < 10) {
        true -> "0$this"
        false -> "$this"
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

class TimePickerFragment(private val onSet: (hourOfDay: Int, minute: Int) -> Unit) : DialogFragment(), OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c[Calendar.HOUR_OF_DAY]
        val minute = c[Calendar.MINUTE]
        val format = DateFormat.is24HourFormat(activity)
        return TimePickerDialog(activity, this, hour, minute, format)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        onSet.invoke(hourOfDay, minute)
    }
}

fun Fragment.popSelf() {
    activity?.supportFragmentManager?.popBackStack()
}

fun FragmentActivity.push(fragment: Fragment, atLayoutId: Int) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
    transaction.replace(atLayoutId, fragment)
    transaction.addToBackStack(null)
    transaction.commit()
}