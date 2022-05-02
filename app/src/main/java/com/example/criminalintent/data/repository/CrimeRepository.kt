package com.example.criminalintent.data.repository

import androidx.lifecycle.LiveData
import com.example.criminalintent.data.local.CrimeDao
import com.example.criminalintent.data.model.Crime
import java.util.concurrent.Executors

class CrimeRepository private constructor(private val crimeDao: CrimeDao) {

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

    companion object {
        private var INSTANCE: CrimeRepository? = null
        const val DATABASE_NAME = "crime-database"

        fun initialize(crimeDao: CrimeDao) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(crimeDao)
            }
        }

        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("Crime repository must be initialized")
        }
    }
}
