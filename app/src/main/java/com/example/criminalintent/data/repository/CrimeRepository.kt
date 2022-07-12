package com.example.criminalintent.data.repository

import androidx.lifecycle.LiveData
import com.example.criminalintent.data.model.Crime
import java.io.File

interface CrimeRepository {
    fun getCrimes(): LiveData<List<Crime>>
    fun getCrime(id: Int): LiveData<Crime?>
    fun updateCrime(crime: Crime)
    fun addCrime(crime: Crime)
    fun addOrUpdate(crime: Crime)
    fun getPhotoFile(fileName: String = TEMPORARY_PHOTO_FILE_NAME): File

    companion object {
        private const val TEMPORARY_PHOTO_FILE_NAME = "temporary_file"
    }
}
