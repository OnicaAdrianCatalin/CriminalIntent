package com.example.criminalintent.presentation.crime_detail

import android.app.Activity.RESULT_OK
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
import com.example.criminalintent.utils.PictureUtils
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
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    private var resultLauncherSuspect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result, REQUEST_PHOTO)
        }
    private var resultLauncherPhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result, REQUEST_CONTACT)
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
        setOnBackPressed()
    }

    private fun loadCrimeFromArguments() {
        val crimeId: Int = arguments?.getSerializable(ARG_CRIME_ID) as Int
        crimeDetailViewModel.loadCrime(crimeId)
    }

    private fun setOnBackPressed() {
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
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addOrUpdateCrime() {
        crimeDetailViewModel.upsert(crimeDetailViewModel.crime)
        activity?.supportFragmentManager?.popBackStack()
    }

    private fun observeData() {
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner
        ) { crime ->
            crime?.let {
                crimeDetailViewModel.crime = crime
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                photoUri = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.example.criminalintent.fileprovider", photoFile
                )
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
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                ).also { intent ->
                    val chooserIntent =
                        Intent.createChooser(intent, getString(R.string.send_report))
                    resultLauncherSuspect.launch(chooserIntent)
                }
            }
        }
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                resultLauncherSuspect.launch(pickContactIntent)
            }
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(
                    pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            if (resolvedActivity == null) {
                isEnabled = true
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                    captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                resultLauncherPhoto.launch(captureImage)
            }
        }
    }

    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        when {
            result.resultCode != RESULT_OK -> return
            requestCode == REQUEST_CONTACT -> {
                val contactURI: Uri? = result.data?.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = requireActivity().contentResolver.query(
                    contactURI!!, queryFields, null, null, null
                )
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crimeDetailViewModel.crime.suspect = suspect
                    crimeDetailViewModel.addCrime(crimeDetailViewModel.crime)
                    suspectButton.text = suspect
                }
            }
            requestCode == REQUEST_PHOTO -> {
                updatePhotoView()
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
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
        if (crimeDetailViewModel.crime.suspect.isNotEmpty()) {
            suspectButton.text = crimeDetailViewModel.crime.suspect
        }
        updatePhotoView()
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
        photoView = view.findViewById(R.id.crime_photo)
        photoButton = view.findViewById(R.id.crime_camera)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = PictureUtils.getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageBitmap(null)
        }
    }

    companion object {
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
