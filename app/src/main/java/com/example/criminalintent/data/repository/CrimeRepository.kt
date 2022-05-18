package com.example.criminalintent.data.repository

import androidx.lifecycle.LiveData
import com.example.criminalintent.data.local.CrimeDao
import com.example.criminalintent.data.model.Crime
import java.io.File
import java.util.concurrent.Executors

class CrimeRepository private constructor(
    private val crimeDao: CrimeDao,
    private val filesDir: File?
) {
    private val executor = Executors.newSingleThreadExecutor()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: Int): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    fun addOrUpdate(crime: Crime) {
        executor.execute {
            crimeDao.addOrUpdate(crime)
        }
    }

    fun getPhotoFile(crime: Crime) = File(filesDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null
        const val DATABASE_NAME = "crime-database"

        fun initialize(crimeDao: CrimeDao, filesDir: File) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(crimeDao, filesDir)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("Crime repository must be initialized")
        }
    }
}
