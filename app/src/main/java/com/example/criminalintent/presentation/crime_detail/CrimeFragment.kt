package com.example.criminalintent.presentation.crime_detail

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
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
        inflater.inflate(R.menu.menu_crime_detail, menu)
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
        reportButton.setOnClickListener {
            sendCrimeReport()
        }
        suspectButton.setOnClickListener {
            pickContactIntent()
        }
    }

    private fun pickContactIntent() {
        val pickContactIntent =
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        resultLauncher.launch(pickContactIntent)
    }

    private fun sendCrimeReport() {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getCrimeReport())
            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.crime_report_subject)
            ).also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
    }

    private fun onActivityResult(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                val contactURI: Uri? = result.data?.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactURI?.let {
                    requireActivity().contentResolver.query(
                        it, queryFields, null, null, null
                    )
                }
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspectName = it.getString(0)
                    crimeDetailViewModel.updateSuspect(suspectName)
                    suspectButton.text = suspectName
                }
            }
        }
    }

    private fun showAlertDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_message)
                .setNegativeButton(
                    R.string.dialog_negative
                ) { _, _ -> activity?.supportFragmentManager?.popBackStack() }
                .setPositiveButton(
                    R.string.dialog_positive
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    private fun updateUI() {
        titleField.setText(crimeDetailViewModel.crime.title)
        dateButton.text = crimeDetailViewModel.crime.date.toString()
        solvedCheckBox.isChecked = crimeDetailViewModel.crime.isSolved
        if (crimeDetailViewModel.crime.suspect.isNotEmpty()) {
            suspectButton.text = crimeDetailViewModel.crime.suspect
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crimeDetailViewModel.crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crimeDetailViewModel.crime.date).toString()
        val suspect = if (crimeDetailViewModel.crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crimeDetailViewModel.crime.suspect)
        }
        return getString(
            R.string.crime_report,
            crimeDetailViewModel.crime.title,
            dateString,
            solvedString,
            suspect
        )
    }

    private fun bindViews(view: View) {
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report)
        suspectButton = view.findViewById(R.id.crime_suspect)
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
        private const val DATE_FORMAT = "EEE, MMM, dd"
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
