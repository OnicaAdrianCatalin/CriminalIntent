package com.example.criminalintent.presentation.crime_detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.criminalintent.R
import com.example.criminalintent.data.model.Crime

class CrimeFragment : Fragment() {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: Int = arguments?.getSerializable(ARG_CRIME_ID) as Int
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        bindViews(view)
        dateButton.apply {
            text = crime.date.toString()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun observeData() {
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                this.crime = crime
                updateUI()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        onTextChangeListener()
        solvedCheckBox.apply {
            crime.isSolved = isChecked
            jumpDrawablesToCurrentState()
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.updateCrime(crime)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.isChecked = crime.isSolved
    }

    private fun bindViews(view: View) {
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
    }

    private fun onTextChangeListener() {
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Not implemented
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                // Not implemented
            }
        }
        titleField.addTextChangedListener(titleWatcher)
    }

    companion object {
        private const val ARG_CRIME_ID = "crime_id"

        fun newInstance(crimeId: Int): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}
