package com.example.criminalintent.data.repository

import androidx.lifecycle.LiveData
import com.example.criminalintent.data.local.CrimeDao
import com.example.criminalintent.data.model.Crime
import java.io.File
import java.util.concurrent.Executors

class CrimeRepositoryImpl(
    private val crimeDao: CrimeDao,
    private val filesDir: File
) : CrimeRepository {
    private val executor = Executors.newSingleThreadExecutor()

    override fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    override fun getCrime(id: Int): LiveData<Crime?> = crimeDao.getCrime(id)

    override fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    override fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    override fun addOrUpdate(crime: Crime) {
        executor.execute {
            crimeDao.addOrUpdate(crime)
        }
    }

    override fun getPhotoFile(fileName: String) =
        File(filesDir, fileName)

    companion object {
        const val DATABASE_NAME = "crime-database"
    }
}
