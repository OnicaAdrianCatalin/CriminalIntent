package com.example.criminalintent.presentation.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar


class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = getInitialDateCalendar()
        val args = arguments?.getSerializable(ARG_DATE) as Date
        date.time = args
        return DatePickerDialog(
            requireContext(),
            getDateListener(),
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun getDateListener(): DatePickerDialog.OnDateSetListener {
        return DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate: Date = GregorianCalendar(year, month, day).time
            val result = bundleOf(RESULT_DATE_KEY to resultDate)
            val resultRequestCode = requireArguments().getString(ARG_REQUEST_CODE, "")
            if (isAdded) {
                parentFragmentManager.setFragmentResult(resultRequestCode, result)
            }
        }
    }

    private fun getInitialDateCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        return calendar.apply {
            this.get(Calendar.YEAR)
            this.get(Calendar.MONTH)
            this.get(Calendar.DAY_OF_MONTH)
        }
    }

    companion object {
        private const val ARG_DATE = "date"
        private const val ARG_REQUEST_CODE = "request_code"
        private const val RESULT_DATE_KEY = "dateKey"

        fun newInstance(date: Date, requestCode: String): DatePickerFragment {
            val args = bundleOf(
                ARG_DATE to date,
                ARG_REQUEST_CODE to requestCode
            )
            return DatePickerFragment().apply {
                arguments = args
            }
        }

        fun getSelectedDate(result: Bundle) = result.getSerializable(RESULT_DATE_KEY) as Date
    }
}
