package com.example.criminalintent.presentation.crime_detail

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.criminalintent.R
import com.example.criminalintent.presentation.dialogs.DatePickerFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class CrimeFragment : Fragment(), FragmentResultListener {

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

    private val viewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    private var resultLauncherSuspect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result, REQUEST_CONTACT)
        }
    private var resultLauncherPhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result, REQUEST_PHOTO)
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
        viewModel.loadCrime(crimeId)
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
        inflater.inflate(R.menu.view_crimedetail, menu)
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
        viewModel.addOrUpdate(viewModel.crime)
    }

    private fun observeData() {
        viewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                viewModel.crime = crime
                updateUI()
            }
        }
    }

    override fun onFragmentResult(requestCode: String, result: Bundle) {
        when (requestCode) {
            DIALOG_DATE -> {
                viewModel.crime.date = DatePickerFragment.getSelectedDate(result)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        onTextChangeListener()
        setOnClickListeners()
        dateButton.text = viewModel.crime.date.toString()
    }

    private fun setOnClickListeners() {
        solvedCheckBox.setOnClickListener(viewModel.checkboxClickListener)
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(viewModel.crime.date, DIALOG_DATE).apply {
                show(this@CrimeFragment.childFragmentManager, DIALOG_DATE)
            }
        }
        photoButton.setOnClickListener {
            onCreatePhoto()
        }
        reportButton.setOnClickListener {
            sendCrimeReport()
        }
        suspectButton.setOnClickListener {
            pickContactIntent()
        }
    }

    private fun onCreatePhoto() {
        val photoFileProviderUri =
            viewModel.getPhotoFile().getFileProviderUri(requireContext(), AUTHORITY)
        val packageManager: PackageManager = requireActivity().packageManager
        val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoFileProviderUri)
        val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
            captureImage,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (cameraActivity in cameraActivities) {
            requireActivity().grantUriPermission(
                cameraActivity.activityInfo.packageName,
                photoFileProviderUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        resultLauncherPhoto.launch(captureImage)
    }

    private fun pickContactIntent() {
        val pickContactIntent =
            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        resultLauncherSuspect.launch(pickContactIntent)
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

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        when {
            result.resultCode != RESULT_OK -> return
            requestCode == REQUEST_CONTACT -> {
                onContactSelected(result)
            }
            requestCode == REQUEST_PHOTO -> {
                updatePhotoView()
                requireActivity().revokeUriPermission(
                    viewModel.getPhotoFile()
                        .getFileProviderUri(requireContext(), AUTHORITY),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }
    }

    private fun onContactSelected(result: ActivityResult) {
        val contactURI: Uri? = result.data?.data
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        if (contactURI != null) {
            val cursor = requireActivity().contentResolver.query(
                contactURI, queryFields, null, null, null
            )
            cursor?.use {
                if (it.count == 0) {
                    return
                }
                it.moveToFirst()
                val suspectName = it.getString(0)
                viewModel.onSuspectNameSelected(suspectName)
                suspectButton.text = suspectName
            }
        } else {
            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun showAlertDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle(R.string.dialog_title)
                .setMessage(R.string.dialog_message)
                .setNegativeButton(
                    R.string.dialog_negative
                ) { _, _ ->
                    activity?.supportFragmentManager?.popBackStack()
                    if(viewModel.crimeLiveData.value?.photoFileName == null){
                        viewModel.getPhotoFile().delete()
                    }
                    Log.d(
                        "value",
                        "showAlertDialog: value is ${viewModel.crimeLiveData.value?.photoFileName} "
                    )
                }
                .setPositiveButton(
                    R.string.dialog_positive
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
        }
    }

    private fun updateUI() {
        titleField.setText(viewModel.crime.title)
        dateButton.text = viewModel.crime.date.toString()
        solvedCheckBox.isChecked = viewModel.crime.isSolved
        if (viewModel.crime.suspect.isNotEmpty()) {
            suspectButton.text = viewModel.crime.suspect
        }
        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (viewModel.crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString =
            DateFormat.format(DATE_FORMAT, viewModel.crime.date).toString()
        val suspect = if (viewModel.crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, viewModel.crime.suspect)
        }
        return getString(
            R.string.crime_report,
            viewModel.crime.title,
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
        photoView = view.findViewById(R.id.crime_photo)
        photoButton = view.findViewById(R.id.crime_camera)
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
                viewModel.crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                // Not implemented
            }
        }
        titleField.addTextChangedListener(titleWatcher)
    }

    private fun updatePhotoView() {
        if (viewModel.getPhotoFile().isEmpty()) {
            photoView.setImageURI(null)
        } else {
            photoView.setImageURI(Uri.fromFile(viewModel.getPhotoFile()))
        }
    }

    companion object {
        private const val AUTHORITY = "com.example.criminalintent.fileprovider"
        private const val REQUEST_CONTACT = 1
        private const val REQUEST_PHOTO = 2
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

private fun File.isEmpty() = length() == 0L

private fun File.getFileProviderUri(context: Context, authority: String): Uri? {
    return FileProvider.getUriForFile(
        context,
        authority, this
    )
}
