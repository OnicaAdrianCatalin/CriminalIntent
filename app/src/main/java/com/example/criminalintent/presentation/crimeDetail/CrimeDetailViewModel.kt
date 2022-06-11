package com.example.criminalintent.presentation.crimeDetail

import android.view.View
import android.widget.CheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.criminalintent.data.model.Crime
import com.example.criminalintent.data.repository.CrimeRepository
import java.io.File

class CrimeDetailViewModel : ViewModel() {

    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<Int>()

    var crime = Crime()

    val checkboxClickListener = View.OnClickListener {
        crime.isSolved = (it as CheckBox).isChecked
    }
    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun getPhotoFile(fileName: String? = null): File {
        return if (fileName == null) {
            crimeRepository.getPhotoFile()
        } else {
            crimeRepository.getPhotoFile(fileName)
        }
    }

    fun addOrUpdatePhotoFile() {
        crimeLiveData.value?.photoFileName?.let { photoFileName ->
            getPhotoFile(photoFileName).apply {
                if (exists() && getPhotoFile().exists()) {
                    delete()
                }
            }
        }
        getPhotoFile().renameTo(
            getPhotoFile(crime.photoFileName)
        )
    }

    fun releaseResources() {
        if (crimeLiveData.value?.photoFileName == null) {
            getPhotoFile().delete()
        }
    }

    fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

    fun loadCrime(crimeId: Int) {
        crimeIdLiveData.value = crimeId
    }

    fun updateCrime(crime: Crime) {
        crimeRepository.updateCrime(crime)
    }

    fun addOrUpdateCrime() {
        crimeRepository.addOrUpdate(crime)
    }
}
