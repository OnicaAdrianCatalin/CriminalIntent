package com.example.criminalintent.data.local

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.criminalintent.data.model.Crime

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id =(:id)")
    fun getCrime(id: Int): LiveData<Crime?>

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)

    fun upsertCrime(crime: Crime) {
        try {
            addCrime(crime)
        } catch (exception: SQLiteConstraintException) {
            Log.e("exception", "upsert: ${exception.message}")
            updateCrime(crime)
        }
    }
}
