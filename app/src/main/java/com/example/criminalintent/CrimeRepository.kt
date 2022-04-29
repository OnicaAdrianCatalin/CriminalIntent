package com.example.criminalintent

import androidx.lifecycle.LiveData
import com.example.criminalintent.database.CrimeDao
import java.util.*

class CrimeRepository private constructor(private val crimeDao: CrimeDao) {

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

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
