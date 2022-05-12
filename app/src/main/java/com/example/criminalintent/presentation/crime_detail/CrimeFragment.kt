package com.example.criminalintent.presentation.crime_detail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.criminalintent.R
import com.example.criminalintent.presentation.dialogs.DatePickerFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CrimeFragment : Fragment(), FragmentResultListener {

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCrimeFromArguments()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        bindViews(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.setFragmentResultListener(DIALOG_DATE, viewLifecycleOwner, this)
        observeData()
        showDialogOnBackPressed()
    }

    private fun loadCrimeFromArguments() {
        val crimeId: Int = arguments?.getSerializable(ARG_CRIME_ID) as Int
        crimeDetailViewModel.loadCrime(crimeId)
    }

    private fun showDialogOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showAlertDialog()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_crimedetail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_crime -> {
                addOrUpdateCrime()
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addOrUpdateCrime() {
        crimeDetailViewModel.addOrUpdate(crimeDetailViewModel.crime)
    }

    private fun observeData() {
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                crimeDetailViewModel.crime = crime
                updateUI()
            }
        }
    }

    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when (requestCode) {
            DIALOG_DATE -> {
                crimeDetailViewModel.crime.date = DatePickerFragment.getSelectedDate(result)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        onTextChangeListener()
        setOnClickListeners()
        dateButton.text = crimeDetailViewModel.crime.date.toString()
    }

    private fun setOnClickListeners() {
        solvedCheckBox.setOnClickListener(crimeDetailViewModel.checkboxClickListener)
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crimeDetailViewModel.crime.date, DIALOG_DATE).apply {
                show(this@CrimeFragment.childFragmentManager, DIALOG_DATE)
            }
        }
    }

    private fun showAlertDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Alert!")
                .setMessage("If you exit, the changes will not be saved")
                .setNegativeButton(
                    "Discard changes"
                ) { _, _ -> activity?.supportFragmentManager?.popBackStack() }
                .setPositiveButton(
                    "Continue editing"
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    private fun updateUI() {
        titleField.setText(crimeDetailViewModel.crime.title)
        dateButton.text = crimeDetailViewModel.crime.date.toString()
        solvedCheckBox.isChecked = crimeDetailViewModel.crime.isSolved
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
                crimeDetailViewModel.crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                // Not implemented
            }
        }
        titleField.addTextChangedListener(titleWatcher)
    }

    companion object {
        private const val ARG_CRIME_ID = "crime_id"
        private const val DIALOG_DATE = "DialogDate"

        fun newInstance(crimeId: Int): CrimeFragment {
            val args = bundleOf(ARG_CRIME_ID to crimeId)
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}
